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

/**
 * Represents an identifiable.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface Identifiable extends Referable {

 // TODO public IAdministrativeInformation getAdministration();
    
    /**
     * Returns the identification of the identifiable.
     * 
     * @return the identification (prefixed according to {@link IdentifierType}, custom if none matches). Can e.g. be 
     *     used with {@link Registry} if not <b>null</b>.
     */
    public String getIdentification();
    
}
