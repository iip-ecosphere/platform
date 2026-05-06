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

package test.de.iip_ecosphere.platform.support.fakeAas;

import de.iip_ecosphere.platform.support.aas.AasVisitor;
import de.iip_ecosphere.platform.support.aas.LangString;
import de.iip_ecosphere.platform.support.aas.MultiLanguageProperty;

/**
 * Implements a fake property for testing.
 * 
 * @author Holger Eichelberger, SSE
 */
public class FakeMultiLanguageProperty extends FakeElement implements MultiLanguageProperty {

    /**
     * A fake property builder.
     * 
     * @author Holger Eichelberger, SSE
     */
    static class FakeMultiLanguagePropertyBuilder implements MultiLanguagePropertyBuilder {

        private FakeSubmodelElementContainerBuilder parent;
        private FakeMultiLanguageProperty instance;
        
        /**
         * Creates the fake property builder.
         * 
         * @param parent the parent builder
         * @param idShort the short id
         */
        FakeMultiLanguagePropertyBuilder(FakeSubmodelElementContainerBuilder parent, String idShort) {
            this.parent = parent;
            this.instance = new FakeMultiLanguageProperty(idShort);
        }
 
        @Override
        public MultiLanguagePropertyBuilder setSemanticId(String semanticId) {
            instance.setSemanticId(semanticId);
            return this;
        }
        
        @Override
        public MultiLanguagePropertyBuilder setDescription(LangString... description) {
            instance.setDescription(description);
            return this;
        }
        
        @Override
        public MultiLanguageProperty build() {
            return parent.register(instance);
        }

        @Override
        public MultiLanguagePropertyBuilder addText(LangString text) {
            instance.addDescription(text);
            return this;
        }
        
    }
    
    /**
     * Creates the instance.
     * 
     * @param idShort the short id.
     */
    protected FakeMultiLanguageProperty(String idShort) {
        super(idShort);
    }

    @Override
    public void accept(AasVisitor visitor) {
        visitor.visitMultiLanguageProperty(this);
    }

}
