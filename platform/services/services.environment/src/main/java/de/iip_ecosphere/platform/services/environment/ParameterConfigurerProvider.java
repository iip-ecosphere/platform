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

/**
 * Provides access to parameter configurers.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface ParameterConfigurerProvider {

    /**
     * Returns the parameter configurer.
     * 
     * @param paramName the name of the parameter
     * @return the associated parameter configurer or <b>null</b> if there is none
     */
    public ParameterConfigurer<?> getParameterConfigurer(String paramName);

}
