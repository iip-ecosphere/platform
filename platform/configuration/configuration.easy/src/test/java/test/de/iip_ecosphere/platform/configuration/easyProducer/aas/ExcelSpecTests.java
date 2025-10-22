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

package test.de.iip_ecosphere.platform.configuration.easyProducer.aas;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import org.junit.Test;

import de.iip_ecosphere.platform.configuration.easyProducer.aas.ReadExcelFile;
import de.iip_ecosphere.platform.support.FileUtils;

import org.junit.Assert;

/**
 * IDTA spec test cases (XLS reader).
 * 
 * @author Holger Eichelberger, SSE
 */
public class ExcelSpecTests {
    
    static final String RESOURCES_DIR = "src/test/resources/idta/";
    static final String TMP_OUT = FileUtils.getTempDirectoryPath() + "/idta.ivml";
    static final String TMP_TEXT = FileUtils.getTempDirectoryPath() + "/idta.text";

    /**
     * Tests a specification with the XLS reader.
     * 
     * @param fileName the file name of the spec, without extension
     * @throws IOException if reading/writing any file fails
     */
    private void testSpecWithXls(String fileName) throws IOException {
        testSpecWithXls(fileName, null);    
    }
    
    /**
     * Tests a specification with the XLS reader.
     * 
     * @param fileName the file name of the spec, without extension
     * @param idShortPrefix optional prefix for idShorts, may be <b>null</b> (use "name" instead)
     * @throws IOException if reading/writing any file fails
     */
    private void testSpecWithXls(String fileName, String idShortPrefix) throws IOException {
        Charset cs = Charset.defaultCharset();
        ReadExcelFile.main(RESOURCES_DIR + fileName + ".xlsx", TMP_OUT, idShortPrefix);
        String out = FileUtils.readFileToString(new File(TMP_OUT), cs);
        out = out.replaceAll("[^\\x00-\\x7F]", "");
        String spec = FileUtils.readFileToString(new File(RESOURCES_DIR + fileName + ".pdf.spec"), cs);
        spec = spec.replaceAll("[^\\x00-\\x7F]", "");
        /*for (int i = 0; i < Math.min(out.length(), spec.length()); i++) {
            if (out.charAt(i) != spec.charAt(i)) {
                System.out.println("Diff at " + i + ": " + ((int) spec.charAt(i)) + " " + ((int) out.charAt(i)) 
                    + " " + spec.charAt(i) + " " + out.charAt(i));
                break;
            }
        }*/
        Assert.assertEquals(spec, out);
        
        String text = FileUtils.readFileToString(new File(TMP_TEXT), cs);
        Assert.assertTrue(text.length() > 0);
    }

    /**
     * Tests IDTA-02001-1-0.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testIdta02001_1_0_xls() throws IOException {
        testSpecWithXls("2001/IDTA 02001-1-0_Submodel_MTP.print");
    }

    /**
     * Tests IDTA-02002-1-0.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testIdta02002_1_0_xls() throws IOException {
        testSpecWithXls("2002/IDTA-02002-1-0_Submodel_ContactInformation");
    }

    /**
     * Tests IDTA-02003-1-2.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testIdta02003_1_2_xls() throws IOException {
        testSpecWithXls("2003/IDTA-02003-1-2_Submodel_TechnicalData");
    }

    /**
     * Tests IDTA-02004-1-2.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testIdta02004_1_2_xls() throws IOException {
        testSpecWithXls("2004/IDTA 02004-1-2_Submodel_Handover Documentation");
    }

    /**
     * Tests IDTA-02005-1-0.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testIdta02005_1_0_xls() throws IOException {
        testSpecWithXls("2005/IDTA 02005-1-0_Submodel_ProvisionOfSimulationModels");
    }
    
    /**
     * Tests IDTA-02006-2-0.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testIdta02006_2_0_xls() throws IOException {
        testSpecWithXls("2006/IDTA-02006-2-0_Submodel_Digital-Nameplate");
    }

    /**
     * Tests IDTA-02007-1-0.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testIdta02007_1_0_xls() throws IOException {
        testSpecWithXls("2007/IDTA-02007-1-0_Submodel_Software-Nameplate");
    }

    /**
     * Tests IDTA-02008-1-1.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testIdta02008_1_1_xls() throws IOException {
        testSpecWithXls("2008/IDTA 02008-1-1_Submodel_TimeSeriesData");
    }

    /**
     * Tests IDTA-02010-1-0.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testIdta02010_1_0_xls() throws IOException {
        testSpecWithXls("2010/IDTA 02010-1-0_Submodel_ServiceRequestNotification");
    }

    /**
     * Tests IDTA-02011-1-0.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testIdta02011_1_0_xls() throws IOException {
        testSpecWithXls("2011/IDTA-02011-1-0_Submodel_HierarchicalStructuresEnablingBoM");
    }
    
    /**
     * Tests IDTA-02012-1-0.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testIdta02012_1_0_xls() throws IOException {
        testSpecWithXls("2012/IDTA 02012-1-0_Submodel_DEXPI");
    }

    /**
     * Tests IDTA-02013-1-0.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testIdta02013_1_0_xls() throws IOException {
        testSpecWithXls("2013/IDTA 02013-1-0_Submodel_Reliability", "Ry");
    }

    /**
     * Tests IDTA-02013-1-0.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testIdta02014_1_0_xls() throws IOException {
        testSpecWithXls("2014/IDTA 02014-1-0_Submodel_FunctionalSafety", "Fs");
    }
    
    /**
     * Tests IDTA 02015-1-0.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testIdta02015_1_0_xls() throws IOException {
        testSpecWithXls("2015/IDTA 02015-1-0 _Submodel_ControlComponentType.print", "Ct");
    }

    /**
     * Tests IDTA 02016-1-0.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testIdta02016_1_0_xls() throws IOException {
        testSpecWithXls("2016/IDTA 02016-1-0 _Submodel_ControlComponentInstance", "Ci");
    }

    /**
     * Tests IDTA 02017-1-0.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testIdta02017_1_0_xls() throws IOException {
        testSpecWithXls("2017/IDTA 02017-1-0_Submodel_Asset Interfaces Description.mod");
    }

    /**
     * Tests IDTA-02023-0-9.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testIdta02021_1_0_xls() throws IOException {
        testSpecWithXls("2021/IDTA 02021-1-0_Submodel_Sizing of Power Drive Trains");
    }

    /**
     * Tests IDTA-02023-0-9.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testIdta02023_0_9_xls() throws IOException {
        testSpecWithXls("2023/IDTA 2023-0-9 _Submodel_CarbonFootprint");
    }
    
    /**
     * Tests 2023-01-24 - Draft_IDTA_Submodel_PCF.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testIdtaDraft20230124_xls() throws IOException {
        testSpecWithXls("2023-01-24 - Draft_IDTA_Submodel_PCF-mod", "Draft");
    }    

    /**
     * Tests IDTA-02022-1-0.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testIdta02022_1_0_xls() throws IOException {
        testSpecWithXls("2022/IDTA 02022-1-0_Submodel_Wireless Communication");
    }
    
    /**
     * Tests IDTA-02026-1-0.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testIdta02026_1_0_xls() throws IOException {
        testSpecWithXls("2026/IDTA_02026-1-0_Submodel_ProvisionOf3DModels");
    }
 
    /**
     * Tests IDTA-02027-1-0.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testIdta02027_1_0_xls() throws IOException {
        testSpecWithXls("2027/IDTA 02027-1-0_Submodel_AssetInterfacesMappingConfiguration");
    }

    /**
     * Tests IDTA-02034-1-0.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testIdta02034_1_0_xls() throws IOException {
        testSpecWithXls("2034/IDTA 02034-1-0 Submodel_CreationAndClassificationOfMaterial");
    }

    /**
     * Tests IDTA-02045-1-0.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testIdta02045_1_0_xls() throws IOException {
        testSpecWithXls("2045/IDTA 02045-1-0_Submodel_Data Model for Asset Location");
    }

    /**
     * Tests IDTA-02046-1-0.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testIdta02046_1_0_xls() throws IOException {
        testSpecWithXls("2046/IDTA 02046-1-0_Submodel_WorkstationWorkerMatchingData");
    }

    /**
     * Tests IDTA-02056-1-0.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testIdta02056_1_0_xls() throws IOException {
        testSpecWithXls("2056/IDTA 02056-1-0_Submodel_Data Retention Policies");
    }

}
