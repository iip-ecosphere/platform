/**
 * ******************************************************************************
 * Copyright (c) {2025} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.aas.basyx2.server.apps.common;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Resolves BaSyx internal exceptions. For example, in operation delegation, when the delegate is not yet/anymore 
 * available, the {@link WebClientRequestException} is emitted by Spring as it is not handled/filtered out before. 
 * This confuses the log/user.
 * 
 * @author Holger Eichelberger, SSE
 */
@Component
public class BaSyxExceptionResolver implements HandlerExceptionResolver {

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler,
        Exception ex) {

        if (ex instanceof WebClientRequestException) { // may need more specific filtering for causing ConnectException
            return resolve(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE, 
                "External service temporarily unavailable: " + ex.getMessage());
        }
        return null; // don't touch the other exceptions
    }
    
    /**
     * Resolves an exception.
     * 
     * @param response the response to modify
     * @param status the response status
     * @param msg the message
     * @return the model and view indicating the the exception is resolved
     */
    private ModelAndView resolve(HttpServletResponse response, int status, String msg) {
        try {
            response.setStatus(status);
            response.getWriter().write(msg);
            response.getWriter().flush();
        } catch (IOException ioException) {
            
        }
        return new ModelAndView();
    }

}
