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

package test.de.iip_ecosphere.platform.support.processInfo;

import de.iip_ecosphere.platform.support.processInfo.ProcessInfoFactory;

/**
 * Test implementation of {@link ProcessInfoFactory}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TestProcessInfoFactory extends ProcessInfoFactory {

    /**
     * Test implementation of {@link ProcessInfo}.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class TestProcessInfo implements ProcessInfo {

        @Override
        public long getVirtualSize() {
            return 100;
        }
        
    }

    @Override
    public ProcessInfo create(long pid) {
        return new TestProcessInfo();
    }
    
    @Override
    public long getProcessId(Process proc) {
        return -1;
    }

    @Override
    public long getProcessId() {
        return -1;
    }

}
