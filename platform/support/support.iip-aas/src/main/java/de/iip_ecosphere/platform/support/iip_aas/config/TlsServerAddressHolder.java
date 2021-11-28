/**
 * ******************************************************************************
 * Copyright (c) {2021} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.iip_aas.config;

import java.io.File;

import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.net.KeyStoreDescriptor;

/**
 * A proxy for {@link ServerAddress} with a protocol, as we do not want to have setters there.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TlsServerAddressHolder extends ServerAddressHolder {

    private File keystore;
    private String keyPassword;
    private String keyAlias;

    /**
     * Creates an instance (deserialization).
     */
    public TlsServerAddressHolder() {
    }

    /**
     * Creates an instance for unencrypted communication.
     * 
     * @param schema the schema
     * @param host the host name
     * @param port the port
     */
    public TlsServerAddressHolder(Schema schema, String host, int port) {
        super(schema, host, port);
    }

    /**
     * Creates an instance from a given instance for unencrypted communication (serialization).
     * 
     * @param addr the instance to take data from
     */
    public TlsServerAddressHolder(ServerAddress addr) {
        super(addr);
    }

    /**
     * Returns the optional TLS keystore.
     * 
     * @return the TLS keystore (suffix ".jks" points to Java Key store, suffix ".p12" to PKCS12 keystore), may 
     *   be <b>null</b> for none
     */
    public File getKeystore() {
        return keystore;
    }

    /**
     * Returns the password for the optional TLS keystore.
     * 
     * @return the TLS keystore, may be <b>null</b> for none
     */
    public String getKeyPassword() {
        return keyPassword;
    }

    /**
     * Returns the alias denoting the key to use.
     * 
     * @return the alias, may be <b>null</b> for none/first match
     */
    public String getKeyAlias() {
        return keyAlias;
    }

    /**
     * Returns the optional TLS keystore. [required by data mapper]
     * 
     * @param keystore the TLS keystore (suffix ".jks" points to Java Key store, suffix ".p12" to PKCS12 keystore), may 
     *   be <b>null</b> for none
     */
    public void setKeystore(File keystore) {
        this.keystore = keystore;
    }

    /**
     * Returns the password for the optional TLS keystore. [required by data mapper]
     * 
     * @param keyPassword the TLS keystore, may be <b>null</b> for none
     */
    public void setKeyPassword(String keyPassword) {
        this.keyPassword = keyPassword;
    }

    /**
     * Returns the alias denoting the key to use. [required by data mapper]
     * 
     * @param alias the alias, may be <b>null</b> for none/first match
     */
    public void setKeyAlias(String alias) {
        this.keyAlias = alias;
    }
    
    /**
     * Returns a keystore descriptor representing the keystore information.
     * 
     * @return the keystore descriptor, may be <b>null</b> if {@link #keystore} is <b>null</b>
     */
    public KeyStoreDescriptor getKeystoreDescriptor() {
        KeyStoreDescriptor result = null;
        if (null != keystore) {
            result = new KeyStoreDescriptor(keystore, keyPassword, keyAlias);
        }
        return result;
    }

}
