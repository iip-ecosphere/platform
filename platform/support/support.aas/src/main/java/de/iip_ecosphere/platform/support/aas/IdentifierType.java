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

/**
 * Common identifiers to be considered by implementations.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface IdentifierType {
    
    /**
     * URN with valid syntax following the prefix. The prefix shall remain with the identifier.
     */
    public static final String URN_PREFIX = "urn:";

    /**
     * URN with raw text following the prefix. The prefix shall be removed from the following raw text.
     */
    public static final String URN_TEXT_PREFIX = "urnText:";

}
