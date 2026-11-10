/**
 * ******************************************************************************
 * Copyright (c) {2026} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.oktoflow.platform.cmdTools.modules;

import java.nio.file.Path;

import javax.lang.model.SourceVersion;

/**
 * Module utilities.
 * 
 * @author ChatGPT
 * @author Holger Eichelberger, SSE
 */
class ModuleUtils {

    /**
     * Determines whether a Java module name contains invalid identifiers,
     * such as Java keywords or reserved literals.
     * Checks all dot-separated components of the module name.
     *
     * @param moduleName the module name to inspect
     * @return {@code true} if the module name contains invalid identifiers,
     *         {@code false} otherwise
     */
    static boolean containsJavaKeywords(String moduleName) {
        return moduleName != null
            && !SourceVersion.isName(moduleName);
    }

    /**
     * Emit and return {@code path}.
     * 
     * @param path the path to emit
     * @return the emitted path
     */
    static Path emit(Path path) {
        System.out.println(" - " + path);
        return path;
    }

}
