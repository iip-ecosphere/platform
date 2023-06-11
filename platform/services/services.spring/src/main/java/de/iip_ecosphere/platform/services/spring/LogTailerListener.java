/**
 * ******************************************************************************
 * Copyright (c) {2023} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.services.spring;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListenerAdapter;

import de.iip_ecosphere.platform.services.environment.services.Sender;
import de.iip_ecosphere.platform.services.environment.services.TransportConverterFactory;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry.AasSetup;
import de.iip_ecosphere.platform.transport.connectors.TransportSetup;
import de.iip_ecosphere.platform.transport.serialization.TypeTranslators;

/**
 * A log tailer listener connected to a transport converter sender.
 * 
 * @author Holger Eichelberger, SSE
 */
class LogTailerListener extends TailerListenerAdapter implements Closeable {
    
    private Sender<String> sender;
    private Tailer tailer;
    private int usageCount = 1; // created to use

    /**
     * Creates a log tailer listener.
     * 
     * @param aas the AAS setup for AAS-based senders
     * @param transport the transport setup for transport-based senders
     * @param path within the mechanism to identify the data, may be an id short, an URI sub-path etc.
     */
    LogTailerListener(AasSetup aas, TransportSetup transport, String path) {
        sender = TransportConverterFactory.getInstance().createSender(aas, transport, path, TypeTranslators.STRING, 
            String.class);
        try {
            sender.connectBlocking();
        } catch (InterruptedException e) {
        }
    }
    
    /**
     * Returns the URI that this sender is connected to.
     *
     * @return the URI connected to (may be <b>null</b>)
     */
    public URI getURI() {
        return null == sender ? null : sender.getURI();
    }
    
    @Override
    public void handle(final String line) {
        if (null != sender) {
            try {
                sender.send(line);
            } catch (IOException e) {
            }
        }
    }

    @Override
    public void close() throws IOException {
        if (null != sender) {
            sender.close();
        }
        if (null != tailer) {
            tailer.stop();
        }
    }
    
    /**
     * Attaches the handling tailer.
     * 
     * @param tailer the tailer
     */
    void attachTailer(Tailer tailer) {
        this.tailer = tailer;
    }

    /**
     * Indicates a further use and increases the usage count.
     * 
     * @return the actual usage count
     */
    int increaseUsageCount() {
        return ++usageCount;
    }
    
    /**
     * Decreases a further use.
     * 
     * @return the actual usage count
     */
    int decreaseUsageCount() {
        return --usageCount;
    }
    
}
