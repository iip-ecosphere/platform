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

package de.iip_ecosphere.platform.services.environment.testing;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.function.Function;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.iip_aas.json.JsonUtils;

/**
 * A simple, customizable data recorder for testing.
 * 
 * @author Holger Eichelberger, SSE
 */
public class DataRecorder {
    
    public static final Function<Object, String> JSON_FORMATTER = o -> JsonUtils.toJson(o);
    
    private Function<Object, String> formatter;
    private PrintStream out;
    private boolean emitChannel = true;

    /**
     * Creates an instance.
     * 
     * @param file the file to write to; if {@code file} is not writable, we will fall back to stdout
     * @param formatter the formatter for data passed in by {@link #record(String, Object)}. If <b>null</b>, data 
     * will be emitted via {@link Object#toString()}.
     */
    public DataRecorder(String file, Function<Object, String> formatter) {
        this(new File(file), formatter);
    }

    /**
     * Creates an instance.
     * 
     * @param file the file to write to; if {@code file} is not writable, we will fall back to stdout
     * @param formatter the formatter for data passed in by {@link #record(String, Object)}. If <b>null</b>, data 
     * will be emitted via {@link Object#toString()}.
     */
    public DataRecorder(File file, Function<Object, String> formatter) {
        if (null != file && null != file.getParentFile()) {
            file.getParentFile().mkdirs();
        }
        if (null == formatter) {
            this.formatter = d -> d.toString();
        } else {
            this.formatter = formatter;
        }
        try {
            out = new PrintStream(new FileOutputStream(file));
        } catch (IOException e) {
            LoggerFactory.getLogger(DataRecorder.class).warn(
                "Cannot open {} for writing. Redirecting to stdout.", file);
            out = System.out;
        }
    }
    
    /**
     * Shall channel information be emitted.
     * 
     * @param emitChannel {@code true} for emit channel information, {@code false} else
     */
    public void emitChannel(boolean emitChannel) {
        this.emitChannel = emitChannel;
    }
    
    /**
     * Records an object.
     * 
     * @param channel the output channel (may be empty or <b>null</b> for none)
     * @param object the object to be recorded
     */
    public synchronized void record(String channel, Object object) {
        String tmp = null == channel || !emitChannel ? "" : channel;
        if (tmp.length() > 0) {
            out.print(tmp);
            out.print(": ");
        }
        out.println(formatter.apply(object));
        out.flush();
    }
    
    /**
     * Closes this recorder.
     */
    public void close() {
        if (out != System.out) {
            PrintStream tmp = out;
            out = System.out;
            tmp.close();
        }
    }

}
