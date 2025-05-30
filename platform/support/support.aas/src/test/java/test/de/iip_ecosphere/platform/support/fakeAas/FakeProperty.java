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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import de.iip_ecosphere.platform.support.aas.AasVisitor;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor;
import de.iip_ecosphere.platform.support.aas.Invokable;
import de.iip_ecosphere.platform.support.aas.LangString;
import de.iip_ecosphere.platform.support.aas.Property;
import de.iip_ecosphere.platform.support.aas.SubmodelElementContainerBuilder;
import de.iip_ecosphere.platform.support.aas.Type;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor.RbacAction;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor.Role;

/**
 * Implements a fake property for testing.
 * 
 * @author Holger Eichelberger, SSE
 */
public class FakeProperty extends FakeElement implements Property {

    private Object value;
    @SuppressWarnings("unused")
    private Type type;
    private Map<String, LangString> description;
    
    /**
     * A fake property builder.
     * 
     * @author Holger Eichelberger, SSE
     */
    static class FakePropertyBuilder implements PropertyBuilder {

        private FakeSubmodelElementContainerBuilder parent;
        private FakeProperty instance;
        
        /**
         * Creates the fake property builder.
         * 
         * @param parent the parent builder
         * @param idShort the short id
         */
        FakePropertyBuilder(FakeSubmodelElementContainerBuilder parent, String idShort) {
            this.parent = parent;
            this.instance = new FakeProperty(idShort);
        }
        
        @Override
        public SubmodelElementContainerBuilder getParentBuilder() {
            return parent;
        }

        @Override
        public PropertyBuilder setType(Type type) {
            instance.type = type;
            return this;
        }

        @Override
        public PropertyBuilder setValue(Object value) {
            instance.value = value;
            return this;
        }
        
        @Override
        public PropertyBuilder setValue(Type type, Object value) {
            instance.type = type;
            instance.value = value;
            return this;
        }

        @Override
        public PropertyBuilder bind(Invokable get, Invokable set) {
            // fake, we ignore this for now until we play even dynamic AAS here
            return this;
        }

        @Override
        public PropertyBuilder setSemanticId(String semanticId) {
            instance.setSemanticId(semanticId);
            return this;
        }
        
        @Override
        public Property build() {
            return parent.register(instance);
        }

        @Override
        public Object getValue() throws ExecutionException {
            return instance.getValue();
        }

        @Override
        public PropertyBuilder setDescription(LangString... description) {
            if (description.length > 0) {
                instance.description = new HashMap<>();
                for (LangString d : description) {
                    instance.description.put(d.getLanguage(), d);
                }
            } else {
                instance.description = null;
            }
            return this;
        }

        @Override
        public PropertyBuilder rbac(AuthenticationDescriptor auth, Role role, RbacAction... actions) {
            // ignore parent paths here
            return AuthenticationDescriptor.elementRbac(this, auth, role, instance.getIdShort(), actions);
        }

    }
    
    /**
     * Creates the instance.
     * 
     * @param idShort the short id.
     */
    protected FakeProperty(String idShort) {
        super(idShort);
    }

    @Override
    public void accept(AasVisitor visitor) {
        visitor.visitProperty(this);
    }

    @Override
    public Object getValue() throws ExecutionException {
        return value;
    }

    @Override
    public void setValue(Object value) throws ExecutionException {
        this.value = value;
    }

    @Override
    public Map<String, LangString> getDescription() {
        return Collections.unmodifiableMap(description);
    }

}
