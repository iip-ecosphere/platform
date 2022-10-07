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

package test.de.iip_ecosphere.platform.support;

import org.junit.Test;

import de.iip_ecosphere.platform.support.TaskRegistry;
import de.iip_ecosphere.platform.support.TaskRegistry.TaskData;
import de.iip_ecosphere.platform.support.TaskRegistry.TaskStatus;
import de.iip_ecosphere.platform.support.TimeUtils;

import org.junit.Assert;

/**
 * Tests {@link TaskRegistry}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TaskRegistryTests {

    /**
     * Tests {@link TaskRegistry}.
     */
    @Test
    public void testRegistry() {
        long old = TaskRegistry.setTimeout(1000);
        
        Assert.assertEquals(TaskRegistry.NO_TASK, TaskRegistry.getTaskData());
        TaskData task = TaskRegistry.registerTask();
        Assert.assertNotNull(task);
        Assert.assertTrue(task.getId().length() > 0);
        Assert.assertEquals(TaskStatus.RUNNING, task.getStatus());
        
        Assert.assertEquals(task, TaskRegistry.getTaskData());
        Assert.assertEquals(task, TaskRegistry.getTaskData(task.getId()));
        
        TaskRegistry.stopTask();
        Assert.assertEquals(TaskRegistry.NO_TASK, TaskRegistry.getTaskData());
        Assert.assertEquals(TaskRegistry.NO_TASK, TaskRegistry.getTaskData(task.getId()));

        task = TaskRegistry.registerTask();
        TaskRegistry.stopTask(task.getId());
        Assert.assertEquals(TaskRegistry.NO_TASK, TaskRegistry.getTaskData(task.getId()));
        Assert.assertEquals(TaskRegistry.NO_TASK, TaskRegistry.getTaskData());

        task = TaskRegistry.registerTask();
        TimeUtils.sleep(1500);
        TaskRegistry.cleanup();
        // still running
        Assert.assertEquals(task, TaskRegistry.getTaskData());
        Assert.assertEquals(task, TaskRegistry.getTaskData(task.getId()));
        // not stopped, suppressing
        TaskData task2 = TaskRegistry.registerTask();
        Assert.assertEquals(task2, TaskRegistry.getTaskData());
        Assert.assertEquals(task2, TaskRegistry.getTaskData(task2.getId()));
        Assert.assertEquals(task, TaskRegistry.getTaskData(task.getId()));
        TimeUtils.sleep(1500);
        TaskRegistry.cleanup();
        Assert.assertEquals(task2, TaskRegistry.getTaskData());
        Assert.assertEquals(task2, TaskRegistry.getTaskData(task2.getId()));
        Assert.assertEquals(TaskRegistry.NO_TASK, TaskRegistry.getTaskData(task.getId()));
        TaskRegistry.stopTask(task2.getId());
        Assert.assertEquals(TaskRegistry.NO_TASK, TaskRegistry.getTaskData());
        Assert.assertEquals(TaskRegistry.NO_TASK, TaskRegistry.getTaskData(task2.getId()));
        
        Assert.assertEquals(1000, TaskRegistry.setTimeout(old));
    }

}
