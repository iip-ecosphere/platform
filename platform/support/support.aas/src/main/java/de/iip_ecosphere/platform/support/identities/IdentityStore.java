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

package de.iip_ecosphere.platform.support.identities;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.Optional;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;

import de.iip_ecosphere.platform.support.jsl.ServiceLoaderUtils;

/**
 * Pluggable identity store mapping abstract names to tokens. Use abstract names in the configuration model and a
 * related identity store.
 * 
 * Loaded via {@link IdentityStoreDescriptor}. Default for none is {@link YamlIdentityStore}.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class IdentityStore {

    private static IdentityStore instance;
    
    /**
     * Returns an instance.
     * 
     * @return the instance
     */
    public static IdentityStore getInstance() {
        if (null == instance) {
            Optional<IdentityStoreDescriptor> desc = ServiceLoaderUtils.findFirst(IdentityStoreDescriptor.class);
            if (desc.isPresent()) {
                instance = desc.get().createStore();
            }
            if (null == instance) { // fallback
                instance = new YamlIdentityStore();
            }
        } 
        return instance;
    }

    /**
     * Returns an identity token returning <b>null</b> if none was found.
     * 
     * @param identity the identity (key) to return the token for
     * @param fallback fallback identities to use instead in given sequence, e.g., instead a specific device a 
     *     device group
     * @return the token, <b>null</b> if none was found
     */
    public IdentityToken getToken(String identity, String... fallback) {
        return getToken(identity, false, fallback);
    }

    /**
     * Returns an identity token.
     * 
     * @param identity the identity (key) to return the token for
     * @param defltAnonymous whether an anonymous token shall be returned instead of <b>null</b>
     * @param fallback fallback identities to use instead in given sequence, e.g., instead a specific device a 
     *     device group
     * @return the token, <b>null</b> or anonymous if none was found
     */
    public abstract IdentityToken getToken(String identity, boolean defltAnonymous, String... fallback);
    
    /**
     * Returns a keystore as a stream for an identity key.
     * 
     * @param identity the identity (key) to return the keystore for
     * @param fallback fallback identities to use instead in given sequence, e.g., instead a specific device a 
     *     device group
     * @return the keystore stream, <b>null</b> if none was found
     */
    public abstract InputStream getKeystoreAsStream(String identity, String... fallback);
    
    /**
     * Returns a keystore for an identity key.
     * 
     * @param identity the identity (key) to return the keystore for
     * @param fallback fallback identities to use instead in given sequence, e.g., instead a specific device a 
     *     device group
     * @return the keystore, <b>null</b> if none was found
     * @throws IOException if creating/reading/opening a specified keystore fails
     */
    public abstract KeyStore getKeystoreFile(String identity, String... fallback) throws IOException;
 
    /**
     * Returns the key manager(s) for an identity key using a specific key manager factory algorithm.
     * 
     * @param identity the identity (key) to return the key manager(s) for
     * @param algorithm the key manager factory algorithm (see {@link KeyManagerFactory}).
     * @param fallback fallback identities to use instead in given sequence, e.g., instead a specific device a 
     *     device group
     * @return the key manager(s), <b>null</b> if none was found
     * @throws IOException if creating/reading/opening a specified key manager fails
     */
    public abstract KeyManager[] getKeyManagers(String identity, String algorithm, String... fallback) 
        throws IOException;

    /**
     * Returns the key manager(s) for an identity key with the (see {@link KeyManagerFactory#getAlgorithm()} default 
     * key manager factory algorithm).
     * 
     * @param identity the identity (key) to return the key manager(s) for
     * @param fallback fallback identities to use instead in given sequence, e.g., instead a specific device a 
     *     device group
     * @return the key manager(s), <b>null</b> if none was found
     * @throws IOException if creating/reading/opening a specified key manager fails
     */
    public KeyManager[] getKeyManagers(String identity, String... fallback) throws IOException {
        return getKeyManagers(identity, KeyManagerFactory.getDefaultAlgorithm(), fallback);
    }
    
}
