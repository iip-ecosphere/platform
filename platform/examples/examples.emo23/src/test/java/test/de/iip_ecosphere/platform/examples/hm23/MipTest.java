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

package test.de.iip_ecosphere.platform.examples.hm23;

import java.io.IOException;

import org.junit.Test;

import de.iip_ecosphere.platform.transport.serialization.GenericJsonSerializer;
import iip.datatypes.MipAiPythonOutput;
import iip.datatypes.MipAiPythonOutputImpl;
import iip.datatypes.MipMqttInputImpl;
import iip.datatypes.MipMqttOutput;
import iip.datatypes.MipMqttOutputImpl;
import iip.nodes.MipMQTTDataConnectorFormatterSerializer;
import iip.nodes.MipMQTTDataConnectorParserSerializer;
import iip.serializers.MipAiPythonOutputImplSerializer;
import org.junit.Assert;

/**
 * Tests MIP serialization (data pruning problem).
 * 
 * @author Holger Eichelberger, SSE
 */
public class MipTest {

    /**
     * Tests MIP serialization (data pruning problem).
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testMipData() throws IOException {
        MipAiPythonOutputImpl d = new MipAiPythonOutputImpl();
        d.setAicontext("read_idtag_data");
        d.setAidate("Fri Aug 18 15:53:47 2023");
        d.setAifrom("ATML2589040200002054");
        d.setAiid_tag("4015");
        d.setAireader("GF708");
        String array = "[";
        final int count = 3100;
        for (int i = 0; i < count; i++) {
            array += "311";
            if (i + 1 < count) {
                array += ",";
            }
        }
        array += "]";
        d.setAiraw_signal_clock(array);
        d.setAiraw_signal_data1(array);
        Assert.assertEquals(array, d.getAiraw_signal_clock());
        
        MipAiPythonOutputImplSerializer ser = new MipAiPythonOutputImplSerializer();
        byte[] s = ser.to(d); 
        MipAiPythonOutputImpl o1 = ser.from(s);
        Assert.assertEquals(array, o1.getAiraw_signal_clock());
        System.out.println(new String(s));
        
        GenericJsonSerializer<MipAiPythonOutput> ser2 = new GenericJsonSerializer<>(MipAiPythonOutput.class);
        s = ser2.to(d); 
        MipAiPythonOutput o2 = ser.from(s);
        Assert.assertEquals(array, o2.getAiraw_signal_clock());
        System.out.println(new String(s));

        GenericJsonSerializer<MipAiPythonOutputImpl> ser3 = new GenericJsonSerializer<>(MipAiPythonOutputImpl.class);
        s = ser3.to(d); 
        MipAiPythonOutputImpl o3 = ser.from(s);
        Assert.assertEquals(array, o3.getAiraw_signal_clock());
        System.out.println(new String(s));
    }
    
    /**
     * Tests the connector parser/formatter.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testMipParserFormatter() throws IOException {
        
        
        MipMQTTDataConnectorParserSerializer pSer 
            = new MipMQTTDataConnectorParserSerializer("US-ASCII", null, null);
        
        MipMqttOutputImpl outData = new MipMqttOutputImpl();
        outData.setMipcontext("read_idtag_data");
        outData.setMipdate("Fri Aug 18 15:53:47 2023");
        outData.setMipfrom("ATML2589040200002054");
        outData.setMipid_tag("4015");
        outData.setMipreader("GF708");
        String array = "[";
        final int count = 3100;
        for (int i = 0; i < count; i++) {
            array += "311";
            if (i + 1 < count) {
                array += ",";
            }
        }
        array += "]";
        outData.setMipraw_signal_clock(array);
        outData.setMipraw_signal_data1(array);
        Assert.assertEquals(array, outData.getMipraw_signal_clock());

        GenericJsonSerializer<MipMqttOutputImpl> outSer = new GenericJsonSerializer<>(MipMqttOutputImpl.class);
        MipMqttOutput pOut = pSer.from(outSer.to(outData));
        
        Assert.assertEquals(outData.getMipcontext(), pOut.getMipcontext());
        Assert.assertEquals(outData.getMipdate(), pOut.getMipdate());
        Assert.assertEquals(outData.getMipfrom(), pOut.getMipfrom());
        Assert.assertEquals(outData.getMipid_tag(), pOut.getMipid_tag());
        Assert.assertEquals(outData.getMipreader(), pOut.getMipreader());
        Assert.assertEquals(outData.getMipraw_signal_clock(), pOut.getMipraw_signal_clock());
        Assert.assertEquals(outData.getMipraw_signal_data1(), pOut.getMipraw_signal_data1());
        
        MipMqttInputImpl inData = new MipMqttInputImpl();
        inData.setMipcontext("iip_echosphere_id_tag_data_format");
        inData.setMipcommand("xxx");
        inData.setMipdate(outData.getMipdate());
        inData.setMipto(outData.getMipfrom());
        inData.setMipfrom("IIP_Ecosphere");
        inData.setMipbitstream_ai_clock("0101010101010101010101");
        inData.setMipbitstream_ai_data1("0101011100010101110100");
        inData.setMipbitstream_ai_data2("0000000000000000000000");
        inData.setMipreader(outData.getMipreader());
        
        MipMQTTDataConnectorFormatterSerializer fSer 
            = new MipMQTTDataConnectorFormatterSerializer("US-ASCII", null, null);
        GenericJsonSerializer<MipMqttInputImpl> inSer = new GenericJsonSerializer<>(MipMqttInputImpl.class);
        MipMqttInputImpl fIn = inSer.from(fSer.to(inData));

        Assert.assertEquals(inData.getMipcontext(), fIn.getMipcontext());
        Assert.assertEquals(inData.getMipcommand(), fIn.getMipcommand());
        Assert.assertEquals(inData.getMipdate(), fIn.getMipdate());
        Assert.assertEquals(inData.getMipfrom(), fIn.getMipfrom());
        Assert.assertEquals(inData.getMipto(), fIn.getMipto());
        Assert.assertEquals(inData.getMipbitstream_ai_clock(), fIn.getMipbitstream_ai_clock());
        Assert.assertEquals(inData.getMipbitstream_ai_data1(), fIn.getMipbitstream_ai_data1());
        Assert.assertEquals(inData.getMipreader(), fIn.getMipreader());
    }

}
