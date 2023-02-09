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

package de.iip_ecosphere.platform.support.iip_aas;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.InvocablesCreator;
import de.iip_ecosphere.platform.support.aas.Type;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.Property.PropertyBuilder;
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.iip_aas.config.ServerAddressHolder;
import de.iip_ecosphere.platform.support.iip_aas.json.JsonUtils;
import de.iip_ecosphere.platform.support.net.ManagedServerAddress;
import de.iip_ecosphere.platform.support.net.NetworkManager;
import de.iip_ecosphere.platform.support.net.NetworkManagerFactory;

import static de.iip_ecosphere.platform.support.iip_aas.AasUtils.*;

/**
 * Builds an active AAS for the {@link NetworkManager}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class NetworkManagerAas implements AasContributor {

    public static final String NAME_SUBMODEL = "netMgt";
    public static final String OP_RELEASE_PORT = "releasePort";
    public static final String OP_IS_IN_USE_PORT = "isInUsePortPort";
    public static final String OP_IS_IN_USE_ADR = "isInUsePortAdr";
    public static final String OP_GET_PORT = "getPort";
    public static final String PROP_HIGH_PORT = "highPort";
    public static final String PROP_LOW_PORT = "lowPort";
    public static final String OP_OBTAIN_PORT = "obtainPort";
    public static final String OP_RESERVE_PORT = "reservePort";
    public static final String OP_REGISTER_INSTANCE = "registerInstance";
    public static final String OP_UNREGISTER_INSTANCE = "unregisterInstance";
    public static final String OP_GET_REGISTERED_INSTANCES = "getRegisteredInstances";
    
    @Override
    public Aas contributeTo(AasBuilder aasBuilder, InvocablesCreator iCreator) {
        SubmodelBuilder smB = aasBuilder.createSubmodelBuilder(NAME_SUBMODEL, null);
        if (smB.isNew()) { // incremental remote deployment, avoid double creation
            smB.createOperationBuilder(OP_RESERVE_PORT)
                .addInputVariable("key", Type.STRING)
                .addInputVariable("address", Type.STRING)
                .setInvocable(iCreator.createInvocable(getQName(OP_RESERVE_PORT)))
                .build(Type.STRING);
            smB.createOperationBuilder(OP_OBTAIN_PORT)
                .addInputVariable("key", Type.STRING)
                .setInvocable(iCreator.createInvocable(getQName(OP_OBTAIN_PORT)))
                .build(Type.STRING);
            smB.createOperationBuilder(OP_GET_PORT)
                .addInputVariable("key", Type.STRING)
                .setInvocable(iCreator.createInvocable(getQName(OP_GET_PORT)))
                .build(Type.STRING);
            smB.createOperationBuilder(OP_IS_IN_USE_PORT)
                .addInputVariable("port", Type.INTEGER)
                .setInvocable(iCreator.createInvocable(getQName(OP_IS_IN_USE_PORT)))
                .build(Type.BOOLEAN);
            smB.createOperationBuilder(OP_IS_IN_USE_ADR)
                .addInputVariable("adr", Type.STRING)
                .setInvocable(iCreator.createInvocable(getQName(OP_IS_IN_USE_ADR)))
                .build(Type.BOOLEAN);
            smB.createOperationBuilder(OP_RELEASE_PORT)
                .addInputVariable("key", Type.STRING)
                .setInvocable(iCreator.createInvocable(getQName(OP_RELEASE_PORT)))
                .build(Type.NONE);
            smB.createOperationBuilder(OP_REGISTER_INSTANCE)
                .addInputVariable("key", Type.STRING)
                .addInputVariable("hostId", Type.STRING)
                .setInvocable(iCreator.createInvocable(getQName(OP_REGISTER_INSTANCE)))
                .build(Type.NONE);
            smB.createOperationBuilder(OP_UNREGISTER_INSTANCE)
                .addInputVariable("key", Type.STRING)
                .addInputVariable("hostId", Type.STRING)
                .setInvocable(iCreator.createInvocable(getQName(OP_UNREGISTER_INSTANCE)))
                .build(Type.NONE);
            smB.createOperationBuilder(OP_GET_REGISTERED_INSTANCES)
                .addInputVariable("key", Type.STRING)
                .setInvocable(iCreator.createInvocable(getQName(OP_GET_REGISTERED_INSTANCES)))
                .build(Type.INTEGER);
            smB.createPropertyBuilder(PROP_HIGH_PORT)
                .setType(Type.INTEGER)
                .bind(iCreator.createGetter(getQName(PROP_HIGH_PORT)), PropertyBuilder.READ_ONLY)
                .build();
            smB.createPropertyBuilder(PROP_LOW_PORT)
                .setType(Type.INTEGER)
                .bind(iCreator.createGetter(getQName(PROP_LOW_PORT)), PropertyBuilder.READ_ONLY)
                .build();
            smB.build();
        }
        return null;
    }

    @Override
    public void contributeTo(ProtocolServerBuilder sBuilder) {
        sBuilder.defineOperation(getQName(OP_RESERVE_PORT), 
            p -> toJson(NetworkManagerFactory.getInstance().reservePort(readString(p, 0, null), 
                JsonUtils.serverAddressFromJson(readString(p, 1, null)))));
        sBuilder.defineOperation(getQName(OP_OBTAIN_PORT), 
            p -> toJson(NetworkManagerFactory.getInstance().obtainPort(readString(p, 0, null))));
        sBuilder.defineOperation(getQName(OP_GET_PORT), 
            p -> toJson(NetworkManagerFactory.getInstance().getPort(readString(p, 0, null))));
        sBuilder.defineOperation(getQName(OP_IS_IN_USE_PORT), 
            p -> NetworkManagerFactory.getInstance().isInUse(readInt(p, 0, -1)));
        sBuilder.defineOperation(getQName(OP_IS_IN_USE_ADR), 
            p -> NetworkManagerFactory.getInstance().isInUse(JsonUtils.serverAddressFromJson(readString(p, 0, null))));
        sBuilder.defineOperation(getQName(OP_RELEASE_PORT), 
            p -> { 
                NetworkManagerFactory.getInstance().releasePort(readString(p, 0, null)); 
                return null; 
            });
        sBuilder.defineOperation(getQName(OP_REGISTER_INSTANCE), 
            p -> { 
                NetworkManagerFactory.getInstance().registerInstance(readString(p, 0, null), readString(p, 1, null)); 
                return null;
            });
        sBuilder.defineOperation(getQName(OP_UNREGISTER_INSTANCE), 
            p -> {
                NetworkManagerFactory.getInstance().unregisterInstance(readString(p, 0, null), readString(p, 1, null)); 
                return null;
            });
        sBuilder.defineOperation(getQName(OP_GET_REGISTERED_INSTANCES), 
            p -> NetworkManagerFactory.getInstance().getRegisteredInstances(readString(p, 0, null)));
        sBuilder.defineProperty(getQName(PROP_HIGH_PORT), 
            () -> NetworkManagerFactory.getInstance().getHighPort(), 
            PropertyBuilder.READ_ONLY);
        sBuilder.defineProperty(getQName(PROP_LOW_PORT), 
            () -> NetworkManagerFactory.getInstance().getLowPort(), 
            PropertyBuilder.READ_ONLY);
    }
    
    /**
     * Returns the qualified name for an operation/property implementation.
     * 
     * @param elementName the element name
     * @return the qualified name
     */
    public static String getQName(String elementName) {
        return NAME_SUBMODEL + "_" + elementName;
    }

    /**
     * A proxy for {@link ManagedServerAddress} as we do not want to have setters there.
     * 
     * @author Holger Eichelberger, SSE
     */
    static class ManagedServerAddressHolder extends ServerAddressHolder {
        
        private boolean isNew;

        /**
         * Creates an instance (deserialization).
         */
        ManagedServerAddressHolder() {
        }

        /**
         * Creates an instance from a given instance (serialization).
         * 
         * @param addr the instance to take data from
         */
        ManagedServerAddressHolder(ManagedServerAddress addr) {
            super(addr);
            isNew = addr.isNew();
        }
        
        /**
         * Returns the {@link #isNew} value.
         * 
         * @return the {@link #isNew} value
         */
        public boolean isNew() {
            return isNew;
        }

        /**
         * Defines the {@link #isNew} value.
         * 
         * @param isNew the new value of {@link #isNew}
         */
        public void setNew(boolean isNew) {
            this.isNew = isNew;
        }
        
    }

    /**
     * Reads a {@link ManagedServerAddress} from a JSON string.
     * 
     * @param json the JSON object, usually a String
     * @return the server address or <b>null</b> if reading fails
     */
    public static ManagedServerAddress managedServerAddressFromJson(Object json) {
        ManagedServerAddress result = null;
        if (null != json) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                ManagedServerAddressHolder tmp = objectMapper.readValue(json.toString(), 
                    ManagedServerAddressHolder.class);
                result = new ManagedServerAddress(tmp.getSchema(), tmp.getHost(), tmp.getPort(), tmp.isNew());
            } catch (JsonProcessingException e) {
                //result = null;
            }
        }
        return result; 
    }

    /**
     * Turns a {@link ManagedServerAddress} into JSON.
     * 
     * @param address the address (may be <b>null</b>)
     * @return the JSON string or an empty string in case of problems/no address
     */
    public static String toJson(ManagedServerAddress address) {
        String result = "";
        if (null != address) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                ManagedServerAddressHolder tmp = new ManagedServerAddressHolder(address);
                result = objectMapper.writeValueAsString(tmp);
            } catch (JsonProcessingException e) {
                // handled by default value
            }
        }
        return result;
    }

    @Override
    public Kind getKind() {
        return Kind.ACTIVE;
    }

    @Override
    public boolean isValid() {
        return NetworkManagerFactory.getInstance() != null;
    }

}
