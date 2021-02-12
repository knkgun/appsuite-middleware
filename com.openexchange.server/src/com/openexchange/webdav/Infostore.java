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

package com.openexchange.webdav;

import java.io.IOException;
import java.time.Duration;
import java.util.HashSet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.java.Strings;
import com.openexchange.java.util.HttpStatusFamily;
import com.openexchange.login.Interface;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.tools.webdav.OXServlet;
import com.openexchange.webdav.protocol.WebdavMethod;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;


/**
 * {@link Infostore} - The WebDAV/XML servlet for infostore module.
 */
public class Infostore extends OXServlet {

    private static final long serialVersionUID = -2064098724675986123L;

    /** The required scope to access read-only WebDAV-related endpoints for restricted sessions (authenticated with app-specific passwords) */
    private static final String RESTRICTED_SCOPE_WEBDAV_READ = "read_webdav";

    /** The required scope to access read-only WebDAV-related endpoints for restricted sessions (authenticated with app-specific passwords) */
    private static final String RESTRICTED_SCOPE_WEBDAV_WRITE = "write_webdav";

    @Override
    protected Interface getInterface() {
        return Interface.WEBDAV_INFOSTORE;
    }

    @Override
    protected boolean useCookies() {
        return false;
    }

    @Override
    protected void doCopy(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        doIt(req, resp, WebdavMethod.COPY);
    }

    @Override
    protected void doLock(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        doIt(req, resp, WebdavMethod.LOCK);
    }

    @Override
    protected void doMkCol(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        doIt(req, resp, WebdavMethod.MKCOL);
    }

    @Override
    protected void doMove(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        doIt(req, resp, WebdavMethod.MOVE);
    }

    @Override
    protected void doOptions(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        doIt(req, resp, WebdavMethod.OPTIONS);
    }

    @Override
    protected void doPropFind(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        doIt(req, resp, WebdavMethod.PROPFIND);
    }

    @Override
    protected void doPropPatch(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        doIt(req, resp, WebdavMethod.PROPPATCH);
    }

    @Override
    protected void doUnLock(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        doIt(req, resp, WebdavMethod.UNLOCK);
    }

    @Override
    protected void doDelete(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        doIt(req, resp, WebdavMethod.DELETE);
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        doIt(req, resp, WebdavMethod.GET);
    }

    @Override
    protected void doHead(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        doIt(req, resp, WebdavMethod.HEAD);
    }

    @Override
    protected void doPut(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        doIt(req, resp, WebdavMethod.PUT);
    }

    @Override
    protected void doTrace(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        doIt(req, resp, WebdavMethod.TRACE);
    }

    /**
     * Performs the infostore action
     *
     * @param req The request
     * @param resp The response
     * @param action The action to perform
     * @throws ServletException If an error is occurred
     * @throws IOException if an I/O error is occurred
     */
    private void doIt(final HttpServletRequest req, final HttpServletResponse resp, final WebdavMethod method) {
        long start = System.nanoTime();
        try {
            ServerSession session;
            try {
                session = ServerSessionAdapter.valueOf(getSession(req));
            } catch (OXException e) {
                org.slf4j.LoggerFactory.getLogger(Infostore.class).warn("Unexpected error getting session associated to request", e);
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }
            final UserConfiguration uc = UserConfigurationStorage.getInstance().getUserConfigurationSafe(session.getUserId(), session.getContext());
            if (!uc.hasWebDAV() || !uc.hasInfostore()) {
                resp.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
                return;
            }
            if (!checkSessionAuthorized(session, method)) {
                removeSession(session.getSessionID());
                addUnauthorizedHeader(resp);
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            InfostorePerformer.getInstance().doIt(req, resp, method, session);
        } finally {
            recordMetric(Duration.ofNanos(System.nanoTime() - start), req.getMethod(), resp.getStatus());
        }
    }

    /**
     * Records the duration
     *
     * @param duration The duration to record
     * @param method The used method
     * @param status The response status
     */
    private void recordMetric(Duration duration, String method, int statusCode) {
        String status = HttpStatusFamily.SUCCESSFUL.equals(HttpStatusFamily.of(statusCode)) ? "OK" : String.valueOf(statusCode);

        // @formatter:off
        Timer.builder("appsuite.webdav.requests")
             .description("Records the timing of webdav requests")
             .serviceLevelObjectives(
                 Duration.ofMillis(50),
                 Duration.ofMillis(100),
                 Duration.ofMillis(150),
                 Duration.ofMillis(200),
                 Duration.ofMillis(250),
                 Duration.ofMillis(300),
                 Duration.ofMillis(400),
                 Duration.ofMillis(500),
                 Duration.ofMillis(750),
                 Duration.ofSeconds(1),
                 Duration.ofSeconds(2),
                 Duration.ofSeconds(5),
                 Duration.ofSeconds(10),
                 Duration.ofSeconds(30),
                 Duration.ofMinutes(1))
             .tags("interface", "infostore", "resource", "infostore", "method", method, "status", status)
             .register(Metrics.globalRegistry).record(duration);
        // @formatter:on
    }

    /**
     * Check restricted session. If present, confirm webdav permission
     *
     * @param session The session
     * @return true if allowed, false if should be prevented
     */
    private boolean checkSessionAuthorized(ServerSession session, WebdavMethod method) {
        /*
         * check that a "webdav" scope appropriate for the method is available when session is restricted (authenticated through app-specific password)
         */
        String restrictedScopes = (String) session.getParameter(Session.PARAM_RESTRICTED);
        if (null != restrictedScopes) {
            String requiredScope = null != method && method.isReadOnly() ? RESTRICTED_SCOPE_WEBDAV_READ : RESTRICTED_SCOPE_WEBDAV_WRITE;
            return Strings.splitByComma(restrictedScopes, new HashSet<String>()).contains(requiredScope);
        }
        /*
         * assume regularly authenticated *DAV session, otherwise
         */
        return true;
    }
}
