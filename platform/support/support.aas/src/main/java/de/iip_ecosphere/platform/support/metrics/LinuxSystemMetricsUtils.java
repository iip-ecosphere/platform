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

package de.iip_ecosphere.platform.support.metrics;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.function.Function;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * Collection of helper functions to read out measurements from the Linux file system, e.g., from {@code /sys}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class LinuxSystemMetricsUtils {

    public static final String THERMAL_FOLDER = "/sys/class/thermal";
    private static String thermalFolder = THERMAL_FOLDER;
    
    /**
     * Allows changing the default value of thermal folder ({@value #THERMAL_FOLDER}).
     * 
     * @param folder the new folder, ignored if <b>null</b> or empty
     * @return the value before setting
     */
    public static String setThermalFolder(String folder) {
        String old = thermalFolder;
        if (null != folder && folder.length() > 0) {
            thermalFolder = folder;
        }
        return old;
    }
    
    /**
     * Returns the {@code temp} file from Linux {@code /sys/class/thermal} with type starting with {@code typePrefix}.
     * 
     * @param file the file that may already contain the temp file (may be a stored/cached info, may be null)
     * @param typePrefix the type prefix to select among multiple thermal files
     * @return the file or <b>null</b> if there is none
     */
    public static File getSysTempFile(File file, String typePrefix) {
        File result = file;
        if (null == result) {
            File thermal = new File(thermalFolder);
            if (thermal.exists()) {
                for (File f : thermal.listFiles()) {
                    if (f.isDirectory()) {
                        File typeFile = new File(f, "type");
                        try {
                            String type = FileUtils.readFileToString(typeFile, Charset.defaultCharset());
                            if (type.startsWith(typePrefix)) {
                                result = new File(f, "temp");
                                break;
                            }
                        } catch (IOException e) {
                        } 
                    }
                }
            }
        }
        return result;
    }
    
    /**
     * Returns the Linux temperature from a thermal {@code temp} file.
     * 
     * @param file the file, may be <b>null</b> leading to {@code SystemMetrics#INVALID_CELSIUS_TEMPERATURE}
     * @return the temperature
     * @see #getSysTempFile(File, String)
     */
    public static float getSysTemp(File file) {
        float result = SystemMetrics.INVALID_CELSIUS_TEMPERATURE;
        if (null != file) {
            try {
                String tmp = FileUtils.readFileToString(file, Charset.defaultCharset()).trim();
                result = (float) (Integer.parseInt(tmp) / 1000.0);
            } catch (IOException | NumberFormatException e) {
            } 
        }
        return result;
    }
    
    /**
     * Some measures can be obtained by running a program with parameters.
     * 
     * @param dflt the default value to return if the program fails
     * @param call the program to call and the arguments in individual strings
     * @return the information read from the program or {@code dflt}
     */
    public static String readStdoutFromProgram(String dflt, String... call) {
        String result = dflt;
        try {
            Process p = new ProcessBuilder(call).start();
            result = IOUtils.toString(p.getInputStream(), Charset.defaultCharset());
        } catch (IOException e) {
        }
        return result;
    }
    
    /**
     * Reads an integer value from a program, assuming that a single integer value will be emitted by the program.
     * 
     * @param dflt the default value to return if the program fails or the output is not a single integer
     * @param call the program to call and the arguments in individual strings
     * @return the integer value or {@code dflt}
     * @see #readIntStdoutFromProgram(int, Function, String...)
     */
    public static int readIntStdoutFromProgram(int dflt, String... call) {
        return readIntStdoutFromProgram(dflt, null, call);
    }
    
    /**
     * Reads an integer value from a program, potentially after processing the output.
     * 
     * @param dflt the default value to return if the program fails or the output is not a single integer
     * @param pre optional pre-processor to select the integer value
     * @param call the program to call and the arguments in individual strings
     * @return the integer value or {@code dflt}
     * @see #readStdoutFromProgram(String, String...)
     */
    public static int readIntStdoutFromProgram(int dflt, Function<String, String> pre, String... call) {
        int result;
        String tmp = readStdoutFromProgram(String.valueOf(dflt), call);
        if (null != pre) {
            tmp = pre.apply(tmp);
        }
        try {
            result = Integer.parseInt(tmp);
        } catch (NumberFormatException e) {
            result = dflt;
        } 
        return result;
    }

}
