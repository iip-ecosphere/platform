/**
 * ******************************************************************************
 * Copyright (c) {2024} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.oktoflow.platform.examples.mdzh;

import java.util.regex.PatternSyntaxException;

import de.iip_ecosphere.platform.connectors.events.ConnectorInputHandler;
import de.iip_ecosphere.platform.connectors.events.EventHandlingConnector;
import de.iip_ecosphere.platform.connectors.events.PatternTriggerQuery;
import iip.datatypes.EanScannerOutput;

/**
 * Triggers the query for a certain AAS.
 * 
 * @author Holger Eichelberger, SSE
 */
public class MdzhConfigInputHandler implements ConnectorInputHandler<EanScannerOutput> {

    @Override
    public void received(EanScannerOutput data, EventHandlingConnector connector) {
        System.out.println("AAS Query " + data.getData());
        try {
            connector.trigger(new PatternTriggerQuery(data.getData()));
        } catch (PatternSyntaxException e) {
            System.out.println("Illegal pattern: " + e.getMessage());
        }
    }

}
