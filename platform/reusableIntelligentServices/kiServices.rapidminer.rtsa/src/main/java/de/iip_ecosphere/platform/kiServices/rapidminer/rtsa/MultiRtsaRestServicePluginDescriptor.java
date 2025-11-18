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

package de.iip_ecosphere.platform.kiServices.rapidminer.rtsa;

import java.util.List;

import de.iip_ecosphere.platform.services.environment.AbstractGenericServicePluginDescriptor;
import de.iip_ecosphere.platform.services.environment.YamlService;

/**
 * The plugin descriptor for {@link MultiRtsaRestService}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class MultiRtsaRestServicePluginDescriptor
    extends AbstractGenericServicePluginDescriptor<MultiRtsaRestService> {

    /**
     * Creates the instance for JSL.
     */
    public MultiRtsaRestServicePluginDescriptor() {
        super(PLUGIN_ID_PREFIX + "rtsa", List.of(MultiRtsaRestService.class.getName()));
    }

    @Override
    public MultiRtsaRestService createService(YamlService yaml, Object... args) {
        return new MultiRtsaRestService(yaml);
    }

}
