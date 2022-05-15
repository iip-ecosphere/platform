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

/**
 * Implements the default profile accepting all lifecycle descriptors.
 * 
 * @author Holger Eichelberger, SSE
 */
public class DefaultProfile implements LifecycleProfile {

    public static final String NAME = "default";
    public static final DefaultProfile INSTANCE = new DefaultProfile();

    /**
     * Prevents external creation.
     */
    private DefaultProfile() {
    }
    
    @Override
    public boolean test(Class<? extends LifecycleDescriptor> descriptor) {
        return true;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void initialize(String[] args) {
    }

}
