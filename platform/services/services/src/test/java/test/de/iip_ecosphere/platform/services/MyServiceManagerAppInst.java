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

package test.de.iip_ecosphere.platform.services;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

import de.iip_ecosphere.platform.services.AbstractServiceManager;
import de.iip_ecosphere.platform.services.TypedDataConnectorDescriptor;
import de.iip_ecosphere.platform.services.environment.switching.ServiceBase;
import de.iip_ecosphere.platform.support.iip_aas.Version;

/**
 * A test service manager for multiple instances in an application. Does not support advanced operations, just
 * for basic functional tests.
 * 
 * @author Holger Eichelberger, SSE
 */
class MyServiceManagerAppInst extends AbstractServiceManager<MyArtifactDescriptor, MyServiceDescriptor> {

    /**
     * Sets up the service manager.
     * 
     * @throws ExecutionException if adding services/artifacts fails
     */
    MyServiceManagerAppInst() throws ExecutionException {
        List<MyServiceDescriptor> services = new ArrayList<>();
        services.add(new MyServiceDescriptor("s1", "a1", "s1", "", new Version("0.1"))); // legacy start
        services.add(new MyServiceDescriptor(
            ServiceBase.composeId("s1", "a1", "1"), "a1", "s1", "", new Version("0.1")));
        services.add(new MyServiceDescriptor(
            ServiceBase.composeId("s1", "a1", "2"), "a1", "s1", "", new Version("0.1")));
        services.add(new MyServiceDescriptor(
            ServiceBase.composeId("s1", "a1", "3"), "a1", "s1", "", new Version("0.1")));
        MyArtifactDescriptor art = new MyArtifactDescriptor("art", "art", null, services);
        addArtifact("art", art);
    }

    @Override
    public void cloneArtifact(String artifactId, URI location) throws ExecutionException {
    }

    @Override
    public String addArtifact(URI location) throws ExecutionException {
        
        return null;
    }

    @Override
    public void startService(String... serviceId) throws ExecutionException {
    }

    @Override
    public void startService(Map<String, String> options, String... serviceId) throws ExecutionException {
    }

    @Override
    public void stopService(String... serviceId) throws ExecutionException {
    }

    @Override
    public void updateService(String serviceId, URI location) throws ExecutionException {
    }

    @Override
    protected Predicate<TypedDataConnectorDescriptor> getAvailablePredicate() {
        return c-> true;
    }

    @Override
    protected MyServiceDescriptor instantiateFromTemplate(MyServiceDescriptor template, String serviceId) {
        MyServiceDescriptor result = new MyServiceDescriptor(serviceId, template.getApplicationId(), 
            template.getName(), template.getDescription(), template.getVersion());
        // ignore further, ensemble leader for now in test
        return result;
    }
    
}