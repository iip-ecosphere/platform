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

package de.iip_ecosphere.platform.support.aas.basyx2.apps.common;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.ClassPathResource;

/**
 * Application initializer additionally loading properties from BaSyX.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class ClasspathResourceLoadingInitializer 
    implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    
    private Pattern classpathPattern;

    /**
     * Creates an instance with a given file name pattern.
     * 
     * @param cpPattern the classpath file (Java RegEx) pattern
     */
    public ClasspathResourceLoadingInitializer(String cpPattern) {
        classpathPattern = Pattern.compile(cpPattern);
    }
    
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        String[] files = System.getProperty("java.class.path").split(File.pathSeparator);
        Optional<String> file = Arrays.stream(files).filter(f -> classpathPattern.matcher(f).matches()).findFirst();
        if (file.isPresent()) {
            try {
                // just try to get the resource within file
                URL jarUrl = new File(file.get()).toURI().toURL();
                URLClassLoader jarLoader = URLClassLoader.newInstance(new URL[]{jarUrl}, 
                    ClassLoader.getPlatformClassLoader());
                ClassPathResource sampleResource = new ClassPathResource("application.yml", jarLoader);

                YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
                factory.setResources(sampleResource);
                factory.afterPropertiesSet();
                applicationContext.getEnvironment().getPropertySources().addLast(
                    new PropertiesPropertySource("Initializer-injected Properties", factory.getObject()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
