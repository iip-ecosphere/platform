/**
 * ******************************************************************************
 * Copyright (c) {2023} The original author or authors
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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.DirectoryScanner;

/**
 * Simplified copy-resources Mojo, intentionally without filtering as this causes dependency conflicts. The resources
 * here allow for specifying multiple source directories, the first existing one is selected as source.
 * 
 * @author Holger Eichelberger, SSE
 */
@Mojo(name = "copy-resources", threadSafe = true)
public class CopyResources extends AbstractMojo {

    @Parameter(required = true)
    private List<Resource> resources;

    @Parameter(required = true)
    private File outputDirectory;

    @Parameter(property = "maven.resources.skip", defaultValue = "false")
    private boolean skip;

    @Parameter(required = false)
    private boolean failOnError = true;

    @Parameter( defaultValue = "${project.basedir}", required = true, readonly = true )
    private File basedir;
    
    @SuppressWarnings("serial")
    public static class Resource extends org.apache.maven.model.Resource {
        
        private boolean prependResourceDirectories;
        private List<String> directories;

        /**
         * Sets {@code dir} as directory of this resource if {@code dir} exists.
         * 
         * @param dir the directory to set
         * @return {@code true} if {@code dir} exists and the directory has been changed
         */
        private boolean setDirectoryIfExists(String dir) {
            boolean done = false;
            if (new File(dir).exists()) {
                setDirectory(dir);
                done = true;
            }
            return done;
        }

    }
    
    /**
     * Returns the files to be consider for {@code resource}.
     * 
     * @param resource the resource
     * @return the files
     */
    public static List<File> getResourceFiles(Resource resource) {
        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir(resource.getDirectory());
    
        if (!resource.getIncludes().isEmpty()) {
            scanner.setIncludes(resource.getIncludes().toArray(new String[0]));
        }
    
        if (!resource.getExcludes().isEmpty()) {
            scanner.setExcludes(resource.getExcludes().toArray(new String[0]));
        }
    
        scanner.addDefaultExcludes();
        scanner.scan();
    
        return Arrays.stream(scanner.getIncludedFiles())
            .map(name -> new File(resource.getDirectory(), name))
            .toList();
    }    
    

    @Override
    public void execute() throws MojoExecutionException {
        boolean failed = false;
        if (skip) {
            getLog().info("Skipping the execution.");
            return;
        }
        int countFiles = 0;
        for (Resource r : resources) {
            String dir = r.getDirectory();
            boolean dirSet = false;
            if (r.prependResourceDirectories) {
                dirSet = r.setDirectoryIfExists(basedir + "/resources.ipr");
                if (!dirSet) {
                    dirSet = r.setDirectoryIfExists(basedir + "/resources");
                }
            }
            dirSet = r.setDirectoryIfExists(dir);
            if (!dirSet && r.directories != null && r.directories.size() > 0) {
                for (String d : r.directories) {
                    if (r.setDirectoryIfExists(d)) {
                        break;
                    }
                }
            }
            if (getLog().isDebugEnabled()) {
                getLog().info("Using target directory " + r.getDirectory());
            }
            List<File> files = getResourceFiles(r);
            if (r.isFiltering()) {
                getLog().warn("Filtering is currently not supported for files in directory " + dir);
            }
            outputDirectory.mkdirs();
            for (File f: files) {
                try {
                    File target = new File(outputDirectory, f.getName());
                    if (getLog().isDebugEnabled()) {
                        getLog().info("Copying " + f + " to " + target);
                    }
                    Files.copy(f.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    countFiles++;
                } catch (IOException e) {
                    getLog().error("Copying file " + f + " failed: " + e.getClass().getSimpleName() + " " 
                        + e.getMessage());
                    failed = true;
                }
            }
            r.setDirectory(dir);
        }
        if (failed && failOnError) {
            throw new MojoExecutionException("Copying resources failed. Reason(s) see above.");
        }
        getLog().info("Copied " + countFiles + " resource files");
    }
    
}
