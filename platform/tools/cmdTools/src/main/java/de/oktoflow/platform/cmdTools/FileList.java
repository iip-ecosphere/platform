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

package de.oktoflow.platform.cmdTools;

import java.io.File;

/**
 * Lists a directory so that excel can import it.
 * 
 * @author Holger Eichelberger, SSE
 */
public class FileList {
    
    /**
     * Lists a directory contents.
     * 
     * @param args the first argument is the directory to list
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: dir");
        } else {
            File[] files = new File(args[0]).listFiles();
            if (null != files) {
                for (File f : files) {
                    if (f.isFile()) {
                        System.out.println(f.getName() + "|" + f.length());
                    }
                }
            }
        }
    }

}
