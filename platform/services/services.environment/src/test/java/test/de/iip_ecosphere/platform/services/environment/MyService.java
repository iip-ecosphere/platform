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

package test.de.iip_ecosphere.platform.services.environment;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import de.iip_ecosphere.platform.services.environment.AbstractService;
import de.iip_ecosphere.platform.services.environment.ServiceKind;
import de.iip_ecosphere.platform.support.iip_aas.Version;

/**
 * Test service, values and behavior aligned with Python.
 * 
 * @author Holger Eichelberger, SSE
 */
public class MyService extends AbstractService {

    /**
     * Creates an instance.
     */
    public MyService() {
        super("1234", "MyService", new Version("1.2.3"), "Default Service", true, ServiceKind.TRANSFORMATION_SERVICE);
    }
    
    @Override
    public void migrate(String resourceId) throws ExecutionException {
        throw new ExecutionException("not implemented", null); // for now
    }

    @Override
    public void update(URI location) throws ExecutionException {
        throw new ExecutionException("not implemented", null); // for now
    }

    @Override
    public void switchTo(String targetId) throws ExecutionException {
        throw new ExecutionException("not implemented", null); // for now
    }

    @Override
    public void reconfigure(Map<String, String> values) throws ExecutionException {
        throw new ExecutionException("not implemented", null); // for now
    }

}
