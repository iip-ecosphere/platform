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

package de.iip_ecosphere.platform.support.aas.types.technicaldata;

import static de.iip_ecosphere.platform.support.aas.IdentifierType.*;
import static de.iip_ecosphere.platform.support.aas.types.common.Utils.*;

import javax.xml.datatype.XMLGregorianCalendar;

import de.iip_ecosphere.platform.support.aas.LangString;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.Type;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection.SubmodelElementCollectionBuilder;
import de.iip_ecosphere.platform.support.aas.types.common.DelegatingSubmodelBuilder;
import de.iip_ecosphere.platform.support.aas.types.common.DelegatingSubmodelElementCollectionBuilder;
import de.iip_ecosphere.platform.support.aas.types.common.Utils;

/**
 * The builder for {@link TechnicalDataSubmodel}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TechnicalDataSubmodelBuilder extends DelegatingSubmodelBuilder {

    private boolean createMultiLanguageProperties = true;
    private boolean hasGeneralInformation;
    private boolean hasTechnicalProperties;

    /**
     * Creates a builder instance.
     * 
     * @param aasBuilder the parent builder
     * @param identification the identification of the submodel
     */
    public TechnicalDataSubmodelBuilder(AasBuilder aasBuilder, String identification) {
        super(aasBuilder.createSubmodelBuilder(TechnicalDataSubmodel.ID_SHORT, identification));
        setSemanticId(iri("https://admin-shell.io/ZVEI/TechnicalData/Submodel/1/2"));
    }

    /**
     * Defines whether multi-language properties shall be created. AASPackageExplorer compliance.
     *
     * @param createMultiLanguageProperties whether multi-language properties shall be created, taints compliance 
     *     if {@code false}
     */
    public void setCreateMultiLanguageProperties(boolean createMultiLanguageProperties) {
        this.createMultiLanguageProperties = createMultiLanguageProperties;
    } 
    
    /**
     * Creates a technical properties builder.
     * 
     * @return the builder
     */
    public TechnicalPropertiesBuilder createTechnicalPropertiesBuilder() {
        hasTechnicalProperties = true;
        return new TechnicalPropertiesBuilder(this);
    }

    /**
     * Creates a product classifications builder.
     * 
     * @return the builder
     */
    public ProductClassificationsBuilder createProductClassificationsBuilder() {
        return new ProductClassificationsBuilder(this);
    }
    
    /**
     * Creates a general information builder.
     * 
     * @param manufacturerName the manufacturer name
     * @param manufacturerPartNumber the manufacturer part number
     * @param manufacturerOrderCode the manufacturer order code
     * @param manufacturerProductDesignation the manufacturer product designation
     * @return the builder
     */
    public GeneralInformationBuilder createGeneralInformationBuilder(String manufacturerName, 
        String manufacturerPartNumber, String manufacturerOrderCode, 
        de.iip_ecosphere.platform.support.aas.LangString... manufacturerProductDesignation) {
        hasGeneralInformation = true;
        return new GeneralInformationBuilder(this, manufacturerName,
            manufacturerPartNumber, manufacturerOrderCode, manufacturerProductDesignation);
    }

    /**
     * Creates a further information builder.
     * 
     * @param validDate denotes a date on which the data specified in the submodel was valid from for the 
     *     associated asset, may be <b>null</b> for now
     * @return the builder
     */
    public FurtherInformationBuilder createFurtherInformationBuilder(XMLGregorianCalendar validDate) {
        return new FurtherInformationBuilder(this, validDate);
    }
    
    @Override
    public Submodel build() {
        assertThat(hasTechnicalProperties, "Must have technical properties");
        assertThat(hasGeneralInformation, "Must have general information");
        return super.build();
    }

    /**
     * The general information builder. There are no specific sub-builders for SmePropertyNotDescribedBySemanticId
     * and arbitrary elements as these are just projections of the elements.
     * 
     * @author Holger Eichelberger, SSE
     */
    public class TechnicalPropertiesBuilder extends DelegatingSubmodelElementCollectionBuilder {

        /**
         * Creates a builder.
         * 
         * @param parent the parent builder
         */
        TechnicalPropertiesBuilder(TechnicalDataSubmodelBuilder parent) {
            super(parent.createSubmodelElementCollectionBuilder(TechnicalProperties.ID_SHORT, false, false));
            setSemanticId(iri("https://admin-shell.io/ZVEI/TechnicalData/TechnicalProperties/1/1"));
        }
        
        /**
         * Creates a main section builder.
         * 
         * @param idShort the idShort of the section
         * @param ordered whether the collection is ordered
         * @param allowDuplicates whether the collection allows duplicates
         * @return the main section builder
         * @throws IllegalArgumentException if {@code idShort} is <b>null</b> or empty
         */
        public SubmodelElementCollectionBuilder createMainSectionBuilder(String idShort, boolean ordered, 
            boolean allowDuplicates) {
            return createSubmodelElementCollectionBuilder(idShort, ordered, allowDuplicates)
                .setSemanticId(TechnicalProperties.IRI_MAIN_SECTION);
        }

        /**
         * Creates a sub section builder.
         * 
         * @param idShort the idShort of the section
         * @param ordered whether the collection is ordered
         * @param allowDuplicates whether the collection allows duplicates
         * @return the sub section builder
         * @throws IllegalArgumentException if {@code idShort} is <b>null</b> or empty
         */
        public SubmodelElementCollectionBuilder createSubSectionBuilder(String idShort, boolean ordered, 
            boolean allowDuplicates) {
            return createSubmodelElementCollectionBuilder(idShort, ordered, allowDuplicates)
                .setSemanticId(TechnicalProperties.IRI_SUB_SECTION);
        }
        
    }
    
    /**
     * The further information builder.
     * 
     * @author Holger Eichelberger, SSE
     */
    public class FurtherInformationBuilder extends DelegatingSubmodelElementCollectionBuilder {

        private int textStatementCount;
        
        /**
         * Creates a further information builder.
         * 
         * @param parent the parent submodel builder
         * @param validDate denotes a date on which the data specified in the submodel was valid from for the 
         *     associated asset, may be <b>null</b> for now
         */
        private FurtherInformationBuilder(TechnicalDataSubmodelBuilder parent, XMLGregorianCalendar validDate) {
            super(parent.createSubmodelElementCollectionBuilder(FurtherInformation.ID_SHORT, false, false));
            setSemanticId(iri("https://admin-shell.io/ZVEI/TechnicalData/FurtherInformation/1/1")); 
            createPropertyBuilder("ValidDate")
                .setValue(Type.DATE_TIME, validDate)
                .setSemanticId(iri("https://admin-shell.io/ZVEI/TechnicalData/ValidDate/1/1"))
                .build();
        }
        
        /**
         * Adds a text statement.
         * 
         * @param name the name to be used as short identifier, may be prefixed by the underlying implementation
         * @param statement the language-annotated statement
         * @return <b>this</b>
         */
        public FurtherInformationBuilder addStatement(String name, LangString... statement) {
            Utils.createMultiLanguageProperty(this, createMultiLanguageProperties, 
                getCountingIdShort(FurtherInformation.TEXT_STATEMENT_PREFIX, ++textStatementCount), 
                FurtherInformation.IRI_TEXT_STATEMENT, statement);
            return this;
        }

    }
    
    /**
     * The general information builder.
     * 
     * @author Holger Eichelberger, SSE
     */
    public class GeneralInformationBuilder extends DelegatingSubmodelElementCollectionBuilder {

        private int productImageNr;
        
        /**
         * Creates a general information builder.
         * 
         * @param parent the parent builder
         * @param manufacturerName the manufacturer name
         * @param manufacturerArticleNumber the manufacturer article number
         * @param manufacturerOrderCode the manufacturer order code
         * @param manufacturerProductDesignation the manufacturer product designation
         */
        private GeneralInformationBuilder(TechnicalDataSubmodelBuilder parent, String manufacturerName, 
            String manufacturerArticleNumber, String manufacturerOrderCode,
            de.iip_ecosphere.platform.support.aas.LangString... manufacturerProductDesignation) {
            super(parent.createSubmodelElementCollectionBuilder(GeneralInformation.ID_SHORT, false, false));
            setSemanticId(iri("https://admin-shell.io/ZVEI/TechnicalData/GeneralInformation/1/1"));
            createPropertyBuilder("ManufacturerName")
                .setValue(Type.STRING, manufacturerName)
                .setSemanticId(irdi("0173-1#02-AAO677#002"))
                .build();
            createMultiLanguageProperty(this, createMultiLanguageProperties, "ManufacturerProductDesignation", 
                irdi("0173-1#02-AAW338#001"), manufacturerProductDesignation);
            createPropertyBuilder("ManufacturerArticleNumber")
                .setValue(Type.STRING, manufacturerArticleNumber)
                .setSemanticId(irdi("0173-1#02-AAO676#003"))
                .build();
            createPropertyBuilder("ManufacturerOrderCode")
                .setValue(Type.STRING, manufacturerOrderCode)
                .setSemanticId(irdi("0173-1#02-AAO227#002"))
                .build();
        }

        /**
         * Adds an optional product image in common format (.png, .jpg).
         * 
         * @param file the relative or absolute file name with extension
         * @param mimeType the mime type of the file
         * @return <b>this</b>
         */
        public GeneralInformationBuilder addProductImageFile(String file, String mimeType) {
            createFileDataElementBuilder(getCountingIdShort(GeneralInformation.PRODUCTIMAGE_PREFIX, ++productImageNr), 
                file, mimeType)
                .setSemanticId(GeneralInformation.SEMANTIC_ID_PRODUCT_IMAGE)
                .build();
            return this;
        }

        /**
         * Sets the logo of the manufacturer provided in common format (.png, .jpg).
         * 
         * @param file the relative or absolute file name with extension
         * @param mimeType the mime type of the file
         * @return <b>this</b>
         */
        public GeneralInformationBuilder setManufacturerLogo(String file, String mimeType) {
            createFileDataElementBuilder(GeneralInformation.MANUFACTURER_LOGOID, file, mimeType)
                .setSemanticId(iri("https://admin-shell.io/ZVEI/TechnicalData/ManufacturerLogo/1/1"))
                .build();
            return this;
        }

    }
    
    /**
     * The general information builder.
     * 
     * @author Holger Eichelberger, SSE
     */
    public class ProductClassificationsBuilder extends DelegatingSubmodelElementCollectionBuilder {
        
        private int itemCounter;
        
        /**
         * Creates a builder instance.
         * 
         * @param parent the parent builder
         */
        private ProductClassificationsBuilder(TechnicalDataSubmodelBuilder parent) {
            super(parent.createSubmodelElementCollectionBuilder(ProductClassifications.ID_SHORT, false, false));
            setSemanticId(iri("https://admin-shell.io/ZVEI/TechnicalData/ProductClassifications/1/1"));
        }

        /**
         * Creates a product classification item builder. 
         * 
         * @param productClassificationSystem the common name of the product classification system, e.g., 
         *   "ECLASS" or "IEC CDD".
         * @param productClassId the class of the associated product or industrial equipment in the classification 
         *   system according to the notation of the system. Ideally, the Property/valueId is used to reference the 
         *   IRI/ IRDI of the product class.
         * @return the builder
         * @throws IllegalArgumentException may be thrown if {@code idShort} is not given
         */
        public ProductClassificationItemBuilder createProductClassificationItemBuilder(
            String productClassificationSystem, String productClassId) {
            return new ProductClassificationItemBuilder(this, 
                getCountingIdShort("ProductClassificationItem", ++itemCounter), productClassificationSystem, 
                productClassId);
        }

    }
    
    /**
     * The general information builder.
     * 
     * @author Holger Eichelberger, SSE
     */
    public class ProductClassificationItemBuilder extends DelegatingSubmodelElementCollectionBuilder {

        /**
         * Creates a product classification item builder. 
         * 
         * @param parent the parent builder
         * @param idShort the id short of the item
         * @param productClassificationSystem the common name of the product classification system, e.g., 
         *   "ECLASS" or "IEC CDD".
         * @param productClassId the class of the associated product or industrial equipment in the classification 
         *   system according to the notation of the system. Ideally, the Property/valueId is used to reference the 
         *   IRI/ IRDI of the product class.
         * @throws IllegalArgumentException may be thrown if {@code idShort} is not given
         */
        private ProductClassificationItemBuilder(SubmodelElementCollectionBuilder parent, String idShort, 
            String productClassificationSystem, String productClassId) {
            super(parent.createSubmodelElementCollectionBuilder(idShort, false, false));
            setSemanticId(ProductClassificationItem.SEMANTIC_ID);
            createPropertyBuilder("ProductClassificationSystem")
                .setValue(Type.STRING, productClassificationSystem)
                .setSemanticId(iri("https://admin-shell.io/ZVEI/TechnicalData/ProductClassificationSystem/1/1"))
                .build();
            createPropertyBuilder("ProductClassId")
                .setValue(Type.STRING, productClassId)
                .setSemanticId(iri("https://admin-shell.io/ZVEI/TechnicalData/ProductClassId/1/1"))
                .build();
        }
        
        /**
         * Defines the version of the classification system.
         * 
         * @param version the version
         * @return <b>this</b>
         */
        public ProductClassificationItemBuilder setClassificationSystemVersion(String version) {
            createPropertyBuilder("ClassificationSystemVersion")
                .setValue(Type.STRING, version)
                .setSemanticId(iri("https://admin-shell.io/ZVEI/TechnicalData/ClassificationSystemVersion/1/1"))
                .build();
            return this;
        }

    }
    
    
}
