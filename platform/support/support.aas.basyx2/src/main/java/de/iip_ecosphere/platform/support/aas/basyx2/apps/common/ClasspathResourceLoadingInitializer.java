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

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.regex.Pattern;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.UrlResource;

import de.iip_ecosphere.platform.support.logging.LoggerFactory;

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
        ClassLoader loader = getClass().getClassLoader(); // TODO plugin
        if (null != classpathPattern) {
            try {
                Enumeration<URL> e = loader.getResources("application.yml");
                while (e.hasMoreElements()) {
                    URL u = e.nextElement();
                    if (classpathPattern.matcher(u.toString()).matches()) {
                        UrlResource resource = new UrlResource(u);
                        YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
                        factory.setResources(resource);
                        factory.afterPropertiesSet();
                        applicationContext.getEnvironment().getPropertySources().addLast(
                            new PropertiesPropertySource("Initializer-injected Properties", factory.getObject()));
                        break;
                    }
                }
            } catch (IOException e) {
                LoggerFactory.getLogger(getClass()).error("Cannot load application.yaml: {}", e.getMessage());
            }
        }
    }
    
}
