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

package de.iip_ecosphere.platform.support.plugins;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * A runtime-only plugin setup descriptor based on classpath and/or index given as stream.
 * 
 * @author Holger Eichelberger, SSE
 */
public class StreamClasspathPluginSetupDescriptor extends URLPluginSetupDescriptor {
    
    private InputStream idx;

    /**
     * Creates a classpath plugin setup descriptor based on streams.
     * 
     * @param cp the classpath stream
     * @param idx the index file stream (may be <b>null</b> for none)
     * @param base the base folder
     * @throws IOException if reading the classpath file fails.
     */
    public StreamClasspathPluginSetupDescriptor(InputStream cp, InputStream idx, File base) throws IOException {
        super(FolderClasspathPluginSetupDescriptor.loadClasspathFileSafe(cp, base, false, null));
        this.idx = idx;
    }
    
    @Override
    protected InputStream getIndexStream() {
        return idx;
    }

}
