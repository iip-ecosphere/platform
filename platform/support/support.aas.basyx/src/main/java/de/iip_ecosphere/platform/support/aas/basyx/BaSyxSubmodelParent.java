package de.iip_ecosphere.platform.support.aas.basyx;

import de.iip_ecosphere.platform.support.aas.basyx.AbstractAas.BaSyxAbstractAasBuilder;

/**
 * Represents the parent instance of a sub-model. Due to the two different AAS types in BaSyx, this
 * cannot just be an AAS instance rather than a pseudo instance being able to provide the correct 
 * operations.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface BaSyxSubmodelParent {
    
    /**
     * Creates an AAS builder on parent level.
     * 
     * @return the AAS builder
     */
    public BaSyxAbstractAasBuilder createAasBuilder();
    
}