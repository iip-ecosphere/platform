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

package de.iip_ecosphere.platform.support;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Thread-based mechanism to track tasks (AAS, through UI). Call {@link #cleanup()} regularly 
 * depending on {@link #getTimeout()}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TaskRegistry {

    public static final TaskData NO_TASK = new TaskData("").setStatus(TaskStatus.UNKNOWN);
    private static long timeout = 2 * 60 * 1000;
    private static Map<String, TaskData> idToData = new HashMap<>();
    private static Map<Long, TaskData> threadToData = new HashMap<>();
    
    /**
     * Constants to represent the task status.
     * 
     * @author Holger Eichelberger, SSE
     */
    public enum TaskStatus {
        UNKNOWN,
        RUNNING,
        STOPPED,
        SUPPRESSED
    }

    /**
     * Represents data associated to a task. The task data may require multiple task status changes to count as 
     * {@lint TaskStatus#STOPPED}. It may also count/compare a certain number of generic events.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class TaskData {
        
        private String id;
        private long threadId;
        private long timestamp;
        private TaskStatus status;
        private int requiredStopCalls = 1;
        private int stopCallCount = 0;
        private int maxEventCount = 0;
        private int eventCount = 0;
        
        /**
         * Creates an instance, sets the id and turns the status to {@link TaskStatus#RUNNING}.
         * 
         * @param taskId a given task id, <b>null</b> for generate one
         */
        private TaskData(String taskId) {
            threadId = Thread.currentThread().getId();
            if (null != taskId) {
                id = taskId;
            } else {
                id = String.valueOf(System.currentTimeMillis()) + String.valueOf(threadId);
            }
            setStatus(TaskStatus.RUNNING);
        }
        
        /**
         * Changes the task status and the timestamp of change.
         * 
         * @param status the new status
         * @return <b>this</b>
         */
        private TaskData setStatus(TaskStatus status) {
            if (TaskStatus.STOPPED == status) {
                stopCallCount++;
            }
            if ((TaskStatus.STOPPED == status && stopCallCount == requiredStopCalls) || status != TaskStatus.STOPPED) {
                this.status = status;
                this.timestamp = System.currentTimeMillis();
            }
            return this;
        }
        
        /**
         * Changes the number of required stop calls ({@link #setStatus(TaskStatus)} with {@link TaskStatus#STOPPED}) 
         * to change the task to stopped. The default value is 1.
         * 
         * @param requiredStopCalls the number of required stop calls, ignored if not positive
         */
        public void setRequiredStopCalls(int requiredStopCalls) {
            if (requiredStopCalls > 0) {
                this.requiredStopCalls = requiredStopCalls;
            }
        }
        
        /**
         * Defines the maximum number of (expected) events.
         * 
         * @param maxEventCount the maximum number of events
         */
        public void setMaxEventCount(int maxEventCount) {
            this.maxEventCount = maxEventCount;
        }

        /**
         * Increases the event counter.
         * 
         * @return the new counter value
         */
        public int incEventCount() {
            return eventCount++;
        }
        
        /**
         * Returns whether the maximum number of (expected) events was already reached.
         * 
         * @return {@code true} for reached, {@code false} else
         */
        public boolean maxEventCountReached() {
            return eventCount >= maxEventCount;
        }
        
        /**
         * Returns the task id.
         * 
         * @return the id
         */
        public String getId() {
            return id;
        }
        
        /**
         * Returns the task status.
         * 
         * @return the status
         */
        public TaskStatus getStatus() {
            return status;
        }
        
    }
    
    /**
     * Returns the timeout indicating a {@link TaskData} instance to be cleaned up.
     * 
     * @return the timeout in ms
     */
    public static long getTimeout() {
        return timeout;
    }

    /**
     * Changes the timeout indicating a {@link TaskData} instance to be cleaned up.
     * 
     * @param to the new timeout in ms
     * @return the old timeout in ms
     */
    public static long setTimeout(long to) {
        long result = timeout;
        timeout = to;
        return result;
    }

    /**
     * Registers a new task on the current task.
     * 
     * @return the task data of the new task
     */
    public static TaskData registerTask() {
        return registerTask(Thread.currentThread());
    }

    /**
     * Registers a new task on the current task.
     * 
     * @param taskId a given task id, e.g., when passed in through distributed execution, <b>null</b> for generate one
     * @return the task data of the new task
     */
    public static TaskData registerTask(String taskId) {
        return registerTask(Thread.currentThread(), taskId);
    }

    /**
     * Registers a new task for the given {@code thread}, turning a still running one 
     * into {@link TaskStatus#SUPPRESSED}.
     * 
     * @param thread the thread to register the task for
     * @return the task data of the new task
     */
    public static TaskData registerTask(Thread thread) {
        return registerTask(thread, null);
    }

    /**
     * Registers a new task for the given {@code thread}, turning a still running one 
     * into {@link TaskStatus#SUPPRESSED}.
     * 
     * @param thread the thread to register the task for
     * @param taskId a given task id, e.g., when passed in through distributed execution, <b>null</b> for generate one
     * @return the task data of the new task
     */
    public static synchronized TaskData registerTask(Thread thread, String taskId) {
        TaskData data = new TaskData(taskId);
        long current = thread.getId();
        TaskData old = threadToData.remove(current);
        // not for idToData, keep for requests until cleanup
        if (null != old && old.status == TaskStatus.RUNNING) {
            old.status = TaskStatus.SUPPRESSED;
        }
        threadToData.put(current, data);
        idToData.put(data.id, data);
        return data;
    }

    /**
     * Returns the task data assigned to the current thread.
     * 
     * @return the task data, may be {@link #NO_TASK} for none 
     */
    public static TaskData getTaskData() {
        return getTaskData(Thread.currentThread());
    }
    
    /**
     * Returns the task data assigned to the given thread.
     * 
     * @param thread the thread denoting the task
     * @return the task data, may be {@link #NO_TASK} for none 
     */
    public static synchronized TaskData getTaskData(Thread thread) {
        TaskData result = threadToData.get(thread.getId());
        if (null == result) {
            result = NO_TASK;
        }
        return result;
    }

    /**
     * Returns the task data assigned to the given task id.
     * 
     * @param id the task id denoting the task
     * @return the task data, may be {@link #NO_TASK} for none 
     */
    public static synchronized TaskData getTaskData(String id) {
        TaskData result = idToData.get(id);
        if (null == result) {
            result = NO_TASK;
        }
        return result;
    }


    /**
     * Stops the actual task of the current thread.
     */
    public static void stopTask() {
        stopTask(Thread.currentThread());
    }

    /**
     * Stops the given task.
     * 
     * @param thread the thread denoting the task
     */
    public static synchronized void stopTask(Thread thread) {
        TaskData data = threadToData.get(thread.getId());
        if (null != data) {
            data.setStatus(TaskStatus.STOPPED);
            threadToData.remove(thread.getId());
            idToData.remove(data.id);
        }
    }
    
    /**
     * Stops the given task.
     * 
     * @param id the task id denoting the task
     */
    public static synchronized void stopTask(String id) {
        TaskData data = idToData.get(id);
        if (null != data) {
            data.setStatus(TaskStatus.STOPPED);
            threadToData.remove(data.threadId);
            idToData.remove(data.id);
        }
    }

    /**
     * Cleans up outdated task data after no change within {@link #getTimeout()}. 
     */
    public static synchronized void cleanup() {
        long now = System.currentTimeMillis();
        cleanup(now, threadToData.values().iterator());
        cleanup(now, idToData.values().iterator());
    }

    /**
     * Checks the task data in {@code iter} for cleanup.
     * 
     * @param now the timestamp for calculating the timeout vs. the last change in a {@link TaskData} instance
     * @param iter the iterator for traversing/removing instances
     */
    private static void cleanup(long now, Iterator<TaskData> iter) {
        while (iter.hasNext()) {
            TaskData d = iter.next();
            if (d.getStatus() != TaskStatus.RUNNING && now - d.timestamp > timeout) {
                iter.remove();
            }
        }
    }

}
