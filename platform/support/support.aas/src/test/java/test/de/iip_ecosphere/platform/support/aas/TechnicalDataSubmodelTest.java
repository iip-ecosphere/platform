/**
 * ******************************************************************************
 * Copyright (c) {2021} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.support.aas;

import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.Test;

import de.iip_ecosphere.platform.support.CollectionUtils;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.FileDataElement;
import de.iip_ecosphere.platform.support.aas.LangString;
import de.iip_ecosphere.platform.support.aas.MultiLanguageProperty;
import de.iip_ecosphere.platform.support.aas.SubmodelElement;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.aas.types.technicaldata.FurtherInformation;
import de.iip_ecosphere.platform.support.aas.types.technicaldata.GeneralInformation;
import de.iip_ecosphere.platform.support.aas.types.technicaldata.ProductClassificationItem;
import de.iip_ecosphere.platform.support.aas.types.technicaldata.ProductClassifications;
import de.iip_ecosphere.platform.support.aas.types.technicaldata.TechnicalDataSubmodel;
import de.iip_ecosphere.platform.support.aas.types.technicaldata.TechnicalDataSubmodelBuilder;
import de.iip_ecosphere.platform.support.aas.types.technicaldata.TechnicalDataSubmodelBuilder.FurtherInformationBuilder;
import de.iip_ecosphere.platform.support.aas.types.technicaldata.TechnicalDataSubmodelBuilder.GeneralInformationBuilder;
import de.iip_ecosphere.platform.support.aas.types.technicaldata.TechnicalDataSubmodelBuilder.ProductClassificationsBuilder;
import de.iip_ecosphere.platform.support.aas.types.technicaldata.TechnicalDataSubmodelBuilder.TechnicalPropertiesBuilder;
import de.iip_ecosphere.platform.support.aas.types.technicaldata.TechnicalProperties;

import org.junit.Assert;

/**
 * A test for the wrapped technical data submodel.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TechnicalDataSubmodelTest {

    private XMLGregorianCalendar cal;
    private String manufacturerName = "manu AG";
    private String manufacturerPartNumber = "12345";
    private LangString manufacturerProductDesignation = new LangString("EN", "Desig");
    private String manufacturerOrderCode = "O12345O";
    private LangString furtherInformationStmt1 = new LangString("EN", "My Statement");
    private LangString furtherInformationStmt2 = new LangString("DE", "Mein Text");
    
    /**
     * Returns the Logo short ID of the manufacturer logo in {@link GeneralInformation}.
     * 
     * @return the id
     */
    protected String getGeneralInformationManufacturerLogoId() {
        return GeneralInformation.MANUFACTURER_LOGOID;
    }

    /**
     * Turns a plain id into an id as used by {@link GeneralInformation} for product images. Although defined
     * by standards, implementation my do that ultimately in individual manner. 
     *  
     * @param id the plain id
     * @return the transformed id
     */
    protected String toGeneralInformationProductImageFileId(String id) {
        return GeneralInformation.PRODUCTIMAGE_PREFIX + id;
    }
    
    /**
     * Tests the technical data.
     * 
     * @throws DatatypeConfigurationException if XML Gregorian calendar is not available
     * @throws ExecutionException shall not occur
     */
    @Test
    public void testTechnicalDataSubmodel() throws DatatypeConfigurationException, ExecutionException {
        AasFactory factory = AasFactory.getInstance();
        AasBuilder aasBuilder = factory.createAasBuilder("MyAas", null);
        TechnicalDataSubmodelBuilder tdSmBuilder = new TechnicalDataSubmodelBuilder(aasBuilder, null);
        assertBuildFail(tdSmBuilder);
        createFurtherInformation(tdSmBuilder);
        assertBuildFail(tdSmBuilder);
        createGeneralInformation(tdSmBuilder);
        assertBuildFail(tdSmBuilder);
        createProductClassifications(tdSmBuilder);
        assertBuildFail(tdSmBuilder);
        createTechnicalProperties(tdSmBuilder);
        
        tdSmBuilder.build();
        Aas aas = aasBuilder.build();
        
        TechnicalDataSubmodel tsub = new TechnicalDataSubmodel(aas);
        Assert.assertNotNull(tsub);
        assertFurtherInformation(tsub);
        assertGeneralInformation(tsub);
        assertProductClassifications(tsub);
        assertTechnicalProperties(tsub);
    }
    
    /**
     * Creates the further information submodel elements collection.
     *  
     * @param tdSmBuilder the technical data submodel builder
     * @throws DatatypeConfigurationException shall not occur
     */
    private void createFurtherInformation(TechnicalDataSubmodelBuilder tdSmBuilder) 
        throws DatatypeConfigurationException {
        final GregorianCalendar now = new GregorianCalendar();
        cal = DatatypeFactory.newInstance().newXMLGregorianCalendar(now);
        FurtherInformationBuilder fiBuilder = tdSmBuilder.createFurtherInformationBuilder(cal);
        fiBuilder.addStatement("myId", furtherInformationStmt1);
        fiBuilder.build();
    }

    /**
     * Creates the general information submodel elements collection.
     *  
     * @param tdSmBuilder the general information submodel builder
     */
    private void createGeneralInformation(TechnicalDataSubmodelBuilder tdSmBuilder) {
        GeneralInformationBuilder giBuilder = tdSmBuilder.createGeneralInformationBuilder(manufacturerName, 
            manufacturerPartNumber, manufacturerOrderCode, manufacturerProductDesignation);
        giBuilder.addProductImageFile("imgP0.png", "PNG");
        giBuilder.setManufacturerLogo("manuAg.png", "PNG");
        giBuilder.build();
    }

    /**
     * Creates the product classifications submodel elements collection.
     *  
     * @param tdSmBuilder the product classifications submodel builder
     */
    private void createProductClassifications(TechnicalDataSubmodelBuilder tdSmBuilder) {
        ProductClassificationsBuilder pcBuilder = tdSmBuilder.createProductClassificationsBuilder();
        pcBuilder
            .createProductClassificationItemBuilder("ECLASS", "a1274858")
            .setClassificationSystemVersion("1.2.3")
            .build();
        pcBuilder.createProductClassificationItemBuilder("ICS", "a-1274858").build();
        pcBuilder.build();
    }

    /**
     * Creates the technical properties submodel elements collection.
     *  
     * @param tdSmBuilder the technical properties submodel builder
     */
    private void createTechnicalProperties(TechnicalDataSubmodelBuilder tdSmBuilder) {
        TechnicalPropertiesBuilder tpBuilder = tdSmBuilder.createTechnicalPropertiesBuilder();
        tpBuilder.createMainSectionBuilder("main1", false, false).build();
        tpBuilder.createMainSectionBuilder("main2", false, false).build();
        tpBuilder.createSubSectionBuilder("sub1", false, false).build();
        tpBuilder.createSubSectionBuilder("sub2", false, false).build();
        tpBuilder.build();
    }

    /**
     * Asserts that the submodel cannot be built as incomplete.
     * 
     * @param tdSmBuilder the technical data submodel builder
     */
    private static void assertBuildFail(TechnicalDataSubmodelBuilder tdSmBuilder) {
        try {
            tdSmBuilder.build();
            Assert.fail("No exception thrown");
        } catch (IllegalArgumentException e) {
            // this is ok, incomplete
        }
    }
    
    /**
     * Asserts the created properties of the further information submodel.
     * 
     * @param tsub the technical data submodel
     * @throws ExecutionException shall not occur
     */
    private void assertFurtherInformation(TechnicalDataSubmodel tsub) throws ExecutionException {
        FurtherInformation fi = tsub.getFurtherInformation();
        Assert.assertNotNull(fi);
        XMLGregorianCalendar fiCal = fi.getValidDate();
        Assert.assertNotNull(fiCal);
        Assert.assertEquals(cal, fiCal);
        Iterable<MultiLanguageProperty> fiStmts = fi.getStatements();
        Assert.assertNotNull(fiStmts);
        assertMLP(furtherInformationStmt1, fi.getStatement(01));

        // modifications
        fi.setValidDate(cal);
        Map<String, Collection<LangString>> stmts = new HashMap<>();
        List<LangString> stmts1 = new ArrayList<LangString>();
        stmts1.add(furtherInformationStmt1);
        stmts1.add(furtherInformationStmt2);
        stmts.put("myId", stmts1);
        /*fi.setStatements(stmts);
        fiStmts = fi.getStatements();
        Assert.assertNotNull(fiStmts);
        Assert.assertNotNull(fiStmts.get(myId));
        Assert.assertTrue(fiStmts.get(myId).contains(furtherInformationStmt1));
        Assert.assertTrue(fiStmts.get(myId).contains(furtherInformationStmt2));*/
    }

    /**
     * Asserts that {@code expected} is in {@code actual}.
     * 
     * @param expected the expected LangString
     * @param actual the actual multi language property
     */
    private void assertMLP(LangString expected, MultiLanguageProperty actual) {
        Assert.assertNotNull(actual);
        String lang = expected.getLanguage();
        Assert.assertTrue(actual.getDescription().containsKey(lang));
        Assert.assertEquals(expected.getDescription(), actual.getDescription().get(lang).getDescription());
    }

    /**
     * Asserts the created properties of the further general information submodel.
     * 
     * @param tsub the general information submodel
     * @throws ExecutionException shall not occur
     */
    private void assertGeneralInformation(TechnicalDataSubmodel tsub) throws ExecutionException {
        GeneralInformation gi = tsub.getGeneralInformation();
        Assert.assertNotNull(gi);
        Assert.assertEquals(manufacturerName, gi.getManufacturerName());
        MultiLanguageProperty desig = gi.getManufacturerProductDesignation();
        Assert.assertNotNull(desig);
        assertMLP(manufacturerProductDesignation, desig);
        Assert.assertEquals(manufacturerPartNumber, gi.getManufacturerArticleNumber());
        Assert.assertEquals(manufacturerOrderCode, gi.getManufacturerOrderCode());

        FileDataElement logo = gi.getManufacturerLogo();
        assertEquals(getGeneralInformationManufacturerLogoId(), "manuAg.png", "PNG", logo);
        List<FileDataElement> files = CollectionUtils.toList(gi.getProductImages().iterator());
        Assert.assertNotNull(files);
        Assert.assertTrue(files.size() == 1);
        assertEquals(toGeneralInformationProductImageFileId("01"), "imgP0.png", "PNG", files.get(0));

        // modifications
        // currently none
    }
    
    /**
     * Asserts that a file data element fits expected values.
     * 
     * @param id the id of the element, if <b>null</b> {@code actual} must be <b>null</b>
     * @param file the file
     * @param mime the mime type
     * @param actual the actual file data element to assert
     */
    private static void assertEquals(String id, String file, String mime, FileDataElement actual) {
        if (null == id) {
            Assert.assertNull(actual);
        } else {
            Assert.assertNotNull(actual);
            Assert.assertEquals(id, actual.getIdShort());
            Assert.assertEquals(file, actual.getContents());
            Assert.assertEquals(mime, actual.getMimeType());
        }
    }

    /**
     * Asserts the created properties of the product classifications submodel.
     * 
     * @param tsub the product classifications submodel
     * @throws ExecutionException shall not occur
     */
    private void assertProductClassifications(TechnicalDataSubmodel tsub) throws ExecutionException {
        ProductClassifications pc = tsub.getProductClassifications();
        Assert.assertNotNull(pc);

        Assert.assertEquals(2, pc.getProductClassificationItemsCount());
        ProductClassificationItem i = pc.getProductClassificationItem(1);
        Assert.assertNotNull(i);
        assertEquals("ProductClassificationItem01", "ECLASS", "1.2.3", "a1274858", i);
        
        i = pc.getProductClassificationItem(2);
        Assert.assertNotNull(i);
        assertEquals("ProductClassificationItem02", "ICS", null, "a-1274858", i);
    }
    
    /**
     * Asserts whether a {@link ProductClassificationItem} has expected information.
     * 
     * @param id the id of the element, if <b>null</b> {@code actual} must be <b>null</b>
     * @param classSystem the classification system name
     * @param classSystemVer the optional classification system version (may be <b>null</b> for not present)
     * @param classId the classification identifier
     * @param actual the actual classification item
     * @throws ExecutionException shall not occur
     */
    private static void assertEquals(String id, String classSystem, String classSystemVer, String classId, 
        ProductClassificationItem actual) throws ExecutionException {
        if (null == id) {
            Assert.assertNull(actual);
        } else {
            Assert.assertNotNull(actual);
            Assert.assertEquals(id, actual.getIdShort());
            Assert.assertEquals(classSystem, actual.getProductClassificationSystem());
            if (null == classSystemVer) {
                Assert.assertNull(actual.getClassificationSystemVersion());
            } else {
                Assert.assertEquals(classSystemVer, actual.getClassificationSystemVersion());
            }
            Assert.assertEquals(classId, actual.getProductClassId());
        }
    }

    /**
     * Asserts the created properties of the technical properties submodel.
     * 
     * @param tsub the technical properties submodel
     */
    private void assertTechnicalProperties(TechnicalDataSubmodel tsub) {
        TechnicalProperties tp = tsub.getTechnicalProperties();
        Assert.assertNotNull(tp);
        
        List<SubmodelElementCollection> c = CollectionUtils.toList(tp.mainSections().iterator());
        Assert.assertNotNull(c);
        Assert.assertTrue(c.size() == 2);
        Set<String> ids = c.stream().map(x -> x.getIdShort()).collect(Collectors.toSet());
        Assert.assertTrue(ids.contains("main1"));
        Assert.assertTrue(ids.contains("main2"));
        
        c = CollectionUtils.toList(tp.subSections().iterator());
        Assert.assertNotNull(c);
        Assert.assertTrue(c.size() == 2);
        ids = c.stream().map(x -> x.getIdShort()).collect(Collectors.toSet());
        Assert.assertTrue(ids.contains("sub1"));
        Assert.assertTrue(ids.contains("sub2"));

        try {
            List<SubmodelElement> s = CollectionUtils.toList(tp.arbitrary().iterator());
            Assert.assertNotNull(s);
            Assert.assertTrue(s.isEmpty());
        } catch (NullPointerException e) {
            // problem in BaSyx, we ignore this for now
        }
        
        try {
            List<SubmodelElement> s = CollectionUtils.toList(tp.sMENotDescribedBySemanticId().iterator());
            Assert.assertNotNull(s);
            Assert.assertTrue(s.isEmpty());        
        } catch (NullPointerException e) {
            // problem in BaSyx, we ignore this for now
        }
        
        // modifications
        // currently none
    }
    
}
