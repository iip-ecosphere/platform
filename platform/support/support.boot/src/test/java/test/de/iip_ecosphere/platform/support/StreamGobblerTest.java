/**
 * ******************************************************************************
 * Copyright (c) {2025} The original author or authors
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.junit.Test;

import de.iip_ecosphere.platform.support.JavaUtils;
import de.iip_ecosphere.platform.support.plugins.StreamGobbler;
import org.junit.Assert;

/**
 * Tests {@link StreamGobbler}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class StreamGobblerTest {
    
    /**
     * Tests default {@link StreamGobbler}.
     * 
     * @throws IOException if the process cannot be created
     */
    @Test
    public void testGobbler() throws IOException {
        Process proc = createProcess();
        StreamGobbler.attach(proc);
        waitFor(proc);
    }

    /**
     * Tests {@link StreamGobbler} with custom consumer.
     * 
     * @throws IOException if the process cannot be created
     */
    @Test
    public void testConsumingGobbler() throws IOException {
        AtomicInteger count = new AtomicInteger();
        Consumer<String> cons = l -> count.incrementAndGet();
        Process proc = createProcess();
        StreamGobbler.attach(proc, cons, cons);
        waitFor(proc);
        Assert.assertTrue(count.get() > 1); // very conservative
    }

    /**
     * Creates a process, java running {@link Main}.
     * 
     * @return the process
     * @throws IOException if the process cannot be created
     */
    private Process createProcess() throws IOException {
        String javaPath = JavaUtils.getJavaPath();
        Process proc = new ProcessBuilder(javaPath + "/java", "-cp", 
            "target/test-classes" + File.pathSeparator + "target/classes", Main.class.getName(), "3")
            .start();
        return proc;
    }

    /**
     * Quietly waits for the termination of {@code proc}.
     * 
     * @param proc the process to wait for
     */
    private void waitFor(Process proc) {
        try {
            proc.waitFor();
        } catch (InterruptedException e) {
        }
    }

}
