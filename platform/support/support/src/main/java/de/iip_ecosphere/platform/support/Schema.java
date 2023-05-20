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

package de.iip_ecosphere.platform.support;

/**
 * Lists common relevant protocol schemas.
 * 
 * @author Holger Eichelberger, SSE
 */
public enum Schema {

    /**
     * HTTP unencrypted.
     */
    HTTP("http://", false), 

    /**
     * HTTP encrypted.
     */
    HTTPS("https://", true), 

    /**
     * TCP unencrypted.
     */
    TCP("tcp://", false),

    /**
     * TCP TLS-encrypted.
     */
    SSL("ssl://", true),
    
    /**
     * Use whatever you like.
     */
    IGNORE("", false);
    
    private String uriSchema;
    private boolean isEncrypted;
    
    /**
     * Creates a constant.
     * 
     * @param uriSchema the URL schema prefix, may be empty for none
     * @param isEncryted {@code true} for encrypted, {@code false} for plaintext
     */
    private Schema(String uriSchema, boolean isEncryted) {
        this.uriSchema = uriSchema;
        this.isEncrypted = isEncryted;
    }
    
    /**
     * Returns whether this schema is supposed to be encrypted.
     * 
     * @return {@code true} for encrypted, {@code false} for plaintext
     */
    public boolean isEncrypted() {
        return isEncrypted;
    }
    
    /**
     * Returns the URI schema prefix.
     * 
     * @return the URI schema prefix, may be empty for none
     */
    public String toUri() {
        return uriSchema;
    }
    
}
