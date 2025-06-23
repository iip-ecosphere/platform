/**
 * ******************************************************************************
 * Copyright (c) {2025} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.aas.basyx2;

import java.io.IOException;

import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAssetAdministrationShell;
import org.eclipse.digitaltwin.basyx.aasrepository.client.ConnectedAasRepository;
import org.eclipse.digitaltwin.basyx.submodelrepository.client.ConnectedSubmodelRepository;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.Builder;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.AssetInformation.AssetInformationBuilder;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor.RbacAction;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor.Role;
import de.iip_ecosphere.platform.support.aas.AssetKind;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor;
import de.iip_ecosphere.platform.support.aas.SetupSpec;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;

/**
 * Wraps a BaSyx AAS.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxAas extends AbstractAas<org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell> 
    implements BaSyxSubmodelParent {

    private BaSyxRegistry registry;
    private ConnectedAasRepository repo;
    
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
         * @param identifier the identifier of the AAS (may be <b>null</b> or empty for an identification based on 
         *    {@code idShort}, interpreted as an URN if this starts with {@code urn})
         * @throws IllegalArgumentException if {@code idShort} or {@code urn} is <b>null</b> or empty
         */
        BaSyxAasBuilder(String idShort, String identifier) {
            org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell aas = new DefaultAssetAdministrationShell();
            aas.setIdShort(Tools.checkId(idShort));
            aas.setId(Tools.translateIdentifierToBaSyx(identifier, idShort));
            instance = new BaSyxAas(aas, null);
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
            if (null == instance.getAsset()) {
                LoggerFactory.getLogger(getClass()).warn("AAS does not have an asset, i.e., the AAS does not indicate "
                    + "whether it is an instance or a type. Further, the missing asset may prevent persisting the "
                    + "AAS.");
            }
            buildMyDeferred();
            if (instance.repo != null) {
                instance.repo.updateAas(instance.getIdentification(), instance.getAas());
            }
            return instance;
        }

        @Override
        public SubmodelBuilder createSubmodelBuilder(String idShort, String identifier) {
            return createSubmodelBuilder(idShort, identifier, null);
        }

        @Override
        public SubmodelBuilder createSubmodelBuilder(String idShort, String identifier, SetupSpec spec) {
            SubmodelBuilder result = instance.getDeferred(idShort, SubmodelBuilder.class);
            if (null == result) {
                Submodel sub =  instance.getSubmodel(idShort);
                if (null == instance.getSubmodel(idShort)) {
                    // spec may be null
                    ConnectedSubmodelRepository sRepo = SubmodelRepositoryUtils.createRepositoryApi(spec); 
                    result = new BaSyxSubmodel.BaSyxSubmodelBuilder(this, idShort, identifier, sRepo);
                } else { // no connected here
                    result = new BaSyxSubmodel.BaSyxSubmodelBuilder(this, (BaSyxSubmodel) sub);
                }
            }
            return result;
        }

        @Override
        public Submodel register(BaSyxSubmodel submodel) throws IOException {
            if (null == instance.getSubmodel(submodel.getIdShort())) {
                org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell aas = instance.getAas();
                aas.setSubmodels(Tools.addElement(aas.getSubmodels(),
                    Tools.createModelReference(submodel.getSubmodel())));
                instance.register(submodel);
                if (null != instance.registry) {
                    instance.registry.createSubmodel(instance, submodel);
                }
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
            return instance;
        }

        @Override
        public AssetInformationBuilder createAssetInformationBuilder(String idShort, String urn, AssetKind kind) {
            return new BaSyxAssetInformation.BaSyxAssetInformationBuilder(this, idShort, urn, kind);
        }

        @Override
        void setAsset(BaSyxAssetInformation asset) {
            instance.registerAsset(asset);
        }
        
        @Override
        void defer(String shortId, Builder<?> builder) {
            getInstance().defer(shortId, builder);
        }

        @Override
        void buildMyDeferred() {
            getInstance().buildDeferred();
        }
        
        @Override
        public AasBuilder rbac(AuthenticationDescriptor auth, Role role, RbacAction... actions) {
            return AuthenticationDescriptor.aasRbac(this, auth, role, getInstance().getIdentification(), actions);
        }

        @Override
        public AasBuilder rbac(AuthenticationDescriptor auth) {
            return this; // usually not needed
        }
        
    }

    /**
     * Creates an instance. Prevents external creation.
     * 
     * @param aas the BaSyx AAS instance
     */
    BaSyxAas(org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell aas, ConnectedAasRepository repo) {
        super(aas);
        this.repo = repo;
    }

    @Override
    public SubmodelBuilder createSubmodelBuilder(String idShort, String identifier) {
        return createSubmodelBuilder(idShort, identifier, null);
    }

    @Override
    public SubmodelBuilder createSubmodelBuilder(String idShort, String identifier, SetupSpec spec) {
        SubmodelBuilder result = getDeferred(idShort, SubmodelBuilder.class);
        if (null == result) {
            BaSyxSubmodel submodel = (BaSyxSubmodel) getSubmodel(idShort);
            if (null == submodel) {
                // spec may be null
                ConnectedSubmodelRepository sRepo = SubmodelRepositoryUtils.createRepositoryApi(spec); 
                result = new BaSyxSubmodel.BaSyxSubmodelBuilder(new BaSyxAasBuilder(this), idShort, identifier, sRepo);
            } else {
                result = new BaSyxSubmodel.BaSyxSubmodelBuilder(new BaSyxAasBuilder(this), submodel);
            }
        } 
        return result;
    }

    @Override
    public BaSyxAbstractAasBuilder createAasBuilder() {
        return new BaSyxAasBuilder(this);
    }

    /**
     * Registers an asset and sets the asset reference in this step. {@link #setAsset(BaSyxAssetInformation)} 
     * is called in here.
     * 
     * @param asset the asset to set
     */
    void registerAsset(BaSyxAssetInformation asset) {
        setAsset(asset);
        getAas().setAssetInformation(asset.getAsset());
    }

    /**
     * Sets the registry as part of a remote deployment process to {@code registry}.
     * 
     * @param registry the registry instance
     */
    void registerRegistry(BaSyxRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void update() {
    }
    
    /**
     * Sets the repository, thus, emulating a connected AAS.
     * 
     * @param repo the repository
     */
    void setRepo(ConnectedAasRepository repo) {
        this.repo = repo;
    }

}
