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

package test.de.iip_ecosphere.platform.support.commons;

import de.iip_ecosphere.platform.support.commons.Commons;
import de.iip_ecosphere.platform.support.commons.CommonsProviderDescriptor;
import de.iip_ecosphere.platform.support.jsl.ExcludeFirst;

/**
 * The test Commons plugin descriptor.
 * 
 * @author Holger Eichelberger, SSE
 */
@ExcludeFirst // reduce priority if there is a plugin
public class TestCommonsPluginDescriptor implements CommonsProviderDescriptor {
    
    @Override
    public Commons create() {
        return new TestCommons();
    }
    
}
