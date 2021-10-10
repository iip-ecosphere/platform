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

package de.iip_ecosphere.platform.transport.connectors;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Properties;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509KeyManager;

/**
 * Some basic SSL helper methods.
 * 
 * @author Holger Eichelberger, SSE
 */
public class SslUtils {

    /**
     * Returns the keystore type based on the file name extension.
     * 
     * @param trustStore the truststore (JKS or PKCS12 with file extension ".p12")
     * @return "JKS" (default) or "PKCS12"
     */
    public static final String getKeystoreType(File trustStore) {
        String tName = trustStore.getName();
        String keystoreType = "JKS";
        if (tName.endsWith(".p12")) {
            keystoreType = "PKCS12";
        }
        return keystoreType;
    }

    /**
     * Opens a keystore {@code store}.
     * 
     * @param store the store file (JKS or PKCS12 with file extension ".p12")
     * @param storePass the password of the store, may be <b>null</b> for none
     * @return the keystore instance, <b>null</b> if {@code store} is <b>null</b> or does not exist
     * @throws IOException if the store cannot be opened
     */
    public static KeyStore openKeyStore(File store, String storePass) throws IOException {
        KeyStore tks = null;
        if (null != store && store.exists()) {
            try {
                String keystoreType = getKeystoreType(store);
                tks = KeyStore.getInstance(keystoreType);
                FileInputStream stream = new FileInputStream(store); 
                tks.load(stream, null == storePass ? null : storePass.toCharArray());
                stream.close();
            } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException e) {
                throw new IOException(e);
            }
        }
        return tks;
    }
    
    /**
     * Creates a (SunX509) trust manager factory from the given {@code trustStore}.
     * 
     * @param trustStore the truststore (JKS or PKCS12 with file extension ".p12")
     * @param storePass the password of the truststore, may be <b>null</b> for none
     * @return the trust manager factory, <b>null</b> if {@code trustStore} is <b>null</b> or does not exist
     * @throws IOException if the trust manager factory cannot be created
     */
    public static TrustManagerFactory createTrustManagerFactory(File trustStore, String storePass) throws IOException {
        TrustManagerFactory tmf = null;
        if (null != trustStore && trustStore.exists()) {
            try {
                KeyStore tks = openKeyStore(trustStore, storePass);
                tmf = TrustManagerFactory.getInstance("SunX509");
                tmf.init(tks);
            } catch (KeyStoreException | NoSuchAlgorithmException e) {
                throw new IOException(e);
            }
        }
        return tmf;
    }

    /**
     * Creates a TLS SSL context from the given {@code trustStore}.
     * 
     * @param trustStore the truststore (must be a JKS with SunX509)
     * @param storePass the password of the truststore, may be <b>null</b> for none
     * @return the TLS-SSL context, <b>null</b> if {@code trustStore} is <b>null</b> or does not exist
     * @throws IOException if the SSL context cannot be created
     * @see {@link #createTrustMangerFactory(File, String)}
     */
    public static SSLContext createTlsContext(File trustStore, String storePass) throws IOException {
        /*SSLContext ctx = null;
        TrustManagerFactory tmf = createTrustManagerFactory(trustStore, storePass);
        if (null != tmf) {
            try {
                ctx = SSLContext.getInstance("TLS");
                ctx.init(null, tmf.getTrustManagers(), null);
            } catch (NoSuchAlgorithmException | KeyManagementException e) {
                throw new IOException(e);
            }
        }
        return ctx;*/
        return createTlsContext(trustStore, storePass, null);
    }
    
    /**
     * Creates a TLS SSL context from the given {@code trustStore} for a certain {@code alias].
     * 
     * @param trustStore the truststore (must be a JKS with SunX509)
     * @param storePass the password of the truststore, may be <b>null</b> for none
     * @param alias alias of the key to use (may be <b>null</b> for none/first match)
     * @return the TLS-SSL context, <b>null</b> if {@code trustStore} is <b>null</b> or does not exist
     * @throws IOException if the SSL context cannot be created
     * @see {@link #createTrustMangerFactory(File, String)}
     */
    public static SSLContext createTlsContext(File trustStore, String storePass, String alias) throws IOException {
        SSLContext ctx = null;
        KeyStore ks = openKeyStore(trustStore, storePass);
        if (null != ks) {
            try {
                TrustManagerFactory tmf = createTrustManagerFactory(trustStore, storePass);
                KeyManager[] kms = null;
                if (null != alias) {
                    KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                    kmf.init(ks, storePass.toCharArray());
        
                    final X509KeyManager origKm = (X509KeyManager) kmf.getKeyManagers()[0];
        
                    X509KeyManager km = new X509KeyManager() {
                        public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket) {
                            return "foo";
                        }
        
                        public X509Certificate[] getCertificateChain(String alias) {
                            return origKm.getCertificateChain(alias);
                        }
        
                        @Override
                        public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
                            return origKm.chooseClientAlias(null, issuers, socket);
                        }
        
                        @Override
                        public String[] getClientAliases(String keyType, Principal[] issuers) {
                            return origKm.getClientAliases(keyType, issuers);
                        }
        
                        @Override
                        public PrivateKey getPrivateKey(String alias) {
                            return origKm.getPrivateKey(alias);
                        }
        
                        @Override
                        public String[] getServerAliases(String keyType, Principal[] issuers) {
                            return origKm.getServerAliases(keyType, issuers);
                        }
        
                    };
                    kms = new KeyManager[] {km};
                }
                ctx = SSLContext.getInstance("TLS");
                ctx.init(kms, tmf.getTrustManagers(), null);
            } catch (NoSuchAlgorithmException | UnrecoverableKeyException | KeyStoreException 
                | KeyManagementException e) {
                throw new IOException(e);
            }
        }
        return ctx;
    }
    
    /**
     * Creates a properties set for an IBM-based TLS implementation.
     * 
     * @param trustStore the truststore (must be a JKS with SunX509)
     * @param storePass the password of the truststore, may be <b>null</b> for none
     * @return the properties set or <b>null</b> if {@code trustStore} is <b>null</b> or does not exist
     */
    public static Properties createIbmTlsProperties(File trustStore, String storePass) {
        Properties result = null;
        if (null != trustStore && trustStore.exists()) {
            result = new Properties();
            result.put("com.ibm.ssl.protocol", "TLS"); // SSL, SSLv3, TLS, TLSv1, SSL_TLS
            // com.ibm.ssl.contextProvider; "IBMJSSE2" or "SunJSSE"
            result.put("com.ibm.ssl.keyStore", trustStore.getAbsoluteFile());
            if (null != storePass) {
                // plaintext or com.ibm.micro.security.Password.obfuscate(char[] password).
                result.put("com.ibm.ssl.keyStorePassword", storePass); 
            }
            result.put("com.ibm.ssl.keyStoreType", getKeystoreType(trustStore));
            // com.ibm.ssl.trustStoreProvider, e.g., "IBMJCE" or "IBMJCEFIPS"
            // com.ibm.ssl.keyStoreProvider, e.g., "IBMJCE" or "IBMJCEFIPS"
            // com.ibm.ssl.keyStore
            // com.ibm.ssl.keyStorePassword
            // com.ibm.ssl.keyStoreType
            // com.ibm.ssl.enabledCipherSuites, e.g., SSL_RSA_WITH_AES_128_CBC_SHA;SSL_RSA_WITH_3DES_EDE_CBC_SHA
            // com.ibm.ssl.keyManager. e.g., Example values: "IbmX509" or "IBMJ9X509"
            // com.ibm.ssl.trustManager, "PKIX" or "IBMJ9X509"
        }
        return result;
    }
    
}
