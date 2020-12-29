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

import java.util.Map;

import org.eclipse.basyx.submodel.metamodel.api.submodelelement.ISubmodelElement;
import org.eclipse.basyx.submodel.metamodel.api.submodelelement.ISubmodelElementCollection;
import org.eclipse.basyx.submodel.metamodel.api.submodelelement.dataelement.IProperty;
import org.eclipse.basyx.submodel.metamodel.api.submodelelement.dataelement.IReferenceElement;
import org.eclipse.basyx.submodel.metamodel.api.submodelelement.operation.IOperation;

/**
 * Bridges between arbitrary BaSyx instances and instances of the IIP-Ecosphere AAS abstraction.
 * The BaSyx interface structure prevents handing sub-model and submodel elements collections in one
 * type, so we define that just for the elements. We may even avoid the type checks in here, but this 
 * shall be ok for now. 
 * 
 * @author Holger Eichelberger, SSE
 */
class BaSyxElementTranslator {

    /**
     * Something that can take over/register data elements.
     * 
     * @author Holger Eichelberger, SSE
     */
    interface DataElementsRegistrar {

        /**
         * Registers a property.
         * 
         * @param property the property
         * @return {@code property}
         */
        BaSyxProperty register(BaSyxProperty property);
        
    }

    /**
     * Registers the data elements.
     * 
     * @param elements the data elements to be processed (as declared by BaSyx)
     * @param reg the IIP-Ecosphere registrar
     */
    static void registerValues(Map<String, Object> elements, DataElementsRegistrar reg) {
        // unclear by now
        /*for (Object elt : elements.values()) {
            if (elt instanceof IProperty) {
                reg.register(new BaSyxProperty((IProperty) elt));
            } // TODO else
        }*/
    }

    /**
     * Something that can take over/register operations.
     * 
     * @author Holger Eichelberger, SSE
     */
    interface OperationsRegistrar {

        /**
         * Registers an operation.
         * 
         * @param operation the operation
         * @return {@code operation}
         */
        BaSyxOperation register(BaSyxOperation operation);
        
    }

    /**
     * Registers the operations.
     * 
     * @param properties the properties to be processed (as declared by BaSyx)
     * @param reg the IIP-Ecosphere registrar
     */
    static void registerProperties(Map<String, IProperty> properties, DataElementsRegistrar reg) {
        for (IProperty op : properties.values()) {
            reg.register(new BaSyxProperty(op));
        }
    }

    /**
     * Registers the operations.
     * 
     * @param operations the operations to be processed (as declared by BaSyx)
     * @param reg the IIP-Ecosphere registrar
     */
    static void registerOperations(Map<String, IOperation> operations, OperationsRegistrar reg) {
        for (IOperation op : operations.values()) {
            reg.register(new BaSyxOperation(op));
        }
    }

    /**
     * Something that can take over/register (remaining) submodel elements.
     * 
     * @author Holger Eichelberger, SSE
     */
    interface RemainingSubmodelElementsRegistrar {
        
        /**
         * Registers a reference element.
         * 
         * @param reference the reference
         * @return {@code reference}
         */
        BaSyxReferenceElement register(BaSyxReferenceElement reference);
        
        /**
         * Registers a sub-model element collection.
         * 
         * @param collection the collection
         * @return {@code collection}
         */
        BaSyxSubmodelElementCollection register(BaSyxSubmodelElementCollection collection);

    }
    
    /**
     * Registers the remaining sub-model elements, i.e., none of those handled by the other methods/interfaces.
     * 
     * @param elements the elements to be processed (as declared by BaSyx)
     * @param reg the IIP-Ecosphere registrar
     */
    static void registerRemainingSubmodelElements(Map<String, ISubmodelElement> elements, 
        RemainingSubmodelElementsRegistrar reg) {
        for (ISubmodelElement se : elements.values()) {
            if (se instanceof IReferenceElement) {
                reg.register(new BaSyxReferenceElement((IReferenceElement) se));
            } else if (se instanceof ISubmodelElementCollection) {
                reg.register(new BaSyxSubmodelElementCollection((ISubmodelElementCollection) se));
            } // TODO else
        }
    }

    /**
     * Convenience interface for everything that can be registered. 
     * 
     * @author Holger Eichelberger, SSE
     */
    interface SubmodelElementsRegistrar extends DataElementsRegistrar, OperationsRegistrar, 
        RemainingSubmodelElementsRegistrar {
    }

}
