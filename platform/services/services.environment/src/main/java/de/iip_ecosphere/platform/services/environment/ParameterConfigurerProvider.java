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

package de.iip_ecosphere.platform.services.environment;

import java.util.Set;

/**
 * Provides access to parameter configurers.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface ParameterConfigurerProvider {

    /**
     * Returns the parameter configurer for a given parameter. We need this generic approach to ease code 
     * generation and initial setting of parameter values.
     * 
     * @param paramName the name of the parameter
     * @return the associated parameter configurer or <b>null</b> if there is none
     */
    public ParameterConfigurer<?> getParameterConfigurer(String paramName);

    /**
     * The set of parameter names.
     * 
     * @return the parameter names, may be <b>null</b> for none
     */
    public Set<String> getParameterNames();
    
}
