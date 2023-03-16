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

package test.de.iip_ecosphere.platform.services.environment.testing;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.services.environment.testing.DataRecorder;

/**
 * Tests {@link DataRecorder}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class DataRecorderTest {
    
    /**
     * Testing data class, usual IIP style.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class Data {
        
        private int value;

        /**
         * Returns the value.
         * 
         * @return the value
         */
        public int getValue() {
            return value;
        }
        
        /**
         * Changes the value.
         * 
         * @param value the new value
         */
        public void setValue(int value) {
            this.value = value;
        }
        
    }
    
    /**
     * Tests the recorder with a valid temporary file.
     * 
     * @throws IOException shall not occur if successful
     */
    @Test
    public void testRecorderValidFile() throws IOException {
        File f = File.createTempFile("iip-test", ".txt");
        testRecorder(f, new DataRecorder(f, DataRecorder.JSON_FORMATTER), DataRecorder.JSON_FORMATTER);
        f.delete();
    }
    
    /**
     * Tests the recorder with an invalid file, shall go to stdout.
     */
    @Test
    public void testRecorderInvalidFile() {
        testRecorder(null, new DataRecorder(new File(""), DataRecorder.JSON_FORMATTER), DataRecorder.JSON_FORMATTER);
    }

    /**
     * Tests the recorder.
     * 
     * @param file the file to read for assertion, may be <b>null</b> for none
     * @param recorder the recorder instance to test
     * @param formatter the formatter used to create the recorder
     */
    private void testRecorder(File file, DataRecorder recorder, Function<Object, String> formatter) {
        Data data = new Data();
        data.setValue(42);
        String text = formatter.apply(data);
        
        recorder.record(null, data);
        recorder.record("channel", data);
        recorder.emitChannel(false);
        recorder.record(null, data);
        recorder.record("channel", data);
        recorder.close();
        
        if (null != file) {
            try {
                String contents = org.apache.commons.io.FileUtils.readFileToString(file, Charset.defaultCharset());
                contents = contents.replace("\n", "*").replace("\r", ""); // unify win/linux EOL
                Assert.assertEquals(text + "*" + "channel: " + text + "*" + text + "*" + text + "*", contents);
            } catch (IOException e) {
                Assert.fail("Unexpected exception: " + e.getMessage());
            }
        }
    }


}
