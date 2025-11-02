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

package de.iip_ecosphere.platform.support.commons;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.ExecutionException;

import de.iip_ecosphere.platform.support.plugins.PluginManager;

/**
 * Commons utility interface.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class Commons {

    private static Commons instance; 

    static {
        instance = PluginManager.getPluginInstance(Commons.class, CommonsProviderDescriptor.class);
    }

    /**
     * Returns the Commons instance.
     * 
     * @return the instance
     */
    public static Commons getInstance() {
        return instance;
    }
    
    /**
     * Manually sets the instance. Shall not be needed, but may be required in some tests.
     * 
     * @param commons the Commons instance
     */
    public static void setInstance(Commons commons) {
        if (null != commons) {
            instance = commons;
        }
    }
    
    // collections

    /**
     * Reverses the order of the given array. There is no special handling for multi-dimensional arrays. This method 
     * does nothing for a <b>null</b> input array.
     *
     * @param array  the array to reverse, may be <b>null</b>
     */
    public abstract void reverse(final Object[] array);

    // objects, beans

    /**
     * Copies all fields from the {@code source} to the {@code target} object.
     * 
     * @param source the source object (may be <b>null</b>, ignored then)
     * @param target the target object (may be <b>null</b>, ignored then)
     * @throws ExecutionException if copying fails
     */
    public abstract void copyFields(Object source, Object target) throws ExecutionException;

    // string

    /**
     * Escapes the characters in a {@code String} using Java String rules.
     * Deals correctly with quotes and control-chars (tab, backslash, cr, ff, etc.)
     *
     * @param input  String to escape values in, may be <b>null</b>
     * @return String with escaped values, <b>null</b> if <b>null</b> string input
     */
    public abstract String escapeJava(final String input);

    /**
     * Unescapes any Java literals found in the {@code String}.
     *
     * @param input  the {@code String} to unescape, may be <b>null</b>
     * @return a new unescaped {@code String}, <b>null</b> if <b>null</b> string input
     */
    public abstract String unescapeJava(final String input);
    
    /**
     * Escapes the characters in a {@code String} using Json String rules.
     * Escapes any values it finds into their Json String form.
     * Deals correctly with quotes and control-chars (tab, backslash, cr, ff, etc.)
     *
     * @param input  String to escape values in, may be <b>null</b>
     * @return String with escaped values, <b>null</b> if null string input
     */
    public abstract String escapeJson(final String input);

    /**
     * Unescapes any Json literals found in the {@code String}.
     *
     * @param input  the {@code String} to unescape, may be <b>null</b>
     * @return A new unescaped {@code String}, <b>null</b> if null string input
     */
    public abstract String unescapeJson(final String input);
    
    /**
     * Returns either the passed in CharSequence, or if the CharSequence is
     * whitespace, empty ({@code ""}) or <b>null</b>, the value of {@code defaultStr}.
     *
     * @param <T> the specific kind of CharSequence
     * @param str the CharSequence to check, may be <b>null</b>
     * @param defaultStr  the default CharSequence to return
     *  if the input is whitespace, empty ({@code ""}) or <b>null</b>
     * @return the passed in CharSequence, or the default
     */
    public abstract <T extends CharSequence> T defaultIfBlank(final T str, final T defaultStr);

    /**
     * Returns either the passed in CharSequence, or if the CharSequence is
     * empty or <b>null</b>, the value of {@code defaultStr}.
     *
     * @param <T> the specific kind of CharSequence
     * @param str  the CharSequence to check, may be null
     * @param defaultStr  the default CharSequence to return
     *  if the input is empty ({@code ""}) or <b>null</b>
     * @return the passed in CharSequence, or the default
     */
    public abstract <T extends CharSequence> T defaultIfEmpty(final T str, final T defaultStr);

    /**
     * Checks if a CharSequence is empty ({@code ""}), <b>null</b> or whitespace only.
     *
     * @param cs  the CharSequence to check, may be <b>null</b>
     * @return {@code true} if the CharSequence is <b>null</b>, empty or whitespace only
     */
    public abstract boolean isBlank(final CharSequence cs);

    /**
     * Checks if a CharSequence is not empty (""), not null and not whitespace only.
     *
     * @param cs  the CharSequence to check, may be null
     * @return {@code true} if the CharSequence is
     *  not empty and not null and not whitespace only
     */
    public abstract boolean isNotBlank(final CharSequence cs);
    
    /**
     * Replaces a String with another String inside a larger String, once.
     *
     * @param text  text to search and replace in, may be null
     * @param searchString  the String to search for, may be null
     * @param replacement  the String to replace with, may be null
     * @return the text with any replacements processed,
     *  <b>null</b> if null String input
     */
    public abstract String replaceOnce(final String text, final String searchString, final String replacement);

    /**
     * Checks if a CharSequence is empty ({@code ""}) or <b>null</b>.
     *
     * @param cs  the CharSequence to check, may be <b>null</b>
     * @return {@code true} if the CharSequence is empty or <b>null</b>
     */
    public abstract boolean isEmpty(final CharSequence cs);
    
    /**
     * Turns an object to an readable string, usually using reflection. Uses some default
     * style.
     * 
     * @param obj the object
     * @return the string representation
     */
    public abstract String toString(Object obj);

    /**
     * Turns an object to an readable string, usually using reflection. Uses oktoflow short style.
     * 
     * @param obj the object
     * @return the string representation
     */
    public abstract String toStringShortStyle(Object obj);
    
    /**
     * Removes a substring only if it is at the beginning of a source string,
     * otherwise returns the source string.

     * @param str  the source String to search, may be null
     * @param remove  the String to search for and remove, may be null
     * @return the substring with the string removed if found,
     *  <b>null</b> if <b>null</b> String input
     */
    public abstract String removeStart(String str, String remove);
    
    /**
     * Removes a substring only if it is at the end of a source string,
     * otherwise returns the source string.
     *
     * @param str  the source String to search, may be null
     * @param remove  the String to search for and remove, may be null
     * @return the substring with the string removed if found,
     *  <b>null</b> if <b>null</b> String input
     */
    public abstract String removeEnd(String str, String remove);
    
    // OS
    
    /**
     * Returns the {@code user.home} System Property. User's home directory.
     */
    public abstract String getUserHome();

    /**
     * Returns whether we are running on windows.
     * 
     * @return {@code true} for windows, {@code false} else
     */
    public abstract boolean isWindows();

    /**
     * Returns whether we are running on Linux.
     * 
     * @return {@code true} for Linux, {@code false} else
     */
    public abstract boolean isLinux();

    /**
     * Returns whether we are running on Unix.
     * 
     * @return {@code true} for Unix, {@code false} else
     */
    public abstract boolean isUnix();

    /**
     * Returns whether we are running on Mac.
     * 
     * @return {@code true} for Mac, {@code false} else
     */
    public abstract boolean isMac();
    
    /**
     * Returns whether we are running on Java 1.8.
     * 
     * @return {@code true} for Java 1.8, {@code false} else
     */
    public abstract boolean isJava1_8();

    /**
     * Returns the operating system name.
     * 
     * @return the operating system name
     */
    public String getOsName() {
        // preliminary, may delegate to implementation
        return System.getProperty("os.name", "");
    }
    
    /**
     * Returns the operating system architecture.
     * 
     * @return the operating system architecture.
     */
    public String getOsArch() {
        // preliminary, may delegate to implementation
        return System.getProperty("os.arch", "");
    }
    
    /**
     * Gets the Java home directory as a {@code File}.
     *
     * @return a directory
     * @throws SecurityException if a security manager exists and its {@code checkPropertyAccess} method doesn't allow
     * access to the specified system property.
     */
    public abstract File getJavaHome();
    
    /**
     * The Java Runtime Environment specification version.
     */
    public abstract String getJavaSpecificationVersion();
    
    // Net
    
    /**
     * Returns whether the given string is an IPv4 address.
     * 
     * @param address the address to validate
     * @return {@code true} for a valid address, {@code false} else
     */
    public abstract boolean isIpV4Addess(String address);

    // IO
    
    /**
     * Gets the contents of an {@link InputStream} as a String
     * using the specified character encoding.
     *
     * @param in the {@link InputStream} to read
     * @param charset the charset to use, <b>null</b> means platform default
     * @return the requested String
     * @throws IOException if an I/O error occurs
     */
    public abstract String toString(InputStream in, Charset charset) throws IOException;

    /**
     * Gets the contents of an {@link InputStream} as a list of Strings,
     * one entry per line, using the specified character encoding.
     *
     * @param in the {@link InputStream} to read
     * @param charset the charset to use, <b>null</b> means platform default
     * @return the list of Strings
     * @throws IOException if an I/O error occurs
     */
    public abstract List<String> readLines(InputStream in, Charset charset) throws IOException;

    // File
    
    /**
     * Deletes a file or directory, not throwing an exception. If file is a directory, delete it and all 
     * sub-directories. [convenience]
     *
     * @param file file or directory to delete, may be {@code null}
     * @return {@code true} if {@code file} was deleted, otherwise {@code false}
     */
    public abstract boolean deleteQuietly(File file);
    
    /**
     * Deletes a file or directory. For a directory, delete it and all subdirectories.
     *
     * @param file file or directory to delete
     * @throws FileNotFoundException if the file was not found
     * @throws IOException           in case deletion is unsuccessful
     */
    public abstract void forceDelete(final File file) throws IOException;

    /**
     * Deletes a file or directory on JVM exit, not throwing an exception. If file is a directory, delete it and all 
     * sub-directories. [convenience]
     *
     * @param file file or directory to delete, may be {@code null}
     */
    public abstract void deleteOnExit(File file);

    /**
     * Returns the path to the system temporary directory. [convenience]
     *
     * @return the path to the system temporary directory.
     */
    public abstract String getTempDirectoryPath();

    /**
     * Returns a {@link File} representing the system temporary directory. [convenience]
     *
     * @return the system temporary directory.
     */
    public abstract File getTempDirectory();
    
    /**
     * Returns a {@link File} representing the user's home directory.
     *
     * @return the user's home directory.
     */
    public abstract File getUserDirectory();

    /**
     * Returns the path to the user's home directory.
     *
     * @return the path to the user's home directory.
     */
    public abstract String getUserDirectoryPath();

    /**
     * Turn {@code file} into a base64 encoded string.
     * 
     * @param file the file
     * @return the base64 encoded string
     * @throws IOException if {@code file} cannot be read
     */
    public abstract String fileToBase64(File file) throws IOException;

    /**
     * Turn base64 encoded {@code string} into a {@code file}.
     * 
     * @param string the base64 encoded string
     * @param file the file
     * @throws IOException if {@code file} cannot be written
     */
    public abstract void base64ToFile(String string, File file) throws IOException;

    /**
     * Tries to find the file {@code name} in {@code folder} and its subfolders.
     * 
     * @param folder the folder to search
     * @param name the file name to find
     * @return the found file or <b>null</b> for none
     */
    public abstract File findFile(File folder, String name);
    
    /**
     * Writes a CharSequence to a file creating the file if it does not exist.
     *
     * @param file     the file to write
     * @param data     the content to write to the file
     * @param charset the requested charset, <b>null</b> means platform default
     * @throws IOException in case of an I/O error
     */
    public abstract void write(final File file, final CharSequence data, final Charset charset) throws IOException;

    /**
     * Reads the contents of a file into a String with platform default charset. The {@code file} is always closed.
     *
     * @param file     the file to read
     * @return the file contents
     * @throws IOException if an I/O error occurs, including when the file does not exist, is a directory rather than a
     *         regular file, or for some other reason why the file cannot be opened for reading.
     */
    public abstract String readFileToString(final File file) throws IOException;

    /**
     * Reads the contents of a file into a String. The {@code file} is always closed.
     *
     * @param file     the file to read
     * @param charset the requested charset, <b>null</b> means platform default
     * @return the file contents
     * @throws IOException if an I/O error occurs, including when the file does not exist, is a directory rather than a
     *         regular file, or for some other reason why the file cannot be opened for reading.
     */
    public abstract String readFileToString(final File file, final Charset charset) throws IOException;

    /**
     * Cleans a directory recursively.
     *
     * @param directory directory to delete
     * @throws IOException              in case deletion is unsuccessful
     * @throws IllegalArgumentException if {@code directory} is not a directory
     */
    public abstract void cleanDirectory(final File directory) throws IOException;
    
    /**
     * Deletes a directory recursively.
     *
     * @param directory directory to delete
     * @throws IOException              in case deletion is unsuccessful
     * @throws IllegalArgumentException if {@code directory} is not a directory
     */
    public abstract void deleteDirectory(final File directory) throws IOException;

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
    public abstract void copyDirectory(final File srcDir, final File destDir, final FileFilter filter)
        throws IOException;

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
    public abstract void copyDirectory(final File srcDir, final File destDir, final FileFilter filter, 
        final boolean preserveFileDate) throws IOException;
    
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
    public abstract void copyDirectory(final File srcDir, final File destDir) throws IOException;
    
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
    public abstract void copyFile(final File srcFile, final File destFile) throws IOException;
    
    /**
     * Writes a String with platform default charset to a file creating the file if it does not exist.
     * The parent directories of the file will be created if they do not exist.
     *
     * @param file     the file to write
     * @param data     the content to write to the file
     * @throws IOException                          in case of an I/O error
     * @throws java.io.UnsupportedEncodingException if the encoding is not supported by the VM
     */
    public abstract void writeStringToFile(final File file, final String data) throws IOException;
    
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
    public abstract void writeStringToFile(final File file, final String data, final Charset charset) 
        throws IOException;

    /**
     * Reads the contents of a file into a byte array.
     * The {@code file} is always closed.
     *
     * @param file the file to read
     * @return the file contents
     * @throws IOException if an I/O error occurs, including when the file does not exist, is a directory rather than a
     *         regular file, or for some other reason why the file cannot be opened for reading.
     */
    public abstract byte[] readFileToByteArray(final File file) throws IOException;
    
    /**
     * Writes a byte array to a file creating the file if it does not exist.
     * The parent directories of the file will be created if they do not exist.
     *
     * @param file the file to write to
     * @param data the content to write to the file
     * @throws IOException in case of an I/O error
     */
    public abstract void writeByteArrayToFile(final File file, final byte[] data) throws IOException;
    
    /**
     * Writes a byte array to a file creating the file if it does not exist.
     *
     * @param file   the file to write to
     * @param data   the content to write to the file
     * @param append if {@code true}, then bytes will be added to the
     *               end of the file rather than overwriting
     * @throws IOException in case of an I/O error
     */
    public abstract void writeByteArrayToFile(final File file, final byte[] data, final boolean append) 
        throws IOException;

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
    public abstract void copyInputStreamToFile(final InputStream source, final File destination) throws IOException;

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
    public abstract boolean contentEquals(final File file1, final File file2) throws IOException;
    
    // date/time
    
    /**
     * Registers plugin-supplied date-time converters that shall be registered with the platform.
     */
    public abstract void registerDateConverters();
    
}
