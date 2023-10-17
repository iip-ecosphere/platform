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

package test.de.iip_ecosphere.platform.examples.hm23;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.connectors.CachingStrategy;
import de.iip_ecosphere.platform.connectors.ConnectorParameter.CacheMode;
import de.iip_ecosphere.platform.services.environment.services.TransportConverterFactory;
import de.iip_ecosphere.platform.support.CollectionUtils;
import de.iip_ecosphere.platform.support.json.JsonUtils;
import de.iip_ecosphere.platform.transport.serialization.GenericJsonToStringTranslator;
import de.iip_ecosphere.platform.transport.serialization.TypeTranslator;
import de.iip_ecosphere.platform.connectors.DefaultCachingStrategy;
import iip.datatypes.AggregatedPlcEnergyMeasurementImpl;
import iip.datatypes.DriveAiResult;
import iip.datatypes.DriveAiResultImpl;
import iip.datatypes.LenzeDriveMeasurement;
import iip.datatypes.LenzeDriveMeasurementImpl;
import iip.datatypes.LenzeDriveMeasurementProcessImpl;
import iip.datatypes.PlcOutput;
import iip.datatypes.PlcOutputImpl;

/**
 * Tests data properties.
 * 
 * @author Holger Eichelberger, SSE
 */
public class DataTest {
    
    /**
     * Tests PLC data.
     */
    @Test
    public void testPlcData() {
        CachingStrategy hashStrategy = new DefaultCachingStrategy();
        hashStrategy.setCacheMode(CacheMode.HASH);
        CachingStrategy equalsStrategy = new DefaultCachingStrategy();
        equalsStrategy.setCacheMode(CacheMode.EQUALS);
        
        PlcOutput out1 = new PlcOutputImpl();
        out1.setHW_Btn0(false);
        out1.setHW_Btn1(false);
        out1.setHW_Btn2(false);
        out1.setPC_ReadyForRequest(true);
        out1.setPC_RobotBusyOperatingAddInfo((short) 0);
        out1.setSafetyOk(true);
        out1.setUR_BusyOperating(false);
        out1.setUR_InSafePosition(false);
        Assert.assertTrue(hashStrategy.checkCache(out1));
        Assert.assertTrue(equalsStrategy.checkCache(out1));
        Assert.assertFalse(hashStrategy.checkCache(out1));
        Assert.assertFalse(equalsStrategy.checkCache(out1));

        PlcOutput out2 = new PlcOutputImpl();
        out2.setHW_Btn0(false);
        out2.setHW_Btn1(false);
        out2.setHW_Btn2(false);
        out2.setPC_ReadyForRequest(true);
        out2.setPC_RobotBusyOperatingAddInfo((short) 0);
        out2.setSafetyOk(true);
        out2.setUR_BusyOperating(false);
        out2.setUR_InSafePosition(false);

        Assert.assertTrue(out1.equals(out1));
        Assert.assertTrue(out1.equals(out2));
        Assert.assertEquals(out1.hashCode(), out2.hashCode());
        Assert.assertFalse(hashStrategy.checkCache(out2));
        Assert.assertFalse(equalsStrategy.checkCache(out2));

        out2.setHW_Btn1(true);
        Assert.assertFalse(out1.equals(out2));
        Assert.assertTrue(out1.hashCode() != out2.hashCode());
        Assert.assertTrue(hashStrategy.checkCache(out2));
        Assert.assertTrue(equalsStrategy.checkCache(out2));
        Assert.assertFalse(hashStrategy.checkCache(out2));
        Assert.assertFalse(equalsStrategy.checkCache(out2));
    }
    
    /**
     * Tests JSON data pruning in the strange Lenze case.
     * Combined from different parts.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testLenzePruning() throws IOException {
        TypeTranslator<DriveAiResult, String> typeTranslator = TransportConverterFactory.ensureTranslator(
            null, DriveAiResult.class);

        Set<String> excludedFields = CollectionUtils.addAll(new HashSet<String>(), "energy", "drive", "PROCESS");
        String[] exclFields =  excludedFields.toArray(new String[excludedFields.size()]);

        if (typeTranslator instanceof GenericJsonToStringTranslator) {
            JsonUtils.exceptFields(((GenericJsonToStringTranslator<?>) typeTranslator).getMapper(), 
                exclFields);
        }
        
        DriveAiResult result = new DriveAiResultImpl();
        result.setAiId("AId");
        result.setError(new String[] {"e1", "e2", "e3"});
        result.setErrorConfidence(new double[] {0.1, 0.2, 0.3});
        result.setIo(true);
        result.setEnergy(new AggregatedPlcEnergyMeasurementImpl());
        LenzeDriveMeasurement ldm = new LenzeDriveMeasurementImpl();
        ldm.setPROCESS(new LenzeDriveMeasurementProcessImpl());
        result.setDrive(null);

        String tmp = typeTranslator.to(result);
        System.out.println(tmp);
    }

}
