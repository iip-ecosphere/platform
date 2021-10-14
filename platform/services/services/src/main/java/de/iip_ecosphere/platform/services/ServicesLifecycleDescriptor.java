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

package de.iip_ecosphere.platform.services;

import de.iip_ecosphere.platform.support.PidLifecycleDescriptor;
import de.iip_ecosphere.platform.support.iip_aas.AbstractAasLifecycleDescriptor;

/**
 * Implements the generic lifecycle descriptor for the service manager.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ServicesLifecycleDescriptor extends AbstractAasLifecycleDescriptor implements PidLifecycleDescriptor {

    /**
     * Creates an instance for the service manager.
     */
    public ServicesLifecycleDescriptor() {
        super("Services", () -> ServiceFactory.getAasSetup());
    }
    
    @Override
    public String getPidFileName() {
        return "iip-serviceMgr.pid";
    }
    
}
