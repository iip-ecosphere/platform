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

package de.iip_ecosphere.platform.support.iip_aas.config;

import java.util.List;

/**
 * Simple command line utilities.
 * 
 * @author Holger Eichelberger, SSE
 */
public class CmdLine {

    /**
     * Parses given {@code text} into individual arguments considering double quotes for string escapement with space 
     * as a command separator.
     * 
     * @param text the text to parse
     * @param cmds the commands (to be modified as a side effect)
     */
    public static void parseToArgs(String text, List<String> cmds) {
        parseToArgs(text, cmds, ' ');
    }

    /**
     * Parses given {@code text} into individual arguments considering double quotes for string escapement and 
     * a given command separator.
     * 
     * @param text the text to parse
     * @param cmds the commands (to be modified as a side effect)
     * @param separator the separator (usually ' ')
     */
    public static void parseToArgs(String text, List<String> cmds, char separator) {
        boolean inQuote = false;
        text = text.trim();
        int lastStart = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if ('"' == c) {
                inQuote = !inQuote;
            } 
            if (separator == c && !inQuote || i + 1 == text.length()) {
                String cmd = text.substring(lastStart, i + 1).trim(); 
                if (cmd.length() > 0) {
                    cmds.add(cmd);
                }
                lastStart = i + 1;
            }
        }
    }

}
