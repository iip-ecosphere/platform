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

package test.de.iip_ecosphere.platform.transport;

import java.io.IOException;

import org.json.simple.JSONObject;

/**
 * Utility methods for JSON serialization.
 * 
 * @author Holger Eichelberger, SSE
 */
public class JsonUtils {

    /**
     * Reads a string field from a JSON object.
     * 
     * @param obj   the object to read from
     * @param field the field to read from
     * @return the string value or <b>null</b>
     */
    public static String readString(JSONObject obj, String field) {
        Object tmp = obj.get(field);
        return null == tmp ? null : tmp.toString();
    }

    /**
     * Reads a double field from a JSON object.
     * 
     * @param obj   the object to read from
     * @param field the field to read from
     * @param dflt  the default value
     * @return the double value, if not accessible {@code dflt}
     * @throws IOException if parsing the double value fails
     */
    public static double readDouble(JSONObject obj, String field, double dflt) throws IOException {
        try {
            Object tmp = obj.get(field);
            return null == tmp ? dflt : Double.parseDouble(tmp.toString());
        } catch (NumberFormatException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

}
