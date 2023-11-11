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

package de.iip_ecosphere.platform.support.collector;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.function.Function;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.FileUtils;
import de.iip_ecosphere.platform.support.OsUtils;
import de.iip_ecosphere.platform.support.resources.FolderResourceResolver;
import de.iip_ecosphere.platform.support.resources.ResourceLoader;

/**
 * Simple data collector for test performance/regression. Build IDs are taken from the environment/system 
 * property {@value #PROPERTY_BUILDID}. Setup is read from {@value #SETUP_NAME} in the user's home directory.
 * Implements a simple data migration function to allow for adding additional fields.
 * 
 * Initial quick and dirty solution, may be replaced, e.g., by Kieker.
 * 
 * @author Holger Eichelberger, SSE
 */
public class Collector {

    public static final String PROPERTY_BUILDID = "iip.ciBuildId";
    public static final String SETUP_NAME = "oktoflow-collector.yml";
    private static final String SEPARATOR = ",";
    private static CollectorSetup setup;
    private static Field[] fields = {
        new Field("timestamp", 0), 
        new Field("buildId", ""), 
        new Field("execTimeMs", 0) 
    };

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
                File dataDir = new File(setup.getDataDir());
                if (!dataDir.exists()) {
                    dataDir.mkdirs();
                }
                File dataFile = new File(dataDir, tag + ".csv");
                boolean dataFileExists = dataFile.exists();
                if (dataFileExists) {
                    checkMigration(dataFile, s -> toString(s));
                }
                try (PrintWriter out = new PrintWriter(new FileWriter(dataFile, true))) {
                    if (!dataFileExists) {
                        out.println(compose(fieldNames()));
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
            return Collector.compose(o -> toString(o), entries);
        }

        /**
         * Turns an object into a string for output.
         * 
         * @param entry the entry
         * @return the corresponding string
         */
        private String toString(Object entry) {
            String result;
            if (entry instanceof String) {
                result = quote(entry.toString());
            } else {
                result = entry.toString();
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
     * A field descriptor.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class Field {
        
        private String name;
        private Object dflt;

        /**
         * Creates a field descriptor.
         * 
         * @param name the field name
         * @param dflt the default value, if formats shall be extended/migrated
         */
        public Field(String name, Object dflt) {
            this.name = name;
            this.dflt = dflt;
        }
        
        /**
         * Returns the name.
         * 
         * @return the name
         */
        public String getName() {
            return name;
        }
        
        /**
         * Returns the default value.
         * 
         * @return the default value
         */
        public Object getDflt() {
            return dflt;
        }
        
    }

    /**
     * Composes entries to an output line.
     * 
     * @param toString the function to be used to turn objects into strings
     * @param entries the individual entries
     * @return the composed line
     */
    private static String compose(Function<Object, String> toString, Object... entries) {
        String result = "";
        boolean first = true;
        for (Object e : entries) {
            if (!first) {
                result += SEPARATOR;
            }
            result += toString.apply(e);
            first = false;
        }
        for (int f = entries.length; f < fields.length; f++) {
            if (!first) {
                result += SEPARATOR;
            }
            result += toString.apply(fields[f].dflt);
            first = false;
        }
        return result;
    }

    /**
     * Checks the file for data migration.
     * 
     * @param dataFile
     * @param toString
     */
    private static void checkMigration(File dataFile, Function<Object, String> toString) {
        File tmpFile = null;
        PrintWriter out = null;
        try (LineNumberReader lnr = new LineNumberReader(new FileReader(dataFile))) {
            String line;
            do {
                line = lnr.readLine();
                if (line != null) {
                    boolean firstLine = lnr.getLineNumber() == 1;
                    String[] orig = line.split(",");
                    Object[] entries = adjust(orig, firstLine, toString);
                    if (firstLine && orig.length != entries.length) {
                        tmpFile = File.createTempFile("collector", ".csv");
                        out = new PrintWriter(new FileWriter(tmpFile));
                    }
                    if (null != out) {
                        out.println(compose(o -> o.toString(), entries));
                    }
                }
            } while (line != null);
            FileUtils.closeQuietly(out);
            FileUtils.closeQuietly(lnr);
            if (null != tmpFile) {
                Files.copy(tmpFile.toPath(), dataFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                FileUtils.deleteQuietly(tmpFile);
            }
        } catch (IOException e) {
            LoggerFactory.getLogger(Collector.class).error("Cannot migrate {}: {}", dataFile, e.getMessage());
        }
    }
    
    /**
     * Adjusts the entries during migration by adding new fields if needed.
     * 
     * @param entries the entries from the file
     * @param header are we in the first/header line or any further line
     * @param toString the toString function
     * @return the (adjusted) entries
     */
    private static Object[] adjust(String[] entries, boolean header, Function<Object, String> toString) {
        String[] result = entries;
        if (fields.length > entries.length) {
            result = new String[fields.length];
            for (int i = 0; i < result.length; i++) {
                if (i < entries.length) {
                    result[i] = entries[i];
                } else {
                    result[i] = toString.apply(header ? fields[i].getName() : fields[i].getDflt());
                }
            }
        }
        return result;
    }
    
    /**
     * Changes the actual field declarations. [testing]
     * 
     * @param newFields the new field declarations (shall add fields to existing ones!)
     */
    public static void setFields(Field[] newFields) {
        fields = newFields;
    }

    /**
     * Returns the actual field declarations. [testing]
     * 
     * @return the new field declarations
     */
    public static Field[] getFields() {
        return fields;
    }

    /**
     * Returns the field names in sequence.
     * 
     * @return the field names
     */
    private static Object[] fieldNames() {
        Object[] result = new String[fields.length];
        for (int f = 0; f < fields.length; f++) {
            result[f] = fields[f].name;
        }
        return result;
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
