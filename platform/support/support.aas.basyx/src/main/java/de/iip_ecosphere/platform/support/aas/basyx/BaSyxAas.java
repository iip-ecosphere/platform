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
public class BaSyxAas extends AbstractAas<AssetAdministrationShell, BaSyxSubmodel> {

    /**
     * Builder for {@code BaSyxAas}.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class BaSyxAasBuilder implements AasBuilder {

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

        @Override
        public Aas build() {
            return instance;
        }

        @Override
        public SubmodelBuilder createSubModelBuilder(String shortId) {
            return new BaSyxSubmodel.BaSyxSubmodelBuilder(this, shortId);
        }

        /**
         * Registers a submodel.
         * 
         * @param subModel the submodel to register
         * @return {@code subModel}
         */
        Submodel register(BaSyxSubmodel subModel) {
            instance.getAas().addSubModel(subModel.getSubModel());
            instance.register(subModel);
            return subModel;
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
}
