/**
 * ******************************************************************************
 * Copyright (c) {2023} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.aas;

/**
 * A receipe that can take a CORS origin specification.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface CorsEnabledRecipe {
    
    /**
     * Sets the access control to allow cross origin.
     * 
     * @param accessControlAllowOrigin the information to be placed in the HTTP header field 
     * "Access-Control-Allow-Origin"; the specific server or {@link DeploymentRecipe#ANY_CORS_ORIGIN}
     * @return an instance of the sub-recipe
     */
    public CorsEnabledRecipe setAccessControlAllowOrigin(String accessControlAllowOrigin);

}
