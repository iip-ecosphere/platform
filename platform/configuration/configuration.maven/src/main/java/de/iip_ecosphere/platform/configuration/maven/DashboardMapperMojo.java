/**
 * ******************************************************************************
 * Copyright (c) {2026} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.configuration.maven;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;

import de.iip_ecosphere.platform.configuration.cfg.ConfigurationFactory;
import de.iip_ecosphere.platform.configuration.cfg.DashboardMapper;
import de.iip_ecosphere.platform.configuration.maven.DependencyResolver.Caller;
import de.iip_ecosphere.platform.support.plugins.CurrentClassloaderPluginSetupDescriptor;
import de.iip_ecosphere.platform.support.plugins.FolderClasspathPluginSetupDescriptor;
import de.iip_ecosphere.platform.support.plugins.PluginManager;
import de.iip_ecosphere.platform.tools.maven.python.AbstractLoggingMojo;
import de.oktoflow.platform.tools.lib.loader.Constants;
import de.oktoflow.platform.tools.lib.loader.LoaderIndex;

/**
 * Experimental dashboard mapper build process integration.
 * 
 * @author Holger Eichelberger, SSE
 */
@Mojo(name = "mapDashboard", defaultPhase = LifecyclePhase.PACKAGE)
public class DashboardMapperMojo extends AbstractLoggingMojo implements Caller {

    private static final boolean AS_PROCESS = true;
    
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;
    
    @Parameter(defaultValue = "${session}", required = true, readonly = true)
    private MavenSession session;
    
    @Parameter(defaultValue = "${plugin.groupId}", readonly = true)
    private String pluginGroupId;

    @Parameter(defaultValue = "${plugin.version}", readonly = true)
    private String pluginVersion;    

    @Component
    private RepositorySystem repoSystem;

    @Component
    private ProjectBuilder projectBuilder;
    
    @Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
    private RepositorySystemSession repoSession;    
    
    @Parameter(property = "configuration.aasPlugins", required = false, 
        defaultValue = "support.aas.basyx, support.aas.basyx2")
    private String plugins;

    @Parameter(property = "configuration.mainConfiguration", required = false, defaultValue = "PlatformConfiguration")
    private String mainConfiguration;

    @Parameter(property = "configuration.projectDirectory", required = false, defaultValue = "")
    private String projectDirectory;

    @Parameter(property = "configuration.metaModelDirectory", required = false, defaultValue = "target/easy")
    private String metaModelDirectory;
    
    @Parameter(property = "configuration.outputFile", required = false, defaultValue = "")
    private String outputFile;

    @Parameter(property = "configuration.pluginId", required = false, defaultValue = "")
    private String pluginId;

    @Parameter(property = "configuration.postUrl", required = false, defaultValue = "")
    private String postUrl;

    @Parameter(property = "configuration.cleanTemp", required = false, defaultValue = "false")
    private boolean cleanTemp;

    private DependencyResolver resolver;

    @Override
    public MavenSession getSession() {
        return session;
    }

    @Override
    public ProjectBuilder getProjectBuilder() {
        return projectBuilder;
    }

    @Override
    public RepositorySystem getRepoSystem() {
        return repoSystem;
    }
    
    @Override
    public MavenProject getProject() {
        return project;
    }    
    
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        resolver = new DependencyResolver(this);
        StringTokenizer plTokenizer = new StringTokenizer(plugins, ",");
        List<File> plugins = new ArrayList<>();
        while (plTokenizer.hasMoreTokens()) {
            String plugin = plTokenizer.nextToken().trim();
            if (plugin.length() > 0) {
                info("Resolving plugin " + plugin);
                DefaultArtifact art = new DefaultArtifact(pluginGroupId, plugin, pluginVersion, 
                    null, "zip", "plugin", null);
                Artifact resolved = resolver.resolve(art);
                boolean done = false;
                if (null != resolved) {
                    File pluginFile = resolved.getFile();
                    if (null != pluginFile) {
                        File ex = extractPlugin(plugin, pluginFile);
                        if (null != ex) {
                            plugins.add(ex);
                            registerPlugin(ex);
                        }
                        done = true;
                    }
                }
                if (!done) {
                    error("Cannot resolve plugin " + plugin);
                }
            }
        }
        if (!AS_PROCESS) {
            PluginManager.registerPlugin(CurrentClassloaderPluginSetupDescriptor.INSTANCE); // load the known remaining
        }
        MavenLogger.install(getLog());

        try {
            DashboardMapper mapper = ConfigurationFactory.createDashboardMapper();
            File procDir = toFile(projectDirectory, "");
            File mmDir = toFile(metaModelDirectory);
            File outFile = toFile(outputFile);
            if (AS_PROCESS) {
                mapper.mapConfigurationToDashboardAsProcess(mainConfiguration, procDir, 
                    mmDir, outFile, plugins, pluginId, postUrl);
            } else  {
                mapper.mapConfigurationToDashboard(mainConfiguration, procDir, 
                    mmDir, outFile, pluginId, postUrl);
            }            
        } catch (ExecutionException e) {

            throw new MojoExecutionException(e.getMessage());
        }
        if (cleanTemp) {
            plugins.forEach(p -> FileUtils.deleteQuietly(p));
        }
    }
    
    /**
     * Registers {@code folder} as plugin.
     * 
     * @param folder the folder
     */
    private static void registerPlugin(File folder) {
        if (!AS_PROCESS) {
            PluginManager.registerPlugin(new FolderClasspathPluginSetupDescriptor(folder));
        }
    }

    /**
     * Turns a string path into a file instance.
     * 
     * @param path the path, may be <b>null</b> or empty for none
     * @param dflt the default path if path is empty or <b>null</b>
     * @return the file instance, will be <b>null</b> if there was no path given
     */
    private File toFile(String path, String dflt) {
        if (path == null || path.length() == 0) {
            return new File(dflt);
        } else {
            return toFile(path);
        }
    }

    /**
     * Turns a string path into a file instance.
     * 
     * @param path the path, may be <b>null</b> or empty for none
     * @return the file instance, will be <b>null</b> if there was no path given
     */
    private File toFile(String path) {
        File result = null;
        if (path != null && path.length() > 0) {
            result = new File(path);
        }
        return result;
    }
    
    /**
     * Creates a temporary directory. 
     * 
     * @param name the name of the directory within the standard temporary folder 
     * @return the temporary directory
     */
    public static File createTmpDir(String name) {
        File result;
        String tmp = System.getProperty("java.io.tempdir");
        if (null == tmp) {
            try {
                File f = File.createTempFile("mvn", "tmp");
                result = f.getParentFile();
                f.delete();
            } catch (IOException e) {
                result = new File("tmp");
                result.mkdirs();
            }
        } else {
            result = new File(tmp);
        }
        if (null != name) {
            result = new File(result, name);
        }
        if (null != result && !result.exists()) {
            result.mkdirs();
        }
        return result;
    }
    
    /**
     * Extracts a plugin.
     * 
     * @param plugin the plugin name
     * @param pluginFile the plugin file as resolved by Maven
     * @return the extracted folder or <b>null</b> if failed
     */
    private File extractPlugin(String plugin, File pluginFile) {
        File result = null;
        File target = createTmpDir("okto.mvn-" + plugin);
        boolean exists = false;
        File cp = new File(target, "classpath");
        exists = cp.exists() && pluginFile.lastModified() > cp.lastModified();
        if (!exists) {
            try (ZipFile zf = new ZipFile(pluginFile)) {
                Enumeration<ZipEntry> entries = zf.getEntries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    File outFile = new File(target, entry.getName());
                    if (entry.isDirectory()) {
                        outFile.mkdirs();
                        continue;
                    } else {
                        outFile.getParentFile().mkdirs();
                    }
                    try (InputStream is = zf.getInputStream(entry);
                        OutputStream os = new FileOutputStream(outFile)) {
                        byte[] buffer = new byte[4096];
                        int len;
                        while ((len = is.read(buffer)) > 0) {
                            os.write(buffer, 0, len);
                        }
                    }
                }
            } catch (IOException e) {
                error("Cannot resolve plugin " + plugin + ": " + e.getMessage());
                FileUtils.deleteQuietly(target);
            }
            if (cp.isFile()) {
                rewriteCpFile(cp);                
            }
        }
        if (cp.isFile()) {
            result = cp;
        }
        return result;
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
     * Returns the base directory of the local repository.
     * 
     * @return the base directory
     */
    File getLocalRepoBaseDir() {
        return repoSession.getLocalRepository().getBasedir();
    }    
    
    /**
     * Rewrites the classpath files specified by {@code file}.
     * 
     * @param file the file to rewrite
     */
    private void rewriteCpFile(File file) {
        Map<String, Artifact> artifacts = new HashMap<>();
        Map<String, String> mapping = new HashMap<>();
        File fileMod = new File(file.getParentFile(), file.getName() + ".mod");
        String localRepo = getLocalRepoBaseDir().getAbsolutePath();
        PrintStream out = null;
        try {
            out = new PrintStream(new FileOutputStream(fileMod));
            for (String line: Files.readAllLines(file.toPath(), StandardCharsets.UTF_8)) {
                if (line.startsWith("#")) {
                    String arts = extractSuffix(Constants.KEY_ARTIFACTS, line, null);
                    if (arts != null) {
                        StringTokenizer artTokens = new StringTokenizer(arts, ",");
                        while (artTokens.hasMoreTokens()) {
                            String art = artTokens.nextToken();
                            String[] parts = art.split(":");
                            String fName = parts[0] + "." + parts[1] + "-" + parts[2] + ".jar";
                            artifacts.put(fName, resolver.resolve(new DefaultArtifact(parts[0], parts[1], 
                                parts[2], null, "jar", "", null)));
                        }
                    }
                    out.println(line);
                } else {
                    out.println(Constants.KEY_BASE_DIR + Constants.VAL_BASE_DIR_MVN);
                    StringTokenizer cpTokens = new StringTokenizer(line, ";:");
                    StringBuilder resolved = new StringBuilder();
                    while (cpTokens.hasMoreTokens()) {
                        String cp = cpTokens.nextToken();
                        String cpMod = cp.replace("\\", "/");
                        int pos = cpMod.lastIndexOf("/");
                        if (pos > 0) {
                            cpMod = cpMod.substring(pos + 1);
                        }
                        Artifact art = artifacts.get(cpMod);
                        if (null != art && art.getFile() != null) {
                            if (resolved.length() > 0) {
                                resolved.append(File.pathSeparatorChar);
                            }
                            String res = art.getFile().getAbsolutePath();
                            if (res.startsWith(localRepo)) {
                                res = res.substring(localRepo.length() + 1);
                            }
                            resolved.append(res);
                            mapping.put(cp, res);
                        }
                    }
                    out.println(resolved);
                }
            }
            out.close();
            file.delete();
            fileMod.renameTo(file);

            File idx = new File(file.getParentFile(), file.getName() + LoaderIndex.INDEX_SUFFIX);
            try {
                if (idx.exists()) {
                    LoaderIndex index = LoaderIndex.fromFile(idx);
                    index.substituteLocations(mapping);
                    LoaderIndex.toFile(index, idx);
                }
            } catch (IOException e1) {
                getLog().error("Cannot rewrite " + idx + ": " + e1.getMessage());
            }
        } catch (IOException e) {
            if (out != null) {
                out.close();
            }
            getLog().error("Cannot rewrite " + file + ": " + e.getMessage());
        }
    }

}
