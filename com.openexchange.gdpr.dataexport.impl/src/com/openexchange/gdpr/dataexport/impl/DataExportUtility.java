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

package com.openexchange.gdpr.dataexport.impl;

import java.net.URI;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.UUID;
import org.apache.commons.lang3.time.FastDateFormat;
import org.slf4j.Logger;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorage;
import com.openexchange.filestore.FileStorageInfoService;
import com.openexchange.filestore.FileStorageService;
import com.openexchange.filestore.FileStorages;
import com.openexchange.gdpr.dataexport.DataExportConfig;
import com.openexchange.gdpr.dataexport.DataExportTask;
import com.openexchange.java.Strings;
import com.openexchange.java.util.UUIDs;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.tools.TimeZoneUtils;
import com.openexchange.tools.filename.FileNameTools;
import com.openexchange.user.User;
import net.gcardone.junidecode.Junidecode;

/**
 * {@link DataExportUtility} - Utility class for data export.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class DataExportUtility {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(DataExportUtility.class);
    }

    /**
     * Content Type for result files.
     */
    public static final String CONTENT_TYPE = "application/zip";

    /**
     * Initializes a new {@link DataExportUtility}.
     */
    private DataExportUtility() {
        super();
    }

    /**
     * The buffer size of 64K.
     */
    public static final int BUFFER_SIZE = 65536;

    /**
     * Gets the file storage instance for specified data export task.
     *
     * @param task The data export task
     * @return The associated file storage
     * @throws OXException If file storage cannot be returned
     */
    public static FileStorage getFileStorageFor(DataExportTask task) throws OXException {
        return getFileStorageFor(task.getFileStorageId(), task.getContextId());
    }

    /**
     * Gets the file storage instance for specified arguments.
     *
     * @param fileStorageId The file storage identifier
     * @param contextId The context identifier
     * @return The associated file storage
     * @throws OXException If file storage cannot be returned
     */
    public static FileStorage getFileStorageFor(int fileStorageId, int contextId) throws OXException {
        FileStorageInfoService infoService = FileStorages.getFileStorageInfoService();
        if (null == infoService) {
            throw ServiceExceptionCode.absentService(FileStorageInfoService.class);
        }

        // Determine base URI and scheme
        URI baseUri = infoService.getFileStorageInfo(fileStorageId).getUri();
        String scheme = baseUri.getScheme();
        if (scheme == null) {
            scheme = "file";
        }

        return getFileStorageFor(baseUri, scheme, contextId);
    }

    /**
     * Gets the file storage instance for specified arguments.
     *
     * @param baseUri The base URI of the file storage
     * @param scheme The schema extracted from base URI
     * @param contextId The context identifier
     * @return The associated file storage
     * @throws OXException If file storage cannot be returned
     */
    public static FileStorage getFileStorageFor(URI baseUri, String scheme, int contextId) throws OXException {
        FileStorageService fileStorageService = FileStorages.getFileStorageService();
        if (fileStorageService == null) {
            throw ServiceExceptionCode.absentService(FileStorageService.class);
        }

        // Prefer a static prefix in case of "file"-schemed file storage
        String prefix;
        if ("file".equals(scheme)) {
            prefix = "gdpr_dataexport";
        } else {
            prefix = new StringBuilder(32).append(contextId).append("_gdpr_dataexport").toString();
        }

        URI uri = FileStorages.getFullyQualifyingUriForPrefix(prefix, baseUri);
        return fileStorageService.getFileStorage(uri);
    }

    /**
     * Extracts the context identifier from given path prefix.
     *
     * @param prefix The path prefix; e.g. "4321_gdpr_dataexport"
     * @return The extracted context identifier or <code>-1</code>
     */
    public static int extractContextIdFrom(String prefix) {
        if (Strings.isEmpty(prefix)) {
            return -1;
        }

        String toExtractFrom = prefix.trim();
        if (!toExtractFrom.endsWith("_gdpr_dataexport")) {
            return -1;
        }

        int length = toExtractFrom.length();
        StringBuilder ctxChars = null;
        int i = 0;
        {
            boolean keepOn = true;
            while (keepOn && i < length) {
                char ch = toExtractFrom.charAt(i);
                int digit = Strings.digitForChar(ch);
                if (digit >= 0) {
                    if (ctxChars == null) {
                        ctxChars = new StringBuilder();
                    }
                    ctxChars.append(ch);
                    i++;
                } else {
                    keepOn = false;
                }
            }
            if (ctxChars == null || keepOn) {
                return -1;
            }
        }

        if (toExtractFrom.charAt(i) != '_') {
            return -1;
        }

        try {
            return Integer.parseInt(ctxChars.toString(), 10);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Deletes specified location in given file storage ignoring any exceptions.
     *
     * @param fileStorageLocation The location to delete
     * @param fileStorage The file storage to delete in
     */
    public static void deleteQuietly(String fileStorageLocation, FileStorage fileStorage) {
        if (fileStorageLocation != null && fileStorage != null) {
            try {
                fileStorage.deleteFile(fileStorageLocation);
            } catch (Exception e) {
                LoggerHolder.LOG.warn("Failed to delete file storage location {} from storage {}", fileStorageLocation, fileStorage.getUri(), e);
            }
        }
    }

    /**
     * Gets the unformatted string representation for given identifier.
     * <p>
     * Example:<br>
     * <code>
     * &nbsp;&nbsp;067e6162-3b6f-4ae2-a171-2470b63dff00
     * </code><br>
     * is converted to<br>
     * <code>
     * &nbsp;&nbsp;067e61623b6f4ae2a1712470b63dff00
     * </code>
     *
     * @param id The identifier
     * @return The unformatted string representation
     */
    public static String stringFor(UUID id) {
        return UUIDs.getUnformattedString(id);
    }

    private static final char DEFAULT_DELIM = '-';

    /**
     * Generates the file name from given arguments.
     *
     * @param prefix The optional prefix; e.g. <code>"archive"</code>
     * @param ext The optional file extension; e.g. <code>".zip"</code>
     * @param number The package number
     * @param total The total number of packages
     * @param creationTime The optional creation time to include in file name
     * @param user The user for whom to generate the file name for
     * @return The file name
     */
    public static String generateFileNameFor(String prefix, String ext, int number, int total, Date creationTime, User user) {
        return generateFileNameFor(prefix, ext, DEFAULT_DELIM, number, total, creationTime, user, false);
    }

    /**
     * Generates the file name from given arguments.
     *
     * @param prefix The optional prefix; e.g. <code>"archive"</code>
     * @param ext The optional file extension; e.g. <code>".zip"</code>
     * @param delim The delimiter character; e.g. <code>'_'</code>
     * @param number The package number
     * @param total The total number of packages
     * @param creationTime The optional creation time to include in file name
     * @param user The user for whom to generate the file name for
     * @param preferLocaleSpecificDateFormat <code>true</code> to prefer {@link DateFormat#SHORT short} {@link DateFormat#getDateInstance(int, java.util.Locale) locale-specific date format}; otherwise <code>false</code> to use standard pattern <code>"yyyy-MM-dd"</code>
     * @return The file name
     */
    public static String generateFileNameFor(String prefix, String ext, char delim, int number, int total, Date creationTime, User user, boolean preferLocaleSpecificDateFormat) {
        StringBuilder fileNameBuilder = new StringBuilder(32);
        if (Strings.isEmpty(prefix)) {
            fileNameBuilder.append("export");
        } else {
            fileNameBuilder.append(prefix);
        }

        if (creationTime != null) {
            if (fileNameBuilder.charAt(fileNameBuilder.length() - 1) != delim) {
                fileNameBuilder.append(delim);
            }
            FastDateFormat dateFormat;
            if (preferLocaleSpecificDateFormat) {
                dateFormat = FastDateFormat.getDateInstance(FastDateFormat.SHORT, TimeZoneUtils.getTimeZone(user.getTimeZone()), user.getLocale());
            } else {
                dateFormat = FastDateFormat.getInstance("yyyy-MM-dd", TimeZoneUtils.getTimeZone(user.getTimeZone()), user.getLocale());
            }
            fileNameBuilder.append(dateFormat.format(creationTime));
        }

        if (total > 1) {
            if (fileNameBuilder.charAt(fileNameBuilder.length() - 1) != delim) {
                fileNameBuilder.append(delim);
            }
            NumberFormat f = NumberFormat.getInstance(user.getLocale());
            if (f instanceof DecimalFormat) {
                StringBuilder pattern = new StringBuilder(total);
                for (int i = total; i > 0; i = i / 10) {
                    pattern.append('0');
                }
                ((DecimalFormat) f).applyPattern(pattern.toString());
            }
            fileNameBuilder.append(f.format(number));
        }

        if (Strings.isEmpty(ext)) {
            fileNameBuilder.append(".zip");
        } else {
            if (!ext.startsWith(".")) {
                fileNameBuilder.append('.');
            }
            fileNameBuilder.append(ext);
        }
        return FileNameTools.sanitizeFilename(fileNameBuilder.toString());
    }

    /**
     * Possibly converts any Unicode characters in given ZIP archive entry name to somewhat reasonable ASCII7-only characters in case given
     * configuration advertises {@link DataExportConfig#isReplaceUnicodeWithAscii()} as <code>true</code>.
     *
     * @param name The ZIP archive entry name to examine
     * @return The possibly converted ZIP archive entry name
     */
    public static String prepareEntryName(String name, DataExportConfig config) {
        if (name == null || config == null || config.isReplaceUnicodeWithAscii() == false) {
            // Return as-is
            return name;
        }

        try {
            return Junidecode.unidecode(name);
        } catch (Exception e) {
            LoggerHolder.LOG.error("Failed to convert ZIP archive entry name to somewhat reasonable ASCII7-only: {}. Using name as-is.", name, e);
            return name;
        }
    }

}
