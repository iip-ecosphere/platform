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

package test.de.iip_ecosphere.platform.support.iip_aas;

import org.junit.Test;

import de.iip_ecosphere.platform.support.iip_aas.AdminShellYamlSemanticIdResolverDescriptor;
import de.iip_ecosphere.platform.support.iip_aas.Eclass;
import de.iip_ecosphere.platform.support.iip_aas.EclassYamlSemanticIdResolverDescriptor;
import de.iip_ecosphere.platform.support.semanticId.SemanticIdResolutionResult;
import de.iip_ecosphere.platform.support.semanticId.SemanticIdResolutionResult.Naming;
import de.iip_ecosphere.platform.support.semanticId.SemanticIdResolver;
import org.junit.Assert;

/**
 * Tests {@link YamlSemanticIdResolverDescriptorTest}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class YamlSemanticIdResolverDescriptorTest {
    
    /**
     * Tests eclass Yaml.
     */
    @Test
    public void testEclassYaml() {
        SemanticIdResolutionResult res = SemanticIdResolver.resolve(Eclass.IRDI_UNIT_MILLISECOND);
        Assert.assertNotNull(res);
        Assert.assertEquals(Eclass.IRDI_UNIT_MILLISECOND, res.getSemanticId());
        Assert.assertEquals(EclassYamlSemanticIdResolverDescriptor.KIND, res.getKind());
        Assert.assertEquals(EclassYamlSemanticIdResolverDescriptor.PUBLISHER, res.getPublisher());
        Assert.assertTrue(res.getRevision().length() > 0);
        Assert.assertTrue(res.getVersion().length() > 0);
        Assert.assertEquals(2, res.getNaming().size());
        Naming naming = res.getNaming().get("de");
        Assert.assertNotNull(naming);
        Assert.assertTrue(naming.getName().length() > 0);
        Assert.assertTrue(naming.getDescription().length() > 0);
        Assert.assertTrue(naming.getStructuredName().length() > 0);
    }

    /**
     * Tests AdminShellIo Yaml.
     */
    @Test
    public void testAdminShellIoYaml() {
        SemanticIdResolutionResult res = SemanticIdResolver.resolve(
            "https://admin-shell.io/ZVEI/TechnicalData/ProductImage/1/1");
        Assert.assertNotNull(res);
        Assert.assertEquals("https://admin-shell.io/ZVEI/TechnicalData/ProductImage/1/1", res.getSemanticId());
        Assert.assertEquals(AdminShellYamlSemanticIdResolverDescriptor.KIND, res.getKind());
        Assert.assertEquals(AdminShellYamlSemanticIdResolverDescriptor.PUBLISHER, res.getPublisher());
        Assert.assertTrue(res.getRevision().length() > 0);
        Assert.assertTrue(res.getVersion().length() > 0);
        Assert.assertEquals(2, res.getNaming().size());
        Naming naming = res.getNaming().get("de");
        Assert.assertNotNull(naming);
        Assert.assertTrue(naming.getName().length() > 0);
        Assert.assertTrue(naming.getDescription().length() > 0);
        Assert.assertTrue(naming.getStructuredName().length() > 0);
    }

}
