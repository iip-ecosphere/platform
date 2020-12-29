/**
 * ******************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.support.iip_aas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.CollectionUtils;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.iip_aas.AasContributor;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;

/**
 * Tests {@link AasPartRegistry}. Do not rename, this class is referenced in {@code META-INF/services}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AasPartRegistryTest {
    
    private static final String NAME_MY_AAS = "myAas";

    /**
     * A first contributor, mapped in via {@code META-INF/services}.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class Contributor1 implements AasContributor {
        
        @Override
        public Aas contributeTo(AasBuilder aasBuilder) {
            aasBuilder.createSubmodelBuilder("c1", null).build();
            return null;
        }
        
    }

    /**
     * A second contributor with own AAS, mapped in via {@code META-INF/services}.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class Contributor2 implements AasContributor {
        
        @Override
        public Aas contributeTo(AasBuilder aasBuilder) {
            AasBuilder builder = AasFactory.getInstance().createAasBuilder(NAME_MY_AAS, "urn:::AAS:::myAas#");
            builder.createSubmodelBuilder("c2", null).build();
            return builder.build();
        }
        
    }

    /**
     * Tests the part registry.
     */
    @Test
    public void testPartRegistry() {
        Assert.assertTrue(CollectionUtils.toSet(AasPartRegistry.contributors()).size() >= 2);
        Set<Class<? extends AasContributor>> cClasses = AasPartRegistry.contributorClasses();
        Assert.assertTrue(cClasses.contains(Contributor1.class));
        Assert.assertTrue(cClasses.contains(Contributor2.class));
        
        List<Aas> aas = AasPartRegistry.build();
        Assert.assertNotNull(aas);
        Assert.assertEquals(2, aas.size());
        
        Map<String, Aas> hashedAas = new HashMap<>();
        for (Aas a : aas) {
            hashedAas.put(a.getIdShort(), a);
        }
        Assert.assertNotNull(hashedAas.get(AasPartRegistry.NAME_AAS));
        Assert.assertNotNull(hashedAas.get(NAME_MY_AAS));
        
        Assert.assertNotNull(hashedAas.get(AasPartRegistry.NAME_AAS).getSubmodel("c1"));
        Assert.assertNotNull(hashedAas.get(NAME_MY_AAS).getSubmodel("c2"));
        
        // TODO test deploy
    }

    /**
     * Tests the {@link AasPartRegistry#getAas(List, String)} helper method.
     */
    @Test
    public void testGet() {
        List<Aas> list = new ArrayList<Aas>();
        Assert.assertNull(AasPartRegistry.getAas(list, ""));
        list.add(AasFactory.getInstance().createAasBuilder("test", "urn:::AAS:::test#").build());
        Assert.assertNull(AasPartRegistry.getAas(list, ""));
        Assert.assertNotNull(AasPartRegistry.getAas(list, "test"));
        list.add(AasFactory.getInstance().createAasBuilder("test2", "urn:::AAS:::test2#").build());
        Assert.assertNull(AasPartRegistry.getAas(list, ""));
        Assert.assertNotNull(AasPartRegistry.getAas(list, "test"));
    }

}
