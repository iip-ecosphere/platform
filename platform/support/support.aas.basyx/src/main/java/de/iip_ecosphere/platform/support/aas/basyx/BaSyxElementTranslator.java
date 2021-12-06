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
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.DataElement;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.File;

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
         * Registers a property.
         * 
         * @param property the property
         * @return {@code property}
         */
        BaSyxProperty register(BaSyxProperty property);
        
        /**
         * Registers an operation.
         * 
         * @param operation the operation
         * @return {@code operation}
         */
        BaSyxOperation register(BaSyxOperation operation);
        
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
        
        /**
         * Registers a data element.
         * 
         * @param <D> the element type
         * @param dataElement the element to register
         * @return {@code dataElement}
         */
        <D extends DataElement> BaSyxDataElement<D> register(BaSyxDataElement<D> dataElement);

    }
    
    /**
     * Registers all sub-model elements, i.e., none of those handled by the other methods/interfaces.
     * 
     * @param elements the elements to be processed (as declared by BaSyx)
     * @param reg the remaining registrar
     */
    static void registerSubmodelElements(Map<String, ISubmodelElement> elements, SubmodelElementsRegistrar reg) {
        for (ISubmodelElement se : elements.values()) {
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
    }

}
