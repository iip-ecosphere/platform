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
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.services.AbstractServiceManager;
import de.iip_ecosphere.platform.services.ArtifactDescriptor;
import de.iip_ecosphere.platform.services.ServiceDescriptor;
import de.iip_ecosphere.platform.services.ServiceFactory;
import de.iip_ecosphere.platform.services.ServiceManager;
import de.iip_ecosphere.platform.services.ServiceState;
import de.iip_ecosphere.platform.services.TypedDataConnectorDescriptor;
import de.iip_ecosphere.platform.support.CollectionUtils;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase.NotificationMode;

/**
 * Tests {@link ServiceManager}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ServiceManagerTest {
    
    /**
     * Tests {@link ServiceManager}.
     * 
     * @throws ExecutionException shall not occur
     * @throws URISyntaxException shall not occur
     */
    @Test
    public void testMgr() throws ExecutionException, URISyntaxException {
        NotificationMode oldM = ActiveAasBase.setNotificationMode(NotificationMode.NONE);
        final URI dummy = new URI("file:///dummy");
        ServiceManager mgr = ServiceFactory.getServiceManager();
        Assert.assertNotNull(mgr);
        
        try {
            mgr.addArtifact(null);
            Assert.fail("No exception");
        } catch (ExecutionException e) {
            // ok
        }
        String aId = mgr.addArtifact(dummy);
        Assert.assertNotNull(aId);
        ArtifactDescriptor aDesc = mgr.getArtifact(aId);
        Assert.assertNotNull(aId);
        Assert.assertTrue(mgr.getArtifactIds().contains(aId));
        Assert.assertTrue(mgr.getArtifacts().contains(aDesc));

        Assert.assertEquals(aId, aDesc.getId());
        Assert.assertTrue(aDesc.getName().length() > 0);
        Assert.assertTrue(aDesc.getServiceIds().size() > 0);
        List<String> sIds = CollectionUtils.toList(aDesc.getServiceIds().iterator());
        String sId = sIds.get(0);
        Assert.assertNotNull(sId);
        ServiceDescriptor sDesc = aDesc.getService(sId);
        Assert.assertNotNull(sDesc);
        Assert.assertTrue(aDesc.getServiceIds().contains(sDesc.getId()));
        Assert.assertTrue(aDesc.getServices().contains(sDesc));
        Assert.assertEquals(ServiceState.AVAILABLE, sDesc.getState());
        
        mgr.startService(sId);
        Assert.assertEquals(ServiceState.RUNNING, sDesc.getState());
        mgr.passivateService(sId);
        Assert.assertEquals(ServiceState.PASSIVATED, sDesc.getState());
        mgr.activateService(sId);
        Assert.assertEquals(ServiceState.RUNNING, sDesc.getState());
        mgr.reconfigureService(sId, new HashMap<String, String>());
        // TODO test parameterDescriptors
        Assert.assertEquals(ServiceState.RUNNING, sDesc.getState());
        mgr.setServiceState(sId, ServiceState.RUNNING); // no effect, just call
        mgr.stopService(sId);
        Assert.assertEquals(ServiceState.STOPPED, sDesc.getState());

        assertException(() -> mgr.cloneArtifact(aId, dummy));
        assertException(() -> mgr.migrateService(aId, "other"));
        assertException(() -> mgr.switchToService(aId, sId));
        mgr.updateService(aId, dummy);
        
        mgr.removeArtifact(aId);
        Assert.assertFalse(mgr.getArtifactIds().contains(aId));
        Assert.assertFalse(mgr.getArtifacts().contains(aDesc));
        ActiveAasBase.setNotificationMode(oldM);
    }
    
    /**
     * A method execution without parameters potentially causing an exception.
     * 
     * @author Holger Eichelberger, SSE
     */
    interface WithExecutionException {
        
        /**
         * A method execution without parameters potentially causing an exception.
         * 
         * @throws ExecutionException may occur if something fails
         */
        public void run() throws ExecutionException;
        
    }
    
    /**
     * Asserts that an exception occurred, e.g., as {@code func} is currently not implemented.
     * 
     * @param func the function to execute
     */
    static void assertException(WithExecutionException func) {
        try {
            func.run();
            Assert.fail("No Exception");
        } catch (ExecutionException e) {
            // ok, not implemented
        }
    }
    
    /**
     * Availability predicate that always returns {@code true} and records tested connection names.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class AvailableTrue implements Predicate<TypedDataConnectorDescriptor> {

        private Set<String> tested = new HashSet<>();
        
        @Override
        public boolean test(TypedDataConnectorDescriptor conn) {
            tested.add(conn.getName());
            return true;
        }
        
        /**
         * Asserts that {@code expected} connection names have been tested in 
         * {@code #test(TypedDataConnectorDescriptor)} before. Clears {@link #tested}.
         * 
         * @param expected the expected connection names
         */
        public void assertTested(String... expected) {
            if (expected.length == 0) {
                Assert.assertTrue("Not expected tested: " + tested, tested.isEmpty());
            } else {
                Assert.assertEquals(expected.length, tested.size());
                for (String e : expected) {
                    Assert.assertTrue("Not tested " + e, tested.contains(e));
                }
                tested.clear();
            }
        }
        
    }
    
    /**
     * Basic tests for {@link AbstractServiceManager#sortByDependency(List, java.util.Collection, 
     * java.util.function.Predicate)}.
     */
    @Test
    public void testSortByDependencyBasic() {
        AvailableTrue av = new AvailableTrue();
        List<MyServiceDescriptor> services = new ArrayList<MyServiceDescriptor>();
        List<MyServiceDescriptor> localServices = new ArrayList<MyServiceDescriptor>();
        
        // no services to sort, no internal services
        List<MyServiceDescriptor> result = AbstractServiceManager.sortByDependency(services, localServices, av);
        Assert.assertTrue(result.isEmpty());
        av.assertTested();
        
        // one service to sort, no internal services
        MyServiceDescriptor s1 = new MyServiceDescriptor("s1", "s1", "", null);
        services.add(s1);
        new MyArtifactDescriptor("a1", "a1", services);
        result = AbstractServiceManager.sortByDependency(services, localServices, av);
        assertList(result, s1);
        av.assertTested();
        
        // two service to sort, no internal services
        services.clear();
        s1 = new MyServiceDescriptor("s1", "s1", "", null);
        MyServiceDescriptor s2 = new MyServiceDescriptor("s2", "s2", "", null);
        services.add(s1);
        services.add(s2);
        new MyArtifactDescriptor("a1", "a1", services);
        result = AbstractServiceManager.sortByDependency(services, localServices, av);
        assertCollection(result, s1, s2);
        av.assertTested();
    }

    /**
     * Tests for {@link AbstractServiceManager#sortByDependency(List, java.util.Collection, 
     * java.util.function.Predicate)} with other services.
     */
    @Test
    public void testSortByDependency() {
        AvailableTrue av = new AvailableTrue();
        List<MyServiceDescriptor> services = new ArrayList<MyServiceDescriptor>();
        List<MyServiceDescriptor> localServices = new ArrayList<MyServiceDescriptor>();

        MyServiceDescriptor s11 = new MyServiceDescriptor("s1.1", "s1.2", "", null);
        s11.addOutputDataConnector(new MyTypedDataConnectorDescriptor("output", "output", "", Integer.class));
        MyServiceDescriptor s12 = new MyServiceDescriptor("s1.2", "s1.2", "", null);
        s12.addInputDataConnector(new MyTypedDataConnectorDescriptor("input", "input", "", Integer.class));
        localServices.add(s11);
        localServices.add(s12);
        new MyArtifactDescriptor("a1", "a1", localServices);

        // s1.1 -> s2.1 -> s2.2 -> s1.2 
        MyServiceDescriptor s21 = new MyServiceDescriptor("s2.1", "s2.2", "", null);
        s21.addInputDataConnector(new MyTypedDataConnectorDescriptor("input", "input", "", Integer.class));
        s21.addOutputDataConnector(new MyTypedDataConnectorDescriptor("im1", "im1", "", Integer.class));
        MyServiceDescriptor s22 = new MyServiceDescriptor("s2.2", "s2.2", "", null);
        s22.addInputDataConnector(new MyTypedDataConnectorDescriptor("im1", "im1", "", Integer.class));
        s22.addOutputDataConnector(new MyTypedDataConnectorDescriptor("output", "output", "", Integer.class));
        services.add(s21);
        services.add(s22);
        new MyArtifactDescriptor("a2", "a2", services);
        localServices.addAll(services);
        
        List<MyServiceDescriptor> result = AbstractServiceManager.sortByDependency(services, localServices, av);
        assertList(result, s22, s21);
        av.assertTested("output", "im1"); // all outgoing connections

        Collections.reverse(services);

        // s1.1 -> s2.1 -> s2.2 -> s1.2 just with reversed input sequence
        result = AbstractServiceManager.sortByDependency(services, localServices, av);
        assertList(result, s22, s21);
        av.assertTested("output", "im1"); // all outgoing connections

        // pretend that "im1" does not exist
        result = AbstractServiceManager.sortByDependency(services, localServices, c -> !c.getId().equals("im1"));
        assertList(result, s22, s21); // same result, but forcibly added to end
    }

    /**
     * Tests for {@link AbstractServiceManager#sortByDependency(List, java.util.Collection, 
     * java.util.function.Predicate)} for ensembles.
     */
    @Test
    public void testSortByDependencyEnsemble() {
        AvailableTrue av = new AvailableTrue();
        List<MyServiceDescriptor> services = new ArrayList<MyServiceDescriptor>();
        List<MyServiceDescriptor> localServices = new ArrayList<MyServiceDescriptor>();

        MyServiceDescriptor s11 = new MyServiceDescriptor("s1.1", "s1.2", "", null);
        s11.addInputDataConnector(new MyTypedDataConnectorDescriptor("input", "input", "", Integer.class));
        s11.addOutputDataConnector(new MyTypedDataConnectorDescriptor("intl1", "intl1", "", Integer.class));
        MyServiceDescriptor s12 = new MyServiceDescriptor("s1.2", "s1.2", "", null);
        s12.addOutputDataConnector(new MyTypedDataConnectorDescriptor("output", "output", "", Integer.class));
        s12.addInputDataConnector(new MyTypedDataConnectorDescriptor("intl1", "intl1", "", Integer.class));
        s11.setEnsembleLeader(s12);
        services.add(s11);
        services.add(s12);

        // ensemble members after ensemble leaders, no further sequence
        MyServiceDescriptor s21 = new MyServiceDescriptor("s2.1", "s2.1", "", null);
        s21.addInputDataConnector(new MyTypedDataConnectorDescriptor("input", "input", "", Integer.class));
        s21.addOutputDataConnector(new MyTypedDataConnectorDescriptor("output", "output", "", Integer.class));
        s21.addOutputDataConnector(new MyTypedDataConnectorDescriptor("im1", "im1", "", Integer.class));
        s21.addInputDataConnector(new MyTypedDataConnectorDescriptor("im2", "im2", "", Integer.class));
        MyServiceDescriptor s22 = new MyServiceDescriptor("s2.2", "s2.2", "", null);
        s22.addInputDataConnector(new MyTypedDataConnectorDescriptor("im1", "im1", "", Integer.class));
        s22.addOutputDataConnector(new MyTypedDataConnectorDescriptor("im2", "im2", "", Integer.class));
        s22.setEnsembleLeader(s21);
        services.add(s21);
        services.add(s22);
        new MyArtifactDescriptor("a", "a", services);
        localServices.addAll(services);
        
        List<MyServiceDescriptor> result = AbstractServiceManager.sortByDependency(services, localServices, av);
        assertCollection(result, s12, s21, s22, s11);
        Assert.assertTrue(result.indexOf(s12) < result.indexOf(s11));
        Assert.assertTrue(result.indexOf(s21) < result.indexOf(s22));
        av.assertTested("output"); // all local services

        Collections.reverse(services);

        // ensemble members after ensemble leaders, no further sequence
        result = AbstractServiceManager.sortByDependency(services, localServices, av);
        assertCollection(result, s12, s21, s22, s11);
        Assert.assertTrue(result.indexOf(s12) < result.indexOf(s11));
        Assert.assertTrue(result.indexOf(s21) < result.indexOf(s22));
        av.assertTested("output"); // all local services
    }

    /**
     * Asserts the contents of a list with considering the sequence.
     * 
     * @param <T> the element type
     * @param list the list
     * @param expected the expected elements and sequence
     */
    @SuppressWarnings("unchecked")
    static <T> void assertList(List<T> list, T... expected) {
        Assert.assertNotNull(list);
        Assert.assertEquals(expected.length, list.size());
        for (int t = 0; t < expected.length; t++) {
            Assert.assertEquals(expected[t], list.get(t));
        }
    }

    /**
     * Asserts the contents of a collection without considering the sequence.
     * 
     * @param <T> the element type
     * @param collection the collection
     * @param expected the expected elements
     */
    @SuppressWarnings("unchecked")
    static <T> void assertCollection(Collection<T> collection, T... expected) {
        Assert.assertNotNull(collection);
        Assert.assertEquals(expected.length, collection.size());
        for (int t = 0; t < expected.length; t++) {
            Assert.assertTrue(collection.contains(expected[t]));
        }
    }

}
