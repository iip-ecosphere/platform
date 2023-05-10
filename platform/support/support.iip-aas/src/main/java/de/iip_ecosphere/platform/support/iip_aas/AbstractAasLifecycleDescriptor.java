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
import java.net.URLConnection;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Predicate;
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

    private static final int AAS_HEARTBEAT_PERIOD = 5000; 
    private static Server implServer; // static if multiple ones share the same, e.g., ecs/svcMgr
    private static ProtocolServerBuilder implServerBuilder;
    private static boolean waitForIipAas = true;
    private String name;
    private Supplier<AasSetup> setupSupplier;
    private Server aasServer;
    private Timer timer;
    
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
     * Defines whether we shall wait for the IIP AAs to come up. [testing]
     * 
     * @param wait {@code true} for waiting, {@code false} for not waiting
     * @return the previous value
     */
    public static boolean setWaitForIipAas(boolean wait) {
        boolean old = waitForIipAas;
        waitForIipAas = wait;
        return old;
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
    private static int getPort(String[] args, String arg, int init) {
        int port = init;
        if (null != arg && port < 0) {
            port = CmdLine.getIntArg(args, arg, -1);
            if (port < 0 && getenv(arg) != null) {
                try {
                    port = Integer.parseInt(getenv(arg));
                } catch (NumberFormatException e) {
                    // ignore
                }
            }
        }
        return port;
    }
    
    /**
     * Returns a value from the system environment, either as given or all in capital characters with dots replaced 
     * by underscores.
     * 
     * @param key the key to look for
     * @return the value, may by <b>null</b> for none
     */
    private static String getenv(String key) {
        String result = System.getenv(key); 
        if (null == result) { // particular for linux
            result = System.getenv(key.toUpperCase().replace('.', '_'));
        }
        return result;
    }
    
    /**
     * Allows to dynamically filter the contributors. May rely on {@link AasPartRegistry#contributors()} or 
     * on {@link AasPartRegistry#contributorClasses()}.
     * 
     * @return the filter, by default a function that returns {@code true}
     */
    protected Predicate<AasContributor> getContributorFilter() {
        return c -> true;
    }
    
    @Override
    public void startup(String[] args) {
        LoggerFactory.getLogger(getClass()).info("System environment: {}", System.getenv());
        deploy(args, getContributorFilter());
        if (null == timer && enableAasHeartbeat()) {
            AasFactory factory = AasFactory.getInstance();
            AasSetup setup = AasPartRegistry.getSetup();
            String serverAdr = factory.getServerBaseUri(setup.getServerEndpoint());
            try {
                final URL serverUrl = new URL(serverAdr);
                timer = new Timer(true);
                timer.schedule(new TimerTask() {
                    
                    private boolean offline = false;
    
                    @Override
                    public void run() {
                        boolean hasConn = connectionOk(serverUrl) && iipAasExists();
                        if (offline) {
                            if (hasConn) {
                                offline = false;
                                LoggerFactory.getLogger(getClass()).warn("AAS server {} back. Re-deploying AAS.", 
                                    serverUrl);
                                // AAS contributors shall consider implement exists() 
                                deploy(args, c ->  !c.exists() && getContributorFilter().test(c));
                                LoggerFactory.getLogger(getClass()).warn("AAS server {} back. AAS re-deployed.", 
                                    serverUrl);
                            }
                        } else { // online
                            if (!hasConn) {
                                LoggerFactory.getLogger(getClass()).warn("AAS server {} offline", serverUrl);
                                offline = true; // after multiple trials?
                            }
                        }
                    }
                }, AAS_HEARTBEAT_PERIOD, AAS_HEARTBEAT_PERIOD);
            } catch (MalformedURLException e) {
                LoggerFactory.getLogger(getClass()).warn("Cannot heartbeat for AAS registry/server. AAS server URL "
                    + "{} invalid: {}", serverAdr, e.getMessage());
            }
        }
    }

    /**
     * Deploys the AAS via {@link AasPartRegistry#build()} and depending on the {@link AasMode} 
     * {@link AasPartRegistry#register(java.util.List, de.iip_ecosphere.platform.support.Endpoint, String...)} or
     * {@link AasPartRegistry#remoteDeploy(java.util.List)}.
     * 
     * @param args the command line arguments from {@link #startup(String[])}
     * @param contributorFilter the AAS contributor filter to apply
     */
    private void deploy(String[] args, Predicate<AasContributor> contributorFilter) {
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
                contributorFilter, null == implServer, implServerBuilder);
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
     * Returns whether AAS server heartbeat shall be enabled.
     * 
     * @return {@code true} for enabled, {@code false} else
     */
    protected boolean enableAasHeartbeat() {
        return true;
    }

    /**
     * Waits for the AAS server to come up.
     */
    protected void waitForAasServer() {
        AasFactory factory = AasFactory.getInstance();
        AasSetup setup = AasPartRegistry.getSetup();
        String regAdr = factory.getFullRegistryUri(setup.getRegistryEndpoint());
        String serverAdr = factory.getServerBaseUri(setup.getServerEndpoint());
        int startupTimeout = setup.getAasStartupTimeout();
        try { 
            URL regUrl = new URL(regAdr);
            URL serverUrl = new URL(serverAdr);
            LoggerFactory.getLogger(getClass()).info("Probing AAS registry {} and server{} for {} ms", 
                regAdr, serverAdr, startupTimeout);
            if (!TimeUtils.waitFor(() -> {
                return !connectionOk(regUrl) || !connectionOk(serverUrl) || !iipAasExists();
            }, startupTimeout, 500)) {
                LoggerFactory.getLogger(getClass()).error("No AAS registry/server reached within {} ms", 
                    startupTimeout);
            } else {
                LoggerFactory.getLogger(getClass()).info("AAS registry found at {} and server at {}", 
                    regAdr, serverAdr);
            }
        } catch (MalformedURLException e) {
            LoggerFactory.getLogger(getClass()).warn("Cannot wait for AAS registry/server. AAS registry "
                + "{} or server {} URL invalid: {}", regAdr, serverAdr, e.getMessage());
        }
    }
    
    /**
     * Returns whether connecting to {@code url} succeeeds.
     * 
     * @param url the URK to connect to
     * @return {@code true} if the connection is ok
     */
    private static boolean connectionOk(URL url) {
        boolean connectionOk = false; // cannot move to NetUtils as difficult to test/mock
        try { 
            URLConnection conn = url.openConnection();
            if (conn instanceof  HttpURLConnection) {
                HttpURLConnection huc = (HttpURLConnection) url.openConnection();
                int responseCode = huc.getResponseCode();
                connectionOk = responseCode == HttpURLConnection.HTTP_OK;
                huc.disconnect();
            }
        } catch (IOException e) {
            // ignore, connectionOk == false
        }
        return connectionOk;
    }
    
    /**
     * Returns whether we have an IIP AAS.
     * 
     * @return {@code true} the AAS exists, {@code false} else
     */
    protected boolean iipAasExists() {
        boolean exists;
        if (waitForIipAas) {
            try {
                exists = null != AasPartRegistry.retrieveIipAas();
            } catch (IOException e) {
                // ignore
                exists = false;
            }
        } else {
            exists = true; // do not wait
        }
        return exists;
    }

    @Override
    public void shutdown() {
        if (null != timer) {
            timer.cancel();
        }
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
