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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang.SystemUtils;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.services.environment.YamlService;
import de.iip_ecosphere.platform.support.TimeUtils;
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
public class KodexRestService<I, O> extends AbstractStringProcessService<I, O>  {

    public static final int WAITING_TIME_WIN = 120000; // preliminary
    public static final int WAITING_TIME_OTHER = 100; // preliminary
    public static final String VERSION = "0.0.7";
    private static final boolean DEBUG = false;
    
    private HttpURLConnection connection;
    private String dataSpec;

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
    }
    
    /**
     * Get Connection to local server.
     *
     * @param quiet shall a connector error be logged or not (quiet) 
     * @throws IOException in case of I/O related problems
     */
    private void obtainConnection(boolean quiet) throws IOException {
        try {
            // TODO: port number and api path are fixed but depend on api.yaml file. Also the bearer.
            // Please read yaml file from s.getHomePath() or fallback new File("./src/test/resources")
            // upon start, store information in attributes and use here
            URL url = new URL("http://localhost:8000/v1/configs/abcdef/transform");
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer aabbccdd");
            connection.connect();
        } catch (ConnectException con) {
            if (quiet) {
                LoggerFactory.getLogger(KodexRestService.class).error(con.getMessage(), con);
                try {
                    setState(ServiceState.FAILED);
                } catch (ExecutionException e) {
                    throw new IOException(e);
                }
            }
        }
    }
    
    @Override
    protected void stop() {
        if (null != connection) {
            connection.disconnect();
            connection = null;
        }
        super.stop();
    }
    
    @Override
    public void process(I data) throws IOException {
        // unclear whether obtaining/sending shall only happen if service is running.
        obtainConnection(true);
        OutputStream os = connection.getOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
        String input = "{\"items\":[" + getInputTranslator().to(data) + "]}";
        osw.write(input);
        osw.flush();
        osw.close();
        os.close();

        // TODO This is now forced synchronous processing. We need this as a parallel thread starting in 
        // obtainConnection if successful.
        String result;
        BufferedInputStream bis = new BufferedInputStream(connection.getInputStream());
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        int read = bis.read();
        while (read != -1) {
            buf.write((byte) read);
            read = bis.read();
        }
        result = buf.toString();
        bis.close();
        buf.close();
        result = result.replace("{\"data\":{\"errors\":[],\"items\":[", "");
        result = result.replace("],\"messages\":[],\"warnings\":[]}}", "");
        
        ReceptionCallback<O> callback = getReceptionCallback();
        try {
            callback.received(getOutputTranslator().to(result));
        } catch (IOException e) {
            LoggerFactory.getLogger(getClass()).error("Receiving result: " + e.getMessage());
        }
    }
    
    @Override
    protected void start() throws ExecutionException {
        String executable = getExecutableName("kodex", VERSION);
        YamlProcess sSpec = getProcessSpec();

        File exe = selectNotNull(sSpec, s -> s.getExecutablePath(), new File("./src/main/resources/")); 
        File home = selectNotNull(sSpec, s -> s.getHomePath(), new File("./src/test/resources"));
        exe = new File(exe, executable); 
        home = home.getAbsoluteFile();
        
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
        boolean portAvailable = false;
        while (!portAvailable) {
            try {
                obtainConnection(false); // quiet, check whether connection exists
                if (connection.getResponseCode() == 400) {
                    portAvailable = true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            TimeUtils.sleep(100);
        }
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
    
}
