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

package test.de.iip_ecosphere.platform.configuration;

import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.configuration.DrawflowGraphFormat;
import de.iip_ecosphere.platform.configuration.ivml.IvmlGraphMapper.IvmlGraph;

/**
 * Tests {@link DrawflowGraphFormat}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class DataflowGraphFormatTest extends AbstractGraphTest {
    
    /**
     * Tests turning a graph into a String representation.
     * 
     * @throws ExecutionException shall not occur
     */
    @Test
    public void testToString() throws ExecutionException {
        DrawflowGraphFormat format = new DrawflowGraphFormat();
        Assert.assertNotNull(format.getName());
        Assert.assertNotNull(format.getFormatKind());
        
        IvmlGraph graph;
        String json = format.toString(null);
        Assert.assertNotNull(json);
        assertGraph(createEmptyGraph(), format.fromString(json, FACTORY, VAR_PROVIDER));

        graph = createEmptyGraph();
        json = format.toString(graph);
        Assert.assertNotNull(json);
        assertGraph(graph, format.fromString(json, FACTORY, VAR_PROVIDER));

        graph = createAbcGraph();
        json = format.toString(graph);
        Assert.assertNotNull(json);
        assertGraph(graph, format.fromString(json, FACTORY, VAR_PROVIDER));
    }

}
