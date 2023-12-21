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

package test.de.iip_ecosphere.platform.support.aas;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.AasUtils;
import de.iip_ecosphere.platform.support.aas.AssetKind;
import de.iip_ecosphere.platform.support.aas.Entity;
import de.iip_ecosphere.platform.support.aas.Entity.EntityType;
import de.iip_ecosphere.platform.support.aas.LangString;
import de.iip_ecosphere.platform.support.aas.PersistenceRecipe.FileResource;
import de.iip_ecosphere.platform.support.aas.Reference;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.aas.types.documentation.HandoverDocumentationBuilder;
import de.iip_ecosphere.platform.support.aas.types.documentation.HandoverDocumentationBuilder.DocumentBuilder;
import de.iip_ecosphere.platform.support.aas.types.documentation.HandoverDocumentationBuilder.DocumentStatus;
import de.iip_ecosphere.platform.support.aas.types.hierarchicalStructure.HierarchicalStructureBuilder;
import de.iip_ecosphere.platform.support.aas.types.hierarchicalStructure.HierarchicalStructureBuilder.ArcheType;
import de.iip_ecosphere.platform.support.aas.types.hierarchicalStructure.HierarchicalStructureBuilder.EntryNodeBuilder;
import de.iip_ecosphere.platform.support.aas.types.technicaldata.FurtherInformation.FurtherInformationBuilder;
import de.iip_ecosphere.platform.support.aas.types.technicaldata.GeneralInformation.GeneralInformationBuilder;
import de.iip_ecosphere.platform.support.aas.types.technicaldata.ProductClassificationItem.ProductClassificationItemBuilder;
import de.iip_ecosphere.platform.support.aas.types.technicaldata.ProductClassifications.ProductClassificationsBuilder;
import de.iip_ecosphere.platform.support.aas.types.technicaldata.TechnicalDataSubmodel.TechnicalDataSubmodelBuilder;
import de.iip_ecosphere.platform.support.resources.ResourceLoader;
import de.iip_ecosphere.platform.support.aas.Type;

import static de.iip_ecosphere.platform.support.aas.IdentifierType.*;

/**
 * A Christmas-AAS for testing and laughting.
 * 
 * @author Holger Eichelberger, SSE
 * @author Claudia Niederée, L3S
 */
public class XmasAas {
    
    // TODO images
    
    private static final String MALE = "male";
    private static final String FEMALE = "female";
    private static final String RAL_RED = "3000";
    private static final String NOSE_RED = RAL_RED;
    private static final String NOSE_BROWN = "8014"; // sepia brown

    private List<Aas> aasList = new ArrayList<Aas>();
    private Map<String, Aas> parts = new TreeMap<>();
    private List<FileResource> resources = new ArrayList<FileResource>();
    private boolean createOperations = true;
    private boolean createMultiLanguageProperties = true;
    private File tmpFolder = new File(FileUtils.getTempDirectory(), "xmas");
    
    /**
     * Returns the target file.
     * 
     * @return the target file
     */
    protected File getTargetFile() {
        return new File("./output/santaAas.aasx");
    }
    
    /**
     * Tests creating and storing the Xmas AAS.
     * 
     * @throws IOException if persisting does not work.
     */
    @Test
    public void testCreateAndStore() throws IOException {
        FileUtils.deleteQuietly(tmpFolder);
        createCompositeAas();
        File aasx = getTargetFile();
        AasFactory.getInstance().createPersistenceRecipe().writeTo(aasList, 
            getFileResource("santaSleigh.png"), resources, aasx);
        FileUtils.deleteQuietly(tmpFolder);
    }
    
    /**
     * Enables/disables creating operations.
     * 
     * @param createOperations shall we create operations
     */
    protected void setCreateOperations(boolean createOperations) {
        this.createOperations = createOperations;
    }

    /**
     * Enables/disables creating multi-language properties.
     * 
     * @param createMultiLanguageProperties shall we create multi-language properties
     */
    protected void setCreateMultiLanguageProperties(boolean createMultiLanguageProperties) {
        this.createMultiLanguageProperties = createMultiLanguageProperties;
    }
    /**
     * Returns a resource as a file. As we store resources on the class path and test execution happens in the
     * specific AAS implementations, we need store a copy in the temporary folder.
     * 
     * @param name the name of the resource
     * @return the file or <b>null</b> if the ressource cannot be found/stored temporarily
     */
    private static File getFileResource(String name) {
        File result = null;
        InputStream in = ResourceLoader.getResourceAsStream("xmas/" + name);
        if (null != in) {
            File parent = new File(FileUtils.getTempDirectory(), "xmas");
            parent.mkdirs();
            File tmp = new File(parent, name);
            if (tmp.exists()) { // we assume it's the right one then
                result = tmp;
            } else {
                try {
                    FileUtils.copyInputStreamToFile(in, tmp);
                    result = tmp;
                    result.deleteOnExit();
                } catch (IOException e) {
                    System.err.println("Cannot write resource to temporary folder. Ignoring resource " + name);
                }
            }
        } else {
            System.err.println("Cannot find resource on classpath. Ignoring resource " + name);
        }
        return result;
    }
    
    /**
     * Creates the all-over composite Santa's sleight AAS.
     * 
     * @return the created AAS
     */
    private Aas createCompositeAas() {
        createSantaAas();
        createSleigh();
        int onSuffix = 0;
        createReindeerAas("Rudolph", NOSE_RED, MALE, onSuffix++, "rudolph.png");
        createReindeerAas("Cupid", NOSE_BROWN, FEMALE, onSuffix++, "cupid.png");
        createReindeerAas("Dasher", NOSE_BROWN, MALE, onSuffix++, "dasher.png"); // gender?
        createReindeerAas("Dancer", NOSE_BROWN, FEMALE, onSuffix++, "dancer.png");
        createReindeerAas("Prancer", NOSE_BROWN, "non-binary", onSuffix++, "prancer.png");
        createReindeerAas("Vixen", NOSE_BROWN, FEMALE, onSuffix++, "vixen.png");
        createReindeerAas("Comet", NOSE_BROWN, FEMALE, onSuffix++, "comet.png");
        createReindeerAas("Donner", NOSE_BROWN, FEMALE, onSuffix++, "donner.png");
        createReindeerAas("Blitzen", NOSE_BROWN, MALE, onSuffix++, "blitzen.png");
        
        AasBuilder aasBuilder = AasFactory.getInstance().createAasBuilder("Santa_s_Sleigh", 
            "urn:::AAS:::SantasSleigh#");
        aasBuilder.createAssetBuilder("santasSleigh", "urn:::Asset:::SantasSleigh#", AssetKind.INSTANCE).build();
        HierarchicalStructureBuilder hsb = new HierarchicalStructureBuilder(aasBuilder, "BOM", 
            "urn:::SM:::SantasSleighBOM#", ArcheType.ONE_DOWN);
        EntryNodeBuilder enb = hsb.createEntryNodeBuilder("SantaSleigh_EntryNode", EntityType.SELFMANAGEDENTITY, null);
        Reference enbRef = enb.createReference();
        parts.forEach((id, part) -> {
            Entity ent = enb.createNodeBuilder(id, EntityType.SELFMANAGEDENTITY, part.createReference()).build();
            enb.addHasPartOf(enbRef, ent.createReference());
        });
        enb.build();
        hsb.build();
        
        createTechnicalDataSubmodel(aasBuilder, "Christmas", 
            LangString.create("Instantiated every year on Christmas"), "MSS-001", "MSS-2023", 
            "santaSleigh.png", "0173-1#01-AIZ481#021");
        
        return registerAas(aasBuilder.build());
    }

    /**
     * Registers a created AAS (for persisting it).
     * 
     * @param aas the AAS
     * @return {@code aas}
     */
    private Aas registerAas(Aas aas) {
        aasList.add(aas);
        parts.put("node_" + aas.getIdShort(), aas);
        return aas;
    }
    
    /**
     * Registers a resource (once).
     * 
     * @param resource the resource to be registered
     * @return {@code resource}
     */
    private FileResource registerResource(FileResource resource) {
        if (!resources.stream().anyMatch(r -> resource.getPath().equals(r.getPath()))) {
            resources.add(resource);
        }
        return resource;
    }

    /**
     * Creates the sleigh AAS.
     * 
     * @return the created AAS
     * @see #createSleighDocumentation(AasBuilder)
     * @see #createTechnicalDataSubmodel(AasBuilder, String, LangString, String, String, String, String)
     * @see #registerAas(Aas)
     */
    private Aas createSleigh() {
        AasBuilder aasBuilder = AasFactory.getInstance().createAasBuilder("Sleigh", "urn:::AAS:::Sleigh#");
        aasBuilder.createAssetBuilder("sleigh", "urn:::Asset:::Sleigh#", AssetKind.INSTANCE).build();

        SubmodelBuilder smBuilder = aasBuilder.createSubmodelBuilder("ProductData", 
            "urn:::SM:::productDataSleigh#");
        smBuilder.createPropertyBuilder("material")
            .setValue(Type.STRING, "wood")
            .build();
        smBuilder.createPropertyBuilder("color")
            .setSemanticId(irdi("0173-1#02-AAH022#001"))
            .setDescription(new LangString("en", "color (RAL)"))
            .setValue(Type.STRING, "3000")
            .build();
        smBuilder.createPropertyBuilder("length")
            .setSemanticId(irdi("0173-1#02-AAG001#004"))
            .setValue(Type.INTEGER, 723)
            .build();
        smBuilder.createPropertyBuilder("width")
            .setSemanticId(irdi("0173-1#02-AAB173#006"))
            .setValue(Type.INTEGER, 339)
            .build();
        if (createOperations) {
            smBuilder.createOperationBuilder("start")
                .build();
            smBuilder.createOperationBuilder("stop")
                .build();
            smBuilder.createOperationBuilder("accelerate")
                .addInputVariable("targetSpeed", Type.DOUBLE, b -> {
                    b.setSemanticId(irdi("0173-1#05-AAA551#003"));
                    b.setDescription(new LangString("en", 
                        "Sets the target speed, implicitly accelerates and decelerates."));
                }).setDescription(new LangString("en", "Accelerates or decelerates the sleigh."))
                .build();
        }
        smBuilder.build();

        createTechnicalDataSubmodel(aasBuilder, "North Pole Wood Company Ltd.", 
            LangString.create("Traditional reindeer-pulled sleigh"), "Sleigh-007/03", "007", "sleigh.png", 
            "0173-1#01-AHE203#002");
        
        createSleighDocumentation(aasBuilder);

        return registerAas(aasBuilder.build());
    }
    
    /**
     * Creates the documentation submodel of the sleigh.
     * 
     * @param aasBuilder the sleigh AAS builder
     */
    private void createSleighDocumentation(AasBuilder aasBuilder) {
        FileResource resource = null;
        String documentFile = "sleighManual.pdf";
        try {
            resource = new FileResource(getFileResource(documentFile), 
                "/aasx/DocumentationSubmodel/" + documentFile);
            registerResource(resource);
        } catch (IOException e) {
            System.err.println("Cannot create file resource, ignoring: " + e.getMessage());
        }
        
        HandoverDocumentationBuilder hdb = new HandoverDocumentationBuilder(aasBuilder, 
            createMultiLanguageProperties, "urn:::SM:::productDataSleighDocs#");
        DocumentBuilder db = hdb.createDocumentBuilder();
        db.createDocumentIdBuilder()
            .setIsPrimary(true)
            .setDocumentDomainId("sleigh-001")
            .setValueId("1234")
            .build();
        db.createDocumentVersionBuilder()
            .setLanguage("en")
            .setDocumentVersionId("2023.12")
            .setTitle(new LangString("en", "Sleigh Operation Manual"))
            .setSummary(new LangString("en", "<missing>"))
            .setKeywords(new LangString("en", "sleigh, reindeers, santa, customized"))
            .setStatus("2023-12-17T00:00:00.000+00:00", DocumentStatus.RELEASED)
            .setOrganizationName("North Pole Wood", "North Pole Wood Company Ltd")
            .addDigitalFile(resource, toMimeType(documentFile))
            .build();
        db.createDocumentClassificationBuilder()
            .setDocumentClass("DC", "IEC61355-1:2008", new LangString("de", "Anleitungen und Handbücher"))
            .build();
        db.build();
        hdb.build();
    }
    
    /**
     * Returns the MIME type of the given resource name.
     * 
     * @param resourceName the resource name
     * @return the MIME type
     */
    private static String toMimeType(String resourceName) {
        String extension = "";
        int pos = resourceName.lastIndexOf('.');
        if (pos > 0 && pos < resourceName.length() - 1) {
            extension = resourceName.substring(pos + 1).toLowerCase();
        }
        String mime;
        switch (extension) {
        case "jpeg":
        case "jpg":
            mime = "image/jpeg";
            break;
        case "png":
            mime = "image/png";
            break;
        case "pdf":
            mime = "application/pdf";
            break;
        default:
            mime = "";
            break;
        }
        return mime;
    }
    
    // checkstyle: stop parameter number check
    
    /**
     * Creates a simple technical data submodel.
     * 
     * @param aasBuilder the parent AAS builder
     * @param manufacturerName the name of the manufacturer
     * @param productDesignation the product designation
     * @param partNumber the part number
     * @param orderCode the order code
     * @param productResourceName the resource name of the product image
     * @param productClassId the ECLASS product class id
     */
    private void createTechnicalDataSubmodel(AasBuilder aasBuilder, String manufacturerName, 
        LangString productDesignation, String partNumber, String orderCode, String productResourceName, 
        String productClassId) {
        TechnicalDataSubmodelBuilder tdBuilder = aasBuilder.createTechnicalDataSubmodelBuilder(
             iri("TechData_" + productDesignation + "_" + partNumber + "_" + orderCode));
        GeneralInformationBuilder giBuilder = tdBuilder.createGeneralInformationBuilder(manufacturerName, 
            productDesignation, partNumber, orderCode);
        if (productResourceName != null && productResourceName.length() > 0) {
            try {
                registerResource(new FileResource(getFileResource(productResourceName), 
                    "/aasx/TechnicalDataSubmodel/" + productResourceName));
                giBuilder.addProductImageFile("", "/aasx/TechnicalDataSubmodel/" + productResourceName, 
                    toMimeType(productResourceName)); // no idShort needed, -> postfix off ProductImage
            } catch (IOException e) {
                System.err.println("Cannot create file resource, ignoring: " + e.getMessage());
            }
        }
        giBuilder.build();
        FurtherInformationBuilder fiBuilder = tdBuilder.createFurtherInformationBuilder(null);
        fiBuilder.build();
        tdBuilder.createTechnicalPropertiesBuilder().build();
        ProductClassificationsBuilder pcBuilder = tdBuilder.createProductClassificationsBuilder();
        ProductClassificationItemBuilder pcIBuilder = pcBuilder
            .createProductClassificationItemBuilder("ProductClassification", "ECLASS", productClassId)
            .setClassificationSystemVersion("13");
        pcIBuilder.build();
        pcBuilder.build();
        tdBuilder.build();
    }

    // checkstyle: resume parameter number check

    /**
     * Creates the Santa AAS.
     * 
     * @return the created AAS
     * @see #registerAas(Aas)
     * @see #createTechnicalDataSubmodel(AasBuilder, String, LangString, String, String, String, String)
     */
    private Aas createSantaAas() {
        AasBuilder aasBuilder = AasFactory.getInstance().createAasBuilder("santa", "urn:::AAS:::aasSanta#");
        aasBuilder.createAssetBuilder("santa", "urn:::Asset:::aasSanta#", AssetKind.INSTANCE).build();
        SubmodelBuilder smBuilder = aasBuilder.createSubmodelBuilder("LivingData", 
            "urn:::SM:::livingDataSanta#");
        smBuilder.createPropertyBuilder("displayName")
            .setValue(Type.STRING, "Santa Claus")
            .build();
        smBuilder.createPropertyBuilder("name")
            .setValue(Type.STRING, "Nikolaus von Myra")
            .build();
        smBuilder.createPropertyBuilder("age")
            .setValue(Type.INT16, 1753)
            .build();
        smBuilder.createPropertyBuilder("clothsColor")
            .setValue(Type.STRING, "red")
            .build();
        smBuilder.build();
        
        createTechnicalDataSubmodel(aasBuilder, "The Lord", 
            LangString.create("The one and only Santa Claus"), "SC-001", "SC-001", 
            "santa.png", "0173-1#01-ADT348#005");

        return registerAas(aasBuilder.build());
    }
    
    /**
     * Creates a reindeer AAS.
     * 
     * @param name the name of the reindeer
     * @param noseColor the color of the nose in RAL
     * @param sex the sex of the reindeer
     * @param orderNumberSuffix numerical suffix to order number
     * @param productResourceName the resource name of the product image
     * @return the created AAS
     * @see #registerAas(Aas)
     * @see #createTechnicalDataSubmodel(AasBuilder, String, LangString, String, String, String, String)
     */
    private Aas createReindeerAas(String name, String noseColor, String sex, int orderNumberSuffix, 
        String productResourceName) {
        AasBuilder aasBuilder = AasFactory.getInstance().createAasBuilder(AasUtils.fixId(name), 
            "urn:::AAS:::reindeer" + name + "#");
        aasBuilder.createAssetBuilder(name, "urn:::Asset:::reindeer" + name + "#", AssetKind.INSTANCE).build();

        SubmodelBuilder smBuilder = aasBuilder.createSubmodelBuilder("LivingData", 
            "urn:::SM:::livingData" + name + "#");
        smBuilder.createPropertyBuilder("noseColor")
            .setSemanticId(irdi("0173-1#02-AAH022#001"))
            .setDescription(new LangString("en", "color (RAL)"))
            .setValue(Type.STRING, "3000")
            .build();
        smBuilder.createPropertyBuilder("gender")
            .setValue(Type.STRING, sex)
            .build();
        smBuilder.createPropertyBuilder("name")
            .setValue(Type.STRING, name)
            .build();
        smBuilder.build();
                
        createTechnicalDataSubmodel(aasBuilder, "Northpole Magic Reindeer Family", 
            LangString.create("Traditional (magical) reindeer"), "Reindeer-1923#13", "MR-" + orderNumberSuffix, 
            productResourceName, "0173-1#01-AGN230#002");

        return registerAas(aasBuilder.build());
    }

}
