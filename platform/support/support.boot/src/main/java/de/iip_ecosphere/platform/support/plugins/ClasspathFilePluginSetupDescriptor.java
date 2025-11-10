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

package de.iip_ecosphere.platform.support.plugins;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Loads a plugin from a given classpath file assuming that the referenced relative URLs are in a sibling folder.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ClasspathFilePluginSetupDescriptor extends URLPluginSetupDescriptor {
    
    private File cpFile;
    private boolean descriptorOnly;

    /**
     * Creates the descriptor for the given classpath file.
     * 
     * @param cpFile the classpath file
     */
    public ClasspathFilePluginSetupDescriptor(File cpFile) {
        this(cpFile, false);
    }
    
    /**
     * Creates the descriptor for the given classpath file.
     * 
     * @param cpFile the classpath file
     * @param descriptorOnly load the descriptor JARs only or the full thing
     */
    public ClasspathFilePluginSetupDescriptor(File cpFile, boolean descriptorOnly) {
        super(FolderClasspathPluginSetupDescriptor.loadClasspathFileSafe(cpFile, descriptorOnly));
        this.cpFile = cpFile;
        this.descriptorOnly = descriptorOnly;
    }

    @Override
    public File getInstallDir() {
        return cpFile.getParentFile();
    }

    @Override
    protected ClassLoader createClassLoader(URL[] urls, ClassLoader parent) {
        return descriptorOnly ? new URLClassLoader(urls, parent) : super.createClassLoader(urls, parent);
    }

}
