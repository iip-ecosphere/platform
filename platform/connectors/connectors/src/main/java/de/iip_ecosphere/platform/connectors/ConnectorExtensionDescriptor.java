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

package de.iip_ecosphere.platform.connectors;

import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.Supplier;

import de.iip_ecosphere.platform.support.jsl.ServiceLoaderUtils;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;

/**
 * Descriptor that allows for connector-specific extensions. An application may have multiple extensions for
 * different connectors so we need to distinguish them. Shall not be used within connector constructors. The holding 
 * service shall set an {@link Connector#setInstanceIdentification(String) instance identification} on the respective 
 * connector instance and check for it in the implementing {@link ConnectorExtensionDescriptor}, ideally using a 
 * service identification provided during through code generation. 
 * 
 * @author Holger Eichelberger, SSE
 */
public interface ConnectorExtensionDescriptor {
    
    /**
     * Returns whether this extension handles the given connector. 
     * 
     * @param identification the connector identification
     * @return {@code true} if handled, {@code false} else
     */
    public boolean handlesConnectorForExtension(String identification);
    
    /**
     * Returns the extension.
     * 
     * @param <T> the type of the extension
     * @param cls the requested extension type
     * @param dflt the default value if none was found
     * @return {@code dflt} or the extension value
     */
    public <T> T getConnectorExtension(Class<T> cls, Supplier<T> dflt);

    /**
     * Returns a connector extension.
     * 
     * @param <T> the type of the extension
     * @param connector the connector requesting the extension, supplying the id through 
     *     {@link Connector#getInstanceIdentification()}
     * @param cls the requested extension type
     * @param dflt the default value if none was found
     * @return {@code dflt} or the extension value
     */
    public static <T> T getExtension(Connector<?, ?, ?, ?> connector, Class<T> cls, Supplier<T> dflt) {
        return getExtension(connector.getInstanceIdentification(), cls, dflt);
    }
    
    /**
     * Returns a connector extension.
     * 
     * @param <T> the type of the extension
     * @param id the connector instance identification to be matched by the related extension
     * @param cls the requested extension type
     * @param dflt the default value if none was found
     * @return {@code dflt} or the extension value
     */
    public static <T> T getExtension(String id, Class<T> cls, Supplier<T> dflt) {
        T result;
        ServiceLoader<ConnectorExtensionDescriptor> loader = ServiceLoaderUtils.load(
            ConnectorExtensionDescriptor.class);
        Optional<ConnectorExtensionDescriptor> desc = ServiceLoaderUtils.stream(loader)
            .filter(d -> d.handlesConnectorForExtension(id))
            .findFirst();
        if (desc.isPresent()) {
            result = desc.get().getConnectorExtension(cls, dflt);
        } else {
            result = dflt.get();
            LoggerFactory.getLogger(ConnectorExtensionDescriptor.class).warn(
                "No connector extension found for '{}'. Loaded via JSL?", cls);
        }
        return result;
    }

    /**
     * Generic simple implementation of {@link #getConnectorExtension(Class, Supplier)}.
     * 
     * @param <T> the type provided by {@link #getConnectorExtension(Class, Supplier)}
     * @param <V> the value type delivered by the implementation
     * @param cls the type class provided by {@link #getConnectorExtension(Class, Supplier)}
     * @param val the value provided called if {@code cls} and {@code valClass} are the same, result must not 
     *     be <b>null</b>
     * @param dflt the default value provided called if {@code cls} and {@code valClass} are not the same
     * @return the value provided by {@code val} or {@code dflt}
     */
    public static <T, V> T getConnectorExtensionValue(Class<T> cls, Supplier<V> val, Supplier<T> dflt) {
        T result;
        V v = val.get();
        if (cls == v.getClass()) {
            result = cls.cast(v);
        } else {
            result = dflt.get();
        }
        return result;
    }

    /**
     * A default implementation of {@link ConnectorExtensionDescriptor} checking for identifier equality and providing
     * the extension value through {@link ConnectorExtensionDescriptor#getConnectorExtensionValue(
     * Class, Supplier, Supplier)}. To become a concrete descriptor, must be extended and no-arg constructor 
     * must supply the constructor arguments.
     * 
     * @param <V> the value type
     * @author Holger Eichelberger, SSE
     */
    public abstract class DefaultConnectorExtension<V> implements ConnectorExtensionDescriptor {

        private String id;
        private Supplier<V> valSupplier;
        
        /**
         * Creates the connector extension.
         * 
         * @param id the connector identifier to match
         * @param valSupplier the value supplier
         */
        public DefaultConnectorExtension(String id, Supplier<V> valSupplier) {
            this.id = id;
            this.valSupplier = valSupplier;
        }
        
        @Override
        public boolean handlesConnectorForExtension(String identification) {
            return id.equals(identification);
        }

        @Override
        public <T> T getConnectorExtension(Class<T> cls, Supplier<T> dflt) {
            return getConnectorExtensionValue(cls, valSupplier, dflt);
        }
        
    }

}
