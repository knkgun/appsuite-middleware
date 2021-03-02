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

package com.openexchange.dav;

import java.io.IOException;
import java.time.Duration;
import java.util.HashSet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import com.openexchange.ajax.Client;
import com.openexchange.ajax.requesthandler.oauth.OAuthConstants;
import com.openexchange.exception.OXException;
import com.openexchange.framework.request.RequestContextHolder;
import com.openexchange.java.Strings;
import com.openexchange.java.util.HttpStatusFamily;
import com.openexchange.log.LogProperties;
import com.openexchange.login.Interface;
import com.openexchange.login.LoginRequest;
import com.openexchange.oauth.provider.resourceserver.OAuthAccess;
import com.openexchange.oauth.provider.resourceserver.scope.Scope;
import com.openexchange.servlet.Constants;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.CountingHttpServletRequest;
import com.openexchange.tools.servlet.http.Authorization.Credentials;
import com.openexchange.tools.servlet.ratelimit.RateLimitedException;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.tools.webdav.AllowAsteriskAsSeparatorCustomizer;
import com.openexchange.tools.webdav.LoginCustomizer;
import com.openexchange.tools.webdav.OXServlet;
import com.openexchange.tools.webdav.WebDAVRequestContext;
import com.openexchange.webdav.action.WebdavAction;
import com.openexchange.webdav.protocol.WebdavMethod;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;

/**
 * {@link DAVServlet}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.1
 */
public class DAVServlet extends OXServlet {

    private static final long serialVersionUID = 9124758398726588039L;
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DAVServlet.class);
    private static final LoginCustomizer ALLOW_ASTERISK_LOGIN_CUSTOMIZER = new AllowAsteriskAsSeparatorCustomizer();

    /** The required scope to access CalDAV/CardDAV related endpoints for restricted sessions (authenticated with app-specific passwords) */
    private static final String RESTRICTED_SCOPE_DAV = "dav";

    protected final DAVPerformer performer;
    private final Interface interfaze;

    /**
     * Initializes a new {@link DAVServlet}.
     *
     * @param performer The performer to use
     * @param interfaze The login interface
     */
    public DAVServlet(DAVPerformer performer, Interface interfaze) {
        super();
        this.performer = performer;
        this.interfaze = interfaze;
    }

    @Override
    protected LoginRequest parseLoginRequest(Credentials credentials, HttpServletRequest request) {
        String userAgent = request.getHeader("user-agent");
        DAVUserAgent davUserAgent = DAVUserAgent.parse(userAgent);
        Interface interfaze = getInterface(davUserAgent);
        Client client = Interface.CARDDAV.equals(interfaze) ? Client.CARDDAV : Client.CALDAV;
        LoginRequest loginRequest = new LoginRequestImpl(request, credentials.getLogin(), credentials.getPassword(), interfaze, client.getClientId(), null, userAgent);
        return ALLOW_ASTERISK_LOGIN_CUSTOMIZER.modifyLogin(loginRequest);
    }

    @Override
    protected Interface getInterface() {
        return interfaze;
    }

    private Interface getInterface(DAVUserAgent userAgent) {
        switch (userAgent) {
            case CARDDAV_SYNC:
            case MAC_CONTACTS:
                return Interface.CARDDAV;
            case UNKNOWN:
                return getInterface();
            default:
                return Interface.CALDAV;
        }
    }

    @Override
    protected boolean useCookies() {
        return false;
    }

    @Override
    protected boolean allowOAuthAccess() {
        return true;
    }

    /**
     * Gets a value indicating whether authentication is required for a specific HTTP request.
     *
     * @param request The request to check
     * @return <code>true</code>, if authentication is required, <code>false</code>, otherwise
     */
    protected boolean needsAuthentication(HttpServletRequest request) {
        return false == WebdavMethod.TRACE.toString().equals(request.getMethod());
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        long start = System.currentTimeMillis();
        /*
         * ensure to have a HTTP session when using cookies
         */
        if (useCookies()) {
            request.getSession(true);
        }
        /*
         * authenticate request
         */
        if (useHttpAuth() && needsAuthentication(request) && false == authenticate(request, response)) {
            return;
        }
        Session session = getSession(request);
        incrementRequests();
        RequestContextHolder.set(new WebDAVRequestContext(request, session));
        LogProperties.putSessionProperties(session);
        WebdavMethod method = null;
        try {
            /*
             * wrap into counting request to check rate limit
             */
            try {
                request = new CountingHttpServletRequest(request);
            } catch (RateLimitedException e) {
                // Mark optional HTTP session as rate-limited
                HttpSession optionalHttpSession = request.getSession(false);
                if (optionalHttpSession != null) {
                    optionalHttpSession.setAttribute(Constants.HTTP_SESSION_ATTR_RATE_LIMITED, Boolean.TRUE);
                }
                // Send error response
                e.send(response);
                return;
            }
            /*
             * get targeted action
             */
            try {
                method = WebdavMethod.valueOf(WebdavMethod.class, request.getMethod());
            } catch (IllegalArgumentException | NullPointerException e) {
                response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                return;
            }
            WebdavAction action = performer.getAction(method);
            if (null == action) {
                response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                return;
            }
            /*
             * perform
             */
            doIt(request, response, method, session);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("", e);
            throw new ServletException(e.getMessage(), e);
        } finally {
            LogProperties.removeSessionProperties();
            RequestContextHolder.reset();
            decrementRequests();
            recordMetric(Duration.ofMillis(System.currentTimeMillis() - start), method == null ? "INVALID": method.name(), response.getStatus());
        }
    }

    /**
     * Records the duration
     *
     * @param duration The duration to record
     * @param method The used method
     * @param statusCode The response status
     */
    private void recordMetric(Duration duration, String method, int statusCode) {
        String res = performer.getURLPrefix();
        if(res.startsWith("/")) {
            res = res.substring(1);
        }
        if(res.endsWith("/")) {
            res = res.substring(0, res.length() - 1);
        }
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
             .tags("interface", interfaze.name(), "resource", res, "method", method, "status", status)
             .register(Metrics.globalRegistry).record(duration);
        // @formatter:on
    }

    private void doIt(HttpServletRequest request, HttpServletResponse response, WebdavMethod method, Session session) {
        ServerSession serverSession = null;
        try {
            serverSession = ServerSessionAdapter.valueOf(session);
        } catch (OXException e) {
            LOG.error(e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        if (false == checkPermission(request, method, serverSession)) {
            addUnauthorizedHeader(response);
            removeSession(session.getSessionID());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        performer.doIt(request, response, method, serverSession);
    }

    /**
     * Performs additional permission checks for executing a specific request after authentication took place.
     * <p/>
     * Override if applicable.
     *
     * @param request the HTTP request
     * @param method The derived WebDAV method for the request
     * @param session The session to check permissions for
     * @return <code>true</code> if permissions are sufficient, <code>false</code>, otherwise
     */
    protected boolean checkPermission(HttpServletRequest request, WebdavMethod method, @SuppressWarnings("unused") ServerSession session) {
        /*
         * check that either "caldav" or "carddav" scope is available when authenticated through OAuth
         */
        OAuthAccess oAuthAccess = (OAuthAccess) request.getAttribute(OAuthConstants.PARAM_OAUTH_ACCESS);
        if (null != oAuthAccess) {
            Scope scope = oAuthAccess.getScope();
            return scope.has(DAVOAuthScope.CALDAV.getScope()) || scope.has(DAVOAuthScope.CARDDAV.getScope());
        }
        /*
         * check that the general "dav" scope is available when session is restricted (authenticated through app-specific password)
         */
        String restrictedScopes = (String) session.getParameter(Session.PARAM_RESTRICTED);
        if (null != restrictedScopes) {
            return Strings.splitByComma(restrictedScopes, new HashSet<String>()).contains(RESTRICTED_SCOPE_DAV);
        }
        /*
         * assume regularly authenticated *DAV session, otherwise
         */
        return true;
    }

}
