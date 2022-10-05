/********************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/
package test.de.iip_ecosphere.platform.support.iip_aas;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Defines the tests to be executed.
 * 
 * @author Holger Eichelberger, SSE
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    IipVersionTest.class,
    IdTest.class,
    VersionTest.class,
    ClassUtilityTest.class,
    AasPartRegistryTest.class,
    JsonUtilsTest.class,
    AasUtilsTest.class,
    JsonResultWrapperTest.class,
    NetworkManagerAasTest.class,
    PlatformAasTest.class,
    AbstractSetupTest.class,
    CmdLineTest.class,
    YamlSemanticIdResolverDescriptorTest.class,
    NameplateSetupTest.class
})
public class AllTests {
}
