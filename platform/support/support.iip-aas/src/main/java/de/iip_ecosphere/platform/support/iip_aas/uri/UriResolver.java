/**
 * ******************************************************************************
 * Copyright (c) {2021} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.iip_aas.uri;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Resolves URIs to files.
 * 
 * @author Holger Eichelberger, SSE
 */
public class UriResolver {

    /**
     * Resolves an external/remote {@code uri} to a local file URI. if {@code uri} is already a file, {@code uri} is 
     * returned.
     * 
     * @param uri the URI to resolve
     * @param dir the directory to store resolved files, may be <b>null</b> for temporary files
     * @return the URI that resolves to a local file
     * @throws IOException in case that URI cannot be resolved/opened/transferred
     */
    public static File resolveToFile(URI uri, File dir) throws IOException {
        File result = null;
        // rather simple approach, more schemes needed? next version may provide plugins 
        if ("file".equals(uri.getScheme())) {
            result = new File(uri);
        } else {
            URL url = uri.toURL();
            File f = null;
            try (InputStream in = url.openStream()) {
                String path = uri.getPath();
                int pos = path.lastIndexOf("/");
                if (pos > 0 && pos < path.length() - 1) {
                    path = path.substring(pos + 1);
                }
                pos = path.lastIndexOf(".");
                String suffix = "";
                if (pos > 0) {
                    suffix = path.substring(pos);
                    path = path.substring(0, pos);
                }
                if (null == dir) {
                    f = File.createTempFile("iip", suffix);
                } else {
                    path += "-" + System.currentTimeMillis() + suffix;
                    f = new File(dir, path);
                }
                Files.copy(in, f.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw e;
            }
            result = f;
        }
        return result;
    }

}
