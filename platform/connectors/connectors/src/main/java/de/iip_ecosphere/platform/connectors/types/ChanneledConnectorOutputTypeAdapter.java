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

package de.iip_ecosphere.platform.connectors.types;

import java.io.IOException;

import de.iip_ecosphere.platform.transport.serialization.OutputTypeTranslator;
import de.iip_ecosphere.platform.transport.serialization.Serializer;

/**
 * Specialized {@link ConnectorOutputTypeAdapter} to handle the reception channel.
 * 
 * @param <T> the target type (see {@link OutputTypeTranslator})
 * @author Holger Eichelberger, SSE
 */
public class ChanneledConnectorOutputTypeAdapter<T> extends ConnectorOutputTypeAdapter<T> 
    implements ChanneledConnectorOutputTypeTranslator<byte[], T> {

    /**
     * A serializer that receives the reception channel name on serialization.
     * 
     * @param <T> the type to be serialized
     * @author Holger Eichelberger, SSE
     */
    public interface ChanneledSerializer<T> extends Serializer<T>, ChanneledInputTypeTranslator<byte[], T> {
    }
    
    private ChanneledSerializer<T> serializer;

    /**
     * Creates an instance.
     * 
     * @param serializer the serializer to adapt
     */
    public ChanneledConnectorOutputTypeAdapter(ChanneledSerializer<T> serializer) {
        super(serializer);
        this.serializer = serializer;
    }

    @Override
    public T to(String channel, byte[] source) throws IOException {
        return serializer.from(channel, source);
    }
}
