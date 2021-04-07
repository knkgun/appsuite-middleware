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

package com.openexchange.imap.command;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.UIDFolder;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MailDateFormat;
import javax.mail.internet.ParameterList;
import com.google.common.collect.ImmutableMap;
import com.openexchange.exception.OXException;
import com.openexchange.imap.IMAPCapabilities;
import com.openexchange.imap.IMAPServerInfo;
import com.openexchange.java.Strings;
import com.openexchange.log.LogProperties;
import com.openexchange.mail.FullnameArgument;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.dataobjects.IDMailMessage;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.PlainTextAddress;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.mail.mime.utils.MimeStorageUtility;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.BODY;
import com.sun.mail.imap.protocol.BODYSTRUCTURE;
import com.sun.mail.imap.protocol.ENVELOPE;
import com.sun.mail.imap.protocol.FLAGS;
import com.sun.mail.imap.protocol.FetchResponse;
import com.sun.mail.imap.protocol.INTERNALDATE;
import com.sun.mail.imap.protocol.Item;
import com.sun.mail.imap.protocol.PREVIEW;
import com.sun.mail.imap.protocol.RFC822DATA;
import com.sun.mail.imap.protocol.RFC822SIZE;
import com.sun.mail.imap.protocol.SNIPPET;
import com.sun.mail.imap.protocol.UID;
import com.sun.mail.imap.protocol.X_REAL_UID;
import gnu.trove.impl.Constants;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TLongIntHashMap;
import gnu.trove.procedure.TIntIntProcedure;
import gnu.trove.procedure.TLongIntProcedure;

/**
 * {@link MailMessageFetchIMAPCommand} - performs a prefetch of messages in given folder with only those fields set that need to be present for
 * display and sorting. A corresponding instance of <code>javax.mail.FetchProfile</code> is going to be generated from given fields.
 * <p>
 * This method avoids calling JavaMail's fetch() methods which implicitly requests whole message envelope (FETCH 1:* (ENVELOPE INTERNALDATE
 * RFC822.SIZE)) when later working on returned <code>javax.mail.Message</code> objects.
 * </p>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailMessageFetchIMAPCommand extends AbstractIMAPCommand<MailMessage[]> {

    /** The logger constant */
    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MailMessageFetchIMAPCommand.class);

    /**
     * Interceptor for fetched instances of {@link MailMessage}.
     */
    public static interface MailMessageFetchInterceptor {

        /**
         * Intercepts specified instance of {@link MailMessage}.
         *
         * @param mail The mail message to intercept
         * @throws MessagingException If interception fails
         */
        void intercept(MailMessage mail) throws MessagingException;

        /**
         * Gets the return value from this interceptor.
         *
         * @return The return value
         */
        MailMessage[] getMails();

    }

    private static final int LENGTH = 9; // "FETCH <nums> (<command>)"
    private static final int LENGTH_WITH_UID = 13; // "UID FETCH <nums> (<command>)"

    private final int accountId;
    private String[] args;
    private final String command;
    private boolean uid;
    private final int length;
    private int index;
    private final MailMessage[] retval;
    private final MailMessageFetchInterceptor interceptor;
    private boolean determineAttachmentByHeader;
    private boolean checkICal;
    private boolean checkVCard;
    private boolean treatEmbeddedAsAttachment;
    private final boolean examineHasAttachmentUserFlags;
    private final String fullname;
    private final Set<FetchItemHandler> lastHandlers;
    private final TLongIntHashMap uid2index;
    private final TIntIntHashMap seqNum2index;

    /**
     * Initializes a new {@link MailMessageFetchIMAPCommand}.
     *
     * @param imapFolder The IMAP folder providing connected protocol
     * @param isRev1 Whether IMAP server has <i>IMAP4rev1</i> capability or not
     * @param seqNums The sequence numbers to fetch
     * @param fp The fetch profile to use
     * @param serverInfo The IMAP server information deduced from configuration
     * @param examineHasAttachmentUserFlags Whether has-attachment user flags should be considered
     * @param previewSupported Whether target IMAP server supports <code>"PREVIEW=FUZZY"</code> capability
     * @param optionalInterceptor The optional interceptor
     * @throws MessagingException If initialization fails
     */
    public MailMessageFetchIMAPCommand(IMAPFolder imapFolder, boolean isRev1, int[] seqNums, FetchProfile fp, IMAPServerInfo serverInfo, boolean examineHasAttachmentUserFlags, boolean previewSupported, Optional<? extends MailMessageFetchInterceptor> optionalInterceptor) throws MessagingException {
        super(imapFolder);
        determineAttachmentByHeader = false;
        this.examineHasAttachmentUserFlags = examineHasAttachmentUserFlags;
        int messageCount = imapFolder.getMessageCount();
        if (messageCount <= 0) {
            returnDefaultValue = true;
        }
        accountId = null == serverInfo ? 0 : serverInfo.getAccountId();
        lastHandlers = new HashSet<FetchItemHandler>();
        command = getFetchCommand(isRev1, fp, false, serverInfo, previewSupported);
        uid = false;
        length = seqNums.length;
        uid2index = null;
        if (optionalInterceptor.isPresent()) {
            seqNum2index = null;
        } else {
            seqNum2index = new TIntIntHashMap(length, Constants.DEFAULT_LOAD_FACTOR, 0, -1);
            for (int i = 0; i < length; i++) {
                seqNum2index.put(seqNums[i], i);
            }
        }
        args = length == messageCount ? (1 == length ? ARGS_FIRST : ARGS_ALL) : IMAPNumArgSplitter.splitSeqNumArg(seqNums, false, LENGTH + command.length());
        if (0 == length) {
            returnDefaultValue = true;
        }
        fullname = imapFolder.getFullName();
        if (optionalInterceptor.isPresent()) {
            retval = null;
            interceptor = optionalInterceptor.get();
        } else {
            retval = new MailMessage[length];
            interceptor = null;
        }
    }

    /**
     * Initializes a new {@link MailMessageFetchIMAPCommand} to fetch all messages.
     *
     * @param imapFolder The IMAP folder providing connected protocol
     * @param isRev1 Whether IMAP server has <i>IMAP4rev1</i> capability or not
     * @param messageCount The IMAP folder's message count
     * @param fp The fetch profile to use
     * @param serverInfo The IMAP server information deduced from configuration
     * @param examineHasAttachmentUserFlags Whether has-attachment user flags should be considered
     * @param previewSupported Whether target IMAP server supports <code>"PREVIEW=FUZZY"</code> capability
     * @param optionalInterceptor The optional interceptor
     */
    public MailMessageFetchIMAPCommand(IMAPFolder imapFolder, boolean isRev1, int messageCount, FetchProfile fp, IMAPServerInfo serverInfo, boolean examineHasAttachmentUserFlags, boolean previewSupported, Optional<? extends MailMessageFetchInterceptor> optionalInterceptor) {
        super(imapFolder);
        determineAttachmentByHeader = false;
        this.examineHasAttachmentUserFlags = examineHasAttachmentUserFlags;
        if (messageCount <= 0) {
            returnDefaultValue = true;
        }
        accountId = null == serverInfo ? 0 : serverInfo.getAccountId();
        lastHandlers = new HashSet<FetchItemHandler>();
        command = getFetchCommand(isRev1, fp, false, serverInfo, previewSupported);
        uid = false;
        length = messageCount;
        uid2index = null;
        seqNum2index = null;
        args = (1 == length ? ARGS_FIRST : ARGS_ALL);
        if (0 == length) {
            returnDefaultValue = true;
        }
        fullname = imapFolder.getFullName();
        if (optionalInterceptor.isPresent()) {
            retval = null;
            interceptor = optionalInterceptor.get();
        } else {
            retval = new MailMessage[length];
            interceptor = null;
        }
    }

    /**
     * Initializes a new {@link MailMessageFetchIMAPCommand}.
     *
     * @param imapFolder The IMAP folder providing connected protocol
     * @param isRev1 Whether IMAP server has <i>IMAP4rev1</i> capability or not
     * @param uids The UIDs to fetch
     * @param fp The fetch profile to use
     * @param serverInfo The IMAP server information deduced from configuration
     * @param examineHasAttachmentUserFlags Whether has-attachment user flags should be considered
     * @param previewSupported Whether target IMAP server supports <code>"PREVIEW=FUZZY"</code> capability
     * @param optionalInterceptor The optional interceptor
     * @throws MessagingException If initialization fails
     */
    public MailMessageFetchIMAPCommand(IMAPFolder imapFolder, boolean isRev1, long[] uids, FetchProfile fp, IMAPServerInfo serverInfo, boolean examineHasAttachmentUserFlags, boolean previewSupported, Optional<? extends MailMessageFetchInterceptor> optionalInterceptor) throws MessagingException {
        super(imapFolder);
        determineAttachmentByHeader = false;
        this.examineHasAttachmentUserFlags = examineHasAttachmentUserFlags;
        int messageCount = imapFolder.getMessageCount();
        if (messageCount <= 0) {
            returnDefaultValue = true;
        }
        accountId = null == serverInfo ? 0 : serverInfo.getAccountId();
        lastHandlers = new HashSet<FetchItemHandler>();
        length = uids.length;
        seqNum2index = null;
        if (optionalInterceptor.isPresent()) {
            uid2index = null;
        } else {
            uid2index = new TLongIntHashMap(length, Constants.DEFAULT_LOAD_FACTOR, 0, -1);
            for (int i = 0; i < length; i++) {
                uid2index.put(uids[i], i);
            }
        }
        if (length == messageCount) {
            fp.add(UIDFolder.FetchProfileItem.UID);
            command = getFetchCommand(isRev1, fp, false, serverInfo, previewSupported);
            args = (1 == length ? ARGS_FIRST : ARGS_ALL);
            uid = false;
        } else {
            command = getFetchCommand(isRev1, fp, false, serverInfo, previewSupported);
            args = IMAPNumArgSplitter.splitUIDArg(uids, false, LENGTH_WITH_UID + command.length());
            uid = true;
        }
        if (0 == length) {
            returnDefaultValue = true;
        }
        fullname = imapFolder.getFullName();
        if (optionalInterceptor.isPresent()) {
            retval = null;
            interceptor = optionalInterceptor.get();
        } else {
            retval = new MailMessage[length];
            interceptor = null;
        }
    }

    /**
     * Sets whether detection if message contains attachment is performed by "Content-Type" header only.
     * <p>
     * If <code>true</code> a message is considered to contain attachments if its "Content-Type" header equals "multipart/mixed".
     *
     * @param determineAttachmentByHeader <code>true</code> to detect if message contains attachment is performed by "Content-Type" header
     *            only; otherwise <code>false</code>
     * @return This FETCH IMAP command with value applied
     */
    public MailMessageFetchIMAPCommand setDetermineAttachmentByHeader(boolean determineAttachmentByHeader) {
        this.determineAttachmentByHeader = determineAttachmentByHeader;
        return this;
    }

    /**
     * Sets the checkICal
     *
     * @param checkICal The checkICal to set
     * @return This FETCH IMAP command with value applied
     */
    public MailMessageFetchIMAPCommand setCheckICal(boolean checkICal) {
        this.checkICal = checkICal;
        return this;
    }

    /**
     * Sets the checkVCard
     *
     * @param checkVCard The checkVCard to set
     * @return This FETCH IMAP command with value applied
     */
    public MailMessageFetchIMAPCommand setCheckVCard(boolean checkVCard) {
        this.checkVCard = checkVCard;
        return this;
    }

    /**
     * Sets the treatEmbeddedAsAttachment
     *
     * @param treatEmbeddedAsAttachment The treatEmbeddedAsAttachment to set
     * @return This FETCH IMAP command with value applied
     */
    public MailMessageFetchIMAPCommand setTreatEmbeddedAsAttachment(boolean treatEmbeddedAsAttachment) {
        this.treatEmbeddedAsAttachment = treatEmbeddedAsAttachment;
        return this;
    }

    @Override
    protected String getDebugInfo(int argsIndex) {
        StringBuilder sb = new StringBuilder(command.length() + 64);
        if (uid) {
            sb.append("UID ");
        }
        sb.append("FETCH ");
        String arg = args[argsIndex];
        if (arg.length() > 32) {
            int pos = arg.indexOf(',');
            if (pos == -1) {
                sb.append("...");
            } else {
                sb.append(arg.substring(0, pos)).append(",...,").append(arg.substring(arg.lastIndexOf(',') + 1));
            }
        } else {
            sb.append(arg);
        }
        sb.append(" (").append(command).append(')');
        return sb.toString();
    }

    @Override
    protected boolean addLoopCondition() {
        return (index < length);
    }

    @Override
    protected String[] getArgs() {
        return args;
    }

    @Override
    protected String getCommand(int argsIndex) {
        StringBuilder sb = new StringBuilder(args[argsIndex].length() + 64);
        if (uid) {
            sb.append("UID ");
        }
        sb.append("FETCH ");
        sb.append(args[argsIndex]);
        sb.append(" (").append(command).append(')');
        return sb.toString();
    }

    private static final MailMessage[] EMPTY_ARR = new MailMessage[0];

    @Override
    protected MailMessage[] getDefaultValue() {
        return EMPTY_ARR;
    }

    @Override
    protected MailMessage[] getReturnVal() throws MessagingException {
        if (interceptor != null) {
            return interceptor.getMails();
        }

        final MailMessage[] retval = this.retval;
        if (null != seqNum2index) {
            seqNum2index.forEachEntry(new TIntIntProcedure() {

                @Override
                public boolean execute(int seqNum, int pos) {
                    if (pos > 0) {
                        retval[pos] = handleMessage(seqNum);
                    }
                    return true;
                }
            });
        } else if (null != uid2index) {
            uid2index.forEachEntry(new TLongIntProcedure() {

                @Override
                public boolean execute(long uid, int pos) {
                    if (pos > 0) {
                        retval[pos] = handleMessage(uid);
                    }
                    return true;
                }
            });
        } else if (index < length) {
            String server = imapFolder.getStore().toString();
            int pos = server.indexOf('@');
            if (pos >= 0 && ++pos < server.length()) {
                server = server.substring(pos);
            }
            MessagingException e = new MessagingException(new StringBuilder(32).append("Expected ").append(length).append(" FETCH responses but got ").append(index).append(" from IMAP folder \"").append(imapFolder.getFullName()).append("\" on server \"").append(server).append("\".").toString());
            LOG.warn("", e);
        }
        return retval;
    }

    IDMailMessage handleMessage(int seqNum) {
        if (seqNum < 0) {
            return null;
        }
        try {
            return handleMessage(imapFolder.getMessage(seqNum));
        } catch (Exception e) {
            LOG.warn("Message #{} discarded", I(seqNum), e);
            return null;
        }
    }

    IDMailMessage handleMessage(long uid) {
        if (uid < 0) {
            return null;
        }
        try {
            return handleMessage(imapFolder.getMessageByUID(uid));
        } catch (Exception e) {
            LOG.warn("Message uid={} discarded", L(uid), e);
            return null;
        }
    }

    private IDMailMessage handleMessage(Message message) {
        if (null == message) {
            return null;
        }
        try {
            IDMailMessage mail = new IDMailMessage(null, fullname);
            for (FetchItemHandler fetchItemHandler : lastHandlers.isEmpty() ? MAP.values() : lastHandlers) {
                fetchItemHandler.handleMessage(message, mail, LOG);
            }
            return mail;
        } catch (Exception e) {
            LOG.warn("Message #{} discarded", I(message.getMessageNumber()), e);
            return null;
        }
    }

    @Override
    protected boolean handleResponse(Response currentReponse) throws MessagingException {
        /*
         * Response is null or not a FetchResponse
         */
        if (!FetchResponse.class.isInstance(currentReponse)) {
            return false;
        }
        FetchResponse fetchResponse = (FetchResponse) currentReponse;
        int seqNum = fetchResponse.getNumber();
        int pos;
        if (null != seqNum2index) {
            pos = seqNum2index.remove(seqNum);
            if (pos < 0) {
                pos = index;
            }
        } else if (null != uid2index) {
            UID uidItem = getItemOf(UID.class, fetchResponse);
            if (null != uidItem) {
                pos = uid2index.remove(uidItem.uid);
                if (pos < 0) {
                    pos = index;
                }
            } else {
                pos = index;
            }
        } else {
            pos = index;
        }
        index++;
        boolean error = true;
        MailMessage mail;
        try {
            mail = handleFetchRespone(fetchResponse, fullname, accountId, lastHandlers, determineAttachmentByHeader, checkICal, checkVCard, treatEmbeddedAsAttachment, examineHasAttachmentUserFlags);
            error = false;
        } catch (MessagingException e) {
            /*
             * Discard corrupt message
             */
            LOG.warn("Message #{} discarded", I(seqNum), MimeMailException.handleMessagingException(e));
            mail = null;
        } catch (OXException e) {
            /*
             * Discard corrupt message
             */
            LOG.warn("Message #{} discarded", I(seqNum), e);
            mail = null;
        }
        if (!error) {
            if (interceptor == null) {
                retval[pos] = mail;
            } else {
                interceptor.intercept(mail);
            }
        }
        return true;
    }

    /**
     * Converts given FETCH response to an appropriate {@link MailMessage} instance.
     *
     * @param fetchResponse The FETCH response to handle
     * @param fullName The full name of associated folder
     * @param accountId The account identifier
     * @param examineHasAttachmentUserFlags Whether has-attachment user flags should be considered
     * @return The resulting mail message
     * @throws MessagingException If a messaging error occurs
     * @throws OXException If an Open-Xchange error occurs
     */
    public static MailMessage handleFetchRespone(FetchResponse fetchResponse, String fullName, int accountId, boolean examineHasAttachmentUserFlags) throws MessagingException, OXException {
        IDMailMessage mail = new IDMailMessage(null, fullName);
        mail.setAccountId(accountId);
        return handleFetchRespone(mail, fetchResponse, fullName, null, false, false, false, false, examineHasAttachmentUserFlags);
    }

    /**
     * Applies given FETCH response to an given {@link MailMessage} instance.
     *
     * @param mail The message to apply to
     * @param fetchResponse The FETCH response to handle
     * @param fullName The full name of associated folder
     * @param examineHasAttachmentUserFlags Whether has-attachment user flags should be considered
     * @return The resulting mail message
     * @throws MessagingException If a messaging error occurs
     * @throws OXException If an Open-Xchange error occurs
     */
    public static MailMessage handleFetchRespone(IDMailMessage mail, FetchResponse fetchResponse, String fullName, boolean examineHasAttachmentUserFlags) throws MessagingException, OXException {
        return handleFetchRespone(mail, fetchResponse, fullName, null, false, false, false, false, examineHasAttachmentUserFlags);
    }

    private static MailMessage handleFetchRespone(FetchResponse fetchResponse, String fullName, int accountId, Set<FetchItemHandler> lastHandlers, boolean determineAttachmentByHeader, boolean checkICal, boolean checkVCard, boolean treatEmbeddedAsAttachment, boolean examineHasAttachmentUserFlags) throws MessagingException, OXException {
        IDMailMessage mail = new IDMailMessage(null, fullName);
        mail.setAccountId(accountId);
        return handleFetchRespone(mail, fetchResponse, fullName, lastHandlers, determineAttachmentByHeader, checkICal, checkVCard, treatEmbeddedAsAttachment, examineHasAttachmentUserFlags);
    }

    private static MailMessage handleFetchRespone(IDMailMessage mail, FetchResponse fetchResponse, String fullName, Set<FetchItemHandler> lastHandlers, boolean determineAttachmentByHeader, boolean checkICal, boolean checkVCard, boolean treatEmbeddedAsAttachment, boolean examineHasAttachmentUserFlags) throws MessagingException, OXException {
        try {
            return doHandleFetchRespone(mail, fetchResponse, fullName, lastHandlers, determineAttachmentByHeader, checkICal, checkVCard, treatEmbeddedAsAttachment, examineHasAttachmentUserFlags);
        } catch (RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private static MailMessage doHandleFetchRespone(IDMailMessage mail, FetchResponse fetchResponse, String fullName, Set<FetchItemHandler> lastHandlers, boolean determineAttachmentByHeader, boolean checkICal, boolean checkVCard, boolean treatEmbeddedAsAttachment, boolean examineHasAttachmentUserFlags) throws MessagingException, OXException {
        IDMailMessage m;
        if (null == mail) {
            m = new IDMailMessage(null, fullName);
        } else {
            m = mail;
            m.setFolder(fullName);
        }
        // mail.setRecentCount(recentCount);
        m.setSeqnum(fetchResponse.getNumber());
        int itemCount = fetchResponse.getItemCount();
        Map<Class<? extends Item>, FetchItemHandler> map = MAP;
        Item delayed = null;
        for (int j = itemCount; j-- > 0;) {
            Item item = fetchResponse.getItem(j);
            if (examineHasAttachmentUserFlags && item instanceof BODYSTRUCTURE) {
                // Delay that item...
                delayed = item;
            } else {
                FetchItemHandler itemHandler = map.get(item.getClass());
                if (null == itemHandler) {
                    itemHandler = getItemHandlerByItem(item, checkICal, checkVCard, treatEmbeddedAsAttachment, examineHasAttachmentUserFlags);
                    if (null == itemHandler) {
                        LOG.warn("Unknown FETCH item: {}", item.getClass().getName());
                    } else {
                        if (null != lastHandlers) {
                            lastHandlers.add(itemHandler);
                        }
                        itemHandler.handleItem(item, m, LOG);
                    }
                } else {
                    if (null != lastHandlers) {
                        lastHandlers.add(itemHandler);
                    }
                    itemHandler.handleItem(item, m, LOG);
                }
            }
        }

        if (null != delayed) {
            FetchItemHandler itemHandler = getItemHandlerByItem(delayed, checkICal, checkVCard, treatEmbeddedAsAttachment, examineHasAttachmentUserFlags);
            if (null == itemHandler) {
                LOG.warn("Unknown FETCH item: {}", delayed.getClass().getName());
            } else {
                if (null != lastHandlers) {
                    lastHandlers.add(itemHandler);
                }
                itemHandler.handleItem(delayed, m, LOG);
            }
        }

        if (determineAttachmentByHeader) {
            String cts = m.getHeader(MessageHeaders.HDR_CONTENT_TYPE, null);
            if (null != cts) {
                m.setAlternativeHasAttachment(new ContentType(cts).startsWith("multipart/mixed"));
            }
        }

        // Drop possibly set "com.openexchange.mail.mailId" log property
        LogProperties.remove(LogProperties.Name.MAIL_MAIL_ID);

        return m;
    }

    private static FetchItemHandler getItemHandlerByItem(Item item, boolean checkICal, boolean checkVCard, boolean treatEmbeddedAsAttachment, boolean examineHasAttachmentUserFlags) {
        if (item instanceof BODYSTRUCTURE) {
            return BODYSTRUCTUREFetchItemHandler.getInstance(checkICal, checkVCard, treatEmbeddedAsAttachment);
        } else if ((item instanceof RFC822DATA) || (item instanceof BODY)) {
            return HEADER_ITEM_HANDLER;
        } else if (item instanceof UID) {
            return UID_ITEM_HANDLER;
        } else if (item instanceof INTERNALDATE) {
            return INTERNALDATE_ITEM_HANDLER;
        } else if (item instanceof Flags) {
            return FLAGSFetchItemHandler.getInstance(examineHasAttachmentUserFlags);
        } else if (item instanceof ENVELOPE) {
            return ENVELOPE_ITEM_HANDLER;
        } else if (item instanceof RFC822SIZE) {
            return SIZE_ITEM_HANDLER;
        } else if (item instanceof X_REAL_UID) {
            return X_REAL_UID_ITEM_HANDLER;
        } else if (item instanceof com.sun.mail.imap.protocol.X_MAILBOX) {
            return X_MAILBOX_ITEM_HANDLER;
        } else if (item instanceof SNIPPET) {
            return SNIPPET_ITEM_HANDLER;
        } else if (item instanceof PREVIEW) {
            return PREVIEW_ITEM_HANDLER;
        } else {
            return null;
        }
    }

    private static interface FetchItemHandler {

        /**
         * Handles given <code>com.sun.mail.imap.protocol.Item</code> instance and applies it to given message.
         *
         * @param item The item to handle
         * @param msg The message to apply to
         * @param logger The logger
         * @throws MessagingException If a messaging error occurs
         * @throws OXException If a mail error occurs
         */
        void handleItem(Item item, IDMailMessage msg, org.slf4j.Logger logger) throws MessagingException, OXException;

        void handleMessage(Message message, IDMailMessage msg, org.slf4j.Logger logger) throws MessagingException, OXException;
    }

    /*-
     * ++++++++++++++ Item handlers ++++++++++++++
     */

    private interface HeaderHandler {

        void handle(Header hdr, IDMailMessage mailMessage) throws OXException;

    }

    private static final FetchItemHandler HEADER_ITEM_HANDLER = new FetchItemHandler() {

        private final Map<String, HeaderHandler> hh = ImmutableMap.<String, HeaderHandler> builder()
            .put(MessageHeaders.HDR_FROM, new HeaderHandler() {

                @Override
                public void handle(Header hdr, IDMailMessage mailMessage) throws OXException {
                    mailMessage.addFrom(MimeMessageUtility.getAddressHeader(hdr.getValue()));
                }
            })
            .put(MessageHeaders.HDR_TO, new HeaderHandler() {

                @Override
                public void handle(Header hdr, IDMailMessage mailMessage) throws OXException {
                    mailMessage.addTo(MimeMessageUtility.getAddressHeader(hdr.getValue()));
                }
            })
            .put(MessageHeaders.HDR_CC, new HeaderHandler() {

                @Override
                public void handle(Header hdr, IDMailMessage mailMessage) throws OXException {
                    mailMessage.addCc(MimeMessageUtility.getAddressHeader(hdr.getValue()));
                }
            })
            .put(MessageHeaders.HDR_BCC, new HeaderHandler() {

                @Override
                public void handle(Header hdr, IDMailMessage mailMessage) throws OXException {
                    mailMessage.addBcc(MimeMessageUtility.getAddressHeader(hdr.getValue()));
                }
            })
            .put(MessageHeaders.HDR_REPLY_TO, new HeaderHandler() {

                @Override
                public void handle(Header hdr, IDMailMessage mailMessage) throws OXException {
                    mailMessage.addReplyTo(MimeMessageUtility.getAddressHeader(hdr.getValue()));
                }
            })
            .put(MessageHeaders.HDR_DISP_NOT_TO, new HeaderHandler() {

                @Override
                public void handle(Header hdr, IDMailMessage mailMessage) throws OXException {
                    mailMessage.setDispositionNotification(MimeMessageUtility.getAddressHeader(hdr.getValue())[0]);
                }
            })
            .put(MessageHeaders.HDR_SUBJECT, new HeaderHandler() {

                @Override
                public void handle(Header hdr, IDMailMessage mailMessage) throws OXException {
                    mailMessage.setSubject(MimeMessageUtility.decodeMultiEncodedHeader(MimeMessageUtility.checkNonAscii(hdr.getValue())), true);
                }
            })
            .put(MessageHeaders.HDR_DATE, new HeaderHandler() {

                @Override
                public void handle(Header hdr, IDMailMessage mailMessage) throws OXException {
                    MailDateFormat mdf = MimeMessageUtility.getDefaultMailDateFormat();
                    synchronized (mdf) {
                        try {
                            mailMessage.setSentDate(mdf.parse(hdr.getValue()));
                        } catch (ParseException e) {
                            LOG.error("", e);
                        }
                    }
                }
            })
            .put(MessageHeaders.HDR_IMPORTANCE, new HeaderHandler() {

                @Override
                public void handle(Header hdr, IDMailMessage mailMessage) throws OXException {
                    String value = hdr.getValue();
                    if (null != value) {
                        mailMessage.setPriority(MimeMessageUtility.parseImportance(value));
                    }
                }
            })
            .put(MessageHeaders.HDR_X_PRIORITY, new HeaderHandler() {

                @Override
                public void handle(Header hdr, IDMailMessage mailMessage) throws OXException {
                    if (!mailMessage.containsPriority()) {
                        mailMessage.setPriority(MimeMessageUtility.parsePriority(hdr.getValue()));
                    }
                }
            })
            .put(MessageHeaders.HDR_REFERENCES, new HeaderHandler() {

                @Override
                public void handle(Header hdr, IDMailMessage mailMessage) throws OXException {
                    mailMessage.setReferences(hdr.getValue());
                }
            })
            .build();

        private final Set<String> headerFields = new HashSet<String>(Arrays.asList("content-type", "from", "to", "cc", "bcc", "disposition-notification-to", "subject"));

        @Override
        public void handleItem(Item item, IDMailMessage msg, org.slf4j.Logger logger) throws MessagingException, OXException {
            List<Header> headers;
            {
                InputStream headerStream = item instanceof BODY ? ((BODY) item).getByteArrayInputStream() : ((RFC822DATA) item).getByteArrayInputStream();
                if (null == headerStream) {
                    logger.debug("Cannot retrieve headers from message #{} in folder {}", I(msg.getSeqnum()), msg.getFolder());
                    headers = Collections.emptyList();
                } else {
                    headers = InternetHeaders.parse(headerStream);
                }
            }

            Set<String> headerFields = new HashSet<String>(this.headerFields);
            for (Header hdr : headers) {
                String name = hdr.getName();
                headerFields.remove(Strings.toLowerCase(name));
                {
                    HeaderHandler headerHandler = hh.get(name);
                    if (null != headerHandler) {
                        headerHandler.handle(hdr, msg);
                    }
                }
                try {
                    msg.addHeader(name, hdr.getValue());
                } catch (IllegalArgumentException illegalArgumentExc) {
                    logger.debug("Ignoring invalid header.", illegalArgumentExc);
                }
                /*-
                 *
                HeaderHandler hdrHandler = hdrHandlers.get(hdr.getName());
                if (hdrHandler == null) {
                    msg.setHeader(hdr.getName(), hdr.getValue());
                } else {
                    hdrHandler.handleHeader(hdr.getValue(), msg);
                }
                 */
            }
            if (headerFields.contains("disposition-notification-to")) {
                msg.setDispositionNotification(null);
            }
        }

        @Override
        public void handleMessage(Message message, IDMailMessage msg, org.slf4j.Logger logger) throws MessagingException, OXException {
            for (Enumeration<Header> e = message.getAllHeaders(); e.hasMoreElements();) {
                Header hdr = e.nextElement();
                String name = hdr.getName();
                {
                    HeaderHandler headerHandler = hh.get(name);
                    if (null != headerHandler) {
                        headerHandler.handle(hdr, msg);
                    }
                }
                try {
                    msg.addHeader(name, hdr.getValue());
                } catch (IllegalArgumentException illegalArgumentExc) {
                    logger.debug("Ignoring invalid header.", illegalArgumentExc);
                }
                /*-
                 *
                HeaderHandler hdrHandler = hdrHandlers.get(hdr.getName());
                if (hdrHandler == null) {
                    msg.setHeader(hdr.getName(), hdr.getValue());
                } else {
                    hdrHandler.handleHeader(hdr.getValue(), msg);
                }
                 */
            }
        }
    };

    private static final class FLAGSFetchItemHandler implements FetchItemHandler {

        private static final FLAGSFetchItemHandler EXAMINE_HAS_ATTACHMENT_USER_FLAGS_INSTANCE = new FLAGSFetchItemHandler(true);

        private static final FLAGSFetchItemHandler REGULAR_INSTANCE = new FLAGSFetchItemHandler(false);

        static FLAGSFetchItemHandler getInstance(boolean examineHasAttachmentUserFlags) {
            return examineHasAttachmentUserFlags ? EXAMINE_HAS_ATTACHMENT_USER_FLAGS_INSTANCE : REGULAR_INSTANCE;
        }

        // ---------------------------------------------------------------------------------------------------------------------------------

        private final boolean examineHasAttachmentUserFlags;

        private FLAGSFetchItemHandler(boolean examineHasAttachmentUserFlags) {
            super();
            this.examineHasAttachmentUserFlags = examineHasAttachmentUserFlags;
        }

        @Override
        public void handleItem(Item item, IDMailMessage msg, org.slf4j.Logger logger) throws MessagingException {
            FLAGS flags = (FLAGS) item;
            /*
             * Parse system flags
             */
            int retval = 0;
            int colorLabel = MailMessage.COLOR_LABEL_NONE;
            Collection<String> ufCol = null;
            if (flags.contains(Flags.Flag.ANSWERED)) {
                retval |= MailMessage.FLAG_ANSWERED;
            }
            if (flags.contains(Flags.Flag.DELETED)) {
                retval |= MailMessage.FLAG_DELETED;
            }
            if (flags.contains(Flags.Flag.DRAFT)) {
                retval |= MailMessage.FLAG_DRAFT;
            }
            if (flags.contains(Flags.Flag.FLAGGED)) {
                retval |= MailMessage.FLAG_FLAGGED;
            }
            if (flags.contains(Flags.Flag.RECENT)) {
                retval |= MailMessage.FLAG_RECENT;
            }
            if (flags.contains(Flags.Flag.SEEN)) {
                retval |= MailMessage.FLAG_SEEN;
            }
            if (flags.contains(Flags.Flag.USER)) {
                retval |= MailMessage.FLAG_USER;
            }
            String[] userFlags = flags.getUserFlags();
            if (userFlags != null) {
                /*
                 * Mark message to contain user flags
                 */
                Set<String> set = new HashSet<String>(userFlags.length);
                for (String userFlag : userFlags) {
                    if (MailMessage.isColorLabel(userFlag)) {
                        try {
                            colorLabel = MailMessage.getColorLabelIntValue(userFlag);
                        } catch (@SuppressWarnings("unused") OXException e) {
                            // Cannot occur
                            colorLabel = MailMessage.COLOR_LABEL_NONE;
                        }
                    } else if (MailMessage.USER_FORWARDED.equalsIgnoreCase(userFlag)) {
                        retval |= MailMessage.FLAG_FORWARDED;
                    } else if (MailMessage.USER_READ_ACK.equalsIgnoreCase(userFlag)) {
                        retval |= MailMessage.FLAG_READ_ACK;
                    } else if (examineHasAttachmentUserFlags && MailMessage.USER_HAS_ATTACHMENT.equalsIgnoreCase(userFlag)) {
                        msg.setHasAttachment(true);
                    } else if (examineHasAttachmentUserFlags && MailMessage.USER_HAS_NO_ATTACHMENT.equalsIgnoreCase(userFlag)) {
                        msg.setHasAttachment(false);
                    } else {
                        set.add(userFlag);
                    }
                }
                ufCol = set.isEmpty() ? null : set;
            }
            /*
             * Apply parsed flags
             */
            msg.setFlags(retval);
            msg.setColorLabel(colorLabel);
            if (null != ufCol) {
                msg.addUserFlags(ufCol.toArray(new String[ufCol.size()]));
            }
        }

        @Override
        public void handleMessage(Message message, IDMailMessage msg, org.slf4j.Logger logger) throws MessagingException {
            Flags flags = message.getFlags();
            /*
             * Parse system flags
             */
            int retval = 0;
            int colorLabel = MailMessage.COLOR_LABEL_NONE;
            Collection<String> ufCol = null;
            if (flags.contains(Flags.Flag.ANSWERED)) {
                retval |= MailMessage.FLAG_ANSWERED;
            }
            if (flags.contains(Flags.Flag.DELETED)) {
                retval |= MailMessage.FLAG_DELETED;
            }
            if (flags.contains(Flags.Flag.DRAFT)) {
                retval |= MailMessage.FLAG_DRAFT;
            }
            if (flags.contains(Flags.Flag.FLAGGED)) {
                retval |= MailMessage.FLAG_FLAGGED;
            }
            if (flags.contains(Flags.Flag.RECENT)) {
                retval |= MailMessage.FLAG_RECENT;
            }
            if (flags.contains(Flags.Flag.SEEN)) {
                retval |= MailMessage.FLAG_SEEN;
            }
            if (flags.contains(Flags.Flag.USER)) {
                retval |= MailMessage.FLAG_USER;
            }
            String[] userFlags = flags.getUserFlags();
            if (userFlags != null) {
                /*
                 * Mark message to contain user flags
                 */
                Set<String> set = new HashSet<String>(userFlags.length);
                for (String userFlag : userFlags) {
                    if (MailMessage.isColorLabel(userFlag)) {
                        try {
                            colorLabel = MailMessage.getColorLabelIntValue(userFlag);
                        } catch (@SuppressWarnings("unused") OXException e) {
                            // Cannot occur
                            colorLabel = MailMessage.COLOR_LABEL_NONE;
                        }
                    } else if (MailMessage.USER_FORWARDED.equalsIgnoreCase(userFlag)) {
                        retval |= MailMessage.FLAG_FORWARDED;
                    } else if (MailMessage.USER_READ_ACK.equalsIgnoreCase(userFlag)) {
                        retval |= MailMessage.FLAG_READ_ACK;
                    } else if (examineHasAttachmentUserFlags && MailMessage.USER_HAS_ATTACHMENT.equalsIgnoreCase(userFlag)) {
                        msg.setHasAttachment(true);
                    } else if (examineHasAttachmentUserFlags && MailMessage.USER_HAS_NO_ATTACHMENT.equalsIgnoreCase(userFlag)) {
                        msg.setHasAttachment(false);
                    }  else {
                        set.add(userFlag);
                    }
                }
                ufCol = set.isEmpty() ? null : set;
            }
            /*
             * Apply parsed flags
             */
            msg.setFlags(retval);
            msg.setColorLabel(colorLabel);
            if (null != ufCol) {
                msg.addUserFlags(ufCol.toArray(new String[ufCol.size()]));
            }
        }
    }

    private static final FetchItemHandler ENVELOPE_ITEM_HANDLER = new FetchItemHandler() {

        @Override
        public void handleItem(Item item, IDMailMessage msg, org.slf4j.Logger logger) throws MessagingException {
            ENVELOPE env = (ENVELOPE) item;
            msg.addFrom(wrap(env.from));
            msg.addTo(wrap(env.to));
            msg.addCc(wrap(env.cc));
            msg.addBcc(wrap(env.bcc));
            msg.addReplyTo(wrap(env.replyTo));
            msg.setHeader("In-Reply-To", env.inReplyTo);
            msg.setHeader("Message-Id", env.messageId);
            msg.setSubject(decodeSubject(env.subject), true);
            msg.setSentDate(env.date);
        }

        private String decodeSubject(String envelopeSubject) {
            try {
                return MimeMessageUtility.decodeEnvelopeSubject(envelopeSubject);
            } catch (Exception e) {
                LOG.warn("Failed to decode subject from ENVELOPE fetch item: `{}\u00b4. Using raw one instead.", envelopeSubject, e);
                return envelopeSubject;
            }
        }

        private InternetAddress[] wrap(InternetAddress... addresses) {
            if (null == addresses || 0 == addresses.length) {
                return null;
            }

            int length = addresses.length;
            InternetAddress[] ret = new InternetAddress[length];
            for (int i = length; i-- > 0;) {
                String sAddress = addresses[i].toString();
                try {
                    ret[i] = new QuotedInternetAddress(sAddress, false);
                } catch (AddressException e) {
                    // Use as-is
                    String parsed = e.getRef();
                    ret[i] = new PlainTextAddress(null == parsed ? sAddress : parsed);
                }
            }
            return ret;
        }

        @Override
        public void handleMessage(Message message, IDMailMessage msg, org.slf4j.Logger logger) throws MessagingException {
            msg.addFrom((InternetAddress[]) message.getFrom());
            msg.addTo((InternetAddress[]) message.getRecipients(RecipientType.TO));
            msg.addCc((InternetAddress[]) message.getRecipients(RecipientType.CC));
            msg.addBcc((InternetAddress[]) message.getRecipients(RecipientType.BCC));
            msg.addReplyTo((InternetAddress[]) message.getReplyTo());
            String[] header = message.getHeader("In-Reply-To");
            if (null != header && header.length > 0) {
                msg.addHeader("In-Reply-To", header[0]);
            }
            header = message.getHeader("Message-Id");
            if (null != header && header.length > 0) {
                msg.addHeader("Message-Id", header[0]);
            }
            header = message.getHeader("Subject");
            if (null != header && header.length > 0) {
                msg.setSubject(MimeMessageUtility.decodeMultiEncodedHeader(header[0]), true);
            }
            msg.setSentDate(message.getSentDate());
        }
    };

    private static final FetchItemHandler INTERNALDATE_ITEM_HANDLER = new FetchItemHandler() {

        @Override
        public void handleItem(Item item, IDMailMessage msg, org.slf4j.Logger logger) {
            msg.setReceivedDate(((INTERNALDATE) item).getDate());
        }

        @Override
        public void handleMessage(Message message, IDMailMessage msg, org.slf4j.Logger logger) throws MessagingException {
            msg.setReceivedDate(message.getReceivedDate());
        }
    };

    private static final FetchItemHandler SIZE_ITEM_HANDLER = new FetchItemHandler() {

        @Override
        public void handleItem(Item item, IDMailMessage msg, org.slf4j.Logger logger) {
            msg.setSize(((RFC822SIZE) item).size);
        }

        @Override
        public void handleMessage(Message message, IDMailMessage msg, org.slf4j.Logger logger) throws MessagingException {
            msg.setSize(message.getSize());
        }
    };

    // ------------------------------------------------------------------------------------------------------------------------------------------------

    private static final class BODYSTRUCTUREFetchItemHandler implements FetchItemHandler {

        private static final BODYSTRUCTUREFetchItemHandler DEFAULT_INSTANCE = new BODYSTRUCTUREFetchItemHandler(false, false, false);

        static BODYSTRUCTUREFetchItemHandler getInstance(boolean checkICal, boolean checkVCard, boolean treatEmbeddedAsAttachment) {
            if (checkICal) {
                return new BODYSTRUCTUREFetchItemHandler(checkICal, checkVCard, treatEmbeddedAsAttachment);
            }
            if (checkVCard) {
                return new BODYSTRUCTUREFetchItemHandler(checkICal, checkVCard, treatEmbeddedAsAttachment);
            }
            if (treatEmbeddedAsAttachment) {
                return new BODYSTRUCTUREFetchItemHandler(checkICal, checkVCard, treatEmbeddedAsAttachment);
            }
            return DEFAULT_INSTANCE;
        }

        // ---------------------------------------------------------------------------------------------------------------------------------

        final boolean checkICal;
        final boolean checkVCard;
        final boolean treatEmbeddedAsAttachment;

        private BODYSTRUCTUREFetchItemHandler(boolean checkICal, boolean checkVCard, boolean treatEmbeddedAsAttachment) {
            super();
            this.checkICal = checkICal;
            this.checkVCard = checkVCard;
            this.treatEmbeddedAsAttachment = treatEmbeddedAsAttachment;
        }

        @Override
        public void handleItem(Item item, IDMailMessage msg, org.slf4j.Logger logger) throws OXException {
            BODYSTRUCTURE bs = (BODYSTRUCTURE) item;
            ContentType contentType = new ContentType().setPrimaryType(bs.type).setSubType(bs.subtype);
            {
                ParameterList cParams = bs.cParams;
                if (cParams != null) {
                    for (Enumeration<?> names = cParams.getNames(); names.hasMoreElements();) {
                        String name = names.nextElement().toString();
                        String value = cParams.get(name);
                        if (!com.openexchange.java.Strings.isEmpty(value)) {
                            try {
                                contentType.setParameterErrorAware(name, MimeMessageUtility.decodeEnvelopeHeader(value));
                            } catch (OXException e) {
                                long uid = msg.getUid();
                                String folder = msg.getFolder();
                                LOG.debug("Ignoring invalid parameter in Content-Type header contained in message {} of folder {}.", uid <= 0 ? "<unknown>" : Long.toString(uid), null == folder ? "<unknown>" : folder, e);
                            }
                        }
                    }
                }
            }
            msg.setContentType(contentType);
            msg.addHeader("Content-Type", contentType.toString(true));

            if (msg.containsHasAttachment()) {
                boolean hasAttachment = msg.isHasAttachment();
                if (hasAttachment) {
                    if (checkICal && hasICal(bs)) {
                        msg.addHeader("X-ICAL", "true");
                    }

                    if (checkVCard && hasVCard(bs)) {
                        msg.addHeader("X-VCARD", "true");
                    }
                }
            } else {
                boolean hasAttachment = MimeMessageUtility.hasAttachments(bs);
                if (hasAttachment) {
                    if (checkICal && hasICal(bs)) {
                        msg.addHeader("X-ICAL", "true");
                    }

                    if (checkVCard && hasVCard(bs)) {
                        msg.addHeader("X-VCARD", "true");
                    }
                } else if (treatEmbeddedAsAttachment) {
                    hasAttachment = hasEmbedded(bs);
                }
                msg.setAlternativeHasAttachment(hasAttachment);
            }
        }

        boolean hasICal(BODYSTRUCTURE bs) {
            String baseContentType = bs.type + "/" + bs.subtype;
            if (baseContentType.indexOf("calendar") >= 0 || baseContentType.indexOf("ics") >= 0) {
                return true;
            }

            BODYSTRUCTURE[] subs = bs.bodies;
            if (null != subs) {
                for (BODYSTRUCTURE sub : subs) {
                    if (hasICal(sub)) {
                        return true;
                    }
                }
            }

            return false;
        }

        boolean hasVCard(BODYSTRUCTURE bs) {
            String baseContentType = bs.type + "/" + bs.subtype;
            if (baseContentType.indexOf("card") >= 0 || baseContentType.indexOf("vcf") >= 0) {
                return true;
            }

            BODYSTRUCTURE[] subs = bs.bodies;
            if (null != subs) {
                for (BODYSTRUCTURE sub : subs) {
                    if (hasVCard(sub)) {
                        return true;
                    }
                }
            }

            return false;
        }

        boolean hasEmbedded(BODYSTRUCTURE bs) {
            if ("image".equals(bs.type) && Strings.isNotEmpty(bs.id)) {
                return true;
            }

            BODYSTRUCTURE[] subs = bs.bodies;
            if (null != subs) {
                for (BODYSTRUCTURE sub : subs) {
                    if (hasEmbedded(sub)) {
                        return true;
                    }
                }
            }

            return false;
        }

        @Override
        public void handleMessage(Message message, IDMailMessage msg, org.slf4j.Logger logger) throws OXException {
            String contentType = null;
            try {
                try {
                    contentType = message.getContentType();
                } catch (@SuppressWarnings("unused") MessagingException e) {
                    String[] header = message.getHeader("Content-Type");
                    if (null != header && header.length > 0) {
                        contentType = header[0];
                    }
                }
                msg.setAlternativeHasAttachment(null == contentType ? false : MimeMessageUtility.hasAttachments(message, contentType));
            } catch (MessagingException e) {
                if (null == contentType) {
                    throw MimeMailException.handleMessagingException(e);
                }
                // Don't know better...
                msg.setAlternativeHasAttachment(Strings.asciiLowerCase(contentType).startsWith("multipart/mixed"));
            } catch (IOException e) {
                if ("com.sun.mail.util.MessageRemovedIOException".equals(e.getClass().getName()) || (e.getCause() instanceof MessageRemovedException)) {
                    throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
                }
                throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
            }
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------

    private static final FetchItemHandler UID_ITEM_HANDLER = new FetchItemHandler() {

        @Override
        public void handleItem(Item item, IDMailMessage msg, org.slf4j.Logger logger) {
            long id = ((UID) item).uid;
            String sUid = Long.toString(id);
            msg.setMailId(sUid);
            msg.setUid(id);
            LogProperties.put(LogProperties.Name.MAIL_MAIL_ID, sUid);
        }

        @Override
        public void handleMessage(Message message, IDMailMessage msg, org.slf4j.Logger logger) throws MessagingException {
            long id = ((IMAPFolder) message.getFolder()).getUID(message);
            msg.setMailId(Long.toString(id));
            msg.setUid(id);
        }
    };

    private static final FetchItemHandler X_REAL_UID_ITEM_HANDLER = new FetchItemHandler() {

        @Override
        public void handleItem(Item item, IDMailMessage msg, org.slf4j.Logger logger) {
            long originalUid = ((X_REAL_UID) item).uid;
            msg.setOriginalUid(originalUid);
        }

        @Override
        public void handleMessage(Message message, IDMailMessage msg, org.slf4j.Logger logger) throws MessagingException {
            // Nothing
        }
    };

    private static final FetchItemHandler X_MAILBOX_ITEM_HANDLER = new FetchItemHandler() {

        @Override
        public void handleItem(Item item, IDMailMessage msg, org.slf4j.Logger logger) {
            String mailbox = ((com.sun.mail.imap.protocol.X_MAILBOX) item).mailbox;
            msg.setOriginalFolder(new FullnameArgument(msg.getAccountId(), mailbox));
        }

        @Override
        public void handleMessage(Message message, IDMailMessage msg, org.slf4j.Logger logger) throws MessagingException {
            // Nothing
        }
    };

    private static final FetchItemHandler SNIPPET_ITEM_HANDLER = new FetchItemHandler() {

        @Override
        public void handleItem(Item item, IDMailMessage msg, org.slf4j.Logger logger) {
            String textPreview = ((SNIPPET) item).getText();
            msg.setTextPreview(textPreview);
        }

        @Override
        public void handleMessage(Message message, IDMailMessage msg, org.slf4j.Logger logger) throws MessagingException {
            // Nothing
        }
    };

    private static final FetchItemHandler PREVIEW_ITEM_HANDLER = new FetchItemHandler() {

        @Override
        public void handleItem(Item item, IDMailMessage msg, org.slf4j.Logger logger) {
            String textPreview = ((PREVIEW) item).getText();
            msg.setTextPreview(textPreview);
        }

        @Override
        public void handleMessage(Message message, IDMailMessage msg, org.slf4j.Logger logger) throws MessagingException {
            // Nothing
        }
    };

    private static final Map<Class<? extends Item>, FetchItemHandler> MAP = ImmutableMap.<Class<? extends Item>, FetchItemHandler> builder()
        .put(UID.class, UID_ITEM_HANDLER)
        .put(X_REAL_UID.class, X_REAL_UID_ITEM_HANDLER)
        .put(com.sun.mail.imap.protocol.X_MAILBOX.class, X_MAILBOX_ITEM_HANDLER)
        .put(SNIPPET.class, SNIPPET_ITEM_HANDLER)
        .put(PREVIEW.class, PREVIEW_ITEM_HANDLER)
        .put(INTERNALDATE.class, INTERNALDATE_ITEM_HANDLER)
        .put(ENVELOPE.class, ENVELOPE_ITEM_HANDLER)
        .put(RFC822SIZE.class, SIZE_ITEM_HANDLER)
        .build();

    /*-
     * ++++++++++++++ End of item handlers ++++++++++++++
     */

    /**
     * This is the Envelope item. Despite of JavaMail's ENVELOPE item, this item does not include INTERNALDATE nor RFC822.SIZE; it solely
     * consists of the ENVELOPE.
     * <p>
     * The Envelope is an aggregation of the common attributes of a Message. Implementations should include the following attributes: From,
     * To, Cc, Bcc, ReplyTo, Subject and Date. More items may be included as well.
     */
    public static final FetchProfile.Item ENVELOPE_ONLY = new MimeStorageUtility.FetchItem("ENVELOPE_ONLY");

    /**
     * This is the INTERNALDATE item.
     */
    public static final FetchProfile.Item INTERNALDATE = new MimeStorageUtility.FetchItem("INTERNALDATE");

    /**
     * This is the X-MAILBOX item.
     */
    public static final FetchProfile.Item ORIGINAL_MAILBOX = MimeStorageUtility.ORIGINAL_MAILBOX;

    /**
     * This is the X-REAL-UID item.
     */
    public static final FetchProfile.Item ORIGINAL_UID = MimeStorageUtility.ORIGINAL_UID;

    /**
     * Turns given fetch profile into FETCH items to craft a FETCH command.
     *
     * @param isRev1 Whether IMAP protocol is revision 1 or not
     * @param fp The fetch profile to convert
     * @param loadBody <code>true</code> if message body should be loaded; otherwise <code>false</code>
     * @param serverInfo The IMAP server information
     * @param previewSupported Whether target IMAP server supports <code>"PREVIEW=FUZZY"</code> capability
     * @return The FETCH items to craft a FETCH command
     */
    public static String getFetchCommand(boolean isRev1, FetchProfile fp, boolean loadBody, IMAPServerInfo serverInfo, boolean previewSupported) {
        StringBuilder command = new StringBuilder(128);
        boolean sizeIncluded;
        if (fp.contains(FetchProfile.Item.ENVELOPE)) {
            if (loadBody) {
                command.append("INTERNALDATE");
                sizeIncluded = false;
            } else {
                command.append("ENVELOPE INTERNALDATE RFC822.SIZE");
                sizeIncluded = true;
            }
        } else if (fp.contains(ENVELOPE_ONLY)) {
            if (loadBody) {
                command.append("INTERNALDATE");
            } else {
                command.append("ENVELOPE INTERNALDATE");
            }
            sizeIncluded = false;
        } else if (fp.contains(INTERNALDATE)) {
            command.append("INTERNALDATE");
            sizeIncluded = false;
        } else {
            command.append("INTERNALDATE");
            sizeIncluded = false;
        }
        if (fp.contains(FetchProfile.Item.FLAGS)) {
            command.append(" FLAGS");
        }
        if (fp.contains(FetchProfile.Item.CONTENT_INFO)) {
            command.append(" BODYSTRUCTURE");
        }
        boolean uidIncluded = fp.contains(UIDFolder.FetchProfileItem.UID);
        if (uidIncluded) {
            command.append(" UID");
        }

        Map<String, String> capabilities = null == serverInfo ? null : serverInfo.getCapabilities();

        // Decide per IMAP server
        if (fp.contains(ORIGINAL_MAILBOX)) {
            if (null != capabilities && capabilities.containsKey("XDOVECOT")) {
                command.append(" X-MAILBOX");
            } else if (!uidIncluded) {
                command.append(" UID");
            }
        }

        // Decide per IMAP server
        if (fp.contains(ORIGINAL_UID)) {
            if (null != capabilities && capabilities.containsKey("XDOVECOT")) {
                command.append(" X-REAL-UID");
            }
        }

        if (previewSupported) {
            if (fp.contains(IMAPFolder.PreviewFetchProfileItem.PREVIEW_LAZY)) {
                if (null != capabilities && capabilities.containsKey(IMAPCapabilities.CAP_TEXT_PREVIEW_NEW)) {
                    command.append(" PREVIEW (LAZY=FUZZY)");
                }
            } else if (fp.contains(IMAPFolder.PreviewFetchProfileItem.PREVIEW)) {
                if (null != capabilities && capabilities.containsKey(IMAPCapabilities.CAP_TEXT_PREVIEW_NEW)) {
                    command.append(" PREVIEW (FUZZY)");
                }
            }
        } else {
            if (fp.contains(IMAPFolder.SnippetFetchProfileItem.SNIPPETS_LAZY)) {
                if (null != capabilities && capabilities.containsKey(IMAPCapabilities.CAP_TEXT_PREVIEW)) {
                    command.append(" SNIPPET (LAZY=FUZZY)");
                }
            } else if (fp.contains(IMAPFolder.SnippetFetchProfileItem.SNIPPETS)) {
                if (null != capabilities && capabilities.containsKey(IMAPCapabilities.CAP_TEXT_PREVIEW)) {
                    command.append(" SNIPPET (FUZZY)");
                }
            }
        }

        boolean allHeaders = (fp.contains(IMAPFolder.FetchProfileItem.HEADERS) && !loadBody);
        if (allHeaders) {
            if (isRev1) {
                command.append(" BODY.PEEK[HEADER]");
            } else {
                command.append(" RFC822.HEADER");
            }
        }
        if (!sizeIncluded && fp.contains(FetchProfile.Item.SIZE)) {
            command.append(" RFC822.SIZE");
        }
        /*
         * If we're not fetching all headers, fetch individual headers
         */
        if (!allHeaders && !loadBody) {
            String[] hdrs = fp.getHeaderNames();
            if (hdrs.length > 0) {
                command.append(' ');
                if (isRev1) {
                    command.append("BODY.PEEK[HEADER.FIELDS (");
                } else {
                    command.append("RFC822.HEADER.LINES (");
                }
                command.append(hdrs[0]);
                for (int i = 1; i < hdrs.length; i++) {
                    command.append(' ');
                    command.append(hdrs[i]);
                }
                if (isRev1) {
                    command.append(")]");
                } else {
                    command.append(')');
                }
            }
        }
        if (loadBody) {
            /*
             * Load full message
             */
            if (isRev1) {
                command.append(" BODY.PEEK[]");
            } else {
                command.append(" RFC822");
            }
        }
        return command.toString();
    }

    /**
     * Strips BODYSTRUCTURE item from given fetch profile.
     *
     * @param fetchProfile The fetch profile
     * @return The fetch profile with BODYSTRUCTURE item stripped
     */
    public static FetchProfile getSafeFetchProfile(FetchProfile fetchProfile) {
        if (fetchProfile.contains(FetchProfile.Item.CONTENT_INFO)) {
            FetchProfile newFetchProfile = new FetchProfile();
            newFetchProfile.add("Content-Type");
            if (!fetchProfile.contains(UIDFolder.FetchProfileItem.UID)) {
                newFetchProfile.add(UIDFolder.FetchProfileItem.UID);
            }
            javax.mail.FetchProfile.Item[] items = fetchProfile.getItems();
            for (javax.mail.FetchProfile.Item item : items) {
                if (!FetchProfile.Item.CONTENT_INFO.equals(item)) {
                    newFetchProfile.add(item);
                }
            }
            String[] names = fetchProfile.getHeaderNames();
            for (String name : names) {
                newFetchProfile.add(name);
            }
            return newFetchProfile;
        }
        return fetchProfile;
    }

    /**
     * Gets the item associated with given class in specified <i>FETCH</i> response.
     *
     * @param <I> The returned item's class
     * @param clazz The item class to look for
     * @param fetchResponse The <i>FETCH</i> response
     * @return The item associated with given class in specified <i>FETCH</i> response or <code>null</code>.
     */
    protected static <I extends Item> I getItemOf(Class<? extends I> clazz, FetchResponse fetchResponse) {
        int len = fetchResponse.getItemCount();
        for (int i = 0; i < len; i++) {
            Item item = fetchResponse.getItem(i);
            if (clazz.isInstance(item)) {
                return clazz.cast(item);
            }
        }
        return null;
    }

}
