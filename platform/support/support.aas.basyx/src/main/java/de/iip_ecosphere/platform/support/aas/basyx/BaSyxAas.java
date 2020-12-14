/**
 * ******************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.aas.basyx;

import org.eclipse.basyx.aas.metamodel.map.AssetAdministrationShell;
import org.eclipse.basyx.aas.metamodel.map.descriptor.ModelUrn;

import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;

/**
 * Wraps a BaSyx AAS.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxAas extends AbstractAas<AssetAdministrationShell> {

    /**
     * Builder for {@code BaSyxAas}.
     * 
     * @author Holger Eichelberger, SSE
     */
    static class BaSyxAasBuilder extends BaSyxAbstractAasBuilder {

        private BaSyxAas instance;
        
        /**
         * Creates an instance. Prevents external creation.
         * 
         * @param idShort the shortId of the AAS
         * @param urn the uniform resource name of the AAS
         * @throws IllegalArgumentException if {@code idShort} or {@code urn} is <b>null</b> or empty
         */
        BaSyxAasBuilder(String idShort, String urn) {
            if (null == idShort || 0 == idShort.length()) {
                throw new IllegalArgumentException("idShort must be given");
            }
            if (null == urn || 0 == urn.length()) {
                throw new IllegalArgumentException("urn must be given");
            }
            AssetAdministrationShell aas = new AssetAdministrationShell();
            ModelUrn aasURN = new ModelUrn(urn);
            aas.setIdentification(aasURN);
            aas.setIdShort(idShort);
            instance = new BaSyxAas(aas);
        }

        /**
         * Creates an instance from an existing BaSyx instance. Prevents external creation.
         * 
         * @param instance the BaSyx instance
         */
        BaSyxAasBuilder(BaSyxAas instance) {
            this.instance = instance;
        }

        @Override
        public Aas build() {
            return instance;
        }

        @Override
        public SubmodelBuilder createSubModelBuilder(String idShort) {
            SubmodelBuilder result;
            Submodel sub =  instance.getSubModel(idShort);
            if (null == instance.getSubModel(idShort)) {
                result = new BaSyxSubmodel.BaSyxSubmodelBuilder(this, idShort);
            } else { // no connected here
                result = new BaSyxSubmodel.BaSyxSubmodelBuilder(this, (BaSyxSubmodel) sub);
            }
            return result;
        }

        @Override
        public Submodel register(BaSyxSubmodel submodel) {
            if (null == instance.getSubModel(submodel.getIdShort())) {
                instance.getAas().addSubModel(submodel.getSubmodel());
                instance.register(submodel);
            }
            return submodel;
        }
        
        /**
         * Returns the instance under creation.
         * 
         * @return the instance
         */
        BaSyxAas getInstance() {
            return instance;
        }

        @Override
        public BaSyxSubmodelParent getSubmodelParent() {
            return new BaSyxSubmodelParent() {

                @Override
                public BaSyxAbstractAasBuilder createAasBuilder() {
                    return new BaSyxAasBuilder(instance);
                }
                
            };
        }
        
    }

    /**
     * Creates an instance. Prevents external creation.
     * 
     * @param aas the BaSyx AAS instance
     */
    private BaSyxAas(AssetAdministrationShell aas) {
        super(aas);
    }
    
    @Override
    public SubmodelBuilder addSubmodel(String idShort) {
        return new BaSyxSubmodel.BaSyxSubmodelBuilder(new BaSyxAasBuilder(this), idShort);
    }

}
