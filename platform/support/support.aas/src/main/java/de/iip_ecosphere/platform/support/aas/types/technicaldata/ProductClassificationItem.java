package de.iip_ecosphere.platform.support.aas.types.technicaldata;

import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;

/**
 * Defines the interface of a product classification item.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface ProductClassificationItem extends SubmodelElementCollection {

    /**
     * The general information builder.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface ProductClassificationItemBuilder extends SubmodelElementCollectionBuilder {

        /**
         * Defines the version of the classification system.
         * 
         * @param version the version
         * @return <b>this</b>
         */
        public ProductClassificationItemBuilder setClassificationSystemVersion(String version);

    }
    
    /**
     * Returns the class of the associated product or industrial equipment in the classification system according to 
     * the notation of the system.
     * 
     * @return the product class id, which is ideally used to reference the IRI/ IRDI of the product class.
     */
    public String getProductClassId();

    /**
     * Returns the common name of the classification system. Examples for common names for classification systems are 
     * "ECLASS" or "IEC CDD".
     * 
     * @return the name of the classification system
     */
    public String getProductClassificationSystem();
    
    /**
     * Returns the version of the classification system.
     * 
     * @return the version (may be <b>null</b>)
     */
    public String getClassificationSystemVersion();
    
}