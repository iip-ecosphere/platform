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
import de.iip_ecosphere.platform.support.setup.CmdLine;
import de.iip_ecosphere.platform.support.net.NetworkManagerFactory;
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
    private String instancePath = "iip_basic/score_v1";
    private int instancePort = 8090;
    private String networkPortKey;

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
    protected ServiceState start() throws ExecutionException {
        YamlProcess sSpec = getProcessSpec();

        File rtsaPath = selectNotNull(sSpec, s -> s.getExecutablePath(), new File("./src/main/resources/rtsa"));
        File origRtsaPath = rtsaPath;
        rtsaPath = checkNesting(rtsaPath);
        File exe = InstalledDependenciesSetup.location(InstalledDependenciesSetup.KEY_JAVA_8);
        home = selectNotNull(sSpec, s -> s.getHomePath(), new File("./src/test/resources"));
        if (!origRtsaPath.equals(rtsaPath)) {
            home = checkNesting(home);
        }
        home = getResolvedFile(home);
        
        List<String> args = new ArrayList<>();
        networkPortKey = "rtsa_" + getServiceSpec().getId();
        instancePort = NetworkManagerFactory.getInstance().obtainPort(networkPortKey).getPort();
        args.add("-Dspring.config.location=" + getConfigLocation(rtsaPath));
        args.add("-Dscoring-agent.baseDir=" + getBaseDir(rtsaPath));
        args.add("-Dserver.port=" + instancePort);
        args.add("-Dspring.pid.file=" + getPidFile(rtsaPath));
        args.add("-Dlog4j2.formatMsgNoLookups=true");
        args.add("-classpath");
        args.add(getClasspath(rtsaPath));
        args.add(getMainClass(rtsaPath));
        addProcessSpecCmdArg(args);
        parseArgs(args.toArray(new String[] {}));
        proc = createAndConfigureProcess(exe, false, home, args);
        setupConnectionManager();
        return null; // don't change state
    }
    
    @Override
    protected ServiceState stop() {
        if (null != networkPortKey) {
            NetworkManagerFactory.getInstance().releasePort(networkPortKey);
            networkPortKey = null;
        }
        return super.stop();
    }
    
    /**
     * If the RTSA is differently packaged, i.e., not directly in main rather than in a single folder, use that folder.
     * 
     * @param rtsaPath the RTSA path
     * @return {@code rtsaPath} or the single directory nested in {@code rtsaPath}
     */
    private static File checkNesting(File rtsaPath) {
        File result = rtsaPath;
        File bin = new File(rtsaPath, "bin");
        if (!bin.exists()) {
            File[] files = rtsaPath.listFiles();
            if (null != files) {
                File dir = null;
                int dirCount = 0;
                for (File f : files) {
                    if (f.isDirectory()) {
                        dirCount++;
                        dir = f;
                    }
                }
                if (dirCount == 1) {
                    result = dir;
                }
            }
        }
        return result;
    }

    /**
     * Takes over command line arguments.
     * 
     * @param args the arguments
     */
    private void parseArgs(String[] args) {
        instancePath = CmdLine.getArg(args, "iip.rtsa.path", instancePath);
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
     * Returns the configuration location to use.
     * 
     * @param rtsaPath the path to the installed RTSA
     * @return the base directory
     */
    protected String getConfigLocation(File rtsaPath) {
        return getResolvedPath(rtsaPath, "/home/config/agent.properties");
    }

    /**
     * Returns the PID file (path) to use.
     * 
     * @param rtsaPath the path to the installed RTSA
     * @return the PID file path
     */
    protected String getPidFile(File rtsaPath) {
        return getResolvedPath(rtsaPath, "/home/pid");
    }

    /**
     * Returns the base directory to use.
     * 
     * @param rtsaPath the path to the installed RTSA
     * @return the base directory
     */
    protected String getBaseDir(File rtsaPath) {
        return getResolvedPath(rtsaPath, "");
    }
    
    /**
     * Returns whether we try to run a fake RTSA. [testing, running without license]
     * 
     * @param rtsaPath the path to the installed RTSA
     * @return {@code true} for fake RTSA, {@code false} for real RTSA
     */
    protected boolean isFakeRtsa(File rtsaPath) {
        File libsFake = new File(rtsaPath, "lib/fakeRtsa.jar");
        return libsFake.exists();
    }
    
    /**
     * Returns the main class of RTSA. [for testing]
     * 
     * @param rtsaPath the path to the installed RTSA
     * @return the main class
     */
    protected String getMainClass(File rtsaPath) {
        String result;
        if (isFakeRtsa(rtsaPath)) {
            result = "de.iip_ecosphere.platform.kiServices.rapidminer.rtsaFake.FakeRtsa";
        } else {
            result = "com.rapidminer.execution.scoring.Application";
        }
        return result;
    }
    
    @Override
    protected String getApiPath() {
        return "http://localhost:" + instancePort + "/services/" + instancePath;
    }
    
    @Override
    protected String getBearerToken() {
        return null;
    }

    @Override
    protected void handleInputStream(InputStream in) { // better via Spring Tomcat?
        new Thread(new Runnable() {

            @Override
            public void run() {
                while (getState() == ServiceState.STARTING || (null != proc && proc.isAlive())) {
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
                        setState(getState() == ServiceState.RUNNING ? ServiceState.STOPPED : ServiceState.FAILED);
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
