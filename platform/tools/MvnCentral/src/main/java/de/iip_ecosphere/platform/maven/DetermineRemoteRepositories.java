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

package de.iip_ecosphere.platform.maven;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;

/**
 * Determines the used remote Maven repositories. If multiple were used for download, prefer "central".
 * 
 * @author Holger Eichelberger, SSE
 */
public class DetermineRemoteRepositories {

    /**
     * The main program.
     * 
     * @param args the command line arguments, may be optional Maven arguments (such as {@code -o}, the last one 
     *     optional the POM file to analyze (else {@code pom.xml})
     */
    public static void main(String[] args) {
        try {
            System.out.println("Executing Maven...");
            List<String> artifacts = getEffectiveDependencies(args);
            
            System.out.println("\nResults:");
            System.out.printf("%-100s | %-20s%n", "Artifact (G:A:V)", "Origin Repository");

            String localRepo = System.getProperty("user.home") + File.separator + ".m2" + File.separator + "repository";
            Set<String> repos = new HashSet<>();
            for (String artifact : artifacts) {
                String repoId = resolveOriginFromLocalFiles(artifact, localRepo);
                System.out.printf("%-100s | %-20s%n", artifact, repoId);
                repos.add(repoId);
            }
            repos.remove("central"); // ok for oktoflow
            repos.remove("SSE-mvn"); // ok for oktoflow
            System.out.println("\nPotentially problematic repos: " + String.join(", ", repos));
        } catch (IOException | InterruptedException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Executes 'mvn dependency:list' using ProcessBuilder. Parses the output to find unique G:A:V:S coordinates.
     * 
     * @param args the command line arguments, may be optional Maven arguments (such as {@code -o}, the last one 
     *     optional the POM file to analyze (else {@code pom.xml})
     */
    private static List<String> getEffectiveDependencies(String[] args) throws IOException, InterruptedException {
        String pomPath = "pom.xml"; 
        if (args.length > 0) {
            pomPath = args[args.length - 1];
        }
        
        String os = System.getProperty("os.name").toLowerCase();
        String cmd = os.contains("win") ? "mvn.cmd" : "mvn";

        List<String> pArgs = new ArrayList<>();
        pArgs.add(cmd);
        pArgs.add("dependency:list");
        for (int a = 0; a < args.length - 1; a++) { // not pomPath, see above
            pArgs.add(args[a]);
        }
        pArgs.add("-f");
        pArgs.add(pomPath);
        pArgs.add("-DexcludeTransitive=false");
        
        Process process = new ProcessBuilder(pArgs)
            .redirectErrorStream(true)
            .start();

        List<String> artifacts = new ArrayList<>();
        // Pattern matches: groupId:artifactId:type:version:scope
        Pattern pattern = Pattern.compile("^\\[INFO\\]\\s+([^:\\s]+:[^:\\s]+:[^:\\s]+:[^:\\s]+:[^:\\s]+)");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line); // see progress
                Matcher m = pattern.matcher(line);
                if (m.find()) {
                    artifacts.add(m.group(1).trim());
                }
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Maven execution failed with exit code " + exitCode);
        }

        return artifacts.stream().distinct().sorted().collect(Collectors.toList());
    }

    /**
     * Navigates the .m2 folder structure and parses repository metadata files.
     * 
     * @param coords the maven coordinates
     * @param localRepoBase the base directory of the local repository
     */
    private static String resolveOriginFromLocalFiles(String coords, String localRepoBase) {
        String[] parts = coords.split(":");
        if (parts.length < 4) {
            return "Unknown Format";
        }

        String groupPath = parts[0].replace(".", File.separator);
        String artifactId = parts[1];
        String version = parts[3];

        Path versionDir = Paths.get(localRepoBase, groupPath, artifactId, version);
        
        // Maven uses either _remote.repositories or _maven.repositories
        File metaFile = new File(versionDir.toFile(), "_remote.repositories");
        if (!metaFile.exists()) {
            metaFile = new File(versionDir.toFile(), "_maven.repositories");
        }

        if (!metaFile.exists()) {
            return "Local/Cached (No Metadata)";
        }

        try {
            List<String> lines = Files.readAllLines(metaFile.toPath());
            Map<String, String> artifacts = new HashMap<>();
            for (String line : lines) {
                // Example line: my-library-1.0.jar>central=
                int brackPos = line.indexOf(">"); 
                int eqPos = line.indexOf("="); 
                if (brackPos > 0 && eqPos > brackPos) {
                    String artId = line.substring(0, brackPos);
                    int typePos = artId.lastIndexOf(".");
                    String type = "";
                    if (typePos > 0) {
                        type = artId.substring(typePos + 1);
                        artId = artId.substring(0, typePos);
                    }
                    int classifierPos = artId.length() - 1;
                    while (classifierPos > 0 && artId.charAt(classifierPos) != '.' 
                        && artId.charAt(classifierPos) != '-' 
                        && !Character.isDigit(artId.charAt(classifierPos))) {
                        classifierPos--;
                    }
                    String classifier = "";
                    if (classifierPos != artId.length() - 1) {
                        classifier = artId.substring(classifierPos + 1) + ".";
                    }
                    String repo = line.substring(line.indexOf(">") + 1, line.indexOf("="));
                    String knownRepo = artifacts.get(classifier + type);
                    if (null == knownRepo || "central".equals(repo)) {
                        artifacts.put(classifier + type, repo);
                    }
                }
            }
            String result = artifacts.get("jar");
            return null == result ? "Not Found in Metadata" : result;
        } catch (IOException e) {
            return "Read Error";
        }
    }
    
}

