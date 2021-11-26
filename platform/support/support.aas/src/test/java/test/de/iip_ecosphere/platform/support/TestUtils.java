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

package test.de.iip_ecosphere.platform.support;

import de.iip_ecosphere.platform.support.NetUtils;

/**
 * Some common test utilities. These functions shall not be part of production code!
 * 
 * @author Holger Eichelberger, SSE
 */
public class TestUtils {
    
    /**
     * Returns whether this JVM is currently executing on the SSE CI Jenkins.
     * This may be needed to disable some tests.
     * 
     * @return {@code true} if we are running on SSE CI, {@code false}
     */
    public static boolean isSseCI() {
        return NetUtils.getOwnHostname().indexOf("jenkins") >= 0;
    }

}
