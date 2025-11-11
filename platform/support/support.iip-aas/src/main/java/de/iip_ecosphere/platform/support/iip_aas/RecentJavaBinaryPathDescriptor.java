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

package de.iip_ecosphere.platform.support.iip_aas;

import java.util.Optional;
import java.util.function.Supplier;

import de.iip_ecosphere.platform.support.JavaBinaryPathDescriptor;

/**
 * Supplies the binary path of a more recent JVM than 8.
 * 
 * @author Holger Eichelberger, SSE
 */
public class RecentJavaBinaryPathDescriptor implements JavaBinaryPathDescriptor {

    @Override
    public Supplier<String> createSupplier() {
        return () -> {
            Optional<String> jp = ProcessHandle.current()
                .info()
                .command();
            if (jp.isPresent()) {
                return jp.get();
            } else {
                return null;
            }
        };
    }

}
