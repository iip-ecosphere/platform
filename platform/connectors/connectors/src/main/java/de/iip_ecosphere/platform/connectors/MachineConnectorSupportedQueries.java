/**
 *******************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.connectors;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import de.iip_ecosphere.platform.connectors.events.ConnectorTriggerQuery;
import de.iip_ecosphere.platform.connectors.model.ModelAccess;

/**
 * Indicates supported {@link ConnectorTriggerQuery}.
 * 
 * @author Holger Eichelberger, SSE
 * @see ModelAccess
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MachineConnectorSupportedQueries {

    /**
     * Whether the machine connector has a model at all.
     * 
     * @return {@code true} for model, {@code} false for no model
     */
    public Class<? extends ConnectorTriggerQuery>[] value();
    
}
