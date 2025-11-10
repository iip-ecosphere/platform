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

package test.de.iip_ecosphere.platform.support.yaml;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;

/**
 * Implements the YAML interface by SnakeYaml.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TestYaml extends de.iip_ecosphere.platform.support.yaml.Yaml {

    @Override
    public Object load(InputStream in) throws IOException {
        return null;
    }
    
    @Override
    public Map<String, Object> loadMapping(InputStream in) throws IOException {
        return null;
    }

    @Override
    public <T> T loadAs(String in, Class<T> cls) throws IOException {
        return null;
    }

    @Override
    public <T> T loadAs(InputStream in, Class<T> cls) throws IOException {
        return null;
    }

    @Override
    public Iterator<Object> loadAll(InputStream in, String path, Class<?> cls) throws IOException {
        return null;
    }

    @Override
    public Iterator<Object> loadAll(InputStream in) throws IOException {
        return null;
    }
    
    @Override
    public <T> T loadTolerantAs(InputStream in, Class<T> cls) {
        return null;
    }
    
    @Override
    public String dump(Object object) throws IOException {
        return null;
    }
    
    @Override
    public void dump(Object object, Class<?> cls, Writer out) throws IOException {
    }

}
