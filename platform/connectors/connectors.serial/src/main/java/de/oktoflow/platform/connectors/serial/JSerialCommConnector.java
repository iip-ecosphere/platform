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

package de.oktoflow.platform.connectors.serial;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListenerWithExceptions;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortInvalidPortException;

import de.iip_ecosphere.platform.connectors.AbstractChannelConnector;
import de.iip_ecosphere.platform.connectors.ChannelAdapterSelector;
import de.iip_ecosphere.platform.connectors.ConnectorDescriptor;
import de.iip_ecosphere.platform.connectors.ConnectorParameter;
import de.iip_ecosphere.platform.connectors.MachineConnector;
import de.iip_ecosphere.platform.connectors.types.ChannelProtocolAdapter;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;

/**
 * Implements the generic serial connector. Requires the port descriptor/name in {@link ConnectorParameter#getHost()}. 
 * Do not rename, this class is referenced in {@code META-INF/services}.
 * 
 * This implementation is potentially not thread-safe, i.e., it may require a sending queue.
 * 
 * Accepts the following specific settings:
 * <ul>
 *   <li>BAUDRATE: Integer (default 9600)</li>
 *   <li>DATABITS: Integer (default 8)</li>
 *   <li>STOPBITS: Integer (default 1)</li>
 *   <li>PARITY: "NO", "EVEN", "ODD", "MARK", "SPACE" (default "NO")</li>
 * </ul>
 * 
 * @param <CO> the output type to the oktoflow platform
 * @param <CI> the input type from the oktoflow platform
 * @author Holger Eichelberger, SSE
 */
@MachineConnector(hasModel = false, supportsEvents = true, supportsHierarchicalQNames = false, 
    supportsModelCalls = false, supportsModelProperties = false, supportsModelStructs = false,
    specificSettings = {"BAUDRATE", "DATABITS", "STOPBITS", "PARITY"})
public class JSerialCommConnector<CO, CI> extends AbstractChannelConnector<byte[], byte[], CO, CI> {

    public static final String NAME = "Serial";
    private static final Logger LOGGER = Logger.getLogger(JSerialCommConnector.class.getName());
    private SerialPort port;

    /**
     * The descriptor of this connector (see META-INF/services).
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class Descriptor implements ConnectorDescriptor {

        @Override
        public String getName() {
            return NAME;
        }

        @Override
        public Class<?> getType() {
            return JSerialCommConnector.class;
        }
        
    }
    
    /**
     * Creates a connector instance.
     * 
     * @param adapter the protocol adapter(s)
     * @throws IllegalArgumentException if {@code adapter} is <b>null</b> or empty or adapters are <b>null</b>
     */
    @SafeVarargs
    public JSerialCommConnector(ChannelProtocolAdapter<byte[], byte[], CO, CI>... adapter) {
        this(null, adapter);
    }

    /**
     * Creates a connector instance.
     * 
     * @param selector the adapter selector (<b>null</b> leads to a default selector for the first adapter)
     * @param adapter the protocol adapter(s)
     * @throws IllegalArgumentException if {@code adapter} is <b>null</b> or empty or adapters are <b>null</b>
     */
    @SafeVarargs
    public JSerialCommConnector(ChannelAdapterSelector<byte[], byte[], CO, CI> selector, 
        ChannelProtocolAdapter<byte[], byte[], CO, CI>... adapter) {
        super(selector, adapter);
    }

    /**
     * The internal reception callback.
     * 
     * @author Holger Eichelberger, SSE
     */
    private class Callback implements SerialPortDataListenerWithExceptions {
        
        private String portDescriptor;

        /**
         * Creates a callback.
         * 
         * @param portDescriptor the descriptor used to create the port
         */
        private Callback(String portDescriptor) {
            this.portDescriptor = portDescriptor;
        }

        @Override
        public int getListeningEvents() {
            return SerialPort.LISTENING_EVENT_DATA_RECEIVED;
        }

        @Override
        public void serialEvent(SerialPortEvent event) {
            if (SerialPort.LISTENING_EVENT_DATA_RECEIVED == event.getEventType()) {
                try {
                    received(portDescriptor, event.getReceivedData());
                } catch (IOException e) {
                    LoggerFactory.getLogger(getClass()).error("When receiving serial message: {}", e.getMessage(), e);
                }
            }
        }

        @Override
        public void catchException(Exception ex) {
            LoggerFactory.getLogger(getClass()).error("When receiving serial message: {}", ex.getMessage(), ex);
        }

    }
    
    
    @Override
    protected void connectImpl(ConnectorParameter params) throws IOException {
        try {
            String portDescriptor = params.getHost();
            LoggerFactory.getLogger(getClass()).info("Connecting to serial port: {}", portDescriptor);
            port = SerialPort.getCommPort(portDescriptor);
            port.addDataListener(new Callback(portDescriptor));
            port.setFlowControl(SerialPort.FLOW_CONTROL_RTS_ENABLED);
            port.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING, 500, 500);
            params.setSpecificIntSetting("BAUDRATE", i -> port.setBaudRate(i));
            params.setSpecificIntSetting("DATABITS", i -> port.setNumDataBits(i));
            params.setSpecificIntSetting("STOPBITS", i -> port.setNumStopBits(i));
            String parity = params.getSpecificStringSetting("PARITY");
            if (null != parity && parity.length() > 0) {
                switch (parity) {
                case "NO":
                    port.setParity(SerialPort.NO_PARITY);
                    break;
                case "EVEN":
                    port.setParity(SerialPort.EVEN_PARITY);
                    break;
                case "ODD":
                    port.setParity(SerialPort.ODD_PARITY);
                    break;
                case "MARK":
                    port.setParity(SerialPort.MARK_PARITY);
                    break;
                case "SPACE":
                    port.setParity(SerialPort.SPACE_PARITY);
                    break;
                default:
                    LoggerFactory.getLogger(JSerialCommConnector.class).warn("Unsupported party value: {}" + parity);
                    break;
                }
            }
            boolean connected = port.openPort();
            if (!connected) {
                throw new IOException("Not connected to serial port: " + portDescriptor);
            }
            LoggerFactory.getLogger(getClass()).info("Connected to serial port: {}", portDescriptor);
        } catch (SerialPortInvalidPortException e) {
            throw new IOException(e);
        }
    }

    @Override
    protected void disconnectImpl() throws IOException {
        if (null != port && port.isOpen()) {
            port.closePort();
            LoggerFactory.getLogger(getClass()).info("Closed serial port: {}", port.getDescriptivePortName());
        }
    }

    @Override
    public void dispose() {
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    protected void writeImpl(byte[] data, String channel) throws IOException {
        if (port.writeBytes(data, data.length) < 0) {
            throw new IOException("Data not written to serial port");
        }
        TimeUtils.sleep(20); // for now, blocking does not separate "messages"
    }

    @Override
    protected byte[] read() throws IOException {
        return null; // no polling at all needed
    }

    @Override
    protected void error(String message, Throwable th) {
        LOGGER.log(Level.SEVERE, message, th);
    }

    @Override
    public String supportedEncryption() {
        return null;
    }

    @Override
    public String enabledEncryption() {
        return null;
    }

}
