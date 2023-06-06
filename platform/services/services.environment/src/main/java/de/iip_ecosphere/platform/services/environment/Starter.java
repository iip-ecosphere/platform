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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.services.environment.switching.ServiceBase;
import de.iip_ecosphere.platform.services.environment.testing.TestBroker;
import de.iip_ecosphere.platform.support.CollectionUtils;
import de.iip_ecosphere.platform.support.FileUtils;
import de.iip_ecosphere.platform.support.JarUtils;
import de.iip_ecosphere.platform.support.NetUtils;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase.NotificationMode;
import de.iip_ecosphere.platform.support.iip_aas.config.CmdLine;
import de.iip_ecosphere.platform.support.resources.ResourceLoader;
import de.iip_ecosphere.platform.transport.Transport;
import de.iip_ecosphere.platform.transport.connectors.TransportSetup;

import static de.iip_ecosphere.platform.support.iip_aas.config.CmdLine.*;

/**
 * Service environment starter reading command server information from the command line.
 * 
 * @author Holger Eichelberger, SSE
 */
public class Starter {
    
    public static final String PARAM_IIP_PROTOCOL = "iip.protocol";
    public static final String PARAM_IIP_PORT = "iip.port";
    public static final String PARAM_IIP_APP_ID = "iip.appId";
    public static final String PARAM_IIP_TRANSPORT_GLOBAL = "iip.transport.global";
    public static final String PARAM_IIP_TEST_TRANSPORT_PORT = "iip.test.transport.port";
    public static final String PARAM_IIP_TEST_AAS_PORT = "iip.test.aas.port";
    public static final String PARAM_IIP_TEST_AASREG_PORT = "iip.test.aasRegistry.port";
    public static final String PARAM_IIP_TEST_SERVICE_AUTOSTART = "iip.test.service.autostart";
    public static final String ARG_AAS_NOTIFICATION = "iip.test.aas.notification";
    public static final String PROPERTY_JAVA8 = "iip.test.java8";
    public static final String IIP_APP_PREFIX = "iip.app.";
    public static final String IIP_TEST_PREFIX = "iip.test.";
    public static final String IIP_TEST_PLUGIN = "iip.test.plugin";
    
    private static ProtocolServerBuilder builder;
    private static Server server;
    private static Map<String, Integer> servicePorts = new HashMap<>();
    private static Map<String, Service> mappedServices = new HashMap<>();
    private static boolean serviceAutostart = false; // shall be off, done by platform, only for testing
    private static boolean onServiceAutostartAttachShutdownHook = true;
    private static int transportPort = -1; // -1 -> use configured one
    private static String transportHost = null;
    private static boolean transportGlobal = false;
    private static EnvironmentSetup setup;
    private static String appId = "";
    private static Map<String, Plugin> plugins = new HashMap<>();
    
    /**
     * Defines a starter plugin.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface Plugin {

        /**
         * Runs the plugin.
         * 
         * @param args the (modified) command line arguments
         */
        public void run(String[] args);
        
        /**
         * Displays the help.
         * 
         * @param indent indentation characters for all lines after the first
         * @return the formatted help text
         */
        public default String getHelp(String indent) {
            return "runs default starter functionality";
        }
        
    }
    /**
     * Default supplier for the local transport setup. This basic implementation is a bit heuristic
     * as it assumes the same authentication/port as the global setup, which may not work in certain container
     * settings.
     */
    protected static final Function<EnvironmentSetup, TransportSetup> DFLT_LOCAL_TRANSPORT_SETUP_SUPPLIER = setup -> {
        TransportSetup localSetup = null;
        TransportSetup globalSetup = setup.getTransport();
        String globalHost = globalSetup.getHost();
        if (!ServerAddress.LOCALHOST.equals(globalHost) 
            && !"127.0.0.1".equals(globalHost) && !NetUtils.isOwnAddress(globalHost)) {
            localSetup = setup.getTransport().copy(); // TODO same authentication/port assumed
            localSetup.setHost(ServerAddress.LOCALHOST);
        }        
        return localSetup;
    };

    /**
     * Defines the supplier for the local transport setup. Called only in {@link #getSetup()} if 
     * {@link #transportGlobal a need for a separation of global/local transport} was detected.
     * Specific service execution implementations may override this using 
     * {@link #setLocalTransportSetupSupplier(Function)}. Default is {@link #DFLT_LOCAL_TRANSPORT_SETUP_SUPPLIER}.
     */
    private static Function<EnvironmentSetup, TransportSetup> localTransportSetupSupplier 
        = DFLT_LOCAL_TRANSPORT_SETUP_SUPPLIER;

    
    /**
     * Registers a functional plugin.
     * 
     * @param name the name (turned to lower case)
     * @param plugin the plugin instance
     */
    protected static void registerPlugin(String name, Plugin plugin) {
        plugins.put(name.toLowerCase(), plugin);
    }
    
    /**
     * Registers the default plugins.
     * 
     * @param dflt the default plugin to be used if no command line argument is given for {@value #IIP_TEST_PLUGIN}.
     */
    protected static void registerDefaultPlugins(Plugin dflt) {
        registerPlugin("", dflt);
        registerPlugin("broker", new TestBroker());
        registerPlugin("help", new HelpPlugin());
    }
    
    /**
     * The default plugin for displaying the help texts of the plugins.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class HelpPlugin implements Plugin {

        @Override
        public void run(String[] args) {
            List<String> names = CollectionUtils.toList(plugins.keySet().iterator());
            Collections.sort(names);
            for (String n : names) {
                System.out.println("- " + n + ": " + plugins.get(n).getHelp("  "));
            }
        }
        
        @Override
        public String getHelp(String indent) {
            return "prints this help";
        }
        
    }
    
    /**
     * Adds all environment properties starting with {@link #IIP_APP_PREFIX} or {@link #IIP_TEST_PREFIX} to the command 
     * line of the service to be started.
     * 
     * @param args the arguments to add the application environment settings
     */
    public static void addAppEnvironment(List<String> args) {
        for (Object k : System.getProperties().keySet()) {
            String key = k.toString();
            if (key != null && key.length() > 0) {
                String val = System.getProperty(key);
                if (key.startsWith(IIP_APP_PREFIX) 
                    || key.startsWith(IIP_TEST_PREFIX)) { // could be dependent on PARAM_IIP_TEST_SERVICE_AUTOSTART
                    args.add("-D" + key + "=" + val);
                }
            }
        }
    }
    
    /**
     * Retrieves the AAS notification mode from cmd line argument {@value #ARG_AAS_NOTIFICATION} and sets this mode 
     * for AAS interactions. [testing]
     *  
     * @param args the command line arguments
     * @param dflt the default value if no argument is present, may be <b>null</b> to keep the actual mode if not 
     *     set explicitly
     * @return the actual AAS notification mode, may be <b>null</b> for none
     */
    public static NotificationMode setAasNotificationMode(String[] args, NotificationMode dflt) {
        NotificationMode mode = dflt;
        String tmp = CmdLine.getArg(args, ARG_AAS_NOTIFICATION, null == mode ? "" : mode.name());
        if (tmp.length() > 0) {
            try {
                mode = NotificationMode.valueOf(tmp);
            } catch (IllegalArgumentException e) {
                getLogger().info("AAS notification mode {} unknown. Resorting to {}", tmp, mode);
            }
        }
        if (null != mode) {
            ActiveAasBase.setNotificationMode(mode);
        }
        return mode;
    }    
    
    /**
     * Considers installed dependencies properties, -D{@value #PROPERTY_JAVA8}.
     */
    public static void considerInstalledDependencies() {
        if (!SystemUtils.IS_JAVA_1_8) {
            String prop = System.getProperty(PROPERTY_JAVA8, null);
            if (prop != null) {
                File java8 = new File(prop);
                LoggerFactory.getLogger(Starter.class).info("Setting Java8 to: {}", java8);
                InstalledDependenciesSetup.getInstance().setLocation(InstalledDependenciesSetup.KEY_JAVA_8, java8);
            }
        }
    }
    
    /**
     * Transfers {@link #IIP_APP_PREFIX} as system property or usual command line
     * argument to the system properties if not already set.
     * 
     * @param args the arguments to be analyzed
     */
    public static void transferArgsToEnvironment(String[] args) {
        for (String a : args) {
            String tmp = null;
            if (a.startsWith("-D" + IIP_APP_PREFIX) 
                || a.startsWith("-D" + IIP_TEST_PREFIX)) { // so far only -D, may need PARAM_IIP_TEST_SERVICE_AUTOSTART
                tmp = a.substring(2);
            } else if (a.startsWith("--" + IIP_APP_PREFIX)) {
                tmp = a.substring(2);
            }
            if (null != tmp) {
                int pos = tmp.indexOf('=');
                if (pos > 0) {
                    String key = tmp.substring(0, pos);
                    if (null == System.getProperty(key)) {
                        String value = tmp.substring(pos + 1);
                        System.setProperty(key, value);
                    }
                }
            }
        }
    }
    
    /**
     * Returns the application/application instance id passed in by {@link #PARAM_IIP_APP_ID}.
     * 
     * @return the app/application instance id (separated by {@link ServiceBase#APPLICATION_SEPARATOR}, may be empty
     */
    public static String getAppId() {
        return appId;
    }

    /**
     * Returns the id of {@code service} taking {@link #getAppId()} into account.
     * 
     * @param service the service
     * @return the service including appId if known/specified
     */
    public static String getServiceId(Service service) {
        return getServiceId(null == service ? "" : service.getId());
    }

    /**
     * Returns the completed service id {@code sId} taking {@link #getAppId()} into account.
     * 
     * @param sId the service id
     * @return the service including appId if known/specified
     */
    public static String getServiceId(String sId) {
        String appId = Starter.getAppId();
        if (null != appId && appId.length() > 0) {
            sId += ServiceBase.APPLICATION_SEPARATOR + appId;
        }
        return sId;
    }

    /**
     * Returns the network manager key used by this descriptor to allocate dynamic network ports for service commands.
     * 
     * @param serviceId the service id
     * @return the key
     */
    public static String getServiceCommandNetworkMgrKey(String serviceId) {
        return "admin_" + serviceId;
    }

    /**
     * Returns the network manager key used by this descriptor to allocate dynamic network ports for a non-Java 
     * realization process.
     * 
     * @param serviceId the service id
     * @return the key
     */
    public static String getServiceProcessNetworkMgrKey(String serviceId) {
        return getServiceCommandNetworkMgrKey(serviceId) + "_process";
    }
    
    /**
     * Composes a command line argument for the starter.
     * 
     * @param argName the argument name
     * @param value the value
     * @return the composed command line argument
     */
    public static String composeArgument(String argName, Object value) {
        return PARAM_PREFIX + argName + PARAM_VALUE_SEP + value.toString();
    }
    
    /**
     * Returns the argument name carrying the delegation port of {@code serviceId}. Arguments of this kind will
     * be collected for {@link #getServicePort(String)}
     * 
     * @param serviceId the service id (will be normalized to command line requirements)
     * @return the argument name
     */
    public static String getServicePortName(String serviceId) {
        return PARAM_IIP_PORT + PARAM_ARG_NAME_SEP + normalizeServiceId(serviceId);
    }

    /**
     * Returns a service port obtained in {@link #parse(String...)}.
     * 
     * @param serviceId the service id (will be normalized to command line requirements)
     * @return the port number, negative if invalid or unknown
     */
    public static int getServicePort(String serviceId) {
        Integer port = servicePorts.get(normalizeServiceId(serviceId));
        return null == port ? -1 : port;
    }
    
    /**
     * Returns the normalized service id.
     * 
     * @param serviceId the service id
     * @return the /normalized) service id
     */
    public static String normalizeServiceId(String serviceId) {
        return serviceId.replaceAll(" ", "");
    }
    
    /**
     * Enables service autostart for the next services to be mapped. Disabled by default. Shall be called before 
     * {@code #main(String[])}. Usually done when needed by platform during service lifecycle. [testing]
     * 
     * @param autostart {@code true} enables autostart, {@code false} disables autostart
     */
    public static void setServiceAutostart(boolean autostart) {
        serviceAutostart = autostart;
    }
    
    /**
     * Enables/disable shutdown hooks on service autostarts for service autostops. Enabled by default. Shall be called 
     * before {@code #main(String[])}. [testing]
     * 
     * @param hook {@code true} enables creation of shutdown hooks, {@code false} disables shutdown hooks on autostart
     */
    public static void setOnServiceAutostartAttachShutdownHook(boolean hook) {
        onServiceAutostartAttachShutdownHook = hook;
    }
    
    /**
     * Returns service mapped by this starter. These are typically all services executed within the same JVM.
     * 
     * @param serviceId the serviceId, ignored if <b>null</b>
     * @return the service if known, <b>null</b> else
     */
    public static Service getMappedService(String serviceId) {
        return null == serviceId ? null : mappedServices.get(serviceId);
    }
    
    /**
     * Parses command line arguments. Collects information for {@link #getServicePort(String)}.
     * 
     * @param args the arguments
     */
    public static void parse(String... args) {
        transferArgsToEnvironment(args);
        considerInstalledDependencies();
        AasFactory factory = AasFactory.getInstance();
        int port = getIntArg(args, PARAM_IIP_PORT, -1);
        if (port < 0) {
            port = NetUtils.getEphemeralPort();
        }
        transportGlobal = getBooleanArg(args, PARAM_IIP_TRANSPORT_GLOBAL, 
            Boolean.valueOf(System.getProperty(PARAM_IIP_TRANSPORT_GLOBAL, "false")));
        transportHost = getArg(args, "transport.host", transportHost);
        transportPort = getIntArg(args, PARAM_IIP_TEST_TRANSPORT_PORT, 
            getIntArg(args, "transport.port", transportPort));
        if (transportPort > 0 || transportHost != null) {
            getSetup();
        }
        int tmpPort = getIntArg(args, PARAM_IIP_TEST_AAS_PORT, -1);
        if (tmpPort > 0) {
            AasPartRegistry.getSetup().getServer().setPort(tmpPort);
            getLogger().info("Configuring IIP server port to {}", tmpPort);
        }
        tmpPort = getIntArg(args, PARAM_IIP_TEST_AASREG_PORT, -1);
        if (tmpPort > 0) {
            AasPartRegistry.getSetup().getRegistry().setPort(tmpPort);
            getLogger().info("Configuring IIP registry port to {}", tmpPort);
        }
        appId = CmdLine.getArg(args, PARAM_IIP_APP_ID, "");
        setAasNotificationMode(args, null); // keep default unless specified differently
        serviceAutostart = getBooleanArg(args, PARAM_IIP_TEST_SERVICE_AUTOSTART, serviceAutostart);
        String protocol = getArg(args, PARAM_IIP_PROTOCOL, AasFactory.DEFAULT_PROTOCOL);
        boolean found = false;
        for (String p : factory.getProtocols()) {
            if (p.equals(protocol)) {
                found = false;
            }
        }
        if (!found) {
            protocol = AasFactory.DEFAULT_PROTOCOL;
        }
        for (String a: args) {
            if (a.startsWith(PARAM_PREFIX + PARAM_IIP_PORT + PARAM_ARG_NAME_SEP)) {
                int valPos = a.indexOf(PARAM_VALUE_SEP);
                if (valPos > 0) {
                    String prefix = a.substring(0, valPos);
                    int idPos = prefix.lastIndexOf(PARAM_ARG_NAME_SEP);
                    try {
                        String serviceId = prefix.substring(idPos + 1);
                        int p = Integer.parseInt(a.substring(valPos + 1));
                        servicePorts.put(serviceId, p);
                    } catch (NumberFormatException e) {
                    }
                }
            }
        }

        getLogger().info("Configuring service command server for protocol '" + protocol 
            + "' (empty means default) and port " + port);
        builder = factory.createProtocolServerBuilder(protocol, port);
    }
    
    /**
     * Starts the server instance(s).
     */
    public static void start() {
        if (null != builder) {
            getLogger().info("Starting service command server");
            server = builder.build();
            server.start();
        } else {
            getLogger().error("Cannot start service command server as no builder is set.");
        }
    }
    
    /**
     * Returns the logger instance.
     * 
     * @return the logger instance
     */
    private static Logger getLogger() {
        return LoggerFactory.getLogger(Starter.class);
    }

    /**
     * Returns the protocol builder for mapping services.
     * 
     * @return the protocol builder, <b>null</b> if {@link #parse(String[])} was not called before
     */
    public static ProtocolServerBuilder getProtocolBuilder() {
        return builder;
    }

    /**
     * Returns the service mapper linked to {@link #getProtocolBuilder()}.
     * 
     * @return the service mapper, <b>null</b> if {@link #parse(String[])} was not called before
     */
    public static ServiceMapper getServiceMapper() {
        return new ServiceMapper(builder);
    }
    
    /**
     * Maps a service through a given mapper and metrics client. No mapping will take place if either {@code service},
     * {@code mapper} or {@link #getProtocolBuilder()} is <b>null</b>. The specific mapping for the metrics will only
     * take place if {@code metricsClient} is not <b>null</b>.
     * 
     * @param mapper the service mapper instance (may be <b>null</b>, no mapping will happen then)
     * @param service the service to be mapped (may be <b>null</b>, no mapping will happen then)
     * @param enableAutostart whether service autostart shall be performed if {@code}, e.g., not for family members
     */
    public static void mapService(ServiceMapper mapper, Service service, boolean enableAutostart) {
        if (null != service && service.getId() != null) {
            mappedServices.put(service.getId(), service);
            if (null != mapper && null != Starter.getProtocolBuilder()) {
                mapper.mapService(service);
            }
            // TODO -> ServiceState.DEPLOYED
            if (serviceAutostart && enableAutostart && service.isTopLevel()) {
                try {
                    getLogger().info("Service autostart: '{}' '{}'", service.getId(), service.getClass().getName());
                    service.setState(ServiceState.STARTING);
                    if (onServiceAutostartAttachShutdownHook) {
                        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                            try {
                                System.out.println("Service autostop: " + service.getId());
                                service.setState(ServiceState.STOPPING);
                            } catch (ExecutionException e) {
                                getLogger().error("Service autostop '{}': {}", service.getId(), e.getMessage());
                            }
                        }));
                    }
                } catch (ExecutionException e) {
                    getLogger().error("Service autostart '{}': {}", service.getId(), e.getMessage());
                }
            }
        }  else {
            if (null == setup || setup.getNotifyServiceNull()) {
                Throwable t = new Throwable("NO EXCEPTION/DEBUGGING: Service null or Service id null");
                t.printStackTrace(System.out);
            }
        }
    }
    
    /**
     * Extracts artifacts that are required for a service being realized of external processes.
     * 
     * @param sId the service id
     * @param pSpec the process specification
     * @param artFile the ZIP/JAR service artifact
     * @param processBaseDir the base directory to be used to create a process home directory within if 
     *     {@link ProcessSpec#getHomePath()} is <b>null</b> 
     * @return the folder into which the process has been extracted. May be {@link ProcessSpec#getHomePath()} or
     *     a temporary directory.
     * @throws IOException if accessing files fails
     */
    public static File extractProcessArtifacts(String sId, ProcessSpec pSpec, File artFile, File processBaseDir) 
        throws IOException {
        // take over / create process home dir
        File processDir = pSpec.getHomePath();
        if (null == processDir) {
            processDir = new File(processBaseDir, normalizeServiceId(sId) + "-" + System.currentTimeMillis());
        }
        if (!pSpec.isStarted()) {
            FileUtils.deleteQuietly(processDir); // unlikely, just to be sure
        }
        processDir.mkdirs();

        // unpack artifacts to home
        for (String artPath : pSpec.getArtifacts()) {
            while (artPath.startsWith("/")) {
                artPath = artPath.substring(1);
            }
            FileInputStream fis = null;
            InputStream artifact = null; 
            try { // spring packaging
                fis = new FileInputStream(artFile);
                artifact = JarUtils.findFile(fis, "BOOT-INF/classes/" + artPath);
                if (null == artifact) {
                    fis = new FileInputStream(artFile); // TODO preliminary, use predicate
                    artifact = JarUtils.findFile(fis, artPath);
                    if (null != artifact) {
                        getLogger().info("Found " + artPath + " in " + artFile + " " 
                            + artifact.getClass().getSimpleName());
                    }
                } else {
                    getLogger().info("Found " + artPath + " in BOOT-INF/classes/" + artPath + " " 
                        + artifact.getClass().getSimpleName());
                }
            } catch (IOException e) {
                getLogger().info("Cannot open " + artFile + ": " + e.getMessage());
            }
            if (null == artifact) { 
                artifact = ResourceLoader.getResourceAsStream(Starter.class, artPath);
                if (null != artifact) {
                    getLogger().info("Found " + artPath + " on classpath " + artifact.getClass().getSimpleName());
                }
            }
            if (null == artifact) {
                throw new IOException("Cannot find artifact '" + artPath + "' in actual service JAR");
            }
            JarUtils.extractZip(artifact, processDir.toPath());
            getLogger().info("Extracted process artifact " + artPath + " to " + processDir);
            FileUtils.closeQuietly(artifact);
            FileUtils.closeQuietly(fis);
        }
        return processDir;
    }

    /**
     * Maps a service through the default mapper and the default metrics client. [Convenience method for generation]
     * 
     * @param service the service to be mapped (may be <b>null</b>, no mapping will happen then)
     * @param enableAutostart whether service autostart shall be performed if {@code}, e.g., not for family members
     * 
     * @see #getServiceMapper()
     * @see #mapService(ServiceMapper, Service, boolean)
     */
    public static void mapService(Service service, boolean enableAutostart) {
        mapService(getServiceMapper(), service, enableAutostart);
    }

    /**
     * Maps a service through the default mapper and the default metrics client. [Convenience method for generation]
     * By default, do autostart.
     * 
     * @param service the service to be mapped (may be <b>null</b>, no mapping will happen then)
     * 
     * @see #getServiceMapper()
     * @see #mapService(ServiceMapper, Service, boolean)
     */
    public static void mapService(Service service) {
        mapService(service, true);
    }
    
    /**
     * Terminates running server instances.
     */
    public static void shutdown() {
        if (null != server) {
            server.stop(false); 
        }
    }
    
    /**
     * Returns the environment setup.
     * 
     * @return the setup
     */
    public static EnvironmentSetup getSetup() {
        if (null == setup) {
            try {
                LoggerFactory.getLogger(Starter.class).info("Loading setup");
                setup = EnvironmentSetup.readFromYaml(EnvironmentSetup.class, getApplicationSetupAsStream());
                if (transportPort > 0) {
                    setup.getTransport().setPort(transportPort);
                }
                if (transportHost != null) {
                    setup.getTransport().setHost(transportHost);
                }
                Transport.setTransportSetup(() -> setup.getTransport());
                Transport.createConnector(); // warmup
                
                // globalhost is part of transport setup.
                TransportSetup globalSetup = setup.getTransport();
                LoggerFactory.getLogger(Starter.class).info("Global transport {}:{}", 
                    globalSetup.getHost(), globalSetup.getPort());
                
                if (!transportGlobal && enablesLocalTransport(globalSetup)) {
                    TransportSetup lSetup = localTransportSetupSupplier.apply(setup);
                    if (null == lSetup && localTransportSetupSupplier != DFLT_LOCAL_TRANSPORT_SETUP_SUPPLIER) {
                        // fallback, in particular for testing
                        lSetup = DFLT_LOCAL_TRANSPORT_SETUP_SUPPLIER.apply(setup);
                    }
                    TransportSetup localSetup = lSetup;
                    if (null != localSetup) {
                        LoggerFactory.getLogger(Starter.class).info("Local transport {}:{}", 
                            localSetup.getHost(), localSetup.getPort());
                        Transport.setLocalSetup(() -> localSetup);
                    }
                } else {
                    LoggerFactory.getLogger(Starter.class).info("Local transport: use global as it is local");
                }
            } catch (IOException e) {
                setup = new EnvironmentSetup();
                LoggerFactory.getLogger(Starter.class).warn("Cannot read application.yml. Aas/Transport setup invalid");
            }
        }
        return setup;
    }
    
    /**
     * Returns whether {@code globalSetup} enables local transport.
     * 
     * @param globalSetup the global setup
     * @return {@code true} for enabled, {@code false} for local transport is sufficient, e.g., in local testing
     */
    protected static final boolean enablesLocalTransport(TransportSetup globalSetup) {
        boolean enable = false;
        String globalHost = globalSetup.getHost();
        if (!ServerAddress.LOCALHOST.equals(globalHost) && !"127.0.0.1".equals(globalHost)) {
            enable = (!NetUtils.isOwnAddress(globalHost) // we are running on a different device 
                || NetUtils.isInContainer()); // or on the server but in an (application) container
        }
        return enable;
    }
    
    /**
     * Returns the application setup as stream.
     *  
     * @return the application setup as stream
     */
    public static InputStream getApplicationSetupAsStream() {
        return ResourceLoader.getResourceAsStream(Starter.class, "application.yml"); // spring only?
    }
    
    /**
     * Changes the local transport supplier determining the setup for the local transport.
     * 
     * @param supplier the new supplier, may be <b>null</b> for none
     */
    protected static void setLocalTransportSetupSupplier(Function<EnvironmentSetup, TransportSetup> supplier) {
        if (null != supplier) { // ensure that there is one
            localTransportSetupSupplier = supplier;
        }
    }
    
    /**
     * Selects the actual functional plugin via cmd line argument of {@value #IIP_TEST_PLUGIN}. If none is given, start 
     * the default plugin. 
     * 
     * @param args command line arguments
     */
    protected static void runPlugin(String[] args) {
        String test = CmdLine.getArg(args, IIP_TEST_PLUGIN, "").toLowerCase();
        Plugin plugin = plugins.get(test);
        if (null == plugin) {
            System.out.println("No start plugin for '" + plugin + "' known. Stopping.");
        } else {
            plugin.run(args);
        }
    }

    /**
     * Simple default start main program without mapping any services before startup. This can be done on-demand
     * through {@link #getProtocolBuilder()} and {@link #getServiceMapper()}.
     * 
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        registerDefaultPlugins(a -> Starter.start());
        Starter.parse(args);
        getSetup(); // ensure instance
        runPlugin(args);
    }

}
