/**
 * ******************************************************************************
 * Copyright (c) {2026} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.tools.maven.python;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import de.iip_ecosphere.platform.tools.maven.python.ListSplitter;
import org.junit.Assert;

/**
 * Tests {@link ListSplitter}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ListSplitterTest {

    /**
     * Tests {@link ListSplitter#splitByLength(List, int, int)}.
     */
    @Test
    public void testListSplitter() {
        List<String> words = new ArrayList<>();
        Collections.addAll(words, "The", "quick", "brown", "fox", "jumps", "over");

        assertSplit(words, " ", 10);
        assertSplit(words, " ", 20);
        assertSplit(words, "  ", 20);
    }
    
    /**
     * Asserts a split scenario.
     * 
     * @param words the input word list
     * @param sep the separator string
     * @param limit the sub-list length
     */
    private void assertSplit(List<String> words, String sep, int limit) {
        List<List<String>> splitLists = ListSplitter.splitByLength(words, sep.length(), limit);
        Assert.assertNotNull(splitLists);
        List<String> check = new ArrayList<>(words);
        for (List<String> sub : splitLists) {
            Assert.assertTrue(String.join(sep, sub).length() <= limit);
            for (String s : sub) {
                check.remove(s);
            }
        }
        Assert.assertTrue(check.isEmpty());
    }

}
