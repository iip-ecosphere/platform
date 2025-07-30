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

package de.iip_ecosphere.platform.tools.maven.dependencies;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.dependency.fromConfiguration.ArtifactItem;
import org.apache.maven.shared.model.fileset.FileSet;
import org.codehaus.plexus.components.io.filemappers.FileMapper;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;

/**
 * Extended unpack Mojo for plugins.
 * 
 * @author Holger Eichelberger, SSE
 */
@Mojo(name = "unpack-plugins", defaultPhase = LifecyclePhase.TEST_COMPILE, requiresProject = false, threadSafe = true)
public class UnpackPluginMojo extends CleaningUnpackMojo {

    private static final String NAME_CLASSPATH_FILE = "classpath";
    
    @Parameter(property = "unpack.plugins", required = false)
    private List<PluginItem> plugins;
    
    @Parameter(property = "unpack.version", required = false, defaultValue = "")
    private String version;

    @Parameter(property = "unpack.relocate", required = false, defaultValue = "false")
    private boolean relocate;
    
    @Parameter(property = "unpack.relocateTarget", required = false, defaultValue = "jars")
    private File relocateTarget;

    @Parameter(property = "unpack.resolveAndCopy", required = false, defaultValue = "true")
    private boolean resolveAndCopy;

    @Parameter( defaultValue = "${project.build.directory}", readonly = true )
    private File targetDirectory;

    private List<String> classpathFiles = new ArrayList<>();
    
    @Parameter(defaultValue = "${project.remoteProjectRepositories}", readonly = true)
    private List<RemoteRepository> remoteRepositories;

    @Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
    private RepositorySystemSession repoSession;
    
    @Component
    private RepositorySystem repoSystem;
    
    private Map<String, List<String>> pluginAppends = new HashMap<>();
    
    /**
     * Represents a plugin, an extended artifact.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class PluginItem extends ArtifactItem {
        
        @Parameter(required = false) 
        private List<String> appends;
        
        /**
         * Returns the name of the plugin item.
         * 
         * @return the name
         */
        private String getName() {
            return lastPart(getArtifactId(), ".");
        }
        
        /**
         * Returns whether this item has appends.
         * 
         * @return {@code true} for appends, {@code false} else
         */
        private boolean hasAppends() {
            return null != appends && !appends.isEmpty();
        }
        
    }
    
    /**
     * Used for relocating classpath files, i.e., to rename them with their plugin short (artifactId) name.
     * 
     * @author Holger Eichelberger, SSE
     */
    private class RelocatingFileMapper implements FileMapper {

        private String targetName;
        private boolean collectIfNotRelocate;
        
        /**
         * Creates a relocating file mapper.
         * 
         * @param targetName the target name to map to
         * @param collectIfNotRelocate collect the file names if we are not relocating
         */
        private RelocatingFileMapper(String targetName, boolean collectIfNotRelocate) {
            this.targetName = targetName;
            this.collectIfNotRelocate = collectIfNotRelocate;
        }
        
        @Override
        public String getMappedFileName(String pName) {
            String name = pName;
            if (name.startsWith(NAME_CLASSPATH_FILE)) {
                if (relocate) {
                    if (name.length() == NAME_CLASSPATH_FILE.length()) {
                        name = targetName;
                    } else {
                        name = targetName + name.substring(NAME_CLASSPATH_FILE.length());
                    }
                    classpathFiles.add(name);
                } else { // else just collect with relative path
                    if (collectIfNotRelocate) {
                        classpathFiles.add(targetName + "/" + name);
                    }
                }
            }
            return name;
        }
        
    }
    
    /**
     * Returns the part after the last ".".
     * 
     * @param name the name to strip
     * @return the last part or {@code name}
     */
    private static String lastPart(String name, String separator) {
        int pos = name.lastIndexOf(separator);
        return pos > 0 ? name.substring(pos + separator.length()) : name;
    }
    
    @Override
    public void doExecute() throws MojoExecutionException, MojoFailureException {
        setForceCleanup(true);
        FileSet cleanup = new FileSet();
        cleanup.setDirectory((relocate ? relocateTarget : new File(targetDirectory, "oktoPlugins")).toString());
        setCleanup(cleanup);

        if (!relocate) {
            // figure out whether we are in development mode, i.e., in git workspace
            File skipIfExists = new File("../../support/support"); // building for arbitrary component
            if (!skipIfExists.exists()) {
                skipIfExists = new File("../support"); // building for a support component
            }
            if (skipIfExists.exists()) {
                setSkipIfExists(skipIfExists);
            }
        } else {
            relocateTarget.mkdirs();
            setForce(true);
        }

        if (plugins != null && plugins.size() > 0) {
            List<ArtifactItem> artifactItems = new ArrayList<>();
            for (PluginItem pl : plugins) {
                ArtifactItem item = new ArtifactItem();
                String name = pl.getName();
                if (StringUtils.isBlank(pl.getGroupId())) {
                    item.setGroupId("de.iip-ecosphere.platform");
                }
                item.setArtifactId(pl.getArtifactId());
                item.setVersion(StringUtils.isBlank(version) ? pl.getVersion() : version);
                item.setType("zip");
                item.setClassifier("plugin");
                item.setOverWrite(String.valueOf(true));
                item.setOutputDirectory(getOutputDir(name));
                getLog().info("Configuring plugin '" + name + "' -> " + item.getOutputDirectory());
                item.setDestFileName(name + ".zip");
                item.setFileMappers(new FileMapper[] {new RelocatingFileMapper(name, pl.hasAppends())});
                artifactItems.add(item);
            }
            setArtifactItems(artifactItems);
        }
        
        super.doExecute();
        
        handleAppends();
        relocate();
    }
    
    /**
     * Returns the output directory for a plugin/classpath file name.
     * 
     * @param name the name
     * @return the output directory
     */
    private File getOutputDir(String name) {
        return relocate ? relocateTarget : new File(targetDirectory, "oktoPlugins/" + name);
    }

    /**
     * Returns the path to a (more qualified) classpath file based on the output directory for a 
     * plugin/classpath file name.
     * 
     * @param name the name
     * @return the output directory
     */
    private File getCpFile(String name) {
        return new File(getOutputDir(name), relocate ? name : "classpath");
    }

    /**
     * Handles the appends.
     */
    private void handleAppends() {
        for (PluginItem pl : plugins) {
            if (pl.appends != null && !pl.appends.isEmpty()) {
                List<String> result = new ArrayList<>();
                for (String name: pl.appends) {
                    File cpFile = getCpFile(name);
                    try (FileInputStream fis = new FileInputStream(cpFile)) {
                        List<String> contents = IOUtils.readLines(fis, Charset.defaultCharset());
                        String prefix = null;
                        for (String line : contents) {
                            if (line.startsWith("#")) {
                                prefix = extractSuffix(BuildPluginClasspathMojo.KEY_PREFIX, line, prefix);
                            } else {
                                Tokenizer tokenizer = new Tokenizer(line);
                                prefix = fixPrefix(prefix, tokenizer);
                                while (tokenizer.hasMoreTokens()) {
                                    String token = tokenizer.nextToken();
                                    token = "../" + name + "/" + token; // relative path for other plugins
                                    result.add(token);
                                }
                            }
                        }
                    } catch (IOException e) {
                        getLog().error("Cannot locate append plugin '" + name + "' - ignoring: " + e.getMessage());
                    }
                }
                pluginAppends.put(pl.getName(), result);
            }
        }
    }
    
    /**
     * Merges files from {@code src} into {@code tgt} by only copying those that do not already exist.
     * 
     * @param src the source folder
     * @param tgt the target folder
     * @throws IOException if copying fails
     */
    private void mergeFiles(File src, File tgt) throws IOException {
        File[] srcFiles = src.listFiles();
        if (null != srcFiles) {
            for (File f : srcFiles) {
                File t = new File(tgt, f.getName());
                if (!t.exists() && f.isFile()) {
                    FileUtils.moveFile(f, t);
                }
            }
        }
    }
    
    /**
     * Extracts the suffix after removing the prefix.
     * 
     * @param prefix the prefix to look for, may be <b>null</b>
     * @param line the line to extract the suffix from
     * @param dflt the default value if there is no prefix, usually {@code line}
     * @return {@code line} or the line without the prefix
     */
    private static String extractSuffix(String prefix, String line, String dflt) {
        String result = dflt;
        if (null != prefix && line.startsWith(prefix)) {
            result = line.substring(prefix.length()).trim();
        }
        return result;
    }
   
    /**
     * Represents an OS-customized tokenizer.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class Tokenizer {
         
        private String sep = ":";
        private boolean win = false;
        private StringTokenizer tokenizer;

        /**
         * Creates and customizes the tokenizer.
         * 
         * @param line the classpath line to customize the tokenizer from
         */
        private Tokenizer(String line) {
            tokenizer = new StringTokenizer(line, sep);
            if (tokenizer.countTokens() < 1) {
                sep = ";";
                tokenizer = new StringTokenizer(line, sep);
                win = true;
            }
        }

        /**
         * Are there more tokens.
         * 
         * @return {@code true} for more tokens, {@code false} for none
         */
        private boolean hasMoreTokens() {
            return tokenizer.hasMoreTokens();
        }

        /**
         * Returns the next token,.
         * 
         * @return the next token
         * @see #hasMoreTokens()
         * @throws NoSuchElementException if there are no tokens
         */
        private String nextToken() {
            return tokenizer.nextToken();
        }
        
    }
    
    /**
     * Fixes the prefix by appending a path separator if needed.
     * 
     * @param prefix the prefix
     * @param tokenizer the tokenizer determining the path separator
     * @return the fixed prefix
     */
    private String fixPrefix(String prefix, Tokenizer tokenizer) {
        String pathSep = tokenizer.win ? "\\" : "/";
        if (null != prefix && !prefix.endsWith(pathSep)) {
            prefix = prefix + pathSep;
        }
        return prefix;
    }
    
    /**
     * Strips a prefix from a classpath token.
     * 
     * @param prefix the prefix
     * @param cpToken the classpath token
     * @return the stripped {@code cpToken} if there is something to strip
     */
    private String stripPrefix(String prefix, String cpToken) {
        cpToken = extractSuffix(prefix, cpToken, cpToken);
        cpToken = extractSuffix("target/jars", cpToken, cpToken); // the default
        return extractSuffix("target/", cpToken, cpToken); // plugin itself        
    }
    
    /**
     * Relocates the unpacked files.
     * 
     * @throws MojoExecutionException if the execution fails
     * @throws MojoFailureException if a remarkable processing failure occurred
     */
    private void relocate() throws MojoExecutionException, MojoFailureException {
        File pluginDir = null;
        if (relocate) {
            File tgtDir = relocateTarget;
            pluginDir = new File(relocateTarget, "../plugins");
            getLog().info("Flattening jars/target/jars and jars/target to jars");        
            try { // flatten
                mergeFiles(new File(tgtDir, "target/jars"), tgtDir);
                mergeFiles(new File(tgtDir, "target"), tgtDir);
            } catch (IOException e) {
                throw new MojoFailureException("Cannot move unpacked files: " + e.getMessage());
            }
            // cleanup
            FileUtils.deleteQuietly(new File(tgtDir, "target"));
        } 
        for (String cpFile : classpathFiles) {
            File src;
            File tgt;
            if (relocate) {
                // move from jars/target and jars/target/jars to jars
                src = new File(relocateTarget, cpFile);
                tgt = new File(pluginDir, cpFile);
                tgt.mkdirs();
                getLog().info("Rewriting classpath file " + src + " to " + tgt);
            } else {
                // just rewrite the same file by appending
                src = getOutputDir(cpFile);
                tgt = src;
                getLog().info("Rewriting classpath file " + src);
            }
            try {
                if (relocate) {
                    FileUtils.deleteQuietly(tgt);
                    FileUtils.moveFile(src, tgt);
                }
                FileInputStream fis = new FileInputStream(tgt);
                List<String> contents = IOUtils.readLines(fis, Charset.defaultCharset());
                fis.close();
                PrintStream out = new PrintStream(new FileOutputStream(tgt));
                String prefix = null;
                String mode = null;
                for (String line : contents) {
                    if (line.startsWith("#")) {
                        prefix = extractSuffix(BuildPluginClasspathMojo.KEY_PREFIX, line, null);
                        mode = extractSuffix(BuildPluginClasspathMojo.KEY_UNPACK_MODE, line, null);
                        out.println(line);
                    } else {
                        Tokenizer tokenizer = new Tokenizer(line);
                        prefix = fixPrefix(prefix, tokenizer);
                        if (relocate) {
                            processCpLineRelocation(mode, out, tokenizer, prefix, relocateTarget);
                        } else {
                            processCpLineNoRelocation(cpFile, line, out, tokenizer);
                        }
                        out.println();
                    } 
                }
                out.close();
                if (relocate) {
                    FileUtils.deleteQuietly(src);
                }
            } catch (IOException e) {
                throw new MojoFailureException("Cannot postprocess " + src + ": " + e.getMessage());
            }
        }
    }
    
    /**
     * Processes a classpath line while plugin relocation. 
     * 
     * @param mode the unpack mode, may be <b>null</b>
     * @param out the output stream for the rewritten classpath file
     * @param tokenizer the tokenizer
     * @param prefix the classpath token prefix
     * @param jarFolder the folder where to copy the jars to
     */
    private void processCpLineRelocation(String mode, PrintStream out, Tokenizer tokenizer, String prefix, 
        File jarFolder) {
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (tokenizer.win) {
                token = token.replace("target\\jars\\", "jars\\");
                token = token.replace("target\\", "jars\\");
            } else {
                token = token.replace("target/jars/", "jars/");
                token = token.replace("target/", "jars/");
            }
            if (null != mode && mode.equalsIgnoreCase("resolve")) {
                token = stripPrefix(prefix, token);
                token = resolve(token, jarFolder);
            }
            out.print(token);
            if (tokenizer.hasMoreTokens()) {
                out.print(tokenizer.sep);
            }
        }        
    }
    
    /**
     * Processes a classpath line without plugin relocation. 
     * 
     * @param name the name of the plugin
     * @param line the classpath line
     * @param out the output stream for the rewritten classpath file
     * @param tokenizer the tokenizer
     */
    private void processCpLineNoRelocation(String name, String line, PrintStream out, Tokenizer tokenizer) {
        Set<String> knownTokens = new HashSet<>();
        while (tokenizer.hasMoreTokens()) {
            knownTokens.add(tokenizer.nextToken()); 
        }
        out.print(line);
        int pos = name.indexOf("/");
        if (pos > 0) { // differentiating directory, if not-relocating
            name = name.substring(0, pos);
        }
        List<String> plAppends = pluginAppends.get(name);
        if (null != plAppends) {
            for (String cp: plAppends) {
                if (!knownTokens.contains(cp)) {
                    out.print(tokenizer.sep);
                    out.print(cp);
                    knownTokens.add(cp);
                }
            }
        }        
    }
   
    /**
     * Parses a classpath path back into a maven artifact.
     * 
     * @param path the path
     * @return the artifact
     */
    private DefaultArtifact parsePath(String path) {
        String groupId = "";
        String artifactId = path;
        String version = "";
        String type = "";
        String classifier = "";
    
        // split from backwards
        int pos = artifactId.lastIndexOf('.');
        if (pos > 0) { // extension
            type = artifactId.substring(pos + 1);
            artifactId = artifactId.substring(0, pos);
        }
        pos = path.lastIndexOf('-');
        if (pos > 0) { // may be version or classifier
            classifier = artifactId.substring(pos + 1);
            artifactId = artifactId.substring(0, pos);
            if (Character.isDigit(classifier.charAt(0))) { // is version, no classifier
                version = classifier;
                classifier = "";
            } else {
                pos = artifactId.lastIndexOf('-');
                if (pos > 0) {
                    if (!Character.isDigit(artifactId.charAt(pos + 1))) { // -SNAPSHOT
                        pos = artifactId.lastIndexOf('-', pos - 1);
                    }
                    if (pos > 0) {
                        version = artifactId.substring(pos + 1);
                        artifactId = artifactId.substring(0, pos);
                    }
                }
                if (classifier.equals("SNAPSHOT")) { // there might be more
                    classifier = "";
                    version += "-SNAPSHOT";
                }
            }
        }
        pos = artifactId.lastIndexOf('.');
        if (pos > 0) { // extension
            groupId = artifactId.substring(0, pos);
            artifactId = artifactId.substring(pos + 1);
        }
    
        return new DefaultArtifact(groupId, artifactId, classifier, type, version);            
    }

    /**
     * Translates a maven artifact to an Aether artifact.
     * 
     * @param artifact the maven artifact
     * @return the aether artifact
     */
    private DefaultArtifact translate(Artifact artifact) {
        return new DefaultArtifact(artifact.getGroupId(), artifact.getArtifactId(), artifact.getClassifier(), 
            artifact.getType(), artifact.getVersion());            
    }

    /**
     * Tries to resolve the path.
     * 
     * @param path the given classpath path
     * @param jarFolder the folder where to copy the jars to
     * @return {@code path} or the resolved path (in the local maven repo)
     */
    private String resolve(String path, File jarFolder) {
        String resolved = path;
        try {
            DefaultArtifact artifact = parsePath(path);
            File res = resolve(artifact);
            if (resolveAndCopy) {
                File tgt = new File(jarFolder, res.getName());
                if (!res.exists()) {
                    try {
                        FileUtils.copyFile(res, tgt);
                    } catch (IOException e) {
                        getLog().error("Cannot copy resolved artifact '" + artifact + "' from '" + res + "' to '" 
                            + tgt + "' - ignoring: " + e.getMessage());
                    }
                }
            } else { // leave it where it is
                resolved = res.getAbsolutePath();
            }
        } catch (ArtifactResolutionException e) {
            getLog().warn("Cannot resolve to artifact, keeping path: " + path);
        }
        return resolved;
    }

    /**
     * Resolves a list of artifacts to a composed path.
     * 
     * @param artifacts the artifacts, may be <b>null</b>
     * @return the path, may be <b>empty</b>
     */
    @SuppressWarnings("unused")
    private String resolve(List<Artifact> artifacts) {
        return null == artifacts ? "" : artifacts.stream()
            .map(a -> resolve(a))
            .filter(p -> p != null)
            .collect(Collectors.joining(":", ":", ""));
    }
    
    /**
     * Resolves a single artifact.
     * 
     * @param artifact the artifact
     * @return the resolved path, empty if not resolvable
     */
    private String resolve(Artifact artifact) {
        String result;
        try {
            result = resolve(translate(artifact)).getAbsolutePath();
        } catch (ArtifactResolutionException e) {
            getLog().warn("Cannot resolve to artifact, ignoring: " + artifact);
            result = "";
        }
        return result;
    }

    /**
     * Resolves an artifact via the repository system.
     * 
     * @param artifact the artifact to resolve
     * @return the resolved path
     * @throws ArtifactResolutionException if resolution fails
     */
    private File resolve(DefaultArtifact artifact) throws ArtifactResolutionException {
        ArtifactRequest request = new ArtifactRequest();
        request.setArtifact(artifact);
        request.setRepositories(remoteRepositories);
        ArtifactResult result = repoSystem.resolveArtifact(repoSession, request);
        return result.getArtifact().getFile();
    }

}
