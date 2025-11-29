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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.plugins.dependency.fromConfiguration.ArtifactItem;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.shared.model.fileset.FileSet;
import org.codehaus.plexus.components.io.filemappers.FileMapper;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactResolutionException;

/**
 * Extended unpack Mojo for plugins.
 * 
 * @author Holger Eichelberger, SSE
 */
@Mojo(name = "unpack-plugins", defaultPhase = LifecyclePhase.TEST_COMPILE, requiresProject = false, threadSafe = true, 
    requiresDependencyResolution = ResolutionScope.RUNTIME, requiresDependencyCollection = ResolutionScope.RUNTIME)
public class UnpackPluginMojo extends CleaningUnpackMojo {

    private static final String NAME_CLASSPATH_FILE = "classpath";
    
    @Parameter(property = "unpack.plugins", required = false)
    private List<PluginItem> plugins;
    
    @Parameter(property = "unpack.version", required = false, defaultValue = "")
    private String version;

    @Parameter(property = "unpack.relocate", required = false, defaultValue = "false")
    private boolean relocate;

    @Parameter(property = "unpack.always", required = false, defaultValue = "false")
    private boolean always;

    @Parameter(property = "unpack.relocateTarget", required = false, defaultValue = "jars")
    private File relocateTarget;

    @Parameter(property = "unpack.forceResolve", required = false, defaultValue = "false")
    private boolean forceResolve;

    @Parameter(property = "unpack.resolveAndCopy", required = false, defaultValue = "false")
    private boolean resolveAndCopy;

    @Parameter( defaultValue = "${project.build.directory}", readonly = true )
    private File targetDirectory;

    @Parameter( property = "mdep.writeResolved", defaultValue = "false")
    private boolean writeResolved;

    @Parameter( property = "mdep.resolvedFile", defaultValue = "*")
    private String resolvedFile;

    private List<ClasspathFile> classpathFiles = new ArrayList<>();
    
    @Parameter( defaultValue = "${project.remoteArtifactRepositories}", readonly = true, required = true )
    private List<ArtifactRepository> remoteArtifactRepositories;

    @Parameter(defaultValue = "${project.remoteProjectRepositories}", readonly = true)
    private List<RemoteRepository> remoteRepositories;

    @Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
    private RepositorySystemSession repoSession;

    @Parameter( defaultValue = "${session}", readonly = true, required = true )
    private MavenSession session;

    @Component
    private RepositorySystem repoSystem;
    
    private Map<String, List<String>> pluginAppends = new HashMap<>();
    private Resolver resolver;
    
    /**
     * Represents a plugin, an extended artifact.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class PluginItem extends ArtifactItem {
        
        @Parameter(required = false) 
        private List<String> appends;
        private boolean asTest;
        private String setupDescriptor;
        
        /**
         * Returns whether this item has appends.
         * 
         * @return {@code true} for appends, {@code false} else
         */
        private boolean hasAppends() {
            return null != appends && !appends.isEmpty();
        }

        /**
         * Returns whether this item has a setupDescriptor.
         * 
         * @return {@code true} for setupDescriptor, {@code false} else
         */
        private boolean hasSetupDescriptor() {
            return null != setupDescriptor && setupDescriptor.length() > 0;
        }

    }
    
    /**
     * Stores classpath file names with additional information for moving in the file system.
     */
    private static class ClasspathFile {
        
        private String name;
        private String origPath;
        private String pathSuffix;
        
        /**
         * Classpath file without need for moving.
         * 
         * @param name the name/path of the file
         */
        private ClasspathFile(String name) {
            this.name = name;
        }

        /**
         * Classpath file with need for moving.
         * 
         * @param name the name/path of the file
         * @param origPath the original path the file is located in
         * @param pathSuffix path addition causing the move
         */
        private ClasspathFile(String name, String origPath, String pathSuffix) {
            this.name = name;
            this.origPath = origPath;
            this.pathSuffix = pathSuffix;
        }
        
        /**
         * The name of the classpath file.
         * 
         * @return the name, original before move/processing, modified after
         */
        private String getName() {
            return name;
        }
        
        /**
         * Preprocesses the file in {@code targetDir}, i.e., moves it if needed. May affect {@link #getName()}.
         * 
         * @param targetDir the target directory where the file/paths are located within
         * @throws MojoFailureException if moving fails
         */
        private void preprocess(File targetDir) throws MojoFailureException {
            if (null != origPath && null != pathSuffix) {
                File orig = new File(targetDir, origPath);
                File target = new File(targetDir, origPath + pathSuffix);
                if (orig.exists() && !target.exists()) { // already renamed, not cleaned
                    try {
                        FileUtils.moveDirectory(new File(targetDir, origPath), 
                            new File(targetDir, origPath + pathSuffix));
                    } catch (IOException e) {
                        throw new MojoFailureException("Cannot move unpacked files: " + e.getMessage());
                    }
                }
                int pos = name.lastIndexOf("/"); // or check origPath as prefix
                if (pos > 0) {
                    name = name.substring(0, pos) + pathSuffix + name.substring(pos);
                }
            }
        }
        
    }
    
    /**
     * Used for relocating classpath files, i.e., to rename them with their plugin short (artifactId) name.
     * 
     * @author Holger Eichelberger, SSE
     */
    private class RelocatingFileMapper implements FileMapper {

        private String targetName;
        private String pathSuffix;
        private boolean collectIfNotRelocate;
        
        /**
         * Creates a relocating file mapper.
         * 
         * @param targetName the target name to map to
         * @param pathSuffix optional path suffix for moving the target directory; underlying maven does not seem to 
         * consider a changed target path in here
         * @param collectIfNotRelocate collect the file names if we are not relocating
         */
        private RelocatingFileMapper(String targetName, String pathSuffix, boolean collectIfNotRelocate) {
            this.targetName = targetName;
            this.pathSuffix = pathSuffix;
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
                    classpathFiles.add(new ClasspathFile(name));
                } else { // else just collect with relative path
                    if (collectIfNotRelocate) {
                        classpathFiles.add(new ClasspathFile(targetName + "/" + name, targetName, 
                            pathSuffix));
                    }
                }
            }
            return name;
        }
        
    }
    
    
    @Override
    public void doExecute() throws MojoExecutionException, MojoFailureException {
        resolver = new Resolver(repoSystem, repoSession, remoteRepositories, getLog());
        File targetDir = getOutputDir(null);
        setForceCleanup(true);
        FileSet cleanup = new FileSet();
        cleanup.setDirectory(targetDir.toString());
        setCleanup(cleanup);

        if (!relocate) {
            if (!always) {
                // figure out whether we are in development mode, i.e., in git workspace
                File skipIfExists = new File("../../support/support"); // building for arbitrary component
                if (!skipIfExists.exists()) {
                    skipIfExists = new File("../support"); // building for a support component
                }
                if (skipIfExists.exists()) {
                    setSkipIfExists(skipIfExists);
                }
            }
        } else {
            relocateTarget.mkdirs();
            setForce(true);
        }

        if (plugins != null && plugins.size() > 0) {
            List<ArtifactItem> artifactItems = new ArrayList<>();
            for (PluginItem pl : plugins) {
                ArtifactItem item = toArtifactItem(pl);
                String artId = item.getArtifactId();
                getLog().info("Configuring plugin '" + artId + "' -> " + item.getOutputDirectory());
                item.setFileMappers(new FileMapper[] {
                    new RelocatingFileMapper(artId, "-" + getActualVersion(pl), true)}); // pl.hasAppends()
                artifactItems.add(item);
            }
            setArtifactItems(artifactItems);
        }

        super.doExecute();
        
        handleSetupDescriptors();
        if (skipIfExists() == null || !skipIfExists().exists()) {
            handleAppends();
        } else {
            if (plugins.stream().anyMatch(p -> p.hasAppends())) {
                getLog().info("Running in shortcut local test mode, cannot handle appends");
            }
        }
        relocate();
        storeResolved();
    }
    
    /**
     * Returns the actual (overridden) version of the given plugin item.
     * 
     * @param pl the plugin item
     * @return the version
     */
    private String getActualVersion(PluginItem pl) {
        return StringUtils.isBlank(version) ? pl.getVersion() : version;
    }
    
    /**
     * Turns a {@link PluginItem} to a pre-configured {@link ArtifactItem} passed on for resolution/download.
     * 
     * @param pl the plugin item to convert
     * @return the converted artifact item
     */
    private ArtifactItem toArtifactItem(PluginItem pl) {
        ArtifactItem item = new ArtifactItem();
        String artId = pl.getArtifactId();
        if (StringUtils.isBlank(pl.getGroupId())) {
            item.setGroupId("de.iip-ecosphere.platform");
        }
        item.setArtifactId(artId);
        item.setVersion(getActualVersion(pl));
        item.setType("zip");
        item.setClassifier("plugin" + (pl.asTest ? "-test" : ""));
        item.setOverWrite(String.valueOf(true));
        item.setOutputDirectory(getOutputDir(artId));
        item.setDestFileName(artId + ".zip");
        return item;
    }
    
    /**
     * Stores the resolved dependencies if enabled.
     * 
     * @throws MojoExecutionException if obtaining the dependencies fails
     */
    private void storeResolved() throws MojoExecutionException {
        if (writeResolved && plugins != null && plugins.size() > 0) {
            File file = null;
            if (resolvedFile == null || resolvedFile.equals("*")) {
                file = new File(targetDirectory, "classes/resolved");
            } else if (resolvedFile.length() > 0) {
                file = new File(resolvedFile);
            }
            if (file != null) {
                Resolver resolver = new Resolver(repoSystem, repoSession, remoteRepositories, getLog());
                try (PrintStream out = new PrintStream(new FileOutputStream(file))) {
                    out.println("[");
                    boolean first = true;
                    for (PluginItem p : plugins) {
                        String urlPath = resolver.resolveToUrl(toArtifactItem(p));
                        if (null != urlPath && urlPath.length() > 0) {
                            if (!first) {
                                out.println(",");
                            }
                            out.print("  {\"url\":\"" + urlPath + "\", \"name\":\"" + p.getArtifactId() 
                                + "-" + getActualVersion(p) + "\"}");
                            first = false;
                        }
                    }
                    if (!first) {
                        out.println();
                    }
                    out.println("]");
                    getLog().info("Wrote resolution file " + file);
                } catch (IOException e) {
                    getLog().error("While writing resolution file " + file + ": " + e.getMessage());
                }
            } else {
                getLog().info("Skipping resolution file as disabled");
            }
        }
    }
    
    /**
     * Returns the output directory for a plugin/classpath file name.
     * 
     * @param name the name, may be <b>null</b> or empty for none/base dir/independent of name
     * @return the output directory
     */
    private File getOutputDir(String name) {
        String oktoSuffix = "";
        if (name != null && name.length() > 0) {
            oktoSuffix = "/" + name;
        }
        return relocate ? relocateTarget : new File(targetDirectory, "oktoPlugins" + oktoSuffix);
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
            if (pl.hasAppends()) {
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
                                    if (!relocate) {
                                        token = "../" + name + "/" + token; // relative path for other plugins
                                    }
                                    result.add(token);
                                }
                            }
                        }
                    } catch (IOException e) {
                        getLog().error("Cannot locate append plugin '" + name + "' - ignoring: " + e.getMessage());
                    }
                }
                pluginAppends.put(pl.getArtifactId(), result);
            }
        }
    }

    /**
     * Handles explicit setup descriptors by rewriting the classpath files.
     */
    private void handleSetupDescriptors() {
        for (PluginItem pl : plugins) {
            if (pl.hasSetupDescriptor()) {
                String name = pl.getArtifactId();
                File cpFile = getCpFile(name);
                List<String> contents = null;
                try (FileInputStream fis = new FileInputStream(cpFile)) {
                    contents = IOUtils.readLines(fis, Charset.defaultCharset());
                    for (int l = 0; l < contents.size(); l++) {
                        String line = contents.get(l);
                        if (line.startsWith(BuildPluginClasspathMojo.KEY_SETUP_DESCRIPTOR)) {
                            contents.set(l, BuildPluginClasspathMojo.KEY_SETUP_DESCRIPTOR + pl.setupDescriptor);
                        }
                    }
                } catch (IOException e) {
                    getLog().error("Cannot read plugin '" + name + "' - ignoring: " + e.getMessage());
                }
                if (null != contents) {
                    try (FileOutputStream fos = new FileOutputStream(cpFile)) {
                        // Linux LF by default for plugin classpaths
                        IOUtils.writeLines(contents, "\n", fos, Charset.defaultCharset());
                    } catch (IOException e) {
                        getLog().error("Cannot read plugin '" + name + "' - ignoring: " + e.getMessage());
                    }
                }
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
            if (tokenizer.countTokens() <= 1) {
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
        int seqNr = 1;
        for (ClasspathFile cf : classpathFiles) {
            cf.preprocess(getOutputDir(null));
            String cpFile = cf.getName();
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
                out.println(BuildPluginClasspathMojo.KEY_SEQUENCE_NR + seqNr);
                String prefix = null;
                String mode = null;
                for (String line : contents) {
                    if (line.startsWith("#")) {
                        prefix = extractSuffix(BuildPluginClasspathMojo.KEY_PREFIX, line, prefix);
                        mode = extractSuffix(BuildPluginClasspathMojo.KEY_UNPACK_MODE, line, mode);
                        out.println(line);
                    } else {
                        Tokenizer tokenizer = new Tokenizer(line);
                        prefix = fixPrefix(prefix, tokenizer);
                        if (relocate) {
                            processCpLineRelocation(cpFile, mode, out, tokenizer, prefix, relocateTarget);
                        } else {
                            processCpLineNoRelocation(cpFile, mode, line, out, tokenizer, prefix);
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
            seqNr++;
        }
    }
    
    // checkstyle: stop parameter number check
    
    /**
     * Processes a classpath line while plugin relocation. 
     * 
     * @param name the name of the plugin
     * @param mode the unpack mode, may be <b>null</b>
     * @param out the output stream for the rewritten classpath file
     * @param tokenizer the tokenizer
     * @param prefix the classpath token prefix
     * @param jarFolder the folder where to copy the jars to
     */
    private void processCpLineRelocation(String name, String mode, PrintStream out, Tokenizer tokenizer, String prefix, 
        File jarFolder) {
        List<String> tokens = new ArrayList<>();
        while (tokenizer.hasMoreTokens()) {
            tokens.add(tokenizer.nextToken());
        }
        Set<String> knownTokens = new HashSet<>(tokens);
        List<String> plAppends = pluginAppends.get(name);
        if (null != plAppends) {
            for (String cp: plAppends) {
                if (!knownTokens.contains(cp)) {
                    tokens.add(cp);
                }
            }            
        }
        Iterator<String> tokenIter = tokens.iterator();
        while (tokenIter.hasNext()) {
            String token = tokenIter.next();
            token = rewriteToken(token, mode, prefix, jarFolder, tokenizer);
            out.print(token);
            if (tokenIter.hasNext()) {
                out.print(tokenizer.sep);
            }
        }
    }

    /**
     * Processes a classpath line without plugin relocation. 
     * 
     * @param name the name of the plugin
     * @param mode the unpack mode, may be <b>null</b>
     * @param line the classpath line
     * @param out the output stream for the rewritten classpath file
     * @param tokenizer the tokenizer
     * @param prefix the classpath token prefix (for {@link #forceResolve})
     */
    private void processCpLineNoRelocation(String name, String mode, String line, PrintStream out, Tokenizer tokenizer, 
        String prefix) {
        List<String> tokens = new ArrayList<>();
        Set<String> knownTokens = new HashSet<>();
        while (tokenizer.hasMoreTokens()) {
            String tok = tokenizer.nextToken();
            knownTokens.add(tok); 
            tokens.add(tok);
        }
        if (isModeResolve(mode)) {
            handleAppends(name, knownTokens, cp -> tokens.add(cp));
            List<String> processedTokens = tokens
                .stream()
                .map(token -> rewriteToken(token, mode, prefix, null, tokenizer))
                .collect(Collectors.toList());
            out.print(String.join(tokenizer.sep, processedTokens));
        } else {
            out.print(line);
            int pos = name.indexOf("/");
            if (pos > 0) { // differentiating directory, if not-relocating
                name = name.substring(0, pos);
            }
            handleAppends(name, knownTokens, cp -> {
                out.print(tokenizer.sep);
                out.print(cp);
            });
        }
    }

    // checkstyle: resume parameter number check

    /**
     * Rewrites a classpath token.
     * 
     * @param token the token
     * @param mode the unpack mode, may be <b>null</b>
     * @param prefix the classpath token prefix (for {@link #forceResolve})
     * @param jarFolder the folder where to copy the jars to (considerd only for {@link #resolveAndCopy}, 
     *     may be <b>null</b> considered as if {@link #resolveAndCopy} is {@code false})
     * @param tokenizer the tokenizer
     * @return {@code token} or the rewritten token
     */
    private String rewriteToken(String token, String mode, String prefix, File jarFolder, Tokenizer tokenizer) {
        if (tokenizer.win) {
            token = token.replace("target\\jars\\", "jars\\");
            token = token.replace("target\\", "jars\\");
        } else {
            token = token.replace("target/jars/", "jars/");
            token = token.replace("target/", "jars/");
        }
        if (isModeResolve(mode)) {
            token = stripPrefix(prefix, token);
            token = resolve(token, jarFolder);
        }
        return token;
    }
    
    /**
     * Returns whether we are in resolve mode, i.e., either {@code mode} indicates "resolve" or {@link #forceResolve}.
     * 
     * @param mode the unpack mode, may be <b>null</b>
     * @return {@code true} for resolve, {@code false} else
     */
    private boolean isModeResolve(String mode) {
        return (null != mode && mode.equalsIgnoreCase("resolve")) || forceResolve;
    }

    /**
     * Handles appends, if specified, by passing elements that are not yet in {@code knownTokens} to {@code handler}.
     * 
     * @param name the name of the plugin
     * @param knownTokens the tokens known so far (classpath entries), modified as a side effect
     * @param handler the handler to be called
     */
    private void handleAppends(String name, Set<String> knownTokens, Consumer<String> handler) {
        List<String> plAppends = pluginAppends.get(name);
        if (null != plAppends) {
            for (String cp: plAppends) {
                if (!knownTokens.contains(cp)) {
                    handler.accept(cp);
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
        String artifactId = "";
        String version = "";
        String type = "";
        String classifier = "";
        // there might be a file path before
        int pos = path.lastIndexOf('/');
        if (pos > 0) {
            path = path.substring(pos + 1);
        }
        artifactId = path;
        // split from backwards
        pos = artifactId.lastIndexOf('.');
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
        pos = findArtifactId(artifactId);
        if (pos > 0) { // extension
            groupId = artifactId.substring(0, pos);
            artifactId = artifactId.substring(pos + 1);
        }
        return new DefaultArtifact(groupId, artifactId, classifier, type, version);            
    }
    
    /**
     * Returns the position of the "." after the groupId in {@code name}.
     * 
     * @param name the name to analyze
     * @return the position
     */
    private int findArtifactId(String name) {
        // we do not follow the "convention" of no dot in artifactId
        final String iipPrefix = "de.iip-ecosphere.platform.";
        if (name.startsWith(iipPrefix)) {
            return iipPrefix.length() - 1;
        } else {
            return name.lastIndexOf('.');
        }
    }

    /**
     * Tries to resolve the path.
     * 
     * @param path the given classpath path
     * @param jarFolder the folder where to copy the jars to (considerd only for {@link #resolveAndCopy}, 
     *     may be <b>null</b> considered as if {@link #resolveAndCopy} is {@code false})
     * @return {@code path} or the resolved path (in the local maven repo)
     */
    private String resolve(String path, File jarFolder) {
        String resolved = path;
        try {
            DefaultArtifact artifact = parsePath(path);
            File res = resolver.resolve(artifact);
            if (resolveAndCopy && jarFolder != null) {
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

    @Override
    public ProjectBuildingRequest newResolveArtifactProjectBuildingRequest() {
        // no artifact resolution so far
        ProjectBuildingRequest buildingRequest =
            new DefaultProjectBuildingRequest(session.getProjectBuildingRequest());
        buildingRequest.setRemoteRepositories(remoteArtifactRepositories);
        return buildingRequest;
    }

}
