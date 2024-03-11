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

/**
 * A data element representing a file.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface Range extends DataElement {

    /**
     * Builds a data element.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface RangeBuilder extends DataElementBuilder<Range> {
    }
    
    /**
     * Returns the minimum value.
     * 
     * @return the minimum value.
     */
    public Object getMin();

    /**
     * Changes the minimum value.
     * 
     * @param min the minimum value.
     */
    public void setMin(Object min);

    /**
     * Returns the maximum value.
     * 
     * @return the maximum value.
     */
    public Object getMax();

    /**
     * Changes the maximum value.
     * 
     * @param max the maximum value.
     */
    public void setMax(Object max);

    /**
     * Returns the value type.
     * 
     * @return the type
     */
    public Type getType();

}
