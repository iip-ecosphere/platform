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

package test.de.iip_ecosphere.platform.support.bytecode;

/**
 * Implements an empty Bytecode interface for simple testing.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TestBytecode extends de.iip_ecosphere.platform.support.bytecode.Bytecode {

    @Override
    public <T> ClassBuilder<T> createClassBuilder(String name, Class<T> cls, ClassLoader loader) {
        return null;
    }

}
