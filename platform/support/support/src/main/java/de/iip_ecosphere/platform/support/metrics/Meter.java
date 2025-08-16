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

package de.iip_ecosphere.platform.support.metrics;

import java.util.List;

/**
 * Something that meters something.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface Meter {

    /**
     * Custom meters may emit metrics like one of these types without implementing
     * the corresponding interface. For example, a heisen-counter like structure
     * will emit the same metric as a {@link Counter} but does not have the same
     * increment-driven API.
     */
    enum Type {
        COUNTER,
        GAUGE,
        LONG_TASK_TIMER,
        TIMER,
        DISTRIBUTION_SUMMARY,
        OTHER;
    }
    
    /**
     * A {@link Meter} id.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface Id {

        /**
         * Returns the name.
         * 
         * @return the name of this meter.
         */
        public String getName();

        /**
         * Returns the tags.
         * 
         * @return a set of dimensions that allows you to break down the name.
         */
        public List<Tag> getTags();
        
        /**
         * Returns the tags.
         * 
         * @return a set of dimensions that allows you to break down the name.
         */
        public Iterable<Tag> getTagsAsIterable();

        /**
         * Returns a specific tag value.
         *
         * @param key The tag key to attempt to match
         * @return A matching tag value, or <b>null</b> if no tag with the provided key exists on this id.
         */
        public String getTag(String key);

        /**
         * Returns the base unit.
         * 
         * @return The base unit of measurement for this meter, may be <b>null</b>
         */
        public String getBaseUnit();
        
        /**
         * Returns the description.
         * 
         * @return A description of the meter's purpose. This description text is published to monitoring systems
         * that support description text.
         */
        public String getDescription();
        
        /**
         * The type is used by different registry implementations to structure the exposition
         * of metrics to different backends.
         *
         * @return The meter's type.
         */
        public Type getType();
        
    }
    
    /**
     * Returns the name of the meter. May be ambiougous if multiple meters exist with different tags. 
     * Then the returned name may refer to all.
     * 
     * @return the name
     */
    public String getName();
    
    /**
     * Returns the id.
     * 
     * @return the id
     */
    public Id getId();

    /**
     * Get a set of measurements. Should always return the same number of measurements and in
     * the same order, regardless of the level of activity or the lack thereof.
     *
     * @return The set of measurements that represents the instantaneous value of this meter.
     */
    public Iterable<Measurement> measure();

}
