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

package de.iip_ecosphere.platform.connectors.formatter;

import java.io.IOException;

/**
 * A dummy instance to avoid NPEs.
 * 
 * @author Holger Eichelberger, SSE
 */
public class DummyFormatter implements OutputFormatter<String> {

    @Override
    public void add(String name, String data) throws IOException {
    }

    @Override
    public byte[] chunkCompleted() throws IOException {
        return new byte[0];
    }

    @Override
    public OutputConverter<String> getConverter() {
        return new ConverterToString();
    }

}
