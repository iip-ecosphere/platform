/**
 * ******************************************************************************
 * Copyright (c) {2024} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.examples.modbusTcp;

/**
 * Implements the input datatype from oktoflow to the connected machine for the Sentron function test.
 * 
 * @author Christian Nikolajew
 */
public class SentronFunctionTestRw {

    private Long betriebsstundenzaehler = null;
    private Long universalzaehler = null;
    private Long impulszaehler = null;
    
    /**
     * Constructor.
     */
    public SentronFunctionTestRw() {
        
    }
    
    /**
     * Getter for betriebsstundenzaehler.
     * 
     * @return the value of betriebsstundenzaehler
     */
    public Long getBetriebsstundenzaehler() {
        return betriebsstundenzaehler;
    }
    
    /**
     * Getter for universalzaehler.
     * 
     * @return the value of universalzaehler
     */
    public Long getUniversalzaehler() {
        return universalzaehler;
    }
    
    /**
     * Getter for impulszaehler.
     * 
     * @return the value of impulszaehler
     */
    public Long getImpulszaehler() {
        return impulszaehler;
    }
    
    /**
     * Setter for betriebsstundenzaehler. 
     * 
     * @param val the int to set
     */
    public void setBetriebsstundenzaehler(Long val) {
        betriebsstundenzaehler = val;
    }
    
    /**
     * Setter for universalzaehler. 
     * 
     * @param val the int to set
     */
    public void setUniversalzaehler(Long val) {
        universalzaehler = val;
    }
    
    /**
     * Setter for impulszaehler. 
     * 
     * @param val the int to set
     */
    public void setImpulszaehler(Long val) {
        impulszaehler = val;
    }
}
