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

package com.openexchange.filestore.s3.internal;

import static com.openexchange.filestore.s3.internal.AbortIfNotConsumedInputStream.closeContentStream;
import static com.openexchange.filestore.s3.internal.S3ExceptionCode.wrap;
import static com.openexchange.java.Autoboxing.L;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import javax.servlet.http.HttpServletResponse;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.policy.Policy;
import com.amazonaws.auth.policy.Principal;
import com.amazonaws.auth.policy.Resource;
import com.amazonaws.auth.policy.Statement;
import com.amazonaws.auth.policy.actions.S3Actions;
import com.amazonaws.auth.policy.conditions.BooleanCondition;
import com.amazonaws.auth.policy.conditions.StringCondition;
import com.amazonaws.auth.policy.conditions.StringCondition.StringComparisonType;
import com.amazonaws.services.s3.internal.BucketNameUtils;
import com.amazonaws.services.s3.model.AbortMultipartUploadRequest;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.CreateBucketRequest;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.MultiObjectDeleteException;
import com.amazonaws.services.s3.model.MultiObjectDeleteException.DeleteError;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PartETag;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.Region;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.SetBucketPolicyRequest;
import com.amazonaws.services.s3.model.UploadPartRequest;
import com.amazonaws.services.s3.model.UploadPartResult;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorage;
import com.openexchange.filestore.FileStorageCodes;
import com.openexchange.filestore.s3.internal.client.S3FileStorageClient;
import com.openexchange.filestore.utils.TempFileHelper;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.java.util.UUIDs;

/**
 * {@link S3FileStorage}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class S3FileStorage implements FileStorage {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(S3FileStorage.class);

    /**
     * The delimiter character to separate the prefix from the keys
     */
    private static final String DELIMITER = "/";

    private final URI uri;
    private final String prefix;
    private final String bucketName;
    private final S3FileStorageClient client;

    /**
     * Initializes a new {@link S3FileStorage}.
     *
     * @param uri The URI that fully qualifies this file storage
     * @param prefix The prefix to use; e.g. <code>"1337ctxstore"</code>
     * @param bucketName The bucket name to use
     * @param client The file storage client
     */
    public S3FileStorage(URI uri, String prefix, String bucketName, S3FileStorageClient client) {
        super();
        BucketNameUtils.validateBucketName(bucketName);
        if (Strings.isEmpty(prefix) || prefix.contains(DELIMITER)) {
            throw new IllegalArgumentException(prefix);
        }
        this.uri = uri;
        this.prefix = prefix;
        this.bucketName = bucketName;
        this.client = client;
        LOG.debug("S3 file storage initialized for \"{}/{}{}\"", bucketName, prefix, DELIMITER);
    }

    @Override
    public URI getUri() {
        return uri;
    }

    @Override
    public String saveNewFile(InputStream input) throws OXException {
        /*
         * perform chunked upload as needed
         */
        String key = generateKey(true);
        S3ChunkedUpload chunkedUpload = null;
        S3UploadChunk chunk = null;
        File tmpFile = null;
        try {
            /*
             * spool to file
             */
            if (!(input instanceof FileInputStream)) {
                Optional<File> optionalTempFile = TempFileHelper.getInstance().newTempFile();
                if (optionalTempFile.isPresent()) {
                    tmpFile = optionalTempFile.get();
                    input = Streams.transferToFileAndCreateStream(input, tmpFile);
                }
            }
            /*
             * proceed
             */
            chunkedUpload = new S3ChunkedUpload(input, client.getEncryptionConfig().isClientEncryptionEnabled(), client.getChunkSize());
            chunk = chunkedUpload.next();
            if (false == chunkedUpload.hasNext()) {
                /*
                 * whole file fits into buffer (this includes a zero byte file), upload directly
                 */
                uploadSingle(key, chunk);
            } else {
                /*
                 * upload in multipart chunks to provide the correct content length
                 */
                InitiateMultipartUploadRequest initiateMultipartUploadRequest = new InitiateMultipartUploadRequest(bucketName, key).withObjectMetadata(prepareMetadataForSSE(new ObjectMetadata()));
                String uploadID = client.getSdkClient().initiateMultipartUpload(initiateMultipartUploadRequest).getUploadId();
                boolean completed = false;
                try {
                    List<PartETag> partETags = new ArrayList<PartETag>();
                    int partNumber = 1;
                    /*
                     * upload n-1 parts
                     */
                    do {
                        partETags.add(uploadPart(key, uploadID, partNumber++, chunk, false).getPartETag());
                        chunk = chunkedUpload.next();
                    } while (chunkedUpload.hasNext());
                    /*
                     * upload last part & complete upload
                     */
                    partETags.add(uploadPart(key, uploadID, partNumber++, chunk, true).getPartETag());
                    client.getSdkClient().completeMultipartUpload(new CompleteMultipartUploadRequest(bucketName, key, uploadID, partETags));
                    completed = true;
                } finally {
                    if (false == completed) {
                        try {
                            client.getSdkClient().abortMultipartUpload(new AbortMultipartUploadRequest(bucketName, key, uploadID));
                        } catch (AmazonClientException e) {
                            LOG.warn("Error aborting multipart upload", e);
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw FileStorageCodes.IOERROR.create(e, e.getMessage());
        } finally {
            Streams.close(chunk, chunkedUpload, input);
            TempFileHelper.deleteQuietly(tmpFile);
        }
        return removePrefix(key);
    }

    @Override
    public InputStream getFile(String name) throws OXException {
        String key = addPrefix(name);
        S3ObjectInputStream objectContent = null;
        try {
            objectContent = getObject(key).getObjectContent();
            InputStream wrapper = wrapperWithoutRangeSupport(objectContent, key);
            objectContent = null; // Avoid premature closing
            return wrapper;
        } catch (AmazonClientException e) {
            throw wrap(e, key);
        } finally {
            closeContentStream(objectContent);
        }
    }

    private InputStream wrapperWithoutRangeSupport(S3ObjectInputStream objectContent, String key) {
        return new ResumableAbortIfNotConsumedInputStream(objectContent, bucketName, key, client.getSdkClient());
    }

    @Override
    public InputStream getFile(String name, long offset, long length) throws OXException {
        // Check validity of given offset/length arguments
        long fileSize = getFileSize(name);
        if (offset >= fileSize || length >= 0 && length > fileSize - offset) {
            throw FileStorageCodes.INVALID_RANGE.create(L(offset), L(length), name, L(fileSize));
        }

        // Check for 0 (zero) requested bytes
        if (length == 0) {
            return Streams.EMPTY_INPUT_STREAM;
        }

        // Initialize appropriate Get-Object request
        String key = addPrefix(name);
        GetObjectRequest request = new GetObjectRequest(bucketName, key);
        long rangeEnd = (length > 0 ? (offset + length) : fileSize) - 1;
        request.setRange(offset, rangeEnd);

        // Return content stream
        S3ObjectInputStream objectContent = null;
        try {
            objectContent = client.getSdkClient().getObject(request).getObjectContent();
            long[] range = new long[] { offset, rangeEnd };
            InputStream wrapper = wrapperWithRangeSupport(objectContent, range, key);
            objectContent = null; // Avoid premature closing
            return wrapper;
        } catch (AmazonClientException e) {
            if (AmazonServiceException.class.isInstance(e) && HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE == ((AmazonServiceException) e).getStatusCode()) {
                throw FileStorageCodes.INVALID_RANGE.create(e, L(offset), L(length), name, L(fileSize));
            }
            throw wrap(e, key);
        } finally {
            closeContentStream(objectContent);
        }
    }

    private InputStream wrapperWithRangeSupport(S3ObjectInputStream objectContent, long[] range, String key) {
        return new RangeAcceptingResumableAbortIfNotConsumedInputStream(objectContent, range, bucketName, key, client.getSdkClient());
    }

    @Override
    public SortedSet<String> getFileList() throws OXException {
        SortedSet<String> files = new TreeSet<String>();
        /*
         * results may be paginated - repeat listing objects as long as result is truncated
         */
        ListObjectsRequest listObjectsRequest = new ListObjectsRequest().withBucketName(bucketName).withDelimiter(DELIMITER).withPrefix(prefix + DELIMITER);
        ObjectListing objectListing;
        do {
            objectListing = client.getSdkClient().listObjects(listObjectsRequest);
            for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
                files.add(removePrefix(objectSummary.getKey()));
            }
            listObjectsRequest.setMarker(objectListing.getNextMarker());
        } while (objectListing.isTruncated());
        return files;
    }

    @Override
    public long getFileSize(final String name) throws OXException {
        return getContentLength(getMetadata(addPrefix(name)));
    }

    @Override
    public String getMimeType(String name) throws OXException {
        //TODO: makes no sense at storage layer
        return getMetadata(addPrefix(name)).getContentType();
    }

    @Override
    public boolean deleteFile(String name) throws OXException {
        String key = addPrefix(name);
        try {
            client.getSdkClient().deleteObject(bucketName, key);
            return true;
        } catch (AmazonClientException e) {
            throw wrap(e, key);
        }
    }

    @Override
    public Set<String> deleteFiles(String[] names) throws OXException {
        if (null != names && 0 < names.length) {
            DeleteObjectsRequest deleteRequest = new DeleteObjectsRequest(bucketName).withKeys(addPrefix(names));
            try {
                client.getSdkClient().deleteObjects(deleteRequest);
            } catch (MultiObjectDeleteException e) {
                List<DeleteError> errors = e.getErrors();
                if (null != errors && 0 < errors.size()) {
                    Set<String> notDeleted = new HashSet<String>();
                    for (DeleteError error : errors) {
                        notDeleted.add(removePrefix(error.getKey()));
                    }
                    return notDeleted;
                }
            } catch (AmazonClientException e) {
                throw wrap(e);
            }
        }
        return Collections.emptySet();
    }

    @Override
    public void remove() throws OXException {
        try {
            /*
             * try and delete all contained files repeatedly
             */
            final int RETRY_COUNT = 10;
            for (int i = 0; i < RETRY_COUNT; i++) {
                try {
                    SortedSet<String> fileList = getFileList();
                    if (null == fileList || 0 == fileList.size()) {
                        break; // no more files found
                    }
                    client.getSdkClient().deleteObjects(new DeleteObjectsRequest(bucketName).withKeys(addPrefix(fileList)));
                } catch (MultiObjectDeleteException e) {
                    if (i < RETRY_COUNT - 1) {
                        LOG.warn("Not all files in bucket deleted yet, trying again.", e);
                    } else {
                        throw FileStorageCodes.NOT_ELIMINATED.create("Not all files in bucket deleted after " + i + " tries, giving up.", e);
                    }
                }
            }
        } catch (OXException e) {
            throw FileStorageCodes.NOT_ELIMINATED.create(e);
        } catch (AmazonClientException e) {
            throw FileStorageCodes.NOT_ELIMINATED.create(wrap(e));
        }
    }

    @Override
    public void recreateStateFile() throws OXException {
        // no
    }

    @Override
    public boolean stateFileIsCorrect() throws OXException {
        return true;
    }

    @Override
    public long appendToFile(InputStream file, String name, long offset) throws OXException {
        File tmpFile = null;
        try {
            /*
             * TODO: This would be more efficient using the "CopyPartRequest", which is not yet supported by ceph
             * http://ceph.com/docs/next/radosgw/s3/#features-support
             */
            /*
             * get existing object
             */
            String tempName = null;
            String key = addPrefix(name);
            S3Object s3Object = null;
            SequenceInputStream inputStream = null;
            try {
                s3Object = getObject(key);
                long currentLength = getContentLength(s3Object.getObjectMetadata());
                if (currentLength != offset) {
                    throw FileStorageCodes.INVALID_OFFSET.create(Long.valueOf(offset), name, Long.valueOf(currentLength));
                }
                /*
                 * spool to file
                 */
                if (!(file instanceof FileInputStream)) {
                    Optional<File> optionalTempFile = TempFileHelper.getInstance().newTempFile();
                    if (optionalTempFile.isPresent()) {
                        tmpFile = optionalTempFile.get();
                        file = Streams.transferToFileAndCreateStream(file, tmpFile);
                    }
                }
                /*
                 * append both streams at temporary location
                 */
                inputStream = new SequenceInputStream(s3Object.getObjectContent(), file);
                tempName = saveNewFile(inputStream);
            } finally {
                Streams.close(inputStream, s3Object);
            }
            /*
             * replace old file, cleanup
             */
            try {
                String tempKey = addPrefix(tempName);
                CopyObjectRequest copyObjectRequest = new CopyObjectRequest(bucketName, tempKey, bucketName, key);
                ObjectMetadata metadata = prepareMetadataForSSE(getMetadata(tempKey).clone());
                copyObjectRequest.setNewObjectMetadata(metadata);
                client.getSdkClient().copyObject(copyObjectRequest);
                client.getSdkClient().deleteObject(bucketName, tempKey);
                return getContentLength(getMetadata(key));
            } catch (AmazonClientException e) {
                throw wrap(e, key);
            }
        } catch (IOException e) {
            throw FileStorageCodes.IOERROR.create(e, e.getMessage());
        } finally {
            Streams.close(file);
            TempFileHelper.deleteQuietly(tmpFile);
        }
    }

    @Override
    public void setFileLength(long length, String name) throws OXException {
        /*
         * TODO: This would be much more efficient using the "CopyPartRequest", which is not yet supported by ceph
         * see: http://ceph.com/docs/next/radosgw/s3/#features-support
         */
        /*
         * copy previous file to temporary file
         */
        String key = addPrefix(name);
        String tempKey = generateKey(true);
        try {
            CopyObjectRequest copyObjectRequest = new CopyObjectRequest(bucketName, key, bucketName, tempKey);
            ObjectMetadata metadata = prepareMetadataForSSE(getMetadata(key).clone());
            copyObjectRequest.setNewObjectMetadata(metadata);
            client.getSdkClient().copyObject(copyObjectRequest);
            /*
             * upload $length bytes from previous file to new current file
             */
            metadata = new ObjectMetadata();
            metadata.setContentLength(length);
            metadata = prepareMetadataForSSE(metadata);
            InputStream inputStream = null;
            try {
                inputStream = getFile(tempKey, 0, length);
                client.getSdkClient().putObject(bucketName, key, inputStream, metadata);
            } finally {
                Streams.close(inputStream);
            }
        } catch (AmazonClientException e) {
            throw wrap(e, key);
        } finally {
            try {
                client.getSdkClient().deleteObject(bucketName, tempKey);
            } catch (AmazonClientException e) {
                LOG.warn("Error cleaning up temporary file", e);
            }
        }
    }

    /**
     * Ensures the configured bucket exists, creating it dynamically if needed.
     *
     * @throws OXException If initialization fails
     */
    public void ensureBucket() throws OXException {
        boolean bucketExists = false;
        try {
            bucketExists = client.getSdkClient().doesBucketExist(bucketName);
        } catch (IllegalArgumentException e) {
            throw S3ExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (AmazonClientException e) {
            throw S3ExceptionCode.wrap(e);
        } catch (RuntimeException e) {
            throw S3ExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }

        if (false == bucketExists) {
            String region = client.getSdkClient().getRegionName();
            try {
                client.getSdkClient().createBucket(new CreateBucketRequest(bucketName, Region.fromValue(region)));
                if (client.getEncryptionConfig().isServerSideEncryptionEnabled()) {
                    client.getSdkClient().setBucketPolicy(new SetBucketPolicyRequest(bucketName, getSSEOnlyBucketPolicy(bucketName)));
                }
            } catch (IllegalArgumentException e) {
                throw S3ExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
            } catch (AmazonS3Exception e) {
                if ("InvalidLocationConstraint".equals(e.getErrorCode())) {
                    // Failed to create such a bucket
                    throw S3ExceptionCode.BUCKET_CREATION_FAILED.create(bucketName, region);
                }
                throw S3ExceptionCode.wrap(e);
            } catch (AmazonServiceException e) {
                throw S3ExceptionCode.wrap(e);
            } catch (AmazonClientException e) {
                throw S3ExceptionCode.wrap(e);
            } catch (RuntimeException e) {
                throw S3ExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        }
    }

    /**
     * Gets the bucket policy for a server side encryption only bucket.
     *
     * @param bucket_name The name of the bucket
     * @return The encryption only policy
     */
    private String getSSEOnlyBucketPolicy(String bucket_name) {
        Policy bucket_policy = new Policy().withStatements(
            new Statement(Statement.Effect.Deny)
                .withId("DenyIncorrectEncryptionHeader")
                .withPrincipals(Principal.AllUsers)
                .withActions(S3Actions.PutObject)
                .withResources(new Resource("arn:aws:s3:::" + bucket_name + "/*"))
                .withConditions(new StringCondition(StringComparisonType.StringNotEquals, "s3:x-amz-server-side-encryption", "AES256")),
            new Statement(Statement.Effect.Deny)
                .withId("DenyUnEncryptedObjectUploads")
                .withPrincipals(Principal.AllUsers)
                .withActions(S3Actions.PutObject)
                .withResources(new Resource("arn:aws:s3:::" + bucket_name + "/*"))
                .withConditions(new BooleanCondition("s3:x-amz-server-side-encryption", true))
                );
        return bucket_policy.toJson();
    }

    /**
     * Gets metadata for an existing file.
     *
     * @param key The (full) key for the new file; no additional prefix will be prepended implicitly
     * @return The upload ID for the multipart upload
     * @throws OXException
     */
    private ObjectMetadata getMetadata(String key) throws OXException {
        try {
            return client.getSdkClient().getObjectMetadata(bucketName, key);
        } catch (AmazonClientException e) {
            throw wrap(e, key);
        }
    }

    /**
     * Gets a stored S3 object.
     *
     * @param key The key of the file
     * @return The S3 object
     * @throws OXException
     */
    private S3Object getObject(String key) throws OXException {
        try {
            return client.getSdkClient().getObject(bucketName, key);
        } catch (AmazonClientException e) {
            throw wrap(e, key);
        }
    }

    /**
     * Creates a new arbitrary key (an unformatted string representation of a new random UUID), optionally prepended with the configured
     * prefix and delimiter.
     *
     * @param withPrefix <code>true</code> to prepend the prefix, <code>false</code>, otherwise
     *
     * @return A new UID string, optionally with prefix and delimiter, e.g. <code>[prefix]/067e61623b6f4ae2a1712470b63dff00</code>.
     */
    private String generateKey(boolean withPrefix) {
        String uuid = UUIDs.getUnformattedString(UUID.randomUUID());
        return withPrefix ? prefix + DELIMITER + uuid : uuid;
    }

    /**
     * Prepends the configured prefix and delimiter character sequence to the supplied name.
     *
     * @param name The name to prepend the prefix
     * @return The name with prefix
     */
    private String addPrefix(String name) {
        return prefix + DELIMITER + name;
    }

    /**
     * Prepends the configured prefix and delimiter character sequence to the supplied names.
     *
     * @param names The names to prepend the prefix
     * @return The names with prefix in an array
     */
    private String[] addPrefix(Collection<? extends String> names) {
        String[] keys = new String[names.size()];
        int i = 0;
        for (String name : names) {
            keys[i++] = addPrefix(name);
        }
        return keys;
    }

    /**
     * Prepends the configured prefix and delimiter character sequence to the supplied names.
     *
     * @param names The names to prepend the prefix
     * @return The names with prefix in an array
     */
    private String[] addPrefix(String[] names) {
        String[] keys = new String[names.length];
        for (int i = 0; i < names.length; i++) {
            keys[i] = addPrefix(names[i]);
        }
        return keys;
    }

    /**
     * Strips the prefix and delimiter character sequence to the supplied key.
     *
     * @param key The key to strip the prefix from
     * @return The key without prefix
     */
    private String removePrefix(String key) {
        int idx = prefix.length() + DELIMITER.length();
        if (idx > key.length() || false == key.startsWith(prefix + DELIMITER)) {
            throw new IllegalArgumentException(key);
        }
        return key.substring(idx);
    }

    /**
     * Puts a single upload chunk to amazon s3.
     *
     * @param key The object key
     * @param chunk The chunk to store
     * @return The put object result passed from the client
     */
    private PutObjectResult uploadSingle(String key, S3UploadChunk chunk) throws OXException {
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(chunk.getSize());
            metadata.setContentMD5(chunk.getMD5Digest());
            prepareMetadataForSSE(metadata);
            return client.getSdkClient().putObject(bucketName, key, chunk.getData(), metadata);
        } finally {
            Streams.close(chunk);
        }
    }

    private ObjectMetadata prepareMetadataForSSE(ObjectMetadata metadata) {
        if (client.getEncryptionConfig().isServerSideEncryptionEnabled()) {
            metadata.setSSEAlgorithm(ObjectMetadata.AES_256_SERVER_SIDE_ENCRYPTION);
        }
        return metadata;
    }

    /**
     * Uploads a single part of a multipart upload.
     *
     * @param key The key to store
     * @param uploadID the upload ID
     * @param partNumber The part number of the chunk
     * @param chunk the chunk to store
     * @param lastPart <code>true</code> if this is the last part, <code>false</code>, otherwise
     * @return The put object result passed from the client
     */
    private UploadPartResult uploadPart(String key, String uploadID, int partNumber, S3UploadChunk chunk, boolean lastPart) throws OXException {
        try {
            UploadPartRequest request = new UploadPartRequest()
                                                .withBucketName(bucketName)
                                                .withKey(key)
                                                .withUploadId(uploadID)
                                                .withInputStream(chunk.getData())
                                                .withPartSize(chunk.getSize())
                                                .withPartNumber(partNumber++)
                                                .withLastPart(lastPart);
            String md5Digest = chunk.getMD5Digest();
            if (null != md5Digest) {
                request.withMD5Digest(md5Digest);
            }
            return client.getSdkClient().uploadPart(request);
        } finally {
            Streams.close(chunk);
        }
    }

    /**
     * Extracts the effective content length from the supplied S3 object metadata, which is the length of the unencrypted content if specified, or the plain content length, otherwise.
     *
     * @param metadata The metadata to get the content length from
     * @return The length of the unencrypted content if specified, or the plain content length, otherwise
     */
    private long getContentLength(ObjectMetadata metadata) throws OXException {
        String unencryptedContentLength = metadata.getUserMetaDataOf(com.amazonaws.services.s3.Headers.UNENCRYPTED_CONTENT_LENGTH);
        if (Strings.isNotEmpty(unencryptedContentLength)) {
            try {
                return Long.parseLong(unencryptedContentLength);
            } catch (NumberFormatException e) {
                throw FileStorageCodes.NO_NUMBER.create(e);
            }
        }
        return metadata.getContentLength();
    }

}
