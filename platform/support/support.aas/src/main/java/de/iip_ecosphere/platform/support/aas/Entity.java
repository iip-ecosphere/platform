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

import de.iip_ecosphere.platform.support.Builder;

/**
 * Defines the interface for a reference element.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface Entity extends SubmodelElement, SubmodelElementCollection {

    /**
     * Denotes the entity type.
     * 
     * @author Holger Eichelberger, SSE
     */
    public enum EntityType {
        
       COMANAGEDENTITY(),
       SELFMANAGEDENTITY();
        
    }
    
    /**
     * Defines the interface for a reference element builder.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface EntityBuilder extends SubmodelElementContainerBuilder, Builder<Entity> {

        /**
         * Returns the parent builder.
         * 
         * @return the parent builder
         */
        public SubmodelElementContainerBuilder getParentBuilder();
        
        /**
         * Sets the description in terms of language strings.
         * 
         * @param description the description
         * @return <b>this</b>
         */
        public EntityBuilder setDescription(LangString... description);

        
        /**
         * Sets the semantic ID of the property in terms of a reference.
         * 
         * @param refValue the reference value (supported: irdi:<i>irdiValue</i>)
         * @return <b>this</b>
         */
        public EntityBuilder setSemanticId(String refValue);
        
        /**
         * Sets the entity type.
         * 
         * @param type the entity type
         * 
         * @return <b>this</b>
         */
        public EntityBuilder setEntityType(EntityType type);

        /**
         * Sets the asset reference.
         * 
         * @param type the asset reference, may be ignored if the actual type does not match
         * 
         * @return <b>this</b>
         */
        public EntityBuilder setAsset(Reference asset);
        
        /**
         * Creates a reference to the entity under creation.
         * 
         * @return the reference
         */
        public Reference createReference();

    }
    
    /**
     * Returns the entity type.
     * 
     * @return the entity type
     */
    public EntityType getType();

}
