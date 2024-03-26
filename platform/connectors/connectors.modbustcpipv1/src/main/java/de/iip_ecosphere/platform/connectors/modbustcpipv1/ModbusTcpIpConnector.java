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

package de.iip_ecosphere.platform.connectors.modbustcpipv1;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.connectors.AbstractConnector;
import de.iip_ecosphere.platform.connectors.AdapterSelector;
import de.iip_ecosphere.platform.connectors.ConnectorDescriptor;
import de.iip_ecosphere.platform.connectors.ConnectorParameter;
import de.iip_ecosphere.platform.connectors.MachineConnector;
import de.iip_ecosphere.platform.connectors.events.ConnectorTriggerQuery;
import de.iip_ecosphere.platform.connectors.model.AbstractModelAccess;
import de.iip_ecosphere.platform.connectors.model.ModelAccess;
import de.iip_ecosphere.platform.connectors.model.ModelInputConverter;
import de.iip_ecosphere.platform.connectors.model.ModelOutputConverter;
import de.iip_ecosphere.platform.connectors.types.ProtocolAdapter;
import net.wimpi.modbus.ModbusException;
import net.wimpi.modbus.ModbusIOException;
import net.wimpi.modbus.ModbusSlaveException;
import net.wimpi.modbus.io.ModbusTCPTransaction;
import net.wimpi.modbus.msg.ModbusRequest;
import net.wimpi.modbus.msg.ReadMultipleRegistersRequest;
import net.wimpi.modbus.msg.ReadMultipleRegistersResponse;
import net.wimpi.modbus.msg.WriteMultipleRegistersRequest;
import net.wimpi.modbus.net.TCPMasterConnection;
import net.wimpi.modbus.procimg.Register;
import net.wimpi.modbus.procimg.SimpleRegister;

/**
 * Implements the generic MODBUS TCP/IP connector.
 *
 * @param <CO> the output type to the IIP-Ecosphere platform
 * @param <CI> the input type from the IIP-Ecosphere platform
 * 
 * @author Christian Nikolajew
 */
@MachineConnector(hasModel = true, supportsModelStructs = false, supportsEvents = false)
public class ModbusTcpIpConnector<CO, CI> extends AbstractConnector<ModbusItem, Object, CO, CI> {

    public static final String NAME = "MODBUS TCP/IP";
    private static final Logger LOGGER = LoggerFactory.getLogger(ModbusTcpIpConnector.class);

    private ModbusItem mItem;   
    private TCPMasterConnection mConnection;
    private ConnectorParameter mParams;
    private AtomicBoolean inPolling = new AtomicBoolean(false);

    /**
     * The descriptor of this connector (see META-INF/services).
     */
    public static class Descriptor implements ConnectorDescriptor {

        @Override
        public String getName() {
            return NAME;
        }

        @Override
        public Class<?> getType() {
            return ModbusTcpIpConnector.class;
        }

    }

    /**
     * Creates an instance and installs the protocol adapter.
     * 
     * @param adapter the protocol adapter
     */
    @SafeVarargs
    public ModbusTcpIpConnector(ProtocolAdapter<ModbusItem, Object, CO, CI>... adapter) {
        this(null, adapter);
    }

    /**
     * Creates an instance and installs the protocol adapter.
     * 
     * @param selector the adapter selector (<b>null</b> leads to a default selector
     *                 for the first adapter)
     * @param adapter  the protocol adapter(s)
     */
    @SafeVarargs
    public ModbusTcpIpConnector(AdapterSelector<ModbusItem, Object, CO, CI> selector,
            ProtocolAdapter<ModbusItem, Object, CO, CI>... adapter) {
        super(selector, adapter);
        configureModelAccess(new ModbusTcpIpModelAccess());
        mItem = new ModbusItem();
    }

    @Override
    public String getName() {
        return NAME;
    }
    
    
    /**
     * Construct the endpoint URL.
     * 
     * @param params the connector parameters
     * @return the endpoint URL
     */
    private String getEndpointUrl(ConnectorParameter params) {

        String result = params.getSchema() + "://" + params.getHost() + ":" + params.getPort(); // + "/" +
        return result;
    }
    
    // checkstyle: stop exception type check

    @Override
    protected void connectImpl(ConnectorParameter params) throws IOException {

        if (mConnection == null) {
            this.mParams = params;

            String endpointURL = getEndpointUrl(params);

            mConnection = new TCPMasterConnection(InetAddress.getByName(params.getHost()));
            mConnection.setPort(params.getPort());

            try {
                mConnection.connect();
                LOGGER.info("MODBUS TCP/IP connecting to {}", endpointURL);
            } catch (Exception e) {
                e.printStackTrace();
                LOGGER.info("MODBUS TCP/IP connection failed: {}");
            }
        }

    }

    // checkstyle: resume exception type check

    @Override
    protected void doPolling() {

        if (!inPolling.getAndSet(true)) {

            ModbusItem data;
            try {
                data = read();
                received(DEFAULT_CHANNEL, data);
            } catch (IOException e) {
                e.printStackTrace();
            }

            inPolling.set(false);
        }
    }

    @Override
    protected void disconnectImpl() throws IOException {
        mConnection = null;
    }

    
    @Override
    protected void error(String arg0, Throwable arg1) {
        LOGGER.error(arg0, arg1);
    }

    @Override
    protected ModbusItem read() throws IOException {
    
        readFromMachine();
        
        return mItem;
    }

    /**
     * Reads data from the machine and set the ModbusItem mItem accordingly.
     */
    private void readFromMachine() {

        try {
            // Read HoldingRegisters
            ReadMultipleRegistersRequest lRequest = new ReadMultipleRegistersRequest(0, mItem.getHoldingRegisterSize());

            ModbusTCPTransaction lTransaction = new ModbusTCPTransaction(mConnection);
            lTransaction.setRequest(lRequest);
            lTransaction.execute();

            ReadMultipleRegistersResponse lResponse = (ReadMultipleRegistersResponse) lTransaction.getResponse();

            // Add result to ModbusItem
            for (int i = 0; i < mItem.getHoldingRegisterSize(); i++) {
                mItem.setHoldingRegister((short) i, (short) lResponse.getRegisterValue(i));
            }

        } catch (ModbusIOException e) {
            e.printStackTrace();
        } catch (ModbusSlaveException e) {
            e.printStackTrace();
        } catch (ModbusException e) {
            e.printStackTrace();
        }

    }
    
    @Override
    public void trigger(ConnectorTriggerQuery query) {
       //Not used for MODBUS TCP/IP
    }

    @Override
    protected void writeImpl(Object data) throws IOException {
        
        writeToMachine();

    }
    
    /**
     * Writes ModbusItem mItem to the machine.
     */
    private void writeToMachine() {
        
        Register[] lHoldingRegisters = new Register[mItem.getHoldingRegisterSize()];
        
        for (int i = 0; i < mItem.getHoldingRegisterSize(); i++) {
            lHoldingRegisters[i] = new SimpleRegister(mItem.getHoldingRegister(i));
        }
 
        try {

            ModbusTCPTransaction lTransaction = new ModbusTCPTransaction(mConnection);
            ModbusRequest lRequest = new WriteMultipleRegistersRequest(0, lHoldingRegisters);
            lTransaction.setRequest(lRequest);
            lTransaction.execute();

        } catch (ModbusIOException e) {
            e.printStackTrace();
        } catch (ModbusSlaveException e) {
            e.printStackTrace();
        } catch (ModbusException e) {
            e.printStackTrace();
        }
    }

    /**
     * Implements the model access for MODBUS TCP/IP.
     * 
     * @author Christian Nikolajew
     */
    protected class ModbusTcpIpModelAccess extends AbstractModelAccess {

        private ModbusTcpIpInputConverter inputConverter = new ModbusTcpIpInputConverter();
        private ModbusTcpIpOutputConverter outputConverter = new ModbusTcpIpOutputConverter();

        /**
         * Creates an instance.
         */
        protected ModbusTcpIpModelAccess() {
            super(ModbusTcpIpConnector.this);
        }
        
        /**
         * Returns the input converter instance.
         * 
         * @return the input converter
         */
        public ModelInputConverter getInputConverter() {
            return inputConverter;
        }

        /**
         * Returns the output converter instance.
         * 
         * @return the output converter
         */
        public ModelOutputConverter getOutputConverter() {
            return outputConverter;
        }

        @Override
        public Object call(String qName, Object... arg1) throws IOException {

            if (qName.equals(ModbusNamespace.NAME_VAR_SHORT_VALUE)) {
                
                mItem.setHoldingRegister((short) 0, Short.valueOf(arg1[0].toString()));
                
            } else  if (qName.equals(ModbusNamespace.NAME_VAR_INT_VALUE)) {
                
                int intToWrite = Integer.valueOf(arg1[0].toString());
                
                short highShort = (short) (intToWrite >> 16);
                short lowShort = (short) (intToWrite & 0xFFFF);

                mItem.setHoldingRegister((short) 1, lowShort);
                mItem.setHoldingRegister((short) 2, highShort);
               
                
            } else  if (qName.equals(ModbusNamespace.NAME_VAR_FLOAT_VALUE)) {
                
                float floatToWrite = Float.valueOf(arg1[0].toString());
                int floatAsInt = Float.floatToIntBits(floatToWrite);
                
                short highShort = (short) ((floatAsInt >> 16) & 0xFFFF);
                short lowShort = (short) ( floatAsInt & 0xFFFF);

                mItem.setHoldingRegister((short) 3, lowShort);
                mItem.setHoldingRegister((short) 4, highShort);
                
            } else  if (qName.equals(ModbusNamespace.NAME_VAR_LONG_VALUE)) {
                
                long longToWrite = Long.valueOf(arg1[0].toString());
                
                short highestShort = (short) ((longToWrite >> 48) & 0xFFFF);
                short highShort = (short) ((longToWrite >> 32) & 0xFFFF);
                short lowShort = (short) ((longToWrite >> 16) & 0xFFFF);
                short lowestShort = (short) (longToWrite & 0xFFFF);
                
                mItem.setHoldingRegister((short) 5, lowestShort);
                mItem.setHoldingRegister((short) 6, lowShort);
                mItem.setHoldingRegister((short) 7, highShort);
                mItem.setHoldingRegister((short) 8, highestShort);
                
            } else  if (qName.equals(ModbusNamespace.NAME_VAR_DOUBLE_VALUE)) {
              
                double doubleToWrite = Double.valueOf(arg1[0].toString());

                ByteBuffer buffer = ByteBuffer.allocate(8);
                buffer.putDouble(doubleToWrite);
                
                short short1 = buffer.getShort(0);
                short short2 = buffer.getShort(2);
                short short3 = buffer.getShort(4);
                short short4 = buffer.getShort(6);
                
                mItem.setHoldingRegister((short) 9, short1);
                mItem.setHoldingRegister((short) 10, short2);
                mItem.setHoldingRegister((short) 11, short3);
                mItem.setHoldingRegister((short) 12, short4);

            } 
            
            writeImpl(mItem);
            doPolling();

            return mItem;
        }

        @Override
        public Object get(String qName) throws IOException {
            
            Object result = new Object();
            
            if (qName.equals(ModbusNamespace.NAME_VAR_SHORT_VALUE)) {
                
                result = mItem.getHoldingRegister(0);
                
            } else  if (qName.equals(ModbusNamespace.NAME_VAR_INT_VALUE)) {
                
                short lowShort = mItem.getHoldingRegister(1);
                short highShort = mItem.getHoldingRegister(2);
                
                int intRes = ((highShort & 0xFFFF) << 16) | (lowShort & 0xFFFF);

                result = intRes;
                
            } else  if (qName.equals(ModbusNamespace.NAME_VAR_FLOAT_VALUE)) {
                
                short lowShort = mItem.getHoldingRegister(3);
                short highShort = mItem.getHoldingRegister(4);
                
                int intRes = ((highShort & 0xFFFF) << 16) | (lowShort & 0xFFFF);
                float floatRes = Float.intBitsToFloat(intRes);
                
                result = floatRes;
                
            } else  if (qName.equals(ModbusNamespace.NAME_VAR_LONG_VALUE)) {
                
                short lowestShort = mItem.getHoldingRegister(5);
                short lowShort = mItem.getHoldingRegister(6); 
                short highShort = mItem.getHoldingRegister(7);
                short highestShort = mItem.getHoldingRegister(8);
                
                long longRes = ((long) highestShort << 48) | ((long) highShort << 32) 
                    | ((long) lowShort << 16) | lowestShort;
                
                result = longRes;
                
            } else  if (qName.equals(ModbusNamespace.NAME_VAR_DOUBLE_VALUE)) {
              
                short short1 = mItem.getHoldingRegister(9);
                short short2 = mItem.getHoldingRegister(10);
                short short3 = mItem.getHoldingRegister(11);
                short short4 = mItem.getHoldingRegister(12);
                
                ByteBuffer buffer = ByteBuffer.allocate(8);
                buffer.putShort(0, short1);
                buffer.putShort(2, short2);
                buffer.putShort(4, short3);
                buffer.putShort(6, short4);
                
                buffer.rewind();
                double doubleRes = buffer.getDouble();
                
                result = doubleRes;
            }
            
            return result;
        }

        @Override
        public String getQSeparator() {
            // Not used for MODBUS TCP/IP
            return null;
        }

        @Override
        public <T> T getStruct(String arg0, Class<T> arg1) throws IOException {
            // Not used for MODBUS TCP/IP
            return null;
        }

        @Override
        public void monitor(String... qName) throws IOException {
            // Not used for MODBUS TCP/IP
        }

        @Override
        public void registerCustomType(Class<?> cls) throws IOException {
            // Not used for MODBUS TCP/IP
        }

        @Override
        public void set(String arg0, Object arg1) throws IOException {
            // Not used for MODBUS TCP/IP
        }

        @Override
        public void setStruct(String arg0, Object arg1) throws IOException {
            // Not used for MODBUS TCP/IP
        }

        @Override
        public String topInstancesQName() {
            // Not used for MODBUS TCP/IP
            return null;
        }

        @Override
        public void monitor(int notificationInterval, String... qNames) throws IOException {
            // Not used for MODBUS TCP/IP
        }

        @Override
        public void monitorModelChanges(int notificationInterval) throws IOException {
            // Not used for MODBUS TCP/IP
        }

        @Override
        public ModelAccess stepInto(String name) throws IOException {
            // Not used for MODBUS TCP/IP
            return null;
        }

        @Override
        public ModelAccess stepOut() {
            // Not used for MODBUS TCP/IP
            return null;
        }

        @Override
        protected ConnectorParameter getConnectorParameter() {
            return mParams;
        }

    }

    @Override
    public String supportedEncryption() {
        // Not used for MODBUS TCP/IP
        return null;
    }

    @Override
    public String enabledEncryption() {
        // Not used for MODBUS TCP/IP
        return null;
    }

}
