/**
 * ******************************************************************************
 * Copyright (c) {2026} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.oktoflow.platform.tools.pluginEnv;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Combines the plugin classpath files in {@code target/standalone} into a single classpath file in 
 * {@code target/standalone/cp} adding a basic oktoflow testing environment. We might also do that via 
 * Maven... somehow.
 * 
 * @author Holger Eichelberger, SSE
 */
public class Standalone {

    private static String m2 = System.getProperty("user.home") + "/.m2/repository/";
    private static File dir = new File(System.getProperty("okto.files", "target/standalone"));
    private static String oktoVer = System.getProperty("okto.version", "");
    private static boolean firstEntry = true;
    private static Map<String, String> mapping = new HashMap<>();
    private static String pwdAdjust = System.getProperty("okto.pwdAdjust", "");
    private static String basePwd = System.getProperty("okto.pwd", "../..");  // for local git workspace
    
    /**
     * Prints a classpath entry.
     * 
     * @param out the output stream to write to
     * @param entry the classpath entry path
     */
    private static void printEntry(PrintStream out, String entry) {
        entry = entry.replace("/", File.separator);
        if (!firstEntry) {
            out.print(File.pathSeparator);
        }
        out.print(entry);
        File tmp = new File(entry);
        if (!tmp.exists()) {
            System.out.println(" WARN: " + tmp + " does not exist");
        }
        firstEntry = false;
    }

    /**
     * Prints an oktoflow entry for the component {@code name}.
     * 
     * @param out the output stream
     * @param name the components artifactId
     */
    private static void printOktoEntry(PrintStream out, String name) {
        printOktoEntry(out, name, "");
    }

    /**
     * Prints an oktoflow entry for the test artifact of the component {@code name}.
     * 
     * @param out the output stream
     * @param name the components artifactId
     */
    private static void printOktoTestEntry(PrintStream out, String name) {
        printOktoEntry(out, name, "tests");
    }

    /**
     * Prints an oktoflow entry for the component {@code name} with the given {@code classifier}.
     * 
     * @param out the output stream
     * @param name the components artifactId
     * @param classifier the classifier, may be empty or <b>null</b> for none
     */
    private static void printOktoEntry(PrintStream out, String name, String classifier) {
        if (classifier == null) {
            classifier = "";
        }
        if (classifier.length() > 0 && !classifier.startsWith("-")) {
            classifier = "-" + classifier;
        }
        printEntry(out, m2 + "de/iip-ecosphere/platform/" + name + "/" + oktoVer + "/" + name + "-" + oktoVer 
            + classifier + ".jar");
    }
    
    /**
     * Tries to obtain the actual oktoflow version from the POM, i.e., the version of the parent pom.
     * 
     * @return the version, may be empty
     * @throws IOException if reading the file fails
     */
    private static String getOktoVer() throws IOException {
        String result = "";
        File pomFile = new File("pom.xml");
        try {
            Document doc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(pomFile);

            NodeList parents = doc.getElementsByTagName("parent");
            if (parents.getLength() > 0) {
                Element parent = (Element) parents.item(0);
                NodeList versions = parent.getElementsByTagName("version");
                if (versions.getLength() > 0) {
                    result = versions.item(0).getTextContent().trim();
                }
            }
        } catch (SAXException | ParserConfigurationException e) {
            throw new IOException(e);
        }        
        return result;
    }
    
    /**
     * Creates and writes the standalone classpath.
     * 
     * @param args ignored
     * @throws IOException if I/O fails
     */
    public static void main(String[] args) throws IOException {
        if (oktoVer.length() == 0) {
            oktoVer = getOktoVer();
        }
        System.out.println("Oktoflow version: " + oktoVer);
        File pwd = new File(basePwd);
        if (pwdAdjust.length() > 0) { // for different setup, e.g., experiment
            pwd = new File(pwd, pwdAdjust);
        }
        System.out.println("Classpath prefix: " + pwd);
        File outFile = new File(dir, "cp");
        PrintStream out = new PrintStream(new FileOutputStream(outFile));
        System.out.println("Adding oktoflow classpath entries...");
        printEntry(out, m2 + "junit/junit/4.12/junit-4.12.jar");
        printEntry(out, m2 + "org/hamcrest/hamcrest-core/2.2\\hamcrest-core-2.2.jar");
        printEntry(out, m2 + "org/hamcrest/hamcrest/2.2/hamcrest-2.2.jar");
        printOktoEntry(out, "tools.lib");
        printOktoEntry(out, "support.boot");
        printOktoEntry(out, "support");
        printOktoTestEntry(out, "support");
        printOktoEntry(out, "support.aas");
        printOktoTestEntry(out, "support.aas");

        File tmp = new File(pwd, "support/support.aas.basyx2/support.aas.basyx2.plugintests/target/test-classes");
        printEntry(out, tmp.getCanonicalFile().getAbsolutePath());

        File[] files = dir.listFiles();
        for (File f : files) {
            if (f.isFile() && !f.getName().equals("cp")) {
                System.out.println("Processing " + f.getAbsolutePath());            
                List<String> lines = Files.readAllLines(f.toPath());
                for (String l : lines) {
                    if (!l.startsWith("#")) {
                        StringTokenizer tokens = new StringTokenizer(l, ";:"); // even out windows/linux
                        while (tokens.hasMoreTokens()) {
                            String t = tokens.nextToken();
                            //System.out.println(t);
                            String tMapped = mapping.get(t);
                            // a bit jar shadowing
                            if (t.contains("netty") && t.contains("4.1.59")) {
                                tMapped = "";
                            }
                            if (t.contains("snakeyaml") && t.contains("1.27")) {
                                tMapped = "";
                            }
                            /*if (t.contains("spring-boot") && t.contains("3.4.3")) { // basyx2-m8
                                tMapped = "";
                            }*/
                            if (tMapped != null) {
                                if (tMapped.length() == 0) {
                                    t = null;
                                } else {
                                    t = tMapped;
                                }
                            }
                            if (t != null) {
                                t = t.replace("\\", "/"); // even out windows/linux, File.separator -> printEntry
                                printEntry(out, m2 + t);
                            }
                        }
                    }
                }
            }
        }
        out.flush();
        out.close();
        System.out.println("Written classpath file " + outFile);
    }

}
