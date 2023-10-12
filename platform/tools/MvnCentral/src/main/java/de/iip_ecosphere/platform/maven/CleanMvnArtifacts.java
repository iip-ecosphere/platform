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

package de.iip_ecosphere.platform.maven;

import java.io.File;
import java.io.IOException;

import org.apache.maven.shared.utils.io.FileUtils;

import de.iip_ecosphere.platform.support.iip_aas.Version;
import de.iip_ecosphere.platform.support.setup.CmdLine;

/**
 * Cleans up maven artifacts according to their version/snapshot in a working directory or a repository.
 * 
 * @author Holger Eichelberger, SSE
 */
public class CleanMvnArtifacts {
    
    private static final String SNAPSHOT_SUFFIX = "-SNAPSHOT";
    private static Version minVer;
    private static Version maxVer;
    private static boolean includeReleases;
    private static boolean onRepo;
    private static boolean simulate;
    private static long cleanupCount = 0;
    private static long cleanupSize = 0;

    /**
     * Cleans up maven artifacts according to their version/snapshot in a working directory or a repository.
     * 
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Cleans target folders of local builds <path> --releases=true --repo=true "
                + "--minVer=<ver> --maxVer=<ver>");
            System.out.println(" --releases: shall also release files be cleaned up, else only snapshots");
            System.out.println(" --repo: do we operate on a Maven repository or a workspace");
            System.out.println(" --minVer: minimum version to consider, do not delete smaller; optional");
            System.out.println(" --maxVer: maxiumuzm version to consider, do not delete larger; optional");
        } else {
            File base = new File(args[0]);
            includeReleases = CmdLine.getBooleanArg(args, "releases", false);
            onRepo = CmdLine.getBooleanArg(args, "repo", false);
            minVer = toVersion(CmdLine.getArg(args, "minVer", null), false);
            maxVer = toVersion(CmdLine.getArg(args, "maxVer", null), false);
            if (minVer == null && maxVer == null) {
                System.err.println("Either minVer or maxVer must be given. Else, we would delete everything.");
            }
            simulate = CmdLine.getBooleanArg(args, "simulate", false);
            for (int i = 0; !simulate && i < args.length; i++) {
                simulate = args[i].equals("--simulate"); // safe side...
            }
            if (onRepo) {
                cleanRepo(base);
            } else {
                cleanTargets(base);
            }
            String intro = simulate ? "Would clean up " : "Cleaned up ";
            System.out.println(intro + cleanupCount + " files with " 
                + MavenUtils.humanReadableByteCount(cleanupSize, false) + " in summary.");
        }
    }
    
    /**
     * Turns {@code text} to a version.
     * 
     * @param text the text, may be <b>null</b>
     * @param ignoreException shall failures be ignored or shall the message be emitted and the JVM be terminated
     * @return the version or <b>null</b>
     */
    private static Version toVersion(String text, boolean ignoreException) {
        Version result = null;
        if (null != text) {
            try {
                result = new Version(text);
            } catch (IllegalArgumentException e) {
                if (!ignoreException) {
                    System.err.println("Version '" + text + "' is not a version specifier. Stopping.");
                    System.exit(0);
                }
            }
        }
        return result;
    }
    
    /**
     * Recursively cleans up a Maven repository starting at {@code file}.
     * 
     * @param file the file/folder to clean up
     */
    private static void cleanRepo(File file) {
        File[] files = file.listFiles();
        if (null != files) {
            for (File f : files) {
                if (f.isDirectory()) {
                    String name = f.getName();
                    if (name.endsWith(SNAPSHOT_SUFFIX)) {
                        name = name.substring(0, name.length() - SNAPSHOT_SUFFIX.length());
                    }
                    if (Version.isVersion(name)) {
                        if (isInVersionRange(toVersion(name, true))) {
                            delete(f);
                        }
                    } else {
                        cleanRepo(f);
                    }
                }
            }
        }
    }

    /**
     * Recursively cleans up artifacts in target folders in a workspace starting at {@code file}.
     * 
     * @param file the file/folder to clean up
     */
    private static void cleanTargets(File file) {
        File[] files = file.listFiles();
        if (null != files) {
            File target = null;
            boolean inMavenProject = false;
            for (File f : files) {
                String name = f.getName();
                inMavenProject |= name.equals("pom.xml");
                if (f.isDirectory()) {
                    if (name.equals("target")) {
                        target = f;
                    } else {
                        cleanTargets(f);
                    }
                }
            }
            if (inMavenProject && null != target) {
                cleanTarget(target);
            }
        }
    }

    /**
     * Cleans up the given workspace {@code targetFolder}.
     * 
     * @param targetFolder the target folder
     */
    private static void cleanTarget(File targetFolder) {
        File[] files = targetFolder.listFiles();
        if (null != files) {
            for (File f : files) {
                if (f.isFile()) {
                    boolean isSnapshot = false;
                    Version version = null;
                    String name = f.getName();
                    int pos = name.lastIndexOf('.');
                    if (pos > 0) { // cut name, start with version
                        name = name.substring(0, pos);
                    }
                    pos = name.indexOf('-');
                    if (pos > 0) { // cut name, start with version
                        name = name.substring(pos + 1);
                    }
                    pos = name.indexOf('-');
                    String versionTmp;
                    if (pos > 0) {
                        versionTmp = name.substring(0, pos);
                        name = name.substring(pos);
                    } else { // just version, no qualifier, no snapshot
                        versionTmp = name;
                        name = "";
                    }
                    version = toVersion(versionTmp, true);
                    if (name.startsWith(SNAPSHOT_SUFFIX)) {
                        isSnapshot = true;
                        name = name.substring(SNAPSHOT_SUFFIX.length());
                    } // name here is qualifier if at all
                    if ((isSnapshot || (includeReleases && !isSnapshot)) && isInVersionRange(version)) {
                        delete(f);
                    }
                }
            }
        }
    }
    
    /**
     * Returns whether the given {@code version} is in the version range of {@link #minVer} and {@link #maxVer}.
     * 
     * @param version the version to check, may be <b>null</b> (will never be in any range)
     * @return {@code true} if {@code version} is in the version range, {@code false} else
     */
    private static boolean isInVersionRange(Version version) {
        boolean result;
        if (version != null) {
            boolean minVerOk = (null == minVer || minVer.compareTo(version) <= 0);
            boolean maxVerOk = (null == maxVer || version.compareTo(maxVer) <= 0);
            result = minVerOk && maxVerOk;
        } else {
            result = false;
        }
        return result;
    }
    
    /**
     * Deletes a file/folder and counts the deleted number of files/bytes.
     * 
     * @param file the file to delete
     */
    private static void delete(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f: files) {
                delete(f);
            }
            if (simulate) {
                System.out.println("Would delete " + file);
            } else {
                try {
                    FileUtils.forceDelete(file);
                } catch (IOException e) {
                    System.err.println("Cannot delete " + file + ":" + e.getMessage());
                }
            }
        } else {
            long size = file.length();
            if (simulate) {
                System.out.println("Would delete " + file);
                cleanupCount++;
                cleanupSize += size;
            } else {
                try {
                    FileUtils.forceDelete(file);
                    System.out.println("Deleted " + file);
                    cleanupCount++;
                    cleanupSize += size;
                } catch (IOException e) {
                    System.err.println("Cannot delete " + file + ":" + e.getMessage());
                }
            }
        }
    }
    
}
