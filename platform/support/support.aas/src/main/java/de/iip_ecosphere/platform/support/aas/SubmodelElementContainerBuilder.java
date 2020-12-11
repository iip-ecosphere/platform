package de.iip_ecosphere.platform.support.aas;

import de.iip_ecosphere.platform.support.aas.Operation.OperationBuilder;
import de.iip_ecosphere.platform.support.aas.Property.PropertyBuilder;
import de.iip_ecosphere.platform.support.aas.ReferenceElement.ReferenceElementBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection.SubmodelElementCollectionBuilder;

/**
 * Builder interface for something that contains submodel elements.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface SubmodelElementContainerBuilder {
    
    /**
     * Creates a builder for a contained property.
     * 
     * @param idShort the short name of the property
     * @return the property builder
     * @throws IllegalArgumentException if {@code idShort} is <b>null</b> or empty
     */
    public PropertyBuilder createPropertyBuilder(String idShort);

    /**
     * Creates a builder for a contained reference element.
     * 
     * @param idShort the short name of the reference element
     * @return the reference element builder
     * @throws IllegalArgumentException if {@code idShort} is <b>null</b> or empty
     */
    public ReferenceElementBuilder createReferenceElementBuilder(String idShort);

    /**
     * Creates a builder for a contained operation.
     * 
     * @param idShort the short name of the operation
     * @return the property builder
     * @throws IllegalArgumentException if {@code idShort} is <b>null</b> or empty
     */
    public OperationBuilder createOperationBuilder(String idShort);

    /**
     * Creates a builder for a contained sub-model element collection.
     * 
     * @param idShort the short name of the reference element
     * @param ordered whether the collection is ordered
     * @param allowDuplicates whether the collection allows duplicates
     * @return the builder
     * @throws IllegalArgumentException if {@code idShort} is <b>null</b> or empty
     */
    public SubmodelElementCollectionBuilder createSubmodelElementCollectionBuilder(String idShort, boolean ordered, 
        boolean allowDuplicates);

}