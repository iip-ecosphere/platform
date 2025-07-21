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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.representer.Representer;

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

    @Override
    public <T> T loadAs(String in, Class<T> cls) throws IOException {
        return createYaml(cls).loadAs(in, cls);
    }

    @Override
    public <T> T loadAs(InputStream in, Class<T> cls) throws IOException {
        return createYaml(cls).loadAs(in, cls);
    }

    @Override
    public Iterator<Object> loadAll(InputStream in, Class<?> cls) throws IOException {
        Iterator<Object> it;
        try {
            it = createYaml(cls).loadAll(in).iterator();
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
        if (null == cls) {
            result = new Yaml(representer);
        } else {
            result = new Yaml(new Constructor(cls), representer);
        }
        return result;
    }
    
    @Override
    public <T> T loadTolerantAs(InputStream in, Class<T> cls) {
        Representer representer = new Representer(new DumperOptions());
        representer.getPropertyUtils().setSkipMissingProperties(true);
        Yaml yaml = new Yaml(new Constructor(cls, new LoaderOptions()), representer);
        return yaml.load(in);        
    }
    
    @Override
    public String dump(Object object) throws IOException {
        return new Yaml().dump(object);
    }    

    @Override
    public void dump(Object object, Class<?> cls, Writer out) throws IOException {
        Constructor constructor = new Constructor(cls);
        TypeDescription configDescription = new TypeDescription(cls);
        constructor.addTypeDescription(configDescription);
        Yaml yaml = new Yaml(constructor);
        yaml.dump(object, out);      
    }    

}
