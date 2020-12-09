/**
 * ******************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.aas.basyx;

import java.util.function.Function;

import org.eclipse.basyx.models.controlcomponent.ExecutionState;
import org.eclipse.basyx.vab.coder.json.connector.JSONConnector;
import org.eclipse.basyx.vab.modelprovider.VABElementProxy;
import org.eclipse.basyx.vab.protocol.basyx.connector.BaSyxConnector;

import de.iip_ecosphere.platform.support.TimeUtils;

/**
 * Helper class to create specific BaSyx invocables to be attached to AAS operations.
 * 
 * @author Holger Eichelberger, SSE
 */
public class Invocables {

    /**
     * Creates an invokable for an AAS-VAB operation, i.e., a remote REST operation.
     * 
     * @param opMode the op mode to trigger the control component
     * @param host the host name to connect to
     * @param port the TCP port on {@code host}
     * @return the invokable
     */
    public static Function<Object[], Object> createInvocable(String opMode, String host, int port) {
        return (params) -> {
            VABElementProxy proxy = new VABElementProxy("", new JSONConnector(new BaSyxConnector(host, port)));
            proxy.setModelPropertyValue("status/opMode", opMode);
            Object result = proxy.invokeOperation("/operations/service/start", params);
            while (!proxy.getModelPropertyValue("status/exState").equals(ExecutionState.COMPLETE.getValue())) {
                TimeUtils.sleep(500);
            }
            proxy.invokeOperation("operations/service/reset");
            return result;
        };
    }

}
