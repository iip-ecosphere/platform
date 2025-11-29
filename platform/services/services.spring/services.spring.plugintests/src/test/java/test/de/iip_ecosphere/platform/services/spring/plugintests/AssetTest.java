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

package test.de.iip_ecosphere.platform.services.spring.plugintests;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.BasicSetupSpec;
import de.iip_ecosphere.platform.support.aas.InvocablesCreator;
import de.iip_ecosphere.platform.support.aas.Invokable;
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder;
import test.de.iip_ecosphere.platform.support.aas.TestWithPlugin;

/**
 * Simple program to test asset server.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AssetTest {

    // checkstyle: stop exception type check
    
    /**
     * Tests the asset server.
     * 
     * @param args ignored
     */
    public static void main(String[] args) {
        TestWithPlugin.addPluginLocation("support", "support.commons-apache");
        TestWithPlugin.addPluginLocation("support", "support.log-slf4j-simple");
        TestWithPlugin.addPluginLocation("support", "support.aas.basyx2", "support.log-slf4j-simple");
        TestWithPlugin.addPluginLocation("support", "support.rest-spark");
        TestWithPlugin.addRunAfterLoading(() -> {
            AasFactory.setPluginId(System.getProperty("okto.test.aas.pluginId", "aas.basyx-2.0")); 
        });        
        TestWithPlugin.loadPlugins();
        BasicSetupSpec spec = new BasicSetupSpec(AasFactory.DEFAULT_PROTOCOL, 8080);
        AasFactory f = AasFactory.getInstance();
        System.out.println(f);
        InvocablesCreator ivc = f.createInvocablesCreator(spec);
        String opName = "service_simpleStream-log@app@1_activate"; // test
        Invokable iv = ivc.createInvocable(opName);
        String url = iv.getUrl();
        System.out.println(url);
        ProtocolServerBuilder psb = f.createProtocolServerBuilder(spec);
        //psb.defineOperation(opName, p -> "TEST-FUNC");
        psb.forTomcat();
        Server s = psb.build();
        s.start();
        Runnable stop = () -> s.stop(true);
        Runtime.getRuntime().addShutdownHook(new Thread(stop));
        System.out.println("running... ");
        
        HttpClient client = HttpClient.newHttpClient();
        
        while (true) {
            TimeUtils.sleep(1000);
            System.out.println(url);
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.noBody())
                .build();            
            try {
                client.sendAsync(request, BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenAccept(System.out::println)
                    .join();            
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    // checkstyle: resume exception type check
    
}
