/**
 * ******************************************************************************
 * Copyright (c) {2022} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.services.environment.metricsProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.apache.commons.io.FileUtils;

import de.iip_ecosphere.platform.support.TimeUtils;

/**
 * A simple log runnable to log experimental results.
 * 
 * @author Holger Eichelberger, SSE
 */
public class LogRunnable implements Runnable {

    private Queue<LogRecord> queue = new ConcurrentLinkedDeque<>();
    private PrintStream out;
    private boolean run = true;

    /**
     * Represents a log record.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class LogRecord {
        private String activity;
        private long duration;
        
        /**
         * Creates a log record.
         * 
         * @param activity the activity to log
         * @param duration the activity duration
         */
        public LogRecord(String activity, long duration) {
            this.activity = activity;
            this.duration = duration;
        }
    }

    
    /**
     * Creates a logging runnable for the given {@code file}.
     * 
     * @param file the file
     * @throws IOException if the log file cannot be created
     */
    public LogRunnable(File file) throws IOException {
        FileUtils.deleteQuietly(file);
        out = new PrintStream(new FileOutputStream(file));
    }
    
    @Override
    public void run() {
        while (run) {
            LogRecord rec = queue.poll();
            if (null != rec) {
                out.println(rec.activity + "\t" + rec.duration);
                out.flush();
            }
            TimeUtils.sleep(100);
        }
    }

    /**
     * Logs an activity.
     * 
     * @param activity the activity name
     * @param duration the duration
     */
    public void log(String activity, long duration) {
        queue.add(new LogRecord(activity, duration));
    }
    
    /**
     * Stops the runnable.
     */
    public void stop() {
        run = false;
    }

}
