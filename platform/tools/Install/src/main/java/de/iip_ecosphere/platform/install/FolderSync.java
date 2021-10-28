/**
 * ******************************************************************************
 * Copyright (c) {2021} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.install;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Set;

/**
 * Synchronizes one or multiple directories so that common files are copied to 
 * <code>targetFolder/{@value #COMMON_FOLDER_NAME}</code>, and individual files into 
 * <code>targetFolder/sourceFolder<i>X</i></code>. The source folder is considered flat, not recursive.
 * 
 * Allows to reduce file/container footprint.
 * 
 * @author Holger Eichelberger, SSE
 */
public class FolderSync {
    
    /**
     * Default target folder name.
     */
    public static final String COMMON_FOLDER_NAME = "common";
    
    /**
     * The main program.
     * 
     * @param args targetFolder, sourceFolder1, sourceFolder2, ...
     */
    public static void main(String... args) {
        if (args.length < 3) {
            System.out.println("Folder sync: targetFolder sourceFolder1 sourceFolder2 ...");
            System.out.println(" Copies equal files from source folder into targetFolder/" + COMMON_FOLDER_NAME);
            System.out.println(" and individual files to targetFolder/sourceFolder1Name ... Folders are considerd ");
            System.out.println(" flat, not recursive. targetFolder is not cleaned before copying.");
        } else {
            sync(args);
        }
    }
        
    /**
     * Does the synchronization.
     * 
     * @param args targetFolder, sourceFolder1, sourceFolder2, ...
     */
    private static void sync(String... args) {
        Set<String> common = new HashSet<String>();
        for (int a = 1; a < args.length; a++) {
            System.out.println("Analyzing " + args[a]);
            File source = new File(args[a]);
            File[] files = source.listFiles();
            Set<String> diff = new HashSet<String>(common);
            if (null != files && files.length > 0) {
                for (File f : files) {
                    String rel = relativize(source, f);
                    if (1 == a) {
                        common.add(rel);
                    } else {
                        diff.remove(rel);
                    }
                }
                common.removeAll(diff);
            } else {
                System.out.println("No files in " + args[a] + " Is this intended?");
            }
        }
        
        File target = new File(args[0]);
        File commonInTarget = new File(target, COMMON_FOLDER_NAME);
        commonInTarget.mkdirs();
        for (int a = 1; a < args.length; a++) {
            File source = new File(args[a]);
            File[] files = source.listFiles();
            File sourceInTarget = new File(target, source.getName());
            sourceInTarget.mkdirs();
            if (null != files && files.length > 0) {
                for (File f : files) {
                    String rel = relativize(source, f);
                    File copyTarget;
                    if (common.contains(rel)) {
                        copyTarget = commonInTarget;
                    } else {
                        copyTarget = sourceInTarget;
                    }
                    try {
                        Files.copy(f.toPath(), new File(copyTarget, rel).toPath(), StandardCopyOption.REPLACE_EXISTING);
                        System.out.println("Copied " + rel + " to " + copyTarget.getName());
                    } catch (IOException e) {
                        System.out.println("Copying " + f + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }        
    }

    /**
     * Relativizes a file within a base folder.
     * 
     * @param base the base folder
     * @param file the file name
     * @return the relative path/name
     */
    private static final String relativize(File base, File file) {
        // we are just considering flat folders
        return file.getName(); // base.toURI().relativize(other.toURI()).getPath()
    }

}
