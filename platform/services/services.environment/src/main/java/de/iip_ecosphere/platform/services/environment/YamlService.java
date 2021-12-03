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
 * Information about a single service.
 * 
 * @author Holger Eichelberger, SSE
 */
public class YamlService extends AbstractYamlService {
    
    private YamlProcess process;
    
    /**
     * Returns an optional attached process realizing the service.
     * 
     * @return the process information, may be <b>null</b>
     */
    public YamlProcess getProcess() {
        return process;
    }

    /**
     * Defines an optional attached process realizing the service. [required by SnakeYaml]
     * 
     * @param process the process information, may be <b>null</b>
     */
    public void setProcess(YamlProcess process) {
        this.process = process;
    }
    
}
