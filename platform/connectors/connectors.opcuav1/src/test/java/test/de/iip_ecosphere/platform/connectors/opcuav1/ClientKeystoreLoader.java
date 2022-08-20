/**
 * ******************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.connectors.opcuav1;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import org.eclipse.milo.opcua.sdk.server.util.HostnameUtil;
import org.eclipse.milo.opcua.stack.core.util.SelfSignedCertificateBuilder;
import org.eclipse.milo.opcua.stack.core.util.SelfSignedCertificateGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads a keystore from the client perspective.
 * 
 * @author Taken from the OPC UA examples
 */
class ClientKeystoreLoader {

    private static final Pattern IP_ADDR_PATTERN = Pattern.compile(
        "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    private static final String CLIENT_ALIAS = "client-ai";
    private static final char[] PASSWORD = "password".toCharArray();

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private X509Certificate clientCertificate;
    private KeyPair clientKeyPair;

    // checkstyle: stop exception type check

    /**
     * Loads the client keystore.
     * 
     * @param baseDir the base directory
     * @return <b>this</b>
     * @throws ExecutionException if anything goes wrong
     */
    ClientKeystoreLoader load(File baseDir) throws ExecutionException {
        try {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            Path serverKeyStore = baseDir.toPath().resolve("example-client.pfx");
    
            logger.info("Loading KeyStore at {}", serverKeyStore);
    
            if (!Files.exists(serverKeyStore)) {
                keyStore.load(null, PASSWORD);
    
                KeyPair keyPair = SelfSignedCertificateGenerator.generateRsaKeyPair(2048);
    
                SelfSignedCertificateBuilder builder = new SelfSignedCertificateBuilder(keyPair)
                    .setCommonName("Eclipse Milo Example Client")
                    .setOrganization("digitalpetri")
                    .setOrganizationalUnit("dev")
                    .setLocalityName("Folsom")
                    .setStateName("CA")
                    .setCountryCode("US")
                    .setApplicationUri("urn:eclipse:milo:examples:client")
                    .addDnsName("localhost")
                    .addIpAddress("127.0.0.1");
    
                // Get as many hostnames and IP addresses as we can listed in the certificate.
                for (String hostname : HostnameUtil.getHostnames("0.0.0.0")) {
                    if (IP_ADDR_PATTERN.matcher(hostname).matches()) {
                        builder.addIpAddress(hostname);
                    } else {
                        builder.addDnsName(hostname);
                    }
                }
    
                X509Certificate certificate = builder.build();
    
                keyStore.setKeyEntry(CLIENT_ALIAS, keyPair.getPrivate(), PASSWORD, new X509Certificate[]{certificate});
                try (OutputStream out = Files.newOutputStream(serverKeyStore)) {
                    keyStore.store(out, PASSWORD);
                }
            } else {
                try (InputStream in = Files.newInputStream(serverKeyStore)) {
                    keyStore.load(in, PASSWORD);
                }
            }
    
            Key serverPrivateKey = keyStore.getKey(CLIENT_ALIAS, PASSWORD);
            if (serverPrivateKey instanceof PrivateKey) {
                clientCertificate = (X509Certificate) keyStore.getCertificate(CLIENT_ALIAS);
                PublicKey serverPublicKey = clientCertificate.getPublicKey();
                clientKeyPair = new KeyPair(serverPublicKey, (PrivateKey) serverPrivateKey);
            }
    
            return this;
        } catch (Exception e) {
            throw new ExecutionException(e); 
        }
    }

    // checkstyle: resume exception type check

    /**
     * Returns the client certificate.
     *
     * @return the client certificate
     */
    X509Certificate getClientCertificate() {
        return clientCertificate;
    }

    /**
     * Returns the client encryption key pair.
     * 
     * @return the client key pair
     */
    KeyPair getClientKeyPair() {
        return clientKeyPair;
    }

}
