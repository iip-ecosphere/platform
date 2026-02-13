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

package de.iip_ecosphere.platform.support.aas.basyx2.server.apps.common;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;

/**
 * Includes specific BaSyX classes in classpath for spring component scan.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class InclusionBasedTypeFilter implements TypeFilter {
    
    private List<String> includePackages = new ArrayList<>();
    private List<String> excludePackages = new ArrayList<>();
    
    /**
     * Initializes the default filters.
     */
    protected InclusionBasedTypeFilter() {
        addInclusion(BaSyxNames.PACKAGE_PLUGIN_BASYX_SERVER_SECURITY);
    }

    /**
     * Enables a package indicated as inclusion prefix.
     * 
     * @param pkg the package (prefix)
     */
    protected void addInclusion(String pkg) {
        includePackages.add(pkg);
    }

    /**
     * Enables a package indicated as inclusion prefix.
     * 
     * @param pkg the package (prefix)
     */
    protected void addExclusion(String pkg) {
        excludePackages.add(pkg);
    }

    @Override
    public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) 
        throws IOException {
        ClassMetadata classMetadata = metadataReader.getClassMetadata();
        String fullyQualifiedName = classMetadata.getClassName();
        AnnotationMetadata metadata = metadataReader.getAnnotationMetadata();
        boolean exclude = true;
        for (int e = 0; exclude && e < includePackages.size(); e++) {
            exclude &= !fullyQualifiedName.startsWith(includePackages.get(e));
        }
        if (!exclude) {
            if (metadata.hasAnnotation(SpringBootApplication.class.getName())) { // Registry, Repository
                exclude = true;
            } else {
                for (int e = 0; !exclude && e < excludePackages.size(); e++) {
                    exclude |= fullyQualifiedName.startsWith(excludePackages.get(e));
                }
            }
        }
        return exclude;
    }
}
