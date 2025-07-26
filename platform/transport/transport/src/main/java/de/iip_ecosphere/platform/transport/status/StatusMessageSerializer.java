/**
 * ******************************************************************************
 * Copyright (c) {2022} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.transport.status;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import de.iip_ecosphere.platform.support.json.JsonUtils.EnumDeserializer;

import de.iip_ecosphere.platform.transport.serialization.GenericJsonToStringTranslator;
import de.iip_ecosphere.platform.transport.serialization.Serializer;
import de.iip_ecosphere.platform.transport.serialization.TypeTranslator;

/**
 * A simple, generic status serializer. Additional enum constants must be registered here.
 * 
 * @author Holger Eichelberger, SSE
 */
public class StatusMessageSerializer implements Serializer<StatusMessage> {

    private static final Map<String, ActionType> ACTION_CONSTANTS = new HashMap<>();
    private static final Map<String, ComponentType> COMPONENT_CONSTANTS = new HashMap<>();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Creates an object mapper instances.
     * 
     * @return the mapper
     */
    static  {
        registerActions(ActionTypes.class);
        registerComponents(ComponentTypes.class);
        
        SimpleModule module = new SimpleModule();
        // fill module
        module.addDeserializer(ActionType.class, 
            new EnumDeserializer<ActionType>(ACTION_CONSTANTS, ActionType.class));
        module.addDeserializer(ComponentType.class, 
            new EnumDeserializer<ComponentType>(COMPONENT_CONSTANTS, ComponentType.class));
        
        MAPPER.registerModule(module);
    }
    
    /**
     * Creates a type translator based on the serialization approach in this class.
     * 
     * @return the type translator
     */
    public static TypeTranslator<StatusMessage, String> createTypeTranslator() {
        return new GenericJsonToStringTranslator<StatusMessage>(StatusMessage.class, MAPPER);
    }
    
    /**
     * Registers custom action types. Already registered types will not be overridden. Registration
     * follows enum constant conventions, i.e., only static final public values of the given type
     * declared in that type will be considered.
     * 
     * @param type the type to register
     */
    public static void registerActions(Class<? extends ActionType> type) {
        register(type, ACTION_CONSTANTS);
    }

    /**
     * Registers custom component types. Already registered types will not be overridden. Registration
     * follows enum constant conventions, i.e., only static final public values of the given type
     * declared in that type will be considered.
     * 
     * @param type the type to register
     */
    public static void registerComponents(Class<? extends ComponentType> type) {
        register(type, COMPONENT_CONSTANTS);
    }
    
    /**
     * Does the registration of {@code cls} as enum constant and modifies {@code mapping} as side effect.
     * 
     * @param <T> the "enum" type
     * @param cls the type
     * @param mapping the mapping to be modified as side effect
     */
    @SuppressWarnings("unchecked")
    private static <T> void register(Class<? extends T> cls, Map<String, T> mapping) {
        for (Field f : cls.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isStatic(mod) && Modifier.isPublic(mod) && Modifier.isFinal(mod)) {
                if (f.getType() == cls && !mapping.containsKey(f.getName())) {
                    try {
                        mapping.put(f.getName(), (T) f.get(null));
                    } catch (IllegalAccessException e) {
                    }
                }
            }
        }
    }

    @Override
    public StatusMessage from(byte[] data) throws IOException {
        return MAPPER.readValue(data, StatusMessage.class);
    }

    @Override
    public byte[] to(StatusMessage source) throws IOException {
        return MAPPER.writeValueAsBytes(source);
    }

    @Override
    public StatusMessage clone(StatusMessage origin) throws IOException {
        return new StatusMessage(origin.getComponentType(), origin.getAction(), origin.getId(), origin.getDeviceId(),
            origin.getAliasIds());
    }

    @Override
    public Class<StatusMessage> getType() {
        return StatusMessage.class;
    }

}
