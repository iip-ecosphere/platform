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

package de.iip_ecosphere.platform.connectors.influxv3;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.influxdb.v3.client.Point;

import de.iip_ecosphere.platform.connectors.ConnectorParameter;
import de.iip_ecosphere.platform.connectors.model.AbstractTypeMappingModelAccess;
import de.iip_ecosphere.platform.connectors.model.ModelAccess;
import de.iip_ecosphere.platform.connectors.model.ModelInputConverter;
import de.iip_ecosphere.platform.connectors.model.ModelOutputConverter;

/**
 * Model access for the INFLUX connector.
 * 
 * @author Holger Eichelberger, SSE
 */
public class InfluxModelAccess extends AbstractTypeMappingModelAccess {

    static final String FIELD_TIME = "*T*";
    private static final String SEPARATOR = "_";
    private List<String> nesting = new ArrayList<>();
    private String prefix = "";
    private List<Point> batch = new ArrayList<Point>(10);
    
    /**
     * Data input converter. Internally, INFLUX converts all smaller types to float and long.
     * 
     * @author Holger Eichelberger
     */
    public class InfluxInputConverter extends ModelInputConverter {

        /**
         * Creates an instance.
         */
        public InfluxInputConverter() {
        }
        
        @Override
        public float toFloat(Object data) throws IOException {
            return ((Double) data).floatValue();
        }
        
        @Override
        public int toInteger(Object data) throws IOException {
            return ((Long) data).intValue();
        }

        @Override
        public byte toByte(Object data) throws IOException {
            return ((Long) data).byteValue();
        }

        @Override
        public short toShort(Object data) throws IOException {
            return ((Long) data).shortValue();
        }        
        
    }    

    /**
     * Data output converter. INFLUX converts internally
     * 
     * @author Holger Eichelberger
     */
    public class InfluxOutputConverter extends ModelOutputConverter {

        /**
         * Creates an instance.
         */
        public InfluxOutputConverter() {
        }
        
    }
    
    private InfluxInputConverter inputConverter = new InfluxInputConverter();
    private InfluxOutputConverter outputConverter = new InfluxOutputConverter();
    private InfluxConnector<?, ?> connector;
    private Point writePoint;
    private Instant writePointTime;
    private Map<String, Object> readValues;

    /**
     * Creates an instance.
     * 
     * @param connector the instance
     */
    InfluxModelAccess(InfluxConnector<?, ?> connector) {
        super(connector);
        this.connector = connector;
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
     * Lazily initializes a point to write.
     */
    private void initPoint() {
        if (null == writePoint) {
            writePoint = Point.measurement(connector.getMeasurement());
            writePointTime = null;
        }
    }
    
    /**
     * Clears the point to write.
     */
    private void clearPoint() {
        writePoint = null;
        writePointTime = null;
    }

    @Override
    public void setInt(String qName, int value) throws IOException {
        initPoint();
        writePoint.setField(prefix + qName, value);
    }

    @Override
    public void setLong(String qName, long value) throws IOException {
        initPoint();
        writePoint.setField(prefix + qName, value);
    }
    
    @Override
    public void setLongIndex(String qName, long value) throws IOException {
        writePointTime = Instant.ofEpochMilli(value);
    }
    
    @Override
    public void setFloatIndex(String qName, float value) throws IOException {
        writePointTime = Instant.ofEpochMilli(connector.toTimestamp(value));
    }    

    @Override
    public void setByte(String qName, byte value) throws IOException {
        initPoint();
        writePoint.setField(prefix + qName, value);
    }

    @Override
    public void setShort(String qName, short value) throws IOException {
        initPoint();
        writePoint.setField(prefix + qName, value);
    }
    
    @Override
    public void setBoolean(String qName, boolean value) throws IOException {
        initPoint();
        writePoint.setField(prefix + qName, value);
    }

    @Override
    public void setDouble(String qName, double value) throws IOException {
        initPoint();
        writePoint.setField(prefix + qName, value);
    }

    @Override
    public void setFloat(String qName, float value) throws IOException {
        initPoint();
        writePoint.setField(prefix + qName, value);
    }

    @Override
    public void setString(String qName, String value) throws IOException {
        initPoint();
        if (connector.getTags().contains(qName)) {
            writePoint.setTag(prefix + qName, value);
        } else {
            writePoint.setField(qName, value);
        }
    }

    @Override
    public Object get(String qName) throws IOException {
        if (null != readValues) {
            return readValues.get(prefix + qName);
        } else {
            throw new IOException("No data to read");
        }
    }

    @Override
    public long getLongIndex(String qName) throws IOException {
        Long result = null;
        if (null != readValues) {
            Object time = readValues.get(FIELD_TIME);
            if (time instanceof Instant) {
                Instant instant = (Instant) time;
                result = instant.getEpochSecond();
            }
        }
        if (null == result) {
            throw new IOException("No data to read");
        }
        return result.longValue();
    }
    
    @Override
    public float getFloatIndex(String qName) throws IOException {
        Float result = null;
        if (null != readValues) {
            Object time = readValues.get(FIELD_TIME);
            if (time instanceof Instant) {
                Instant instant = (Instant) time;
                result = connector.fromTimestamp(instant.getEpochSecond());
            }
        }
        if (null == result) {
            throw new IOException("No data to read");
        }
        return result.floatValue();
    }
    
    /**
     * Called by connector when writing of the current object is completed.
     */
    void writeCompleted() {
        if (null != writePoint) {
            Instant time = writePointTime;
            if (null == writePointTime) {
                time = Instant.now();
            }
            writePoint.setTimestamp(time);
            int batchSize = connector.getBatchSize();
            if (batchSize > 1) {
                if (batch.size() == batchSize) {
                    connector.getClient().writePoints(batch);
                    batch.clear();
                } else {
                    batch.add(writePoint);
                }
            } else {
                connector.getClient().writePoint(writePoint);
            }
            clearPoint();
        }
    }
    
    /**
     * Connector sets the data to read from.
     * 
     * @param values the values as map (field name; value)
     */
    void setReadData(Map<String, Object> values) {
        readValues = values;
    }
    
    /**
     * Connector indicates end reading, record can be disposed.
     */
    void readCompleted() {
        readValues = null;
    }
    
    @Override
    public void dispose() {
        writeCompleted(); // prevent data loss
        readCompleted();
    }

    @Override
    public Object call(String qName, Object... arg1) throws IOException {
        return null;
    }

    @Override
    public String getQSeparator() {
        return SEPARATOR;
    }

    @Override
    public <T> T getStruct(String arg0, Class<T> arg1) throws IOException {
        return null;
    }

    @Override
    public void monitor(String... qName) throws IOException {
    }

    @Override
    public void registerCustomType(Class<?> cls) throws IOException {
    }

    @Override
    public void setStruct(String arg0, Object arg1) throws IOException {
    }

    @Override
    public String topInstancesQName() {
        return null;
    }

    @Override
    public void monitor(int notificationInterval, String... qNames) throws IOException {
    }

    @Override
    public void monitorModelChanges(int notificationInterval) throws IOException {
    }

    @Override
    public ModelAccess stepInto(String name) throws IOException {
        nesting.add(name);
        prefix += name + SEPARATOR;
        return this;
    }

    @Override
    public ModelAccess stepOut() {
        if (nesting.size() > 0) {
            String last = nesting.remove(nesting.size() - 1);
            prefix = prefix.substring(0, prefix.length() - last.length() - SEPARATOR.length());
        }
        return this;
    }

    @Override
    public ConnectorParameter getConnectorParameter() {
        return connector.getParameter();
    }
    
}
