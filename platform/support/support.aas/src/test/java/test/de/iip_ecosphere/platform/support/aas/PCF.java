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

package test.de.iip_ecosphere.platform.support.aas;

import java.io.File;

import static de.iip_ecosphere.platform.support.aas.IdentifierType.*;

import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.AssetKind;
import de.iip_ecosphere.platform.support.aas.Entity;
import de.iip_ecosphere.platform.support.aas.Entity.EntityType;
import de.iip_ecosphere.platform.support.aas.LangString;
import de.iip_ecosphere.platform.support.aas.Type;
import de.iip_ecosphere.platform.support.aas.types.carbonFootprint.CarbonFootprintBuilder;
import de.iip_ecosphere.platform.support.aas.types.carbonFootprint.CarbonFootprintBuilder.PcfCalculationMethod;
import de.iip_ecosphere.platform.support.aas.types.carbonFootprint.CarbonFootprintBuilder.PcfLiveCyclePhase;
import de.iip_ecosphere.platform.support.aas.types.carbonFootprint.CarbonFootprintBuilder.ProductCarbonFootprintBuilder;
import de.iip_ecosphere.platform.support.aas.types.carbonFootprint.CarbonFootprintBuilder.QuantityUnit;
import de.iip_ecosphere.platform.support.aas.types.common.Utils;
import de.iip_ecosphere.platform.support.aas.types.hierarchicalStructure.HierarchicalStructureBuilder;
import de.iip_ecosphere.platform.support.aas.types.hierarchicalStructure.HierarchicalStructureBuilder.ArcheType;
import de.iip_ecosphere.platform.support.aas.types.hierarchicalStructure.HierarchicalStructureBuilder.EntryNodeBuilder;
import de.iip_ecosphere.platform.support.aas.types.technicaldata.ProductClassifications.ProductClassificationsBuilder;
import de.iip_ecosphere.platform.support.aas.types.technicaldata.TechnicalDataSubmodel.TechnicalDataSubmodelBuilder;
import de.iip_ecosphere.platform.support.aas.types.technicaldata.TechnicalProperties.TechnicalPropertiesBuilder;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;

/**
 * Example for PCF. Inspired by <a href="https://aas2.uni-h.de/aas/lni1319">LNI</a>.
 * 
 * @author Holger Eichelberger, SSE
 * @author Thomas Lepper, IFW
 */
public class PCF extends AbstractAasExample {

    @Override
    protected String getFolderName() {
        return "pcf";
    }
    
    @Override
    public File[] getTargetFiles() {
        return new File[] {new File("./output/pcf.aasx")};
    }

    @Override
    protected void createAas() {
        AasBuilder aasBuilder = AasFactory.getInstance().createAasBuilder("aas_lni1319", 
            iri("https://aas2.uni-h.de/aas/lni1319"));
        aasBuilder.createAssetBuilder("lni1319", iri("https://aas2.uni-h.de/lni1319"), AssetKind.INSTANCE).build();

        createPcfSubmodel(aasBuilder);
        createTechnicalDataSubmodel(aasBuilder);
        createProductionProcess(aasBuilder);
        
        registerAas(aasBuilder);
    }
    
    /**
     * Creates the PCF submodel.
     * 
     * @param aasBuilder the AAS builder
     */
    private void createPcfSubmodel(AasBuilder aasBuilder) {
        CarbonFootprintBuilder cfBuilder = new CarbonFootprintBuilder(aasBuilder, null, "urn:::SM:::PCF#");

        ProductCarbonFootprintBuilder pcfBuilder = cfBuilder.createProductCarbonFootprintBuilder()
            .setPCFCalculationMethod(PcfCalculationMethod.GHG_PROTOCOL)
            .setPCFCO2eq(0.235)
            .setPCFReferenceValueForCalculation(QuantityUnit.PIECE)
            .setPCFQuantityOfMeasureForCalculation(1)
            .addPCFLiveCyclePhase(PcfLiveCyclePhase.A1);
        pcfBuilder.createPCFGoodsAddressHandoverBuilder()
            .setStreet("Koblenzer Str")
            .setHouseNumber("122")
            .setZipCode("41468")
            .setCityTown("Neuss")
            .setCountry("Germany")
            .setLatitude(51.151277079867036)
            .setLongitude(6.777799507862787)
            .build();
        pcfBuilder.build();

        pcfBuilder = cfBuilder.createProductCarbonFootprintBuilder()
            .setPCFCalculationMethod(PcfCalculationMethod.GHG_PROTOCOL)
            .setPCFCO2eq(0.553)
            .setPCFReferenceValueForCalculation(QuantityUnit.PIECE)
            .setPCFQuantityOfMeasureForCalculation(1)
            .addPCFLiveCyclePhase(PcfLiveCyclePhase.A2);
        pcfBuilder.createPCFGoodsAddressHandoverBuilder()
            .setStreet("Osteriede")
            .setHouseNumber("6")
            .setZipCode("30827")
            .setCityTown("Garbsen")
            .setCountry("Germany")
            .setLatitude(52.42771449186328)
            .setLongitude(9.613097399392698)
            .build();
        pcfBuilder.build();

        pcfBuilder = cfBuilder.createProductCarbonFootprintBuilder()
            .setPCFCalculationMethod(PcfCalculationMethod.GHG_PROTOCOL)
            .setPCFCO2eq(0.823)
            .setPCFReferenceValueForCalculation(QuantityUnit.PIECE)
            .setPCFQuantityOfMeasureForCalculation(1)
            .addPCFLiveCyclePhase(PcfLiveCyclePhase.A3);
        pcfBuilder.createPCFGoodsAddressHandoverBuilder()
            .setStreet("Messegelaende")
            .setHouseNumber("Halle 8")
            .setZipCode("30521")
            .setCityTown("Hannover")
            .setCountry("Germany")
            .setLatitude(52.322049093658464)
            .setLongitude(9.81150344024394)
            .build();
        pcfBuilder.build();

        pcfBuilder = cfBuilder.createProductCarbonFootprintBuilder()
            .setPCFCalculationMethod(PcfCalculationMethod.GHG_PROTOCOL)
            .setPCFCO2eq(0.123)
            .setPCFReferenceValueForCalculation(QuantityUnit.PIECE)
            .setPCFQuantityOfMeasureForCalculation(1)
            .addPCFLiveCyclePhase(PcfLiveCyclePhase.A4);
        pcfBuilder.createPCFGoodsAddressHandoverBuilder()
            .setStreet("Austrasse")
            .setHouseNumber("35")
            .setZipCode("86153")
            .setCityTown("Augsburg")
            .setCountry("Germany")
            .setLatitude(48.389832)
            .setLongitude(10.887690)
            .build();
        pcfBuilder.build();

        cfBuilder.build();
    }

    /**
     * Creates a technical data submodel.
     * 
     * @param aasBuilder the AAS builder
     */
    private void createTechnicalDataSubmodel(AasBuilder aasBuilder) {
        TechnicalDataSubmodelBuilder tdBuilder = aasBuilder.createTechnicalDataSubmodelBuilder("urn:::SM:::TD#");
        
        tdBuilder.createFurtherInformationBuilder(Utils.parse("2024-01-04T10:00:00.000+00:00"))
            .build();

        tdBuilder.createGeneralInformationBuilder("Mittelstand-Digital Zentrum Hannover", 
            new LangString("de", "Kugelschreiber"), "K01", "K01")
            .build();

        ProductClassificationsBuilder pcBuilder = tdBuilder.createProductClassificationsBuilder();
        pcBuilder
            .createProductClassificationItemBuilder("ProductClassification", "ECLASS", "24-24-05-01")
            .setClassificationSystemVersion("12.0 (BASIC)")
            .build();
        pcBuilder.build();

        TechnicalPropertiesBuilder tpBuilder = tdBuilder.createTechnicalPropertiesBuilder();
        // intentionally mostly no semantic IDs
        tpBuilder.createPropertyBuilder("Length").setValue(Type.STRING, "145 mm")
            .build();
        tpBuilder.createPropertyBuilder("Diameter").setValue(Type.STRING, "12.8 mm")
            .build();
        tpBuilder.createPropertyBuilder("Weight").setValue(Type.STRING, "32 g")
            .build();
        tpBuilder.createPropertyBuilder("SerialNumber").setValue(Type.STRING, "MDZHMjAyMzExMjAxMTQyMjY1MTUwMzM")
            .build();
        tpBuilder.createPropertyBuilder("HardwareRevision").setValue(Type.STRING, "MDZH_LNI_v1.1")
            .build();
        tpBuilder.createPropertyBuilder("Material").setValue(Type.STRING, "aluminium")
            .build();
        tpBuilder.createPropertyBuilder("WritingColor").setValue(Type.STRING, "blue")
            .build();
        tpBuilder.createPropertyBuilder("LockColor").setValue(Type.STRING, "blue")
            .build();
        tpBuilder.createPropertyBuilder("TipShape").setValue(Type.STRING, "convex")
            .build();
        tpBuilder.createPropertyBuilder("HandleShapeRadius").setValue(Type.STRING, "0")
            .build();
        tpBuilder.createPropertyBuilder("EngravingText").setValue(Type.STRING, "ABC")
            .build();
        tpBuilder.createPropertyBuilder("EngravingTextBack").setValue(Type.STRING, "MDZH")
            .build();
        tpBuilder.build();

        tdBuilder.build();
    }
    
    /**
     * Creates a hierarchical node with has part of relationship.
     * 
     * @param enb the entry node builder
     * @param id the node id
     * @param part the target of the relationship
     */
    private void createNodeHasPartOf(EntryNodeBuilder enb, String id, AasBuilder part) {
        Entity ent = enb.createNodeBuilder(id, EntityType.SELFMANAGEDENTITY, part.createReference()).build();
        enb.addHasPartOf(enb.createReference(), ent.createReference());
    }
    
    /**
     * Creates the production process (sub).
     * 
     * @param aasBuilder the AAS builder
     */
    private void createProductionProcess(AasBuilder aasBuilder) {
        HierarchicalStructureBuilder hsBuilder = new HierarchicalStructureBuilder(aasBuilder, 
            "ProductionProcess", "urn:::SM:::PP#", ArcheType.ONE_DOWN);
        EntryNodeBuilder enb = hsBuilder.createEntryNodeBuilder("ProductionProcess", 
            EntityType.SELFMANAGEDENTITY, null);

        AasFactory f = AasFactory.getInstance();
        AasBuilder b = f.createAasBuilder("mdzh_configurator", 
            iri("https://aas2.uni-h.de/aas/mdzh_configurator"));
        b.createAssetBuilder("Vision_mudLaser", iri("https://aas2.uni-h.de/mdzh_configurator"), AssetKind.INSTANCE)
            .build();
        createNodeHasPartOf(enb, "Station 1 - Configuration", b);
        registerAas(b);
        
        b = f.createAasBuilder("mdzh_pickbylight", 
            iri("https://aas2.uni-h.de/aas/mdzh_pickbylight"));
        b.createAssetBuilder("mdzh_pickbylight", iri("https://aas2.uni-h.de/mdzh_pickbylight"), AssetKind.INSTANCE)
            .build();
        createNodeHasPartOf(enb, "Station 2 - Commissioning", b);
        registerAas(b);

        b = f.createAasBuilder("aas_DMG_NEF400", 
            iri("https://aas2.uni-h.de/aas/DMG_NEF400"));
        b.createAssetBuilder("DMG_NEF400", iri("https://aas2.uni-h.de/DMG_NEF400"), AssetKind.INSTANCE)
            .build();
        // intentionally no further submodels
        createNodeHasPartOf(enb, "Station 3 - Turning", b);
        registerAas(b);
        
        b = f.createAasBuilder("mdzh_drill", 
            iri("https://aas2.uni-h.de/aas/mdzh_drill"));
        b.createAssetBuilder("mdzh_drill", iri("https://aas2.uni-h.de/mdzh_drill"), AssetKind.INSTANCE)
            .build();
        createNodeHasPartOf(enb, "Station 4 - Deburring", b);
        registerAas(b);

        b = f.createAasBuilder("aas_Vision_mudLaser", 
            iri("https://aas2.uni-h.de/aas/Vision_mudLaser"));
        b.createAssetBuilder("Vision_mudLaser", iri("https://aas2.uni-h.de/Vision_mudLaser"), AssetKind.INSTANCE)
            .build();
        // intentionally no further submodels
        createNodeHasPartOf(enb, "Station 5 - Laser Marking", b);
        enb.createNodeBuilder("Station 5 - Assembly", EntityType.SELFMANAGEDENTITY, null)
            .build();
        enb.createNodeBuilder("Station 5 - Quality Check", EntityType.SELFMANAGEDENTITY, null)
            .build();
        
        enb.build();
        hsBuilder.build();
    }
    
    @Override
    protected File getThumbnail() {
        return null;
    }
    
}
