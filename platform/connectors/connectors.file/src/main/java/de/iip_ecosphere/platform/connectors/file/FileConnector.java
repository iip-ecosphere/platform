/**
 * ******************************************************************************
 * Copyright (c) {2025} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.connectors.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.text.StringTokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.connectors.AbstractChannelConnector;
import de.iip_ecosphere.platform.connectors.ChannelAdapterSelector;
import de.iip_ecosphere.platform.connectors.ConnectorDescriptor;
import de.iip_ecosphere.platform.connectors.ConnectorParameter;
import de.iip_ecosphere.platform.connectors.MachineConnector;
import de.iip_ecosphere.platform.connectors.formatter.OutputFormatter;
import de.iip_ecosphere.platform.connectors.parser.InputParser;
import de.iip_ecosphere.platform.connectors.types.ChannelProtocolAdapter;
import de.iip_ecosphere.platform.support.CollectionUtils;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.resources.ResourceLoader;

/**
 * Implements the generic file connector. Intended to use with appropriate data serializers or wrapped 
 * {@link InputParser}/{@link OutputFormatter} considered by code generation, e.g., to realize a JSON or CSV 
 * data connector. Do not rename, this class is referenced in {@code META-INF/services}.
 * 
 * @param <CO> the output type to the IIP-Ecosphere platform
 * @param <CI> the input type from the IIP-Ecosphere platform
 * @author Holger Eichelberger, SSE
 */
@MachineConnector(hasModel = false, supportsEvents = true, supportsHierarchicalQNames = false, 
    supportsModelCalls = false, supportsModelProperties = false, supportsModelStructs = false, specificSettings = {}, 
    supportsDataTimeDifference = true)
public class FileConnector<CO, CI> extends AbstractChannelConnector<byte[], byte[], CO, CI> {

    public static final String NAME = "File";
    public static final String SETTING_READ_FILES = "READ_FILES";
    public static final String SETTING_WRITE_FILES = "WRITE_FILES";
    public static final String SETTING_DATA_TIMEDIFFL = "DATA_TIMEDIFF";
    public static final String OUT_NAME_PREFIX = "FileConnector_";
    public static final String OUT_NAME_SUFFIX = ".txt";
    
    private static final Logger LOGGER = LoggerFactory.getLogger(FileConnector.class);
    private List<File> readFiles = new ArrayList<>();
    private File writeFiles;
    private boolean connected = false;
    private Map<String, PrintStream> out = new HashMap<>();
    private boolean outFailed = false;
    
    private String pollResult = null;
    private boolean polling = false;
    private int requestTimeout = -1;
    private int pollingFrequency = 0;
    private int fixedDataInterval = 0;
    private int nextDataInterval = -1; // use fixedDataInterval

    /**
     * The descriptor of this connector (see META-INF/services).
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class Descriptor implements ConnectorDescriptor {

        @Override
        public String getName() {
            return NAME;
        }

        @Override
        public Class<?> getType() {
            return FileConnector.class;
        }
        
    }
    
    /**
     * Creates a connector instance.
     * 
     * @param adapter the protocol adapter(s)
     * @throws IllegalArgumentException if {@code adapter} is <b>null</b> or empty or adapters are <b>null</b>
     */
    @SafeVarargs
    public FileConnector(ChannelProtocolAdapter<byte[], byte[], CO, CI>... adapter) {
        this(null, adapter);
    }

    /**
     * Creates a connector instance.
     * 
     * @param selector the adapter selector (<b>null</b> leads to a default selector for the first adapter)
     * @param adapter the protocol adapter(s)
     * @throws IllegalArgumentException if {@code adapter} is <b>null</b> or empty or adapters are <b>null</b>
     */
    @SafeVarargs
    public FileConnector(ChannelAdapterSelector<byte[], byte[], CO, CI> selector, 
        ChannelProtocolAdapter<byte[], byte[], CO, CI>... adapter) {
        super(selector, adapter);
    }
    
    @Override
    protected void connectImpl(ConnectorParameter params) throws IOException {
        String inFiles = params.getSpecificStringSetting(SETTING_READ_FILES);
        if (null != inFiles) {
            StringTokenizer tokens = new StringTokenizer(inFiles, ";:");
            while (tokens.hasNext()) {
                String token = tokens.nextToken();
                File f = new File(token);
                File[] tmp = null;
                if (f.isFile() && f.exists()) { // existing file, take that
                    this.readFiles.add(f);
                } else if (f.isDirectory() && f.exists()) { // existing directory, take all files
                    tmp = f.listFiles();
                } else { // try to compose patterns and look in parent path; more would need an Ant matcher
                    try {
                        Pattern p = Pattern.compile(token);
                        String dirTmp = "";
                        String sepTmp = "";
                        int pos = token.lastIndexOf('/');
                        if (pos > 0) {
                            dirTmp = token.substring(0, pos);
                            sepTmp = "/";
                        } else {
                            pos = token.lastIndexOf('\\');
                            if (pos > 0) {
                                dirTmp = token.substring(0, pos);
                                sepTmp = "\\";
                            }
                        }
                        final String sep = sepTmp;
                        final String dir = dirTmp;
                        
                        tmp = new File(dir).listFiles(new FilenameFilter() {
    
                            @Override
                            public boolean accept(File dirFile, String name) {
                                return p.matcher(dir + sep + name).matches();
                            }
                            
                        });
                    } catch (PatternSyntaxException e) {
                        LOGGER.warn("Pattern '{}' is not a Java regular expression. Ignoring/trying as resource.", 
                            f.getName());
                    }
                    if (tmp == null || tmp.length == 0) { // might not be a pattern rather than a resource
                        tmp = new File[] {new File(token)};
                    }
                }
                if (null != tmp) {
                    CollectionUtils.addAll(this.readFiles, tmp);
                    Collections.sort(this.readFiles, (f1, f2) -> f1.getAbsolutePath().compareTo(f2.getAbsolutePath()));
                }
            }
        } else {
            LOGGER.warn("No READ_FILES specified.");
        }
        connected = true;
        String writeFiles = params.getSpecificStringSetting(SETTING_WRITE_FILES);
        if (null != writeFiles) {
            this.writeFiles = new File(writeFiles); // file or directory
        } // warn?
        LOGGER.info("File connected with InputFile(s) " + readFiles + " OutputFile" + writeFiles);
        fixedDataInterval = params.getSpecificIntSetting(SETTING_DATA_TIMEDIFFL);
        pollingFrequency = params.getNotificationInterval();
        requestTimeout = params.getRequestTimeout();
        readData();
    }
    
    /**
     * Tries to open {@code file} or, as fallback, as a resource.
     * 
     * @param file the file to open
     * @return the opened stream
     * @throws IOException if opening {@code file} finally fails
     */
    private BufferedReader open(File file) throws IOException {
        BufferedReader result;
        try {
            result = new BufferedReader(new FileReader(file));
        } catch (IOException e) {
            InputStream in = ResourceLoader.getResourceAsStream(file.toString());
            if (in != null) {
                result = new BufferedReader(new InputStreamReader(in));
            } else {
                throw e;
            }
        }
        return result;
    }
    
    /**
     * Reads the data from all files line-by-line.
     */
    private void readData() {
        new Thread(() -> {
            for (File f: readFiles) {
                
                
                try (BufferedReader br = open(f)) {
                    String line;
                    while (connected && (line = br.readLine()) != null) {
                        if (pollingFrequency > 0) {
                            while (connected && (!polling || pollResult != null)) {
                                TimeUtils.sleep(50);
                            }
                            pollResult = line;
                        } else {
                            try {
                                received(f.getName(), line.getBytes());
                            } catch (IOException e) {
                                LoggerFactory.getLogger(getClass()).error("When receiving line: {}", e.getMessage(), e);
                            }
                            int interval = fixedDataInterval;
                            if (nextDataInterval >= 0) { // requested by parent class during received
                                interval = nextDataInterval;
                            }
                            if (interval > 0) {
                                TimeUtils.sleep(interval);
                            }
                        }
                    }
                } catch (IOException e) {
                    LOGGER.error("While reading file {}: {}", f, e.getMessage(), e);
                }
                if (!connected) {
                    break;
                }
            }
        }).start();
    }

    @Override
    protected void notifyDataTimeDifference(int difference) {
        nextDataInterval = difference;
    }

    @Override
    protected synchronized void disconnectImpl() throws IOException {
        connected = false;
        if (out != null) {
            for (PrintStream o : out.values()) {
                o.close();
            }
            out.clear();
        }
    }

    @Override
    public void dispose() {
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    protected synchronized void writeImpl(byte[] data, String channel) throws IOException {
        if (null == channel) {
            channel = "";
        }
        PrintStream out = this.out.get(channel);
        if (null != writeFiles && null == out && !outFailed) {
            File f = writeFiles;
            if (writeFiles.isDirectory()) {
                writeFiles.mkdirs();
                f = new File(writeFiles, OUT_NAME_PREFIX + System.currentTimeMillis() + "_" 
                    + Thread.currentThread().getId() + OUT_NAME_SUFFIX);
            }
            try {
                out = new PrintStream(new FileOutputStream(f));
                this.out.put(channel, out);
            } catch (IOException e) {
                outFailed = true;
                LOGGER.error("While reading file {}: {}", f, e.getMessage(), e);
            }
        }
        if (null != out) {
            out.write(data);
            out.println();
        }
    }

    @Override
    protected byte[] read() throws IOException {
        byte[] result = null;
        if (!polling) {
            polling = true;
            TimeUtils.waitFor(() -> pollResult == null, requestTimeout, 20);
            result = null == pollResult ? null : pollResult.getBytes();
            pollResult = null;
            polling = false;
        }
        return result;
    }

    @Override
    protected void error(String message, Throwable th) {
        LOGGER.error(message, th);
    }

    @Override
    public String supportedEncryption() {
        return null;
    }

    @Override
    public String enabledEncryption() {
        return null;
    }

    /**
     * Returns a file name filter for files being written if {@link #SETTING_WRITE_FILES} is a directory.
     * 
     * @return the file name filter instance
     */
    public static FilenameFilter getWriteFileNameFilter(boolean considerThread) {
        final String suffix = (considerThread ? "_" + Thread.currentThread().getId() : "") + OUT_NAME_SUFFIX;
        return new FilenameFilter() {
            
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith(OUT_NAME_PREFIX) && name.endsWith(suffix);
            }
        };
    }

}
