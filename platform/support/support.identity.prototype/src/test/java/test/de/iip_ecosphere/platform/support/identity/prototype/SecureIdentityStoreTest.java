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
     * Tests entering and deleting symmetric entries into the secure identity store.
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
     * Tests entering and deleting private entries into the secure identity store.
     * @throws Exception 
     * 
     */
    @Test
    public void testEnteringRetrievingDeletingPrivateEntries() throws Exception {

        
        //Create generator with RSA algorithm and 2048 key size
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        
        // Get private key and create an alias
        String aliasPrivate = "testEntryPrivate";
        //PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();
        
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


        //Build the certificates and put them in the chain
        X509Certificate certificate = builder.build();
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
