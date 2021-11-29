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

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import de.iip_ecosphere.platform.services.environment.ServiceState;
import de.iip_ecosphere.platform.services.environment.YamlService;
import de.iip_ecosphere.platform.support.TimeUtils;
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
    private static final boolean DEBUG = true;
    private PrintWriter serviceIn;
    private Process proc;

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
    public void process(I data) throws IOException {
        serviceIn.println(getInputTranslator().to(data));
    }
    
    /**
     * Preliminary: Starts the service and the background process.
     * 
     * @throws ExecutionException if starting the process fails
     */
    private void start() throws ExecutionException {
        String executable = getExecutableName("kodex", VERSION);
        File exe = new File("./src/main/resources/" + executable); // folder fixed? 
        File home = new File("./src/test/resources").getAbsoluteFile();

        List<String> a = new ArrayList<>();
        if (DEBUG) {
            a.add("--level");
            a.add("debug");
        }
        a.add("run");
        a.add("example-data.yml");
        try {
            proc = createProcess(exe, home, a);
            
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(proc.getOutputStream()));
            //BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            serviceIn = new PrintWriter(writer);
            
            redirectIO(proc.getInputStream(), getReceptionCallback());
            redirectIO(proc.getErrorStream(), System.err);
        } catch (IOException e) {
            throw new ExecutionException(e);
        }
    }
    
    /**
     * Preliminary: Stops the service and the background process.
     */
    private void stop() {
        if (null != serviceIn) {
            serviceIn.flush();
            serviceIn = null;
        }
        if (null != proc) {
            TimeUtils.sleep(WAITING_TIME); // preliminary, Andreas will try to fix this
            proc.destroy();
            proc = null;
        }
    }
    
    @Override
    public void activate() throws ExecutionException {
        super.setState(ServiceState.ACTIVATING);
        stop();
        super.setState(ServiceState.ACTIVATING);
    }

    @Override
    public void passivate() throws ExecutionException {
        super.setState(ServiceState.PASSIVATING);
        start();
        super.setState(ServiceState.PASSIVATED);
    }

    @Override
    public void setState(ServiceState state) throws ExecutionException {
        switch (state) {
        case STARTING:
            start();
            break;
        case STOPPING:
            stop();
            break;
        default:
            break;
        }
        super.setState(state);
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
    
    // preliminary, to be removed
    
}
