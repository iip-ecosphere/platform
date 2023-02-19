/**
 * ******************************************************************************
 * Copyright (c) {2021} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.services.spring;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.springframework.boot.loader.archive.Archive;
import org.springframework.boot.loader.archive.JarFileArchive;

import de.iip_ecosphere.platform.services.spring.DescriptorUtils.AccessibleJarLauncher;

/**
 * Simple program to test Spring-packaged jar files for contained classes.
 * 
 * @author Holger Eichelberger, SSE
 */
public class JarTest extends AccessibleJarLauncher {

    /**
     * Creates an instance of the test program.
     * 
     * @param archive the JAR archive to load
     */
    protected JarTest(Archive archive) {
        super(archive);
    }

    // checkstyle: stop exception type check

    /**
     * Executes the test program.
     * 
     * @param args command line arguments, first is the filename of the Spring JAR archive to load, second is the class 
     *     to search for
     * @throws Exception any kind of exception if loading archive or class failed
     */
    public static void main(String[] args) throws Exception {
        System.out.println("Spring boot archive tester");
        if (args.length < 2) {
            System.out.println("Help:");
            System.out.println(" - file name or Spring-packaged JAR file");
            System.out.println(" - Java class name to search for");
            System.out.println(" - optional folder with jars to load with");
        } else {
            String fileName = args[0];
            System.out.println("Loading Spring archive " + fileName);
            JarTest test = new JarTest(new JarFileArchive(new File(fileName)));
            System.out.println("Creating class loader... ");
            ClassLoader cl = test.createClassLoader();
            
            if (args.length >= 3) {
                // akin to ServiceEnvironment.Starter
                StringTokenizer t = new StringTokenizer(args[2].replace(";", ":"), ";");
                while (t.hasMoreTokens()) {
                    String jarFolder = t.nextToken();
                    System.out.println("Scanning " + jarFolder + " for shared libraries");
                    File jf = new File(jarFolder);
                    File[] files = jf.listFiles();
                    if (null != files) {
                        List<URL> urls = new ArrayList<>();
                        for (File f : files) {
                            if (f.getName().endsWith(".jar")) {
                                try {
                                    urls.add(f.toURI().toURL());
                                } catch (MalformedURLException e) {
                                    System.out.println("Cannot turn shared JAR file " + f + " to URL");
                                }
                            }
                        }
                        if (urls.size() > 0) {
                            System.out.println("Configuring shared libraries: " + urls);
                            cl = new URLClassLoader(urls.toArray(new URL[0]), cl);
                        }
                    }
                }
            }
            
            System.out.println("Loading class " + cl.loadClass(args[1]));
            System.out.println("Ok.");
        }
    }

    // checkstyle: resume exception type check

}
