/**
 * ******************************************************************************
 * Copyright (c) {2026} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.net;

import java.io.IOException;
import java.net.http.HttpClient;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import javax.net.ssl.SSLContext;

import de.iip_ecosphere.platform.support.logging.LoggerFactory;

/**
 * Places the HTTP client into the platforms core classloader.
 * 
 * @author Holger Eichelberger, SSE
 */
public class HttpClientHelper {

    /**
     * Returns the class loader adquate for {@link HttpClient}.
     * 
     * @return the classloader
     */
    public static ClassLoader getClientLoader() {
        return ClassLoader.getSystemClassLoader();
    }

    /**
     * Wraps {@code func} into the right/expected context.
     * 
     * @param <R> the result type
     * @param func the function to execute
     * @return the result of {@code func}
     */
    public static <R> R inContext(Supplier<R> func) {
        Thread curThread = Thread.currentThread(); // "Keep" HttpClientBuilder and TLS setup in system classloader
        ClassLoader tccl = curThread.getContextClassLoader();
        curThread.setContextClassLoader(getClientLoader());
        R result = func.get();
        curThread.setContextClassLoader(tccl);
        return result;
    }

    /**
     * Creates a HTTP client builder from a keystore descriptor.
     * 
     * @param desc the descriptor
     * @return the client builder
     */
    public static HttpClient.Builder createHttpClient(KeyStoreDescriptor desc) {
        return inContext(() -> createHttpClientImpl(desc));
    }

    /**
     * Creates a HTTP client builder from a keystore descriptor.
     * 
     * @param desc the descriptor
     * @return the client builder
     */
    private static HttpClient.Builder createHttpClientImpl(KeyStoreDescriptor desc) {
        SSLContext context = null;
        Boolean oldHNV = null;
        if (null != desc) {
            oldHNV = setJdkHostnameVerification(desc);
            try {
                context = SslUtils.createTlsContext(desc.getPath(), desc.getPassword(), desc.getAlias());
            } catch (IOException e) {
                LoggerFactory.getLogger(HttpClientHelper.class).error(
                    "Http client creation failed: {} Falling back to non-TLS client.", e.getMessage());
            }
        }
        HttpClient.Builder result = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2);
        if (null != context) {
            result.sslContext(context);
            
            final ClassLoader loader = getClientLoader();
            Executor executor = Executors.newFixedThreadPool(4, r -> {
                Thread t = new Thread(r);
                t.setContextClassLoader(loader);
                return t;
            });
            result.executor(executor);
        } 
        /* if (AUTHENTICATED) {
            // authenticator sets header empty, requires challenge-response-auth 
            result.authenticator(new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                }
            });
        }*/
        if (null != oldHNV) {
            setJdkHostnameVerification(oldHNV);
        }
        
        return result;
    }

    /**
     * Sets JDK HTTP/SSL hostname verification.
     * 
     * @param desc the keystore descriptor indicating whether verification is enabled or disabled
     * @return the value of the flag before, by default {@code false}
     */
    public static boolean setJdkHostnameVerification(KeyStoreDescriptor desc) {
        return setJdkHostnameVerification(!desc.applyHostnameVerification());
    }

    /**
     * Sets JDK HTTP/SSL hostname verification.
     * 
     * @param disable {@code true} the verification, {@code false} enables it
     * @return the value of the flag before, by default {@code false}
     */
    public static boolean setJdkHostnameVerification(boolean disable) {
        final String prop = "jdk.internal.httpclient.disableHostnameVerification";
        boolean old = Boolean.valueOf(System.getProperty(prop, "false"));
        System.setProperty(prop, String.valueOf(disable));
        return old;
    }

}
