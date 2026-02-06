/**
 * ******************************************************************************
 * Copyright (c) {2023} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.configuration.easyProducer.ivml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.function.Function;
import java.util.function.Predicate;

import de.iip_ecosphere.platform.configuration.easyProducer.ModelInfo;
import de.iip_ecosphere.platform.support.aas.LangString;
import de.iip_ecosphere.platform.support.aas.SubmodelElementContainerBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElementList.SubmodelElementListBuilder;
import de.iip_ecosphere.platform.support.aas.Type;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection.SubmodelElementCollectionBuilder;
import de.iip_ecosphere.platform.support.aas.AasUtils;
import net.ssehub.easy.instantiation.core.model.vilTypes.configuration.Configuration;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.cst.ConstantValue;
import net.ssehub.easy.varModel.cst.ConstraintSyntaxTree;
import net.ssehub.easy.varModel.cst.Variable;
import net.ssehub.easy.varModel.cstEvaluation.EvaluationVisitor;
import net.ssehub.easy.varModel.model.AbstractVariable;
import net.ssehub.easy.varModel.model.Attribute;
import net.ssehub.easy.varModel.model.AttributeAssignment;
import net.ssehub.easy.varModel.model.ContainableModelElement;
import net.ssehub.easy.varModel.model.DecisionVariableDeclaration;
import net.ssehub.easy.varModel.model.IDecisionVariableContainer;
import net.ssehub.easy.varModel.model.IvmlDatatypeVisitor;
import net.ssehub.easy.varModel.model.Project;
import net.ssehub.easy.varModel.model.AttributeAssignment.Assignment;
import net.ssehub.easy.varModel.model.datatypes.Compound;
import net.ssehub.easy.varModel.model.datatypes.Container;
import net.ssehub.easy.varModel.model.datatypes.CustomDatatype;
import net.ssehub.easy.varModel.model.datatypes.DerivedDatatype;
import net.ssehub.easy.varModel.model.datatypes.Enum;
import net.ssehub.easy.varModel.model.datatypes.EnumLiteral;
import net.ssehub.easy.varModel.model.datatypes.IDatatype;
import net.ssehub.easy.varModel.model.datatypes.Reference;
import net.ssehub.easy.varModel.model.values.Value;

/**
 * Maps types to IVML.
 * 
 * @author Holger Eichelberger, SSE
 */
class TypeMapper {
    
    public static final int UIGROUP_SPACING = 100;
    public static final int UIGROUP_POS_FRONT = 99;

    private Configuration cfg;
    private Set<Project> doneProjects = new HashSet<>();
    private Set<String> doneTypes = new HashSet<>();
    private SubmodelElementContainerBuilder builder;
    private Predicate<AbstractVariable> variableFilter;
    private Stack<Map<String, Object>> assignments = new Stack<>();
    private Function<String, String> metaShortId;
    private Map<String, Integer> uiGroups = new HashMap<>();
    private Set<String> requiredTypes;
    
    /**
     * Creates a type mapper instance.
     * 
     * @param cfg the configuration to map the declared types for
     * @param variableFilter a variable filter to exclude certain variables/types
     * @param builder the builder to place AAS elements into
     * @param metaShortId function to build a meta shortId property name
     * @param requiredTypes required type names
     */
    TypeMapper(Configuration cfg, Predicate<AbstractVariable> variableFilter, 
        SubmodelElementContainerBuilder builder, Function<String, String> metaShortId, Set<String> requiredTypes) {
        this.cfg = cfg;
        this.builder = builder;
        this.variableFilter = variableFilter;
        this.metaShortId = metaShortId;
        this.requiredTypes = requiredTypes;
    }
    
    /**
     * Maps all types.
     */
    void mapTypes() {
        mapPrimitiveType("String");
        mapPrimitiveType("Boolean");
        mapPrimitiveType("Real");
        mapPrimitiveType("Integer");
        mapTypes(cfg.getConfiguration().getProject());
    }

    /**
     * Maps all types declared in {@code project} and imported projects.
     * 
     * @param project the project to map
     */
    private void mapTypes(Project project) {
        if (null != project && !doneProjects.contains(project)) {
            doneProjects.add(project);
            for (int e = 0; e < project.getElementCount(); e++) {
                ContainableModelElement elt = project.getElement(e);
                if (elt instanceof CustomDatatype) {
                    mapType((CustomDatatype) elt);
                }
            }
            for (int i = 0; i < project.getImportsCount(); i++) {
                mapTypes(project.getImport(i).getResolved());
            }
        }
    }

    /**
     * Maps a primitive type.
     * 
     * @param name the name of the primitive type.
     */
    private void mapPrimitiveType(String name) {
        String typeId = AasUtils.fixId(name);
        if (!isDoneType(typeId)) {
            SubmodelElementCollectionBuilder typeB = builder.createSubmodelElementCollectionBuilder(typeId);
            addTypeKind(typeB, IvmlTypeKind.PRIMITIVE, metaShortId);
            typeB.build();
        }
    }

    /**
     * Adds an AAS type kind property.
     * 
     * @param typeB the type builder
     * @param type the type
     * @param metaShortId function to build a meta shortId property name
     */
    public static void addTypeKind(SubmodelElementContainerBuilder typeB, IDatatype type, 
        Function<String, String> metaShortId) {
        addTypeKind(typeB, IvmlTypeKind.asTypeKind(type), metaShortId);
    }
    
    /**
     * Adds an AAS type kind property.
     * 
     * @param typeB the type builder
     * @param kind the type kind
     * @param metaShortId function to build a meta shortId property name
     */
    public static void addTypeKind(SubmodelElementContainerBuilder typeB, IvmlTypeKind kind, 
        Function<String, String> metaShortId) {
        typeB.createPropertyBuilder(AasUtils.fixId(metaShortId.apply("typeKind")))
            .setValue(Type.INTEGER, kind.getId())
            .build();
    }

    /**
     * Maps a compound type. May be called for duplicates but leads only to one entry.
     * 
     * @param type the compound type
     */
    private void mapCompoundType(Compound type) {
        String typeId = AasUtils.fixId(type.getName());
        if (!isDoneType(typeId)) {
            SubmodelElementListBuilder typeB = builder.createSubmodelElementListBuilder(typeId);
            Map<String, SubmodelElementCollectionBuilder> doneSlots = new HashMap<>();
            mapCompoundSlots(type, type, type, typeB, doneSlots);
            mapRefines(type, type, typeB, doneSlots);
            for (SubmodelElementCollectionBuilder builder : sortMembers(type, doneSlots)) {
                builder.build();
            }
            typeB.createPropertyBuilder(metaShortId.apply("abstract"))
                .setValue(Type.BOOLEAN, type.isAbstract())
                .build();
            typeB.createPropertyBuilder(metaShortId.apply("refines"))
                .setValue(Type.STRING, getRefines(type))
                .build();
            addTypeKind(typeB, IvmlTypeKind.COMPOUND, metaShortId);
            typeB.build();
        }
    }
    
    /**
     * Sort members for display sequence in UI.
     * 
     * @param type the type to sort the members for
     * @param slots the SME builders per slot
     * @return the sorted members
     */
    private List<SubmodelElementCollectionBuilder> sortMembers(Compound type, 
        Map<String, SubmodelElementCollectionBuilder> slots) {
        List<SubmodelElementCollectionBuilder> result = new ArrayList<>();
        Set<String> done = null;
        if (type instanceof Compound) {
            List<String> names = new ArrayList<String>();
            done = new HashSet<>();
            collectMemberNames((Compound) type, names, done);
            Map<Integer, List<SubmodelElementCollectionBuilder>> namesByUiGroups = new HashMap<>();
            for (String name : names) {
                SubmodelElementCollectionBuilder elt = slots.get(name);
                if (null != elt) {
                    int uiGroup = getUiGroup(type.getName(), name);
                    int uiPos = uiGroup % UIGROUP_SPACING;
                    uiGroup /= UIGROUP_SPACING;
                    List<SubmodelElementCollectionBuilder> tmp = namesByUiGroups.get(uiGroup);
                    if (null == tmp) {
                        tmp = new ArrayList<>();
                        namesByUiGroups.put(uiGroup, tmp);
                    }
                    if (uiPos == 0) { // usually, this shall be the sorted sequence by name, i.e., at the end, see next
                        uiPos = -1;
                    }
                    if (uiPos == UIGROUP_POS_FRONT) {
                        uiPos = 0;
                    }
                    if (0 <= uiPos && uiPos < tmp.size()) {
                        tmp.add(uiPos, elt);
                    } else {
                        tmp.add(elt);
                    }
                    done.remove(name);
                } // EASy problems with Any
            }
            add(namesByUiGroups, result, 1, 1);   // mandatory UI groups
            add(namesByUiGroups, result, 0, 0);   // invisible UI group (optional, may not be there)
            add(namesByUiGroups, result, -1, -1); // optional UI groups
        } 
        return result;
    }

   /**
    * Adds all entries from {@code src} with matching key to {@code tgt}. Assumption: There are no holes between the 
    * groups.
    * 
    * @param src source structure
    * @param tgt target structure
    * @param start the start ui group/key
    * @param inc the increment, terminate immediately after first transfer if {@code 0}
    */
    private void add(Map<Integer, List<SubmodelElementCollectionBuilder>> src, 
        List<SubmodelElementCollectionBuilder> tgt, int start, int inc) {
        int u = start;
        while (src.get(u) != null) {
            tgt.addAll(src.get(u));
            if (inc == 0) {
                break;
            }
            u += inc;
        }
    }

    /**
     * Collects the member names for type.
     * 
     * @param type the compound type
     * @param result the resulting name sequence
     * @param done the done names
     */
    private void collectMemberNames(Compound type, List<String> result, Set<String> done) {
        if (!type.getProject().getName().equals("MetaConcepts")) {
            for (int r = 0; r < type.getRefinesCount(); r++) { // important: parents go first
                collectMemberNames(type.getRefines(r), result, done);
            }
            collectMemberNamesContainer(type, result, done);
        }
    }

    /**
     * Collects the member names for the given container.
     * 
     * @param cnt the container
     * @param result the resulting name sequence
     * @param done the done names
     */
    private void collectMemberNamesContainer(IDecisionVariableContainer cnt, List<String> result, Set<String> done) {
        for (int e = 0; e < cnt.getModelElementCount(); e++) { // important: keep sequence
            ContainableModelElement element = cnt.getModelElement(e);
            if (element instanceof AbstractVariable) {
                String name = element.getName();
                if (!done.contains(name)) {
                    done.add(name);
                    result.add(name); // uiGroup % 100 might affect position here
                }
            } else if (element instanceof AttributeAssignment) {
                collectMemberNamesContainer((AttributeAssignment) element, result, done);
            }
        }
    }

    /**
     * Maps an IVML type. May be called for duplicates but leads only to one entry.
     * 
     * @param type the IVML type
     */
    void mapType(IDatatype type) {
        type = Reference.dereference(type);
        if (type instanceof DerivedDatatype) {
            mapDerivedType((DerivedDatatype) type);
        } else if (type instanceof Enum) {
            mapEnumType((Enum) type);
        } else {
            if (type instanceof Container) {
                type = ((Container) type).getContainedType();
            } else if (type instanceof Compound) {
                mapCompoundType((Compound) type);
            }
        }
    }
    
    /**
     * Maps an IVML enum type.
     * 
     * @param type the type
     */
    private void mapEnumType(Enum type) {
        String typeId = AasUtils.fixId(type.getName());
        if (!isDoneType(typeId)) {
            SubmodelElementCollectionBuilder typeB = builder.createSubmodelElementCollectionBuilder(typeId);
            for (int l = 0; l < type.getLiteralCount(); l++) {
                EnumLiteral lit = type.getLiteral(l);
                SubmodelElementCollectionBuilder litB = typeB.createSubmodelElementCollectionBuilder(
                    AasUtils.fixId(lit.getName()));
                // value is reserved by BaSyx/AAS
                litB.createPropertyBuilder("varValue")
                    .setValue(Type.STRING, lit.getName())
                    .build();
                litB.createPropertyBuilder("ordinal")
                    .setValue(Type.INTEGER, lit.getOrdinal())
                    .build();
                litB.build();
            }
            addTypeKind(typeB, IvmlTypeKind.ENUM, metaShortId);
            typeB.build();
        }
    }
    
    /**
     * Returns whether {@code typeId} represents a done type (in {@link #doneTypes}). If not, adds {@code typeId} 
     * to {@link #doneTypes}.
     * 
     * @param typeId the type id to search for
     * @return {@code true} if the type is considered as done, {@code false} else
     */
    private boolean isDoneType(String typeId) {
        boolean known = doneTypes.contains(typeId);
        if (!known) {
            doneTypes.add(typeId);
        }
        return known;
    }

    /**
     * Maps a derived type. May be called for duplicates but leads only to one entry.
     * 
     * @param type the derived type
     */
    private void mapDerivedType(DerivedDatatype type) {
        String typeId = AasUtils.fixId(type.getName());
        IDatatype baseType = type.getBasisType();
        if (!isDoneType(typeId)) {
            SubmodelElementCollectionBuilder typeB = builder.createSubmodelElementCollectionBuilder(typeId);
            typeB.createPropertyBuilder(metaShortId.apply("refines"))
                .setValue(Type.STRING, IvmlDatatypeVisitor.getUnqualifiedType(baseType))
                .build();
            addTypeKind(typeB, type, metaShortId);
            typeB.build();
        }
        mapType(baseType);
    }

    /**
     * Returns the refined type of {@code type} as comma-separated string list.
     * 
     * @param type the type to refine
     * @return the refined types
     */
    private static String getRefines(Compound type) {
        String result = "";
        for (int i = 0; i < type.getRefinesCount(); i++) {
            if (result.length() > 0) {
                result += ", ";
            }
            result += IvmlDatatypeVisitor.getUnqualifiedType(type.getRefines(i));
        }
        return result;
    }

    /**
     * Maps refines of a compound type.
     * 
     * @param type the type
     * @param topType the top-level type the mapping started with
     * @param typeB the type builder
     * @param doneSlots already done slot names
     */
    private void mapRefines(Compound type, Compound topType, SubmodelElementListBuilder typeB, 
        Map<String, SubmodelElementCollectionBuilder> doneSlots) {
        for (int r = 0; r < type.getRefinesCount(); r++) {
            Compound refines = type.getRefines(r);
            mapCompoundSlots(refines, refines, topType, typeB, doneSlots);
            mapRefines(refines, topType, typeB, doneSlots);
        }
    }

    /**
     * Maps slots of a compound type including annotation assignments.
     * 
     * @param cnt the container (compound or annotation assignment)
     * @param type the containing compound type
     * @param topType the top-level type the mapping started with
     * @param typeB the type builder
     * @param doneSlots already done slot names
     */
    private void mapCompoundSlots(IDecisionVariableContainer cnt, Compound type, Compound topType, 
        SubmodelElementListBuilder typeB, Map<String, SubmodelElementCollectionBuilder> doneSlots) {
        for (int i = 0; i < cnt.getElementCount(); i++) {
            mapCompoundSlot(cnt.getElement(i), type, topType, typeB, doneSlots);
        }
        for (int a = 0; a < cnt.getAssignmentCount(); a++) {
            AttributeAssignment assng = cnt.getAssignment(a);
            Map<String, Object> thisLevelAssignments = new HashMap<>();
            for (int d = 0; d < assng.getAssignmentDataCount(); d++) {
                Assignment data = assng.getAssignmentData(d);
                ConstraintSyntaxTree expr = data.getExpression();
                Value val = null;
                // very simplistic interpretation of expressions; official -> ExpressionEvaluator
                if (expr instanceof Variable) {
                    IDecisionVariable var = cfg.getConfiguration().getDecision(((Variable) expr).getVariable());
                    if (null != var) {
                        val = var.getValue();
                    }
                } else if (expr instanceof ConstantValue) {
                    val = IvmlUtils.getConstValue(expr);
                }
                if (val != null) {
                    thisLevelAssignments.put(data.getName(), val.getValue());
                }
            }
            assignments.push(thisLevelAssignments);
            mapCompoundSlots(assng, type, topType, typeB, doneSlots);
            assignments.pop();
        }
    }

    /**
     * Adds the default value of {@code var} if there is a default value.
     * 
     * @param var the variable to take the default value from
     * @param varBuilder the variable builder to add the meta-value to
     * @param metaShortId function to build a meta shortId property name
     */
    static void addMetaDefault(IDecisionVariable var, SubmodelElementContainerBuilder varBuilder, 
        Function<String, String> metaShortId) {
        addMetaDefault(var.getConfiguration(), var.getDeclaration(), varBuilder, metaShortId);
    }
    
    // checkstyle: stop exception type check

    /**
     * Adds the default value of {@code var} if there is a default value.
     * 
     * @param cfg the configuration to use for reasoning/expression evaluation
     * @param decl the variable declaration to take the default value from
     * @param varBuilder the variable builder to add the meta-value to
     * @param metaShortId function to build a meta shortId property name
     */
    static void addMetaDefault(net.ssehub.easy.varModel.confModel.Configuration cfg, AbstractVariable decl, 
        SubmodelElementContainerBuilder varBuilder, Function<String, String> metaShortId) {
        ConstraintSyntaxTree dflt = decl.getDefaultValue();
        if (null != dflt) {
            EvaluationVisitor eval = new EvaluationVisitor(cfg, null, false, null);
            try {
                dflt.accept(eval);
                Value dfltValue = eval.getResult();
                eval.clear();
                if (dfltValue != null) {
                    ValueVisitor valueVisitor = new ValueVisitor();
                    dfltValue.accept(valueVisitor);
                    Object aasValue = valueVisitor.getAasValue();
                    if (null != aasValue) {
                        varBuilder.createPropertyBuilder(AasUtils.fixId(metaShortId.apply("default")))
                            .setValue(Type.STRING, aasValue.toString())
                            .build();
                    }
                }
            } catch (Throwable t) {
                // preliminary, cfg may not be complete, not a problem of "self"
            }
        }
    }

    // checkstyle: resume exception type check

    /**
     * Map a compound slot.
     * 
     * @param slot the slot to map
     * @param type the containing compound type
     * @param topType the top-level type the mapping started with
     * @param typeB the type builder
     * @param doneSlots already done slot names
     */
    private void mapCompoundSlot(DecisionVariableDeclaration slot, Compound type, Compound topType,
        SubmodelElementListBuilder typeB, Map<String, SubmodelElementCollectionBuilder> doneSlots) {
        // if we get into trouble with property ids, we have to sub-structure that
        String slotName = AasUtils.fixId(slot.getName());
        if (!doneSlots.containsKey(slotName) && variableFilter.test(slot)) {
            String lang = AasIvmlMapper.getLang();
            IDatatype slotType = slot.getType();
            SubmodelElementCollectionBuilder propB = typeB.createSubmodelElementCollectionBuilder(slotName);
            doneSlots.put(slotName, propB);
            propB.createPropertyBuilder("name")
                .setValue(Type.STRING, slotName)
                .setDescription(new LangString(lang, ModelInfo.getCommentSafe(slot)))
                .build();
            String typeName = IvmlDatatypeVisitor.getUnqualifiedType(slotType);
            propB.createPropertyBuilder("type")
                .setValue(Type.STRING, typeName)
                .build();
            int uiGroup = 100; // default, always there, mandatory
            Object tmp = getAssignmentValue("uiGroup"); 
            if (tmp instanceof Integer) {
                uiGroup = (Integer) tmp;
            } else {
                for (int a = 0; a < slot.getAttributesCount(); a++) {
                    Attribute attribute = slot.getAttribute(a);
                    if ("uiGroup".equals(attribute.getName())) {
                        uiGroup = IvmlUtils.getIntValue(attribute.getDefaultValue(), uiGroup);
                    }
                }
            }
            uiGroups.put(topType.getName() + "." + slotName, uiGroup);
            if (uiGroup >= 100 || uiGroup <= 100) {
                uiGroup /= 100;
            }
            propB.createPropertyBuilder("uiGroup")
                .setValue(Type.INTEGER, uiGroup)
                .build();
            addMetaDefault(cfg.getConfiguration(), slot, propB, metaShortId);
            addMetaRequired(propB, typeName, metaShortId, requiredTypes);
            if (slotType != type) {
                mapType(slotType);
            }
            addDisplayName(null, propB, typeName, slotName, metaShortId);
            // no propB.build() here, happens after sorting
        }
    }
    
    
    /**
     * Adds a potential display name. If no {@code displayName} is yet given, it might be potentially a default
     * mapping, e.g., for {@code OktoVersion} and {@code ver}.
     * 
     * @param displayName the display name known so far, may be <b>null</b> for none.
     * @param varBuilder the variable builder to add the meta-value to
     * @param typeName the type name
     * @param varName the variable name
     * @param metaShortId function to build a meta shortId property name
     */
    public static void addDisplayName(String displayName, SubmodelElementContainerBuilder varBuilder, String typeName, 
        String varName, Function<String, String> metaShortId) {
        if (null == displayName && "OktoVersion".equals(typeName) && "ver".equals(varName)) {
            // mitigate field misnomer for now
            displayName = "version";
        }
        if (null != displayName) {
            varBuilder.createPropertyBuilder(AasUtils.fixId(metaShortId.apply("displayName")))
                .setValue(Type.STRING, displayName)
                .build();
        }
    }
    
    /**
     * Adds an optional property indicating that the containing field is required.
     * 
     * @param varBuilder the variable builder to add the meta-value to
     * @param typeName the type name to look up
     * @param metaShortId function to build a meta shortId property name
     * @param requiredTypes the required types in which to look up {@code typeName}
     */
    public static void addMetaRequired(SubmodelElementContainerBuilder varBuilder, String typeName, 
        Function<String, String> metaShortId, Set<String> requiredTypes) {
        if (requiredTypes.contains(typeName)) {
            varBuilder.createPropertyBuilder(AasUtils.fixId(metaShortId.apply("required")))
                .setValue(Type.BOOLEAN, true)
                .build();
        }
    }

    /**
     * Returns the recorded UI group.
     * 
     * @param type the type name
     * @param slot the name of the slot within {@code type}
     * @return the uiGroup value, defaults to 100
     */
    public int getUiGroup(String type, String slot) {
        Integer tmp = uiGroups.get(type + "." + slot);
        return null == tmp ? 100 : tmp;
    }
    
    /**
     * Returns the top-most collected assignment value.
     * 
     * @param name the name of the assignment variable
     * @return the value or <b>null</b> for none
     */
    private Object getAssignmentValue(String name) {
        Object result = null;
        for (int i = assignments.size() - 1; null == result && i >= 0; i--) {
            result = assignments.get(i).get(name);
        }
        return result;
    }

}
