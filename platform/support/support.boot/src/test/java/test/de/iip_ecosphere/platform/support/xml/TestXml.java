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

package test.de.iip_ecosphere.platform.support.xml;

import java.io.IOException;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import de.iip_ecosphere.platform.support.xml.Xml;

/**
 * Implements the JSON interface by Jackson.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TestXml extends de.iip_ecosphere.platform.support.xml.Xml {
    
    @Override
    public Xml createInstanceImpl(boolean considerAnnotations) {
        return new TestXml();
    }

    @Override
    public <T> T readValue(String src, Class<T> cls) throws IOException {
        XmlMapper om = new XmlMapper();
        return om.readValue(src, cls);
    }
    
    @Override
    public <T> T readValue(byte[] src, Class<T> cls) throws IOException {
        XmlMapper om = new XmlMapper();
        return om.readValue(src, cls);
    }
    
    @Override
    public byte[] writeValueAsBytes(Object value) throws IOException {
        XmlMapper om = new XmlMapper();
        om.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        return om.writeValueAsBytes(value);
    }
    
    @Override
    public Xml configureFor(Class<?> cls) {
        return this;
    }
    
    @Override
    public Xml handleIipDataClasses() {
        return this;
    }
    
    @Override
    public Xml defineOptionals(Class<?> cls, String... fieldNames) {
        return this;
    }
    
    @Override
    public Xml defineFields(String... fieldNames) {
        return this;
    }
    
    @Override
    public Xml exceptFields(String... fieldNames) {
        return this;
    }

    @Override
    public Xml failOnUnknownProperties(boolean fail) {
        return this;
    }

    @Override
    public String writeValueAsString(Object value) throws IOException {
        XmlMapper om = new XmlMapper();
        om.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        return om.writeValueAsString(value);
    }

}
