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

package de.iip_ecosphere.platform.support.aas.basyx;

import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;

import org.eclipse.basyx.submodel.metamodel.api.IElementContainer;
import org.eclipse.basyx.submodel.metamodel.api.submodelelement.ISubmodelElement;
import org.eclipse.basyx.submodel.metamodel.api.submodelelement.ISubmodelElementCollection;
import org.eclipse.basyx.submodel.metamodel.api.submodelelement.dataelement.IProperty;
import org.eclipse.basyx.submodel.metamodel.api.submodelelement.dataelement.IReferenceElement;
import org.eclipse.basyx.submodel.metamodel.api.submodelelement.operation.IOperation;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.DataElement;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.File;
import org.eclipse.basyx.vab.exception.provider.ResourceNotFoundException;

import de.iip_ecosphere.platform.support.aas.FileDataElement;
import de.iip_ecosphere.platform.support.aas.Property;
import de.iip_ecosphere.platform.support.aas.ReferenceElement;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.SubmodelElement;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.aas.SubmodelElementContainerBuilder;
import de.iip_ecosphere.platform.support.aas.Submodel.IteratorFunction;
import de.iip_ecosphere.platform.support.aas.basyx.BaSyxSubmodelElementCollection.BaSyxSubmodelElementCollectionBuilder;
import de.iip_ecosphere.platform.support.aas.basyx.VabInvocablesCreator.Operation;

/**
 * Bridges between arbitrary BaSyx instances and instances of the IIP-Ecosphere AAS abstraction.
 * The BaSyx interface structure prevents handing sub-model and submodel elements collections in one
 * type, so we define that just for the elements. We may even avoid the type checks in here, but this 
 * shall be ok for now. 
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxElementTranslator {

    /**
     * Something that can take over/register submodel elements.
     * 
     * @author Holger Eichelberger, SSE
     */
    interface SubmodelElementsRegistrar {

        /**
         * Registers an element. Default for all remaining registration functions in this interface.
         * 
         * @param <T> the actual type of the element
         * @param elt the element
         * @return {@code elt}
         */
        public <T extends SubmodelElement> T registerElement(T elt);
        
        /**
         * Registers a property.
         * 
         * @param property the property
         * @return {@code property}
         */
        public default BaSyxProperty register(BaSyxProperty property)  {
            return registerElement(property);
        }

        /**
         * Registers a file data element.
         * 
         * @param file the file data element
         * @return {@code file}
         */
        public default BaSyxFile register(BaSyxFile file)  {
            return registerElement(file);
        }

        /**
         * Registers a BLOB data element.
         * 
         * @param blob the BLOB data element
         * @return {@code blob}
         */
        public default BaSyxBlob register(BaSyxBlob blob)  {
            return registerElement(blob);
        }

        /**
         * Registers a multi-language property.
         * 
         * @param property the property
         * @return {@code property}
         */
        public default BaSyxMultiLanguageProperty register(BaSyxMultiLanguageProperty property) {
            return registerElement(property);
        }

        /**
         * Registers a relationship element.
         * 
         * @param relationship the relationship element
         * @return {@code relationship}
         */
        public default BaSyxRelationshipElement register(BaSyxRelationshipElement relationship) {
            return registerElement(relationship);
        }

        /**
         * Registers an entity.
         * 
         * @param entity the entity
         * @return {@code entity}
         */
        public default BaSyxEntity register(BaSyxEntity entity) {
            return registerElement(entity);
        }
        
        /**
         * Registers an operation.
         * 
         * @param operation the operation
         * @return {@code operation}
         */
        public default BaSyxOperation register(BaSyxOperation operation) {
            return registerElement(operation);
        }
        
        /**
         * Registers a reference element.
         * 
         * @param reference the reference
         * @return {@code reference}
         */
        public default BaSyxReferenceElement register(BaSyxReferenceElement reference) {
            return registerElement(reference);
        }
        
        /**
         * Registers a sub-model element collection.
         * 
         * @param collection the collection
         * @return {@code collection}
         */
        public default BaSyxSubmodelElementCollection register(BaSyxSubmodelElementCollection collection) {
            return registerElement(collection);
        }
        
        /**
         * Registers a data element.
         * 
         * @param <D> the element type
         * @param dataElement the element to register
         * @return {@code dataElement}
         */
        public default <D extends DataElement> BaSyxDataElement<D> register(BaSyxDataElement<D> dataElement) {
            return registerElement(dataElement);
        }

    }
    
    /**
     * Registers all sub-model elements, i.e., none of those handled by the other methods/interfaces.
     * 
     * @param elements the elements to be processed (as declared by BaSyx)
     * @param reg the remaining registrar
     */
    static void registerSubmodelElements(Map<String, ISubmodelElement> elements, SubmodelElementsRegistrar reg) {
        registerSubmodelElements(elements.values(), reg);
    }

    /**
     * Registers all sub-model elements, i.e., none of those handled by the other methods/interfaces.
     * 
     * @param elements the elements to be processed (as declared by BaSyx)
     * @param reg the remaining registrar
     */
    static void registerSubmodelElements(Collection<ISubmodelElement> elements, SubmodelElementsRegistrar reg) {
        for (ISubmodelElement se : elements) {
            registerSubmodelElement(se, reg);
        }        
    }

    /**
     * Registers a single submodel element with {@code reg}.
     * 
     * @param se the submodel element
     * @param reg the registrar to apply for registration
     */
    static void registerSubmodelElement(ISubmodelElement se, SubmodelElementsRegistrar reg) {
        if (se instanceof IProperty) {
            reg.register(new BaSyxProperty((IProperty) se));
        } else if (se instanceof IOperation) {
            reg.register(new BaSyxOperation((IOperation) se));
        } else if (se instanceof IReferenceElement) {
            reg.register(new BaSyxReferenceElement((IReferenceElement) se));
        } else if (se instanceof ISubmodelElementCollection) {
            reg.register(new BaSyxSubmodelElementCollection((ISubmodelElementCollection) se));
        } else if (se instanceof File) {
            reg.register(new BaSyxFile((File) se));
        } // TODO else
    }
    
    /**
     * Returns whether the interface type {@code cls} matches the type of the implementing BaSyx submodel element.
     * 
     * @param <T> the interface type
     * @param elt the element to check for a type match
     * @param cls the interface type
     * @return {@code true} if the type matches, {@code false}
     */
    static <T extends SubmodelElement> boolean matchesType(ISubmodelElement elt, Class<T> cls) {
        boolean result;
        if (Property.class.isAssignableFrom(cls)) {
            result = org.eclipse.basyx.submodel.metamodel.api.submodelelement.dataelement.IProperty.class
                .isInstance(elt);
        } else if (Operation.class.isAssignableFrom(cls)) {
            result = org.eclipse.basyx.submodel.metamodel.api.submodelelement.operation.IOperation.class
                .isInstance(elt);
        } else if (ReferenceElement.class.isAssignableFrom(cls)) {
            result = org.eclipse.basyx.submodel.metamodel.api.submodelelement.dataelement.IReferenceElement.class
                .isInstance(elt);
        } else if (SubmodelElementCollection.class.isAssignableFrom(cls)) {
            result = org.eclipse.basyx.submodel.metamodel.api.submodelelement.ISubmodelElementCollection.class
                .isInstance(elt);
        } else if (FileDataElement.class.isAssignableFrom(cls)) {
            result = File.class.isInstance(elt);
        } else if (SubmodelElement.class.isAssignableFrom(cls)) {
            result = org.eclipse.basyx.submodel.metamodel.api.submodelelement.ISubmodelElement.class.isInstance(elt);
        } else {
            result = false;
        }
        return result;
    }
    
    /**
     * Internal registrar applying a function to already 
     * {@link BaSyxElementTranslator#matchesType(ISubmodelElement, Class) type-matched submodel elements}. 
     * 
     * @param <T> the target submodel element type used for matching
     * @author Holger Eichelberger, SSE
     */
    private static class IterationSubmodelElementsRegistrar<T extends SubmodelElement> 
        implements SubmodelElementsRegistrar {

        private IteratorFunction<T> func;
        private Class<T> cls;
        private boolean applied;
        private boolean cont = true;
        
        /**
         * Creates an instance.
         * 
         * @param func the function to apply
         * @param cls the type used for matching
         */
        private IterationSubmodelElementsRegistrar(IteratorFunction<T> func, Class<T> cls) {
            this.func = func;
            this.cls = cls;
        }

        /**
         * Executes {@code elt} to {@link #func}.  
         * 
         * @param <S> the element type
         * @param elt the element
         * @return {@code elt}
         */
        private <S> S accept(S elt) {
            cont = func.apply(cls.cast(elt));
            applied = true;
            return elt;
        }
        
        @Override
        public <E extends SubmodelElement> E registerElement(E elt) {
            return accept(elt);
        }
        
        @Override
        public BaSyxProperty register(BaSyxProperty property) {
            return accept(property);
        }

        @Override
        public BaSyxFile register(BaSyxFile file) {
            return accept(file);
        }

        @Override
        public BaSyxBlob register(BaSyxBlob blob) {
            return accept(blob);
        }

        @Override
        public BaSyxMultiLanguageProperty register(BaSyxMultiLanguageProperty property) {
            return accept(property);
        }

        @Override
        public BaSyxOperation register(BaSyxOperation operation) {
            return accept(operation);
        }

        @Override
        public BaSyxRelationshipElement register(BaSyxRelationshipElement relationship) {
            return accept(relationship);
        }

        @Override
        public BaSyxEntity register(BaSyxEntity entity) {
            return accept(entity);
        }

        @Override
        public BaSyxReferenceElement register(BaSyxReferenceElement reference) {
            return accept(reference);
        }

        @Override
        public BaSyxSubmodelElementCollection register(BaSyxSubmodelElementCollection collection) {
            return accept(collection);
        }

        @Override
        public <D extends DataElement> BaSyxDataElement<D> register(BaSyxDataElement<D> dataElement) {
            return accept(dataElement);
        }
        
        /**
         * Returns whether the function given in the constructor was applied at least once.
         *  
         * @return {@code true} for applied {code false} else
         */
        private boolean wasApplied() {
            return applied;
        }
        
        /**
         * Returns whether iteration shall be continued.
         * 
         * @return {@code true} for continue, {@code false} else
         */
        private boolean isContinue() {
            return cont;
        }
        
    }

    /**
     * Iterates over the elements of the nested submodel element collection in {@code cont} denoted by {@code path} and 
     * applies {@code func} to each element matching the type of {@code cls}.
     * 
     * @param <T> the element type
     * @param cont the container to start at
     * @param func the function to apply to each matching element
     * @param cls the class/element type used for filtering 
     * @param path the path to the submodel element collection starting at the elements of {@code cont}
     * @return {@code true} if {@code func} was applied at least once, {@code false} if path did not point to a 
     *   submodel element collection containing at least one element matching {@code cls}
     */
    static <T extends SubmodelElement> boolean iterate(IElementContainer cont, IteratorFunction<T> func, Class<T> cls, 
        String... path) {
        boolean result = false;
        cont = findInPath(cont, path);
        if (cont instanceof IElementContainer) {
            IterationSubmodelElementsRegistrar<T> reg = new IterationSubmodelElementsRegistrar<>(func, cls);
            for (ISubmodelElement e: ((IElementContainer) cont).getSubmodelElements().values()) {
                if (matchesType(e, cls)) {
                    registerSubmodelElement(e, reg);
                    if (!reg.isContinue()) {
                        break;
                    }
                }
            }
            result = reg.wasApplied();
        }
        return result;
    }

    /**
     * Allows to create new elements denoted by {@code func} in the nested submodel element collection in {@code cont} 
     * denoted by {@code path}.
     * 
     * @param sub the submodel to start at
     * @param func the function to apply to the appointed submodel element collection
     * @param propagate the change into the interface instance; if applied frequently, may imply a performance issue
     * @param path the path to the submodel element collection starting at the elements of {@code cont}
     * @return {@code true} if {@code func} was applied, {@code false} if path did not point to a submodel 
     * element collection
     */
    static boolean create(Submodel sub, Consumer<SubmodelElementContainerBuilder> func, boolean propagate, 
        String... path) {
        SubmodelElementContainerBuilder builder = null;
        if (path.length > 0) {
            // boolean build parameters are not relevant as we aim for an existing builder (chain)
            builder = sub.createSubmodelElementCollectionBuilder(path[0], true, true);
            if (builder.isNew()) {
                builder = null;
            } else {
                for (int i = 1; i < path.length; i++) {
                    builder = builder.createSubmodelElementCollectionBuilder(path[i], true, true);
                    if (builder.isNew()) {
                        builder = null;
                        break;
                    }
                }
            }
        } else {
            builder = ((AbstractSubmodel<?>) sub).getAas().createAasBuilder().createSubmodelBuilder(
                sub.getIdShort(), sub.getIdentification());
        }
        if (builder != null) {
            if (builder instanceof BaSyxSubmodelElementCollectionBuilder) {
                ((BaSyxSubmodelElementCollectionBuilder) builder).setPropagation(propagate);
            }
            func.accept(builder);
        }
        return builder != null;
    }

    /**
     * Finds a nested element container in {@code path} starting at {@code cont}.
     * 
     * @param cont the container to start at
     * @param path the path to follow
     * @return the nested container in {@code cont} specified by {@code path}, <b>null</b> for none
     */
    static IElementContainer findInPath(IElementContainer cont, String... path) {
        for (int i = 0; i < path.length; i++) {
            try {
                ISubmodelElement elt = cont.getSubmodelElement(path[i]);
                if (elt instanceof IElementContainer) {
                    cont = (IElementContainer) elt;
                } else {
                    cont = null;
                    break;
                }
            } catch (ResourceNotFoundException e) {
                cont = null;
                break;
            }
        }
        return cont;
    }

}
