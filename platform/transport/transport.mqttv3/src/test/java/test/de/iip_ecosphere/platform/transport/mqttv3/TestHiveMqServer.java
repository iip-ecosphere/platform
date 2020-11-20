/********************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/
package test.de.iip_ecosphere.platform.transport.mqttv3;

import java.io.File;

import org.apache.commons.io.FileUtils;

import com.hivemq.embedded.EmbeddedHiveMQ;
import com.hivemq.embedded.EmbeddedHiveMQBuilder;

/**
 * A simple embedded HiveMQ test server for MQTT.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TestHiveMqServer {
    
    private EmbeddedHiveMQ hiveMQ;

    /**
     * Starts the server.
     * 
     * @param host the host name
     * @param port the port number
     */
    public void start(String host, int port) {
        if (null == hiveMQ) {
            String tmp = System.getProperty("java.io.tmpdir");
            File hiveTmp = new File(tmp, "hivemq");
            FileUtils.deleteQuietly(hiveTmp);
            hiveTmp.mkdir();

            System.setProperty("HIVEMQ_PORT", Integer.toString(port));
            System.setProperty("HIVEMQ_ADDRESS", host);
            System.setProperty("hivemq.log.folder", hiveTmp.getAbsolutePath());
            
            File cfg = new File("./src/test");
            final EmbeddedHiveMQBuilder embeddedHiveMQBuilder = EmbeddedHiveMQBuilder.builder()
                .withConfigurationFolder(cfg.toPath())
                .withDataFolder(hiveTmp.toPath())
                .withExtensionsFolder(new File(cfg, "extensions").toPath());
    
            hiveMQ = embeddedHiveMQBuilder.build();
            hiveMQ.start().join();
        }
    }
    
    /**
     * Stops the server.
     */
    public void stop() {
        hiveMQ.stop().join();
        hiveMQ = null;
    }

}
