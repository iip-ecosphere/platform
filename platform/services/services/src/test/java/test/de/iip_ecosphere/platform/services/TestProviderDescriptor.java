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

package test.de.iip_ecosphere.platform.services;

import de.iip_ecosphere.platform.support.jsl.ExcludeFirst;

/**
 * For testing test provisioning.
 * 
 * @author Holger Eichelberger, SSE
 */
@ExcludeFirst // give priority to real JSL descriptors
public class TestProviderDescriptor implements de.iip_ecosphere.platform.services.TestProviderDescriptor {

    @Override
    public Class<?>[] getTests(int index) {
        return index >= 0 && index < 2 ? new Class<?>[] {Object.class} : null;
    }

}
