package test.de.iip_ecosphere.platform.services;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import de.iip_ecosphere.platform.services.AbstractServiceDescriptor;
import de.iip_ecosphere.platform.services.TypedDataConnectorDescriptor;

import static test.de.iip_ecosphere.platform.services.ServiceManagerTest.*;

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

/**
 * Tests static methods in {@link AbstractServiceDescriptor}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AbstractServiceDescriptorTest {

    /**
     * Tests {@link AbstractServiceDescriptor#ensemble(de.iip_ecosphere.platform.services.ServiceDescriptor)}.
     */
    @Test
    public void testEnsemble() {
        List<MyServiceDescriptor> services = new ArrayList<MyServiceDescriptor>();

        MyServiceDescriptor s0 = new MyServiceDescriptor("s0", "s0", "", null);
        services.add(s0);
        MyServiceDescriptor s11 = new MyServiceDescriptor("s1.1", "s1.2", "", null);
        MyServiceDescriptor s12 = new MyServiceDescriptor("s1.2", "s1.2", "", null);
        s11.setEnsembleLeader(s12);
        services.add(s11);
        services.add(s12);
        MyServiceDescriptor s21 = new MyServiceDescriptor("s2.1", "s2.1", "", null);
        MyServiceDescriptor s22 = new MyServiceDescriptor("s2.2", "s2.2", "", null);
        s22.setEnsembleLeader(s21);
        MyServiceDescriptor s23 = new MyServiceDescriptor("s2.3", "s2.3", "", null);
        s23.setEnsembleLeader(s21);
        services.add(s21);
        services.add(s22);
        services.add(s23);

        // artifact needed as common structure to identify related services
        new MyArtifactDescriptor("a", "a", services);
        
        assertCollection(AbstractServiceDescriptor.ensemble(s0), s0);
        assertCollection(AbstractServiceDescriptor.ensemble(s11), s11, s12);
        assertCollection(AbstractServiceDescriptor.ensemble(s12), s11, s12);
        assertCollection(AbstractServiceDescriptor.ensemble(s21), s21, s22, s23);
        assertCollection(AbstractServiceDescriptor.ensemble(s22), s21, s22, s23);
        assertCollection(AbstractServiceDescriptor.ensemble(s23), s21, s22, s23);
    }
    
    /**
     * Tests {@link AbstractServiceDescriptor#connectorNames(java.util.Collection)}.
     */
    @Test
    public void testConnectorNames() {
        List<TypedDataConnectorDescriptor> conns = new ArrayList<>();
        assertCollection(AbstractServiceDescriptor.connectorNames(conns)); // none
        conns.add(new MyTypedDataConnectorDescriptor("c1", "conn1", Integer.class));
        assertCollection(AbstractServiceDescriptor.connectorNames(conns), "c1");
        conns.add(new MyTypedDataConnectorDescriptor("c2", "conn2", Integer.class));
        assertCollection(AbstractServiceDescriptor.connectorNames(conns), "c1", "c2");
    }
    
    /**
     * Tests {@link AbstractServiceDescriptor#ensembleConnectionNames(
     * de.iip_ecosphere.platform.services.ServiceDescriptor)}.
     */
    @Test
    public void testEnsembleConnectionNames() {
        List<MyServiceDescriptor> services = new ArrayList<MyServiceDescriptor>();

        MyServiceDescriptor s0 = new MyServiceDescriptor("s0", "s0", "", null);
        s0.addOutputDataConnector(new MyTypedDataConnectorDescriptor("output", "output", Integer.class));
        services.add(s0);
        MyServiceDescriptor s11 = new MyServiceDescriptor("s1.1", "s1.2", "", null);
        s11.addOutputDataConnector(new MyTypedDataConnectorDescriptor("int1", "int1", Integer.class));
        MyServiceDescriptor s12 = new MyServiceDescriptor("s1.2", "s1.2", "", null);
        s12.addInputDataConnector(new MyTypedDataConnectorDescriptor("int1", "int1", Integer.class));
        s12.addOutputDataConnector(new MyTypedDataConnectorDescriptor("output", "output", Integer.class));
        s11.setEnsembleLeader(s12);
        services.add(s11);
        services.add(s12);
        MyServiceDescriptor s21 = new MyServiceDescriptor("s2.1", "s2.1", "", null);
        s21.addInputDataConnector(new MyTypedDataConnectorDescriptor("input", "input", Integer.class));
        s21.addOutputDataConnector(new MyTypedDataConnectorDescriptor("int20", "int20", Integer.class));
        MyServiceDescriptor s22 = new MyServiceDescriptor("s2.2", "s2.2", "", null);
        s22.addInputDataConnector(new MyTypedDataConnectorDescriptor("int20", "int20", Integer.class));
        s22.addOutputDataConnector(new MyTypedDataConnectorDescriptor("int21", "int21", Integer.class));
        s22.setEnsembleLeader(s21);
        MyServiceDescriptor s23 = new MyServiceDescriptor("s2.3", "s2.3", "", null);
        s23.addInputDataConnector(new MyTypedDataConnectorDescriptor("int21", "int21", Integer.class));
        s23.addOutputDataConnector(new MyTypedDataConnectorDescriptor("output", "output", Integer.class));
        s23.setEnsembleLeader(s21);
        services.add(s21);
        services.add(s22);
        services.add(s23);

        // artifact needed as common structure to identify related services
        new MyArtifactDescriptor("a", "a", services);

        assertCollection(AbstractServiceDescriptor.ensembleConnectorNames(s0)); // none
        assertCollection(AbstractServiceDescriptor.ensembleConnectorNames(s11), "int1");
        assertCollection(AbstractServiceDescriptor.ensembleConnectorNames(s12), "int1");
        assertCollection(AbstractServiceDescriptor.ensembleConnectorNames(s21), "int20", "int21");
        assertCollection(AbstractServiceDescriptor.ensembleConnectorNames(s22), "int20", "int21");
        assertCollection(AbstractServiceDescriptor.ensembleConnectorNames(s23), "int20", "int21");
    }

}
