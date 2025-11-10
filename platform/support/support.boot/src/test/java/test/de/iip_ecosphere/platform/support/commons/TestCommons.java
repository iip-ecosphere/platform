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
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ExecutionException;

import de.iip_ecosphere.platform.support.commons.Commons;
import de.iip_ecosphere.platform.support.commons.FileAlterationMonitor;
import de.iip_ecosphere.platform.support.commons.FileAlterationObserver;
import de.iip_ecosphere.platform.support.commons.Tailer;
import de.iip_ecosphere.platform.support.commons.TailerListener;

/**
 * Empty test class for commons.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TestCommons extends Commons {

    // collections

    @Override
    public void reverse(final Object[] array) { // needed for test
        if (null != array) {
            for (int i = 0; i < array.length / 2; i++) {
                Object temp = array[i];
                array[i] = array[array.length - 1 - i];
                array[array.length - 1 - i] = temp;
            }
        }
    }

    // objects, beans

    @Override
    public void copyFields(Object source, Object target) throws ExecutionException {
    }
    
    // Strings

    @Override
    public final String escapeJava(final String input) {
        return null;
    }

    @Override
    public final String unescapeJava(final String input) {
        return null;
    }
    
    @Override
    public String escapeJson(final String input) {
        return null;
    }

    @Override
    public String unescapeJson(final String input) {
        return null;
    }
    
    @Override
    public <T extends CharSequence> T defaultIfBlank(final T str, final T defaultStr) {
        return null;
    }

    @Override
    public <T extends CharSequence> T defaultIfEmpty(final T str, final T defaultStr) {
        return null;
    }

    @Override
    public boolean isBlank(final CharSequence cs) {
        return false;
    }

    @Override
    public boolean isNotBlank(final CharSequence cs) {
        return false;
    }
    
    @Override
    public String replaceOnce(final String text, final String searchString, final String replacement) {
        return null;
    }

    @Override
    public boolean isEmpty(final CharSequence cs) {
        return false;
    }
    
    @Override
    public String toString(Object obj) {
        return null;
    }

    @Override
    public String toStringShortStyle(Object obj) {
        return null;
    }
    
    @Override
    public String removeStart(String str, String remove) {
        return null;
    }
    
    @Override
    public String removeEnd(String str, String remove) {
        return null;
    }
    
    // OS
    
    @Override
    public boolean isWindows() {
        return false;
    }

    @Override
    public boolean isLinux() {
        return false;
    }

    @Override
    public boolean isUnix() {
        return false;
    }

    @Override
    public boolean isMac() {
        return false;
    }
    
    @Override
    public boolean isJava1_8() {
        return false;
    }
    
    @Override
    public boolean isAtLeastJava9() {
        return false;
    }
    
    // Net

    @Override
    public boolean isIpV4Addess(String address) {
        return false;
    }

    // IO
    
    @Override
    public String toString(InputStream in, Charset charset) throws IOException {
        return new String(in.readAllBytes(), charset);
    }

    @Override
    public List<String> readLines(InputStream in, Charset charset) throws IOException {
        return null;
    }

    @Override
    public byte[] toByteArray(InputStream inputStream) throws IOException {
        return null;
    }

    // File

    @Override
    public boolean deleteQuietly(File file) {
        return false;
    }
    
    @Override
    public void forceDelete(final File file) throws IOException {
    }

    @Override
    public void deleteOnExit(File file) {
    }

    @Override
    public String fileToBase64(File file) throws IOException {
        byte[] fileContent = Files.readAllBytes(file.toPath());
        return new String(Base64.getDecoder().decode(fileContent));
    }

    @Override
    public void base64ToFile(String string, File file) throws IOException {
        String txt = Base64.getEncoder().encodeToString(string.getBytes());
        Files.writeString(file.toPath(), txt);
    }

    @Override
    public File findFile(File folder, String name) {
        return null;
    }
    
    @Override
    public void write(final File file, final CharSequence data, final Charset charset) throws IOException {
    }

    @Override
    public String readFileToString(final File file) throws IOException {
        return null;
    }

    @Override
    public String readFileToString(final File file, final Charset charset) throws IOException {
        return null;
    }

    @Override
    public void deleteDirectory(final File directory) throws IOException {
    }

    @Override
    public void cleanDirectory(final File directory) throws IOException {
    }

    @Override
    public void copyDirectory(final File srcDir, final File destDir, final FileFilter filter) throws IOException {
    }    

    @Override
    public void copyDirectory(final File srcDir, final File destDir, final FileFilter filter, 
        final boolean preserveFileDate) throws IOException {
    }
    
    @Override
    public void copyDirectory(final File srcDir, final File destDir) throws IOException {
    }
    
    @Override
    public void copyFile(final File srcFile, final File destFile) throws IOException {
    }
    
    @Override
    public void writeStringToFile(final File file, final String data) throws IOException {
    }    
    
    @Override
    public void writeStringToFile(final File file, final String data, final Charset charset) throws IOException {
    }

    @Override
    public byte[] readFileToByteArray(final File file) throws IOException {
        return null;
    }
    
    @Override
    public void writeByteArrayToFile(final File file, final byte[] data) throws IOException {
    }
    
    @Override
    public void writeByteArrayToFile(final File file, final byte[] data, final boolean append) throws IOException {
    }

    @Override
    public void copyInputStreamToFile(final InputStream source, final File destination) throws IOException {
    }

    @Override
    public boolean contentEquals(final File file1, final File file2) throws IOException {
        return false;
    }

    @Override
    public void registerDateConverters() {
    }

    // tailer

    @Override
    public Tailer createTailer(File file, TailerListener listener, Duration delayDuration, boolean fromEnd) {
        return null;
    }

    @Override
    public FileAlterationObserver createFileAlterationObserver(String directory, FileFilter fileFilter) {
        return null;
    }

    @Override
    public FileAlterationMonitor createFileAlterationMonitor(long interval, FileAlterationObserver... observers) {
        return null;
    }
    
}
