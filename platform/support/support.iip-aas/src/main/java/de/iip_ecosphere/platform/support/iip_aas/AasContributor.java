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

package de.iip_ecosphere.platform.support.iip_aas;

import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.InvocablesCreator;
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor;
import de.iip_ecosphere.platform.support.jsl.ExcludeFirst;

/**
 * Service interface to contribute to an AAS. Platform components with individual AAS
 * shall implement this interface and contribute to the {@link AasPartRegistry}.
 * 
 * @author Holger Eichelberger, SSE
 * @see ExcludeFirst
 */
public interface AasContributor {
    
    /**
     * The kind of AAS being built.
     * 
     * @author Holger Eichelberger, SSE
     */
    public enum Kind {
        
        /**
         * A passive AAS with static information only.
         */
        PASSIVE,

        /**
         * A dynamic AAS with static and dynamic information, but no remote operations/properties. The structure
         * of the AAS may change dynamically.
         */
        DYNAMIC,
        
        /**
         * An active AAS which also provides remote operations/properties.
         */
        ACTIVE
    }
    
    /**
     * Contribute to the given {@code aasBuilder}.
     * 
     * @param aasBuilder the AAS to contribute to
     * @param iCreator the invocables creator for binding (remote) property and operation implementations. Property and 
     *    function names shall be qualified by the AAS short id.
     * @return the contributor may ignore {@code aasBuilder} and create an own AAS and return that. If this contributor 
     *   just contributes to the {@code aasBuilder} the result shall be <b>null</b>
     */
    public Aas contributeTo(AasBuilder aasBuilder, InvocablesCreator iCreator);
    
    /**
     * Returns the AAS authentication descriptor. [shortcut]
     * 
     * @return the AAS authentication descriptor, may be <b>null</b> for none
     * @see AasPartRegistry#getAasAuthentication()
     */
    public default AuthenticationDescriptor getAasAuthentication() {
        return AasPartRegistry.getAasAuthentication();
    }

    /**
     * Returns the submodel authentication descriptor. [shortcut]
     * 
     * @return the submodel authentication descriptor, may be <b>null</b> for none
     * @see AasPartRegistry#getSubmodelAuthentication()
     */
    public default AuthenticationDescriptor getSubmodelAuthentication() {
        return AasPartRegistry.getSubmodelAuthentication();
    }

    /**
     * Contributes the real implementation functions to the {@code sBuilder}. Names used for the {@code iCreator} in
     * {@link #contributeTo(AasBuilder, InvocablesCreator)} must be the same as used here. Property and function names
     * shall be qualified by the AAS short id.
     * 
     * @param sBuilder the server builder
     */
    public void contributeTo(ProtocolServerBuilder sBuilder);

    /**
     * Returns the kind of AAS being created. This is helpful for filtering.
     * 
     * @return the kind of AAS
     */
    public Kind getKind();
    
    /**
     * Allow the AAS contributor to declare itself as invalid.
     * 
     * @return {@code true} as prerequisite to execute {@link #contributeTo(ProtocolServerBuilder)} and 
     *     {@link #contributeTo(AasBuilder, InvocablesCreator)}, {@code false} else
     */
    public boolean isValid();
    
    /**
     * Returns whether the contribution of this contributor already exists. This would skip
     * this contributor. In particular considered when automatically re-building AAS after server disconnect.
     * 
     * @return {@code true} for exists, {@code false} else
     */
    public default boolean exists() {
        return false;
    }
    
}
