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

package de.iip_ecosphere.platform.configuration.defaultLib;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimerTask;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.services.environment.DataIngestors;
import iip.datatypes.MipMqttInput;
import iip.datatypes.MipMqttInputImpl;

/**
 * Support code for the <a href="https://mip-technology.de/">MIP technology</a> identification sensor.
 * Sensor commands can be created and sent directly or scheduled using a timer and {@link MipTimerTask}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class MipIdentificationSensor {

    public static final String DATE_PATTERN = "E MMM dd HH:mm:ss yyyy";

    /**
     * Task to send signals to MIP read async to PLC process.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class MipTimerTask extends TimerTask {

        private boolean start;
        private DataIngestors<MipMqttInput> ingestors;
        private String sensorId;

        /**
         * Creates a timer task.
         * 
         * @param start start/switch on the sensor if {@code true}, stop/switch off the sensor if {@code false}
         * @param sensorId the id of the sensor to send the data to
         * @param ingestors the ingestors to use for sending
         */
        public MipTimerTask(boolean start, String sensorId, DataIngestors<MipMqttInput> ingestors) {
            this.start = start;
            this.sensorId = sensorId;
            this.ingestors = ingestors;
        }
        
        @Override
        public void run() {
            sendStartStopCommand(start, sensorId, ingestors);
        }
        
    }
    
    /**
     * Formats a date in MIP style.
     * 
     * @param date the date to be formatted
     * @return the formatted date
     */
    public static String formatDate(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_PATTERN, Locale.GERMAN);
        return simpleDateFormat.format(date).replace(".", "");
    }
    
    /**
     * Creates a start/stop command for a MIP reader.
     * 
     * @param start start/switch on the sensor if {@code true}, stop/switch off the sensor if {@code false}
     * @param sensorId the id of the sensor to send the data to
     * @return the created command instance
     */
    public static MipMqttInput createStartStopCommand(boolean start, String sensorId) {
        MipMqttInput cmd = new MipMqttInputImpl();
        cmd.setMipcontext("reader_command");
        cmd.setMipdate(formatDate(new Date())); // seems to be ignored
        cmd.setMipto(sensorId); 
        cmd.setMipcommand(start ? "reader_read s 1\n" : "reader_read s 0\n");
        return cmd;
    }
    
    /**
     * Sends a start/stop command to an MIP reader. [convenience]
     * 
     * @param start start/switch on the sensor if {@code true}, stop/switch off the sensor if {@code false}
     * @param sensorId the id of the sensor to send the data to
     * @param ingestors ingestors to send the command to
     */
    public static void sendStartStopCommand(boolean start, String sensorId, DataIngestors<MipMqttInput> ingestors) {
        MipMqttInput cmd = createStartStopCommand(start, sensorId);
        LoggerFactory.getLogger(MipIdentificationSensor.class).info("Sending MIP sensor command (start: {}, id:{}) "
            + "via ingestors", start, sensorId);
        ingestors.ingest(cmd);
    }

}
