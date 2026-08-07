/**
 * ******************************************************************************
 * Copyright (c) {2026} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.oktoflow.platform.cmdTools;

/**
 * CSV conversion utils.
 * 
 * @author ChatGPT
 */
public class CsvUtils {

    /**
     * Escapes a value so it is safe to use in CSV.
     */
    public static String csvEscape(String value) {
        if (value.contains(",") 
            || value.contains("\"") 
            || value.contains("\n") 
            || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
    
}
