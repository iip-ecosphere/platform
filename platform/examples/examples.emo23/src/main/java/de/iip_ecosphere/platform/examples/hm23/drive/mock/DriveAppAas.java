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

package de.iip_ecosphere.platform.examples.hm23.drive.mock;

import java.io.InputStream;
import java.util.function.Predicate;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import de.iip_ecosphere.platform.services.environment.IipStringStyle;
import de.iip_ecosphere.platform.services.environment.YamlService;
import de.iip_ecosphere.platform.services.environment.testing.DataRecorder;
import de.iip_ecosphere.platform.support.iip_aas.ApplicationSetup;
import de.iip_ecosphere.platform.transport.status.TraceRecord;
import iip.datatypes.BeckhoffOutput;
import iip.datatypes.DriveAiResult;
import iip.datatypes.DriveBeckhoffOutput;
import iip.datatypes.MipAiPythonOutput;
import iip.datatypes.PlcEnergyMeasurementJson;
import iip.datatypes.PlcOutput;

/**
 * Mocking class.
 * 
 * @author Holger Eichelberger, SSE
 */
public class DriveAppAas extends de.iip_ecosphere.platform.examples.hm23.drive.DriveAppAas {
    
    private static final ToStringStyle TRACE_OUT_STYLE = IipStringStyle.SHORT_STRING_STYLE; // MULTI_LINE_STYLE
    
    /**
     * Creates a service instance from a service id and a YAML artifact.
     * 
     * @param serviceId the service id
     * @param ymlFile the YML file containing the YAML artifact with the service descriptor
     */
    public DriveAppAas(String serviceId, InputStream ymlFile) {
        super(serviceId, ymlFile);
    }
    
    /**
     * Creates a service instance. [for testing]
     *
     * @param app static information about the application
     * @param yaml the service description 
     */
    public DriveAppAas(ApplicationSetup app, YamlService yaml) {
        super(app, yaml);
    }

    @Override
    protected Predicate<TraceRecord> getHandleNewFilter() {
        final Predicate<TraceRecord> parentFilter = super.getHandleNewFilter();
        return data -> {
            Object payload = data.getPayload();
            boolean beckhoffOut = payload instanceof DriveBeckhoffOutput;
            boolean beckhoffCamOut = payload instanceof BeckhoffOutput;
            boolean plcNextOut = payload instanceof PlcOutput;
            boolean plcEnergy = payload instanceof PlcEnergyMeasurementJson;
            if (!beckhoffOut && !plcNextOut && !beckhoffCamOut && !plcEnergy) {
                System.out.println("APP Trace RCV: " + ReflectionToStringBuilder.toString(data, TRACE_OUT_STYLE));
            }
            return parentFilter.test(data);
        };
    }
    
    @Override
    public void processDriveAiResult(DriveAiResult data) {
        super.processDriveAiResult(data);
        recordData("data", data);
    }
    
    @Override
    public void processMipAiPythonOutput(MipAiPythonOutput data) {
        super.processMipAiPythonOutput(data);
        recordData("mip", data);
    }

    @Override
    protected DataRecorder createDataRecorder() {
        return createDataRecorderOrig();
    }

}
