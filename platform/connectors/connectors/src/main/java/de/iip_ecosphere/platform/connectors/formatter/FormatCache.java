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

package de.iip_ecosphere.platform.connectors.formatter;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Caches formatter instances.
 * 
 * @author Holger Eichelberger, SSE
 */
public class FormatCache {
    
    private static final Map<String, SimpleDateFormat> DATE_FORMATTER = new HashMap<>();
    
    /**
     * Returns a (cached) simple date formatter.
     * 
     * @param format the format for the requested formatter
     * @return the formatter instance
     * @throws IOException if {@code format} is not valid
     */
    public static SimpleDateFormat getDateFormatter(String format) throws IOException {
        SimpleDateFormat result = DATE_FORMATTER.get(format);
        if (null == result) {
            try {
                result = new SimpleDateFormat(format);
                DATE_FORMATTER.put(format, result);
            } catch (IllegalArgumentException e) {
                throw new IOException(e);
            }
        }
        return result;
    }

}
