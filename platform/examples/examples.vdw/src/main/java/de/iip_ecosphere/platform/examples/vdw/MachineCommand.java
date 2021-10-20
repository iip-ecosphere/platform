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
 * Some machine command for testing.
 * 
 * @author Holger Eichelberger, SSE
 */
public class MachineCommand {
    
    private boolean start;
    private boolean stop;
    private int lotSize;

    /**
     * Sets whether the machine shall be started.
     * 
     * @param start whether the machine shall be started
     */
    public void setStart(boolean start) {
        this.start = start;
    }
    
    /**
     * Sets whether the machine shall be stopped.
     * 
     * @param stop whether the machine shall be stopped
     */
    public void setStop(boolean stop) {
        this.stop = stop;
    }
    
    /**
     * Defines the lot size to produce with.
     * 
     * @param lotSize the lot size
     */
    public void setLotSize(int lotSize) {
        this.lotSize = lotSize;
    }

    /**
     * Returns whether the machine shall be started.
     * 
     * @return {@code true} shall be started
     */
    public boolean isStart() {
        return start;
    }

    /**
     * Returns whether the machine shall be stopped.
     * 
     * @return {@code true} shall be stoped
     */
    public boolean isStop() {
        return stop;
    }
    
    /**
     * The lot size to produce with.
     * 
     * @return the lot size
     */
    public int getLotSize() {
        return lotSize;
    }
    
}