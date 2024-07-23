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
import java.math.BigInteger;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.ModbusIOException;
import com.ghgande.j2mod.modbus.ModbusSlaveException;
import com.ghgande.j2mod.modbus.io.ModbusTCPTransaction;
import com.ghgande.j2mod.modbus.msg.ModbusRequest;
import com.ghgande.j2mod.modbus.msg.ReadMultipleRegistersRequest;
import com.ghgande.j2mod.modbus.msg.ReadMultipleRegistersResponse;
import com.ghgande.j2mod.modbus.msg.WriteMultipleRegistersRequest;
import com.ghgande.j2mod.modbus.net.TCPMasterConnection;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.procimg.SimpleRegister;

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

/**
 * Implements the generic MODBUS TCP/IP connector.
 *
 * @param <CO> the output type of the connector
 * @param <CI> the input type of the connector
 * 
 * @author Christian Nikolajew
 */
@MachineConnector(hasModel = true, supportsModelStructs = false, supportsEvents = false, specificSettings = {
    "SERVER_STRUCTURE", "UNITID", "TIMEOUT", "BIGBYTE" })
public class ModbusTcpIpConnector<CO, CI> extends AbstractConnector<ModbusItem, Object, CO, CI> {

    public static final String NAME = "MODBUS TCP/IP";
    private static final Logger LOGGER = LoggerFactory.getLogger(ModbusTcpIpConnector.class);

    private ModbusMap map = null;
    private ModbusItem item;
    private TCPMasterConnection connection;
    private ConnectorParameter params;
    private AtomicBoolean inPolling = new AtomicBoolean(false);
    private int timeout = 1000;
    private boolean bigByte = true;
    private int unitId = 1;

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

        if (connection == null) {
            this.params = params;

            setModbusMap();

            item = new ModbusItem(map);
            
            System.out.println("Map:" + map);
            System.out.println("Item:" + item);

            String endpointURL = getEndpointUrl(params);

            connection = new TCPMasterConnection(InetAddress.getByName(params.getHost()));
            connection.setPort(params.getPort());
            connection.setTimeout(timeout);

            try {
                connection.connect();
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

        Set<String> keys = params.getSpecificSettingKeys();

        for (String key : keys) {
            Object serverSettings = params.getSpecificSetting(key);
            if (key.equals("SERVER_STRUCTURE")) {
                map = JsonUtils.fromJson(serverSettings, ModbusMap.class);
            } else if (key.equals("BIGBYTE")) {
                bigByte = (Boolean) serverSettings;
            } else if (key.equals("UNITID")) {
                unitId = (Integer) serverSettings;
            } else if (key.equals("TIMEOUT")) {
                timeout = (Integer) serverSettings;
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
        connection = null;
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
                        varItem.getTypeRegisterSize());
                request.setUnitID(unitId);

                ModbusTCPTransaction lTransaction = new ModbusTCPTransaction(connection);
                lTransaction.setRequest(request);
                lTransaction.execute();

                ReadMultipleRegistersResponse lResponse = (ReadMultipleRegistersResponse) lTransaction.getResponse();

                if (varItem.getType().equals("short")) {
                    
                    item.setRegister(varItem.getOffset(), lResponse.getRegisters()[0].toShort());

                } else if (varItem.getType().equals("ushort")) {

                    item.setRegister(varItem.getOffset(), lResponse.getRegisters()[0].toUnsignedShort());
                    
                } else if (varItem.getType().equals("integer")) {

                    item.setRegister(varItem.getOffset(), getIntegerFromRegisters(lResponse.getRegisters()));

                } else if (varItem.getType().equals("uinteger")) {

                    item.setRegister(varItem.getOffset(), getUnsignedIntegerFromRegisters(lResponse.getRegisters()));

                } else if (varItem.getType().equals("long")) {

                    item.setRegister(varItem.getOffset(), getLongFromRegisters(lResponse.getRegisters()));

                } else if (varItem.getType().equals("ulong")) {

                    item.setRegister(varItem.getOffset(), getUnsignedLongFromRegisters(lResponse.getRegisters()));

                } else if (varItem.getType().equals("float")) {

                    item.setRegister(varItem.getOffset(), getFloatFromRegisters(lResponse.getRegisters()));

                } else if (varItem.getType().equals("double")) {

                    item.setRegister(varItem.getOffset(), getDoubleFromRegisters(lResponse.getRegisters()));

                } else if (varItem.getType().equals("ascii")) {

                    item.setRegister(varItem.getOffset(), getStringFromRegisters(lResponse.getRegisters()));

                } else if (varItem.getType().equals("datetime")) {

                    item.setRegister(varItem.getOffset(), getDatetimeFromRegisters(lResponse.getRegisters()));
                }

            } catch (ModbusIOException e) {
                e.printStackTrace();
            } catch (ModbusSlaveException e) {
                e.printStackTrace();
            } catch (ModbusException e) {
                e.printStackTrace();
            }
        }

        return item;
    }

    /**
     * Creates an Integer out of two Registers.
     * 
     * @param registers containing the Integer
     * @return the Integer
     */
    private int getIntegerFromRegisters(Register[] registers) {

        short lowShort;
        short highShort;

        if (bigByte) {
            lowShort = registers[0].toShort();
            highShort = registers[1].toShort();
        } else {
            highShort = registers[0].toShort();
            lowShort = registers[1].toShort();
        }

        int result = ((highShort & 0xFFFF) << 16) | (lowShort & 0xFFFF);

        return result;
    }

    /**
     * Creates an Long containing the value of a unsignedInteger.
     * 
     * @param registers containing the unsignedInteger
     * @return the value of unsignedInteger as long
     */
    private long getUnsignedIntegerFromRegisters(Register[] registers) {

        short lowShort;
        short highShort;

        if (bigByte) {
            lowShort = registers[0].toShort();
            highShort = registers[1].toShort();
        } else {
            highShort = registers[0].toShort();
            lowShort = registers[1].toShort();
        }

        int lowInt = lowShort & 0xFFFF;
        int highInt = highShort & 0xFFFF;

        long result = ((long) highInt << 16) | lowInt;
        return result;
    }

    /**
     * Creates an Float out of two Registers.
     * 
     * @param registers containing the Float
     * @return the Float
     */
    private float getFloatFromRegisters(Register[] registers) {

        short lowShort;
        short highShort;

        if (bigByte) {
            lowShort = registers[0].toShort();
            highShort = registers[1].toShort();
        } else {
            highShort = registers[0].toShort();
            lowShort = registers[1].toShort();
        }

        int intRes = ((lowShort & 0xFFFF) << 16) | (highShort & 0xFFFF);
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

        short lowestShort;
        short lowShort;
        short highShort;
        short highestShort;

        if (bigByte) {
            lowestShort = registers[0].toShort();
            lowShort = registers[1].toShort();
            highShort = registers[2].toShort();
            highestShort = registers[3].toShort();
        } else {
            highestShort = registers[0].toShort();
            highShort = registers[1].toShort();
            lowShort = registers[2].toShort();
            lowestShort = registers[3].toShort();
        }

        Long result = ((long) highestShort << 48) | ((long) highShort << 32) | ((long) lowShort << 16) | lowestShort;

        return result;
    }

    /**
     * Creates a BigInteger containing the value of a unsignedLong.
     * 
     * @param registers containing the unsignedLong
     * @return the value of unsignedLong as BigInteger
     */
    private BigInteger getUnsignedLongFromRegisters(Register[] registers) {

        short lowestShort;
        short lowShort;
        short highShort;
        short highestShort;

        if (bigByte) {
            lowestShort = registers[0].toShort();
            lowShort = registers[1].toShort();
            highShort = registers[2].toShort();
            highestShort = registers[3].toShort();
        } else {
            highestShort = registers[0].toShort();
            highShort = registers[1].toShort();
            lowShort = registers[2].toShort();
            lowestShort = registers[3].toShort();
        }

        long lowestLong = lowestShort & 0xFFFFL;
        long lowLong = lowShort & 0xFFFFL;
        long highLong = highShort & 0xFFFFL;
        long highestLong = highestShort & 0xFFFFL;

        BigInteger result = BigInteger.valueOf(highestLong).shiftLeft(48).or(BigInteger.valueOf(highLong).shiftLeft(32))
                .or(BigInteger.valueOf(lowLong).shiftLeft(16)).or(BigInteger.valueOf(lowestLong));

        return result;
    }

    /**
     * Creates an Double out of four Registers.
     * 
     * @param registers containing the Double
     * @return the Double
     */
    private double getDoubleFromRegisters(Register[] registers) {

        short lowestShort;
        short lowShort;
        short highShort;
        short highestShort;

        if (bigByte) {
            lowestShort = registers[0].toShort();
            lowShort = registers[1].toShort();
            highShort = registers[2].toShort();
            highestShort = registers[3].toShort();
        } else {
            highestShort = registers[0].toShort();
            highShort = registers[1].toShort();
            lowShort = registers[2].toShort();
            lowestShort = registers[3].toShort();
        }

        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putShort(0, lowestShort);
        buffer.putShort(2, lowShort);
        buffer.putShort(4, highShort);
        buffer.putShort(6, highestShort);

        buffer.rewind();
        Double result = buffer.getDouble();

        return result;
    }

    /**
     * Creates a ASCII String out of Registers.
     * 
     * @param registers containing the String
     * @return the String
     */
    private String getStringFromRegisters(Register[] registers) {

        ByteBuffer buffer = ByteBuffer.allocate(registers.length * 2);

        for (Register reg : registers) {
            buffer.put((byte) (reg.toShort() >> 8));
            buffer.put((byte) (reg.toShort() & 0xFF));
        }

        return new String(buffer.array(), StandardCharsets.US_ASCII).trim();
    }

    /**
     * Create a LocalDateTime out of Registers.
     * 
     * @param registers containing LocalDateTime
     * @return The LocalDateTime
     */
    private LocalDateTime getDatetimeFromRegisters(Register[] registers) {

        long timePart1 = ((long) registers[0].toShort() << 48) | (long) registers[1].toShort() << 32;
        long timePart2 = ((long) registers[2].toShort() << 16) | (registers[3].toShort() & 0xFFFF);
        long timestamp = timePart1 | timePart2;

        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneOffset.UTC);
    }

    @Override
    public void trigger(ConnectorTriggerQuery query) {
        // Not used for MODBUS TCP/IP
    }

    @Override
    protected void writeImpl(Object data) throws IOException {

        if (data != null) {
            ModbusVarItem varItem = (ModbusVarItem) data;
            Object varItemValue = item.getRegister(varItem.getOffset());

            Register[] lHoldingRegisters = registers(varItem, varItemValue);

            try {

                ModbusRequest request = new WriteMultipleRegistersRequest(varItem.getOffset(), lHoldingRegisters);
                ModbusTCPTransaction lTransaction = new ModbusTCPTransaction(connection);
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

        Register[] holdingRegisters = new Register[varItem.getTypeRegisterSize()];

        if (varItem.getType().equals("short")) {

            holdingRegisters[0] = new SimpleRegister((short) varItemValue);

        } else if (varItem.getType().equals("ushort")) {

            holdingRegisters[0] = new SimpleRegister(((Integer) varItemValue) & 0xFFFF);

        } else if (varItem.getType().equals("integer")) {

            holdingRegisters = getIntegerAsRegisters(varItem);

        } else if (varItem.getType().equals("uinteger")) {

            holdingRegisters = getUnsignedIntegerAsRegisters(varItem);

        } else if (varItem.getType().equals("float")) {

            holdingRegisters = getFloatAsRegisters(varItem);

        } else if (varItem.getType().equals("long")) {

            holdingRegisters = getLongAsRegisters(varItem);

        } else if (varItem.getType().equals("ulong")) {

            holdingRegisters = getUnsignedLongAsRegisters(varItem);

        } else if (varItem.getType().equals("double")) {

            holdingRegisters = getDoubleAsRegisters(varItem);

        } else if (varItem.getType().equals("ascii")) {

            holdingRegisters = getStringAsRegisters(varItem);

        } else if (varItem.getType().equals("datetime")) {

            holdingRegisters = getDatetimeAsRegisters(varItem);
        }

        return holdingRegisters;
    }

    /**
     * Creates Register[] out of a Integer value.
     * 
     * @param varItem to set the Integer value for
     * @return Register[] with the Integer value in it
     */
    private Register[] getIntegerAsRegisters(ModbusVarItem varItem) {

        Register[] reg = new Register[varItem.getTypeRegisterSize()];
        int value = (int) item.getRegister(varItem.getOffset());

        short lowShort;
        short highShort;

        if (bigByte) {
            lowShort = (short) (value & 0xFFFF);
            highShort = (short) (value >> 16);
        } else {
            highShort = (short) (value & 0xFFFF);
            lowShort = (short) (value >> 16);
        }

        reg[0] = new SimpleRegister(lowShort);
        reg[1] = new SimpleRegister(highShort);

        return reg;
    }

    /**
     * Creates Register[] out of a unsigned Integer value.
     * 
     * @param varItem to set the unsigned Integer value for
     * @return Register[] with the unsigned Integer value in it
     */
    private Register[] getUnsignedIntegerAsRegisters(ModbusVarItem varItem) {

        Register[] reg = new Register[varItem.getTypeRegisterSize()];
        long value = ((Long) item.getRegister(varItem.getOffset()));

        short lowShort;
        short highShort;

        if (bigByte) {
            lowShort = (short) (value & 0xFFFF);
            highShort = (short) (value >> 16);
        } else {
            highShort = (short) (value & 0xFFFF);
            lowShort = (short) (value >> 16);
        }

        reg[0] = new SimpleRegister(lowShort);
        reg[1] = new SimpleRegister(highShort);

        return reg;
    }

    /**
     * Creates Register[] out of a Float value.
     * 
     * @param varItem to set the Float value for
     * @return the Register with the Float value in it
     */
    private Register[] getFloatAsRegisters(ModbusVarItem varItem) {

        Register[] reg = new Register[varItem.getTypeRegisterSize()];
        float value = (float) item.getRegister(varItem.getOffset());
        int floatAsInt = Float.floatToIntBits(value);

        short lowShort;
        short highShort;

        if (bigByte) {
            lowShort = (short) ((floatAsInt >> 16) & 0xFFFF);
            highShort = (short) (floatAsInt & 0xFFFF);
        } else {
            highShort = (short) ((floatAsInt >> 16) & 0xFFFF);
            lowShort = (short) (floatAsInt & 0xFFFF);
        }

        reg[0] = new SimpleRegister(lowShort);
        reg[1] = new SimpleRegister(highShort);

        return reg;
    }

    /**
     * Creates Register[] out of a Long value.
     * 
     * @param varItem to set the Long value for
     * @return the Register with the Long value in it
     */
    private Register[] getLongAsRegisters(ModbusVarItem varItem) {

        Register[] reg = new Register[varItem.getTypeRegisterSize()];
        long value = (long) item.getRegister(varItem.getOffset());

        short lowestShort;
        short lowShort;
        short highShort;
        short highestShort;

        if (bigByte) {
            lowestShort = (short) (value & 0xFFFF);
            lowShort = (short) ((value >> 16) & 0xFFFF);
            highShort = (short) ((value >> 32) & 0xFFFF);
            highestShort = (short) ((value >> 48) & 0xFFFF);
        } else {
            highestShort = (short) (value & 0xFFFF);
            highShort = (short) ((value >> 16) & 0xFFFF);
            lowShort = (short) ((value >> 32) & 0xFFFF);
            lowestShort = (short) ((value >> 48) & 0xFFFF);
        }

        reg[0] = new SimpleRegister(lowestShort);
        reg[1] = new SimpleRegister(lowShort);
        reg[2] = new SimpleRegister(highShort);
        reg[3] = new SimpleRegister(highestShort);

        return reg;
    }

    /**
     * Creates Register[] out of a unsigned Long value.
     * 
     * @param varItem to set the unsigned Long value for
     * @return the Register with the unsigned Long value in it
     */
    private Register[] getUnsignedLongAsRegisters(ModbusVarItem varItem) {

        Register[] reg = new Register[varItem.getTypeRegisterSize()];
        BigInteger value = (BigInteger) item.getRegister(varItem.getOffset());

        byte[] bytes = value.toByteArray();
        byte[] fullBytes = new byte[8];
        int start = Math.max(0, bytes.length - 8);
        int length = Math.min(8, bytes.length);
        System.arraycopy(bytes, start, fullBytes, 8 - length, length);

        if (bigByte) {
            reg[3] = new SimpleRegister((short) (((fullBytes[0] & 0xFF) << 8) | (fullBytes[1] & 0xFF)));
            reg[2] = new SimpleRegister((short) (((fullBytes[2] & 0xFF) << 8) | (fullBytes[3] & 0xFF)));
            reg[1] = new SimpleRegister((short) (((fullBytes[4] & 0xFF) << 8) | (fullBytes[5] & 0xFF)));
            reg[0] = new SimpleRegister((short) (((fullBytes[6] & 0xFF) << 8) | (fullBytes[7] & 0xFF)));
        } else {
            reg[0] = new SimpleRegister((short) (((fullBytes[0] & 0xFF) << 8) | (fullBytes[1] & 0xFF)));
            reg[1] = new SimpleRegister((short) (((fullBytes[2] & 0xFF) << 8) | (fullBytes[3] & 0xFF)));
            reg[2] = new SimpleRegister((short) (((fullBytes[4] & 0xFF) << 8) | (fullBytes[5] & 0xFF)));
            reg[3] = new SimpleRegister((short) (((fullBytes[6] & 0xFF) << 8) | (fullBytes[7] & 0xFF)));
        }

        return reg;
    }

    /**
     * Creates Register[] out of a Double value.
     * 
     * @param varItem to set the Double value for
     * @return the Register with the Double value in it
     */
    private Register[] getDoubleAsRegisters(ModbusVarItem varItem) {

        Register[] reg = new Register[varItem.getTypeRegisterSize()];
        double value = (double) item.getRegister(varItem.getOffset());

        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putDouble(value);

        short lowestShort;
        short lowShort;
        short highShort;
        short highestShort;

        if (bigByte) {
            lowestShort = buffer.getShort(0);
            lowShort = buffer.getShort(2);
            highShort = buffer.getShort(4);
            highestShort = buffer.getShort(6);
        } else {
            lowestShort = buffer.getShort(6);
            lowShort = buffer.getShort(4);
            highShort = buffer.getShort(2);
            highestShort = buffer.getShort(0);
        }

        reg[0] = new SimpleRegister(lowestShort);
        reg[1] = new SimpleRegister(lowShort);
        reg[2] = new SimpleRegister(highShort);
        reg[3] = new SimpleRegister(highestShort);

        return reg;
    }

    /**
     * Creates Register[] out of a String value.
     * 
     * @param varItem to set the String value for
     * @return the Register with the String value in it
     */
    private Register[] getStringAsRegisters(ModbusVarItem varItem) {

        Register[] reg = new Register[varItem.getTypeRegisterSize()];
        String value = (String) item.getRegister(varItem.getOffset());
        byte[] bytes = value.getBytes();

        for (int i = 0; i < bytes.length; i += 2) {
            int lowByte = (i + 1 < bytes.length) ? (bytes[i + 1] & 0xFF) : 0;
            int highByte = bytes[i] & 0xFF;

            reg[i / 2] = new SimpleRegister((short) ((highByte << 8) | lowByte));
        }

        return reg;
    }

    /**
     * Creates Register[] out of a Datetime value.
     * 
     * @param varItem to set the Datetime value for
     * @return the Register with the Datetime value in it
     */
    private Register[] getDatetimeAsRegisters(ModbusVarItem varItem) {

        Register[] reg = new Register[varItem.getTypeRegisterSize()];
        LocalDateTime datetime = (LocalDateTime) item.getRegister(varItem.getOffset());

        long timestamp = datetime.toInstant(ZoneOffset.UTC).toEpochMilli();

        reg[0] = new SimpleRegister((short) (timestamp >> 48));
        reg[1] = new SimpleRegister((short) (timestamp >> 32));
        reg[2] = new SimpleRegister((short) (timestamp >> 16));
        reg[3] = new SimpleRegister((short) (timestamp & 0xFFFF));

        return reg;
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
        public void set(String qName, Object arg1) throws IOException {

            ModbusVarItem varItem = map.get(qName);
            item.setRegister(varItem.getOffset(), arg1);
            writeImpl(varItem);
            doPolling();
        }

        @Override
        public Object get(String qName) throws IOException {

            Object result = new Object();
            ModbusVarItem varItem = map.get(qName);
            result = item.getRegister(varItem.getOffset());
            return result;
        }

        @Override
        public Object call(String qName, Object... arg1) throws IOException {
            // Not used for MODBUS TCP/IP
            return null;
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
            return params;
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
