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

package de.iip_ecosphere.platform.security.services.kodex;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.services.environment.YamlService;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import de.iip_ecosphere.platform.transport.serialization.TypeTranslator;

/**
 * Implements an abstract asynchronous process-based service which require a String-based communication with
 * the actual service process, e.g., via JSON.
 * 
 * @author Holger Eichelberger, SSE
 *
 * @param <I> the input data type
 * @param <O> the output data type
 */
public abstract class AbstractStringProcessService<I, O> extends AbstractProcessService<I, String, String, O> {

    /**
     * Creates an instance of the service with the required type translators.
     * 
     * @param inTrans the input translator
     * @param outTrans the output translator
     * @param callback called when data from the service is available
     * @param yaml the service description 
     */
    protected AbstractStringProcessService(TypeTranslator<I, String> inTrans, TypeTranslator<String, O> outTrans,
        ReceptionCallback<O> callback, YamlService yaml) {
        super(inTrans, outTrans, callback, yaml);
    }
    
    // TODO move up to service environment
    
    /**
     * Redirects an input stream to another stream (in parallel).
     * 
     * @param in the input stream of the spawned process (e.g., input/error)
     * @param callback the callback to inform
     */
    public void redirectIO(final InputStream in, ReceptionCallback<O> callback) {
        if (null != callback) {
            new Thread(new Runnable() {
                public void run() {
                    Scanner sc = new Scanner(in);
                    while (sc.hasNextLine()) {
                        String line = sc.nextLine();
                        try {
                            callback.received(getOutputTranslator().to(line));
                        } catch (IOException e) {
                            LoggerFactory.getLogger(getClass()).error("Receiving result: " + e.getMessage());
                        }
                    }
                    sc.close();
                }
            }).start();
        }
    }

    @Override
    protected void handleInputStream(InputStream in) {
        redirectIO(in, getReceptionCallback());
    }

}
