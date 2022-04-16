/*
 * Copyright 2009-2016 University of Hildesheim, Software Systems Engineering
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.iip_ecosphere.platform.maven;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Some Maven utility functions. Taken over from Qualimaster.
 * 
 * @author Holger Eichelberger
 * @author Pastuschek
 */
public class MavenUtils {

    public static final String SNAPSHOT_SUFFIX = "-SNAPSHOT";
    
    /**
     * Some cleanup statistics.
     * 
     * @author Holger Eichelberger
     */
    public static class CleanupStatistics {
        private int fileCount;
        private long bytesCleared;
        
        /**
         * Called when a file was cleared.
         * 
         * @param length the number of bytes
         */
        private void clearedFile(long length) {
            fileCount++;
            bytesCleared += length;
        }

        /**
         * Returns the number of files cleared.
         * 
         * @return the number of files
         */
        public int getFileCount() {
            return fileCount;
        }
        
        /**
         * Returns the number of bytes cleared.
         * 
         * @return the number of bytes
         */
        public long getBytesCleared() {
            return bytesCleared;
        }
        
    }
    
    /**
     * Returns the path to the maven repository.
     * 
     * @return the path
     */
    public static String mavenRepository() {
        String mavenPath = System.getenv("M2_REPO");
        if (null == mavenPath || mavenPath.isEmpty()) {
            
            mavenPath = System.getProperty("user.home") + "/.m2/repository";
            System.out.println("No Systemvariable for Maven Repository found! Assuming location in: " + mavenPath);
        }
        return mavenPath;
    }

    /**
     * Cleans up snapshots in the actual repository.
     * 
     * @param generations the number of generations to keep (always at least 1)
     * @return cleanup statistics
     */
    public static CleanupStatistics cleanSnapshots(int generations) {
        CleanupStatistics statistics = new CleanupStatistics();
        generations = Math.max(1,  generations);
        File repo = new File(mavenRepository());
        if (repo.exists()) {
            cleanSnapshotsInRepositoryDir(repo, generations, statistics);
        }
        return statistics;
    }

    /**
     * Returns whether <code>file</code> is a snapshot directory.
     * 
     * @param file the file to consider
     * @return <code>true</code> for snapshot, <code>false</code> else
     */
    private static boolean isSnapshotDir(File file) {
        String name = file.getName();
        return file.isDirectory() && name.endsWith(SNAPSHOT_SUFFIX);
    }

    /**
     * Cleans the snapshots in <code>base</code>. 
     * 
     * @param base the directory to clear recursively
     * @param generations the number of generations to keep
     * @param statistics cleanup statistics (to be modified as a side effect)
     */
    private static void cleanSnapshotsInRepositoryDir(File base, int generations, CleanupStatistics statistics) {
        File[] files = base.listFiles();
        if (null != files) {
            for (File f : files) {
                if (f.isDirectory()) {
                    if (isSnapshotDir(f)) {
                        cleanSnapshots(f, generations, statistics);
                    } else {
                        cleanSnapshotsInRepositoryDir(f, generations, statistics);
                    }
                }
            }
        }
    }
    
    /**
     * Cleans the snapshots in <code>dir</code>. 
     * 
     * @param dir the directory to clear
     * @param generations the number of generations to keep
     * @param statistics cleanup statistics (to be modified as a side effect)
     */
    private static void cleanSnapshots(File dir, int generations, CleanupStatistics statistics) {
        Map<Integer, List<File>> gens = new HashMap<Integer, List<File>>(30);
        File[] files = dir.listFiles();
        for (File f : files) {
            String name = f.getName();
            Integer generation = getGeneration(name);
            if (null != generation) {
                List<File> members = gens.get(generation);
                if (null == members) {
                    members = new ArrayList<File>(10);
                    gens.put(generation, members);
                }
                members.add(f);
            }
        }
        List<Integer> gensSorted = new ArrayList<Integer>(gens.size());
        gensSorted.addAll(gens.keySet());
        Collections.sort(gensSorted);
        int maxCleanGenPos = gensSorted.size() - generations;
        for (int i = 0; i < maxCleanGenPos; i++) {
            List<File> fls = gens.get(gensSorted.get(i));
            for (File f : fls) {
                long len = f.length();
                System.out.println("Deleting " + f);
                if (f.delete()) {
                    statistics.clearedFile(len);
                }
            }
        }
    }

    /**
     * Returns the snapshot generation in <code>name</code>.
     * 
     * @param name the file name
     * @return the snapshot generation or <b>null</b> if none was identifier
     */
    private static final Integer getGeneration(String name) {
        Integer result = null;
        int snapStartPos = name.lastIndexOf('-');
        if (snapStartPos > 0) {
            int snapEndPos;
            if (snapStartPos > 1 && snapStartPos + 1 < name.length() 
                && Character.isAlphabetic(name.charAt(snapStartPos + 1))) { 
                snapEndPos = snapStartPos;
                snapStartPos = name.lastIndexOf('-', snapStartPos - 1); // skip artifact type
            } else {
                snapEndPos = name.indexOf('.', snapStartPos);
            }
            if (snapStartPos > 0 && snapStartPos + 1 < snapEndPos) {
                String tmp = name.substring(snapStartPos + 1, snapEndPos);
                try {
                    result = Integer.valueOf(tmp);
                } catch (NumberFormatException e) {
                    //System.out.println("Generation " + tmp + " not a number in " + name);
                }
            }
        }
        return result;
    }

    /**
     * Returns an amount of bytes in human readable format.
     * 
     * @param bytes the number of bytes
     * @param si use SI units (based on 1000 if <code>true</code>) or 1024 as base (<code>false</code>)
     * @return the human readable string representation
     */
    public static String humanReadableByteCount(long bytes, boolean si) {
        // http://stackoverflow.com/questions/3758606/how-to-convert-byte-size-into-human-readable-format-in-java
        String result;
        int unit = si ? 1000 : 1024;
        if (bytes < unit) {
            result = bytes + " B";
        } else {
            int exp = (int) (Math.log(bytes) / Math.log(unit));
            String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
            result = String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
        }
        return result;
    }
    
}
