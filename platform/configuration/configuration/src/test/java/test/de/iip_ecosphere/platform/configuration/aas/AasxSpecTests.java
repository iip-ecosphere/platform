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

package test.de.iip_ecosphere.platform.configuration.aas;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.configuration.aas.ReadAasxFile;
import de.iip_ecosphere.platform.support.FileUtils;

/**
 * IDTA spec test cases (AASX reader).
 * 
 * @author Holger Eichelberger, SSE
 */
public class AasxSpecTests {

    /**
     * Tests a specification with the XLS reader.
     * 
     * @param fileName the file name of the spec, without extension
     * @throws IOException if reading/writing any file fails
     */
    private void testSpecWithAasx(String fileName) throws IOException {
        testSpecWithAasx(fileName, null);
    }
    
    /**
     * Tests a specification with the XLS reader.
     * 
     * @param fileName the file name of the spec, without extension
     * @param idShortPrefix optional prefix for idShorts, may be <b>null</b> (use "name" instead)
     * @throws IOException if reading/writing any file fails
     */
    private void testSpecWithAasx(String fileName, String idShortPrefix) throws IOException {
        Charset cs = Charset.defaultCharset();
        String specNumber = ReadAasxFile.getSpecNumber(fileName);
        ReadAasxFile.main(ExcelSpecTests.RESOURCES_DIR + fileName + ".aasx", ExcelSpecTests.TMP_OUT, 
            specNumber, idShortPrefix);
        String out = FileUtils.readFileToString(new File(ExcelSpecTests.TMP_OUT), cs);
        out = out.replaceAll("[^\\x00-\\x7F]", "");
        String spec = FileUtils.readFileToString(new File(ExcelSpecTests.RESOURCES_DIR + fileName + ".aasx.spec"), cs);
        spec = spec.replaceAll("[^\\x00-\\x7F]", "");
        Assert.assertEquals(spec, out);
        
        String text = FileUtils.readFileToString(new File(ExcelSpecTests.TMP_TEXT), cs);
        Assert.assertTrue(text.length() > 0);
    }
    
    /**
     * Tests IDTA 02001-1-0.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testIdta02001_1_0() throws IOException {
        testSpecWithAasx("2001/IDTA 02001-1-0_Subomdel_MTPv1.0-rc2-with-documentation");
    }    

    /**
     * Tests IDTA 02002-1-0.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testIdta02002_1_0() throws IOException {
        testSpecWithAasx("2002/IDTA 02002-1-0_Template_ContactInformation");
    }

    /**
     * Tests IDTA 02003-1-2.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testIdta02003_1_2() throws IOException {
        testSpecWithAasx("2003/IDTA 02003-1-2_SubmodelTemplate_TechnicalData_v1.2_withQualifier");
    }

    /**
     * Tests IDTA 02004-1-2.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testIdta02004_1_2() throws IOException {
        testSpecWithAasx("2004/IDTA 02004-1-2_Template_Handover Documentation");
    }

    /**
     * Tests IDTA 02005-1-0.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testIdta02005_1_0() throws IOException {
        testSpecWithAasx("2005/IDTA 02005-1-0_Template_ProvisionOfSimulationModels");
    }
    
    /**
     * Tests IDTA 02006-2-0.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testIdta02006_2_0() throws IOException {
        testSpecWithAasx("2006/IDTA 02006-2-0_Template_Digital Nameplate");
    }    
    
    /**
     * Tests IDTA 02007-1-0.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testIdta02007_1_0() throws IOException {
        testSpecWithAasx("2007/IDTA 02007-1-0_Template_Software Nameplate");
    }

    /**
     * Tests IDTA 02008-1-1.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testIdta02008_1_1() throws IOException {
        testSpecWithAasx("2008/IDTA 02008-1-1_Template_withOperations_TimeSeriesData");
    }

    /**
     * Tests IDTA 02010-1-1.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testIdta02010_1_0() throws IOException {
        testSpecWithAasx("2010/IDTA 02010-1-0_Template_ServiceRequestNotification");
    }

    /**
     * Tests IDTA 02011-1-0.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testIdta02011_1_0() throws IOException {
        testSpecWithAasx("2011/IDTA 02011-1-0_Template_HierarchicalStructuresEnablingBoM");
    }

    /**
     * Tests IDTA 02012-1-0.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testIdta02012_1_0() throws IOException {
        testSpecWithAasx("2012/IDTA 02012-1-0_Template_DEXPI");
    }

    /**
     * Tests IDTA 02013-1-0.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testIdta02013_1_0() throws IOException {
        testSpecWithAasx("2013/IDTA 02013-1-0_Template_Reliability", "Ry");
    }

    /**
     * Tests IDTA 02014-1-0.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testIdta02014_1_0() throws IOException {
        testSpecWithAasx("2014/IDTA 02014-1-0_Template_FunctionalSafety", "Fs");
    }

    /**
     * Tests IDTA 02015-1-0.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testIdta02015_1_0() throws IOException {
        testSpecWithAasx("2015/IDTA 02015-1-0 _Template_ControlComponentType", "Ct");
    }

    /**
     * Tests IDTA 02016-1-0.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testIdta02016_1_0() throws IOException {
        testSpecWithAasx("2016/IDTA 02016-1-0 _Template_ControlComponentInstance", "Ci");
    }

    /**
     * Tests IDTA 02017-1-0.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testIdta02017_1_0() throws IOException {
        testSpecWithAasx("2017/IDTA 02017-1-0_Template_Asset Interfaces Description");
    }

    /**
     * Tests IDTA 02021-1-0.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testIdta02021_1_0() throws IOException {
        testSpecWithAasx("2021/IDTA 02021-1-0_Template_Sizing of Power Drive Trains");
    }

    /**
     * Tests IDTA 02023-0-9.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testIdta02023_0_9() throws IOException {
        testSpecWithAasx("2023/IDTA 2023-0-9 _Template_CarbonFootprint");
    }
    
}
