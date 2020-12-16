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

package test.de.iip_ecosphere.platform.support.aas.basyx;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.basyx.vab.coder.json.connector.JSONConnector;
import org.eclipse.basyx.vab.exception.provider.ResourceNotFoundException;
import org.eclipse.basyx.vab.modelprovider.VABElementProxy;
import org.eclipse.basyx.vab.modelprovider.VABPathTools;
import org.eclipse.basyx.vab.modelprovider.generic.IVABElementHandler;
import org.eclipse.basyx.vab.modelprovider.generic.VABModelProvider;
import org.eclipse.basyx.vab.protocol.basyx.connector.BaSyxConnector;

/**
 * Implements a simple VAB operations provider following a simple status/operations/service structure 
 * as the VABMapProvider. The idea is to attach the relevant operations of an implementing object to
 * this provider, whereby the implementing object is actually providing the functionality and this provider
 * just acts as an intermediary. For this purpose, you can register function objects for operations and 
 * supplier/consumer objects of the implementing object in the provider. The provider then offers the respective
 * access under 
 * <ul>
 *  <li>{@value #STATUS}/<i>propertyName</i></li>
 *  <li>{@value #OPERATIONS}/{@value #SERVICE}/<i>operationName</i></li>
 * </ul>
 * while "{@value #SERVICE}" is just seen as a category of many potential operation categories that you can register.
 * "/" is the VAB-defined path separator, which is available as {@link #SEPARATOR constant}.
 * The respective invocables must follow this naming schema for access.
 * 
 * An instance of this class can be used as a kind of implicit builder, i.e., operations to define properties and 
 * operations return the instance the operation is called on. Ultimately, call {@link #createModelProvider()} which
 * turns this instance into a model provider to be utilized to create a server.
 * 
 * @author Holger Eichelberger, SSE
 */
public class VABOperationsProvider extends HashMap<String, Object> {

    /**
     * The path separator.
     */
    public static final String SEPARATOR =  VABPathTools.SEPERATOR;

    /**
     * The path prefix for properties.
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
         * 'Refers' to {@link VABOperationsProvider#properties}.
         */
        PROPERTY,

        /**
         * 'Refers' to {@link VABOperationsProvider#operations}.
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
     * Creates a VAB operations provider instance.
     */
    public VABOperationsProvider() {
        super();
        put(STATUS, status);
        put(OPERATIONS, operations);
        operations.put(SERVICE, service);
    }

    /**
     * Ultimately creates the model provider.
     * 
     * @return the model provider
     */
    public VABModelProvider createModelProvider() {
        return new VABModelProvider(this, new VABElementHandler());
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

    /**
     * Defines an operation.
     * 
     * @param category the category within {@value #OPERATIONS}
     * @param name the name of the operation
     * @param function the implementing function
     * @return <b>this</b>
     */
    public VABOperationsProvider defineOperation(String category, String name, Function<Object[], Object> function) {
        String uName = makeUnique(operationFunctions, category + "/" + name);
        operationFunctions.put(uName, function);
        
        Map<String, Entry> o = this.operations.get(category);
        if (null == o) {
            o = new HashMap<String, Entry>();
            this.operations.put(category, o);
        }
        o.put(name, new Entry(Kind.OPERATION, uName));
        return this;
    }

    /**
     * Defines a service function (i.e., in {@value #OPERATIONS}/{@value #SERVICE}).
     * 
     * @param name the name of the operation
     * @param function the implementing function
     * @return <b>this</b>
     * @see #defineOperation(String, String, Function)
     */
    public VABOperationsProvider defineServiceFunction(String name, Function<Object[], Object> function) {
        return defineOperation(SERVICE, name, function);
    }
    
    /**
     * Defines a property with getter/setter implementation within {@value #STATUS}. Theoretically, either getter/setter
     * may be <b>null</b> for read-only/write-only properties, but this must be, however, reflected in the AAS so that 
     * no wrong can access happens.
     * 
     * @param name the name of the property
     * @param get the supplier providing read access to the property value (may be <b>null</b>)
     * @param set the consumer providing write access to the property value (may be <b>null</b>)
     * @return <b>this</b>
     */
    public VABOperationsProvider defineProperty(String name, Supplier<Object> get, Consumer<Object> set) {
        properties.put(name, new Property(get, set));
        status.put(name, new Entry(Kind.PROPERTY, name));
        return this;
    }
    
    /**
     * Defines the implementation to handle VAB elements, i.e., the mappings to the defined operations and functions.
     * 
     * @author Holger Eichelberger, SSE
     */
    private class VABElementHandler implements IVABElementHandler {
    
        @Override
        public Object postprocessObject(Object element) {
            Object result = element;
            if (element instanceof Property) {
                result = ((Property) element).get.get();
            }
            return result;
        }
    
        @Override
        public Object getElementProperty(Object element, String propertyName) {
            Map<?, ?> map = (Map<?, ?>) element;
            Object result = map.get(propertyName);
            if (result instanceof Entry) {
                Entry entry = (Entry) result;
                switch (entry.kind) {
                case PROPERTY:
                    result = properties.get(entry.uName);
                    break;
                case OPERATION:
                    result = operationFunctions.get(entry.uName);
                    break;
                default:
                    throw new ResourceNotFoundException("Unkown entry kind for " + propertyName);
                }
                if (null == result) {
                    throw new ResourceNotFoundException(entry.kind.name().toLowerCase() + propertyName + " not found.");
                }
            }
            return result;
        }
    
        @Override
        public void setModelPropertyValue(Object element, String propertyName, Object newValue) {
            if (STATUS.equals(element)) {
                Property prop = properties.get(propertyName);
                if (null == prop) {
                    throw new ResourceNotFoundException("Property " + propertyName + " in element " + element 
                        + " not found.");
                }  else if (null == prop.set) {
                    throw new ResourceNotFoundException("Property " + propertyName + " in element " + element 
                        + " not found (for reading).");
                } 
                prop.set.accept(newValue);
            } else {
                throw new ResourceNotFoundException("Element " + element + " not found.");
            }
        }
    
        @Override
        public void createValue(Object element, Object newValue) {
            throw new ResourceNotFoundException("Element " + element + " not supported.");
        }
    
        @Override
        public void deleteValue(Object element, String propertyName) {
            throw new ResourceNotFoundException("Element " + element + " not supported.");
        }
    
        @Override
        public void deleteValue(Object element, Object property) {
            // deletes from a map
            throw new ResourceNotFoundException("Element " + element + " not found.");
        }
        
    }

    /**
     * Creates a VAB setter call for the given property {@code name} on {@code host} and {@code port}.
     * 
     * @param name the property name
     * @param host the host name of the VAB server
     * @param port the port number of the VAB server
     * @return the consumer object to be attached to the AAS property
     */
    public static Consumer<Object> createSetter(String name, String host, int port) {
        return (params) -> {
            VABElementProxy proxy = new VABElementProxy("", new JSONConnector(new BaSyxConnector(host, port)));
            proxy.setModelPropertyValue(VABOperationsProvider.PREFIX_STATUS + name, params);
        };
    }

    /**
     * Creates a VAB getter call for the given property {@code name} on {@code host} and {@code port}.
     * 
     * @param name the property name
     * @param host the host name of the VAB server
     * @param port the port number of the VAB server
     * @return the supplier object to be attached to the AAS property
     */
    public static Supplier<Object> createGetter(String name, String host, int port) {
        return () -> {
            VABElementProxy proxy = new VABElementProxy("", new JSONConnector(new BaSyxConnector(host, port)));
            return proxy.getModelPropertyValue(VABOperationsProvider.PREFIX_STATUS + name);
        };
    }

    /**
     * Creates a VAB function call for the given operation {@code name} on {@code host} and {@code port}.
     * 
     * @param name the operation name
     * @param host the host name of the VAB server
     * @param port the port number of the VAB server
     * @return the function object to be attached to the AAS property
     */
    public static Function<Object[], Object> createInvocable(String name, String host, int port) {
        return (params) -> {
            VABElementProxy proxy = new VABElementProxy("", new JSONConnector(new BaSyxConnector(host, port)));
            return proxy.invokeOperation(VABOperationsProvider.PREFIX_SERVICE + name, params);
        };
    }

}
