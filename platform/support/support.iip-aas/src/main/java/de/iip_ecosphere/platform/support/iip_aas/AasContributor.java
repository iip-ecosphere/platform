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
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;

/**
 * Service interface to contribute to an AAS. Platform components with individual AAS
 * shall implement this interface and contribute to the {@link AasPartRegistry}.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface AasContributor {
    
    /**
     * Contribute to the given {@code aasBuilder}.
     * 
     * @param aasBuilder the AAS to contribute to
     * @return the contributor may ignore {@link aasBuilder} and create an own AAS and return that. If this contributor 
     *   just contributes to the {@code aasBuilder} the result shall be <b>null</b>
     */
    public Aas contributeTo(AasBuilder aasBuilder);

}
