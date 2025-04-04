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

package de.iip_ecosphere.platform.tools.maven.dependencies;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * Reused build-classpath Mojo.
 * 
 * @author Holger Eichelberger, SSE
 */
@Mojo( name = "copy-dependencies", requiresDependencyResolution = ResolutionScope.TEST, 
    defaultPhase = LifecyclePhase.PROCESS_SOURCES, threadSafe = true )
public class CopyDependenciesMojo extends org.apache.maven.plugins.dependency.fromDependencies.CopyDependenciesMojo {
}
