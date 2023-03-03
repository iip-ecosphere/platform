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

import de.iip_ecosphere.platform.connectors.AdapterSelector;
import de.iip_ecosphere.platform.transport.serialization.InputTypeTranslator;

/**
 * Extended {@link InputTypeTranslator} to receive the reception channel including a delegating default 
 * implementation of the serializer method passing in the {@link AdapterSelector#DEFAULT_CHANNEL default channel name}.
 * 
 * @param <T> the target type
 * @param <S> the source type 
 * @author Holger Eichelberger, SSE
 */
public interface ChanneledInputTypeTranslator<T, S> extends InputTypeTranslator<T, S> {

    /**
     * Deserializes a target value into a source value ("input <b>from</b> external").
     * 
     * @param channel the channel the data came from
     * @param data the data to be translated back
     * @return the serialized object
     * @throws IOException in case that serialization fails
     */
    public S from(String channel, T data) throws IOException;
    
    @Override
    public default S from(T data) throws IOException {
        return from(AdapterSelector.DEFAULT_CHANNEL, data);
    }
    
}
