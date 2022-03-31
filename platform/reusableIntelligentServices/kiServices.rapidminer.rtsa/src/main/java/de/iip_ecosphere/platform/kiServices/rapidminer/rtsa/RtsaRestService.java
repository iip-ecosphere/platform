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

package de.iip_ecosphere.platform.kiServices.rapidminer.rtsa;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.services.environment.YamlService;
import de.iip_ecosphere.platform.services.environment.AbstractRestProcessService;
import de.iip_ecosphere.platform.services.environment.InstalledDependenciesSetup;
import de.iip_ecosphere.platform.services.environment.ServiceState;
import de.iip_ecosphere.platform.services.environment.YamlProcess;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import de.iip_ecosphere.platform.transport.serialization.TypeTranslator;

import static de.iip_ecosphere.platform.support.FileUtils.*;

/**
 * Integration of <a href="https://github.com/kiprotect/kodex">KIPROTECT KODEX</a> as a service.
 * 
 * @param <I> the input type
 * @param <O> the output type
 * @author Holger Eichelberger, SSE
 */
public class RtsaRestService<I, O> extends AbstractRestProcessService<I, O>  {

    public static final String VERSION = "0.14.5";
   
    private File home;
    private Process proc;

    /**
     * Creates an instance of the service with the required type translators to/from JSON.
     * 
     * @param inTrans the input translator
     * @param outTrans the output translator
     * @param callback called when a processed item is received from the service
     * @param yaml the service description
     */
    public RtsaRestService(TypeTranslator<I, String> inTrans, TypeTranslator<String, O> outTrans, 
        ReceptionCallback<O> callback, YamlService yaml) {
        super(inTrans, outTrans, callback, yaml);
    }
    
    @Override
    protected void start() throws ExecutionException {
        YamlProcess sSpec = getProcessSpec();

        File exe = InstalledDependenciesSetup.location(InstalledDependenciesSetup.KEY_JAVA_8);
        File rtsaPath = selectNotNull(sSpec, s -> s.getExecutablePath(), new File("./src/main/resources/rtsa"));
        home = selectNotNull(sSpec, s -> s.getHomePath(), new File("./src/test/resources"));
        home = getResolvedFile(home);
        
        List<String> args = new ArrayList<>();
        
        args.add("-Dspring.config.location=" + getResolvedPath(rtsaPath, "/home/config/agent.properties"));
        args.add("-Dscoring-agent.baseDir=" + getResolvedPath(rtsaPath, ""));
        args.add("-Dspring.pid.file=" + getResolvedPath(rtsaPath, "/home/pid"));
        args.add("-Dlog4j2.formatMsgNoLookups=true");
        args.add("-classpath");
        args.add(getClasspath(rtsaPath));
        args.add(getMainClass());
        addProcessSpecCmdArg(args);
        // TODO change spring Server port
        // TODO change RTSA port
        
        proc = createAndConfigureProcess(exe, false, home, args);
    }
    
    /**
     * Returns the RTSA classpath.
     * 
     * @param rtsaPath the path to the installed RTSA
     * @return the classpath
     */
    protected String getClasspath(File rtsaPath) {
        return getResolvedPath(rtsaPath, "lib") + File.separator + "*";
    }
    
    /**
     * Returns the main class of RTSA. [for testing]
     * 
     * @return the main class
     */
    protected String getMainClass() {
        return "com.rapidminer.execution.scoring.Application";
    }
    
    @Override
    protected String getApiPath() {
        return "http://localhost:8090/services/iip_basic/score_v1";
    }
    
    @Override
    protected String getBearerToken() {
        return null;
    }

    @Override
    protected void handleInputStream(InputStream in) { // better via Spring Tomcat?
        new Thread(new Runnable() {
            public void run() {
                while (getState() == ServiceState.AVAILABLE || (null != proc && proc.isAlive())) {
                    Scanner sc = new Scanner(in);
                    while (sc.hasNextLine()) {
                        String line = sc.nextLine();
                        LoggerFactory.getLogger(getClass()).info(line);
                        if (line.contains("Started Application in")) {
                            try {
                                setState(ServiceState.RUNNING);
                            } catch (ExecutionException e) {
                                LoggerFactory.getLogger(getClass()).error(e.getMessage());
                            }
                        }
                    }
                    sc.close();
                    try {
                        setState(ServiceState.STOPPED);
                    } catch (ExecutionException e) {
                        LoggerFactory.getLogger(getClass()).error(e.getMessage());
                    }
                }
            }
        }).start();
    }
    
    @Override
    protected String adjustRestQuery(String input) {
        return "{\"data\":[" + input + "]}";
    }
    
    @Override
    protected String adjustRestResponse(String response) {
        String result = response.replace("{\"data\":[", "");
        result = result.replace("]}", "");
        return result;
    }

    @Override
    protected int getWaitTimeBeforeDestroy() {
        return 100; // preliminary 
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
