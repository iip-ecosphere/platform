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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import de.iip_ecosphere.platform.ecsRuntime.EcsClient;
import de.iip_ecosphere.platform.platform.cli.ArgsCommandProvider;
import de.iip_ecosphere.platform.platform.cli.CommandProvider;
import de.iip_ecosphere.platform.platform.cli.EcsClientFactory;
import de.iip_ecosphere.platform.platform.cli.Level;
import de.iip_ecosphere.platform.platform.cli.PrintVisitor;
import de.iip_ecosphere.platform.platform.cli.ResourcesClientFactory;
import de.iip_ecosphere.platform.platform.cli.ScannerCommandProvider;
import de.iip_ecosphere.platform.platform.cli.ServicesClientFactory;
import de.iip_ecosphere.platform.platform.cli.PrintVisitor.PrintType;
import de.iip_ecosphere.platform.services.ServicesClient;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.SubmodelElement;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;

/**
 * A simple (optional interactive) command line client providing initial platform functionality through the various AAS.
 * Just initial rather than all potential commands are provided.
 * 
 * @author Holger Eichelberger, SSE
 */
public class Cli {

    public static final Consumer<String> DEFAULT_ERROR_CONSUMER = s -> { };
    private static ServicesClientFactory servicesFactory = ServicesClientFactory.DEFAULT;
    private static EcsClientFactory ecsFactory = EcsClientFactory.DEFAULT;
    private static ResourcesClientFactory resourcesFactory = ResourcesClientFactory.DEFAULT;
    private static Consumer<String> errorConsumer = DEFAULT_ERROR_CONSUMER;
    
    /**
     * Changes the client factories. [testing]
     * 
     * @param services the services client factory 
     * @param ecs the ECS client factory
     * @param resources the resources client factory
     */
    public static void setFactories(ServicesClientFactory services, EcsClientFactory ecs, 
        ResourcesClientFactory resources) {
        servicesFactory = services;
        ecsFactory = ecs;
        resourcesFactory = resources;
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
     * The command line main function.
     * 
     * @param args interactive mode if no arguments are given, else one shot execution (may be 
     */
    public static void main(String[] args) {
        PlatformConfiguration setup = PlatformConfiguration.getInstance();
        AasPartRegistry.setAasSetup(setup.getAas());
        CommandProvider provider;
        if (0 == args.length) {
            provider = new ScannerCommandProvider(new Scanner(System.in));
            println("IIP-Ecosphere, interactive platform command line");
            println("AAS server: " + setup.getAas().getServerEndpoint().toUri());
            println("AAS registry: " + setup.getAas().getRegistryEndpoint().toUri());
            println("Type \"help\" for help.");
        } else {
            provider = new ArgsCommandProvider(args);
        }
        TopLevelCommandInterpreter tci = new TopLevelCommandInterpreter();
        tci.interpret(provider, Level.TOP);
        //interpretTopLevel(provider);
    }
    
    private abstract static class AbstractCommandInterpreter {

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
         */
        protected boolean interpretFurther(CommandProvider provider, Level level, String cmd) 
            throws ExecutionException, URISyntaxException {
            unknownCommand(cmd);
            return false;
        }

    }

    /**
     * The top-level command interpreter.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class TopLevelCommandInterpreter extends AbstractCommandInterpreter {

        @Override
        protected boolean interpretFurther(CommandProvider provider, Level level, String cmd) 
            throws ExecutionException, URISyntaxException {
            boolean exit = false;
            String resourceId;
            switch (cmd.toLowerCase()) {
            case "services":
                resourceId = provider.nextCommand();
                if (null == resourceId) {
                    error("No resourceId given.");
                } else {
                    ServicesCommandInterpreter sci = new ServicesCommandInterpreter();
                    exit = sci.interpret(provider, Level.SERVICES, resourceId);
                }
                break;
            case "container":
                resourceId = provider.nextCommand();
                if (null == resourceId) {
                    error("No resourceId given.");
                } else {
                    ContainerCommandInterpreter cci = new ContainerCommandInterpreter();
                    exit = cci.interpret(provider, Level.CONTAINER, resourceId);
                }
                break;
            case "resources":
                ResourcesCommandInterpreter rci = new ResourcesCommandInterpreter();
                exit = rci.interpret(provider, Level.RESOURCES);
                break;
            default:
                exit = super.interpretFurther(provider, level, cmd);
                break;
            }            
            return exit;
        }

    }

    /**
     * The service-level command interpreter.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class ServicesCommandInterpreter extends AbstractCommandInterpreter {

        private boolean changedServices = false;
        private boolean changedArtifacts = false;
        private ServicesClient client;

        @Override
        protected void initialize(String... args) throws IOException {
            client = servicesFactory.create(args[0]);
        }
        
        @Override
        protected boolean interpretFurther(CommandProvider provider, Level level, String cmd) 
            throws ExecutionException, URISyntaxException {
            boolean exit = false;
            String id;
            switch (cmd.toLowerCase()) {
            case "listservices":
                changedServices = checkReload(client.getServices(), changedServices);
                print(client.getServices(), "- Service ", PrintType.NO, PrintType.PREFIX);
                break;
            case "listartifacts":
                changedArtifacts = checkReload(client.getArtifacts(), changedArtifacts);
                print(client.getArtifacts(), "- Artifact ", PrintType.NO, PrintType.PREFIX);
                break;
            case "add":
                String uri = provider.nextCommand();
                if (null == uri) {
                    error("No URI given.");
                } else {
                    client.addArtifact(new URI(uri));
                    changedArtifacts = true;
                }
                break;
            case "startall":
                id = provider.nextCommand();
                if (null == id) {
                    error("No artifactId given.");
                } else {
                    client.startService(client.getServices(id));
                    changedServices = true;
                }
                break;
            case "stopall":
                id = provider.nextCommand();
                if (null == id) {
                    error("No artifactId given.");
                } else {
                    client.stopService(client.getServices(id));
                    changedServices = true;
                }
                break;
            case "remove":
                id = provider.nextCommand();
                if (null == id) {
                    error("No artifactId given.");
                    break;
                }
                client.removeArtifact(id);
                changedArtifacts = true;
                break;
            default:
                exit = super.interpretFurther(provider, level, cmd);
                break;
            }
            return exit;
        }
        
    }
    
    /**
     * The container-level command interpreter.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class ContainerCommandInterpreter extends AbstractCommandInterpreter {

        private boolean changed = false;
        private EcsClient client;
        
        @Override
        protected void initialize(String... args) throws IOException {
            client = ecsFactory.create(args[0]);
        }
        
        @Override
        protected boolean interpretFurther(CommandProvider provider, Level level, String cmd) 
            throws ExecutionException, URISyntaxException {
            boolean exit = false;
            switch (cmd.toLowerCase()) {
            case "list":
                changed = checkReload(client.getContainers(), changed);
                print(client.getContainers(), "- Container ", PrintType.PREFIX);
                break;
            case "add":
                String uri = provider.nextCommand();
                if (null == uri) {
                    System.out.println("No URI given.");
                } else {
                    client.addContainer(new URI(uri));
                    changed = true;
                }
                break;
            case "start":
                String id = provider.nextCommand();
                if (null == id) {
                    System.out.println("No Id given.");
                } else {
                    client.startContainer(id);
                    changed = true;
                }
                break;
            case "stop":
                id = provider.nextCommand();
                if (null == id) {
                    System.out.println("No Id given.");
                } else {
                    client.stopContainer(id);
                    changed = true;
                }
                break;
            case "undeploy":
                id = provider.nextCommand();
                if (null == id) {
                    System.out.println("No Id given.");
                    break;
                }
                client.undeployContainer(id);
                changed = true;
                break;
            default:
                exit = super.interpretFurther(provider, level, cmd);
                break;
            }
            return exit;
        }
        
    }

    /**
     * The resource-level command interpreter.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class ResourcesCommandInterpreter extends AbstractCommandInterpreter {
        
        @Override
        protected boolean interpretFurther(CommandProvider provider, Level level, String cmd) 
            throws ExecutionException, URISyntaxException {
            boolean exit = false;
            switch (cmd.toLowerCase()) {
            case "list":
                listResources();
                break;
            default:
                exit = super.interpretFurther(provider, level, cmd);
                break;
            }
            return exit;
        }
        
    }
    
    /**
     * Checks whether a reload is needed.
     * 
     * @param coll the respective collection that may be subject to a reload
     * @param changed the flag indicating whether a reload is needed
     * @return {@code false}
     */
    private static boolean checkReload(SubmodelElementCollection coll, boolean changed) {
        if (changed && null != coll) {
            coll.update();
            changed = false;
        }
        return changed;
    }

    /**
     * Lists the resources available to the platform.
     */
    private static void listResources() {
        try {
            print(resourcesFactory.create().getResources(), "- Resource ", PrintType.PREFIX);
        } catch (IOException e) {
            println(e);
        }
    }
    
    /**
     * Prints a sub-model element using {@link PrintVisitor}.
     * 
     * @param elt the element to print
     * @param collPrefix a prefix string to be printed before the name of collection, may be <b>null</b> for not 
     *     printing the collection name
     * @param skipLevel how to handle printout per level, if not given {@link PrintType#PREFIX} is used
     */
    private static void print(SubmodelElement elt, String collPrefix, PrintType... skipLevel) {
        if (null != elt) {
            PrintVisitor vis = new PrintVisitor(collPrefix, skipLevel);
            elt.accept(vis);
        } else {
            println("None.");
        }
    }

    /**
     * Prints a sub-model using {@link PrintVisitor}.
     * 
     * @param submodel the sub-model to print
     * @param collPrefix a prefix string to be printed before the name of collection, may be <b>null</b> for not 
     *     printing the collection name
     * @param skipLevel how to handle printout per level, if not given {@link PrintType#PREFIX} is used
     */
    private static void print(Submodel submodel, String collPrefix, PrintType... skipLevel) {
        if (null != submodel) {
            PrintVisitor vis = new PrintVisitor(collPrefix, skipLevel);
            submodel.accept(vis);
        } else {
            println("None.");
        }
    }

    /**
     * Prints the help.
     * 
     * @param provider the command provider
     * @param level the actual prompt level
     */
    private static void printHelp(CommandProvider provider, Level level) {
        if (level.isTopLevel()) {
            println("IIP-Ecosphere simple platform client. Commands (in levels) are:");
            println(" services <resourceId> - enters service commands for <resourceId>");
        }
        if (level.isTopLevel() || Level.SERVICES == level) {
            println("  listServices - lists known services");
            println("  listArtifacts - lists known artifacts");
            println("  add <URI> - addes an artifact");
            println("  startAll <artifactId> - starts all services in <artifactId>");
            println("  stopAll <artifactId> - stops all services in <artifactId>");
            println("  remove <artifactId> - removes <artifactId>");
            println("  help - prints help for this level");
            println("  back - to previous level", provider);
            println("  ..  - to previous level", provider);
            println("  exit - exits the program", provider);
        }
        if (level.isTopLevel()) {
            println(" container <resourceId> - enters the container command level for <resourceId>");
        }
        if (level.isTopLevel() || Level.CONTAINER == level) {
            println("  list - lists all known container");
            println("  add <URI> - adds a container via its descriptor");
            println("  start <containerId> - starts <containerId>");
            println("  stop <containerId> - stops <containerId>");
            println("  undeploy <containerId> - removes container <containerId>");
            println("  help - prints help for this level");
            println("  back - to previous level", provider);
            println("  .. - to previous level", provider);
            println("  exit - exits the program", provider);
        }
        if (level.isTopLevel()) {
            println(" resources - enters the platform resource command level");
        }
        if (level.isTopLevel() || Level.RESOURCES == level) {
            println("  list - lists all known resources");
            println("  help - prints help for this level");
            println("  back - to previous level", provider);
            println("  .. - to previous level", provider);
            println("  exit - exits the program", provider);
        }
        if (level.isTopLevel()) {
            println(" help - prints help for this program");
            println(" exit - exits the program", provider);
        }
    }

    /**
     * Prints {@code text} with linebreak if {@code provider} {@link CommandProvider#isInteractive()}.
     * 
     * @param text the text
     * @param provider the command provider
     */
    private static void println(String text, CommandProvider provider) {
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
    private static void error(String text) {
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

}
