/**
 * ******************************************************************************
 * Copyright (c) {2022} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.services.environment.spring;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import de.iip_ecosphere.platform.support.FileUtils;
import de.iip_ecosphere.platform.support.JarUtils;

/**
 * Lists available javax.el APIs.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ListElApis {

    /**
     * Lists factory classes and all JSL service files to determine overlaps.
     * 
     * @param args ignored
     */
    public static void main(String[] args) {
        File file = new File("target/jars");
        FileUtils.listFiles(file, 
            f -> f.isDirectory() || f.getName().endsWith(".jar"), 
            f -> {
                try {
                    JarUtils.listFiles(new FileInputStream(f), 
                        e -> !e.isDirectory() && (
                            (e.getName().contains("/services/") && !e.getName().endsWith(".class")) // service spec 
                                || e.getName().contains("ExpressionFactory")), // factory class
                        e -> System.out.println(f + ": " + e));
                } catch (IOException e) {
                    // ignore
                }
            }
        );
    }

}
