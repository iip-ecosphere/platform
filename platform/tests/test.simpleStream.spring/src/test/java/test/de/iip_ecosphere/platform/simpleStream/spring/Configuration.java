/**
 * ******************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.simpleStream.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * A simple test configuration, also allowing for external overriding.
 * 
 * @author Holger Eichelberger, SSE
 */
@Component
@ConfigurationProperties(prefix = "test")
public class Configuration {
    
    private boolean debug = false;
    private int ingestCount = 100;
    
    /**
     * Returns whether debug-mode is on.
     * 
     * @return {@code true} for debug, {@code false} else
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * Returns the number of items to ingest until end of test.
     * 
     * @return the number of items
     */
    public int getIngestCount() {
        return ingestCount;
    }

    /**
     * Defines whether debug-mode is on.  [required by Spring]
     * 
     * @param debug {@code true} for debug, {@code false} else
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }
    
    /**
     * Defines the number of items to ingest until end of test. [required by Spring]
     * 
     * @param ingestCount the number of items
     */
    public void setIngestCount(int ingestCount) {
        this.ingestCount = ingestCount;
    }

}
