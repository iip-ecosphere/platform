/**
 * ******************************************************************************
 * Copyright (c) {2025} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.transport;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.plugins.DefaultPluginDescriptor;
import de.iip_ecosphere.platform.support.plugins.FolderClasspathPluginSetupDescriptor;
import de.iip_ecosphere.platform.support.plugins.StreamGobbler;
import test.de.iip_ecosphere.platform.transport.TestServerBuilder.InstanceCreator;


/**
 * A test server plugin descriptor. May create JVM process to run the server within. Last resort if dependencies are 
 * massively conflicting.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TestServerPluginDescriptor extends DefaultPluginDescriptor<TestServerBuilder> {

    /**
     * Creates a descriptor instance.
     * 
     * @param id the plugin id
     * @param instanceCreator an instance creator to be passed into a {@link JvmTestServer}
     */
    public TestServerPluginDescriptor(String id, InstanceCreator instanceCreator) {
        super(id, null, TestServerBuilder.class, 
            p -> new TestServerBuilder(id, instanceCreator, p.getInstallDir()));
    }

    /**
     * Supplies whether a {@link JvmTestServer} is up and running.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface ProcessUpSupplier {

        /**
         * Primary interface, based on system output of the process (yet not error output).
         * 
         * @param output the output line
         * @return {@code true} for process is up and running, {@code false} else
         */
        public boolean isRunning(String output);
        
        /**
         * Secondary interface, some other output independent test, e.g., based on network availability.
         * 
         * @return {@code true} for process is up and running, {@code false} else
         */
        public default boolean isRunning() {
            return false;
        }
        
    }
    
    /**
     * A JVM-based test server, i.e., a wrapping server process implemented through an own JVM process. By convention,
     * passes the port number as first process argument.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class JvmTestServer extends AbstractTestServer {

        private String id;
        private String cls;
        private Process proc;
        private ServerAddress address;
        private File installDir;
        private ProcessUpSupplier checker;
        private int upTimeout = 3000;
        
        /**
         * Creates the server instance.
         * 
         * @param id the plugin id
         * @param cls the class to start (better string to avoid class loading)
         * @param address the server address to supply to the server instance
         * @param installDir the installation directory of the plugin, may be <b>null</b>
         * @param checker the process up checker
         */
        public JvmTestServer(String id, String cls, ServerAddress address, File installDir, ProcessUpSupplier checker) {
            this.id = id;
            this.cls = cls;
            this.address = address;
            this.installDir = installDir;
            this.checker = checker;
        }
        
        /**
         * Changes the timeout to wait for the JVM to be up. 
         * 
         * @param upTimeout the timeout, disables waiting if negativ, shall be larger than 200 (else set to 250)
         * @return <b>this</b> for chaining
         */
        public JvmTestServer setUpTimeout(int upTimeout) {
            this.upTimeout = upTimeout;
            if (this.upTimeout > 0 && this.upTimeout < 200) {
                this.upTimeout = 250; // -> sleepMs below
            }
            return this;
        }
        
        @Override
        public Server start() {
            AtomicBoolean started = new AtomicBoolean(false);
            try {
                File java = new File(System.getProperty("java.home") + "/bin/java");
                File instDir = installDir == null ? new File("") : installDir;  
                instDir = instDir.getCanonicalFile();
                File cp = FolderClasspathPluginSetupDescriptor.findClasspathFile(instDir, "-win"); // TODO
                List<String> args = new ArrayList<>();
                args.add(java.getAbsolutePath());
                args.add("-cp");
                args.add("@" + cp.getAbsolutePath());
                args.add(cls);
                args.add(String.valueOf(address.getPort()));
                args.add(new File("").getAbsolutePath()); // shall be user.dir
                ProcessBuilder b = new ProcessBuilder(args);
                b.directory(instDir.getAbsoluteFile());
                LoggerFactory.getLogger(getClass()).info("Spawning process for plugin {} with arguments {} "
                    + "in directory {}", id, String.join(" ", args), instDir);
                proc = b.start();
                StreamGobbler.attach(proc, s -> started.set(checker.isRunning(s)), null);
                if (upTimeout > 0) {
                    TimeUtils.waitFor(() -> !started.get() && !checker.isRunning(), upTimeout, 200);
                }
            } catch (IOException e) {
                LoggerFactory.getLogger(getClass()).error("Cannot start {}: {}", cls, e.getMessage());
            }
            return this;
        }
        
        @Override
        public void stop(boolean dispose) {
            if (null != proc) {
                proc.destroyForcibly();
            }
        }
        
    }

}
