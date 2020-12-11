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
import de.iip_ecosphere.platform.support.aas.Submodel;


/**
 * Abstract implementation of the {@link Aas} interface.
 * 
 * @param <A> the BaSyx AAS type to wrap
 * @param <S> the IIP-Ecosphere sub-model type to use/return
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractAas<A extends IAssetAdministrationShell, S extends Submodel> implements Aas {

    private A aas;
    private Map<String, S> submodels = new HashMap<>();

    /**
     * Creates an instance. Prevents external creation.
     * 
     * @param aas the BaSyx AAS instance
     */
    protected AbstractAas(A aas) {
        this.aas = aas;
    }
// TODO reduce to package; 
    /**
     * Returns the AAS instance.
     * 
     * @return the AAS instance
     */
    public A getAas() {
        return aas;
    }
    
    @Override
    public String getIdShort() {
        return aas.getIdShort();
    }

    @Override
    public Iterable<S> submodels() {
        return submodels.values();
    }

    @Override
    public int getSubmodelCount() {
        return submodels.size();
    }
    
    @Override
    public Submodel getSubModel(String idShort) {
        return submodels.get(idShort);
    }

    /**
     * Registers a submodel.
     * 
     * @param subModel the submodel to register
     * @return {@code subModel}
     */
    S register(S subModel) {
        submodels.put(subModel.getIdShort(), subModel);
        return subModel;
    }

    @Override
    public void accept(AasVisitor visitor) {
        visitor.visitAas(this);
        for (Submodel sm : submodels.values()) {
            sm.accept(visitor);
        }
        visitor.endAas(this);
    }
    
}
