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

package de.iip_ecosphere.platform.support.aas.basyx.security;

import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.web.filter.OncePerRequestFilter;

import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor;

/**
 * Request filter to fail if there is no authorization.
 * 
 * @author Holger Eichelberger, SSE
 */
public class FailNoAuthorizationFilter extends OncePerRequestFilter {

    private SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder
            .getContextHolderStrategy();
    private Pattern uriException;
    private boolean allowAnonymous;

    /**
     * Creates a filter instance with no exception.
     */
    public FailNoAuthorizationFilter() {
        this(null, false);
    }

    /**
     * Creates a filter instance.
     * 
     * @param exceptionUriRegEx regular expression determining URIs that may pass without authentication, may be 
     *     <b>null</b> for no exception
     * @throws PatternSyntaxException if {@code exceptionUriRegEx} cannot be compiled to a pattern
     */
    public FailNoAuthorizationFilter(String exceptionUriRegEx, boolean allowAnonymous) {
        uriException = null == exceptionUriRegEx ? null : Pattern.compile(exceptionUriRegEx);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        Authentication existingAuth = this.securityContextHolderStrategy.getContext().getAuthentication();
        if (null == existingAuth) {
            if (null != uriException && uriException.matcher(request.getRequestURI()).matches()) {
                filterChain.doFilter(request, response);
            } else if (allowAnonymous) {
                AuthenticationDescriptor.Role role = AuthenticationDescriptor.DefaultRole.NONE;
                AnonymousAuthenticationToken token = new AnonymousAuthenticationToken(role.name(), null, null);
                token.setDetails(role);
                this.securityContextHolderStrategy.getContext().setAuthentication(token);
                filterChain.doFilter(request, response);
            } else {
                response.setStatus(HttpStatus.SC_UNAUTHORIZED);
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }

}