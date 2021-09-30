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
import org.eclipse.basyx.submodel.metamodel.api.ISubmodel;

import de.iip_ecosphere.platform.support.Builder;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.AssetKind;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.Asset.AssetBuilder;
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
            buildMyDeferred();
            return instance;
        }

        @Override
        public SubmodelBuilder createSubmodelBuilder(String idShort, String identifier) {
            return instance.obtainSubmodelBuilder(this, idShort, identifier);
        }
        
        @Override
        public Submodel register(BaSyxSubmodel submodel) {
            if (null == instance.getSubmodel(submodel.getIdShort())) {
                instance.getAas().addSubmodel(submodel.getSubmodel());
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

        @Override
        public AssetBuilder createAssetBuilder(String idShort, String urn, AssetKind kind) {
            throw new IllegalArgumentException("Asset cannot be created on/assigned to deployed AAS.");
        }

        @Override
        void setAsset(BaSyxAsset asset) {
            // do nothing, not possible
        }
        
        @Override
        void defer(String shortId, Builder<?> builder) {
            getInstance().defer(shortId, builder);
        }

        @Override
        void buildMyDeferred() {
            getInstance().buildDeferred();
        }

    }
    
    /**
     * Creates a connected AAS instance.
     * 
     * @param aas the implementing AAS
     */
    BaSyxConnectedAas(ConnectedAssetAdministrationShell aas) {
        super(aas);
        for (ISubmodel sm : aas.getSubmodels().values()) {
            register(new BaSyxISubmodel(this, sm));
        }
    }
    
    @Override
    public void update() {
    }
    
    /**
     * Obtains a sub-model builder.
     *
     * @param builder the AAS builder
     * @param idShort the short id
     * @param identifier the identifier of the sub-model (may be <b>null</b> or empty for an identification based on
     *    {@code idShort}, interpreted as an URN if this starts with {@code urn})
     * @return the created sub-model builder
     */
    private SubmodelBuilder obtainSubmodelBuilder(BaSyxConnectedAasBuilder builder, String idShort, String identifier) {
        SubmodelBuilder result = getDeferred(idShort, SubmodelBuilder.class);
        if (null == result) {
            Submodel sub =  getSubmodel(idShort);
            if (null == sub) { // new here
                result = new BaSyxSubmodel.BaSyxSubmodelBuilder(builder, idShort, identifier);
            } else if (sub instanceof BaSyxSubmodel) { // after add
                result = new BaSyxSubmodel.BaSyxSubmodelBuilder(builder, (BaSyxSubmodel) sub);
            } else { // connected
                result = new BaSyxISubmodel.BaSyxISubmodelBuilder(builder, (BaSyxISubmodel) sub);
            }
        }
        return result;
    }

    @Override
    public SubmodelBuilder createSubmodelBuilder(String idShort, String identifier) {
        return obtainSubmodelBuilder(new BaSyxConnectedAasBuilder(this), idShort, identifier);
    }

    @Override
    public AasBuilder createAasBuilder() {
        return new BaSyxConnectedAasBuilder(this);
    }

}
