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

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.identities.YamlIdentityFile.IdentityInformation;

/**
 * Simple file-based identity store.
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
        InputStream in = YamlIdentityStore.class.getResourceAsStream("identityStore.yml");
        if (null == in) {
            String storeFolder = System.getProperty("iip.identityStore", ".");
            File f = new File(storeFolder, "identityStore.yml");
            // for local testing
            if (!f.exists()) {
                f = new File("src/test/resources/identityStore.yml");
            }
            // for local development/deployment preparation
            if (!f.exists()) {
                f = new File("src/main/resources/identityStore.yml");
            }
            if (f.exists()) {
                try {
                    in = new FileInputStream(f);
                } catch (IOException e) {
                    LoggerFactory.getLogger(getClass()).info("Cannot load identityStore.yml: {}", e.getMessage());
                }
            } else {
                in = null;
            }
        }
        data = YamlIdentityFile.load(in); // can cope with null
    }

    @Override
    public IdentityToken getToken(String identity, boolean defltAnonymous, String... fallback) {
        IdentityToken result = null;
        IdentityInformation info = data.getData(identity);
        if (null == info) {
            for (String f : fallback) {
                info = data.getData(f);
                if (info != null) {
                    break;
                }
            }
        }
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
                "No token found for {} (with fallbacks {}). Using anonymous token: {}", 
                identity, fallback, defltAnonymous);
        }
        return result;
    }

}
