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

    /**
     * Converts a getter result.
     * 
     * @param <T> the target type of the value
     * @param getId the getter ID
     * @param dflt the default value to return if a conversion is not possible, e.g., response is <b>null</b>
     * @param conversion the conversion operation
     * @return the converted value or {@code dflt}
     */
    private <T> T convertGetterResult(String getId, T dflt, Function<Object, T> conversion) {
        T result;
        Object response = getters.get(getId).get();
        if (null == response) {
            result = dflt;
        } else {
            result = conversion.apply(response);
        }
        return result;
    }
    
    /**
     * Returns the result of the specified getter converted to string with default value "".
     * 
     * @param getId the getter ID
     * @return the converted value
     * @see #convertGetterResult(String, Object, Function)
     */
    private String convertGetterResultToString(String getId) {
        return convertGetterResult(getId, "", r -> r.toString());
    }
    
    /**
     * Converts an object {@code val} to an enum value without throwing exceptions.
     * 
     * @param <T> the enum value type
     * @param val the value to be converted, may be <b>null</b>, a non-matching string, etc.
     * @param dflt the default value if the conversion cannot be applied
     * @param cls the enum type
     * @return the converted value or {@code dflt}
     */
    public static <T extends Enum<T>> T convertToEnumSafe(Object val, T dflt, Class<T> cls) {
        T result;
        try {
            result = Enum.valueOf(cls, val.toString());
        } catch (NullPointerException | IllegalArgumentException e) { // official exceptions
            result = dflt;
        }
        return result;
    }

    /**
     * Returns the result of the specified getter converted to the given enum type.
     * 
     * @param <T> the enum value type
     * @param getId the getter ID
     * @param dflt the default value if the conversion fails
     * @param cls the enum type
     * @return the converted value or {@code dflt}
     * @see #convertGetterResult(String, Object, Function)
     * @see #convertToEnumSafe(Object, Enum, Class)
     */
    private <T extends Enum<T>> T convertGetterResultToEnum(String getId, T dflt, Class<T> cls) {
        return convertGetterResult(getId, dflt, r -> convertToEnumSafe(r, dflt, cls));
    }
    
    @Override
    public String getId() {
        return convertGetterResultToString(NAME_PROP_ID);
    }

    @Override
    public String getName() {
        return convertGetterResultToString(NAME_PROP_NAME);
    }

    @Override
    public Version getVersion() {
        return convertGetterResult(NAME_PROP_VERSION, null, r -> new Version(r.toString()));
    }

    @Override
    public String getDescription() {
        return convertGetterResultToString(NAME_PROP_DESCRIPTION);
    }

    @Override
    public ServiceState getState() {
        return convertGetterResultToEnum(NAME_PROP_STATE, null, ServiceState.class);
    }

    @Override
    public boolean isDeployable() {
        return convertGetterResult(NAME_PROP_DEPLOYABLE, false, r -> Boolean.valueOf(r.toString()));
    }

    @Override
    public ServiceKind getKind() {
        return convertGetterResultToEnum(NAME_PROP_KIND, null, ServiceKind.class);
    }
    
    @Override
    public void setState(ServiceState state) throws ExecutionException {
        Object[] param = new Object[] {state.name()};
        JsonResultWrapper.fromJson(operations.get(NAME_OP_SET_STATE), param);
    }

    @Override
    public void migrate(String resourceId) throws ExecutionException {
        JsonResultWrapper.fromJson(operations.get(NAME_OP_MIGRATE));
    }

    @Override
    public void update(URI location) throws ExecutionException {
        Object[] param = new Object[] {location.toString()};
        JsonResultWrapper.fromJson(operations.get(NAME_OP_UPDATE), param);
    }

    @Override
    public void switchTo(String targetId) throws ExecutionException {
        Object[] param = new Object[] {targetId};
        JsonResultWrapper.fromJson(operations.get(NAME_OP_SWITCH), param);
    }

    @Override
    public void activate() throws ExecutionException {
        JsonResultWrapper.fromJson(operations.get(NAME_OP_ACTIVATE));
    }

    @Override
    public void passivate() throws ExecutionException {
        JsonResultWrapper.fromJson(operations.get(NAME_OP_PASSIVATE));
    }

    @Override
    public void reconfigure(Map<String, String> values) throws ExecutionException {
        Object[] param = new Object[] {AasUtils.writeMap(values)};
        JsonResultWrapper.fromJson(operations.get(NAME_OP_RECONF), param);
    }

}
