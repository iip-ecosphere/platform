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

package de.iip_ecosphere.platform.connectors.types;

import de.iip_ecosphere.platform.connectors.model.ModelAccessProvider;
import de.iip_ecosphere.platform.transport.serialization.InputTypeTranslator;
import de.iip_ecosphere.platform.transport.serialization.TypeTranslator;

/**
 * Refines the {@link TypeTranslator} for the use with machine connectors.
 * 
 * @author Holger Eichelberger, SSE
 *
 * @param <T> the target type (see {@link TypeTranslator})
 * @param <S> the source type (see {@link TypeTranslator})
 * @param <D> the protocol-specific data type for values (see {@link ModelAccessProvider}) 
 */
public interface ConnectorInputTypeTranslator<T, S, D> extends InputTypeTranslator<T, S>, ModelAccessProvider<D> {
 
    /**
     * Returns the source type.
     * 
     * @return the source type
     */
    public Class<? extends S> getSourceType();

    /**
     * Returns the target type.
     * 
     * @return the target type
     */
    public Class<? extends T> getTargetType();

}
