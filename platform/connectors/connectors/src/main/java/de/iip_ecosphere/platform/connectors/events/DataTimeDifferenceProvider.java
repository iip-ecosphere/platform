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

package de.iip_ecosphere.platform.connectors.events;

/**
 * For connectors with simulated time, determines the difference between two data points based on the
 * data point before. Considered only if the connector implementation supports this.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface DataTimeDifferenceProvider<T> {

    /**
     * Returns the time difference. Called also for new arriving, cached/not-passed-on data.
     * 
     * @param data the data point before applying the difference, i.e., the last processed input data point
     * @return the time difference in ms, may be negative to rely on the default value known to the connector or 
     *     {@code 0} for no simulated difference (use with care)
     */
    public int determineDifference(T data);
    
}
