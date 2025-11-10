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

package test.de.iip_ecosphere.platform.support;

import org.junit.Test;

import de.iip_ecosphere.platform.support.ObjectUtils;
import org.junit.Assert;

/**
 * Tests {@link ObjectUtils}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ObjectUtilsTest {

    /**
     * Data class as used in oktoflow.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class Base {

        private int iValue;
        private String sValue;
        
        /**
         * Returns the iValue.
         * 
         * @return the iValue
         */
        public int getIValue() {
            return iValue;
        }

        /**
         * Changes the iValue.
         * 
         * @param iValue the iValue to set
         */
        public void setIValue(int iValue) {
            this.iValue = iValue;
        }
        
        /**
         * Returns the sValue.
         * 
         * @return the sValue
         */
        public String getSValue() {
            return sValue;
        }
        
        /**
         * Changes the sValue.
         * 
         * @param sValue the sValue to set
         */
        public void setSValue(String sValue) {
            this.sValue = sValue;
        }
    }
    
    /**
     * Extended data class akin to refined oktoflow data objects.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class Extended extends Base {

        private String[] sArray;

        /**
         * Returns the sArray.
         * 
         * @return the sArray
         */
        public String[] getSArray() {
            return sArray;
        }

        /**
         * Changes the sArray.
         * 
         * @param sArray the sArray to set
         */
        public void setSArray(String[] sArray) {
            this.sArray = sArray;
        }

    }
    
    /**
     * Tests {@link ObjectUtils#copyFields(Object, Object)} and {@link ObjectUtils#copyFieldsSafe(Object, Object)}.
     */
    @Test
    public void testCopyFields() {
        // as created outside service
        Extended exIn = new Extended();
        exIn.setIValue(1);
        exIn.setSValue("a");
        exIn.setSArray(new String[] {"b", "c"});
        
        // as received inside service
        Base bIn = exIn;
        Extended exOut = new Extended(); // via abstraction, directly to Base, TBD
        Base bOut = exOut; 
        ObjectUtils.copyFieldsSafe(bIn, bOut);
        
        Assert.assertEquals(bOut.getIValue(), exIn.getIValue());
        Assert.assertEquals(bOut.getSValue(), exIn.getSValue());
        Assert.assertArrayEquals(exOut.getSArray(), exIn.getSArray());
    }

}
