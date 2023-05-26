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

package de.iip_ecosphere.platform.support;

import java.util.function.Supplier;

/**
 * Test utilities.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TimeUtils {

    /**
     * Preventing external creation.
     */
    private TimeUtils() {
    }
    
    /**
     * Just sleeps for the given amount of milliseconds.
     * 
     * @param ms the milliseconds to wait for
     */
    public static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
        }
    }

    /**
     * A generic function that waits either until the {@code timeout} is reached or the {@code endCondition}
     * returns {@code true}.
     * 
     * @param continueCondition function that returns {@code true} when waiting go on and {@code false} 
     *     when waiting shall stop
     * @param timeoutMs timeout from calling in ms, may be negative for endless waiting
     * @param sleepMs time between two trials of {@code timeoutMs} and {@code endCondition}
     * @return {@code true} if the {@code continueCondition} stopped waiting, {@code false} if the 
     *     timeout caused the end of waiting
     */
    public static boolean waitFor(Supplier<Boolean> continueCondition, int timeoutMs, int sleepMs) {
        boolean endConditionMet = false;
        long start = System.currentTimeMillis();
        while (timeoutMs < 0 || System.currentTimeMillis() - start < timeoutMs) {
            endConditionMet = !continueCondition.get();
            if (endConditionMet) {
                break;
            }
            sleep(sleepMs);
        }
        return endConditionMet;
    }

}
