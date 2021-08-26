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

package test.de.iip_ecosphere.platform.support.aas;

import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.InvocablesCreator;
import de.iip_ecosphere.platform.support.aas.LocalInvocablesCreator;
import de.iip_ecosphere.platform.support.aas.LocalProtocolServerBuilder;
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder;
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder.PayloadConsumer;
import de.iip_ecosphere.platform.support.aas.SerialPayloadCodec;
import de.iip_ecosphere.platform.support.aas.SimpleOperationsProvider;

/**
 * Tests the local invocation classes.
 * 
 * @author Holger Eichelberger, SSE
 */
public class LocalInvocationTest {

    private int propVal = 0;

    /**
     * Tests {@link SerialPayloadCodec}.
     */
    @Test
    public void testSerialPayloadCodec() {
        SerialPayloadCodec codec = new SerialPayloadCodec();
        final AtomicBoolean decoded = new AtomicBoolean(false);
        final String info = "info";
        byte[] payload = "PAYLOAD".getBytes();
        byte[] data = codec.encode(info, payload);
        codec.decode(data, new PayloadConsumer() {
            
            @Override
            public void decoded(String inf, byte[] pload) {
                Assert.assertEquals(info, inf);
                Assert.assertArrayEquals(payload, pload);
                decoded.set(true);
            }
        });
        Assert.assertTrue(decoded.get());
        Assert.assertNotNull(codec.getCharset());
        codec.setCharset(Charset.defaultCharset());
        Assert.assertEquals(Charset.defaultCharset(), codec.getCharset());
        // ignore
        Assert.assertTrue(codec.getDataBytesLength() >= 0);
        Assert.assertTrue(codec.decodeDataLength(data) >= 0);
    }
    
    /**
     * Implements a setter for {@link #testLocalInvocation()}.
     * 
     * @param obj the object to be used as value for {@link #propVal}
     */
    private void setPropVal(Object obj) {
        if (obj instanceof Integer) { 
            propVal = ((Integer) obj).intValue(); 
        }
    }

    /**
     * Tests the local invocation classes {@link LocalInvocablesCreator} and {@link LocalProtocolServerBuilder} with
     * {@link SimpleOperationsProvider}.
     */
    @Test
    public void testLocalInvocation() {
        SimpleOperationsProvider provider = new SimpleOperationsProvider();
        LocalInvocablesCreator creator = new LocalInvocablesCreator(provider);
        LocalProtocolServerBuilder pBuilder = new LocalProtocolServerBuilder(provider);
        
        Supplier<Object> myGetter = creator.createGetter("prop");
        Consumer<Object> mySetter = creator.createSetter("prop");
        Function<Object[], Object> myFunc = creator.createInvocable("myFunc");
        provider.defineOperation("myCat", "myFunc", myFunc);
        
        Assert.assertNull(provider.getGetter("prop"));
        Assert.assertNull(provider.getSetter("prop"));
        Assert.assertNull(provider.getServiceFunction("myFunc"));
        Assert.assertTrue(provider.getOperation("myCat", "myFunc") == myFunc);
        
        Assert.assertNotNull(pBuilder.createPayloadCodec());
        Assert.assertNotNull(pBuilder.build());
        Function<Object[], Object>  implFunc = param -> "FUNC";
        pBuilder.defineOperation("myFunc", implFunc);
        Supplier<Object> implGetter = () -> propVal;
        Consumer<Object> implSetter = p -> setPropVal(p);
        pBuilder.defineProperty("prop", implGetter, implSetter);

        Assert.assertTrue(provider.getGetter("prop") == implGetter);
        Assert.assertTrue(provider.getSetter("prop") == implSetter);
        Assert.assertTrue(provider.getServiceFunction("myFunc") == implFunc);

        Assert.assertEquals(propVal, myGetter.get());
        mySetter.accept(25);
        Assert.assertEquals(25, propVal);
        Assert.assertEquals(propVal, myGetter.get());

        Assert.assertEquals("FUNC", myFunc.apply(new Object[0]));
    }

    /**
     * Tests the local invocation classes {@link LocalInvocablesCreator} and {@link LocalProtocolServerBuilder} with
     * {@link SimpleOperationsProvider}.
     */
    @Test
    public void testLocalInvocationViaFactory() {
        AasFactory factory = AasFactory.getInstance();
        // both must be called as pair, host/port are irrelevant for local protocol
        InvocablesCreator creator = factory.createInvocablesCreator(AasFactory.LOCAL_PROTOCOL, "", 0); 
        ProtocolServerBuilder pBuilder = factory.createProtocolServerBuilder(AasFactory.LOCAL_PROTOCOL, 0);

        Supplier<Object> myGetter = creator.createGetter("prop");
        Consumer<Object> mySetter = creator.createSetter("prop");
        Function<Object[], Object> myFunc = creator.createInvocable("myFunc");
        
        Assert.assertNotNull(pBuilder.createPayloadCodec());
        Assert.assertNotNull(pBuilder.build());
        pBuilder.defineOperation("myFunc", param -> "FUNC");
        pBuilder.defineProperty("prop", () -> propVal, p -> setPropVal(p));
        
        Assert.assertEquals(propVal, myGetter.get());
        mySetter.accept(25);
        Assert.assertEquals(25, propVal);
        Assert.assertEquals(propVal, myGetter.get());
        
        Assert.assertEquals("FUNC", myFunc.apply(new Object[0]));
    }

}
