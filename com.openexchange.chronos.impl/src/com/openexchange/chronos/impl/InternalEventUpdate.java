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

package com.openexchange.chronos.impl;

import static com.openexchange.chronos.common.CalendarUtils.add;
import static com.openexchange.chronos.common.CalendarUtils.calculateEnd;
import static com.openexchange.chronos.common.CalendarUtils.calculateStart;
import static com.openexchange.chronos.common.CalendarUtils.combine;
import static com.openexchange.chronos.common.CalendarUtils.contains;
import static com.openexchange.chronos.common.CalendarUtils.find;
import static com.openexchange.chronos.common.CalendarUtils.getExceptionDateUpdates;
import static com.openexchange.chronos.common.CalendarUtils.hasExternalOrganizer;
import static com.openexchange.chronos.common.CalendarUtils.initRecurrenceRule;
import static com.openexchange.chronos.common.CalendarUtils.isAttendeeSchedulingResource;
import static com.openexchange.chronos.common.CalendarUtils.isGroupScheduled;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesException;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesMaster;
import static com.openexchange.chronos.common.CalendarUtils.matches;
import static com.openexchange.chronos.common.CalendarUtils.removeNonMatching;
import static com.openexchange.chronos.common.CalendarUtils.shiftRecurrenceId;
import static com.openexchange.chronos.common.CalendarUtils.shiftRecurrenceIds;
import static com.openexchange.chronos.impl.Utils.asList;
import static com.openexchange.chronos.impl.Utils.coversDifferentTimePeriod;
import static com.openexchange.chronos.impl.Utils.prepareOrganizer;
import static com.openexchange.java.Autoboxing.b;
import static com.openexchange.tools.arrays.Collections.isNullOrEmpty;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.dmfs.rfc5545.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmField;
import com.openexchange.chronos.Attachment;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.CalendarObjectResource;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Classification;
import com.openexchange.chronos.Conference;
import com.openexchange.chronos.ConferenceField;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.Organizer;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.AlarmUtils;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.DefaultCalendarObjectResource;
import com.openexchange.chronos.common.DefaultRecurrenceData;
import com.openexchange.chronos.common.DeltaEvent;
import com.openexchange.chronos.common.EventOccurrence;
import com.openexchange.chronos.common.mapping.AttendeeMapper;
import com.openexchange.chronos.common.mapping.ConferenceMapper;
import com.openexchange.chronos.common.mapping.DefaultItemUpdate;
import com.openexchange.chronos.common.mapping.EventMapper;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.CollectionUpdate;
import com.openexchange.chronos.service.EventUpdate;
import com.openexchange.chronos.service.ItemUpdate;
import com.openexchange.chronos.service.RecurrenceData;
import com.openexchange.chronos.service.RecurrenceIterator;
import com.openexchange.chronos.service.SimpleCollectionUpdate;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.type.PublicType;
import com.openexchange.groupware.tools.mappings.Mapping;

/**
 * {@link InternalEventUpdate}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class InternalEventUpdate implements EventUpdate {

    private static final Logger LOG = LoggerFactory.getLogger(InternalEventUpdate.class);

    private final CalendarSession session;
    private final CalendarUser calendarUser;
    private final CalendarFolder folder;

    private final InternalAttendeeUpdates attendeeUpdates;
    private final SimpleCollectionUpdate<Attachment> attachmentUpdates;
    private final CollectionUpdate<Alarm, AlarmField> alarmUpdates;
    private final CollectionUpdate<Conference, ConferenceField> conferenceUpdates;
    private final ItemUpdate<Event, EventField> eventUpdate;
    private final Event deltaEvent;
    private final List<Event> originalChangeExceptions;
    private final CollectionUpdate<Event, EventField> exceptionUpdates;
    private final Date timestamp;

    /**
     * Initializes a new {@link InternalEventUpdate}.
     *
     * @param session The calendar session
     * @param folder The folder the update operation is performed in
     * @param originalEvent The original event being updated
     * @param originalChangeExceptions The change exceptions of the original series event, or <code>null</code> if not applicable
     * @param originalSeriesMasterEvent The original series master event when a change exception is updated, or <code>null</code> if not applicable
     * @param updatedEvent The updated event, as passed by the client
     * @param timestamp The timestamp to apply in the updated event data
     * @param assumeExternalOrganizerUpdate <code>true</code> if an external organizer update can be assumed, <code>false</code>, otherwise
     * @param ignoredFields Additional fields to ignore during the update
     */
    public InternalEventUpdate(CalendarSession session, CalendarFolder folder, Event originalEvent, List<Event> originalChangeExceptions, Event originalSeriesMasterEvent, Event updatedEvent, Date timestamp, boolean assumeExternalOrganizerUpdate, EventField... ignoredFields) throws OXException {
        super();
        this.session = session;
        this.folder = folder;
        this.calendarUser = Utils.getCalendarUser(session, folder);
        this.originalChangeExceptions = originalChangeExceptions;
        this.timestamp = timestamp;
        /*
         * apply, check, adjust event update as needed
         */
        Event changedEvent = apply(originalEvent, updatedEvent, assumeExternalOrganizerUpdate, ignoredFields);
        checkIntegrity(originalEvent, changedEvent, originalSeriesMasterEvent);
        ensureConsistency(originalEvent, changedEvent, originalSeriesMasterEvent);
        List<Event> changedChangeExceptions = adjustExceptions(originalEvent, changedEvent, originalChangeExceptions);
        /*
         * derive & take over event update
         */
        Set<EventField> differentFields = EventMapper.getInstance().getDifferentFields(originalEvent, changedEvent, true);
        this.eventUpdate = new DefaultItemUpdate<Event, EventField>(originalEvent, changedEvent, differentFields);
        this.attendeeUpdates = InternalAttendeeUpdates.onUpdatedEvent(session, folder, originalEvent, changedEvent, timestamp);
        this.attachmentUpdates = CalendarUtils.getAttachmentUpdates(originalEvent.getAttachments(), changedEvent.getAttachments());
        this.conferenceUpdates = CalendarUtils.getConferenceUpdates(originalEvent.getConferences(), changedEvent.getConferences());
        this.alarmUpdates = AlarmUtils.getAlarmUpdates(originalEvent.getAlarms(), changedEvent.getAlarms());
        this.exceptionUpdates = CalendarUtils.getEventUpdates(originalChangeExceptions, changedChangeExceptions, EventField.ID);
        /*
         * generate special 'delta' event on top of the changed event data to indicate actual differences during storage update
         */
        this.deltaEvent = new DeltaEvent(changedEvent, differentFields);
    }

    /**
     * Gets a collection update representing the implicit changes to existing change exception events.
     *
     * @return The exception updates, or an empty update if there are none
     */
    public CollectionUpdate<Event, EventField> getExceptionUpdates() {
        return exceptionUpdates;
    }

    /**
     * Gets a list of newly deleted exceptions within a recurring event series, which includes both deleted change exceptions, as well as
     * virtual event occurrences for newly indicated delete exception events.
     *
     * @return The deleted exceptions, or an empty list of there are none
     */
    public List<Event> getDeletedExceptions() {
        if (false == isSeriesMaster(getOriginal())) {
            return Collections.emptyList();
        }
        List<Event> deletedExceptions = new ArrayList<Event>();
        deletedExceptions.addAll(getExceptionUpdates().getRemovedItems());
        SimpleCollectionUpdate<RecurrenceId> exceptionDateUpdates = getExceptionDateUpdates(getOriginal().getDeleteExceptionDates(), getUpdate().getDeleteExceptionDates());
        for (RecurrenceId recurrenceId : exceptionDateUpdates.getAddedItems()) {
            deletedExceptions.add(new EventOccurrence(getOriginal(), recurrenceId));
        }
        return deletedExceptions;
    }

    /**
     * Gets a special 'delta' event on top of the changed event data to indicate actual property differences during the storage update.
     *
     * @return The generated delta event
     */
    public Event getDelta() {
        return deltaEvent;
    }

    /**
     * Gets the <i>original</i> calendar object resource, i.e. the calendar resource before any changes have been applied.
     *
     * @return The original calendar object resource
     */
    public CalendarObjectResource getOriginalResource() {
        return new DefaultCalendarObjectResource(getOriginal(), originalChangeExceptions);
    }

    /**
     * Gets a value indicating whether the applied changes represent a <i>re-scheduling</i> of the calendar object resource or not,
     * depending on the modified event fields.
     * <p/>
     * Besides changes to the event's recurrence, start- or end-time, this also includes further important event properties, or changes
     * in the attendee line-up.
     *
     * @return <code>true</code> if the calendar resource is re-scheduled along with the update, <code>false</code>, otherwise
     * @throws OXException
     */
    public boolean isReschedule() {
        return Utils.isReschedule(this);
    }

    @Override
    public Event getOriginal() {
        return eventUpdate.getOriginal();
    }

    @Override
    public Event getUpdate() {
        return eventUpdate.getUpdate();
    }

    @Override
    public Set<EventField> getUpdatedFields() {
        return eventUpdate.getUpdatedFields();
    }

    @Override
    public boolean containsAnyChangeOf(EventField[] fields) {
        return eventUpdate.containsAnyChangeOf(fields);
    }

    @Override
    public InternalAttendeeUpdates getAttendeeUpdates() {
        return attendeeUpdates;
    }

    @Override
    public CollectionUpdate<Alarm, AlarmField> getAlarmUpdates() {
        return alarmUpdates;
    }

    @Override
    public SimpleCollectionUpdate<Attachment> getAttachmentUpdates() {
        return attachmentUpdates;
    }

    @Override
    public CollectionUpdate<Conference, ConferenceField> getConferenceUpdates() {
        return conferenceUpdates;
    }

    @Override
    public String toString() {
        return "InternalEventUpdate [eventUpdate=" + eventUpdate + ", attendeeUpdates=" + attendeeUpdates +
            ", attachmentUpdates=" + attachmentUpdates + ", conferenceUpdates=" + conferenceUpdates + ", exceptionUpdates=" + exceptionUpdates + "]";
    }

    /**
     * Adjusts any change- and delete exceptions of a recurring event along with the update of the series master, in case the original
     * event is not organized externally.
     * <p/>
     * In particular, the following changes are applied for the changed event and -exceptions:
     * <ul>
     * <li>If an event series is turned into a single event, any series exceptions are removed</li>
     * <li>If the series master event's start-date is changed, the recurrence identifiers of all change- and delete-exceptions are
     * adjusted accordingly to reflect the time shift</li>
     * <li>If the recurrence rule changes, any exceptions whose recurrence identifier no longer matches the recurrence are removed</li>
     * <li>Further changes of the series master event that can be taken over in the same way are propagated in existing change exception
     * events</li>
     * </ul>
     *
     * @param originalEvent The original event being updated
     * @param changedEvent The event representing the updated event
     * @param originalChangeExceptions The change exceptions of the original series event, or <code>null</code> if not applicable
     * @return The resulting list of (possibly adjusted) change exceptions
     */
    private List<Event> adjustExceptions(Event originalEvent, Event changedEvent, List<Event> originalChangeExceptions) throws OXException {
        if (hasExternalOrganizer(originalEvent)) {
            return originalChangeExceptions;
        }
        if (false == isSeriesMaster(originalEvent)) {
            return Collections.emptyList();
        }
        if (null == changedEvent.getRecurrenceRule()) {
            /*
             * reset all delete- and change exceptions if recurrence is deleted & indicate an empty change exception list
             */
            changedEvent.setDeleteExceptionDates(null);
            changedEvent.setChangeExceptionDates(null);
            changedEvent.setSeriesId(null);
            return Collections.emptyList();
        }
        /*
         * adjust recurrence identifiers in series master and change exceptions (to reflect change of start date)
         */
        List<Event> changedChangeExceptions = adjustRecurrenceIds(originalEvent, changedEvent, originalChangeExceptions);
        /*
         * remove not matching recurrences in change- and delete-exceptions (to reflect change of recurrence rule)
         */
        changedChangeExceptions = removeInvalidRecurrenceIds(changedEvent, changedChangeExceptions);
        /*
         * apply potential changes in exception dates (to reflect newly added delete exception dates)
         */
        changedChangeExceptions = adjustDeletedChangeExceptions(changedEvent, changedChangeExceptions);
        /*
         * ensure consistency of change exceptions and series master's change exception dates
         */
        changedChangeExceptions = ensureChangeExceptionConsistency(changedEvent, changedChangeExceptions);
        /*
         * take over non-conflicting changes in series master to change exceptions
         */
        changedChangeExceptions = propagateToChangeExceptions(originalEvent, changedEvent, originalChangeExceptions, changedChangeExceptions);

        return changedChangeExceptions;
    }

    private void ensureConsistency(Event originalEvent, Event updatedEvent, Event originalSeriesMaster) throws OXException {
        Consistency.adjustAllDayDates(updatedEvent);
        Consistency.adjustTimeZones(session.getSession(), calendarUser.getEntity(), updatedEvent, originalEvent);
        Consistency.adjustRecurrenceRule(updatedEvent);
        DateTime referenceDate = (isSeriesException(originalEvent) && null != originalSeriesMaster ? originalSeriesMaster : originalEvent).getStartDate();
        Consistency.normalizeRecurrenceIDs(referenceDate, updatedEvent);
        /*
         * adjust recurrence-related properties
         */
        if (isSeriesMaster(originalEvent) && updatedEvent.containsRecurrenceRule() && null == updatedEvent.getRecurrenceRule()) {
            /*
             * series to single event, remove recurrence & ensure all necessary recurrence data is present in passed event update
             */
            updatedEvent.setSeriesId(null);
            updatedEvent.setChangeExceptionDates(null);
            updatedEvent.setDeleteExceptionDates(null);
        }
        if (false == isSeriesMaster(originalEvent) && false == isSeriesException(originalEvent) && updatedEvent.containsRecurrenceRule() && null != updatedEvent.getRecurrenceRule()) {
            /*
             * single event to series, take over series id
             */
            updatedEvent.setSeriesId(originalEvent.getId());
        }
        /*
         * adjust attendee-dependent fields (ignore for change exceptions)
         */
        if (isSeriesException(originalEvent)) {
            EventMapper.getInstance().copy(originalEvent, updatedEvent, EventField.ORGANIZER, EventField.FOLDER_ID, EventField.CALENDAR_USER);
        } else if (isNullOrEmpty(updatedEvent.getAttendees())) {
            adjustForNonGroupScheduled(originalEvent, updatedEvent);
        } else {
            adjustForGroupScheduled(originalEvent, updatedEvent);
        }
        if (CalendarUtils.isInternal(originalEvent.getOrganizer(), CalendarUserType.INDIVIDUAL)) {
            /*
             * reset attendee's partstats if required & increment sequence number as needed
             */
            if (needsParticipationStatusReset(originalEvent, updatedEvent)) {
                resetParticipationStatus(updatedEvent.getAttendees());
            }
            if (originalEvent.getSequence() >= updatedEvent.getSequence() && needsSequenceNumberIncrement(originalEvent, updatedEvent)) {
                updatedEvent.setSequence(originalEvent.getSequence() + 1);
            }
        }
        /*
         * apply timestamp/last-modified info
         */
        Consistency.setModified(session, timestamp, updatedEvent, session.getUserId());
    }

    private void checkIntegrity(Event originalEvent, Event updatedEvent, EventField updatedField, Event originalSeriesMaster) throws OXException {
        switch (updatedField) {
            case GEO:
                Check.geoLocationIsValid(updatedEvent);
                break;
            case ATTENDEE_PRIVILEGES:
                /*
                 * check validity based on folder / organizer
                 */
                Check.attendeePrivilegesAreValid(updatedEvent.getAttendeePrivileges(), folder, updatedEvent.getOrganizer());
                /*
                 * deny different values for change exceptions
                 */
                if (isSeriesException(originalEvent) && (null == originalSeriesMaster ||
                    false == EventMapper.getInstance().get(EventField.ATTENDEE_PRIVILEGES).equals(originalSeriesMaster, updatedEvent))) {
                    throw CalendarExceptionCodes.FORBIDDEN_CHANGE.create(originalEvent.getId(), updatedField);
                }
                /*
                 * restrict changes to event organizer (but ignore if set to 'null')
                 */
                if (isGroupScheduled(originalEvent) && false == matches(originalEvent.getOrganizer(), calendarUser)) {
                    if (null != updatedEvent.getAttendeePrivileges()) {
                        throw CalendarExceptionCodes.NOT_ORGANIZER.create(
                            folder.getId(), originalEvent.getId(), originalEvent.getOrganizer().getUri(), originalEvent.getOrganizer().getCn());
                    }
                    updatedEvent.removeAttendeePrivileges();
                }
                break;
            case CLASSIFICATION:
                if (Classification.PUBLIC.matches(originalEvent.getClassification()) && Classification.PUBLIC.matches(updatedEvent.getClassification())) {
                    /*
                     * reset to original value if classification matches
                     */
                    updatedEvent.setClassification(originalEvent.getClassification());
                } else {
                    /*
                     * check validity based on folder / attendees
                     */
                    Check.classificationIsValid(updatedEvent.getClassification(), folder, updatedEvent.getAttendees());
                    /*
                     * deny different values for change exceptions
                     */
                    if (isSeriesException(originalEvent) && (null == originalSeriesMaster ||
                        false == EventMapper.getInstance().get(EventField.CLASSIFICATION).equals(originalSeriesMaster, updatedEvent))) {
                        throw CalendarExceptionCodes.UNSUPPORTED_CLASSIFICATION_FOR_OCCURRENCE.create(
                            String.valueOf(updatedEvent.getClassification()), originalEvent.getSeriesId(), String.valueOf(originalEvent.getRecurrenceId()));
                    }
                }
                break;
            case SEQUENCE:
                Check.requireInSequence(originalEvent, updatedEvent);
                break;
            case ORGANIZER:
                /*
                 * (re-)check organizer
                 */
                Check.internalOrganizerIsAttendee(updatedEvent, folder);
                break;
            case ATTENDEES:
                /*
                 * (re-)check organizer
                 */
                Check.internalOrganizerIsAttendee(updatedEvent, folder);
                /*
                 * (re-)check classification validity
                 */
                Check.classificationIsValid(updatedEvent.getClassification(), folder, updatedEvent.getAttendees());
                break;
            case START_DATE:
            case END_DATE:
                Check.startAndEndDate(session, updatedEvent);
                break;
            case RECURRENCE_RULE:
                /*
                 * ignore a 'matching' recurrence rule
                 */
                if (null != updatedEvent.getRecurrenceRule() && null != originalEvent.getRecurrenceRule() &&
                    initRecurrenceRule(updatedEvent.getRecurrenceRule()).toString().equals(initRecurrenceRule(originalEvent.getRecurrenceRule()).toString())) {
                    updatedEvent.setRecurrenceRule(originalEvent.getRecurrenceRule());
                    break;
                }
                /*
                 * deny update for change exceptions (but ignore if set to 'null')
                 */
                if (isSeriesException(originalEvent)) {
                    if (null != updatedEvent.getRecurrenceRule()) {
                        throw CalendarExceptionCodes.FORBIDDEN_CHANGE.create(originalEvent.getId(), updatedField);
                    }
                    updatedEvent.removeRecurrenceRule();
                    break;
                }
                /*
                 * check rule's validity if set
                 */
                if (null != updatedEvent.getRecurrenceRule()) {
                    String rrule = updatedEvent.getRecurrenceRule();
                    DateTime seriesStart = null != updatedEvent.getStartDate() ? updatedEvent.getStartDate() : originalEvent.getStartDate();
                    Check.recurrenceDataIsValid(session.getRecurrenceService(), new DefaultRecurrenceData(rrule, seriesStart));
                }
                break;
            case DELETE_EXCEPTION_DATES:
                /*
                 * (re-)check delete exception dates
                 */
                Check.recurrenceIdsExist(session.getRecurrenceService(), updatedEvent, updatedEvent.getDeleteExceptionDates());
                break;
            case UID:
            case SERIES_ID:
            case CALENDAR_USER:
                throw CalendarExceptionCodes.FORBIDDEN_CHANGE.create(originalEvent.getId(), updatedField);
            default:
                break;
        }
    }

    private void checkIntegrity(Event originalEvent, Event updatedEvent, Event seriesMaster) throws OXException {
        EventField[] differentFields = EventMapper.getInstance().getDifferentFields(originalEvent, updatedEvent);
        for (EventField updatedField : differentFields) {
            checkIntegrity(originalEvent, updatedEvent, updatedField, seriesMaster);
        }
    }

    /**
     * Generates the updated event by applying all changes to the original event data.
     * <ul>
     * <li>No further, implicit changes to ensure the consistency of the resulting event are performed</li>
     * <li>The updated fields collection in the resulting item update will reflect all <i>set</i> fields of the update</li>
     * </ul>
     *
     * @param originalEvent The original event being updated
     * @param updatedEvent The updated event, as passed by the client
     * @param assumeExternalOrganizerUpdate <code>true</code> if an external organizer update can be assumed, <code>false</code>, otherwise
     * @param ignoredFields Optional event fields to ignore during the update
     * @return The changed event
     */
    private Event apply(Event originalEvent, Event updatedEvent, boolean assumeExternalOrganizerUpdate, EventField... ignoredFields) throws OXException {
        /*
         * determine relevant changes in passed event update
         */
        Set<EventField> updatedFields = new HashSet<EventField>(java.util.Arrays.asList(EventMapper.getInstance().getAssignedFields(updatedEvent)));
        if (null != ignoredFields) {
            for (EventField ignoredField : ignoredFields) {
                updatedFields.remove(ignoredField);
            }
        }
        /*
         * only consider whitelist of fields in attendee scheduling resources as needed
         */
        boolean isAttendeeSchedulingResource = isAttendeeSchedulingResource(originalEvent, calendarUser.getEntity());
        boolean ignoreForbiddenAttendeenChanges = b(session.get(CalendarParameters.PARAMETER_IGNORE_FORBIDDEN_ATTENDEE_CHANGES, Boolean.class, Boolean.FALSE));
        if (isAttendeeSchedulingResource && ignoreForbiddenAttendeenChanges && !assumeExternalOrganizerUpdate) {
            //TODO: TRANSP is not yet handled as per-user property, so ignore changes in attendee scheduling resources for now
            updatedFields.retainAll(EnumSet.of(
                EventField.ALARMS, EventField.ATTENDEES, /* EventField.TRANSP,*/ EventField.DELETE_EXCEPTION_DATES, EventField.CREATED, EventField.TIMESTAMP, EventField.LAST_MODIFIED
            ));
        }
        /*
         * strip any 'per-user' properties
         */
        updatedFields.remove(EventField.FOLDER_ID);
        updatedFields.remove(EventField.ALARMS);
        /*
         * (virtually) apply all changes of the passed event update
         */
        EventField[] changedFields = updatedFields.toArray(new EventField[updatedFields.size()]);
        Event changedEvent = EventMapper.getInstance().copy(originalEvent, null, (EventField[]) null);
        changedEvent = EventMapper.getInstance().copy(updatedEvent, changedEvent, changedFields);
        /*
         * (virtually) apply & take over attendee updates in changed event
         */
        if (updatedFields.contains(EventField.ATTENDEES)) {
            List<Attendee> changedAttendees = InternalAttendeeUpdates.onUpdatedEvent(session, folder, originalEvent, updatedEvent, timestamp).previewChanges();
            /*
             * only consider 'own' attendee in attendee scheduling resources as needed
             */
            if (isAttendeeSchedulingResource && ignoreForbiddenAttendeenChanges) {
                Attendee changedUserAttendee = find(changedAttendees, calendarUser);
                List<Attendee> updatedAttendees = new ArrayList<Attendee>(originalEvent.getAttendees().size());
                for (Attendee originalAttendee : originalEvent.getAttendees()) {
                    if (false == matches(originalAttendee, calendarUser)) {
                        updatedAttendees.add(originalAttendee);
                    } else if (null != changedUserAttendee) {
                        updatedAttendees.add(changedUserAttendee);
                    }
                }
                changedAttendees = updatedAttendees;
            }
            changedEvent.setAttendees(AttendeeMapper.getInstance().copy(changedAttendees, (AttendeeField[]) null));
        }
        return changedEvent;
    }

    /**
     * Prepares certain properties in the passed event update to reflect that the event is or is now a <i>group-scheduled</i> event.
     * <p/>
     * This includes the organizer property of the event, as well as the common parent folder identifier and associated the calendar user
     * of the event.
     *
     * @param originalEvent The original event
     * @param updatedEvent The updated event
     */
    private void adjustForGroupScheduled(Event originalEvent, Event updatedEvent) throws OXException {
        /*
         * group-scheduled event, ensure to take over an appropriate organizer & reset common calendar folder (unless public)
         */
        if (null == originalEvent.getOrganizer()) {
            updatedEvent.setOrganizer(prepareOrganizer(session, folder, updatedEvent.getOrganizer(), null));
        } else if (updatedEvent.containsOrganizer()) {
            Organizer organizer = session.getEntityResolver().prepare(updatedEvent.getOrganizer(), CalendarUserType.INDIVIDUAL);
            if (null != organizer && false == matches(originalEvent.getOrganizer(), organizer)) {
                throw CalendarExceptionCodes.FORBIDDEN_CHANGE.create(originalEvent.getId(), EventField.ORGANIZER);
            }
            updatedEvent.setOrganizer(originalEvent.getOrganizer()); // ignore
        }
        if (PublicType.getInstance().equals(folder.getType())) {
            if (null == originalEvent.getFolderId() || updatedEvent.containsFolderId()) {
                updatedEvent.setFolderId(folder.getId());
            }
            if (null != originalEvent.getCalendarUser() || updatedEvent.containsCalendarUser()) {
                updatedEvent.setCalendarUser(null);
            }
        } else {
            if (null != originalEvent.getFolderId() || updatedEvent.containsFolderId()) {
                updatedEvent.setFolderId(null);
            }
            if (null == originalEvent.getCalendarUser()) {
                updatedEvent.setCalendarUser(calendarUser);
            } else if (updatedEvent.containsCalendarUser()) {
                if (false == matches(updatedEvent.getCalendarUser(), originalEvent.getCalendarUser())) {
                    throw CalendarExceptionCodes.FORBIDDEN_CHANGE.create(originalEvent.getId(), EventField.CALENDAR_USER);
                }
                updatedEvent.setCalendarUser(originalEvent.getCalendarUser()); // ignore
            }
        }
    }

    /**
     * Prepares certain properties in the passed event update to reflect that the event is not or no longer a <i>group-scheduled</i> event.
     * <p/>
     * This includes the organizer property of the event, as well as the common parent folder identifier and associated the calendar user
     * of the event.
     *
     * @param originalEvent The original event
     * @param updatedEvent The updated event
     */
    private void adjustForNonGroupScheduled(Event originalEvent, Event updatedEvent) {
        /*
         * no group-scheduled event (any longer), ensure to take over common calendar folder & user, remove organizer
         */
        if (null != originalEvent.getOrganizer() || updatedEvent.containsOrganizer()) {
            updatedEvent.setOrganizer(null);
        }
        if (false == folder.getId().equals(originalEvent.getFolderId()) || updatedEvent.containsFolderId()) {
            updatedEvent.setFolderId(folder.getId());
        }
        CalendarUser newCalendarUser = PublicType.getInstance().equals(folder.getType()) ? null : calendarUser;
        if (false == matches(newCalendarUser, originalEvent.getCalendarUser()) || updatedEvent.containsCalendarUser()) {
            updatedEvent.setCalendarUser(newCalendarUser);
        }
    }

    /**
     * Gets a value indicating whether the event's sequence number ought to be incremented along with the update or not.
     *
     * @param originalEvent The original event
     * @param updatedEvent The updated event
     * @return <code>true</code> if the event's sequence number should be updated, <code>false</code>, otherwise
     * @see com.openexchange.chronos.impl.performer.AbstractUpdatePerformer
     */
    private static boolean needsSequenceNumberIncrement(Event originalEvent, Event updatedEvent) {
        EventField[] relevantFields = new EventField[] {
            EventField.SUMMARY, EventField.LOCATION, EventField.RECURRENCE_RULE, EventField.START_DATE, EventField.END_DATE,
            EventField.RECURRENCE_DATES, EventField.DELETE_EXCEPTION_DATES, EventField.TRANSP, EventField.CONFERENCES
        };
        if (false == EventMapper.getInstance().equalsByFields(originalEvent, updatedEvent, relevantFields)) {
            return true;
        }
        if (false == matches(originalEvent.getAttendees(), updatedEvent.getAttendees())) {
            //TODO: more distinct evaluation of attendee updates
            return true;
        }
        return false;
    }

    private List<Event> propagateToChangeExceptions(Event originalMaster, Event updatedMaster, List<Event> originalChangeExceptions, List<Event> updatedChangeExceptions) throws OXException {
        List<Event> changedChangeExceptions = EventMapper.getInstance().copy(updatedChangeExceptions, (EventField[]) null);
        /*
         * apply common changes in 'basic' fields'
         */
        EventField[] basicFields = {
            EventField.TRANSP, EventField.STATUS, EventField.CATEGORIES, EventField.SUMMARY, EventField.LOCATION, EventField.DESCRIPTION,
            EventField.COLOR, EventField.URL, EventField.GEO
        };
        for (EventField field : basicFields) {
            propagateFieldUpdate(originalMaster, updatedMaster, field, changedChangeExceptions, false);
        }
        /*
         * always take over a change in classification (to prevent different classification in event series)
         */
        EventField[] overwrittenFields = { EventField.CLASSIFICATION, EventField.ATTENDEE_PRIVILEGES };
        for (EventField field : overwrittenFields) {
            propagateFieldUpdate(originalMaster, updatedMaster, field, changedChangeExceptions, true);
        }
        /*
         * take over changes in start- and/or end-date based on calculated original timeslot
         */
        if (false == EventMapper.getInstance().get(EventField.START_DATE).equals(originalMaster, updatedMaster) ||
            false == EventMapper.getInstance().get(EventField.END_DATE).equals(originalMaster, updatedMaster)) {
            for (Event changedChangeException : changedChangeExceptions) {
                Event originalChangeException = find(originalChangeExceptions, changedChangeException.getId());
                if (null == originalChangeException) {
                    continue;
                }
                DateTime originalOccurrenceStart = calculateStart(originalMaster, originalChangeException.getRecurrenceId());
                DateTime originalOccurrenceEnd = calculateEnd(originalMaster, originalChangeException.getRecurrenceId());
                if (originalOccurrenceStart.equals(originalChangeException.getStartDate()) && originalOccurrenceEnd.equals(originalChangeException.getEndDate())) {
                    changedChangeException.setStartDate(calculateStart(updatedMaster, changedChangeException.getRecurrenceId()));
                    changedChangeException.setEndDate(calculateEnd(updatedMaster, changedChangeException.getRecurrenceId()));
                }
            }
        }
        /*
         * apply added & removed attendees
         */
        InternalAttendeeUpdates attendeeUpdates = InternalAttendeeUpdates.onUpdatedEvent(session, folder, originalMaster, updatedMaster, timestamp);
        changedChangeExceptions = propagateAttendeeUpdates(attendeeUpdates, changedChangeExceptions);
        /*
         * apply added & removed conferences
         */
        CollectionUpdate<Conference, ConferenceField> conferenceUpdates = CalendarUtils.getConferenceUpdates(originalMaster.getConferences(), updatedMaster.getConferences());
        changedChangeExceptions = propagateConferenceUpdates(originalMaster.getConferences(), conferenceUpdates, changedChangeExceptions);

        return changedChangeExceptions;
    }

    /**
     * Propagates an update of a specific property in a series master event to any change exception events, i.e. the new property value is
     * also applied in the change exception events. Optionally, the value is only taken over if the value in the change exception equals
     * the value of the original series event.
     *
     * @param originalMaster The original series master event being updated
     * @param updatedMaster The updated series master event
     * @param field The event field to propagate
     * @param changeExceptions The list of events to propagate the field update to
     * @param overwrite <code>true</code> to always apply the updated value in the change exceptions, <code>false</code> to only apply the
     *            new value if the property's value is unchanged from the original series master event
     * @return The (possibly adjusted) list of change exception events
     */
    private static List<Event> propagateFieldUpdate(Event originalMaster, Event updatedMaster, EventField field, List<Event> changeExceptions, boolean overwrite) throws OXException {
        Mapping<? extends Object, Event> mapping = EventMapper.getInstance().get(field);
        if (mapping.equals(originalMaster, updatedMaster) || isNullOrEmpty(changeExceptions)) {
            return changeExceptions;
        }
        for (Event changeException : changeExceptions) {
            if (overwrite || mapping.equals(originalMaster, changeException)) {
                mapping.copy(updatedMaster, changeException);
            }
        }
        return changeExceptions;
    }

    /**
     * Propagates any added and removed attendees found in a specific collection update to one or more events, i.e. added attendees are
     * also added in each attendee list of the supplied events, unless they do not already attend, and removed attendees are also removed
     * from each attendee list if contained.
     *
     * @param attendeeUpdates The attendee collection update to propagate
     * @param changeExceptions The list of events to propagate the attendee updates to
     * @return The (possibly adjusted) list of events
     */
    private static List<Event> propagateAttendeeUpdates(SimpleCollectionUpdate<Attendee> attendeeUpdates, List<Event> changeExceptions) {
        if (null == attendeeUpdates || attendeeUpdates.isEmpty() || isNullOrEmpty(changeExceptions)) {
            return changeExceptions;
        }
        for (Event changeException : changeExceptions) {
            for (Attendee addedAttendee : attendeeUpdates.getAddedItems()) {
                if (false == contains(changeException.getAttendees(), addedAttendee)) {
                    changeException.getAttendees().add(addedAttendee);
                }
            }
            for (Attendee removedAttendee : attendeeUpdates.getRemovedItems()) {
                Attendee matchingAttendee = find(changeException.getAttendees(), removedAttendee);
                if (null != matchingAttendee) {
                    changeException.getAttendees().remove(matchingAttendee);
                }
            }
        }
        return changeExceptions;
    }

    /**
     * Propagates any added and removed conferences found in a specific collection update to one or more events, i.e. added conferences are
     * also added in each conference list of the supplied events, unless they do not already exist, and removed conferences are also removed
     * from each attendee list if contained.
     *
     * @param originalSeriesConferences The original conferences of the series master event, or <code>null</code> if there were none
     * @param conferenceUpdates The conference collection update to propagate
     * @param changeExceptions The list of events to propagate the conference updates to
     * @return The (possibly adjusted) list of events
     */
    private static List<Event> propagateConferenceUpdates(List<Conference> originalSeriesConferences, CollectionUpdate<Conference, ConferenceField> conferenceUpdates, List<Event> changeExceptions) throws OXException {
        if (null == conferenceUpdates || conferenceUpdates.isEmpty() || isNullOrEmpty(changeExceptions)) {
            return changeExceptions;
        }
        for (Event changeException : changeExceptions) {
            /*
             * propagate collection update if change exception's conference collection matches the original conferences of the series
             */
            if (false == CalendarUtils.getConferenceUpdates(originalSeriesConferences, changeException.getConferences()).isEmpty()) {
                continue; // already diverged
            }
            for (Conference addedItem : conferenceUpdates.getAddedItems()) {
                Conference conference = ConferenceMapper.getInstance().copy(addedItem, null, (ConferenceField[]) null);
                conference.removeId();
                if (null == changeException.getConferences()) {
                    changeException.setConferences(new ArrayList<Conference>());
                }
                changeException.getConferences().add(addedItem);
            }
            for (Conference removedItem : conferenceUpdates.getRemovedItems()) {
                Conference matchingConference = find(changeException.getConferences(), removedItem);
                if (null != matchingConference) {
                    changeException.getConferences().remove(matchingConference);
                }
            }
            for (ItemUpdate<Conference, ConferenceField> updatedItem : conferenceUpdates.getUpdatedItems()) {
                Conference matchingConference = find(changeException.getConferences(), updatedItem.getOriginal());
                if (null != matchingConference) {
                    ConferenceField[] updatedFields = updatedItem.getUpdatedFields().toArray(new ConferenceField[updatedItem.getUpdatedFields().size()]);
                    ConferenceMapper.getInstance().copy(updatedItem.getUpdate(), matchingConference, updatedFields);
                }
            }
        }
        return changeExceptions;
    }

    protected List<Event> removeInvalidRecurrenceIds(Event seriesMaster, List<Event> changeExceptions) throws OXException {
        /*
         * build list of possible exception dates
         */
        SortedSet<RecurrenceId> exceptionDates = combine(seriesMaster.getDeleteExceptionDates(), seriesMaster.getChangeExceptionDates());
        if (exceptionDates.isEmpty()) {
            return Collections.emptyList();
        }
        RecurrenceData recurrenceData = new DefaultRecurrenceData(seriesMaster.getRecurrenceRule(), seriesMaster.getStartDate(), null);
        Date from = new Date(exceptionDates.first().getValue().getTimestamp());
        Date until = add(new Date(exceptionDates.last().getValue().getTimestamp()), Calendar.DATE, 1);
        List<RecurrenceId> possibleExceptionDates = asList(session.getRecurrenceService().iterateRecurrenceIds(recurrenceData, from, until));
        /*
         * remove not matching delete- and change exceptions
         */
        if (false == isNullOrEmpty(seriesMaster.getDeleteExceptionDates())) {
            SortedSet<RecurrenceId> newDeleteExceptionDates = new TreeSet<RecurrenceId>(seriesMaster.getDeleteExceptionDates());
            if (removeNonMatching(newDeleteExceptionDates, possibleExceptionDates)) {
                seriesMaster.setDeleteExceptionDates(newDeleteExceptionDates);
            }
        }
        if (false == isNullOrEmpty(seriesMaster.getChangeExceptionDates())) {
            SortedSet<RecurrenceId> newChangeExceptionDates = new TreeSet<RecurrenceId>(seriesMaster.getChangeExceptionDates());
            if (removeNonMatching(newChangeExceptionDates, possibleExceptionDates)) {
                seriesMaster.setChangeExceptionDates(newChangeExceptionDates);
            }
        }
        if (false == isNullOrEmpty(changeExceptions)) {
            List<Event> newChangeExceptions = new ArrayList<Event>(changeExceptions);
            if (newChangeExceptions.removeIf(event -> false == contains(possibleExceptionDates, event.getRecurrenceId()))) {
                changeExceptions = newChangeExceptions;
            }
        }
        return changeExceptions;
    }

    /**
     * Removes any change exception in case it is indicated within the series master event's set of delete exception dates. This may
     * affect both the series master event's change exception dates, as well as the collection of actual change exception events.
     *
     * @param seriesMaster The series master event
     * @param changeExceptions The change exception events
     * @return The resulting list of (possibly adjusted) change exceptions
     */
    private List<Event> adjustDeletedChangeExceptions(Event seriesMaster, List<Event> changeExceptions) {
        if (false == isNullOrEmpty(seriesMaster.getDeleteExceptionDates())) {
            if (false == isNullOrEmpty(changeExceptions)) {
                List<Event> newChangeExceptions = new ArrayList<Event>(changeExceptions);
                if (newChangeExceptions.removeIf(event -> contains(seriesMaster.getDeleteExceptionDates(), event.getRecurrenceId()))) {
                    changeExceptions = newChangeExceptions;
                }
            }
            if (false == isNullOrEmpty(seriesMaster.getChangeExceptionDates())) {
                SortedSet<RecurrenceId> newChangeExceptionDates = new TreeSet<RecurrenceId>(seriesMaster.getChangeExceptionDates());
                if (newChangeExceptionDates.removeIf(recurrenceId -> contains(seriesMaster.getDeleteExceptionDates(), recurrenceId))) {
                    seriesMaster.setChangeExceptionDates(newChangeExceptionDates);
                }
            }
        }
        return changeExceptions;
    }

    /**
     * Removes any <i>redundant</i> change exception events in case there are multiple defined for the same recurrence identifier so that
     * the series' consistency is guaranteed. In such a case, only the 'first' one is preserved in the list of change exception events.
     * Also, the series master event's list of change exception dates is updated accordingly.
     *
     * @param seriesMaster The series master event
     * @param changeExceptions The change exception events
     * @return The resulting list of (possibly adjusted) change exceptions
     */
    private List<Event> ensureChangeExceptionConsistency(Event seriesMaster, List<Event> changeExceptions) {
        if (isNullOrEmpty(changeExceptions)) {
            /*
             * ensure series master's change exception dates is empty, too
             */
            if (false == isNullOrEmpty(seriesMaster.getChangeExceptionDates())) {
                LOG.warn("Inconsistent list of change exception dates in series master {}, correcting to {}.",
                    seriesMaster.getChangeExceptionDates(), Collections.emptyList());
                seriesMaster.setChangeExceptionDates(null);
            }
            return changeExceptions;
        }
        /*
         * remove any duplicate recurrence identifiers within change exceptions, as well as invalid recurrence identifiers within
         * series master's list of change exceptions
         */
        List<Event> checkedChangeExceptions = new ArrayList<Event>(changeExceptions.size());
        SortedSet<RecurrenceId> checkedChangeExceptionDates = new TreeSet<RecurrenceId>();
        for (Event changeException : changeExceptions) {
            if (false == contains(checkedChangeExceptionDates, changeException.getRecurrenceId())) {
                checkedChangeExceptionDates.add(changeException.getRecurrenceId());
                checkedChangeExceptions.add(changeException);
            } else {
                LOG.warn("Duplicate change exception event {}, skipping.", changeException);
            }
        }
        if (false == getExceptionDateUpdates(checkedChangeExceptionDates, seriesMaster.getChangeExceptionDates()).isEmpty()) {
            LOG.warn("Inconsistent list of change exception dates in series master {}, correcting to {}.", seriesMaster.getChangeExceptionDates(), checkedChangeExceptionDates);
            seriesMaster.setChangeExceptionDates(checkedChangeExceptionDates);
        }
        return checkedChangeExceptions;
    }

    /**
     * Adjusts the recurrence identifiers of any change- and delete-exceptions in an event series to reflect a change of the series start
     * date by applying an offset based on the difference of an original and updated series start date.
     *
     * @param originalEvent The original event
     * @param updatedEvent The updated event
     * @param originalChangeExceptions The change exceptions of the original series event, or <code>null</code> if not applicable
     * @return The resulting list of (possibly adjusted) change exceptions
     */
    private List<Event> adjustRecurrenceIds(Event originalEvent, Event updatedEvent, List<Event> originalChangeExceptions) throws OXException {
        if (false == isSeriesMaster(originalEvent)) {
            return Collections.emptyList();
        }
        DateTime originalSeriesStart = originalEvent.getStartDate();
        DateTime updatedSeriesStart = updatedEvent.getStartDate();
        if (false == originalSeriesStart.equals(updatedSeriesStart)) {
            /*
             * start date change, determine start- and end-time of first occurrence
             */
            RecurrenceIterator<RecurrenceId> iterator = session.getRecurrenceService().iterateRecurrenceIds(new DefaultRecurrenceData(originalEvent.getRecurrenceRule(), originalSeriesStart));
            if (iterator.hasNext()) {
                originalSeriesStart = iterator.next().getValue();
            }
            iterator = session.getRecurrenceService().iterateRecurrenceIds(new DefaultRecurrenceData(updatedEvent.getRecurrenceRule(), updatedSeriesStart));
            if (iterator.hasNext()) {
                updatedSeriesStart = iterator.next().getValue();
            }
            /*
             * shift recurrence identifiers for delete- and change-exception collections in changed event by same offset
             * (unless already done by the client)
             */
            updatedEvent.setDeleteExceptionDates(shiftRecurrenceIds(originalEvent.getDeleteExceptionDates(), updatedEvent.getDeleteExceptionDates(), originalSeriesStart, updatedSeriesStart));
            updatedEvent.setChangeExceptionDates(shiftRecurrenceIds(originalEvent.getChangeExceptionDates(), updatedEvent.getChangeExceptionDates(), originalSeriesStart, updatedSeriesStart));
            /*
             * also shift recurrence identifier of existing change exceptions
             */
            if (false == isNullOrEmpty(originalChangeExceptions)) {
                List<Event> changedChangeExceptions = EventMapper.getInstance().copy(originalChangeExceptions, (EventField[]) null);
                for (Event changeException : changedChangeExceptions) {
                    RecurrenceId newRecurrenceId = shiftRecurrenceId(changeException.getRecurrenceId(), originalSeriesStart, updatedSeriesStart);
                    changeException.setRecurrenceId(newRecurrenceId);
                    changeException.setChangeExceptionDates(new TreeSet<RecurrenceId>(Collections.singleton(newRecurrenceId)));
                }
                return changedChangeExceptions;
            }
        }
        return originalChangeExceptions;
    }

    /**
     * Resets the participation status of all individual attendees - excluding the current calendar user - to
     * {@link ParticipationStatus#NEEDS_ACTION} for a specific event.
     *
     * @param attendees The event's attendees
     */
    private void resetParticipationStatus(List<Attendee> attendees) {
        for (Attendee attendee : CalendarUtils.filter(attendees, null, CalendarUserType.INDIVIDUAL)) {
            if (calendarUser.getEntity() != attendee.getEntity()) {
                attendee.setPartStat(ParticipationStatus.NEEDS_ACTION); //TODO: or reset to initial partstat based on folder type?
                attendee.setTimestamp(timestamp.getTime());
                attendee.setHidden(false);
                continue;
            }
        }
    }

    /**
     * Gets a value indicating whether the participation status of the event's attendees needs to be reset along with the update or not.
     *
     * @param originalEvent The original event being updated
     * @param updatedEvent The updated event, as passed by the client
     * @return <code>true</code> if the attendee's participation status should be reseted, <code>false</code>, otherwise
     * @see <a href="https://tools.ietf.org/html/rfc6638#section-3.2.8">RFC 6638, section 3.2.8</a>
     */
    private boolean needsParticipationStatusReset(Event originalEvent, Event updatedEvent) throws OXException {
        /*
         * reset participation status if a different time period will be occupied by the update
         */
        return coversDifferentTimePeriod(originalEvent, updatedEvent);
    }

}
