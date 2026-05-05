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

package de.iip_ecosphere.platform.tools.maven.python;

import java.util.ArrayList;
import java.util.List;

/**
 * List splitter (initial, may be moved to tools.lib).
 * 
 * @author Holger Eichelberger, SSE
 * @author Gemini
 */
public class ListSplitter {

    /**
     * Splits a list of strings into sub-lists so that no sub-list exceeds {@code limit} if sub-lists are separated by 
     * {@code sep} characters.
     * 
     * @param input the input list of strings
     * @param sep the number of separation chars
     * @param limit the sub-list length limit
     * @return the splitted sub-lists
     */
    public static List<List<String>> splitByLength(List<String> input, int sep, int limit) {
        List<List<String>> result = new ArrayList<>();
        List<String> currentSubList = new ArrayList<>();
        int currentLength = 0;

        for (String word : input) {
            // Calculate length if we add this word (including the sep)
            int spaceAddition = currentSubList.isEmpty() ? 0 : sep;
            int wordLengthWithSpace = word.length() + spaceAddition;

            if (currentLength + wordLengthWithSpace <= limit) {
                currentSubList.add(word);
                currentLength += wordLengthWithSpace;
            } else {
                // Current sublist is full, start a new one
                if (!currentSubList.isEmpty()) {
                    result.add(new ArrayList<>(currentSubList));
                }
                currentSubList = new ArrayList<>();
                currentSubList.add(word);
                currentLength = word.length();
            }
        }
        // Add the final sublist if it contains items
        if (!currentSubList.isEmpty()) {
            result.add(currentSubList);
        }
        return result;
    }

}
