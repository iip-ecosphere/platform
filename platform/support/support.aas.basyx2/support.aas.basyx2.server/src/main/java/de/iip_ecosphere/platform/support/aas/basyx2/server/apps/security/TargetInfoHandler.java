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

package de.iip_ecosphere.platform.support.aas.basyx2.server.apps.security;

import java.util.List;

import org.eclipse.digitaltwin.basyx.authorization.rbac.TargetInformation;

import de.iip_ecosphere.platform.support.CollectionUtils;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor.RbacRule;

/**
 * BaSyX target information creator and handler.
 * 
 * @param <T> target information type
 * @author Holger Eichelberger, SSE
 */
abstract class TargetInfoHandler<T extends TargetInformation> {

    private Class<T> cls;
    
    /**
     * Creates an instance.
     * 
     * @param cls the class being handled/created
     */
    TargetInfoHandler(Class<T> cls) {
        this.cls = cls;
    }
    
    /**
     * Creates an instance.
     * 
     * @param rule the rule to create the instance for
     * @return the instance
     */
    public abstract T create(RbacRule rule);
    
    /**
     * Returns the type of information being handled.
     * @return the type
     */
    public Class<? extends T> getType() {
        return cls;
    }

    /**
     * Joins two given target information instances.
     * 
     * @param t1 the first target information (to join into)
     * @param t2 the second target information (to join)
     * @return the joined instance, may be {@code t1} if modifiable
     */
    public abstract T join(T t1, T t2);

    /**
     * Returns the name of {@code #getType()}.
     * 
     * @return the name
     */
    public String getTypeName() {
        return getType().getName();
    }

    /**
     * Turns a generic rule path to a BaSyx rule path.
     * 
     * @param rule the rule, paths may be <b>null</b>
     * @return the paths
     */
    static final List<String> toPaths(RbacRule rule) {
        List<String> result = null;
        String path = rule.getPath();
        if (null != path) {
            path = path.replace(RbacRule.PATH_SEPARATOR, ".");
            result = CollectionUtils.toList(path);
        }
        return result;
    }

}