/**
 * ******************************************************************************
 * Copyright (c) {2022} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support;

import java.util.function.Predicate;

/**
 * Defines a lifecycle profile.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface LifecycleProfile extends Predicate<Class<? extends LifecycleDescriptor>> {
    
    /**
     * Returns the name of the protocol it is elected via commandline using "-iip.profile=&lt;name&gt;".
     * 
     * @return the name
     */
    public String getName();
    
    /**
     * Does initialization before the lifecycles are started.
     * 
     * @param args command line arguments
     */
    public void initialize(String[] args);

}
