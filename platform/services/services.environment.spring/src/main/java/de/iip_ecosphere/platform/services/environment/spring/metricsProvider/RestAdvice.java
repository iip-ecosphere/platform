/**
 * ******************************************************************************
 * Copyright (c) {2021} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.services.environment.spring.metricsProvider;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * This class aims to provide a handler for expected exceptions that could occur
 * when using the {@link MetricsProviderRestService}.<br>
 * This class contains a mechanism implemented by Spring Rest Controller that
 * allows the creation of custom exception handlers to expected exceptions that
 * may occur during requests and, as a result provide a more custom response to
 * the request.<br>
 * There are two types of exceptions handled by this class:
 * <ul>
 * <li>IllegalArgumentExceptions</li>
 * <li>NumberFormatExceptions</li>
 * </ul>
 * 
 * @author Miguel Gomez
 */
@ControllerAdvice
public class RestAdvice {

    /**
     * Captures and handles an IllegalArgumentException.<br>
     * Methods within the {@link MetricsProvider} may throw an
     * IllegalArgumentException if the provided arguments are not valid or suitable
     * for the execution of a certain task. If the
     * {@link MetricsProviderRestService} receives a request that cannot be executed
     * due to invalid arguments, this handler will return an client error status
     * with the message from the produced exception in the response body.
     * 
     * @param iae IllegalArgumentException that has been thrown
     * @return the message nested in the exception as a response body of a HTTP
     *         response code 4XX
     */
    @ResponseBody
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public String illegalArgumentExceptionHandler(IllegalArgumentException iae) {
        return iae.getMessage();
    }

    /**
     * Captures and handles an NumberFormatException.<br>
     * If the request sent to the {@link MetricsProviderRestService} contains a
     * string that cannot be parsed into a valid number that is expected by the
     * method, this exception will be thrown. The message indicating that the
     * request couldn't be parsed will be sent as response body.
     * 
     * @param nfe NumberFormatException that has been thrown
     * @return the message nested in the exception as a response body of a HTTP
     *         response code 4XX
     */
    @ResponseBody
    @ExceptionHandler(NumberFormatException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String numberFormatExceptionHandler(NumberFormatException nfe) {
        return nfe.getMessage();

    }

}
