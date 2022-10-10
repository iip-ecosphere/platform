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

package de.iip_ecosphere.platform.transport.status;

import de.iip_ecosphere.platform.support.TaskRegistry;
import de.iip_ecosphere.platform.support.TaskRegistry.TaskData;
import de.iip_ecosphere.platform.support.iip_aas.json.JsonResultWrapper.ExceptionFunction;
import de.iip_ecosphere.platform.transport.Transport;

/**
 * Generic execution of tasks in combination with {@link TaskRegistry}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TaskUtils {
    
    /**
     * Executes {@code func} as task within this thread and sends respective {@link StatusMessage}s.
     * 
     * @param componentId the component id of the execution function
     * @param func the function to execute
     * @param params the function parameters
     * @return the task id
     */
    public static String executeAsTask(String componentId, ExceptionFunction func, Object... params) {
        return executeAsTask(TaskRegistry.registerTask(), componentId, func, params);
    }
    
    // checkstyle: stop exception type check
    
    /**
     * Executes {@code func} as task within the given task {@code data} and sends respective {@link StatusMessage}s.
     *
     * @param data the task data
     * @param componentId the component id of the execution function
     * @param func the function to execute
     * @param params the function parameters
     * @return the task id
     */
    public static String executeAsTask(TaskData data, String componentId, ExceptionFunction func, Object... params) {
        new Thread(() -> {
            try {
                Transport.sendProcessStatus(componentId, ActionTypes.RESULT, func.apply(params));
            } catch (Throwable e) {
                Transport.sendProcessStatus(componentId, ActionTypes.RESULT, e.getMessage());
            }
            TaskRegistry.stopTask(data.getId());
        }).start();
        return data.getId();        
    }
    
    // checkstyle: resume exception type check

}
