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

package de.iip_ecosphere.platform.configuration.easyProducer.aas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import de.iip_ecosphere.platform.support.Version;
import de.iip_ecosphere.platform.support.aas.IdentifierType;

/**
 * Defines known types to be considered as imports.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AasImports {
    
    private static final Set<String> KNOWN_TYPES = new HashSet<>();
    
    /**
     * Describes an import.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class Import {
        
        private String projectName;
        private Version version;
        private Set<String> knownTypes = new HashSet<>();
        
        /**
         * Creates an import information.
         * 
         * @param projectName the project name
         * @param version the required version, may be <b>null</b>
         * @param knownTypes optional listing of known types
         */
        private Import(String projectName, Version version, String... knownTypes) {
            this.projectName = projectName;
            this.version = version; // min, max, equals?
            for (String s: knownTypes) {
                this.knownTypes.add(s);
                AasImports.KNOWN_TYPES.add(s);
            }
        }
        
        /**
         * Returns the name of the project to import.
         * 
         * @return the name of the project
         */
        public String getProjectName() {
            return projectName;
        }
        
        /**
         * Returns the required version.
         * 
         * @return the version, may be <b>null</b> for none
         */
        public Version getVersion() {
            return version;
        }

        /**
         * Returns whether {@code type} shall be known through this import.
         * 
         * @param type the type (may be <b>null</b>)
         * @return {@code true} for known, {@code false} else
         */
        public boolean isKnownType(String type) {
            return null != type && knownTypes.contains(type);
        }
        
    }
    
    private static Map<String, Import> imports = new HashMap<>();
    private static Map<String, String> specificType = new HashMap<>();
    
    // might be read from files, currently only for specific ones
    static {
        Import contactInformation = new Import("IDTA_02002_ContactInformations", new Version(1, 0), 
            "RoleOfContactPerson", "TypeOfTelephone", "TypeOfFaxNumber", "TypeOfEmailAddress", "ContactInformations", 
            "ContactInformation", "Phone", "Fax", "Email", "IPCommunication"); 
        
        addImport(IdentifierType.compose(IdentifierType.IRI_PREFIX, 
            "https://admin-shell.io/zvei/nameplate/1/0/ContactInformations"), 
            "ContactInformations", contactInformation);
        addImport("irdi:0173-1#02-AAQ837#007", "ContactInformations", contactInformation); // isCaseOf, IDTA-02010-1-0
        addImport(IdentifierType.compose(IdentifierType.IRI_PREFIX, 
            "https://admin-shell.io/zvei/nameplate/1/0/ContactInformations/ContactInformation"), 
            "ContactInformation", contactInformation);
        addImport("irdi:0173-1#01-ADR448#007", "ContactInformation", contactInformation); // isCaseOf, IDTA-02010-1-0
        addImport(IdentifierType.compose(IdentifierType.IRI_PREFIX, 
            "https://admin-shell.io/zvei/nameplate/1/0/ContactInformations/ContactInformation/Phone"), 
            "Phone", contactInformation);
        addImport("irdi:0173-1#02-AAQ834#005", "Fax", contactInformation);
        addImport("irdi:0173-1#02-AAQ836#005", "Email", contactInformation);
        addImport(IdentifierType.compose(IdentifierType.IRI_PREFIX, 
            "iri:https://admin-shell.io/zvei/nameplate/1/0/ContactInformations/ContactInformation/IPCommunication/"), 
            "IPCommunication", contactInformation);
    }
    
    /**
     * Adds an import.
     * 
     * @param semanticId the semantic id
     * @param specificType the represented specific type
     * @param imp the underlying import
     */
    private static void addImport(String semanticId, String specificType, Import imp) {
        imports.put(semanticId, imp);
        if (null != specificType) {
            AasImports.specificType.put(semanticId, specificType);
        }
    }
    
    /**
     * Returns the import representing {@code semanticId}.
     * 
     * @param semanticId the semantic id in the notation of the AAS abstraction of the platform
     * @return the import, <b>null</b> for none
     */
    static Import getImport(String semanticId) {
        return null == semanticId ? null : imports.get(semanticId);
    }
    
    /**
     * Returns the specific type represented by {@code semanticId}.
     * 
     * @param semanticId the semantic id in the notation of the AAS abstraction of the platform
     * @return the type, <b>null</b> for none
     */
    static String getSpecificType(String semanticId) {
        return null == semanticId ? null : specificType.get(semanticId);
    }

    /**
     * Returns whether {@code type} shall be known through this import.
     * 
     * @param type the type (may be <b>null</b>)
     * @param excludes imports to be excluded
     * @return {@code true} for known, {@code false} else
     */
    static boolean isKnownType(String type, Set<Import> excludes) {
        boolean result = false;
        if (null == excludes) {
            result = null != type && KNOWN_TYPES.contains(type);
        } else {
            for (Import imp : imports.values()) {
                if (!excludes.contains(imp)) {
                    if (imp.isKnownType(type)) {
                        result = true;
                        break;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Sorts {@code imports} according to {@link Import#getProjectName()}.
     * 
     * @param imports the imports
     * @return {@code imports}
     */
    public static List<Import> sort(List<Import> imports) {
        Collections.sort(imports, (i1, i2) -> i1.getProjectName().compareTo(i2.getProjectName()));
        return imports;
    }
    
    /**
     * Returns the available imports (sorted).
     * 
     * @return the available imports
     */
    public static Iterable<Import> imports() {
        return sort(new ArrayList<>(new HashSet<Import>(imports.values())));
    }

    /**
     * Returns the available imports (sorted).
     * 
     * @return the available imports
     */
    public static Stream<Import> importsStream() {
        return sort(new ArrayList<>(new HashSet<Import>(imports.values()))).stream();
    }

}
