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

package de.iip_ecosphere.platform.connectors.model;

import java.io.IOException;

import de.iip_ecosphere.platform.connectors.MachineConnector;
import de.iip_ecosphere.platform.connectors.types.ConnectorOutputTypeTranslator;

/**
 * Provides access to a model-based protocol such as OPC UA or AAS. This interface shall be implemented by the connector
 * and provided to the protocol adapter/type translators so that they can access the underlying model in a uniform
 * manner. This interface is preliminary and not complete. Qualified names ({@code qName}) follow a hierarchical naming
 * schema separated by {@link #getQSeparator()}. 
 * 
 * For now, we assume a simple property/operation-based model with basic support for individual struct/record-based 
 * types. In individual protocols, some operations may not be supported and shall be terminated by an exception. 
 * The assumption is that the respective adapter is either programmed having the protocol in mind or adequately 
 * generated. See the annotation {@link MachineConnector} that a connector shall be decorated with to steer the 
 * generation.
 * 
 * Initially we considered having direct access into the connector instance as fallback. However, in the mean time
 * we believe that we then have to adjust the abstraction accordingly.
 * 
 * @param <D> the protocol-specific data type for values, parameters and return values; {@code Object} indicates that 
 *   the  protocol accepts (at least) Java primitive types as object Wrappers. In that case, the conversion functions
 *   may be superfluous (see {@link MachineConnector#acceptsObject()} is {@code true}). 
 * @author Holger Eichelberger, SSE
 */
public interface ModelAccess <D> {
    
    /**
     * Returns the qualified name separator.
     * 
     * @return the qualified name separator, empty if {@link MachineConnector#supportsHierarchicalQNames()} is 
     * {@code false}
     */
    public String getQSeparator();
    
    /**
     * Calls an operation on the model.
     *  
     * @param qName the qualified name of the operation (composed using {@link #getQSeparator()}).
     * @param args the arguments for the call
     * @return the return value (may be <b>null</b> for void)
     * @throws IOException in case that the call fails or calls are not implemented (see 
     * {@link MachineConnector#supportsModelCalls()} is {@code false})
     */
    @SuppressWarnings("unchecked")
    public D call(String qName, D... args) throws IOException;
    
    /**
     * Returns a property value.
     * 
     * @param qName the qualified name of the property (composed using {@link #getQSeparator()}).
     * @return the property value (may be <b>null</b> for void)
     * @throws IOException in case that the access fails or reading properties is not implemented (see 
     * {@link MachineConnector#supportsModelProperties()} is {@code false})
     */
    public D get(String qName) throws IOException;

    /**
     * Changes a property value.
     * 
     * @param qName the qualified name of the property (composed using {@link #getQSeparator()}).
     * @param value the new property value
     * @throws IOException in case that the access fails or setting properties is not implemented (see 
     * {@link MachineConnector#supportsModelProperties()} is {@code false})
     */
    public void set(String qName, D value) throws IOException;

    // complex types
    
    /**
     * Returns the "struct" value of a property. We assume that there is a type definition for the struct
     * realized as class which also represents the actual values. Usually, such custom datatypes must
     * be registered through {@link #registerCustomType(Class)}. Details shall be documented by the 
     * implementing connector. [struct]
     * 
     * @param <T> the type of the struct
     * @param qName the qualified name of the property
     * @param type the expected type
     * @return the value of the slot
     * @throws IOException in case that the access fails or structs are not supported (see 
     *     {@link MachineConnector#supportsModelStructs()} is {@code false})
     * @see #registerCustomType(Class)
     */
    public <T> T getStruct(String qName, Class<T> type) throws IOException;

    /**
     * Changes the "struct" value of a property. We assume that there is a type definition for the struct
     * realized as class which also represents the actual values. Usually, such custom datatypes must
     * be registered through {@link #registerCustomType(Class)}. Details shall be documented by the 
     * implementing connector. [struct]
     * 
     * @param qName the qualified name of the slot in the struct
     * @param value the slot value
     * @throws IOException in case that the access fails or structs are not supported (see 
     *     {@link MachineConnector#supportsModelStructs()} is {@code false})
     * @see #registerCustomType(Class)
     */
    public void setStruct(String qName, Object value) throws IOException;

    /**
     * Registers {@code cls} as a custom type, e.g., for structs.
     * 
     * @param cls the class representing the custom type 
     * @throws IOException in case that accessing relevant information on {@code cls} for performing the registration 
     *   fails or structs are not supported (see {@link MachineConnector#supportsModelStructs()} is {@code false})
     */
    public void registerCustomType(Class<?> cls) throws IOException;

    // converters to D

    /**
     * Turns a Java int value into a model value. May not be needed if the model accepts Java values as object 
     * directly (see {@link MachineConnector#acceptsObject()} is {@code true}). [conversion]
     * 
     * @param value the Java int value
     * @return the model value
     * @throws IOException if the conversion fails
     */
    public D fromInt(int value) throws IOException;

    /**
     * Turns a Java String value into a model value. May not be needed if the model accepts Java values as object 
     * directly (see {@link MachineConnector#acceptsObject()} is {@code true}). [conversion]
     * 
     * @param value the Java String value
     * @return the model value
     * @throws IOException if the conversion fails
     */
    public D fromString(String value) throws IOException;

    /**
     * Turns a Java double value into a model value. May not be needed if the model accepts Java values as object 
     * directly (see {@link MachineConnector#acceptsObject()} is {@code true}). [conversion]
     * 
     * @param value the Java double value
     * @return the model value
     * @throws IOException if the conversion fails
     */
    public D fromDouble(double value) throws IOException;
    
    // further types missing
    
    // converters from D

    /**
     * Turns a model int value into a Java value. May not be needed if the model accepts Java values as object 
     * directly (see {@link MachineConnector#acceptsObject()} is {@code true}). [conversion]
     * 
     * @param value the model int value
     * @return the Java int value
     * @throws IOException if the conversion fails
     */
    public int toInt(D value) throws IOException;

    /**
     * Turns a model String value into a Java value. May not be needed if the model accepts Java values as object 
     * directly (see {@link MachineConnector#acceptsObject()} is {@code true}). [conversion]
     * 
     * @param value the model String value
     * @return the Java String value
     * @throws IOException if the conversion fails
     */
    public String toString(D value) throws IOException;

    /**
     * Turns a model double value into a Java value. May not be needed if the model accepts Java values as object 
     * directly (see {@link MachineConnector#acceptsObject()} is {@code true}). [conversion]
     * 
     * @param value the model double value
     * @return the Java double value
     * @throws IOException if the conversion fails
     */
    public double toDouble(D value) throws IOException;
    
    // further types missing
    
    // monitoring
    
    /**
     * Monitors the given {@code qName} element in the server namespace and upon changes, triggers a reception in 
     * the connector. Intended to be used in {@link ConnectorOutputTypeTranslator#initializeModelAccess()}. [monitoring]
     *
     * @param qName the qualified names of the elements to monitor
     * @throws IOException if creating the monitor fails
     */
    public void monitor(String... qName) throws IOException;

    /**
     * Whether the connector shall send detailed information about monitored changes. Intended to be used in 
     * {@link ConnectorOutputTypeTranslator#initializeModelAccess()}. [monitoring]
     * 
     * @param detail {@code true} for details, {@code false} for <b>null</b> (default)
     */
    public void setDetailNotifiedItem(boolean detail);
    
    /**
     * Use notifications or polling. This is required here, as the related translator code {@link #monitor(String...)} 
     * depends on that. [monitoring]
     * 
     * @param notifications {@code true} for notifications, {@code false} for polling
     */
    public void useNotifications(boolean notifications);
    
}
