/**
 * ******************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.aas;

import java.nio.charset.Charset;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import de.iip_ecosphere.platform.support.Builder;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.TimeUtils;

/**
 * The implementing counterpart of {@link InvocablesCreator} in terms of a builder.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface ProtocolServerBuilder extends Builder<Server> {

    /**
     * Encodes/decodes payload in the style of this protocol server. We introduced
     * this codec because TCP VAB closes its connection after each data frame, which
     * harms streaming performance. This form of codec is intended for inter-process
     * communication using a similar protocol as VAB.
     * 
     * For encoding call {@link #encode(String, byte[])}. For 
     * decoding, fetch first an array of size {@link #getDataBytesLength()}. Use 
     * {@link #decodeDataLength(byte[])} to read an array of the actual length of the 
     * following payload. Use then {@link #decode(byte[], PayloadConsumer)} to decode the payload.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface PayloadCodec {
        
        /**
         * The charset being used to encode characters. If called before {@link #setCharset(Charset)},
         * this method shall return the default encoding charset, e.g., UTF-8.
         * 
         * @return the charset
         */
        public Charset getCharset();

        /**
         * Changes charset being used to encode characters.
         * 
         * @param charset the new charset
         */
        public void setCharset(Charset charset);
        
        /**
         * The intended schema for this codec.
         * 
         * @return the intended schema
         */
        public Schema intendedSchema();

        /**
         * Receives the decoded information.
         * 
         * @param info optional descriptive information, may be <b>null</b> or empty
         * @param payload the payload to be encoded
         * @return the encoded data
         */
        public byte[] encode(String info, byte[] payload);
        
        /**
         * Returns the (usually fixed) number of bytes to read the size of the encoded
         * data.
         * 
         * @return the number of bytes (may be 0 for none)
         */
        public int getDataBytesLength();
        
        /**
         * Decodes an array of size {@link #getDataBytesLength()} to obtain the length
         * of the following data array.
         * 
         * @param data the data to decode
         * @return the decoded data length (may be 0 for none)
         */
        public int decodeDataLength(byte[] data);
        
        /**
         * Decodes the part after tha data length.
         * 
         * @param data the data array in the size as given by {@link #decodeDataLength(byte[])}
         * @param consumer a consumer object
         */
        public void decode(byte[] data, PayloadConsumer consumer);
        
    }
    
    /**
     * Consumes decoded payload.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface PayloadConsumer {

        /**
         * Receives the decoded information.
         * 
         * @param info the desciptive information, may be <b>null</b>
         * @param payload the decoded payload
         */
        public void decoded(String info, byte[] payload);
        
    }
    
    /**
     * Defines a service function.
     * 
     * @param name the name of the operation
     * @param function the implementing function
     * @return <b>this</b>
     * @throws IllegalArgumentException if the operation is already registered
     */
    public ProtocolServerBuilder defineOperation(String name, Function<Object[], Object> function);

    /**
     * Defines a property with getter/setter implementation. Theoretically, either getter/setter
     * may be <b>null</b> for read-only/write-only properties, but this must be, however, reflected in the AAS so that 
     * no wrong can access happens.
     * 
     * @param name the name of the property
     * @param get the supplier providing read access to the property value (may be <b>null</b>)
     * @param set the consumer providing write access to the property value (may be <b>null</b>)
     * @return <b>this</b>
     * @throws IllegalArgumentException if the property is already registered
     */
    public ProtocolServerBuilder defineProperty(String name, Supplier<Object> get, Consumer<Object> set);
   
    /**
     * Creates a code that encodes byte array payload in the style of this protocol server.
     * 
     * @return the payload codec (may be <b>null</b> if no such codec exists)
     */
    public PayloadCodec createPayloadCodec();
    
    /**
     * Checks whether the port assigned to this builder is available. May be used without 
     * {@link #build()} before.
     * 
     * @param host the host to look for
     * @return {@code true} if available, {@code false} else
     */
    public boolean isAvailable(String host);

    /**
     * Checks whether the port assigned to this builder is available. May be used without 
     * {@link #build()} before.
     *
     * @param host the host to look for
     * @param maxWaitingTime the maximum waiting time in ms, shall be positive, otherwise waits endless
     * @param waitingPeriod the time in ms to wait between two trials of calling {@link #isAvailable(String)}, if 
     *     negative set to 500 ms 
     * @return {@code true} if available, {@code false} else
     */
    public default boolean isAvailable(String host, int maxWaitingTime, int waitingPeriod) {
        boolean result = false;
        int timeTaken = 0;
        if (waitingPeriod <= 0) {
            waitingPeriod = 500;
        }
        while (timeTaken < maxWaitingTime) {
            if (isAvailable(host)) {
                result = true;
                break;
            }
            TimeUtils.sleep(waitingPeriod);
            timeTaken += waitingPeriod;
        }
        return result;
    }

    /**
     * Checks whether the port assigned to this builder is available. May be used without 
     * {@link #build()} before. Uses 500 ms as default time between to trials of calling {@link #isAvailable(String)}
     *
     * @param host the host to look for
     * @param maxWaitingTime the maximum waiting time in ms, shall be positive, otherwise waits endless
     * @return {@code true} if available, {@code false} else
     */
    public default boolean isAvailable(String host, int maxWaitingTime) {
        return isAvailable(host, maxWaitingTime, 500);
    }

}
