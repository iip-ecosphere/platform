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

package de.iip_ecosphere.platform.support.aas.basyx2.apps.security;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.AbstractHttp11Protocol;
import org.apache.tomcat.util.net.SSLContext;
import org.apache.tomcat.util.net.SSLHostConfig;
import org.apache.tomcat.util.net.SSLHostConfigCertificate;
import org.apache.tomcat.util.net.SSLHostConfigCertificate.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.context.annotation.Configuration;

import de.iip_ecosphere.platform.support.net.KeyStoreDescriptor;
import de.iip_ecosphere.platform.support.net.SslUtils;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;

/**
 * Customizes the SSL setup.
 * 
 * @author Holger Eichelberger, SSE
 */
@Configuration
public class SSLConnectorCustomizer implements TomcatConnectorCustomizer {

    @Autowired(required = false)
    private KeyStoreDescriptor kstore;
    private final int port;

    /**
     * Creates an instance.
     * 
     * @param port the server port (injected)
     */
    public SSLConnectorCustomizer(@Value("${server.port}") int port) {
        this.port = port;
    }

    @Override
    public void customize(Connector connector) {
        if (null != kstore) {
            try {
                KeyStore ks = SslUtils.openKeyStore(kstore.getPath(), kstore.getPassword());
                TrustManagerFactory tmf = SslUtils.createTrustManagerFactory(ks);
                KeyManager[] kms = SslUtils.createKeyManagers(ks, kstore.getPassword(), kstore.getAlias());
                Runtime.Version ver = Runtime.version();
                String protocolName = ver.feature() >= 17 ? "TLSv1.3" : "TLSv1";
                SSLContext sslContext = new JSSESSLContext(protocolName);
                sslContext.init(kms, tmf.getTrustManagers(), new SecureRandom());

                connector.setScheme("https");
                connector.setSecure(true);
                connector.setPort(port);
    
                AbstractHttp11Protocol<?> protocol = (AbstractHttp11Protocol<?>) connector.getProtocolHandler();
                protocol.setSSLEnabled(true);
    
                SSLHostConfig sslHostConfig = new SSLHostConfig();
                SSLHostConfigCertificate certificate = new SSLHostConfigCertificate(sslHostConfig, Type.UNDEFINED);
                certificate.setSslContext(sslContext);
                sslHostConfig.addCertificate(certificate);
                protocol.addSslHostConfig(sslHostConfig);
            } catch (IOException | KeyManagementException e) {
                LoggerFactory.getLogger(getClass()).error("Cannot setup SSL on port {}, staying with http: {}", 
                    port, e.getMessage());
            }
        }
    }
    
    /**
     * Delegating JSSE Context, taken over from package-local non-reusable class 
     * {@code org.apache.tomcat.util.net.jsse.JSSESSLContext}.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class JSSESSLContext implements SSLContext {
        
        private javax.net.ssl.SSLContext context;
        private KeyManager[] kms;
        private TrustManager[] tms;
        
        /**
         * Creates a context.
         * 
         * @param protocol the desired SSL protocol
         * @throws IOException if the protocol does not exist
         */
        private JSSESSLContext(String protocol) throws IOException {
            try {
                context = javax.net.ssl.SSLContext.getInstance(protocol);
            } catch (NoSuchAlgorithmException e) {
                throw new IOException(e);
            }
        }
        
        @Override
        public void init(KeyManager[] kms, TrustManager[] tms, SecureRandom sr) throws KeyManagementException {
            this.kms = kms;
            this.tms = tms;
            context.init(kms, tms, sr);
        }

        @Override
        public void destroy() {
        }

        @Override
        public SSLSessionContext getServerSessionContext() {
            return context.getServerSessionContext();
        }

        @Override
        public SSLEngine createSSLEngine() {
            return context.createSSLEngine();
        }

        @Override
        public SSLServerSocketFactory getServerSocketFactory() {
            return context.getServerSocketFactory();
        }

        @Override
        public SSLParameters getSupportedSSLParameters() {
            return context.getSupportedSSLParameters();
        }

        @Override
        public X509Certificate[] getCertificateChain(String alias) {
            X509Certificate[] result = null;
            if (kms != null) {
                for (int i = 0; i < kms.length && result == null; i++) {
                    if (kms[i] instanceof X509KeyManager) {
                        result = ((X509KeyManager) kms[i]).getCertificateChain(alias);
                    }
                }
            }
            return result;
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            Set<X509Certificate> certs = new HashSet<>();
            if (tms != null) {
                for (TrustManager tm : tms) {
                    if (tm instanceof X509TrustManager) {
                        X509Certificate[] accepted = ((X509TrustManager) tm).getAcceptedIssuers();
                        certs.addAll(Arrays.asList(accepted));
                    }
                }
            }
            return certs.toArray(new X509Certificate[0]);
        }
        
    }
    
}
