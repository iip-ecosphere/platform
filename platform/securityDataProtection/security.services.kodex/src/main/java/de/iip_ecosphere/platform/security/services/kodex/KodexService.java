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

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import de.iip_ecosphere.platform.services.environment.YamlService;
import de.iip_ecosphere.platform.services.environment.AbstractStringProcessService;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import de.iip_ecosphere.platform.transport.serialization.TypeTranslator;

/**
 * Integration of <a href="https://github.com/kiprotect/kodex">KIPROTECT KODEX</a> as a service.
 * 
 * @param <I> the input type
 * @param <O> the output type
 * @author Holger Eichelberger, SSE
 */
public class KodexService<I, O> extends AbstractStringProcessService<I, O>  {

    public static final int WAITING_TIME = 120000; // preliminary
    public static final String VERSION = "0.0.7";
    private static final boolean DEBUG = false;

    /**
     * Creates an instance of the service with the required type translators to/from JSON.
     * 
     * @param inTrans the input translator
     * @param outTrans the output translator
     * @param callback called when a processed item is received from the service
     * @param yaml the service description 
     */
    public KodexService(TypeTranslator<I, String> inTrans, TypeTranslator<String, O> outTrans, 
        ReceptionCallback<O> callback, YamlService yaml) {
        super(inTrans, outTrans, callback, yaml);
    }
    
    @Override
    protected void start() throws ExecutionException {
        String executable = getExecutableName("kodex", VERSION);
        File exe = new File("./src/main/resources/" + executable); // folder fixed? 
        File home = new File("./src/test/resources").getAbsoluteFile();

        List<String> args = new ArrayList<>();
        if (DEBUG) {
            args.add("--level");
            args.add("debug");
        }
        args.add("run");
        args.add("example-data.yml");
        createAndConfigureProcess(exe, false, home, args);
    }

    @Override
    protected int getWaitTimeBeforeDestroy() {
        return WAITING_TIME; // preliminary, Andreas will try to fix this
    }
    
    @Override
    public void migrate(String resourceId) throws ExecutionException {
    }

    @Override
    public void update(URI location) throws ExecutionException {
    }

    @Override
    public void switchTo(String targetId) throws ExecutionException {
    }

    @Override
    public void reconfigure(Map<String, String> values) throws ExecutionException {
    }
    
}
