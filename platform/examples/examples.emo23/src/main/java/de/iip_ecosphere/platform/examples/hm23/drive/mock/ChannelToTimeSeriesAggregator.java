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

package de.iip_ecosphere.platform.examples.hm23.drive.mock;

import java.io.InputStream;

/**
 * Mocking class.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ChannelToTimeSeriesAggregator 
    extends de.iip_ecosphere.platform.examples.hm23.drive.ChannelToTimeSeriesAggregator {

    /**
     * Fallback constructor, also used for testing main program.
     */
    public ChannelToTimeSeriesAggregator() {
        super();
    }
    
    /**
     * Creates a service instance from a service id and a YAML artifact.
     * 
     * @param serviceId the service id
     * @param ymlFile the YML file containing the YAML artifact with the service descriptor
     */
    public ChannelToTimeSeriesAggregator(String serviceId, InputStream ymlFile) {
        super(serviceId, ymlFile);
    }

    @Override
    protected boolean initIsAggregating() {
        return true; // ignore
    }

}
