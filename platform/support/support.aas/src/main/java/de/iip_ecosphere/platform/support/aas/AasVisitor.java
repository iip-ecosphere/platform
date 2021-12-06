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

/**
 * Defines a visitor over the so far defined AAS elements. Just to briefly summarize the relations:
 * <ul>
 *   <li>If you want to perform operations over an AAS, e.g., printing out all elements as we do in the
 *       test part of this component: Just implement this interface. Be aware that the methods in here
 *       just shall care for the elements themselves, not their container elements. They will be addressed
 *       after the respective method is completed.</li>
 *   <li>Container elements call the methods in this visitor in pairs, the usual visit method at the beginning and
 *       an end method at the end of the visit. If not needed, you can just ignore the end method call, i.e., leave 
 *       it empty.</li>
 *   <li>Pass your implementation to the AAS instance (or if you want to start with a deeper traversal, just start 
 *       at the element of your choice) and the traversal happens "automatically". Call the 
 *       {@link Element#accept(AasVisitor)} for starting a traversal.</li>
 *   <li>All concrete AAS elements implement {@link Element#accept(AasVisitor)}, a method that receives
 *       the actual visitor, call the respective method on the visitor and if relevant call iterate over
 *       the contained instances and call {@link Element#accept(AasVisitor)} on them. This is part of a contract,
 *       i.e., concrete implementations must care for the traversal. For elements that are registered multiple times,
 *       e.g., submodel elements, only call the most specific visit method, 
 *       e.g., {@link #visitOperation(Operation)}.</li>
 *   <li>Why do we follow such a complex approach: The visitor is passed to the starting element and the implementing
 *       element classes call the visitor. This causes an inversion of control that avoids typechecks (through 
 *       polymorphism). The operations done by the visitor are exchangable, i.e., you can define multiple visitors for 
 *       multiple purposes without changing or even deeply knowing the implementing classes. However, new element 
 *       interfaces must be added to {@link AasVisitor} (requiring subsequent changes) and require a call from the 
 *       implementing classes. As we hope to converge soon to the specification, we believe that such extensions will 
 *       happen rather infrequently.</li>
 *   <li>Indeed, this is still a rather traditional implementation. There are more dynamic ones as some visitors for
 *       the EMF models. Let's keep things more or less simple.</li>
 * </ul>
 * 
 * @author Holger Eichelberger, SSE
 */
public interface AasVisitor {

    /**
     * Visits an AAS.
     * 
     * @param aas the aas
     */
    public void visitAas(Aas aas);

    /**
     * Notifies the end of visiting an AAS.
     * 
     * @param aas the aas
     */
    public void endAas(Aas aas);

    /**
     * Visits an asset.
     * 
     * @param asset the asset
     */
    public void visitAsset(Asset asset);
    
    /**
     * Visits a sub-model.
     * 
     * @param submodel the sub-model
     */
    public void visitSubmodel(Submodel submodel);

    /**
     * Notifies the end of visiting a sub-model.
     * 
     * @param submodel the sub-model
     */
    public void endSubmodel(Submodel submodel);

    /**
     * Visits a property.
     * 
     * @param property the property
     */
    public void visitProperty(Property property);

    /**
     * Visits a operation.
     * 
     * @param operation the operation
     */
    public void visitOperation(Operation operation);

    /**
     * Visits a reference element.
     * 
     * @param referenceElement the reference element
     */
    public void visitReferenceElement(ReferenceElement referenceElement);

    /**
     * Visits a sub-model element collection.
     * 
     * @param collection the collection
     */
    public void visitSubmodelElementCollection(SubmodelElementCollection collection);

    /**
     * Notifies ending the visits of a sub-model element collection.
     * 
     * @param collection the collection
     */
    public void endSubmodelElementCollection(SubmodelElementCollection collection);

    /**
     * Visits a data element.
     * 
     * @param dataElement the reference element
     */
    public void visitDataElement(DataElement dataElement);
    
}
