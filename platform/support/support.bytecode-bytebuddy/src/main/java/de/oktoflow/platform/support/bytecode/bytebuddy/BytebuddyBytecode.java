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

package de.oktoflow.platform.support.bytecode.bytebuddy;

import java.lang.annotation.Annotation;

import de.iip_ecosphere.platform.support.bytecode.Bytecode.ClassBuilder.FieldAnnotationBuilder;
import de.iip_ecosphere.platform.support.bytecode.Bytecode.ClassBuilder.FieldBuilder;
import de.iip_ecosphere.platform.support.bytecode.Bytecode.ClassBuilder.TypeAnnotationBuilder;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.DynamicType.Builder.FieldDefinition;

/**
 * Implements the bytecode interface by bytebuddy.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BytebuddyBytecode extends de.iip_ecosphere.platform.support.bytecode.Bytecode {
    
    /**
     * Implements the type annotation builder.

     * @param <T> the parent class type
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class BytebuddyTypeAnnotationBuilder<T> implements TypeAnnotationBuilder<T> {

        private AnnotationDescription.Builder builder;
        private BytebuddyClassBuilder<T> classBuilder;
        
        /**
         * Creates a builder instance.
         * 
         * @param type the annotation type
         * @param classBuilder the parent class builder
         */
        private BytebuddyTypeAnnotationBuilder(Class<? extends Annotation> type, 
            BytebuddyClassBuilder<T> classBuilder) {
            builder = AnnotationDescription.Latent.Builder.ofType(type);
            this.classBuilder = classBuilder;
        }
        
        @Override
        public TypeAnnotationBuilder<T> define(String property, String value) {
            builder = builder.define(property, value);
            return this;
        }

        @Override
        public TypeAnnotationBuilder<T> define(String property, boolean value) {
            builder = builder.define(property, value);
            return this;
        }

        @Override
        public TypeAnnotationBuilder<T> define(String property, int value) {
            builder = builder.define(property, value);
            return this;
        }

        @Override
        public ClassBuilder<T> build() {
            classBuilder.builder = classBuilder.builder.annotateType(builder.build());
            return classBuilder;
        }
        
    }

    /**
     * Implements the type annotation builder.

     * @param <T> the parent class type
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class BytebuddyFieldAnnotationBuilder<T> implements FieldAnnotationBuilder<T> {

        private AnnotationDescription.Builder builder;
        private BytebuddyFieldBuilder<T> fieldBuilder;
        
        /**
         * Creates a builder instance.
         * 
         * @param type the annotation type
         * @param fieldBuilder the parent field builder
         */
        private BytebuddyFieldAnnotationBuilder(Class<? extends Annotation> type, 
            BytebuddyFieldBuilder<T> fieldBuilder) {
            builder = AnnotationDescription.Latent.Builder.ofType(type);
            this.fieldBuilder = fieldBuilder;
        }
        
        @Override
        public FieldAnnotationBuilder<T> define(String property, String value) {
            builder = builder.define(property, value);
            return this;
        }

        @Override
        public FieldAnnotationBuilder<T> define(String property, boolean value) {
            builder = builder.define(property, value);
            return this;
        }

        @Override
        public FieldAnnotationBuilder<T> define(String property, int value) {
            builder = builder.define(property, value);
            return this;
        }

        @Override
        public FieldBuilder<T> build() {
            fieldBuilder.field = fieldBuilder.field.annotateField(builder.build());
            return fieldBuilder;
        }
        
    }

    /**
     * Implements the annotation builder.
     *
     * @param <T> the parent class type
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class BytebuddyFieldBuilder<T> implements FieldBuilder<T> {

        private FieldDefinition.Optional<T> field;
        private BytebuddyClassBuilder<T> classBuilder;
        
        /**
         * Creates a builder instance.
         * 
         * @param field the field to annotate
         * @param classBuilder the parent class builder
         */
        private BytebuddyFieldBuilder(FieldDefinition.Optional<T> field, BytebuddyClassBuilder<T> classBuilder) {
            this.field = field;
            this.classBuilder = classBuilder;
        }

        @Override
        public FieldAnnotationBuilder<T> annotate(Class<? extends Annotation> type) {
            return new BytebuddyFieldAnnotationBuilder<T>(type, this);
        }

        @Override
        public ClassBuilder<T> build() {
            classBuilder.builder = field;
            return classBuilder;
        }
        
    }

    /**
     * Implements the class builder.
     * 
     * @param <T> the base type of class to be build
     * @author Holger Eichelberger, SSE
     */
    private static class BytebuddyClassBuilder<T> implements ClassBuilder<T> {
        
        private DynamicType.Builder<T> builder;
        private ClassLoader loader;
        
        /**
         * Creates a builder instance.
         * 
         * @param name
         * @param superCls
         * @param loader
         */
        private BytebuddyClassBuilder(String name, Class<T> superCls, ClassLoader loader) {
            builder = new ByteBuddy()
                .subclass(superCls)
                .name(name);
            this.loader = loader;
        }

        @Override
        public ClassBuilder<T> implement(Class<?> cls) {
            builder = builder.implement(cls);
            return this;
        }
        
        @Override
        public FieldBuilder<T> defineProperty(String name, Class<?> type) {
            return new BytebuddyFieldBuilder<T>(builder.defineProperty(name, type), this);
        }

        @Override
        public FieldBuilder<T> definePublicField(String name, Class<?> type) {
            return new BytebuddyFieldBuilder<T>(builder.defineField(name, type, Visibility.PUBLIC), this);
        }

        @Override
        public TypeAnnotationBuilder<T> annotate(Class<? extends Annotation> type) {
            return new BytebuddyTypeAnnotationBuilder<T>(type, this);
        }

        @Override
        public Class<? extends T> build() throws IllegalStateException {
            return builder.make()
                .load(loader)
                .getLoaded();
        }
        
    }

    @Override
    public <T> ClassBuilder<T> createClassBuilder(String name, Class<T> cls, ClassLoader loader) {
        return new BytebuddyClassBuilder<T>(name, cls, loader);
    }

}
