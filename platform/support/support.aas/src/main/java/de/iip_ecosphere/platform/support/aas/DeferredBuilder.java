/**
 * ******************************************************************************
 * Copyright (c) {2021} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.aas;

import java.util.HashMap;
import java.util.Map;

import de.iip_ecosphere.platform.support.Builder;

/**
 * Allows deferring the build/closing of the builder to its parent. Either you call {@link Builder#build()} to complete
 * the construction of an AAS element or, alternatively, one of the methods defined in this interface. To realize a 
 * deferred builder, the builder must implement the methods of this interface. The parent must take up the deferred
 * builder, execute them in its own build method and consider the known deferred builder when returning builder
 * instances. A {@link DeferredParent deferred parent} may explicitly offer the operation {@link #buildDeferred()} to
 * allow forcing the execution of {@link #build()} in dynamic situations where no parent builder is available. Basic 
 * implementations for these methods are provided as static methods below.
 * 
 * @param <I> The type of the instance to build.
 * @author Holger Eichelberger, SSE
 */
public interface DeferredBuilder<I> extends Builder<I> {

    /**
     * In some cases you would like to keep a builder open and continue the construction later. However, the underlying
     * AAS implementation may "close" the construction with {@link #build()} so that continuing fails. In this case,
     * you can use this method instead of {@link #build()} to tell the parent that it shall call {@code #build()}
     * for you, typically when {@link #build()} is performed on the parent.
     */
    public void defer();

    /**
     * Forces building the deferred builders registered with the parent builder. In the basic construction of an AAS
     * this is not needed, because the parent, at latest the submodel is supposed to perform building the deferred 
     * builders in it's own {@link #build()} method. However, in dynamic situations the parent may already be build, 
     * i.e., there is no builder instance, and you must explicitly force building the deferred builders. This method 
     * will perform this task and transitively also build nested deferred builders.
     * 
     * @see DeferredParent
     */
    public void buildDeferred();
    
    /**
     * Defers a builder.
     * 
     * @param shortId the id of the element for which building shall be deferred
     * @param builder the builder for the element with {@code shortId}
     * @param deferred the deferred builders to return a builder from, may be <b>null</b> for non registered so far
     * @return {@code deferred} containing {@code builder} or a new map containing {@code builder} if {@code deferred} 
     *   was <b>null</b>
     */
    public static Map<String, Builder<?>> defer(String shortId, Builder<?> builder, Map<String, Builder<?>> deferred) {
        if (null == deferred) {
            deferred = new HashMap<>();
        }
        deferred.put(shortId, builder);
        return deferred;
    }

    /**
     * Builds the deferred builders, i.e., calls {@link Builder#build()} on each element in {@code deferred} and
     * clears {@code deferred}.
     * 
     * @param deferred the deferred builders to return a builder from, may be <b>null</b> for non registered so far
     */
    public static void buildDeferred(Map<String, Builder<?>> deferred) {
        if (null != deferred) {
            for (Builder<?> b : deferred.values()) {
                b.build();
            }
            deferred.clear();
        }
    }

    /**
     * Returns a deferred builder.
     * 
     * @param <B> the builder type
     * @param shortId the short id
     * @param cls the builder type
     * @param deferred the deferred builders to return a builder from, may be <b>null</b> for non registered so far
     * @return the builder or <b>null</b> if no builder for {@code shortId} with the respective type is registered
     */
    public static <B extends Builder<?>> B getDeferred(String shortId, Class<B> cls, Map<String, Builder<?>> deferred) {
        B result = null;
        if (null != deferred) {
            Builder<?> tmp = deferred.get(shortId);
            if (null != tmp && cls.isInstance(tmp)) {
                result = cls.cast(tmp);
            }
        }
        return result;
    }

}
