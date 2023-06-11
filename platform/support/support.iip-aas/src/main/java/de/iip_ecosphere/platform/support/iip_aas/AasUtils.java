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
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.FileUtils;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.ElementsAccess;
import de.iip_ecosphere.platform.support.aas.Property;
import de.iip_ecosphere.platform.support.iip_aas.json.JsonUtils;
import de.iip_ecosphere.platform.support.resources.ResourceLoader;
import de.iip_ecosphere.platform.support.resources.ResourceResolver;

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
        (c, n) -> ResourceLoader.getResourceAsStream(ResourceLoader.prependSlash(n));

    /**
     * Resolves a resource from the resources directory in the main classpath.
     */
    public static final ResourceResolver CLASSPATH_RESOURCE_RESOLVER = 
        (c, n) -> ResourceLoader.getResourceAsStream("/resources" + ResourceLoader.prependSlash(n));
        
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
     * Reads the {@code index} argument from {@code} args as double.
     * 
     * @param args the array to take the value from 
     * @param index the 0-based index into {@code} args
     * @param dflt default value if the {@code index} is wrong, there is no value/null, the value is no int...
     * @return the value
     */
    public static double readDouble(Object[] args, int index, int dflt) {
        Object param = index >= 0 && index < args.length ? args[index] : null;
        double result = dflt;
        if (null != param) {
            try {
                result = Double.parseDouble(param.toString());
            } catch (NumberFormatException e) {
                // handled by result = deflt
            }
        }
        return result;
    }

    /**
     * Reads the {@code index} argument from {@code} args as boolean.
     * 
     * @param args the array to take the value from 
     * @param index the 0-based index into {@code} args
     * @param dflt default value if the {@code index} is wrong, there is no value/null
     * @return the value
     */
    public static boolean readBoolean(Object[] args, int index, boolean dflt) {
        Object param = index >= 0 && index < args.length ? args[index] : null;
        boolean result = dflt;
        if (null != param) {
            result = Boolean.parseBoolean(param.toString());
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
            result = JsonUtils.fromJson(param, Map.class);
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
     * Resolves an image from a nameplate/application JAML file.
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
                LoggerFactory.getLogger(AasUtils.class).warn("Cannot resolve image '{}' {}", 
                    image, prevMsg);
            }
        }
        if (!resolved && handleAlways) {
            handler.handle("", "", "");
        }
    }
    
    // TODO move the next three functions down to support.aas?

    /**
     * Returns the value of the specified property without throwing exceptions.
     * 
     * @param <T> the type of the value to return
     * @param coll the collection to take the value from
     * @param propIdShort the short id of the property
     * @param type the type of the value to return
     * @param transformer the value transformer
     * @param dflt the default value if the property cannot be found
     * @return the value or {@code dflt}
     */
    public static <T> T getPropertyValueSafe(ElementsAccess coll, String propIdShort, Class<T> type, 
        Function<Object, T> transformer, T dflt) {
        T result = dflt;
        Property prop = coll.getProperty(propIdShort);
        if (null != prop) {
            try {
                result = transformer.apply(prop.getValue());
            } catch (ExecutionException e) {
                LoggerFactory.getLogger(AasUtils.class).warn("Cannot access AAS property {} value: {}", 
                    propIdShort, e.getMessage());
            }
        } else {
            LoggerFactory.getLogger(AasUtils.class).warn("Cannot find AAS property {} in collection {}", 
                propIdShort, coll.getIdShort());
        }
        return result;
    }

    /**
     * Returns the value of the specified property as string without throwing exceptions.
     * 
     * @param parent the collection/submodel to take the value from
     * @param propIdShort the short id of the property
     * @param dflt the default value if the property cannot be found or it's value is <b>null</b>
     * @return the value or {@code dflt}
     */
    public static String getPropertyValueAsStringSafe(ElementsAccess parent, String propIdShort, 
        String dflt) {
        return getPropertyValueSafe(parent, propIdShort, String.class, o -> null == o ? dflt : o.toString(), dflt);
    }

    /**
     * Returns the value of the specified property as Integer without throwing exceptions.
     * 
     * @param parent the collection/submodel to take the value from
     * @param propIdShort the short id of the property
     * @param dflt the default value if the property cannot be found or it's value is <b>null</b>
     * @return the value or {@code dflt}
     */
    public static Integer getPropertyValueAsIntegerSafe(ElementsAccess parent, String propIdShort, 
        Integer dflt) {
        return getPropertyValueSafe(parent, propIdShort, Integer.class, o -> null == o ? dflt : (Integer) o, dflt);
    }

    /**
     * Sets the value of property {@code propIdShort} on {@code parent}.
     * 
     * @param parent the collection/submodel to take the property from, may be <b>null</b>, call is ignored then
     * @param propIdShort the idShort of the property; call is ignored if the property does not exist
     * @param value the value to set
     * @throws ExecutionException if the value cannot be set
     */
    public static void setPropertyValue(ElementsAccess parent, String propIdShort, Object value) 
        throws ExecutionException {
        if (null != parent) {
            Property prop = parent.getProperty(propIdShort);
            if (null != prop) {
                prop.setValue(value);
            }
        }
    }

    /**
     * Sets the value of property {@code propIdShort} on {@code parent} logging potential errors.
     * 
     * @param parent the collection/submodel to take the property from, may be <b>null</b>, call is ignored then
     * @param propIdShort the idShort of the property; call is ignored if the property does not exist
     * @param value the value to set
     */
    public static void setPropertyValueSafe(ElementsAccess parent, String propIdShort, Object value) {
        try {
            setPropertyValue(parent, propIdShort, value);
        } catch (ExecutionException e) {
            LoggerFactory.getLogger(AasUtils.class).warn("Cannot set value for AAS property {}: {}", 
                propIdShort, e.getMessage());
        }
    }

}
