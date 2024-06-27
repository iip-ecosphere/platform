/**
 * ******************************************************************************
 * Copyright (c) {2024} The original author or authors
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
 * Represents a simple timeseries query given by start/end time assuming that the connector knows
 * from where to get the data.
 * 
 * @author Holger Eichelberger, SSE
 */
public class SimpleTimeseriesQuery implements ConnectorTriggerQuery {

    /**
     * Time value interpretations.
     * 
     * @author Holger Eichelberger, SSE
     */
    public enum TimeKind {
        
        /**
         * Unspecified, to be ignored.
         */
        UNSPECIFIED,
        
        /**
         * Absolute as point in time since 1970-01-01T00:00:00Z.
         */
        ABSOLUTE,

        /**
         * Relative from now in weeks.
         */
        RELATIVE_WEEKS,

        /**
         * Relative from now in days.
         */
        RELATIVE_DAYS,

        /**
         * Relative from now in hours.
         */
        RELATIVE_HOURS,

        /**
         * Relative from now in minutes.
         */
        RELATIVE_MINUTES,

        /**
         * Relative from now in seconds.
         */
        RELATIVE_SECONDS,
        
        /**
         * Relative from now in milli seconds.
         */
        RELATIVE_MILLISECONDS,

        /**
         * Relative from now in micro seconds.
         */
        RELATIVE_MICROSECONDS

    }
    
    private int start;
    private TimeKind startKind;
    private int end;
    private TimeKind endKind;
    private int delay;

    /**
     * Creates a simple time series query with start time point and unspecified end.
     * 
     * @param start the start time
     * @param startKind the start kind as interpretation of {@code start}
     */
    public SimpleTimeseriesQuery(int start, TimeKind startKind) {
        this(start, startKind, -1, TimeKind.UNSPECIFIED);
    }

    /**
     * Creates a simple time series query.
     * 
     * @param start the start time
     * @param startKind the start kind as interpretation of {@code start}
     * @param end the end time
     * @param endKind the end kind as interpretation of {@code start}
     */
    public SimpleTimeseriesQuery(int start, TimeKind startKind, int end, TimeKind endKind) {
        this(start, startKind, end, endKind, 0);
    }
    
    /**
     * Creates a simple time series query.
     * 
     * @param start the start time
     * @param startKind the start kind as interpretation of {@code start}
     * @param end the end time
     * @param endKind the end kind as interpretation of {@code start}
     * @param delay the fixed absolute delay of a result timeseries in ms, 0 for none
     */
    public SimpleTimeseriesQuery(int start, TimeKind startKind, int end, TimeKind endKind, int delay) {
        this.start = start;
        this.startKind = startKind;
        this.end = end;
        this.endKind = endKind;
        this.delay = delay;
    }

    /**
     * Returns the start time.
     * 
     * @return the start time, interpretation see {@link #getStartKind()}
     */
    public int getStart() {
        return start;
    }

    /**
     * Returns the interpretation of the start time.
     * 
     * @return the the start kind
     */
    public TimeKind getStartKind() {
        return startKind;
    }

    /**
     * Returns the end time.
     * 
     * @return the end time, interpretation see {@link #getEndKind()}
     */
    public int getEnd() {
        return end;
    }

    /**
     * Returns the interpretation of the end time.
     * 
     * @return the end kind
     */
    public TimeKind getEndKind() {
        return endKind;
    }

    @Override
    public int delay() {
        return delay;
    }

}
