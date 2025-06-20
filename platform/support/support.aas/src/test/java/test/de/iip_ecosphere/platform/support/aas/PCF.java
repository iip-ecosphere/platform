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
import java.util.Date;

import static de.iip_ecosphere.platform.support.aas.IdentifierType.*;

import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.AssetKind;
import de.iip_ecosphere.platform.support.aas.Entity;
import de.iip_ecosphere.platform.support.aas.Entity.EntityBuilder;
import de.iip_ecosphere.platform.support.aas.Entity.EntityType;
import de.iip_ecosphere.platform.support.aas.IdentifierType;
import de.iip_ecosphere.platform.support.aas.LangString;
import de.iip_ecosphere.platform.support.aas.Type;
import de.iip_ecosphere.platform.support.aas.types.carbonFootprint.CarbonFootprintBuilder;
import de.iip_ecosphere.platform.support.aas.types.carbonFootprint.CarbonFootprintBuilder.PCFCalculationMethod;
import de.iip_ecosphere.platform.support.aas.types.carbonFootprint.CarbonFootprintBuilder
    .PCFGoodsAddressHandoverBuilder;
import de.iip_ecosphere.platform.support.aas.types.carbonFootprint.CarbonFootprintBuilder.PCFLifeCyclePhase;
import de.iip_ecosphere.platform.support.aas.types.carbonFootprint.CarbonFootprintBuilder
    .PCFReferenceValueForCalculation;
import de.iip_ecosphere.platform.support.aas.types.carbonFootprint.CarbonFootprintBuilder.ProductCarbonFootprintBuilder;
import de.iip_ecosphere.platform.support.aas.types.common.Utils;
import de.iip_ecosphere.platform.support.aas.types.hierarchicalStructure.HierarchicalStructuresBuilder;
import de.iip_ecosphere.platform.support.aas.types.hierarchicalStructure.HierarchicalStructuresBuilder.ArcheType;
import de.iip_ecosphere.platform.support.aas.types.hierarchicalStructure.HierarchicalStructuresBuilder.EntryNodeBuilder;
import de.iip_ecosphere.platform.support.aas.types.technicaldata.TechnicalDataBuilder;
import de.iip_ecosphere.platform.support.aas.types.technicaldata.TechnicalDataBuilder.ProductClassificationsBuilder;
import de.iip_ecosphere.platform.support.aas.types.technicaldata.TechnicalDataBuilder.TechnicalPropertiesBuilder;
import de.iip_ecosphere.platform.support.aas.types.timeSeriesData.TimeSeriesBuilder;
import de.iip_ecosphere.platform.support.aas.types.timeSeriesData.TimeSeriesBuilder.MetadataBuilder;
import de.iip_ecosphere.platform.support.aas.types.timeSeriesData.TimeSeriesBuilder.SegmentsBuilder;
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
        aasBuilder.createAssetInformationBuilder("lni1319", iri("https://aas2.uni-h.de/lni1319"), AssetKind.INSTANCE)
            .build();

        createPcfSubmodel(aasBuilder);
        createTechnicalDataSubmodel(aasBuilder);
        createProductionProcess(aasBuilder);
        
        registerAas(aasBuilder);
    }

    /**
     * Sets location latitude/longitude as additional properties.
     * 
     * @param builder the builder to set the values on
     * @param latitude the latitude
     * @param longitude the longitude
     * @return {@code builder}
     */
    private PCFGoodsAddressHandoverBuilder setLocation(PCFGoodsAddressHandoverBuilder builder, double latitude, 
        double longitude) {
        builder.createPropertyBuilder("Latitude")
            .setValue(Type.DOUBLE, latitude)
            .setSemanticId(IdentifierType.irdi("0173-1#02-ABH960#001"))
            .build();
        builder.createPropertyBuilder("Longitude")
            .setValue(Type.DOUBLE, longitude)
            .setSemanticId(IdentifierType.irdi("0173-1#02-ABH961#001"))
            .build();
        return builder;
    }
    
    /**
     * Creates the PCF submodel.
     * 
     * @param aasBuilder the AAS builder
     */
    private void createPcfSubmodel(AasBuilder aasBuilder) {
        CarbonFootprintBuilder cfBuilder = new CarbonFootprintBuilder(aasBuilder, "urn:::SM:::PCF#", "CarbonFootprint");
        Date date = Utils.parseDate("2023-12-17T00:00:00.000Z");
        ProductCarbonFootprintBuilder pcfBuilder = cfBuilder.createProductCarbonFootprintBuilder()
            .setPCFCalculationMethod(PCFCalculationMethod.GHG_PROTOCOL)
            .setPCFCO2eq(0.235)
            .setPCFReferenceValueForCalculation(PCFReferenceValueForCalculation.PIECE)
            .setPCFQuantityOfMeasureForCalculation(1)
            .setPCFLifeCyclePhase(PCFLifeCyclePhase.A1)
            .setPublicationDate(date);
        PCFGoodsAddressHandoverBuilder ho = pcfBuilder.createPCFGoodsAddressHandoverBuilder()
            .setStreet("Koblenzer Str")
            .setHouseNumber("122")
            .setZipCode("41468")
            .setCityTown("Neuss")
            .setCountry("Germany");
        setLocation(ho, 51.151277079867036, 6.777799507862787)
            .build();
        pcfBuilder.build();

        pcfBuilder = cfBuilder.createProductCarbonFootprintBuilder()
            .setPCFCalculationMethod(PCFCalculationMethod.GHG_PROTOCOL)
            .setPCFCO2eq(0.553)
            .setPCFReferenceValueForCalculation(PCFReferenceValueForCalculation.PIECE)
            .setPCFQuantityOfMeasureForCalculation(1)
            .setPCFLifeCyclePhase(PCFLifeCyclePhase.A2)
            .setPublicationDate(date);
        ho = pcfBuilder.createPCFGoodsAddressHandoverBuilder()
            .setStreet("Osteriede")
            .setHouseNumber("6")
            .setZipCode("30827")
            .setCityTown("Garbsen")
            .setCountry("Germany");
        setLocation(ho, 52.42771449186328, 9.613097399392698)
            .build();
        pcfBuilder.build();

        pcfBuilder = cfBuilder.createProductCarbonFootprintBuilder()
            .setPCFCalculationMethod(PCFCalculationMethod.GHG_PROTOCOL)
            .setPCFCO2eq(0.823)
            .setPCFReferenceValueForCalculation(PCFReferenceValueForCalculation.PIECE)
            .setPCFQuantityOfMeasureForCalculation(1)
            .setPCFLifeCyclePhase(PCFLifeCyclePhase.A3)
            .setPublicationDate(date);
        ho = pcfBuilder.createPCFGoodsAddressHandoverBuilder()
            .setStreet("Messegelaende")
            .setHouseNumber("Halle 8")
            .setZipCode("30521")
            .setCityTown("Hannover")
            .setCountry("Germany");
        setLocation(ho, 52.322049093658464, 9.81150344024394)
            .build();
        pcfBuilder.build();

        pcfBuilder = cfBuilder.createProductCarbonFootprintBuilder()
            .setPCFCalculationMethod(PCFCalculationMethod.GHG_PROTOCOL)
            .setPCFCO2eq(0.123)
            .setPCFReferenceValueForCalculation(PCFReferenceValueForCalculation.PIECE)
            .setPCFQuantityOfMeasureForCalculation(1)
            .setPCFLifeCyclePhase(PCFLifeCyclePhase.A4)
            .setPublicationDate(date);
        ho = pcfBuilder.createPCFGoodsAddressHandoverBuilder()
            .setStreet("Austrasse")
            .setHouseNumber("35")
            .setZipCode("86153")
            .setCityTown("Augsburg")
            .setCountry("Germany");
        setLocation(ho, 48.389832, 10.887690)
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
        TechnicalDataBuilder tdBuilder = new TechnicalDataBuilder(aasBuilder, "urn:::SM:::TD#");
        tdBuilder.setCreateMultiLanguageProperties(isCreateMultiLanguageProperties());
        
        tdBuilder.createFurtherInformationBuilder().
            setValidDate(Utils.parseDate("2024-01-04T10:00:00.000+00:00"))
            .build();

        tdBuilder.createGeneralInformationBuilder()
            .setManufacturerName("Mittelstand-Digital Zentrum Hannover")
            .setManufacturerArticleNumber("K01")
            .setManufacturerOrderCode("K01")
            .setManufacturerProductDesignation(new LangString("de", "Kugelschreiber"))
            .build();

        ProductClassificationsBuilder pcBuilder = tdBuilder.createProductClassificationsBuilder();
        pcBuilder
            .createProductClassificationItemBuilder()
            .setProductClassificationSystem("ECLASS")
            .setClassificationSystemVersion("12.0 (BASIC)")
            .setProductClassId("24-24-05-01")
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
        Entity ent = setName(enb.createNodeBuilder(), "id")
            .setEntityType(EntityType.SELFMANAGEDENTITY)
            .setAsset(part.createReference())
            .build();
        enb.setHasPart(enb.createReference(), ent.createReference());
    }
    
    /**
     * Creates the production process (sub).
     * 
     * @param aasBuilder the AAS builder
     */
    private void createProductionProcess(AasBuilder aasBuilder) {
        HierarchicalStructuresBuilder hsBuilder = new HierarchicalStructuresBuilder(aasBuilder, 
            "urn:::SM:::PP#", "ProductionProcess");
        hsBuilder.setArcheType(ArcheType.ONEDOWN);
        EntryNodeBuilder enb = setName(hsBuilder.createEntryNodeBuilder(), "ProductionProcess");

        AasFactory f = AasFactory.getInstance();
        AasBuilder b = f.createAasBuilder("mdzh_configurator", 
            iri("https://aas2.uni-h.de/aas/mdzh_configurator"));
        b.createAssetInformationBuilder("Vision_mudLaser", iri("https://aas2.uni-h.de/mdzh_configurator"), 
            AssetKind.INSTANCE).build();
        createNodeHasPartOf(enb, "Station 1 - Configuration", b);
        registerAas(b);
        
        b = f.createAasBuilder("mdzh_pickbylight", 
            iri("https://aas2.uni-h.de/aas/mdzh_pickbylight"));
        b.createAssetInformationBuilder("mdzh_pickbylight", iri("https://aas2.uni-h.de/mdzh_pickbylight"), 
            AssetKind.INSTANCE).build();
        createNodeHasPartOf(enb, "Station 2 - Commissioning", b);
        registerAas(b);

        b = f.createAasBuilder("aas_DMG_NEF400", 
            iri("https://aas2.uni-h.de/aas/DMG_NEF400"));
        b.createAssetInformationBuilder("DMG_NEF400", iri("https://aas2.uni-h.de/DMG_NEF400"), AssetKind.INSTANCE)
            .build();
        // intentionally no further submodels
        createNodeHasPartOf(enb, "Station 3 - Turning", b);
        TimeSeriesBuilder tsd = new TimeSeriesBuilder(b, iri("https://aas2.uni-h.de/aas/DMG_NEF400/tsd"));
        tsd.setCreateMultiLanguageProperties(isCreateMultiLanguageProperties());
        MetadataBuilder mdb = tsd.createMetadataBuilder()
            .setName(new LangString("en", "Turning Power Time Series"))
            .setDescription(new LangString("en", "Contains time series of the machine"));
        mdb.createRecordBuilder().setTime("2024-01-01T12:00:00.000+00:00", null).build();
        mdb.build();
        SegmentsBuilder sb = tsd.createSegmentsBuilder();
        sb.createLinkedSegmentBuilder()
            .setName(new LangString("en", "PowerUsage"))
            .setDescription(new LangString("en", "Power usage of the machine in Watts, averaged per minute"))
            .setEndpoint("https://mnestix-dev.azurewebsites.net/influx/api/v2/query?org=LNIAAS")
            .setQuery("import \"experimental/aggregate\" from(bucket: \"MDZHannover\") |> range(start: -12h) "
                    + "|> filter(fn: (r) => r[\"_measurement\"] == \"eventhub_consumer\") "
                    + "|> filter(fn: (r) => r[\"_field\"] == \"Messages_0_Payload_Energy_Body\") "
                    + "|> filter(fn: (r) => r[\"host\"] == \"145370d6156b\") |> aggregate.rate(every: 10m, unit: 1m) "
                    + "|> yield()")
            .build();
        sb.createLinkedSegmentBuilder()
            .setName(new LangString("en", "Total Energy Consumption"))
            .setDescription(new LangString("en", "Energy consumption of the machine in W/h"))
            .setEndpoint("https://mnestix-dev.azurewebsites.net/influx/api/v2/query?org=LNIAAS")
            .setQuery("from(bucket: \"MDZHannover\") |> range(start: -12h) "
                    + "|> filter(fn: (r) => r[\"_measurement\"] == \"eventhub_consumer\") "
                    + "|> filter(fn: (r) => r[\"_field\"] == \"Messages_0_Payload_Energy_Body\") "
                    + "|> filter(fn: (r) => r[\"host\"] == \"145370d6156b\") "
                    + "|> aggregateWindow(every: 10m, fn: mean, createEmpty: false) |> yield()")
            .build();
        sb.build();
        tsd.build();
        registerAas(b);
        
        b = f.createAasBuilder("mdzh_drill", 
            iri("https://aas2.uni-h.de/aas/mdzh_drill"));
        b.createAssetInformationBuilder("mdzh_drill", iri("https://aas2.uni-h.de/mdzh_drill"), AssetKind.INSTANCE)
            .build();
        createNodeHasPartOf(enb, "Station 4 - Deburring", b);
        registerAas(b);

        b = f.createAasBuilder("aas_Vision_mudLaser", 
            iri("https://aas2.uni-h.de/aas/Vision_mudLaser"));
        b.createAssetInformationBuilder("Vision_mudLaser", iri("https://aas2.uni-h.de/Vision_mudLaser"), 
            AssetKind.INSTANCE).build();
        // intentionally no further submodels
        createNodeHasPartOf(enb, "Station 5 - Laser Marking", b);
        setName(enb.createNodeBuilder(), "Station 5 - Assembly")
            .build();
        setName(enb.createNodeBuilder(), "Station 5 - Quality Check")
            .build();
        
        enb.build();
        hsBuilder.build();
    }
    
    /**
     * Sets the name of an entity as property, previously it was just the idShort.
     * 
     * @param <B> the builder type
     * @param builder the builder
     * @param name the name value
     * @return {@code builder}
     */
    private <B extends EntityBuilder> B setName(B builder, String name) {
        builder.createPropertyBuilder("name")
            .setValue(Type.STRING, name)
            .build();
        return builder;
    }
    
    @Override
    protected File getThumbnail() {
        return null;
    }
    
}
