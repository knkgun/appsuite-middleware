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

package com.openexchange.mail.compose;

import java.util.List;
import java.util.Map;
import com.openexchange.java.Strings;

/**
 * {@link Message} - Represents a message, which is being composed.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public interface Message {

    /** The type of a message's (textual) content */
    public static enum ContentType {

        /**
         * The <code>"text/plain"</code> content type.
         */
        TEXT_PLAIN("text/plain", false),
        /**
         * The <code>"text/html"</code> content type.
         */
        TEXT_HTML("text/html", true),
        /**
         * The <code>"multipart/alternative"</code> content type.
         */
        MULTIPART_ALTERNATIVE("multipart/alternative", true);

        private final String id;
        private final boolean impliesHtml;

        private ContentType(String id, boolean impliesHtml) {
            this.id = id;
            this.impliesHtml = impliesHtml;
        }

        /**
         * Gets the identifier
         *
         * @return The identifier
         */
        public String getId() {
            return id;
        }

        /**
         * Whether this content type implies HTML content.
         *
         * @return <code>true</code> if HTML content is implied; otherwise <code>false</code>
         */
        public boolean isImpliesHtml() {
            return impliesHtml;
        }

        /**
         * Gets the Content-Type for given identifier
         *
         * @param contentType The identifier to look-up
         * @return The associated Content-Type or <code>null</code>
         */
        public static ContentType contentTypeFor(String contentType) {
            if (Strings.isEmpty(contentType)) {
                return null;
            }

            String lk = contentType.trim();
            if ("alternative".equalsIgnoreCase(lk)) {
                // Old behavior for requesting to build a multipart/alternative message.
                return ContentType.MULTIPART_ALTERNATIVE;
            }
            for (ContentType ct : ContentType.values()) {
                if (lk.equalsIgnoreCase(ct.id)) {
                    return ct;
                }
            }
            return null;
        }
    }

    /** A message's priority */
    public static enum Priority {
        /**
         * The <code>"low"</code> priority.
         */
        LOW("low", 5),
        /**
         * The <code>"normal"</code> priority.
         */
        NORMAL("normal", 3),
        /**
         * The <code>"high"</code> priority.
         */
        HIGH("high", 1);

        private final String id;
        private final int level;

        private Priority(String id, int level) {
            this.id = id;
            this.level = level;
        }

        /**
         * Gets the level
         *
         * @return The level
         */
        public int getLevel() {
            return level;
        }

        /**
         * Gets the identifier
         *
         * @return The identifier
         */
        public String getId() {
            return id;
        }

        /**
         * Gets the priority for given identifier
         *
         * @param priority The identifier to look-up
         * @return The associated priority or <code>null</code>
         */
        public static Priority priorityFor(String priority) {
            if (Strings.isEmpty(priority)) {
                return null;
            }

            String lk = priority.trim();
            for (Priority p : Priority.values()) {
                if (lk.equalsIgnoreCase(p.id)) {
                    return p;
                }
            }
            return null;
        }

        /**
         * Gets the priority for given identifier
         *
         * @param level The level to look-up
         * @return The associated priority or <code>null</code>
         */
        public static Priority priorityForLevel(int level) {
            for (Priority p : Priority.values()) {
                if (level == p.level) {
                    return p;
                }
            }
            return null;
        }

    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Gets the "From" address.
     *
     * @return The "From" address
     */
    Address getFrom();

    /**
     * Gets the "Sender" address.
     *
     * @return The "Sender" address
     */
    Address getSender();
    
    /**
     * Gets the "Reply-To" address.
     *
     * @return The "Reply-To" address
     */
    Address getReplyTo();

    /**
     * Gets the "To" addresses.
     *
     * @return The "To" addresses (might be <code>null</code> or an empty list to signal no such recipient addresses)
     */
    List<Address> getTo();

    /**
     * Gets the "Cc" addresses.
     *
     * @return The "Cc" addresses (might be <code>null</code> or an empty list to signal no such recipient addresses)
     */
    List<Address> getCc();

    /**
     * Gets the "Bcc" addresses.
     *
     * @return The "Bcc" addresses (might be <code>null</code> or an empty list to signal no such recipient addresses)
     */
    List<Address> getBcc();

    /**
     * Gets the subject.
     *
     * @return The subject
     */
    String getSubject();

    /**
     * Gets the content.
     *
     * @return The content
     */
    String getContent();

    /**
     * Gets the content type.
     *
     * @return The content type
     */
    ContentType getContentType();

    /**
     * Indicates whether a read receipt is supposed to be requested.
     *
     * @return <code>true</code> to request a read receipt; otherwise <code>false</code>
     */
    boolean isRequestReadReceipt();

    /**
     * Gets the information whether attachments are supposed to be shared (via a link), rather than attaching them to the message.
     *
     * @return The shared attachments information
     * @see SharedAttachmentsInfo#DISABLED
     */
    SharedAttachmentsInfo getSharedAttachments();

    /**
     * Gets the attachment listing for this message.
     *
     * @return The attachments (might be <code>null</code> or an empty list to signal no attachments)
     */
    List<Attachment> getAttachments();

    /**
     * Gets the meta information for this message.
     *
     * @return The meta information
     */
    Meta getMeta();

    /**
     * Gets the optional custom headers.
     *
     * @return The custom headers or <code>null</code>
     */
    Map<String, String> getCustomHeaders();

    /**
     * Gets the security information for this message.
     *
     * @return The security information
     * @see Security#DISABLED
     */
    Security getSecurity();

    /**
     * Gets this message's priority.
     *
     * @return The priority
     */
    Priority getPriority();

    /**
     * Checks if the content of this message is stored encrypted.
     *
     * @return <code>true</code> if encrypted; otherwise <code>false</code>
     */
    boolean isContentEncrypted();

}
