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

package de.iip_ecosphere.platform.services.spring.loader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

import org.springframework.boot.loader.JarLauncher;
import org.springframework.boot.loader.LaunchedURLClassLoader;
import org.springframework.boot.loader.archive.Archive;
import org.springframework.boot.loader.archive.ExplodedArchive;
import org.springframework.boot.loader.archive.JarFileArchive;

import de.iip_ecosphere.platform.support.FileUtils;
import de.iip_ecosphere.platform.support.IOUtils;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;
import de.iip_ecosphere.platform.support.plugins.ChildClassLoader;
import de.iip_ecosphere.platform.support.plugins.ChildFirstClassLoader;
import de.iip_ecosphere.platform.support.plugins.ChildFirstURLClassLoader;
import de.iip_ecosphere.platform.support.plugins.CompoundEnumeration;
import de.iip_ecosphere.platform.support.plugins.FindClassClassLoader;

import org.springframework.boot.loader.archive.Archive.EntryFilter;

/**
 * Frontend class for the actual generated application start class to get the class loading context right. This class 
 * shall have no dependencies into oktoflow to keep the initial class loader clean. 
 * 
 * @author Holger Eichelberger, SSE
 */
public class AppStarter {
    
    public static final String PROPERTY_MAIN_CLASS = "iip.appStarter.mainClass";
    public static final String APP_CLASSPATH_INDEX_LOCATION = "BOOT-INF/classpath-app.idx";
    public static final String APP_CLASSPATH = "classpath-app";
    private static String mainClass = System.getProperty(PROPERTY_MAIN_CLASS, "iip.Starter");

    /**
     * Re-defines the qualified name of the main class.
     * 
     * @param cls the qualified name of the main class
     */
    public static void setMainClass(String cls) {
        if (null != cls && cls.length() > 0) {
            mainClass = cls;
        }
    }
    
    // checkstyle: stop exception type check
    
    /**
     * Implements an accessible launcher just to access the Spring packaged JARs in the way Spring is doing it.
     * Needed, as one method in the Spring Launcher hierarchy is final and the ClassPathIndexFile is package-local. 
     * Call {@link #createStackedLoader(ClassLoader)} on an instance.
     * 
     * @author Spring
     * @author Holger Eichelberger, SSE
     */
    public static class AccessibleLauncher {
        
        /**
         * Filter for nested archive entries.
         */
        static final EntryFilter NESTED_ARCHIVE_ENTRY_FILTER = (entry) -> {
            if (entry.isDirectory()) {
                return entry.getName().equals("BOOT-INF/classes-app/");
            }
            return entry.getName().startsWith("BOOT-INF/lib-app/");
        };        
        
        private Archive archive;
        private ClassPathIndexFile classPathIndex;
        
        /**
         * Creates the launcher by accessing the archive and trying to read the classpath index file.
         * 
         * @throws IllegalStateException if the archive/classpath index cannot be accessed; anyway, there may not be a 
         *     classpath index (legacy loading)
         */
        public AccessibleLauncher() {
            try {
                this.archive = createArchive();
                this.classPathIndex = getClassPathIndex(this.archive);
            } catch (NoSuchElementException ex) {
                // ok, classPathIndex == null, start without classpath isolation
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        }
        
        /**
         * Creates the launcher via an already existing archive.
         * 
         * @throws IllegalStateException if the archive/classpath index cannot be accessed; anyway, there may not be a 
         *     classpath index (legacy loading)
         */
        public AccessibleLauncher(Archive archive) {
            try {
                this.archive = archive;
                this.classPathIndex = getClassPathIndex(this.archive);
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        }

        /**
         * Loads the classpath index file.
         * 
         * @param archive the archive to load from 
         * @return the classpath index file, may be <b>null</b> for none
         * @throws IOException if loading fails
         */
        protected ClassPathIndexFile getClassPathIndex(Archive archive) throws IOException {
            ClassPathIndexFile result = null;
            JarFile jf = null;
            try {
                // also for testing, not via classloader
                jf = new JarFile(new File(archive.getUrl().toURI())); 
                ZipEntry ent = jf.getEntry(APP_CLASSPATH_INDEX_LOCATION);
                if (null != ent) {
                    result = new ClassPathIndexFile(ClassPathIndexFile.asFile(archive.getUrl()), 
                        ClassPathIndexFile.loadLines(jf.getInputStream(ent)));
                }
                jf.close();
                if (null == ent) {
                    throw new NoSuchElementException(APP_CLASSPATH_INDEX_LOCATION + " not found");
                }
            } catch (FileNotFoundException e) {
                // ok, if not packaged for classpath separation
                if (null != jf) {
                    jf.close();
                }
            } catch (URISyntaxException | IOException e) {
                System.out.println("Cannot open archive for reading " + APP_CLASSPATH_INDEX_LOCATION);
                if (null != jf) {
                    jf.close();
                }
            }
            if (null == result) {
                // Only needed for exploded archives, regular ones already have a defined order
                // if (archive instanceof ExplodedArchive) {
                String location = APP_CLASSPATH_INDEX_LOCATION; // diff from origin, do not try to access manifest
                result = ClassPathIndexFile.loadIfPossible(archive.getUrl(), location);
                //}
            }
            return result;
        }
        
        /**
         * Creates the archive.
         * 
         * @return
         * @throws Exception
         */
        protected final Archive createArchive() throws Exception {
            ProtectionDomain protectionDomain = JarLauncher.class.getProtectionDomain(); // Changed class here
            CodeSource codeSource = protectionDomain.getCodeSource();
            URI location = (codeSource != null) ? codeSource.getLocation().toURI() : null;
            String path = (location != null) ? location.getSchemeSpecificPart() : null;
            if (path == null) {
                throw new IllegalStateException("Unable to determine code source archive");
            }
            File root = new File(path);
            if (!root.exists()) {
                throw new IllegalStateException("Unable to determine code source archive from " + root);
            }
            return (root.isDirectory() ? new ExplodedArchive(root) : new JarFileArchive(root));
        }        
        
        /**
         * Creates a stacked class loader.
         * 
         * @param parent the parent classloader to be used as default
         * @return the classloader or {@code parent}
         * @throws Exception if creating the classloader fails
         */
        public ClassLoader createStackedLoader(ClassLoader parent) throws Exception {
            if (!isExploded()) {
                org.springframework.boot.loader.jar.JarFile.registerUrlProtocolHandler();
            }            
            ClassLoader result = parent;
            if (null != classPathIndex) { // legacy loading
                result = createClassLoader(getClassPathArchivesIterator());
            }
            return result;
        }
        
        /**
         * Returns a iterator over the class path archives.
         * 
         * @return the iterator
         * @throws Exception if accessing/constructing the iterator fails
         */
        protected Iterator<Archive> getClassPathArchivesIterator() throws Exception {
            Archive.EntryFilter searchFilter = this::isSearchCandidate;
            Iterator<Archive> archives = this.archive.getNestedArchives(searchFilter,
                (entry) -> isNestedArchive(entry) && !isEntryIndexed(entry));
            if (isPostProcessingClassPathArchives()) {
                archives = applyClassPathArchivePostProcessing(archives);
            }
            return archives;
        }

        /**
         * Returns whether a post processing of the class path archives is needed.
         * 
         * @return {@code true} for post processing
         */
        protected boolean isPostProcessingClassPathArchives() {
            return false;
        }
        
        /**
         * Returns whether {@code entry} is a nested archive.
         * 
         * @param entry the entry to analyze
         * @return {@code true} for nested archive, {@code false} for flat
         */
        protected boolean isNestedArchive(Archive.Entry entry) {
            return NESTED_ARCHIVE_ENTRY_FILTER.matches(entry);
        }
        
        /**
         * Returns whether {@code entry} is a search candidate.
         * 
         * @param entry the entry
         * @return {@code true} for search candidate, {@code false} else
         */
        protected boolean isSearchCandidate(Archive.Entry entry) {
            return entry.getName().startsWith("BOOT-INF/");
        }
        
        /**
         * Returns whether {@code entry} is indexed.
         * 
         * @param entry the entry
         * @return {@code true} for indexed, {@code false} else
         */
        private boolean isEntryIndexed(Archive.Entry entry) {
            if (this.classPathIndex != null) {
                return this.classPathIndex.containsEntry(entry.getName());
            }
            return false;
        }
        
        /**
         * Performs class path archive post processing.
         * 
         * @param archives the archives
         * @return the archives iterator
         * @throws Exception if accessing the iterator/postprocessing fails
         * @see #postProcessClassPathArchives(List)
         */
        private Iterator<Archive> applyClassPathArchivePostProcessing(Iterator<Archive> archives) throws Exception {
            List<Archive> list = new ArrayList<>();
            while (archives.hasNext()) {
                list.add(archives.next());
            }
            postProcessClassPathArchives(list);
            return list.iterator();
        }
        
        /**
         * Called to post-process archive entries before they are used. Implementations can
         * add and remove entries.
         * @param archives the archives
         * @throws Exception if the post processing fails
         * @see #isPostProcessingClassPathArchives()
         */
        protected void postProcessClassPathArchives(List<Archive> archives) throws Exception {
        }   
        
        /**
         * Returns whether the archive is exploded.
         * 
         * @return {@code true} for exploded
         */
        protected boolean isExploded() {
            return this.archive.isExploded();
        }

        /**
         * Returns the archive.
         * 
         * @return the archive
         */
        protected final Archive getArchive() {
            return this.archive;
        }
        
        /**
         * Creates the class loader based on an iterator of archives.
         * 
         * @param archives the archives
         * @return the classloader
         * @throws Exception if creating the class loader fails
         * @see #createClassLoader(URL[])
         */
        protected ClassLoader createClassLoader(Iterator<Archive> archives) throws Exception {
            List<URL> urls = new ArrayList<>(guessClassPathSize());
            while (archives.hasNext()) {
                urls.add(archives.next().getUrl());
            }
            if (this.classPathIndex != null) {
                urls.addAll(this.classPathIndex.getUrls());
            }
            return createClassLoader(urls.toArray(new URL[0]));
        }
        
        /**
         * Guesses the classpath size.
         * 
         * @return the guessed classpath size
         */
        private int guessClassPathSize() {
            if (this.classPathIndex != null) {
                return this.classPathIndex.size() + 10;
            }
            return 50;
        }        

        /**
         * Creates the archive class loader based on the loaded index file.
         * 
         * @param urls the URLs to construct the class loader
         * @return the classloader
         * @throws Exception if creating the class loader fails
         */
        protected ClassLoader createClassLoader(URL[] urls) throws Exception {
            // could also be an URL classloader
            //return new LaunchedURLClassLoader(isExploded(), getArchive(), urls, getClass().getClassLoader());
            return new ChildFirstLaunchedURLClassLoader(isExploded(), getArchive(), urls, getClass().getClassLoader());
        }
        
    }
    
    /**
     * A delegating child classloader to make internal methods accessible.
     * 
     * @author Stackoverflow
     */
    static class ChildLaunchedURLClassLoader extends LaunchedURLClassLoader implements ChildClassLoader {
        
        private FindClassClassLoader realParent;
        private Map<String, Class<?>> classes = new HashMap<>();

        /**
         * Creates an instance with delegation to the real parent class loader.
         * 
         * @param urls the URLs to load classes from
         * @param realParent the real parent class loader
         */
        public ChildLaunchedURLClassLoader(boolean exploded, Archive rootArchive, URL[] urls, 
            FindClassClassLoader realParent) {
            super(exploded, rootArchive, urls, null);
            this.realParent = realParent;
        }
        
        @Override
        public URL getResource(String name) {
            URL result = realParent.getResource(name);
            if (null == result) {
                result = super.getResource(name);
            }
            return result;
        }
        
        @Override
        public Enumeration<URL> getResources(String name) throws IOException {
            @SuppressWarnings("unchecked")
            Enumeration<URL>[] tmp = (Enumeration<URL>[]) new Enumeration<?>[2];
            int index = 0;
            IOException ex1 = null;
            IOException ex2 = null;
            try {
                tmp[index++] = realParent.getResources(name);
            } catch (IOException ex) {
                ex1 = ex;
            }
            try {
                tmp[index++] = super.getResources(name);
            } catch (IOException ex) {
                ex2 = ex;
            }
            if (ex1 != null && ex2 != null) {
                throw ex1;
            }
            return new CompoundEnumeration<>(tmp);        
        }
        
        @Override
        public InputStream getResourceAsStream(String name) {
            InputStream result = realParent.getResourceAsStream(name);
            if (null == result) {
                result = super.getResourceAsStream(name);
            }
            return result;
        }
        
        @Override
        public Class<?> findClass(String name) throws ClassNotFoundException {
            Class<?> result = classes.get(name);
            if (null == result) { // linkage error else, due to inconsistent response (?)
                result = findClassIntern(name);
                classes.put(name, result);
            }
            return result;
        }
        
        /**
         * Finds a class (no caching).
         * 
         * @param name the qualified class name
         * @return the class object
         * @throws ClassNotFoundException if the class cannot be found
         */
        public Class<?> findClassIntern(String name) throws ClassNotFoundException {
            boolean isJava = name.startsWith("java.") || name.startsWith("javax."); // java is java
            try {
                if (isJava) {
                    return realParent.loadClass(name);
                }
            } catch (ClassNotFoundException e) {
                // may also fail if there is eg no logger, the try super
            }
            try {
                // first try to use the URLClassLoader findClass
                return super.findClass(name);
            } catch (ClassNotFoundException e) {
                // if that fails, we ask our real parent classloader to load the class (we give up)
                return realParent.loadClass(name);
            }
        }
    }    
    
    /**
     * A delegating child-first classloader.
     * 
     * @author Holger Eichelberger, SSE
     */
    static class ChildFirstLaunchedURLClassLoader extends ChildFirstClassLoader {

        /**
         * Creates a child-first classloader.
         * 
         * @param urls the URLs to load classes from
         * @param parent the parent class loader
         */
        public ChildFirstLaunchedURLClassLoader(boolean exploded, Archive rootArchive, URL[] urls, ClassLoader parent) {
            super(p -> new ChildLaunchedURLClassLoader(exploded, rootArchive, urls, p), parent);
        }
        
    }    
    
    
    /**
     * A class path index file that provides ordering information for JARs. Taken over from spring as not accessible.
     *
     * @author Spring
     */
    static final class ClassPathIndexFile {

        private final File root;
        private final List<String> lines;

        /**
         * Creates an instance based on a file root and a listing of index lines.
         * 
         * @param root the root
         * @param lines the lines
         */
        private ClassPathIndexFile(File root, List<String> lines) {
            this.root = root;
            this.lines = lines.stream().map(this::extractName).collect(Collectors.toList());
        }

        /**
         * Extracts the name from the line.
         * 
         * @param line the line
         * @return the extracted name
         * @throws IllegalStateException if the classpath index is malformed
         */
        private String extractName(String line) {
            if (line.startsWith("- \"") && line.endsWith("\"")) {
                return line.substring(3, line.length() - 1);
            }
            throw new IllegalStateException("Malformed classpath index line [" + line + "]");
        }

        /**
         * Returns the size of the classpath index, i.e., the number of lines.
         * 
         * @return the size
         */
        int size() {
            return this.lines.size();
        }

        /**
         * Returns whether a given name is contained in the index.
         * 
         * @param name the name to look for
         * @return {@code true} whether an entry for that name is known, {@code false} else
         */
        boolean containsEntry(String name) {
            if (name == null || name.isEmpty()) {
                return false;
            }
            return this.lines.contains(name);
        }

        /**
         * Returns the URLs of all entries.
         * 
         * @return the URLs
         */
        List<URL> getUrls() {
            return Collections.unmodifiableList(this.lines.stream().map(this::asUrl).collect(Collectors.toList()));
        }

        /**
         * Turns a line into an URL.
         * 
         * @param line the line
         * @return the URL
         * @throws IllegalStateException if the URL cannot be constructed
         */
        private URL asUrl(String line) {
            try {
                String tmp = "jar:" + this.root.toURI() + "!/" + line + "!/";
                return new URL(tmp);
                // whyever this does not work; return new File(this.root, line).toURI().toURL();
            } catch (MalformedURLException ex) {
                throw new IllegalStateException(ex);
            }
        }

        /**
         * Loads the classpath index file if possible.
         * 
         * @param root the root URL
         * @param location the location of the classpath index file
         * @return the classpath index file, may be <b>null</b> for none/not found (legacy loading)
         * @throws IOException if the file cannot be read
         */
        static ClassPathIndexFile loadIfPossible(URL root, String location) throws IOException {
            return loadIfPossible(asFile(root), location);
        }

        /**
         * Loads the classpath index file if possible.
         * 
         * @param root the root URL
         * @param location the location of the classpath index file
         * @return the classpath index file, may be <b>null</b> for none/not found (legacy loading)
         * @throws IOException if the file cannot be read
         */
        private static ClassPathIndexFile loadIfPossible(File root, String location) throws IOException {
            return loadIfPossible(root, new File(root, location));
        }

        /**
         * Loads the classpath index file if possible.
         * 
         * @param root the root URL
         * @param indexFile the classpath index file
         * @return the classpath index file, may be <b>null</b> for none/not found (legacy loading)
         * @throws IOException if the file cannot be read
         */
        private static ClassPathIndexFile loadIfPossible(File root, File indexFile) throws IOException {
            if (indexFile.exists() && indexFile.isFile()) {
                try (InputStream inputStream = new FileInputStream(indexFile)) {
                    return new ClassPathIndexFile(root, loadLines(inputStream));
                }
            }
            return null;
        }

        /**
         * Loads the lines of the index file.
         * 
         * @param inputStream the input stream to load from
         * @return the lines
         * @throws IOException if reading the lines fails
         */
        private static List<String> loadLines(InputStream inputStream) throws IOException {
            List<String> lines = new ArrayList<>();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String line = reader.readLine();
            while (line != null) {
                if (!line.trim().isEmpty()) {
                    lines.add(line);
                }
                line = reader.readLine();
            }
            return Collections.unmodifiableList(lines);
        }

        /**
         * Turns an URL into a file.
         * 
         * @param url the URL
         * @return the file
         */
        private static File asFile(URL url) {
            if (!"file".equals(url.getProtocol())) {
                throw new IllegalArgumentException("URL does not reference a file");
            }
            try {
                return new File(url.toURI());
            } catch (URISyntaxException ex) {
                return new File(url.getPath());
            }
        }

    }    
    
    /**
     * Executes the application by getting the class loading right and by calling the corresponding main method of 
     * {@link #mainClass}. 
     * 
     * @param args command line arguments
     * @throws Exception if any exception occurred during startup
     */
    public static void main(String[] args) throws Exception {
        ClassLoader loader = AppStarter.class.getClassLoader();
        System.out.println("oktoflow Spring application loader, main class loader " + loader);        
        // adjust class loader
        String zipAppClasspathFile = System.getProperty("okto.loader.app");
        File cpFile = null;
        InputStream cp = null;
        if (zipAppClasspathFile != null) {
            cpFile = new File(zipAppClasspathFile);
            if (cpFile.isDirectory()) {
                cpFile = new File(cpFile, APP_CLASSPATH);
            }
            try {
                cp = new FileInputStream(cpFile);
            } catch (IOException e) {
                // not found, ok
            }
        }
        if (cp != null) { // legacy loading
            List<String> lines = IOUtils.readLines(cp);
            for (String line: lines) {
                if (!line.startsWith("#")) {
                    List<URL> cpUrls = new ArrayList<>();
                    StringTokenizer tokenizer = new StringTokenizer(line, ":;");
                    File cpParentFile = cpFile.getParentFile();
                    while (tokenizer.hasMoreTokens()) {
                        File f = new File(cpParentFile, tokenizer.nextToken()).getAbsoluteFile();
                        cpUrls.add(f.toURI().toURL());
                    }
                    loader = new ChildFirstURLClassLoader(cpUrls.toArray(new URL[cpUrls.size()]), loader);
                    break;
                }
            }
            FileUtils.closeQuietly(cp);
        } else if (loader.getClass().getName().equals(LaunchedURLClassLoader.class.getName())) { // spring packaged
            // try it, loader == loader if there is no spring resource
            loader = new AccessibleLauncher().createStackedLoader(loader); // implies child first
        }
        System.out.println("Using app class loader " + loader);        
        Thread.currentThread().setContextClassLoader(loader);
        // load/execute dynamically, no dependencies
        try {
            LoggerFactory.getLogger(AppStarter.class).info("Loading/starting {}", mainClass); 
            Class<?> cls = loader.loadClass(mainClass); // there by convention/code generation
            Method m = cls.getDeclaredMethod("main", String[].class);
            m.invoke(null, new Object[] {args});
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException e) {
            LoggerFactory.getLogger(AppStarter.class).error("Cannot execute application starter ({}): {} {}", 
                mainClass, e.getClass().getSimpleName(), e.getMessage());
        } catch (InvocationTargetException e) {
            LoggerFactory.getLogger(AppStarter.class).error("Cannot execute application starter ({}):", mainClass); 
            LoggerFactory.getLogger(AppStarter.class).error("Trace: ", e); 
        }
    }

    // checkstyle: resume exception type check
    
}
