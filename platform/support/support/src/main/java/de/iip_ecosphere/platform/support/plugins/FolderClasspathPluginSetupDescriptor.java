/**
 * ******************************************************************************
 * Copyright (c) {2024} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.plugins;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.io.IOUtils;

import de.iip_ecosphere.platform.support.logging.LoggerFactory;

/**
 * Default plugin setup descriptor based based on loading from a project folder containing jars and the 
 * classpath in "classpath" or in "target/classes/classpath".
 * 
 * @author Holger Eichelberger, SSE
 */
public class FolderClasspathPluginSetupDescriptor extends URLPluginSetupDescriptor {

    public static final String KEY_SETUP_DESCRIPTOR = "# setupDescriptor: ";
    public static final String KEY_PLUGIN_IDS = "# pluginIds: ";

    private File folder;
    private boolean descriptorOnly;

    /**
     * Creates an instance based on project folder containing jars and the classpath in "target/classes/classpath".
     * 
     * @param folder the basis folder
     */
    public FolderClasspathPluginSetupDescriptor(File folder) {
        this(folder, false);
    }

    /**
     * Creates an instance based on project folder containing jars and the classpath in "target/classes/classpath".
     * 
     * @param folder the basis folder
     * @param descriptorOnly load the descriptor JARs only or the full thing
     * @param appends further classpath files that shall be appended, e.g., logging, may be <b>null</b>
     */
    public FolderClasspathPluginSetupDescriptor(File folder, boolean descriptorOnly, File... appends) {
        super(loadClasspathSafe(folder, descriptorOnly, appends));
        this.folder = folder;
        this.descriptorOnly = descriptorOnly;
    }

    @Override
    public File getInstallDir() {
        return folder;
    }
    
    /**
     * Finds the classpath file.
     * 
     * @param folder the folder to start searching
     * @param suffix optional suffix, e.g., "-win", may be empty or <b>null</b> for none
     * @return the classpath file
     */
    public static File findClasspathFile(File folder, String suffix) {
        suffix = suffix == null ? "" : suffix;
        File f = new File(folder, "classpath" + suffix); // unpacked
        if (!f.exists()) {
            f = new File(folder, "target/classes/classpath" + suffix); // development, in project
        }
        return f;
    }

    /**
     * Loads a resource in classpath format and returns the specified classpath entries as URLs. Logs errors and 
     * exceptions.
     * 
     * @param folder the basis folder
     * @param descriptorOnly only the first/two entries, the full thing else
     * @param appends further classpath files that shall be appended, e.g., logging, may be <b>null</b>
     * @return the URLs, may be empty
     */
    public static URL[] loadClasspathSafe(File folder, boolean descriptorOnly, File... appends) {
        return loadClasspathFileSafe(findClasspathFile(folder, ""), folder, descriptorOnly, appends);
    }
    
    /**
     * Loads a classpath file relative to the actual jars and returns the specified classpath entries as URLs. 
     * Logs errors and  exceptions.
     * 
     * @param cpFile the classpath file
     * @param descriptorOnly only the first/two entries, the full thing else
     * @return the URLs, may be empty
     */
    public static URL[] loadClasspathFileSafe(File cpFile, boolean descriptorOnly) {
        return loadClasspathFileSafe(cpFile, cpFile.getParentFile(), descriptorOnly);
    }
    
    /**
     * Loads a classpath file relative to the actual jars and returns the specified classpath entries as URLs. 
     * Logs errors and  exceptions.
     * 
     * @param cpFile the classpath file
     * @param base the base folder use to make relative classpath entries absolute
     * @param descriptorOnly only the first/two entries, the full thing else
     * @param appends further classpath files that shall be appended, e.g., logging, may be <b>null</b>
     * @return the URLs, may be empty
     */
    private static URL[] loadClasspathFileSafe(File cpFile, File base, boolean descriptorOnly, File... appends) {
        URL[] result = null;
        try (InputStream in = new FileInputStream(cpFile)) {
            LoggerFactory.getLogger(URLPluginSetupDescriptor.class).info("Loading classpath from '{}'", cpFile);
            ClasspathFile cpf = readClasspathFile(in, base);
            if (null != appends) {
                for (File a: appends) {
                    try (InputStream aIn = new FileInputStream(a)) {
                        LoggerFactory.getLogger(URLPluginSetupDescriptor.class).info(
                            "Appending classpath from '{}'", a);
                        ClasspathFile aCpf = readClasspathFile(aIn, base);
                        cpf.entries.addAll(aCpf.entries);
                    } catch (IOException e) {
                        LoggerFactory.getLogger(URLPluginSetupDescriptor.class).error(
                            "While reading append classpath from '{}': {} Ignoring.", a, e.getMessage());
                    }
                }
            }
            List<File> entries = new ArrayList<File>(cpf.entries);
            if (descriptorOnly) {
                if (entries.size() > 1) {
                    List<File> tmp = new ArrayList<>();
                    tmp.add(entries.get(0));
                    String first = stripExtension(entries.get(0).getName());
                    String second = stripExtension(entries.get(1).getName());
                    if (second.startsWith(first)) { // xxx-tests
                        tmp.add(entries.get(1));
                    }
                    entries = tmp;
                }
            }
            result = toURLSafe(entries.toArray(new File[entries.size()]));
        } catch (IOException e) {
            LoggerFactory.getLogger(URLPluginSetupDescriptor.class).error(
                "While reading classpath from '{}': {} Ignoring.", cpFile, e.getMessage());
        }
        if (null == result) {
            result = new URL[0];
        }
        return result;
    }

    /**
     * Represents a classpath file and selected descriptive entries.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class ClasspathFile {
        
        private List<File> entries = new ArrayList<>();
        private String setupDescriptor;
        private List<String> pluginIds;
        
        /**
         * The classpath entries.
         * 
         * @return the entries
         */
        public List<File> getEntries() {
            return entries;
        }
        
        /**
         * The setup descriptor.
         * 
         * @return the setup descriptor as short or qualified name
         */
        public String getSetupDescriptor() {
            return setupDescriptor;
        }
        
        /**
         * Returns the plugin ids.
         * 
         * @return the plugin ids, may be <b>null</b> for none
         */
        public List<String> pluginIds() {
            return pluginIds;
        }
        
    }
    
    /**
     * Reads and parses a given classpath file.
     * 
     * @param in the input stream
     * @param base the base folder use to make relative classpath entries absolute
     * @return the classpath file description object
     * @throws IOException if accessing the classpath file fails
     */
    public static ClasspathFile readClasspathFile(InputStream in, File base) throws IOException {
        ClasspathFile result = new ClasspathFile();
        List<String> contents = IOUtils.readLines(in, Charset.defaultCharset());
        for (String line : contents) {
            if (line.startsWith("#")) {
                if (line.startsWith(KEY_SETUP_DESCRIPTOR)) {
                    result.setupDescriptor = line.substring(KEY_SETUP_DESCRIPTOR.length()).trim();
                }
                if (line.startsWith(KEY_PLUGIN_IDS)) {
                    result.pluginIds = new ArrayList<>();
                    String tmp = line.substring(KEY_SETUP_DESCRIPTOR.length()).trim();
                    StringTokenizer t = new StringTokenizer(tmp, ",");
                    while (t.hasMoreTokens()) {
                        result.pluginIds.add(t.nextToken().trim());
                    }
                }
            } else {
                StringTokenizer tokenizer = new StringTokenizer(line, ":;");
                while (tokenizer.hasMoreTokens()) {
                    result.entries.add(new File(base, tokenizer.nextToken()));
                }
            }
        }
        return result;
    }
    
    /**
     * Strip the extension of a file name.
     * 
     * @param name the name
     * @return the stripped name
     */
    private static final String stripExtension(String name) {
        int pos = name.lastIndexOf('.');
        if (pos > 0) {
            name = name.substring(0, pos);
        }
        return name;
    }

    @Override
    protected ClassLoader createClassLoader(URL[] urls, ClassLoader parent) {
        return descriptorOnly ? new URLClassLoader(urls, parent) : super.createClassLoader(urls, parent);
    }

}
