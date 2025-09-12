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

package test.de.iip_ecosphere.platform.services.spring;

import de.iip_ecosphere.platform.support.plugins.CurrentClassloaderPluginSetupDescriptor;
import de.iip_ecosphere.platform.support.plugins.PluginManager;

/**
 * Abstract common test setup.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AbstractTest {

    static {
        PluginManager.registerPlugin(CurrentClassloaderPluginSetupDescriptor.INSTANCE);
    }
    
}
