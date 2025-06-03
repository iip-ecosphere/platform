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

import java.util.concurrent.ExecutionException;

/**
 * A re-usable print visitor for AAS structures. Just prints the AAS to the console. Not applicable
 * within this component, but usable for on concrete implementations. Useful for debugging.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AasPrintVisitor implements AasVisitor {

    private String indentation = "";
    
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
        // may be replaced by a real logger, left open as UKL thinks about secure logging
        System.out.println(indentation + text);
    }
    
    @Override
    public void visitAas(Aas aas) {
        log("AAS " + aas.getIdShort());
        increaseIndentation();
    }
    
    @Override
    public void endAas(Aas aas) {
        decreaseIndentation();
    }

    @Override
    public void visitAsset(Asset asset) {
        log("ASSET " + asset.getIdShort() + " " + asset.getAssetKind());
    }

    @Override
    public void visitSubmodel(Submodel submodel) {
        log("SUBMODEL " + submodel.getIdShort());
        increaseIndentation();
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
    public void visitSubmodelElementCollection(SubmodelElementCollection collection) {
        log("COLLECTION " + collection.getIdShort());
        increaseIndentation();
    }

    @Override
    public void endSubmodelElementCollection(SubmodelElementCollection collection) {
        decreaseIndentation();
    }

    @Override
    public void visitSubmodelElementList(SubmodelElementList list) {
        log("LIST " + list.getIdShort());
        increaseIndentation();
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
    public void visitEntity(Entity entity) {
        log("ENTITY " + entity.getIdShort());
        increaseIndentation();
    }

    @Override
    public void endVisitEntity(Entity entity) {
        decreaseIndentation();
    }

}
