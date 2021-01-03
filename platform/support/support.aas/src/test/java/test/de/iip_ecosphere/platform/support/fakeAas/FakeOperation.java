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

import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import de.iip_ecosphere.platform.support.aas.AasVisitor;
import de.iip_ecosphere.platform.support.aas.Operation;
import de.iip_ecosphere.platform.support.aas.SubmodelElementContainerBuilder;
import de.iip_ecosphere.platform.support.aas.Type;

/**
 * A simple fake operation. No real arguments/parameters, just fake.
 * 
 * @author Holger Eichelberger, SSE
 */
public class FakeOperation extends FakeElement implements Operation {

    private int inArgs;
    private int outArgs;
    private int inOutArgs;
    
    /**
     * A fake operation builder.
     * 
     * @author Holger Eichelberger, SSE
     */
    static class FakeOperationBuilder implements OperationBuilder {

        private FakeSubmodelElementContainerBuilder parent;
        private FakeOperation instance;
        
        /**
         * Creates a builder instance.
         * 
         * @param parent the parent builder
         * @param idShort the short id
         */
        FakeOperationBuilder(FakeSubmodelElementContainerBuilder parent, String idShort) {
            this.parent = parent;
            this.instance = new FakeOperation(idShort, 0, 0, 0);
        }
        
        @Override
        public SubmodelElementContainerBuilder getParentBuilder() {
            return parent;
        }

        @Override
        public OperationBuilder addInputVariable(String name, Type type) {
            instance.inArgs++;
            return this;
        }

        @Override
        public OperationBuilder addOutputVariable(String name, Type type) {
            instance.outArgs++;
            return this;
        }

        @Override
        public OperationBuilder addInOutVariable(String name, Type type) {
            instance.inOutArgs++;
            return this;
        }

        @Override
        public OperationBuilder setInvocable(Function<Object[], Object> invocable) {
            return null; // we ignore this for now until we go even for dynamic AAS here
        }

        @Override
        public Operation build() {
            parent.register(instance);
            return instance;
        }
        
    }
    
    /**
     * Creates a fake operation instance.
     * 
     * @param idShort the short id
     * @param inArgs the number of input args
     * @param outArgs the number of output args
     * @param inOutArgs the nuber of input/output args
     */
    protected FakeOperation(String idShort, int inArgs, int outArgs, int inOutArgs) {
        super(idShort);
    }

    @Override
    public void accept(AasVisitor visitor) {
        visitor.visitOperation(this);
    }

    @Override
    public int getInArgsCount() {
        return inArgs;
    }

    @Override
    public int getOutArgsCount() {
        return outArgs;
    }

    @Override
    public int getInOutArgsCount() {
        return inOutArgs;
    }

    @Override
    public int getArgsCount() {
        return inArgs + outArgs + inOutArgs;
    }

    @Override
    public Object invoke(Object... args) throws ExecutionException {
        return null; //we do not invoke here
    }

}
