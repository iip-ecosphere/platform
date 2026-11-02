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

import static de.iip_ecosphere.platform.support.aas.AasUtils.*;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Supplier;

import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder;
import de.iip_ecosphere.platform.support.json.JsonResultWrapper;
import de.iip_ecosphere.platform.support.json.JsonResultWrapper.ExceptionFunction;
import de.iip_ecosphere.platform.transport.Transport;
import de.iip_ecosphere.platform.transport.status.TraceRecord;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;

/**
 * Template.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ServiceMapper {
    
    // aligned to Python.ServiceMapper
    public static final String NAME_SUBMODEL = "service";
    public static final String NAME_PROP_ID = "id";
    public static final String NAME_PROP_NAME = "name";
    public static final String NAME_PROP_STATE = "state";
    public static final String NAME_PROP_DEPLOYABLE = "deployable";
    public static final String NAME_PROP_TOPLEVEL = "topLevel";
    public static final String NAME_PROP_KIND = "kind";
    public static final String NAME_PROP_VERSION = "version";
    public static final String NAME_PROP_DESCRIPTION = "description";
    public static final String NAME_PROP_ARTIFACT = "artifact";
    public static final String NAME_OP_ACTIVATE = "activate";
    public static final String NAME_OP_PASSIVATE = "passivate";
    public static final String NAME_OP_MIGRATE = "migrate";
    public static final String NAME_OP_UPDATE = "update";
    public static final String NAME_OP_SWITCH = "switchTo";
    public static final String NAME_OP_RECONF = "reconfigure";
    public static final String NAME_OP_SET_STATE = "setState";
    public static final String NAME_OP_GET_STATE = "getState";
    
    public static final String[] PROP_READONLY = {NAME_PROP_ID, NAME_PROP_NAME, NAME_PROP_DEPLOYABLE, 
        NAME_PROP_TOPLEVEL, NAME_PROP_KIND, NAME_PROP_VERSION, NAME_PROP_DESCRIPTION, NAME_PROP_ARTIFACT, 
        NAME_PROP_STATE}; 
    public static final String[] PROP_WRITEONLY = {}; 
    public static final String[] PROP_READWRITE = {}; 
    public static final String[] OPERATIONS = {NAME_OP_ACTIVATE, NAME_OP_PASSIVATE, NAME_OP_MIGRATE, 
        NAME_OP_UPDATE, NAME_OP_SWITCH, NAME_OP_RECONF, NAME_OP_SET_STATE, NAME_OP_GET_STATE}; 
    
    private ProtocolServerBuilder builder;
    
    /**
     * Creates a service builder instance.
     * 
     * @param builder the builder
     */
    public ServiceMapper(ProtocolServerBuilder builder) {
        this.builder = builder;
    }

    /**
     * Maps the given service onto the protocol service builder. Registers the service 
     * calling {@link Starter#setService(Service)}. [testing]
     * 
     * @param service the service to define
     */
    public void mapService(Service service) { // move to testing
        mapService(service.getId());
        Starter.setService(service);
    }

    /**
     * Defines an AAS property with supplier, obtaining the service from {@code Starter} at runtime. Suppliers are
     * not used in AAS v3 anymore, just for AAS v2.
     * 
     * @param serviceId the service ID to create the property for
     * @param name the name of the property
     * @param supplierCreator the supplier creator, receiving a service instance supplier as context, delivering the 
     *     actual property supplier eventually using the service instance supplier
     */
    private void defineProperty(String serviceId, String name, 
        Function<Supplier<Service>, Supplier<Object>> supplierCreator) {
        builder.defineProperty(getQName(serviceId, name), 
            supplierCreator.apply(() -> Starter.getMappedServiceSafe(serviceId)), null); 
    }

    /**
     * Defines an AAS operation through a function, obtaining the service from {@code Starter} at runtime. 
     * 
     * @param serviceId the service ID to create the property for
     * @param name the name of the operation
     * @param functionCreator the function creator, receiving a service instance supplier as context, delivering the 
     *     actual function eventually using the service instance supplier
     */
    private void defineOperation(String serviceId, String name, 
        Function<Supplier<Service>, ExceptionFunction> functionCreator) {
        ExceptionFunction func = functionCreator.apply(() -> Starter.getMappedServiceSafe(serviceId));
        builder.defineOperation(getQName(serviceId, name), 
            new JsonResultWrapper(p -> func.apply(p))
        );
    }

    /**
     * Maps the given service onto the protocol service builder. Does not register the service in {@link Starter}.
     * 
     * @param serviceId the id of the service to define
     */
    public void mapService(String serviceId) {
        try {
            defineProperty(serviceId, NAME_PROP_ID, s -> () -> s.get().getId());
            defineProperty(serviceId, NAME_PROP_DESCRIPTION, s -> () -> s.get().getDescription());
            defineProperty(serviceId, NAME_PROP_ARTIFACT, s -> () -> s.get().getArtifact());
            defineProperty(serviceId, NAME_PROP_VERSION, s -> () -> s.get().getVersion().toString());
            defineProperty(serviceId, NAME_PROP_KIND, s -> () -> s.get().getKind().toString());
            defineProperty(serviceId, NAME_PROP_STATE, s -> () -> s.get().getState().toString());
            defineProperty(serviceId, NAME_PROP_NAME, s -> () -> s.get().getName());
            defineProperty(serviceId, NAME_PROP_DEPLOYABLE, s -> () -> s.get().isDeployable());
            defineProperty(serviceId, NAME_PROP_TOPLEVEL, s -> () -> s.get().isTopLevel());

            defineOperation(serviceId, NAME_OP_ACTIVATE, s -> p -> {
                s.get().activate(); 
                return null; 
            });
            defineOperation(serviceId, NAME_OP_PASSIVATE, s -> p -> {
                s.get().passivate(); 
                return null; 
            });
            defineOperation(serviceId, NAME_OP_MIGRATE, s -> p -> {
                s.get().migrate(readString(p)); 
                return null; 
            });
            defineOperation(serviceId, NAME_OP_RECONF, s -> p -> {
                Map<String, String> values = readMap(p, 0, null);
                Service service = s.get();
                Transport.sendTraceRecord(new TraceRecord(service.getId(), "reconfigure", values)); // disable?
                service.reconfigure(values); 
                return null;
            });
            defineOperation(serviceId, NAME_OP_GET_STATE, s -> p -> {
                return s.get().getState().toString();
            });
            defineOperation(serviceId, NAME_OP_SET_STATE, s -> p -> {
                ServiceState state = ServiceState.valueOf(readString(p, 0, "")); // exception -> wrapper
                Service service = s.get();
                service.setState(state);
                LoggerFactory.getLogger(ServiceMapper.class).info("Setting state " + state 
                    + " on " + service.getId() + " -> " + service.getState());
                return null;
            });
            defineOperation(serviceId, NAME_OP_SWITCH, s -> p -> {
                s.get().switchTo(readString(p));
                return null;
            });
            defineOperation(serviceId, NAME_OP_UPDATE, s -> p -> {
                updateService(s.get(), readUri(p, 0, EMPTY_URI));
                return null;
            });
        } catch (IllegalArgumentException e) {
            LoggerFactory.getLogger(ServiceMapper.class).error("Cannot map/register service: " + e.getMessage());
        }
    }
    
    /**
     * Updates a service including calling its update handler if known.
     * 
     * @param service the service to update
     * @see Starter#getUpdateHandler(String)
     */
    static void updateService(Service service, URI location) throws ExecutionException {
        service.update(location);
        Supplier<? extends Service> updateHandler = Starter.getUpdateHandler(service.getId());
        if (null != updateHandler) {
            Service updatedService = updateHandler.get();
            if (null != updatedService && updatedService != service) {
                updatedService.transferState(service);
                Starter.setService(updatedService);
                LoggerFactory.getLogger(ServiceMapper.class).info("Updated service '{}'", service.getId());
            }
        }
    }
    
    /**
     * Returns the qualified name for an operation/property implementation.
     * 
     * @param service the service to prefix
     * @param elementName the element name
     * @return the qualified name
     */
    public static String getQName(Service service, String elementName) {
        return getQName(Starter.getServiceId(service), elementName);
    }

    /**
     * Returns the qualified name for an operation/property implementation.
     * 
     * @param serviceId the service id to prefix
     * @param elementName the element name
     * @return the qualified name
     */
    public static String getQName(String serviceId, String elementName) {
        return NAME_SUBMODEL + "_" + serviceId + "_" + elementName;
    }
    
    /**
     * Unqualifies a qualified name.
     * 
     * @param name the name
     * @return the unqualified name
     */
    public static String unqualify(String name) {
        int pos = name.lastIndexOf("_");
        if (pos > 0) {
            name = name.substring(pos + 1);
        }
        return name;
    }

}
