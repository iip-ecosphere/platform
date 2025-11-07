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

package de.iip_ecosphere.platform.services.spring;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.springframework.core.env.PropertySource;

import de.iip_ecosphere.platform.support.setup.AbstractSetup;

/**
 * Specialized parameter source as fallback.
 * 
 * @author Holger Eichelberger, SSE
 */
public class YamlPropertySource extends PropertySource<String> {

    private Map<String, Object> properties;
    private Pattern kebabPattern = Pattern.compile("-(.)");
    private Map<String, Object> overwrite = new HashMap<>();
    private Set<String> ignore = new HashSet<>();

    /**
     * Constructs a property source.
     */
    public YamlPropertySource() {
        this(null, null);
    }

    /**
     * Constructs a property source.
     */
    public YamlPropertySource(Map<String, Object> overwrite) {
        this(overwrite, null);
    }

    /**
     * Constructs a property source.
     * 
     * @param overwrite properties to overwrite as full-name/value pairs, may be <b>null</b> for none
     * @param ignore properties in full-name to ignore and to return no value at all, e.g., as defined via command line;
     *    may be <b>null</b> or empty for none
     */
    public YamlPropertySource(Map<String, Object> overwrite, Iterable<String> ignore) {
        super("oktoflow YAML");
        try {
            properties = AbstractSetup.readMappingFromYaml();
        } catch (IOException e) {
            properties = new HashMap<>();
        }
        if (null != overwrite) {
            this.overwrite.putAll(overwrite);
        }
        if (null != ignore) {
            ignore.forEach(ig -> this.ignore.add(ig));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object getProperty(String name) {
        Object result = null;
        if (!ignore.contains(name)) {
            Map<String, Object> prop = properties;
            StringTokenizer parts = new StringTokenizer(name, ".");
            while (parts.hasMoreTokens()) {
                String key = parts.nextToken();
                Object tmp = prop.get(key);
                if (tmp == null) {
                    key = kebabPattern.matcher(key).replaceAll(mr -> mr.group(1).toUpperCase());
                    tmp = prop.get(key);
                }
                if (parts.hasMoreTokens()) {
                    if (tmp instanceof Map) {
                        prop = (Map<String, Object>) tmp;
                    } else {
                        break;
                    }
                } else {
                    result = tmp;
                }
            }
            if (null != overwrite && overwrite.containsKey(name)) {
                result = overwrite.get(name);
            }
        }
        return result;
    }

}
