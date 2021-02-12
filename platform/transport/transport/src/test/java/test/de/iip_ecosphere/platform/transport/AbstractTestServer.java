/**
 * ******************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.transport;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;

import de.iip_ecosphere.platform.support.Server;

/**
 * A basic abstract server for testing/experiments.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractTestServer implements Server {

    private static File configDir;

    /**
     * Creates a temporary folder without cleanup.
     * 
     * @param name the name of the temporary folder within the system/user temporary directory
     * @return the temporary folder (descriptor)
     */
    public static File createTmpFolder(String name) {
        return createTmpFolder(name, false);
    }

    /**
     * Creates a temporary folder.
     * 
     * @param name the name of the temporary folder within the system/user temporary directory
     * @param cleanup try to do an auto cleanup at JVM shutdown
     * @return the temporary folder (descriptor)
     */
    public static File createTmpFolder(String name, boolean cleanup) {
        String tmp = System.getProperty("java.io.tmpdir");
        File result = new File(tmp, name);
        FileUtils.deleteQuietly(result);
        result.mkdir();
        if (cleanup) {
            result.deleteOnExit();
        }
        return result;
    }

    /**
     * Defines the server configuration directory.
     * 
     * @param directory the directory (may be <b>null</b>, leads to the given default value in 
     *     {@link #getConfigDir(File)})
     * @return {@code Directory}
     */
    public static final File setConfigDir(File directory) {
        configDir = directory;
        return directory;
    }

    /**
     * Returns the server configuration directory.
     * 
     * @param deflt the default value if {@link #setConfigDir(File)} was not called before
     * @return the server configuration directory
     */
    public static File getConfigDir(String deflt) {
        return getConfigDir(new File(deflt));
    }
    
    /**
     * Returns the server configuration directory.
     * 
     * @param deflt the default value if {@link #setConfigDir(File)} was not called before
     * @return the server configuration directory
     */
    public static File getConfigDir(File deflt) {
        return null == configDir ? deflt : configDir;
    }

    /**
     * Extracts a server configuration from a resource from the actual class loader.
     * 
     * @param location the location within the resource
     * @param dfltConfigDir the default configuration dir for {@link #getConfigDir(String)}
     * @throws IOException if something I/O related fails
     */
    public static void extractConfiguration(String location, String dfltConfigDir) throws IOException {
        extractConfiguration(AbstractTestServer.class.getClassLoader(), location, dfltConfigDir);
    }

    /**
     * Extracts a server configuration from a resource.
     * 
     * @param loader the class loader holding the resource
     * @param location the location within the resource
     * @param dfltConfigDir the default configuration dir for {@link #getConfigDir(String)}
     * @throws IOException if something I/O related fails
     */
    public static void extractConfiguration(ClassLoader loader, String location, String dfltConfigDir) 
        throws IOException {
        File cfgDir = getConfigDir(dfltConfigDir);
        cfgDir.mkdirs();
        InputStream in = loader.getResourceAsStream(location);
        if (null != in) {
            extractZip(in, cfgDir.toPath());
        } else {
            throw new IOException("Location '" + location + "' cannot be found");
        }
    }

    /**
     * Returns whether this class is/we ware running from a JAR file.
     * 
     * @return {@code true} for execution from JAR, {@code false}
     */
    public static boolean runsFromJar() {
        String intlName = AbstractTestServer.class.getName().replace('.', '/');
        String classJar = AbstractTestServer.class.getResource("/" + intlName + ".class").toString();
        return classJar.startsWith("jar:");
    }

    // adapted from https://mkyong.com/java/how-to-decompress-files-from-a-zip-file/
    // may be worth moving down...
    
    /**
     * Extracts a ZIP file.
     * 
     * @param in the input stream containing the ZIP file
     * @param target the target path
     * @throws IOException if something I/O related fails
     */
    public static void extractZip(InputStream in, Path target) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(in)) {
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                boolean isDirectory = zipEntry.getName().endsWith(File.separator);
                Path newPath = zipSlipProtect(zipEntry, target);
                if (isDirectory) {
                    Files.createDirectories(newPath);
                } else {
                    if (newPath.getParent() != null) {
                        if (Files.notExists(newPath.getParent())) {
                            Files.createDirectories(newPath.getParent());
                        }
                    }
                    Files.copy(zis, newPath, StandardCopyOption.REPLACE_EXISTING);
                }

                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
        } catch (IOException e) {
            throw e;
        }
    }
    
    /**
     * Protects from ZIP slip attack.
     * 
     * @param zipEntry the ZIP entry
     * @param targetDir the target directory
     * @return the normalized/fixed path
     * @throws IOException if something I/O related fails
     */
    private static Path zipSlipProtect(ZipEntry zipEntry, Path targetDir)
        throws IOException {

        Path targetDirResolved = targetDir.resolve(zipEntry.getName());

        // make sure normalized file still has targetDir as its prefix
        // else throws exception
        Path normalizePath = targetDirResolved.normalize();
        if (!normalizePath.startsWith(targetDir)) {
            throw new IOException("Bad zip entry: " + zipEntry.getName());
        }

        return normalizePath;
    }
    
}
