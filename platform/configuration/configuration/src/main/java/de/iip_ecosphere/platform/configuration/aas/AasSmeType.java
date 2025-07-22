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

package de.iip_ecosphere.platform.configuration.aas;

import java.util.HashMap;
import java.util.Map;

import de.iip_ecosphere.platform.support.logging.Logger;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;

/**
 * Denotes potential {@link AasType} and {@link AasField} (meta) types.
 * 
 * @author Holger Eichelberger, SSE
 */
enum AasSmeType {
    
    AAS(true),
    SUBMODEL(true),
    SUBMODEL_LIST(true),
    SUBMODEL_ELEMENT_COLLECTION(true),
    SUBMODEL_ELEMENT_LIST(true),
    SUBMODEL_ELEMENT(true),
    PROPERTY(false),
    MULTI_LANGUAGE_PROPERTY(false),
    LANG_STRING(false),
    ENTITY(true),
    RELATION(false),
    REFERENCE(false),
    BLOB(false),
    RANGE(false),
    OPERATION(false),
    FILE(false);
    
    private static final Map<String, AasSmeType> TYPE_TRANSLATION = new HashMap<>();
    private boolean isType; // IDTA 02026-1-0
    
    /**
     * Creates a constant.
     * 
     * @param isType whether the constant represents a type (also then in IVML)
     */
    private AasSmeType(boolean isType) {
        this.isType = isType;
    }

    /**
     * Returns whether this SME type represents a type (also then in IVML).
     * 
     * @return {@code true} for type, {@code false} for operation, property, etc.
     */
    public boolean isType() {
        return isType;
    }
    
    static { // explicity mapping to see potential inconsistencies
        TYPE_TRANSLATION.put("Property", AasSmeType.PROPERTY);
        TYPE_TRANSLATION.put("Prop", AasSmeType.PROPERTY); // 2026-1-0
        TYPE_TRANSLATION.put("MLP", AasSmeType.MULTI_LANGUAGE_PROPERTY);
        TYPE_TRANSLATION.put("Range", AasSmeType.RANGE);
        TYPE_TRANSLATION.put("File", AasSmeType.FILE);
        TYPE_TRANSLATION.put("Blob", AasSmeType.BLOB);
        TYPE_TRANSLATION.put("Ref", AasSmeType.REFERENCE);
        TYPE_TRANSLATION.put("Rel", AasSmeType.RELATION);
        TYPE_TRANSLATION.put("SMC", AasSmeType.SUBMODEL_ELEMENT_COLLECTION); // topic: 02003-1-2
        
        TYPE_TRANSLATION.put("langString", AasSmeType.LANG_STRING);
        TYPE_TRANSLATION.put("Submodel", AasSmeType.SUBMODEL);
        TYPE_TRANSLATION.put("SubmodelElementCollection", AasSmeType.SUBMODEL_ELEMENT_COLLECTION); // topic: 02002-1-0
        TYPE_TRANSLATION.put("SubmodelElementCollecttion", AasSmeType.SUBMODEL_ELEMENT_COLLECTION); // typo: 02016-1-0
        TYPE_TRANSLATION.put("SubmodelElementCollection (SMC)", 
            AasSmeType.SUBMODEL_ELEMENT_COLLECTION); // topic: 02003-1-2
        TYPE_TRANSLATION.put("Submodel Element Collection (SMC)", AasSmeType.SUBMODEL_ELEMENT_COLLECTION); // typo 02021
        TYPE_TRANSLATION.put("SME", AasSmeType.SUBMODEL_ELEMENT); // topic: 02003-1-2
        TYPE_TRANSLATION.put("Entity", AasSmeType.ENTITY);
        TYPE_TRANSLATION.put("Opr", AasSmeType.OPERATION); // IDTA:02008-1-1
        TYPE_TRANSLATION.put("Operation", AasSmeType.OPERATION); // IDTA:02008-1-1
        TYPE_TRANSLATION.put("MultiLanguageProperty", AasSmeType.MULTI_LANGUAGE_PROPERTY); // IDTA:02008-1-1
        TYPE_TRANSLATION.put("property", AasSmeType.PROPERTY); // IDTA:02008-1-1
        TYPE_TRANSLATION.put("ReferenceElement", AasSmeType.REFERENCE); // IDTA:02006-2-0
        TYPE_TRANSLATION.put("RelationshipElement", AasSmeType.RELATION); // IDTA 02010-1-0
        TYPE_TRANSLATION.put("file", AasSmeType.FILE); // IDTA 02012-1-0

        TYPE_TRANSLATION.put("SML", AasSmeType.SUBMODEL_ELEMENT_LIST); // topic: 02017-1-0
        TYPE_TRANSLATION.put("SubmodelElementList", AasSmeType.SUBMODEL_ELEMENT_LIST); // topic: 02017-1-0
        TYPE_TRANSLATION.put("SubmodelElementList (SML)", AasSmeType.SUBMODEL_ELEMENT_LIST); // topic: 02017-1-0
        TYPE_TRANSLATION.put("SubmodelList", AasSmeType.SUBMODEL_LIST); // topic: 02017-1-0
    }
    
    /**
     * Turns the string value into a constant.
     * 
     * @param data the data
     * @return the constant, may be <b>null</b>
     */
    public static AasSmeType toType(String data) {
        AasSmeType result = null;
        if (null != data) {
            result = TYPE_TRANSLATION.get(data);
            if (null == result) {
                int pos = data.indexOf(" "); // works only if it's a single word!!!
                if (pos > 0) { // IDTA 02011-1-0
                    data = data.substring(0, pos).trim();
                }
                result = TYPE_TRANSLATION.get(data);
                if (null == result) {
                    StringBuilder tmp = new StringBuilder(data);
                    for (int i = 1; i < tmp.length(); i++) { // ignore first!
                        char c = tmp.charAt(i);
                        if (Character.isUpperCase(c)) {
                            tmp.insert(i, "_");
                            i++;
                        }
                    }
                    try {
                        result = AasSmeType.valueOf(tmp.toString().toUpperCase());
                        getLogger().warn("Fallback AasType (add mapping) '{}' as type name '{}' enum value {}", 
                            data, tmp, result);
                    } catch (IllegalArgumentException e) {
                        getLogger().warn("Unknown AasType '{}' ('{}')", data, tmp);
                    }
                }
            }
        } else {
            getLogger().warn("Unknown AasType: null");
        }
        return result;
    }
    
    /**
     * Returns the logger instance for this class.
     * 
     * @return the logger instance
     */
    private static Logger getLogger() {
        return LoggerFactory.getLogger(ReadExcelFile.class);
    }
    
}