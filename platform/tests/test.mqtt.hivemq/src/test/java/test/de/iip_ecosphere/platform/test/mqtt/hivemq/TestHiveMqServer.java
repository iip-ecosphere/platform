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
package test.de.iip_ecosphere.platform.test.mqtt.hivemq;

import java.io.File;

import com.hivemq.embedded.EmbeddedHiveMQ;
import com.hivemq.embedded.EmbeddedHiveMQBuilder;

import de.iip_ecosphere.platform.support.FileUtils;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.setup.CmdLine;
import test.de.iip_ecosphere.platform.transport.AbstractTestServer;

/**
 * A simple embedded HiveMQ/MQTT test server for testing/experiments. This class requires Java 11.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TestHiveMqServer extends AbstractTestServer {
    
    public static final String KEYSTORE_PASSWORD = "a1234567";
    public static final String TRUSTSTORE_PASSWORD = KEYSTORE_PASSWORD;
    public static final String KEY_ALIAS = "qpid";
    private EmbeddedHiveMQ hiveMQ;
    private ServerAddress addr;

    /**
     * Creates the server instance.
     * 
     * @param addr the server address (schema is ignored)
     */
    public TestHiveMqServer(ServerAddress addr) {
        this.addr = addr;
    }
    
    @Override
    public Server start() {
        if (null == hiveMQ) {
            FileUtils.listFiles(FileUtils.getTempDirectory(), 
                f -> f.getName().startsWith("hivemq_v5"), 
                f -> FileUtils.deleteQuietly(f)); // try to clean up left-over temp folders
            File hiveTmp = FileUtils.createTmpFolder("hivemq_v5", true);

            System.setProperty("HIVEMQ_PORT", Integer.toString(addr.getPort()));
            System.setProperty("HIVEMQ_ADDRESS", addr.getHost());
            System.setProperty("hivemq.log.folder", hiveTmp.getAbsolutePath());
            System.setProperty("hivemq.data.folder", hiveTmp.getAbsolutePath()); // sometimes below fails
            
            File cfg = getConfigDir("./src/test");
            System.setProperty("HIVEMQ_CFG", cfg.getAbsolutePath());
            final EmbeddedHiveMQBuilder embeddedHiveMQBuilder = EmbeddedHiveMQBuilder.builder()
                .withConfigurationFolder(cfg.toPath())
                .withDataFolder(hiveTmp.toPath())
                .withExtensionsFolder(new File(cfg, "extensions").toPath());
            hiveMQ = embeddedHiveMQBuilder.build();
            hiveMQ.start().join();
        }
        return this;
    }
    
    @Override
    public void stop(boolean dispose) {
        if (null != hiveMQ) {
            hiveMQ.stop().join();
            hiveMQ = null;
        }
    }
    
    /**
     * Determins the server address from the command line.
     * 
     * @param args the first argument may be the port number, else 8883 is used; 
     *     optionally --host=<name> may be given afterwards
     * @return the server address
     */
    protected static ServerAddress getServerAddress(String[] args) {
        return new ServerAddress(Schema.IGNORE, 
            CmdLine.getArg(args, "host", ServerAddress.LOCALHOST), 
            getInteger(args, 8883));
    }
    
    /**
     * Starts the server from the command line.
     * 
     * @param args the first argument may be the port number, else 8883 is used; 
     *     optionally --host=<name> may be given afterwards
     */
    public static void main(String[] args) {
        TestHiveMqServer server = new TestHiveMqServer(getServerAddress(args));
        server.start();
    }

}
