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

package de.iip_ecosphere.platform.examples.hm22;

import iip.datatypes.Command;
import iip.datatypes.CommandImpl;

/**
 * Command constants.
 * 
 * @author Holger Eichelberger, SSE
 */
public enum Commands {

    // UI or OPC request
    
    REQUEST_START,

    REQUEST_SWITCH_AI,

    REQUEST_QUIT,

    SEND_FEEDBACK_TO_AI,
    
    // software commands, sent by ActionDecider

    /**
     * Take a picture and do a QR detection.
     */
    SOURCE_DO_QR_SCAN,

    /**
     * Take a picture.
     */
    SOURCE_TAKE_PICTURE,
    
    /**
     * Switch the AI. {@code stringParam} may contain the serviceId of the AI to switch to.
     */
    SWITCH_AI,
    
    /**
     * Queries the car AAS for a certain id given as parameter.
     */
    QUERY_CAR_AAS,
    
    /**
     * Invalid command, do nothing.
     */
    INVALID;
    
    /**
     * Returns whether {@code name} equals the value of {@link #name()}.
     * 
     * @param name the name to check for
     * @return {@code true} if the names are equal, {@code false} else
     */
    public boolean equalsName(String name) {
        return name().equals(name);
    }

    /**
     * Sends a command through the asynchronous ingestor. Logs an error if there is no ingestor.
     * 
     * @param cmd the command
     * @return the created command
     */
    public static Command createCommand(Commands cmd) {
        return createCommand(cmd, null);
    }

    /**
     * Sends a command through the asynchronous ingestor. Logs an error if there is no ingestor.
     * 
     * @param cmd the command
     * @param param value of the string param, may be <b>null</b>
     * @return the created command
     */
    public static Command createCommand(Commands cmd, String param) {
        Command c = new CommandImpl();
        c.setCommand(cmd.name());
        c.setStringParam(param);
        return c;
    }

    /**
     * Turns a command safely into a constant.
     * 
     * @param cmd the command to be turned into a constant, may be <b>null</b>
     * @return the constant, may be {@link #INVALID}
     */
    public static Commands valueOfSafe(Command cmd) {
        return valueOfSafe(null == cmd ? null : cmd.getCommand());
    }

    /**
     * Turns a string safely into a constant.
     * 
     * @param string the string to be turned into a constant, may be <b>null</b>
     * @return the constant, may be {@link #INVALID}
     */
    public static Commands valueOfSafe(String string) {
        try {
            return valueOf(string);
        } catch (NullPointerException | IllegalArgumentException e) {
            return INVALID;
        }
    }

}
