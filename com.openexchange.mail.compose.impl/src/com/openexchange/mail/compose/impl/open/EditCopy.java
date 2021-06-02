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

package com.openexchange.mail.compose.impl.open;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.compose.Attachment;
import com.openexchange.mail.compose.AttachmentDescription;
import com.openexchange.mail.compose.AttachmentStorageService;
import com.openexchange.mail.compose.AttachmentStorages;
import com.openexchange.mail.compose.CompositionSpaces;
import com.openexchange.mail.compose.ContentId;
import com.openexchange.mail.compose.HeaderUtility;
import com.openexchange.mail.compose.Message;
import com.openexchange.mail.compose.Meta;
import com.openexchange.mail.compose.OpenCompositionSpaceParameters;
import com.openexchange.mail.compose.Security;
import com.openexchange.mail.compose.SharedAttachmentsInfo;
import com.openexchange.mail.compose.Message.Priority;
import com.openexchange.mail.compose.impl.attachment.AttachmentImageDataSource;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.processing.MimeProcessingUtility;
import com.openexchange.mail.mime.processing.TextAndContentType;
import com.openexchange.mail.parser.MailMessageParser;
import com.openexchange.mail.parser.handlers.InlineContentHandler;
import com.openexchange.mail.parser.handlers.NonInlineForwardPartHandler;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link EditCopy} - Utility class to open a composition space for a edit-copy.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class EditCopy extends AbstractOpener {

    /**
     * Initializes a new {@link EditCopy}.
     */
    public EditCopy(AttachmentStorageService attachmentStorageService, ServiceLookup services) {
        super(attachmentStorageService, services);
    }

    /**
     * Prepares opening a composition space for a reply.
     *
     * @param isEditFor Whether an edit should be prepared
     * @param parameters The parameters
     * @param state The state
     * @param session The session
     * @throws OXException If an Open-Xchange error occurs
     * @throws MessagingException If a messaging error occurs
     */
    public void doOpenForEditCopy(boolean isEditFor, OpenCompositionSpaceParameters parameters, OpenState state, Session session) throws OXException {
        MailPath editFor = parameters.getReferencedMails().get(0);
        if (isEditFor) {
            state.metaBuilder.withEditFor(editFor);
        }
        state.mailInterface = MailServletInterface.getInstanceWithDecryptionSupport(session, null);
        MailMessage originalMail = requireMailMessage(editFor, state.mailInterface);
        state.metaBuilder.withDate(originalMail.getSentDate());

        UserSettingMail usm = parameters.getMailSettings();

        // Restore Content-Type, meta, security, and shared attachments info from draft message
        Optional<Message.ContentType> optionalContentType = Optional.empty();
        {
            String headerValue = HeaderUtility.decodeHeaderValue(originalMail.getFirstHeader(HeaderUtility.HEADER_X_OX_CONTENT_TYPE));
            if (Strings.isNotEmpty(headerValue)) {
                Message.ContentType ct = Message.ContentType.contentTypeFor(headerValue);
                if (ct != null) {
                    optionalContentType = Optional.of(ct);
                }
            }

            headerValue = HeaderUtility.decodeHeaderValue(originalMail.getFirstHeader(HeaderUtility.HEADER_X_OX_META));
            if (Strings.isNotEmpty(headerValue)) {
                Meta parsedMeta = HeaderUtility.headerValue2Meta(headerValue);
                state.metaBuilder.applyFromDraft(parsedMeta);
            }

            headerValue = HeaderUtility.decodeHeaderValue(originalMail.getFirstHeader(HeaderUtility.HEADER_X_OX_SECURITY));
            if (Strings.isNotEmpty(headerValue)) {
                Security parsedSecurity = HeaderUtility.headerValue2Security(headerValue);
                state.message.setSecurity(parsedSecurity);
            }

            headerValue = HeaderUtility.decodeHeaderValue(originalMail.getFirstHeader(HeaderUtility.HEADER_X_OX_SHARED_ATTACHMENTS));
            if (Strings.isNotEmpty(headerValue)) {
                SharedAttachmentsInfo parsedSharedAttachments = HeaderUtility.headerValue2SharedAttachments(headerValue);
                state.message.setsharedAttachmentsInfo(parsedSharedAttachments);
            }

            headerValue = HeaderUtility.decodeHeaderValue(originalMail.getFirstHeader(HeaderUtility.HEADER_X_OX_READ_RECEIPT));
            if (Strings.isNotEmpty(headerValue)) {
                if ("true".equalsIgnoreCase(headerValue)) {
                    state.message.setRequestReadReceipt(true);
                }
            }

            headerValue = HeaderUtility.decodeHeaderValue(originalMail.getFirstHeader(HeaderUtility.HEADER_X_OX_CUSTOM_HEADERS));
            if (Strings.isNotEmpty(headerValue)) {
                Map<String, String> customHeaders = HeaderUtility.headerValue2CustomHeaders(headerValue);
                if (customHeaders != null) {
                    state.message.setCustomHeaders(customHeaders);
                }
            }
        }

        // Pre-set subject
        String origSubject = originalMail.getSubject();
        if (Strings.isNotEmpty(origSubject)) {
            state.message.setSubject(origSubject);
        }

        // Set "From"
        {
            InternetAddress[] from = originalMail.getFrom();
            if (null != from && from.length > 0) {
                state.message.setFrom(toAddress(from[0], false));
            }
        }

        // Set "Reply-To"
        {
            InternetAddress[] replyTo = originalMail.getReplyTo();
            if (null != replyTo && replyTo.length > 0) {
                state.message.setReplyTo(toAddress(replyTo[0], true));
            }
        }

        // Pre-set recipients
        {
            InternetAddress[] recipients = originalMail.getTo();
            if (null != recipients && recipients.length > 0) {
                state.message.addTo(toAddresses(recipients));
            }
            recipients = originalMail.getCc();
            if (null != recipients && recipients.length > 0) {
                state.message.addCc(toAddresses(recipients));
            }
            recipients = originalMail.getBcc();
            if (null != recipients && recipients.length > 0) {
                state.message.addBcc(toAddresses(recipients));
            }
        }

        // Priority
        if (state.message.containsPriority() == false) {
            int level = originalMail.getPriority();
            Priority priority = Priority.priorityForLevel(level);
            if (priority != Priority.NORMAL) {
                state.message.setPriority(priority);
            }
        }

        // Grab first seen text from original message and check for possible referenced inline images
        boolean multipart = originalMail.getContentType().startsWith("multipart/");
        List<String> contentIds = multipart ? new ArrayList<String>() : null;
        if (optionalContentType.isPresent() || parameters.getContentType() != null) {
            Message.ContentType contentType = optionalContentType.isPresent() ? optionalContentType.get() : parameters.getContentType();
            TextAndContentType textForForward = MimeProcessingUtility.getTextForForward(originalMail, contentType.isImpliesHtml(), false, contentIds, session);
            if (null == textForForward) {
                state.message.setContent("");
                state.message.setContentType(contentType);
            } else {
                state.message.setContent(textForForward.getText());
                if (textForForward.isHtml()) {
                    state.message.setContentType(contentType.isImpliesHtml() ? contentType : TEXT_HTML);
                } else {
                    state.message.setContentType(TEXT_PLAIN);
                }
            }
        } else {
            TextAndContentType textForForward = MimeProcessingUtility.getTextForForward(originalMail, usm.isDisplayHtmlInlineContent(), false, contentIds, session);
            if (null == textForForward) {
                state.message.setContent("");
                state.message.setContentType(usm.isDisplayHtmlInlineContent() ? TEXT_HTML : TEXT_PLAIN);
            } else {
                state.message.setContent(textForForward.getText());
                state.message.setContentType(textForForward.isHtml() ? TEXT_HTML : TEXT_PLAIN);
            }
        }

        // Check if original mail may contain attachments
        if (multipart) {
            Optional<Boolean> optionalEncrypt = Optional.of(state.encrypt);
            // Add mail's non-inline parts
            {
                NonInlineForwardPartHandler handler = new NonInlineForwardPartHandler();
                if (null != contentIds && !contentIds.isEmpty()) {
                    handler.setImageContentIds(contentIds);
                }
                new MailMessageParser().setInlineDetectorBehavior(true).parseMailMessage(originalMail, handler);
                List<MailPart> nonInlineParts = handler.getNonInlineParts();
                if (null != nonInlineParts && !nonInlineParts.isEmpty()) {
                    state.attachmentStorage = getAttachmentStorage(session);
                    state.attachments = new ArrayList<>(nonInlineParts.size());
                    int i = 0;
                    for (MailPart mailPart : nonInlineParts) {
                        // Compile & store attachment
                        AttachmentDescription attachment = AttachmentStorages.createAttachmentDescriptionFor(mailPart, i + 1, state.compositionSpaceId, session);
                        Attachment partAttachment = AttachmentStorages.saveAttachment(mailPart.getInputStream(), attachment, optionalEncrypt, session, state.attachmentStorage);
                        state.attachments.add(partAttachment);
                        i++;
                    }
                }
            }

            // Add mail's inline images
            if (state.message.getContentType().isImpliesHtml() && null != contentIds && !contentIds.isEmpty()) {
                InlineContentHandler inlineHandler = new InlineContentHandler(contentIds);
                new MailMessageParser().setInlineDetectorBehavior(true).parseMailMessage(originalMail, inlineHandler);
                Map<String, MailPart> inlineParts = inlineHandler.getInlineContents();
                if (null != inlineParts && !inlineParts.isEmpty()) {
                    if (null == state.attachmentStorage) {
                        state.attachmentStorage = getAttachmentStorage(session);
                    }
                    if (null == state.attachments) {
                        state.attachments = new ArrayList<>(inlineParts.size());
                    }

                    Map<ContentId, Attachment> inlineAttachments = new HashMap<ContentId, Attachment>(inlineParts.size());
                    int i = 0;
                    for (Map.Entry<String, MailPart> inlineEntry : inlineParts.entrySet()) {
                        MailPart mailPart = inlineEntry.getValue();
                        // Compile & store attachment
                        ContentId contentId = ContentId.valueOf(inlineEntry.getKey());
                        AttachmentDescription attachment = AttachmentStorages.createInlineAttachmentDescriptionFor(mailPart, contentId, i + 1, state.compositionSpaceId);
                        Attachment partAttachment = AttachmentStorages.saveAttachment(mailPart.getInputStream(), attachment, optionalEncrypt, session, state.attachmentStorage);
                        state.attachments.add(partAttachment);

                        inlineAttachments.put(contentId, partAttachment);
                        i++;
                    }

                    state.message.setContent(CompositionSpaces.replaceCidInlineImages(state.message.getContent(), inlineAttachments, AttachmentImageDataSource.getInstance(), session));
                }
            }
        }
    }

}
