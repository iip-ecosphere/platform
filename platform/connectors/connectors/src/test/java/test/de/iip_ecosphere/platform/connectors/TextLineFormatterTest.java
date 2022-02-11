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

package test.de.iip_ecosphere.platform.connectors;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import de.iip_ecosphere.platform.connectors.formatter.OutputFormatter.OutputConverter;
import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.connectors.formatter.TextLineFormatter;

/**
 * Tests {@link TextLineFormatter}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TextLineFormatterTest {
    
    /**
     * Tests the formatter.
     */
    @Test
    public void testFormatter() throws IOException {
        String charset = StandardCharsets.UTF_8.name();
        TextLineFormatter formatter = new TextLineFormatter(charset, "#");
        OutputConverter<String> conv = formatter.getConverter();
        Assert.assertNotNull(conv);
        formatter.add("field1", conv.fromString("abba"));
        formatter.add("field2", conv.fromInt(21));
        formatter.add("field2", conv.fromDouble(1.234));
        formatter.add("field2", conv.fromFloat(1.235f));
        formatter.add("field2", conv.fromLong(3456L));
        formatter.add("field2", conv.fromBoolean(true));
        byte[] data = formatter.chunkCompleted();

        String text = new String(data, charset);
        Assert.assertEquals("abba#21#1.234#1.235#3456#true", text);
    }

}
