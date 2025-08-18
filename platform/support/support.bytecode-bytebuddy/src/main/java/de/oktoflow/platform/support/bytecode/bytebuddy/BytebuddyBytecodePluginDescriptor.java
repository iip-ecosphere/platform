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

package de.oktoflow.platform.support.bytecode.bytebuddy;

import de.iip_ecosphere.platform.support.bytecode.Bytecode;
import de.iip_ecosphere.platform.support.bytecode.BytecodeProviderDescriptor;
import de.iip_ecosphere.platform.support.jsl.ExcludeFirst;
import de.iip_ecosphere.platform.support.plugins.SingletonPluginDescriptor;

/**
 * The Bytebuddy plugin descriptor.
 * 
 * @author Holger Eichelberger, SSE
 */
@ExcludeFirst // reduce priority if there is a plugin
public class BytebuddyBytecodePluginDescriptor extends SingletonPluginDescriptor<Bytecode> 
    implements BytecodeProviderDescriptor {
    
    /**
     * Creates the descriptor.
     */
    public BytebuddyBytecodePluginDescriptor() {
        super("bytecode-bytebuddy", null, Bytecode.class, p -> new BytebuddyBytecode());
    }
    
    @Override
    public Bytecode create() {
        return new BytebuddyBytecode();
    }
    
}
