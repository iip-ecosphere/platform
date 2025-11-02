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

package test.de.iip_ecosphere.platform.support.commons;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.joda.time.DateTime;

import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.TimeUtils.AbstractDateConverter;
import de.iip_ecosphere.platform.support.commons.Commons;

/**
 * Preliminary test implementation for commons.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TestCommons extends Commons {

    /**
     * Short prefix style with limited string output.
     */
    static final ToStringStyle SHORT_STRING_STYLE = new ShortStringToStringStyle(); 

    
    // collections

    @Override
    public void reverse(final Object[] array) {
        ArrayUtils.reverse(array);
    }

    // objects, beans

    @Override
    public void copyFields(Object source, Object target) throws ExecutionException {
        if (null != target && null != source) {
            try {
                BeanUtils.copyProperties(target, source);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new ExecutionException(e);
            }
        }
    }
    
    // Strings

    @Override
    public final String escapeJava(final String input) {
        return StringEscapeUtils.escapeJava(input);
    }

    @Override
    public final String unescapeJava(final String input) {
        return StringEscapeUtils.unescapeJava(input);
    }
    
    @Override
    public String escapeJson(final String input) {
        return StringEscapeUtils.escapeJson(input);
    }

    @Override
    public String unescapeJson(final String input) {
        return StringEscapeUtils.unescapeJson(input);
    }
    
    @Override
    public <T extends CharSequence> T defaultIfBlank(final T str, final T defaultStr) {
        return org.apache.commons.lang3.StringUtils.defaultIfBlank(str, defaultStr);
    }

    @Override
    public <T extends CharSequence> T defaultIfEmpty(final T str, final T defaultStr) {
        return org.apache.commons.lang3.StringUtils.defaultIfEmpty(str, defaultStr);
    }

    @Override
    public boolean isBlank(final CharSequence cs) {
        return org.apache.commons.lang3.StringUtils.isBlank(cs);
    }

    @Override
    public boolean isNotBlank(final CharSequence cs) {
        return org.apache.commons.lang3.StringUtils.isNotBlank(cs);
    }
    
    @Override
    public String replaceOnce(final String text, final String searchString, final String replacement) {
        return org.apache.commons.lang3.StringUtils.replaceOnce(text, searchString, replacement);
    }

    @Override
    public boolean isEmpty(final CharSequence cs) {
        return org.apache.commons.lang3.StringUtils.isEmpty(cs);
    }
    
    @Override
    public String toString(Object obj) {
        return ReflectionToStringBuilder.toString(obj);
    }
    
    /**
     * Short prefix style with limited string output.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static final class ShortStringToStringStyle extends ToStringStyle {
        
        private static final long serialVersionUID = 1L;

        /**
         * <p>Constructor.</p>
         *
         * <p>Use the static constant rather than instantiating.</p>
         */
        ShortStringToStringStyle() {
            super();
            this.setUseShortClassName(true);
            this.setUseIdentityHashCode(false);
        }
        
        @Override
        public void append(StringBuffer buffer, String fieldName, Object value, Boolean fullDetail) {
            if (value instanceof String) { // in particular base64 images
                String sVal = (String) value;
                if (sVal.length() > 20) {
                    value = sVal.substring(0, 20) + "...";
                }
            }
            super.append(buffer, fieldName, value, fullDetail);
        }

        /**
         * <p>Ensure singleton after serialization.</p>
         * @return the singleton
         */
        private Object readResolve() {
            return SHORT_STRING_STYLE;
        }
        
    }

    @Override
    public String toStringShortStyle(Object obj) {
        return ReflectionToStringBuilder.toString(obj, SHORT_STRING_STYLE);
    }
    
    @Override
    public String removeStart(String str, String remove) {
        return org.apache.commons.lang3.StringUtils.removeStart(str, remove);
    }
    
    @Override
    public String removeEnd(String str, String remove) {
        return org.apache.commons.lang3.StringUtils.removeEnd(str, remove);
    }
    
    // OS
    
    @Override
    public String getUserHome() {
        return SystemUtils.USER_HOME;
    }

    @Override
    public boolean isWindows() {
        return SystemUtils.IS_OS_WINDOWS;
    }

    @Override
    public boolean isLinux() {
        return SystemUtils.IS_OS_LINUX;
    }

    @Override
    public boolean isUnix() {
        return SystemUtils.IS_OS_UNIX;
    }

    @Override
    public boolean isMac() {
        return SystemUtils.IS_OS_MAC;
    }
    
    @Override
    public boolean isJava1_8() {
        return SystemUtils.IS_JAVA_1_8;
    }
    
    @Override
    public File getJavaHome() {
        return SystemUtils.getJavaHome();
    }
    
    @Override
    public String getJavaSpecificationVersion() {
        return SystemUtils.JAVA_SPECIFICATION_VERSION;
    }
    
    // Net

    @Override
    public boolean isIpV4Addess(String address) {
        return InetAddressValidator.getInstance().isValidInet4Address(address);
    }

    // IO
    
    @Override
    public String toString(InputStream in, Charset charset) throws IOException {
        return org.apache.commons.io.IOUtils.toString(in, charset);
    }

    @Override
    public List<String> readLines(InputStream in, Charset charset) throws IOException {
        try {
            return org.apache.commons.io.IOUtils.readLines(in, charset);
        } catch (UncheckedIOException e) {
            throw new IOException(e);
        }
    }

    // File

    @Override
    public boolean deleteQuietly(File file) {
        return org.apache.commons.io.FileUtils.deleteQuietly(file);        
    }
    
    @Override
    public void forceDelete(final File file) throws IOException {
        org.apache.commons.io.FileUtils.forceDelete(file);
    }

    @Override
    public void deleteOnExit(File file) {
        try {
            if (null != file) {
                org.apache.commons.io.FileUtils.forceDeleteOnExit(file);
            }
        } catch (IOException e) {
        }
    }

    @Override
    public String getTempDirectoryPath() {
        return org.apache.commons.io.FileUtils.getTempDirectoryPath();
    }

    @Override
    public File getTempDirectory() {
        return org.apache.commons.io.FileUtils.getTempDirectory();
    }
    
    @Override
    public File getUserDirectory() {
        return org.apache.commons.io.FileUtils.getUserDirectory();
    }

    @Override
    public String getUserDirectoryPath() {
        return org.apache.commons.io.FileUtils.getUserDirectoryPath();
    }

    @Override
    public String fileToBase64(File file) throws IOException {
        byte[] fileContent = org.apache.commons.io.FileUtils.readFileToByteArray(file);
        return Base64.getEncoder().encodeToString(fileContent);        
    }

    @Override
    public void base64ToFile(String string, File file) throws IOException {
        byte[] decodedBytes = Base64.getDecoder().decode(string);
        org.apache.commons.io.FileUtils.writeByteArrayToFile(file, decodedBytes);   
    }

    @Override
    public File findFile(File folder, String name) {
        Collection<File> tmp = org.apache.commons.io.FileUtils.listFiles(folder, 
            FileFilterUtils.nameFileFilter(name), 
            TrueFileFilter.INSTANCE);
        Iterator<File> iter = tmp.iterator();
        File result;
        if (iter.hasNext()) {
            result = iter.next();
        } else {
            result = null;
        }
        return result;
    }
    
    @Override
    public void write(final File file, final CharSequence data, final Charset charset) throws IOException {
        org.apache.commons.io.FileUtils.write(file, data, charset);
    }

    @Override
    public String readFileToString(final File file) throws IOException {
        return org.apache.commons.io.FileUtils.readFileToString(file, Charset.defaultCharset());
    }

    @Override
    public String readFileToString(final File file, final Charset charset) throws IOException {
        return org.apache.commons.io.FileUtils.readFileToString(file, charset);
    }

    @Override
    public void deleteDirectory(final File directory) throws IOException {
        org.apache.commons.io.FileUtils.deleteDirectory(directory);
    }

    @Override
    public void cleanDirectory(final File directory) throws IOException {
        org.apache.commons.io.FileUtils.cleanDirectory(directory);
    }

    @Override
    public void copyDirectory(final File srcDir, final File destDir, final FileFilter filter) throws IOException {
        org.apache.commons.io.FileUtils.copyDirectory(srcDir, destDir, filter);
    }    

    @Override
    public void copyDirectory(final File srcDir, final File destDir, final FileFilter filter, 
        final boolean preserveFileDate) throws IOException {
        org.apache.commons.io.FileUtils.copyDirectory(srcDir, destDir, filter, preserveFileDate);
    }
    
    @Override
    public void copyDirectory(final File srcDir, final File destDir) throws IOException {
        org.apache.commons.io.FileUtils.copyDirectory(srcDir, destDir);
    }
    
    @Override
    public void copyFile(final File srcFile, final File destFile) throws IOException {
        org.apache.commons.io.FileUtils.copyFile(srcFile, destFile);
    }
    
    @Override
    public void writeStringToFile(final File file, final String data) throws IOException {
        org.apache.commons.io.FileUtils.writeStringToFile(file, data, Charset.defaultCharset());
    }    
    
    @Override
    public void writeStringToFile(final File file, final String data, final Charset charset) throws IOException {
        org.apache.commons.io.FileUtils.writeStringToFile(file, data, charset);
    }

    @Override
    public byte[] readFileToByteArray(final File file) throws IOException {
        return org.apache.commons.io.FileUtils.readFileToByteArray(file);
    }
    
    @Override
    public void writeByteArrayToFile(final File file, final byte[] data) throws IOException {
        org.apache.commons.io.FileUtils.writeByteArrayToFile(file, data);
    }
    
    @Override
    public void writeByteArrayToFile(final File file, final byte[] data, final boolean append) throws IOException {
        org.apache.commons.io.FileUtils.writeByteArrayToFile(file, data, append);
    }

    @Override
    public void copyInputStreamToFile(final InputStream source, final File destination) throws IOException {
        org.apache.commons.io.FileUtils.copyInputStreamToFile(source, destination);
    }

    @Override
    public boolean contentEquals(final File file1, final File file2) throws IOException {
        return org.apache.commons.io.FileUtils.contentEquals(file1, file2);
    }

    @Override
    public void registerDateConverters() {
        TimeUtils.registerConverter(new AbstractDateConverter<DateTime>(DateTime.class) {

            @Override
            public Date toDate(DateTime data) {
                return data.toDate();
            }
            
        });
    }

}
