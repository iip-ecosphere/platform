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

package test.de.iip_ecosphere.platform.services.environment;

import de.iip_ecosphere.platform.services.environment.AbstractService;
import de.iip_ecosphere.platform.services.environment.Service;
import de.iip_ecosphere.platform.services.environment.ServiceKind;
import de.iip_ecosphere.platform.services.environment.switching.ServiceBase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests parts of the service interface.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ServiceTest {

    /**
     * Tests part of the service interface.
     */
    @Test
    public void testService() {
        Service instance = AbstractService.createInstance(
            "test.de.iip_ecosphere.platform.services.environment.MyService", Service.class);
        Assert.assertNotNull(instance);
    }
    
    /**
     * Tests composing and splitting ids.
     */
    @Test
    public void testComposedIds() {
        String id = ServiceBase.composeId("sId", null);
        Assert.assertEquals("sId", ServiceBase.getServiceId(id));
        Assert.assertEquals("", ServiceBase.getApplicationId(id));
        Assert.assertEquals("", ServiceBase.getApplicationInstanceId(id));

        id = ServiceBase.composeId("sId", "");
        Assert.assertEquals("sId", ServiceBase.getServiceId(id));
        Assert.assertEquals("", ServiceBase.getApplicationId(id));
        Assert.assertEquals("", ServiceBase.getApplicationInstanceId(id));

        id = ServiceBase.composeId("sId", "aId");
        Assert.assertEquals("sId", ServiceBase.getServiceId(id));
        Assert.assertEquals("aId", ServiceBase.getApplicationId(id));
        Assert.assertEquals("", ServiceBase.getApplicationInstanceId(id));

        id = ServiceBase.composeId("sId", "aId", "001");
        Assert.assertEquals("sId", ServiceBase.getServiceId(id));
        Assert.assertEquals("aId", ServiceBase.getApplicationId(id));
        Assert.assertEquals("001", ServiceBase.getApplicationInstanceId(id));

        id = ServiceBase.composeId("s@Id", "a@Id", "0@01");
        Assert.assertEquals("sId", ServiceBase.getServiceId(id));
        Assert.assertEquals("aId", ServiceBase.getApplicationId(id));
        Assert.assertEquals("001", ServiceBase.getApplicationInstanceId(id));
    }

    /**
     * Tests {@link ServiceBase#validateApplicationInstanceId(String)}.
     */
    @Test
    public void testValidateApplicationInstanceId() {
        Assert.assertEquals(ServiceBase.DEFAULT_APPLICATION_INSTANCE_ID, 
            ServiceBase.validateApplicationInstanceId(null));
        Assert.assertEquals(ServiceBase.DEFAULT_APPLICATION_INSTANCE_ID, 
            ServiceBase.validateApplicationInstanceId(""));
        Assert.assertEquals("id", 
            ServiceBase.validateApplicationInstanceId("id"));
    }
    
    /**
     * Tests {@link ServiceKind#START_COMPARATOR}.
     */
    @Test
    public void testServiceComparator() {
        Assert.assertTrue(ServiceKind.TRANSFORMATION_SERVICE.before(ServiceKind.SOURCE_SERVICE, false));
        Assert.assertFalse(ServiceKind.TRANSFORMATION_SERVICE.before(ServiceKind.TRANSFORMATION_SERVICE, false));
        Assert.assertTrue(ServiceKind.TRANSFORMATION_SERVICE.before(ServiceKind.TRANSFORMATION_SERVICE, true));

        Assert.assertTrue(ServiceKind.SOURCE_SERVICE.after(ServiceKind.SINK_SERVICE, false));
        Assert.assertFalse(ServiceKind.SOURCE_SERVICE.after(ServiceKind.SOURCE_SERVICE, false));
        Assert.assertTrue(ServiceKind.SOURCE_SERVICE.after(ServiceKind.SOURCE_SERVICE, true));

        List<ServiceKind> kinds = new ArrayList<>(List.of(ServiceKind.SOURCE_SERVICE, ServiceKind.SOURCE_SERVICE, 
            ServiceKind.TRANSFORMATION_SERVICE, ServiceKind.PROBE_SERVICE, ServiceKind.SINK_SERVICE));
        Collections.sort(kinds, ServiceKind.START_COMPARATOR);
        for (int i = 0; i < kinds.size() - 1; i++) {
            ServiceKind k1 = kinds.get(i);
            ServiceKind k2 = kinds.get(i + 1);
            Assert.assertTrue(k1.before(k2, true));
        }
        
        List<Service> services = new ArrayList<>();
        services.add(new MyService("so1", ServiceKind.SOURCE_SERVICE));
        services.add(new MyService("so2", ServiceKind.SOURCE_SERVICE));
        services.add(new MyService("tr1", ServiceKind.TRANSFORMATION_SERVICE));
        services.add(new MyService("pr1", ServiceKind.PROBE_SERVICE));
        services.add(new MyService("si1", ServiceKind.SINK_SERVICE));
        services.add(new MyService("si2", ServiceKind.SINK_SERVICE));
        Collections.sort(services, Service.START_COMPARATOR);
        for (int s = 0; s < services.size() - 1; s++) {
            Service s1 = services.get(s);
            Service s2 = services.get(s + 1);
            Assert.assertTrue(s1.getKind().before(s2.getKind(), true));
        }
    }
    
}
