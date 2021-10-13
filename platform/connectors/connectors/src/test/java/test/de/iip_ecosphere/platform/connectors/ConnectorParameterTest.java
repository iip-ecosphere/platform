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

package test.de.iip_ecosphere.platform.connectors;

import java.io.File;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.connectors.ConnectorParameter;
import de.iip_ecosphere.platform.connectors.ConnectorParameter.ConnectorParameterBuilder;
import de.iip_ecosphere.platform.connectors.IdentityToken;
import de.iip_ecosphere.platform.connectors.IdentityToken.IdentityTokenBuilder;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.ServerAddress;

/**
 * Tests {@link ConnectorParameter} and the related builder. Data is irrelevant/fake as we test the implementation
 * holding that data.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ConnectorParameterTest {

    /**
     * Tests creating connector parameters with default settings.
     */
    @Test
    public void testDefaultConnectorParameter() {
        ConnectorParameter params = ConnectorParameterBuilder
            .newBuilder("aaa", 1234)
            .build();
        Assert.assertEquals("aaa", params.getHost());
        Assert.assertEquals(1234, params.getPort());
        Assert.assertEquals("", params.getApplicationId());
        Assert.assertEquals("", params.getApplicationDescription());
        Assert.assertEquals("", params.getEndpointPath());
        Assert.assertEquals(ConnectorParameter.DEFAULT_SCHEMA, params.getSchema());
        Assert.assertEquals(ConnectorParameter.DEFAULT_KEEP_ALIVE, params.getKeepAlive());
        Assert.assertEquals(ConnectorParameter.DEFAULT_NOTIFICATION_INTERVAL, params.getNotificationInterval());
        Assert.assertNull(params.getClientCertificate());
        Assert.assertNull(params.getClientKeyPair());
        Assert.assertNull(params.getIdentityToken(ConnectorParameter.ANY_ENDPOINT));
    }
    
    /**
     * Tests the TLS setup.
     */
    @Test
    public void testTransportTlsParameter() {
        ServerAddress addr = new ServerAddress(Schema.IGNORE, "local", 1234);
        File keystore = new File("./keystore.jks");
        String passwd = "abc";
        String alias = "alias";
        ConnectorParameter params = ConnectorParameterBuilder
            .newBuilder(addr)
            .setKeystore(null, null)
            .build();
        Assert.assertEquals(addr.getHost(), params.getHost());
        Assert.assertEquals(addr.getPort(), params.getPort());
        Assert.assertEquals(null, params.getKeystore());
        Assert.assertEquals(null, params.getKeystorePassword());
        Assert.assertEquals(null, params.getKeyAlias());
        Assert.assertFalse(params.getHostnameVerification());
        
        params = ConnectorParameterBuilder
            .newBuilder(addr)
            .setKeystore(keystore, null)
            .setHostnameVerification(false)
            .build();
        Assert.assertEquals(addr.getHost(), params.getHost());
        Assert.assertEquals(addr.getPort(), params.getPort());
        Assert.assertEquals(keystore, params.getKeystore());
        Assert.assertEquals(null, params.getKeystorePassword());
        Assert.assertEquals(null, params.getKeyAlias());
        Assert.assertFalse(params.getHostnameVerification());

        params = ConnectorParameterBuilder
            .newBuilder(addr)
            .setKeystore(keystore, passwd)
            .setKeyAlias(alias)
            .setHostnameVerification(true)
            .build();
        Assert.assertEquals(addr.getHost(), params.getHost());
        Assert.assertEquals(addr.getPort(), params.getPort());
        Assert.assertEquals(keystore, params.getKeystore());
        Assert.assertEquals(passwd, params.getKeystorePassword());
        Assert.assertEquals(alias, params.getKeyAlias());
        Assert.assertTrue(params.getHostnameVerification());
    }
    
    /**
     * Implements a fake certificate instance.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class FakeCertificate extends X509Certificate {
        
        @Override
        public boolean hasUnsupportedCriticalExtension() {
            return false;
        }
        
        @Override
        public Set<String> getNonCriticalExtensionOIDs() {
            return null;
        }
        
        @Override
        public byte[] getExtensionValue(String oid) {
            return null;
        }
        
        @Override
        public Set<String> getCriticalExtensionOIDs() {
            return null;
        }
        
        @Override
        public void verify(PublicKey key, String sigProvider) throws CertificateException, NoSuchAlgorithmException,
            InvalidKeyException, NoSuchProviderException, SignatureException {
        }
        
        @Override
        public void verify(PublicKey key) throws CertificateException, NoSuchAlgorithmException, 
            InvalidKeyException, NoSuchProviderException, SignatureException {
        }
        
        @Override
        public String toString() {
            return null;
        }
        
        @Override
        public PublicKey getPublicKey() {
            return null;
        }
        
        @Override
        public byte[] getEncoded() throws CertificateEncodingException {
            return null;
        }
        
        @Override
        public int getVersion() {
            return 0;
        }
        
        @Override
        public byte[] getTBSCertificate() throws CertificateEncodingException {
            return null;
        }
        
        @Override
        public boolean[] getSubjectUniqueID() {
            return null;
        }
        
        @Override
        public Principal getSubjectDN() {
            return null;
        }
        
        @Override
        public byte[] getSignature() {
            return null;
        }
        
        @Override
        public byte[] getSigAlgParams() {
            return null;
        }
        
        @Override
        public String getSigAlgOID() {
            return null;
        }
        
        @Override
        public String getSigAlgName() {
            return null;
        }
        
        @Override
        public BigInteger getSerialNumber() {
            return null;
        }
        
        @Override
        public Date getNotBefore() {
            return null;
        }
        
        @Override
        public Date getNotAfter() {
            return null;
        }
        
        @Override
        public boolean[] getKeyUsage() {
            return null;
        }
        
        @Override
        public boolean[] getIssuerUniqueID() {
            return null;
        }
        
        @Override
        public Principal getIssuerDN() {
            return null;
        }
        
        @Override
        public int getBasicConstraints() {
            return 0;
        }
        
        @Override
        public void checkValidity(Date date) throws CertificateExpiredException, CertificateNotYetValidException {
        }
        
        @Override
        public void checkValidity() throws CertificateExpiredException, CertificateNotYetValidException {
        }
    }

    /**
     * Tests creating connector parameters with custom settings.
     */
    @Test
    public void testCustomConnectorParameter() {
        byte[] bytes = "aaa".getBytes();
        Map<String, IdentityToken> tokens = new HashMap<String, IdentityToken>();
        tokens.put("a", IdentityTokenBuilder.newBuilder("a", "b", bytes).build());
        tokens.put("i", IdentityTokenBuilder.newBuilder("i", "j", bytes).setIssuedToken(bytes, "abc").build());
        tokens.put("u", IdentityTokenBuilder.newBuilder("u", "k", bytes).setUsernameToken("me", bytes, "abc").build());
        tokens.put("x", IdentityTokenBuilder.newBuilder("u", "k", bytes).setX509Token(bytes).build());

        X509Certificate cert = new FakeCertificate();
        KeyPair pair = new KeyPair(null, null);
        
        ConnectorParameter params = ConnectorParameterBuilder
            .newBuilder("aaa", 1234, Schema.TCP)
            .setApplicationInformation("aI", "aD")
            .setAutoApplicationId(false)
            .setEndpointPath("epp/")
            .setKeepAlive(2345)
            .setNotificationInterval(9999)
            .setRequestTimeout(3421)
            .setIdentities(tokens)
            .setSecurityInformation(cert, pair)
            .build();

        Assert.assertEquals("aaa", params.getHost());
        Assert.assertEquals(1234, params.getPort());
        Assert.assertEquals(Schema.TCP, params.getSchema());
        Assert.assertEquals("aI", params.getApplicationId());
        Assert.assertEquals("aD", params.getApplicationDescription());
        Assert.assertFalse(params.getAutoApplicationId());
        Assert.assertEquals("epp/", params.getEndpointPath());
        Assert.assertEquals(2345, params.getKeepAlive());
        Assert.assertEquals(9999, params.getNotificationInterval());
        Assert.assertEquals(3421, params.getRequestTimeout());
        Assert.assertTrue(tokens.get("a") == params.getIdentityToken("a"));
        Assert.assertTrue(tokens.get("i") == params.getIdentityToken("i"));
        Assert.assertTrue(tokens.get("u") == params.getIdentityToken("u"));
        Assert.assertTrue(tokens.get("x") == params.getIdentityToken("x"));
        Assert.assertTrue(cert == params.getClientCertificate());
        Assert.assertTrue(pair == params.getClientKeyPair());
        
        ServerAddress addr = new ServerAddress(Schema.TCP, "aaa", 1234);
        params = ConnectorParameterBuilder
            .newBuilder(addr)
            .build();
        Assert.assertEquals(addr.getHost(), params.getHost());
        Assert.assertEquals(addr.getPort(), params.getPort());
        Assert.assertEquals(addr.getSchema(), params.getSchema());
    }

}
