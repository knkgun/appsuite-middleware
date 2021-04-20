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

import java.util.Date;
import java.util.TimeZone;
import org.dmfs.rfc5545.DateTime;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.type.PublicType;
import com.openexchange.session.Session;

/**
 * {@link Consistency}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class Consistency extends com.openexchange.chronos.common.Consistency {

    /**
     * Checks and adjusts the timezones of the event's start- and end-time (in case they are <i>set</i>) to match well-known & valid
     * timezones, using different fallbacks if no exactly matching timezone is available.
     *
     * @param session The session
     * @param calendarUserId The identifier of the user to get the fallback timezone from
     * @param event The event to set the timezones in
     * @param originalEvent The original event, or <code>null</code> if not applicable
     */
    public static void adjustTimeZones(Session session, int calendarUserId, Event event, Event originalEvent) throws OXException {
        if (event.containsStartDate()) {
            event.setStartDate(selectTimeZone(session, event.getStartDate(), calendarUserId, null == originalEvent ? null : originalEvent.getStartDate()));
        }
        if (event.containsEndDate()) {
            event.setEndDate(selectTimeZone(session, event.getEndDate(), calendarUserId, null == originalEvent ? null : originalEvent.getEndDate()));
        }
    }

    private static DateTime selectTimeZone(Session session, DateTime dateTime, int calendarUserId, DateTime originalDateTime) throws OXException {
        if (null == dateTime || dateTime.isFloating() || null == dateTime.getTimeZone()) {
            return dateTime;
        }
        TimeZone selectedTimeZone = Utils.selectTimeZone(session, calendarUserId, dateTime.getTimeZone(), null == originalDateTime ? null : originalDateTime.getTimeZone());
        if (false == dateTime.getTimeZone().equals(selectedTimeZone)) {
            return DateTime.parse(selectedTimeZone, dateTime.toString());
        }
        return dateTime;
    }

    public static void setModified(CalendarSession session, Date lastModified, Event event, int modifiedBy) throws OXException {
        setModified(lastModified, event, session.getEntityResolver().applyEntityData(new CalendarUser(), modifiedBy));
    }

    public static void setCreated(CalendarSession session, Date created, Event event, int createdBy) throws OXException {
        setCreated(created, event, session.getEntityResolver().applyEntityData(new CalendarUser(), createdBy));
    }

    public static void setModified(Date lastModified, Event event, CalendarUser modifiedBy) {
        event.setLastModified(lastModified);
        event.setModifiedBy(modifiedBy);
        event.setTimestamp(lastModified.getTime());
    }

    public static void setCreated(Date created, Event event, CalendarUser createdBy) {
        event.setCreated(created);
        event.setCreatedBy(createdBy);
    }

    public static void setCalenderUser(CalendarSession session, CalendarFolder folder, Event event) throws OXException {
        if (PublicType.getInstance().equals(folder.getType())) {
            event.setCalendarUser(null);
        } else {
            event.setCalendarUser(session.getEntityResolver().applyEntityData(new CalendarUser(), folder.getCreatedBy()));
        }
    }

    private Consistency() {
        super();
    }

}
