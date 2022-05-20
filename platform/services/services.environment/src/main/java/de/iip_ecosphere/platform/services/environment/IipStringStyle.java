/**
 * ******************************************************************************
 * Copyright (c) {2022} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.services.environment;

import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Specific styles for {@link org.apache.commons.lang3.builder.ReflectionToStringBuilder}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class IipStringStyle {
    
    /**
     * Short prefix style with limited string output.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static final class ShortStringToStringStyle extends ToStringStyle {
        
        private static final long serialVersionUID = 1L;

        /**
         * <p>Constructor.</p>
         *
         * <p>Use the static constant rather than instantiating.</p>
         */
        ShortStringToStringStyle() {
            super();
            this.setUseShortClassName(true);
            this.setUseIdentityHashCode(false);
        }
        
        @Override
        public void append(StringBuffer buffer, String fieldName, Object value, Boolean fullDetail) {
            if (value instanceof String) { // in particular base64 images
                String sVal = (String) value;
                if (sVal.length() > 20) {
                    value = sVal.substring(0, 20) + "...";
                }
            }
            super.append(buffer, fieldName, value, fullDetail);
        }

        /**
         * <p>Ensure singleton after serialization.</p>
         * @return the singleton
         */
        private Object readResolve() {
            return SHORT_STRING_STYLE;
        }
        
    }
    
    /**
     * Short prefix style with limited string output.
     */
    public static final ToStringStyle SHORT_STRING_STYLE = new ShortStringToStringStyle(); 
    
}
