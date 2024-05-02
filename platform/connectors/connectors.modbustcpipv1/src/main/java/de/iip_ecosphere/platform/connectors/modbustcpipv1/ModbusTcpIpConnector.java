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
import java.util.Set;
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
import de.iip_ecosphere.platform.support.json.JsonUtils;
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

    private ModbusMap map = null;

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

        String result = params.getHost() + ":" + params.getPort();
        return result;
    }

    // checkstyle: stop exception type check

    @Override
    protected void connectImpl(ConnectorParameter params) throws IOException {

        if (mConnection == null) {
            this.mParams = params;

            setModbusMap();

            mItem = new ModbusItem(map);

            String endpointURL = getEndpointUrl(params);

            mConnection = new TCPMasterConnection(InetAddress.getByName(params.getHost()));
            mConnection.setPort(params.getPort());

            try {
                mConnection.connect();
                LOGGER.info("MODBUS TCP/IP connecting to " + endpointURL);
            } catch (Exception e) {
                e.printStackTrace();
                LOGGER.info("MODBUS TCP/IP connection failed: {}");
            }
        }

    }

    /**
     * Sets the ModbusMap.
     */
    private void setModbusMap() {

        Set<String> keys = mParams.getSpecificSettingKeys();

        for (String key : keys) {

            if (key.equals("SERVER_STRUCTURE")) {

                Object serverSettings = mParams.getSpecificSetting(key);
                map = JsonUtils.fromJson(serverSettings, ModbusMap.class);

            }
        }

        if (map == null) {
            System.out.println("ModbusTcpIpConnector -> No SERVER_STRUCTURE found");
        } 
    }

    /**
     * Getter for ModbusMap.
     * 
     * @return the ModbusMap
     */
    public ModbusMap getMap() {
        return map;
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

        for (ModbusMap.Entry<String, ModbusVarItem> entry : map.entrySet()) {

            ModbusVarItem varItem = entry.getValue();

            try {

                ReadMultipleRegistersRequest request = new ReadMultipleRegistersRequest(varItem.getOffset(),
                        varItem.getTypsRegisterSize());

                ModbusTCPTransaction lTransaction = new ModbusTCPTransaction(mConnection);
                lTransaction.setRequest(request);
                lTransaction.execute();

                ReadMultipleRegistersResponse lResponse = (ReadMultipleRegistersResponse) lTransaction.getResponse();

                if (varItem.getType().equals("short")) {

                    Object value = lResponse.getRegister(0).toShort();
                    mItem.setRegister(varItem.getOffset(), value);

                } else if (varItem.getType().equals("integer")) {

                    mItem.setRegister(varItem.getOffset(), getIntegerFromRegisters(lResponse.getRegisters()));

                } else if (varItem.getType().equals("float")) {

                    mItem.setRegister(varItem.getOffset(), getFloatFromRegisters(lResponse.getRegisters()));

                } else if (varItem.getType().equals("long")) {

                    mItem.setRegister(varItem.getOffset(), getLongFromRegisters(lResponse.getRegisters()));

                } else if (varItem.getType().equals("double")) {

                    mItem.setRegister(varItem.getOffset(), getDoubleFromRegisters(lResponse.getRegisters()));

                } else if (varItem.getType().equals("dword")) {

                    mItem.setRegister(varItem.getOffset(), lResponse.getRegister(0).toShort());

                }

            } catch (ModbusIOException e) {
                e.printStackTrace();
            } catch (ModbusSlaveException e) {
                e.printStackTrace();
            } catch (ModbusException e) {
                e.printStackTrace();
            }
        }

        return mItem;
    }

    /**
     * Creates an Integer out of two Registers.
     * 
     * @param registers containing the Integer 
     * @return the Integer
     */
    private int getIntegerFromRegisters(Register[] registers) {

        short lowShort = registers[0].toShort();
        short highShort = registers[1].toShort();

        int result = ((highShort & 0xFFFF) << 16) | (lowShort & 0xFFFF);

        return result;
    }
    
    /**
     * Creates an Float out of two Registers.
     * 
     * @param registers containing the Float
     * @return the Float
     */
    private float getFloatFromRegisters(Register[] registers) {
        
        short lowShort = registers[0].toShort();
        short highShort = registers[1].toShort();
        
        int intRes = ((highShort & 0xFFFF) << 16) | (lowShort & 0xFFFF);
        Float result = Float.intBitsToFloat(intRes); 
        return result;
    }

    /**
     * Creates an Long out of four Registers.
     * 
     * @param registers containing the Long
     * @return the Long
     */
    private long getLongFromRegisters(Register[] registers) {
        
        short lowestShort = registers[0].toShort();
        short lowShort = registers[1].toShort();
        short highShort = registers[2].toShort();
        short highestShort = registers[3].toShort();

        Long result = ((long) highestShort << 48) | ((long) highShort << 32) | ((long) lowShort << 16)
                | lowestShort;
        
        return result;
    }
    
    /**
     * Creates an Double out of four Registers.
     * 
     * @param registers containing the Double
     * @return the Double
     */
    private double getDoubleFromRegisters(Register[] registers) {
        
        short short0 = registers[0].toShort();
        short short1 = registers[1].toShort();
        short short2 = registers[2].toShort();
        short short3 = registers[3].toShort();

        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putShort(0, short0);
        buffer.putShort(2, short1);
        buffer.putShort(4, short2);
        buffer.putShort(6, short3);

        buffer.rewind();
        Double result = buffer.getDouble();
        
        return result;
    }
    
    @Override
    public void trigger(ConnectorTriggerQuery query) {
        // Not used for MODBUS TCP/IP
    }

    @Override
    protected void writeImpl(Object data) throws IOException {
        
        if (data != null) {
            ModbusVarItem varItem = (ModbusVarItem) data;
            Object varItemValue = mItem.getRegister(varItem.getOffset());            

            Register[] lHoldingRegisters = registers(varItem, varItemValue);

            try {

                ModbusTCPTransaction lTransaction = new ModbusTCPTransaction(mConnection);
                ModbusRequest request = new WriteMultipleRegistersRequest(varItem.getOffset(), lHoldingRegisters);
                lTransaction.setRequest(request);
                lTransaction.execute();

            } catch (ModbusIOException e) {
                e.printStackTrace();
            } catch (ModbusSlaveException e) {
                e.printStackTrace();
            } catch (ModbusException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Creates the registers to write to the Machine.
     * 
     * @param varItem      the ModbusVarItem to write
     * @param varItemValue the Value for the ModbusVarItem to write
     * @return the registers to write to the Machine
     */
    private Register[] registers(ModbusVarItem varItem, Object varItemValue) {

        Register[] holdingRegisters = new Register[varItem.getTypsRegisterSize()];

        if (varItem.getType().equals("short")) {

            holdingRegisters[0] = new SimpleRegister((short) varItemValue);

        } else if (varItem.getType().equals("integer")) {

            int intToWrite = (int) varItemValue;

            short highShort = (short) (intToWrite >> 16);
            short lowShort = (short) (intToWrite & 0xFFFF);

            holdingRegisters[0] = new SimpleRegister(lowShort);
            holdingRegisters[1] = new SimpleRegister(highShort);

        } else if (varItem.getType().equals("float")) {

            float floatToWrite = (float) varItemValue;
            int floatAsInt = Float.floatToIntBits(floatToWrite);

            short highShort = (short) ((floatAsInt >> 16) & 0xFFFF);
            short lowShort = (short) (floatAsInt & 0xFFFF);

            holdingRegisters[0] = new SimpleRegister(lowShort);
            holdingRegisters[1] = new SimpleRegister(highShort);

        } else if (varItem.getType().equals("long")) {

            long longToWrite = (long) varItemValue;

            short highestShort = (short) ((longToWrite >> 48) & 0xFFFF);
            short highShort = (short) ((longToWrite >> 32) & 0xFFFF);
            short lowShort = (short) ((longToWrite >> 16) & 0xFFFF);
            short lowestShort = (short) (longToWrite & 0xFFFF);

            holdingRegisters[0] = new SimpleRegister(lowestShort);
            holdingRegisters[1] = new SimpleRegister(lowShort);
            holdingRegisters[2] = new SimpleRegister(highShort);
            holdingRegisters[3] = new SimpleRegister(highestShort);

        } else if (varItem.getType().equals("double")) {

            double doubleToWrite = (double) varItemValue;

            ByteBuffer buffer = ByteBuffer.allocate(8);
            buffer.putDouble(doubleToWrite);

            short short1 = buffer.getShort(0);
            short short2 = buffer.getShort(2);
            short short3 = buffer.getShort(4);
            short short4 = buffer.getShort(6);

            holdingRegisters[0] = new SimpleRegister(short1);
            holdingRegisters[1] = new SimpleRegister(short2);
            holdingRegisters[2] = new SimpleRegister(short3);
            holdingRegisters[3] = new SimpleRegister(short4);

        }

        return holdingRegisters;
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

        /**
         * Returns the ModbusMap.
         * 
         * @return the ModbusMap
         */
        public ModbusMap getMap() {
            return map;
        }

        @Override
        public Object call(String qName, Object... arg1) throws IOException {

            ModbusMap map = ModbusTcpIpConnector.this.getMap();
            ModbusVarItem varItem = map.get(qName);

            mItem.setRegister(varItem.getOffset(), arg1[0]);

            writeImpl(varItem);
            doPolling();

            return mItem;
        }

        @Override
        public Object get(String qName) throws IOException {

            Object result = new Object();
            ModbusMap map = ModbusTcpIpConnector.this.getMap();
            ModbusVarItem varItem = map.get(qName);

            result = mItem.getRegister(varItem.getOffset());

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
        public ConnectorParameter getConnectorParameter() {
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
