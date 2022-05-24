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

package de.iip_ecosphere.platform.support.iip_aas;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Map;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.FileUtils;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.iip_aas.json.JsonUtils;

/**
 * Helper functions for active AAS.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AasUtils {

    /**
     * An empty URI for {@link #readUri(Object[], int, URI)}.
     */
    public static final URI EMPTY_URI;

    /**
     * Resolves a resource from the main classpath.
     */
    public static final ResourceResolver CLASSPATH_RESOLVER = 
        n -> PlatformAas.class.getResourceAsStream(prependSlash(n));

    /**
     * Resolves a resource from the resources directory in the main classpath.
     */
    public static final ResourceResolver CLASSPATH_RESOURCE_RESOLVER = 
        n -> PlatformAas.class.getResourceAsStream("/resources" + prependSlash(n));

    /**
     * Prepends a "/" if there is none at the beginning of {@code text}.
     * 
     * @param text the text to use as basis
     * @return test with "/" prepended
     */
    public static final String prependSlash(String text) {
        if (!text.startsWith("/")) {
            text = "/" + text;
        }
        return text;
    }
        
    static {
        URI tmp;
        try {
            tmp = new URI("");
        } catch (URISyntaxException e) {
            tmp = null;
        }
        EMPTY_URI = tmp;
    }
    
    /**
     * Reads the first argument from {@code} args as String with default value empty.
     * 
     * @param args the array to take the value from 
     * @return the value
     */
    public static String readString(Object[] args) {
        return readString(args, 0);
    }

    /**
     * Reads the {@code index} argument from {@code} args as String with default value empty.
     * 
     * @param args the array to take the value from 
     * @param index the 0-based index into {@code} args
     * @return the value
     */
    public static String readString(Object[] args, int index) {
        return readString(args, index, "");
    }

    /**
     * Reads the {@code index} argument from {@code} args as String.
     * 
     * @param args the array to take the value from 
     * @param index the 0-based index into {@code} args
     * @param dflt default value if the {@code index} is wrong, there is no value/null ...
     * @return the value
     */
    public static String readString(Object[] args, int index, String dflt) {
        Object param = index >= 0 && index < args.length ? args[index] : null;
        return null == param ? dflt : param.toString();
    }
    
    /**
     * Reads the {@code index} argument from {@code} args as int.
     * 
     * @param args the array to take the value from 
     * @param index the 0-based index into {@code} args
     * @param dflt default value if the {@code index} is wrong, there is no value/null, the value is no int...
     * @return the value
     */
    public static int readInt(Object[] args, int index, int dflt) {
        Object param = index >= 0 && index < args.length ? args[index] : null;
        int result = dflt;
        if (null != param) {
            try {
                result = Integer.parseInt(param.toString());
            } catch (NumberFormatException e) {
                // handled by result = deflt
            }
        }
        return result;
    }

    /**
     * Reads the {@code index} argument from {@code} args as URI.
     * 
     * @param args the array to take the value from 
     * @param index the 0-based index into {@code} args
     * @param dflt default value if the {@code index} is wrong, there is no value/null
     * @return the value
     * @throws URISyntaxException if the value cannot be turned into an URI...
     */
    public static URI readUriEx(Object[] args, int index, URI dflt) throws URISyntaxException {
        Object param = index >= 0 && index < args.length ? args[index] : null;
        URI result = dflt;
        if (null != param) {
            result = new URI(param.toString());
        } 
        return result;
    }

    /**
     * Reads the {@code index} argument from {@code} args as URI.
     * 
     * @param args the array to take the value from 
     * @param index the 0-based index into {@code} args
     * @param dflt default value if the {@code index} is wrong, there is no value/null, the value cannot be turned 
     *   into an URI...
     * @return the value
     */
    public static URI readUri(Object[] args, int index, URI dflt) {
        URI result;
        try {
            result = readUriEx(args, index, dflt);
        } catch (URISyntaxException e) {
            result = dflt;
        }
        return result;
    }
    
    /**
     * Modifies a given {@code id} so that it fits the needs of the implementation. Uses 
     * {@link AasFactory#fixId(String)}.
     * 
     * @param id the id
     * @return the fixed id
     */
    public static String fixId(String id) {
        return AasFactory.getInstance().fixId(id);
    }
    
    /**
     * Reads the {@code index} argument from {@code} args as map of strings.
     * 
     * @param args the array to take the value from 
     * @param index the 0-based index into {@code} args
     * @param dflt default value if the {@code index} is wrong, there is no value/null ...
     * @return the map
     */
    @SuppressWarnings("unchecked")
    public static Map<String, String> readMap(Object[] args, int index, Map<String, String> dflt) {
        Object param = index >= 0 && index < args.length ? args[index] : null;
        Map<String, String> result = dflt;
        if (null != param) {
            result = JsonUtils.fromJson(result, Map.class);
        }
        return result;
    }
    
    /**
     * Writes a map to JSON.
     * 
     * @param map the map
     * @return the JSON representation
     */
    public static String writeMap(Map<String, String> map) {
        return JsonUtils.toJson(map);
    }
    
    /**
     * Resolves resources.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface ResourceResolver {
        
        /**
         * Resolves a resource to an input stream.
         * 
         * @param resource the name of the resource
         * @return the related input stream, may be <b>null</b> for none
         */
        public InputStream resolve(String resource);
        
    }
    
    /**
     * Handles a resolved resource.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface ResourceHandler {

        /**
         * Handle the resource. All arguments could be empty if there is no resolution result but the 
         * handler shall be called always.
         * 
         * @param name the name of the resource
         * @param resolved the resolved file contents/URI
         * @param mimeType the mime type
         */
        public void handle(String name, String resolved, String mimeType);
        
    }

    /**
     * Resolves an image from a nameplate/application JAML file. May call {@link #upload(File, String)}
     * to upload the file to the AAS server.
     * 
     * @param image the image as URL or as local resource name (may be <b>null</b> or empty)
     * @param resolver the resolver to use, {@link #CLASSPATH_RESOURCE_RESOLVER} used as default
     * @param handleAlways whether the handler shall also be called for an empty/no resolution result
     * @param handler handles the resolved resource
     */
    public static void resolveImage(String image, ResourceResolver resolver, boolean handleAlways, 
        ResourceHandler handler) {
        boolean resolved = false;
        if (null == resolver) {
            resolver = CLASSPATH_RESOURCE_RESOLVER;
        }
        if (null != image && image.length() > 0) {
            String prevMsg = "";
            try {
                String fName = FileUtils.sanitizeFileName(image);
                File f = new File(FileUtils.getTempDirectory(), fName);
                FileUtils.deleteOnExit(f);
                InputStream is = resolver.resolve(image);
                if (null != is) {
                    org.apache.commons.io.FileUtils.copyInputStreamToFile(is, f); // closes is
                    String contents = FileUtils.fileToBase64(f);
                    String mimeType = Files.probeContentType(f.toPath());
                    handler.handle(fName, contents, mimeType);
                    resolved = true;
                }
            } catch (IOException e) {
                prevMsg = e.getMessage();
            }
            if (!resolved) {
                try {
                    URL url = new URL(image);
                    if ("http".equals(url.getProtocol()) || "https".equals(url.getProtocol())) {
                        String name = url.getFile();
                        int pos = name.lastIndexOf('/');
                        if (pos > 0) {
                            name = name.substring(pos + 1);
                        }
                        name = AasUtils.fixId(name);
                        handler.handle(name, url.toString(), "text/x-uri");
                        resolved = true;
                    }
                } catch (MalformedURLException e) {
                    // ok, we will go on below
                }
            }
            if (!resolved) {
                LoggerFactory.getLogger(PlatformAas.class).warn("Cannot resolve image '{}' {}", 
                    image, prevMsg);
            }
        }
        if (!resolved && handleAlways) {
            handler.handle("", "", "");
        }
    }

}
