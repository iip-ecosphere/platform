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

import de.iip_ecosphere.platform.support.identities.IdentityStore;
import de.iip_ecosphere.platform.support.identities.IdentityStore.KeystoreCoordinate;

/**
 * Describes the location and access to a key/trust store.
 * 
 * @author Holger Eichelberger, SSE
 */
public class KeyStoreDescriptor implements Serializable {

    private static final long serialVersionUID = 8475063607431932314L;
    private File path;
    private String password;
    private String alias;
    private boolean appliesToClient = true;
    private boolean hostNameVerification = true;

    /**
     * Creates a descriptor.
     * 
     * @param path the path to the key file/store, no encryption if <b>null</b> or non-existent
     * @param password the password to access the key file/store
     * @param alias the alias denoting the key/certificate to use, ignored if <b>null</b>
     */
    public KeyStoreDescriptor(File path, String password, String alias) {
        this(path, password, alias, true, true);
    }
    
    /**
     * Creates a descriptor.
     * 
     * @param path the path to the key file/store, no encryption if <b>null</b> or non-existent
     * @param password the password to access the key file/store
     * @param alias the alias denoting the key/certificate to use, ignored if <b>null</b>
     * @param appliesToClient whether this keystore applies to client instances or shall be ignored there
     * @param hostNameVerification enable/disable hostname verification
     */
    public KeyStoreDescriptor(File path, String password, String alias, boolean appliesToClient, 
        boolean hostNameVerification) {
        this.path = path;
        this.password = password;
        this.alias = alias;
        this.hostNameVerification = true;
        this.appliesToClient = true;
    }

    /**
     * Creates a descriptor.
     * 
     * @param keystore the keystore coordinate containing path and password
     * @param alias the alias denoting the key/certificate to use, ignored if <b>null</b>
     * @param appliesToClient whether this keystore applies to client instances or shall be ignored there
     * @param hostNameVerification enable/disable hostname verification
     * @throws IOException if the keystore cannot be accessed
     */
    public KeyStoreDescriptor(KeystoreCoordinate keystore, String alias, boolean appliesToClient, 
        boolean hostNameVerification) {
        this(new File(keystore.getPassword()), keystore.getPassword(), alias, appliesToClient, hostNameVerification);
    }

    /**
     * Creates a descriptor from a {@code keystoreKey} via the {@link IdentityStore}.
     * 
     * @param keystoreKey the keystore key to obtain the keystore from the identitiy store
     * @param alias the alias denoting the key/certificate to use, ignored if <b>null</b>
     * @param appliesToClient whether this keystore applies to client instances or shall be ignored there
     * @param hostNameVerification enable/disable hostname verification
     * @throws IOException if the keystore cannot be accessed
     */
    public static KeyStoreDescriptor create(String keystoreKey, String alias, boolean appliesToClient, 
        boolean hostNameVerification) throws IOException {
        KeystoreCoordinate keystore = IdentityStore.getInstance().getKeystoreCoordinate(keystoreKey);
        if (null == keystore) {
            throw new IOException("Keystore with key '" + keystoreKey + "' not found.");
        }
        return new KeyStoreDescriptor(keystore, alias, appliesToClient, hostNameVerification);
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
    
    /**
     * Whether this keystore applies to client instances or shall be ignored there.
     * 
     * @return {@code true} for client, {@code false} for not client
     */
    public boolean appliesToClient() {
        return appliesToClient;
    }
    
    /**
     * Returns whether hostname verification shall be applied. May not be applied to every HTTP client.
     * 
     * @return {@code true} for hostname verification, {@code false} els
     */
    public boolean applyHostnameVerification() {
        return hostNameVerification;
    }
    
}
