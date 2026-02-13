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
import org.springframework.web.bind.annotation.RestController;

import static de.iip_ecosphere.platform.support.aas.basyx2.server.apps.common.BaSyxNames.*;

/**
 * Excludes specific BaSyX classes in classpath from spring component scan.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class ExcludeTypeFilter implements TypeFilter {
    
    private List<String> excludePackages = new ArrayList<>();
    
    /**
     * Initializes the default filters.
     */
    protected ExcludeTypeFilter() {
        excludePackages.add(AAS_REGISTRY);
        excludePackages.add(AAS_REPOSITORY);
        excludePackages.add(SM_REGISTRY);
        excludePackages.add(SM_REPOSITORY);
        excludePackages.add(CONCEPT_REPOSITORY);
        excludePackages.add(AUTODISCOVERY);
        excludePackages.add(AAS_ENVIRONMENT);
//        excludePackages.add("org.eclipse.digitaltwin.basyx.aasrepository.client.");
//        excludePackages.add("org.eclipse.digitaltwin.basyx.aasservice.");
//        excludePackages.add("org.eclipse.digitaltwin.basyx.client.");
//        excludePackages.add("org.eclipse.digitaltwin.basyx.aasxfileserver.");
//        excludePackages.add("org.eclipse.digitaltwin.basyx.core.");
//        excludePackages.add("org.eclipse.digitaltwin.basyx.serialization.");
//        excludePackages.add("org.eclipse.digitaltwin.basyx.deserialization.");
//        excludePackages.add("org.eclipse.digitaltwin.basyx.common.");
        excludePackages.add("org.eclipse.digitaltwin.basyx.authorization.");
//        excludePackages.add("org.eclipse.digitaltwin.basyx.http.");
//        excludePackages.add("org.eclipse.digitaltwin.basyx.mixins.");
//        excludePackages.add("org.eclipse.digitaltwin.basyx.operation.");
//        excludePackages.add("org.eclipse.digitaltwin.basyx.pagination.");
//        excludePackages.add("org.eclipse.digitaltwin.basyx.submodelservice.");
    }

    /**
     * Re-enables a package indicated as exclusion prefix.
     * 
     * @param pkg the package (prefix)
     */
    protected void removeExclude(String pkg) {
        excludePackages.remove(pkg);
    }

    @Override
    public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) 
        throws IOException {
        ClassMetadata classMetadata = metadataReader.getClassMetadata();
        String fullyQualifiedName = classMetadata.getClassName();
        AnnotationMetadata metadata = metadataReader.getAnnotationMetadata();
        boolean exclude = false;
        if (metadata.hasAnnotation(SpringBootApplication.class.getName()) // Registry, Repository
            || metadata.hasAnnotation(RestController.class.getName())) { // controllers in basyx.http
            exclude = true;
        }
        for (int e = 0; !exclude && e < excludePackages.size(); e++) {
            exclude |= fullyQualifiedName.startsWith(excludePackages.get(e));
        }
        return exclude;
    }
}
