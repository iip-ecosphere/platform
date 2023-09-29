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

package de.iip_ecosphere.platform.support.identity.prototype;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Enumeration;

import javax.net.ssl.KeyManager;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.identities.IdentityStore;
import de.iip_ecosphere.platform.support.identities.IdentityStoreDescriptor;
import de.iip_ecosphere.platform.support.identities.IdentityToken;

/**
 * Extention of the security store with encryption. Tries to load
 * {@code identityStore.yml} from the classpath (root or folder
 * {@code resources}) and as development fallbacks from
 * {@code src/main/resources} or {@code src/test/resources}.
 * 
 * @author Lea Gerling, SSE
 */
public class SecureIdentityStore extends IdentityStore {

    private KeyStore keyStore;
    private String keyStoreName;
    private String keyStoreType;
    private char[] keyStorePassword;

    /**
     * The JSL descriptor for this store.
     * 
     * @author Lea Gerling, SSE
     */
    public static class SecureIdentityStoreDescriptor implements IdentityStoreDescriptor {

        @Override
        public IdentityStore createStore() {
            return new SecureIdentityStore();
        }
    }
    
    /**
     * Creates a secure identity store. Usually, shall be created via JSL
     * ({@link IdentityStoreDescriptor}). [testing]
     */
    public SecureIdentityStore() {
        this.keyStoreName = "secureKeystorePrototype";
        this.keyStoreType = KeyStore.getDefaultType();
        this.keyStorePassword = "pwd".toCharArray();
    }


    @Override
    public IdentityToken getToken(String identity, boolean defltAnonymous, String... fallback) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public InputStream getKeystoreAsStream(String identity, String... fallback) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public KeyStore getKeystoreFile(String identity, String... fallback) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public KeyManager[] getKeyManagers(String identity, String algorithm, String... fallback) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Key getKeystoreKey(String identity, KeyStore keystore, String keyAlias, String... fallback)
            throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Creates a new and empty secure identity store.
     * 
     * @return boolean for testing.
     */
    public boolean createEmptyKeyStore() {
        try {
            keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, keyStorePassword);
            FileOutputStream fos = new FileOutputStream(keyStoreName);
            keyStore.store(fos, keyStorePassword);
            fos.close();
            return true;
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
            LoggerFactory.getLogger(getClass()).warn("Error when creating keystore, see stacktrace");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Loads an existing secure identity store.
     * 
     * @return boolean for testing.
     */
    public boolean loadKeyStore() {
        FileInputStream fis;
        try {
            fis = new FileInputStream(keyStoreName);
            keyStore.load(fis, keyStorePassword);
            fis.close();
            return true;
        } catch (IOException | NoSuchAlgorithmException | CertificateException e) {
            LoggerFactory.getLogger(getClass()).warn("Error when loading keystore, see stacktrace");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Saves a new entry (a symmetric key) in the security store.
     * 
     * @param alias               The name of the new entry.
     * @param secretKeyEntry      The content of the new entry (the secret itself).
     * @param protectionParameter A password used for protection.
     * @return boolean for testing.
     */
    public boolean setSymmetricKeyEntry(String alias, KeyStore.SecretKeyEntry secretKeyEntry,
            KeyStore.ProtectionParameter protectionParameter) {
        try {
            keyStore.setEntry(alias, secretKeyEntry, protectionParameter);
            return true;
        } catch (KeyStoreException e) {
            LoggerFactory.getLogger(getClass()).warn("Error when writing entry into keystore, see stacktrace");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Returns an existing entry in the security store by its alias.
     * 
     * @param alias          The name of the new entry.
     * @param protParam      A password used for protection.
     * @return Keystore.Entry Returns either the requested entry or null, if not
     *         successful.
     */
    public KeyStore.Entry getEntry(String alias, KeyStore.ProtectionParameter protParam) {

        try {
            return keyStore.getEntry(alias, protParam);
        } catch (NoSuchAlgorithmException | UnrecoverableEntryException | KeyStoreException e) {
            LoggerFactory.getLogger(getClass()).warn("Error when retrieving entry from keystore, see stacktrace");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Saves a new entry (a private key) in the security store.
     * 
     * @param alias            The name of the new entry.
     * @param privateKey       The content of the new entry (the secret itself).
     * @param keyPassword      A password used for protection.
     * @param certificateChain A certificate chain to certify the corresponding
     *                         public key
     * @return boolean for testing.
     */
    public boolean setPrivateKeyEntry(String alias, PrivateKey privateKey, char[] keyPassword,
            Certificate[] certificateChain) {
        try {
            keyStore.setKeyEntry(alias, privateKey, keyPassword, certificateChain);
            return true;
        } catch (KeyStoreException e) {
            LoggerFactory.getLogger(getClass()).warn("Error when writing entry into keystore, see stacktrace");
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Saves a new entry (a certificate) in the security store.
     * 
     * @param alias            The name of the new entry.
     * @param certificate      The content of the new entry (the certificate itself).
     * @return boolean for testing.
     */
    public boolean setCertificateEntry(String alias, Certificate certificate) throws KeyStoreException {     
        try {
            keyStore.setCertificateEntry(alias, certificate);
            return true;
        } catch (KeyStoreException e) {
            LoggerFactory.getLogger(getClass()).warn("Error when writing entry into keystore, see stacktrace");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Returns an existing certificate entry in the security store by its alias.
     * 
     * @param alias               The name of the new entry.
     * @return Returns either the requested certificate entry or null, if not
     *         successful.
     */
    public Certificate getCertificate(String alias) throws KeyStoreException {
        return keyStore.getCertificate(alias);
    }

    /**
     * Deletes an existing entry in the security store by its alias.
     * 
     * @param alias The name of the new entry.
     * @return boolean for testing.
     */
    public boolean deleteEntry(String alias) {
        try {
            keyStore.deleteEntry(alias);
            return true;
        } catch (KeyStoreException e) {
            LoggerFactory.getLogger(getClass()).warn("Error when deleting entry from keystore, see stacktrace");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Deletes an existing secure identity store.
     * 
     * @return boolean for testing.
     */
    public boolean deleteKeyStore() {
        Enumeration<String> aliases;

        try {
            aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                keyStore.deleteEntry(alias);
            }
            Path keyStoreFile = Paths.get(keyStoreName);
            Files.delete(keyStoreFile);
            return true;
        } catch (KeyStoreException | IOException e) {
            LoggerFactory.getLogger(getClass()).warn("Error when deleting keystore, see stacktrace");
            e.printStackTrace();
            return false;
        }
    }

}
