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

package test.de.iip_ecosphere.platform.support.aas;

import java.util.HashMap;
import java.util.Map;

import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.AasVisitor;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;

/**
 * Implements a fake AAS for testing.
 * 
 * @author Holger Eichelberger, SSE
 */
public class FakeAas extends FakeElement implements Aas {

    private String idShort;
    private Map<String, Submodel> submodels = new HashMap<String, Submodel>();
    
    static class FakeAasBuilder implements AasBuilder {

        private FakeAas instance;
        
        /**
         * Creates an instance.
         * 
         * @param idShort the short id
         * @param urn the URN
         */
        FakeAasBuilder(String idShort, String urn) {
            instance = new FakeAas(idShort); // we do not return the URN so far, so we ignore it here
        }
        
        @Override
        public SubmodelBuilder createSubModelBuilder(String idShort) {
            return new FakeSubmodel.FakeSubmodelBuilder(this, idShort);
        }
        
        /**
         * Registers a sub-model.
         * 
         * @param submodel the sub-model
         * @return {@code submodel}
         */
        Submodel register(Submodel submodel) {
            instance.submodels.put(submodel.getIdShort(), submodel);
            return submodel;
        }

        @Override
        public Aas build() {
            return instance;
        }
        
    }
    
    /**
     * Creates the instance.
     * 
     * @param idShort the short id.
     */
    FakeAas(String idShort) {
        super(idShort);
    }

    @Override
    public String getIdShort() {
        return idShort;
    }

    @Override
    public void accept(AasVisitor visitor) {
        visitor.visitAas(this);
        for (Submodel sm : submodels()) {
            sm.accept(visitor);
        }
        visitor.endAas(this);
    }

    @Override
    public Iterable<? extends Submodel> submodels() {
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

}
