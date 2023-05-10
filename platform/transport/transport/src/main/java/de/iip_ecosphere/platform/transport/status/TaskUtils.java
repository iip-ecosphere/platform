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

import java.io.IOException;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.TaskRegistry;
import de.iip_ecosphere.platform.support.TaskRegistry.TaskData;
import de.iip_ecosphere.platform.support.iip_aas.json.JsonResultWrapper.ExceptionFunction;
import de.iip_ecosphere.platform.transport.Transport;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;

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

    /**
     * Executes {@code func} as task within this thread and sends respective {@link StatusMessage}s.
     * 
     * @param componentId the component id of the execution function
     * @param func the function to execute
     * @param pred optional task completed predicate function, may be <b>null</b>
     * @param params the function parameters
     * @return the task id
     */
    public static String executeAsTask(String componentId, ExceptionFunction func, TaskCompletedPredicate pred, 
        Object... params) {
        return executeAsTask(TaskRegistry.registerTask(), componentId, func, pred, params);
    }

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
        return executeAsTask(data, componentId,  func, null, params);
    }

    /**
     * Predicate to check for whether the task is completed with the given message.
     * 
     * @author Holger Eichelberger, SSE
     */
    @FunctionalInterface
    public interface TaskCompletedPredicate {

        /**
         * Returns whether the {@code task} is completed with the given message {@code msg}. Task id of {@code task}
         * and {@code msg} are already matched and the same in this call.
         * 
         * @param task the task to test for
         * @param msg the received message
         * @return {@code true} if completed, {@code false} if still ongoing
         */
        public boolean test(TaskData task, StatusMessage msg);
        
    }
    
    // checkstyle: stop exception type check
    
    /**
     * Executes {@code func} as task within the given task {@code data} and sends respective {@link StatusMessage}s.
     *
     * @param data the task data
     * @param componentId the component id of the execution function
     * @param func the function to execute
     * @param pred optional task completed predicate function, may be <b>null</b>
     * @param params the function parameters
     * @return the task id
     */
    public static String executeAsTask(TaskData data, String componentId, ExceptionFunction func, 
        TaskCompletedPredicate pred, Object... params) {
        new Thread(() -> {
            if (null != pred) {
                data.setRequiredStopCalls(2);
                ReceptionCallback<StatusMessage> cb = new ReceptionCallback<>() {

                    @Override
                    public void received(StatusMessage msg) {
                        if (msg.getTaskId() == data.getId() && pred.test(data, msg)) {
                            try {
                                Transport.getConnector().detachReceptionCallback(StatusMessage.STATUS_STREAM, this);
                            } catch (IOException e) {
                                LoggerFactory.getLogger(TaskUtils.class).error("Cannot stop tracking task status of {} "
                                    + "for component {}: {}", data.getId(), componentId, e.getMessage());
                            }
                            TaskRegistry.stopTask(data.getId());
                        }
                    }

                    @Override
                    public Class<StatusMessage> getType() {
                        return StatusMessage.class;
                    }
                };
                try {
                    Transport.createConnector().setReceptionCallback(StatusMessage.STATUS_STREAM, cb);
                } catch (IOException e) {
                    LoggerFactory.getLogger(TaskUtils.class).error("Cannot track task status of {} for component "
                        + "{}: {}", data.getId(), componentId, e.getMessage());
                }
            }
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
