/**
 * ******************************************************************************
 * Copyright (c) {2023} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.support.collector;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.OsUtils;
import de.iip_ecosphere.platform.support.resources.FolderResourceResolver;
import de.iip_ecosphere.platform.support.resources.ResourceLoader;

/**
 * Simple data collector for test performance/regression. Build IDs are taken from the environment/system 
 * property {@value #PROPERTY_BUILDID}. Setup is read from {@value #SETUP_NAME} in the user's home directory.
 * 
 * @author Holger Eichelberger, SSE
 */
public class Collector {

    public static final String PROPERTY_BUILDID = "iip.ciBuildId";
    public static final String SETUP_NAME = "oktoflow-collector.yml";
    private static CollectorSetup setup;
    private static final String SEPARATOR = ",";

    /**
     * A datapoint constructor.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface DatapointConstructor {

        /**
         * Adds an execution time in milliseconds.
         * 
         * @param execTimeMs the execution time in milliseconds
         * @return <b>this</b> (builder style)
         */
        public DatapointConstructor addExecutionTimeMs(long execTimeMs);
        
        /**
         * Measures the execution of {@code prg}.
         * 
         * @param run the runnable to execute
         * @return <b>this</b> (builder style)
         * @see #addExecutionTimeMs(long)
         */
        public default DatapointConstructor measureMs(Runnable run) {
            long now = System.currentTimeMillis();
            run.run();
            addExecutionTimeMs(System.currentTimeMillis() - now);
            return this;
        }

        /**
         * Closes the collector, tries to persist it.
         */
        public void close();

    }

    /**
     * A CSV datapoint constructor.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class CsvDatapointConstructor implements DatapointConstructor {
        
        private String tag;
        private long timestamp;
        private String buildId = OsUtils.getPropertyOrEnv(PROPERTY_BUILDID, "").replace(",", "_");
        private Long execTimeMs;
        
        /**
         * Creates a collector constructor.
         * 
         * @param tag data collection tag
         */
        private CsvDatapointConstructor(String tag) {
            this.timestamp = System.currentTimeMillis();
            this.tag = tag;
        }

        @Override
        public DatapointConstructor addExecutionTimeMs(long execTimeMs) {
            this.execTimeMs = execTimeMs;
            return this;
        }

        @Override
        public void close() {
            if (setup != null) {
                File dataFile = new File(setup.getDataDir(), tag + ".csv");
                boolean dataFileExists = dataFile.exists();
                try (PrintWriter out = new PrintWriter(new FileWriter(dataFile))) {
                    if (!dataFileExists) {
                        out.println(compose("timestamp", "buildId", "execTimeMs"));
                    }
                    out.println(compose(timestamp, buildId, execTimeMs));
                } catch (IOException e) {
                    LoggerFactory.getLogger(Collector.class).info(
                        "Cannot write collector entry for tag '{}': {}.", tag, e.getMessage());
                }
            } else {
                LoggerFactory.getLogger(Collector.class).info(
                    "No collector setup, will discard collector entry for tag '{}'.", tag);
            }
        }

        /**
         * Composes entries to an output line.
         * 
         * @param entries the individual entries
         * @return the composed line
         */
        private String compose(Object... entries) {
            String result = "";
            boolean first = true;
            for (Object e : entries) {
                if (!first) {
                    result += SEPARATOR;
                }
                if (e instanceof String) {
                    result += quote(e.toString());
                } else {
                    result += e.toString();
                }
                first = false;
            }
            return result;
        }

        /**
         * Quotes a string text.
         * 
         * @param text the text to be quoted
         * @return the quoted text
         */
        private String quote(String text) {
            return "\"" + text + "\"";
        }

    }
    
    /**
     * Defines the setup. [testing]
     * 
     * @param aSetup the setup instance
     * @return the old setup before the call
     */
    public static CollectorSetup setSetup(CollectorSetup aSetup) {
        CollectorSetup old = setup;
        setup = aSetup;
        return old;
    }
    
    /**
     * Loads collector properties if needed.
     */
    private static void loadPropertiesIfNeeded() {
        if (null == setup) {
            InputStream in = ResourceLoader.getResourceAsStream(SETUP_NAME, FolderResourceResolver.USER_HOME);
            if (null == in) {
                LoggerFactory.getLogger(Collector.class).info("Cannot load collector properties {}. Disabling.", 
                    SETUP_NAME);
            } else {
                try {
                    setup = CollectorSetup.readFromYaml(CollectorSetup.class, in, null);
                } catch (IOException e) {
                    LoggerFactory.getLogger(Collector.class).info("Cannot load collector properties {}: {} Disabling.", 
                        SETUP_NAME, e.getMessage());
                }
            }
        }
    }
    
    /**
     * Creates a datapoint constructor.
     * 
     * @param tag the data tollection tag
     * @return the constructor instance
     */
    public static DatapointConstructor collect(String tag) {
        loadPropertiesIfNeeded();
        return new CsvDatapointConstructor(tag);
    }

}
