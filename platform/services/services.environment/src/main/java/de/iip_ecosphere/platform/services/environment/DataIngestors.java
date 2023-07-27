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

package de.iip_ecosphere.platform.services.environment;

import java.util.ArrayList;
import java.util.List;

/**
 * Supports manual ingestor implementation if generated service base classes cannot be used.
 * 
 * @author Holger Eichelberger, SSE
 */
public class DataIngestors<T> {
    
    private List<DataIngestor<T>> ingestors = new ArrayList<>();

    /**
     * Ingests a drive command.
     * 
     * @param data the data instance to be ingested
     */
    public void ingest(T data) {
        if (null != data) {
            for (DataIngestor<T> commandIngestor : ingestors) {
                commandIngestor.ingest(data);
            }
        }
    }

    /**
     * Attaches an ingestor.
     * 
     * @param ingestor the ingestor to be attached
     */
    public void attachIngestor(DataIngestor<T> ingestor) {
        if (null != ingestor) {
            ingestors.add(ingestor);
        }
    }


}
