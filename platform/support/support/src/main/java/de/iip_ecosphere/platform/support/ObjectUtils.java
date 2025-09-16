/**
 * ******************************************************************************
 * Copyright (c) {2025} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support;

import java.util.concurrent.ExecutionException;

import de.iip_ecosphere.platform.support.commons.Commons;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;

/**
 * Object utilities.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ObjectUtils {

    /**
     * Copies all fields from the {@code source} to the {@code target} object.
     * 
     * @param source the source object
     * @param target the target object
     * @throws ExecutionException if copying fails
     */
    public static void copyFields(Object source, Object target) throws ExecutionException {
        Commons.getInstance().copyFields(source, target);
    }

    /**
     * Copies all fields from the {@code source} to the {@code target} object.
     * Logs errors/exceptions.
     * 
     * @param source the source object
     * @param target the target object
     */
    public static void copyFieldsSafe(Object source, Object target) {
        try {
            copyFields(source, target);
        } catch (ExecutionException e) {
            LoggerFactory.getLogger(ObjectUtils.class).error("While copying data fields: {}", 
                e.getMessage());
        }
    }

}
