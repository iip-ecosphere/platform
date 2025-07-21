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
import java.io.IOException;

import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.identities.IdentityStore;
import de.iip_ecosphere.platform.support.net.KeyStoreDescriptor;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;

/**
 * A proxy for {@link ServerAddress} with a protocol, as we do not want to have setters there.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TlsServerAddressHolder extends ServerAddressHolder {

    private File keystore;
    private String keyPassword;
    private String keystoreKey;
    private String keyAlias;
    private boolean appliesToClient = true;
    private boolean hostnameVerification = true;

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
     * Creates an instance by copying data from a given instance.
     * 
     * @param holder the holder to copy from
     */
    public TlsServerAddressHolder(TlsServerAddressHolder holder) {
        super(holder);
        keyAlias = holder.keyAlias;
        keyPassword = holder.keyPassword;
        keystoreKey = holder.keystoreKey;
        keystore = holder.keystore;
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
     * Returns the keystore key, which, via the {@link IdentityStore} may replace {@link #getKeystore()} and 
     * {@link #getKeyPassword()}.
     * 
     * @return the keystore key, may be <b>null</b> for none
     */
    public String getKeystoreKey() {
        return keystoreKey;
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
     * Returns whether a SSL client shall use the keystore or rely on the default certificate chain.
     * 
     * @return {@code true} for keystore (default), {@code false} else for default chain
     */
    public boolean getAppliesToClient() {
        return appliesToClient;
    }

    /**
     * Returns whether SSL hostname verification shall be enabled or not. May not be applied to every HTTP client.
     * 
     * @return {@code true} for enabled, {@code false} else
     */
    public boolean getHostnameVerification() {
        return hostnameVerification;
    }

    /**
     * Defines the optional TLS keystore. [required by data mapper]
     * 
     * @param keystore the TLS keystore (suffix ".jks" points to Java Key store, suffix ".p12" to PKCS12 keystore), may 
     *   be <b>null</b> for none
     */
    public void setKeystore(File keystore) {
        this.keystore = keystore;
    }

    /**
     * Defines the password for the optional TLS keystore. [required by data mapper]
     * 
     * @param keyPassword the TLS keystore, may be <b>null</b> for none
     */
    public void setKeyPassword(String keyPassword) {
        this.keyPassword = keyPassword;
    }

    /**
     * Defines the keystore key, which, via the {@link IdentityStore} may replace {@link #getKeystore()} and 
     * {@link #getKeyPassword()}. [required by data mapper]
     * 
     * @param keystoreKey the keystore key, may be <b>null</b> for none
     */
    public void setKeystoreKey(String keystoreKey) {
        this.keystoreKey = keystoreKey;
    }

    /**
     * Changes the alias denoting the key to use. [required by data mapper]
     * 
     * @param alias the alias, may be <b>null</b> for none/first match
     */
    public void setKeyAlias(String alias) {
        this.keyAlias = alias;
    }

    /**
     * Defines whether a SSL client shall use the keytore or rely on the default certificate chain. [required by 
     * data mapper]
     * 
     * @param appliesToClient {@code true} for keystore (default), {@code false} else for default chain
     */
    public void setAppliesToClient(boolean appliesToClient) {
        this.appliesToClient = appliesToClient;
    }

    /**
     * Defines whether SSL hostname verification shall be enabled or not.  May not be applied to every HTTP client. 
     * [required by data mapper]
     * 
     * @param hostnameVerification {@code true} for enabled, {@code false} else
     */
    public void setHostnameVerification(boolean hostnameVerification) {
        this.hostnameVerification = hostnameVerification;
    }

    /**
     * Returns a keystore descriptor representing the keystore information.
     * 
     * @return the keystore descriptor, may be <b>null</b> if {@link #keystore} is <b>null</b>
     */
    public KeyStoreDescriptor getKeystoreDescriptor() {
        KeyStoreDescriptor result = null;
        if (null != keystoreKey) {
            try {
                result = KeyStoreDescriptor.create(keystoreKey, keyAlias, appliesToClient, hostnameVerification);
            } catch (IOException e) {
                LoggerFactory.getLogger(getClass()).warn("Cannot obtain keystore via keystore key '{}' due to {}. "
                    + "Trying to obtain keystore directly.", keystoreKey, e.getMessage());
            }
        }
        if (null != keystore && null == result) {
            result = new KeyStoreDescriptor(keystore, keyPassword, keyAlias, appliesToClient, hostnameVerification);
        }
        return result;
    }

}
