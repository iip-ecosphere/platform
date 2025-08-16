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

package de.oktoflow.platform.support.metrics.micrometer;

import java.util.ArrayList;
import java.util.List;

import de.iip_ecosphere.platform.support.metrics.Measurement;
import de.iip_ecosphere.platform.support.metrics.Meter;
import de.iip_ecosphere.platform.support.metrics.Tag;

/**
 * Abstract wrapping meter implementation.
 * 
 * @param <M> the represented micrometer type
 * @author Holger Eichelberger, SSE
 */
class AbstractMeter<M extends io.micrometer.core.instrument.Meter> implements Meter {

    private M meter;
    private MicrometerId id;

    /**
     * A wrapped id.
     * 
     * @author Holger Eichelberger, SSE
     */
    static class MicrometerId implements Id {
        
        private io.micrometer.core.instrument.Meter.Id id;
        private List<Tag> tags;
        
        /**
         * Creates a wrapped instance.
         *
         * @param id the instance to wrap
         */
        MicrometerId(io.micrometer.core.instrument.Meter.Id id) {
            this.id = id;
        }

        @Override
        public String getName() {
            return id.getName();
        }
        
        /**
         * Initializes the tags lazily.
         */
        private void initTags() {
            if (null == tags && id.getTags() != null) {
                tags = new ArrayList<>();
                for (io.micrometer.core.instrument.Tag t : id.getTags()) {
                    tags.add(new MicrometerTag(t));
                }
            }
        }

        @Override
        public List<Tag> getTags() {
            initTags();
            return tags;
        }
        
        @Override
        public Iterable<Tag> getTagsAsIterable() {
            initTags();
            return tags;
        }

        @Override
        public String getTag(String key) {
            return id.getTag(key);
        }
        
        @Override
        public String getBaseUnit() {
            return id.getBaseUnit();
        }

        @Override
        public String getDescription() {
            return id.getDescription();
        }

        @Override
        public String toString() {
            return id.toString();
        }
        
        /**
         * The type is used by different registry implementations to structure the exposition
         * of metrics to different backends.
         *
         * @return the meter's type.
         */
        public Type getType() {
            return MicrometerUtils.typeValue(id.getType());
        }
        
        /**
         * Returns the implementing id.
         * 
         * @return the implementing id
         */
        public io.micrometer.core.instrument.Meter.Id getId() {
            return id;
        }
        
        @Override
        public int hashCode() {
            return id.hashCode();
        }

        @Override
        public boolean equals(Object other) {
            return other == this || id.equals(
                other instanceof MicrometerId ? ((MicrometerId) other).id : other);
        }        
        
    }

    /**
     * Creates a wrapping instance.
     * 
     * @param meter the wrapped instance
     */
    protected AbstractMeter(M meter) {
        this.meter = meter;
    }
    
    /**
     * Returns the wrapped meter.
     * 
     * @return the meter
     */
    protected M getMeter() {
        return meter;
    }
    
    @Override
    public String getName() {
        return meter.getId().getName();
    }
    
    @Override
    public int hashCode() {
        return meter.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return other == this || meter.equals(
            other instanceof AbstractMeter ? ((AbstractMeter<?>) other).getMeter() : other);
    }
    
    @Override
    public Id getId() {
        if (null == id) {
            id = new MicrometerId(meter.getId());
        }
        return id;
    }
    
    @Override
    public Iterable<Measurement> measure() {
        return MicrometerUtils.mmWrapMeasurementIterable(meter.measure());
    }

}
