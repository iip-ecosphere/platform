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

package de.iip_ecosphere.platform.support.aas;

import java.io.PrintStream;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

/**
 * A re-usable print visitor for AAS structures. Just prints the AAS to the console. Not applicable
 * within this component, but usable for on concrete implementations. Useful for debugging.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AasPrintVisitor implements AasVisitor {

    private String indentation = "";
    private PrintStream out;
    private Predicate<Aas> aasPredicate = a -> true;
    private Predicate<Submodel> submodelPredicate = s -> true;
    private Predicate<SubmodelElementCollection> submodelElementCollectionPredicate = c -> true;
    private Predicate<SubmodelElementList> submodelElementListPredicate = l -> true;
    private Predicate<Entity> entityPredicate = e -> true;

    /**
     * Creates a print visitor on {@code System.out}.
     */
    public AasPrintVisitor() {
        this(null);
    }

    /**
     * Creates a print visitor on {@code out}.
     * 
     * @param out the output stream, may be <b>null</b> then {@code System.out} is used instead 
     */
    public AasPrintVisitor(PrintStream out) {
        this.out = null == out ? System.out : out;
    }
    
    /**
     * Increases the indentation.
     */
    private void increaseIndentation() {
        indentation = indentation + " ";
    }

    /**
     * Decreases the indentation.
     */
    private void decreaseIndentation() {
        if (indentation.length() > 0) {
            indentation = indentation.substring(0, indentation.length() - 1);
        }
    }
    
    /**
     * Logs the {@code text}.
     * 
     * @param text the text
     */
    private void log(String text) {
        out.println(indentation + text);
    }
    
    @Override
    public boolean visitAas(Aas aas) {
        log("AAS " + aas.getIdShort() + " [" + aas.getIdentification() + "]");
        increaseIndentation();
        return aasPredicate.test(aas);
    }
    
    @Override
    public void endAas(Aas aas) {
        decreaseIndentation();
    }

    @Override
    public void visitAsset(AssetInformation asset) {
        log("ASSET " + asset.getIdShort() + " " + asset.getAssetKind());
    }

    @Override
    public boolean visitSubmodel(Submodel submodel) {
        log("SUBMODEL " + submodel.getIdShort() + " [" + submodel.getIdentification() + "]");
        increaseIndentation();
        return submodelPredicate.test(submodel);
    }

    @Override
    public void endSubmodel(Submodel submodel) {
        decreaseIndentation();
    }
    
    @Override
    public void visitProperty(Property property) {
        String value;
        try {
            value = String.valueOf(property.getValue());
        } catch (ExecutionException e) {
            value = "?";
        }
        String semId = property.getSemanticId();
        if (null == semId) {
            semId = "";
        } else {
            semId = " (semanticId: " + semId + ")";
        }
        log("PROPERTY " + property.getIdShort() + " = " + value + semId);
    }

    @Override
    public void visitOperation(Operation operation) {
        log("OPERATION " + operation.getIdShort() + " #args " + operation.getArgsCount());
    }

    @Override
    public void visitReferenceElement(ReferenceElement referenceElement) {
        log("REFERENCE " + referenceElement.getIdShort() + " = " + referenceElement.getValue());
    }

    @Override
    public boolean visitSubmodelElementCollection(SubmodelElementCollection collection) {
        log("COLLECTION " + collection.getIdShort());
        increaseIndentation();
        return submodelElementCollectionPredicate.test(collection);
    }

    @Override
    public void endSubmodelElementCollection(SubmodelElementCollection collection) {
        decreaseIndentation();
    }

    @Override
    public boolean visitSubmodelElementList(SubmodelElementList list) {
        log("LIST " + list.getIdShort());
        increaseIndentation();
        return submodelElementListPredicate.test(list);
    }

    @Override
    public void endSubmodelElementList(SubmodelElementList list) {
        decreaseIndentation();
    }

    @Override
    public void visitDataElement(DataElement dataElement) {
        log("DATAELT " + dataElement.getIdShort());
    }
    
    @Override
    public void visitFileDataElement(FileDataElement dataElement) {
        log("FILEDATAELT " + dataElement.getIdShort());
    }

    @Override
    public void visitRange(Range range) {
        log("RANGE " + range.getMin() + " " + range.getMax());
    }
    
    @Override
    public void visitBlobDataElement(BlobDataElement dataElement) {
        log("BLOBDATAELT " + dataElement.getIdShort());
    }
    
    @Override
    public void visitMultiLanguageProperty(MultiLanguageProperty property) {
        log("MLP " + property.getIdShort());
    }

    @Override
    public void visitRelationshipElement(RelationshipElement relationship) {
        log("RELATIONSHIP " + relationship.getIdShort());
    }

    @Override
    public boolean visitEntity(Entity entity) {
        log("ENTITY " + entity.getIdShort());
        increaseIndentation();
        return entityPredicate.test(entity);
    }

    @Override
    public void endVisitEntity(Entity entity) {
        decreaseIndentation();
    }

    /**
     * Sets a predicate that enables or disables deeper AAS visiting.
     * 
     * @param aasPredicate the predicated, ignored if <b>null</b>
     * @return <b>this</b> for chaining
     */
    public AasPrintVisitor setAasPredicate(Predicate<Aas> aasPredicate) {
        if (null != aasPredicate) {
            this.aasPredicate = aasPredicate;
        }
        return this;
    }

    /**
     * Sets a predicate that enables or disables deeper entity visiting.
     * 
     * @param entityPredicate the predicated, ignored if <b>null</b>
     * @return <b>this</b> for chaining
     */
    public AasPrintVisitor setEntityPredicate(Predicate<Entity> entityPredicate) {
        if (null != entityPredicate) {
            this.entityPredicate = entityPredicate;
        }
        return this;
    }

    /**
     * Sets a predicate that enables or disables deeper submodel visiting.
     * 
     * @param submodelPredicate the predicated, ignored if <b>null</b>
     * @return <b>this</b> for chaining
     */
    public AasPrintVisitor setSubmodelPredicate(Predicate<Submodel> submodelPredicate) {
        if (null != submodelPredicate) {
            this.submodelPredicate = submodelPredicate;
        }
        return this;
    }

    /**
     * Sets a predicate that enables or disables deeper SMEC visiting.
     * 
     * @param submodelElementCollectionPredicate the predicated, ignored if <b>null</b>
     * @return <b>this</b> for chaining
     */
    public AasPrintVisitor setSubmodelElementCollectionPredicate(
        Predicate<SubmodelElementCollection> submodelElementCollectionPredicate) {
        if (null != submodelElementCollectionPredicate) {
            this.submodelElementCollectionPredicate = submodelElementCollectionPredicate;
        }
        return this;
    }

    /**
     * Sets a predicate that enables or disables deeper SMEL visiting.
     * 
     * @param submodelElementListPredicate the predicated, ignored if <b>null</b>
     * @return <b>this</b> for chaining
     */
    public AasPrintVisitor setSubmodelElementListPredicate(Predicate<SubmodelElementList> 
        submodelElementListPredicate) {
        if (null != submodelElementListPredicate) {
            this.submodelElementListPredicate = submodelElementListPredicate;
        }
        return this;
    }

}
