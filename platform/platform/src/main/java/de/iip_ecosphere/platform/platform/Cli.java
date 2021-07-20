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
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

import de.iip_ecosphere.platform.ecsRuntime.EcsAasClient;
import de.iip_ecosphere.platform.services.ServicesAasClient;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.AasVisitor;
import de.iip_ecosphere.platform.support.aas.Asset;
import de.iip_ecosphere.platform.support.aas.Operation;
import de.iip_ecosphere.platform.support.aas.Property;
import de.iip_ecosphere.platform.support.aas.ReferenceElement;
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

    /**
     * The input/shell level.
     * 
     * @author Holger Eichelberger, SSE
     */
    private enum Level {
        TOP(""),
        SERVICES("services"),
        CONTAINER("container"),
        RESOURCES("resources");
        
        private String prompt;
        
        /**
         * Creates a level constant with prompt text.
         * 
         * @param prompt the prompt text
         */
        private Level(String prompt) {
            this.prompt = prompt;
        }
        
        /**
         * Returns the prompt text.
         * 
         * @return the prompt text
         */
        public String getPrompt() {
            return prompt;
        }
        
        /**
         * Returns whether this level is top-level.
         * 
         * @return {@code true} for top-level, {@code false} else
         */
        public boolean isTopLevel() {
            return TOP == this;
        }
        
    }
    
    /**
     * Provides access to incremental command input.
     * 
     * @author Holger Eichelberger, SSE
     */
    private interface CommandProvider {
        
        /**
         * Returns the next command.
         * 
         * @return the next command, <b>null</b> for none/end of input
         */
        public String nextCommand();
        
        /**
         * Returns whether this provider is interactive.
         * 
         * @return <code>true</code> for interactive, <code>false</code> for static/one shot
         */
        public boolean isInteractive();
    }
    
    /**
     * A command line provider wrapping interactive command line commands.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class ScannerCommandProvider implements CommandProvider {

        private Scanner scanner;
        private ArrayList<String> cmds = new ArrayList<String>();
        private int pos = 0;

        /**
         * Creates the command line provider.
         * 
         * @param scanner the scanner to be used
         */
        private ScannerCommandProvider(Scanner scanner) {
            this.scanner = scanner;
        }

        @Override
        public String nextCommand() {
            String result = null;
            if (pos >= 0 && pos < cmds.size()) {
                result = cmds.get(pos++);
            } else if (pos >= 0) {
                String line = scanner.nextLine();
                if (null != line) {
                    cmds.clear();
                    boolean inQuote = false;
                    line = line.trim();
                    int lastStart = 0;
                    for (int i = 0; i < line.length(); i++) {
                        char c = line.charAt(i);
                        if ('"' == c) {
                            inQuote = !inQuote;
                        } else if (' ' == c && !inQuote || i + 1 == line.length()) {
                            String cmd = line.substring(lastStart, i + 1).trim(); 
                            if (cmd.length() > 0) {
                                cmds.add(cmd);
                            }
                            lastStart = i + 1;
                        }
                    }
                    pos = 0;
                    if (cmds.size() > 0) {
                        result = cmds.get(pos++);
                    }
                } else {
                    pos = -1;
                }
            } 
            return result;
        }

        @Override
        public boolean isInteractive() {
            return true;
        }
        
    }
    
    /**
     * A command provider wrapping command line arguments.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class ArgsCommandProvider implements CommandProvider {
        
        private String[] args;
        private int pos = 0;
        
        /**
         * Creates a command provider based on command line arguments.
         * 
         * @param args the command line arguments
         */
        private ArgsCommandProvider(String[] args) {
            this.args = args;
        }

        @Override
        public String nextCommand() {
            String result;
            skipOptions();
            if (pos < args.length) {
                result = args[pos++];
            } else {
                result = null;
            }
            return result;
        }
        
        /**
         * Advances {@link #pos} until a command, skips options.
         */
        private void skipOptions() {
            // no options so far
        }

        @Override
        public boolean isInteractive() {
            return false;
        }
        
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
            println("Type \"help\" for help.");
        } else {
            provider = new ArgsCommandProvider(args);
        }
        interpretTopLevel(provider);
    }
    
    /**
     * Interprets the first/top command line level.
     * 
     * @param provider the command provider
     */
    private static void interpretTopLevel(CommandProvider provider) {
        final Level level = Level.TOP;
        String cmd;
        String resourceId;
        do {
            prompt(level, provider);
            cmd = provider.nextCommand();
            if (null != cmd) {
                switch (cmd.toLowerCase()) {
                case "services":
                    resourceId = provider.nextCommand();
                    if (null == resourceId) {
                        System.out.println("No resourceId given.");
                        break;
                    }
                    interpretServices(provider, resourceId);
                    break;
                case "container":
                    resourceId = provider.nextCommand();
                    if (null == resourceId) {
                        System.out.println("No resourceId given.");
                        break;
                    }
                    interpretContainer(provider, resourceId);
                    break;
                case "resources":
                    interpretResources(provider);
                    break;
                case "help":
                    printHelp(provider, level);
                    break;
                case "exit":
                    cmd = null;
                    break;
                default:
                    println("Unknown command on this level: " + cmd);
                    break;
                }
            }
        } while (null != cmd);
    }

    /**
     * Interprets the service commands.
     * 
     * @param provider the command provider
     * @param resourceId the resourceId of the resource to take the services from
     */
    private static void interpretServices(CommandProvider provider, String resourceId) {
        final Level level = Level.SERVICES;
        try {
            ServicesAasClient client = new ServicesAasClient(resourceId);
            String cmd;
            do {
                prompt(level, provider);
                cmd = provider.nextCommand();
                if (null != cmd) {
                    try {
                        String id;
                        switch (cmd.toLowerCase()) {
                        case "listServices":
                            print(client.getServices(), "- Service ");
                            break;
                        case "listArtifacts":
                            print(client.getArtifacts(), "- Artifact ");
                            break;
                        case "add":
                            String uri = provider.nextCommand();
                            if (null == uri) {
                                System.out.println("No URI given.");
                                break;
                            }
                            client.addArtifact(new URI(uri));
                            break;
                        case "startAll":
                            id = provider.nextCommand();
                            if (null == id) {
                                System.out.println("No artifactId given.");
                                break;
                            }
                            client.startService(client.getServices(id));
                            break;
                        case "stopAll":
                            id = provider.nextCommand();
                            if (null == id) {
                                System.out.println("No artifactId given.");
                                break;
                            }
                            client.stopService(client.getServices(id));
                            break;
                        case "remove":
                            id = provider.nextCommand();
                            if (null == id) {
                                System.out.println("No artifactId given.");
                                break;
                            }
                            client.removeArtifact(id);
                            break;
                        case "help":
                            printHelp(provider, level);
                            break;
                        case "back":
                            cmd = null;
                            break;
                        default:
                            println("Unknown command on this level: " + cmd);
                            break;
                        }
                    } catch (ExecutionException | URISyntaxException e) {
                        println(e);
                    }
                }
            } while (null != cmd);
        } catch (IOException e) {
            println(e);
        }
    }
    
    /**
     * Interprets the container commands.
     * 
     * @param provider the command provider
     * @param resourceId the resourceId of the resource to take the container from
     */
    private static void interpretContainer(CommandProvider provider, String resourceId) {
        final Level level = Level.CONTAINER;
        try {
            EcsAasClient client = new EcsAasClient(resourceId);
            String cmd;
            do {
                prompt(level, provider);
                cmd = provider.nextCommand();
                if (null != cmd) {
                    try {
                        switch (cmd.toLowerCase()) {
                        case "list":
                            print(client.getContainers(), "- Container ");
                            break;
                        case "add":
                            String uri = provider.nextCommand();
                            if (null == uri) {
                                System.out.println("No URI given.");
                                break;
                            }
                            client.addContainer(new URI(uri));
                            break;
                        case "start":
                            String id = provider.nextCommand();
                            if (null == id) {
                                System.out.println("No Id given.");
                                break;
                            }
                            client.startContainer(id);
                            break;
                        case "stop":
                            id = provider.nextCommand();
                            if (null == id) {
                                System.out.println("No Id given.");
                                break;
                            }
                            client.startContainer(id);
                            break;
                        case "undeploy":
                            id = provider.nextCommand();
                            if (null == id) {
                                System.out.println("No Id given.");
                                break;
                            }
                            client.undeployContainer(id);
                            break;
                        case "help":
                            printHelp(provider, level);
                            break;
                        case "back":
                            cmd = null;
                            break;
                        default:
                            println("Unknown command on this level: " + cmd);
                            break;
                        }
                    } catch (ExecutionException | URISyntaxException e) {
                        println(e);
                    }
                }
            } while (null != cmd);
        } catch (IOException e) {
            println(e);
        }
    }

    /**
     * Interprets the resources commands.
     * 
     * @param provider the command provider
     */
    private static void interpretResources(CommandProvider provider) {
        final Level level = Level.RESOURCES;
        String cmd;
        do {
            prompt(level, provider);
            cmd = provider.nextCommand();
            if (null != cmd) {
                switch (cmd.toLowerCase()) {
                case "list":
                    listResources();
                    break;
                case "help":
                    printHelp(provider, level);
                    break;
                case "back":
                    cmd = null;
                    break;
                default:
                    println("Unknown command on this level: " + cmd);
                    break;
                }
            }
        } while (null != cmd);
    }

    /**
     * Lists the resources available to the platform.
     */
    private static void listResources() {
        try {
            Aas aas = AasPartRegistry.retrieveIipAas();
            print(aas.getSubmodel(AasPartRegistry.NAME_SUBMODEL_RESOURCES), "- Resource ");
        } catch (IOException e) {
            println(e);
        }
    }
    
    /**
     * A visitor for printing a structured list of sub-model elements collections and their properties.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class PrintVisitor implements AasVisitor {
        
        private String collPrefix;
        private String indent = "";
        
        /**
         * Creates a visitor instance.
         * 
         * @param collPrefix a prefix string to be printed before the name of collection, may be <b>null</b> for not 
         *     printing the collection name
         */
        private PrintVisitor(String collPrefix) {
            this.collPrefix = collPrefix;
        }

        @Override
        public void visitAas(Aas aas) {
        }

        @Override
        public void endAas(Aas aas) {
        }

        @Override
        public void visitAsset(Asset asset) {
        }

        @Override
        public void visitSubmodel(Submodel submodel) {
            submodel.accept(this);
        }

        @Override
        public void endSubmodel(Submodel submodel) {
        }

        @Override
        public void visitProperty(Property property) {
            Object val;
            try {
                val = property.getValue();
            } catch (ExecutionException e) {
                val = "?";
            }
            println(indent + property.getIdShort() + " " + val);
        }

        @Override
        public void visitOperation(Operation operation) {
        }

        @Override
        public void visitReferenceElement(ReferenceElement referenceElement) {
        }

        @Override
        public void visitSubmodelElementCollection(SubmodelElementCollection collection) {
            if (null != collPrefix) {
                println(collPrefix + collection.getIdShort());
            }
            indent += " ";
            collection.accept(this);
            indent = indent.substring(0, indent.length() - 1);
        }

        @Override
        public void endSubmodelElementCollection(SubmodelElementCollection collection) {
        }
        
    }
    
    /**
     * Prints a sub-model element using {@link PrintVisitor}.
     * 
     * @param elt the element to print
     * @param collPrefix a prefix string to be printed before the name of collection, may be <b>null</b> for not 
     *     printing the collection name
     */
    private static void print(SubmodelElement elt, String collPrefix) {
        if (null != elt) {
            PrintVisitor vis = new PrintVisitor(collPrefix);
            elt.accept(vis);
        }
    }

    /**
     * Prints a sub-model using {@link PrintVisitor}.
     * 
     * @param submodel the sub-model to print
     * @param collPrefix a prefix string to be printed before the name of collection, may be <b>null</b> for not 
     *     printing the collection name
     */
    private static void print(Submodel submodel, String collPrefix) {
        if (null != submodel) {
            PrintVisitor vis = new PrintVisitor(collPrefix);
            submodel.accept(vis);
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
            println(" services <resourceId>");
        }
        if (level.isTopLevel() || Level.SERVICES == level) {
            println("  listServices");
            println("  listArtifacts");
            println("  add <URI>");
            println("  startAll <artifactId>");
            println("  stopAll <artifactId>");
            println("  remove <artifactId>");
            println("  help");
            println("  back", provider);
        }
        if (level.isTopLevel()) {
            println(" container <resourceId>");
        }
        if (level.isTopLevel() || Level.CONTAINER == level) {
            println("  list");
            println("  add <URI>");
            println("  start <containerId>");
            println("  stop <containerId>");
            println("  undeploy <containerId>");
            println("  help");
            println("  back", provider);
        }
        if (level.isTopLevel()) {
            println(" resources");
        }
        if (level.isTopLevel() || Level.RESOURCES == level) {
            println("  list");
            println("  help");
            println("  back", provider);
        }
        if (level.isTopLevel()) {
            println(" help");
            println(" exit", provider);
        }
    }

    /**
     * Prints {@code text} with linebreak if {@code provider} {@link Cli.CommandProvider#isInteractive()}.
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
        th.printStackTrace();        
    }
    
    /**
     * Prints {@code text} with linebreak.
     * 
     * @param text the text
     */
    private static void println(String text) {
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
