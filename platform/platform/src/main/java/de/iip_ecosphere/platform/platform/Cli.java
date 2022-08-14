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
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

import de.iip_ecosphere.platform.ecsRuntime.EcsClient;
import de.iip_ecosphere.platform.platform.cli.ArgsCommandProvider;
import de.iip_ecosphere.platform.platform.cli.CommandProvider;
import de.iip_ecosphere.platform.platform.cli.Level;
import de.iip_ecosphere.platform.platform.cli.ScannerCommandProvider;
import de.iip_ecosphere.platform.platform.cli.PrintVisitor.PrintType;
import de.iip_ecosphere.platform.services.ServicesClient;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.support.iip_aas.IipVersion;
import de.iip_ecosphere.platform.support.semanticId.SemanticIdResolver;

/**
 * A simple (optional interactive) command line client providing initial platform functionality through the various AAS.
 * Just initial rather than all potential commands are provided.
 * 
 * @author Holger Eichelberger, SSE
 */
public class Cli extends CliBackend {

    /**
     * The command line main function.
     * 
     * @param args interactive mode if no arguments are given, else one shot execution (may be 
     */
    public static void main(String[] args) {
        PlatformSetup setup = PlatformSetup.getInstance();
        AasPartRegistry.setAasSetup(setup.getAas());
        CommandProvider provider;
        if (0 == args.length) {
            provider = new ScannerCommandProvider(new Scanner(System.in));
            println("IIP-Ecosphere, interactive platform command line " + IipVersion.getInstance().getVersion() + ".");
            println("AAS server: " + setup.getAas().getServerEndpoint().toUri());
            println("AAS registry: " + setup.getAas().getRegistryEndpoint().toUri());
            println("Type \"help\" to see commands and their description.");
        } else {
            provider = new ArgsCommandProvider(args);
        }
        SemanticIdResolver.resolve(""); // warm-up, initialize
        TopLevelCommandInterpreter tci = new TopLevelCommandInterpreter();
        tci.interpret(provider, Level.TOP);
        //interpretTopLevel(provider);
    }
    
    /**
     * A basic command interpreter.
     * 
     * @author Holger Eichelberger, SSE
     */
    private abstract static class AbstractHelpCommandInterpreter extends AbstractCommandInterpreter {

        /**
         * Prints the help.
         * 
         * @param provider the command provider
         * @param level the actual prompt level
         */
        void printHelp(CommandProvider provider, Level level) {
            if (level.isTopLevel()) {
                println("IIP-Ecosphere simple platform client. Commands (in levels) are:");
                println(" services <resourceId> - enters service commands for <resourceId>");
            }
            if (level.isTopLevel() || Level.SERVICES == level) {
                println("  listArtifacts - lists known artifacts");
                println("  listServices - lists known services");
                println("  add <path/URI> - adds an artifact");
                println("  startAll <artifactId> - starts all services in <artifactId>");
                println("  start <serviceId>+ . - starts the given services, note the \".\" at the end");
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
                println("  add <path/URI> - adds a container via its descriptor");
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
                println("  createSsh <resourceId> - creates an SSH server at resource <resourceId>");
                println("  help - prints help for this level");
                println("  back - to previous level", provider);
                println("  .. - to previous level", provider);
                println("  exit - exits the program", provider);
            }
            if (level.isTopLevel()) {
                println(" deploy <file/URI> runs the given deployment plan");
                println(" undeploy <file/URI> reverts the given deployment plan");
                println(" snapshotAAS - creates a snapshot of the AAS of the platform");
                println(" help - prints help for this program");
                println(" exit - exits the program", provider);
            }
        }
        
    }


    /**
     * The top-level command interpreter.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class TopLevelCommandInterpreter extends AbstractHelpCommandInterpreter {

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
            case "deploy":
                callWithUri(provider, uri -> deployPlan(uri));
                break;
            case "undeploy":
                callWithUri(provider, uri -> undeployPlan(uri));
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
    private static class ServicesCommandInterpreter extends AbstractHelpCommandInterpreter {

        private boolean changedServices = false;
        private boolean changedArtifacts = false;
        private ServicesClient client;

        @Override
        protected void initialize(String... args) throws IOException {
            client = getServicesFactory().create(args[0]);
        }
        
        @Override
        protected boolean interpretFurther(CommandProvider provider, Level level, String cmd) 
            throws ExecutionException, URISyntaxException {
            boolean exit = false;
            switch (cmd.toLowerCase()) {
            case "listservices":
                changedServices = checkReload(client.getServices(), changedServices);
                print(client.getServices(), "- Service ", null, PrintType.NO, PrintType.PREFIX);
                break;
            case "listartifacts":
                changedArtifacts = checkReload(client.getArtifacts(), changedArtifacts);
                print(client.getArtifacts(), "- Artifact ", null, PrintType.NO, PrintType.PREFIX);
                break;
            case "add":
                changedArtifacts = callWithUri(provider, uri -> client.addArtifact(uri));
                break;
            case "startall":
                changedServices = callWithArtifactId(provider, id -> client.startService(client.getServices(id, true)));
                break;
            case "start":
                List<String> sIds = new ArrayList<String>();
                String tmp; 
                while (true) {
                    tmp = provider.nextCommand();
                    if (null == tmp) {
                        sIds = null;
                        error("No serviceId given.");
                        break;
                    } else if (!".".equals(tmp)) {
                        sIds.add(tmp);
                    } else {
                        break;
                    }
                    if (null != sIds) {
                        String[] sTmp = new String[sIds.size()];
                        client.startService(sIds.toArray(sTmp));
                        changedServices = true;
                    }
                }
                break;
            case "stopall":
                changedServices = callWithArtifactId(provider, id -> client.stopService(client.getServices(id, true)));
                break;
            case "remove":
                changedArtifacts = callWithArtifactId(provider, id -> client.removeArtifact(id));
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
    private static class ContainerCommandInterpreter extends AbstractHelpCommandInterpreter {

        private boolean changed = false;
        private EcsClient client;
        
        @Override
        protected void initialize(String... args) throws IOException {
            client = getEcsFactory().create(args[0]);
        }
        
        @Override
        protected boolean interpretFurther(CommandProvider provider, Level level, String cmd) 
            throws ExecutionException, URISyntaxException {
            boolean exit = false;
            switch (cmd.toLowerCase()) {
            case "list":
                changed = checkReload(client.getContainers(), changed);
                print(client.getContainers(), "- Container ", null, PrintType.NO, PrintType.PREFIX);
                break;
            case "add":
                changed = callWithUri(provider, uri -> client.addContainer(uri));
                break;
            case "start":
                changed = callWithContainerId(provider, id -> client.startContainer(id));
                break;
            case "stop":
                changed = callWithContainerId(provider, id -> client.stopContainer(id));
                break;
            case "undeploy":
                changed = callWithContainerId(provider, id -> client.undeployContainer(id));
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
    private static class ResourcesCommandInterpreter extends AbstractHelpCommandInterpreter {
        
        @Override
        protected boolean interpretFurther(CommandProvider provider, Level level, String cmd) 
            throws ExecutionException, URISyntaxException {
            boolean exit = false;
            switch (cmd.toLowerCase()) {
            case "list":
                listResources();
                break;
            case "createssh":
                callWithResourceId(provider, id -> createSshServer(id));
                break;
            default:
                exit = super.interpretFurther(provider, level, cmd);
                break;
            }
            return exit;
        }
        
    }

}
