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

/**
 * Builder interface.
 * 
 * @param <B> the builder type
 * @author Holger Eichelberger, SSE
 */
public interface MetricsBuilder<M extends Meter, B extends MetricsBuilder<M, B>> {

    /**
     * Adds tags to the object to build.
     * 
     * @param tags must be an even number of arguments representing key/value pairs of tags
     * @return <b>this</b> for chaining
     */
    public B tags(String... tags);
    
    /**
     * Adds a description to the object to build.
     * 
     * @param description Description text of the eventual timer, may be <b>null</b>.
     * @return <b>this</b> for chaining
     */
    public B description(String description);

    /**
     * Add the meter to be built to a single registry, or returns an meter in that registry. The returned
     * meter will be unique for each registry, but each registry is guaranteed to only create one meter
     * for the same combination of name and tags.
     *
     * @param registry a registry to add the meter to, if it doesn't already exist.
     * @return a new or existing meter
     */
    public M register(MeterRegistry registry);
    
}
