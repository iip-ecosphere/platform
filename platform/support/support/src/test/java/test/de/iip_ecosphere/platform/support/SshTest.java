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

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.ssh.Ssh;
import de.iip_ecosphere.platform.support.ssh.Ssh.SshServer;
import test.de.iip_ecosphere.platform.support.ssh.TestSsh;

/**
 * Tests {@link Ssh}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class SshTest {
    
    /**
     * Tests basic SSH functions.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testSsh() throws IOException {
        // just the very basic
        Ssh ssh = Ssh.getInstance();
        Assert.assertTrue(ssh instanceof TestSsh);
        Ssh.setInstance(ssh);
        
        SshServer server = ssh.createServer(new ServerAddress(Schema.SSH));
        server.setAuthenticator((u, p) -> true);
        server.setHostKey(new File("file.ser"));
        server.setShellInit(null); // ignore

        server.start();
        TimeUtils.sleep(500);
        server.stop(true);
    }

}
