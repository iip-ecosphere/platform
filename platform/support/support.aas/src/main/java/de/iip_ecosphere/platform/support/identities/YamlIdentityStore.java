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

package de.iip_ecosphere.platform.support.identities;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.identities.IdentityToken.TokenType;
import de.iip_ecosphere.platform.support.identities.YamlIdentityFile.IdentityInformation;
import de.iip_ecosphere.platform.support.net.SslUtils;
import de.iip_ecosphere.platform.support.net.UriResolver;
import de.iip_ecosphere.platform.support.resources.ResourceLoader;

/**
 * Simple file-based identity store. Tries to load {@code identityStore.yml} from the classpath (root or folder 
 * {@code resources}) and as development fallbacks from {@code src/main/resources} or {@code src/test/resources}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class YamlIdentityStore extends IdentityStore {
    
    private YamlIdentityFile data;
    
    /**
     * The JSL descriptor for this store.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class YamlIdentityStoreDescriptor implements IdentityStoreDescriptor {
        
        @Override
        public IdentityStore createStore() {
            return new YamlIdentityStore();
        }
    }
    
    /**
     * Creates a YAML identity store. Usually, shall be created via JSL ({@link IdentityStoreDescriptor}). [testing]
     */
    public YamlIdentityStore() {
        data = YamlIdentityFile.load(resolve("identityStore.yml")); // can cope with null
        LoggerFactory.getLogger(YamlIdentityFile.class).info("Loaded identityStore {}", data.getName());
    }
    
    /**
     * Resolves a resource, identity store file or key file listed in identity store.
     * 
     * @param resource the resource to resolve
     * @return the resolved resource or <b>null</b> if not found
     */
    private static InputStream resolve(String resource) {
        String source = "classpath";
        InputStream in = ResourceLoader.getResourceAsStream(resource);
        // this is old style, could delegate more into resolvers...
        if (null == in) {
            in = ResourceLoader.getResourceAsStream("resources/" + resource);
            source = "classpath: resources";
        }
        if (null == in) {
            String storeFolder = System.getProperty("iip.identityStore", ".");
            File f = new File(storeFolder, resource);
            // for local testing
            if (!f.exists()) {
                f = new File("src/test/resources/" + resource);
            }
            // for local development/deployment preparation
            if (!f.exists()) {
                f = new File("src/main/resources/" + resource);
            }
            if (f.exists()) {
                try {
                    in = new FileInputStream(f);
                    source = f.getAbsolutePath();
                } catch (IOException e) {
                    LoggerFactory.getLogger(YamlIdentityFile.class)
                        .info("Cannot load {}: {}", resource, e.getMessage());
                }
            } else {
                in = null;
            }
        }
        if (null != in) {
            LoggerFactory.getLogger(YamlIdentityFile.class).info("Loading {} from {}", resource, source);
        } else {
            LoggerFactory.getLogger(YamlIdentityFile.class).warn("{} not found!", resource);
        }
        return in;
    }
    
    /**
     * Resolves the requested {@link IdentityInformation}.
     * 
     * @param identity the identity (key) to return the information instance for
     * @param fallback fallback identities to use instead in given sequence, e.g., instead a specific device a 
     *     device group
     * @return the identity information or <b>null</b>
     */
    private IdentityInformation resolve(String identity, String... fallback) {
        IdentityInformation info = data.getData(identity);
        if (null == info) {
            for (String f : fallback) {
                info = data.getData(f);
                if (info != null) {
                    break;
                }
            }
        }
        return info;
    }
    
    /**
     * Resolves the requested {@link IdentityInformation} and logs if none was found.
     * 
     * @param identity the identity (key) to return the information instance for
     * @param fallback fallback identities to use instead in given sequence, e.g., instead a specific device a 
     *     device group
     * @return the identity information or <b>null</b>
     */
    private IdentityInformation resolveWithLogging(String identity, String... fallback) {
        IdentityInformation info = resolve(identity, fallback);
        if (null == info) {
            LoggerFactory.getLogger(getClass()).warn(
                "No identity information found for {} in store {} (with fallbacks {})", 
                    identity, data.getName(), fallback);
        }
        return info;
    }

    @Override
    public IdentityToken getToken(String identity, boolean defltAnonymous, String... fallback) {
        IdentityToken result = null;
        IdentityInformation info = resolve(identity, fallback);
        IdentityToken.IdentityTokenBuilder builder = null;
        if (null == info && defltAnonymous) {
            builder = IdentityToken.IdentityTokenBuilder.newBuilder();
        } else if (info != null) {
            builder = IdentityToken.IdentityTokenBuilder.newBuilder(info.getTokenPolicyId(), 
                info.getSignatureAlgorithm(), info.getSignatureAsBytes());
            switch (info.getType()) {
            case ISSUED:
                builder.setIssuedToken(info.getTokenDataAsBytes(), info.getTokenEncryptionAlgorithm());
                break;
            case USERNAME:
                builder.setUsernameToken(info.getUserName(), info.getTokenDataAsBytes(), 
                    info.getTokenEncryptionAlgorithm());
                break;
            case X509:
                builder.setX509Token(info.getTokenDataAsBytes());
                break;
            default: // fallback to anonymous
                break;
            }
        }
        if (null != builder) {
            result = builder.build();
        } else {
            LoggerFactory.getLogger(getClass()).warn(
                "No identity information found for {} (with fallbacks {}) in store {}. Using anonymous token: {}", 
                identity, fallback, data.getName(), defltAnonymous);
        }
        return result;
    }
    
    @Override
    public InputStream getKeystoreAsStream(String identity, String... fallback) {
        InputStream result = null;
        IdentityInformation info = resolveForKeystore(identity, fallback);
        if (isOkForKeystore(info)) {
            try {
                URI uri = new URI(info.getFile());
                File f = UriResolver.resolveToFile(uri, null);
                if (null != f && f.exists()) {
                    result = new FileInputStream(f);
                }
            } catch (URISyntaxException | IllegalArgumentException e) {
                // ignore, we just figure out whether it could be an URI
            } catch (IOException e) {
                LoggerFactory.getLogger(getClass()).warn(
                    "Resolving key file {} failed: {}. Falling back to resource resolution.", 
                        info.getFile(), e.getMessage());
            }
            if (null == result) {
                result = resolve(info.getFile());
            }
        } 
        return result;
    }

    @Override
    public KeyStore getKeystoreFile(String identity, String... fallback) throws IOException {
        KeyStore result = null;
        IdentityInformation info = resolveForKeystore(identity, fallback);
        if (isOkForKeystore(info)) {
            try {
                InputStream stream = getKeystoreAsStream(identity, fallback);
                String keystoreType = SslUtils.getKeystoreType(info.getFile());
                result = KeyStore.getInstance(keystoreType);
                result.load(stream, info.getTokenData().toCharArray());
                stream.close();
            } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException e) {
                throw new IOException(e);
            }
        } 
        return result;
    }

    @Override
    public Key getKeystoreKey(String identity, KeyStore keystore, String alias, String... fallback) throws IOException {
        Key key = null;
        IdentityInformation info = resolveForKeystore(identity, fallback);
        if (isOkForKeystore(info)) {
            if (null == keystore) {
                keystore = getKeystoreFile(identity, fallback);
            }
            try {
                key = keystore.getKey(alias, info.getTokenData().toCharArray());
            } catch (UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException e) {
                throw new IOException(e.getMessage());
            }
        }
        return key;
    }

    /**
     * Resolves the requested {@link IdentityInformation} for obtaining a keystore and logs if none was found.
     * 
     * @param identity the identity (key) to return the information instance for
     * @param fallback fallback identities to use instead in given sequence, e.g., instead a specific device a 
     *     device group
     * @return the identity information or <b>null</b>
     */
    private IdentityInformation resolveForKeystore(String identity, String... fallback) {
        IdentityInformation info = resolveWithLogging(identity, fallback);
        if (info != null && !isOkForKeystore(info)) {
            if (TokenType.USERNAME != info.getType()) {
                LoggerFactory.getLogger(getClass()).warn(
                    "Keystore information found for {} (with fallbacks {}), but type is not USERNAME",
                        identity, fallback);
            } else if (null == info.getTokenData()) {
                LoggerFactory.getLogger(getClass()).warn(
                    "Keystore information found for {} (with fallbacks {}) with type USERNAME but it has no token data",
                        identity, fallback);
            } else if (null == info.getFile()) {
                LoggerFactory.getLogger(getClass()).warn(
                    "Keystore information found for {} (with fallbacks {}), but no keystore file specified", 
                        identity, fallback);
            }
        }
        return info;
    }
    
    /**
     * Checks the identity information to load a keystore. To be used after 
     * {@link #resolveForKeystore(String, String...)}
     * 
     * @param info the information
     * @return {@code true} for ok, {@code false} else
     */
    private boolean isOkForKeystore(IdentityInformation info) {
        return (null != info && TokenType.USERNAME == info.getType() && null != info.getTokenData() 
            && null != info.getFile());
    }

    @Override
    public KeyManager[] getKeyManagers(String identity, String algorithm, String... fallback) throws IOException {
        KeyManager[] result = null;
        IdentityInformation info = resolveWithLogging(identity, fallback);
        if (null != info) {
            KeyStore ks = getKeystoreFile(identity, fallback);
            if (null != ks) {
                try {
                    KeyManagerFactory factory = KeyManagerFactory.getInstance(algorithm);
                    factory.init(ks, info.getTokenData().toCharArray());
                    result = factory.getKeyManagers();
                } catch (NoSuchAlgorithmException | UnrecoverableKeyException | KeyStoreException e) {
                    throw new IOException(e);
                }
            }
        } 
        return result;
    }


}
