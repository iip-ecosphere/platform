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

package de.iip_ecosphere.platform.support.aas.basyx2.apps.security;

import org.eclipse.digitaltwin.basyx.submodelrepository.feature.authorization.SubmodelTargetInformation;

import de.iip_ecosphere.platform.support.CollectionUtils;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor.RbacRule;

/**
 * A target information handler for submodel target information.
 * 
 * @author Holger Eichelberger, SSE
 */
class SubmodelInfoCreator extends TargetInfoHandler<SubmodelTargetInformation> {

    static final SubmodelInfoCreator INSTANCE = new SubmodelInfoCreator();
    
    /**
     * Creates an instance.
     */
    private SubmodelInfoCreator() {
        super(SubmodelTargetInformation.class);
    }
    
    @Override
    public SubmodelTargetInformation create(RbacRule rule) {
        return new SubmodelTargetInformation(CollectionUtils.toList(rule.getElement()), toPaths(rule));
    }

    @Override
    public SubmodelTargetInformation join(SubmodelTargetInformation t1, SubmodelTargetInformation t2) {
        t1.getSubmodelIds().addAll(t2.getSubmodelIds());
        t1.getSubmodelElementIdShortPaths().addAll(t2.getSubmodelElementIdShortPaths());
        return t1;
    }
    
}