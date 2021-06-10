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
 * Defines the service kind.
 * 
 * @author Holger Eichelberger, SSE
 */
public enum ServiceKind {

    /**
     * A source service providing some form of input data, e.g., a connector.
     */
    SOURCE_SERVICE,
    
    /**
     * A transformation service in the widest sense, may be an AI service.
     */
    TRANSFORMATION_SERVICE,
    
    /**
     * A sink service consuming data and not re-emitting data.
     */
    SINK_SERVICE,
    
    /**
     * A probe service receiving data and turning it into alarms or monitoring information.
     */
    PROBE_SERVICE
    
}
