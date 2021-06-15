/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.push.dovecot;

import static com.openexchange.java.Autoboxing.I;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;
import com.openexchange.dovecot.doveadm.client.DoveAdmClient;
import com.openexchange.exception.OXException;
import com.openexchange.hazelcast.Hazelcasts;
import com.openexchange.java.Streams;
import com.openexchange.lock.AccessControl;
import com.openexchange.lock.LockService;
import com.openexchange.lock.ReentrantLockAccessControl;
import com.openexchange.push.PushListener;
import com.openexchange.push.PushListenerService;
import com.openexchange.push.PushManagerExtendedService;
import com.openexchange.push.PushUser;
import com.openexchange.push.PushUserInfo;
import com.openexchange.push.PushUtility;
import com.openexchange.push.dovecot.locking.DovecotPushClusterLock;
import com.openexchange.push.dovecot.locking.SessionInfo;
import com.openexchange.push.dovecot.registration.RegistrationContext;
import com.openexchange.push.dovecot.registration.RegistrationContext.DoveAdmClientProvider;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.ObfuscatorService;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessionMatcher;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.sessionstorage.hazelcast.serialization.PortableMultipleActiveSessionRemoteLookUp;
import com.openexchange.sessionstorage.hazelcast.serialization.PortableSession;
import com.openexchange.sessionstorage.hazelcast.serialization.PortableSessionCollection;


/**
 * {@link DovecotPushManagerService}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.2
 */
public class DovecotPushManagerService implements PushManagerExtendedService {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(DovecotPushManagerService.class);

    private static enum StopResult {
        NONE, RECONNECTED, RECONNECTED_AS_PERMANENT, STOPPED;
    }

    private static volatile DovecotPushManagerService instance;

    /**
     * Initializes the push manager instance.
     *
     * @param endPoint The HTTP end-point
     * @param clusterLock The cluster lock
     * @param services The service look-up
     * @return The new instance
     * @throws OXException If initialization fails
     */
    public static DovecotPushManagerService newInstance(DovecotPushClusterLock clusterLock, ServiceLookup services) {
        DovecotPushManagerService newi = new DovecotPushManagerService(clusterLock, services);
        instance = newi;
        return newi;
    }

    /**
     * Drops the instance.
     */
    public static void dropInstance() {
        DovecotPushManagerService pushManager = instance;
        if (null != pushManager) {
            instance = null;
            pushManager.stopAll();
        }
    }

    /**
     * Gets the instance.
     *
     * @return The instance
     */
    public static DovecotPushManagerService getInstance() {
        return instance;
    }

    // --------------------------------------------------------------------------------------------------------------------------------

    private final String name;
    private final ServiceLookup services;
    private final Map<SimpleKey, DovecotPushListener> listeners;
    private final DovecotPushClusterLock clusterLock;
    private final AccessControl globalLock;

    /**
     * Initializes a new {@link DovecotPushManagerService}.
     */
    private DovecotPushManagerService(DovecotPushClusterLock clusterLock, ServiceLookup services) {
        super();
        name = "Dovecot Push Manager";
        this.services = services;
        this.clusterLock = clusterLock;
        listeners = new ConcurrentHashMap<SimpleKey, DovecotPushListener>(512, 0.9F, 1);

        // The fall-back lock
        globalLock = new ReentrantLockAccessControl();
    }

    @Override
    public String toString() {
        return name;
    }

    private AccessControl getlockFor(int userId, int contextId) {
        LockService lockService = services.getOptionalService(LockService.class);
        if (null == lockService) {
            return globalLock;
        }

        try {
            return lockService.getAccessControlFor("dovecotpush", 1, userId, contextId);
        } catch (Exception e) {
            LOGGER.warn("Failed to acquire lock for user {} in context {}. Using global lock instead.", I(userId), I(contextId), e);
            return globalLock;
        }
    }

    @Override
    public boolean supportsPermanentListeners() {
        return true;
    }

    /**
     * Tries to look-up another valid session and injects a new listener for it (discarding the existing one bound to given <code>oldSession</code>)
     *
     * @param oldSession The expired/outdated session
     * @return The new listener or <code>null</code>
     * @throws OXException If operation fails
     */
    public DovecotPushListener injectAnotherListenerFor(Session oldSession) {
        int contextId = oldSession.getContextId();
        int userId = oldSession.getUserId();

        // Prefer permanent listener prior to performing look-up for another valid session
        if (hasPermanentPush(userId, contextId)) {
            try {
                DoveAdmClient doveAdmClient = services.getOptionalService(DoveAdmClient.class);
                RegistrationContext registrationContext;
                if (null == doveAdmClient) {
                    Session session = generateSessionFor(userId, contextId);
                    registrationContext = RegistrationContext.createSessionContext(session);
                } else {
                    registrationContext = RegistrationContext.createDoveAdmClientContext(userId, contextId, new ServiceLookupDoveAdmClientProvider(services));
                }

                return injectAnotherListenerUsing(registrationContext, true);
            } catch (OXException e) {
                // Failed to inject a permanent listener
            }
        }

        Session session = lookUpSessionFor(userId, contextId, oldSession);
        if (null == session) {
            // No push-capable session
            return null;
        }

        DoveAdmClient doveAdmClient = services.getOptionalService(DoveAdmClient.class);
        RegistrationContext registrationContext = null == doveAdmClient ? RegistrationContext.createSessionContext(session) : RegistrationContext.createDoveAdmClientContext(userId, contextId, new ServiceLookupDoveAdmClientProvider(services));
        return injectAnotherListenerUsing(registrationContext, false);
    }

    /**
     * Looks-up a push-capable session for specified user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param optOldSession The optional old session (which expired)
     * @return A push-capable session or <code>null</code>
     */
    public Session lookUpSessionFor(int userId, int contextId, Session optOldSession) {
        // Look-up sessions
        SessiondService sessiondService = services.getService(SessiondService.class);
        if (null != sessiondService) {
            final String oldSessionId = null == optOldSession ? null : optOldSession.getSessionID();

            // Query local ones
            SessionMatcher matcher = new SessionMatcher() {

                @Override
                public Set<Flag> flags() {
                    return SessionMatcher.ALL_FLAGS;
                }

                @Override
                public boolean accepts(Session session) {
                    return (oldSessionId == null || !oldSessionId.equals(session.getSessionID())) && PushUtility.allowedClient(session.getClient(), session, true);
                }
            };
            Session anotherActiveSession = sessiondService.findFirstMatchingSessionForUser(userId, contextId, matcher);
            if (anotherActiveSession != null) {
                return anotherActiveSession;
            }

            // Look-up remote sessions, too, if possible
            Session session = lookUpRemoteSessionFor(userId, contextId, optOldSession);
            if (session != null) {
                Session ses = sessiondService.getSession(session.getSessionID());
                if (ses != null) {
                    return ses;
                }
            }
        }

        return null;
    }

    private Session lookUpRemoteSessionFor(int userId, int contextId, Session optOldSession) {
        HazelcastInstance hzInstance = services.getOptionalService(HazelcastInstance.class);
        final ObfuscatorService obfuscatorService = services.getOptionalService(ObfuscatorService.class);
        if (null == hzInstance || null == obfuscatorService) {
            return null;
        }

        // Determine other cluster members
        Set<Member> otherMembers = Hazelcasts.getRemoteMembers(hzInstance);
        if (otherMembers.isEmpty()) {
            return null;
        }

        final String oldSessionId = null == optOldSession ? null : optOldSession.getSessionID();
        Hazelcasts.Filter<PortableSessionCollection, PortableSession> filter = new Hazelcasts.Filter<PortableSessionCollection, PortableSession>() {

            @Override
            public PortableSession accept(PortableSessionCollection portableSessionCollection) {
                PortableSession[] portableSessions = portableSessionCollection.getSessions();
                if (null != portableSessions) {
                    for (PortableSession portableSession : portableSessions) {
                        if ((null == oldSessionId || false == oldSessionId.equals(portableSession.getSessionID())) && PushUtility.allowedClient(portableSession.getClient(), portableSession, true)) {
                            portableSession.setPassword(obfuscatorService.unobfuscate(portableSession.getPassword()));
                            return portableSession;
                        }
                    }
                }
                return null;
            }
        };
        try {
            return Hazelcasts.executeByMembersAndFilter(new PortableMultipleActiveSessionRemoteLookUp(userId, contextId), otherMembers, hzInstance.getExecutorService("default"), filter);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw ((RuntimeException) cause);
            }
            if (cause instanceof Error) {
                throw (Error) cause;
            }
            throw new IllegalStateException("Not unchecked", cause);
        }
    }

    /**
     * Tries to look-up another valid session and injects a new listener for it (discarding the existing one bound to given <code>oldSession</code>)
     *
     * @param newSession The new session to use
     * @param permanent <code>true</code> if permanent; otherwise <code>false</code>
     * @return The new listener or <code>null</code>
     * @throws OXException If operation fails
     */
    public DovecotPushListener injectAnotherListenerUsing(RegistrationContext registrationContext, boolean permanent) {
        DovecotPushListener listener = new DovecotPushListener(registrationContext, permanent, this, services);
        // Replace old/existing one
        listeners.put(SimpleKey.valueOf(registrationContext.getUserId(), registrationContext.getContextId()), listener);
        return listener;
    }

    private void stopAll() {
        for (Map.Entry<SimpleKey, DovecotPushListener> entry : listeners.entrySet()) {
            try {
                entry.getValue().unregister(false);
            } catch (Exception e) {
                SimpleKey key = entry.getKey();
                LOGGER.warn("Failed to stop listener for user {} in context {}", I(key.userId), I(key.contextId));
            }
        }
        listeners.clear();
    }

    /**
     * Stops the listener associated with given user.
     *
     * @param tryToReconnect <code>true</code> to signal that a reconnect using another sessions should be performed; otherwise <code>false</code>
     * @param stopIfPermanent <code>true</code> to signal that current listener is supposed to be stopped even though it might be associated with a permanent push registration; otherwise <code>false</code>
     * @param userId The user identifier
     * @param contextId The corresponding context identifier
     * @return The stop result
     * @throws OXException If unregistration attempt fails
     */
    public StopResult stopListener(boolean tryToReconnect, boolean stopIfPermanent, int userId, int contextId) throws OXException {
        AccessControl lock = getlockFor(userId, contextId);
        Runnable cleanUpTask = null;
        try {
            lock.acquireGrant();
            SimpleKey key = SimpleKey.valueOf(userId, contextId);
            DovecotPushListener listener = listeners.get(key);
            if (null != listener) {
                if (!stopIfPermanent && listener.isPermanent()) {
                    return StopResult.NONE;
                }

                // Remove from map
                listeners.remove(key);

                boolean tryRecon = tryToReconnect || (!listener.isPermanent() && hasPermanentPush(userId, contextId));
                cleanUpTask = listener.unregister(tryRecon);
                if (null != cleanUpTask) {
                    return StopResult.STOPPED;
                }

                DovecotPushListener newListener = listeners.get(key);
                return (null != newListener && newListener.isPermanent()) ? StopResult.RECONNECTED_AS_PERMANENT : StopResult.RECONNECTED;
            }
            return StopResult.NONE;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        } finally {
            Streams.close(lock);
            if (null != cleanUpTask) {
                cleanUpTask.run();
            }
        }
    }

    /**
     * Releases the possibly held lock for given user.
     *
     * @param sessionInfo The associated session
     * @throws OXException If release operation fails
     */
    public void releaseLock(SessionInfo sessionInfo) throws OXException {
        clusterLock.releaseLock(sessionInfo);
    }

    /**
     * Refreshes the lock for given user.
     *
     * @param sessionInfo The associated session
     * @throws OXException If refresh operation fails
     */
    public void refreshLock(SessionInfo sessionInfo) throws OXException {
        clusterLock.refreshLock(sessionInfo);
    }

    static class ServiceLookupDoveAdmClientProvider implements DoveAdmClientProvider {

        private final ServiceLookup services;

        /**
         * Initializes a new {@link DovecotPushManagerService.ServiceLookupDoveAdmClientProvider}.
         */
        ServiceLookupDoveAdmClientProvider(ServiceLookup services) {
            super();
            this.services = services;
        }

        @Override
        public DoveAdmClient getDoveAdmClient() throws OXException {
            DoveAdmClient doveAdmClient = services.getOptionalService(DoveAdmClient.class);
            if (null == doveAdmClient) {
                throw ServiceExceptionCode.absentService(DoveAdmClient.class);
            }
            return doveAdmClient;
        }
    }

    // --------------------------------------------------------------------------------------------------------------------------

    @Override
    public PushListener startListener(Session session) throws OXException {
        if (null == session) {
            return null;
        }
        int contextId = session.getContextId();
        int userId = session.getUserId();
        DoveAdmClient doveAdmClient = services.getOptionalService(DoveAdmClient.class);
        RegistrationContext registrationContext = null == doveAdmClient ? RegistrationContext.createSessionContext(session) : RegistrationContext.createDoveAdmClientContext(userId, contextId, new ServiceLookupDoveAdmClientProvider(services));

        SessionInfo sessionInfo = new SessionInfo(registrationContext, false);
        if (clusterLock.acquireLock(sessionInfo)) {
            // Locked...
            boolean unlock = true;
            try {
                boolean removeListener = false;
                SimpleKey key = SimpleKey.valueOf(userId, contextId);
                AccessControl lock = getlockFor(userId, contextId);
                try {
                    lock.acquireGrant();
                    if (false == listeners.containsKey(key)) {
                        DovecotPushListener listener = new DovecotPushListener(registrationContext, false, this, services);
                        listeners.put(key, listener);
                        removeListener = true;
                        String reason = listener.initateRegistration(true);
                        if (null == reason) {
                            removeListener = false;
                            unlock = false;
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("Started Dovecot listener for user {} in context {} with session {} ({})", I(userId), I(contextId), session.getSessionID(), session.getClient(), new Throwable("Dovecot start listener trace"));
                            } else {
                                LOGGER.info("Started Dovecot listener for user {} in context {} with session {} ({})", I(userId), I(contextId), session.getSessionID(), session.getClient());
                            }
                            return listener;
                        }

                        // Registration failed
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Could not register Dovecot listener for user {} in context {} with session {} ({})", I(userId), I(contextId), session.getSessionID(), session.getClient(), new Throwable("Dovecot start listener trace"));
                        } else {
                            LOGGER.info("Could not register Dovecot listener for user {} in context {} with session {} ({})", I(userId), I(contextId), session.getSessionID(), session.getClient());
                        }
                    } else {
                        // Already running for session user
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Did not start Dovecot listener for user {} in context {} with session {} ({}) as there is already an associated listener", I(userId), I(contextId), session.getSessionID(), session.getClient(), new Throwable("Dovecot start listener trace"));
                        } else {
                            LOGGER.info("Did not start Dovecot listener for user {} in context {} with session {} ({}) as there is already an associated listener", I(userId), I(contextId), session.getSessionID(), session.getClient());
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new OXException(e);
                } finally {
                    if (removeListener) {
                        listeners.remove(key);
                    }
                    Streams.close(lock);
                }
            } finally {
                if (unlock) {
                    releaseLock(sessionInfo);
                }
            }
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Could not acquire lock to start Dovecot listener for user {} in context {} with session {} ({}) as there is already an associated listener", I(userId), I(contextId), session.getSessionID(), session.getClient(), new Throwable("Dovecot start listener trace"));
            } else {
                LOGGER.info("Could not acquire lock to start Dovecot listener for user {} in context {} with session {} ({}) as there is already an associated listener", I(userId), I(contextId), session.getSessionID(), session.getClient());
            }
            new DovecotPushListener(registrationContext, false, this, services).initateRegistration(false);
        }

        // No listener registered for given session
        return null;
    }

    @Override
    public boolean stopListener(Session session) throws OXException {
        if (null == session) {
            return false;
        }

        StopResult stopResult = stopListener(true, false, session.getUserId(), session.getContextId());
        switch (stopResult) {
        case RECONNECTED:
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Reconnected Dovecot listener for user {} in context {} using another session", I(session.getUserId()), I(session.getContextId()), new Throwable("Dovecot stop listener trace"));
            } else {
                LOGGER.info("Reconnected Dovecot listener for user {} in context {} using another session", I(session.getUserId()), I(session.getContextId()));
            }
            return true;
        case RECONNECTED_AS_PERMANENT:
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Reconnected as permanent Dovecot listener for user {} in context {}", I(session.getUserId()), I(session.getContextId()), new Throwable("Dovecot stop listener trace"));
            } else {
                LOGGER.info("Reconnected as permanent Dovecot listener for user {} in context {}", I(session.getUserId()), I(session.getContextId()));
            }
            return true;
        case STOPPED:
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Stopped Dovecot listener for user {} in context {} with session {}", I(session.getUserId()), I(session.getContextId()), session.getSessionID(), new Throwable("Dovecot stop listener trace"));
            } else {
                LOGGER.info("Stopped Dovecot listener for user {} in context {} with session {}", I(session.getUserId()), I(session.getContextId()), session.getSessionID());
            }
            return true;
        default:
            break;
        }

        return false;
    }

    // --------------------------------------------------------------------------------------------------------------------------

    private boolean hasPermanentPush(int userId, int contextId) {
        try {
            PushListenerService pushListenerService = services.getService(PushListenerService.class);
            return pushListenerService.hasRegistration(new PushUser(userId, contextId));
        } catch (Exception e) {
            LOGGER.warn("Failed to check for push registration for user {} in context {}", I(userId), I(contextId), e);
            return false;
        }
    }

    private Session generateSessionFor(int userId, int contextId) throws OXException {
        return generateSessionFor(new PushUser(userId, contextId));
    }

    private Session generateSessionFor(PushUser pushUser) throws OXException {
        PushListenerService pushListenerService = services.getService(PushListenerService.class);
        return pushListenerService.generateSessionFor(pushUser);
    }

    @Override
    public PushListener startPermanentListener(PushUser pushUser) throws OXException {
        if (null == pushUser) {
            return null;
        }

        int contextId = pushUser.getContextId();
        int userId = pushUser.getUserId();
        DoveAdmClient doveAdmClient = services.getOptionalService(DoveAdmClient.class);
        RegistrationContext registrationContext;
        if (null == doveAdmClient) {
            Session session = generateSessionFor(pushUser);
            registrationContext = RegistrationContext.createSessionContext(session);
        } else {
            registrationContext = RegistrationContext.createDoveAdmClientContext(userId, contextId, new ServiceLookupDoveAdmClientProvider(services));
        }

        SessionInfo sessionInfo = new SessionInfo(registrationContext, true);
        if (clusterLock.acquireLock(sessionInfo)) {
            // Locked...
            boolean unlock = true;
            try {
                boolean removeListener = false;
                SimpleKey key = SimpleKey.valueOf(userId, contextId);
                AccessControl lock = getlockFor(userId, contextId);
                try {
                    lock.acquireGrant();
                    DovecotPushListener current = listeners.get(key);
                    if (null == current) {
                        DovecotPushListener listener = new DovecotPushListener(registrationContext, true, this, services);
                        listeners.put(key, listener);
                        removeListener = true;
                        String reason = listener.initateRegistration(true);
                        if (null == reason) {
                            removeListener = false;
                            unlock = false;
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("Started permanent Dovecot listener for user {} in context {}", I(userId), I(contextId), new Throwable("Dovecot start permanent listener trace"));
                            } else {
                                LOGGER.info("Started permanent Dovecot listener for user {} in context {}", I(userId), I(contextId));
                            }
                            return listener;
                        }

                        // Registration failed
                        LOGGER.info("Could not register permanent Dovecot listener for user {} in context {}", I(userId), I(contextId));
                    } else if (!current.isPermanent()) {
                        // Cancel current & replace
                        current.unregister(false);
                        DovecotPushListener listener = new DovecotPushListener(registrationContext, true, this, services);
                        listeners.put(key, listener);
                        removeListener = true;
                        String reason = listener.initateRegistration(true);
                        if (null == reason) {
                            removeListener = false;
                            unlock = false;
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("Started permanent Dovecot listener for user {} in context {}", I(userId), I(contextId), new Throwable("Dovecot start permanent listener trace"));
                            } else {
                                LOGGER.info("Started permanent Dovecot listener for user {} in context {}", I(userId), I(contextId));
                            }
                            return listener;
                        }

                        // Registration failed
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Could not register permanent Dovecot listener for user {} in context {}", I(userId), I(contextId), new Throwable("Dovecot start permanent listener trace"));
                        } else {
                            LOGGER.info("Could not register permanent Dovecot listener for user {} in context {}", I(userId), I(contextId));
                        }
                    } else {
                        // Already running for session user
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Did not start permanent Dovecot listener for user {} in context {} as there is already an associated listener", I(userId), I(contextId), new Throwable("Dovecot start permanent listener trace"));
                        } else {
                            LOGGER.info("Did not start permanent Dovecot listener for user {} in context {} as there is already an associated listener", I(userId), I(contextId));
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new OXException(e);
                } finally {
                    if (removeListener) {
                        listeners.remove(key);
                    }
                    Streams.close(lock);
                }
            } finally {
                if (unlock) {
                    releaseLock(sessionInfo);
                }
            }
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Could not acquire lock to start permanent Dovecot listener for user {} in context {} as there is already an associated listener", I(userId), I(contextId), new Throwable("Dovecot start permanent listener trace"));
            } else {
                LOGGER.info("Could not acquire lock to start permanent Dovecot listener for user {} in context {} as there is already an associated listener", I(userId), I(contextId));
            }
            new DovecotPushListener(registrationContext, true, this, services).initateRegistration(false);
        }

        // No listener registered for given session
        return null;
    }

    @Override
    public boolean stopPermanentListener(PushUser pushUser, boolean tryToReconnect) throws OXException {
        if (null == pushUser) {
            return false;
        }

        StopResult stopResult = stopListener(tryToReconnect, true, pushUser.getUserId(), pushUser.getContextId());
        switch (stopResult) {
        case RECONNECTED:
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Reconnected permanent Dovecot listener for user {} in context {}", I(pushUser.getUserId()), I(pushUser.getContextId()), new Throwable("Dovecot stop permanent listener trace"));
            } else {
                LOGGER.info("Reconnected permanent Dovecot listener for user {} in context {}", I(pushUser.getUserId()), I(pushUser.getContextId()));
            }
            return true;
        case STOPPED:
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Stopped permanent Dovecot listener for user {} in context {}", I(pushUser.getUserId()), I(pushUser.getContextId()), new Throwable("Dovecot stop permanent listener trace"));
            } else {
                LOGGER.info("Stopped permanent Dovecot listener for user {} in context {}", I(pushUser.getUserId()), I(pushUser.getContextId()));
            }
            return true;
        default:
            break;
        }

        return false;
    }

    @Override
    public List<PushUserInfo> getAvailablePushUsers() {
        List<PushUserInfo> l = new LinkedList<PushUserInfo>();
        for (Map.Entry<SimpleKey, DovecotPushListener> entry : listeners.entrySet()) {
            SimpleKey key = entry.getKey();
            l.add(new PushUserInfo(new PushUser(key.userId, key.contextId), entry.getValue().isPermanent()));
        }
        return l;
    }

}
