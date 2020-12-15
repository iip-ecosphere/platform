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

import org.eclipse.basyx.aas.metamodel.connected.ConnectedAssetAdministrationShell;
import org.eclipse.basyx.submodel.metamodel.api.ISubModel;

import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;

/**
 * Represents a connected AAS.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxConnectedAas extends AbstractAas<ConnectedAssetAdministrationShell> {

    /**
     * Builder for {@code BaSyxConnectedAas}.
     * 
     * @author Holger Eichelberger, SSE
     */
    static class BaSyxConnectedAasBuilder extends BaSyxAbstractAasBuilder {

        private BaSyxConnectedAas instance;

        /**
         * Creates an instance from an existing BaSyx instance. Prevents external creation.
         * 
         * @param instance the BaSyx instance
         */
        BaSyxConnectedAasBuilder(BaSyxConnectedAas instance) {
            this.instance = instance;
        }

        @Override
        public Aas build() {
            return instance;
        }

        @Override
        public SubmodelBuilder createSubmodelBuilder(String idShort) {
            SubmodelBuilder result;
            Submodel sub =  instance.getSubmodel(idShort);
            if (null == instance.getSubmodel(idShort)) { // new here
                result = new BaSyxSubmodel.BaSyxSubmodelBuilder(this, idShort);
            } else if (sub instanceof BaSyxSubmodel) { // after add
                result = new BaSyxSubmodel.BaSyxSubmodelBuilder(this, (BaSyxSubmodel) sub);
            } else { // connected
                result = new BaSyxISubmodel.BaSyxISubmodelBuilder(this, (BaSyxISubmodel) sub);
            }
            return result;
        }

        @Override
        public Submodel register(BaSyxSubmodel submodel) {
            if (null == instance.getSubmodel(submodel.getIdShort())) {
                instance.getAas().addSubModel(submodel.getSubmodel());
                instance.register(submodel);
            }
            return submodel;
        }

        @Override
        public BaSyxSubmodelParent getSubmodelParent() {
            return new BaSyxSubmodelParent() {

                @Override
                public BaSyxAbstractAasBuilder createAasBuilder() {
                    return new BaSyxConnectedAasBuilder(instance);
                }
                
            };
        }

        @Override
        BaSyxConnectedAas getInstance() {
            return instance;
        }

    }
    
    /**
     * Creates a connected AAS instance.
     * 
     * @param aas the implementing AAS
     */
    BaSyxConnectedAas(ConnectedAssetAdministrationShell aas) {
        super(aas);
        for (ISubModel sm : aas.getSubModels().values()) {
            register(new BaSyxISubmodel(this, sm));
        }
    }

    @Override
    public SubmodelBuilder addSubmodel(String idShort) {
        return new BaSyxSubmodel.BaSyxSubmodelBuilder(new BaSyxConnectedAasBuilder(this), idShort);
    }

}
