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
import de.iip_ecosphere.platform.support.FileUtils;
import de.iip_ecosphere.platform.support.OsUtils;
import de.iip_ecosphere.platform.services.environment.AbstractService;
import de.iip_ecosphere.platform.services.environment.AbstractStringProcessService;
import de.iip_ecosphere.platform.services.environment.ServiceState;
import de.iip_ecosphere.platform.services.environment.YamlProcess;
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

    public static final int WAITING_TIME_WIN = 100; // preliminary
    public static final int WAITING_TIME_OTHER = 100; // preliminary
    public static final String VERSION = "0.1.6";
    public static final String DFLT_DATA_SPEC = "data.yml";
    private static final boolean DEBUG = false;
    
    private String dataSpec;

    /**
     * Creates an instance of the service with the required type translators to/from JSON. Data file is 
     * "{@value #DFLT_DATA_SPEC}.
     * 
     * @param inTrans the input translator
     * @param outTrans the output translator
     * @param callback called when a processed item is received from the service
     * @param yaml the service description 
     */
    public KodexService(TypeTranslator<I, String> inTrans, TypeTranslator<String, O> outTrans, 
        ReceptionCallback<O> callback, YamlService yaml) {
        this(inTrans, outTrans, callback, yaml, DFLT_DATA_SPEC);
    }
    
    /**
     * Creates an instance of the service with the required type translators to/from JSON.
     * 
     * @param inTrans the input translator
     * @param outTrans the output translator
     * @param callback called when a processed item is received from the service
     * @param yaml the service description
     * @param args the first argument shall be the name of the data spec file (within the process home path) to pass 
     *     to KODEX; related files such as api or actions must be there as well and referenced from the data spec file 
     */
    public KodexService(TypeTranslator<I, String> inTrans, TypeTranslator<String, O> outTrans, 
        ReceptionCallback<O> callback, YamlService yaml, Object... args) {
        this(inTrans, outTrans, callback, yaml, getDataSpecArg(args));
    }
    
    /**
     * Creates an instance of the service with the required type translators to/from JSON.
     * 
     * @param inTrans the input translator
     * @param outTrans the output translator
     * @param callback called when a processed item is received from the service
     * @param yaml the service description
     * @param dataSpec name of the data spec file (within the process home path) to pass to KODEX; related files such 
     *     as api or actions must be there as well and referenced from the data spec file 
     */
    public KodexService(TypeTranslator<I, String> inTrans, TypeTranslator<String, O> outTrans, 
        ReceptionCallback<O> callback, YamlService yaml, String dataSpec) {
        super(inTrans, outTrans, callback, yaml);
        this.dataSpec = dataSpec;
    }
    
    /**
     * Extracts the first argument from {@code args} with default {@link #DFLT_DATA_SPEC}.
     */
    static String getDataSpecArg(Object[] args) {
        return AbstractService.getStringArg(0, args, DFLT_DATA_SPEC);
    }
    
    /**
     * Cleans up KODEX files. 
     */
    static void cleanFiles() {
        File kiProtectStore = new File(System.getProperty("user.home"), ".kiprotect");
        if (kiProtectStore.exists() && kiProtectStore.isDirectory()) {
            FileUtils.deleteQuietly(new File(kiProtectStore, "parameters.kip"));
        }
    }
    
    @Override
    protected ServiceState start() throws ExecutionException {
        cleanFiles();
        String executable = getExecutableName("kodex", VERSION);
        YamlProcess sSpec = getProcessSpec();

        File exe = selectNotNull(sSpec, s -> s.getExecutablePath(), new File("./src/main/resources/")); 
        File home = selectNotNull(sSpec, s -> s.getHomePath(), new File("./src/test/resources"));
        exe = new File(exe, executable); 
        exe.setExecutable(true);
        home = home.getAbsoluteFile();
        
        List<String> args = new ArrayList<>();
        if (DEBUG) {
            args.add("--level");
            args.add("debug");
        }
        args.add("run");
        args.add(dataSpec);
        addProcessSpecCmdArg(args);
        
        createAndConfigureProcess(exe, false, home, args);
        return ServiceState.RUNNING;
    }

    @Override
    protected int getWaitTimeBeforeDestroy() {
        // preliminary, Andreas will try to fix this; Win vs. Other -> experimental
        return OsUtils.isWindows() ? WAITING_TIME_WIN : WAITING_TIME_OTHER; 
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
