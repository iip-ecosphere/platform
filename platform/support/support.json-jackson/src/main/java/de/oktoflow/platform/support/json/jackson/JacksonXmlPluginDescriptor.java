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

package de.oktoflow.platform.support.json.jackson;

import de.iip_ecosphere.platform.support.plugins.SingletonPluginDescriptor;
import de.iip_ecosphere.platform.support.xml.Xml;
import de.iip_ecosphere.platform.support.xml.XmlProviderDescriptor;

/**
 * The Jackson XML plugin descriptor.
 * 
 * @author Holger Eichelberger, SSE
 */
public class JacksonXmlPluginDescriptor extends SingletonPluginDescriptor<Xml> implements XmlProviderDescriptor {
    
    /**
     * Creates the descriptor.
     */
    public JacksonXmlPluginDescriptor() {
        super("xml-jackson", null, Xml.class, p -> new JacksonXml());
    }

    @Override
    public Xml create() {
        return new JacksonXml();
    }
    
}
