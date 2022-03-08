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

package de.iip_ecosphere.platform.test.apps.serviceImpl;

import org.slf4j.LoggerFactory;

import iip.datatypes.Rec13;

/**
 * Helper class to create data instances. <b>This is not required in normal service programming.</b> However,
 * we follow a lazy testing approach here, where services implement interfaces from generated shared- and non-shared 
 * platform interfaces at the same time. In the one mode, a data type name represents an interface, in the other mode
 * a class. We handle this here by (slow) reflection-based instance creation.
 *  
 * @author Holger Eichelberger, SSE
 */
public class SimpleApp3Helper {

    private static Class<?> ifCls;
    private static Class<?> implCls;
    
    static {
        try {
            ifCls = Class.forName("iip.datatypes.Rec13");
            // is a class in non-shared interface mode, is an interface in shared interface mode 
        } catch (ClassNotFoundException e) {
            LoggerFactory.getLogger(SimpleApp3Helper.class).error("Cannot find iip.Rec13!");
        }
        try {
            implCls = Class.forName("iip.datatypes.Rec13Impl");
        } catch (ClassNotFoundException e) {
            // iip.Rec1Impl is only there when shared interfaces were created 
        }
    }
    
    /**
     * Creates a data instance.
     * 
     * @return the data instance
     */
    static Rec13 createRec13Instance() {
        Rec13 result = null;
        try {
            if (implCls != null) {
                result = (Rec13) implCls.newInstance();
            } else if (ifCls != null) {
                result = (Rec13) ifCls.newInstance();
            }
        } catch (IllegalAccessException | InstantiationException e) {
            // shall not occur
        }
        return result;
    }
    
}
