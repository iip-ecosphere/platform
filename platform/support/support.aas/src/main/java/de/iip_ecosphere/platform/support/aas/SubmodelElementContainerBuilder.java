package de.iip_ecosphere.platform.support.aas;

import de.iip_ecosphere.platform.support.Builder;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.BlobDataElement.BlobDataElementBuilder;
import de.iip_ecosphere.platform.support.aas.Entity.EntityBuilder;
import de.iip_ecosphere.platform.support.aas.Entity.EntityType;
import de.iip_ecosphere.platform.support.aas.FileDataElement.FileDataElementBuilder;
import de.iip_ecosphere.platform.support.aas.MultiLanguageProperty.MultiLanguagePropertyBuilder;
import de.iip_ecosphere.platform.support.aas.Operation.OperationBuilder;
import de.iip_ecosphere.platform.support.aas.Property.PropertyBuilder;
import de.iip_ecosphere.platform.support.aas.Range.RangeBuilder;
import de.iip_ecosphere.platform.support.aas.ReferenceElement.ReferenceElementBuilder;
import de.iip_ecosphere.platform.support.aas.RelationshipElement.RelationshipElementBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection.SubmodelElementCollectionBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElementList.SubmodelElementListBuilder;

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
     * Creates a builder for a multi-language property.
     * 
     * @param idShort the short name of the property
     * @return the property builder
     * @throws IllegalArgumentException if {@code idShort} is <b>null</b> or empty
     */
    public MultiLanguagePropertyBuilder createMultiLanguagePropertyBuilder(String idShort);

    /**
     * Creates a builder for a relationship element.
     * 
     * @param idShort the short name of the reference
     * @param first the first reference
     * @param second the second reference
     * @return the property builder
     * @throws IllegalArgumentException if {@code idShort} is <b>null</b> or empty
     */
    public RelationshipElementBuilder createRelationshipElementBuilder(String idShort, 
        Reference first, Reference second);

    /**
     * Creates an Entity builder.
     * 
     * @param idShort the idShort of the entity
     * @param type the entity type
     * @param asset optional reference to asset, may be <b>null</b> for none
     * @return the entity builder
     */
    public EntityBuilder createEntityBuilder(String idShort, EntityType type, Reference asset);
    
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
     * Creates a nested file data element builder.
     * 
     * @param idShort the short id of the data element
     * @param contents the file contents/value
     * @param mimeType the mime type of the file
     * @return the data element builder
     */
    public FileDataElementBuilder createFileDataElementBuilder(String idShort, String contents, String mimeType);

    /**
     * Creates a nested range builder.
     * 
     * @param idShort the short id of the data element
     * @param type the value type
     * @param min the minimum value
     * @param max the maximum value
     * @return the range builder
     */
    public RangeBuilder createRangeBuilder(String idShort, Type type, Object min, Object max);

    /**
     * Creates a nested BLOB data element builder.
     * 
     * @param idShort the short id of the data element
     * @param contents the file contents/value (may be <b>null</b> for none)
     * @param mimeType the mime type of the file
     * @return the data element builder
     */
    public BlobDataElementBuilder createBlobDataElementBuilder(String idShort, String contents, String mimeType);
    
    /**
     * Creates a builder for a contained sub-model element collection (not ordered, no duplicates). Calling this method 
     * again with the same name shall lead to a builder that allows for modifying the sub-model.
     * 
     * @param idShort the short name of the reference element
     * @return the builder
     * @throws IllegalArgumentException if {@code idShort} is <b>null</b> or empty; or if modification is not possible
     */
    public SubmodelElementCollectionBuilder createSubmodelElementCollectionBuilder(String idShort);

    /**
     * Creates a builder for a contained sub-model element list. Calling this method 
     * again with the same name shall lead to a builder that allows for modifying the sub-model.
     * 
     * @param idShort the short name of the reference element
     * @return the builder
     * @throws IllegalArgumentException if {@code idShort} is <b>null</b> or empty; or if modification is not possible
     */
    public SubmodelElementListBuilder createSubmodelElementListBuilder(String idShort);

    /**
     * Returns a sub-model element container builder either by providing access to an existing collection or list or 
     * through a builder to add a new sub-model elements collection or list (default collection, ultimately only if 
     * {@link Builder#build()} was called).
     * 
     * @param idShort the short name of the list
     * @return the sub-model collection builder
     */
    public SubmodelElementContainerBuilder createSubmodelElementContainerBuilder(String idShort);
    
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

    /**
     * Returns whether there is an element with the given {@code idShort} in the collection.
     * 
     * @param idShort the element so search for
     * @return {@code true} if the element exist, {@code false} else
     */
    public boolean hasElement(String idShort);

    /**
     * Generic build method that shall call {@link Builder#build()} but without returning the created instance.
     */
    public void justBuild();
    
}