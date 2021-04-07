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

package com.openexchange.chronos.common;

import java.util.TimeZone;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.Duration;
import org.dmfs.rfc5545.recur.RecurrenceRule;
import com.openexchange.chronos.Event;
import com.openexchange.exception.OXException;

/**
 * {@link Consistency}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v8.0.0
 */
public class Consistency {

    /**
     * Adjusts the start- and end-date of the supplied event in case it is marked as "all-day". This includes the truncation of the
     * time-part in <code>UTC</code>-timezone for the start- and end-date, as well as ensuring that the end-date is at least 1 day after
     * the start-date.
     *
     * @param event The event to adjust
     */
    public static void adjustAllDayDates(Event event) {
        if (null != event.getStartDate() && event.getStartDate().isAllDay()) {
            if (event.containsEndDate() && null != event.getEndDate()) {
                if (event.getEndDate().equals(event.getStartDate())) {
                    event.setEndDate(event.getEndDate().addDuration(new Duration(1, 1, 0)));
                }
            }
        }
    }

    /**
     * Normalizes all recurrence identifiers within the supplied event so that their value matches the date type and timezone of a certain
     * <i>reference</i> date, which is usually the start date of the recurring component.
     * <p/>
     * This includes the recurrence identifier itself, and additionally the event's collection of recurrence dates, exceptions dates, and
     * change exception dates, if set.
     *
     * @param referenceDate The reference date to which the recurrence identifiers will be normalized to
     * @param event The event to normalize the recurrence identifiers in
     */
    public static void normalizeRecurrenceIDs(DateTime referenceDate, Event event) {
        if (event.containsRecurrenceId()) {
            event.setRecurrenceId(CalendarUtils.normalizeRecurrenceID(referenceDate, event.getRecurrenceId()));
        }
        if (event.containsRecurrenceDates()) {
            event.setRecurrenceDates(CalendarUtils.normalizeRecurrenceIDs(referenceDate, event.getRecurrenceDates()));
        }
        if (event.containsDeleteExceptionDates()) {
            event.setDeleteExceptionDates(CalendarUtils.normalizeRecurrenceIDs(referenceDate, event.getDeleteExceptionDates()));
        }
        if (event.containsChangeExceptionDates()) {
            event.setChangeExceptionDates(CalendarUtils.normalizeRecurrenceIDs(referenceDate, event.getChangeExceptionDates()));
        }
    }

    /**
     * Adjusts the recurrence rule according to the <a href="https://tools.ietf.org/html/rfc5545#section-3.3.10">RFC-5545, Section 3.3.10</a>
     * specification, which ensures that the value type of the UNTIL part of the recurrence rule has the same value type as the DTSTART.
     *
     * @param event The event to adjust
     * @throws OXException if the event has an invalid recurrence rule
     */
    public static void adjustRecurrenceRule(Event event) throws OXException {
        if (null == event.getRecurrenceRule()) {
            return;
        }
        RecurrenceRule rule = CalendarUtils.initRecurrenceRule(event.getRecurrenceRule());
        if (null == rule.getUntil()) {
            return;
        }
        DateTime until = rule.getUntil();
        TimeZone timeZone = event.getStartDate().getTimeZone();
        boolean startAllDay = event.getStartDate().isAllDay();
        boolean untilAllDay = until.isAllDay();
        if (startAllDay && !untilAllDay) {
            rule.setUntil(until.toAllDay());
        } else if (!startAllDay && untilAllDay) {
            rule.setUntil(new DateTime(until.getCalendarMetrics(), timeZone, until.getTimestamp()));
        } else {
            return;
        }
        event.setRecurrenceRule(rule.toString());
    }

}
