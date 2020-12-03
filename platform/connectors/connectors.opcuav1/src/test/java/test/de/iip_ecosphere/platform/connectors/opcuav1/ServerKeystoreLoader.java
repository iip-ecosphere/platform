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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import com.google.common.collect.Sets;
import org.eclipse.milo.opcua.sdk.server.util.HostnameUtil;
import org.eclipse.milo.opcua.stack.core.util.SelfSignedCertificateBuilder;
import org.eclipse.milo.opcua.stack.core.util.SelfSignedCertificateGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A server keystore loader/generator.
 * 
 * @author Taken from the OPC UA examples
 */
class ServerKeystoreLoader {

    private static final Pattern IP_ADDR_PATTERN = Pattern.compile(
        "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    private static final String SERVER_ALIAS = "server-ai";
    private static final char[] PASSWORD = "password".toCharArray();

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private X509Certificate[] serverCertificateChain;
    private X509Certificate serverCertificate;
    private KeyPair serverKeyPair;

    // checkstyle: stop exception type check
    
    /**
     * Loads the server keystore from the given base directory.
     * 
     * @param baseDir the base directory
     * @return <b>this</b>
     * @throws ExecutionException if anything goes wrong
     */
    ServerKeystoreLoader load(File baseDir) throws ExecutionException {
        try {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
    
            File serverKeyStore = baseDir.toPath().resolve("example-server.pfx").toFile();
    
            logger.info("Loading KeyStore at {}", serverKeyStore);
    
            if (!serverKeyStore.exists()) {
                keyStore.load(null, PASSWORD);
    
                KeyPair keyPair = SelfSignedCertificateGenerator.generateRsaKeyPair(2048);
    
                String applicationUri = "urn:eclipse:milo:examples:server:" + UUID.randomUUID();
    
                SelfSignedCertificateBuilder builder = new SelfSignedCertificateBuilder(keyPair)
                    .setCommonName("Eclipse Milo Example Server")
                    .setOrganization("digitalpetri")
                    .setOrganizationalUnit("dev")
                    .setLocalityName("Folsom")
                    .setStateName("CA")
                    .setCountryCode("US")
                    .setApplicationUri(applicationUri);
    
                // Get as many hostnames and IP addresses as we can listed in the certificate.
                Set<String> hostnames = Sets.union(
                    Sets.newHashSet(HostnameUtil.getHostname()),
                    HostnameUtil.getHostnames("0.0.0.0", false)
                );
    
                for (String hostname : hostnames) {
                    if (IP_ADDR_PATTERN.matcher(hostname).matches()) {
                        builder.addIpAddress(hostname);
                    } else {
                        builder.addDnsName(hostname);
                    }
                }
    
                X509Certificate certificate = builder.build();
    
                keyStore.setKeyEntry(SERVER_ALIAS, keyPair.getPrivate(), PASSWORD, new X509Certificate[]{certificate});
                keyStore.store(new FileOutputStream(serverKeyStore), PASSWORD);
            } else {
                keyStore.load(new FileInputStream(serverKeyStore), PASSWORD);
            }
    
            Key serverPrivateKey = keyStore.getKey(SERVER_ALIAS, PASSWORD);
            if (serverPrivateKey instanceof PrivateKey) {
                serverCertificate = (X509Certificate) keyStore.getCertificate(SERVER_ALIAS);
    
                serverCertificateChain = Arrays.stream(keyStore.getCertificateChain(SERVER_ALIAS))
                    .map(X509Certificate.class::cast)
                    .toArray(X509Certificate[]::new);
    
                PublicKey serverPublicKey = serverCertificate.getPublicKey();
                serverKeyPair = new KeyPair(serverPublicKey, (PrivateKey) serverPrivateKey);
            }
    
            return this;
        } catch (Exception e) {
            throw new ExecutionException(e);
        }
    }
    
    // checkstyle: resume exception type check

    /**
     * Returns the server certificate.
     * 
     * @return the server certificate
     */
    X509Certificate getServerCertificate() {
        return serverCertificate;
    }

    /**
     * Returns the server certificate chain.
     * 
     * @return the server certificate chain
     */
    public X509Certificate[] getServerCertificateChain() {
        return serverCertificateChain;
    }

    /**
     * Returns the server key pair.
     * 
     * @return the server key pair
     */
    KeyPair getServerKeyPair() {
        return serverKeyPair;
    }

}
