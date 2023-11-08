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

package de.iip_ecosphere.platform.support.collector;

import org.apache.commons.io.FileUtils;

import de.iip_ecosphere.platform.support.setup.AbstractSetup;

/**
 * Collector setup information.
 * 
 * @author Holger Eichelberger, SSE
 */
public class CollectorSetup extends AbstractSetup {
    
    private String dataDir = FileUtils.getUserDirectoryPath() + "/oktoflow-collector";

    /**
     * Returns the data directory of the collector.
     * 
     * @return the data directory
     */
    public String getDataDir() {
        return dataDir;
    }

    /**
     * Changes the data directory of the collector. [snakeyaml]
     * 
     * @param dataDir the data directory
     */
    public void setDataDir(String dataDir) {
        this.dataDir = dataDir;
    }

}
