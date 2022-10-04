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

package de.iip_ecosphere.platform.configuration;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import net.ssehub.easy.varModel.confModel.AbstractConfigurationStatisticsVisitor;
import net.ssehub.easy.varModel.confModel.IConfigurationElement;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.model.AbstractVariable;
import net.ssehub.easy.varModel.model.AttributeAssignment;
import net.ssehub.easy.varModel.model.ModelElement;
import net.ssehub.easy.varModel.model.Project;
import net.ssehub.easy.varModel.model.datatypes.Compound;
import net.ssehub.easy.varModel.model.datatypes.TypeQueries;

/**
 * Implements an extended statistics visitor.
 * 
 * @author Holger Eichelberger, SSE
 */
public class StatisticsVisitor extends AbstractConfigurationStatisticsVisitor {

    /**
     * Extended statistics.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class Statistics extends AbstractConfigurationStatisticsVisitor.ConfigStatistics {

        private int metaVars = 0;
        private int metaVarsWithComment = 0;
        
        /**
         * Returns the number of meta variables.
         * 
         * @return the number of meta variables
         */
        public int getMetaVars() {
            return metaVars;
        }

        /**
         * Returns the number of commented meta variables.
         * 
         * @return the number of meta variables
         */
        public int getMetaVarsWithComment() {
            return metaVarsWithComment;
        }

    }
    
    private Consumer<AbstractVariable> noComment = v -> { };
    private Set<Object> done = new HashSet<>();

    /**
     * Creates a statistics visitor.
     */
    public StatisticsVisitor() {
        super(new Statistics());
    }

    /**
     * Optional consumer if a variable has no comment/description.
     * 
     * @param noComment the no comment consumer, may be <b>null</b>, ignored then
     */
    public void setNoCommentConsumer(Consumer<AbstractVariable> noComment) {
        if (null != noComment) {
            this.noComment = noComment;
        }
    }

    /**
     * Returns whether the parent of {@code variable} is a container.
     * 
     * @param variable the variable
     * @return {@code true} if the parent is a container, {@code false} else
     */
    private static boolean isParentContainer(IDecisionVariable variable) {
        IConfigurationElement elt = variable.getParent();
        return null == elt.getDeclaration() ? false : TypeQueries.isContainer(elt.getDeclaration().getType());
    }

    @Override
    protected void specialTreatment(IDecisionVariable variable) {
        AbstractVariable decl = variable.getDeclaration();
        Project prj = decl.getProject();
        if (ModelInfo.isMetaProject(prj)) {
            if (!isParentContainer(variable)) {
                specialTreatment(decl);
            }
            if (!done.contains(prj)) {
                done.add(prj);
                for (int e = 0; e < prj.getElementCount(); e++) {
                    ModelElement elt = prj.getElement(e);
                    if (elt instanceof Compound) {
                        specialTreatment((Compound) elt);
                    }
                }
            }
        }
    }
    
    /**
     * Handles variable declarations.
     * 
     * @param var the variable
     */
    private void specialTreatment(AbstractVariable var) {
        if (!done.contains(var)) {
            Statistics stat = getStatistics();
            stat.metaVars++;
            if (ModelInfo.hasComment(var)) {
                stat.metaVarsWithComment++;
            } else {
                noComment.accept(var);
            }
            done.add(var);
        }
    }

    /**
     * Handles compound declarations.
     * 
     * @param cmp the compound
     */
    private void specialTreatment(Compound cmp) {
        for (int e = 0; e < cmp.getDeclarationCount(); e++) {
            specialTreatment(cmp.getDeclaration(e));
        }
        for (int a = 0; a < cmp.getAssignmentCount(); a++) {
            specialTreatment(cmp.getAssignment(a));
        }
    }

    /**
     * Handles attribute assignments.
     * 
     * @param assng the assignment
     */
    private void specialTreatment(AttributeAssignment assng) {
        for (int e = 0; e < assng.getDeclarationCount(); e++) {
            specialTreatment(assng.getDeclaration(e));
        }
        for (int a = 0; a < assng.getAssignmentCount(); a++) {
            specialTreatment(assng.getAssignment(a));
        }
    }

    @Override
    protected void specialTreatment(Project mainProject) {
    }

    @Override
    public Statistics getStatistics() {
        return (Statistics) super.getStatistics();
    }

}
