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

package test.de.iip_ecosphere.platform.support.identity.prototype;

import static org.junit.Assert.assertEquals;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.util.io.pem.PemReader;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import de.iip_ecosphere.platform.support.identities.IdentityStore;
import de.iip_ecosphere.platform.support.identity.prototype.SecureIdentityStore;



/**
 * Tests the secure identity store.
 * 
 * @author Lea Gerling, SSE
 */
public class SecureIdentityStoreTest {
    
    static IdentityStore IStore;
    static SecureIdentityStore store;
    static char[] password = "savePassword".toCharArray();
    
    /**
     * Creates and loads a new SecureIdentityStore for testing.
     */
    @BeforeClass
    public static void setup() {
        //Create a new instance of the SecureIdentityStore
        IStore = IdentityStore.getInstance();
        Assert.assertTrue(IStore instanceof SecureIdentityStore);
        store = (SecureIdentityStore) IStore;
        // Creates a new empty store
        assertEquals("Successfull creation should return true", true, store.createEmptyKeyStore());
        // Loads the existing keystore
        assertEquals("Successfull loading should return true", true, store.loadKeyStore());
    }
    
    /**
     * Deletes the keystore after testing.
     */
    @AfterClass
    public static void tearDown() {
        // Deletes the existing keystore
        assertEquals("Successfull deletion should return true", true, store.deleteKeyStore());
    }    
    
    /**
     * Tests entering and deleting symmetric entries into the secure identity store.
     * 
     */
    @Test
    public void testEnteringDeletingSymmetricEntries() {
        KeyStore.ProtectionParameter protParam = new KeyStore.PasswordProtection(password);

        // Create a new symmetric secret and alias
        KeyStore.SecretKeyEntry symmetricSecret = new KeyStore.SecretKeyEntry(
                (SecretKey) getKeyFromKeyGenerator("AES", 128));
        String aliasSymmetric = "testEntrySymmetric";      
        
        // Inserts a new symmetric key entry
        assertEquals("Successfull insertion should return true", true,
                store.setSymmetricKeyEntry(aliasSymmetric, symmetricSecret, protParam));
        
        // Retrieves the symmetric key entry
        assertEquals("Retrieved entry should match the inserted secret", symmetricSecret.toString(),
                store.getEntry(aliasSymmetric, protParam).toString());
        
        // Deletes the entry
        assertEquals("Successfull deletion should return true", true, store.deleteEntry(aliasSymmetric));
        
        // Retrieve the (no longer existing) entry
        assertEquals("Unsuccessfull retrieval should return null", null, store.getEntry(aliasSymmetric, protParam));
    }
    
    
    

    /**
     * Tests entering and deleting private entries into the secure identity store.
     * 
     * @throws CertificateException
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    @Test
    public void testEnteringDeletingPrivateEntries()
            throws CertificateException, IOException, NoSuchAlgorithmException, InvalidKeySpecException {


        KeyStore.ProtectionParameter protParam = new KeyStore.PasswordProtection(password);



        // For private keys
        String aliasPrivate = "testEntryPrivate";
        KeyPair keyPair = getKeyPairFromKeyGenerator("DSA", 2048);
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        KeyFactory factory = KeyFactory.getInstance("RSA");
        RSAPrivateKey privateKeyFromFile;
        
        
        //This works, but certificate chain is still not valid
        try (FileReader keyReader = new FileReader("src/test/resources/keypair.pem");
                PemReader pemReader = new PemReader(keyReader)) {

//            PemObject pemObject = pemReader.readPemObject();
//            byte[] content = pemObject.getContent();
            RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(new BigInteger(
                    "AD6E684550542947AD95EF9BACDC0AC2C9168C6EB3212D378C23E5539266111DB2E5B4D42B1E47EB4F7A65DB63D9782C72BC365492FD1E5C7B4CD2174C611668C29013FEDE22619B3F58DA3531BB6C02B3266768B7895CBDAFB3F9AC7A7B2F3DB17EF4DCF03BD2575604BDE0A01BB1FB7B0E733AD63E464DB4D7D89626297A214D7CECCD0C50421A322A01E9DCEA23443F6A9339576B31DFA504A133076394562CB57F3FDEDB26F9A82BED2F6D52D6F6BF8286E2497EF0B5C8456F32B4668F5A9F5FCD3781345DDDB749792C37238A53D18FD976C0C9D1F1E211F1A4A9AAE679C45B92D1741EF0D3C3F373232CE7FB93E9BC461E1C508A20B74E7E3361B3C527",
                    16), new BigInteger("10002", 16));
            privateKeyFromFile = (RSAPrivateKey) factory.generatePrivate(keySpec);

        }
        
        
//        RSAPrivateKey privateKeyFromFile;
        //This does not work: java.lang.IllegalArgumentException: unknown object in getInstance: org.bouncycastle.openssl.PEMKeyPair
        try (FileReader keyReader = new FileReader("src/test/resources/keypair.pem")) {

            PEMParser pemParser = new PEMParser(keyReader);
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
            PrivateKeyInfo privateKeyInfo = PrivateKeyInfo.getInstance(pemParser.readObject());

            privateKeyFromFile = (RSAPrivateKey) converter.getPrivateKey(privateKeyInfo);
        }

        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        // Read from test file (this is a little complicated as other solutions always
        // resulted in empty input or decoding errors
        FileInputStream inStreamClient = new FileInputStream("src/test/resources/testCertificateRoot.CER");
        BufferedInputStream bis = new BufferedInputStream(inStreamClient);

        X509Certificate clientCert;
        X509Certificate[] certificateChain = new X509Certificate[2];
        // Generate certificates
        while (bis.available() > 0) {
            clientCert = (X509Certificate) cf.generateCertificate(bis);
            // Save in chain
            certificateChain[0] = clientCert;
            System.out.println(clientCert.toString());
        }

        inStreamClient = new FileInputStream("src/test/resources/testCertificate.CER");
        bis = new BufferedInputStream(inStreamClient);

        while (bis.available() > 0) {
            clientCert = (X509Certificate) cf.generateCertificate(bis);
            // Save in chain
            certificateChain[1] = clientCert;
            System.out.println(clientCert.toString());
        }


        // Inserts a new private key entry
        assertEquals("Successfull insertion should return true", true,
                store.setPrivateKeyEntry(aliasPrivate, (PrivateKey) privateKeyFromFile, password, certificateChain));


        // Retrieves the private key entry
        assertEquals("Retrieved entry should match the inserted secret", privateKey.toString(),
                store.getEntry(aliasPrivate, protParam).toString());

        //Delete
        assertEquals("Successfull deletion should return true", true, store.deleteEntry(aliasPrivate));

        // Retrieve the (no longer existing) entries
        assertEquals("Unsuccessfull retrieval should return null", null, store.getEntry(aliasPrivate, protParam));



//        
//        assertNull(store.getToken("xxx")); // no anonymous fallback
//        assertEquals(TokenType.ANONYMOUS, store.getToken("xxx", true).getType());
//        assertEquals(TokenType.ANONYMOUS, store.getToken("aas", false).getType());
//        assertEquals(TokenType.X509, store.getToken("secureStore", false).getType());
//        assertEquals(TokenType.USERNAME, store.getToken("axc3151-2", false).getType());
//        assertEquals(TokenType.ISSUED, store.getToken("UI", false).getType());
//        assertEquals(TokenType.USERNAME, store.getToken("myEdge", false, "plcEdges", "axc3151-2").getType());
//        
//        try {
//            KeyStore ks = store.getKeystoreFile("keystore");
//            assertNotNull(ks);
//        } catch (IOException e) {
//            fail("No exception shall occur here");
//        }
    }

    /**
     * Helper method for generating a key.
     * 
     * @param cipher
     * @param keySize
     * @return Either the generated SecretKey or null.
     */
    private static Key getKeyFromKeyGenerator(String cipher, int keySize) {
        KeyGenerator keyGenerator;
        try {
            keyGenerator = KeyGenerator.getInstance(cipher);
            keyGenerator.init(keySize);
            return keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Problem with key generator!");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Helper method for generating a key pair.
     * 
     * @param cipher
     * @param keySize
     * @return Either the generated key pair or null.
     */
    private static KeyPair getKeyPairFromKeyGenerator(String cipher, int keySize) {
        KeyPairGenerator keyPairGen;
        try {
            keyPairGen = KeyPairGenerator.getInstance(cipher);
            keyPairGen.initialize(keySize);
            return keyPairGen.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Problem with key pair generator!");
            e.printStackTrace();
            return null;
        }
    }

}
