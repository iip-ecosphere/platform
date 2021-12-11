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

package de.iip_ecosphere.platform.support.iip_aas;

/**
 * Provides the device id.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface IdProvider {

    public static final String ID_PARAM_NAME = "iip.id";
    
    /**
     * Returns the id of this device. As the ID is also used as shortId in the platform AAS, 
     * it may be that the AAS implementation adjusts the ID to comply with the AAS id rules. 
     * Thus, it may be adequate that the returned ID already complies with these rules, e.g., 
     * must start with a character.
     * 
     * @return the id, may be <b>null</b> if for some reason the ID cannot be provided and a 
     * fallback shall be used
     */
    public String provideId();
    
    /**
     * Returns whether the console parameter "--{@value #ID_PARAM_NAME}" may
     * override the value of this provider.
     * 
     * @return {@code true} for override, {@code false} else
     */
    public boolean allowsConsoleOverride();
    
}
