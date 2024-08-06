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

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;

import de.iip_ecosphere.platform.configuration.FallbackLogger;

/**
 * Represents a field in an {@link AasType}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AasField extends AbstractAasElement {

    private static Logger logger;
    private static final Set<String> BASIC_TYPES = new HashSet<>();

    private AasSmeType smeType;
    private boolean isMultiValued;
    private String valueType;
    private String[] exampleValues;
    private String exampleExplanation;
    private int lowerCardinality = Integer.MIN_VALUE;
    private int upperCardinality = Integer.MIN_VALUE;
    private String aspect;
    // -> copy

    /**
     * Creates an instance.
     */
    AasField() {
    }
    
    /**
     * Copies values of {@code field}.
     * 
     * @param field the source element
     */    
    AasField(AasField field) {
        super(field);
        this.smeType = field.smeType;
        this.isMultiValued = field.isMultiValued;
        this.valueType = field.valueType;
        if (null != field.exampleValues) {
            this.exampleValues = new String[field.exampleValues.length];
            System.arraycopy(field.exampleValues, 0, this.exampleValues, 0, this.exampleValues.length);
        }
        this.exampleExplanation = field.exampleExplanation;
        this.lowerCardinality = field.lowerCardinality;
        this.upperCardinality = field.upperCardinality;
        this.aspect = field.aspect;
    }
    
    /**
     * Defines the submodelelement type.
     * 
     * @param smeType the submodelelement type
     */
    void setSmeType(AasSmeType smeType) {
        this.smeType = smeType;
    }
    
    /**
     * Returns the submodelelement type.
     * 
     * @return the submodelelement type
     */
    public AasSmeType getSmeType() {
        return smeType;
    }

    /**
     * Defines the idShort.
     * 
     * @param idShort the idShort without multi-value counting suffix.
     * @param isMultiValued whether the field is multi-valued
     */
    void setIdShort(String idShort, boolean isMultiValued) {
        setIdShort(idShort);
        this.isMultiValued = isMultiValued;
    }
    
    /**
     * Returns whether the field is multi-valued, i.e., with multi-value counting suffix.
     * 
     * @return {@code true} for multi-valued, {@code false} else
     */
    public boolean isMultiValued() {
        return isMultiValued;
    }
    
    /**
     * Returns the optional aspect this field is assigned to.
     * 
     * @return the aspect, may be <b>null</b> for none
     */
    public String getAspect() {
        return aspect;
    }

    /**
     * Defines the aspect this field is assigned to.
     * 
     * @param aspect the aspect, may be <b>null</b> for none
     */
    void setAspect(String aspect) {
        this.aspect = aspect;
    }

    /**
     * Defines the value type.
     * 
     * @param valueType the value type
     */
    void setValueType(String valueType) {
        this.valueType = valueType;
    }
    
    /**
     * Returns whether this field has a value type defined.
     * 
     * @return {@code true} for defined, {@code false} for not defined
     */
    public boolean hasValueType() {
        return valueType != null && valueType.length() > 0;
    }
    
    /**
     * Defines the optional example value(s).
     * 
     * @param value the example value(s)
     */
    void setExampleValues(String... value) {
        this.exampleValues = value;
    }
    
    /**
     * Returns the optional example value(s).
     * 
     * @return the example values
     */
    public String[] getExampleValues() {
        return exampleValues;
    }

    /**
     * Defines the optional example explanation.
     * 
     * @param exampleExplanation the example explanation
     */
    void setExampleExplanation(String exampleExplanation) {
        this.exampleExplanation = exampleExplanation;
    }
    
    /**
     * Returns the optional example explanation.
     * 
     * @return the example explanation
     */
    public String getExampleExplanation() {
        return exampleExplanation;
    }

    /**
     * Defines the lower and upper cardinality to the same value.
     * 
     * @param lowerUpper the lower and upper cardinality, the minimum integer for none
     */
    void setCardinality(int lowerUpper) {
        setCardinality(lowerUpper, lowerUpper);
    }

    /**
     * Defines the lower and upper cardinality.
     * 
     * @param lowerCardinality the lower cardinality, the minimum integer for none
     * @param upperCardinality the upper cardinality, the minimum integer for none
     */
    void setCardinality(int lowerCardinality, int upperCardinality) {
        this.lowerCardinality = lowerCardinality;
        this.upperCardinality = upperCardinality;
    }

    /**
     * Returns whether the lower cardinality.
     * 
     * @return the upper cardinality, the minimum integer for none
     */
    public int getLowerCardinality() {
        return lowerCardinality;
    }

    /**
     * Returns whether the upper cardinality.
     * 
     * @return the upper cardinality, the minimum integer for none
     */
    public int getUpperCardinality() {
        return upperCardinality;
    }

    /**
     * Returns whether the field has a specified cardinality.
     * 
     * @return {@code true} for cardinality, {@code false} else
     */
    public boolean hasCardinality() {
        return lowerCardinality == Integer.MIN_VALUE || upperCardinality == Integer.MIN_VALUE;
    }


    
    /**
     * Returns the value type.
     * 
     * @return the value type
     */
    public String getValueType() {
        return valueType;
    }
    
    /**
     * Returns whether the given {@code type} is considered to be a known, basic type.
     * 
     * @param type the type
     * @return {@code true} for known, {@code false} else
     */
    public static boolean isBasicType(String type) {
        return BASIC_TYPES.contains(type);        
    }
    
    static {
        BASIC_TYPES.add("StringType");
        BASIC_TYPES.add("IntegerType");
        BASIC_TYPES.add("RealType");
        BASIC_TYPES.add("BooleanType");
        BASIC_TYPES.add("LongType");
        BASIC_TYPES.add("FloatType");
        BASIC_TYPES.add("DoubleType");
        BASIC_TYPES.add("UnsignedInteger64Type");
        BASIC_TYPES.add("AasBlobType");
        BASIC_TYPES.add("DateTimeType");
        BASIC_TYPES.add("AasLangStringType");
        BASIC_TYPES.add("AasMultiLangStringType");
        BASIC_TYPES.add("AasFileResourceType");
        BASIC_TYPES.add("AasReferenceType");
        BASIC_TYPES.add("AasRelationType");
        BASIC_TYPES.add("AasAnyURIType");
        BASIC_TYPES.add("AasRangeType");
        BASIC_TYPES.add("AasGenericSubmodelElementCollection");
        BASIC_TYPES.add("AasGenericEntityType");
    }

    // checkstyle: stop method length check
    
    /**
     * Maps a field {@link #valueType} for AAS properties.
     * 
     * @param valueType the actual type, may be <b>null</b> or empty
     * @param dflt the default value if none matchex, may be <b>null</b>, {@code valueType} etc.
     * @return the resulting type, may be <b>null</b>
     */
    static String mapPropertyType(String valueType, String dflt) {
        String result = null;
        if (valueType != null && valueType.length() > 0) {
            switch(valueType) {
            case "listofProperties<string>": // IDTA 02017-1-0
            case "xs:string": // IDTA-02023-0-9
            case "String": // IDTA-02002-1-0; aas/Basyx small
            case "string": // IDTA 02003-1-2
            case "STRING": // IDTA 02021-1-0
                result = "StringType";
                break;
            case "Decimal": // IDTA-02006-2-0
            case "int": // IDTA-02013-1-0
            case "integer": // IDTA-02007-1-0
            case "Integer(count)": // IDTA-02010-1-0
            case "Integer (count)": // IDTA-02010-1-0
            case "INTEGER_COUNT": // IDTA-02021-1-0
            case "xs:integer": // IDTA-02010-1-0
            case "xs:int": // IDTA-02017-1-0
            case "Integer":
                result = "IntegerType";
                break;
            case "LONG": // IDTA-02008-1-1
            case "xs:long": // IDTA-02021-1-0
            case "long":
                result = "LongType";
                break;
            case "Float": // IDTA-02045-1-0
            case "float":
                result = "FloatType";
                break;
            case "xs:double": // IDTA-02023-0-9
            case "Double": // IDTA-02023-0-9
            case "double":
            case "Real":
            case "REAL_MEASURE": // IDTA-02021-1-0
            case "real":
                result = "DoubleType";
                break;
            case "STRING_TRANSLATABLE": // IDTA-02008-1-1
                result = "AasMultiLangStringType";                
                break;
            case "unsignedShort":
                result = "UnsignedInteger16Type";
                break;
            case "unsignedInt":
                result = "UnsignedInteger32Type";
                break;
            case "xs:unsignedLong": // IDTA-02021-1-0
            case "unsignedLong":
                result = "UnsignedInteger64Type";
                break;
            case "xs:boolean": // IDTA-02017-1-0
            case "boolean": // IDTA-0204-1-2
            case "Boolean":
                result = "BooleanType";
                break;
            case "xs:anyURI": // IDTA-02017-1-1
            case "anyURI":
                result = "AasAnyURIType";
                break;
            case "file":
                result = "AasFileResourceType";
                break;
            case "Blob":
                result = "AasBlobType";
                break;
            case "LangStringSet": // IDTA-02015-1-0
            case "langString":
                result = "AasLangStringType";
                break;
            case "dateTimeStamp": // IDTA-02008-1-1 AASX
            case "TIMESTSAMP": // typo in IDTA-02008-1-1
            case "Date": // IDTA-02007-1-0
            case "dateTime": // IDTA-02045-1-0
            case "DateTime": // IDTA-02010-1-0
            case "xs:dateTime": // IDTA-02010-1-0
            case "TimeStamp": // IDTA-02021-1-0
            case "date":
                result = "DateTimeType";
                break;
            default: // incomplete, agile
                result = dflt;
                break;
            }
        }
        return result;
    }
    
    // checkstyle: stop method length check

    /**
     * Returns the value type as IVML type.
     * 
     * @param log log the output
     * @return the IVML value type
     */
    public String getIvmlValueType(boolean log) {
        String result = null;
        if (null == smeType) {
            smeType = AasSmeType.PROPERTY; // unclear, may be wrong, compensating missing type in IDTA-02014-1-0
        }
        if (AasSmeType.PROPERTY == smeType) {
            result = mapPropertyType(valueType, valueType);
        }
        if (null == result && smeType != null) {
            switch (smeType) {
            case SUBMODEL_ELEMENT_LIST: // preliminary
            case SUBMODEL_ELEMENT_COLLECTION:
                if (null != valueType && valueType.length() > 0) {
                    result = valueType; // set so before, may be also idShort                        
                } else {
                    result = "AasGenericSubmodelElementCollection"; // for now, unclear
                }
                break;
            case MULTI_LANGUAGE_PROPERTY:
                result = "AasMultiLangStringType";
                break;
            case FILE:
                result = "AasFileResourceType";
                break;
            case REFERENCE:
                result = "AasReferenceType";
                break;
            case RELATION:
                result = "AasRelationType";
                break;
            case ENTITY:
                if (null != valueType && valueType.length() > 0) {
                    result = valueType; // set so before, may be also idShort                        
                } else {
                    result = "AasGenericEntityType"; // for now, unclear
                }
                break;
            case BLOB: 
                result = "AasBlobType";
                break;
            case RANGE:
                result = "AasRangeType";
                break;
            default:
                result = "StringType"; // FALLBACK TYPE
                if (log) {
                    getLogger().warn("No type mapping specified for valueType '{}' / smeType '{}'. Mapping to {}.", 
                        valueType, smeType, result);
                }
                break;
            }
        }
        return "refBy(" + result + ")";
    }
    
    /**
     * Returns the logger instance for this class.
     * 
     * @return the logger instance
     */
    private static Logger getLogger() {
        logger = FallbackLogger.getLogger(logger, AasField.class, ParsingUtils.getLoggingLevel());
        return logger;
    }    

}