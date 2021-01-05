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

package de.iip_ecosphere.platform.support.iip_aas;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.Reference;
import de.iip_ecosphere.platform.support.aas.SubmodelElement;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection.SubmodelElementCollectionBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElementContainerBuilder;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.aas.Type;

/**
 * Utility functions for representing types in AAS. A Java type is turned into
 * 
 * <ul>
 *   <li>submodel: types
 *     <ul>
 *       <li>submodel elements collection: <i>Java type name, inner classes as "."</i>
 *       <ul>
 *         <li>Property: attribute1 name, value = type (for primitives, arrays and String)</li>
 *         <li>ReferenceElement: attribute1 name, value = ref-to collection (for reference types)</li>
 *       </ul>
 *     </ul>
 *   </li>
 * </ul>
 * 
 * Type usages, e.g., for input/output types are translated to
 * <ul>
 *   <li>Property: attribute1 name, value = type (for primitives, arrays and String)</li>
 *   <li>ReferenceElement: attribute1 name, value = ref-to collection (for reference types)</li>
 * </ul>
 * 
 * The implemented format is initial and will change over time (array of ref type unclear, generics).
 * 
 * @author Holger Eichelberger, SSE
 */
public class ClassUtility {

    public static final String NAME_TYPE_SUBMODEL = "types";
    private static final String JVM_NAME = ManagementFactory.getRuntimeMXBean().getName();

    /**
     * Adds a type to an AAS as sub-model. If the type already exists in the AAS/submodel, no new element will be 
     * created just a reference to it will be returned. [static data]
     * 
     * @param aasBuilder the AAS builder
     * @param type the type to add
     * @return the reference to the sub-model (<b>null</b> if nothing was created)
     */
    public static Reference addType(AasBuilder aasBuilder, Class<?> type) {
        SubmodelBuilder smb = aasBuilder.createSubmodelBuilder(NAME_TYPE_SUBMODEL, null); // create or re-open
        SubmodelElementCollectionBuilder typeCollection = smb.createSubmodelElementCollectionBuilder(
            getName(type), false, false);
        Reference result = addType(typeCollection, type);
        typeCollection.build();
        smb.build(); // ok also in case of re-open
        return result;
    }

    /**
     * Adds a type to a sub-model. [static data]
     * 
     * @param builder the target sub-model
     * @param type the type to add
     * @return the reference to the type (<b>null</b> if nothing was created)
     */
    private static Reference addType(SubmodelElementCollectionBuilder builder, Class<?> type) {
        Reference result;
        if (type.isPrimitive() || type.isArray()) {
            result = null;
        } else {
            if (builder.isNew()) {
                for (Field f: type.getDeclaredFields()) {
                    if (!Modifier.isStatic(f.getModifiers()) && null == f.getAnnotation(Skip.class)) {
                        addTypeSubModelElement(builder, translateToAasName(f.getName()), f.getType());
                    }
                }
                if (Object.class != type.getSuperclass()) {
                    addType(builder, type.getSuperclass());
                }
            }
            result = builder.createReference();
        }
        return result;
    }
    
    /**
     * Adds a type-representing sub-model element. [static data]
     * 
     * @param subModelBuilder the target sub-model
     * @param idShort the name of the element
     * @param type the type to represent in the element
     * @return the created sub-model element
     */
    public static SubmodelElement addTypeSubModelElement(SubmodelElementContainerBuilder subModelBuilder, 
        String idShort, Class<?> type) {
        SubmodelElement result;
        // TODO not modifiable properties
        if (type.isPrimitive() || String.class == type) {
            result = subModelBuilder
                .createPropertyBuilder(idShort)
                .setType(Type.STRING)
                .setValue(type.getSimpleName())
                .build();
        } else if (type.isArray()) {
            result = subModelBuilder
                .createPropertyBuilder(idShort)
                .setType(Type.STRING)
                .setValue(type.getSimpleName())
                .build();
        } else {
            Reference aRef = addType(subModelBuilder.getAasBuilder(), type);
            result = subModelBuilder
                .createReferenceElementBuilder(idShort)
                .setValue(aRef)
                .build();
        }        
        return result;
    }
    
    /**
     * Translates a Java identifier name to an AAS short name.
     * 
     * @param javaIdentifier the Java identifier
     * @return the AAS short name
     */
    static String translateToAasName(String javaIdentifier) {
        return javaIdentifier;
    }
    
    /**
     * Returns the name of the associated model element.
     * 
     * @param type the type
     * @return the name
     */
    public static String getName(Class<?> type) {
        return translateToAasName(type.getName()).replace("$", ".");
    }

    /**
     * Turns the {@code} object into a unique id with given (optional) prefix.
     * 
     * @param prefix an optional prefix, use an empty string for none; shall end with a separator, e.g., "_"
     * @param object the object to be turned into a unique id
     * @return the combined id
     */
    public static String getId(String prefix, Object object) {
        return prefix + JVM_NAME + "_" + System.identityHashCode(object);
    }

}
