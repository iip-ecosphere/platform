/**
 * ******************************************************************************
 * Copyright (c) {2022} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.examples.hm22;

import java.util.regex.PatternSyntaxException;

import de.iip_ecosphere.platform.connectors.events.ConnectorInputHandler;
import de.iip_ecosphere.platform.connectors.events.EventHandlingConnector;
import de.iip_ecosphere.platform.connectors.events.PatternTriggerQuery;
import iip.datatypes.Command;

/**
 * Triggers the query for a certain AAS.
 * 
 * @author Holger Eichelberger, SSE
 */
public class MdzhInputHandler implements ConnectorInputHandler<Command> {

    @Override
    public void received(Command data, EventHandlingConnector connector) {
        if (Commands.valueOfSafe(data.getCommand()) == Commands.QUERY_CAR_AAS 
            && data.getStringParam() != null && data.getStringParam().length() > 0) {
            System.out.println("Car AAS Query " + data.getStringParam());
            try {
                connector.trigger(new PatternTriggerQuery(data.getStringParam()));
            } catch (PatternSyntaxException e) {
                System.out.println("Illegal pattern: " + e.getMessage());
            }
        }
    }

}
