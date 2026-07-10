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

package de.oktoflow.platform.support.yaml.snakeyaml;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.representer.Representer;

import de.iip_ecosphere.platform.support.CollectionUtils;
import de.iip_ecosphere.platform.support.IOUtils;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;

/**
 * Implements the YAML interface by SnakeYaml.
 * 
 * @author Holger Eichelberger, SSE
 */
public class SnakeYaml extends de.iip_ecosphere.platform.support.yaml.Yaml {

    @Override
    public Object load(InputStream in) throws IOException {
        Yaml yaml = new Yaml();
        return yaml.load(in);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> loadMapping(InputStream in) throws IOException {
        return (Map<String, Object>) load(in);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> loadMapping(InputStream in, Map<String, Predicate<Object>> conds) throws IOException {
        Map<String, Object> result = new HashMap<>();
        Iterator<Object> docs = loadAll(in, null, null, conds);
        while (docs.hasNext()) {
            Object o = docs.next();
            if (o instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) o;
                CollectionUtils.merge(result, map);
            }
        }
        return result;
    }

    @Override
    public <T> T loadAs(String in, Class<T> cls) throws IOException {
        return createYaml(cls).loadAs(in, cls);
    }

    @Override
    public <T> T loadAs(InputStream in, Class<T> cls) throws IOException {
        return createYaml(cls).loadAs(in, cls);
    }

    @Override
    public Iterator<Object> loadAll(InputStream in, String path, Class<?> cls) throws IOException {
        return loadAll(in, path, cls, null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterator<Object> loadAll(InputStream in, String path, Class<?> cls, Map<String, Predicate<Object>> conds) 
        throws IOException {
        Iterator<Object> it = null;
        try {
            Yaml yaml = createYaml(Map.class);
            List<Object> documents = new ArrayList<>();
            String content = IOUtils.toString(in);
            String[] rawDocuments = content.split("(?m)^---$");
            for (String docText : rawDocuments) {
                if (docText.trim().isEmpty()) {
                    continue;
                }
                Map<String, Object> map = yaml.load(docText);
                Map<String, Object> data = map;
                if (path != null && path.length() > 0) {
                    data = (Map<String, Object>) map.get(path);
                }
                if (matchesConditions(data, conds)) {
                    if (null == cls) {
                        documents.add(yaml.load(yaml.dump(data)));
                    } else {
                        documents.add(yaml.loadAs(yaml.dump(data), cls));
                    }
                } else {
                    if (conds == null) {
                        LoggerFactory.getLogger(this).warn("Cannot find YAML path {}, falling back to "
                            + "full document", path);
                    }
                }
            }
            if (documents.size() > 0) {
                it = documents.iterator();
            }
            if (it == null) { // fallback if property not found
                it = createYaml(cls).loadAll(in).iterator();
            }
            if (null != cls) {
                List<Object> results = new ArrayList<>();
                if (it.hasNext()) {
                    Object o = it.next();
                    if (cls.isInstance(o)) {
                        results.add(cls.cast(o));
                    }
                }
                it = results.iterator();
            }
        } catch (YAMLException e) {
            throw new IOException(e);
        }
        return it;
    }
    
    /**
     * Returns whether at least one condition in {@code conds} holds for the specified mapping.
     * 
     * @param data the data containing the mapping
     * @param conds the conditions with key = "."-separated key in {@code data} and value = the predicate checking the 
     *     value for the associated key, {@code conds} may be <b>null</b>
     * @return {@code true} if there are no conditions and {@code data} is not <b>null</b> or if at least one condition 
     *     meets, {@code false} else
     */
    private boolean matchesConditions(Map<String, Object> data, Map<String, Predicate<Object>> conds) {
        boolean matches = data != null;
        if (matches && conds != null) {
            matches = false;
            for (Map.Entry<String, Predicate<Object>> cond: conds.entrySet()) {
                Object value = getValue(data, cond.getKey(), null, v -> v);
                matches = cond.getValue().test(value);
                if (matches) {
                    break;
                }
            }
        }
        return matches;
    }
    
    @Override
    public Iterator<Object> loadAll(InputStream in) throws IOException {
        return loadAll(in, null);
    }
    
    /**
     * Creates a tolerant YAML object to read objects of type {@code cls}.
     * 
     * @param cls the type to read, may be <b>null</b>
     * @return the yamp object
     */
    private static Yaml createYaml(Class<?> cls) {
        Yaml result;
        Representer representer = new Representer(new DumperOptions());
        representer.getPropertyUtils().setSkipMissingProperties(true);
        DumperOptions dumperOptions = initDumperOptions(representer);
        if (null == cls) {
            result = new Yaml(representer, dumperOptions);
        } else {
            result = new Yaml(new FocusedConstructor(cls), representer, dumperOptions);
        }
        return result;
    }
    
    /**
     * Extends snakeyaml's constructor by loading via the class loader of the specified class, not
     * just via the context classloader.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class FocusedConstructor extends Constructor {
        
        private ClassLoader loader;
        
        /**
         * Creates a constructor instance for the given class.
         * 
         * @param cls the class to construct for, primarily querying the class loader of {@code cls}
         */
        public FocusedConstructor(Class<? extends Object> cls) {
            super(cls, new LoaderOptions());
            loader = cls.getClassLoader();
        }
        
        @Override
        protected Class<?> getClassForName(String name) throws ClassNotFoundException {
            Class<?> cls = null;
            if (null != loader) {
                try {
                    cls = loader.loadClass(name);
                } catch (ClassNotFoundException e) {
                    // may also consider PluginSetup.getClassLoader()
                }
            } 
            if (null == cls) {
                cls = super.getClassForName(name);
            }
            return cls;
        }        

    }
    
    @Override
    public <T> T loadTolerantAs(InputStream in, Class<T> cls) {
        Representer representer = new Representer(new DumperOptions());
        representer.getPropertyUtils().setSkipMissingProperties(true);
        Yaml yaml = new Yaml(new Constructor(cls, new LoaderOptions()), representer, initDumperOptions(representer));
        return yaml.load(in);        
    }
    
    /**
     * Initialize dumper from {@code representer}, takeover in migration from 1.27 to 2.3.
     * 
     * @param representer the representer to initialize from
     * @return the initialized dumper
     */
    private static DumperOptions initDumperOptions(Representer representer) {
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setDefaultFlowStyle(representer.getDefaultFlowStyle());
        dumperOptions.setDefaultScalarStyle(representer.getDefaultScalarStyle());
        dumperOptions.setAllowReadOnlyProperties(representer.getPropertyUtils().isAllowReadOnlyProperties());
        dumperOptions.setTimeZone(representer.getTimeZone());
        return dumperOptions;
    }    
    
    @Override
    public String dump(Object object) throws IOException {
        return new Yaml().dump(object);
    }    

    @Override
    public void dump(Object object, Class<?> cls, Writer out) throws IOException {
        Constructor constructor = new Constructor(cls, new LoaderOptions());
        TypeDescription configDescription = new TypeDescription(cls);
        constructor.addTypeDescription(configDescription);
        Yaml yaml = new Yaml(constructor);
        yaml.dump(object, out);      
    }    

}
