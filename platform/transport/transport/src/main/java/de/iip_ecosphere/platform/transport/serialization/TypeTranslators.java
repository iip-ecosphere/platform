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

package de.iip_ecosphere.platform.transport.serialization;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.ClassLoaderUtils;

/**
 * Defines a set of type translators for primitive types.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TypeTranslators {

    /**
     * Identity transformation for strings.
     */
    public static final TypeTranslator<String, String> STRING = new TypeTranslator<String, String>() {

        @Override
        public String from(String data) throws IOException {
            return data;
        }

        @Override
        public String to(String source) throws IOException {
            return source;
        }
        
    };

    /**
     * Turns a string into a quoted string and back.
     */
    public static final TypeTranslator<String, String> JSON_STRING = new TypeTranslator<String, String>() {

        @Override
        public String from(String data) throws IOException {
            return StringEscapeUtils.escapeJson(data);
        }

        @Override
        public String to(String source) throws IOException {
            return StringEscapeUtils.unescapeJson(source);
        }
        
    };

    /**
     * Turns an Integer into a String and back.
     */
    public static final TypeTranslator<String, Integer> INTEGER = new TypeTranslator<String, Integer>() {

        @Override
        public String from(Integer data) throws IOException {
            return data.toString();
        }

        @Override
        public Integer to(String source) throws IOException {
            try {
                return Integer.parseInt(source);
            } catch (NumberFormatException e) {
                throw new IOException(e);
            }
        }
        
    };

    /**
     * Turns a Long into a String and back.
     */
    public static final TypeTranslator<String, Long> LONG = new TypeTranslator<String, Long>() {

        @Override
        public String from(Long data) throws IOException {
            return data.toString();
        }

        @Override
        public Long to(String source) throws IOException {
            try {
                return Long.parseLong(source);
            } catch (NumberFormatException e) {
                throw new IOException(e);
            }
        }
        
    };

    /**
     * Turns a Boolean into a String and back.
     */
    public static final TypeTranslator<String, Boolean> BOOLEAN = new TypeTranslator<String, Boolean>() {

        @Override
        public String from(Boolean data) throws IOException {
            return data.toString();
        }

        @Override
        public Boolean to(String source) throws IOException {
            return Boolean.valueOf(source);
        }
        
    };

    /**
     * Turns a Double into a String and back.
     */
    public static final TypeTranslator<String, Double> DOUBLE = new TypeTranslator<String, Double>() {

        @Override
        public String from(Double data) throws IOException {
            return data.toString();
        }

        @Override
        public Double to(String source) throws IOException {
            try {
                return Double.parseDouble(source);
            } catch (NumberFormatException e) {
                throw new IOException(e);
            }
        }
        
    };
    
    /**
     * Convenience method for creating (custom) type translator instances.
     * 
     * @param loader the class loader to load the class with
     * @param className the name of the type translator class (must provide a no-argument constructor)
     * @return the type translator instance (or <b>null</b> if the translator cannot be found/initialized)
     */
    public static TypeTranslator<?, ?> createTypeTranslator(ClassLoader loader, String className) {
        TypeTranslator<?, ?> result = null;
        try {
            Class<?> translatorClass = loader.loadClass(className);
            result = (TypeTranslator<?, ?>) translatorClass.getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | ClassCastException 
            | NoSuchMethodException | InvocationTargetException e) {
            String loaders = ClassLoaderUtils.hierarchyToString(loader);
            LoggerFactory.getLogger(TypeTranslators.class).error("Cannot instantiate instance of type '" 
                + className + " via " + loaders + "': " + e.getClass().getSimpleName() + " " + e.getMessage());
        }
        return result;
    }

    /**
     * Convenience method for creating (custom) serializer instances.
     * 
     * @param loader the class loader to load the class with
     * @param className the name of the type serializer class (must provide a no-argument constructor)
     * @return the type serializer instance (or <b>null</b> if the serializer cannot be found/initialized)
     */
    public static Serializer<?> createSerializer(ClassLoader loader, String className) {
        return (Serializer<?>) createTypeTranslator(loader, className);
    }

}
