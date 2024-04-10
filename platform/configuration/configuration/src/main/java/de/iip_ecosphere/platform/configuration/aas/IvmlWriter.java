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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import org.slf4j.Logger;

import de.iip_ecosphere.platform.configuration.FallbackLogger;
import de.iip_ecosphere.platform.configuration.aas.AasImports.Import;
import de.iip_ecosphere.platform.support.FileUtils;
import de.iip_ecosphere.platform.support.Version;

import static de.iip_ecosphere.platform.configuration.aas.ParsingUtils.*;

/**
 * Stores the AAS representation as IVML and also produces the related text file.
 * 
 * @author Holger Eichelberger, SSE
 */
public class IvmlWriter {

    private static Logger logger;
    private static final Pattern BASIC_IVML_NAME = Pattern.compile("^[\\w \\[\\]\\-\\Q$_\\E]+$");
    private PrintStream out = System.out;
    private String indent = "";
    private Consumer<PrintStream> closer = p -> { };
    private Set<Import> selfImports = new HashSet<>(); 
    private int idCounter = 0;
    private String fileName;
    private String namePrefix = null;
    
    /**
     * Creates a writer to sysout.
     */
    public IvmlWriter() {
    }

    /**
     * Creates a writer to the given file.
     * 
     * @param fileName the file to write to, may be <b>null</b> for none/sysout
     */
    public IvmlWriter(String fileName) throws IOException {
        this.fileName = fileName;
        if (fileName != null) {
            System.setProperty("file.encoding", "Cp1252");
            out = new PrintStream(new FileOutputStream(fileName));
            closer = p -> FileUtils.closeQuietly(p);
        }
    }
    
    /**
     * Sets an optional prefix for differing type values (usually same as "name").
     * 
     * @param namePrefix the prefix, may be <b>null</b> for none
     * @return <b>this</b> (builder style)
     */
    public IvmlWriter setNamePrefix(String namePrefix) {
        this.namePrefix = namePrefix;
        return this;
    }
    
    /**
     * Increases output indent.
     */
    private void increaseIndent() {
        indent += "  ";
    }

    /**
     * Decreases output indent.
     */
    private void decreaseIndent() {
        if (indent.length() >= 2) {
            indent = indent.substring(2);
        }
    }
    
    /**
     * Writes an empty line.
     */
    private void println() {
        out.println();
    }
    
    /**
     * Writes {@code text} after indentation.
     * 
     * @param text the text
     */
    private void println(String text) {
        out.println(indent + text);
    }

    /**
     * Writes a field/value.
     * 
     * @param fieldName the field name
     * @param value the field value
     * @param comma shall a comma be appended
     */
    private void printlnField(String fieldName, Object value, boolean comma) {
        printlnField(fieldName, value, comma, null);
    }

    /**
     * Writes a field/value.
     * 
     * @param fieldName the field name
     * @param value the field value
     * @param comma shall a comma be appended
     * @param dflt the default value of field skipping the output if value is the same
     */
    private void printlnField(String fieldName, Object value, boolean comma, Object dflt) {
        if (null == dflt || value != dflt) {
            println(fieldName + " = " + value + (comma ? "," : ""));
        }
    }

    /**
     * Writes a cardinality field, only if {@code value} is different from integer min value and {@code -1}.
     * 
     * @param fieldName the field name
     * @param value the field value
     * @param comma shall a comma be appended
     */
    private void printlnCardinalityField(String fieldName, int value, boolean comma) {
        if (value != Integer.MIN_VALUE && value != -1) {
            printlnField(fieldName, value, comma);
        }
    }

    /**
     * Writes a string-valued field.
     * 
     * @param fieldName the field name
     * @param value the field value
     * @param comma shall a comma be appended
     */
    private void printlnStringField(String fieldName, String value, boolean comma) {
        if (null != value && value.length() > 0) {
            println(fieldName + " = \"" + quoteIvmlString(value) + "\"" + (comma ? "," : ""));
        }
    }

    /**
     * Quotes a string for IVML.
     * 
     * @param value the string value
     * @return the quoted/escaped string
     */
    private String quoteIvmlString(String value) {
        return value
            .replace("\"", "'")
            .replace("\\", "\\\\");
    }
    
    /**
     * Writes the enums in AAS result.
     * 
     * @param aasResult the AAS result
     */
    private void printEnums(AasSpecSummary aasResult) {
        for (AasEnum e: aasResult.enums()) {
            if (emit(e)) {
                println();
                println("AasEnumType " + validateVariableName(e.getIdShort()) + " = {");
                increaseIndent();
                String name = validateName(e.getIdShort());
                if (namePrefix != null) {
                    name = namePrefix + name;
                }
                printlnStringField("name", name, true);
                printlnStringField("description", e.getDescription(), true);
                printlnField("isOpen", e.isOpen(), true, false);
                printlnStringField("versionIdentifier", aasResult.getVersionIdentifier(), true);
                println("literals = {");
                increaseIndent();
                boolean hasValues = false;
                boolean requiresValues = false;
                for (AasEnumLiteral l: e.literals()) {
                    hasValues |= isValue(l.getValue());
                    requiresValues |= !BASIC_IVML_NAME.matcher(l.getIdShort()).matches();
                }                
                
                for (AasEnumLiteral l: e.literals()) {
                    boolean last = e.isLast(l);
                    println("AasEnumLiteral {");
                    increaseIndent();
                    //printlnField("ordinal", );
                    String lName = l.getIdShort().replace(",", "").replace("/", " ")
                        .replace("(", "").replace(")", "").trim(); // -> oktoflow model
                    printlnStringField("name", lName, isValue(l.getIdentifier()) 
                        || isValue(l.getDescription()) || isValue(l.getValue()) || isValue(l.getValueId()));
                    printlnStringField("identifier", l.getIdentifier(), true);
                    printlnStringField("description", l.getDescription(), true);
                    String value = l.getValue();
                    if (!hasValues && requiresValues) {
                        value = l.getIdShort();
                    }
                    printlnStringField("value", value, true);
                    printlnStringField("semanticId", l.getValueId(), false);
                    decreaseIndent();
                    println("}" + (last ? "" : ","));
                }
                decreaseIndent();
                println("}");
                decreaseIndent();
                println("};");
            }
        }
    }
    
    /**
     * Returns whether {@code elem} shall be emitted.
     * 
     * @param elem the element
     * @return {@code true} for emit, {@code false} else
     */
    private boolean emit(AbstractAasElement elem) {
        boolean emit = true;
        // check if self vs. imported
        Import imp = AasImports.getImport(elem.getSemanticId());
        if (null != imp) {
            emit = selfImports.contains(imp);
        } else {
            emit = !AasImports.isKnownType(elem.getIdShort(), selfImports);
        }
        return emit;
    }

    /**
     * Writes the types in AAS result.
     * 
     * @param aasResult the AAS result
     */
    private void printTypes(AasSpecSummary aasResult) {
        for (AasType t: aasResult.types()) {
            if (null == t.getSmeType()) {
                getLogger().error("Type {} has no SME type assigned. Cannot print IVML. ", t.getIdShort());
            } else if (emit(t)) {
                String ivmlType = getIvmlType(t);
                if (null != ivmlType) {
                    println();
                    String varName = t.getIdShort();
                    // EASy-Problem allInstances() based on simpleNames
                    if (null != namePrefix && AasSmeType.SUBMODEL == t.getSmeType()) {
                        varName = namePrefix + varName;
                    }
                    println(ivmlType + " " + validateVariableName(varName) + " = {");
                    increaseIndent();
                    String name = validateName(t.getDisplayName());
                    if (null != namePrefix) {
                        printlnStringField("name", namePrefix + name, true);
                        printlnStringField("idShort", name, true);    
                    } else {
                        printlnStringField("name", name, true);    
                    }
                    printlnStringField("semanticId", t.getSemanticId(), true);
                    printlnField("multiSemanticIds", t.hasMultiSemanticIds(), true, false);
                    printlnStringField("description", t.getDescription(), true);
                    printlnStringField("versionIdentifier", aasResult.getVersionIdentifier(), true);
                    printlnField("isGeneric", t.isGeneric(), true, false);
                    printlnField("allowDuplicates", t.isAllowDuplicates(), true, false);
                    printlnField("ordered", t.isOrdered(), true, false);
                    printlnField("fixedName", t.isFixedIdShort(), true, false);
                    println("fields = {");
                    increaseIndent();
                    for (AasField f: t.fields()) {
                        printField(f, t);
                    }
                    decreaseIndent();
                    println("}");
                    decreaseIndent();
                    println("};");
                }
            }
        }
    }
    
    /**
     * Returns the IVML type of {@code type}.
     * 
     * @param type the type
     * @return the IVML type, may be <b>null</b>
     */
    private String getIvmlType(AasType type) {
        String ivmlType;
        switch (type.getSmeType()) {
        case SUBMODEL:
            ivmlType = "AasSubmodelType";
            break;
        case SUBMODEL_LIST: // preliminary
            ivmlType = "AasSubmodelListType";
            break;
        case SUBMODEL_ELEMENT_LIST: // preliminary
            ivmlType = "AasSubmodelElementListType";
            break;
        case SUBMODEL_ELEMENT_COLLECTION:
            ivmlType = "AasSubmodelElementCollectionType";
            break;
        case ENTITY:
            ivmlType = "AasEntityType";
            break;
        case FILE:
            ivmlType = "AasFileResourceType";
            break;
        default:
            ivmlType = null;
            break;
        }
        return ivmlType;
    }
    
    /**
     * Returns the IVML type of {@code field}. Adjusts multi-valued field type to comply with
     * the oktoflow metamodel and the generation.
     * 
     * @param field the field
     * @return the IVML type
     */
    private String getIvmlType(AasField field) {
        String result = field.getIvmlValueType(false);
        if (field.isMultiValued() || (field.getUpperCardinality() > 1 
            || (field.getLowerCardinality() >= 0 && field.getUpperCardinality() < 0))) {
            String tmp = null;
            switch (stripRefBy(result)) {
            case "IntegerType":
                tmp = "IntegerListType";
                break;
            case "BooleanType":
                tmp = "BooleanListType";
                break;
            case "DoubleType":
                tmp = "DoubleListType";
                break;
            case "FloatType":
                tmp = "FloatListType";
                break;
            case "StringType":
                tmp = "StringListType";
                break;
            default:
                tmp = null;
                break;
            }
            if (null != tmp) {
                result = "refBy(" + tmp + ")";
            }
        }
        return result;        
    }
    
    /**
     * Prints {@code field} of {@code type}.
     * 
     * @param field the field
     * @param type the type
     */
    private void printField(AasField field, AasType type) {
        boolean last = type.isLast(field);
        println("AasField {");
        increaseIndent();
        printlnStringField("name", validateName(field.getIdShort()), true);
        if (field.getDisplayName() != null && !field.getIdShort().equals(field.getDisplayName())) {
            printlnStringField("displayName", field.getDisplayName(), true);
        }
        printlnStringField("semanticId", field.getSemanticId(), true);
        printlnField("multiSemanticIds", field.hasMultiSemanticIds(), true, false);
        printlnField("isGeneric", field.isGeneric(), true, false);
        printlnField("counting", field.isMultiValued(), true, false);
        boolean afterMaxInstances = isValue(field.getDescription()) || isValue(field.getExampleValues()); 
        boolean afterMinInstances = field.getUpperCardinality() >= 0 || afterMaxInstances;
        printlnField("type", getIvmlType(field), isValue(field.getAspect()) 
            || field.getLowerCardinality() >= 0 || afterMinInstances, null);
        printlnStringField("aspect", field.getAspect(), true);
        printlnCardinalityField("minimumInstances", field.getLowerCardinality(), afterMinInstances);
        String[] examples = field.getExampleValues();
        printlnCardinalityField("maximumInstances", field.getUpperCardinality(), afterMaxInstances);
        if (null != examples) {
            String tmp = "{";
            for (int e = 0; e < examples.length; e++) {
                if (e > 0) {
                    tmp += ",";
                }
                tmp += "\"" + quoteIvmlString(examples[e]) + "\"";
            }
            tmp += "}";
            printlnField("examples", tmp, isValue(field.getDescription()));
        }
        // rest
        printlnStringField("description", field.getDescription(), false);
        decreaseIndent();
        println("}" + (last ? "" : ","));        
    }
    
    /**
     * Prints the imports.
     * 
     * @param aasResult the AAS result
     * @param projectName the own project name
     */
    private void printImports(AasSpecSummary aasResult, String projectName) {
        Set<Import> imports = new HashSet<Import>(); 
        for (AasType type : aasResult.types()) {
            addImport(AasImports.getImport(type.getSemanticId()), imports, projectName);
            for (AasField field : type.fields()) {
                addImport(AasImports.getImport(field.getSemanticId()), imports, projectName);
            }
        }
        for (Import imp : AasImports.sort(new ArrayList<>(imports))) {
            Version ver = imp.getVersion();
            String verPart = "";
            if (null != ver) {
                verPart = " with (" + imp.getProjectName() + ".version == v" + ver.toString() + ")"; 
            }
            println("import " + imp.getProjectName() + verPart + ";");
        }
    }
    
    /**
     * Adds an import. May modify {@link #selfImports} as a side effect.
     * 
     * @param imp the import (may be <b>null</b>)
     * @param imports the set of imports to be filled as a side effect
     * @param projectName the own project name
     * @see #selfImports
     */
    private void addImport(Import imp, Set<Import> imports, String projectName) {
        if (null != imp) {
            if (imp.getProjectName().equals(projectName)) {
                selfImports.add(imp);
            } else {
                imports.add(imp);
            }
        }
    }

    /**
     * Writes IVML for the given {@code aasResult}.
     * 
     * @param aasResult the AAS result
     */
    public void toIvml(AasSpecSummary aasResult) {
        String projectName = getProjectName(aasResult);
        println("project " + projectName + " {");
        increaseIndent();
        if (aasResult.getVersion() != null) {
            println();
            println("version v" + aasResult.getVersion().toString() + ";");
        }
        println();
        println("import AASDataTypes;");
        printImports(aasResult, projectName);
        println();
        println("annotate BindingTime bindingTime = BindingTime::compile to .;");
        printEnums(aasResult);
        printTypes(aasResult);

        println();
        println("freeze {");
        increaseIndent();
        println(".;");
        decreaseIndent();
        println("} but (f|f.bindingTime >= BindingTime.runtimeMon);");
        
        decreaseIndent();
        println("}");
        
        closer.accept(out);
    }
    
    /**
     * Returns the IVML project name for {@code aasResult}.
     * 
     * @param aasResult the AAS result
     * @return the project name
     */
    public String getProjectName(AasSpecSummary aasResult) {
        String projectName = aasResult.getProjectName();
        if (null == projectName) {
            Optional<AasType> main = aasResult.getMainSubmodel();
            if (main.isPresent()) {
                String specNumber = aasResult.getSpecNumber();
                if (!specNumber.startsWith("0")) {
                    specNumber = "0" + specNumber; 
                }
                projectName = "IDTA_" + specNumber + "_" + main.get().getIdShort();
            } else {
                projectName = "Unknown";
            }
        }
        return projectName;
    }

    /**
     * Writes IVML for the given {@code aasResult}. Only applied if this writer was created with a file name
     * {@link #IvmlWriter(String)}.
     * 
     * @param aasResult the AAS result
     */
    public void toIvmlText(AasSpecSummary aasResult) throws IOException {
        if (fileName != null) {
            String textFilename = fileName;
            int pos = fileName.lastIndexOf(".");
            if (pos > 0) {
                textFilename = fileName.substring(0, pos) + ".text";
            }
            PrintStream outTmp = out;
            String projectName = getProjectName(aasResult);
            try (FileOutputStream fos = new FileOutputStream(textFilename)) {
                out = new PrintStream(fos);
                for (AasType t: aasResult.types()) {
                    if (t.getSmeType() != null && emit(t)) {
                        String ivmlType = getIvmlType(t);
                        if (null != ivmlType) {
                            String desc = t.getDescription();
                            if (!isValue(desc)) {
                                desc = "Declaration for AAS type " + t.getIdShort();
                            }
                            println(projectName + "::" + validateVariableName(t.getIdShort()) + " = " + desc);
                        }
                    }
                }
            } finally {
                out = outTmp;
            }
        }
    }
    
    /**
     * Validates an idShort before output.
     * 
     * @param idShort the idshort
     * @return the idshort
     */
    private String validateName(String idShort) {
        String result = idShort;
        if (isNoIdShort(idShort)) {
            result = "<NoIdShort>"; // IVML name must be given
        }
        return result;
    }

    /**
     * Validates an idShort before output.
     * 
     * @param idShort the idshort
     * @return the idshort
     */
    private String validateVariableName(String idShort) {
        String result = idShort;
        if (isNoIdShort(idShort)) {
            result = "NoIdShort_" + (idCounter++); // IVML name must be given
        }
        return result;
    }
    
    /**
     * Returns whether {@code idShort} indicates that there shall be none.
     * 
     * @param idShort the idshort to test
     * @return {@code true} for no idShort, {@code false} else
     */
    private boolean isNoIdShort(String idShort) {
        boolean result = false;
        if (null != idShort) {
            String tmp = ParsingUtils.removeWhitespace(idShort).toLowerCase();
            result = tmp.equals("<noidshort>") || tmp.equals("<no_idshort>");
        }
        return result;
    }
    
    /**
     * Returns the logger instance for this class.
     * 
     * @return the logger instance
     */
    private static Logger getLogger() {
        logger = FallbackLogger.getLogger(logger, IvmlWriter.class, ParsingUtils.getLoggingLevel());
        return logger;
    }

}