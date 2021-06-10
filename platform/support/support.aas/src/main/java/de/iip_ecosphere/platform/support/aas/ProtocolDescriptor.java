/**
 * ******************************************************************************
 * Copyright (c) {2021} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.aas;

import de.iip_ecosphere.platform.support.aas.AasFactory.ProtocolCreator;
import de.iip_ecosphere.platform.support.jsl.ExcludeFirst;

/**
 * A JSL protocol descriptor for adding custom protocols. Protocols introduced via JLS can be overridden
 * through fixed protocols defined by an AAS. Multiple descriptors given in a services file are considered. Test
 * descriptors shall be annotated with {@link ExcludeFirst} to avoid that they occur in real factories.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface ProtocolDescriptor {

    /**
     * Returns the protocol name as to occur in {@link AasFactory#getProtocols()}.
     * 
     * @return the protocol name
     */
    public String getName();
    
    /**
     * Returns the protocol creator instance.
     * 
     * @return the instance
     */
    public ProtocolCreator createInstance();
    
}
