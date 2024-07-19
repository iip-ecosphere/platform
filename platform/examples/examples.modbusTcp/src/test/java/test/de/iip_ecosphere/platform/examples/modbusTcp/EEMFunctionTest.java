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

import de.iip_ecosphere.platform.connectors.modbustcpipv1.ModbusItem;
import de.iip_ecosphere.platform.connectors.modbustcpipv1.ModbusTcpIpConnector;
import de.iip_ecosphere.platform.connectors.types.TranslatingProtocolAdapter;

/**
 * Implements the output datatype from the connected machine to oktoflow for the EEM function test.
 * 
 * @author Christian Nikolajew
 */
public class EEMFunctionTest {

    private short day;
    private short month;
    private short year;
    private float u12;
    private float u23;
    private float u31;

    /**
     * Constructor.
     */
    public EEMFunctionTest() {

    }

    /**
     * Getter for day.
     * 
     * @return the value of day
     */
    public short getDay() {
        return day;
    }

    /**
     * Getter for month.
     * 
     * @return the value of month
     */
    public short getMonth() {
        return month;
    }

    /**
     * Getter for year.
     * 
     * @return the value of year
     */
    public short getYear() {
        return year;
    }

    /**
     * Getter for u12.
     * 
     * @return the value of u12
     */
    public float getU12() {
        return u12;
    }

    /**
     * Getter for u23.
     * 
     * @return the value of u23
     */
    public float getU23() {
        return u23;
    }

    /**
     * Getter for u31.
     * 
     * @return the value of u31
     */
    public float getU31() {
        return u31;
    }

    /**
     * Setter for day.
     * 
     * @param val the short to set
     */
    public void setDay(short val) {
        day = val;
    }

    /**
     * Setter for month.
     * 
     * @param val the short to set
     */
    public void setMonth(short val) {
        month = val;
    }

    /**
     * Setter for year.
     * 
     * @param val the short to set
     */
    public void setYear(short val) {
        year = val;
    }

    /**
     * Setter for u12.
     * 
     * @param val the float to set
     */
    public void setU12(float val) {
        u12 = val;
    }

    /**
     * Setter for u23.
     * 
     * @param val the float to set
     */
    public void setU23(float val) {
        u23 = val;
    }

    /**
     * Setter for u31.
     * 
     * @param val the float to set
     */
    public void setU31(float val) {
        u31 = val;
    }

    /**
     * Creates the serverSettings for the EEM function test.
     * 
     * @return the serverSettings as String
     */
    public static String createServerSettings() {

        String serverSettings = "{";
        serverSettings += "\"Day\" : {\"offset\" : 1282, \"type\" : \"ushort\"},";
        serverSettings += "\"Month\" : {\"offset\" : 1283, \"type\" : \"ushort\"},";
        serverSettings += "\"Year\" : {\"offset\" : 1284, \"type\" : \"ushort\"},";
        serverSettings += "\"U12\" : {\"offset\" : 32768, \"type\" : \"float\"},";
        serverSettings += "\"U23\" : {\"offset\" : 32770, \"type\" : \"float\"},";
        serverSettings += "\"U31\" : {\"offset\" : 32772, \"type\" : \"float\"}";
        serverSettings += "}";
        
        return serverSettings;
    }

    /**
     * Creates the connector adapter. 
 
     * @return the connector adapter
     */
    private static TranslatingProtocolAdapter<ModbusItem, Object, EEMFunctionTest, EEMFunctionTestRw> 
        createConnectorAdapter() {
        
        TranslatingProtocolAdapter<ModbusItem, Object, EEMFunctionTest, EEMFunctionTestRw> adapter;
        
        adapter = new TranslatingProtocolAdapter<ModbusItem, Object, EEMFunctionTest, EEMFunctionTestRw>(
                new EEMFunctionTestOutputTranslator<ModbusItem>(false, ModbusItem.class),
                new EEMFunctionTestInputTranslator<Object>(Object.class));
        
        return adapter;
    }

    /**
     * Creates a ModbusTcpIpConnector instance for the EEM function test.
     * 
     * @return a ModbusTcpIpConnector instance for the EEM function test
     */
    public static ModbusTcpIpConnector<EEMFunctionTest, EEMFunctionTestRw> createFunctionTestConnector() {

        ModbusTcpIpConnector<EEMFunctionTest, EEMFunctionTestRw> connector = new ModbusTcpIpConnector<>(
                createConnectorAdapter());
        
        return connector;
    }

}
