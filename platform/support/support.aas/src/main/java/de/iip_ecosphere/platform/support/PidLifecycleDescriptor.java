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

package de.iip_ecosphere.platform.support;

/**
 * A lifecycle descriptor that may lead to the creation of a {@link PidFile PID file}. Shall only be used for 
 * major platform parts.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface PidLifecycleDescriptor extends LifecycleDescriptor {
    
    /**
     * Returns the name of the PID file to create. Shall only be provided by the "main" lifecycle descriptor. 
     * "First one" is taken for granted.
     * 
     * @return the name of the PID file to identify the process/application, may be <b>null</b> for none
     */
    public String getPidFileName();

}
