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

package de.iip_ecosphere.platform.support.aas.basyx2.server.apps.security;

import org.eclipse.digitaltwin.basyx.aasrepository.feature.authorization.AasTargetInformation;

import de.iip_ecosphere.platform.support.CollectionUtils;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor.RbacRule;

/**
 * A target information handler for AAS target information.
 * 
 * @author Holger Eichelberger, SSE
 */
class AasInfoHandler extends TargetInfoHandler<AasTargetInformation> {

    static final AasInfoHandler INSTANCE = new AasInfoHandler();
    
    /**
     * Creates an instance.
     */
    private AasInfoHandler() {
        super(AasTargetInformation.class);
    }
    
    @Override
    public AasTargetInformation create(RbacRule rule) {
        return new AasTargetInformation(CollectionUtils.toList(rule.getElement()));
    }

    @Override
    public AasTargetInformation join(AasTargetInformation t1, AasTargetInformation t2) {
        t1.getAasIds().addAll(t2.getAasIds());
        return t1;
    }
    
}