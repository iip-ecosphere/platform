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

import static de.iip_ecosphere.platform.support.iip_aas.AasUtils.*;

import java.util.Map;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder;
import de.iip_ecosphere.platform.support.iip_aas.json.JsonResultWrapper;
import de.iip_ecosphere.platform.transport.Transport;
import de.iip_ecosphere.platform.transport.status.TraceRecord;

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
    public static final String NAME_PROP_KIND = "kind";
    public static final String NAME_PROP_VERSION = "version";
    public static final String NAME_PROP_DESCRIPTION = "description";
    public static final String NAME_OP_ACTIVATE = "activate";
    public static final String NAME_OP_PASSIVATE = "passivate";
    public static final String NAME_OP_MIGRATE = "migrate";
    public static final String NAME_OP_UPDATE = "update";
    public static final String NAME_OP_SWITCH = "switchTo";
    public static final String NAME_OP_RECONF = "reconfigure";
    public static final String NAME_OP_SET_STATE = "setState";
    
    public static final String[] PROP_READONLY = {NAME_PROP_ID, NAME_PROP_NAME, NAME_PROP_STATE, NAME_PROP_DEPLOYABLE, 
        NAME_PROP_KIND, NAME_PROP_VERSION, NAME_PROP_DESCRIPTION}; 
    public static final String[] PROP_WRITEONLY = {}; 
    public static final String[] PROP_READWRITE = {}; 
    public static final String[] OPERATIONS = {NAME_OP_ACTIVATE, NAME_OP_PASSIVATE, NAME_OP_MIGRATE, NAME_OP_UPDATE, 
        NAME_OP_SWITCH, NAME_OP_RECONF, NAME_OP_SET_STATE}; 
    
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
     * Maps the given service onto the protocol service builder.
     * 
     * @param service the service to define
     */
    public void mapService(Service service) {
        try {
            builder.defineProperty(getQName(service, NAME_PROP_ID), 
                () -> service.getId(), null);
            builder.defineProperty(getQName(service, NAME_PROP_DESCRIPTION), 
                () -> service.getDescription(), null);
            builder.defineProperty(getQName(service, NAME_PROP_VERSION), 
                () -> service.getVersion().toString(), null);
            builder.defineProperty(getQName(service, NAME_PROP_KIND), 
                () -> service.getKind().toString(), null);
            builder.defineProperty(getQName(service, NAME_PROP_STATE), 
                () -> service.getState().toString(), null);
            builder.defineProperty(getQName(service, NAME_PROP_NAME), 
                () -> service.getName(), null);
            builder.defineProperty(getQName(service, NAME_PROP_DEPLOYABLE), 
                () -> service.isDeployable(), null);
            builder.defineOperation(getQName(service, NAME_OP_ACTIVATE), 
                new JsonResultWrapper(p -> {
                    service.activate(); 
                    return null;
                }
            ));
            builder.defineOperation(getQName(service, NAME_OP_PASSIVATE), 
                new JsonResultWrapper(p -> {
                    service.passivate(); 
                    return null;
                }
            ));
            builder.defineOperation(getQName(service, NAME_OP_MIGRATE), 
                new JsonResultWrapper(p -> {
                    service.migrate(readString(p)); 
                    return null;
                }
            ));
            builder.defineOperation(getQName(service, NAME_OP_RECONF), 
                new JsonResultWrapper(p -> {
                    Map<String, String> values = readMap(p, 0, null);
                    Transport.sendTraceRecord(new TraceRecord(service.getId(), "reconfigure", values)); // disable?
                    service.reconfigure(values); 
                    return null;
                }
            ));
            builder.defineOperation(getQName(service, NAME_OP_SET_STATE), 
                new JsonResultWrapper(p -> {
                    ServiceState state = ServiceState.valueOf(readString(p, 0, "")); // exception -> wrapper
                    service.setState(state);
                    return null;
                }
            ));
            builder.defineOperation(getQName(service, NAME_OP_SWITCH), 
                new JsonResultWrapper(p -> {
                    service.switchTo(readString(p));
                    return null;
                }
            ));
            builder.defineOperation(getQName(service, NAME_OP_UPDATE), 
                new JsonResultWrapper(p -> {
                    service.update(readUri(p, 0, EMPTY_URI));
                    return null;
                }
            ));
        } catch (IllegalArgumentException e) {
            LoggerFactory.getLogger(ServiceMapper.class).error("Cannot map/register service: " + e.getMessage());
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
        return getQName(service.getId(), elementName);
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

}
