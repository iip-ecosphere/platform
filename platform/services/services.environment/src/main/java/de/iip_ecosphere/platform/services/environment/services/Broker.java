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

package de.iip_ecosphere.platform.services.environment.services;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import de.iip_ecosphere.platform.support.CollectionUtils;
import de.iip_ecosphere.platform.support.FileUtils;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.transport.connectors.TransportConnector;
import de.iip_ecosphere.platform.transport.AbstractServer;

/**
 * Represents a broker process. We rely on a common application configuration and ignore that spring cloud streams
 * cannot/shall not start up for the broker (no option to disable).
 * 
 * @author Holger Eichelberger, SSE
 */
public class Broker {

    private static boolean serverConfigExtracted = false;
    private static Map<BrokerType, BrokerCreator> creators = new HashMap<>();
    private static List<Class<?>> brokerTypes = new ArrayList<>();
    private Server instance;

    /**
     * Broker type interface (extensible).
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface BrokerType {
    }
    
    /**
     * Broker creator.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class BrokerCreator {
        
        private String location;
        private String className;
        private String transportConnectorClsName;
        private String configProperty;
        private String[] binderName;
        private int defaultPort;
        
        // checkstyle: stop parameter number check
        
        /**
         * Creates a type constant.
         * 
         * @param location the server configuration location within the executing JAR, may be <b>null</b> or empty 
         *     for none
         * @param configProperty the config property determining the local port
         * @param className the name of the server class, created dynamically due to potential newer libs than JDK 1.8, 
         *   requires a constructor with ServerAddress as parameter
         * @param transportConnectorClsName the name of the transport connector
         * @param defaultPort the default network port
         * @param binderName the name(s) of the default binder determining the broker to start
         */
        public BrokerCreator(String location, String configProperty, String className, 
            String transportConnectorClsName, int defaultPort, String... binderName) {
            this.location = location;
            this.className = className;
            this.transportConnectorClsName = transportConnectorClsName;
            this.configProperty = configProperty;
            this.binderName = binderName;
            this.defaultPort = defaultPort;
        }

        // checkstyle: resume parameter number check

        /**
         * Returns the archive/packed configuration location.
         * 
         * @return the server configuration location within the executing JAR, may be <b>null</b> or empty for none
         */
        public String getArchiveLocation() {
            return null != location ? location.toLowerCase() + ".zip" : null; // maven archive name convention
        }

        /**
         * Returns the unpacked configuration location.
         * 
         * @return the server configuration location in development setup, may be <b>null</b> or empty for none
         */
        public File getUnpackedLocation() {
            return null != location ? new File("./src/test/", location) : null; // maven archive name convention
        }
        
        /**
         * Returns the name of the default binder determining the broker to start.
         * 
         * @return the name of the default binder
         */
        public String getBinderName() {
            return binderName[0];
        }
        
        /**
         * Returns whether this broker handles the given binder name.
         * 
         * @param name the binder name
         * @return {@code true} for handles, {@code false} for not supported
         */
        public boolean handlesBinder(String name) {
            boolean found = false;
            for (String n : binderName) {
                if (n.equals(name)) {
                    found = true;
                    break;
                }
            }
            return found;
        }
        
        /**
         * Returns the config property determining the local port.
         * 
         * @return the config property
         */
        public String getConfigProperty() {
            return configProperty;
        }
        
        /**
         * Returns the default port.
         * 
         * @return the default port
         */
        public int getDefaultPort() {
            return defaultPort;
        }
        
        /**
         * Creates a server instance.
         * 
         * @param addr the server address
         * @return the server instance
         */
        public Server createInstance(ServerAddress addr) {
            try {
                Class<?> cls = Class.forName(className);
                return (Server) cls.getConstructor(ServerAddress.class).newInstance(addr);
            } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException 
                | IllegalAccessException | InstantiationException e) {
                throw new IllegalArgumentException("Cannot instantiate Broker: " + e.getMessage());
            }
        }

        /**
         * Returns an associated (disconnected) transport connector instance.
         *  
         * @return the instance
         */
        public TransportConnector createTransportConnector() {
            try {
                Class<?> cls = Class.forName(transportConnectorClsName);
                return (TransportConnector) cls.getConstructor().newInstance();
            } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException 
                | IllegalAccessException | InstantiationException e) {
                throw new IllegalArgumentException("Cannot instantiate Broker: " + e.getMessage());
            }
        }
        
        /**
         * Whether we rely on TLS for transport encryption.
         * 
         * @return {@code true} for TLS, {@code false} for plain 
         */
        public boolean isTls() {
            return location.endsWith("Tls"); // by convention
        }
        
    }
    
    /**
     * Default broker types. Using projects must state respective dependencies.
     * 
     * @author Holger Eichelberger, SSE
     */
    public enum DefaultType implements BrokerType {
        
        HIVE_MQTT_V5(new BrokerCreator("hiveMqv5Cfg", "mqtt.port", 
            "test.de.iip_ecosphere.platform.test.mqtt.hivemq.TestHiveMqServer",
            "de.iip_ecosphere.platform.transport.mqttv3.PahoMqttV3TransportConnector",
            8883, "mqttv5Binder", "hivemqv5Binder")),
        HIVE_MQTT_V5_TLS(new BrokerCreator("hiveMqv5CfgTls", "mqtt.port", 
            "test.de.iip_ecosphere.platform.test.mqtt.hivemq.TestHiveMqServer",
            "de.iip_ecosphere.platform.transport.mqttv3.PahoMqttV3TransportConnector",
            8883, "mqttv5Binder", "hivemqv5Binder")),
        HIVE_MQTT_V3(new BrokerCreator("hiveMqv3Cfg", "mqtt.port", 
            "test.de.iip_ecosphere.platform.test.mqtt.hivemq.TestHiveMqServer", 
            "de.iip_ecosphere.platform.transport.mqttv5.PahoMqttV5TransportConnector",
            8883, "mqttv3Binder", "hivemqv3Binder")),
        HIVE_MQTT_V3_TLS(new BrokerCreator("hiveMqv3CfgTls", "mqtt.port", 
            "test.de.iip_ecosphere.platform.test.mqtt.hivemq.TestHiveMqServer", 
            "de.iip_ecosphere.platform.transport.mqttv5.PahoMqttV5TransportConnector",
            8883, "mqttv3Binder", "hivemqv3Binder")),
        QPID(new BrokerCreator("qpidCfg", "amqp.port", 
            "test.de.iip_ecosphere.platform.test.amqp.qpid.TestQpidServer", 
            "de.iip_ecosphere.platform.transport.connectors.rabbitmq.RabbitMqAmqpTransportConnector",
            8883, "amqpBinder")),
        QPID_TLS(new BrokerCreator("qpidCfgTls", "amqp.port", 
            "test.de.iip_ecosphere.platform.test.amqp.qpid.TestQpidServer", 
            "de.iip_ecosphere.platform.transport.connectors.rabbitmq.RabbitMqAmqpTransportConnector",
            8883, "amqpBinder")),
        MOQUETTE_MQTT_V3(new BrokerCreator(null, "mqtt.port", 
             "test.de.iip_ecosphere.platform.test.mqtt.moquette.TestMoquetteServer", 
             "de.iip_ecosphere.platform.transport.mqttv3.PahoMqttV3TransportConnector", 8883, 
            "mqttv3Binder"));
        
        /**
         * Creates a type constant.
         * 
         * @param creator the associated broker creator, may be <b>null</b> but then no broker will be created later
         */
        private DefaultType(BrokerCreator creator) {
            registerType(this, creator);
        }
        
    }
    
    /**
     * Creates a broker instance and attaches a shutdown hook.
     */
    public Broker() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> this.stopInstance()));
    }

    /**
     * Registers a {@code type} with it's creator. Call e.g. in constructor of type enum.
     * 
     * @param type the type, may be <b>null</b>, ignored then
     * @param creator the creator, may be <b>null</b> but then nothing will be created later
     */
    public static void registerType(BrokerType type, BrokerCreator creator) {
        if (null != type) {
            creators.put(type, creator);
            if (!brokerTypes.contains(type.getClass())) {
                brokerTypes.add(type.getClass());
            }
        }
    }
    
    /**
     * Returns all known/registered types. There are not necessarily creators for these types.
     * 
     * @return the types as unmodifiable list
     */
    public static List<BrokerType> allTypes() {
        List<BrokerType> result = new ArrayList<>();
        CollectionUtils.addAll(result, DefaultType.values()); // for order
        for (BrokerType t : creators.keySet()) { // remainders
            if (!result.contains(t)) {
                result.add(t);
            }
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Returns the object value of {@code value}.
     * 
     * @param value the value, may be <b>null</b>
     * @return the object value or <b>null</b> for unknown
     */
    public static BrokerType valueOf(String value) {
        BrokerType result = null;
        if (null != value) {
            for (Class<?> c : brokerTypes) {
                if (c.isEnum()) {
                    try {
                        Field f = c.getField(value);
                        int mod = f.getModifiers();
                        if (Modifier.isStatic(mod) && Modifier.isPublic(mod) && Modifier.isFinal(mod)) {
                            result = (BrokerType) f.get(null);
                        }
                    } catch (NoSuchFieldException | ClassCastException | IllegalAccessException e) {
                    }
                }
            }
        }
        return result;
    }
    
    /**
     * Executes an operation on the associated broker creator and returns the result.
     * 
     * @param <R> the result type
     * @param type the broker type
     * @param func the function to execute
     * @return the result or <b>null</b> if no broker creator/{@code func} are present
     */
    public static <R> R onCreator(BrokerType type, Function<BrokerCreator, R> func) {
        R result = null;
        BrokerCreator creator = creators.get(type);
        if (null != creator) {
            if (null != func) {
                result = func.apply(creator);
            }
        } else {
            System.err.println("No/invalid broker creator for " + type + ". Provided function ignored.");
        }
        return result;
    }
    
    /**
     * Defines/overrides the current instance.
     * 
     * @param inst the instance
     */
    protected void setInstance(Server inst) {
        instance = inst;
    }

    /**
     * Creates a server instance.
     * 
     * @param type the server type
     * @param port the port number
     * @return the server instance
     */
    public static Server createInstance(BrokerType type, int port) {
        extractServerConfig(type);
        final ServerAddress addr = new ServerAddress(Schema.IGNORE, port);
        System.out.println("Starting " + type + " broker server on port " + addr.getPort());
        return onCreator(type, c -> c.createInstance(addr));
    }

    /**
     * Creates a transport connector instance.
     * 
     * @param type the server type
     * @return the transport connector instance
     */
    public static TransportConnector createTransportConnector(BrokerType type) {
        return onCreator(type, c -> c.createTransportConnector());
    }
    
    /**
     * Tries to extract the server configuration.
     * 
     * @param type the server type carrying the config location
     */
    public static void extractServerConfig(BrokerType type) {
        if (!serverConfigExtracted) {
            try {
                String loc = onCreator(type, c -> c.getArchiveLocation());
                if (AbstractServer.runsFromJar() && null != loc && loc.length() > 0) {
                    System.out.println("Extracting server configuration from " + loc);
                    AbstractServer.setConfigDir(FileUtils.createTmpFolder("brokerConfig"));
                    AbstractServer.extractConfiguration(loc, "");
                } else { // dev execution, unpacked
                    File unpackedLoc = onCreator(type, c -> c.getUnpackedLocation());
                    AbstractServer.setConfigDir(unpackedLoc); // null is ok
                }
            } catch (IOException e) {
                System.err.println("Cannot find/extract server configuration: " + e.getMessage());
            }
            serverConfigExtracted = true;
        }
    }
    
    /**
     * Returns the TLS keystore file. Requires {@link #extractServerConfig(Type)} before. Only valid if TLS enabled.
     * 
     * @return the keystore file
     */
    
    // checkstyle: stop exception type check
    
    /**
     * Stops the broker instance.
     */
    public void stopInstance() {
        if (null != instance) {
            try {
                instance.stop(true);
            } catch (Throwable t) {
            }
        }
    }

    // checkstyle: resume exception type check

}
