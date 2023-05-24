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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import de.iip_ecosphere.platform.services.spring.DescriptorUtils;
import de.iip_ecosphere.platform.services.spring.SpringCloudArtifactDescriptor;
import de.iip_ecosphere.platform.services.spring.yaml.YamlArtifact;

/**
 * Allows testing service descriptors in Spring Service Jar artifacts.
 * 
 * @author Holger Eichelberger, SSE
 */
public class DescriptorTest {
    
    /**
     * Tests the descriptor.
     * 
     * @param args file names of Spring Service Jar artifact to test
     */
    public static void main(String... args) {
        if (args.length > 0) {
            for (String arg : args) {
                File f = new File(arg);
                if (!f.exists()) {
                    System.out.println("File " + f + " does not exist");    
                } else {
                    System.out.println("Testing service descriptor in " + f);
                    try {
                        YamlArtifact yamlArtifact = null;
                        if (f.getName().endsWith(".jar")) {
                            yamlArtifact = DescriptorUtils.readFromFile(f);
                        } else if (f.getName().endsWith(".yml")) {
                            try (FileInputStream fis = new FileInputStream(f)) {
                                yamlArtifact = YamlArtifact.readFromYaml(fis);
                            } catch (IOException e) {
                                System.out.println("Error:\n" + e.getMessage());
                            }
                        }
                        if (null != yamlArtifact) {
                            System.out.println("Testing YAML artifact in " + f);
                            SpringCloudArtifactDescriptor.createInstance(yamlArtifact, f.toURI(), null);
                        }
                    } catch (ExecutionException e) {
                        System.out.println("Error:\n" + e.getMessage());
                    }
                }
            }
        } else {
            System.out.println("No file given.");
        }
    }

}
