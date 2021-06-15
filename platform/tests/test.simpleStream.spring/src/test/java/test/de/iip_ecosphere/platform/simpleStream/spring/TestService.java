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

package test.de.iip_ecosphere.platform.simpleStream.spring;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import de.iip_ecosphere.platform.services.environment.AbstractService;
import de.iip_ecosphere.platform.services.environment.YamlService;

/**
 * Represents a service in terms of it (implemented) administrative functions.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TestService extends AbstractService {

    /**
     * Creates a service instance from YAML information.
     * 
     * @param yaml the YAML information
     */
    public TestService(YamlService yaml) {
        super(yaml);
    }

    @Override
    public void migrate(String resourceId) throws ExecutionException {
        throw new ExecutionException("not implemented", null);
    }

    @Override
    public void update(URI location) throws ExecutionException {
        throw new ExecutionException("not implemented", null);
    }

    @Override
    public void switchTo(String targetId) throws ExecutionException {
        throw new ExecutionException("not implemented", null);
    }

    @Override
    public void reconfigure(Map<String, String> values) throws ExecutionException {
        // ignore
    }

}
