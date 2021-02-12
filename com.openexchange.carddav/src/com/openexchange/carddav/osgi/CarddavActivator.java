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

package com.openexchange.carddav.osgi;

import static com.openexchange.dav.DAVTools.getExternalPath;
import static com.openexchange.dav.DAVTools.getInternalPath;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.capabilities.CapabilitySet;
import com.openexchange.carddav.Tools;
import com.openexchange.carddav.photos.PhotoPerformer;
import com.openexchange.carddav.servlet.CardDAV;
import com.openexchange.carddav.servlet.CarddavPerformer;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.contact.ContactService;
import com.openexchange.contact.similarity.ContactSimilarityService;
import com.openexchange.contact.vcard.VCardService;
import com.openexchange.contact.vcard.storage.VCardStorageFactory;
import com.openexchange.dav.DAVServlet;
import com.openexchange.dav.WellKnownServlet;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.group.GroupService;
import com.openexchange.groupware.userconfiguration.Permission;
import com.openexchange.imagetransformation.ImageTransformationService;
import com.openexchange.login.Interface;
import com.openexchange.oauth.provider.resourceserver.scope.AbstractScopeProvider;
import com.openexchange.oauth.provider.resourceserver.scope.OAuthScopeProvider;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.resource.ResourceService;
import com.openexchange.user.UserService;
import com.openexchange.webdav.protocol.osgi.OSGiPropertyMixin;

/**
 * {@link CarddavActivator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class CarddavActivator extends HousekeepingActivator {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(CarddavActivator.class);

    private OSGiPropertyMixin mixin;

    /**
     * Initializes a new {@link CarddavActivator}.
     */
    public CarddavActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] {
            HttpService.class, FolderService.class, ConfigViewFactory.class, UserService.class, ContactService.class,
            ResourceService.class, VCardService.class, GroupService.class, CapabilityService.class, LeanConfigurationService.class
        };
    }

    @Override
    protected synchronized void startBundle() throws Exception {
        try {
            LOG.info("starting bundle: \"com.openexchange.carddav\"");
            /*
             * prepare CardDAV performer & initialize global OSGi property mixin
             */
            CarddavPerformer performer = new CarddavPerformer(this);
            OSGiPropertyMixin mixin = new OSGiPropertyMixin(context, performer);
            performer.setGlobalMixins(mixin);
            this.mixin = mixin;
            /*
             * register CardDAV servlet & WebDAV path
             */
            ConfigViewFactory configViewFactory = getServiceSafe(ConfigViewFactory.class);
            getService(HttpService.class).registerServlet(getInternalPath(configViewFactory, "/carddav"), new CardDAV(performer), null, null);
            getService(HttpService.class).registerServlet("/.well-known/carddav", new WellKnownServlet(getExternalPath(configViewFactory, "/carddav"), Interface.CARDDAV), null, null);
            registerService(OAuthScopeProvider.class, new AbstractScopeProvider(Tools.OAUTH_SCOPE, OAuthStrings.SYNC_CONTACTS) {

                @Override
                public boolean canBeGranted(CapabilitySet capabilities) {
                    return capabilities.contains(Permission.CARDDAV.getCapabilityName());
                }
            });
            /*
             * register Photo performer for referenced contact images in vCards
             */
            getService(HttpService.class).registerServlet(getInternalPath(configViewFactory, "/photos"), new DAVServlet(new PhotoPerformer(this), Interface.CARDDAV), null, null);
            /*
             * track optional services
             */
            trackService(VCardStorageFactory.class);
            trackService(ContactSimilarityService.class);
            trackService(ImageTransformationService.class);
            openTrackers();
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(CarddavActivator.class).error("", e);
            throw e;
        }
    }

    @Override
    protected synchronized void stopBundle() throws Exception {
        LOG.info("stopping bundle: \"com.openexchange.carddav\"");
        /*
         * close OSGi property mixin
         */
        OSGiPropertyMixin mixin = this.mixin;
        if (null != mixin) {
            mixin.close();
            this.mixin = null;
        }
        /*
         * unregister servlet
         */
        HttpService httpService = getService(HttpService.class);
        if (null != httpService) {
            ConfigViewFactory configViewFactory = getServiceSafe(ConfigViewFactory.class);
            String carddavPath = getInternalPath(configViewFactory, "/carddav");
            httpService.unregister(carddavPath);
        }
        super.stopBundle();
    }
}
