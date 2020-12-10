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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.Reference;
import de.iip_ecosphere.platform.support.aas.SubModelElement;
import de.iip_ecosphere.platform.support.aas.SubModel.SubModelBuilder;
import de.iip_ecosphere.platform.support.aas.Type;

/**
 * Utility functions for representing types in AAS. The implemented format is initial and will change over time.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ClassUtility {

    public static final String PREFIX = "type_";
    private static Map<Class<?>, Reference> mapping = new HashMap<>();

    /**
     * Adds a type to an AAS as sub-model. [static data]
     * 
     * @param aasBuilder the AAS builder
     * @param type the type to add
     * @return the reference to the sub-model (<b>null</b> if nothing was created)
     */
    public static Reference addType(AasBuilder aasBuilder, Class<?> type) {
        SubModelBuilder smb = aasBuilder.createSubModelBuilder(getSubmodelName(type));
        Reference result = addType(smb, type);
        smb.build();
        return result;
    }

    /**
     * Adds a type to a sub-model. [static data]
     * 
     * @param subModelBuilder the target sub-model
     * @param type the type to add
     * @return the reference to the type (<b>null</b> if nothing was created)
     */
    private static Reference addType(SubModelBuilder subModelBuilder, Class<?> type) {
        Reference result;
        if (type.isPrimitive() || type.isArray()) {
            result = null;
        } else {
            result = mapping.get(type);
            // TODO resolve from global dictionary?
            if (null == result) {
                for (Field f: type.getDeclaredFields()) {
                    if (!Modifier.isStatic(f.getModifiers()) && null == f.getAnnotation(Skip.class)) {
                        addTypeSubModelElement(subModelBuilder, translateToAasName(f.getName()), f.getType());
                    }
                }
                if (Object.class != type.getSuperclass()) {
                    addType(subModelBuilder, type.getSuperclass());
                }
                result = subModelBuilder.createReference();
                mapping.put(type, result);
            }
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
    public static SubModelElement addTypeSubModelElement(SubModelBuilder subModelBuilder, String idShort, 
        Class<?> type) {
        SubModelElement result;
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
            Reference aRef = mapping.get(type);
            if (null == aRef) {
                aRef = addType(subModelBuilder.getParentBuilder(), type);
            } 
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
     * Returns the name of the associated sub-model.
     * 
     * @param type the type
     * @return the name
     */
    public static String getSubmodelName(Class<?> type) {
        return PREFIX + translateToAasName(type.getSimpleName());
    }

}
