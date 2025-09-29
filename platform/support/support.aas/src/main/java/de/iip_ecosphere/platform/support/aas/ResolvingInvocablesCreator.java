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

package de.iip_ecosphere.platform.support.aas;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import de.iip_ecosphere.platform.support.function.IOSupplier;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;

/**
 * A {@link InvocablesCreator} that resolves within a given AAS.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ResolvingInvocablesCreator implements InvocablesCreator {

    private InvocablesCreator origin;
    private Function<String, String> unqualifier;
    private IOSupplier<Aas> aasSupplier;
    private String[] elementPath;
    
    /**
     * Creates an instance.
     * 
     * @param aasSupplier supplies the AAS where to start at
     * @param elementPath supplies the element path within the AAS
     * @param unqualifier a function turning invokable names to unqualified names, may be <b>null</b> for none; 
     *     qualification is replaced here by {@code elementPath}
     */
    public ResolvingInvocablesCreator(InvocablesCreator origin, IOSupplier<Aas> aasSupplier, String[] elementPath, 
         Function<String, String> unqualifier) {
        this.origin = origin;
        this.unqualifier = null == unqualifier ? s -> s : unqualifier;
        this.aasSupplier = aasSupplier;
        this.elementPath = elementPath;
    }
    
    @Override
    public Invokable createGetter(String name) {
        return new PropertyResolvingInvokable(null, unqualifier.apply(name));
    }

    @Override
    public Invokable createSetter(String name) {
        return new PropertyResolvingInvokable(null, unqualifier.apply(name));
    }

    @Override
    public Invokable createInvocable(String name) {
        return new OperationResolvingInvokable(origin.createInvocable(name), unqualifier.apply(name));
    }
    
    /**
     * A BaSyx1 fake invokable.
     * 
     * @author Holger Eichelberger, SSE
     */
    private abstract class AbstractResolvingInvokable implements Invokable {

        private Invokable delegate;
        private String name;
        
        /**
         * Creates a fake invokable.
         * 
         * @param name the name of the property/operation
         */
        protected AbstractResolvingInvokable(Invokable delegate, String name) {
            this.delegate = delegate;
            this.name = name;
        }

        /**
         * Resolves the elements parent via AAS access.
         * 
         * @return the parent or <b>null</b> for none
         */
        protected ElementsAccess resolveParent() {
            AasFactory factory = AasFactory.getInstance();
            ElementsAccess parent = null;
            try {
                Aas aas = aasSupplier.get();
                int pathPos = 0;
                parent = aas.getSubmodel(factory.fixId(elementPath[pathPos++]));
                while (parent != null && pathPos < elementPath.length) {
                    SubmodelElement elt = parent.getSubmodelElement(factory.fixId(elementPath[pathPos++]));
                    parent = elt instanceof ElementsAccess ? (ElementsAccess) elt : null;
                }
            } catch (IOException e) {
                LoggerFactory.getLogger(getClass()).error("While resolving {}: {}", 
                    name, e.getMessage());
            }
            return parent;
        }
        
        /**
         * Returns the name of the property/operation.
         * 
         * @return the name
         */
        protected String getName() {
            return name;
        }
        
        @Override
        public String getUrl() {
            return null == delegate ? null : delegate.getUrl();
        }

        @Override
        public String getSubmodelRepositoryUrl() {
            return null == delegate ? null : delegate.getSubmodelRepositoryUrl();
        }

        @Override
        public void execute(OperationInvocation invocation) throws IOException {
            if (null != delegate) {
                delegate.equals(invocation);
            }
        }        

    }

    
    /**
     * A BaSyx1 fake invokable.
     * 
     * @author Holger Eichelberger, SSE
     */
    private class PropertyResolvingInvokable extends AbstractResolvingInvokable {

        private Property property;
        
        /**
         * Creates a fake invokable.
         * 
         * @param delegate the delegate
         * @param name the name of the property
         */
        private PropertyResolvingInvokable(Invokable delegate, String name) {
            super(delegate, name);
        }
        
        /**
         * Resolves the named property via AAS access.
         * 
         * @return {@code true} if the property was resolved, {@code false} else
         */
        private boolean resolveProperty() {
            if (null == property) {
                ElementsAccess parent = resolveParent();
                if (null != parent) {
                    property = parent.getProperty(getName());
                }
            }
            return property != null;
        }

        @Override
        public Supplier<Object> getGetter() {
            return () -> {
                Object result = null;
                if (resolveProperty()) {
                    try {
                        return property.getValue();
                    } catch (ExecutionException e) {
                        LoggerFactory.getLogger(getClass()).error("While getting value of {}: {}", 
                            getName(), e.getMessage());
                        // result == null
                    }
                }
                return result;
            };
        }

        @Override
        public Consumer<Object> getSetter() {
            return v -> {
                if (resolveProperty()) {
                    try {
                        property.setValue(v);
                    } catch (ExecutionException e) {
                        LoggerFactory.getLogger(getClass()).error("While setting value of {}: {}", 
                            getName(), e.getMessage());
                    }
                }
            };
        }

    }

    /**
     * A BaSyx1 fake invokable.
     * 
     * @author Holger Eichelberger, SSE
     */
    private class OperationResolvingInvokable extends AbstractResolvingInvokable {

        private Operation operation;
        
        /**
         * Creates a fake invokable.
         * 
         * @param delegate the delegate
         * @param name the name of the property
         */
        private OperationResolvingInvokable(Invokable delegate, String name) {
            super(delegate, name);
        }

        /**
         * Resolves the named operation via AAS access.
         * 
         * @return {@code true} if the operation was resolved, {@code false} else
         */
        private boolean resolveOperation() {
            if (null == operation) {
                ElementsAccess parent = resolveParent();
                if (null != parent) {
                    operation = parent.getOperation(getName());
                }
            }
            return operation != null;
        }
        
        @Override
        public Function<Object[], Object> getOperation() {
            return args -> {
                Object result = null;
                if (resolveOperation()) {
                    try {
                        return operation.invoke(args);
                    } catch (ExecutionException e) {
                        LoggerFactory.getLogger(getClass()).error("While invoking {}: {}", 
                            getName(), e.getMessage());
                        // result == null
                    }
                } else {
                    LoggerFactory.getLogger(getClass()).warn("Cannot resolve AAS operation `{}`", getName());
                }
                return result;
            };
        }
    }

}
