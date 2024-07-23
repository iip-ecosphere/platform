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
 * Implements the output datatype from the connected machine to oktoflow for the Sentron function test.
 * 
 * @author Christian Nikolajew
 */
public class SentronFunctionTest {

    private long betriebsstundenzaehler;
    private long universalzaehler;
    private long impulszaehler;
    private float spannungL1L3;
    private float spannungL2L3;
    private float spannungL3L1;
    
    /**
     * Construvtor.
     */
    public SentronFunctionTest() {
        
    }
    
    /**
     * Getter for betriebsstundenzaehler.
     * 
     * @return the value of betriebsstundenzaehler
     */
    public long getBetriebsstundenzaehler() {
        return betriebsstundenzaehler;
    }
    
    /**
     * Getter for universalzaehler.
     * 
     * @return the value of universalzaehler
     */
    public long getUniversalzaehler() {
        return universalzaehler;
    }
    
    /**
     * Getter for impulszaehler.
     * 
     * @return the value of impulszaehler
     */
    public long getImpulszaehler() {
        return impulszaehler;
    }
    
    /**
     * Getter for spannungL1L3.
     * 
     * @return the value of spannungL1L3
     */
    public float getSpannungL1L3() {
        return spannungL1L3;
    }
    
    /**
     * Getter for spannungL2L3.
     * 
     * @return the value of spannungL2L3
     */
    public float getSpannungL2L3() {
        return spannungL2L3;
    }
    
    /**
     * Getter for spannungL3L1.
     * 
     * @return the value of spannungL3L1
     */
    public float getSpannungL3L1() {
        return spannungL3L1;
    }
    
    /**
     * Setter for betriebsstundenzaehler. 
     * 
     * @param val the int to set
     */
    public void setBetriebsstundenzaehler(long val) {
        betriebsstundenzaehler = val;
    }
    
    /**
     * Setter for universalzaehler. 
     * 
     * @param val the int to set
     */
    public void setUniversalzaehler(long val) {
        universalzaehler = val;
    }
    
    /**
     * Setter for impulszaehler. 
     * 
     * @param val the int to set
     */
    public void setImpulszaehler(long val) {
        impulszaehler = val;
    }
    
    /**
     * Setter for spannungL1L3. 
     * 
     * @param val the float to set
     */
    public void setSpannungL1L3(float val) {
        spannungL1L3 = val;
    }
    
    /**
     * Setter for spannungL2L3. 
     * 
     * @param val the float to set
     */
    public void setSpannungL2L3(float val) {
        spannungL2L3 = val;
    }
    
    /**
     * Setter for spannungL3L1. 
     * 
     * @param val the float to set
     */
    public void setSpannungL3L1(float val) {
        spannungL3L1 = val;
    }
    
    /**
     * Creates the serverSettings for the Sentron function test.
     * 
     * @return the serverSettings as String
     */
    public static String createServerSettings() {

        String serverSettings = "{";
        serverSettings += "\"Betriebsstundenzaehler\" : {\"offset\" : 213, \"type\" : \"uinteger\"},";
        serverSettings += "\"Universalzaehler\" : {\"offset\" : 215, \"type\" : \"uinteger\"},";
        serverSettings += "\"Impulszaehler 0\" : {\"offset\" : 373, \"type\" : \"uinteger\"},";
        serverSettings += "\"Spannung L1-L3\" : {\"offset\" : 7, \"type\" : \"float\"},";
        serverSettings += "\"Spannung L2-L3\" : {\"offset\" : 9, \"type\" : \"float\"},";
        serverSettings += "\"Spannung L3-L1\" : {\"offset\" : 11, \"type\" : \"float\"}";
        serverSettings += "}";
        
        return serverSettings;
    }
    
    /**
     * Creates the connector adapter. 
 
     * @return the connector adapter
     */
    private static TranslatingProtocolAdapter<ModbusItem, Object, SentronFunctionTest, SentronFunctionTestRw> 
        createConnectorAdapter() {
        
        TranslatingProtocolAdapter<ModbusItem, Object, SentronFunctionTest, SentronFunctionTestRw> adapter;
        
        adapter = new TranslatingProtocolAdapter<ModbusItem, Object, SentronFunctionTest, SentronFunctionTestRw>(
                new SentronFunctionTestOutputTranslator<ModbusItem>(false, ModbusItem.class),
                new SentronFunctionTestInputTranslator<Object>(Object.class));
        
        return adapter;
    }
    
    /**
     * Creates a ModbusTcpIpConnector instance for the Sentron function test.
     * 
     * @return a ModbusTcpIpConnector instance for the Sentron function test
     */
    public static ModbusTcpIpConnector<SentronFunctionTest, SentronFunctionTestRw> createFunctionTestConnector() {

        ModbusTcpIpConnector<SentronFunctionTest, SentronFunctionTestRw> connector = new ModbusTcpIpConnector<>(
                createConnectorAdapter());
        
        return connector;
    }
}
