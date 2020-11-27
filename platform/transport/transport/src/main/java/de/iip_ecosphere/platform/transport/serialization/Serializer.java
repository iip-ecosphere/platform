/********************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/
package de.iip_ecosphere.platform.transport.serialization;

import java.io.IOException;

/**
 * A data serializer (so far for homogeneous streams, may require unique ids). Open: Do we have to cope with 
 * <b>null</b> values here?
 * 
 * @param <T> the type to be serialized
 * @author Holger Eichelberger, SSE
 */
public interface Serializer<T> extends TypeTranslator<T, byte[]> {

    /**
     * Creates a new value instance and copies the values from {@code origin} to the new instance.
     * Use the {@link SerializerRegistry} to implement cloning of nested object values.
     *  
     * @param origin the object to clone
     * @return the cloned object
     * @throws IOException in case that cloning/serialization operations fail
     */
    public T clone(T origin) throws IOException;
    
    /**
     * The type to be handled by this serializer.
     * 
     * @return the type
     */
    public Class<T> getType();

}
