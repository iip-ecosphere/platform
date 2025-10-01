/**
 * ******************************************************************************
 * Copyright (c) {2022} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.configuration.ivml;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import net.ssehub.easy.basics.messages.Status;
import net.ssehub.easy.reasoning.core.reasoner.Message;
import net.ssehub.easy.reasoning.core.reasoner.ReasoningResult;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.cst.AttributeVariable;
import net.ssehub.easy.varModel.cst.CompoundAccess;
import net.ssehub.easy.varModel.cst.ConstantValue;
import net.ssehub.easy.varModel.cst.ConstraintSyntaxTree;
import net.ssehub.easy.varModel.cst.ContainerOperationCall;
import net.ssehub.easy.varModel.cst.Let;
import net.ssehub.easy.varModel.model.AbstractProjectVisitor;
import net.ssehub.easy.varModel.model.AbstractVariable;
import net.ssehub.easy.varModel.model.Attribute;
import net.ssehub.easy.varModel.model.AttributeAssignment;
import net.ssehub.easy.varModel.model.Comment;
import net.ssehub.easy.varModel.model.CompoundAccessStatement;
import net.ssehub.easy.varModel.model.Constraint;
import net.ssehub.easy.varModel.model.ContainableModelElement;
import net.ssehub.easy.varModel.model.DecisionVariableDeclaration;
import net.ssehub.easy.varModel.model.FreezeBlock;
import net.ssehub.easy.varModel.model.ModelQuery;
import net.ssehub.easy.varModel.model.ModelQueryException;
import net.ssehub.easy.varModel.model.OperationDefinition;
import net.ssehub.easy.varModel.model.PartialEvaluationBlock;
import net.ssehub.easy.varModel.model.Project;
import net.ssehub.easy.varModel.model.ProjectImport;
import net.ssehub.easy.varModel.model.ProjectInterface;
import net.ssehub.easy.varModel.model.datatypes.Compound;
import net.ssehub.easy.varModel.model.datatypes.DerivedDatatype;
import net.ssehub.easy.varModel.model.datatypes.Enum;
import net.ssehub.easy.varModel.model.datatypes.EnumLiteral;
import net.ssehub.easy.varModel.model.datatypes.IDatatype;
import net.ssehub.easy.varModel.model.datatypes.OrderedEnum;
import net.ssehub.easy.varModel.model.datatypes.Reference;
import net.ssehub.easy.varModel.model.datatypes.Sequence;
import net.ssehub.easy.varModel.model.datatypes.TypeQueries;
import net.ssehub.easy.varModel.model.filter.AbstractVariableInConstraintFinder;
import net.ssehub.easy.varModel.model.filter.FilterType;
import net.ssehub.easy.varModel.model.values.BooleanValue;
import net.ssehub.easy.varModel.model.values.EnumValue;
import net.ssehub.easy.varModel.model.values.IntValue;
import net.ssehub.easy.varModel.model.values.StringValue;
import net.ssehub.easy.varModel.model.values.Value;

/**
 * Utilities to access IVML/configuration values.
 * 
 * @author Holger Eichelberger, SSE
 */
public class IvmlUtils {

    /**
     * Returns whether the given {@code var} is of compound type with name {@code typeName}.
     * 
     * @param var the variable
     * @param typeName the type name of the compound
     * @return {@code true} if {@code var} is of a compound type with {@code typeName}, {@code false} else
     * @see #isOfCompoundType(AbstractVariable, String)
     */
    public static boolean isOfCompoundType(IDecisionVariable var, String typeName) {
        return isOfCompoundType(var.getDeclaration(), typeName);
    }

    /**
     * Returns whether the given {@code var} is of compound type with name {@code typeName}.
     * 
     * @param var the variable
     * @param typeName the type name of the compound
     * @return {@code true} if {@code var} is of a compound type with {@code typeName}, {@code false} else
     */
    public static boolean isOfCompoundType(AbstractVariable var, String typeName) {
        IDatatype type = var.getType();
        return TypeQueries.isCompound(type) && typeName.equals(type.getName());
    }

    /**
     * Returns a nested variable, returning <b>null</b> if {@code var} is null.
     * 
     * @param var the variable
     * @param nested the name of the nested variable
     * @return the nested variable or <b>null</b>
     */
    public static IDecisionVariable getNestedSafe(IDecisionVariable var, String nested) {
        return null == var ? null : var.getNestedElement(nested);
    }
    
    /**
     * Returns a string value from the given {@code var}.
     * 
     * @param var the variable (may be <b>null</b>)
     * @param deflt the default value to return if no value can be obtained
     * @return the value or {@code deflt}
     */
    public static String getStringValue(IDecisionVariable var, String deflt) {
        String result;
        if (var == null) {
            result = deflt;
        } else {
            Value val = var.getValue();
            if (!(val instanceof StringValue)) {
                result = deflt;
            } else {
                result = ((StringValue) val).getValue();
            }
        }
        return result;
    }

    /**
     * Returns a string value of the {@code nested} field of {@code var}.
     * 
     * @param var the variable (may be <b>null</b>)
     * @param nested the nested field to take the value from
     * @param deflt the default value to return if no value can be obtained
     * @return the value or {@code deflt}
     */
    public static String getStringValue(IDecisionVariable var, String nested, String deflt) {
        return getStringValue(null == var ? null : var.getNestedElement(nested), deflt);
    }

    /**
     * Returns an int value of the {@code nested} field of {@code var}.
     * 
     * @param var the variable (may be <b>null</b>)
     * @param nested the nested field to take the value from
     * @param deflt the default value to return if no value can be obtained
     * @return the value or {@code deflt}
     */
    public static int getIntValue(IDecisionVariable var, String nested, int deflt) {
        return getIntValue(null == var ? null : var.getNestedElement(nested), deflt);
    }

    /**
     * Returns a Boolean value of the {@code nested} field of {@code var}.
     * 
     * @param var the variable (may be <b>null</b>)
     * @param nested the nested field to take the value from
     * @param deflt the default value to return if no value can be obtained
     * @return the value or {@code deflt}
     */
    public static boolean getBooleanValue(IDecisionVariable var, String nested, boolean deflt) {
        return getBooleanValue(null == var ? null : var.getNestedElement(nested), deflt);
    }

    /**
     * Returns an enum value from the given {@code var}.
     * 
     * @param var the variable (may be <b>null</b>)
     * @return the value or <b>null</b>
     */
    public static EnumValue getEnumValue(IDecisionVariable var) {
        EnumValue result = null;
        if (var != null) {
            Value val = var.getValue();
            if (val instanceof EnumValue) {
                result = (EnumValue) val;
            }
        }
        return result;
    }
    
    /**
     * Turns an enum value to its name.
     * 
     * @param value the value, may be <b>null</b>
     * @param deflt the default value to return if no value can be obtained
     * @return the name of the enum literal or {@code deflt}
     */
    public static String toName(EnumValue value, String deflt) {
        String result;
        if (null == value) {
            result = deflt;
        } else {
            result = value.getValue().getName();
        }
        return result;
    }

    /**
     * Returns an integer value from the given {@code var}.
     * 
     * @param var the variable (may be <b>null</b>)
     * @param deflt the default value to return if no value can be obtained
     * @return the value or {@code deflt}
     */
    public static int getIntValue(IDecisionVariable var, int deflt) {
        int result;
        if (var == null) {
            result = deflt;
        } else {
            Value val = var.getValue();
            if (!(val instanceof IntValue)) {
                result = deflt;
            } else {
                result = ((IntValue) val).getValue();
            }
        }
        return result;
    }

    /**
     * Returns a Boolean value from the given {@code var}.
     * 
     * @param var the variable (may be <b>null</b>)
     * @param deflt the default value to return if no value can be obtained
     * @return the value or {@code deflt}
     */
    public static boolean getBooleanValue(IDecisionVariable var, boolean deflt) {
        boolean result;
        if (var == null) {
            result = deflt;
        } else {
            Value val = var.getValue();
            if (!(val instanceof BooleanValue)) {
                result = deflt;
            } else {
                result = ((BooleanValue) val).getValue();
            }
        }
        return result;
    }

    /**
     * Returns the name of {@code var} taking <b>null</b> into account.
     * 
     * @param var the variable
     * @param dflt the default if {@code var} is <b>null</b>
     * @return the name or {@code dflt}
     */
    public static String getVarNameSafe(AbstractVariable var, String dflt) {
        return null == var ? dflt : var.getName();
    }

    /**
     * Returns the constant value of {@code cst} if {@code cst} is a constant expression.
     * 
     * @param cst the constraint syntax tree
     * @return the constant value or <b>null</b>
     */
    public static Value getConstValue(ConstraintSyntaxTree cst) {
        Value result = null;
        if (cst instanceof ConstantValue) {
            result = ((ConstantValue) cst).getConstantValue();
        }
        return result;
    }

    /**
     * Returns the constant int value of {@code cst} if {@code cst} is a constant int expression.
     * 
     * @param cst the constraint syntax tree
     * @param dflt the default value if {@code cst} is not a constant int expression
     * @return the constant value or {@code dflt}
     */
    public static int getIntValue(ConstraintSyntaxTree cst, int dflt) {
        int result = dflt;
        Value val = getConstValue(cst);
        if (val instanceof IntValue) {
            result = ((IntValue) val).getValue().intValue();
        }
        return result;
    }
    
    /**
     * Analyzes/prints the relevant information from reasoning messages.
     * 
     * @param res the reasoning result to print
     * @param emitWarnings shall warnings be emitted
     * @param emitMessages shall messages be emitted
     * @return {@code true} for conflict, {@code false} for ok
     */
    public static boolean analyzeReasoningResult(ReasoningResult res, boolean emitWarnings, boolean emitMessages) {
        boolean hasConflict = true;
        if (null != res) {
            hasConflict = res.hasConflict();
            int errorCount = 0;
            int templateErrorCount = 0;
            for (int m = 0; m < res.getMessageCount(); m++) {
                Message msg = res.getMessage(m);
                Status status = msg.getStatus();
                boolean emit = true;
                if (status == Status.ERROR) {
                    errorCount++;
                    boolean addressesTemplate = msg.getConflictProjects()
                        .stream()
                        .anyMatch(p -> isTemplate(p));
                    if (addressesTemplate) {
                        templateErrorCount++;
                        emit = false;
                    }
                } else if (status == Status.WARNING) {
                    emit = emitWarnings;
                }
                if (emit && emitMessages) {
                    System.out.println(msg.getDescription());
                    if (!msg.getConflictComments().isEmpty()) {
                        System.out.println(msg.getConflictComments());
                    }
                    if (!msg.getConflictSuggestions().isEmpty()) {
                        System.out.println(msg.getConflictSuggestions());
                    }
                }
            }
            if (templateErrorCount > 0 && templateErrorCount == errorCount) {
                hasConflict = false; // ignore if we have only template errors
            }
        } // else see init
        return hasConflict;
    }    

    /**
     * Analyzes the given reasoning result for template completion.
     * 
     * @param res the reasoning result
     * @param varName the variable representing the application
     * @return the names of the open variables
     */
    public static List<String> analyzeForTemplate(ReasoningResult res, String varName) {
        List<String> openVars = new ArrayList<>();
        for (int m = 0; m < res.getMessageCount(); m++) {
            Message msg = res.getMessage(m);
            Status status = msg.getStatus();
            if (status == Status.ERROR) {
                boolean addressesTemplate = msg.getConflictProjects()
                    .stream()
                    .anyMatch(p -> isTemplate(p) && containsVariable(p, varName));
                if (addressesTemplate) {
                    for (Set<AbstractVariable> tmp : msg.getConstraintVariables()) {
                        for (AbstractVariable var : tmp) {
                            openVars.add(var.getName());
                        }
                    }
                }
            } 
        }
        return openVars;
    }  
    
    /**
     * Dereferences {@code var}.
     * 
     * @param var the variable to dereference
     * @return the dereferenced variable
     */
    public static IDecisionVariable dereference(IDecisionVariable var) {
        return net.ssehub.easy.varModel.confModel.Configuration.dereference(var);
    }
    
    /**
     * Returns all templates in the related projects of {@code root}.
     * 
     * @param root the root project to start searching
     * @return the templates, may be empty
     */
    public static List<AbstractVariable> findTemplates(Project root) {
        List<AbstractVariable> result = new ArrayList<>();
        Set<Project> projects = new HashSet<>();
        collectImports(root, projects);
        for (Project p: projects) {
            for (int e = 0; e < p.getElementCount(); e++) {
                ContainableModelElement elt = p.getElement(e);
                if (elt instanceof AbstractVariable) {
                    AbstractVariable var = ((AbstractVariable) elt);
                    if (isTemplate(var)) {
                        result.add(var);
                    }
                }
            }
        }
        return result;
    }
    
    /**
     * Returns whether {@code prj} declares a variable named {@code varName}.
     * 
     * @param prj the project to inspect
     * @param varName the variable to look for
     * @return {@code true} for contained/declared, {@code false} else
     */
    public static boolean containsVariable(Project prj, String varName) {
        boolean result = false;
        try {
            result = ModelQuery.findVariable(prj, varName, null) != null;
        } catch (ModelQueryException e) {
            // result == false
        }
        return result;
    }

    /**
     * Returns whether {@code prj} represents an application template project.
     * 
     * @param prj the project to check
     * @return {@code true} for template, {@code false} else
     */
    public static boolean isTemplate(Project prj) {
        return prj.getName().startsWith("TemplatePart");
    }
    
    /**
     * Returns whether {@code var} represents an application template.
     * 
     * @param var the variable to check
     * @return {@code true} for template, {@code false} else
     */
    public static boolean isTemplate(AbstractVariable var) {
        return isApplication(var) && isInTemplate(var);
    }

    /**
     * Returns whether {@code var} represents an application.
     * 
     * @param var the variable to check
     * @return {@code true} for application, {@code false} else
     */
    public static boolean isApplication(AbstractVariable var) {
        return isOfCompoundType(var, "Application");
    }
        
    /**
     * Returns whether {@code var} is in a template.
     * 
     * @param var the variable to check
     * @return {@code true} if {@code var} is declared in a template, {@code false} else
     */
    public static boolean isInTemplate(AbstractVariable var) {
        return isTemplate(var.getProject());
    }

    /**
     * Returns the projects that use {@code var}.
     * 
     * @param root the project to start looking into
     * @param var the variable to search for
     * @return the projects using {@code var}
     */
    public static Set<Project> findProjectsUsingVariable(Project root, AbstractVariable var) {
        Set<Project> usedIn = new HashSet<>();
        root.accept(new VariableInProjectFinder(root, var, usedIn));
        return usedIn;
    }
    
    /**
     * Finds a variable use in IVML projects.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class VariableInProjectFinder extends AbstractProjectVisitor {

        private VariableInConstraintFinder varFinder;
        private AbstractVariable toFind;
        private Set<Project> result;
        
        /**
         * Creates the finder.
         * 
         * @param originProject the project where the search started at
         * @param toFind the variable to find
         * @param result the set of projects using {@code var} to be modified as a side effect
         */
        protected VariableInProjectFinder(Project originProject, AbstractVariable toFind, Set<Project> result) {
            super(originProject, FilterType.ALL);
            this.toFind = toFind;
            this.result = result;
            varFinder = new VariableInConstraintFinder(toFind, result);
        }
        
        @Override
        public void visitDecisionVariableDeclaration(DecisionVariableDeclaration decl) {
            if (decl == toFind) {
                result.add(decl.getProject());
            }
            if (decl.getDefaultValue() != null) {
                decl.getDefaultValue().accept(varFinder);
            }
        }

        @Override
        public void visitAttribute(Attribute attribute) {
            if (attribute == toFind) {
                result.add(attribute.getProject());
            }
            if (attribute.getDefaultValue() != null) {
                attribute.getDefaultValue().accept(varFinder);
            }
        }

        @Override
        public void visitConstraint(Constraint constraint) {
            constraint.getConsSyntax().accept(varFinder);
        }

        @Override
        public void visitFreezeBlock(FreezeBlock freeze) {
            for (int f = 0; f < freeze.getFreezableCount(); f++) {
                freeze.getFreezable(f).accept(this);
            }
        }

        @Override
        public void visitOperationDefinition(OperationDefinition opdef) {
            opdef.getOperation().getFunction().accept(varFinder);
        }

        @Override
        public void visitPartialEvaluationBlock(PartialEvaluationBlock block) {
        }

        @Override
        public void visitProjectInterface(ProjectInterface iface) {
        }

        @Override
        public void visitComment(Comment comment) {
        }

        @Override
        public void visitAttributeAssignment(AttributeAssignment assignment) {
            for (int c = 0; c < assignment.getConstraintsCount(); c++) {
                assignment.getConstraint(c).accept(this);
            }
            for (int d = 0; d < assignment.getDeclarationCount(); d++) {
                assignment.getDeclaration(d).accept(this);
            }
        }

        @Override
        public void visitCompoundAccessStatement(CompoundAccessStatement access) {
            access.getCompoundVariable().accept(this);
        }

        @Override
        public void visitEnum(Enum eenum) {
        }

        @Override
        public void visitOrderedEnum(OrderedEnum eenum) {
        }

        @Override
        public void visitCompound(Compound compound) {
            for (int c = 0; c < compound.getConstraintsCount(); c++) {
                compound.getConstraint(c).accept(this);
            }
            for (int d = 0; d < compound.getDeclarationCount(); d++) {
                compound.getDeclaration(d).accept(this);
            }
        }

        @Override
        public void visitDerivedDatatype(DerivedDatatype datatype) {
            for (int c = 0; c < datatype.getConstraintCount(); c++) {
                datatype.getConstraint(c).accept(this);
            }
        }

        @Override
        public void visitEnumLiteral(EnumLiteral literal) {
        }

        @Override
        public void visitReference(Reference reference) {
        }

        @Override
        public void visitSequence(Sequence sequence) {
        }

        @Override
        public void visitSet(net.ssehub.easy.varModel.model.datatypes.Set set) {
        }
        
    }

    /**
     * Finds variables in IVML expressions. 
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class VariableInConstraintFinder extends AbstractVariableInConstraintFinder {

        private AbstractVariable toFind;
        private Set<Project> result; 

        /**
         * Creates the finder.
         * 
         * @param toFind the variable to find
         * @param result the set of projects using {@code var} to be modified as a side effect
         */
        protected VariableInConstraintFinder(AbstractVariable toFind, Set<Project> result) {
            super(true);
            this.toFind = toFind;
            this.result = result;
        }

        @Override
        public void visitAnnotationVariable(AttributeVariable variable) {
            addVariable(variable.getVariable());
            if (null != variable.getQualifier()) {
                variable.getQualifier().accept(this);
            }
        }

        @Override
        public void visitLet(Let let) {
            let.getInitExpression().accept(this);
            let.getInExpression().accept(this);
        }

        @Override
        public void visitContainerOperationCall(ContainerOperationCall call) {
            call.getContainer().accept(this);
            call.getExpression().accept(this);
        }

        @Override
        public void visitCompoundAccess(CompoundAccess access) {
            addVariable(access.getResolvedSlot());
        }

        @Override
        protected void addVariable(AbstractVariable declaration) {
            if (declaration == toFind) {
                result.add(declaration.getProject());
            }
        }
        
    }
    
    /**
     * Collects all imports, i.e., the import hull starting with {@code prj}.
     * 
     * @param prj the project to start the imports with
     * @param projects the imported projects
     */
    public static void collectImports(Project prj, Set<Project> projects) {
        if (null != prj && !projects.contains(prj)) {
            projects.add(prj);
            for (int i = 0; i < prj.getImportsCount(); i++) {
                ProjectImport imp = prj.getImport(i);
                collectImports(imp.getResolved(), projects);
            }
        }
    }
    
    /**
     * Iterates through specified elements of {@code prj}.
     * 
     * @param <E> the type to select/iterate over
     * @param prj the project to take the elements from
     * @param cls representation of the type/filter
     * @param consumer called for each matching element
     */
    public static <E> void iterElements(Project prj, Class<E> cls, Consumer<E> consumer) {
        for (int e = 0; e < prj.getElementCount(); e++) {
            ContainableModelElement elt = prj.getElement(e);
            if (cls.isInstance(elt)) {
                consumer.accept(cls.cast(elt));
            }
        }
    }

}
