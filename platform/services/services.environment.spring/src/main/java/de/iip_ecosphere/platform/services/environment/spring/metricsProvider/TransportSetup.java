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

package de.iip_ecosphere.platform.services.environment.spring.metricsProvider;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Binds the {@link de.iip_ecosphere.platform.transport.connectors.TransportSetup} against Spring.
 * 
 * @author Holger Eichelberger, SSE
 */
@Component
@ConfigurationProperties(prefix = "transport")
public class TransportSetup extends de.iip_ecosphere.platform.transport.connectors.TransportSetup {

    private static final long serialVersionUID = 6379744405680096287L;

}
