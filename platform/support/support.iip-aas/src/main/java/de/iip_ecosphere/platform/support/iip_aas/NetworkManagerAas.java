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

import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.InvocablesCreator;
import de.iip_ecosphere.platform.support.aas.Type;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.Property.PropertyBuilder;
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.net.ManagedServerAddress;
import de.iip_ecosphere.platform.support.net.NetworkManager;
import de.iip_ecosphere.platform.support.net.NetworkManagerFactory;

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
    
    @Override
    public Aas contributeTo(AasBuilder aasBuilder, InvocablesCreator iCreator) {
        SubmodelBuilder smB = aasBuilder.createSubmodelBuilder(NAME_SUBMODEL, null);
        smB.createOperationBuilder(OP_RESERVE_PORT)
            .addInputVariable("key", Type.STRING)
            .addInputVariable("address", Type.STRING)
            .addOutputVariable("result", Type.STRING)
            .setInvocable(iCreator.createInvocable(getQName(OP_RESERVE_PORT)))
            .build();
        smB.createOperationBuilder(OP_OBTAIN_PORT)
            .addInputVariable("key", Type.STRING)
            .addOutputVariable("result", Type.STRING)
            .setInvocable(iCreator.createInvocable(getQName(OP_OBTAIN_PORT)))
            .build();
        smB.createOperationBuilder(OP_GET_PORT)
            .addInputVariable("key", Type.STRING)
            .addOutputVariable("result", Type.STRING)
            .setInvocable(iCreator.createInvocable(getQName(OP_GET_PORT)))
            .build();
        smB.createOperationBuilder(OP_IS_IN_USE_PORT)
            .addInputVariable("port", Type.INTEGER)
            .addOutputVariable("result", Type.BOOLEAN)
            .setInvocable(iCreator.createInvocable(getQName(OP_IS_IN_USE_PORT)))
            .build();
        smB.createOperationBuilder(OP_IS_IN_USE_ADR)
            .addInputVariable("adr", Type.STRING)
            .addOutputVariable("result", Type.BOOLEAN)
            .setInvocable(iCreator.createInvocable(getQName(OP_IS_IN_USE_ADR)))
            .build();
        smB.createOperationBuilder(OP_RELEASE_PORT)
            .addInputVariable("key", Type.STRING)
            .setInvocable(iCreator.createInvocable(getQName(OP_RELEASE_PORT)))
            .build();
        smB.createPropertyBuilder(PROP_HIGH_PORT)
            .setType(Type.INTEGER)
            .bind(iCreator.createGetter(getQName(PROP_HIGH_PORT)), PropertyBuilder.READ_ONLY)
            .build();
        smB.createPropertyBuilder(PROP_LOW_PORT)
            .setType(Type.INTEGER)
            .bind(iCreator.createGetter(getQName(PROP_LOW_PORT)), PropertyBuilder.READ_ONLY)
            .build();
        smB.build();
        return null;
    }

    @Override
    public void contributeTo(ProtocolServerBuilder sBuilder) {
        sBuilder.defineOperation(getQName(OP_RESERVE_PORT), 
            p -> toJson(NetworkManagerFactory.getInstance().reservePort(readString(p, 0, null), 
                readServerAddress(readString(p, 1, null)))));
        sBuilder.defineOperation(getQName(OP_OBTAIN_PORT), 
            p -> toJson(NetworkManagerFactory.getInstance().obtainPort(readString(p, 0, null))));
        sBuilder.defineOperation(getQName(OP_GET_PORT), 
            p -> toJson(NetworkManagerFactory.getInstance().getPort(readString(p, 0, null))));
        sBuilder.defineOperation(getQName(OP_IS_IN_USE_PORT), 
            p -> NetworkManagerFactory.getInstance().isInUse(readInt(p, 0, -1)));
        sBuilder.defineOperation(getQName(OP_IS_IN_USE_ADR), 
            p -> NetworkManagerFactory.getInstance().isInUse(readServerAddress(readString(p, 0, null))));
        sBuilder.defineOperation(getQName(OP_RELEASE_PORT), 
            p -> { NetworkManagerFactory.getInstance().releasePort(readString(p, 0, null)); return null; });
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
     * Reads the {@code index} argument from {@code} args as String.
     * 
     * @param args the array to take the value from 
     * @param index the 0-based index into {@code} args
     * @param dflt default value if the {@code index} is wrong, there is no value/null ...
     * @return the value
     */
    public static String readString(Object[] args, int index, String dflt) {
        Object param = index >= 0 && index < args.length ? args[index] : null;
        return null == param ? dflt : param.toString();
    }
    
    /**
     * Reads the {@code index} argument from {@code} args as int.
     * 
     * @param args the array to take the value from 
     * @param index the 0-based index into {@code} args
     * @param dflt default value if the {@code index} is wrong, there is no value/null, the value is no int...
     * @return the value
     */
    public static int readInt(Object[] args, int index, int dflt) {
        Object param = index >= 0 && index < args.length ? args[index] : null;
        int result = dflt;
        if (null != param) {
            try {
                result = Integer.parseInt(param.toString());
            } catch (NumberFormatException e) {
                // handled by result = deflt
            }
        }
        return result;
    }

    /**
     * A proxy for {@link ServerAddress} as we do not want to have setters there.
     * 
     * @author Holger Eichelberger, SSE
     */
    static class ServerAddressHolder {
        private int port;
        private String host;
        private Schema schema;

        /**
         * Creates an instance (deserialization).
         */
        ServerAddressHolder() {
        }

        /**
         * Creates an instance from a given instance (serialization).
         * 
         * @param addr the instance to take data from
         */
        ServerAddressHolder(ServerAddress addr) {
            port = addr.getPort();
            host = addr.getHost();
            schema = addr.getSchema();
        }
        
        /**
         * Returns the port value.
         * 
         * @return the port
         */
        public int getPort() {
            return port;
        }
        
        /**
         * Defines the {@link #port} value.
         * 
         * @param port the new value of {@link #port}
         */
        public void setPort(int port) {
            this.port = port;
        }
        
        /**
         * Returns the host value.
         * 
         * @return the host
         */
        public String getHost() {
            return host;
        }
        
        /**
         * Defines the {@link #host} value.
         * 
         * @param host the new value of {@link #host}
         */
        public void setHost(String host) {
            this.host = host;
        }
        
        /**
         * Returns the schema value.
         * 
         * @return the schema
         */
        public Schema getSchema() {
            return schema;
        }

        /**
         * Defines the {@link #schema} value.
         * 
         * @param schema the new value of {@link #schema}
         */
        public void setSchema(Schema schema) {
            this.schema = schema;
        }
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
     * Reads a {@link ServerAddress} from a JSON string.
     * 
     * @param json the JSON string
     * @return the server address or <b>null</b> if reading fails
     */
    public static ServerAddress readServerAddress(String json) {
        ServerAddress result = null;
        if (null != json) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                ServerAddressHolder tmp = objectMapper.readValue(json, ServerAddressHolder.class);
                result = new ServerAddress(tmp.getSchema(), tmp.getHost(), tmp.getPort());
            } catch (JsonProcessingException e) {
                // result = null;
            }
        }
        return result;        
    }
    
    /**
     * Turns a {@link ServerAddress} into JSON.
     * 
     * @param address the address (may be <b>null</b>)
     * @return the JSON string or an empty string in case of problems/no address
     */
    public static String toJson(ServerAddress address) {
        String result = "";
        if (null != address) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                ServerAddressHolder tmp = new ServerAddressHolder(address);
                result = objectMapper.writeValueAsString(tmp);
            } catch (JsonProcessingException e) {
                // handled by default value
            }
        } 
        return result;
    }

    /**
     * Reads a {@link ManagedServerAddress} from a JSON string.
     * 
     * @param json the JSON string
     * @return the server address or <b>null</b> if reading fails
     */
    public static ManagedServerAddress readManagedServerAddress(String json) {
        ManagedServerAddress result = null;
        if (null != json) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                ManagedServerAddressHolder tmp = objectMapper.readValue(json, ManagedServerAddressHolder.class);
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

}
