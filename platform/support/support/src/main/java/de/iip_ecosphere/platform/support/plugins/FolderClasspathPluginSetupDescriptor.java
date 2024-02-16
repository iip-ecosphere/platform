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
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.io.IOUtils;
import org.slf4j.LoggerFactory;

/**
 * Default plugin setup descriptor based based on loading from a project folder containing jars and the 
 * classpath in "target/classes/classpath".
 * 
 * @author Holger Eichelberger, SSE
 */
public class FolderClasspathPluginSetupDescriptor extends URLPluginSetupDescriptor {

    /**
     * Creates an instance based on project folder containing jars and the classpath in "target/classes/classpath".
     * 
     * @param folder the basis folder
     */
    public FolderClasspathPluginSetupDescriptor(File folder) {
        super(loadClasspathSafe(folder));
    }

    /**
     * Loads a resource in classpath format and returns the specified classpath entries as URLs. Logs errors and 
     * exceptions.
     * 
     * @param folder the basis folder
     * @return the URLs, may be empty
     */
    public static URL[] loadClasspathSafe(File folder) {
        URL[] result = null;
        try (InputStream in = new FileInputStream(new File(folder, "target/classes/classpath"))) {
            List<File> entries = new ArrayList<File>();
            String contents = IOUtils.toString(in, Charset.defaultCharset());
            StringTokenizer tokenizer = new StringTokenizer(contents, ":;");
            while (tokenizer.hasMoreTokens()) {
                entries.add(new File(folder, tokenizer.nextToken()));
            }
            result = toURLSafe(entries.toArray(new File[entries.size()]));
        } catch (IOException e) {
            LoggerFactory.getLogger(URLPluginSetupDescriptor.class).error(
                "While classpath from '{}': {} Ignoring.", folder, e.getMessage());
        }
        if (null == result) {
            result = new URL[0];
        }
        return result;
    }

}
