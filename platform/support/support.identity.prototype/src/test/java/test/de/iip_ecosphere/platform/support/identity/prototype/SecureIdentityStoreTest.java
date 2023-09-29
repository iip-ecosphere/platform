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

import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.UUID;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.eclipse.milo.opcua.stack.core.util.SelfSignedCertificateBuilder;
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
    KeyStore.ProtectionParameter protParam = new KeyStore.PasswordProtection(password);
    
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
     * Tests entering, retrieving, and deleting symmetric entries into the secure identity store.
     * 
     */
    @Test
    public void testEnteringRetrievingDeletingSymmetricEntries() {
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
     * Tests entering, retrieving, and deleting private entries into the secure identity store.
     * @throws Exception 
     * 
     */
    @Test
    public void testEnteringRetrievingDeletingPrivateEntries() throws Exception {        
        //Create key pair with RSA algorithm and 2048 key size
        KeyPair keyPair = getKeyPairFromKeyGenerator("RSA", 2048);
        
        // Get private key and create an alias
        String aliasPrivate = "testEntryPrivate";
        PrivateKey privateKey = keyPair.getPrivate();
        
        //Get a new certificate and store it into the chain
        X509Certificate certificate = getCertificateFromBuilder(keyPair);
        X509Certificate[] certificateChain =  new X509Certificate[]{certificate};
        
        
        // Inserts a new private key entry
        assertEquals("Successfull insertion should return true", true,
                store.setPrivateKeyEntry(aliasPrivate, privateKey, password, certificateChain));


        // Retrieves the private key entry (the entry itself contains more than just the private key)
        assertEquals("Retrieved entry should match the inserted secret", privateKey.toString(),
                ((PrivateKeyEntry) store.getEntry(aliasPrivate, protParam)).getPrivateKey().toString());

        //Delete
        assertEquals("Successfull deletion should return true", true, store.deleteEntry(aliasPrivate));

        // Retrieve the (no longer existing) entries
        assertEquals("Unsuccessfull retrieval should return null", null, store.getEntry(aliasPrivate, protParam));

    }
    
    
    /**
     * Tests entering, retrieving, and deleting certificate entries into the secure identity store.
     * @throws Exception 
     * 
     */
    @Test
    public void testEnteringRetrievingDeletingCertificateEntries() throws Exception {
        //Create key pair with RSA algorithm and 2048 key size
        KeyPair keyPair = getKeyPairFromKeyGenerator("RSA", 2048);        
        
        //Creates a new certificate and its alias
        X509Certificate certificate = getCertificateFromBuilder(keyPair);
        String aliasCert = "testEntryCert";
  
        
        // Inserts a new certificate entry
        assertEquals("Successfull insertion should return true", true,
                store.setCertificateEntry(aliasCert, certificate));
        
        // Retrieves the certificate entry
        assertEquals("Retrieved entry should match the inserted secret", certificate.toString(),
                store.getCertificate(aliasCert).toString());
        
        // Deletes the entry
        assertEquals("Successfull deletion should return true", true, store.deleteEntry(aliasCert));
        
        // Retrieve the (no longer existing) entry
        assertEquals("Unsuccessfull retrieval should return null", null,  store.getCertificate(aliasCert));
    }
    
    /**
     * Helper method for generating a self-signed certificate.
     * @param keyPair A pair of a public and private key required to build the certificate
     * @return A newly generated X509Certificate 
     * @throws Exception 
     */
    private static X509Certificate getCertificateFromBuilder(KeyPair keyPair) throws Exception {               
        //Copied from /connectors.opcuav1/src/test/java/test/de/iip_ecosphere/platform/connectors/opcuav1/ServerKeystoreLoader.java
        String applicationUri = "urn:eclipse:milo:examples:server:" + UUID.randomUUID();       
        SelfSignedCertificateBuilder builder = new SelfSignedCertificateBuilder(keyPair)
            .setCommonName("Eclipse Milo Example Server")
            .setOrganization("digitalpetri")
            .setOrganizationalUnit("dev")
            .setLocalityName("Folsom")
            .setStateName("CA")
            .setCountryCode("US")
            .setApplicationUri(applicationUri);

        //Build the X509Certificate certificate with the builder and return it               
        return builder.build();     
    }
 
    /**
     * Helper method for generating a key pair (public + private).
     * 
     * @param cipher
     * @param keySize
     * @return Either the generated SecretKey or null.
     */
    private static KeyPair getKeyPairFromKeyGenerator(String cipher, int keySize) {
        KeyPairGenerator keyGen;
        try {
            //Create generator with cipher algorithm and keySize
            keyGen = KeyPairGenerator.getInstance(cipher);
            keyGen.initialize(keySize);
            
            return keyGen.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Problem with key pair generator!");
            e.printStackTrace();
            return null;
        }
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


}
