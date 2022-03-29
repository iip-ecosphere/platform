package de.iip_ecosphere.platform.monitoring.prometheus.util;
/**
 * 
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

/** Help class for the config-file to better define start, finish and name of each section.
 * @author bettelsc
 *
 */
public class ConfigTriplet {
    
    private int start;
    private int end;
    private String name;
    
    /** Default constructor.
     * 
     */
    public ConfigTriplet() {}
    
    /** Constructor.
     * 
     * @param start
     * @param end
     * @param name
     */
    public ConfigTriplet(int start, int end, String name) {
        this.start = start;
        this.end = end;
        this.name = name;
    }
    /** getter for the start.
     * 
     * @return start
     */
    public int getStart() {
        return start;
    }
    /** setter for the start.
     * 
     * @param start
     */
    public void setStart(int start) {
        this.start = start;
    }
    /** getter for the end.
     * 
     * @return end
     */
    public int getEnd() {
        return end;
    }
    /** setter for the end.
     * 
     * @param end
     */
    public void setEnd(int end) {
        this.end = end;
    }
    /** getter for the name.
     * 
     * @return name
     */
    public String getName() {
        return name;
    }
    /** setter for the name.
     * 
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }
}
