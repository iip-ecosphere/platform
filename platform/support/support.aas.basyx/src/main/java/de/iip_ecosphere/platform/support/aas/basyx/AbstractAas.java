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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.basyx.aas.metamodel.api.IAssetAdministrationShell;

import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.AasVisitor;
import de.iip_ecosphere.platform.support.aas.Reference;
import de.iip_ecosphere.platform.support.aas.Submodel;

/**
 * Abstract implementation of the {@link Aas} interface.
 * 
 * @param <A> the BaSyx AAS type to wrap
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractAas<A extends IAssetAdministrationShell> implements Aas {

    /**
     * Represents the parent instance of a sub-model. Due to the two different AAS types in BaSyx, this
     * cannot just be an AAS instance rather than a pseudo instance being able to provide the correct 
     * operations.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface BaSyxSubmodelParent {
        /**
         * Creates an AAS builder on parent level.
         * 
         * @return the AAS builder
         */
        public BaSyxAbstractAasBuilder createAasBuilder();
        
    }
    
    /**
     * An abstract builder for two concrete AAS types in BaSyx.
     * 
     * @author Holger Eichelberger, SSE
     */
    abstract static class BaSyxAbstractAasBuilder implements AasBuilder {
        
        /**
         * Registers a sub-model.
         * 
         * @param submodel the sub-model
         * @return {@code submodel}
         */
        abstract Submodel register(BaSyxSubmodel submodel);
        
        /**
         * Returns the sub-model parent.
         * 
         * @return the sub-model parent
         */
        abstract BaSyxSubmodelParent getSubmodelParent();

        /**
         * Returns the instance under creation.
         * 
         * @return the instance
         */
        abstract Aas getInstance(); 
        
        @Override
        public Reference createReference() {
            return getInstance().createReference();
        }
        
    }
    
    private A aas;
    private Map<String, Submodel> submodels = new HashMap<>();

    /**
     * Creates an instance. Prevents external creation.
     * 
     * @param aas the BaSyx AAS instance
     */
    protected AbstractAas(A aas) {
        this.aas = aas;
    }

    /**
     * Returns the AAS instance.
     * 
     * @return the AAS instance
     */
    A getAas() {
        return aas;
    }
    
    @Override
    public String getIdShort() {
        return aas.getIdShort();
    }

    @Override
    public Iterable<Submodel> submodels() {
        return submodels.values();
    }

    @Override
    public int getSubmodelCount() {
        return submodels.size();
    }
    
    @Override
    public Submodel getSubmodel(String idShort) {
        return submodels.get(idShort);
    }

    /**
     * Registers a sub-model.
     * 
     * @param <S> the actual sub-model type
     * @param submodel the sub-model to register
     * @return {@code subModel}
     */
    <S extends Submodel> S register(S submodel) {
        submodels.put(submodel.getIdShort(), submodel);
        return submodel;
    }

    @Override
    public void accept(AasVisitor visitor) {
        visitor.visitAas(this);
        for (Submodel sm : submodels.values()) {
            sm.accept(visitor);
        }
        visitor.endAas(this);
    }

    @Override
    public Reference createReference() {
        return new BaSyxReference(getAas().getReference());
    }

}
