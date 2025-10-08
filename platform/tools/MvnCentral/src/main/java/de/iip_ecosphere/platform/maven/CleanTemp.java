
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

package de.iip_ecosphere.platform.maven;

import java.io.File;

import de.iip_ecosphere.platform.maven.MavenUtils.CleanupStatistics;
import de.iip_ecosphere.platform.support.FileUtils;

/**
 * Deletes typical temp-leftovers.
 * 
 * @author Holger Eichelberger, SSE
 */
public class CleanTemp {
    
    /**
     * Cleans temp.
     * 
     * @param args ignored
     */
    public static void main(String[] args) {
        CleanupStatistics statistics = new CleanupStatistics();
        File tmp = new File(System.getProperty("java.io.tmpdir"));
        File[] files = tmp.listFiles();
        if (null != files) {
            for (File f : files) {
                String name = f.getName();
                boolean delete = name.startsWith("tomcat.");
                delete |= name.startsWith("tomcat-docbase");
                delete |= name.startsWith("java-server");
                delete |= name.startsWith("basyx-temp");
                if (delete) {
                    System.out.println("Deleting " + f);
                    statistics.clearedFile(f.length());
                    FileUtils.deleteQuietly(f);
                }
            }
        }
        System.out.println("Cleaned up " + statistics.getFileCount() + " files/dirs");
    }

}
