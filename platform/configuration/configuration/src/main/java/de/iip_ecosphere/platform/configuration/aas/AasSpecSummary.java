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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import de.iip_ecosphere.platform.configuration.FallbackLogger;
import de.iip_ecosphere.platform.support.Version;
import de.iip_ecosphere.platform.support.aas.IdentifierType;

import static de.iip_ecosphere.platform.configuration.aas.ParsingUtils.*;

/**
 * Summarizes a specification.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AasSpecSummary {
    
    private static Logger logger;
    
    private Version version;
    private String projectName;
    private String versionIdentifier;
    private String specNumber;
    private String title;
    private List<AasType> types;
    private List<AasEnum> enums;
    

    /**
     * Creates a specification summary. Validates the input and potentially modifies it.
     * 
     * @param types the types in the specification
     * @param enums the enums in the specification
     */
    AasSpecSummary(List<AasType> types, List<AasEnum> enums) {
        this.types = types;
        this.enums = enums;
        validate();
    }
    
    /**
     * Sets the identification.
     * 
     * @param projectName the IVML project name
     * @param versionIdentifier the IDTA version identifier
     * @param version the numeric version
     * @param title the full title of the specification
     * @param specNumber the specification number
     */
    void setIdentifier(String projectName, String versionIdentifier, Version version, String title, 
        String specNumber) {
        this.projectName = projectName;
        this.versionIdentifier = versionIdentifier;
        this.version = version;
        this.title = title;
        this.specNumber = specNumber;
    }
    
    /**
     * Registers {@code type}.
     * 
     * @param typeMap the idShort/type map to modify as a side effect
     * @param sIdTypeMap the semanticId/type map to modify as a side effect
     * @param idShort the idShort to use (typically the one of {@code type} but if being renamed, the old idShort 
     *     before renaming)
     * @param type the type to register
     */
    private void registerType(Map<String, AasType> typeMap, Map<String, AasType> sIdTypeMap, String idShort, 
        AasType type) {
        typeMap.put(idShort, type);
        if (type.getSemanticId() != null) {
            sIdTypeMap.put(type.getSemanticId(), type);
        }
    }
    
    // checkstyle: stop method length check
    
    /**
     * Validates the model information towards IVML postprocessing.
     */
    private void validate() {
        Map<String, AasType> typeMap = new HashMap<>();
        Map<String, AasType> sIdTypeMap = new HashMap<>();
        Map<String, AasEnum> enumMap = new HashMap<>();
        Map<String, String> rename = new HashMap<>();
        Map<String, Integer> counter = new HashMap<>();
        List<AasType> removeType = new ArrayList<>();
        for (AasType t: types) {
            String idShort = t.getIdShort();
            if (idShort.startsWith("{")) { // remaining generics
                String newIdShort = idShort.replace("{", "").replace("}", "");
                rename.put(t.getSemanticId(), newIdShort);
                t.setIdShort(newIdShort);
                t.setDisplayName(idShort);
                getLogger().warn("Renamed type {} to {} for semanticId {} as no IVML identifier.", idShort, newIdShort, 
                    t.getSemanticId());
                idShort = newIdShort;
            }
            if (!typeMap.containsKey(idShort)) {
                registerType(typeMap, sIdTypeMap, idShort, t);
                counter.put(idShort, 1);
            } else {
                if (!sIdTypeMap.containsKey(t.getSemanticId())) {
                    int cnt = counter.get(idShort) + 1;
                    counter.put(idShort, cnt);
                    String newIdShort = idShort + "_" + cnt;
                    t.setIdShort(newIdShort);
                    rename.put(t.getSemanticId(), newIdShort);
                    registerType(typeMap, sIdTypeMap, newIdShort, t);
                    getLogger().warn("Renamed type {} to {} for semanticId {}.", idShort, newIdShort, 
                        t.getSemanticId());
                } else {
                    removeType.add(t); // just duplicate and hopefully not needed
                }
            }
        }
        types.removeAll(removeType);
        for (AasEnum e: enums) {
            validate(e);
            enumMap.put(e.getIdShort(), e);
        }
        String imports = AasImports.importsStream()
            .map(i -> i.getProjectName())
            .collect(Collectors.joining(", "));
        for (AasType t: types) {
            if (null == t.getSmeType()) {
                getLogger().error("Type {} has no SME type assigned. Cannot print IVML. ", t.getIdShort());
            }            
            for (AasField f : t.fields()) {
                String idShort = f.getIdShort();
                if (null != idShort && idShort.startsWith("{")) {
                    f.setDisplayName(idShort);
                    int pos = idShort.indexOf("}");
                    if (pos > 0) {
                        idShort = idShort.substring(0, pos);
                    }
                    idShort = idShort.replace("{", "").replace("}", "");
                    String semId = f.getSemanticId();
                    if (semId != null && semId.startsWith(IdentifierType.IRI_PREFIX)) {
                        pos = semId.lastIndexOf('/');
                        String semIdNamePart = semId.substring(pos + 1);
                        // exists and minimum length; check for number?
                        if (pos > 0 && semIdNamePart.length() >= 3 
                            && Character.isAlphabetic(semIdNamePart.charAt(0))) {
                            idShort =  ParsingUtils.toIdentifier(semIdNamePart);
                        }
                    }
                    f.setIdShort(idShort);
                }
                String ivmlValueType = f.getIvmlValueType(true);
                if (null != ivmlValueType) {
                    ivmlValueType = stripRefBy(ivmlValueType);
                    String semId = f.getSemanticId();
                    String renameTo = rename.get(semId);
                    if (null != renameTo) {
                        ivmlValueType = renameTo;
                        f.setValueType(ivmlValueType);
                        getLogger().warn("Changed type of {}/{} to {} as type of semanticId {} was ambiguous. ", 
                            t.getIdShort(), f.getIdShort(), ivmlValueType, semId);
                    } else {
                        String impType = AasImports.getSpecificType(semId);
                        if (null != impType) {
                            ivmlValueType = impType;
                            f.setValueType(impType); // fix this
                            getLogger().warn("Changed type of {}/{} to {} due to import for semanticId {}. ", 
                                t.getIdShort(), f.getIdShort(), semId);
                        }
                    }
                    if (!AasField.isBasicType(ivmlValueType)) {
                        boolean isType = typeMap.containsKey(ivmlValueType);
                        boolean isEnum = enumMap.containsKey(ivmlValueType);
                        if (!isType && !isEnum && !AasImports.isKnownType(ivmlValueType, null)) {
                            String impType = null;
                            AasType refType = sIdTypeMap.get(semId);
                            if (null != refType) {
                                impType = refType.getIdShort();
                            }
                            if (null != impType) {
                                boolean diff = !f.getValueType().equals(impType);
                                f.setValueType(impType); // fix this
                                warn(diff, "Changed type of {}/{} to {} due to declared semanticId {}. ", 
                                    t.getIdShort(), f.getIdShort(), impType, semId);
                            } else {
                                getLogger().warn("Value type of field {} in type {} is not defined: {}. Using "
                                    + "generic type. Declared imports are: {}", f.getIdShort(), t.getIdShort(), 
                                    ivmlValueType, imports);
                                f.setValueType(null); // not found, reset and use generic type
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Conditionally emits a warning.
     * 
     * @param condition the condition, must be {@code true} for emitting the warning
     * @param text the text in logger style
     * @param args the arguments
     */
    private void warn(boolean condition, String text, Object... args) {
        if (condition) {
            getLogger().warn(text, args);
        }
    }
    
    /**
     * Validates an enumeration specification.
     * 
     * @param en the enumeration to validate
     */
    private void validate(AasEnum en) {
        int count = 1;
        for (AasEnumLiteral lit : en.literals()) {
            if (!isValue(lit.getIdShort())) { // IDTA 2021-1-0
                String idShort = "VALUE_" + count;
                if (isValue(lit.getIdentifier())) {
                    idShort = lit.getIdentifier();
                } else if (isValue(lit.getDisplayName())) {
                    idShort = ParsingUtils.toIdentifier(lit.getDisplayName());
                } else if (isValue(lit.getValueId())) {
                    idShort = ParsingUtils.toIdentifier(lit.getValueId());
                }
                lit.setIdShort(idShort);
            }
            count++;
        }
    }

    // checkstyle: resume method length check

    /**
     * Returns the IVML project name.
     * 
     * @return the project name
     */
    public String getProjectName() {
        return projectName;
    }
    
    /**
     * Returns the numeric version of the specification.
     * 
     * @return the version (if present, else <b>null</b>)
     */
    public Version getVersion() {
        return version;
    }

    /**
     * Returns the IDTA version identifier.
     * 
     * @return the version identifier
     */
    public String getVersionIdentifier() {
        return versionIdentifier;
    }
    
    /**
     * Returns the full title of the specification.
     * 
     * @return the full title of the specification
     */
    public String getTitle() {
        return title;
    }
    
    /**
     * Returns the number of types.
     * 
     * @return the number of types
     */
    public int getTypesCount() {
        return types.size();
    }

    /**
     * Returns the main submodel of the specification.
     * 
     * @return the main submodel
     */
    public Optional<AasType> getMainSubmodel() {
        return types
            .stream()
            .filter(t -> t.getSmeType() == AasSmeType.SUBMODEL)
            .findFirst();
    }
    
    /**
     * Returns the types in the specification.
     * 
     * @return the types
     */
    public Iterable<AasType> types() {
        return types;
    }

    /**
     * Returns the enumerations in the specification.
     * 
     * @return the enumerations
     */
    public Iterable<AasEnum> enums() {
        return enums;
    }
    
    /**
     * Returns the specification number.
     * 
     * @return the specification number
     */
    public String getSpecNumber() {
        return specNumber;
    }
    
    /**
     * Emits some model statistics onto {@code out}.
     * 
     * @param out the output stream
     */
    public void printStatistics(PrintStream out) {
        int fields = 0;
        int ops = 0;
        for (AasType t : types) {
            fields += t.getFieldsCount();
            ops += t.getOperationsCount();
        }
        
        out.println("Statistics: " + title);
        out.println(" - types: " + types.size());
        out.println(" - fields: " + fields);
        out.println(" - operations: " + ops);
        out.println(" - enums: " + enums.size());
    }

    /**
     * Returns the logger instance for this class.
     * 
     * @return the logger instance
     */
    private static Logger getLogger() {
        logger = FallbackLogger.getLogger(logger, AasSpecSummary.class, ParsingUtils.getLoggingLevel());
        return logger;
    }    
    
}