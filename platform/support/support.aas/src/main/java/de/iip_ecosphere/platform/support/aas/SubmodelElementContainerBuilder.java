package de.iip_ecosphere.platform.support.aas;

import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
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
     * Creates a builder for a contained sub-model element collection. Calling this method again with the same name 
     * shall lead to a builder that allows for modifying the sub-model.
     * 
     * @param idShort the short name of the reference element
     * @param ordered whether the collection is ordered
     * @param allowDuplicates whether the collection allows duplicates
     * @return the builder
     * @throws IllegalArgumentException if {@code idShort} is <b>null</b> or empty; or if modification is not possible
     */
    public SubmodelElementCollectionBuilder createSubmodelElementCollectionBuilder(String idShort, boolean ordered, 
        boolean allowDuplicates);
    
    /**
     * Returns the parent builder.
     * 
     * @return the parent builder or <b>null</b> if we are at a sub-model
     */
    public SubmodelElementContainerBuilder getParentBuilder();
    
    /**
     * Returns the AAS builder.
     * 
     * @return the AAS builder
     */
    public AasBuilder getAasBuilder();
    
    /**
     * Returns whether a new element is being created or the builder refers to an existing element.
     * 
     * @return {@code true} for new, {@code false} for existing
     */
    public boolean isNew();

}