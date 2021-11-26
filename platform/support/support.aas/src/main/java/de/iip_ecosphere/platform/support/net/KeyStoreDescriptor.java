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

package de.iip_ecosphere.platform.support.net;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

/**
 * Describes the location and access to a key/trust store.
 * 
 * @author Holger Eichelberger, SSE
 */
public class KeyStoreDescriptor implements Serializable {

    private static final long serialVersionUID = 1L;
    private File path;
    private String password;
    private String alias;
    
    /**
     * Creates a descriptor.
     * 
     * @param path the path to the key file/store, no encryption if <b>null</b> or non-existent
     * @param password the password to access the key file/store
     * @param alias the alias denoting the key/certificate to use, ignored if <b>null</b>
     */
    public KeyStoreDescriptor(File path, String password, String alias) {
        this.path = path;
        this.password = password;
        this.alias = alias;
    }

    /**
     * Returns the path to the key store.
     * 
     * @return the path to the key file/store, no encryption if <b>null</b> or non-existent
     */
    public File getPath() {
        return path;
    }

    /**
     * Returns the absolute path to the key store.
     * 
     * @return the absolute path, may be <b>null</b> if the path itself is null; shall be canonical if possible
     */
    public String getAbsolutePath() {
        String result = null;
        if (null != path) {
            try {
                result = path.getCanonicalFile().getAbsolutePath();
            } catch (IOException e) {
                result = path.getAbsolutePath();
            }
        }
        return result;
    }

    /**
     * Returns the password.
     * 
     * @return the password to access the key file/store
     */
    public String getPassword() {
        return password;
    }

    /**
     * Returns the key alias.
     * 
     * @return the alias denoting the key/certificate to use, ignored if <b>null</b>
     */
    public String getAlias() {
        return alias;
    }
    
}
