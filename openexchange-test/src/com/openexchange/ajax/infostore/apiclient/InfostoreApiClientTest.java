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

package com.openexchange.ajax.infostore.apiclient;

import static com.openexchange.java.Autoboxing.L;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.After;
import org.junit.Before;
import com.openexchange.ajax.config.actions.Tree;
import com.openexchange.ajax.framework.AbstractConfigAwareAPIClientSession;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.modules.Module;
import com.openexchange.junit.Assert;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.ConfigResponse;
import com.openexchange.testing.httpclient.models.FolderUpdateResponse;
import com.openexchange.testing.httpclient.models.FoldersCleanUpResponse;
import com.openexchange.testing.httpclient.models.InfoItemBody;
import com.openexchange.testing.httpclient.models.InfoItemData;
import com.openexchange.testing.httpclient.models.InfoItemListElement;
import com.openexchange.testing.httpclient.models.InfoItemPermission;
import com.openexchange.testing.httpclient.models.InfoItemResponse;
import com.openexchange.testing.httpclient.models.InfoItemUpdateResponse;
import com.openexchange.testing.httpclient.models.InfoItemsMovedResponse;
import com.openexchange.testing.httpclient.models.InfoItemsResponse;
import com.openexchange.testing.httpclient.models.InfoItemsRestoreResponse;
import com.openexchange.testing.httpclient.models.InfoItemsRestoreResponseData;
import com.openexchange.testing.httpclient.models.NewFolderBody;
import com.openexchange.testing.httpclient.models.NewFolderBodyFolder;
import com.openexchange.testing.httpclient.modules.ConfigApi;
import com.openexchange.testing.httpclient.modules.FoldersApi;
import com.openexchange.testing.httpclient.modules.InfostoreApi;
import com.openexchange.tools.io.IOTools;

/**
 *
 * {@link InfostoreApiClientTest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class InfostoreApiClientTest extends AbstractConfigAwareAPIClientSession {

    protected static final int[] virtualFolders = { FolderObject.SYSTEM_INFOSTORE_FOLDER_ID, FolderObject.VIRTUAL_LIST_INFOSTORE_FOLDER_ID, FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID };

    public static final String INFOSTORE_FOLDER = "infostore.folder";

    protected String folderId;
    protected String folderTitle;
    protected List<InfoItemListElement> fileIds = new ArrayList<>();
    protected List<String> folders = new ArrayList<>();

    protected String hostName = null;

    private String privateInfostoreFolder;

    protected InfostoreApi infostoreApi;

    protected static final String MIME_TEXT_PLAIN = "text/plain";
    protected static final String MIME_IMAGE_JPG = "image/jpeg";

    protected Long timestamp = null;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        folderTitle = "NewInfostoreFolder" + UUID.randomUUID().toString();
        folderId = createFolderForTest(folderTitle);
        rememberFolder(folderId);
        infostoreApi = new InfostoreApi(getApiClient());
    }

    @Override
    @After
    public void tearDown() throws Exception {
        if (!fileIds.isEmpty()) {
            infostoreApi.deleteInfoItems(getApiClient().getSession(), L(new Date().getTime()), fileIds, Boolean.TRUE, null);
        }
        if (!folders.isEmpty()) {
            FoldersApi folderApi = new FoldersApi(getApiClient());
            folderApi.deleteFolders(getApiClient().getSession(), folders, "1", L(new Date().getTime()), null, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE, null);
        }
        super.tearDown();
    }

    protected String uploadInfoItem(File file, String mimeType) throws ApiException, FileNotFoundException, IOException {
        return uploadInfoItem(null, file, mimeType, null);
    }

    protected String uploadInfoItem(String id, File file, String mimeType, String versionComment) throws ApiException, FileNotFoundException, IOException {
        byte[] bytes = IOTools.getBytes(new FileInputStream(file));
        return uploadInfoItem(id, file, mimeType, versionComment, bytes, null, null, null);
    }

    protected String uploadInfoItem(String id, File file, String mimeType, String versionComment, String filename) throws ApiException, FileNotFoundException, IOException {
        byte[] bytes = IOTools.getBytes(new FileInputStream(file));
        return uploadInfoItem(id, file, mimeType, versionComment, bytes, null, null, filename);
    }

    protected String uploadInfoItem(String id, File file, String mimeType, String versionComment, byte[] bytes, Long offset, Long filesize, String filename) throws ApiException {
        String name = filename == null ? file.getName() : filename;
        InfoItemUpdateResponse uploadInfoItem = infostoreApi.uploadInfoItem(getApiClient().getSession(), folderId, name, bytes, timestamp, id, name, mimeType, null, null, null, null, versionComment, null, null, filesize == null ? Long.valueOf(bytes.length) : filesize, Boolean.FALSE, Boolean.FALSE, offset, null);
        Assert.assertNull(uploadInfoItem.getErrorDesc(), uploadInfoItem.getError());
        Assert.assertNotNull(uploadInfoItem.getData());
        timestamp = uploadInfoItem.getTimestamp();
        return uploadInfoItem.getData();
    }

    protected String uploadInfoItem(String id, InfoItemData file, String mimeType, String versionComment, byte[] bytes, Long offset, Long filesize, String filename) throws ApiException {
        String name = filename == null ? file.getFilename() : filename;
        // @formatter:off
        InfoItemUpdateResponse uploadInfoItem = infostoreApi.uploadInfoItem(
            getApiClient().getSession(), 
            folderId, 
            name, 
            bytes, 
            timestamp, 
            id, 
            name, 
            mimeType, 
            null, 
            null, 
            null, 
            null, 
            versionComment, 
            null, 
            null, 
            filesize == null ? Long.valueOf(bytes.length) : filesize, 
            Boolean.FALSE, 
            Boolean.FALSE, 
            offset, 
            null);
        // @formatter:on
        Assert.assertNull(uploadInfoItem.getErrorDesc(), uploadInfoItem.getError());
        Assert.assertNotNull(uploadInfoItem.getData());
        timestamp = uploadInfoItem.getTimestamp();
        return uploadInfoItem.getData();
    }

    protected InfoItemUpdateResponse uploadInfoItemWithError(String id, File file, String mimeType, String versionComment) throws ApiException, FileNotFoundException, IOException {
        byte[] bytes = IOTools.getBytes(new FileInputStream(file));
        return uploadInfoItemWithError(id, file, mimeType, versionComment, bytes, null, null, null);
    }

    protected InfoItemUpdateResponse uploadInfoItemWithError(String id, File file, String mimeType, String versionComment, String filename) throws ApiException, FileNotFoundException, IOException {
        byte[] bytes = IOTools.getBytes(new FileInputStream(file));
        return uploadInfoItemWithError(id, file, mimeType, versionComment, bytes, null, null, filename);
    }

    protected InfoItemUpdateResponse uploadInfoItemWithError(String id, File file, String mimeType, String versionComment, byte[] bytes, Long offset, Long filesize, String filename) throws ApiException {
        String name = filename == null ? file.getName() : filename;
        InfoItemUpdateResponse uploadInfoItem = infostoreApi.uploadInfoItem(getApiClient().getSession(), folderId, name, bytes, timestamp, id, name, mimeType, null, null, null, null, versionComment, null, null, filesize == null ? Long.valueOf(bytes.length) : filesize, Boolean.FALSE, Boolean.FALSE, offset, null);
        Assert.assertNotNull(uploadInfoItem.getErrorDesc(), uploadInfoItem.getError());
        timestamp = uploadInfoItem.getTimestamp();
        return uploadInfoItem;
    }

    protected void rememberFile(String id, String folder) {
        InfoItemListElement element = new InfoItemListElement();
        element.setId(id);
        element.setFolder(folder);
        fileIds.add(element);
    }

    protected void rememberFolder(String folder) {
        folders.add(folder);
    }

    protected InfoItemData getItem(String id) throws ApiException {
        InfoItemResponse infoItem = infostoreApi.getInfoItem(getApiClient().getSession(), id, folderId);
        Assert.assertNull(infoItem.getError());
        Assert.assertNotNull(infoItem.getData());
        return infoItem.getData();
    }

    private String createFolderForTest(String title) throws ApiException {
        final String parent = getPrivateInfostoreFolder();
        FoldersApi folderApi = new FoldersApi(getApiClient());
        NewFolderBody body = new NewFolderBody();
        NewFolderBodyFolder folder = new NewFolderBodyFolder();
        folder.setModule(Module.INFOSTORE.getName());
        folder.setSummary(title);
        folder.setTitle(folder.getSummary());
        folder.setSubscribed(Boolean.TRUE);
        folder.setPermissions(null);
        body.setFolder(folder);
        FolderUpdateResponse folderUpdateResponse = folderApi.createFolder(parent, getApiClient().getSession(), body, "1", null, null, null);
        return checkResponse(folderUpdateResponse);
    }

    protected void deleteFolder(String folderId, Boolean hardDelete) throws ApiException {
        FoldersApi folderApi = new FoldersApi(getApiClient());
        FoldersCleanUpResponse deleteFolderResponse = folderApi.deleteFolders(getApiClient().getSession(), Collections.singletonList(folderId), "1", timestamp, null, hardDelete, Boolean.TRUE, Boolean.FALSE, null);
        Assert.assertNull(deleteFolderResponse.getErrorDesc(), deleteFolderResponse.getError());
        Assert.assertNotNull(deleteFolderResponse.getData());
        Assert.assertEquals(0, deleteFolderResponse.getData().size());
    }

    public String getPrivateInfostoreFolder() throws ApiException {
        if (null == privateInfostoreFolder) {
            ConfigApi configApi = new ConfigApi(getApiClient());
            ConfigResponse configNode = configApi.getConfigNode(Tree.PrivateInfostoreFolder.getPath(), getApiClient().getSession());
            Object data = checkResponse(configNode);
            if (data != null && !data.toString().equalsIgnoreCase("null")) {
                privateInfostoreFolder = String.valueOf(data);
            } else {
                Assert.fail("It seems that the user doesn't support drive.");
            }

        }
        return privateInfostoreFolder;
    }

    private Object checkResponse(ConfigResponse resp) {
        Assert.assertNull(resp.getErrorDesc(), resp.getError());
        Assert.assertNotNull(resp.getData());
        return resp.getData();
    }

    private String checkResponse(FolderUpdateResponse resp) {
        Assert.assertNull(resp.getErrorDesc(), resp.getError());
        Assert.assertNotNull(resp.getData());
        return resp.getData();
    }

    protected void deleteInfoItems(List<InfoItemListElement> toDelete, Boolean hardDelete) throws ApiException {
        InfoItemsResponse deleteInfoItems = infostoreApi.deleteInfoItems(getApiClient().getSession(), timestamp == null ? L(System.currentTimeMillis()) : timestamp, toDelete, hardDelete, null);
        Assert.assertNull(deleteInfoItems.getError());
        Assert.assertNotNull(deleteInfoItems.getData());
        timestamp = deleteInfoItems.getTimestamp();
        Object data = deleteInfoItems.getData();
        Assert.assertTrue(data instanceof ArrayList<?>);
        ArrayList<?> arrayData = (ArrayList<?>) data;
        assertEquals(0, arrayData.size());
    }

    protected String copyInfoItem(String id, InfoItemData modifiedData) throws ApiException {
        InfoItemUpdateResponse response = infostoreApi.copyInfoItem(getApiClient().getSession(), id, modifiedData, null);
        Assert.assertNull(response.getError());
        Assert.assertNotNull(response.getData());
        timestamp = response.getTimestamp();
        return response.getData();
    }

    protected List<String> moveInfoItems(String id, List<InfoItemListElement> toMove) throws ApiException {
        InfoItemsMovedResponse response = infostoreApi.moveInfoItems(getApiClient().getSession(), id, toMove, null);
        Assert.assertNull(response.getError());
        Assert.assertNotNull(response.getData());
        timestamp = response.getTimestamp();
        return response.getData();
    }

    protected List<InfoItemsRestoreResponseData> restoreInfoItems(List<InfoItemListElement> toRestore) throws ApiException {
        InfoItemsRestoreResponse restoredItems = infostoreApi.restoreInfoItemsFromTrash(getApiClient().getSession(), toRestore, null);
        Assert.assertNull(restoredItems.getError());
        Assert.assertNotNull(restoredItems.getData());
        timestamp = restoredItems.getTimestamp();
        return restoredItems.getData();
    }

    protected void assertFileExistsInFolder(String folderId, String itemId) throws Exception {
        // @formatter:off
        InfoItemsResponse allInfoItems = infostoreApi.getAllInfoItems(getApiClient().getSession(),
            folderId,
            Integer.toString(Metadata.ID),
            null,
            null,
            null,
            null,
            null,
            null);
        // @formatter:off
        List<List<String>> ret = (List<List<String>>)checkResponse(allInfoItems.getError(), allInfoItems.getErrorDesc(), allInfoItems.getData());
        Assert.assertTrue("The item is not present in the given folder", ret.stream().filter( l -> l.contains(itemId)).count() == 1);
    }

    /**
     * Updates the permissions of the document with the given id
     *
     * @param id The id of the document
     * @param perms The new permissions
     * @throws ApiException
     */
    protected void updatePermissions(String id, List<InfoItemPermission> perms) throws ApiException {
        updatePermissions(id, perms, Optional.empty());
    }

    /**
     *
     * Updates the permissions of the document with the given id
     *
     * @param id The id of the document
     * @param perms The new permissions
     * @param errorCode The expected error code
     * @throws ApiException
     */
    protected void updatePermissions(String id, List<InfoItemPermission> perms, Optional<String> errorCode) throws ApiException {
        InfoItemData file = new InfoItemData();
        file.setObjectPermissions(perms);
        InfoItemBody body = new InfoItemBody();
        body.file(file);
        InfoItemUpdateResponse resp = infostoreApi.updateInfoItem(getSessionId(), id, timestamp, body, null);
        if (errorCode.isPresent()) {
            assertNotNull("Expected an error but the response didn't contain one.", resp.getError());
            assertEquals("Request returned with an unexpected error: " + resp.getErrorDesc(), errorCode.get(), resp.getCode());
            return;
        }
        assertNull(resp.getError());
        assertNotNull(resp.getData());
    }

}
