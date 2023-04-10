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

package de.iip_ecosphere.platform.platform;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.apache.commons.lang.ArrayUtils;

import de.iip_ecosphere.platform.deviceMgt.DeviceRemoteManagementOperations;
import de.iip_ecosphere.platform.ecsRuntime.ContainerState;
import de.iip_ecosphere.platform.ecsRuntime.EcsClient;
import de.iip_ecosphere.platform.platform.cli.CommandProvider;
import de.iip_ecosphere.platform.platform.cli.DeviceManagementClientFactory;
import de.iip_ecosphere.platform.platform.cli.EcsClientFactory;
import de.iip_ecosphere.platform.platform.cli.Level;
import de.iip_ecosphere.platform.platform.cli.PlatformClientFactory;
import de.iip_ecosphere.platform.platform.cli.PrintVisitor;
import de.iip_ecosphere.platform.platform.cli.ResourcesClientFactory;
import de.iip_ecosphere.platform.platform.cli.ServiceDeploymentPlan;
import de.iip_ecosphere.platform.platform.cli.ServiceDeploymentPlan.ContainerResourceAssignment;
import de.iip_ecosphere.platform.platform.cli.ServiceDeploymentPlan.ServiceResourceAssignment;
import de.iip_ecosphere.platform.platform.cli.ServicesClientFactory;
import de.iip_ecosphere.platform.services.ServicesClient;
import de.iip_ecosphere.platform.platform.cli.PrintVisitor.PrintType;
import de.iip_ecosphere.platform.support.TaskRegistry;
import de.iip_ecosphere.platform.support.TaskRegistry.TaskData;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.SubmodelElement;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.iip_aas.ApplicationInstancesAasClient;
import de.iip_ecosphere.platform.support.iip_aas.json.JsonUtils;
import de.iip_ecosphere.platform.support.net.UriResolver;

/**
 * The backend part of the command line interface providing initial platform functionality through the various AAS.
 * Just initial rather than all potential commands are provided.
 * 
 * @author Holger Eichelberger, SSE
 */
class CliBackend {

    public static final Consumer<String> DEFAULT_ERROR_CONSUMER = s -> { };
    private static ServicesClientFactory servicesFactory = ServicesClientFactory.DEFAULT;
    private static EcsClientFactory ecsFactory = EcsClientFactory.DEFAULT;
    private static ResourcesClientFactory resourcesFactory = ResourcesClientFactory.DEFAULT;
    private static DeviceManagementClientFactory deviceManagementFactory = DeviceManagementClientFactory.DEFAULT;
    private static PlatformClientFactory platformFactory = PlatformClientFactory.LOCAL;
    private static Consumer<String> errorConsumer = DEFAULT_ERROR_CONSUMER;

    /**
     * Returns the ECS runtime client factory.
     * 
     * @return the factory
     */
    protected static EcsClientFactory getEcsFactory() {
        return ecsFactory;
    }
    
    /**
     * Returns the services client factory.
     * 
     * @return the factory
     */
    protected static ServicesClientFactory getServicesFactory() {
        return servicesFactory;
    }
    
    /**
     * Changes the client factories. [testing]
     * 
     * @param services the services client factory 
     * @param ecs the ECS client factory
     * @param resources the resources client factory
     * @param deviceManagement the device management factory
     * @param platform the platform client factory
     */
    public static void setFactories(ServicesClientFactory services, EcsClientFactory ecs, 
        ResourcesClientFactory resources, DeviceManagementClientFactory deviceManagement, 
        PlatformClientFactory platform) {
        servicesFactory = services;
        ecsFactory = ecs;
        resourcesFactory = resources;
        deviceManagementFactory = deviceManagement;
        platformFactory =  platform;
    }
    
    /**
     * Changes the error consumer. [testing].
     * 
     * @param consumer the new error consumer
     */
    public static void setErrorConsumer(Consumer<String> consumer) {
        errorConsumer = consumer;
    }
    
    /**
     * A basic command interpreter.
     * 
     * @author Holger Eichelberger, SSE
     */
    protected abstract static class AbstractCommandInterpreter {

        /**
         * Prints the help.
         * 
         * @param provider the command provider
         * @param level the actual prompt level
         */
        abstract void printHelp(CommandProvider provider, Level level);
        
        /**
         * Interprets the provided commands.
         * 
         * @param provider the command provider
         * @param level the level to interpret for
         * @param args arguments from the last level, used to {@link #initialize(String...)} this level
         * @return {@code true} for exit, {@code false} for continue
         * @see #initialize(String...)
         * @see #interpretFurther(CommandProvider, Level, String)
         */
        boolean interpret(CommandProvider provider, Level level, String... args) {
            boolean exit = false;
            try {
                String cmd;
                initialize(args);
                do {
                    prompt(level, provider);
                    cmd = provider.nextCommand();
                    if (null != cmd) {
                        switch (cmd.toLowerCase()) {
                        case "help":
                            printHelp(provider, level);
                            break;
                        case "snapshotaas":
                            snapshotAas();
                            break;
                        case "exit":
                            exit = true;
                            cmd = null;
                            break;
                        case "..":
                        case "back":
                            if (level.isTopLevel()) {
                                unknownCommand(cmd);
                            } else {
                                cmd = null;
                            }
                            break;
                        default:
                            try {
                                exit = interpretFurther(provider, level, cmd);
                            } catch (ExecutionException | URISyntaxException e) {
                                println(e);
                            }
                            break;
                        }
                    }
                } while (null != cmd && !exit);
            } catch (IOException e) {
                println(e);
            }
            return exit;
        }

        /**
         * Initializes this instance, to be specialized by subclasses.
         * 
         * @param args arguments from the last level
         * @throws IOException if initialization fails for some reason
         */
        protected void initialize(String... args) throws IOException {
        }
        
        /**
         * Called to indicate an unknown command.
         * 
         * @param cmd the command
         */
        protected void unknownCommand(String cmd) {
            error("Unknown command on this level: " + cmd);
        }
        
        /**
         * Interprets further commands, to be specialized by subclasses.
         * 
         * @param provider the command provider
         * @param level the level to interpret for
         * @param cmd the command to interpret
         * @return {@code true} for exit, {@code false} for continue
         * @throws ExecutionException if the execution fails
         * @throws URISyntaxException if an URI was requested but the syntax is erroneous
         */
        protected boolean interpretFurther(CommandProvider provider, Level level, String cmd) 
            throws ExecutionException, URISyntaxException {
            unknownCommand(cmd);
            return false;
        }

    }
    
    /**
     * Checks whether a reload is needed.
     * 
     * @param coll the respective collection that may be subject to a reload
     * @param changed the flag indicating whether a reload is needed
     * @return {@code false}
     */
    protected static boolean checkReload(SubmodelElementCollection coll, boolean changed) {
        if (changed && null != coll) {
            coll.update();
            changed = false;
        }
        return changed;
    }
    
    /**
     * Determines whether a submodel element collection in the resources submodel shall be 
     * excluded from listing.
     *  
     * @param coll the collection
     * @return {@code true} for exclude, {@code false} for include
     */
    private static boolean resourceExclusion(SubmodelElementCollection coll) {
        String idShort = coll.getIdShort(); 
        return idShort.equals("containers") || idShort.equals("deviceRegistry")  || idShort.equals("deviceManager");
    }

    /**
     * Lists the resources available to the platform.
     */
    protected static void listResources() {
        try {
            print(resourcesFactory.create().getResources(), "- Resource ", 
                c -> !resourceExclusion(c), PrintType.PREFIX);
        } catch (IOException e) {
            println(e);
        }
    }
    
    /**
     * Stores the platform AAS into a file.
     */
    private static void snapshotAas() {
        try {
            String file = platformFactory.create().snapshotAas("cli");
            if (platformFactory == PlatformClientFactory.LOCAL) {
                println("Platform AAS written to " + file);
            } else {
                println("Platform AAS written (on server) to " + file);
            }
        } catch (IOException | ExecutionException e) {
            println(e);
        }
    }
    
    /**
     * Prints a sub-model element using {@link PrintVisitor}.
     * 
     * @param elt the element to print
     * @param collPrefix a prefix string to be printed before the name of collection, may be <b>null</b> for not 
     *     printing the collection name
     * @param filter optional submodel elements collection filter, may be <b>null</b> for none
     * @param skipLevel how to handle printout per level, if not given {@link PrintType#PREFIX} is used
     */
    protected static void print(SubmodelElement elt, String collPrefix, Predicate<SubmodelElementCollection> filter, 
        PrintType... skipLevel) {
        if (null != elt) {
            PrintVisitor vis = new PrintVisitor(collPrefix, filter, platformFactory, skipLevel);
            elt.accept(vis);
        } else {
            println("None.");
        }
    }

    /**
     * Creates an SSH server on a given resource.
     * 
     * @param id the resource id
     */
    protected static void createSshServer(String id) {
        try {
            DeviceRemoteManagementOperations.SSHConnectionDetails sshServer 
                = deviceManagementFactory.create().establishSsh(id);
            System.out.println("Use the following line to connect to the edge-ssh-server:");
            System.out.println("ssh " + sshServer.getUsername() + "@" + sshServer.getHost() 
                + " -p " + sshServer.getPort());
            System.out.println("Password: " + sshServer.getPassword());
        } catch (ExecutionException | IOException e) {
            println(e);
        }
    }
    
    /**
     * A runnable for starting services (in parallel).
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class StartServicesRunnable implements Runnable {

        private ServicesClient client;
        private String[] serviceIds;
        private ExecutionException exception;
        private String resourceId;
        private Map<String, String> options;

        /**
         * Creates the runnable.
         * 
         * @param resourceId the resource id (for failure identification)
         * @param client the services client to be used
         * @param serviceIds the ids of the services to be started
         * @param options the service start options, may be <b>null</b>
         */
        private StartServicesRunnable(String resourceId, ServicesClient client, String[] serviceIds, 
            Map<String, String> options) {
            this.client = client;
            this.serviceIds = serviceIds;
            this.resourceId = resourceId;
            this.options = options;
        }

        @Override
        public void run() {
            try {
                TaskData data = TaskRegistry.getTaskData();
                println(getMessagePrefix(true) + (null == data ? "" : ": " + data.getId()));
                if (null != data) {
                    client.startServiceAsTask(data.getId(), options, serviceIds);
                } else {
                    client.startService(options, serviceIds);
                }
                println(getMessagePrefix(false) + ": Done.");
            } catch (ExecutionException e) {
                exception = e;
            }
        }
        
        /**
         * Returns a message prefix composed of {@link #resourceId} and {@link #serviceIds}.
         * 
         * @param withOptions emit also the options
         * @return the prefix
         */
        private String getMessagePrefix(boolean withOptions) {
            String result = "On " + resourceId + " starting " + Arrays.toString(serviceIds);
            if (withOptions) {
                result += " with options " + options;
            }
            return result;
        }
        
        /**
         * Returns a formatted message composed from an exception caught while starting the service.
         * 
         * @return <b>null</b> if no exception occurred, else a text 
         * @see #getMessagePrefix(boolean)
         */
        private String getExceptionMessage() {
            return null == exception ? null : getMessagePrefix(false) + ": " + exception.getMessage();
        }
        
    }
    
    /**
     * Returns the artifact for a service assignment.
     * 
     * @param artifact the global artifact
     * @param assng the assignment
     * @return either {@code artifact} or the specific one specified in {@code assng}
     * @throws URISyntaxException if the resulting URI cannot be created due to syntax reasons
     */
    private static URI getArtifact(URI artifact, ServiceResourceAssignment assng) throws URISyntaxException {
        URI result = artifact;
        String art = assng.getArtifact();
        if (art != null && art.length() > 0) {
            result = toUri(art);
        }
        return result;
    }
    
    /**
     * Checks whether we have an already known exception.
     * 
     * @param ex the exception
     * @param marker the already known marker
     * @param text the text to print out as information on the element
     * @param id the id of the element
     * @throws ExecutionException if it is not an already known exception
     */
    private static void checkAlreadyKnown(ExecutionException ex, String marker, String text, String id) 
        throws ExecutionException {
        if (!ex.getMessage().contains(marker)) {
            throw ex;
        } else {
            println("Skipping " + text + " '" + id + "' as already known");
        }
    }
    
    /**
     * Deploys the containers for {@code plan}.
     * 
     * @param plan the deployment plan
     * @throws ExecutionException if executing a container operation fails
     * @throws IOException if calling an AAS container operation fails
     * @throws URISyntaxException if building a container URI fails
     */
    private static void deployContainers(ServiceDeploymentPlan plan) throws ExecutionException, 
        IOException, URISyntaxException {
        for (ContainerResourceAssignment c : plan.getContainer()) {
            try {
                EcsClient client = getEcsFactory().create(c.getResource());
                println("Adding container '" + c.getContainerDesc() + "' to resource " + c.getResource());
                URI contURI = toUri(c.getContainerDesc());
                ContainerState contState = client.getState(contURI.toString());
                String cId;
                if (ContainerState.UNKNOWN == contState) {
                    cId = client.addContainer(contURI);
                    printlnDone();
                } else {
                    cId = client.getId(contURI);
                }
                if (null == cId || cId.length() == 0) {
                    throw new ExecutionException("Cannot add/retrieve container for " + contURI, null);
                }
                if (ContainerState.AVAILABLE == contState || ContainerState.STOPPED == contState) {
                    println("Starting container '" + c.getContainerDesc() + "'(" + cId + ") on resource " 
                        + c.getResource());
                    client.startContainer(cId);
                    printlnDone();
                }
            } catch (ExecutionException e) { 
                checkAlreadyKnown(e, EcsClient.EXC_ALREADY_KNOWN, "container", c.getContainerDesc()); // just in case
            }
        }
    }
    
    /**
     * Turns information form {@code plan} into service start options.
     * 
     * @param plan the plan to take the information from
     * @return the service start options, may be <b>null</b>
     */
    private static Map<String, String> getStartOptions(ServiceDeploymentPlan plan) {
        Map<String, String> options = new HashMap<String, String>();

        Map<String, String> ensembles = plan.getEnsembles();
        if (null != ensembles && ensembles.size() > 0) {
            options.put(ServicesClient.OPTION_ENSEMBLE, JsonUtils.toJson(ensembles));
        }
        
        List<String> args = plan.getArguments();
        if (null != args && args.size() > 0) {
            options.put(ServicesClient.OPTION_ARGS, JsonUtils.toJson(args));
        }

        Map<String, String> servers = plan.getServers();
        if (null != servers && servers.size() > 0) {
            options.put(ServicesClient.OPTION_SERVERS, JsonUtils.toJson(servers));
        }

        Map<String, Map<String, String>> sParams = plan.getServiceParams();
        if (null != sParams && sParams.size() > 0) {
            options.put(ServicesClient.OPTION_PARAMS, JsonUtils.toJson(sParams));
        }

        if (options.size() == 0) {
            options = null;
        }
        
        return options;
    }
    
    /**
     * Deploys a {@link ServiceDeploymentPlan} given in terms of an URI.
     * 
     * @param plan the plan to be deployed
     * @return the application instance id of the started application, <b>null</b> for none
     * @throws ExecutionException if deploying the plain fails
     */
    protected static String deployPlan(URI plan) throws ExecutionException {
        ServiceDeploymentPlan p = loadPlan(plan);
        String appInstanceId = null;
        try {
            if (!p.isMultiExecution()) {
                ApplicationInstancesAasClient cl = new ApplicationInstancesAasClient();
                if (cl.getInstanceCount(p.getAppId(), p.getId()) > 0) {
                    throw new ExecutionException("Plan " + p.getId() + " is specified to prevent multiple execution of"
                        + " application " + p.getAppId() + ", which is already running", null);
                }
            }
            appInstanceId = PlatformAas.notifyAppNewInstance(p.getAppId(), p.getId());
            deployContainers(p);
            URI artifact = toUri(p.getArtifact());
            Map<String, ServicesClient> serviceClients = new HashMap<>();
            for (ServiceResourceAssignment a: p.getAssignments()) {
                URI art = getArtifact(artifact, a);
                try {
                    println("Adding artifact '" + art + "' to resource " + a.getResource());
                    ServicesClient client = getServicesFactory().create(a.getResource(), p.getAppId());
                    serviceClients.put(a.getResource(), client);
                    client.addArtifact(art);
                    printlnDone();
                } catch (ExecutionException e) {
                    checkAlreadyKnown(e, ServicesClient.EXC_ALREADY_KNOWN, "artifact", art.toString());
                }
            }
            ExecutorService es = p.isParallelize() ? Executors.newCachedThreadPool() : null;
            List<StartServicesRunnable> runnables = new ArrayList<>();
            Map<String, String> options = getStartOptions(p);
            for (ServiceResourceAssignment a: p.getAssignments()) {
                StartServicesRunnable r = new StartServicesRunnable(a.getResource(),
                    serviceClients.get(a.getResource()), a.getServicesAsArray(p.getAppId(), appInstanceId), 
                        options);
                runnables.add(r);
                if (null != es) {
                    es.execute(r);
                } else {
                    r.run();
                }
            }
            boolean finished = true;
            if (null != es) {
                es.shutdown();
                try {
                    finished = es.awaitTermination(3, TimeUnit.MINUTES);
                    if (!finished) {
                        println("Not all start commands finished within timeout");
                    }
                } catch (InterruptedException e) {
                    PlatformAas.notifyAppInstanceStopped(p.getAppId(), appInstanceId);
                    throw new ExecutionException(e);
                }
            }
            String msg = "";
            for (StartServicesRunnable r : runnables) {
                String tmp = r.getExceptionMessage();
                if (null != tmp) {
                    if (msg.length() > 0) {
                        msg = msg + ", ";
                    }
                    msg = msg + tmp;
                }
            }
            if (msg.length() > 0) {
                PlatformAas.notifyAppInstanceStopped(p.getAppId(), appInstanceId);
                throw new ExecutionException(msg, null);
            }
        } catch (IOException | URISyntaxException e) {
            PlatformAas.notifyAppInstanceStopped(p.getAppId(), appInstanceId);
            throw new ExecutionException(e);
        }
        return appInstanceId;
    }
    
    /**
     * Returns whether the resource with {@code resourceId} still has running service instances.
     * 
     * @param stillRunning the map of resources and instances after terminating requested services
     * @param resourceId the resource id
     * @return {@code true} for still running, {@code false} else
     */
    private static boolean isStillRunning(Map<String, Integer> stillRunning, String resourceId) {
        Integer instances = stillRunning.get(resourceId);
        if (null == instances) {
            instances = 0;
        }
        return instances > 0;
    }

    /**
     * Undeploys a {@link ServiceDeploymentPlan} given in terms of an URI.
     * 
     * @param plan the plan to be deployed
     * @param appInstanceId the application instance id, may be empty or <b>null</b> for legacy/default starts
     * @throws ExecutionException if deploying the plain fails
     */
    protected static void undeployPlan(URI plan, String appInstanceId) throws ExecutionException {
        ServiceDeploymentPlan p = loadPlan(plan);
        try {
            URI artifact = toUri(p.getArtifact());
            Map<String, ServicesClient> serviceClients = new HashMap<>();
            Map<String, Integer> stillRunning = new HashMap<>();
            List<ServiceResourceAssignment> assignments = new ArrayList<>(p.getAssignments());
            Collections.reverse(assignments);
            for (ServiceResourceAssignment a: assignments) {
                ServicesClient client = getServicesFactory().create(a.getResource(), p.getAppId());
                serviceClients.put(a.getResource(), client);
                String[] services = a.getServicesAsArray(p.getAppId(), appInstanceId);
                ArrayUtils.reverse(services);
                println("Stopping services " + Arrays.toString(services) + " on " + a.getResource());
                int running = 0;
                for (int i = 0; i < services.length; i++) {
                    running += Math.max(client.getServiceInstanceCount(services[i]) - 1, 0); // stopping one
                }
                stillRunning.put(a.getResource(), running);
                TaskData data = TaskRegistry.getTaskData();
                if (null != data) {
                    client.stopServiceAsTask(data.getId(), services);
                } else {
                    client.stopService(services);
                }
            }
            if (p.isOnUndeployRemoveArtifact()) {
                Set<String> done = new HashSet<String>();
                for (ServiceResourceAssignment a: p.getAssignments()) {
                    String resourceId = a.getResource();
                    if (!done.contains(resourceId) && !isStillRunning(stillRunning, resourceId)) {
                        done.add(resourceId);
                        URI art = getArtifact(artifact, a);
                        String uri = art.normalize().toString();
                        println("Removing artifact " + art + " from " + resourceId);
                        serviceClients.get(resourceId).removeArtifact(uri); // works also with Artifact URI
                    }
                }
            }
            List<ContainerResourceAssignment> container = new ArrayList<>(p.getContainer());
            Collections.reverse(container);
            for (ContainerResourceAssignment c : container) {
                String resourceId = c.getResource();
                if (!isStillRunning(stillRunning, resourceId)) {
                    EcsClient client = getEcsFactory().create(resourceId);
                    String cDescUri = toUri(c.getContainerDesc()).normalize().toString();
                    println("Stopping container '" + c.getContainerDesc() + " on resource " + c.getResource());
                    client.stopContainer(cDescUri);
                    printlnDone();
                    
                    if (p.isOnUndeployRemoveArtifact()) {
                        println("Removing container '" + c.getContainerDesc() + "' from resource " + c.getResource());
                        client.undeployContainer(cDescUri);
                        printlnDone();
                    }
                }
            }
            PlatformAas.notifyAppInstanceStopped(p.getAppId(), appInstanceId);
        } catch (URISyntaxException | IOException e) {
            throw new ExecutionException(e);
        }
    }
    
    /**
     * Resolves and loads the given {@code uri} to a {@link ServiceDeploymentPlan}, potentially downloading the 
     * contents of the URI to the temporary directory.
     * 
     * @param uri the URI pointing to the plain to resolve
     * @return the deployment plan instance
     * @throws ExecutionException if the resolution fails
     * @see UriResolver#resolveToFile(URI, File)
     * @see ServiceDeploymentPlan#readFromYaml(Class, java.io.InputStream)
     */
    protected static ServiceDeploymentPlan loadPlan(URI uri) throws ExecutionException {
        ServiceDeploymentPlan result = null;
        try {
            File f = UriResolver.resolveToFile(uri, null);
            FileInputStream fis = new FileInputStream(f);
            result = ServiceDeploymentPlan.readFromYaml(ServiceDeploymentPlan.class, fis);
            fis.close();
        } catch (IOException e) {
            throw new ExecutionException(e);
        }
        return result;
    }

    /**
     * Prints a sub-model using {@link PrintVisitor}.
     * 
     * @param submodel the sub-model to print
     * @param filter optional submodel elements collection filter, may be <b>null</b> for none
     * @param collPrefix a prefix string to be printed before the name of collection, may be <b>null</b> for not 
     *     printing the collection name
     * @param skipLevel how to handle printout per level, if not given {@link PrintType#PREFIX} is used
     */
    private static void print(Submodel submodel, String collPrefix, Predicate<SubmodelElementCollection> filter, 
        PrintType... skipLevel) {
        if (null != submodel) {
            PrintVisitor vis = new PrintVisitor(collPrefix, filter, platformFactory, skipLevel);
            submodel.accept(vis);
        } else {
            println("None.");
        }
    }

    /**
     * Prints {@code text} with linebreak if {@code provider} {@link CommandProvider#isInteractive()}.
     * 
     * @param text the text
     * @param provider the command provider
     */
    protected static void println(String text, CommandProvider provider) {
        println(text, provider.isInteractive());
    }

    /**
     * Prints {@code text} with linebreak if {@code enabled}.
     * 
     * @param text the text
     * @param enabled whether printing is enabled
     */
    private static void println(String text, boolean enabled) {
        if (enabled) {
            System.out.println(text);
        }
    }

    /**
     * Prints the given throwable.
     * 
     * @param th the throwable
     */
    private static void println(Throwable th) {
        println("ERROR: " + th.getMessage());
        //th.printStackTrace();        
    }

    /**
     * Prints {@code text} as errorwith linebreak. Informs {@link #errorConsumer},
     * 
     * @param text the text
     */
    protected static void error(String text) {
        errorConsumer.accept(text);
        println(text, true);
    }

    /**
     * Prints {@code text} with linebreak.
     * 
     * @param text the text
     */
    static void println(String text) {
        println(text, true);
    }

    /**
     * Prints that a command was successfully executed.
     */
    static void printlnDone() {
        println("Done.");
    }

    /**
     * Prints the interactive prompt.
     * 
     * @param level the prompt/command level
     * @param provider the command provider indicating whether we are running in interactive mode
     */
    private static void prompt(Level level, CommandProvider provider) {
        if (provider.isInteractive()) {
            System.out.print(level.getPrompt() + "> ");
        }
    }

    /**
     * Turns a given path/URI text into an URI. Adds "file:/" if there is no ":" in {@code text}.
     * 
     * @param text the text to be turned into an URI
     * @return the URI
     * @throws URISyntaxException if no URI can be constructed due to syntax errors
     */
    protected static URI toUri(String text) throws URISyntaxException {
        URI result;
        if (null == text) {
            text = ""; // shall result in an URISyntaxException, not an NPE  
        }
        if (text.indexOf(':') < 0) {
            File f = new File(text).getAbsoluteFile();
            result = f.toURI();
        } else {
            result = new URI(text);
        }
        return result;
    }
    
    /**
     * Represents a CLI function with a single typed parameter.
     * 
     * @param <P> the type of the parameter
     * @author Holger Eichelberger, SSE
     */
    protected interface CliFunction<P> {
        
        /**
         * Executes the function.
         * 
         * @param param the parameter
         * @throws ExecutionException if the execution fails for a given reason
         */
        public void apply(P param) throws ExecutionException;
        
    }

    /**
     * Calls the given function {@code func} after requesting a path or an URI from the {@code provider}.
     * 
     * @param provider the provider
     * @param func the function to be executed
     * @return {@code true} for successful execution, {@code false} if not successful (and no exception is thrown)
     * @throws ExecutionException if the execution of {@code func} fails for some reason
     * @throws URISyntaxException if the required UI cannot be constructed
     * @see #toUri(String)
     */
    protected static boolean callWithUri(CommandProvider provider, CliFunction<URI> func) 
        throws ExecutionException, URISyntaxException {
        boolean done = false;
        String uri = provider.nextCommand();
        if (null == uri) {
            error("No URI given.");
        } else {
            func.apply(toUri(uri));
            printlnDone();
            done = true;
        }
        return done;
    }

    /**
     * Calls the given function {@code func} after requesting an artifact id from the {@code provider}.
     * 
     * @param provider the provider
     * @param func the function to be executed
     * @return {@code true} for successful execution, {@code false} if not successful (and no exception is thrown)
     * @throws ExecutionException if the execution of {@code func} fails for some reason
     * @see #callWithId(CommandProvider, String, CliFunction)
     */
    protected static boolean callWithArtifactId(CommandProvider provider, CliFunction<String> func) 
        throws ExecutionException {
        return callWithId(provider, "artifactId", func);
    }

    /**
     * Calls the given function {@code func} after requesting a container id from the {@code provider}.
     * 
     * @param provider the provider
     * @param func the function to be executed
     * @return {@code true} for successful execution, {@code false} if not successful (and no exception is thrown)
     * @throws ExecutionException if the execution of {@code func} fails for some reason
     * @see #callWithId(CommandProvider, String, CliFunction)
     */
    protected static boolean callWithContainerId(CommandProvider provider, CliFunction<String> func) 
        throws ExecutionException {
        return callWithId(provider, "containerId", func);
    }

    /**
     * Calls the given function {@code func} after requesting a resource id from the {@code provider}.
     * 
     * @param provider the provider
     * @param func the function to be executed
     * @return {@code true} for successful execution, {@code false} if not successful (and no exception is thrown)
     * @throws ExecutionException if the execution of {@code func} fails for some reason
     * @see #callWithId(CommandProvider, String, CliFunction)
     */
    protected static boolean callWithResourceId(CommandProvider provider, CliFunction<String> func) 
        throws ExecutionException {
        return callWithId(provider, "resourceId", func);
    }

    /**
     * Calls the given function {@code func} after requesting an id from the {@code provider}.
     * 
     * @param provider the provider
     * @param idName the "name" of the id to be emitted if no id is given
     * @param func the function to be executed
     * @return {@code true} for successful execution, {@code false} if not successful (and no exception is thrown)
     * @throws ExecutionException if the execution of {@code func} fails for some reason
     */
    protected static boolean callWithId(CommandProvider provider, String idName, CliFunction<String> func) 
        throws ExecutionException {
        boolean done = false;
        String id = provider.nextCommand();
        if (null == id) {
            System.out.println("No " + idName + " given.");
        } else {
            func.apply(id);
            printlnDone();
            done = true;
        }
        return done;
    }

}
