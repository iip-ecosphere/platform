/**
 * ******************************************************************************
 * Copyright (c) {2026} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.oktoflow.platform.cmdTools;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;

/**
 * Lists  a directory so that excel can import it.
 * 
 * @author Holger Eichelberger, SSE
 */
public class PluginList {
    
    private static final boolean SHOW_ALL = true;
    private static final String TARGET_MARKER = File.separator + "target" + File.separator;
    private static final String TEST_CLASSES_MARKER = File.separator + "test-classes" + File.separator;
    
    /**
     * Lists all plugins.
     * 
     * @param args the first argument is the directory to list
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: dir");
        } else {
            Map<String, Long> plugins = new TreeMap<>();
            listPlugins(new File(args[0]), plugins);
            System.out.println();
            if (SHOW_ALL) {
                System.out.println("------------ summary-----------");
            }
            for (String fName: plugins.keySet()) {
                System.out.println(fName + "|" + plugins.get(fName));
            }
        }
    }
    
    /**
     * Lists all plugins in {@code file}.
     * 
     * @param file the file/folder to start listing
     * @param map file name - length mapping, modified as result
     */
    private static void listPlugins(File file, Map<String, Long> plugins) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (null != files) {
                if (!SHOW_ALL) {
                    System.out.print(".");
                }
                for (File f : files) {
                    listPlugins(f, plugins);
                }
            }
        } else if (file.isFile()) {
            if (isPlugin(file)) {
                long fLen = file.length();
                String fName = file.getName();
                Long pLen = plugins.get(fName);
                if (null == pLen || pLen.longValue() < fLen) {
                    plugins.put(fName, fLen);
                }
                if (SHOW_ALL) {
                    System.out.println(file.getAbsolutePath() + "|" + plugins.get(fName));
                }
            }
        }
    }
    
    /**
     * Returns whether {@code file} is probably a plugin archive.
     * 
     * @param file the file
     * @return {@code true} for plugin, {@code false} else
     */
    private static boolean isPlugin(File file) {
        boolean result = file.isFile();
        result &= file.getPath().contains(TARGET_MARKER); // no src/.../resources
        result &= !file.getPath().contains(TEST_CLASSES_MARKER); // no copied resources
        result &= file.getName().endsWith("-plugin.zip");
        return result;
    }

}
