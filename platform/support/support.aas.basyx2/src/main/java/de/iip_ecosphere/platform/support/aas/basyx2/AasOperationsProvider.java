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

package de.iip_ecosphere.platform.support.aas.basyx2;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;

import de.iip_ecosphere.platform.support.NetUtils;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.aas.JsonPayloadCodec;
import de.iip_ecosphere.platform.support.aas.OperationsProvider;
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder;
import de.iip_ecosphere.platform.support.aas.SetupSpec;
import de.iip_ecosphere.platform.support.aas.SetupSpec.AasComponent;
import de.iip_ecosphere.platform.support.aas.basyx2.apps.asset.AssetSpringApp;

/**
 * Implements a simple operations provider still following a simple status/operations/service structure. The idea is to 
 * attach the relevant operations of an implementing object to
 * this provider, whereby the implementing object is actually providing the functionality and this provider
 * just acts as an intermediary. For this purpose, you can register function objects for operations and 
 * supplier/consumer objects of the implementing object in the provider. The provider then offers the respective
 * access under 
 * <ul>
 *  <li>{@value #STATUS}/<i>propertyName</i></li>
 *  <li>{@value #OPERATIONS}/{@value #SERVICE}/<i>operationName</i></li>
 * </ul>
 * while "{@value #SERVICE}" is just seen as a category of many potential operation categories that you can register.
 * Use "/" as path separator, which is available as {@link #SEPARATOR constant}.
 * The respective invocables must follow this naming schema for access.
 * @author Holger Eichelberger, SSE
 */
public class AasOperationsProvider extends HashMap<String, Object> implements OperationsProvider {

    /**
     * The path separator.
     */
    public static final String SEPARATOR =  "/";

    /**
     * The path prefix for status/properties.
     */
    public static final String STATUS = "status";

    /**
     * A convenient combination of {@link #STATUS} + {@link #SEPARATOR}.
     */
    public static final String PREFIX_STATUS = STATUS + SEPARATOR;

    /**
     * The path prefix for operations.
     */
    public static final String OPERATIONS = "operations";

    /**
     * A convenient combination of {@link #OPERATIONS} + {@link #SEPARATOR}.
     */
    public static final String PREFIX_OPERATIONS = OPERATIONS + SEPARATOR;

    /**
     * The operations category sub-path for operations.
     */
    public static final String SERVICE = "service";

    /**
     * A convenient combination of {@link #OPERATIONS} + {@link #SEPARATOR} + {@link #SEPARATOR} + {@link #SERVICE} .
     */
    public static final String PREFIX_SERVICE = OPERATIONS + SEPARATOR + SERVICE + SEPARATOR;

    private static final long serialVersionUID = 6355197555283292724L;

    // maps for the path entries; entries uniquely map to either properties or operationFunctions, however
    // without referencing them as otherwise the contained objects would be subject to serialization/transfer by the VAB
    private Map<String, Entry> status = new HashMap<>(); 
    private Map<String, Map<String, Entry>> operations = new HashMap<>(); 
    private Map<String, Entry> service = new HashMap<>(); 
    
    private Map<String, Property> properties = new HashMap<>();
    private Map<String, Function<Object[], Object>> operationFunctions = new HashMap<>();

    /**
     * The main kinds of entries.
     * 
     * @author Holger Eichelberger, SSE
     */
    private enum Kind {
        
        /**
         * 'Refers' to properties.
         */
        PROPERTY,

        /**
         * 'Refers' to operations.
         */
        OPERATION
    }

    /**
     * An entry as last element of a potentially hierarchically nested access path. An entry
     * can refer to an operation or to a property, each with unique names within their kind.
     * As {@link Entry} belongs to VAB paths, {@link Entry} must be serializable and consist of 
     * serializable attributes.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class Entry implements Serializable {

        private static final long serialVersionUID = 3003478232778729891L;
        private Kind kind;
        private String uName;

        /**
         * Creates an entry instance.
         * 
         * @param kind the entry kind
         * @param uName the unique name of the operation/property within {@code kind}
         */
        private Entry(Kind kind, String uName) {
            this.kind = kind;
            this.uName = uName;
        }
        
    }
    
    /**
     * Represents a property consisting of a consumer and a supplier function. Both functions
     * may map to attributes or accessor functions depending on the implementing objet.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class Property {

        private Consumer<Object> set;
        private Supplier<Object> get;
        
        /**
         * Creates a property instance. Theoretically, either entry may be <b>null</b> for read-only/write-only
         * properties, but this must be, however, reflected in the AAS so that no wrong can access happens.
         * 
         * @param get the supplier providing read access to the property value (may be <b>null</b>)
         * @param set the consumer providing write access to the property value (may be <b>null</b>)
         */
        private Property(Supplier<Object> get, Consumer<Object> set) {
            this.set = set;
            this.get = get;
        }
    }
    
    /**
     * The protocol server builder for this provider.
     * 
     * @author Holger Eichelberger, SSE
     */
    static class AasRestOperationsBuilder implements ProtocolServerBuilder {

        private SetupSpec spec;
        private AasOperationsProvider instance;
        
        /**
         * Creates a builder instance.
         * 
         * @param spec the setup specification
         */
        AasRestOperationsBuilder(SetupSpec spec) {
            this.spec = spec;
            this.instance = new AasOperationsProvider();
        }
        
        @Override
        public AasRestOperationsBuilder defineOperation(String name, Function<Object[], Object> function) {
            instance.defineServiceFunction(name, function);
            return this;
        }

        @Override
        public AasRestOperationsBuilder defineProperty(String name, Supplier<Object> get, Consumer<Object> set) {
            instance.defineProperty(name, get, set);
            return this;
        }
        
        @Override
        public Server build() {
            Server result = new Server() {

                private ConfigurableApplicationContext ctx;
                
                @Override
                public Server start() {
                    int port = spec.getAssetServerAddress().getPort();
                    if (BaSyxAbstractAasServer.shallStart(spec.getAssetServerState())) {
                        System.out.println("Starting AAS-REST server on " + port);
                        ctx = BaSyxAbstractAasServer.createContext(AssetSpringApp.class, port, 
                            BaSyxAbstractAasServer.createConfigurer(spec.getSetup(AasComponent.ASSET))
                                .addBeanRegistrationInitializer(AasOperationsProvider.class, instance), 
                            s -> spec.notifyAssetServerStateChange(s));
                    }
                    return this;
                }

                @Override
                public void stop(boolean dispose) {
                    BaSyxAbstractAasServer.close(ctx, s -> spec.notifyAssetServerStateChange(s));
                }

            };
            return result;
        }

        @Override
        public PayloadCodec createPayloadCodec() {
            return new JsonPayloadCodec(); // should hopefully not harm
        }

        @Override
        public boolean isAvailable(String host) {
            return NetUtils.isAvailable(host, spec.getAssetServerAddress().getPort());
        }
        
    }
    
    /**
     * Creates a VAB operations provider instance.
     */
    public AasOperationsProvider() {
        super();
        put(getStatusPath(), status);
        put(getOperationsPath(), operations);
        operations.put(getServicePath(), service);
    }

    /**
     * Returns the base path name for status/properties. Allows for overriding the default settings.
     * 
     * @return the path name, by default {@link #STATUS}
     */
    protected String getStatusPath() {
        return STATUS;
    }

    /**
     * Returns the base path name for operations. Allows for overriding the default settings.
     * 
     * @return the path name, by default {@link #OPERATIONS}
     */
    protected String getOperationsPath() {
        return OPERATIONS;
    }

    /**
     * Returns the base (sub-)path name for services. Allows for overriding the default settings.
     * 
     * @return the path name, by default {@link #SERVICE} (interpreted as sub-path of {@link #getOperationsPath()}.
     */
    protected String getServicePath() {
        return SERVICE;
    }

    /**
     * Makes a name unique within its kind, i.e., the given map.
     * 
     * @param map the map
     * @param baseName the (base) name to be made unique
     * @return the unique name
     */
    private String makeUnique(Map<String, ?> map, String baseName) {
        String uName = baseName;
        int pos = 1;
        while (operationFunctions.containsKey(uName)) {
            uName = baseName + "_" + pos;
            pos++;
        }
        return uName;
    }

    @Override
    public AasOperationsProvider defineOperation(String category, String name, Function<Object[], Object> function) {
        String uName = makeUnique(operationFunctions, category + "/" + name);
        Map<String, Entry> o = this.operations.get(category);
        if (null == o) {
            o = new HashMap<String, Entry>();
            this.operations.put(category, o);
        }
        o.put(name, new Entry(Kind.OPERATION, uName));
        operationFunctions.put(uName, function);
        LoggerFactory.getLogger(getClass()).info("Operation " + category + "/" 
            + name + " defined (uname " + uName + ")");
        return this;
    }

    @Override
    public Function<Object[], Object> getOperation(String category, String name) {
        Function<Object[], Object> result = null;
        Map<String, Entry> cat = operations.get(category);
        if (null != cat) {
            Entry ent = cat.get(name);
            if (Kind.OPERATION == ent.kind) {
                result = operationFunctions.get(ent.uName);
            }
        }
        return result;
    }

    @Override
    public Function<Object[], Object> getServiceFunction(String name) {
        return getOperation(getServicePath(), name);
    }

    @Override
    public AasOperationsProvider defineServiceFunction(String name, Function<Object[], Object> function) {
        return defineOperation(getServicePath(), name, function);
    }

    @Override
    public AasOperationsProvider defineProperty(String name, Supplier<Object> get, Consumer<Object> set) {
        properties.put(name, new Property(get, set));
        status.put(name, new Entry(Kind.PROPERTY, name));
        LoggerFactory.getLogger(getClass()).info("Property " + name + " defined");
        return this;
    }

    @Override
    public Supplier<Object> getGetter(String name) {
        Property prop = properties.get(name);
        return null == prop ? null : prop.get;
    }

    @Override
    public Consumer<Object> getSetter(String name) {
        Property prop = properties.get(name);
        return null == prop ? null : prop.set;
    }

}
