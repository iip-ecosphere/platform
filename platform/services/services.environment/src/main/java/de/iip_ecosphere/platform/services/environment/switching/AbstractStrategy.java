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

package de.iip_ecosphere.platform.services.environment.switching;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic strategy implementation.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractStrategy implements Strategy {

    /**
     * Returns the logger instance.
     * 
     * @return the logger
     */
    protected Logger getLogger() {
        return LoggerFactory.getLogger(getClass());
    }
    
}
