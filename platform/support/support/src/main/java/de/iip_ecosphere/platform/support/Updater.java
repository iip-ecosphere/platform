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

package de.iip_ecosphere.platform.support;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.iip_ecosphere.platform.support.json.Json;
import de.iip_ecosphere.platform.support.json.JsonIterator;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;

/**
 * Utility class to update application plugins (upon application start).
 * 
 * @author Holger Eichelberger, SSE
 */
public class Updater {
    
    private static final String SNAPSHOT = "SNAPSHOT";

    /**
     * Update plugins quietly.
     * 
     * @param resolved the resolved plugin file as written by the oktoflow resource plugin for packaging 
     *   oktoflow plugins
     * @param pluginsFolder the target plugins folder
     * @param updatePlugins whether stored (SNAPSHOT) plugins shall be updated at all
     */
    public static void updatePluginsQuiet(InputStream resolved, File pluginsFolder, boolean updatePlugins) {
        try {
            updatePlugins(resolved, pluginsFolder, updatePlugins);
        } catch (IOException e) {
            LoggerFactory.getLogger(Updater.class).warn("During plugins/dependency update: {}", e.getMessage());
        }
    }

    /**
     * Update plugins.
     * 
     * @param resolved the resolved plugin file as written by the oktoflow resource plugin for packaging 
     *   oktoflow plugins
     * @param pluginsFolder the target plugins folder
     * @param updatePlugins whether stored (SNAPSHOT) plugins shall be updated at all
     * @throws IOException if the resolved plugin file cannot be read
     */
    public static void updatePlugins(InputStream resolved, File pluginsFolder, boolean updatePlugins) 
        throws IOException {
        if (null == resolved) {
            throw new IOException("No \"resolved\" input stream given.");
        }
        String json = IOUtils.toString(resolved);
        JsonIterator it = Json.parse(json);
        for (int i = 0; i < it.size(); i++) {
            JsonIterator eIt = it.get(i);
            String url = eIt.get("url").toStringValue();
            if (url.startsWith("./") || url.startsWith(".\\")) {
                // for testing, allow relative "URLs" and resolve them here
                url = url.substring(2);
                url = new File(url).toURI().toURL().toString();
            }
            String name = eIt.get("name").toStringValue();
            boolean plugin = eIt.get("plugin").toBooleanValue();
            
            File pluginFile = new File(pluginsFolder, name);
            String type = plugin ? "plugin" : "dependency";
            if (!pluginFile.exists() || (name.endsWith(SNAPSHOT) && updatePlugins)) {
                LoggerFactory.getLogger(Updater.class).info("Resolving {} {}", type, name);
                File metadataFile = new File(pluginsFolder, ".metadata");
                Properties metadata = new Properties();
                try {
                    metadata.load(new FileInputStream(metadataFile));
                } catch (IOException e) {
                    // ignore, it's not there
                }
                PluginResolutionResult res = resolvePlugin(name, url, metadata.get(name));
                if (null != res && null != res.stream) {
                    LoggerFactory.getLogger(Updater.class).info("Updating {} {}", type, name);
                    String fName = name + (plugin ? ".zip" : ".jar");
                    File targetFile = new File(pluginsFolder, fName);
                    FileUtils.copyInputStreamToFile(res.stream, targetFile);
                    FileUtils.closeQuietly(res.stream);
                    if (plugin) {
                        unzipPlugin(targetFile, name);
                    }
                    if (res.hasBuildNr()) {
                        metadata.put(name, res.buildNr);
                        try {
                            metadata.store(new FileOutputStream(metadataFile), name);
                        } catch (IOException e) {
                            LoggerFactory.getLogger(Updater.class).warn(
                                "Cannot write {} metadata file {}: {}", type, metadataFile, e.getMessage());
                        }    
                    }
                }
            }
        }
    }
    
    /**
     * Unzips a plugin.
     * 
     * @param file the file containing the plugin (stored in plugins folder)
     * @param name the name of the plugin
     * @throws IOException if unzipping fails
     */
    private static void unzipPlugin(File file, String name) throws IOException {
        File pluginDir = file.getParentFile();
        ZipUtils.extractZip(new FileInputStream(file), pluginDir.toPath(), 
            e -> !e.getName().equals("resolved"));
        File cp = new File(pluginDir, "classpath");
        File cpTarget = new File(pluginDir, name);
        FileUtils.deleteQuietly(cpTarget);
        cp.renameTo(cpTarget);
        FileUtils.deleteQuietly(file);
        // relocate target -> plugins ?
    }

    /**
     * Represents a plugin resolution result for a found plugin, potentially with Maven snapshot build number.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class PluginResolutionResult {
        
        private InputStream stream;
        private String buildNr;
        
        /**
         * Creates an instance for a resolved plugin given as input stream without build number.
         * 
         * @param stream the plugin contents stream
         */
        private PluginResolutionResult(InputStream stream) {
            this(stream, null);
        }

        /**
         * Creates an instance for a resolved plugin given as input stream with associated Maven build number.
         * 
         * @param stream the plugin contents stream
         * @param buildNr the build number, may be <b>null</b>/empty for none
         */
        private PluginResolutionResult(InputStream stream, String buildNr) {
            this.stream = stream;
            this.buildNr = buildNr;
        }
        
        /**
         * Returns whether this result has a build number.
         * 
         * @return {@code true} for build number, {@code false} else
         */
        private boolean hasBuildNr() {
            return buildNr != null && buildNr.length() > 0;
        }

    }

    /**
     * Resolves the given {@code pluginName} from the (initial complete, potentially outdated) Maven {@code pluginUrl}.
     * If the URL does not exist anymore, this method tries to resolve it based on Maven metadata in the containing 
     * folder.
     * 
     * @param pluginName the name of the plugin
     * @param pluginUrl the (initial complete, potentially outdated) Maven plugin download URL
     * @param lastBuildNr the build number of the last update, may be <b>null</b> or empty for none
     * @return the input stream of the resolved plugin or <b>null</b> for none
     */
    private static PluginResolutionResult resolvePlugin(String pluginName, String pluginUrl, Object lastBuildNr) {
        PluginResolutionResult result = null;
        try {
            URL url = new URL(pluginUrl);
            try {
                result = new PluginResolutionResult(url.openStream());
            } catch (IOException e) {
                result = resolvePluginWithMaven(pluginName, pluginUrl, lastBuildNr);
            }
        } catch (MalformedURLException e) {
        }
        return result;
    }

    /**
     * Resolves a not-found plugin with Maven metadata.
     * 
     * @param pluginName the name of the plugin
     * @param pluginUrl the (initial complete, potentially outdated) Maven plugin download URL
     * @param lastBuildNr the build number of the last update, may be <b>null</b> or empty for none
     * @return the input stream of the resolved plugin or <b>null</b> for none
     * @throws MalformedURLException in case that creating an URL fails
     */
    private static PluginResolutionResult resolvePluginWithMaven(String pluginName, String pluginUrl, 
        Object lastBuildNr) throws MalformedURLException {
        PluginResolutionResult result = null;
        if (pluginName.endsWith(SNAPSHOT)) {
            int pos = pluginUrl.lastIndexOf("/");
            if (pos > 0) {
                String base = pluginUrl.substring(0, pos + 1);
                String name = pluginUrl.substring(pos + 1);
                URL url = new URL(base + "maven-metadata.xml");
                try {
                    DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                    Document doc = builder.parse(url.openStream());
                    Node versioning = getElement(doc.getDocumentElement().getChildNodes(), "versioning");
                    Node snapshot = getElement(versioning, "snapshot");
                    Node timestamp = getElement(snapshot, "timestamp");
                    Node buildNr = getElement(snapshot, "buildNumber");
                    if (timestamp != null && buildNr != null) {
                        int endPos = name.lastIndexOf("."); // extension
                        if (endPos > 0) { // classifier
                            endPos = name.lastIndexOf("-", endPos - 1);
                        }
                        int startPos = endPos;
                        if (startPos > 0) { // buildnr
                            startPos = name.lastIndexOf("-", startPos - 1);
                        }
                        if (startPos > 0) { // timestamp
                            startPos = name.lastIndexOf("-", startPos - 1);
                        }
                        String buildNrText = buildNr.getTextContent().trim();
                        if (startPos != endPos && isMoreRecent(lastBuildNr, buildNrText)) {
                            name = name.substring(0, startPos + 1) + timestamp.getTextContent().trim() + "-" 
                                + buildNrText + name.substring(endPos);
                            try {
                                url = new URL(base + name);
                                result = new PluginResolutionResult(url.openStream(), buildNrText);
                            } catch (IOException e3) {
                            }
                        }
                    }
                } catch (IOException | ParserConfigurationException | SAXException ex) {
                }
            }
        }
        return result;
    }
 
    /**
     * Returns whether the actual build number is more recent than the last known one.
     * 
     * @param lastBuildNr the last build number, may be <b>null</b> for none
     * @param buildNr the actual build number as text
     * @return {@code true} if more recent, no {@code lastBuildNr} or build numbers not readable, {@code false} if 
     *     not more recent
     */
    private static boolean isMoreRecent(Object lastBuildNr, String buildNr) {
        boolean isMoreRecent = true;
        if (lastBuildNr != null) {
            try {
                int last = Integer.parseInt(lastBuildNr.toString());
                int actual = Integer.parseInt(buildNr);
                isMoreRecent = actual > last;
            } catch (NumberFormatException e) {
                // ignore, update anyway
            }
        } // ignore, update anyway
        return isMoreRecent;
    }

    /**
     * Returns the XML child element with the given {@code name} from the parent {@code node}.
     * 
     * @param node the parent node
     * @param name the child node name
     * @return the child node or <b>null</b> for none
     */
    private static Node getElement(Node node, String name) {
        if (null != node) {
            return getElement(node.getChildNodes(), name);
        }
        return null;
    }

    /**
     * Returns the XML element with the given {@code name} from the node {@code list}.
     * 
     * @param list the nodes to search
     * @param name the child node name
     * @return the child node or <b>null</b> for none
     */
    private static Node getElement(NodeList list, String name) {
        if (null != list) {
            for (int n = 0; n < list.getLength(); n++) {
                Node no = list.item(n);
                if (no.getNodeName().equals(name)) {
                    return no;
                }
            }
        }
        return null;
    }

}
