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

package de.iip_ecosphere.platform.support.iip_aas;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.function.Supplier;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.LifecycleDescriptor;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry.AasMode;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry.AasSetup;
import de.iip_ecosphere.platform.support.setup.CmdLine;

/**
 * Implements the generic lifecycle descriptor for the service manager.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AbstractAasLifecycleDescriptor implements LifecycleDescriptor {

    /**
     * Explicitly determine the AAS implementation server port. If not given, use an ephemeral one.
     */
    public static final String PARAM_IIP_PORT = "iip.port";
    
    private static Server implServer; // static if multiple ones share the same, e.g., ecs/svcMgr
    private static ProtocolServerBuilder implServerBuilder;
    private String name;
    private Supplier<AasSetup> setupSupplier;
    private Server aasServer;
    
    /**
     * Creates a descriptor instance.
     * 
     * @param name the name of the AAS to build for logging
     * @param setupSupplier the {@link AasSetup} supplier
     */
    protected AbstractAasLifecycleDescriptor(String name, Supplier<AasSetup> setupSupplier) {
        this.name = name;
        this.setupSupplier = setupSupplier;
    }
    
    /**
     * Returns the AAS setup via the passed in setup supplier.
     * 
     * @return the actual AAS setup
     */
    public AasSetup getAasSetup() {
        return setupSupplier.get();
    }
    
    /**
     * Returns the name of a command line/system environment parameter overriding {@link #PARAM_IIP_PORT}.
     * 
     * @return the name of the parameter, <b>null</b> for none (default)
     */
    protected String getOverridePortArg() {
        return null;
    }
    
    /**
     * Returns the port value, either from args or from the system environment.
     * 
     * @param args the command line arguments
     * @param arg the parameter name to search for
     * @param init the initial value, a already known port if chained, usually {@code -1}
     * @return the port, may be {@code -1} for none
     */
    private int getPort(String[] args, String arg, int init) {
        int port = init;
        if (null != arg && port < 0) {
            port = CmdLine.getIntArg(args, arg, -1);
            if (port < 0 && System.getenv(arg) != null) {
                try {
                    port = Integer.parseInt(System.getenv(arg));
                } catch (NumberFormatException e) {
                    // ignore
                }
            }
        }
        return port;
    }
    
    @Override
    public void startup(String[] args) {
        int port = getPort(args, PARAM_IIP_PORT, getPort(args, getOverridePortArg(), -1));
        if (port > 0) {
            setupSupplier.get().getImplementation().setPort(port);
            LoggerFactory.getLogger(getClass()).info("Using port " + port + " for the AAS implementation server.");
        }
        if (AasFactory.isFullInstance()) {
            AasSetup setup = getAasSetup();
            AasPartRegistry.setAasSetup(setup);
            waitForAasServer();
            // startImplServer=true due to incremental deployment; chain implServerBuilders
            AasPartRegistry.AasBuildResult res = AasPartRegistry.build(
                c -> true, null == implServer, implServerBuilder);
            if (null == implServer) {
                implServerBuilder = res.getProtocolServerBuilder();
                implServer = res.getProtocolServer();
            }

            boolean success = true;
            if (AasMode.REGISTER == setup.getMode()) {
                try {
                    aasServer = AasPartRegistry.register(res.getAas(), setup.getRegistryEndpoint()); 
                    aasServer.start();
                } catch (IOException e) {
                    LoggerFactory.getLogger(getClass()).error("Cannot register AAS " + name + " with " 
                        + setup.getRegistryEndpoint().toUri() + ":" + e.getMessage());
                    success = false;
                }
            } else {
                try {
                    AasPartRegistry.remoteDeploy(res.getAas());
                } catch (IOException e) {
                    LoggerFactory.getLogger(getClass()).error("Cannot deploy AAS " + name + ": " + e.getMessage());
                    success = false;
                }
            }
            if (success) {
                AasPartRegistry.setAasSupplier(() -> res.getAas());
            }
        } else {
            LoggerFactory.getLogger(getClass()).warn("No full AAS implementation registered. Cannot build up {} AAS. "
                + "Please add an appropriate dependency.", name);
        }
    }

    /**
     * Waits for the AAS server to come up.
     */
    protected void waitForAasServer() {
        AasFactory factory = AasFactory.getInstance();
        AasSetup setup = AasPartRegistry.getSetup();
        String regUri = factory.getFullRegistryUri(setup.getRegistryEndpoint());
        int startupTimeout = setup.getAasStartupTimeout();
        try {
            URL url = new URL(regUri);
            LoggerFactory.getLogger(getClass()).info("Probing AAS registry {} for {} ms", regUri, startupTimeout);
            if (!TimeUtils.waitFor(() -> {
                boolean continueWaiting = true;
                try { // initial, incomplete, move to AasFactory?
                    HttpURLConnection huc = (HttpURLConnection) url.openConnection();
                    int responseCode = huc.getResponseCode();
                    continueWaiting = responseCode != HttpURLConnection.HTTP_OK;
                } catch (IOException e) {
                    // ignore
                }
                return continueWaiting;
            }, startupTimeout, 500)) {
                LoggerFactory.getLogger(getClass()).error("No AAS registry/server reached within {} ms", 
                    startupTimeout);
            } else {
                LoggerFactory.getLogger(getClass()).info("AAS registry/server found for {}", regUri);
            }
        } catch (MalformedURLException e) {
            LoggerFactory.getLogger(getClass()).warn("Cannot wait for AAS registry/server. AAS registry URL "
                + "{} invalid: {}", regUri, e.getMessage());
        }
    }

    @Override
    public void shutdown() {
        if (null != implServer) {
            implServer.stop(true);
        }
        if (null != aasServer) {
            implServer.stop(true);
        }
    }

    @Override
    public Thread getShutdownHook() {
        return null;
    }
    
    @Override
    public int priority() {
        return AAS_PRIORITY;
    }

}
