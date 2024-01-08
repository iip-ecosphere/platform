/**
 * ******************************************************************************
 * Copyright (c) {2023} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.support.aas.basyx;

import test.de.iip_ecosphere.platform.support.aas.TimeSeriesDataTest;

/**
 * Sets up the {@link TimeSeriesDataTest} for BaSyx. You will find the persisted AAS in the {@code output} folder.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxTimeSeriesDataTest extends TimeSeriesDataTest {

    /**
     * Executes the test standalone.
     * 
     * @param args command line arguments, the first argument may be {@code --withOperations} to 
     *   enable the creation of operations
     */
    public static void main(String[] args) {
        BaSyxExampleUtils.execute(new BaSyxTimeSeriesDataTest(), args);
    }

}
