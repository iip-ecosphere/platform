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

import java.util.concurrent.ExecutionException;

/**
 * Interface to platform nameplate operations.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface PlatformClient {
    
    /**
     * Snapshots the platform AAS if possible.
     * 
     * @param id an optional id to characterize the snapshot
     * @return the name of the written file
     */
    public String snapshotAas(String id) throws ExecutionException; 

}
