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
package test.de.iip_ecosphere.platform.kiServices.functions;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import test.de.iip_ecosphere.platform.kiServices.functions.aggregation.ChannelTimeSeriesAggregatorTest;

/**
 * Defines the tests to be executed.
 * 
 * @author Alexander Weber, SSE
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    ImageTests.class,
    ChannelTimeSeriesAggregatorTest.class
})
public class AllTests {
}
