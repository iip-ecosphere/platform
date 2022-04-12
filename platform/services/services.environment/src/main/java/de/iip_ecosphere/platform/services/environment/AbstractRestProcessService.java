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

package de.iip_ecosphere.platform.services.environment;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import de.iip_ecosphere.platform.transport.serialization.TypeTranslator;

/**
 * Implements an abstract asynchronous process-based service which require a Rest-based communication with
 * the actual service process for a single pair of input-output types, e.g., via JSON.
 * 
 * @author Marcel Nöhre
 *
 * @param <I> the input data type
 * @param <O> the output data type
 */
public abstract class AbstractRestProcessService<I, O> extends AbstractProcessService<I, String, String, O> {
    
    private String apiPath;
    private HttpURLConnection connection;
    private ExecutorService executor = Executors.newFixedThreadPool(5);

    /**
     * Creates an instance of the service with the required type translators.
     * 
     * @param inTrans the input translator
     * @param outTrans the output translator
     * @param callback called when data from the service is available
     * @param yaml the service description 
     */
    protected AbstractRestProcessService(TypeTranslator<I, String> inTrans, TypeTranslator<String, O> outTrans,
        ReceptionCallback<O> callback, YamlService yaml) {
        super(inTrans, outTrans, callback, yaml);
    }
    
    /**
     * Returns the HTTP/HTTPS path to the REST API.
     * 
     * @return the path
     */
    protected String getApiPath() {
        apiPath = "http://localhost:8000/v1/configs/abcdef/transform";
        return apiPath;
    }
    
    /**
     * Returns the connection instance.
     * 
     * @return the connection instance, may be <b>null</b> if service was not started before
     */
    protected HttpURLConnection getConnection() {
        return connection;
    }
    
    /**
     * Returns the bearer token for authentication.
     * 
     * @return the bearer token, may be <b>null</b> for none
     */
    protected abstract String getBearerToken();
    
    /**
     * Creates a new connection and overwrites the existing connection instance.
     * 
     * @return the created connection
     * @throws IOException if creating the connection fails
     */
    protected HttpURLConnection getNewConnectionInstance() throws IOException {
        URL url = new URL(getApiPath());
        connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        String bearer = getBearerToken();
        if (null != bearer) {
            connection.setRequestProperty("Authorization", bearer);
        }
        connection.connect();
        return connection;
    }
    
    /**
     * Get Connection to local server.
     * 
     * @param changeState whether the state shall be changed if the connection creation fails
     */
    protected void getNewConnectionInstanceQuiet(boolean changeState) {
        try {
            getNewConnectionInstance();
        } catch (IOException con) {
            LoggerFactory.getLogger(AbstractRestProcessService.class).warn(con.getMessage() + " " + getApiPath());
            if (changeState) {
                try {
                    setState(ServiceState.FAILED);
                } catch (ExecutionException e) {
                    LoggerFactory.getLogger(AbstractRestProcessService.class).error(e.getMessage(), e);
                }
            }
        }
    }
    
    @Override
    public void process(I data) throws IOException {
        connection = getNewConnectionInstance();
        OutputStream os = connection.getOutputStream();
        OutputStreamWriter osw;
        osw = new OutputStreamWriter(os, "UTF-8");
        String input = adjustRestQuery(getInputTranslator().to(data));
        osw.write(input);
        osw.flush();
        redirectRest(connection, getReceptionCallback());
    }
    
    /**
     * Adjusts the input produced by {@link #getInputTranslator()} to the actual receiver.
     *  
     * @param input the input
     * @return the adjusted input
     */
    protected abstract String adjustRestQuery(String input);
    
    /**
     * The rest response.
     * 
     * @param response the received response
     * @return the adjusted response
     */
    protected abstract String adjustRestResponse(String response);
    
    /**
     * Redirects rest answers to the reception callback.
     * 
     * @param connection the connection to redirect
     * @param callback the callback to use
     * @see #adjustRestResponse(String)
     */
    public void redirectRest(final HttpURLConnection connection, ReceptionCallback<O> callback) {
        if (null != callback) {
            executor.execute(new Runnable() {
                public void run() {
                    try {
                        BufferedInputStream bis = new BufferedInputStream(connection.getInputStream());
                        ByteArrayOutputStream buf = new ByteArrayOutputStream();
                        String result;
                        int read;
                        read = bis.read();
                        while (read != -1) {
                            buf.write((byte) read);
                            read = bis.read();
                        }
                        result = adjustRestResponse(buf.toString());
                        try {
                            callback.received(getOutputTranslator().to(result));
                            connection.disconnect();
                        } catch (IOException e) {
                            LoggerFactory.getLogger(getClass()).error("Receiving result: " + e.getMessage());
                            connection.disconnect();
                        }
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            });
        }
    }

    @Override
    protected void handleInputStream(InputStream in) {
        // not needed
    }
    
    @Override
    protected ServiceState stop() {
        if (null != connection) {
            connection.disconnect();
            connection = null;
        }
        return super.stop();
    }
    
}
