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

package de.iip_ecosphere.platform.maven;

import de.iip_ecosphere.platform.maven.MavenUtils.CleanupStatistics;

/**
 * Performs a cleanup except for the most recent 3 generations.
 * 
 * @author Holger Eichelberger, SSE
 */
public class CleanMvnSnapshots {

    /**
     * Performs a cleanup except for the most recent 3 generations.
     * 
     * @param args ignored
     */
    public static void main(String[] args) {
        CleanupStatistics statistics = MavenUtils.cleanSnapshots(3);

        System.out.println("Cleaned up " + statistics.getFileCount() + " files with " 
            + MavenUtils.humanReadableByteCount(statistics.getBytesCleared(), false) + " in summary.");
    }

}
