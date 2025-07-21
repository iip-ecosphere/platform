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

import java.io.Serializable;

import org.eclipse.basyx.vab.modelprovider.VABElementProxy;

import de.iip_ecosphere.platform.support.aas.InvocablesCreator;
import de.iip_ecosphere.platform.support.aas.Invokable;
import de.iip_ecosphere.platform.support.aas.Invokable.GetterInvokable;
import de.iip_ecosphere.platform.support.aas.Invokable.OperationInvokable;
import de.iip_ecosphere.platform.support.aas.Invokable.SetterInvokable;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;

/**
 * Implements an abstract invocables creator for the VAB following the naming conventions of 
 * {@link VabOperationsProvider}. Function objects as well as class itself must be serializable for remote deployment.
 * 
 * Although serializable lambda functions appear feasible, we experienced deserialization problems and rely now on
 * explicit functor instances. Failing operation executions throw the occuring (runtime) exception and require catching 
 * them.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class VabInvocablesCreator implements InvocablesCreator, Serializable {

    private static final long serialVersionUID = -4388430468665656598L;
    
    /**
     * Creates the element proxy.
     * 
     * @return the element proxy
     */
    protected abstract VABElementProxy createProxy();

    /**
     * Returns an identifier for the underlying connection, e.g., host + port.
     * 
     * @return the identifier
     */
    protected abstract String getId();

    /**
     * Defines an abstract, generic, serializable functor.
     * 
     * @author Holger Eichelberger, SSE
     */
    protected abstract static class AbstractFunctor implements Serializable {

        protected static final long TIMEOUT = 60 * 1000; // a minute, preliminary
        private static final long serialVersionUID = 4104858388150273917L;
        private static Map<String, Long> failed;
        private VabInvocablesCreator creator;
        private String name;
        
        /**
         * Creates a functor instance.
         * 
         * @param creator the creator instance
         * @param name the name
         */
        protected AbstractFunctor(VabInvocablesCreator creator, String name) {
            this.creator = creator;
            this.name = name;
        }
        
        /**
         * Marks the underlying connection as failed for {@link #TIMEOUT}.
         */
        protected void markAsFailed() {
            if (null == failed) { // lazy due to serialization
                failed = new HashMap<String, Long>();
            }
            failed.put(creator.getId(), System.currentTimeMillis());
        }

        /**
         * Returns whether the underlying connection is ok.
         * 
         * @return {@code true} for ok, {@code false} for currently failing 
         */
        protected boolean isOk() {
            boolean ok = true;
            if (null != failed) { // lazy due to serialization
                String id = creator.getId();
                Long time = failed.get(id);
                if (null != time) {
                    if (System.currentTimeMillis() - time >= TIMEOUT) {
                        failed.remove(id); // implicitly ok, clean up -> link to networkMgr release
                    } else {
                        ok = false; // within timeout, fail
                    }
                }
            }
            return ok;
        }
        
        /**
         * Returns the creator instance.
         * 
         * @return the creator instance
         */
        protected VabInvocablesCreator getCreator() {
            return creator;
        }

        /**
         * Creates the element proxy.
         * 
         * @return the element proxy
         */
        protected VABElementProxy createProxy() {
            return creator.createProxy();
        }
        
        /**
         * Returns the id of the creator.
         * 
         * @return the id
         */
        protected String getId() {
            return creator.getId();
        }

        /**
         * Returns the name of the implementation element.
         * 
         * @return the name
         */
        public String getName() {
            return name;
        }
        
    }

    /**
     * Defines a generic, serializable getter.
     * 
     * @author Holger Eichelberger, SSE
     */
    protected static class Getter extends AbstractFunctor implements GetterInvokable {

        private static final long serialVersionUID = -9126944118648742265L;

        /**
         * Creates a getter instance.
         * 
         * @param creator the creator instance
         * @param name the name
         */
        protected Getter(VabInvocablesCreator creator, String name) {
            super(creator, name);
        }
        
        // checkstyle: stop exception type check
        
        @Override
        public Object get() {
            Object result = null;
            try {
                if (isOk()) {
                    result = createProxy().getValue(VabOperationsProvider.PREFIX_STATUS + getName());
                }
            } catch (Throwable t) {
                markAsFailed();
                LoggerFactory.getLogger(getClass()).info("Getter " + getName() + " on " + getId() 
                    + " failed: " + t.getMessage());
            }
            return result;
        }

        // checkstyle: resume exception type check

    }
    
    /**
     * Defines a generic, serializable setter.
     * 
     * @author Holger Eichelberger, SSE
     */
    protected static class Setter extends AbstractFunctor implements SetterInvokable {

        private static final long serialVersionUID = 2420859918496174956L;

        /**
         * Creates a setter instance.
         * 
         * @param creator the creator instance
         * @param name the name
         */
        protected Setter(VabInvocablesCreator creator, String name) {
            super(creator, name);
        }

        // checkstyle: stop exception type check

        @Override
        public void accept(Object value) {
            try {
                if (isOk()) {
                    createProxy().setValue(VabOperationsProvider.PREFIX_STATUS + getName(), value);
                }
            } catch (Throwable t) {
                markAsFailed();
                LoggerFactory.getLogger(getClass()).info("Setter " + getName() + " on " + getId() 
                    + " failed: " + t.getMessage());
            }
        }

        // checkstyle: resume exception type check

    }

    /**
     * Defines a generic, serializable operation.
     * 
     * @author Holger Eichelberger, SSE
     */
    protected static class Operation extends AbstractFunctor implements OperationInvokable {

        private static final long serialVersionUID = -3021593348876711589L;

        /**
         * Creates an operation instance.
         * 
         * @param creator the creator instance
         * @param name the name
         */
        protected Operation(VabInvocablesCreator creator, String name) {
            super(creator, name);
        }

        // checkstyle: stop exception type check

        @Override
        public Object apply(Object[] params) {
            Object result = null;
            try {
                if (isOk()) {
                    result = createProxy().invokeOperation(VabOperationsProvider.PREFIX_SERVICE + getName(), params);
                } else {
                    throw new RuntimeException("Connection failed before and was blocked until timeout of " 
                        + TIMEOUT + " ms");
                }
            } catch (Throwable t) {
                markAsFailed();
                LoggerFactory.getLogger(getClass()).info("Operation " + getName() + " on " + getId() + " failed: " 
                        + t.getMessage());
                throw t; // shall be caught by caller
            }
            return result;
        }

        // checkstyle: resume exception type check
        
    }
    
    @Override
    public Invokable createGetter(String name) {
        return new Getter(this, name);
    }

    @Override
    public Invokable createSetter(String name) {
        return new Setter(this, name);
    }

    @Override
    public Invokable createInvocable(String name) {
        return new Operation(this, name);
    }

}
