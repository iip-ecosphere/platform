/**
 * ******************************************************************************
 * Copyright (c) {2026} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.xml;

import java.io.IOException;

import de.iip_ecosphere.platform.support.plugins.PluginManager;

/**
 * XML interface, for serialization.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class Xml {

    private static Xml prototype;
    
    static {
        prototype = PluginManager.getPluginInstance(Xml.class, XmlProviderDescriptor.class);
    }
    
    /**
     * Creates a configurable instance, no annotations are considered by default.
     * 
     * @return the Xml instance
     */
    public static Xml createInstance() {
        return prototype.createInstanceImpl(false);
    }

    /**
     * Creates a configurable instance, all annotations are considered by default.
     * 
     * @return the Xml instance
     */
    public static Xml createInstance4All() {
        return prototype.createInstanceImpl(true);
    }

    /**
     * Creates a pre-configured configurable instance by applying {@link Xml#configureFor(Class)} so that
     * usual annotations are considered.
     * 
     * @param cls the class to configure for
     * @return the Xml instance
     */
    public static Xml createInstance(Class<?> cls) {
        return prototype.createInstanceImpl(false).configureFor(cls);
    }

    /**
     * Creates a pre-configured configurable instance by applying {@link Xml#configureFor(Class)} so that
     * usual annotations are considered.
     * 
     * @param cls the classes to configure for
     * @return the Xml instance
     */
    public static Xml createInstance(Class<?>... cls) {
        Xml result = prototype.createInstanceImpl(false);
        for (Class<?> c: cls) {
            result.configureFor(c);
        }
        return result;
    }
    
    /**
     * Manually sets the instance. Shall not be needed, but may be required in some tests.
     * 
     * @param xml the Xml instance
     */
    public static void setPrototype(Xml xml) {
        if (null != xml) {
            prototype = xml;
        }
    }

    /**
     * Creates the actual instance.
     * 
     * @param considerAnnotations whether annotations shall be considered automatically
     * @return the instance
     */
    protected abstract Xml createInstanceImpl(boolean considerAnnotations);

    /**
     * Configures this instance for {@code cls} by considering the annotations in {@code cls}.
     * 
     * @param cls the class
     * @return <b>this</b> for chaining
     */
    public abstract Xml configureFor(Class<?> cls);

    /**
     * Configures this instance for the given {@code fieldNames} as optional during deserialization.
     * 
     * @param cls the cls the class {@code fieldNames} are member of
     * @param fieldNames the field names (names of Java fields)
     * @return <b>this</b> for chaining
     */
    public abstract Xml defineOptionals(Class<?> cls, String... fieldNames);

    /**
     * Configures this instance so that Java field names map exactly to the given names.
     * 
     * @param fieldNames the field names (names of XML/Java fields)
     * @return <b>this</b> for chaining
     */
    public abstract Xml defineFields(String... fieldNames);
    
    /**
     * Configures this instance so that it excludes the {@code fieldNames} to be excluded.
     * 
     * @param fieldNames the field names
     * @return <b>this</b> for chaining
     */
    public abstract Xml exceptFields(String... fieldNames);

    /**
     * Configures this instance for IIP conventions.
     * 
     * @return <b>this</b> for chaining
     */
    public abstract Xml handleIipDataClasses();

    /**
     * Configures this instance to fail/fail not on unknown fields/properties.
     * 
     * @param fail to fail or not to fail
     * @return <b>this</b> for chaining
     */
    public abstract Xml failOnUnknownProperties(boolean fail);

    /**
     * Reads a value from a string.
     * 
     * @param <T> the result type
     * @param src the value as string
     * @param cls the value type
     * @throws IOException if reading fails
     */
    public abstract <T> T readValue(String src, Class<T> cls) throws IOException;

    /**
     * Reads a value from a string using the default instance.
     * 
     * @param <T> the result type
     * @param src the value as string
     * @param cls the value type
     * @throws IOException if reading fails
     */
    public static <T> T readValueDflt(String src, Class<T> cls) throws IOException {
        return prototype.readValue(src, cls);
    }
    
    /**
     * Reads a value from a byte array.
     * 
     * @param <T> the result type
     * @param src the value as byte array
     * @param cls the value type
     * @throws IOException if reading fails
     */
    public abstract <T> T readValue(byte[] src, Class<T> cls) throws IOException;

    /**
     * Reads a value from a byte array using the default instance.
     * 
     * @param <T> the result type
     * @param src the value as byte array
     * @param cls the value type
     * @throws IOException if reading fails
     */
    public static <T> T readValueDflt(byte[] src, Class<T> cls) throws IOException {
        return prototype.readValue(src, cls);
    }

    /**
     * Writes a given values into a byte array representation.
     * 
     * @param value the value to write
     * @return the byte array representation
     * @throws IOException if writing fails
     */
    public abstract byte[] writeValueAsBytes(Object value) throws IOException;
    
    /**
     * Writes a given values into a byte array representation.
     * 
     * @param value the value to write
     * @return the byte array representation
     * @throws IOException if writing fails
     */
    public static byte[] writeValueAsBytesDflt(Object value) throws IOException {
        return prototype.writeValueAsBytes(value);
    }
    
    /**
     * Writes a given values into a string representation.
     * 
     * @param value the value to write
     * @return the string representation
     * @throws IOException if writing fails
     */
    public abstract String writeValueAsString(Object value) throws IOException;

    /**
     * Writes a given values into a string representation.
     * 
     * @param value the value to write
     * @return the string representation
     * @throws IOException if writing fails
     */
    public static String writeValueAsStringDflt(Object value) throws IOException {
        return prototype.writeValueAsString(value);
    }

}
