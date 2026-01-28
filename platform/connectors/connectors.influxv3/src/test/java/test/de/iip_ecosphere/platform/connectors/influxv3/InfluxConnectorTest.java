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

package test.de.iip_ecosphere.platform.connectors.influxv3;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.influxdb.v3.client.InfluxDBClient;
import com.influxdb.v3.client.Point;
import com.influxdb.v3.client.PointValues;
import com.influxdb.v3.client.query.QueryOptions;

import de.iip_ecosphere.platform.connectors.ConnectorParameter;
import de.iip_ecosphere.platform.connectors.ConnectorParameter.ConnectorParameterBuilder;
import de.iip_ecosphere.platform.connectors.events.SimpleTimeseriesQuery;
import de.iip_ecosphere.platform.connectors.events.SimpleTimeseriesQuery.TimeKind;
import de.iip_ecosphere.platform.connectors.influxv3.InfluxConnector;
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
@PrepareForTest({InfluxDBClient.class})
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
    private Stream<PointValues> getDataTable() {
        List<PointValues> result = new ArrayList<>();
        Instant now = Instant.now();
        for (MachineData d : expectedData) {
            PointValues record = new PointValues();
            record.setTimestamp(now);
            record.setField("start", now); // machine data has more fields
            record.setField("stop", now);
            record.setField("lotSize", d.getLotSize());
            record.setField("powerConsumption", d.getPowerConsumption());
            record.setField("vendor", d.getVendor());
            now = now.plus(500, ChronoUnit.MILLIS);
            result.add(record);
        }
        return result.stream();
    }
    
    /**
     * Turns a point back to a machine command.
     * 
     * @param point the point
     * @return the machine command instance, <b>null</b> if value access is not possible
     */
    private MachineCommand toMachineCommand(Point point) {
        MachineCommand result = new MachineCommand();
        result.setStart(point.getBooleanField("start")); // machine command has less fields
        result.setStop(point.getBooleanField("stop"));
        result.setLotSize(toInt(point, "lotSize", -1));
        return result;
    }

    /**
     * Turns an INFLUX object value to an int.
     * 
     * @param fields the point fields to read the value from
     * @param field the field to read the value from
     * @param dflt a default value if the stored object does not exist or cannot be converted
     * @return the int value or {@code dflt}
     */
    private int toInt(Point point, String field, int dflt) {
        int result = dflt;
        Long object = point.getIntegerField(field);
        if (object != null) { // INFLUX writes only longs
            result = object.intValue();
        }
        return result;
    }

    // checkstyle: stop exception type check
    
    /**
     * Tests the {@link InfluxConnector}.
     * 
     * @throws Exception shall not occur
     */
    @Test
    public void testInfluxConnector() throws Exception {
        NotificationMode mode = ActiveAasBase.setNotificationMode(NotificationMode.NONE);
        List<MachineData> received = new ArrayList<>();
        MyCustomizer customizer = new MyCustomizer();

        PowerMockito.mockStatic(InfluxDBClient.class);
        InfluxDBClient dbClientMock = PowerMockito.mock(InfluxDBClient.class);
        PowerMockito.when(dbClientMock.queryPoints(Mockito.anyString(), Mockito.any(QueryOptions.class)))
            .thenReturn(getDataTable());
        PowerMockito.doAnswer(in -> { 
            written = in.getArgument(0, Point.class);
            return null;
        }).doNothing().when(dbClientMock).writePoint(Mockito.any(Point.class));
        PowerMockito.doAnswer(in -> { 
            written = in.getArgument(0, Point.class);
            return null;
        }).doNothing().when(dbClientMock).writePoints(Mockito.anyList());

        PowerMockito.doNothing().when(dbClientMock).close();
        PowerMockito.when(InfluxDBClient.getInstance(Mockito.anyString(), Mockito.any(char[].class), 
            Mockito.anyString())).thenReturn(dbClientMock);
        
        InfluxConnector<MachineData, MachineCommand> conn = new InfluxConnector<>(
            new TranslatingProtocolAdapter<Object, Object, MachineData, MachineCommand>(
                new MachineDataOutputTranslator<Object>(false, Object.class, customizer),
                new MachineCommandInputTranslator<Object>(Object.class, customizer)));
        Map<String, IdentityToken> identities = new HashMap<>();
        byte[] token = "MyToken".getBytes();
        identities.put(ConnectorParameter.ANY_ENDPOINT, 
            IdentityTokenBuilder.newBuilder().setIssuedToken(token, "plain").build());
        ConnectorParameter param = ConnectorParameterBuilder.newBuilder("localhost", 1234, Schema.HTTP) // mocked
            .setSpecificSetting("DATABASE", "myDatabase")
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

    // checkstyle: resume exception type check
    
}
