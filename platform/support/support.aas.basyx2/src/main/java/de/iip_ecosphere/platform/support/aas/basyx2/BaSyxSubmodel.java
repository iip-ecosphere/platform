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

import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor.RbacAction;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor.Role;

import java.util.List;
import java.util.function.Consumer;

import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodel;
import org.eclipse.digitaltwin.basyx.aasenvironment.IdShortPathBuilder;
import org.eclipse.digitaltwin.basyx.client.internal.ApiException;
import org.eclipse.digitaltwin.basyx.core.exceptions.ElementDoesNotExistException;
import org.eclipse.digitaltwin.basyx.submodelrepository.client.ConnectedSubmodelRepository;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.SubmodelElement;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection.SubmodelElementCollectionBuilder;
import de.iip_ecosphere.platform.support.aas.basyx2.AbstractAas.BaSyxAbstractAasBuilder;
import de.iip_ecosphere.platform.support.aas.basyx2.BaSyxSubmodelElement.PathFunction;
import de.iip_ecosphere.platform.support.aas.SubmodelElementContainerBuilder;

/**
 * Wraps a BaSyx sub-model.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxSubmodel extends AbstractSubmodel<org.eclipse.digitaltwin.aas4j.v3.model.Submodel> {

    private BaSyxSubmodelParent parent;
    private ConnectedSubmodelRepository repo;
    
    /**
     * Builder for {@link BaSyxSubmodel}.
     * 
     * @author Holger Eichelberger, SSE
     */
    protected static class BaSyxSubmodelBuilder extends BaSyxSubmodelElementContainerBuilder
        <org.eclipse.digitaltwin.aas4j.v3.model.Submodel> implements SubmodelBuilder {

        private BaSyxAbstractAasBuilder parentBuilder;
        private BaSyxSubmodel instance;
        private boolean isNew = true;

        /**
         * Creates an instance. Prevents external creation.
         * 
         * @param parentBuilder the parent builder (may be <b>null</b> for a standalone sub-model)
         * @param idShort the short id of the sub-model
         * @param identifier the identifier of the model
         * @throws IllegalArgumentException may be thrown if {@code idShort} is not given
         */
        protected BaSyxSubmodelBuilder(BaSyxAbstractAasBuilder parentBuilder, String idShort, String identifier, 
            ConnectedSubmodelRepository repo) {
            this(parentBuilder);
            org.eclipse.digitaltwin.aas4j.v3.model.Submodel sub =
                new DefaultSubmodel.Builder()
                    .idShort(Tools.checkId(idShort))
                    .id(Tools.translateIdentifierToBaSyx(identifier, idShort)).build();
            setInstance(new BaSyxSubmodel(sub, repo));
        }

        /**
         * Creates an uninitialized instance, e.g., for delayed creation. Use 
         * {@link #setInstance(BaSyxSubmodel)}.
         * 
         * @param parentBuilder the parent builder (may be <b>null</b> for a standalone sub-model)
         * @throws IllegalArgumentException may be thrown if {@code idShort} is not given
         */
        protected BaSyxSubmodelBuilder(BaSyxAbstractAasBuilder parentBuilder) {
            this.parentBuilder = parentBuilder;
        }
        
        /**
         * Creates an instance from an existing BaSyx instance.
         * 
         * @param parentBuilder the parent builder (may be <b>null</b> for a standalone sub-model)
         * @param instance the BaSyx instance wrapper
         */
        protected BaSyxSubmodelBuilder(BaSyxAbstractAasBuilder parentBuilder, BaSyxSubmodel instance) {
            this.parentBuilder = parentBuilder;
            this.instance = instance;
            this.isNew = false;
        }

        /**
         * Sets the instance.
         * 
         * @param instance the wrapped instance
         */
        protected void setInstance(BaSyxSubmodel instance) {
            this.instance = instance;
            instance.parent = null == parentBuilder ? null : parentBuilder.getSubmodelParent();
        }

        @Override
        public SubmodelElementCollectionBuilder createSubmodelElementCollectionBuilder(String idShort, boolean ordered, 
            boolean allowDuplicates) {
            SubmodelElementCollectionBuilder result = instance.getDeferred(idShort, 
                SubmodelElementCollectionBuilder.class);
            if (null == result) {
                result = instance.obtainSubmodelElementCollectionBuilder(this, idShort, ordered, allowDuplicates);
            }
            return result;
        }
        
        @Override
        public void defer() {
            parentBuilder.defer(instance.getIdShort(), this);
        }

        @Override
        public void buildDeferred() {
            parentBuilder.buildMyDeferred();
        }

        @Override
        public Submodel build() {
            buildMyDeferred();
            return null == parentBuilder ? instance : parentBuilder.register(instance);
        }

        @Override
        public AasBuilder getAasBuilder() {
            return parentBuilder;
        }

        @Override
        public SubmodelElementContainerBuilder getParentBuilder() {
            return null;
        }

        @Override
        protected AbstractSubmodel<org.eclipse.digitaltwin.aas4j.v3.model.Submodel> getInstance() {
            return instance;
        }

        @Override
        public boolean isNew() {
            return isNew;
        }
        
        @Override
        public boolean hasElement(String idShort) {
            return instance.getSubmodelElement(idShort) != null;
        }
        
        @Override
        public SubmodelBuilder setSemanticId(String refValue) {
            return Tools.setSemanticId(this, refValue, instance.getSubmodel());
        }
        
        @Override
        public SubmodelBuilder rbac(AuthenticationDescriptor auth, Role role, RbacAction... actions) {
            return AuthenticationDescriptor.submodelRbac(this, auth, role, getInstance().getIdShort(), actions);
        }

    }

    /**
     * Creates an instance. Prevents external creation.
     * 
     * @param subModel the sub-model instance
     */
    protected BaSyxSubmodel(org.eclipse.digitaltwin.aas4j.v3.model.Submodel subModel, 
        ConnectedSubmodelRepository repo) {
        super(subModel);
        this.repo = repo;
    }
    
    /**
     * Creates an instance based on a given instance.
     * 
     * @param parent the parent instance
     * @param instance the BaSyx submodel instance
     */
    protected BaSyxSubmodel(BaSyxSubmodelParent parent, org.eclipse.digitaltwin.aas4j.v3.model.Submodel instance, 
        ConnectedSubmodelRepository repo) {
        super(instance);
        this.parent = parent;
        this.repo = repo;
        BaSyxElementTranslator.registerSubmodelElements(instance.getSubmodelElements(), this);
    }
    
    /**
     * Adds a submodel element.
     * 
     * @param submodel the submodel to add the element to
     * @param element the element to add
     */
    static void addSubmodelElement(org.eclipse.digitaltwin.aas4j.v3.model.Submodel submodel,
        org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement element) {
        submodel.setSubmodelElements(Tools.addElement(submodel.getSubmodelElements(), element));
    }
    
    /**
     * Creates a builder for a contained sub-model element collection. Calling this method again with the same name 
     * shall lead to a builder that allows for modifying the sub-model.
     * 
     * @param parent the parent builder
     * @param idShort the short name of the reference element
     * @param ordered whether the collection is ordered
     * @param allowDuplicates whether the collection allows duplicates
     * @return the builder
     * @throws IllegalArgumentException if {@code idShort} is <b>null</b> or empty; or if modification is not possible
     */
    private SubmodelElementCollectionBuilder obtainSubmodelElementCollectionBuilder(
        BaSyxSubmodelElementContainerBuilder<?> parent, String idShort, boolean ordered, boolean allowDuplicates) {
        SubmodelElementCollectionBuilder result = getDeferred(idShort, SubmodelElementCollectionBuilder.class);
        if (null == result) {
            SubmodelElementCollection sub = getSubmodelElementCollection(idShort);
            if (null == sub) {
                result = new BaSyxSubmodelElementCollection.BaSyxSubmodelElementCollectionBuilder(parent, idShort, 
                    ordered, allowDuplicates);
            } else {
                result = new BaSyxSubmodelElementCollection.BaSyxSubmodelElementCollectionBuilder(parent, 
                   (BaSyxSubmodelElementCollection) sub);
            }
        }
        return result;
    }
    
    @Override
    public SubmodelElementCollectionBuilder createSubmodelElementCollectionBuilder(String idShort, boolean ordered,
        boolean allowDuplicates) {
        LoggerFactory.getLogger(getClass()).warn("Adding a submodel to a deployed AAS currently does not lead to "
            + "the deployment of the new submodel (as for initial AAS). If possible, create the submodel in advance.");
        return obtainSubmodelElementCollectionBuilder(new BaSyxSubmodelBuilder(parent.createAasBuilder(), this), 
            idShort, ordered, allowDuplicates);
    }

    @Override
    public void update() {
    }

    @Override
    public boolean create(Consumer<SubmodelElementContainerBuilder> func, boolean propagate, String... path) {
        return BaSyxElementTranslator.create(this, func, propagate, path);
    }

    @Override
    public <T extends SubmodelElement> boolean iterate(IteratorFunction<T> func, Class<T> cls, String... path) {
        return BaSyxElementTranslator.iterate(getSubmodel().getSubmodelElements(), func, cls, path);
    }

    @Override
    BaSyxSubmodelParent getAas() {
        return parent;
    }

    @Override
    public void setSemanticId(String semanticId) {
        Tools.setSemanticId(this, semanticId, getSubmodel());
    }
    
    /**
     * Sets the repository, emulating a connected submodel.
     * 
     * @param repo the repository
     */
    void setRepo(ConnectedSubmodelRepository repo) {
        this.repo = repo;
    }

    @Override
    public org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement processOnPath(
        List<org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement> path, 
        boolean skipIfNoRepo, PathFunction function) {
        org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement result = null;
        if ((skipIfNoRepo && repo != null) || !skipIfNoRepo) {
            try {
                String idShortPath = ""; // function shall react on that
                if (!path.isEmpty()) {
                    IdShortPathBuilder pBuilder = new IdShortPathBuilder(path);
                    idShortPath = pBuilder.build();
                }
                result = function.apply(getIdShort(), idShortPath, repo);
            } catch (ElementDoesNotExistException e) {
                e.printStackTrace(System.out); // TODO preliminary
            } catch (ApiException e) {
                e.printStackTrace(System.out); // TODO preliminary
            }
        }
        return result;
    }
    
    @Override
    public void deleteElement(String idShort) {
        super.deleteElement(idShort);
        if (null != repo) {
            repo.updateSubmodel(getIdentification(), getSubmodel());
        }
    }    

    @Override
    public void deleteElement(SubmodelElement elt) {
        super.deleteElement(elt);
        if (null != repo) {
            repo.updateSubmodel(getIdentification(), getSubmodel());
        }
    }    

    @Override
    public BaSyxSubmodelElementParent getParent() {
        return null; // end of hierarchy
    }

    @Override
    public org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement getPathElement() {
        return null; // this is not a submodel element
    }
    
}
