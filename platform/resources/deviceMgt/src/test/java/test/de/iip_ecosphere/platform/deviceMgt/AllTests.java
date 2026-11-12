/********************************************************************************
 * Copyright (c) {2021} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.deviceMgt;

import test.de.iip_ecosphere.platform.deviceMgt.registry.DeviceRegistryAasClientTest;
import test.de.iip_ecosphere.platform.deviceMgt.registry.DeviceRegistryAasTest;
import test.de.iip_ecosphere.platform.deviceMgt.registry.DeviceRegistryFactoryTest;
import test.de.iip_ecosphere.platform.deviceMgt.ssh.ProxyTest;
import test.de.iip_ecosphere.platform.deviceMgt.storage.StorageFactoryTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Defines the tests to be executed.
 * 
 * @author Dennis Pidun, University of Hildesheim
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    DeviceManagementAasTest.class,
    DeviceRegistryAasTest.class,
    DeviceRegistryFactoryTest.class,
    DeviceRegistryAasClientTest.class,
    ProxyTest.class,
    StorageFactoryTest.class
})
public class AllTests {
}
