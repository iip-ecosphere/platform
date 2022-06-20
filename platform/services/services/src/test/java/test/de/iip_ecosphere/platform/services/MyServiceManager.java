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

package test.de.iip_ecosphere.platform.services;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

import de.iip_ecosphere.platform.services.AbstractServiceManager;
import de.iip_ecosphere.platform.services.TypedDataConnectorDescriptor;
import de.iip_ecosphere.platform.services.environment.ServiceState;
import de.iip_ecosphere.platform.support.iip_aas.Version;

/**
 * A test service manager.
 * 
 * @author Holger Eichelberger, SSE
 */
class MyServiceManager extends AbstractServiceManager<MyArtifactDescriptor, MyServiceDescriptor> {

    private static int connectorCount;
    
    private int artifactId;
    private int serviceId;
    private boolean small;

    /**
     * Prevents external creation.
     * 
     * @param small "small" setup for usual tests or "big" setup for external connection tests
     */
    MyServiceManager(boolean small) {
        this.small = small;
        connectorCount = 0; // reset per test
    }
    
    /**
     * Creates an artifact id.
     * 
     * @return the artifact id
     */
    private String createArtifactId() {
        return "art_" + artifactId++;
    }
    
    /**
     * Creates a service id.
     * 
     * @return the service id
     */
    private String createServiceId() {
        return "service_" + serviceId++;
    }
    
    @Override
    public String addArtifact(URI location) throws ExecutionException {
        if (null == location) {
            throw new ExecutionException("location must not be null", null);
        }
        String aId = createArtifactId();
        List<MyServiceDescriptor> services = new ArrayList<>();
        String text = location.toString();
        MyServiceDescriptor sd1 = 
             new MyServiceDescriptor(createServiceId(), "name " + text, "desc " + text, new Version(1, 0));
        MyServiceDescriptor sd2 = 
             new MyServiceDescriptor(createServiceId(), "name " + text, "desc " + text, new Version(1, 1));
        services.add(setupData(sd1, sd2));
        if (small) {
            services.add(setupData(sd2));
        } else {
            MyServiceDescriptor sd3 = 
                new MyServiceDescriptor(createServiceId(), "name " + text, "desc " + text, new Version(1, 2));
            MyServiceDescriptor sd4 = 
                new MyServiceDescriptor(createServiceId(), "name " + text, "desc " + text, new Version(1, 3));
            services.add(setupData(sd2, sd3, sd4));
            services.add(setupData(sd3));
            services.add(setupData(sd4));
        }
        super.addArtifact(aId, new MyArtifactDescriptor(aId, text, location, services));
        return aId;
    }
    
    /**
     * Adds some data to test against.
     * 
     * @param sd the descriptor instance
     * @param out the outgoing services
     * @return {@code sd}
     */
    private MyServiceDescriptor setupData(MyServiceDescriptor sd, MyServiceDescriptor... out) {
        sd.addParameter(new MyTypedDataDescriptor("NAME", "reconfigures the name", String.class));
        sd.addInputDataConnector(new MyTypedDataConnectorDescriptor("conn-" + connectorCount, "conn-" + connectorCount, 
            "", Integer.TYPE, null));
        for (MyServiceDescriptor o: out) {
            connectorCount++;
            sd.addOutputDataConnector(new MyTypedDataConnectorDescriptor("conn-" + connectorCount, 
                "conn-" + connectorCount, "", Integer.TYPE, o.getId()));
        }
        return sd;
    }

    @Override
    public void startService(String... serviceIds) throws ExecutionException {
        for (String s: serviceIds) {
            if (ServiceState.START_SERVICE == ServiceState.STARTING) { // full
                setState(getServiceDescriptor(s, "serviceId", "start"), ServiceState.STARTING);
            }
            setState(getServiceDescriptor(s, "serviceId", "start"), ServiceState.RUNNING);
        }
    }

    @Override
    public void stopService(String... serviceIds) throws ExecutionException {
        for (String s: serviceIds) {
            setState(getServiceDescriptor(s, "serviceId", "stop"), ServiceState.STOPPING);
            setState(getServiceDescriptor(s, "serviceId", "stop"), ServiceState.STOPPED);
        }
    }

    @Override
    public void updateService(String id, URI location) throws ExecutionException {
    }
    
    @Override
    public void removeArtifact(String id) throws ExecutionException {
        super.removeArtifact(id);
    }
    
    @Override
    public void switchToService(String id, String targetId) throws ExecutionException {
        super.switchToService(id, targetId);
    }

    @Override
    public void migrateService(String id, String resourceId) throws ExecutionException {
        super.migrateService(id, resourceId);
    }

    @Override
    public void cloneArtifact(String artifactId, URI location) throws ExecutionException {
        throw new ExecutionException("Not implemented", null);
    }
    
    @Override
    public void activateService(String serviceId) throws ExecutionException {
        MyServiceDescriptor service = getServiceDescriptor(serviceId, "serviceId", "activate");
        if (ServiceState.PASSIVATED == service.getState()) {
            setState(service, ServiceState.ACTIVATING);
            setState(service, ServiceState.RUNNING);
        } else {
            throw new ExecutionException("Cannot passivate as service is in state " + service.getState(), null);
        }
    }

    @Override
    public void passivateService(String serviceId) throws ExecutionException {
        MyServiceDescriptor service = getServiceDescriptor(serviceId, "serviceId", "passivate");
        if (ServiceState.RUNNING == service.getState()) {
            setState(service, ServiceState.PASSIVATING);
            setState(service, ServiceState.PASSIVATED);
        } else {
            throw new ExecutionException("Cannot passivate as service is in state " + service.getState(), null);
        }
    }

    @Override
    public void reconfigureService(String serviceId, Map<String, String> values) throws ExecutionException {
        MyServiceDescriptor service = getServiceDescriptor(serviceId, "serviceId", "reconfigure");
        ServiceState state = service.getState();
        setState(service, ServiceState.RECONFIGURING);
        // reconfigure
        setState(service, state);
    }
    
    @Override
    protected Predicate<TypedDataConnectorDescriptor> getAvailablePredicate() {
        return c-> true;
    }
    
}