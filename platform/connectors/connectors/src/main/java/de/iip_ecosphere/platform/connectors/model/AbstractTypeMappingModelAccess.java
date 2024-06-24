/**
 * ******************************************************************************
 * Copyright (c) {2024} The original author or authors
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
import java.util.HashMap;
import java.util.Map;

import de.iip_ecosphere.platform.connectors.MachineConnector;

/**
 * Delegates {@link #set(String, Object)} to typed calls by casting.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractTypeMappingModelAccess extends AbstractModelAccess {

    private Map<Class<?>, Setter> setters = new HashMap<>();

    /**
     * Internal setter interface to generically call set methods.
     * 
     * @author Holger Eichelberger, SSE
     */
    private interface Setter {
        
        /**
         * Changes a property value.
         * 
         * @param qName the qualified name of the property (composed using {@link #getQSeparator()}).
         * @param value the new property value
         * @throws IOException in case that the access fails or setting properties is not implemented (see 
         * {@link MachineConnector#supportsModelProperties()} is {@code false})
         */
        public void set(String qName, Object value) throws IOException;
        
    }

    /**
     * Creates an instance.
     * 
     * @param notificationChangedListener listener to be called when the notification settings
     *   have been changed, typically during initialization of the connector/model 
     */
    protected AbstractTypeMappingModelAccess(NotificationChangedListener notificationChangedListener) {
        super(notificationChangedListener);
        setters.put(Integer.class, (n, v) -> setInt(n, (Integer) v));
        setters.put(Double.class,  (n, v) -> setDouble(n, (Double) v));
        setters.put(Float.class, (n, v) -> setFloat(n, (Float) v));
        setters.put(Long.class, (n, v) -> setLong(n, (Long) v));
        setters.put(Byte.class, (n, v) -> setByte(n, (Byte) v));
        setters.put(Short.class, (n, v) -> setShort(n, (Short) v));
        setters.put(Boolean.class, (n, v) -> setBoolean(n, (Boolean) v));
        setters.put(String.class, (n, v) -> setString(n, (String) v));
    }

    @Override
    public void set(String qName, Object value) throws IOException {
        Setter setter = setters.get(value.getClass());
        if (null != setter) {
            setter.set(qName, value);
        } else {
            setString(qName, value.toString());
        }
    }
    
    // default implementation would cause infinite loop if not overridden
    
    @Override
    public abstract void setInt(String qName, int value) throws IOException;

    @Override
    public abstract void setLong(String qName, long value) throws IOException;

    @Override
    public abstract void setByte(String qName, byte value) throws IOException;

    @Override
    public abstract void setShort(String qName, short value) throws IOException;
    
    @Override
    public abstract void setBoolean(String qName, boolean value) throws IOException;

    @Override
    public abstract void setDouble(String qName, double value) throws IOException;

    @Override
    public abstract void setFloat(String qName, float value) throws IOException;

    @Override
    public abstract void setString(String qName, String value) throws IOException;

}
