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

package de.iip_ecosphere.platform.support.bytecode;

import de.iip_ecosphere.platform.support.plugins.PluginManager;

import java.lang.annotation.Annotation;

import de.iip_ecosphere.platform.support.Builder;

/**
 * Generic access to Bytecode manipulation. Requires an implementing plugin of type {@link Bytecode} or an active 
 * {@link BytecodeProviderDescriptor}. Abstraction based on bytebuddy.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class Bytecode {
    
    private static Bytecode instance; 

    static {
        instance = PluginManager.getPluginInstance(Bytecode.class, BytecodeProviderDescriptor.class);
    }

    /**
     * Returns the Rest instance.
     * 
     * @return the instance
     */
    public static Bytecode getInstance() {
        return instance;
    }
    
    /**
     * Manually sets the instance. Shall not be needed, but may be required in some tests.
     * 
     * @param rest the Rest instance
     */
    public static void setInstance(Bytecode rest) {
        if (null != rest) {
            instance = rest;
        }
    }

    /**
     * Builds a dynamic class.
     * 
     * @param <T> the type of base class the class shall be created for
     * @author Holger Eichelberger, SSE
     */
    public interface ClassBuilder<T> extends Builder<Class<? extends T>> {

        /**
         * Sub-builder for type annotations.
         * 
         * @param <T> the parent type of base class the class shall be created for
         * @author Holger Eichelberger, SSE
         */
        public interface AnnotationBuilder<T extends AnnotationBuilder<T>> {

            /**
             * Defines an annotation value.
             * 
             * @param property the property
             * @param value the property value
             * @return <b>this</b> for chaining
             */
            public T define(String property, String value);

            /**
             * Defines an annotation value.
             * 
             * @param property the property
             * @param value the property value
             * @return <b>this</b> for chaining
             */
            public T define(String property, int value);

            /**
             * Defines an annotation value.
             * 
             * @param property the property
             * @param value the property value
             * @return <b>this</b> for chaining
             */
            public T define(String property, boolean value);
            
            // incomplete

        }

        /**
         * Sub-builder for type annotations.
         * 
         * @param <T> the parent type of base class the class shall be created for
         * @author Holger Eichelberger, SSE
         */
        public interface TypeAnnotationBuilder<T> extends AnnotationBuilder<TypeAnnotationBuilder<T>>, 
            Builder<ClassBuilder<T>> {
            
            /**
             * Builds the annotation.
             * 
             * @return the parent class builder for chaining
             */
            @Override
            public ClassBuilder<T> build();

        }

        /**
         * Sub-builder for field annotations.
         * 
         * @param <T> the parent type of base class the class shall be created for
         * @author Holger Eichelberger, SSE
         */
        public interface FieldAnnotationBuilder<T> extends AnnotationBuilder<FieldAnnotationBuilder<T>>, 
            Builder<FieldBuilder<T>> {
            
            /**
             * Builds the annotation.
             * 
             * @return the parent class builder for chaining
             */
            @Override
            public FieldBuilder<T> build();

        }

        /**
         * Sub-builder for annotations.
         * 
         * @param <T> the parent type of base class the class shall be created for
         * @author Holger Eichelberger, SSE
         */
        public interface FieldBuilder<T> extends Builder<ClassBuilder<T>> {

            /**
             * Annotates the class with the given annotation.
             * 
             * @param type the annotation type
             * @return the annotation builder for further properties
             */
            public FieldAnnotationBuilder<T> annotate(Class<? extends Annotation> type);
            
            /**
             * Builds the field.
             * 
             * @return the parent class builder for chaining
             */
            @Override
            public ClassBuilder<T> build();

        }

        /**
         * Defines an interface class to be built shall implement.
         * 
         * @param cls the interface to implement
         * @return <b>this</b> for chaining
         */
        public ClassBuilder<T> implement(Class<?> cls);
        
        /**
         * Defines a Java bean property, i.e., a private field as well as corresponding getter and setter.
         * 
         * @param name the name of the property
         * @param type the type of the property
         * @return <b>this</b> for chaining
         */
        public FieldBuilder<T> defineProperty(String name, Class<?> type);

        /**
         * Defines a public Java field.
         * 
         * @param name the name of the field
         * @param type the type of the field
         * @return the field builder
         */
        public FieldBuilder<T> definePublicField(String name, Class<?> type);

        /**
         * Annotates the class with the given annotation.
         * 
         * @param type the annotation type
         * @return the annotation builder for further properties
         */
        public TypeAnnotationBuilder<T> annotate(Class<? extends Annotation> type);

        /**
         * Builds and loads the class instance.
         * 
         * @throws IllegalStateException if the specification of the class is illegal
         */
        @Override
        public Class<? extends T> build() throws IllegalStateException;

    }

    /**
     * Creates a class builder for a dynamic class.
     * 
     * @param <T> the type the dynamic class shall interit from
     * @param name the name of the class
     * @param cls type the dynamic class shall interit from
     * @param loader the class loader the class shall be loaded by and made available through
     * @return the class builder for further setuo
     */
    public abstract <T> ClassBuilder<T> createClassBuilder(String name, Class<T> cls, ClassLoader loader);

}
