/**
 * ******************************************************************************
 * Copyright (c) {2023} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.aas.basyx;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.basyx.submodel.metamodel.api.reference.IReference;
import org.eclipse.basyx.submodel.metamodel.api.submodelelement.ISubmodelElement;

import de.iip_ecosphere.platform.support.aas.AasVisitor;
import de.iip_ecosphere.platform.support.aas.LangString;
import de.iip_ecosphere.platform.support.aas.MultiLanguageProperty;

/**
 * Multi-Language property representation for BaSyx.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxMultiLanguageProperty extends BaSyxSubmodelElement implements MultiLanguageProperty {

    private org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.MultiLanguageProperty property;

    /**
     * Builder for {@link BaSyxProperty}.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class BaSyxMultiLanguagePropertyBuilder implements MultiLanguagePropertyBuilder {

        private BaSyxSubmodelElementContainerBuilder<?> parentBuilder;
        private BaSyxMultiLanguageProperty instance;
        private List<LangString> text = new ArrayList<LangString>();

        /**
         * Creates an instance. Prevents external creation.
         * 
         * @param parentBuilder the parent builder
         * @param idShort the short name of the property
         * @throws IllegalArgumentException if {@code idShort} is <b>null</b> or empty
         */
        BaSyxMultiLanguagePropertyBuilder(BaSyxSubmodelElementContainerBuilder<?> parentBuilder, String idShort) {
            this.parentBuilder = parentBuilder;
            this.instance = new BaSyxMultiLanguageProperty();
            this.instance.property = new org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.
                MultiLanguageProperty(Tools.checkId(idShort));
        }

        @Override
        public MultiLanguagePropertyBuilder addText(LangString text) {
            this.text.add(text);
            return this;
        }

        @Override
        public MultiLanguagePropertyBuilder setDescription(LangString... description) {
            instance.property.setDescription(Tools.translate(description));
            return this;
        }

        @Override
        public MultiLanguagePropertyBuilder setSemanticId(String refValue) {
            IReference ref = Tools.translateReference(refValue);
            if (ref != null) {
                instance.property.setSemanticId(ref);
            }
            return this;
        }
        
        @Override
        public MultiLanguageProperty build() {
            LangString[] tmp = new LangString[text.size()];
            text.toArray(tmp);
            instance.property.setValue(Tools.translate(tmp));
            return parentBuilder.register(instance);
        }

    }

    @Override
    public String getIdShort() {
        return property.getIdShort();
    }

    @Override
    public void accept(AasVisitor visitor) {
        visitor.visitMultiLanguageProperty(this);
    }

    @Override
    ISubmodelElement getSubmodelElement() {
        return property;
    }

    @Override
    public String getSemanticId(boolean stripPrefix) {
        return Tools.translateReference(property.getSemanticId(), stripPrefix);
    }

    @Override
    public Map<String, LangString> getDescription() {
        return Tools.translate(property.getValue());
    }

}