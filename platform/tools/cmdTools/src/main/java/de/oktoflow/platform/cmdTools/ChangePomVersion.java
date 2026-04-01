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

package de.oktoflow.platform.cmdTools;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import de.iip_ecosphere.platform.support.setup.CmdLine;
import de.oktoflow.platform.cmdTools.PomReader.PomInfo;
import de.oktoflow.platform.cmdTools.PomReader.Result;

/**
 * Changes POM versions.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ChangePomVersion {

    public static final String PARAM_PREFIX = "--";
    public static final String PARAM_VALUE_SEP = "=";
    private static final String SUFFIX_SNAPSHOT = "-SNAPSHOT";
    private static final String SUFFIX_QUALIFIER = ".qualifier";
    private static final String KEY_BUNDLE_VERSION = "Bundle-Version:";
    
    private String oldPomVersion;
    private String newPomVersion;
    private String oldParentPomVersion;
    private String newParentPomVersion;
    private boolean simulate;
    private boolean verbose;
    private boolean modifyManifest;
    
    private Predicate<String> incPattern;
    private Predicate<String> excPattern;
    private Predicate<String> groupIdPattern;
    
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
        String tmp = getArg(args, "includeGroupId", null);
        if (null != tmp && tmp.length() > 0) {
            groupIdPattern = Pattern.compile(tmp).asPredicate();
        }
        tmp = getArg(args, "excludeGroupId", null);
        if (null != tmp && tmp.length() > 0) {
            Predicate<String> p = Pattern.compile(tmp).asPredicate();
            if (null == groupIdPattern) {
                groupIdPattern = p;
            } else {
                groupIdPattern.and(p.negate());
            }
        }
        String includes = getArg(args, "includes", "");
        String excludes = getArg(args, "excludes", ".*/gen/.*");
        simulate = CmdLine.getBooleanArgNoVal(args, "simulate", false);
        verbose = CmdLine.getBooleanArgNoVal(args, "verbose", false);
        modifyManifest = CmdLine.getBooleanArgNoVal(args, "modifyManifest", false);
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
        String prefix = file.getAbsolutePath() + ": ";
        if (simulate) {
            PomInfo info = PomReader.getInfo(file, e -> System.out.println(prefix + e));
            if (null != info) {
                if (groupIdPattern == null || groupIdPattern.test(info.getGroupId())) {
                    if (PomReader.equalsSafe(info.getVersion(), oldPomVersion)) {
                        System.out.println(prefix + "would change version -> " + newPomVersion);
                    } else if (PomReader.equalsSafe(info.getParentVersion(), oldParentPomVersion)) {
                        System.out.println(prefix + "would change parent -> " + newParentPomVersion);
                    } else {
                        System.out.println(prefix + "not changed (-> wrong version)");
                    }
                } else {
                    if (verbose) {
                        System.out.println(prefix + "ignored");
                    }
                }
            } else {
                System.out.println(prefix + "no path");
            }
        } else {
            try {
                Result res = PomReader.replaceVersion(file, oldPomVersion, newPomVersion, oldParentPomVersion, 
                    newParentPomVersion, properties, groupIdPattern);
                String txt = prefix + res.name().toLowerCase();
                if (Result.MODIFIED == res && modifyManifest) {
                    String mf = "META-INF/MANIFEST.MF";
                    Result mRes = replaceManifestVersion(new File(file.getParentFile(), mf), 
                        oldPomVersion, newPomVersion);
                    txt += " " + mf + " " + mRes.name().toLowerCase();
                }
                System.out.println(txt);
            } catch (IOException e) {
                System.out.println(prefix + e.getMessage());
            }
        }
    }
    
    /**
     * Strips the suffix.
     * 
     * @param text the text to modify
     * @param suffix the suffix to look for
     * @return the text with suffix replaced
     */
    private static String stripSuffix(String text, String suffix) {
        if (text.endsWith(suffix)) {
            text = text.substring(0, text.length() - suffix.length());
        }
        return text;
    }
    
    /**
     * Replaces bundle versions in Manifest files.
     * 
     * @param file the file to modify
     * @param oldVersion the old (mvn) version
     * @param newVersion the new (mvn) version
     * @return the result state
     * @throws IOException if reading/writing fails
     */
    private static Result replaceManifestVersion(File file, String oldVersion, String newVersion) throws IOException {
        Result result = Result.SKIPPED;
        if (file.exists()) {
            String oldVer = stripSuffix(oldVersion, SUFFIX_SNAPSHOT);
            boolean oldVerSnapshot = oldVer.length() != oldVersion.length();
            String newVer = stripSuffix(newVersion, SUFFIX_SNAPSHOT);
            boolean newVerSnapshot = newVer.length() != newVersion.length();
            // Manifest operates on hash, sequence may change
            List<String> manifest = Files.readAllLines(file.toPath());
            for (int l = 0; l < manifest.size(); l++) {
                String line = manifest.get(l);
                if (line.startsWith(KEY_BUNDLE_VERSION)) {
                    String mfVersion = line.substring(KEY_BUNDLE_VERSION.length()).trim();
                    String mfVer = stripSuffix(mfVersion, SUFFIX_QUALIFIER);
                    boolean mfVerSnapshot = oldVer.length() != oldVersion.length();
                    if (oldVerSnapshot == mfVerSnapshot && mfVer.equals(oldVer)) {
                        line = KEY_BUNDLE_VERSION + " " + newVer;
                        if (newVerSnapshot) {
                            line += SUFFIX_QUALIFIER;
                        }
                        manifest.set(l, line);
                        result = Result.MODIFIED;
                    }
                }
            }
            Files.write(file.toPath(), manifest);
        }
        return result;
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
            System.out.println(" --includeGroupId=<regEx>");
            System.out.println(" --excludeGroupId=<regEx>");
            System.out.println(" --verbose=<boolean>");
            System.out.println(" --modifyManifest=<boolean>");
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
