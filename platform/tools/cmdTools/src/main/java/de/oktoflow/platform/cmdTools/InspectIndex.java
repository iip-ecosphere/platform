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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.oktoflow.platform.tools.lib.loader.LoaderIndex;

/**
 * Inspects class loader indexes.
 * 
 * @author Holger Eichelberger, SSE
 */
public class InspectIndex {
    
    /**
     * Sorts the contents of an iterable.
     * 
     * @param iterable the iterable
     * @return the sorted contents
     */
    private static List<String> sort(Iterable<String> iterable) {
        ArrayList<String> tmp = new ArrayList<>();
        for (String elt : iterable) {
            tmp.add(elt);
        }
        Collections.sort(tmp);
        return tmp;
    }

    /**
     * Lists the contents of all indexes in the given directory or the index in the given file.
     * 
     * @param args the first argument indicates the file/directory
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: directory/file");
        } else {
            File file = new File(args[0]);
            File[] files;
            if (file.isDirectory()) {
                files = file.listFiles(); 
            } else {
                files = new File[] {file};
            }
            if (null != files) {
                for (File f : files) {
                    if (f.getName().endsWith(".idx")) {
                        if (f.length() == 0) {
                            System.out.println("INDEX " + f + " (empty marker file for disabled indexes)");
                        } else {
                            try {
                                System.out.println("INDEX " + f);
                                LoaderIndex index = LoaderIndex.fromFile(f);
                                for (String cls : sort(index.getClasses())) {
                                    System.out.println("class " + cls + " -> " + index.getClassLocation(cls));
                                }
                                for (String res : sort(index.getResources())) {
                                    System.out.println("resource " + res + " -> " 
                                        + toString(index.getResourceLocations(res)));
                                }
                                System.out.println("Classpath: " + index.getFiles());
                                System.out.println("#classes " + index.getClassesCount() + " #resources " 
                                    + index.getResourcesCount() + " #locations " + index.getLocationsCount());
                                System.out.println();
                            } catch (IOException e) {
                                System.out.println("Cannot display " + f + ": " + e.getMessage());
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Turns multiple locations to a string output.
     * 
     * @param locations the location(s), may be <b>null</b>
     * @return the output
     */
    private static String toString(String[] locations) {
        String result = null;
        if (null != locations) {
            result = String.join(", ", locations);
        }
        return result;
    }

}
