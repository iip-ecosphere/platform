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
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Properties;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

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
                String keystoreType = getKeystoreType(trustStore);
                // may take instance type from file extension
                KeyStore tks = KeyStore.getInstance(keystoreType);
                FileInputStream stream = new FileInputStream(trustStore); 
                tks.load(stream, null == storePass ? null : storePass.toCharArray());
                stream.close();
                tmf = TrustManagerFactory.getInstance("SunX509");
                tmf.init(tks);
            } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException e) {
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
        SSLContext ctx = null;
        TrustManagerFactory tmf = createTrustManagerFactory(trustStore, storePass);
        if (null != tmf) {
            try {
                ctx = SSLContext.getInstance("TLS");
                ctx.init(null, tmf.getTrustManagers(), null);
            } catch (NoSuchAlgorithmException | KeyManagementException e) {
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
            result.put("com.ibm.ssl.trustStore", trustStore.getAbsoluteFile());
            if (null != storePass) {
                // plaintext or com.ibm.micro.security.Password.obfuscate(char[] password).
                result.put("com.ibm.ssl.trustStorePassword", storePass); 
            }
            result.put("com.ibm.ssl.trustStoreTypeType", getKeystoreType(trustStore));
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
