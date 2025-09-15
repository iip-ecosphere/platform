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

package iip;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import de.iip_ecosphere.platform.support.FileUtils;

/**
 * Simple Starter for testing.
 * 
 * @author Holger Eichelberger, SSE
 */
public class Starter {

    /**
     * Runs the starter.
     * 
     * @param args first argument may be the name of the test file to be written, else "AppStarter.test" in temp 
     *     is assumed
     */
    public static void main(String[] args) {
        File tmpFile;
        if (args.length > 0) {
            tmpFile = new File(args[0]);
        } else {
            tmpFile = new File(FileUtils.getTempDirectory(), "AppStarter.test");
        }
        tmpFile.delete();
        try {
            System.out.println("Writing to file " + tmpFile);
            Files.writeString(tmpFile.toPath(), "iip.Starter", StandardCharsets.UTF_8);        
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

}
