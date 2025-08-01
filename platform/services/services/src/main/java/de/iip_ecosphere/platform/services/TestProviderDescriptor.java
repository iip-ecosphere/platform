/**
 * ******************************************************************************
 * Copyright (c) {2025} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.services;

/**
 * JSL descriptor to provide services. Shall be implemented by tests only.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface TestProviderDescriptor {

    /**
     * Returns a test represented as it's class for execution in jUnit. This is required if a test running a service 
     * manager and other components like AAS as plugins shall get execute a test independently. [testing]
     * 
     * @param index a 0-based index of the test/suite to return; usually test and implementing service manager are in 
     *    close relationship and know the valid indexes
     * @return the test classes or <b>null</b> if there is none for the given index
     */
    public Class<?>[] getTests(int index);

}
