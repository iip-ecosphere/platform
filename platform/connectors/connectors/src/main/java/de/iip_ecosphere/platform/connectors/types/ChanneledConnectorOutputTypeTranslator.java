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
import de.iip_ecosphere.platform.transport.serialization.OutputTypeTranslator;
import de.iip_ecosphere.platform.transport.serialization.TypeTranslator;

/**
 * Refines the {@link TypeTranslator} for the use with channeled connectors and actual channel names including a 
 * delegating default implementation of the type translator method passing in the 
 * {@link AdapterSelector#DEFAULT_CHANNEL default channel name}.
 * 
 * @author Holger Eichelberger, SSE
 *
 * @param <S> the source type (see {@link TypeTranslator})
 * @param <T> the target type (see {@link TypeTranslator})
 */
public interface ChanneledConnectorOutputTypeTranslator<S, T> extends OutputTypeTranslator<S, T> {

    /**
     * Translates a source value into a target value ("output <b>to</b> external").
     * 
     * @param channel the channel the value was received on
     * @param source the source value to be translated
     * @return the target value
     * @throws IOException in case that translation fails
     */
    public T to(String channel, S source) throws IOException;

    @Override
    public default T to(S source) throws IOException {
        return to(AdapterSelector.DEFAULT_CHANNEL, source);
    }
    
}
