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

package de.iip_ecosphere.platform.services.spring.descriptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType;

/**
 * Resolves declared types to dynamic Java classes (just attributes). 
 * 
 * @author Holger Eichelberger, SSE
 */
public class TypeResolver {

    private static final Map<String, Class<?>> PRIMITIVES = new HashMap<>();
    private Map<String, Class<?>> classes = new HashMap<>();
    private ClassLoader loader;

    static {
        PRIMITIVES.put("String", String.class);
        PRIMITIVES.put("String[]", String[].class);
        PRIMITIVES.put("int", Integer.TYPE);
        PRIMITIVES.put("int[]", int[].class);
        PRIMITIVES.put("boolean", Boolean.TYPE);
        PRIMITIVES.put("boolean[]", boolean[].class);
        PRIMITIVES.put("byte", Byte.TYPE);
        PRIMITIVES.put("byte[]", byte[].class);
        PRIMITIVES.put("double", Double.TYPE);
        PRIMITIVES.put("double[]", double[].class);
        PRIMITIVES.put("float", Float.TYPE);
        PRIMITIVES.put("float[]", float[].class);
        PRIMITIVES.put("char", Character.TYPE);
        PRIMITIVES.put("char[]", char[].class);
        PRIMITIVES.put("short", Short.TYPE);
        PRIMITIVES.put("short[]", short[].class);
        PRIMITIVES.put("long", Long.TYPE);
        PRIMITIVES.put("long[]", long[].class);
    }
    
    /**
     * Creates a type resolver and creates internal classes for {@code declarations}.
     * 
     * @param declarations the type declarations to be used as descriptor-defined types
     */
    public TypeResolver(List<? extends Type> declarations) {
        List<? extends Type> unresolved = createClasses(declarations);
        if (!unresolved.isEmpty()) {
            String msg = "";
            for (Type t : unresolved) {
                if (msg.length() > 0) {
                    msg += ", ";
                }
                msg += t.getName();
            }
            LogManager.getLogger(getClass()).error("Cannot resolve " + msg 
                + ", potentially due to cyclic dependencies");
        }
    }
    
    /**
     * Creates classes for all given type {@code declarations}.
     * 
     * @param declarations the declarations
     * @return the left-over types that cannot be resolved, e.g., due to cyclic dependencies; empty if completely
     *   successful
     */
    private List<Type> createClasses(List<? extends Type> declarations) {
        List<Type> decls = new ArrayList<>();
        decls.addAll(declarations);
        int thisSize;
        int lastSize;
        do {
            lastSize = decls.size();
            for (int d = decls.size() - 1; d >= 0; d--) {
                if (createClasses(decls.get(d))) {
                    decls.remove(d);
                }
            }
            thisSize = decls.size();
        } while (thisSize > 0 && lastSize != thisSize);
        return decls;
    }
    
    /**
     * Returns whether a given type name is considered to be pre-defined/primitive (including "String").
     * 
     * @param name the name to look for
     * @return {@code true} for primitive/pre-defined, {@code false} else
     */
    public static boolean isPrimitive(String name) {
        return PRIMITIVES.get(name) != null; 
    }
    
    /**
     * Creates the classes for a given descriptor-defined type.
     * 
     * @param type the type
     * @return {@code true} if the {@code type} is resolvable, the class was created and registered; {@code false}
     *    if the resolution failed, e.g., as required types are not (yet) defined.
     */
    private boolean createClasses(Type type) {
        boolean resolvable = true;
        for (Field f : type.getFields()) {
            if (resolve(f.getType()) == null) {
                resolvable = false;
                break;
            }
        }
        if (resolvable) {
            DynamicType.Builder<?> typeBuilder = new ByteBuddy()
                .subclass(Object.class)
                .name(type.getName());
            for (Field f : type.getFields()) {
                typeBuilder = typeBuilder.defineField(f.getName(), resolve(f.getType()), Visibility.PUBLIC);
            }
            DynamicType.Unloaded<?> unloadedType = typeBuilder.make();
            // for loading the first class, use the loader of this class
            // for following classes, use Buddy's loader of the first class so that they can find each other
            Class<?> cls = unloadedType.load(null == loader ? getClass().getClassLoader() : loader)
                .getLoaded();
            if (null == loader) {
                loader = cls.getClassLoader();
            }
            classes.put(type.getName(), cls);
        }
        return resolvable;
    }
    
    /**
     * Resolve a type name to a type. Considers pre-defined types ({@link #PRIMITIVES}) as well as
     * descriptor-defined types used as input to this instance. 
     * 
     * @param name the type name
     * @return the type, may be <b>null</b>
     */
    public Class<?> resolve(String name) {
        int arrayDims = 0;
        Class<?> result = PRIMITIVES.get(name);
        if (null == result) {
            while (name.endsWith("[]")) {
                name = name + name.substring(0, name.length() - 2);
                arrayDims++;
            }
            try {
                result = Class.forName(name);
            } catch (ClassNotFoundException e) {
            }
        }
        if (null == result) {
            result = classes.get(name);
        }
        if (null != result && arrayDims > 0 && !result.isArray()) {
            try {
                result = Class.forName("[L" + result.getName() + ";");
            } catch (ClassNotFoundException e) {
            }
        }
        return result;
    }

}
