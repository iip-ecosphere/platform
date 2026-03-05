
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

package de.iip_ecosphere.platform.maven;

import java.io.File;

import de.iip_ecosphere.platform.maven.MavenUtils.CleanupStatistics;

/**
 * Deletes indexes.
 * 
 * @author Holger Eichelberger, SSE
 */
public class CleanIndexes {
    
    /**
     * Deletes indexes.
     * 
     * @param args the first argument is the directory/file where to start cleaning
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: directory/file");
        } else {
            CleanupStatistics statistics = new CleanupStatistics();
            cleanIndexes(new File(args[0]), statistics);
            System.out.println("Cleaned up " + statistics.getFileCount() + " files with " 
                + MavenUtils.humanReadableByteCount(statistics.getBytesCleared(), false) + " in summary.");
        }
    }
    
    /**
     * Cleans indexes in {@code file} and subfolders.
     * 
     * @param file the file to look for indexes
     * @param statistics cleanup statistics
     */
    private static void cleanIndexes(File file, CleanupStatistics statistics) {
        if (file.isDirectory()) {
            File[] files = file.listFiles(); 
            if (null != files) {
                for (File f : files) {
                    cleanIndexes(f, statistics);
                }
            }
        } else if (file.getName().endsWith(".idx")) {
            System.out.println("deleting " + file);
            statistics.cleared(file);
            file.delete();
        }
    }

}
