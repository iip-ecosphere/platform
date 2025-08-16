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

import de.iip_ecosphere.platform.support.metrics.Statistic;
import de.iip_ecosphere.platform.support.metrics.Tag;
import de.oktoflow.platform.support.metrics.micrometer.AbstractMeter.MicrometerId;
import io.micrometer.core.instrument.Measurement;

import java.util.Iterator;
import java.util.function.Function;

import de.iip_ecosphere.platform.support.metrics.Meter;
import de.iip_ecosphere.platform.support.metrics.MeterFilter;
import de.iip_ecosphere.platform.support.metrics.Meter.Id;
import de.iip_ecosphere.platform.support.metrics.Meter.Type;
import de.iip_ecosphere.platform.support.metrics.MeterFilter.MeterFilterReply;

/**
 * Translation utilities, intentionally public.
 * 
 * @author Holger Eichelberger, SSE
 */
public class MicrometerUtils {

    /**
     * Turns a string into an enum value considering {@code dflt}.
     * 
     * @param <T> the enum type
     * @param cls the enum class
     * @param value the value as string
     * @param dflt the default value if {@code value} cannot be converted/found
     * @return the value
     */
    public static <T extends Enum<T>> T value(Class<T> cls, String value, T dflt) {
        T result;
        if (value == null) {
            result = dflt;
        } else {
            try {
                result = Enum.valueOf(cls, value);
            } catch (IllegalArgumentException e) {
                result = dflt;
            }
        }
        return result;
    }
    
    /**
     * Translates an abstracted id into a micrometer id.
     * 
     * @param id the abstracted id
     * @return the micrometer id, <b>null</b> of not translatable
     */
    public static io.micrometer.core.instrument.Meter.Id idValue(Id id) {
        return id instanceof MicrometerId ? ((MicrometerId) id).getId() : null;
    }

    /**
     * Translates an abstracted id into a micrometer tag.
     * 
     * @param tag the abstracted tag
     * @return the micrometer tag, <b>null</b> of not translatable
     */
    public static io.micrometer.core.instrument.Tag tagValue(Tag tag) {
        return tag instanceof MicrometerTag ? ((MicrometerTag) tag).getTag() : null;
    }

    /**
     * Translates an abstracted filter into a micrometer filter.
     * 
     * @param filter the abstracted filter
     * @return the micrometer filter, <b>null</b> of not translatable
     */
    public static io.micrometer.core.instrument.config.MeterFilter filterValue(MeterFilter filter) {
        return filter instanceof MicrometerMeterFilter ? ((MicrometerMeterFilter) filter).getFilter() : null;
    }    

    /**
     * Translates an abstracted filter into a micrometer filter.
     * 
     * @param filter the abstracted filter
     * @return the micrometer filter, <b>null</b> of not translatable
     */
    public static MeterFilter mmFilterValue(io.micrometer.core.instrument.config.MeterFilter filter) {
        return new MicrometerMeterFilter(filter);
    }    

    /**
     * Translates abstracted filters into micrometer filters.
     * 
     * @param filter the abstracted filters
     * @return the micrometer filters, <b>null</b> of not translatable
     */
    public static io.micrometer.core.instrument.config.MeterFilter[] filterValue(MeterFilter... filter) {
        io.micrometer.core.instrument.config.MeterFilter[] result = 
            new io.micrometer.core.instrument.config.MeterFilter[filter.length];
        for (int i = 0; i < filter.length; i++) {
            result[i] = filterValue(filter[i]);
        }
        return result;
    }

    /**
     * Translates micrometer filters into abstracted filters.
     * 
     * @param filter the micrometer filters
     * @return the abstracted filters, <b>null</b> of not translatable
     */
    public static MeterFilter[] mmFilterValue(io.micrometer.core.instrument.config.MeterFilter... filter) {
        MeterFilter[] result = new MeterFilter[filter.length];
        for (int i = 0; i < filter.length; i++) {
            result[i] = mmFilterValue(filter[i]);
        }
        return result;
    }

    /**
     * Turns a String into a {@link Type} using {@link Type#OTHER} as default.
     * 
     * @param value the value
     * @return the corresponding type or {@link Type#OTHER}
     */
    public static Type typeValue(io.micrometer.core.instrument.Meter.Type value) { 
        return value(Type.class, value.name(), Type.OTHER);
    }

    /**
     * Turns a String into a {@link Type} using {@link io.micrometer.core.instrument.Meter.Type#OTHER} as default.
     * 
     * @param value the value
     * @return the corresponding type or {@link io.micrometer.core.instrument.Meter.Type#OTHER}
     */
    public static io.micrometer.core.instrument.Meter.Type mmTypeValue(Type value) { 
        return value(io.micrometer.core.instrument.Meter.Type.class, value == null ? null : value.name(), 
            io.micrometer.core.instrument.Meter.Type.OTHER);
    }

    /**
     * Turns a String into a {@link Type} using {@link Statistic#UNKNOWN} as default.
     * 
     * @param value the value
     * @return the corresponding type or {@link Statistic#UNKNOWN}
     */
    public static Statistic mmStatisticValue(io.micrometer.core.instrument.Statistic value) { 
        return value(Statistic.class, value == null ? null : value.name(), Statistic.UNKNOWN);
    }

    /**
     * Turns a String into a {@link Type} using {@link io.micrometer.core.instrument.Statistic#UNKNOWN} as default.
     * 
     * @param statistic the value
     * @return the corresponding type or {@link io.micrometer.core.instrument.Statistic#UNKNOWN}
     */
    public static io.micrometer.core.instrument.Statistic statisticValue(Statistic statistic) { 
        return value(io.micrometer.core.instrument.Statistic.class, statistic == null ? null : statistic.name(), 
            io.micrometer.core.instrument.Statistic.UNKNOWN);
    }
    
    /**
     * Wraps an iterable.
     * 
     * @param <TS> the source type
     * @param <TT> the target type
     * @param iter the source iterable
     * @param func the translation function
     * @return the target iterable
     */
    public static <TS, TT> Iterable<TT> wrapIterable(Iterable<TS> iter, Function<TS, TT> func) {
        return new Iterable<TT>() {

            @Override
            public Iterator<TT> iterator() {
                return wrapIterator(iter.iterator(), func);
            }
        };
    }
    
    /**
     * Wraps an iterator.
     * 
     * @param <TS> the source type
     * @param <TT> the target type
     * @param iter the source iterator
     * @param func the translation function
     * @return the target iterator
     */
    public static <TS, TT> Iterator<TT> wrapIterator(Iterator<TS> iter, Function<TS, TT> func) {
        return new Iterator<TT>() {

            @Override
            public boolean hasNext() {
                return iter.hasNext();
            }

            @Override
            public TT next() {
                return func.apply(iter.next());
            }
            
        };
    }
    
    /**
     * Maps an abstracted iterable of measurements to a micrometer iterable of measurements.
     * 
     * @param iter the abstracted iterable
     * @return the micrometer iterable
     */
    public static Iterable<de.iip_ecosphere.platform.support.metrics.Measurement> mmWrapMeasurementIterable(
        Iterable<Measurement> iter) {
        return wrapIterable(iter, s -> new MicrometerMeasurement(s));
    }

    /**
     * Maps an abstracted iterable of tags to a micrometer iterable of tags.
     * 
     * @param iter the abstracted iterable
     * @return the micrometer iterable
     */
    public static Iterable<io.micrometer.core.instrument.Tag> wrapTagIterable(
        Iterable<Tag> iter) {
        return wrapIterable(iter, s -> tagValue(s));
    }
    
    /**
     * Maps an abstracted iterable of tags to a micrometer iterable of tags.
     * 
     * @param iter the abstracted iterable
     * @return the micrometer iterable
     */
    public static Iterable<Tag> mmWrapTagIterable(
        Iterable<io.micrometer.core.instrument.Tag> iter) {
        return wrapIterable(iter, s -> new MicrometerTag(s));
    }    

    /**
     * Maps an micrometer iterable of measurements to an abstracted iterable of measurements.
     * 
     * @param iter the micrometer iterable
     * @return the abstracted iterable
     */
    public static Iterable<Measurement> wrapMeasurementIterable(
        Iterable<de.iip_ecosphere.platform.support.metrics.Measurement> iter) {
        return wrapIterable(iter, s -> ((MicrometerMeasurement) s).getMeasurement());
    }
    
    /**
     * Abstract meter creator to translate among implementation and abstraction.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface MeterCreator {
        
        /**
         * Creates an implementation meter.
         * 
         * @param id the meter id
         * @param type the meter type
         * @param measure the meter measures
         * @return the created meter
         */
        public io.micrometer.core.instrument.Meter create(io.micrometer.core.instrument.Meter.Id id, 
            io.micrometer.core.instrument.Meter.Type type, Iterable<Measurement> measure);
        
    }

    /**
     * Creates a meter via an unknown implementation function.
     * 
     * @param meter the meter
     * @param creator the creator function
     * @return the created wrapped meter
     */
    public static Meter createMeter(Meter meter, MeterCreator creator) {
        Meter result = null;
        if (meter instanceof AbstractMeter) {
            Id id = meter.getId();
            result = MicrometerMeterRegistry.wrap(creator.create(idValue(id), 
                mmTypeValue(id.getType()), wrapMeasurementIterable(meter.measure())), Meter.class);
        }
        return result;
    }

    /**
     * Appends {@code addition} to {@code base}.
     * 
     * @param base the base filters
     * @param addition the addition filters
     * @return the combined filter array
     */
    public static io.micrometer.core.instrument.config.MeterFilter[] append(
        io.micrometer.core.instrument.config.MeterFilter[] base, 
        io.micrometer.core.instrument.config.MeterFilter... addition) {
        io.micrometer.core.instrument.config.MeterFilter[] result 
            = new io.micrometer.core.instrument.config.MeterFilter[base.length + addition.length];
        int pos = 0;
        for (int i = 0; i < base.length; i++) {
            result[pos++] = base[i];
        }
        for (int i = 0; i < addition.length; i++) {
            result[pos++] = addition[i];
        }
        return result;
    }    
    
    /**
     * Applies the given list of meter filters.
     * 
     * @param registry the registry to apply the filters to
     * @param filters the filters to apply
     */
    public static void apply(io.micrometer.core.instrument.MeterRegistry registry, 
        io.micrometer.core.instrument.config.MeterFilter... filters) {
        for (io.micrometer.core.instrument.config.MeterFilter f: filters) {
            registry.config().meterFilter(f);
        }
    }

    /**
     * Returns whether a meter id shall be included into a result set.
     * 
     * @param id the meter id
     * @param filters the filters to be applied. The first matching filter returning {@link MeterFilterReply#DENY} will 
     *    remove a metric from the result list, an {@link MeterFilterReply#NEUTRAL} will keep it as long as there is no 
     *    {@link MeterFilterReply#DENY} filter until the end of the filter list and {@link MeterFilterReply#ACCEPT} will
     *    immediately accept the actual meter.
     * @return {@code true} for apply, {@code false} else
     */
    public static boolean include(io.micrometer.core.instrument.Meter.Id id, 
        io.micrometer.core.instrument.config.MeterFilter... filters) {
        boolean include = true;
        for (int f = 0; include && f < filters.length; f++) {
            io.micrometer.core.instrument.config.MeterFilterReply reply = filters[f].accept(id);
            if (io.micrometer.core.instrument.config.MeterFilterReply.DENY == reply) {
                include = false; // if filter applies, throw out metric
            } else if (io.micrometer.core.instrument.config.MeterFilterReply.ACCEPT == reply) {
                include = true;
                break;
            } else {
                include = true; // if filter applies, keep metric
            }
        }
        return include;
    }

    /**
     * Returns whether a meter id shall be included into a result set.
     * 
     * @param id the meter id
     * @param filters the filters to be applied. The first matching filter returning {@link MeterFilterReply#DENY} will 
     *    remove a metric from the result list, an {@link MeterFilterReply#NEUTRAL} will keep it as long as there is no 
     *    {@link MeterFilterReply#DENY} filter until the end of the filter list and {@link MeterFilterReply#ACCEPT} will
     *    immediately accept the actual meter.
     * @return {@code true} for apply, {@code false} else
     */
    public static boolean include(String id, io.micrometer.core.instrument.config.MeterFilter... filters) {
        return include(new io.micrometer.core.instrument.Meter.Id(id, null, null, null, null), filters);
    }
    
}
