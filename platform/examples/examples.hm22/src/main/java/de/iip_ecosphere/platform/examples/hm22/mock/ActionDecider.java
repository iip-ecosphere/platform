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

package de.iip_ecosphere.platform.examples.hm22.mock;

import java.io.File;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import de.iip_ecosphere.platform.services.environment.DataIngestor;
import de.iip_ecosphere.platform.services.environment.IipStringStyle;
import iip.datatypes.AiResult;
import iip.datatypes.Command;
import iip.datatypes.MdzhOutput;
import iip.datatypes.PlcOutput;

/**
 * Mocks the action decider.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ActionDecider extends de.iip_ecosphere.platform.examples.hm22.ActionDecider {

    private static final boolean LOG_ALL = Boolean.valueOf(System.getProperty("iip.app.hm22.mock.logAll", "false"));
    private static final boolean CALL_ROBOT 
        = Boolean.valueOf(System.getProperty("iip.app.hm22.mock.callRobot", "true"));
    
    /**
     * Fallback constructor, also used for testing main program.
     */
    public ActionDecider() {
        super();
    }

    /**
     * Creates a service instance from a service id and a YAML artifact.
     * 
     * @param serviceId the service id
     * @param ymlFile the YML file containing the YAML artifact with the service descriptor
     */
    public ActionDecider(String serviceId, InputStream ymlFile) {
        super(serviceId, ymlFile);
    }

    @Override
    protected void sendRobotCommand(int op, int next) {
        System.out.println("Call robot on PLC " + op + " next " + next);
        super.sendRobotCommand(op, next);
    }
    
    @Override
    protected void callRobot(int pos) {
        // don't call the robot in mock
        System.out.println("Call robot " + pos + " " + CALL_ROBOT);
        if (CALL_ROBOT) {
            super.callRobot(pos);
        }
    }

    @Override
    protected boolean readyForRequest() {
        // don't wait as PLC is not there in mocking
        return true;
    }
   
    @Override
    public void processAiResult(AiResult data) {
        System.out.println("DECIDER RCV: " + getCurrentState() + " " + getAiResultsCount() + " " + data);
        super.processAiResult(data);
        System.out.println("DECIDER: " + getCurrentState() + " " + getAiResultsCount());
    }
    
    @Override
    public void processPlcOutput(PlcOutput data) {
        if (LOG_ALL) {
            System.out.println("DECIDER RCV: " + data);
        }
        super.processPlcOutput(data);
    }

    @Override
    public void processMdzhOutput(MdzhOutput data) {
        if (LOG_ALL) {
            System.out.println("DECIDER RCV: " + data);
        }
        super.processMdzhOutput(data);
    }

    @Override
    public void processCommand(Command data) {
        System.out.println("DECIDER RCV: " + data);
        super.processCommand(data);
    }
    
    @Override
    public void attachCommandIngestor(DataIngestor<Command> ingestor) {
        super.attachCommandIngestor(ingestor);
    }

    @Override
    protected File getStorageBaseFolder() {
        return FileUtils.getTempDirectory();
    }

    @Override
    protected void storeToJson(File file, Object data) {
        System.out.println("Storing (" + file + ") " 
            + ReflectionToStringBuilder.toString(data, IipStringStyle.SHORT_STRING_STYLE)); 
        super.storeToJson(file, data);
        file.deleteOnExit();
    }

}
