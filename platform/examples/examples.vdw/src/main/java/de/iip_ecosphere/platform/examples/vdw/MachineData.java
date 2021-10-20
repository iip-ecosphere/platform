/**
 * ******************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.examples.vdw;

/**
 * Test class representing two example sub-trees of machine data from the VDW OPC UA information model. 
 * 
 * Plan: This class shall be generated from the configuration model.
 * 
 * @author Holger Eichelberger, SSE
 */
public class MachineData {

    private BasicWoodworking basicWoodworking;
    private FullMachineToolDynamic fullMachineToolDynamic;
    
    /**
     * Creates a new machine data instance.
     * 
     * @param basicWoodworking the basic woodworking instance
     * @param fullMachineToolDynamic the full machine tool dynamic instance
     */
    public MachineData(BasicWoodworking basicWoodworking, FullMachineToolDynamic fullMachineToolDynamic) {
        this.basicWoodworking = basicWoodworking;
        this.fullMachineToolDynamic = fullMachineToolDynamic;
    }
    
    /**
     * Represents some data from the basic woodworking.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class BasicWoodworking {

        private int currentMode;
        private int currentState;

        /**
         * Creates a machine data object.
         * 
         * @param currentMode the current mode of the machine
         * @param currentState the current state
         */
        public BasicWoodworking(int currentMode, int currentState) {
            this.currentMode = currentMode;
            this.currentState = currentState;
        }

        /**
         * Returns the current mode of the machine.
         * 
         * @return the current mode
         */
        public int getCurrentMode() {
            return currentMode;
        }

        /**
         * Returns the current state.
         * 
         * @return the current state
         */
        public double getCurrentState() {
            return currentState;
        }
        
        @Override
        public String toString() {
            return "BasicWoodworking (mode=" + getCurrentMode() + ", state=" + getCurrentState() + ")";
        }

    }
    
    /**
     * From the FullMachineToolDynamic. We could go more into the dynamic data, but this also requires a basic 
     * understanding.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class FullMachineToolDynamic {
        
        private int myJob1State;
        
        /**
         * Creates an instance.
         * 
         * @param myJob1State the job1 state
         */
        public FullMachineToolDynamic(int myJob1State) {
            this.myJob1State = myJob1State;
        }
        
        /**
         * Returns the job1 state.
         * 
         * @return the job1 state
         */
        public int getMyJob1State() {
            return myJob1State;
        }

        @Override
        public String toString() {
            return "FullMachineToolDynamic (myJob1State=" + getMyJob1State() + ")"; 
        }

    }

    /**
     * Returns the basic woodworking instance.
     * 
     * @return the instance
     */
    public BasicWoodworking getBasicWoodworking() {
        return basicWoodworking;
    }

    /**
     * Returns the full machine tool dynamic.
     * 
     * @return the instance
     */
    public FullMachineToolDynamic getFullMachineToolDynamic() {
        return fullMachineToolDynamic;
    }

    @Override
    public String toString() {
        String result = getBasicWoodworking().toString();
        if (null != getFullMachineToolDynamic()) {
            result += ";" + getFullMachineToolDynamic();
        }
        return result; 
    }

}
