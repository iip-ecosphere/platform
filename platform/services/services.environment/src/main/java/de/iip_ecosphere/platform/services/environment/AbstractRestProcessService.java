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

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import de.iip_ecosphere.platform.transport.serialization.TypeTranslator;

/**
 * Implements an abstract asynchronous process-based service which require a Rest-based communication with
 * the actual service process for a single pair of input-output types, e.g., via JSON. Call 
 * {@link #setupConnectionManager()} at the end of your {@link #start()} method implementation.
 * 
 * @author Marcel N�hre
 *
 * @param <I> the input data type
 * @param <O> the output data type
 */
public abstract class AbstractRestProcessService<I, O> extends AbstractProcessService<I, String, String, O> {
    
    private HttpURLConnection connection;
    private ExecutorService executor = Executors.newFixedThreadPool(5);
    private CloseableHttpClient client;

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
    protected abstract String getApiPath();
    
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
     * Sets up the connection manager and creates a new httpClient based on a connection pool.
     */
    protected void setupConnectionManager() {
        HttpClientConnectionManager poolingConnectionManager = new PoolingHttpClientConnectionManager();
        client = HttpClients.custom().setConnectionManager(poolingConnectionManager).build();
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

    /**
     * Waits until a connection is established.
     */
    protected void waitForConnection() {
        boolean portAvailable = false;
        while (!portAvailable) {
            try {
                getNewConnectionInstanceQuiet(false); // quiet, check whether connection exists
                if (getConnection().getResponseCode() == 400) {
                    portAvailable = true;
                }
            } catch (IOException e) {
                // be quiet, checking connections
            }
            TimeUtils.sleep(100);
        }
    }
    
    @Override
    public void process(I data) throws IOException {
        process(data, "");
    }

    /**
     * Processes input.
     * 
     * @param data the data to be processed
     * @param inTypeName the symbolic input type name, may be empty if just one is handled
     * @throws IOException if processing fails
     */
    public void process(I data, String inTypeName) throws IOException {
        executor.execute(new Runnable() {
            public void run() {
                try {
                    HttpPost post = new HttpPost(getApiPath());
                    String bearer = getBearerToken();
                    String input = toSendString(data, inTypeName);
                    StringEntity entity = new StringEntity(input);
                    post.setEntity(entity);
                    post.setHeader("Accept", "application/json");
                    post.setHeader("Content-type", "application/json");
                    post.setHeader("Authorization", bearer);
                    if (client != null) {
                        CloseableHttpResponse response = client.execute(post);
                        String output = EntityUtils.toString(response.getEntity());
                        handleReception(output);
                    } else {
                        LoggerFactory.getLogger(getClass()).info("Connection not yet open. Cannot process data.");
                    }
                } catch (IOException e1) {
                    LoggerFactory.getLogger(getClass()).error("Receiving result: {}", e1.getMessage());
                }
            }
        });
    }

    /**
     * Turns {@code data} to an input string of the underlying REST service.
     * 
     * @param data the data
     * @param inTypeName the symbolic input type name, may be empty if just one is handled
     * @return the translated input string, by default via {@link #adjustRestQuery(String, String)} 
     *    and {@link #getInputTranslator()}
     * @throws IOException if processing the input fails
     */
    protected String toSendString(I data, String inTypeName) throws IOException {
        return adjustRestQuery(getInputTranslator().to(data), inTypeName);
    }
    
    /**
     * Handles the reception of {@code data}. By default, applies {@link #adjustRestResponse(String)}, 
     * {@link #getOutputTranslator()} and {@link #notifyCallbacks(Object)}.
     * 
     * @param data the information received from the underlying REST service
     */
    protected void handleReception(String data) {
        String result = adjustRestResponse(data);
        try {
            notifyCallbacks(getOutputTranslator().to(result));
        } catch (IOException e) {
            LoggerFactory.getLogger(getClass()).error("Receiving result: {}", e.getMessage());
        }
    }

    /**
     * Adjusts the input produced by {@link #getInputTranslator()} to the actual receiver.
     *  
     * @param input the input
     * @param inTypeName the symbolic input type name, may be empty if just one is handled
     * @return the adjusted input
     */
    protected abstract String adjustRestQuery(String input, String inTypeName);
    
    /**
     * The rest response.
     * 
     * @param response the received response
     * @return the adjusted response
     */
    protected abstract String adjustRestResponse(String response);

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
