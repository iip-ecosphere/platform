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

import de.iip_ecosphere.platform.support.metrics.Tag;

/**
 * Wraps a tag.
 * 
 * @author Holger Eichelberger, SSE
 */
class MicrometerTag implements Tag {

    private io.micrometer.core.instrument.Tag tag;

    /**
     * Creates a wrapped tag.
     * 
     * @param tag the tag
     */
    MicrometerTag(io.micrometer.core.instrument.Tag tag) {
        this.tag = tag;
    }
    
    @Override
    public String getKey() {
        return tag.getKey();
    }

    @Override
    public String getValue() {
        return tag.getValue();
    }

    @Override
    public int hashCode() {
        return tag.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return other == this || tag.equals(
            other instanceof MicrometerTag ? ((MicrometerTag) other).tag : other);
    }
    
    /**
     * Returns the wrapped/implementing tag.
     * 
     * @return the tag
     */
    public io.micrometer.core.instrument.Tag getTag() {
        return tag;
    }
    
}
