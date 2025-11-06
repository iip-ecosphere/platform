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

package de.iip_ecosphere.platform.support;

import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import de.iip_ecosphere.platform.support.commons.Commons;

/**
 * Basic file functionality. 
 * 
 * @author Holger Eichelberger, SSE
 */
public class FileUtils {
    
    /**
     * Preventing external creation.
     */
    private FileUtils() {
    }

    /**
     * Creates a temporary folder in {@code java.io.tmpdir} without cleanup.
     * 
     * @param name the name of the temporary folder within the system/user temporary directory
     * @return the temporary folder (descriptor)
     */
    public static File createTmpFolder(String name) {
        return createTmpFolder(name, false);
    }

    /**
     * Creates a temporary folder in {@code java.io.tmpdir}.
     * 
     * @param name the name of the temporary folder within the system/user temporary directory
     * @param cleanup try to do an auto cleanup at JVM shutdown
     * @return the temporary folder (descriptor), may receive a suffix if there is already a 
     *     folder that cannot be deleted
     */
    public static File createTmpFolder(String name, boolean cleanup) {
        String tmp = System.getProperty("java.io.tmpdir");
        File result = new File(tmp, name);
        if (result.exists() && !deleteQuietly(result)) {
            result = new File(tmp, name + "_" + System.currentTimeMillis());
        }
        result.mkdir();
        if (cleanup) {
            result.deleteOnExit();
        }
        return result;
    }
    
    // checkstyle: stop exception type check 
    
    /**
     * Deletes a file or directory, not throwing an exception. If file is a directory, delete it and all 
     * sub-directories. [convenience]
     *
     * @param file file or directory to delete, may be {@code null}
     * @return {@code true} if {@code file} was deleted, otherwise {@code false}
     */
    public static boolean deleteQuietly(File file) { // required by test broker
        if (file == null) {
            return false;
        }
        try {
            if (file.isDirectory()) {
                cleanDirectory(file);
            }
        } catch (Exception ignored) {
            // ignore
        }

        try {
            return file.delete();
        } catch (Exception ignored) {
            return false;
        }
    }

    // checkstyle: resume exception type check 

    /**
     * Recursively deletes all files and subdirectories within a given directory.
     *
     * @param directory The directory to clean.
     * @throws IOException if deleting/cleaning fails
     */
    public static void cleanDirectory(File directory) throws IOException { // required by test broker
        try (Stream<Path> paths = Files.walk(directory.toPath())) {
            paths.sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
        }        
        /*if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        cleanDirectory(file);
                    }
                    if (!file.delete()) {
                        throw new IOException("Failed to delete " + file.getAbsolutePath());
                    }
                }
            }
        }*/
    }    
    
    /**
     * Deletes a file or directory. For a directory, delete it and all subdirectories.
     *
     * @param file file or directory to delete
     * @throws FileNotFoundException if the file was not found
     * @throws IOException           in case deletion is unsuccessful
     */
    public static void forceDelete(final File file) throws IOException {
        Commons.getInstance().forceDelete(file);
    }
    
    /**
     * Deletes a file or directory on JVM exit, not throwing an exception. If file is a directory, delete it and all 
     * sub-directories. [convenience]
     *
     * @param file file or directory to delete, may be {@code null}
     */
    public static void deleteOnExit(File file) {
        Commons.getInstance().deleteOnExit(file);
    }

    /**
     * Returns the path to the system temporary directory. [convenience]
     *
     * @return the path to the system temporary directory.
     */
    public static String getTempDirectoryPath() {
        return System.getProperty("java.io.tmpdir"); // required by test broker
    }

    /**
     * Returns a {@link File} representing the system temporary directory. [convenience]
     *
     * @return the system temporary directory.
     */
    public static File getTempDirectory() {
        return new File(getTempDirectoryPath()); // required by test broker
    }
    
    /**
     * Returns a {@link File} representing the user's home directory.
     *
     * @return the user's home directory.
     */
    public static File getUserDirectory() {
        return Commons.getInstance().getUserDirectory();
    }

    /**
     * Returns the path to the user's home directory.
     *
     * @return the path to the user's home directory.
     */
    public static String getUserDirectoryPath() {
        return Commons.getInstance().getUserDirectoryPath();
    }

    /**
     * Closes a closable quietly.
     * 
     * @param closable the closable, may be <b>null</b>
     */
    public static void closeQuietly(Closeable closable) {
        if (null != closable) {
            try {
                closable.close();
            } catch (IOException e ) {
                // do nothing, quietly
            }
        }
    }
    
    /**
     * Lists contained files.
     * 
     * @param file the file/folder to list
     * @param accept accept the file for further (nested) listing, not called for folders
     * @param handle handle an accepted file
     */
    public static void listFiles(File file, Predicate<File> accept, Consumer<File> handle) {
        if (accept.test(file)) {
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (null != files) {
                    for (File f : files) {
                        listFiles(f, accept, handle);
                    }
                }
            } else {
                handle.accept(file);
            }
        }
    }
    
    /**
     * Composes a path and returns the canonical/absolute path (single-time use).
     * 
     * @param path the path
     * @param name the name to be added (may be a path, may be empty or <b>null</b>)
     * @return the canonical/absolute path
     */
    public static String getResolvedPath(File path, String name) {
        File f;
        if (null != name && name.length() > 0) {
            f = new File(path, name);
        } else {
            f = path;
        }
        try {
            return f.getCanonicalPath();
        } catch (IOException e) {
            return f.getAbsolutePath();
        }
    }

    /**
     * Resolves a path and returns the canonical/absolute path.
     * 
     * @param path the path
     * @return the canonical/absolute path
     */
    public static File getResolvedFile(File path) {
        try {
            return path.getCanonicalFile();
        } catch (IOException e) {
            return path.getAbsoluteFile();
        }
    }
    
    /**
     * Turn {@code file} into a base64 encoded string.
     * 
     * @param file the file
     * @return the base64 encoded string
     * @throws IOException if {@code file} cannot be read
     */
    public static String fileToBase64(File file) throws IOException {
        return Commons.getInstance().fileToBase64(file);
    }

    /**
     * Turn base64 encoded {@code string} into a {@code file}.
     * 
     * @param string the base64 encoded string
     * @param file the file
     * @throws IOException if {@code file} cannot be written
     */
    public static void base64ToFile(String string, File file) throws IOException {
        Commons.getInstance().base64ToFile(string, file);
    }
    
    /**
     * Turns an arbitrary string into something that can be used as a file name. 
     * 
     * @param str the string to use
     * @return the file name
     */
    public static String sanitizeFileName(String str) {
        return str.replaceAll("[^a-zA-Z0-9-_\\.]", "_");
    }
    
    /**
     * Turns an arbitrary string into something that can be used as a file name. 
     * 
     * @param str the string to use
     * @param addTimestamp whether the current timestamp shall be added to {@code str}
     * @return the file name
     * @see #sanitizeFileName(String)
     */
    public static String sanitizeFileName(String str, boolean addTimestamp) {
        String tmp = addTimestamp ? str + "-" + System.currentTimeMillis() : str;
        return sanitizeFileName(tmp);
    }

    /**
     * Tries to find the file {@code name} in {@code folder} and its subfolders.
     * 
     * @param folder the folder to search
     * @param name the file name to find
     * @return the found file or <b>null</b> for none
     */
    public static File findFile(File folder, String name) {
        return Commons.getInstance().findFile(folder, name);
    }
    
    /**
     * Returns the system root folder.
     * 
     * @return the system root
     */
    public static File getSystemRoot() {
        // https://stackoverflow.com/questions/4362786/getting-the-default-root-directory-in-java
        File result;
        if (OsUtils.isWindows()) {
            String sysDrive = System.getenv("SystemDrive");
            if (null == sysDrive) {
                sysDrive = "C:";
            }
            result = new File(sysDrive);
        } else {
            result = new File("/");
        }
        return result;
    }

    /**
     * Writes a CharSequence with platform default charset to a file creating the file if it does not exist.
     *
     * @param file     the file to write
     * @param data     the content to write to the file
     * @throws IOException in case of an I/O error
     * @since 2.3
     */
    public static void write(final File file, final CharSequence data) throws IOException {
        write(file, data, Charset.defaultCharset());
    }

    /**
     * Writes a CharSequence to a file creating the file if it does not exist.
     *
     * @param file     the file to write
     * @param data     the content to write to the file
     * @param charset the requested charset, <b>null</b> means platform default
     * @throws IOException in case of an I/O error
     */
    public static void write(final File file, final CharSequence data, final Charset charset) throws IOException {
        Commons.getInstance().write(file, data, charset);
    }

    /**
     * Reads the contents of a file into a String with platform default charset. The {@code file} is always closed.
     *
     * @param file     the file to read
     * @return the file contents
     * @throws IOException if an I/O error occurs, including when the file does not exist, is a directory rather than a
     *         regular file, or for some other reason why the file cannot be opened for reading.
     */
    public static String readFileToString(final File file) throws IOException {
        return Commons.getInstance().readFileToString(file, Charset.defaultCharset());
    }

    /**
     * Reads the contents of a file into a String. The {@code file} is always closed.
     *
     * @param file     the file to read
     * @param charset the requested charset, <b>null</b> means platform default
     * @return the file contents
     * @throws IOException if an I/O error occurs, including when the file does not exist, is a directory rather than a
     *         regular file, or for some other reason why the file cannot be opened for reading.
     */
    public static String readFileToString(final File file, final Charset charset) throws IOException {
        return Commons.getInstance().readFileToString(file, charset);
    }

    /**
     * Deletes a directory recursively.
     *
     * @param directory directory to delete
     * @throws IOException              in case deletion is unsuccessful
     * @throws IllegalArgumentException if {@code directory} is not a directory
     */
    public static void deleteDirectory(final File directory) throws IOException {
        Commons.getInstance().deleteDirectory(directory);
    }

    /**
     * Copies a filtered directory to a new location preserving the file dates.
     * This method copies the contents of the specified source directory to within the specified destination directory.
     * The destination directory is created if it does not exist. If the destination directory does exist, then this
     * method merges the source with the destination, with the source taking precedence.
     *
     * @param srcDir an existing directory to copy
     * @param destDir the new directory
     * @param filter the filter to apply, null means copy all directories and files should be the same as the original
     * @throws IllegalArgumentException if {@code srcDir} exists but is not a directory, or
     *     the source and the destination directory are the same
     * @throws FileNotFoundException if the source does not exist
     * @throws IOException if an error occurs, the destination is not writable, or setting the last-modified time 
     *  didn't succeed
     */
    public static void copyDirectory(final File srcDir, final File destDir, final FileFilter filter)
        throws IOException {
        Commons.getInstance().copyDirectory(srcDir, destDir, filter);
    }    

    /**
     * Copies a filtered directory to a new location.
     * This method copies the contents of the specified source directory to within the specified destination directory.
     * The destination directory is created if it does not exist. If the destination directory does exist, then this
     * method merges the source with the destination, with the source taking precedence.
     *
     * @param srcDir an existing directory to copy
     * @param destDir the new directory
     * @param filter the filter to apply, null means copy all directories and files
     * @param preserveFileDate true if the file date of the copy should be the same as the original
     * @throws IllegalArgumentException if {@code srcDir} exists but is not a directory,
     *     the source and the destination directory are the same, or the destination is not writable
     * @throws FileNotFoundException if the source does not exist
     * @throws IOException if an error occurs or setting the last-modified time didn't succeed
     */
    public static void copyDirectory(final File srcDir, final File destDir, final FileFilter filter, 
        final boolean preserveFileDate) throws IOException {
        Commons.getInstance().copyDirectory(srcDir, destDir, filter, preserveFileDate);
    }
    
    /**
     * Copies a whole directory to a new location, preserving the file dates.
     * This method copies the specified directory and all its child directories and files to the specified destination.
     * The destination is the new location and name of the directory. That is, copying /home/bar to /tmp/bang
     * copies the contents of /home/bar into /tmp/bang. It does not create /tmp/bang/bar.
     * The destination directory is created if it does not exist. If the destination directory does exist, then this
     * method merges the source with the destination, with the source taking precedence.
     *
     * @param srcDir an existing directory to copy
     * @param destDir the new directory
     * @throws IllegalArgumentException if {@code srcDir} exists but is not a directory,
     *     the source and the destination directory are the same
     * @throws FileNotFoundException if the source does not exist.
     * @throws IOException if an error occurs, the destination is not writable, or setting the last-modified time 
     * didn't succeed
     */
    public static void copyDirectory(final File srcDir, final File destDir) throws IOException {
        Commons.getInstance().copyDirectory(srcDir, destDir);
    }
    
    /**
     * Copies a file to a new location preserving the file date.
     * This method copies the contents of the specified source file to the specified destination file. The directory
     * holding the destination file is created if it does not exist. If the destination file exists, then this method
     * overwrites it. A symbolic link is resolved before copying so the new file is not a link.
     *
     * @param srcFile an existing file to copy
     * @param destFile the new file
     * @throws IOException if source or destination is invalid, if an error occurs or setting the last-modified time 
     * didn't succeed, if the output file length is not the same as the input file length after the copy completes
     */
    public static void copyFile(final File srcFile, final File destFile) throws IOException {
        Commons.getInstance().copyFile(srcFile, destFile);
    }
    
    /**
     * Writes a String with platform default charset to a file creating the file if it does not exist.
     * The parent directories of the file will be created if they do not exist.
     *
     * @param file     the file to write
     * @param data     the content to write to the file
     * @throws IOException                          in case of an I/O error
     * @throws java.io.UnsupportedEncodingException if the encoding is not supported by the VM
     */
    public static void writeStringToFile(final File file, final String data) throws IOException {
        Commons.getInstance().writeStringToFile(file, data, Charset.defaultCharset());
    }    
    
    /**
     * Writes a String to a file creating the file if it does not exist.
     * The parent directories of the file will be created if they do not exist.
     *
     * @param file     the file to write
     * @param data     the content to write to the file
     * @param charset the charset to use, <b>null</b> means platform default
     * @throws IOException                          in case of an I/O error
     * @throws java.io.UnsupportedEncodingException if the encoding is not supported by the VM
     */
    public static void writeStringToFile(final File file, final String data, final Charset charset) throws IOException {
        Commons.getInstance().writeStringToFile(file, data, charset);
    }

    /**
     * Reads the contents of a file into a byte array.
     * The {@code file} is always closed.
     *
     * @param file the file to read
     * @return the file contents
     * @throws IOException if an I/O error occurs, including when the file does not exist, is a directory rather than a
     *         regular file, or for some other reason why the file cannot be opened for reading.
     */
    public static byte[] readFileToByteArray(final File file) throws IOException {
        return Commons.getInstance().readFileToByteArray(file);
    }
    
    /**
     * Writes a byte array to a file creating the file if it does not exist.
     * The parent directories of the file will be created if they do not exist.
     *
     * @param file the file to write to
     * @param data the content to write to the file
     * @throws IOException in case of an I/O error
     */
    public static void writeByteArrayToFile(final File file, final byte[] data) throws IOException {
        Commons.getInstance().writeByteArrayToFile(file, data);
    }
    
    /**
     * Writes a byte array to a file creating the file if it does not exist.
     *
     * @param file   the file to write to
     * @param data   the content to write to the file
     * @param append if {@code true}, then bytes will be added to the
     *               end of the file rather than overwriting
     * @throws IOException in case of an I/O error
     */
    public static void writeByteArrayToFile(final File file, final byte[] data, final boolean append) 
        throws IOException {
        Commons.getInstance().writeByteArrayToFile(file, data, append);
    }

    /**
     * Copies bytes from an {@link InputStream} {@code source} to a file
     * {@code destination}. The directories up to {@code destination}
     * will be created if they don't already exist. {@code destination}
     * will be overwritten if it already exists.
     *
     * @param source      the {@link InputStream} to copy bytes from, must not be, will be closed
     * @param destination the non-directory {@link File} to write bytes to (possibly overwriting)
     * @throws IOException if {@code destination} is a directory, if {@code destination} cannot be written, if 
     *     {@code destination} needs creating but can't be, if an IO error occurs during copying
     */
    public static void copyInputStreamToFile(final InputStream source, final File destination) throws IOException {
        Commons.getInstance().copyInputStreamToFile(source, destination);
    }

    /**
     * Tests whether the contents of two files are equal.
     * This method checks to see if the two files are different lengths or if they point to the same file, before
     * resorting to byte-by-byte comparison of the contents.
     *
     * @param file1 the first file
     * @param file2 the second file
     * @return {@code true} if the content of the files are equal or they both don't exist, {@code false} otherwise
     * @throws IllegalArgumentException when an input is not a file.
     * @throws IOException If an I/O error occurs.
     */
    public static boolean contentEquals(final File file1, final File file2) throws IOException {
        return Commons.getInstance().contentEquals(file1, file2);
    }

}
