/**
 * ******************************************************************************
 * Copyright (c) {2023} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.connectors.ads;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import de.iip_ecosphere.platform.connectors.AbstractConnector;
import de.iip_ecosphere.platform.connectors.AdapterSelector;
import de.iip_ecosphere.platform.connectors.ConnectorDescriptor;
import de.iip_ecosphere.platform.connectors.ConnectorParameter;
import de.iip_ecosphere.platform.connectors.MachineConnector;
import de.iip_ecosphere.platform.connectors.model.AbstractModelAccess;
import de.iip_ecosphere.platform.connectors.types.ProtocolAdapter;
import de.iip_ecosphere.platform.libs.ads.AdsCommunication;
import de.iip_ecosphere.platform.libs.ads.MemorySizeCalcs;
import de.iip_ecosphere.platform.libs.ads.MemorySizeCalculator;
import de.iip_ecosphere.platform.libs.ads.ReadVisitorsArrays;
import de.iip_ecosphere.platform.libs.ads.WriteVisitorsArrays;
import de.iip_ecosphere.platform.support.logging.Logger;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;
import de.iip_ecosphere.platform.libs.ads.ReadVisitor.ReadVisitorSupplier;
import de.iip_ecosphere.platform.libs.ads.WriteVisitor.WriteVisitorSupplier;

/**
 * A generic ADS TwinCat connector. 
 * 
 * @param <CO> the output type to the IIP-Ecosphere platform
 * @param <CI> the input type from the IIP-Ecosphere platform
 *
 * @author Holger Eichelberger, SSE
 */
@MachineConnector(hasModel = true, supportsModelStructs = true, supportsEvents = false, requiresTypedAccess = true, 
    supportsModelCalls = false, specificSettings = {})
public class AdsConnector<CO, CI> extends AbstractConnector<Object, Object, CO, CI> {

    public static final String NAME = "Generic ADS connector";
    private static final Logger LOGGER = LoggerFactory.getLogger(AdsConnector.class);
    private static final Object DUMMY = new Object();
    private static final Map<Class<?>, TypeDescriptor<?>> TYPE_DESCRIPTORS = new HashMap<>();

    private ConnectorParameter params;
    private AdsCommunication comm;

    /**
     * Creates instances.
     * 
     * @param <T> the type of the instance
     * @author Holger Eichelberger, SSE
     */
    public interface InstanceCreator<T> {
        
        /**
         * Creates an instance.
         * 
         * @param size the size for array instances, ignored for usual objects
         * @return the created instance
         */
        public T create(int size);
        
    }
    
    private static class TypeDescriptor<T> {

        private MemorySizeCalculator<T> sizeCalculator;
        private ReadVisitorSupplier<T> reader;
        private WriteVisitorSupplier<T> writer;
        private InstanceCreator<T> creator;
        
        /**
         * Creates a type descriptor.
         * 
         * @param sizeCalculator the memory size calculator 
         * @param reader the object reader
         * @param writer the object writer
         * @param creator the object instance creator
         */
        private TypeDescriptor(MemorySizeCalculator<T> sizeCalculator, ReadVisitorSupplier<T> reader, 
            WriteVisitorSupplier<T> writer, InstanceCreator<T> creator) {
            this.sizeCalculator = sizeCalculator;
            this.reader = reader;
            this.writer = writer;
            this.creator = creator;
        }
        
        /**
         * Writes an object.
         * 
         * @param comm the communicator instance
         * @param name the object name in ADS
         * @param value the object value
         * @throws IOException if writing or converting fails
         */
        @SuppressWarnings("unchecked")
        private void write(AdsCommunication comm, String name, Object value) throws IOException {
            try {
                comm.writeStructByName(name, (T) value, sizeCalculator, writer);
            } catch (ClassCastException e) {
                throw new IOException(e);
            }
        }
        
        /**
         * Reads an object.
         * 
         * @param comm the communicator instance
         * @param name the object name in ADS
         * @param size the object value in case of arrays, ignored for non-arrays
         * @return the object
         * @throws IOException in case that reading fails for some reason
         */
        private T read(AdsCommunication comm, String name, int size) throws IOException {
            T value = creator.create(size);
            comm.readStructByNameSimple(name, creator.create(0), sizeCalculator, reader);
            return value;
        }
        
    }

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
            return AdsConnector.class;
        }
        
    }
    
    /**
     * Creates an instance and installs the protocol adapter.
     * 
     * @param adapter the protocol adapter
     * @throws IllegalArgumentException if {@code adapter} is <b>null</b> or empty or adapters are <b>null</b>
     */
    @SafeVarargs
    public AdsConnector(ProtocolAdapter<Object, Object, CO, CI>... adapter) {
        this(null, adapter);
    }
    
    /**
     * Creates an instance and installs the protocol adapter.
     * 
     * @param selector the adapter selector (<b>null</b> leads to a default selector for the first adapter)
     * @param adapter the protocol adapter(s)
     * @throws IllegalArgumentException if {@code adapter} is <b>null</b> or empty or adapters are <b>null</b>
     */
    @SafeVarargs
    public AdsConnector(AdapterSelector<Object, Object, CO, CI> selector, 
        ProtocolAdapter<Object, Object, CO, CI>... adapter) {
        super(selector, adapter);
        configureModelAccess(new AdsModelAccess());
    }

    /**
     * Registers a type.
     * 
     * @param <T> the java type
     * @param cls the java type class
     * @param sizeCalculator the related size calculator
     * @param reader the reader
     * @param writer the writer
     * @param creator the instance creator 
     */
    public static <T> void registerType(Class<T> cls, MemorySizeCalculator<T> sizeCalculator, 
        ReadVisitorSupplier<T> reader, WriteVisitorSupplier<T> writer, InstanceCreator<T> creator) {
        TYPE_DESCRIPTORS.put(cls, new TypeDescriptor<>(sizeCalculator, reader, writer, creator));
    }
    
    static {
        registerType(double[].class, MemorySizeCalcs.getSizeCalculatorDoubleArray(), 
            ReadVisitorsArrays.getReaderDouble(), WriteVisitorsArrays.getWriteSupplierLReal(), s -> new double[s]);
        registerType(float[].class, MemorySizeCalcs.getSizeCalculatorFloatArray(), 
            ReadVisitorsArrays.getReaderFloat(), WriteVisitorsArrays.getWriteSupplierReal(), s -> new float[s]);
        registerType(int[].class, MemorySizeCalcs.getSizeCalculatorIntArray(), 
            ReadVisitorsArrays.getReaderDIntArray(), WriteVisitorsArrays.getWriteSupplierDInt(), s -> new int[s]);
        registerType(long[].class, MemorySizeCalcs.getSizeCalculatorLongArray(), 
            ReadVisitorsArrays.getReaderLIntArray(), WriteVisitorsArrays.getWriteSupplierLInt(), s -> new long[s]);
        registerType(short[].class, MemorySizeCalcs.getSizeCalculatorShortArray(), 
            ReadVisitorsArrays.getReaderIntArray(), WriteVisitorsArrays.getWriteSupplierInt(), s -> new short[s]);
        registerType(byte[].class, MemorySizeCalcs.getSizeCalculatorSIntArray(), 
            ReadVisitorsArrays.getReaderSIntArray(), WriteVisitorsArrays.getWriteSupplierSInt(), s -> new byte[s]);
    }

    // checkstyle: stop exception type check
    
    @Override
    protected void connectImpl(ConnectorParameter params) throws IOException {
        if (null == comm) {
            this.params = params;
            comm = new AdsCommunication(params.getHost(), params.getPort());
            LOGGER.info("Initializing ADS communication with {}:{}", params.getHost(), params.getPort());
            comm.initCommunication();
            LOGGER.info("ADS communication with {}:{} initialized ", params.getHost(), params.getPort());
        }
    }
    
    // checkstyle: resume exception type check

    @Override
    protected void disconnectImpl() throws IOException {
        if (comm != null) {
            comm.close();
            comm = null;
        }
    }

    @Override
    public void dispose() {
        // nothing to dispose
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    protected void writeImpl(Object data) throws IOException {
        // not needed, we do this via model access
    }

    @Override
    protected Object read() throws IOException {
        return DUMMY; // allow for polling, no change information so far
    }

    @Override
    protected void error(String message, Throwable th) {
        LOGGER.error(message + ": " + th.getMessage());
    }

    /**
     * Implements the model access for AAS.
     * 
     * @author Holger Eichelberger, SSE
     */
    private class AdsModelAccess extends AbstractModelAccess {

        private static final String SEPARATOR_STRING = "/";
        private String basePath = "";
        private AdsModelAccess parent;

        /**
         * Creates the instance and binds the listener to the creating connector instance.
         */
        protected AdsModelAccess() {
            super(AdsConnector.this);
        }
        
        /**
         * Creates the instance and binds the listener to the creating connector instance.
         * 
         * @param basePath the base path to resolve on
         * @param parent the return parent for {@link #stepOut()}
         */
        protected AdsModelAccess(String basePath, AdsModelAccess parent) {
            this();
            this.basePath = basePath;
            this.parent = parent;
        }
        
        @Override
        public String topInstancesQName() {
            return ""; // none
        }

        @Override
        public String getQSeparator() {
            return SEPARATOR_STRING;
        }

        // checkstyle: stop exception type check
        
        @Override
        public Object call(String qName, Object... args) throws IOException {
            throw new IOException("Not implemented " + qName); // see @MachineConnector
        }
        
        @Override
        public Object get(String qName) throws IOException {
            throw new IOException("Shall not be called"); // see @MachineConnector
        }

        @Override
        public int getInt(String qName) throws IOException {
            return comm.readDIntByName(basePath + qName);
        }

        @Override
        public float getFloat(String qName) throws IOException {
            return comm.readRealByName(basePath + qName);
        }

        @Override
        public double getDouble(String qName) throws IOException {
            return comm.readLRealByName(basePath + qName);
        }

        @Override
        public long getLong(String qName) throws IOException {
            return comm.readLIntByName(basePath + qName);
        }

        @Override
        public short getShort(String qName) throws IOException {
            return comm.readIntByName(basePath + qName);
        }

        @Override
        public byte getByte(String qName) throws IOException {
            return comm.readSIntByName(basePath + qName);
        }

        @Override
        public String getString(String qName) throws IOException {
            return comm.readStringByName(basePath + qName);
        }

        @Override
        public void set(String qName, Object value) throws IOException {
            comm.writeObjectByName(basePath + qName, value);
        }
        
        @Override
        public void setInt(String qName, int value) throws IOException {
            comm.writeDIntByName(basePath + qName, value);
        }

        @Override
        public void setLong(String qName, long value) throws IOException {
            comm.writeLIntByName(basePath + qName, value);
        }

        @Override
        public void setByte(String qName, byte value) throws IOException {
            comm.writeSIntByName(basePath + qName, value);
        }

        @Override
        public void setShort(String qName, short value) throws IOException {
            comm.writeIntByName(basePath + qName, value);
        }

        @Override
        public void setDouble(String qName, double value) throws IOException {
            comm.writeLRealByName(basePath + qName, value);
        }

        @Override
        public void setFloat(String qName, float value) throws IOException {
            comm.writeRealByName(basePath + qName, value);
        }

        @Override
        public void setString(String qName, String value) throws IOException {
            comm.writeStringByName(basePath + qName, value);
        }

        // checkstyle: resume exception type check

        @SuppressWarnings("unchecked")
        @Override
        public <T> T getStruct(String qName, Class<T> type) throws IOException { // TODO size for arrays
            TypeDescriptor<?> desc = TYPE_DESCRIPTORS.get(type);
            if (null != desc) {
                return (T) desc.read(comm, qName, 0);
            } else { // reflection fallback
                try {
                    T result = type.getConstructor().newInstance();
                    comm.readObjectByName(basePath + qName, result);
                    return result;
                } catch (InvocationTargetException | IllegalAccessException | InstantiationException 
                    | NoSuchMethodException e) {
                    throw new IOException(e);
                }
            }
        }

        @Override
        public void setStruct(String qName, Object value) throws IOException {
            TypeDescriptor<?> desc = TYPE_DESCRIPTORS.get(value.getClass());
            if (null != desc) {
                desc.write(comm, qName, value);
            } else {
                comm.writeObjectByName(basePath + qName, value); // reflection fallback
            }
        }

        @Override
        public void registerCustomType(Class<?> cls) throws IOException {
        }

        @Override
        public void monitor(int notificationInterval, String... qName) throws IOException {
            throw new IOException("Event-based monitoring is not supported. Please use polling.");
        }

        @Override
        public void monitorModelChanges(int notificationInterval) throws IOException {
            throw new IOException("Event-based monitoring is not supported. Please use polling.");
        }

        @Override
        public ConnectorParameter getConnectorParameter() {
            return params;
        }

        @Override
        public AdsModelAccess stepInto(String name) throws IOException {
            String n = basePath;
            if (n.length() == 0) {
                n = name;
            } else {
                n = n + "/" + name;
            }            
            return new AdsModelAccess(n, this);
        }

        @Override
        public AdsModelAccess stepOut() {
            return parent;
        }
        
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
