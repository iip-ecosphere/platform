/**
 * ******************************************************************************
 * Copyright (c) {2022} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.connectors.events;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Implements a query represented by a regular expression on the relevant data identifier of a connector.
 * 
 * @author Holger Eichelberger, SSE
 */
public class PatternTriggerQuery implements ConnectorTriggerQuery {

    private Pattern pattern;
    
    /**
     * Creates a pattern-based trigger query.
     * 
     * @param regEx the Java regular expression
     * @throws PatternSyntaxException if {@code regEx} is invalid
     */
    public PatternTriggerQuery(String regEx) throws PatternSyntaxException {
        pattern = Pattern.compile(regEx);
    }
    
    /**
     * Returns the query pattern.
     * 
     * @return the query pattern
     */
    public Pattern getPattern() {
        return pattern;
    }
    
}
