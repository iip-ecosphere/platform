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

package de.iip_ecosphere.platform.examples.hm23;

import de.iip_ecosphere.platform.transport.status.TraceRecordSerializer;
import iip.datatypes.DriveAiResultImpl;
import iip.datatypes.LenzeDriveMeasurementImpl;
import iip.datatypes.MipMqttInputImpl;
import iip.datatypes.MipMqttOutputImpl;

/**
 * Initializes the {@link TraceRecordSerializer} in a way that large data is ignored in serialization.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TraceRecordFilter implements de.iip_ecosphere.platform.transport.status.TraceRecordFilter {

    @Override
    public void initialize() {
        TraceRecordSerializer.ignoreFields(MipMqttInputImpl.class, 
            "mipbitstream_ai_clock", "mipbitstream_ai_data1", "mipbitstream_ai_data2");

        TraceRecordSerializer.ignoreFields(MipMqttOutputImpl.class, 
            "mipraw_signal_clock", "mipraw_signal_data1", "mipraw_signal_data2");

        TraceRecordSerializer.ignoreFields(DriveAiResultImpl.class, 
            "energy", "drive");

        TraceRecordSerializer.ignoreFields(LenzeDriveMeasurementImpl.class, 
            "PROCESS");
    }

}
