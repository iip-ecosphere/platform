package de.iip_ecosphere.platform.configuration.ivml;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.function.Predicate;

import de.iip_ecosphere.platform.configuration.ModelInfo;
import de.iip_ecosphere.platform.support.aas.LangString;
import de.iip_ecosphere.platform.support.aas.Type;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection.SubmodelElementCollectionBuilder;
import de.iip_ecosphere.platform.support.iip_aas.AasUtils;
import net.ssehub.easy.instantiation.core.model.vilTypes.configuration.Configuration;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.cst.ConstantValue;
import net.ssehub.easy.varModel.cst.ConstraintSyntaxTree;
import net.ssehub.easy.varModel.cst.Variable;
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
import net.ssehub.easy.varModel.model.datatypes.IDatatype;
import net.ssehub.easy.varModel.model.datatypes.Reference;
import net.ssehub.easy.varModel.model.values.Value;

class TypeMapper {

    private Configuration cfg;
    private Set<Project> doneProjects = new HashSet<>();
    private Set<String> doneTypes = new HashSet<>();
    private SubmodelElementCollectionBuilder builder;
    private Predicate<AbstractVariable> variableFilter;
    private Stack<Map<String, Object>> assignments = new Stack<>();
    
    /**
     * Creates a type mapper instance.
     * 
     * @param cfg the configuration to map the declared types for
     * @param variableFilter a variable filter to exclude certain variables/types
     * @param builder the builder for {@link #META_TYPE_NAME}
     */
    TypeMapper(Configuration cfg, Predicate<AbstractVariable> variableFilter, 
        SubmodelElementCollectionBuilder builder) {
        this.cfg = cfg;
        this.builder = builder;
        this.variableFilter = variableFilter;
    }
    
    /**
     * Maps all types.
     */
    void mapTypes() {
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
     * Maps a compound type into the SMEC {@value #META_TYPE_NAME}. May be called for duplicates but leads only to
     * one entry.
     * 
     * @param type the compound type
     */
    private void mapCompoundType(Compound type) {
        String typeId = AasUtils.fixId(type.getName());
        if (!isDoneType(typeId)) {
            SubmodelElementCollectionBuilder typeB = builder.createSubmodelElementCollectionBuilder(
                typeId, false, false);
            Set<String> doneSlots = new HashSet<>();
            mapCompoundSlots(type, type, typeB, doneSlots);
            mapRefines(type, typeB, doneSlots);
            typeB.createPropertyBuilder(AasIvmlMapper.SHORTID_PREFIX_META.apply("abstract"))
                .setValue(Type.BOOLEAN, type.isAbstract())
                .build();
            typeB.createPropertyBuilder(AasIvmlMapper.SHORTID_PREFIX_META.apply("refines"))
                .setValue(Type.STRING, getRefines(type))
                .build();
            typeB.build();
        }
    }

    /**
     * Maps an IVML type into the SMEC {@value #META_TYPE_NAME}. May be called for duplicates but leads only to
     * one entry.
     * 
     * @param type the IVML type
     */
    void mapType(IDatatype type) {
        type = Reference.dereference(type);
        if (type instanceof DerivedDatatype) {
            mapDerivedType((DerivedDatatype) type);
        } else {
            if (type instanceof Container) {
                type = ((Container) type).getContainedType();
            }
            if (type instanceof Compound) {
                mapCompoundType((Compound) type);
            }
        }
    }
    
    /**
     * Returns whether {@code typeId} represents a done type (in {@link #doneTypes}). If not, adds {@cpde typeId} 
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
     * Maps a derived type into the SMEC {@value #META_TYPE_NAME}. May be called for duplicates but leads only to
     * one entry.
     * 
     * @param type the derived type
     */
    private void mapDerivedType(DerivedDatatype type) {
        String typeId = AasUtils.fixId(type.getName());
        IDatatype baseType = type.getBasisType();
        if (!isDoneType(typeId)) {
            SubmodelElementCollectionBuilder typeB = builder.createSubmodelElementCollectionBuilder(
                typeId, false, false);
            typeB.createPropertyBuilder(AasIvmlMapper.SHORTID_PREFIX_META.apply("refines"))
                .setValue(Type.STRING, IvmlDatatypeVisitor.getUnqualifiedType(baseType))
                .build();
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
     * @param typeB the type builder
     * @param doneSlots already done slot names
     */
    private void mapRefines(Compound type, SubmodelElementCollectionBuilder typeB, Set<String> doneSlots) {
        for (int r = 0; r < type.getRefinesCount(); r++) {
            Compound refines = type.getRefines(r);
            mapCompoundSlots(refines, refines, typeB, doneSlots);
            mapRefines(refines, typeB, doneSlots);
        }
    }

    /**
     * Maps slots of a compound type including annotation assignments.
     * 
     * @param cnt the container (compound or annotation assignment)
     * @param type the containing compound type
     * @param typeB the type builder
     * @param doneSlots already done slot names
     */
    private void mapCompoundSlots(IDecisionVariableContainer cnt, Compound type, SubmodelElementCollectionBuilder typeB,
        Set<String> doneSlots) {
        for (int i = 0; i < cnt.getElementCount(); i++) {
            mapCompoundSlot(cnt.getElement(i), type, typeB, doneSlots);
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
            mapCompoundSlots(assng, type, typeB, doneSlots);
            assignments.pop();
        }
    }

    /**
     * Map a compound slot.
     * 
     * @param slot the slot to map
     * @param type the containing compound type
     * @param typeB the type builder
     * @param doneSlots already done slot names
     */
    private void mapCompoundSlot(DecisionVariableDeclaration slot, Compound type, 
        SubmodelElementCollectionBuilder typeB, Set<String> doneSlots) {
        // if we get into trouble with property ids, we have to sub-structure that
        String slotName = AasUtils.fixId(slot.getName());
        if (!doneSlots.contains(slotName) && variableFilter.test(slot)) {
            doneSlots.add(slotName);
            String lang = AasIvmlMapper.getLang();
            IDatatype slotType = slot.getType();
            SubmodelElementCollectionBuilder propB = typeB.createSubmodelElementCollectionBuilder(slotName, 
                true, false);
            propB.createPropertyBuilder("name")
                .setValue(Type.STRING, slotName)
                .setDescription(new LangString(ModelInfo.getCommentSafe(slot), lang))
                .build();
            propB.createPropertyBuilder("type")
                .setValue(Type.STRING, IvmlDatatypeVisitor.getUnqualifiedType(slotType))
                .build();
            int uiGroup = 1; // default, always there, mandatory
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
            propB.createPropertyBuilder("uiGroup")
                .setValue(Type.INTEGER, uiGroup)
                .build();
            propB.build();
            if (slotType != type) {
                mapType(slotType);
            }
        }
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
