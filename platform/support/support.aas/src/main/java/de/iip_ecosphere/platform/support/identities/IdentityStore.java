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
import java.security.Key;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import de.iip_ecosphere.platform.support.jsl.ServiceLoaderUtils;
import de.iip_ecosphere.platform.support.net.SslUtils;

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
     * Enumerates known identities based on a given {@code prefixId}.
     * 
     * @param prefixId the prefix id
     * @return the known identities
     */
    public abstract Iterable<String> enumerateIdentities(String prefixId);

    /**
     * Enumerates known identity tokens based on a given {@code prefixId}.
     * 
     * @param prefixId the prefix id
     * @return the known identities
     * @see #enumerateIdentities(String)
     */
    public Iterable<IdentityToken> enumerateTokens(String prefixId) {
        List<IdentityToken> result = new ArrayList<>();
        for (String id : enumerateIdentities(prefixId)) {
            result.add(getToken(id));
        }
        return result;
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
     * Returns a keystore coordinate, i.e., basic information to open a keystore.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class KeystoreCoordinate {
        
        private String file;
        private String password;

        /**
         * Creates a keystore coordinate.
         * 
         * @param file the file of the keystore
         * @param password the password
         */
        public KeystoreCoordinate(String file, String password) {
            this.file = file;
            this.password = password;
        }

        /**
         * Returns the file.
         * 
         * @return the file
         */
        public String getFile() {
            return file;
        }
        
        /**
         * Returns the password.
         * 
         * @return the password
         */
        public String getPassword() {
            return password;
        }
        
    }

    /**
     * Returns the keystore as coordinate as some mechanisms want the read the keystore by themselves. The result may
     * be the coordinate of a temporary keystore.
     * 
     * @param identity the identity (key) to return the keystore for
     * @param fallback fallback identities to use instead in given sequence, e.g., instead a specific device a 
     *     device group
     * @return the keystore coordinate, <b>null</b> if none was found
     * @throws IOException if creating/reading/opening a specified keystore fails
     */
    public abstract KeystoreCoordinate getKeystoreCoordinate(String identity, String... fallback) throws IOException;
    
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
     * Returns the key manager(s) for an identity key with the (see {@link KeyManagerFactory#getDefaultAlgorithm()} 
     * default key manager factory algorithm).
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

    /**
     * Returns a SSL/TlS context for the given {@code identity} with default key manager algorithm (see 
     * {@link KeyManagerFactory#getDefaultAlgorithm()}) and default context algorithm 
     * (see {@link SslUtils#DEFAULT_CONTEXT_ALG}).
     * 
     * @param identity the identity (key) to return the keystore password and the key manager(s) for
     * @param keyAlias the alias that the key manager shall support, may be <b>null</b> for none 
     * @param fallback fallback identities to use instead in given sequence, e.g., instead a specific device a 
     *     device group
     * @return the created SSL/TLS context
     * @throws IOException if creating/reading/opening a specified keystore/key manager fails
     * @see #getKeystoreFile(String, String...)
     * @see #getKeyManagers(String, String, String...)
     */
    public SSLContext createTlsContext(String identity, String keyAlias, String... fallback) throws IOException {
        return createTlsContext(identity, KeyManagerFactory.getDefaultAlgorithm(), keyAlias, 
            SslUtils.DEFAULT_CONTEXT_ALG, fallback);
    }
    
    /**
     * Returns a SSL/TlS context for the given {@code identity}. The identity must allow for obtaining an password
     * and a keystore.
     * 
     * @param identity the identity (key) to return the keystore password and the key manager(s) for
     * @param algorithm the key manager factory algorithm (see {@link KeyManagerFactory})
     * @param keyAlias the alias that the key manager shall support, may be <b>null</b> for none 
     * @param contextAlg the algorithm to initialize the SSL context with (see {@link SslUtils} for constants)
     * @param fallback fallback identities to use instead in given sequence, e.g., instead a specific device a 
     *     device group
     * @return the created SSL/TLS context
     * @throws IOException if creating/reading/opening a specified keystore/key manager fails
     * @see #getKeystoreFile(String, String...)
     * @see #getKeyManagers(String, String, String...)
     */
    public SSLContext createTlsContext(String identity, String algorithm, String keyAlias, 
        String contextAlg, String... fallback) throws IOException {
        SSLContext ctx = null;
        KeyStore ks = getKeystoreFile(identity, fallback);
        if (null != ks) {
            try {
                TrustManagerFactory tmf = SslUtils.createTrustManagerFactory(ks);
                KeyManager[] kms = getKeyManagers(identity, algorithm, fallback);
                if (null != keyAlias) {
                    kms = SslUtils.createProjectingKeyManagers(keyAlias, kms);
                }
                ctx = SSLContext.getInstance(contextAlg);
                ctx.init(kms, tmf.getTrustManagers(), null);
            } catch (NoSuchAlgorithmException | KeyManagementException e) {
                throw new IOException(e);
            }
        }
        return ctx;
    }

    /**
     * Returns a key from a keystore. 
     * 
     * @param identity the identity (key) to return the keystore password and the key manager(s) for
     * @param keyAlias the alias that the key manager shall support, may be <b>null</b> for none 
     * @param fallback fallback identities to use instead in given sequence, e.g., instead a specific device a 
     *     device group
     * @return the key
     * @throws IOException if creating/reading/opening a specified keystore/key manager fails
     */
    public Key getKeystoreKey(String identity, String keyAlias, String... fallback) throws IOException {
        return getKeystoreKey(identity, null, keyAlias, fallback);
    }

    /**
     * Returns a key from a keystore. 
     * 
     * @param identity the identity (key) to return the keystore password and the key manager(s) for
     * @param keystore an already known keystore matching to {@code identity}, may be <b>null</b> if the store shall 
     *     retrieve a keystore
     * @param keyAlias the alias that the key manager shall support, may be <b>null</b> for none 
     * @param fallback fallback identities to use instead in given sequence, e.g., instead a specific device a 
     *     device group
     * @return the key
     * @throws IOException if creating/reading/opening a specified keystore/key manager fails
     */
    public abstract Key getKeystoreKey(String identity, KeyStore keystore, String keyAlias, String... fallback) 
        throws IOException;
    
}
