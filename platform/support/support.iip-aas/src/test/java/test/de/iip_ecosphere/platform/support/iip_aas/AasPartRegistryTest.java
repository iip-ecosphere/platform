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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.iip_aas.AasContributor;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;

/**
 * Tests {@link AasPartRegistry}.
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
            aasBuilder.createSubModelBuilder("c1").build();
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
            builder.createSubModelBuilder("c2").build();
            return builder.build();
        }
        
    }

    /**
     * Tests the part registry.
     */
    @Test
    public void testPartRegistry() {
        List<Aas> aas = AasPartRegistry.build();
        Assert.assertNotNull(aas);
        Assert.assertEquals(2, aas.size());
        
        Map<String, Aas> hashedAas = new HashMap<>();
        for (Aas a : aas) {
            hashedAas.put(a.getIdShort(), a);
        }
        Assert.assertNotNull(hashedAas.get(AasPartRegistry.ID_SHORT));
        Assert.assertNotNull(hashedAas.get(NAME_MY_AAS));
        
        Assert.assertNotNull(hashedAas.get(AasPartRegistry.ID_SHORT).getSubModel("c1"));
        Assert.assertNotNull(hashedAas.get(NAME_MY_AAS).getSubModel("c2"));
        
        // TODO test deploy
    }

}
