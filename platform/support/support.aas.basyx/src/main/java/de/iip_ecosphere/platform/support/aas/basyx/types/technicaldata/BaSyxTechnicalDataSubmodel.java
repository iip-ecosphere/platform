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

package de.iip_ecosphere.platform.support.aas.basyx.types.technicaldata;

import javax.xml.datatype.XMLGregorianCalendar;

import org.eclipse.basyx.submodel.metamodel.map.identifier.Identifier;
import org.eclipse.basyx.submodel.types.technicaldata.TechnicalDataSubmodel;

import de.iip_ecosphere.platform.support.aas.Submodel;

import de.iip_ecosphere.platform.support.aas.Operation.OperationBuilder;
import de.iip_ecosphere.platform.support.aas.Property.PropertyBuilder;
import de.iip_ecosphere.platform.support.aas.ReferenceElement.ReferenceElementBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection.SubmodelElementCollectionBuilder;
import de.iip_ecosphere.platform.support.aas.basyx.BaSyxSubmodel;
import de.iip_ecosphere.platform.support.aas.basyx.BaSyxSubmodelElementCollection;
import de.iip_ecosphere.platform.support.aas.basyx.Tools;
import de.iip_ecosphere.platform.support.aas.basyx.AbstractAas.BaSyxAbstractAasBuilder;
import de.iip_ecosphere.platform.support.aas.basyx.AbstractAas.BaSyxSubmodelParent;
import de.iip_ecosphere.platform.support.aas.types.technicaldata.FurtherInformation;
import de.iip_ecosphere.platform.support.aas.types.technicaldata.FurtherInformation.FurtherInformationBuilder;
import de.iip_ecosphere.platform.support.aas.types.technicaldata.GeneralInformation;
import de.iip_ecosphere.platform.support.aas.types.technicaldata.GeneralInformation.GeneralInformationBuilder;
import de.iip_ecosphere.platform.support.aas.types.technicaldata.ProductClassifications;
import de.iip_ecosphere.platform.support.aas.types.technicaldata.ProductClassifications.ProductClassificationsBuilder;
import de.iip_ecosphere.platform.support.aas.types.technicaldata.TechnicalProperties;
import de.iip_ecosphere.platform.support.aas.types.technicaldata.TechnicalProperties.TechnicalPropertiesBuilder;

/**
 * Wrapper for the BaSyx technical data submodel class.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxTechnicalDataSubmodel extends BaSyxSubmodel implements 
    de.iip_ecosphere.platform.support.aas.types.technicaldata.TechnicalDataSubmodel {

    public static final String ID_SHORT = TechnicalDataSubmodel.SUBMODELID;
    
    /**
     * The sub-model element collection builder.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class BaSyxTechnicalDataSubmodelBuilder extends BaSyxSubmodelBuilder 
        implements TechnicalDataSubmodelBuilder {
        
        private String identifier;
        private BaSyxFurtherInformation furtherInformation;
        private BaSyxGeneralInformation generalInformation;
        private BaSyxProductClassifications productClassifications;
        private BaSyxTechnicalProperties technicalProperties;

        /**
         * Creates an instance. Prevents external creation.
         * 
         * @param parentBuilder the parent builder (may be <b>null</b> for a standalone sub-model)
         * @param identifier the identifier of the sub-model (may be <b>null</b> or empty for an identification based on
         *    {@code idShort}, interpreted as an URN if this starts with {@code urn})
         */
        public BaSyxTechnicalDataSubmodelBuilder(BaSyxAbstractAasBuilder parentBuilder, String identifier) {
            super(parentBuilder);
            this.identifier = identifier;
        }
        
        /**
         * Creates an instance from an existing BaSyx instance.
         * 
         * @param parentBuilder the parent builder (may be <b>null</b> for a standalone sub-model)
         * @param instance the BaSyx instance wrapper
         */
        public BaSyxTechnicalDataSubmodelBuilder(BaSyxAbstractAasBuilder parentBuilder, 
            BaSyxTechnicalDataSubmodel instance) {
            super(parentBuilder, instance);
        }

        @Override
        protected BaSyxSubmodelElementCollection register(BaSyxSubmodelElementCollection collection, 
            boolean propagate) {
            if (collection instanceof BaSyxFurtherInformation) {
                this.furtherInformation = (BaSyxFurtherInformation) collection;
            } else if (collection instanceof BaSyxGeneralInformation) {
                this.generalInformation = (BaSyxGeneralInformation) collection;
            } else if (collection instanceof BaSyxProductClassifications) {
                this.productClassifications = (BaSyxProductClassifications) collection;
            } else if (collection instanceof BaSyxTechnicalProperties) {
                this.technicalProperties = (BaSyxTechnicalProperties) collection;
            }
            return collection;
        } // may require further overrides

        @Override
        public Submodel build() {
            if (null == furtherInformation) {
                throw new IllegalArgumentException("No further information instance available.");
            }
            if (null == generalInformation) {
                throw new IllegalArgumentException("No general information instance available.");
            }
            if (null == technicalProperties) {
                throw new IllegalArgumentException("No technical properties instance available.");
            }
            if (null == productClassifications) {
                throw new IllegalArgumentException("No product Classifications instance available.");
            }
            TechnicalDataSubmodel tds = new TechnicalDataSubmodel(ID_SHORT, 
                (Identifier) Tools.translateIdentifier(identifier, ID_SHORT), 
                generalInformation.getSubmodelElement(),
                productClassifications.getSubmodelElement(),
                technicalProperties.getSubmodelElement(),
                furtherInformation.getSubmodelElement());
            setInstance(new BaSyxTechnicalDataSubmodel(tds));
            super.register(furtherInformation, true); // propagation by default
            super.register(generalInformation, true);
            super.register(technicalProperties, true);
            super.register(productClassifications, true);
            return super.build();
        }
        
        @Override
        public PropertyBuilder createPropertyBuilder(String idShort) {
            // let's try, predefined, contrast to BaSyx
            throw new IllegalArgumentException("No further custom properties allowed here");
        }

        @Override
        public ReferenceElementBuilder createReferenceElementBuilder(String idShort) {
            // let's try, predefined, contrast to BaSyx
            throw new IllegalArgumentException("No further custom reference elements allowed here");
        }
        
        @Override
        public OperationBuilder createOperationBuilder(String idShort) {
            // let's try, predefined, contrast to BaSyx
            throw new IllegalArgumentException("No further custom operations elements allowed here");
        }

        @Override
        public SubmodelElementCollectionBuilder createSubmodelElementCollectionBuilder(String idShort, boolean ordered, 
            boolean allowDuplicates) {
            // let's try, predefined, contrast to BaSyx
            throw new IllegalArgumentException("No further custom submodel elements allowed here");
        }

        @Override
        public TechnicalPropertiesBuilder createTechnicalPropertiesBuilder() {
            TechnicalPropertiesBuilder result;
            if (null == technicalProperties) {
                result = new BaSyxTechnicalProperties.BaSyxTechnicalPropertiesBuilder(this);
            } else {
                result = new BaSyxTechnicalProperties.BaSyxTechnicalPropertiesBuilder(this, 
                        technicalProperties);
            }
            return result;
        }

        @Override
        public ProductClassificationsBuilder createProductClassificationsBuilder() {
            ProductClassificationsBuilder result;
            if (null == productClassifications) {
                result = new BaSyxProductClassifications.BaSyxProductClassificationsBuilder(this);
            } else {
                result = new BaSyxProductClassifications.BaSyxProductClassificationsBuilder(this, 
                   productClassifications);
            }
            return result;
        }

        @Override
        public GeneralInformationBuilder createGeneralInformationBuilder(String manufacturerName, 
            de.iip_ecosphere.platform.support.aas.LangString manufacturerProductDesignation, 
            String manufacturerPartNumber, String manufacturerOrderCode) {
            GeneralInformationBuilder result;
            if (null == generalInformation) {
                result = new BaSyxGeneralInformation.BaSyxGeneralInformationBuilder(this, manufacturerName, 
                    manufacturerProductDesignation, manufacturerPartNumber, manufacturerOrderCode);
            } else {
                result = new BaSyxGeneralInformation.BaSyxGeneralInformationBuilder(this, 
                   generalInformation);
            }
            return result;
        }

        @Override
        public FurtherInformationBuilder createFurtherInformationBuilder(XMLGregorianCalendar validDate) {
            FurtherInformationBuilder result;
            if (null == furtherInformation) {
                result = new BaSyxFurtherInformation.BaSyxFurtherInformationBuilder(this, validDate);
            } else {
                result = new BaSyxFurtherInformation.BaSyxFurtherInformationBuilder(this, 
                   furtherInformation);
            }
            return result;
        }

    }
    
    /**
     * Creates an instance. Prevents external creation.
     * 
     * @param subModel the sub-model instance
     */
    private BaSyxTechnicalDataSubmodel(org.eclipse.basyx.submodel.metamodel.map.Submodel subModel) {
        super(subModel);
    }
    
    /**
     * Creates an instance based on a given instance.
     * 
     * @param parent the parent instance
     * @param instance the BaSyx submodel instance
     */
    BaSyxTechnicalDataSubmodel(BaSyxSubmodelParent parent, 
        org.eclipse.basyx.submodel.types.technicaldata.TechnicalDataSubmodel instance) {
        super(parent, instance);
    }

    @Override
    public TechnicalProperties getTechnicalProperties() {
        return (TechnicalProperties) getSubmodelElementCollection(BaSyxTechnicalProperties.ID_SHORT);
    }

    @Override
    public ProductClassifications getProductClassifications() {
        return (ProductClassifications) getSubmodelElementCollection(BaSyxProductClassifications.ID_SHORT);
    }

    @Override
    public GeneralInformation getGeneralInformation() {
        return (GeneralInformation) getSubmodelElementCollection(BaSyxGeneralInformation.ID_SHORT);
    }

    @Override
    public FurtherInformation getFurtherInformation() {
        return (FurtherInformation) getSubmodelElementCollection(BaSyxFurtherInformation.ID_SHORT);
    }
    
}
