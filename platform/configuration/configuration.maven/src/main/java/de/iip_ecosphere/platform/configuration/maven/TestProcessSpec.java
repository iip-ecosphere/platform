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

package de.iip_ecosphere.platform.configuration.maven;

import org.apache.maven.plugins.annotations.Parameter;

/**
 * Defines an additional process to be executed in the test build process.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TestProcessSpec extends BasicProcessSpec {
    
    @Parameter(required = false, defaultValue = "true")
    private boolean waitFor;

    /**
     * Returns whether the process shall be completely executed before continuing.
     * 
     * @return {@code true} for wait for completion, {@code false} for parallel execution
     */
    public boolean isWaitFor() {
        return waitFor;
    }

    /**
     * Sets whether the process shall be completely executed before continuing. [mvn]
     * 
     * @param waitFor the flag to set
     */
    public void setWaitFor(boolean waitFor) {
        this.waitFor = waitFor;
    }    

}
