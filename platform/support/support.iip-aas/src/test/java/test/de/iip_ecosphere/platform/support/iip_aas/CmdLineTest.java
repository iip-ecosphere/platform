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

package test.de.iip_ecosphere.platform.support.iip_aas;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.iip_aas.config.CmdLine;

/**
 * Tests {@link CmdLine}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class CmdLineTest {
    
    /**
     * Tests {@link CmdLine#parseToArgs(String, java.util.List)}.
     */
    @Test
    public void testParseToArgs() {
        List<String> result = new ArrayList<String>();

        CmdLine.parseToArgs("", result);
        Assert.assertTrue(result.size() == 0);
        
        CmdLine.parseToArgs("--a=0", result);
        Assert.assertTrue(result.size() == 1);
        Assert.assertEquals("--a=0", result.get(0));
        result.clear();

        CmdLine.parseToArgs("--a=0 --b=5", result);
        Assert.assertTrue(result.size() == 2);
        Assert.assertEquals("--a=0", result.get(0));
        Assert.assertEquals("--b=5", result.get(1));
        result.clear();

        CmdLine.parseToArgs("\"--a=0 --b=5\"", result);
        Assert.assertTrue(result.size() == 1);
        Assert.assertEquals("\"--a=0 --b=5\"", result.get(0));
        result.clear();

        CmdLine.parseToArgs("--a=0 --c=\"x y\" --b=5", result);
        Assert.assertTrue(result.size() == 3);
        Assert.assertEquals("--a=0", result.get(0));
        Assert.assertEquals("--c=\"x y\"", result.get(1));
        Assert.assertEquals("--b=5", result.get(2));
        result.clear();
    }
    
    /**
     * Tests the getArg functions.
     */
    @Test
    public void testGetArg() {
        String[] args = {"--bli=5", "--bla=6", "--xyz=String", "whatever", "--bVal=true"};
        Assert.assertEquals("6", CmdLine.getArg(args, "bla", ""));
        Assert.assertEquals("5", CmdLine.getArg(args, "bli", ""));
        Assert.assertEquals("String", CmdLine.getArg(args, "xyz", ""));

        Assert.assertEquals(5, CmdLine.getIntArg(args, "bli", 0));
        Assert.assertEquals(0, CmdLine.getIntArg(args, "blii", 0));
        
        Assert.assertEquals(true, CmdLine.getBooleanArg(args, "bVal", false));
        Assert.assertEquals(false, CmdLine.getBooleanArg(args, "xVal", false));
        Assert.assertEquals(false, CmdLine.getBooleanArg(args, "bla", false));
    }

    /**
     * Tests {@link CmdLine#toArgs(String)}.
     */
    @Test
    public void testToArgs() {
        String[] tmp = CmdLine.toArgs("");
        Assert.assertNotNull(tmp);
        Assert.assertEquals(0, tmp.length);

        tmp = CmdLine.toArgs("--iip.app.p=5 --iip.app.w=7 --transport=5");
        Assert.assertNotNull(tmp);
        Assert.assertEquals(3, tmp.length);
        Assert.assertEquals("--iip.app.p=5", tmp[0]);
        Assert.assertEquals("--iip.app.w=7", tmp[1]);
        Assert.assertEquals("--transport=5", tmp[2]);

        tmp = CmdLine.toArgs("--iip.app.p='5' --iip.app.w=\"7\" --transport=5");
        Assert.assertNotNull(tmp);
        Assert.assertEquals(3, tmp.length);
        Assert.assertEquals("--iip.app.p='5'", tmp[0]);
        Assert.assertEquals("--iip.app.w=\"7\"", tmp[1]);
        Assert.assertEquals("--transport=5", tmp[2]);
    }
    
}
