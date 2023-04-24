/**
 * ******************************************************************************
 * Copyright (c) {2023} The original author or authors
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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.iip_ecosphere.platform.services.environment.GenericMultiTypeServiceImpl.OutTypeInfo;
import de.iip_ecosphere.platform.support.PythonUtils;
import de.iip_ecosphere.platform.support.iip_aas.json.JsonUtils;
import de.iip_ecosphere.platform.support.net.NetworkManagerFactory;

/**
 * Generic WebSocket-based Python integration for asynchronous processing of multiple data types. Conventions:
 * <ul>
 *   <li>Python is determined by {@link PythonUtils#getPythonExecutable()}. The default is "ServiceEnvironment.py" 
 *       which must run for this integration with "--mode WS" and given port.</li>
 *   <li>The Python program runs endless until stopped by this class.</li>
 *   <li>An asynchronous Python program receives the data via command line input streams based on the input serializer 
 *       and the symbolic type name.</li>
 * </ul>
 * 
 * @author Holger Eichelberger, SSE
 */
public class PythonWsProcessService extends PythonAsyncProcessService {

    private int instancePort;
    private long averageResponseTime = 0;
    private WebSocket socket;
    private String networkPortKey;

    /**
     * Creates an instance from a service id and a YAML artifact.
     * 
     * @param serviceId the service id
     * @param ymlFile the YML file containing the YAML artifact with the service descriptor
     */
    public PythonWsProcessService(String serviceId, InputStream ymlFile) {
        super(serviceId, ymlFile);
    }
    
    /**
     * Creates an instance of the service.
     * 
     * @param yaml the service description
     */
    public PythonWsProcessService(YamlService yaml) {
        super(yaml);
    }
    
    @Override
    protected void initializeFrom(YamlService yaml) {
        networkPortKey = "python_" + Starter.getServiceId(yaml.getId());
        instancePort = NetworkManagerFactory.getInstance().obtainPort(networkPortKey).getPort();
        super.initializeFrom(yaml);
    }

    @Override
    protected void customizePythonArgs(List<String> pythonArgs) {
        int portIndex = -1;
        int modeIndex = -1;
        for (int i = 0; i < pythonArgs.size(); i++) {
            if (i + 1 < pythonArgs.size()) { // has value
                if (pythonArgs.get(i).equals("--port")) {
                    portIndex = i + 1;
                } else if (pythonArgs.get(i).equals("--mode")) {
                    modeIndex = i + 1;
                }
            }
        }
        if (modeIndex < 0) {
            pythonArgs.add("--mode");
            pythonArgs.add("");
            portIndex = pythonArgs.size() - 1;
        }
        if (portIndex < 0) {
            pythonArgs.add("--port");
            pythonArgs.add("");
            portIndex = pythonArgs.size() - 1;
        }
        pythonArgs.set(portIndex, String.valueOf(instancePort));
        pythonArgs.set(modeIndex, "WS");
    }
    
    @Override
    protected void createScanInputThread(Process proc) {
        try {
            socket = new WebSocket(new URI("ws://localhost:" + instancePort));
            socket.connectBlocking();
        } catch (URISyntaxException | InterruptedException e) {
            getLogger().error("Connecting to port {}: {}", instancePort, e.getMessage());
        } 
    }
    
    @Override
    protected ServiceState stop() {
        if (null != networkPortKey) {
            NetworkManagerFactory.getInstance().releasePort(networkPortKey);
            networkPortKey = null;
        }
        socket.close();
        return super.stop();
    }

    @Override
    protected void sendToService(String type, Object data) throws ExecutionException {
        if (null != socket) {
            try {
                InData tmp = new InData(type, data);
                socket.send(JsonUtils.toJson(tmp));
            } catch (WebsocketNotConnectedException e) {
                // in shutdown?
                if (getState() != ServiceState.STOPPING && getState() != ServiceState.STOPPED) {
                    throw new ExecutionException(e);
                }
            }
        } // not connected, cache?
    }

    /**
     * Returns the logger.
     * 
     * @return the logger
     */
    protected static Logger getLogger() {
        return LoggerFactory.getLogger(PythonWsProcessService.class);
    }
    
    @Override
    public long getAvgResponseTime() {
        return averageResponseTime;
    }
    
    /**
     * Websocket client implementation.
     * 
     * @author Holger Eichelberger, SSE
     */
    private class WebSocket extends WebSocketClient {

        /**
         * Creates a web socket for the given server URI.
         * 
         * @param serverUri the server URI
         */
        public WebSocket(URI serverUri) {
            super(serverUri);
        }

        /**
         * Creates a web socket for the given server URI.
         * 
         * @param serverUri the server URI
         * @param httpHeaders the headers to use
         */
        public WebSocket(URI serverUri, Map<String, String> httpHeaders) {
            super(serverUri, httpHeaders);
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
        }

        @Override
        public void onMessage(String message) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                OutData data = objectMapper.readValue(message, OutData.class);
                OutTypeInfo<?> info = getOutTypeInfo(data.getType());
                info.validateAndIngest(data.getType(), data.getData());
                averageResponseTime = data.getTime();
            } catch (IOException e) {
                getLogger().error("While ingesting result data: {}", e.getMessage());
            }
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            if (remote) {
                getLogger().info("Connection closed by remote peer, code: {} reason: {}", code, reason);
            }
        }

        @Override
        public void onError(Exception ex) {
            getLogger().error("While running Python: {}", ex.getMessage());
        }

    }

}

/**
 * Represents wrapped input data towards Python. Fields are only used for/in JSON serialization.
 * 
 * @author Holger Eichelberger, SSE
 */
class InData {
    
    private String type;
    private Object data;
    
    /**
     * Creates an input data instance.
     * 
     * @param type the symbolic type name
     * @param data the data
     */
    InData(String type, Object data) {
        this.type = type;
        this.data = data;
    }

    /**
     * Returns the type. [public for JSON]
     * 
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Returns the data. [public for JSON]
     * 
     * @return the data
     */
    public Object getData() {
        return data;
    }
    
}

/**
 * Represents wrapped output data from Python.
 * 
 * @author Holger Eichelberger, SSE
 */
class OutData {
    
    private String type;
    private String data;
    private long time;

    /**
     * Returns the symbolic data type name.
     * 
     * @return the type name
     */
    String getType() {
        return type;
    }
    
    /**
     * The payload data in JSON format.
     * 
     * @return the payload data
     */
    String getData() {
        return data;
    }
    
    /**
     * Returns the time taken for processing in Python.
     * 
     * @return the time in ms
     */
    long getTime() {
        return time;
    }

    /**
     * Changes the type. [public for JSON]
     * 
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Changes the data.  [public for JSON]
     * 
     * @param data the data to set
     */
    public void setData(String data) {
        this.data = data;
    }

    /**
     * Changes the processing time.  [public for JSON]
     * 
     * @param time the time to set
     */
    public void setTime(long time) {
        this.time = time;
    }
    
}

