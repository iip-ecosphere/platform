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

package de.iip_ecosphere.platform.tools.maven.python;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.maven.project.MavenProject;

/**
 * A simple MD5-hash based file change detector.
 * 
 * @author Holger Eichelberger, SSE
 */
public class FileChangeDetector {
    
    public static final String FILE_EXTENSION = "fpf"; 
    
    private File md5File;
    private boolean useHash = true;
    private Logger logger;
    private String task;
    private transient Map<String, String> md5Hashes = new HashMap<>();

    /**
     * Creates a file change detector instance.
     * 
     * @param md5File the file where the hashes are/shall be stored
     * @param logger the logger instance
     * @param task a task description for logging (may be <b>null</b> or empty for no logging)
     */
    public FileChangeDetector(File md5File, Logger logger, String task) {
        this.md5File = md5File;
        this.logger = logger;
        this.task = task;
    }
    
    /**
     * Enable/disable the detector.
     * 
     * @param useHash whether the detector/hashing shall be used
     * @return <b>this</b> (builder style)
     */
    public FileChangeDetector useHash(boolean useHash) {
        this.useHash = useHash;
        return this;
    }
    
    /**
     * Reads the MD5 hash file.
     */
    @SuppressWarnings("unchecked")
    public void readHashFile() {
        md5Hashes.clear();
        if (md5File.exists() && useHash) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(md5File))) {
                md5Hashes = (Map<String, String>) ois.readObject();
                logger.info("Using hash file " + md5File);
            } catch (IOException | ClassNotFoundException | ClassCastException e) {
                logger.warn("Cannot read existing fingerprint file '" + md5File.getName() + "': " + e.getMessage());
            }
        }
    }

    /**
     * Writes {@code md5Hashes} to the hash file.
     */
    public void writeHashFile() {
        if (useHash) {
            md5File.getParentFile().mkdirs();
            try (ObjectOutputStream ois = new ObjectOutputStream(new FileOutputStream(md5File))) {
                ois.writeObject(md5Hashes);
                logger.info("Wrote hash file " + md5File);
            } catch (IOException | ClassCastException e) {
                logger.warn("Write fingerprint file '" + md5File + "': " + e.getMessage());
            }
        }
    }

    /**
     * Removes {@code file} from the hashes.
     * 
     * @param file the file to remove
     */
    public void remove(File file) {
        md5Hashes.remove(getHashFilePath(file));
    }

    /**
     * Returns the file path to be used for MD5 hashing of {@code file}.
     * 
     * @param file the file
     * @return the file path
     */
    private String getHashFilePath(File file) {
        String path;
        try {
            path = file.getCanonicalPath();
        } catch (IOException e) {
            path = file.getAbsolutePath();
        }
        return path;
    }
    
    /**
     * Checks {@code files} for existing/known hashes.
     * 
     * @param files the files
     * @return a subset of {@code files} to process
     */
    public List<File> checkHashes(List<File> files) {
        List<File> result = new ArrayList<>();
        for (File f: files) {
            if (f.exists()) {
                String path = getHashFilePath(f);
                String knownMd5 = md5Hashes.get(path);
                String md5 = null;
                try (InputStream is = Files.newInputStream(f.toPath())) {
                    md5 = DigestUtils.md5Hex(is);
                } catch (IOException e) {
                }
                if (md5 != null) {
                    md5Hashes.put(path, md5);
                }
                if (knownMd5 != null) {
                    if (!knownMd5.equals(md5)) {
                        result.add(f);
                    } else {
                        if (task != null && task.length() > 0) {
                            logger.info("Skipping " + task + " for " + f + " as unchanged.");
                        }
                    }
                } else {
                    result.add(f);
                }
            }
        }
        return result;
    }

    /**
     * Returns the name of a hash file with {@code #FILE_EXTENSION} in the maven target folder.
     * 
     * @param project the project determining the maven target folder
     * @param hashFileName the name of the hash file
     * @return the hash file
     */
    public static File getHashFileInTarget(MavenProject project, String hashFileName) {
        return new File(project.getBuild().getDirectory(), hashFileName + "." + FILE_EXTENSION);
    }

}
