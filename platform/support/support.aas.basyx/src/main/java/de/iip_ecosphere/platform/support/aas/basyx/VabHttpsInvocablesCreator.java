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

package de.iip_ecosphere.platform.support.aas.basyx;

import java.io.IOException;
import java.security.KeyStore;

import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManagerFactory;

import org.eclipse.basyx.vab.coder.json.connector.JSONConnector;
import org.eclipse.basyx.vab.modelprovider.VABElementProxy;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.net.KeyStoreDescriptor;
import de.iip_ecosphere.platform.support.net.SslUtils;

/**
 * implements an invocables creator for HTTPS-based VAB.
 * 
 * @author Holger Eichelberger, SSE
 */
public class VabHttpsInvocablesCreator extends VabInvocablesCreator {

    private static final long serialVersionUID = 8021322086051502297L;
    private String address;
    private KeyStoreDescriptor kstore;
    private transient BaSyxJerseyHttpsClientFactory factory;
    
    /**
     * Creates an invocables creator instance.
     * 
     * @param address the HTTP address to connect to
     * @param kstore the key store descriptor, ignored if <b>null</b>
     */
    VabHttpsInvocablesCreator(String address, KeyStoreDescriptor kstore) {
        this.address = address;
        this.kstore = kstore;
    }
    
    @Override
    protected VABElementProxy createProxy() {
        if (null == factory && null != kstore) {
            try {
                KeyStore ks = SslUtils.openKeyStore(kstore.getPath(), kstore.getPassword());
                TrustManagerFactory tmf = SslUtils.createTrustManagerFactory(ks);
                KeyManager[] kms = SslUtils.createKeyManagers(ks, kstore.getPassword(), kstore.getAlias());
                factory = new BaSyxJerseyHttpsClientFactory(kms, tmf.getTrustManagers());
            } catch (IOException e) {
                LoggerFactory.getLogger(VabHttpsInvocablesCreator.class).error(
                    "Creating VAB-HTTPS client factory: " + e.getMessage());
            }
        }
        return new VABElementProxy("", new JSONConnector(new BaSyxHTTPSConnector(address, factory)));
    }

    @Override
    protected String getId() {
        return address;
    }

}
