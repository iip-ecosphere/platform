/**
 * ******************************************************************************
 * Copyright (c) {2025} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.aas.basyx2;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.digitaltwin.basyx.core.exceptions.ElementDoesNotExistException;
import org.eclipse.digitaltwin.basyx.submodelrepository.client.ConnectedSubmodelRepository;

import de.iip_ecosphere.platform.support.CollectionUtils;
import de.iip_ecosphere.platform.support.aas.SubmodelElement;

/**
 * Implements an abstract BaSyx sub-model element wrapper.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class BaSyxSubmodelElement implements SubmodelElement {

    private BaSyxSubmodelElementParent parent;
    
    /**
     * Returns the implementing sub-model element.
     * 
     * @return the submodel element
     */
    abstract org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement getSubmodelElement();
    
    /**
     * Defines the parent element.
     * 
     * @param parent the parent
     */
    void setParent(BaSyxSubmodelElementParent parent) {
        this.parent = parent;
    }
    
    /**
     * Returns the parent element.
     * 
     * @return the parent element
     */
    public BaSyxSubmodelElementParent getParent() {
        return parent;
    }

    @Override
    public void setSemanticId(String semanticId) {
        Tools.setSemanticId(this, semanticId, getSubmodelElement());
        updateConnectedSubmodelElement();
    }

    @Override
    public String getSemanticId(boolean stripPrefix) {
        return Tools.translateReference(getSubmodelElement().getSemanticId(), stripPrefix);
    }

    @Override
    public String getIdShort() {
        return getSubmodelElement().getIdShort();
    }
    
    @Override
    public void update() {
        // nothing to do
    }
    
    /**
     * A function to be called on an accumulated submodel element path.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface PathFunction {

        /**
         * The path function.
         * 
         * @param submodelId the id of the parent submodel
         * @param idShortPath the full path to the original caller, may be empty (!)
         * @param repo the submodel repository, may be <b>null</b> if not skipped
         * @throws ElementDoesNotExistException if the element does not exist
         */
        public org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement apply(String submodelId, String idShortPath, 
            ConnectedSubmodelRepository repo) throws ElementDoesNotExistException;
        
    }

    /**
     * Iterates up the path up to the submodel and calls {@code function} on the aggregated path.
     * 
     * @param path the path accumulated so far
     * @param skipIfNoRepo skip executing function if there is no repository to pass into
     * @param function the function to call at the end of the path
     * @return the result of {@code function}
     */
    public org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement processOnPath(
        List<org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement> path, boolean skipIfNoRepo, 
        PathFunction function) {
        org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement result = null;
        if (null != getParent()) {
            result = getParent().processOnPath(composePath(path, this), skipIfNoRepo, function);
        }
        return result;
    }

    /**
     * Composes the input for an idShort path.
     * 
     * @param path the idShort path composed so far as list, may be <b>null</b> or empty, may be modified as a 
     *     side effect
     * @param element the element to be added to {@code path}
     * @return the composed path
     */
    static List<org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement> composePath(
        List<org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement> path, BaSyxSubmodelElement element) {
        return composePath(path, element.getSubmodelElement());
    }
    
    /**
     * Composes the input for an idShort path.
     * 
     * @param path the idShort path composed so far as list, may be <b>null</b> or empty, may be modified as a 
     *     side effect
     * @param element the element to be added to {@code path}
     * @return the composed path
     */
    static List<org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement> composePath(
        List<org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement> path, 
        org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement element) {
        if (path == null) {
            path = new ArrayList<>();
        }
        if (null != element) {
            path.addFirst(element);
        }
        return path;
    }

    /**
     * Creates a submodel element.
     * 
     * @param submodelId the submodel element Id
     * @param path the submodelId path
     * @param repo the submodel repository
     * @param elt the submodel element
     * @return <b>null</b>
     * @throws ElementDoesNotExistException
     */
    static org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement createSubmodelElement(String submodelId, String path, 
        ConnectedSubmodelRepository repo, 
        BaSyxSubmodelElement elt) throws ElementDoesNotExistException {
        if (null == path || path.length() == 0) {
            repo.createSubmodelElement(submodelId, elt.getSubmodelElement());
        } else {
            repo.createSubmodelElement(submodelId, path, elt.getSubmodelElement());
        }
        return null;
    }
    
    /**
     * Uses {@link #processOnPath(List, boolean, PathFunction)} to delete the givenelement in the submodel 
     * registry (if present). Must be called by setters.
     */
    protected void deleteConnectedSubmodelElement(SubmodelElement element) {
        if (null != getParent() && null != element) {
            ((BaSyxSubmodelElement) element).getSubmodelElement();
            getParent().processOnPath(CollectionUtils.toList(getSubmodelElement(), 
                ((BaSyxSubmodelElement) element).getSubmodelElement()), true, (sId, p, r) -> {
                    r.deleteSubmodelElement(sId, p);
                    return null;
                });
        }
    }    
    
    /**
     * Uses {@link #processOnPath(List, boolean, PathFunction)} to update this element in the submodel 
     * registry (if present). Must be called by setters.
     */
    protected void updateConnectedSubmodelElement() {
        if (null != getParent()) {
            getParent().processOnPath(CollectionUtils.toList(getSubmodelElement()), true, (sId, p, r) -> {
                r.updateSubmodelElement(sId, p, getSubmodelElement());
                return null;
            });
        }
    }
    
    /**
     * Uses {@link #processOnPath(List, boolean, PathFunction)} to create a corresponding element in the submodel 
     * registry (if present).
     */
    protected void createConnectedSubmodelElement() {
        if (null != getParent()) {
            // CollectionUtils.toList() as element does not yet exist
            getParent().processOnPath(CollectionUtils.toList(), true, 
                (sId, p, r) -> createSubmodelElement(sId, p, r, this));
        }
    }
    
    /**
     * Returns the submodel repository from the parent submodel.
     * 
     * @return the submodel repository, may be <b>null</b> if there is none
     */
    protected ConnectedSubmodelRepository getRepo() {
        AtomicReference<ConnectedSubmodelRepository> result = new AtomicReference<>();
        if (null != getParent()) {
            // path is not relevant
            getParent().processOnPath(CollectionUtils.toList(), true, 
                (sId, p, r) -> { 
                    result.set(r); 
                    return null;
                } 
            );
        }
        return result.get();
    }

    /**
     * Conditional update/create.
     * 
     * @param isNew whether {@code instance} is created/new in builder or updating
     * @param instance the actual instance
     * 
     * @see #createConnectedSubmodelElement()
     * @see #updateConnectedSubmodelElement()
     */
    protected static <I extends BaSyxSubmodelElement> I updateInBuild(boolean isNew, I instance) {
        if (isNew) {
            instance.createConnectedSubmodelElement();
        } else {
            instance.updateConnectedSubmodelElement();
        }
        return instance;
    }

}
