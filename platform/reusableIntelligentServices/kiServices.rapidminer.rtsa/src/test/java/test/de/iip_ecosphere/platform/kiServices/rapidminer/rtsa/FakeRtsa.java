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

package test.de.iip_ecosphere.platform.kiServices.rapidminer.rtsa;

import java.io.IOException;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import spark.Spark;

import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.put;
import static spark.Spark.delete;

/**
 * A very simple RTSA fake server as we are not allowed to publish RTSA.
 * 
 * @author Holger Eichelberger, SSE
 */
public class FakeRtsa {

    public static final String PARAM_PREFIX = "--";
    public static final String PARAM_ARG_NAME_SEP = ".";
    public static final String PARAM_VALUE_SEP = "=";

    private static final String ENDING = "}]}";
    private static Random random = new Random();

    /**
     * Emulates reading a Spring-like parameter if the configuration is not yet in place.
     * 
     * @param args the arguments
     * @param argName the argument name (without {@link #PARAM_PREFIX} or {@link #PARAM_VALUE_SEP})
     * @param dflt the default value if the argument cannot be found
     * @return the value of argument or {@code deflt}
     */
    public static String getArg(String[] args, String argName, String dflt) {
        String result = dflt;
        String prefix = PARAM_PREFIX + argName + PARAM_VALUE_SEP;
        for (int a = 0; a < args.length; a++) {
            String arg = args[a];
            if (arg.startsWith(prefix)) {
                result = arg.substring(prefix.length());
                break;
            }
        }
        return result;
    }
    
    /**
     * Returns an int command line argument.
     * 
     * @param args the arguments
     * @param argName the argument name (without {@link #PARAM_PREFIX} or {@link #PARAM_VALUE_SEP})
     * @param dflt the default value if the argument cannot be found
     * @return the value of argument or {@code deflt}
     */
    public static int getIntArg(String[] args, String argName, int dflt) {
        int result;
        try {
            result = Integer.parseInt(getArg(args, argName, String.valueOf(dflt)));
        } catch (NumberFormatException e) {
            result = dflt;
        }
        return result;
    }
    
    /**
     * Returns a Boolean command line argument.
     * 
     * @param args the arguments
     * @param argName the argument name (without {@link #PARAM_PREFIX} or {@link #PARAM_VALUE_SEP})
     * @param dflt the default value if the argument cannot be found
     * @return the value of argument or {@code deflt}
     */
    public static boolean getBooleanArg(String[] args, String argName, boolean dflt) {
        return Boolean.valueOf(getArg(args, argName, String.valueOf(dflt)));
    }    
    
    /**
     * Executes the fake server.
     * 
     * @param args ignored
     * @throws IOException shall not occur
     */
    public static void main(String[] args) throws IOException {
        int serverPort = Integer.parseInt(System.getProperty("server.port", "8090")); 
        String path = getArg(args, "iip.rtsa.path", "iip_basic/score_v1");
        boolean verbose = getBooleanArg(args, "verbose", true);
        boolean waitAtStart = getBooleanArg(args, "waitAtStart", true);
        System.out.println("This is FakeRtsa on port: " + serverPort);
//        HttpServer server = HttpServer.create(new InetSocketAddress(serverPort), 0);
//        server.createContext("/services/" + path, (exchange -> {
//            String request = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))
//                .lines().collect(Collectors.joining("\n"));            
//            if (verbose) {
//                System.out.println("FakeRtsa Received Request: " + request);
//            }
//            String respText = createResponse(request);
//            exchange.sendResponseHeaders(200, respText.getBytes().length);
//            OutputStream output = exchange.getResponseBody();
//            output.write(respText.getBytes());
//            output.flush();
//            exchange.close();
//        }));
        
        Spark.port(serverPort);
        post("/services/" + path, (req, res) -> { 
            String request = lines(req.body()).collect(Collectors.joining("\n"));
            if (verbose) {
                System.out.println("FakeRtsa Received Request: " + request);
            }
            String respText = createResponse(request);
            res.body(respText);
            res.status(200);
            return res.body();
        });
        
        get("/services/" + path, (req, res) -> { 
            String request = lines(req.body()).collect(Collectors.joining("\n"));
            if (verbose) {
                System.out.println("FakeRtsa Received Request: " + request);
            }
            String respText = createResponse(request);
            res.body(respText);
            res.status(200);
            return res.body();
        });
        
        put("/services/" + path, (req, res) -> { 
            String request = lines(req.body()).collect(Collectors.joining("\n"));
            if (verbose) {
                System.out.println("FakeRtsa Received Request: " + request);
            }
            String respText = createResponse(request);
            res.body(respText);
            res.status(200);
            return res.body();
        });

        delete("/services/" + path, (req, res) -> { 
            String request = lines(req.body()).collect(Collectors.joining("\n"));
            if (verbose) {
                System.out.println("FakeRtsa Received Request: " + request);
            }
            String respText = createResponse(request);
            res.body(respText);
            res.status(200);
            return res.body();
        });
        
        new Thread(() -> {
            if (waitAtStart) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    // puuh
                }
                System.out.println("Started Application in 2500 ms"); // we need some output for state change
            } else {
                System.out.println("Started Application in 50 ms"); // we need some output for state change
            }
        }).start();

    }
    
    /**
     * Replacement for Java 11 {@code String.lines()}.
     * 
     * @param string the string to stream
     * @return the streamed string in terms of individual lines
     */
    public static Stream<String> lines(String string) {
        return Stream.of(string.replace("\r\n", "\n").split("\n"));
    }
    
    /**
     * Creates a fake response. We do not have a JSON parser available unless we add libraries to the fake RTSA.
     * 
     * @param request the request
     * @return the response
     */
    private static String createResponse(String request) {
        String result = request;
        if (request.endsWith(ENDING)) {
            double conf = random.nextDouble();
            boolean pred = conf > 0.75;
            String predResp = String.format(",\"confidence\":%.3f,\"prediction\":\"%b\"", conf, pred);
            result = request.substring(0, request.length() - ENDING.length()) + predResp + ENDING;
        }
        return result;
    }

}
