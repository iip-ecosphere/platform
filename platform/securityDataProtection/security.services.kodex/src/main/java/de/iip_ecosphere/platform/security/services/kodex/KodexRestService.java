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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang.SystemUtils;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import com.esotericsoftware.yamlbeans.YamlWriter;

import de.iip_ecosphere.platform.services.environment.YamlService;
import de.iip_ecosphere.platform.services.environment.AbstractRestProcessService;
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
public class KodexRestService<I, O> extends AbstractRestProcessService<I, O>  {

    public static final int WAITING_TIME_WIN = 120000; // preliminary
    public static final int WAITING_TIME_OTHER = 100; // preliminary
    public static final String VERSION = "0.0.8";
    private static final boolean DEBUG = false;
   
    private String dataSpec;
    private String bearerToken;
    private File home;
    private int port;
    private String host;

    /**
     * Creates an instance of the service with the required type translators to/from JSON. Data file is 
     * "data.yml".
     * 
     * @param inTrans the input translator
     * @param outTrans the output translator
     * @param callback called when a processed item is received from the service
     * @param yaml the service description 
     */
    public KodexRestService(TypeTranslator<I, String> inTrans, TypeTranslator<String, O> outTrans, 
        ReceptionCallback<O> callback, YamlService yaml) {
        this(inTrans, outTrans, callback, yaml, "data.yml");
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
    public KodexRestService(TypeTranslator<I, String> inTrans, TypeTranslator<String, O> outTrans, 
        ReceptionCallback<O> callback, YamlService yaml, String dataSpec) {
        super(inTrans, outTrans, callback, yaml);
        this.dataSpec = dataSpec;
        setNetworkDefaults();
    }
    
    /**
     * Sets network settings to KODEX defaults.
     */
    private void setNetworkDefaults() {
        port = 8000;
        host = "0.0.0.0";
    }
    
    @Override
    protected ServiceState start() throws ExecutionException {
        String executable = getExecutableName("kodex", VERSION);
        YamlProcess sSpec = getProcessSpec();

        // TODO change port to ephemeral when KODEX port change is working
        File exe = selectNotNull(sSpec, s -> s.getExecutablePath(), new File("./src/main/resources/")); 
        home = selectNotNull(sSpec, s -> s.getHomePath(), new File("./src/test/resources"));
        exe = new File(exe, executable); 
        exe.setExecutable(true);
        home = home.getAbsoluteFile();
        Map<String, Object> apiSettings = new HashMap<>();
        apiSettings.put("port", port);
        apiSettings.put("host", host);
        File settingFile = new File(home, "apiSettings.yml");
        try {
            YamlWriter writer = new YamlWriter(new FileWriter(settingFile));
            writer.write(apiSettings);
            writer.close();
        } catch (IOException e) {
            setNetworkDefaults();
            LoggerFactory.getLogger(getClass()).error("Cannot write kodex setting file '{}': {}. Falling back "
                + "to KODEX defaults. May clash on used port.", settingFile.getAbsolutePath(), e.getMessage());
        }
        
        List<String> args = new ArrayList<>();
        if (DEBUG) {
            args.add("--level");
            args.add("debug");
        }
        args.add("api");
        args.add("run");
        args.add(dataSpec);
        addProcessSpecCmdArg(args);
        
        createAndConfigureProcess(exe, false, home, args);
        waitForConnection();
        setupConnectionManager();
        return ServiceState.RUNNING;
    }
    
    @Override
    protected void configure(ProcessBuilder builder) {
        // TODO enable if KODEX works with that
        //Map<String, String> env = builder.environment();
        //env.put("KODEX_SETTINGS", "apiSettings.yml");
    }
    
    @Override
    protected String getApiPath() {
        return "http://" + host + ":" + port + "/v1/configs/abcdef/transform";
    }
    
    @Override
    protected String getBearerToken() {
        if (null == bearerToken) {
            try {
                InputStream inputStream = new FileInputStream(new File(home, "api.yml"));
                Yaml yaml = new Yaml();
                Map<String, Object> data = yaml.load(inputStream);
                Object users = data.get("users"); 
                String[] usersSplit = users.toString().split(",");
                String accessToken = null;
                for (String item : usersSplit) {
                    if (item.contains("accessToken")) {
                        accessToken = item;
                    }
                }
                String[] accesTokenSplit = accessToken.split("=");
                String token = null;
                for (String item : accesTokenSplit) {
                    token = item;
                }
                bearerToken = "Bearer " + token;
            } catch (FileNotFoundException e) {
                LoggerFactory.getLogger(getClass()).error("Reading bearer " + e.getMessage(), e);
            }
        }
        return bearerToken;
    }

    @Override
    protected String adjustRestQuery(String input) {
        return "{\"items\":[" + input + "]}";
    }
    
    @Override
    protected String adjustRestResponse(String response) {
        String result = response.replace("{\"data\":{\"errors\":[],\"items\":[", "");
        result = result.replace("],\"messages\":[],\"warnings\":[]}}", "");
        return result;
    }

    @Override
    protected int getWaitTimeBeforeDestroy() {
        // preliminary, Andreas will try to fix this; Win vs. Other -> experimental
        return SystemUtils.IS_OS_WINDOWS ? WAITING_TIME_WIN : WAITING_TIME_OTHER; 
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

    @Override
    protected void handleInputStream(InputStream in) { 
    }
}
