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

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;

/**
 * Excludes all BaSyX classes in classpath from spring component scan.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ExcludeBasyxTypeFilter implements TypeFilter {

    @Override
    public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) 
        throws IOException {
        ClassMetadata classMetadata = metadataReader.getClassMetadata();
        String fullyQualifiedName = classMetadata.getClassName();
        boolean exclude = fullyQualifiedName.startsWith("org.eclipse.digitaltwin.basyx.");
        exclude &= !fullyQualifiedName.startsWith(BaSyxNames.BASYX_HTTP);
        exclude &= !fullyQualifiedName.startsWith("org.eclipse.digitaltwin.basyx.serialization");
        exclude &= !fullyQualifiedName.startsWith("org.eclipse.digitaltwin.basyx.deserialization");
        exclude &= !fullyQualifiedName.startsWith(BaSyxNames.AAS_REPOSITORY_CLIENT);
        exclude &= !fullyQualifiedName.startsWith(BaSyxNames.SM_SERVICE);
        exclude &= !fullyQualifiedName.startsWith("org.eclipse.digitaltwin.aas4j");
        exclude &= !fullyQualifiedName.startsWith("org.eclipse.digitaltwin.basyx.submodelrepository.client");
        AnnotationMetadata metadata = metadataReader.getAnnotationMetadata();
        if (metadata.hasAnnotation(SpringBootApplication.class.getName())) { // Registry, Repository
            exclude = true;
        }
        return exclude;
    }
    
}
