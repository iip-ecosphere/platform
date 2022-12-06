/**
 * ******************************************************************************
 * Copyright (c) {2022} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.maven;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import de.iip_ecosphere.platform.maven.PomReader.PomInfo;

/**
 * Changes POM versions.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ChangePomVersion {

    public static final String PARAM_PREFIX = "--";
    public static final String PARAM_VALUE_SEP = "=";

    
    private String oldPomVersion;
    private String newPomVersion;
    private String oldParentPomVersion;
    private String newParentPomVersion;
    private Boolean simulate;
    
    private Predicate<String> incPattern;
    private Predicate<String> excPattern;
    
    private Set<String> properties = new HashSet<>();

    /**
     * Creates a program instance.
     * 
     * @param args the command line arguments
     * @throws PatternSyntaxException if a RegEx is invalid
     */
    private ChangePomVersion(String[] args) throws PatternSyntaxException {
        oldPomVersion = getArg(args, "oldPOMVersion", "");
        newPomVersion = getArg(args, "newPOMVersion", "");
        oldParentPomVersion = getArg(args, "oldParentPOMVersion", "");
        newParentPomVersion = getArg(args, "newParentPOMVersion", "");
        String includes = getArg(args, "includes", "");
        String excludes = getArg(args, "excludes", ".*/gen/.*");
        simulate = Boolean.valueOf(getArg(args, "simulate", "false"));
        for (int i = 0; !simulate && i < args.length; i++) {
            simulate = args[i].equals("--simulate"); // safe side...
        }
        Collections.addAll(properties, getArg(args, "properties", "").replace(';', ':').split(":"));
        
        if (includes.length() == 0) {
            includes = ".*/pom(-model)?.xml$";
        }
        incPattern = Pattern.compile(includes).asPredicate();
        excPattern = excludes.length() == 0 ? s -> false : Pattern.compile(excludes).asPredicate();
    }
    
    /**
     * Returns whether the setup is valid.
     * 
     * @return {@code true} for valid, {@code false} else
     */
    private boolean isValid() {
        return ((oldPomVersion.length() > 0 && newPomVersion.length() > 0) 
            || (oldParentPomVersion.length() > 0 && newParentPomVersion.length() > 0));
    }

    /**
     * Scans {@code file}.
     * 
     * @param file file or directory to scan for processing
     */
    private void scan(File file) {
        if (file.isFile()) {
            if (isOk(file)) {
                process(file);
            }
        } else if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (null != files) {
                for (File f: files) {
                    if (isOk(f)) {
                        scan(f);
                    }
                }
            }
        }
    }
    
    /**
     * Processes {@code file}.
     * 
     * @param file the file (not directory) to process
     */
    private void process(File file) {
        System.out.print(file.getAbsolutePath() + ": ");
        if (simulate) {
            PomInfo info = PomReader.getInfo(file);
            if (null != info) {
                if (PomReader.equalsSafe(info.getVersion(), oldPomVersion)) {
                    System.out.println("not changed (version -> " + newPomVersion + ")");
                } else if (PomReader.equalsSafe(info.getParentVersion(), oldParentPomVersion)) {
                    System.out.println("not changed (parent -> " + newParentPomVersion + ")");
                } else {
                    System.out.println("not changed (-> wrong version)");
                }
            } 
        } else {
            try {
                PomReader.replaceVersion(file, oldPomVersion, newPomVersion, oldParentPomVersion, newParentPomVersion, 
                    properties);
                System.out.println("done");
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    /**
     * Returns whether a file is ok according to inclusion and exclusion patterns.
     * 
     * @param file the file to check
     * @return {@code true} for ok, {@code false} else
     */
    private boolean isOk(File file) {
        boolean result;
        String path = file.getAbsolutePath().replace('\\', '/');
        if (file.isFile()) {
            result = incPattern.test(path) && !excPattern.test(path);
        } else {
            result = !excPattern.test(path); // directories are never "included"
        }
        return result;
    }

    /**
     * Emulates reading a Spring-like parameter if the configuration is not yet in place.
     * 
     * @param args the arguments
     * @param argName the argument name (without {@link #PARAM_PREFIX} or {@link #PARAM_VALUE_SEP})
     * @param dflt the default value if the argument cannot be found
     * @return the value of argument or {@code deflt}
     */
    public static String getArg(String[] args, String argName, String dflt) {
        String result = dflt;
        String prefix = PARAM_PREFIX + argName + PARAM_VALUE_SEP;
        for (int a = 0; a < args.length; a++) {
            String arg = args[a];
            if (arg.startsWith(prefix)) {
                result = arg.substring(prefix.length());
                break;
            }
        }
        return result;
    }
    
    /**
     * Changes POM versions.
     * 
     * @param args
     */
    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Changes POM versions file");
            System.out.println(" file/folder");
            System.out.println(" --oldPOMVersion=<ver>");
            System.out.println(" --newPOMVersion=<ver>");
            System.out.println(" --oldParentPOMVersion=<ver>");
            System.out.println(" --newParentPOMVersion=<ver>");
            System.out.println(" --includes=.*/pom(-model)?.xml$; regEx match all paths with /");
            System.out.println(" --excludes=.*/gen/.*; regEx match all paths with /");
            System.out.println(" --properties=<str> : or ; separated list of properties to replace POM version");
            System.out.println(" --simulate=true/false; do not execute");
        } else {
            File f = new File(args[0]);
            try {
                ChangePomVersion prog = new ChangePomVersion(args);
                if (!prog.isValid()) {
                    System.out.println("One pair of old/new versions must be given.");
                } else {
                    System.out.println("Scanning...");
                    prog.scan(f);
                    System.out.println("Done...");
                }
            } catch (PatternSyntaxException e) {
                System.out.println(e.getMessage());
            }
        }
    }
    
}
