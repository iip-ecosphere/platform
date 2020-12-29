package de.iip_ecosphere.platform.support.aas;

/**
 * Represents an element container.
 */
public interface ElementContainer {

    /**
     * Returns all sub-model elements in the element container.
     * 
     * @return all sub-model elements
     */
    public Iterable<SubmodelElement> submodelElements();
    
    /**
     * Returns the number of sub-model elements in the element container.
     * 
     * @return the number of sub-model elements
     */
    public int getSubmodelElementsCount();
    
    /**
     * Returns all data elements in the element container.
     * 
     * @return all data elements
     */
    public Iterable<DataElement> dataElements();

    /**
     * Returns the number of data elements in the element container.
     * 
     * @return the number of data elements
     */
    public int getDataElementsCount();

    /**
     * Returns all properties in the element container.
     * 
     * @return all properties
     */
    public Iterable<Property> properties();

    /**
     * Returns the number of properties in the element container.
     * 
     * @return the number of properties
     */
    public int getPropertiesCount();
    
    /**
     * Returns all operations in the element container.
     * 
     * @return all operations
     */
    public Iterable<Operation> operations();
    
    /**
     * Returns the number of operations in the element container.
     * 
     * @return the number of operations
     */
    public int getOperationsCount();
    
}
