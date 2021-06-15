/**
 * ******************************************************************************
 * Copyright (c) {2021} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.services.environment;

import static de.iip_ecosphere.platform.services.environment.ServiceMapper.*;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import de.iip_ecosphere.platform.support.aas.InvocablesCreator;
import de.iip_ecosphere.platform.support.iip_aas.AasUtils;
import de.iip_ecosphere.platform.support.iip_aas.Version;
import de.iip_ecosphere.platform.support.iip_aas.json.JsonResultWrapper;

/**
 * Implements the service interface through AAS protocol operations. The operations stored in here can be
 * directly used as suppliers, consumers and functions for an AAS. In addition, this class can be used standalone
 * with an appropriate invocables creator as client stub.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ServiceStub implements Service {

    private Map<String, Supplier<Object>> getters = new HashMap<>();
    private Map<String, Consumer<Object>> setters = new HashMap<>();
    private Map<String, Function<Object[], Object>> operations = new HashMap<>();
    
    /**
     * Creates the setup and registers the operations.
     * 
     * @param iCreator the AAS invocables creator
     * @param serviceId the service id to create the qualified names via {@link ServiceMapper#getQName(Service, String)}
     */
    public ServiceStub(InvocablesCreator iCreator, String serviceId) {
        for (String n : PROP_READONLY) {
            registerProperty(n, iCreator.createGetter(getQName(serviceId, n)), InvocablesCreator.READ_ONLY);
        }
        for (String n : PROP_WRITEONLY) {
            registerProperty(n, InvocablesCreator.WRITE_ONLY, iCreator.createSetter(getQName(serviceId, n)));
        }
        for (String n : PROP_READWRITE) {
            registerProperty(n, iCreator.createGetter(getQName(serviceId, n)), 
                iCreator.createSetter(getQName(serviceId, n)));
        }
        for (String n : OPERATIONS) {
            registerOperation(n, iCreator.createInvocable(getQName(serviceId, n)));
        }
    }
    
    /**
     * Registers the functors for a property.
     * 
     * @param name the (unqualified) name of the property 
     * @param getter the getter functor
     * @param setter the setter functor
     */
    private void registerProperty(String name, Supplier<Object> getter, Consumer<Object> setter) {
        getters.put(name, getter);
        setters.put(name, setter);
    }

    /**
     * Registers the functor for an operation.
     * 
     * @param name the (unqualified) name of the property 
     * @param operation the operation functor
     */
    private void registerOperation(String name, Function<Object[], Object> operation) {
        operations.put(name, operation);
    }

    /**
     * Returns the getter functor for a given property.
     * 
     * @param name the (unqualified) property name
     * @return the functor, may be <b>null</b> for none
     */
    public Supplier<Object> getGetter(String name) {
        return getters.get(name);
    }

    /**
     * Returns the setter functor for a given property.
     * 
     * @param name the (unqualified) property name
     * @return the functor, may be <b>null</b> for none
     */
    public Consumer<Object> getSetter(String name) {
        return setters.get(name);
    }

    /**
     * Returns the functor for a given operation.
     * 
     * @param name the (unqualified) operation name
     * @return the functor, may be <b>null</b> for none
     */
    public Function<Object[], Object> getOperation(String name) {
        return operations.get(name);
    }
    
    @Override
    public String getId() {
        return getters.get(NAME_PROP_ID).get().toString();
    }

    @Override
    public String getName() {
        return getters.get(NAME_PROP_NAME).get().toString();
    }

    @Override
    public Version getVersion() {
        return new Version(getters.get(NAME_PROP_VERSION).get().toString());
    }

    @Override
    public String getDescription() {
        return getters.get(NAME_PROP_DESCRIPTION).get().toString();
    }

    @Override
    public ServiceState getState() {
        return ServiceState.valueOf(getters.get(NAME_PROP_STATE).get().toString());
    }

    @Override
    public boolean isDeployable() {
        return Boolean.valueOf(getters.get(NAME_PROP_DEPLOYABLE).get().toString());
    }

    @Override
    public ServiceKind getKind() {
        return ServiceKind.valueOf(getters.get(NAME_PROP_KIND).get().toString());
    }
    
    @Override
    public void setState(ServiceState state) throws ExecutionException {
        Object[] param = new Object[] {state.name()};
        JsonResultWrapper.fromJson(operations.get(NAME_OP_SET_STATE).apply(param));
    }

    @Override
    public void migrate(String resourceId) throws ExecutionException {
        JsonResultWrapper.fromJson(operations.get(NAME_OP_MIGRATE).apply(new String[] {}));
    }

    @Override
    public void update(URI location) throws ExecutionException {
        Object[] param = new Object[] {location.toString()};
        JsonResultWrapper.fromJson(operations.get(NAME_OP_UPDATE).apply(param));
    }

    @Override
    public void switchTo(String targetId) throws ExecutionException {
        Object[] param = new Object[] {targetId};
        JsonResultWrapper.fromJson(operations.get(NAME_OP_SWITCH).apply(param));
    }

    @Override
    public void activate() throws ExecutionException {
        JsonResultWrapper.fromJson(operations.get(NAME_OP_ACTIVATE).apply(new String[] {}));
    }

    @Override
    public void passivate() throws ExecutionException {
        JsonResultWrapper.fromJson(operations.get(NAME_OP_PASSIVATE).apply(new String[] {}));
    }

    @Override
    public void reconfigure(Map<String, String> values) throws ExecutionException {
        Object[] param = new Object[] {AasUtils.writeMap(values)};
        JsonResultWrapper.fromJson(operations.get(NAME_OP_RECONF).apply(param));
    }

}
