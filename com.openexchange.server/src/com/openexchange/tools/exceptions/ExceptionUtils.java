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

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.openexchange.tools.exceptions;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import com.openexchange.java.Strings;
import com.openexchange.java.util.Pair;
import com.openexchange.log.LogProperties;

/**
 * Utilities for handling <tt>Throwable</tt>s and <tt>Exception</tt>s.
 */
public class ExceptionUtils {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ExceptionUtils.class);

    private static final String MARKER = " ---=== /!\\ ===--- ";

    /**
     * Checks whether the supplied <tt>Throwable</tt> is one that needs to be rethrown and swallows all others.
     *
     * @param t The <tt>Throwable</tt> to check
     */
    public static void handleThrowable(final Throwable t) {
        if (t instanceof ThreadDeath) {
            final Map<String, String> taskProperties = LogProperties.getPropertyMap();
            if (null == taskProperties) {
                LOG.error("{}Thread death{}", MARKER, MARKER, t);
            } else {
                final StringBuilder logBuilder = new StringBuilder(512);
                final Map<String, String> sorted = new TreeMap<String, String>();
                for (final Map.Entry<String, String> entry : taskProperties.entrySet()) {
                    final String propertyName = entry.getKey();
                    final String value = entry.getValue();
                    if (null != value) {
                        sorted.put(propertyName, value);
                    }
                }
                for (final Map.Entry<String, String> entry : sorted.entrySet()) {
                    logBuilder.append(Strings.getLineSeparator()).append(entry.getKey()).append('=').append(entry.getValue());
                }
                logBuilder.deleteCharAt(0);
                logBuilder.append(Strings.getLineSeparator()).append(Strings.getLineSeparator());
                logBuilder.append(MARKER);
                logBuilder.append("Thread death");
                logBuilder.append(MARKER);
                LOG.error(logBuilder.toString(), t);
            }
            throw (ThreadDeath) t;
        }
        if (t instanceof OutOfMemoryError) {
            String message = t.getMessage();
            if ("unable to create new native thread".equalsIgnoreCase(message)) {
                if (!Boolean.TRUE.equals(System.getProperties().get("__thread_dump_created"))) {
                    System.getProperties().put("__thread_dump_created", Boolean.TRUE);
                    boolean error = true;
                    StringBuilder sb = new StringBuilder(2048);
                    try {
                        // Dump all the threads to the log
                        Map<Thread, StackTraceElement[]> threads = Thread.getAllStackTraces();
                        String ls = Strings.getLineSeparator();
                        LOG.info("{}Threads: {}", ls, threads.size());
                        for (Map.Entry<Thread, StackTraceElement[]> mapEntry : threads.entrySet()) {
                            Thread thread = mapEntry.getKey();
                            sb.setLength(0);
                            sb.append("        Thread: ").append(thread).append(" java.lang.Thread.State: ").append(thread.getState().name()).append(ls);
                            for (StackTraceElement elem : mapEntry.getValue()) {
                                sb.append(elem).append(ls);
                            }
                            LOG.info(sb.toString());
                        }
                        sb = null; // Might help GC
                        LOG.info("{}    Thread dump finished{}", ls, ls);
                        error = false;
                    } finally {
                        if (error) {
                            System.getProperties().remove("__thread_dump_created");
                        }
                    }
                }
            } else if ("Java heap space".equalsIgnoreCase(message)) {
                try {
                    MBeanServer server = ManagementFactory.getPlatformMBeanServer();

                    Pair<Boolean, String> heapDumpArgs = checkHeapDumpArguments();

                    // Is HeapDumpOnOutOfMemoryError enabled?
                    if (!heapDumpArgs.getFirst().booleanValue() && !Boolean.TRUE.equals(System.getProperties().get("__heap_dump_created"))) {
                        System.getProperties().put("__heap_dump_created", Boolean.TRUE);
                        boolean error = true;
                        try {
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-hh:mm:ss", Locale.US);
                            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                            // Either "/tmp" or path configured through "-XX:HeapDumpPath" JVM argument
                            String path = null == heapDumpArgs.getSecond() ? "/tmp" : heapDumpArgs.getSecond();
                            String fn = path + "/" + dateFormat.format(new Date()) + "-heap.hprof";
                            String mbeanName = "com.sun.management:type=HotSpotDiagnostic";
                            server.invoke(new ObjectName(mbeanName), "dumpHeap", new Object[] { fn, Boolean.TRUE }, new String[] { String.class.getCanonicalName(), "boolean" });
                            LOG.info("{}    Heap snapshot dumped to file {}{}", Strings.getLineSeparator(), fn, Strings.getLineSeparator());
                            error = false;
                        } finally {
                            if (error) {
                                System.getProperties().remove("__heap_dump_created");
                            }
                        }
                    }
                } catch (Exception e) {
                    // Failed for any reason...
                }
            }
            logVirtualMachineError(t);
            throw (OutOfMemoryError) t;
        }
        if (t instanceof VirtualMachineError) {
            logVirtualMachineError(t);
            throw (VirtualMachineError) t;
        }
        // All other instances of Throwable will be silently swallowed
    }

    private static void logVirtualMachineError(final Throwable t) {
        Map<String, String> taskProperties = LogProperties.getPropertyMap();
        if (null == taskProperties) {
            LOG.error("{}The Java Virtual Machine is broken or has run out of resources necessary for it to continue operating.{}", MARKER, MARKER, t);
        } else {
            StringBuilder logBuilder = new StringBuilder(512);
            Map<String, String> sorted = new TreeMap<String, String>();
            for (Map.Entry<String, String> entry : taskProperties.entrySet()) {
                String propertyName = entry.getKey();
                String value = entry.getValue();
                if (null != value) {
                    sorted.put(propertyName, value);
                }
            }
            for (Map.Entry<String, String> entry : sorted.entrySet()) {
                logBuilder.append(Strings.getLineSeparator()).append(entry.getKey()).append('=').append(entry.getValue());
            }
            logBuilder.deleteCharAt(0);
            logBuilder.append(Strings.getLineSeparator()).append(Strings.getLineSeparator());
            logBuilder.append(MARKER);
            logBuilder.append("The Java Virtual Machine is broken or has run out of resources necessary for it to continue operating.");
            logBuilder.append(MARKER);
            LOG.error(logBuilder.toString(), t);
        }
    }

    private static Pair<Boolean, String> checkHeapDumpArguments() {
        RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
        List<String> arguments = runtimeMxBean.getInputArguments();
        boolean heapDumpOnOOm = false;
        String path = null;
        for (String argument : arguments) {
            if ("-XX:+HeapDumpOnOutOfMemoryError".equals(argument)) {
                heapDumpOnOOm = true;
            } else if (argument.startsWith("-XX:HeapDumpPath=")) {
                path = argument.substring(17).trim();
                File file = new File(path);
                if (!file.exists() || !file.canWrite()) {
                   path = null;
                }
            }
        }
        return new Pair<Boolean, String>(Boolean.valueOf(heapDumpOnOOm), path);
    }

}
