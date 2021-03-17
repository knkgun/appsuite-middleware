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

package com.openexchange.dav.resources;

import static com.openexchange.dav.DAVProtocol.protocolException;
import static com.openexchange.java.Autoboxing.b;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Objects;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.config.ConfigTools;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.dav.DAVFactory;
import com.openexchange.dav.DAVProtocol;
import com.openexchange.dav.DAVUserAgent;
import com.openexchange.dav.PreconditionException;
import com.openexchange.dav.Tools;
import com.openexchange.dav.internal.FolderUpdate;
import com.openexchange.dav.mixins.ACL;
import com.openexchange.dav.mixins.ACLRestrictions;
import com.openexchange.dav.mixins.CTag;
import com.openexchange.dav.mixins.Invite;
import com.openexchange.dav.mixins.Principal;
import com.openexchange.dav.mixins.ShareAccess;
import com.openexchange.dav.mixins.ShareResourceURI;
import com.openexchange.dav.mixins.SupportedPrivilegeSet;
import com.openexchange.dav.reports.SyncStatus;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXException.IncorrectString;
import com.openexchange.exception.OXException.ProblematicAttribute;
import com.openexchange.folderstorage.FolderField;
import com.openexchange.folderstorage.FolderProperty;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.ParameterizedFolder;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.type.PrivateType;
import com.openexchange.folderstorage.type.SharedType;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.user.User;
import com.openexchange.user.UserService;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;
import com.openexchange.webdav.protocol.helpers.AbstractResource;

/**
 * {@link FolderCollection}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.1
 */
public abstract class FolderCollection<T> extends DAVCollection {

    protected final DAVFactory factory;
    protected final UserizedFolder folder;

    private ParameterizedFolder folderToUpdate;

    /**
     * Initializes a new {@link FolderCollection}.
     *
     * @param factory The factory
     * @param url The WebDAV path
     * @param folder The underlying folder, or <code>null</code> if it not yet exists
     */
    protected FolderCollection(DAVFactory factory, WebdavPath url, UserizedFolder folder) throws OXException {
        super(factory, url);
        this.factory = factory;
        this.folder = folder;
        if (null != folder) {
            ConfigViewFactory configViewFactory = factory.getService(ConfigViewFactory.class);
            includeProperties(new CTag(this), new com.openexchange.dav.mixins.SyncToken(this),
                new ACL(folder.getPermissions(), configViewFactory), new ACLRestrictions(), new SupportedPrivilegeSet(), new Principal(getOwner(), configViewFactory));
            if (supportsPermissions(folder)) {
                includeProperties(new ShareAccess(this), new Invite(this), new ShareResourceURI(this, factory.getServiceSafe(ConfigViewFactory.class)));
            }
        }
    }

    /**
     * Gets a value indicating whether a specific folder supports permissions or not.
     *
     * @param folder The folder to check
     * @return <code>true</code> if permissions are supported, <code>false</code>, otherwise
     */
    protected static boolean supportsPermissions(UserizedFolder folder) {
        return null != folder && null != folder.getSupportedCapabilities() && folder.getSupportedCapabilities().contains("permissions");
    }

    @Override
    public Permission[] getPermissions() {
        return folder.getPermissions();
    }

    /**
     * Gets all groupware objects in the collection.
     *
     * @return The objects
     */
    protected abstract Collection<T> getObjects() throws OXException;

    /**
     * Gets a groupware object by its resource name.
     *
     * @param resourceName The resource name
     * @return The object
     */
    protected abstract T getObject(String resourceName) throws OXException;

    /**
     * Creates a new resource in this collection.
     *
     * @param object The object to create
     * @param url The URL to use
     * @return The created resource
     */
    protected abstract AbstractResource createResource(T object, WebdavPath url) throws OXException;

    /**
     * Gets the file extension to use for resources in this collcetion.
     *
     * @return The file extension
     */
    protected abstract String getFileExtension();

    /**
     * Gets the value of the collection's CTag. This default implementation constructs the CTag based on the sync token - override if
     * applicable.
     *
     * @return The CTag value
     */
    public String getCTag() throws WebdavProtocolException {
        String folderID = null != getFolder() ? getFolder().getID() : "0";
        return folderID + '-' + getSyncToken();
    }

    @Override
    public List<WebdavResource> getChildren() throws WebdavProtocolException {
        try {
            List<WebdavResource> children = new ArrayList<WebdavResource>();
            for (T object : this.getObjects()) {
                children.add(createResource(object, constructPathForChildResource(object)));
            }
            LOG.debug("{}: added {} child resources.", this.getUrl(), Integer.valueOf(children.size()));
            return children;
        } catch (OXException e) {
            throw protocolException(getUrl(), e);
        }
    }

    /**
     * Gets the resource name from a filename, i.e. the resource name without the expected filename extension.
     *
     * @param filename The filename
     * @return The resource name
     */
    public String extractResourceName(String filename) {
        return Tools.extractResourceName(filename, getFileExtension());
    }

    /**
     * Gets a child resource from this collection by name. If the resource
     * does not yet exists, a placeholder resource is created.
     *
     * @param name the name of the resource
     * @return the child resource
     * @throws WebdavProtocolException
     */
    @Override
    public AbstractResource getChild(String name) throws WebdavProtocolException {
        try {
            String resourceName = Tools.extractResourceName(name, getFileExtension());
            if (null == resourceName) {
                LOG.debug("{}: child resource '{}' not found, creating placeholder resource", this.getUrl(), name);
                return createResource(null, constructPathForChildResource(name));
            }

            T object = this.getObject(resourceName);
            if (null != object) {
                LOG.debug("{}: found child resource by name '{}'", this.getUrl(), name);
                return createResource(object, constructPathForChildResource(object));
            }

            LOG.debug("{}: child resource '{}' not found, creating placeholder resource", this.getUrl(), name);
            return createResource(null, constructPathForChildResource(name));
        } catch (OXException e) {
            throw protocolException(getUrl(), e);
        }
    }

    /**
     * Gets an updated {@link SyncStatus} based on the supplied sync token for this collection.
     *
     * @param token The last sync token as supplied by the client, or <code>null</code> for the initial synchronization
     * @param limit The maximum number of items to return in the response, or <code>-1</code> if unlimited
     * @return The sync status
     */
    public SyncStatus<WebdavResource> getSyncStatus(String token, int limit) throws WebdavProtocolException {
        /*
         * parse & check provided token
         */
        SyncToken syncToken;
        try {
            syncToken = SyncToken.parse(token);
        } catch (IllegalArgumentException e) {
            OXException cause = OXException.general("Invalid sync token \"" + token + "\"", e);
            throw new PreconditionException(cause, DAVProtocol.DAV_NS.getURI(), "valid-sync-token", getUrl(), HttpServletResponse.SC_FORBIDDEN);
        }
        Date since = new Date(syncToken.getTimestamp());
        if (0L < since.getTime() && false == syncToken.isInitial() && false == syncToken.isTruncated()) {
            Date minimumSince = getMinimumSinceForSync();
            if (null != minimumSince && minimumSince.after(since)) {
                /*
                 * token out of range to cover known tombstone data; require slow-sync unless token matches the current one (no changes)
                 */
                if (false == Objects.equals(token, getSyncToken())) {
                    OXException cause = OXException.general("Token \"" + token + "\" out of range.");
                    LOG.debug("", cause);
                    throw new PreconditionException(cause, DAVProtocol.DAV_NS.getURI(), "valid-sync-token", getUrl(), HttpServletResponse.SC_FORBIDDEN);
                }
            }
        }
        /*
         * get & return sync-status
         */
        try {
            return 0 < limit ? getSyncStatus(syncToken, limit) : getSyncStatus(syncToken);
        } catch (OXException e) {
            throw protocolException(getUrl(), e);
        }
    }

    /**
     * Gets the minimum date representing the point in time how long data about deleted items is available in the storage, i.e. the date
     * since when incremental synchronization is supported.
     *
     * @return The minimum date until which differential synchronization is possible, or <code>null</code> if not restricted
     */
    private Date getMinimumSinceForSync() {
        try {
            ConfigView configView = factory.requireService(ConfigViewFactory.class).getView();
            if (false == b(configView.opt("com.openexchange.database.tombstone.cleanup.enabled", Boolean.class, Boolean.TRUE))) {
                return null; // no restrictions
            }
            String timespanStr = configView.opt("com.openexchange.database.tombstone.cleanup.timespan", String.class, "12w");
            long timespan = ConfigTools.parseTimespan(timespanStr);
            if (timespan < 1) {
                LOG.warn("Cleanup enabled but no meaningful value defined. Will use the default of 3 months.");
                timespan = ConfigTools.parseTimespan("12w");
            }
            return new Date(System.currentTimeMillis() - timespan);
        } catch (IllegalArgumentException | OXException e) {
            LOG.warn("Error determing tombstone cleanup timespan, falling back to default of 3 months.", e);
            return new Date(System.currentTimeMillis() - ConfigTools.parseTimespan("12w"));
        }
    }

    @Override
    public Date getCreationDate() {
        return null != folder ? folder.getCreationDateUTC() : null;
    }

    @Override
    public String getDisplayName() throws WebdavProtocolException {
        Locale locale = factory.getUser().getLocale();
        String name = null != locale ? folder.getLocalizedName(locale) : folder.getName();
        String ownerName = SharedType.getInstance().equals(this.folder.getType()) ? getOwnerName() : null;
        if (null != ownerName && 0 < ownerName.length()) {
            return String.format("%s (%s)", name, ownerName);
        }
        return name;
    }

    @Override
    public void setDisplayName(String displayName) throws WebdavProtocolException {
        if (folder.isDefault() || false == PrivateType.getInstance().equals(folder.getType())) {
            throw protocolException(getUrl(), HttpServletResponse.SC_FORBIDDEN);
        }
        getFolderToUpdate().setName(displayName);
    }

    @Override
    public void save() throws WebdavProtocolException {
        if (false == exists() || null == folder) {
            throw protocolException(getUrl(), HttpServletResponse.SC_NOT_FOUND);
        }
        if (null == folderToUpdate) {
            return; // no changes
        }
        try {
            factory.getService(FolderService.class).updateFolder(folderToUpdate, folder.getLastModifiedUTC(), factory.getSession(), null);
        } catch (OXException e) {
            if ("FLD-0092".equals(e.getErrorCode())) {
                /*
                 * 'Unsupported character "..." in field "Folder name".
                 */
                ProblematicAttribute[] problematics = e.getProblematics();
                if (null != problematics && 0 < problematics.length && null != problematics[0] && IncorrectString.class.isInstance(problematics[0])) {
                    IncorrectString incorrectString = ((IncorrectString) problematics[0]);
                    if (FolderObject.FOLDER_NAME == incorrectString.getId()) {
                        String name = folderToUpdate.getName();
                        String correctedDisplayName = name.replace(incorrectString.getIncorrectString(), "");
                        if (false == correctedDisplayName.equals(name)) {
                            folderToUpdate.setName(correctedDisplayName);
                            save();
                            return;
                        }
                    }
                }
                throw protocolException(getUrl(), e);
            }
        }
    }

    @Override
    public void delete() throws WebdavProtocolException {
        internalDelete();
    }

    @Override
    protected void internalDelete() throws WebdavProtocolException {
        if (null == folder) {
            throw protocolException(getUrl(), HttpServletResponse.SC_NOT_FOUND);
        }
        if (null != folder.getOwnPermission() && false == folder.getOwnPermission().isAdmin() && DAVUserAgent.MAC_CALENDAR.equals(getUserAgent())) {
            // Client will continue to show an exclamation mark if responding with 403 on an "unsubscribe" request,
            // so pretend a successful deletion here
            LOG.info("{}: Ignoring delete/unsubscribe request for folder {} due to missing admin permissions of user {}.", getUrl(), folder, Integer.valueOf(factory.getUser().getId()));
            return;
        }
        if (folder.isDefault()) {
            throw protocolException(getUrl(), HttpServletResponse.SC_FORBIDDEN);
        }
        String treeID = null != folder.getTreeID() ? folder.getTreeID() : FolderStorage.REAL_TREE_ID;
        try {
            factory.getService(FolderService.class).deleteFolder(treeID, folder.getID(), folder.getLastModifiedUTC(), factory.getSession(), null);
        } catch (OXException e) {
            throw protocolException(getUrl(), e);
        }
    }

    /**
     * Gets the folder of this collection.
     *
     * @return the folder
     */
    public UserizedFolder getFolder() {
        return folder;
    }

    public User getOwner() throws OXException {
        if (PrivateType.getInstance().equals(folder.getType())) {
            return factory.getUser();
        }
        if (null != folder.getPermissions()) {
            for (Permission permission : folder.getPermissions()) {
                if (permission.isAdmin() && false == permission.isGroup()) {
                    return factory.requireService(UserService.class).getUser(permission.getEntity(), folder.getContext());
                }
            }
        }
        return null;
    }

    /**
     * Constructs a {@link WebdavPath} for a child resource of this
     * collection with the resource name found in the supplied object.
     *
     * @param object the groupware object represented by the resource
     * @return the path
     */
    protected abstract WebdavPath constructPathForChildResource(T object);

    /**
     * Create a 'sync-status' multistatus report considering all changes since the supplied sync-token.
     *
     * @param syncToken The requested sync-token to generate the sync-status for
     * @return The sync status
     */
    protected abstract SyncStatus<WebdavResource> getSyncStatus(SyncToken syncToken) throws OXException;

    /**
     * Create a 'sync-status' multistatus report considering all changes since the supplied time.
     * <p/>
     * The default implementation delegates to {@link #getSyncStatus(Date)} if no limit is specified and throws an exception otherwise.
     * Override if applicable.
     *
     * @param syncToken The requested sync-token to generate the sync-status for
     * @param limit The maximum number of items to return in the response, or <code>-1</code> if unlimited
     * @return The sync status
     */
    protected SyncStatus<WebdavResource> getSyncStatus(SyncToken syncToken, int limit) throws OXException {
        if (0 < limit) {
            throw new PreconditionException(DAVProtocol.DAV_NS.getURI(), "number-of-matches-within-limits", getUrl(), DAVProtocol.SC_INSUFFICIENT_STORAGE);
        }
        return getSyncStatus(syncToken);
    }

    /**
     * Gets a new folder with "settable" properties to apply any property changes. The actual {@link #save()}-operation that is called
     * afterwards then uses this folder template for the update-operation.
     *
     * @return An updatable folder based on the current folder
     */
    protected ParameterizedFolder getFolderToUpdate() {
        if (null == folderToUpdate) {
            folderToUpdate = prepareUpdatableFolder(getFolder());
        }
        return folderToUpdate;
    }

    /**
     * Prepares an "updateable" folder to use with the folder service.
     *
     * @param folder The original folder to use as template, or <code>null</code> to initialize a blank folder
     * @return The folder
     */
    public static ParameterizedFolder prepareUpdatableFolder(UserizedFolder folder) {
        ParameterizedFolder updatableFolder = new FolderUpdate();
        if (null != folder) {
            updatableFolder.setID(folder.getID());
            updatableFolder.setTreeID(folder.getTreeID());
            updatableFolder.setType(folder.getType());
            updatableFolder.setParentID(folder.getParentID());
            updatableFolder.setMeta(null != folder.getMeta() ? new HashMap<String, Object>(folder.getMeta()) : new HashMap<String, Object>());
            if (null != folder.getProperties()) {
                for (Entry<FolderField, FolderProperty> entry : folder.getProperties().entrySet()) {
                    updatableFolder.setProperty(entry.getKey(), null != entry.getValue() ? entry.getValue().getValue() : null);
                }
            }
        } else {
            updatableFolder.setMeta(new HashMap<String, Object>());
        }
        return updatableFolder;
    }

    protected String getOwnerName() {
        User owner = null;
        try {
            owner = getOwner();
        } catch (OXException e) {
            LOG.error("Error resolving owner", e);
        }
        return null != owner ? owner.getDisplayName() : null;
    }

}
