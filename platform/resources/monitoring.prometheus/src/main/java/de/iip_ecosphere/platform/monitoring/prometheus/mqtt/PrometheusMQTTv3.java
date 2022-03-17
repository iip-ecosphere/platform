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

package de.iip_ecosphere.platform.monitoring.prometheus.mqtt;

import java.io.IOException;


import de.iip_ecosphere.platform.monitoring.prometheus.Callback;
import de.iip_ecosphere.platform.monitoring.prometheus.TestObject;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.transport.connectors.TransportParameter;
import de.iip_ecosphere.platform.transport.connectors.TransportParameter.TransportParameterBuilder;
import de.iip_ecosphere.platform.transport.mqttv3.PahoMqttV3TransportConnector;

/** PrometheusMQTTv3 test.
 * 
 * @author const
 *
 */
public class PrometheusMQTTv3 {
    private PahoMqttV3TransportConnector connector;
    private TransportParameterBuilder builder;
    private TransportParameter params;
    @SuppressWarnings("unused")
    private String fromstream;
    @SuppressWarnings("unused")
    private String fromstreamname;
    @SuppressWarnings("unused")
    private String fromstreamparent;
    @SuppressWarnings("unused")
    private String tostream;
    @SuppressWarnings("unused")
    private String tostreamname;
    @SuppressWarnings("unused")
    private String tostreamparent;
    private Callback callback;
    private Object object;
    private String applicationId;
    
    /** Default Constructor.
     * 
     */
    public PrometheusMQTTv3() {}
    
    /** init-methode.
     * 
     * @param broker_ip
     * @param port
     */
    public void init(String broker_ip, int port) {
        setConnector(new PahoMqttV3TransportConnector());
        setBuilder(TransportParameterBuilder.newBuilder(broker_ip, port));
    }
    /** executes a sample connector.
     * 
     */
    public static void executeConnector() {
        PahoMqttV3TransportConnector cl1 = new PahoMqttV3TransportConnector();
        TransportParameterBuilder builder = TransportParameterBuilder
                .newBuilder("localhost", 9321)
                .setApplicationId("cl1");
        TransportParameter param1 = builder.build();
        try {
            cl1.connect(param1);
            System.out.println("Connector .toString Methode: " + cl1.toString());
            System.out.println("Connector .getName Metheode: " + cl1.getName());
            final String stream1 = cl1.composeStreamName("", "stream1");
            final String stream2 = cl1.composeStreamName("", "stream2");
            final Callback cb1 = new Callback();
            cl1.setReceptionCallback(stream2, cb1);
            
            TransportParameterBuilder builder2 = TransportParameterBuilder
                    .newBuilder("localhost", 9321)
                    .setApplicationId("cl2");
            TransportParameter param2 = builder2.build();
            PahoMqttV3TransportConnector cl2 = new PahoMqttV3TransportConnector();
            System.out.println("Connecting connector 2 to: " + "localhost" + ":" + 9321);
            cl2.connect(param2);
            final Callback cb2 = new Callback();
            cl2.setReceptionCallback(stream1, cb2);
            TestObject obj1 = new TestObject("obj1", 10);
            TestObject obj2 = new TestObject("obj2", 20);
            System.out.println("Sending/Receiving");
            cl1.syncSend(stream1, obj1);
            cl2.syncSend(stream2, obj2);
            TimeUtils.sleep(2000);
            System.out.println("Cleaning up");
            System.out.println("Waiting 20 seconds...");
            TimeUtils.sleep(20000);
            cl1.disconnect();
            cl2.disconnect();
            System.out.println("Connectors disconnected!");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    /** getConnector.
     * 
     * @return connector
     */
    public PahoMqttV3TransportConnector getConnector() {
        return connector;
    }

    /** setter connector.
     * 
     * @param connector
     */
    public void setConnector(PahoMqttV3TransportConnector connector) {
        this.connector = connector;
    }
    
    /** getter builder.
     * 
     * @return builder
     */
    public TransportParameterBuilder getBuilder() {
        return builder;
    }
    
    /** setter builder.
     * 
     * @param builder
     */
    public void setBuilder(TransportParameterBuilder builder) {
        this.builder = builder;
    }
    
    /** Getter object.
     * 
     * @return object
     */
    public Object getObject() {
        return object;
    }
    
    /** setter Object.
     * 
     * @param object
     */
    public void setObject(Object object) {
        this.object = object;
    }
    
    /** getter Callback.
     * 
     * @return callback
     */
    public Callback getCallback() {
        return callback;
    }
    
    /** Setter Callback.
     * 
     * @param callback
     */
    public void setCallback(Callback callback) {
        this.callback = callback;
    }
    
    /** Getter appId.
     *  
     * @return applicationId
     */
    public String getApplicationId() {
        return applicationId;
    }
    
    /** Setter AppId.
     * 
     * @param applicationId
     */
    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }
    
    /** Getter TransportParams.
     * 
     * @return params
     */
    public TransportParameter getParams() {
        return params;
    }
}
