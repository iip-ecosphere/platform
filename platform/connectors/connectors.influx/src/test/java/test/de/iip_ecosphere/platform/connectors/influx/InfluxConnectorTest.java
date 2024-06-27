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

package test.de.iip_ecosphere.platform.connectors.influx;

import java.io.IOException;
import java.lang.reflect.Field;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.QueryApi;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;

import de.iip_ecosphere.platform.connectors.ConnectorParameter;
import de.iip_ecosphere.platform.connectors.ConnectorParameter.ConnectorParameterBuilder;
import de.iip_ecosphere.platform.connectors.events.SimpleTimeseriesQuery;
import de.iip_ecosphere.platform.connectors.events.SimpleTimeseriesQuery.TimeKind;
import de.iip_ecosphere.platform.connectors.influx.InfluxConnector;
import de.iip_ecosphere.platform.connectors.model.ModelAccess;
import de.iip_ecosphere.platform.connectors.types.TranslatingProtocolAdapter;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.identities.IdentityToken;
import de.iip_ecosphere.platform.support.identities.IdentityToken.IdentityTokenBuilder;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase.NotificationMode;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import test.de.iip_ecosphere.platform.connectors.MachineCommand;
import test.de.iip_ecosphere.platform.connectors.MachineCommandInputTranslator;
import test.de.iip_ecosphere.platform.connectors.MachineCommandInputTranslator.InputCustomizer;
import test.de.iip_ecosphere.platform.connectors.MachineData;
import test.de.iip_ecosphere.platform.connectors.MachineDataOutputTranslator;
import test.de.iip_ecosphere.platform.connectors.MachineDataOutputTranslator.OutputCustomizer;

/**
 * Tests the {@link InfluxConnector}.
 * 
 * @author Holger Eichelberger, SSE
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({InfluxDBClient.class, InfluxDBClientFactory.class})
public class InfluxConnectorTest {

    private Point written = null;
    private List<MachineData> expectedData = new ArrayList<>();
    
    {
        expectedData.add(new MachineData(1, 1.24, "DMG"));
        expectedData.add(new MachineData(5, 3.12, "Fanuc"));
        expectedData.add(new MachineData(2, 0.72, "Kuka"));
    }

    /**
     * Customizes the type translators/assertions for this case.
     * 
     * @author Holger Eichelberger, SSE
     */
    private class MyCustomizer implements OutputCustomizer, InputCustomizer {

        @Override
        public String getQNameOperationStartMachine() {
            return null;
        }

        @Override
        public String getQNameOperationStopMachine() {
            return null;
        }

        @Override
        public void additionalFromActions(ModelAccess access, MachineCommand data) throws IOException {
        }

        @Override
        public void initializeModelAccess(ModelAccess access, boolean withNotifications) throws IOException {
        }

        @Override
        public String getVendor(ModelAccess access) throws IOException {
            return access.getString("vendor");
        }

        @Override
        public String getTopLevelModelPartName() {
            return "";
        }

        @Override
        public String getQNameVarLotSize() {
            return "lotSize";
        }

        @Override
        public String getQNameVarPowerConsumption() {
            return "powerConsumption";
        }
        
        @Override
        public boolean assertSetExceptions() {
            return false;
        }

        @Override
        public boolean assertOperationExceptions() {
            return false;
        }
        
        @Override
        public String getQNameStart() {
            return "start";
        }

        @Override
        public String getQNameStop() {
            return "stop";
        }

        @Override
        public boolean assertNotExistingProperties() {
            return false;
        }

    }
    
    /**
     * Creates/returns INFLUX data/query result for mocking.
     * 
     * @return the table(s)
     */
    private List<FluxTable> getDataTable() {
        List<FluxTable> result = new ArrayList<>();
        FluxTable table = new FluxTable();
        result.add(table);
        List<FluxRecord> records = table.getRecords();
        Instant now = Instant.now();
        for (MachineData d : expectedData) {
            addFluxRecord(records, now, "lotSize", d.getLotSize());
            addFluxRecord(records, now, "powerConsumption", d.getPowerConsumption());
            addFluxRecord(records, now, "vendor", d.getVendor());
            now = now.plus(500, ChronoUnit.MILLIS);
        }
        return result;
    }
    
    /**
     * Adds a flux record to {@code records}.
     * 
     * @param records the records list to modify
     * @param now the time stamp(s) of the record
     * @param field the field to represent
     * @param value the value of the field
     */
    private void addFluxRecord(List<FluxRecord> records, Instant now, String field, Object value) {
        FluxRecord record = new FluxRecord(1);
        Map<String, Object> values = record.getValues();
        values.put("_start", now);
        values.put("_stop", now);
        values.put("_time", now);
        values.put("_field", field);
        if (value instanceof Float) { // INFLUX cast
            value = Double.valueOf((float) value);
        } else if (value instanceof Integer) {
            value = Long.valueOf((int) value);
        } else if (value instanceof Short) {
            value = Long.valueOf((short) value);
        } else if (value instanceof Byte) {
            value = Long.valueOf((byte) value);
        }
        values.put("_value", value);
        records.add(record);
    }
    
    /**
     * Turns a point back to a machine command.
     * 
     * @param point the point
     * @return the machine command instance, <b>null</b> if value access is not possible
     */
    private MachineCommand toMachineCommand(Point point) {
        MachineCommand result = new MachineCommand();
        try {
            Field fld = Point.class.getDeclaredField("fields");
            fld.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, Object> fields = (Map<String, Object>) fld.get(point);
            result.setStart(toBoolean(fields, "start"));
            result.setStop(toBoolean(fields, "stop"));
            result.setLotSize(toInt(fields, "lotSize", -1));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            result = null;
        }
        return result;
    }

    /**
     * Turns an INFLUX object value to a boolean.
     * 
     * @param fields the point fields to read the value from
     * @param field the field to read the value from
     * @return the boolean value, <b>false</b> if the value does not exist
     */
    private boolean toBoolean(Map<String, Object> fields, String field) {
        Object object = fields.get(field);
        return object instanceof Boolean && (Boolean) object;
    }

    /**
     * Turns an INFLUX object value to an int.
     * 
     * @param fields the point fields to read the value from
     * @param field the field to read the value from
     * @param dflt a default value if the stored object does not exist or cannot be converted
     * @return the int value or {@code dflt}
     */
    private int toInt(Map<String, Object> fields, String field, int dflt) {
        int result = dflt;
        Object object = fields.get(field);
        if (object instanceof Number) { // INFLUX writes only longs
            result = ((Number) object).intValue();
        }
        return result;
    }

    /**
     * Tests the {@link InfluxConnector}.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testInfluxConnector() throws IOException {
        NotificationMode mode = ActiveAasBase.setNotificationMode(NotificationMode.NONE);
        List<MachineData> received = new ArrayList<>();
        MyCustomizer customizer = new MyCustomizer();

        PowerMockito.mockStatic(InfluxDBClientFactory.class);
        PowerMockito.mockStatic(InfluxDBClient.class);
        InfluxDBClient dbClientMock = PowerMockito.mock(InfluxDBClient.class);
        QueryApi dbQueryApiMock = PowerMockito.mock(QueryApi.class);
        PowerMockito.when(dbQueryApiMock.query(Mockito.anyString())).thenReturn(getDataTable());
        WriteApiBlocking dbWriteApiBlockingMock = PowerMockito.mock(WriteApiBlocking.class);
        PowerMockito.doAnswer(in -> { 
            written = in.getArgument(0, Point.class);
            return null;
        }).doNothing().when(dbWriteApiBlockingMock).writePoint(Mockito.any(Point.class));
        PowerMockito.doNothing().when(dbClientMock).close();
        PowerMockito.when(dbClientMock.getQueryApi()).thenReturn(dbQueryApiMock);
        PowerMockito.when(dbClientMock.getWriteApiBlocking()).thenReturn(dbWriteApiBlockingMock);
        PowerMockito.when(InfluxDBClientFactory.create(Mockito.anyString(), Mockito.any(char[].class), 
            Mockito.anyString(), Mockito.anyString())).thenReturn(dbClientMock);
        
        InfluxConnector<MachineData, MachineCommand> conn = new InfluxConnector<>(
            new TranslatingProtocolAdapter<Object, Object, MachineData, MachineCommand>(
                new MachineDataOutputTranslator<Object>(false, Object.class, customizer),
                new MachineCommandInputTranslator<Object>(Object.class, customizer)));
        Map<String, IdentityToken> identities = new HashMap<>();
        byte[] token = "MyToken".getBytes();
        identities.put(ConnectorParameter.ANY_ENDPOINT, 
            IdentityTokenBuilder.newBuilder().setIssuedToken(token, "plain").build());
        ConnectorParameter param = ConnectorParameterBuilder.newBuilder("localhost", 1234, Schema.HTTP) // mocked
            .setSpecificSetting("ORG", "myOrganization")
            .setSpecificSetting("BUCKET", "myBucket")
            .setSpecificSetting("MEASUREMENT", "machineData")
            .setSpecificSetting("TAGS", "") // none so far
            .setSpecificSetting("BATCH", "1") // so far no batching
            .setIdentities(identities)
            .build();
        conn.connect(param);
        conn.setReceptionCallback(new ReceptionCallback<MachineData>() {
            
            @Override
            public void received(MachineData data) {
                received.add(data);
            }
            
            @Override
            public Class<MachineData> getType() {
                return MachineData.class;
            }
        });

        MachineCommand cmd = new MachineCommand();
        cmd.setStart(true);
        cmd.setLotSize(4);
        conn.write(cmd);
        
        SimpleTimeseriesQuery q = new SimpleTimeseriesQuery(0, TimeKind.ABSOLUTE, -1, TimeKind.UNSPECIFIED);
        conn.trigger(q);
        
        TimeUtils.sleep(3000); // 3*500
        
        conn.disconnect();
        ActiveAasBase.setNotificationMode(mode);
        
        Assert.assertNotNull(written);
        Assert.assertEquals(cmd, toMachineCommand(written));
        Assert.assertEquals(expectedData.size(), received.size());
        Assert.assertEquals(expectedData, received);
    }

}
