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

package de.iip_ecosphere.platform.support.aas.types.carbonFootprint;

import static de.iip_ecosphere.platform.support.aas.IdentifierType.irdi;
import static de.iip_ecosphere.platform.support.aas.types.common.Utils.*;

import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection.SubmodelElementCollectionBuilder;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.aas.Type;
import de.iip_ecosphere.platform.support.aas.types.common.DelegatingSubmodelBuilder;
import de.iip_ecosphere.platform.support.aas.types.common.DelegatingSubmodelElementCollectionBuilder;
import de.iip_ecosphere.platform.support.aas.types.common.Utils;

/**
 * A PCF submodel builder for <a href="https://github.com/admin-shell-io/submodel-templates/
 * tree/main/development/Carbon%20Footprint/1/0">IDTA 2023-01-24 Draft Submodel PCF</a>.
 * 
 * @author Holger Eichelberger, SSE
 */
public class CarbonFootprintBuilder extends DelegatingSubmodelBuilder {

    private int pcfCount;
    private int tcfCount;

    /**
     * Creates a carbon footprint builder.
     * 
     * @param aasBuilder the parent AAS
     * @param idShort the idshort of the submodel, if <b>null</b> the default idshort is used
     * @param identifier the submodel identifier
     */
    public CarbonFootprintBuilder(AasBuilder aasBuilder, String idShort, String identifier) {
        super(aasBuilder.createSubmodelBuilder(null == idShort ? "CarbonFootprint" : idShort, identifier));
        setSemanticId(irdi("0173-1#01-AHE712#001"));
    }
    
    /**
     * Creates a transport carbon footprint  builder.
     * 
     * @return the builder
     */
    public ProductCarbonFootprintBuilder createProductCarbonFootprintBuilder() {
        return new ProductCarbonFootprintBuilder(getDelegate(), pcfCount++);
    }

    /**
     * Creates a product carbon footprint builder.
     * 
     * @return the builder
     */
    public TransportCarbonFootprintBuilder createTransportCarbonFootprintBuilder() {
        return new TransportCarbonFootprintBuilder(getDelegate(), tcfCount++);
    }
    
    /**
     * The PCF calculation methods.
     * 
     * @author Holger Eichelberger, SSE
     */
    public enum PcfCalculationMethod {
        
        EN_15804(1, "EN 15804", irdi("0173-1#07-ABU223#001")),
        GHG_PROTOCOL(2, "GHG Protocol", irdi("0173-1#07-ABU221#001")),
        IEC_TS_63058(3, "IEC TS 63058", irdi("0173-1#07-ABU222#001")),
        ISO_14040(4, "ISO 14040", irdi("0173-1#07-ABV505#001")),
        ISO_14044(5, "ISO 14044", irdi("0173-1#07-ABV506#001")),
        ISO_14067(5, "ISO 14067", irdi("0173-1#07-ABU218#001"));
        
        private int valueCode;
        private String value;
        private String valueId;
        
        /**
         * Creates a PCF calculation method constant.
         * 
         * @param valueCode the value code
         * @param value the value
         * @param valueId the value id
         */
        private PcfCalculationMethod(int valueCode, String value, String valueId) {
            this.valueCode = valueCode;
            this.value = value;
            this.valueId = valueId;
        }

        /**
         * Returns the value code.
         * 
         * @return the value code
         */
        public int getValueCode() {
            return valueCode;
        }

        /**
         * Returns the value.
         * 
         * @return the value
         */
        public String getValue() {
            return value;
        }

        /**
         * Returns the value id.
         * 
         * @return the value id
         */
        public String getValueId() {
            return valueId;
        }
        
    }

    /**
     * The TCF calculation methods.
     * 
     * @author Holger Eichelberger, SSE
     */
    public enum TcfCalculationMethod {
        
        EN_16258(1, "EN 16258", irdi("0173-1#07-ABU224#001"));
        
        private int valueCode;
        private String value;
        private String valueId;
        
        /**
         * Creates a TCF calculation method constant.
         * 
         * @param valueCode the value code
         * @param value the value
         * @param valueId the value id
         */
        private TcfCalculationMethod(int valueCode, String value, String valueId) {
            this.valueCode = valueCode;
            this.value = value;
            this.valueId = valueId;
        }

        /**
         * Returns the value code.
         * 
         * @return the value code
         */
        public int getValueCode() {
            return valueCode;
        }

        /**
         * Returns the value.
         * 
         * @return the value
         */
        public String getValue() {
            return value;
        }

        /**
         * Returns the value id.
         * 
         * @return the value id
         */
        public String getValueId() {
            return valueId;
        }
        
    }

    /**
     * The quantity units.
     * 
     * @author Holger Eichelberger, SSE
     */
    public enum QuantityUnit {

        G(1, "g", irdi("0173-1#07-ABZ596#001")),
        KG(2, "kg", irdi("0173-1#07-ABZ597#001")),
        T(3, "t", irdi("0173-1#07-ABZ598#001")),
        ML(4, "ml", irdi("0173-1#07-ABZ599#001")),
        L(5, "l", irdi("0173-1#07-ABZ600#001")),
        CBM(6, "cbm", irdi("0173-1#07-ABZ601#001")),
        QM(7, "qm", irdi("0173-1#07-ABZ602#001")),
        PIECE(8, "piece", irdi("0173-1#07-ABZ603#001"));
        
        private int valueCode;
        private String value;
        private String valueId;
        
        /**
         * Creates a PCF calculation method constant.
         * 
         * @param valueCode the value code
         * @param value the value
         * @param valueId the value id
         */
        private QuantityUnit(int valueCode, String value, String valueId) {
            this.valueCode = valueCode;
            this.value = value;
            this.valueId = valueId;
        }

        /**
         * Returns the value code.
         * 
         * @return the value code
         */
        public int getValueCode() {
            return valueCode;
        }

        /**
         * Returns the value.
         * 
         * @return the value
         */
        public String getValue() {
            return value;
        }

        /**
         * Returns the value id.
         * 
         * @return the value id
         */
        public String getValueId() {
            return valueId;
        }

    }

    /**
     * The live cycle phases.
     * 
     * @author Holger Eichelberger, SSE
     */
    public enum PcfLiveCyclePhase {
        A1(1, "A1 - raw material supply (and upstream production)", irdi("0173-1#07-ABU208#001")),
        A2(2, "A2 - cradle-to-gate transport to factory", irdi("0173-1#07-ABU209#001")),
        A3(3, "A3 - production", irdi("0173-1#07-ABU210#001")),
        A4(4, "A4 - transport to final destination", irdi("0173-1#07-ABU211#001")),
        B1(5, "B1 - usage phase", irdi("0173-1#07-ABU212#001")),
        B2(6, "B2 - maintenance", irdi("0173-1#07-ABV498#001")),
        B3(15, "B3 - repair", irdi("0173-1#07-ABV497#001")),
        B5(7, "B5 - update/upgrade, refurbishing", irdi("0173-1#07-ABV499#001")),
        B6(8, "B6 - usage energy consumption", irdi("0173-1#07-ABV500#001")),
        B7(9, "B7 - usage water consumption", irdi("0173-1#07-ABV501#001")),
        C1(10, "C1 - reassembly", irdi("0173-1#07-ABV502#001")),
        C2(11, "C2 - transport to recycler", irdi("0173-1#07-ABU213#001")),
        C3(12, "C3 - recycling, waste treatment", irdi("0173-1#07-ABV503#001")),
        C4(13, "C4 - landfill", irdi("0173-1#07-ABV504#001")),
        D(14, "D - reuse", irdi("0173-1#07-ABU214#001")),
        A1_A3(16, "A1-A3", irdi("0173-1#07-ABZ789#001"));
        
        private int valueCode;
        private String value;
        private String valueId;
        
        /**
         * Creates a PCF livecycle constant.
         * 
         * @param valueCode the value code
         * @param value the value
         * @param valueId the value id
         */
        private PcfLiveCyclePhase(int valueCode, String value, String valueId) {
            this.valueCode = valueCode;
            this.value = value;
            this.valueId = valueId;
        }

        /**
         * Returns the value code.
         * 
         * @return the value code
         */
        public int getValueCode() {
            return valueCode;
        }

        /**
         * Returns the value.
         * 
         * @return the value
         */
        public String getValue() {
            return value;
        }

        /**
         * Returns the value id.
         * 
         * @return the value id
         */
        public String getValueId() {
            return valueId;
        }

    }

    /**
     * The live cycle phases.
     * 
     * @author Holger Eichelberger, SSE
     */
    public enum TcfProcess {

        WTT(1, "WTT - Well-to-Tank", irdi("0173-1#07-ABU216#001")),
        TTW(2, "TTW - Tank-to-Wheel", irdi("0173-1#07-ABU215#001")),
        WTW(3, "WTW - Well-to-Wheel", irdi("0173-1#07-ABU217#001"));
        
        private int valueCode;
        private String value;
        private String valueId;
        
        /**
         * Creates a TCF process constant.
         * 
         * @param valueCode the value code
         * @param value the value
         * @param valueId the value id
         */
        private TcfProcess(int valueCode, String value, String valueId) {
            this.valueCode = valueCode;
            this.value = value;
            this.valueId = valueId;
        }

        /**
         * Returns the value code.
         * 
         * @return the value code
         */
        public int getValueCode() {
            return valueCode;
        }

        /**
         * Returns the value.
         * 
         * @return the value
         */
        public String getValue() {
            return value;
        }

        /**
         * Returns the value id.
         * 
         * @return the value id
         */
        public String getValueId() {
            return valueId;
        }

    }

    /**
     * The product carbon footprint builder.
     * 
     * @author Holger Eichelberger, SSE
     */
    public class ProductCarbonFootprintBuilder extends DelegatingSubmodelElementCollectionBuilder {

        private boolean hasMethod = false;
        private boolean hasEmissions = false;
        private boolean hasQuantityUnit = false;
        private boolean hasQuantity = false;
        private int livecyclePhaseCounter;
        private boolean hasAddress = false;
        
        /**
         * Creates a PCF builder.
         * 
         * @param smBuilder the parent submodel builder
         * @param pcfNr the SMC number
         */
        private ProductCarbonFootprintBuilder(SubmodelBuilder smBuilder, int pcfNr) {
            super(smBuilder.createSubmodelElementCollectionBuilder(
                getCountingIdShort("ProductCarbonFootprint", pcfNr), false, false));
            setSemanticId(irdi("0173-1#01-AHE716#001"));
        }

        /**
         * Defines the PCF calculation method.
         * 
         * @param method the method
         * @return <b>this</b>
         */
        public ProductCarbonFootprintBuilder setPCFCalculationMethod(PcfCalculationMethod method) {
            createPropertyBuilder("PCFCalculationMethod")
                .setSemanticId(irdi("0173-1#02-ABG854#001"))
                .setValue(Type.STRING, method.getValue())
                .build();
            hasMethod = true;
            return this;
        }
        
        /**
         * Defines the sum of all greenhouse gas emissions of a product according to the
         * quantification requirements of the standard.
         * 
         * @param sumOfEmissions the sum of all emissions
         * @return <b>this</b>
         */
        public ProductCarbonFootprintBuilder setPCFCO2eq(double sumOfEmissions) {
            createPropertyBuilder("PCFCO2eq")
                .setSemanticId(irdi("0173-1#02-ABG855#001"))
                .setValue(Type.DOUBLE, sumOfEmissions)
                .build();
            hasEmissions = true;
            return this;
        }

        /**
         * Sets the quantity unit of the product to which the PCF information on the CO2 footprint refers.
         * 
         * @param quantityUnit the quantity unit
         * @return <b>this</b>
         */
        public ProductCarbonFootprintBuilder setPCFReferenceValueForCalculation(QuantityUnit quantityUnit) {
            createPropertyBuilder("PCFReferenceValueForCalculation")
                .setSemanticId(irdi("0173-1#02-ABG856#001"))
                .setValue(Type.STRING, quantityUnit.getValue())
                .build();
            hasQuantityUnit = true;
            return this;
        }

        /**
         * Sets the quantity of the product to which the PCF information on the CO2 footprint refers.
         * 
         * @param quantity the quantity
         * @return <b>this</b>
         */
        public ProductCarbonFootprintBuilder setPCFQuantityOfMeasureForCalculation(double quantity) {
            createPropertyBuilder("PCFQuantityOfMeasureForCalculation")
                .setSemanticId(irdi("0173-1#02-ABG857#001"))
                .setValue(Type.DOUBLE, quantity)
                .build();
            hasQuantity = true;
            return this;
        }        

        /**
         * Sets the live cycle stages of the product according to the quantification
         * requirements of the standard to which the PCF carbon footprint statement
         * refers.
         * 
         * @param phase the phase
         * @return <b>this</b>
         */
        public ProductCarbonFootprintBuilder addPCFLiveCyclePhase(PcfLiveCyclePhase phase) {
            createPropertyBuilder(Utils.getCountingIdShort("PCFLiveCyclePhase", livecyclePhaseCounter++))
                .setSemanticId(irdi("0173-1#02-ABG858#001"))
                .setValue(Type.STRING, phase.getValue())
                .build();
            return this;
        }
        
        /**
         * Creates a builder that indicates the place of hand-over of the goods.
         * 
         * @return the builder
         */
        public AddressBuilder createPCFGoodsAddressHandoverBuilder() {
            hasAddress = true;
            return new AddressBuilder(this, "PCFGoodsAddressHandoverBuilder", irdi("0173-1#02-ABI497#001"));
        }
        
        @Override
        public SubmodelElementCollection build() {
            assertThat(hasMethod, "PCF Method missing");
            assertThat(hasEmissions, "Emissions missing");
            assertThat(hasQuantityUnit, "Quantity unit missing");
            assertThat(hasQuantity, "Quantity missing");
            assertThat(livecyclePhaseCounter > 0, "Livecycle phase(s) missing");
            assertThat(hasAddress, "Handover address missing");
            return super.build();
        }

    }

    /**
     * The transport carbon footpring builder.
     * 
     * @author Holger Eichelberger, SSE
     */
    public class TransportCarbonFootprintBuilder extends DelegatingSubmodelElementCollectionBuilder {

        private boolean hasMethod = false;
        private boolean hasEmissions = false;
        private boolean hasAmountUnit = false;
        private boolean hasQuantity = false;
        private boolean hasProcess = false;
        private boolean hasToAddress = false;
        private boolean hasHoAddress = false;

        /**
         * Creates a TCF builder.
         * 
         * @param smBuilder the parent submodel builder
         * @param tcfNr the SMC number
         */
        private TransportCarbonFootprintBuilder(SubmodelBuilder smBuilder, int tcfNr) {
            super(smBuilder.createSubmodelElementCollectionBuilder(
                getCountingIdShort("TransportCarbonFootprint", tcfNr), false, false));
            setSemanticId(irdi("0173-1#01-AHE717#001"));
        }

        /**
         * Defines the PCF calculation method.
         * 
         * @param method the method
         * @return <b>this</b>
         */
        public TransportCarbonFootprintBuilder setTcfCalculationMethod(TcfCalculationMethod method) {
            createPropertyBuilder("TCFCalculationMethod")
                .setSemanticId(irdi("0173-1#02-ABG859#001"))
                .setValue(Type.STRING, method.getValue())
                .build();
            hasMethod = true;
            return this;
        }
        
        /**
         * Defines the sum of all greenhouse gas emissions from vehicle operation.
         * 
         * @param sumOfEmissions the sum of all emissions
         * @return <b>this</b>
         */
        public TransportCarbonFootprintBuilder setTCFCO2eq(double sumOfEmissions) {
            createPropertyBuilder("TCFCO2eq")
                .setSemanticId(irdi("0173-1#02-ABG860#001"))
                .setValue(Type.DOUBLE, sumOfEmissions)
                .build();
            hasEmissions = true;
            return this;
        }

        /**
         * Sets the amount of product to which the TCF carbon footprint statement relates.
         * 
         * @param amountUnit the amount unit
         * @return <b>this</b>
         */
        public TransportCarbonFootprintBuilder setTCFReferenceValueForCalculation(QuantityUnit amountUnit) {
            createPropertyBuilder("TCFReferenceValueForCalculation")
                .setSemanticId(irdi("0173-1#02-ABG861#001"))
                .setValue(Type.STRING, amountUnit.getValue())
                .build();
            hasAmountUnit = true;
            return this;
        }

        /**
         * Sets the quantity of the product to which the TCF information on the CO2 footprint refers.
         * 
         * @param quantity the quantity
         * @return <b>this</b>
         */
        public TransportCarbonFootprintBuilder setTCFQuantityOfMeasureForCalculation(double quantity) {
            createPropertyBuilder("PCFQuantityOfMeasureForCalculation")
                .setSemanticId(irdi("0173-1#02-ABG862#001"))
                .setValue(Type.DOUBLE, quantity)
                .build();
            hasQuantity = true;
            return this;
        }        

        /**
         * Sets the live cycle stages of the product according to the quantification
         * requirements of the standard to which the PCF carbon footprint statement
         * refers.
         * 
         * @param process the process
         * @return <b>this</b>
         */
        public TransportCarbonFootprintBuilder setTCFProcessesForGreenhouseGasEmissionInATransportService(
            TcfProcess process) {
            createPropertyBuilder("TCFProcessesForGreenhouseGasEmissionInATransportService")
                .setSemanticId(irdi("0173-1#02-ABG863#001"))
                .setValue(Type.STRING, process.getValue())
                .build();
            hasProcess = true;
            return this;
        }

        /**
         * Creates a builder for the indication of the place of receipt of goods.
         * 
         * @return the builder
         */
        public AddressBuilder createTCFGoodsTransportAddressTakeoverBuilder() {
            hasToAddress = false;
            return new AddressBuilder(this, "TCFGoodsTransportAddressTakeover", irdi("0173-1#02-ABI499#001"));
        }

        /**
         * Creates a builder that indicates the hand-over address of the goods transport.
         * 
         * @return the builder
         */
        public AddressBuilder createTCFGoodsTransportAddressHandoverBuilder() {
            hasHoAddress = true;
            return new AddressBuilder(this, "TCFGoodsTransportAddressHandover", irdi("0173-1#02-ABI498#001"));
        }

        @Override
        public SubmodelElementCollection build() {
            assertThat(hasMethod, "TCF method missing");
            assertThat(hasEmissions, "Emissions missing");
            assertThat(hasAmountUnit, "Amount unit missing");
            assertThat(hasQuantity, "Quantity missing");
            assertThat(hasProcess, "Process missing");
            assertThat(hasToAddress, "Takeover address missing");
            assertThat(hasHoAddress, "Handover address missing");
            return super.build();
        }

    }
    
    /**
     * The address builder.
     * 
     * @author Holger Eichelberger, SSE
     */
    public class AddressBuilder extends DelegatingSubmodelElementCollectionBuilder {
        
        /**
         * Creates a PCF builder.
         * 
         * @param smBuilder the parent submodel builder
         * @param idShort the idDhort
         * @param semanticId the semantic id
         */
        private AddressBuilder(SubmodelElementCollectionBuilder smBuilder, String idShort, String semanticId) {
            super(smBuilder.createSubmodelElementCollectionBuilder(idShort, false, false));
            setSemanticId(semanticId);
        }

        /**
         * Sets the street indication of the place of transfer of goods.
         * 
         * @param street the street
         * @return <b>this</b>
         */
        public AddressBuilder setStreet(String street) {
            createPropertyBuilder("Street")
                .setSemanticId(irdi("0173-1#02-ABH956#001"))
                .setValue(Type.STRING, street)
                .build();
            return this;
        }

        /**
         * Sets the number for identification or differentiation of individual houses of a street.
         * 
         * @param houseNumber the number
         * @return <b>this</b>
         */
        public AddressBuilder setHouseNumber(String houseNumber) {
            createPropertyBuilder("HouseNumber")
                .setSemanticId(irdi("0173-1#02-ABH957#001"))
                .setValue(Type.STRING, houseNumber)
                .build();
            return this;
        }

        /**
         * Sets the zip code of the goods transfer address.
         * 
         * @param zipCode the ZIP code
         * @return <b>this</b>
         */
        public AddressBuilder setZipCode(String zipCode) {
            createPropertyBuilder("ZipCode")
                .setSemanticId(irdi("0173-1#02-ABH958#001"))
                .setValue(Type.STRING, zipCode)
                .build();
            return this;
        }

        /**
         * Sets the indication of the city or town of the transfer of goods.
         * 
         * @param cityTown the city/town
         * @return <b>this</b>
         */
        public AddressBuilder setCityTown(String cityTown) {
            createPropertyBuilder("CityTown")
                .setSemanticId(irdi("0173-1#02-ABH959#001"))
                .setValue(Type.STRING, cityTown)
                .build();
            return this;
        }

        /**
         * Sets the country where the product is transmitted.
         * 
         * @param country the country
         * @return <b>this</b>
         */
        public AddressBuilder setCountry(String country) {
            createPropertyBuilder("Country")
                .setSemanticId(irdi("0173-1#02-AAO259#005"))
                .setValue(Type.STRING, country)
                .build();
            return this;
        }        

        /**
         * Sets the latitude.
         * 
         * @param latitude the latitude
         * @return <b>this</b>
         */
        public AddressBuilder setLatitude(double latitude) {
            createPropertyBuilder("Latitude")
                .setSemanticId(irdi("0173-1#02-ABH960#001"))
                .setValue(Type.DOUBLE, latitude)
                .build();
            return this;
        }        

        /**
         * Sets the longitude.
         * 
         * @param longitude the longitude
         * @return <b>this</b>
         */
        public AddressBuilder setLongitude(double longitude) {
            createPropertyBuilder("Longitude")
                .setSemanticId(irdi("0173-1#02-ABH961#001"))
                .setValue(Type.DOUBLE, longitude)
                .build();
            return this;
        }        

    }

    @Override
    public Submodel build() {
        // no restrictions
        return super.build();
    }

}
