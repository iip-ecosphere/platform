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

package test.de.iip_ecosphere.platform.support;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import de.iip_ecosphere.platform.support.PidFile;
import org.junit.Assert;

/**
 * Tests {@link PidFile}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class PidFileTest {
    
    /**
     * Tests the {@link PidFile} functionality.
     */
    @Test
    public void testPidFile() throws IOException {
        String filename = "testPidFile.pid";
        File file = new File(FileUtils.getTempDirectory(), filename);
        FileUtils.deleteQuietly(file); // if it's still there, get rid of it
        
        PidFile pid = PidFile.createInDefaultDir(filename, false);
        System.out.println("PID file " + pid.getPath() + ": " + pid.getPid());
        Assert.assertEquals(PidFile.getJvmPid(), pid.getPid());
        Assert.assertFalse(pid.isDeleteOnExit());
        Assert.assertEquals(file, pid.getPath().toFile());
        file.delete(); // unusual, but needed for test
        
        pid = PidFile.createInDefaultDir(filename, true);
        System.out.println("PID file " + pid.getPath() + ": " + pid.getPid());
        Assert.assertEquals(PidFile.getJvmPid(), pid.getPid());
        Assert.assertTrue(pid.isDeleteOnExit());
        Assert.assertEquals(file, pid.getPath().toFile());
    }

}
