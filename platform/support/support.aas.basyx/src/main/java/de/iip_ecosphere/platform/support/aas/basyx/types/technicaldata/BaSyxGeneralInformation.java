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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.basyx.submodel.metamodel.map.qualifier.LangString;
import org.eclipse.basyx.submodel.metamodel.map.qualifier.LangStrings;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.File;
import org.eclipse.basyx.submodel.types.technicaldata.submodelelementcollections.generalinformation.GeneralInformation;

import de.iip_ecosphere.platform.support.aas.basyx.Tools;
import de.iip_ecosphere.platform.support.aas.FileDataElement;
import de.iip_ecosphere.platform.support.aas.basyx.BaSyxFile;
import de.iip_ecosphere.platform.support.aas.basyx.BaSyxSubmodelElementCollection;
import de.iip_ecosphere.platform.support.aas.basyx.BaSyxSubmodelElementContainerBuilder;

/**
 * Wrapper for the BaSyx general information class.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxGeneralInformation extends BaSyxSubmodelElementCollection implements 
    de.iip_ecosphere.platform.support.aas.types.technicaldata.GeneralInformation {

    public static final String ID_SHORT = GeneralInformation.IDSHORT;
    
    /**
     * The sub-model element collection builder.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class BaSyxGeneralInformationBuilder extends BaSyxSubmodelElementCollectionBuilder 
        implements GeneralInformationBuilder {
        
        private List<BaSyxFile> productImages;
        private BaSyxFile manufacturerLogo;

        /**
         * Creates a sub-model element collection builder. The parent builder must be set by the calling
         * constructor.
         * 
         * @param parentBuilder the parent builder
         * @param manufacturerName the manufacturer name
         * @param manufacturerProductDesignation the manufacturer product designation
         * @param manufacturerPartNumber the manufacturer part number
         * @param manufacturerOrderCode the manufacturer order code
         * @throws IllegalArgumentException may be thrown if {@code idShort} is not given
         */
        BaSyxGeneralInformationBuilder(BaSyxSubmodelElementContainerBuilder<?> parentBuilder, String manufacturerName, 
            de.iip_ecosphere.platform.support.aas.LangString manufacturerProductDesignation, 
            String manufacturerPartNumber, String manufacturerOrderCode) {
            super(parentBuilder, ID_SHORT, 
                () -> new BaSyxGeneralInformation(), 
                () -> new GeneralInformation(manufacturerName, Tools.translate(manufacturerProductDesignation), 
                    manufacturerPartNumber, manufacturerOrderCode)); 
        }
        
        /**
         * Creates an instance from an existing BaSyx instance.
         * 
         * @param parentBuilder the parent builder
         * @param instance the BaSyx instance
         */
        BaSyxGeneralInformationBuilder(BaSyxSubmodelElementContainerBuilder<?> parentBuilder,
            BaSyxSubmodelElementCollection instance) {
            super(parentBuilder, instance);
        }
        
        @Override
        public BaSyxSubmodelElementCollection build() {
            GeneralInformation gi = (GeneralInformation) getCollection();
            if (null != productImages) {
                List<File> tmp = new ArrayList<File>();
                for (BaSyxFile f : productImages) {
                    getCollectionInstance().register(f);
                    tmp.add(f.getDataElement());
                }
                gi.setProductImages(tmp);
            }
            if (null != manufacturerLogo) {
                getCollectionInstance().register(manufacturerLogo);
                gi.setManufacturerLogo(manufacturerLogo.getDataElement());
            }
            return super.build();
        }

        @Override
        public GeneralInformationBuilder addProductImageFile(String name, String file, String mimeType) {
            if (null == productImages) {
                productImages = new ArrayList<BaSyxFile>();
            }
            productImages.add(new BaSyxFile(GeneralInformation.PRODUCTIMAGEPREFIX + name, file, mimeType));
            return this;
        }

        @Override
        public GeneralInformationBuilder setManufacturerLogo(String file, String mimeType) {
            manufacturerLogo = new BaSyxFile(GeneralInformation.MANUFACTURERLOGOID, file, mimeType);
            return this;
        }

    }

    /**
     * Creates an instance. Prevents external creation.
     */
    private BaSyxGeneralInformation() {
        super();
    }

    /**
     * Creates an instance and sets the BaSyx instance directly.
     * 
     * @param collection the collection instance
     */
    BaSyxGeneralInformation(org.eclipse.basyx.submodel.types.technicaldata.submodelelementcollections
        .generalinformation.GeneralInformation collection) {
        super(collection);
    }

    @Override
    public GeneralInformation getSubmodelElement() {
        return (GeneralInformation) super.getSubmodelElement();
    }
    
    @Override
    public String getManufacturerName() {
        return (String) getSubmodelElement().getManufacturerName().getValue();
    }

    @Override
    public List<de.iip_ecosphere.platform.support.aas.LangString> getManufacturerProductDesignation() {
        LangStrings ls = getSubmodelElement().getManufacturerProductDesignation().getValue();
        List<de.iip_ecosphere.platform.support.aas.LangString> result = new ArrayList<>();
        for (LangString l : ls) {
            result.add(Tools.translate(l));
        }
        return result;
    }

    @Override
    public String getManufacturerPartNumber() {
        return (String) getSubmodelElement().getManufacturerPartNumber().getValue();
    }

    @Override
    public String getManufacturerOrderCode() {
        return (String) getSubmodelElement().getManufacturerOrderCode().getValue();
    }
    
    @Override
    public FileDataElement getManufacturerLogo() {
        return (FileDataElement) getDataElement(GeneralInformation.MANUFACTURERLOGOID);
    }
    
    @Override
    public Iterable<FileDataElement> getProductImages() {
        return getElements(s -> s.getIdShort().startsWith(GeneralInformation.PRODUCTIMAGEPREFIX), 
            FileDataElement.class);
    }

}
