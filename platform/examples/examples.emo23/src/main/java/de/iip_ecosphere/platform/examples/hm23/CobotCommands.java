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

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.transport.AppIntercom;
import iip.datatypes.Command;
import iip.datatypes.PlcInput;
import iip.datatypes.PlcInputImpl;
import iip.datatypes.PlcOutput;
import iip.datatypes.PlcOutputImpl;

/**
 * Re-usable cobot parts.
 * 
 * @author Holger Eichelberger, SSE
 */
public class CobotCommands {

    // between starting to wait / we accept completed
    public static final long TIMEDIFF_ROBOT_BUSY_SAFE = TimeUnit.SECONDS.toMillis(40); 
    public static final int TIMEDIFF_ROBOT_BUSY_SAFE_INT = (int) TIMEDIFF_ROBOT_BUSY_SAFE; 
    public static final int PLC_ROBOT_NOP = 0;
    public static final int PLC_ROBOT_NEXT_QUIT = -2; // translate, do not send to PLC
    public static final int PLC_ROBOT_NEXT_NONE = -1; // do not send to PLC
    public static final int PLC_ROBOT_NEXT_FOLD = 10; // index in PLC -> cobot 10
    public static final int PLC_ROBOT_NEXT_UNFOLD = 11; // index in PLC -> cobot 11
    //public static final int PLC_ROBOT_NEXT_NO_CAR = 12;
    public static final int PLC_ROBOT_NEXT_BASE = 9; // index in PLC -> cobot 21
    public static final int PLC_ROBOT_DEFAULT_START = 0; // index in PLC -> cobot 1
    public static final int PLC_ROBOT_NEXT_ANY = 100; // pseudo-index in PLC program

    public static final int PLC_ROBOT_GRIP_CAR = 12; // index in PLC -> cobot 30 => 13
    public static final int PLC_ROBOT_NEXT_GOTO_ID_SCAN_POS = 13; // index in PLC -> cobot 31 => 14
    public static final int PLC_ROBOT_NEXT_DO_ID_SCAN = 14; // index in PLC -> cobot 32 => 15
    public static final int PLC_ROBOT_NEXT_SAFE_FOR_RESCAN = 15; // index in PLC -> cobot 33, may be go back to ID_SCAN
    public static final int PLC_ROBOT_NEXT_FWD_DRIVE = 16; // index in PLC -> cobot 34
    public static final int PLC_ROBOT_NEXT_PLACE_CAR = 17; // index in PLC -> cobot 35
    public static final int PLC_ROBOT_NEXT_BWD_DRIVE = 18; // index in PLC -> cobot 36, may be base

    private PlcOutput lastPlcOutput = new PlcOutputImpl();
    private Runnable beforeNext;
    private Consumer<PlcInput> ingestPlcInput;
    private boolean mock = false;
    
    /**
     * Creates a cobot commands sending instance.
     * 
     * @param beforeNext operation to be executed before advancing to next, may be <b>null</b>
     * @param ingestPlcInput operation to ingest the PLC input
     */
    public CobotCommands(Runnable beforeNext, Consumer<PlcInput> ingestPlcInput) {
        this.beforeNext = beforeNext;
        this.ingestPlcInput = ingestPlcInput;
    }
    
    /**
     * Changes the mocking state.
     * 
     * @param mock enable/disable mocking
     */
    public void setMock(boolean mock) {
        this.mock = mock;
    }
    
    /**
     * Sends a robot command.
     * 
     * @param cobotId the cobot id
     * @param next the next command in pre-defined movement chain, ignored if negative
     */
    public void sendCommand(int cobotId, int next) {
        if (mock) {
            System.out.println("Call robot " + cobotId + " on PLC with next " + next);            
        }
        PlcInput pi = new PlcInputImpl();
        if (next == CobotCommands.PLC_ROBOT_NEXT_QUIT) {
            pi.setPC_Quit(true);
        } else {
            if (null != beforeNext) {
                beforeNext.run();
            }
            pi.setPC_Command01((short) next);
            if (next < PLC_ROBOT_NEXT_ANY && readyForRequest()) {
                // only needed for real start, not for change running sequence
                pi.setPC_StartOperation(true); 
            }
        }
        ingestPlcInput.accept(pi);
    }
    
    /**
     * Returns whether the PLC is ready to request.
     * 
     * @return {@code true} if the PLC is ready, {@code false} else
     */
    public boolean readyForRequest() {
        return getLastValue(l -> l.getPC_ReadyForRequest(), false, true); // mocking: don't block PLC
    }

    /**
     * Returns whether the PLC is ready to request.
     * 
     * @return {@code true} if the PLC is ready, {@code false} else
     */
    public boolean isInSafePosition() {
        return getLastValue(l -> l.getUR_InSafePosition(), false, true); // mocking: don't block PLC
    }

    /**
     * Returns whether the cobot safety is ok.
     * 
     * @return {@code true} if safety is ok, {@code false} else
     */
    public boolean isSafetyOk() {
        return getLastValue(l -> l.getSafetyOk(), false, true); // mocking: don't block PLC
    }

    /**
     * Returns a value from {@link #lastPlcOutput}.
     * 
     * @param <R> the type of the value (return type)
     * @param getter the getter function obtaining the value from {@link #lastPlcOutput}
     * @param dfltValue value to return if there is no {@link #lastPlcOutput} and {@link #mock mocking} is disabled
     * @param mockedValue value to return if {@link #mock mocking} is enabled
     * 
     * @return {@code true} if the PLC is ready, {@code false} else
     */
    private <R> R getLastValue(Function<PlcOutput, R> getter, R dfltValue, R mockedValue) {
        R result;
        if (mock) {
            result = mockedValue; // don't wait for PLC
        } else {
            if (null != lastPlcOutput) {
                result = getter.apply(lastPlcOutput);
            } else {
                result = dfltValue;
            }
        }
        return result;
    }

    /**
     * Records the last PLC output.
     * 
     * @param data the last output
     */
    public void setLastPlcOutput(PlcOutput data) {
        lastPlcOutput = data;        
    }
    
    /**
     * Sends a command between apps on a separate transport channel. This channel is required
     * as the apps cannot communicate directly due to app/instance-specific channel names.
     * 
     * @param intercom the intercom instance
     * @param command the command
     * @return {@code true} command was sent, {@code false} sending failed
     */
    public static boolean sendIntercom(AppIntercom<Command> intercom, Command command) {
        boolean success = false;
        try {
            intercom.asyncSend(command);
            success = true;
        } catch (IOException e) {
            LoggerFactory.getLogger(CobotCommands.class).error("Cannot send intercom command: " + e.getMessage());
        }
        return success;
    }

}
