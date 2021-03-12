/**
 * ******************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.transport;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import de.iip_ecosphere.platform.support.JarUtils;
import de.iip_ecosphere.platform.support.Server;

/**
 * A basic abstract server for testing/experiments.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractTestServer implements Server {

    private static File configDir;

    /**
     * Defines the server configuration directory.
     * 
     * @param directory the directory (may be <b>null</b>, leads to the given default value in 
     *     {@link #getConfigDir(File)})
     * @return {@code Directory}
     */
    public static final File setConfigDir(File directory) {
        configDir = directory;
        return directory;
    }

    /**
     * Returns the server configuration directory.
     * 
     * @param deflt the default value if {@link #setConfigDir(File)} was not called before
     * @return the server configuration directory
     */
    public static File getConfigDir(String deflt) {
        return getConfigDir(new File(deflt));
    }
    
    /**
     * Returns the server configuration directory.
     * 
     * @param deflt the default value if {@link #setConfigDir(File)} was not called before
     * @return the server configuration directory
     */
    public static File getConfigDir(File deflt) {
        return null == configDir ? deflt : configDir;
    }

    /**
     * Extracts a server configuration from a resource from the actual class loader.
     * 
     * @param location the location within the resource
     * @param dfltConfigDir the default configuration dir for {@link #getConfigDir(String)}
     * @throws IOException if something I/O related fails
     */
    public static void extractConfiguration(String location, String dfltConfigDir) throws IOException {
        extractConfiguration(AbstractTestServer.class.getClassLoader(), location, dfltConfigDir);
    }

    /**
     * Extracts a server configuration from a resource.
     * 
     * @param loader the class loader holding the resource
     * @param location the location within the resource
     * @param dfltConfigDir the default configuration dir for {@link #getConfigDir(String)}
     * @throws IOException if something I/O related fails
     */
    public static void extractConfiguration(ClassLoader loader, String location, String dfltConfigDir) 
        throws IOException {
        File cfgDir = getConfigDir(dfltConfigDir);
        cfgDir.mkdirs();
        InputStream in = loader.getResourceAsStream(location);
        if (null != in) {
            JarUtils.extractZip(in, cfgDir.toPath());
        } else {
            throw new IOException("Location '" + location + "' cannot be found");
        }
    }

    /**
     * Returns whether this class is/we ware running from a JAR file.
     * 
     * @return {@code true} for execution from JAR, {@code false}
     */
    public static boolean runsFromJar() {
        String intlName = AbstractTestServer.class.getName().replace('.', '/');
        String classJar = AbstractTestServer.class.getResource("/" + intlName + ".class").toString();
        return classJar.startsWith("jar:");
    }
    
}
