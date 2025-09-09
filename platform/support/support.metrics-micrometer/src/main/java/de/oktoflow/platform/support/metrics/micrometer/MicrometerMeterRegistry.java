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

import de.iip_ecosphere.platform.support.logging.LoggerFactory;
import de.iip_ecosphere.platform.support.metrics.Clock;
import de.iip_ecosphere.platform.support.metrics.Counter;
import de.iip_ecosphere.platform.support.metrics.Gauge;
import de.iip_ecosphere.platform.support.metrics.Meter;
import de.iip_ecosphere.platform.support.metrics.Meter.Id;
import de.iip_ecosphere.platform.support.metrics.MeterFilter;
import de.iip_ecosphere.platform.support.metrics.MeterRegistry;
import de.iip_ecosphere.platform.support.metrics.Tag;
import de.iip_ecosphere.platform.support.metrics.Timer;
import de.oktoflow.platform.support.metrics.micrometer.AbstractMeter.MicrometerId;
import io.micrometer.core.instrument.search.MeterNotFoundException;
import io.micrometer.core.instrument.search.RequiredSearch;

/**
 * A wrapping meter registry.
 * 
 * @author Holger Eichelberger, SSE
 */
class MicrometerMeterRegistry implements MeterRegistry, MeterRegistry.Config {

    private io.micrometer.core.instrument.MeterRegistry registry;
    
    /**
     * Creates a wrapping registry.
     * 
     * @param registry the underlying micrometer registry
     */
    MicrometerMeterRegistry(io.micrometer.core.instrument.MeterRegistry registry) {
        this.registry = registry;
    }
    
    @Override
    public Config config() {
        return this;
    }

    @Override
    public Meter remove(Meter meter) {
        return null == meter ? null : remove(meter.getName());
    }

    @Override
    public Meter remove(String name) {
        Meter result;
        try {
            result = wrap(registry.remove(registry.get(name).meter()), Meter.class);
        } catch (MeterNotFoundException e) {
            result = null;
        }
        return result;
    }
    
    @Override
    public Meter remove(Id id) {
        Meter result;
        if (id instanceof MicrometerId) {
            result = wrap(registry.remove(MicrometerUtils.idValue(id)), Meter.class);
        } else {
            LoggerFactory.getLogger(this).warn("Cannot convert {} to {}", id, MicrometerId.class.getName());
            result = null;
        }
        return result;
    }

    /**
     * Turns a micrometer meter instance into a wrapped instance.
     * 
     * @param meter the meter, may be <b>null</b>
     * @return the wrapped instance, may be <b>null</b>
     */
    static <T extends Meter> T wrap(io.micrometer.core.instrument.Meter meter, Class<T> filter) {
        Meter result = null;
        if (applies(filter, Gauge.class) && meter instanceof io.micrometer.core.instrument.Gauge) {
            result = new MicrometerGauge((io.micrometer.core.instrument.Gauge) meter);
        } else if (applies(filter, Timer.class) && meter instanceof io.micrometer.core.instrument.Timer) {
            result = new MicrometerTimer((io.micrometer.core.instrument.Timer) meter);
        } else if (applies(filter, Counter.class) && meter instanceof io.micrometer.core.instrument.Counter) {
            result = new MicrometerCounter((io.micrometer.core.instrument.Counter) meter);
        } else { // incomplete, e.g., FunctionCounter missing
            result = new GenericMeterWrapper(meter);
        }
        return filter.cast(result);
    }
    
    /**
     * Just wraps a not further supported meter so that it is not lost.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class GenericMeterWrapper extends AbstractMeter<io.micrometer.core.instrument.Meter> {

        /**
         * Creates an instance.
         * 
         * @param meter the meter to wrap
         */
        protected GenericMeterWrapper(io.micrometer.core.instrument.Meter meter) {
            super(meter);
        }
        
    }
    
    /**
     * Returns whether filter applies.
     * 
     * @param filter the type to filter for
     * @param provided the type that will be created if the filter applies
     * @return {@code true} for apply, {@code false} else
     */
    static boolean applies(Class<? extends Meter> filter, Class<? extends Meter> provided) {
        return filter == Meter.class || filter == provided;
    }

    @Override
    public Meter getMeter(String name) {
        return wrap(getMeterImpl(name, null), Meter.class);
    }

    @Override
    public Meter getMeter(String name, Iterable<Tag> tags) {
        return wrap(getMeterImpl(name, MicrometerUtils.wrapTagIterable(tags)), Meter.class);
    }

    @Override
    public Gauge getGauge(String name) {
        return wrap(getMeterImpl(name, null), Gauge.class);
    }

    @Override
    public Counter getCounter(String name) {
        return wrap(getMeterImpl(name, null), Counter.class);
    }

    @Override
    public Timer getTimer(String name) {
        return wrap(getMeterImpl(name, null), Timer.class);
    }
    
    /**
     * Returns a meter implementation.
     * 
     * @param name the name of the meter
     * @param tags the required tags, may be <b>null</b> for none
     * @return the implementation, <b>null</b> if not found
     */
    private io.micrometer.core.instrument.Meter getMeterImpl(String name, 
        Iterable<io.micrometer.core.instrument.Tag> tags) {
        io.micrometer.core.instrument.Meter result;
        try {
            RequiredSearch s = registry.get(name);
            if (null != tags) {
                s = s.tags(tags);
            }
            result = s.meter();
        } catch (MeterNotFoundException e) {
            result = null;
        }
        return result;
    }

    /**
     * Returns the underlying micrometer registry.
     * 
     * @return the registry
     */
    io.micrometer.core.instrument.MeterRegistry getRegistry() {
        return registry;
    }

    @Override
    public Config meterFilter(MeterFilter filter) {
        if (filter instanceof MicrometerMeterFilter) {
            registry.config().meterFilter(((MicrometerMeterFilter) filter).getFilter());
        }
        return this;
    }

    @Override
    public Clock clock() {
        return new MicrometerClock(registry.config().clock());
    }

    @Override
    public <T extends Number> T gauge(String name, T number) {
        return registry.gauge(name, number);
    }

    @Override
    public Counter counter(String name, String... tags) {
        return wrap(registry.counter(name, tags), Counter.class);
    }

    @Override
    public Timer timer(String name, String... tags) {
        return wrap(registry.timer(name, tags), Timer.class);
    }
    
    @Override
    public List<Meter> getMeters() {
        List<Meter> result = new ArrayList<>();
        for (io.micrometer.core.instrument.Meter m : registry.getMeters()) {
            result.add(wrap(m, Meter.class));
        }
        return result;
    }

}
