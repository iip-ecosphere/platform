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

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.representer.Representer;

import de.iip_ecosphere.platform.support.FileUtils;
import de.iip_ecosphere.platform.support.identities.IdentityToken.TokenType;

/**
 * A YAML-based identity file store. 
 * 
 * @author Holger Eichelberger, SSE
 */
public class YamlIdentityFile {
    
    private String name = "<undefined>";
    private Map<String, IdentityInformation> identities = new HashMap<>();

    /**
     * Represents an identity data entry. Information is required according to the type of the data. 
     * Required information:
     * <ol>
     *     <li>{@link TokenType#ISSUED}: {@link #getSignatureAlgorithm()}, {@link #getSignature()}, 
     *         {@link #getTokenData()}, {@link #getTokenEncryptionAlgorithm()}</li>
     *     <li>{@link TokenType#X509}: {@link #getSignatureAlgorithm()}, {@link #getSignature()}, 
     *         {@link #getTokenData()}</li>
     *     <li>{@link TokenType#USERNAME}: {@link #getUserName()}, {@link #getTokenData()}, 
     *         {@link #getTokenEncryptionAlgorithm()}</li>
     *     <li>{@link TokenType#ANONYMOUS}</li>
     * </ol>
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class IdentityInformation {
        
        private TokenType type;
        private String tokenPolicyId;
        private String userName;
        private String tokenData;
        private String tokenEncryptionAlgorithm;
        private String signatureAlgorithm;
        private String signature;
        private String file;
        
        /**
         * Returns the token identity type.
         * 
         * @return the token type
         */
        public IdentityToken.TokenType getType() {
            return type;
        }
        
        /**
         * Changes the token identity type.
         * 
         * @param type the token type
         */
        public void setType(IdentityToken.TokenType type) {
            this.type = type;
        }

        /**
         * Returns the token policy identifier.
         * 
         * @return the token policy identifier, e.g., from OPC UA
         */
        public String getTokenPolicyId() {
            return tokenPolicyId;
        }
        
        /**
         * Changes the token policy identifier.
         * 
         * @param tokenPolicyId the token policy identifier, e.g., from OPC UA
         */
        public void setTokenPolicyId(String tokenPolicyId) {
            this.tokenPolicyId = tokenPolicyId;
        }
        
        /**
         * Returns the user name if {@code #getType()} is {@link TokenType#USERNAME}.
         * 
         * @return the token type
         */
        public String getUserName() {
            return userName;
        }
        
        /**
         * Attached file, e.g., keystore (must then be a {@link TokenType#USERNAME}, only password is used).
         * 
         * @return the file, may be <b>null</b>
         */
        public String getFile() {
            return file;
        }

        /**
         * Changes the user name if {@code #getType()} is {@link TokenType#USERNAME}. [snakeyaml]
         * 
         * @param userName the user name
         */
        public void setUserName(String userName) {
            this.userName = userName;
        }
        
        /**
         * Returns the token data, e.g., password if {@code #getType()} is 
         * {@link TokenType#USERNAME}.
         * 
         * @return the token data
         */
        public String getTokenData() {
            return tokenData;
        }
        
        /**
         * Changes the token data.
         * 
         * @param tokenData the token data
         */
        public void setTokenData(String tokenData) {
            this.tokenData = tokenData;
        }

        /**
         * Returns the token data as byte array.
         * 
         * @return the token data
         */
        public byte[] getTokenDataAsBytes() {
            // token encoding may be needed
            return null == tokenData ? null : tokenData.getBytes();
        }

        /**
         * Returns the token encryption algorithm.
         * 
         * @return the algorithm name, may be <b>null</b> or empty for plain text user tokens
         */
        public String getTokenEncryptionAlgorithm() {
            return tokenEncryptionAlgorithm;
        }

        /**
         * Changes the token encryption algorithm. [snakeyaml]
         * 
         * @param tokenEncryptionAlgorithm the algorithm name, may be <b>null</b> or empty for plain text user tokens
         */
        public void setTokenEncryptionAlgorithm(String tokenEncryptionAlgorithm) {
            this.tokenEncryptionAlgorithm = tokenEncryptionAlgorithm;
        }
        
        /**
         * Returns the algorithm used for the signature.
         * 
         * @return the algorithm name
         */
        public String getSignatureAlgorithm() {
            return signatureAlgorithm;
        }

        /**
         * Changes the algorithm used for the signature. [snakeyaml]
         * 
         * @param signatureAlgorithm the algorithm name
         */
        public void setSignatureAlgorithm(String signatureAlgorithm) {
            this.signatureAlgorithm = signatureAlgorithm;
        }

        /**
         * Returns the signature.
         * 
         * @return the signature
         */
        public String getSignature() {
            return signature;
        }

        /**
         * Returns the signature as byte array.
         * 
         * @return the signature
         */
        public byte[] getSignatureAsBytes() {
            // signature encoding may be needed
            return null == signature ? null : signature.getBytes();
        }

        /**
         * Changes the signature. [snakeyaml]
         * 
         * @param signature the signature
         */
        public void setSignature(String signature) {
            this.signature = signature;
        }
        
        /**
         * Attached file, e.g., keystore (must then be a {@link TokenType#USERNAME}, only password is used).
         * 
         * @param file the file, may be <b>null</b>
         */
        public void setFile(String file) {
            this.file = file;
        }

    }

    /**
     * Returns the identities. [snakeyaml]
     * 
     * @return the identities
     */
    public Map<String, IdentityInformation> getIdentities() {
        return identities;
    }
    
    /**
     * Returns the identity information for a given {@code key}.
     * 
     * @param key the key
     * @return the identity information, <b>null</b> if not found
     */
    public IdentityInformation getData(String key) {
        return identities.get(key);
    }
    
    /**
     * Returns the name of the identity store.
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the identities. [snakeyaml]
     * 
     * @param data the identities
     */
    public void setIdentities(Map<String, IdentityInformation> data) {
        this.identities = data;
    }

    /**
     * Changes the name of the identity store. [snakeyaml]
     * 
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Loads a Yaml identity file.
     * 
     * @param in the input stream, may be <b>null</b>
     * @return the file instance, if not readable an empty instance
     */
    public static YamlIdentityFile load(InputStream in) {
        YamlIdentityFile result = null;
        if (null == in) {
            LoggerFactory.getLogger(YamlIdentityFile.class).warn(
                "No input stream given: Falling back to empty instance.");
            result = new YamlIdentityFile();
        } else {
            try {
                Representer representer = new Representer();
                representer.getPropertyUtils().setSkipMissingProperties(true);
                Yaml yaml = new Yaml(new Constructor(YamlIdentityFile.class), representer);
                result = yaml.load(in);
                FileUtils.closeQuietly(in);
            } catch (YAMLException e) {
                LoggerFactory.getLogger(YamlIdentityFile.class).warn(
                    "Cannot load input stream: {} Falling back to empty instance.", e.getMessage());
                FileUtils.closeQuietly(in);
                result = new YamlIdentityFile();
            }
        }
        return result;
    }

}
