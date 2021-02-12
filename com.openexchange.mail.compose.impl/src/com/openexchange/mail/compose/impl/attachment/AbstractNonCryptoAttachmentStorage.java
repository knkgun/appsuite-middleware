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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.mail.compose.impl.attachment;

import static com.openexchange.java.Autoboxing.I;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.java.util.UUIDs;
import com.openexchange.lock.AccessControl;
import com.openexchange.lock.LockService;
import com.openexchange.mail.compose.Attachment;
import com.openexchange.mail.compose.Attachment.ContentDisposition;
import com.openexchange.mail.compose.AttachmentDescription;
import com.openexchange.mail.compose.AttachmentOrigin;
import com.openexchange.mail.compose.AttachmentStorage;
import com.openexchange.mail.compose.AttachmentStorageIdentifier;
import com.openexchange.mail.compose.AttachmentStorageIdentifier.KnownArgument;
import com.openexchange.mail.compose.AttachmentStorageReference;
import com.openexchange.mail.compose.AttachmentStorages;
import com.openexchange.mail.compose.CompositionSpaceErrorCode;
import com.openexchange.mail.compose.ContentId;
import com.openexchange.mail.compose.DataProvider;
import com.openexchange.mail.compose.DefaultAttachment;
import com.openexchange.mail.compose.SizeProvider;
import com.openexchange.mail.compose.SizeReturner;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.stream.CountingOnlyInputStream;


/**
 * {@link AbstractNonCryptoAttachmentStorage} - The abstract non-en/decrypting attachment storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public abstract class AbstractNonCryptoAttachmentStorage implements AttachmentStorage {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractNonCryptoAttachmentStorage.class);
    }

    /** The service look-up for tracked OSGi services */
    protected final ServiceLookup services;

    /**
     * Initializes a new {@link AbstractNonCryptoAttachmentStorage}.
     */
    protected AbstractNonCryptoAttachmentStorage(ServiceLookup services) {
        super();
        this.services = services;
    }

    /**
     * Gets the data provider for storage resource associated with given identifier
     *
     * @param storageIdentifier The storage identifier
     * @param session The session providing user information
     * @return The data provider
     * @throws OXException If data provider cannot be returned
     */
    protected abstract DataProvider getDataProviderFor(AttachmentStorageIdentifier storageIdentifier, Session session) throws OXException;

    /**
     * Saves the specified data to storage.
     *
     * @param input The data to store
     * @param size The size (if known)
     * @param session The session providing user information
     * @return The storage identifier
     * @throws OXException If data cannot be saved
     */
    protected abstract AttachmentStorageIdentifier saveData(InputStream input, long size, Session session) throws OXException;

    /**
     * Deletes the specified data associated with given identifier from storage.
     *
     * @param storageIdentifier The storage identifier
     * @param session The session providing user information
     * @return <code>true</code> if deletion was successful; otherwise <code>false</code>
     * @throws OXException If data cannot be deleted
     */
    protected abstract boolean deleteData(AttachmentStorageIdentifier storageIdentifier, Session session) throws OXException;

    /**
     * Requires the database service
     *
     * @return The database service
     * @throws OXException If database service is not available
     */
    protected DatabaseService requireDatabaseService() throws OXException {
        DatabaseService databaseService = services.getOptionalService(DatabaseService.class);
        if (null == databaseService) {
            throw ServiceExceptionCode.absentService(DatabaseService.class);
        }
        return databaseService;
    }

    @Override
    public Attachment getAttachment(UUID id, Optional<Boolean> optionalEncrypt, Session session) throws OXException {
        DatabaseService databaseService = requireDatabaseService();
        Connection con = databaseService.getReadOnly(session.getContextId());
        try {
            return getAttachment(id, session, con);
        } finally {
            databaseService.backReadOnly(session.getContextId(), con);
        }
    }

    /**
     * Gets the attachment associated with given identifier
     *
     * @param id The attachment identifier
     * @param session The session providing user information
     * @param con The connection to use
     * @return The attachment or <code>null</code> if no such attachment exists
     * @throws OXException If attachment cannot be returned
     */
    public Attachment getAttachment(UUID id, Session session, Connection con) throws OXException {
        if (null == con) {
            return getAttachment(id, Optional.empty(), session);
        }

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT cid, user, refType, refId, name, size, mimeType, contentId, disposition, origin, csid, dedicatedFileStorageId FROM compositionSpaceAttachmentMeta WHERE uuid=?");
            stmt.setBytes(1, UUIDs.toByteArray(id));
            rs = stmt.executeQuery();
            if (false == rs.next()) {
                return null;
            }

            if (session.getContextId() != rs.getInt(1)) {
                // Context does not match
                return null;
            }
            if (session.getUserId() != rs.getInt(2)) {
                // User does not match
                return null;
            }
            if (getStorageType().getType() != rs.getInt(3)) {
                // Storage type does not match
                return null;
            }

            AttachmentStorageIdentifier storageIdentifier = storageIdentifierFrom(rs);

            DefaultAttachment.Builder attachment = DefaultAttachment.builder(id);
            attachment.withStorageReference(new AttachmentStorageReference(storageIdentifier, getStorageType()));
            attachment.withName(rs.getString(5));
            attachment.withSize(rs.getLong(6));
            attachment.withMimeType(rs.getString(7));
            attachment.withContentId(rs.getString(8));
            attachment.withDisposition(ContentDisposition.dispositionFor(rs.getString(9)));
            attachment.withOrigin(AttachmentOrigin.getOriginFor(rs.getString(10)));
            attachment.withCompositionSpaceId(UUIDs.toUUID(rs.getBytes(11)));
            attachment.withDataProvider(getDataProviderFor(storageIdentifier, session));
            return attachment.build();
        } catch (SQLException e) {
            throw CompositionSpaceErrorCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    @Override
    public List<Attachment> getAttachmentsByCompositionSpace(UUID compositionSpaceId, Session session) throws OXException {
        DatabaseService databaseService = requireDatabaseService();
        Connection con = databaseService.getReadOnly(session.getContextId());
        try {
            return getAttachmentsByCompositionSpace(compositionSpaceId, session, con);
        } finally {
            databaseService.backReadOnly(session.getContextId(), con);
        }
    }

    /**
     * Gets the attachments associated with given composition space.
     *
     * @param compositionSpaceId The composition space identifier
     * @param session The session providing user information
     * @param con The connection to use
     * @return The attachments or an empty list if there are no attachments associated with given composition space
     * @throws OXException If attachments cannot be returned
     */
    public List<Attachment> getAttachmentsByCompositionSpace(UUID compositionSpaceId, Session session, Connection con) throws OXException {
        if (null == con) {
            return getAttachmentsByCompositionSpace(compositionSpaceId, session);
        }

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT cid, user, refType, refId, name, size, mimeType, contentId, disposition, origin, uuid, dedicatedFileStorageId FROM compositionSpaceAttachmentMeta WHERE csid=?");
            stmt.setBytes(1, UUIDs.toByteArray(compositionSpaceId));
            rs = stmt.executeQuery();
            if (false == rs.next()) {
                return Collections.emptyList();
            }

            List<Attachment> attachments = new LinkedList<>();
            do {
                if (session.getContextId() != rs.getInt(1)) {
                    // Context does not match
                    continue;
                }
                if (session.getUserId() != rs.getInt(2)) {
                    // User does not match
                    continue;
                }
                if (getStorageType().getType() != rs.getInt(3)) {
                    // Storage type does not match
                    continue;
                }

                AttachmentStorageIdentifier storageIdentifier = storageIdentifierFrom(rs);

                DefaultAttachment.Builder attachment = DefaultAttachment.builder(UUIDs.toUUID(rs.getBytes(11)));
                attachment.withStorageReference(new AttachmentStorageReference(storageIdentifier, getStorageType()));
                attachment.withName(rs.getString(5));
                attachment.withSize(rs.getLong(6));
                attachment.withMimeType(rs.getString(7));
                attachment.withContentId(rs.getString(8));
                attachment.withDisposition(ContentDisposition.dispositionFor(rs.getString(9)));
                attachment.withOrigin(AttachmentOrigin.getOriginFor(rs.getString(10)));
                attachment.withCompositionSpaceId(compositionSpaceId);
                attachment.withDataProvider(getDataProviderFor(storageIdentifier, session));
                attachments.add(attachment.build());
            } while (rs.next());
            return attachments;
        } catch (SQLException e) {
            throw CompositionSpaceErrorCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    @Override
    public SizeReturner getSizeOfAttachmentsByCompositionSpace(UUID compositionSpaceId, Session session) throws OXException {
        DatabaseService databaseService = requireDatabaseService();
        Connection con = databaseService.getReadOnly(session.getContextId());
        try {
            return getSizeOfAttachmentsByCompositionSpace(compositionSpaceId, session, con);
        } finally {
            databaseService.backReadOnly(session.getContextId(), con);
        }
    }

    /**
     * Gets the total size of attachments associated with given composition space.
     *
     * @param compositionSpaceId The composition space identifier
     * @param session The session providing user information
     * @param con The connection to use
     * @return The attachments' size or <code>0</code> (zero) if there are no attachments associated with given composition space
     * @throws OXException If attachments' size cannot be returned
     */
    public SizeReturner getSizeOfAttachmentsByCompositionSpace(UUID compositionSpaceId, Session session, Connection con) throws OXException {
        if (null == con) {
            return getSizeOfAttachmentsByCompositionSpace(compositionSpaceId, session);
        }

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT cid, user, refType, refId, size, dedicatedFileStorageId FROM compositionSpaceAttachmentMeta WHERE csid=?");
            stmt.setBytes(1, UUIDs.toByteArray(compositionSpaceId));
            rs = stmt.executeQuery();
            if (false == rs.next()) {
                return SizeReturner.sizeReturnerFor(0);
            }

            SizeReturner.Builder sizeReturner = SizeReturner.builder();
            do {
                if (session.getContextId() != rs.getInt(1)) {
                    // Context does not match
                    continue;
                }
                if (session.getUserId() != rs.getInt(2)) {
                    // User does not match
                    continue;
                }
                if (getStorageType().getType() != rs.getInt(3)) {
                    // Storage type does not match
                    continue;
                }

                long size = rs.getLong(5);
                if (size < 0) {
                    // Need to count...
                    AttachmentStorageIdentifier storageIdentifier = storageIdentifierFrom(rs);
                    sizeReturner.addDataProvider(getDataProviderFor(storageIdentifier, session));
                } else {
                    sizeReturner.addSize(size);
                }
            } while (rs.next());
            return sizeReturner.build();
        } catch (SQLException e) {
            throw CompositionSpaceErrorCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    /**
     * Saves given data to storage and detects proper size.
     *
     * @param input The stream providing the data to save
     * @param givenSize The size as indicated by caller
     * @param sizeProvider The optional size provider; considered if given size is less than <code>0</code> (zero)
     * @param session The session
     * @return A tuple providing storage identifier and size
     * @throws OXException If saving data fails
     */
    private StorageIdentifierAndSize saveData(InputStream input, long givenSize, SizeProvider sizeProvider, Session session) throws OXException {
        AttachmentStorageIdentifier storageIdentifier = null;
        try {
            long size = givenSize;

            // Save data into storage (either with counting bytes or not)
            if (size < 0) {
                // Size is unknown...
                if (null == sizeProvider) {
                    // Need to count to determine number of bytes
                    CountingOnlyInputStream countingStream = null;
                    try {
                        countingStream = new CountingOnlyInputStream(input);
                        storageIdentifier = saveData(countingStream, -1, session);
                        size = countingStream.getCount();
                    } finally {
                        Streams.close(countingStream, input);
                    }
                } else {
                    // Obtain size from SizeProvider instance after saving to storage
                    try {
                        storageIdentifier = saveData(input, -1, session);
                    } finally {
                        Streams.close(input);
                    }
                    size = sizeProvider.getSize();
                }
            } else {
                // Exact size is given
                try {
                    storageIdentifier = saveData(input, size, session);
                } finally {
                    Streams.close(input);
                }
            }

            StorageIdentifierAndSize retval = new StorageIdentifierAndSize(storageIdentifier, size);
            storageIdentifier = null; // Avoid premature deletion
            return retval;
        } finally {
            deleteSafely(storageIdentifier, session);
        }
    }

    /**
     * Safely deletes the storage resource referenced by given storage identifier (if any given).
     *
     * @param optStorageIdentifier The storage identifier or <code>null</code>
     * @param session The session
     */
    private void deleteSafely(AttachmentStorageIdentifier optStorageIdentifier, Session session) {
        if (null != optStorageIdentifier) {
            try {
                if (false == deleteData(optStorageIdentifier, session)) {
                    LoggerHolder.LOG.error("Failed to delete storage resource with identifier {}", optStorageIdentifier.getIdentifier());
                }
            } catch (Exception e) {
                LoggerHolder.LOG.error("Failed to delete storage resource with identifier {}", optStorageIdentifier.getIdentifier(), e);
            }
        }
    }

    @Override
    public Attachment saveAttachment(InputStream input, AttachmentDescription attachment, SizeProvider sizeProvider, Optional<Boolean> optionalEncrypt, Session session) throws OXException {
        DatabaseService databaseService = requireDatabaseService();

        StorageIdentifierAndSize identifierAndSize = saveData(input, attachment.getSize(), sizeProvider, session);

        AttachmentStorageIdentifier storageIdentifierToDelete = identifierAndSize.storageIdentifier;
        Connection con = null;
        int rollback = 0;
        Attachment savedAttachment = null;
        try {
            con = databaseService.getWritable(session.getContextId());

            con.setAutoCommit(false); // BEGIN
            rollback = 1;

            try {
                savedAttachment = saveAttachmentMetaData(identifierAndSize, attachment, session, con);
            } catch (OXException e) {
                // Assume storage resource already deleted. Null'ify and re-throw...
                storageIdentifierToDelete = null;
                throw e;
            }

            con.commit(); // COMMIT
            rollback = 2;

            storageIdentifierToDelete = null;
            return savedAttachment;
        } catch (SQLException e) {
            throw CompositionSpaceErrorCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            if (null != con) {
                if (rollback > 0) {
                    if (rollback == 1) {
                        Databases.rollback(con);
                    }
                    Databases.autocommit(con);
                }
                if (savedAttachment == null) {
                    databaseService.backWritableAfterReading(session.getContextId(), con);
                } else {
                    databaseService.backWritable(session.getContextId(), con);
                }
            }
            deleteSafely(storageIdentifierToDelete, session);
        }
    }

    /**
     * Saves the specified attachment binary data and meta data
     *
     * @param input The input stream providing binary data
     * @param attachment The attachment providing meta data
     * @param sizeProvider The optional size provider
     * @param session The session providing user information
     * @param con The connection to use
     * @return The resulting attachment
     * @throws OXException If saving attachment fails
     */
    public Attachment saveAttachment(InputStream input, AttachmentDescription attachment, SizeProvider sizeProvider, Session session, Connection con) throws OXException {
        if (null == con) {
            return saveAttachment(input, attachment, sizeProvider, Optional.empty(), session);
        }

        StorageIdentifierAndSize identifierAndSize = saveData(input, attachment.getSize(), sizeProvider, session);
        return saveAttachmentMetaData(identifierAndSize, attachment, session, con);
    }

    private Attachment saveAttachmentMetaData(StorageIdentifierAndSize identifierAndSize, AttachmentDescription attachmentDesc, Session session, Connection con) throws OXException {
        AttachmentStorageIdentifier storageIdentifierToDelete = identifierAndSize.storageIdentifier;
        PreparedStatement stmt = null;
        try {
            // Build appropriate attachment instance.
            UUID uuid = UUID.randomUUID();
            ContentId contentId = attachmentDesc.getContentId();
            if (contentId == null && ContentDisposition.INLINE == attachmentDesc.getContentDisposition()) {
                contentId = AttachmentStorages.generateContentIdForAttachmentId(uuid);
            }

            DefaultAttachment.Builder builder = DefaultAttachment.builder(uuid);
            builder.withStorageReference(new AttachmentStorageReference(storageIdentifierToDelete, getStorageType()));
            builder.withName(attachmentDesc.getName());
            builder.withSize(identifierAndSize.size);
            builder.withMimeType(attachmentDesc.getMimeType());
            builder.withContentId(contentId);
            builder.withDisposition(attachmentDesc.getContentDisposition());
            builder.withOrigin(attachmentDesc.getOrigin());
            builder.withCompositionSpaceId(attachmentDesc.getCompositionSpaceId());
            builder.withDataProvider(getDataProviderFor(storageIdentifierToDelete, session));
            DefaultAttachment defaultAttachment = builder.build();

            // Insert into database
            stmt = con.prepareStatement("INSERT INTO compositionSpaceAttachmentMeta (uuid, cid, user, refType, refId, name, size, mimeType, contentId, disposition, origin, csid, dedicatedFileStorageId) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            stmt.setBytes(1, UUIDs.toByteArray(defaultAttachment.getId()));
            stmt.setInt(2, session.getContextId());
            stmt.setInt(3, session.getUserId());
            stmt.setInt(4, getStorageType().getType());
            stmt.setString(5, storageIdentifierToDelete.getIdentifier());
            setOptVarChar(6, attachmentDesc.getName(), stmt);
            stmt.setLong(7, identifierAndSize.size);
            setOptVarChar(8, attachmentDesc.getMimeType(), stmt);
            setOptVarChar(9, contentId == null ? null : contentId.getContentId(), stmt);
            setOptVarChar(10, null == attachmentDesc.getContentDisposition() ? null : attachmentDesc.getContentDisposition().getId(), stmt);
            setOptVarChar(11, null == attachmentDesc.getOrigin() ? null : attachmentDesc.getOrigin().getIdentifier(), stmt);
            stmt.setBytes(12, UUIDs.toByteArray(attachmentDesc.getCompositionSpaceId()));
            {
                Optional<Integer> dedicatedFileStorageId = storageIdentifierToDelete.getArgument(KnownArgument.FILE_STORAGE_IDENTIFIER);
                stmt.setInt(13, dedicatedFileStorageId.isPresent() ? dedicatedFileStorageId.get().intValue() : 0);
            }
            stmt.executeUpdate();
            Databases.closeSQLStuff(stmt);
            stmt = null;

            storageIdentifierToDelete = null;
            return defaultAttachment;
        } catch (SQLException e) {
            throw CompositionSpaceErrorCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
            deleteSafely(storageIdentifierToDelete, session);
        }
    }

    private static void setOptVarChar(int parameterIndex, String value, PreparedStatement stmt) throws SQLException {
        if (null == value) {
            stmt.setNull(parameterIndex, Types.VARCHAR);
        } else {
            stmt.setString(parameterIndex, value);
        }
    }

    @Override
    public void deleteAttachmentsByCompositionSpace(UUID compositionSpaceId, Session session) throws OXException {
        DatabaseService databaseService = requireDatabaseService();
        Connection con = databaseService.getWritable(session.getContextId());
        boolean deleted = false;
        int rollback = 0;
        try {
            Databases.startTransaction(con);
            rollback = 1;

            List<AttachmentStorageIdentifier> storageIdentifiers = new ArrayList<>();
            deleted = deleteAttachmentsByCompositionSpace(compositionSpaceId, session, storageIdentifiers, con);

            con.commit();
            rollback = 2;

            for (AttachmentStorageIdentifier storageIdentifier : storageIdentifiers) {
                deleteSafely(storageIdentifier, session);
            }
        } catch (SQLException e) {
            throw CompositionSpaceErrorCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback == 1) {
                    Databases.rollback(con);
                }
                Databases.autocommit(con);
            }
            if (deleted) {
                databaseService.backWritable(session.getContextId(), con);
            } else {
                databaseService.backWritableAfterReading(session.getContextId(), con);
            }
        }
    }

    /**
     * Deletes the attachments associated with given composition space.
     *
     * @param compositionSpaceId The composition space identifier
     * @param session The session providing user information
     * @param storageIdentifiers A container for storage identifiers, which are supposed to be deleted
     * @param con The connection to use
     * @return <code>true</code> if at least one attachment has been deleted; otherwise <code>false</code>
     * @throws OXException If attachments cannot be deleted
     */
    private boolean deleteAttachmentsByCompositionSpace(UUID compositionSpaceId, Session session, List<AttachmentStorageIdentifier> storageIdentifiers, Connection con) throws OXException {
        if (null == con) {
            throw OXException.general("Connection must not be null");
        }

        boolean error = true; // pessimistic
        try {
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                stmt = con.prepareStatement("SELECT uuid, cid, user, refType, refId, dedicatedFileStorageId FROM compositionSpaceAttachmentMeta WHERE cid=? AND csid=?");
                stmt.setInt(1, session.getContextId());
                stmt.setBytes(2, UUIDs.toByteArray(compositionSpaceId));
                rs = stmt.executeQuery();

                if (false == rs.next()) {
                    // No such attachment meta data for given composition space
                    error = false; // All went fine
                    return false;
                }

                List<byte[]> ids2Delete = new LinkedList<byte[]>();
                int storageType = getStorageType().getType();
                do {
                    if ((session.getUserId() == rs.getInt(3)) && (storageType == rs.getInt(4))) {
                        byte[] id = rs.getBytes(1);
                        AttachmentStorageIdentifier storageIdentifier = storageIdentifierFrom(rs);
                        storageIdentifiers.add(storageIdentifier);

                        ids2Delete.add(id);
                    }
                } while (rs.next());
                Databases.closeSQLStuff(rs, stmt);
                stmt = null;
                rs = null;

                stmt = con.prepareStatement("DELETE FROM compositionSpaceAttachmentMeta WHERE uuid=?");
                for (byte[] id : ids2Delete) {
                    stmt.setBytes(1, id);
                    stmt.addBatch();
                }
                int[] updateCounts = stmt.executeBatch();

                error = false; // All went fine
                for (int updateCount : updateCounts) {
                    if (updateCount > 0) {
                        return true;
                    }
                }
                return false;
            } catch (SQLException e) {
                throw CompositionSpaceErrorCode.SQL_ERROR.create(e, e.getMessage());
            } finally {
                Databases.closeSQLStuff(rs, stmt);
            }
        } finally {
            if (error) {
                // Prevent from prematurely deleting storage resources
                storageIdentifiers.clear();
            }
        }
    }

    @Override
    public void deleteAttachments(List<UUID> ids, Session session) throws OXException {
        if (null == ids || ids.isEmpty()) {
            return;
        }

        DatabaseService databaseService = requireDatabaseService();
        Connection con = databaseService.getWritable(session.getContextId());
        int rollback = 0;
        boolean deleted = false;
        try {
            Databases.startTransaction(con);
            rollback = 1;

            List<AttachmentStorageIdentifier> storageIdentifiers = new ArrayList<>(ids.size());
            deleted = deleteAttachments(ids, session, storageIdentifiers, con);

            con.commit();
            rollback = 2;

            for (AttachmentStorageIdentifier storageIdentifier : storageIdentifiers) {
                deleteSafely(storageIdentifier, session);
            }
        } catch (SQLException e) {
            throw CompositionSpaceErrorCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback == 1) {
                    Databases.rollback(con);
                }
                Databases.autocommit(con);
            }
            if (deleted) {
                databaseService.backWritable(session.getContextId(), con);
            } else {
                databaseService.backWritableAfterReading(session.getContextId(), con);
            }
        }
    }

    /**
     * Deletes the attachments associated with given identifiers
     *
     * @param ids The attachment identifiers
     * @param session The session providing user information
     * @param storageIdentifiers A container for storage identifiers, which are supposed to be deleted
     * @param con The connection to use
     * @return <code>true</code> if at least one attachment has been deleted; otherwise <code>false</code>
     * @throws OXException If attachments cannot be deleted
     */
    private boolean deleteAttachments(List<UUID> ids, Session session, List<AttachmentStorageIdentifier> storageIdentifiers, Connection con) throws OXException {
        if (null == con) {
            throw OXException.general("Connection must not be null");
        }

        boolean deleted = false;
        boolean error = true; // pessimistic
        try {
            if (ids.size() == 1) {
                UUID id = ids.get(0);
                PreparedStatement stmt = null;
                ResultSet rs = null;
                try {
                    stmt = con.prepareStatement("SELECT cid, user, refType, refId, dedicatedFileStorageId FROM compositionSpaceAttachmentMeta WHERE uuid=?");
                    stmt.setBytes(1, UUIDs.toByteArray(id));
                    rs = stmt.executeQuery();
                    if (rs.next() && (session.getContextId() == rs.getInt(1)) && (session.getUserId() == rs.getInt(2)) && (getStorageType().getType() == rs.getInt(3))) {
                        AttachmentStorageIdentifier storageIdentifier = storageIdentifierFrom(rs);
                        storageIdentifiers.add(storageIdentifier);

                        Databases.closeSQLStuff(rs, stmt);
                        stmt = con.prepareStatement("DELETE FROM compositionSpaceAttachmentMeta WHERE uuid=?");
                        stmt.setBytes(1, UUIDs.toByteArray(id));
                        deleted = stmt.executeUpdate() > 0;
                    }
                } catch (SQLException e) {
                    throw CompositionSpaceErrorCode.SQL_ERROR.create(e, e.getMessage());
                } finally {
                    Databases.closeSQLStuff(rs, stmt);
                }
            } else {
                List<UUID> ids2Delete = new ArrayList<>(ids.size());
                for (UUID id : ids) {
                    PreparedStatement stmt = null;
                    ResultSet rs = null;
                    try {
                        stmt = con.prepareStatement("SELECT cid, user, refType, refId, dedicatedFileStorageId FROM compositionSpaceAttachmentMeta WHERE uuid=?");
                        stmt.setBytes(1, UUIDs.toByteArray(id));
                        rs = stmt.executeQuery();
                        if (rs.next() && (session.getContextId() == rs.getInt(1)) && (session.getUserId() == rs.getInt(2)) && (getStorageType().getType() == rs.getInt(3))) {
                            AttachmentStorageIdentifier storageIdentifier = storageIdentifierFrom(rs);
                            storageIdentifiers.add(storageIdentifier);

                            Databases.closeSQLStuff(rs, stmt);
                            ids2Delete.add(id);
                        }
                    } catch (SQLException e) {
                        throw CompositionSpaceErrorCode.SQL_ERROR.create(e, e.getMessage());
                    } finally {
                        Databases.closeSQLStuff(rs, stmt);
                    }
                }

                PreparedStatement stmt = null;
                try {
                    stmt = con.prepareStatement("DELETE FROM compositionSpaceAttachmentMeta WHERE uuid=?");
                    for (UUID id : ids2Delete) {
                        stmt.setBytes(1, UUIDs.toByteArray(id));
                        stmt.addBatch();
                    }
                    int[] updateCounts = stmt.executeBatch();
                    for (int i = updateCounts.length; !deleted && i-- > 0;) {
                        deleted = updateCounts[i] > 0;
                    }
                } catch (SQLException e) {
                    throw CompositionSpaceErrorCode.SQL_ERROR.create(e, e.getMessage());
                } finally {
                    Databases.closeSQLStuff(stmt);
                }
            }

            error = false; // All went fine
            return deleted;
        } finally {
            if (error) {
                // Prevent from prematurely deleting storage resources
                storageIdentifiers.clear();
            }
        }
    }

    @Override
    public void deleteUnreferencedAttachments(Session session) throws OXException {
        LockService optionalService = services.getOptionalService(LockService.class);
        if (optionalService == null) {
            // No lock service available
            deleteUnreferencedAttachments0(session);
            return;
        }

        // Use lock service to synchronize deletion of unreferenced attachments
        boolean acquired = false;
        AccessControl accessControl = optionalService.getAccessControlFor("msgcs.attach.unref", 1, session.getUserId(), session.getContextId());
        try {
            acquired = accessControl.tryAcquireGrant();
            if (false == acquired) {
                // Release manually and null'ify
                accessControl.release(false);
                accessControl = null;
            } else {
                // Grant acquired...
                deleteUnreferencedAttachments0(session);
            }
        } finally {
            Streams.close(accessControl);
        }
    }

    private void deleteUnreferencedAttachments0(Session session) throws OXException {
        DatabaseService databaseService = requireDatabaseService();
        Connection con = databaseService.getWritable(session.getContextId());
        int rollback = 0;
        try {
            Databases.startTransaction(con);
            rollback = 1;

            List<AttachmentStorageIdentifier> storageIdentifiers = new ArrayList<>();
            deleteUnreferencedAttachments(session, storageIdentifiers, con);

            con.commit();
            rollback = 2;

            for (AttachmentStorageIdentifier storageIdentifier : storageIdentifiers) {
                deleteSafely(storageIdentifier, session);
            }
        } catch (SQLException e) {
            throw CompositionSpaceErrorCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback == 1) {
                    Databases.rollback(con);
                }
                Databases.autocommit(con);
            }
            databaseService.backWritable(session.getContextId(), con);
        }
    }

    /**
     * Deletes all user-associated attachments, which are not referenced by an existent composition space.
     *
     * @param session The session providing user data
     * @param storageIdentifiers The listing of storage identifier
     * @param con The connection to use
     * @throws OXException If operation fails
     */
    private void deleteUnreferencedAttachments(Session session, List<AttachmentStorageIdentifier> storageIdentifiers, Connection con) throws OXException {
        if (null == con) {
            deleteUnreferencedAttachments0(session);
            return;
        }

        boolean error = true; // pessimistic
        try {
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                stmt = con.prepareStatement("SELECT compositionSpaceAttachmentMeta.uuid, refType, refId, dedicatedFileStorageId FROM compositionSpaceAttachmentMeta LEFT JOIN compositionSpace ON compositionSpaceAttachmentMeta.csid=compositionSpace.uuid WHERE compositionSpaceAttachmentMeta.cid=? AND compositionSpaceAttachmentMeta.user=? AND compositionSpace.uuid IS NULL");
                stmt.setInt(1, session.getContextId());
                stmt.setInt(2, session.getUserId());
                rs = stmt.executeQuery();
                if (rs.next()) {
                    List<UUID> ids2Delete = new LinkedList<>();
                    do {
                        if (getStorageType().getType() == rs.getInt(2)) {
                            UUID attachmentId = UUIDs.toUUID(rs.getBytes(1));
                            ids2Delete.add(attachmentId);
                            AttachmentStorageIdentifier storageIdentifier = storageIdentifierFrom(rs);
                            storageIdentifiers.add(storageIdentifier);
                        }
                    } while (rs.next());
                    Databases.closeSQLStuff(rs, stmt);
                    rs = null;
                    stmt = null;

                    if (!ids2Delete.isEmpty()) {
                        stmt = con.prepareStatement("DELETE FROM compositionSpaceAttachmentMeta WHERE uuid=?");
                        for (UUID id : ids2Delete) {
                            stmt.setBytes(1, UUIDs.toByteArray(id));
                            stmt.addBatch();
                        }
                        stmt.executeBatch();
                    }
                }
            } catch (SQLException e) {
                throw CompositionSpaceErrorCode.SQL_ERROR.create(e, e.getMessage());
            } finally {
                Databases.closeSQLStuff(rs, stmt);
            }

            error = false; // All went fine
        } finally {
            if (error) {
                // Prevent from prematurely deleting storage resources
                storageIdentifiers.clear();
            }
        }
    }

    private static AttachmentStorageIdentifier storageIdentifierFrom(ResultSet rs) throws SQLException {
        String storageIdentifier = rs.getString("refId");
        int dedicatedFileStorageId = rs.getInt("dedicatedFileStorageId");
        if (dedicatedFileStorageId <= 0) {
            return new AttachmentStorageIdentifier(storageIdentifier, KnownArgument.FILE_STORAGE_IDENTIFIER, I(0));
        }
        return new AttachmentStorageIdentifier(storageIdentifier, KnownArgument.FILE_STORAGE_IDENTIFIER, I(dedicatedFileStorageId));
    }

    private static class StorageIdentifierAndSize {

        final AttachmentStorageIdentifier storageIdentifier;
        final long size;

        StorageIdentifierAndSize(AttachmentStorageIdentifier storageIdentifier, long size) {
            super();
            this.storageIdentifier = storageIdentifier;
            this.size = size;
        }
    }

}
