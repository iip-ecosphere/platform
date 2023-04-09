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

package de.iip_ecosphere.platform.support.metrics.bitmotec;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;

import de.iip_ecosphere.platform.support.metrics.SystemMetrics;
import de.iip_ecosphere.platform.support.metrics.SystemMetricsDescriptor;

/**
 * The default JSL system metrics descriptor. Enables the metrics if {@code /etc/os-release} or
 * {@code /etc/os-release-bitmo} looks like a bitmotec file. 
 * 
 * @author Holger Eichelberger, SSE
 */
public class BitmotecSystemMetricsDescriptor implements SystemMetricsDescriptor {

    @Override
    public SystemMetrics createInstance() {
        return BitmotecSystemMetrics.INSTANCE;
    }

    @Override
    public boolean isEnabled() {
        boolean enabled = isOsRelease(new File("/etc/os-release")); // as discussed with Bitmotec
        enabled |= isOsRelease(new File("/etc/os-release-bitmo")); // mounted in container
        // potential alternative: /etc/core/settings.env
        return enabled;
    }

    /**
     * Checks the given file considering it as {@code os-release} file.
     * 
     * @param file the file
     * @return {@code true} if bitmotec is detected, {@code false} else
     */
    private boolean isOsRelease(File file) {
        boolean enabled = false;
        try {
            String contents = FileUtils.readFileToString(file, Charset.defaultCharset());
            enabled = contents.contains("Bitmoteco Core OS");
        } catch (IOException e) {
            // ignore, no bitmotec
        }
        return enabled;
    }

    @Override 
    public boolean isFallback() {
        return false; // act as fallback if there is no specific enabled one
    }

}
