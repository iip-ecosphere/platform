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

package test.de.iip_ecosphere.platform.support.semanticId.eclass;

import java.io.File;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.semanticId.DefaultSemanticIdResolutionResult;
import de.iip_ecosphere.platform.support.semanticId.DefaultSemanticIdResolutionResult.DefaultNaming;
import de.iip_ecosphere.platform.support.semanticId.SemanticIdResolutionResult;
import de.iip_ecosphere.platform.support.semanticId.SemanticIdResolver;
import de.iip_ecosphere.platform.support.semanticId.eclass.EclassSemanticIdResolver;
import de.iip_ecosphere.platform.support.semanticId.eclass.model.TranslatableLabel;

/**
 * Tests {@link EclassSemanticIdResolver}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class EclassSemanticIdResolverTest {
    
    private static final String IRDI_UNIT_DEGREES_CELSIUS = "0173-1#05-AAA567#004";

    /**
     * Tests the resolution. Through the test in the resolver, we feed the resolver with structurally valid IRDIs.
     */
    @Test
    public void testResolution() {
        if (new File("./resources.ipr").exists() || new File("./resources").exists()) {
            // we can only test against Eclass if there is a certificate
            
            EclassSemanticIdResolver resolver = new EclassSemanticIdResolver();
            SemanticIdResolutionResult res = resolver.resolveSemanticId(IRDI_UNIT_DEGREES_CELSIUS);
            
            Assert.assertNotNull(res);
            Assert.assertEquals(IRDI_UNIT_DEGREES_CELSIUS, res.getSemanticId());
            
            // for now, let's see what Eclass webservice returns
            Assert.assertNotNull(res.getKind());
            Assert.assertNotNull(res.getPublisher());
            Assert.assertNotNull(res.getRevision());
            Assert.assertNotNull(res.getVersion());
            Assert.assertNotNull(res.getNaming());
        }
    }
    
    /**
     * Instance creation operations.
     */
    @Test
    public void testInstance() {
        // shall be there via JSL
        Assert.assertTrue(SemanticIdResolver.hasResolver(EclassSemanticIdResolver.class));
        
        // not fully testable, as certificate cannot be added here and Eclass API throws timeouts  
        EclassSemanticIdResolver resolver = new EclassSemanticIdResolver();
        Assert.assertTrue(resolver.isResponsible(IRDI_UNIT_DEGREES_CELSIUS));
        Assert.assertFalse(resolver.isResponsible("0173-1#05#AAA567-004"));
        Assert.assertFalse(resolver.isResponsible("abba"));
        
        DefaultSemanticIdResolutionResult res = EclassSemanticIdResolver.createInstance(IRDI_UNIT_DEGREES_CELSIUS);
        Assert.assertNotNull(res);
        Assert.assertEquals(IRDI_UNIT_DEGREES_CELSIUS, res.getSemanticId());
        Assert.assertNotNull(res.getKind());
        Assert.assertNotNull(res.getPublisher());
        Assert.assertEquals("1", res.getRevision());
        Assert.assertEquals("4", res.getVersion());
        
        Map<String, DefaultNaming> naming = EclassSemanticIdResolver.createNaming(null, null, null);
        Assert.assertNotNull(naming);
        Assert.assertTrue(naming.isEmpty());
        
        TranslatableLabel p = new TranslatableLabel();
        p.put(Locale.GERMAN.toString(), "Deutsch");
        p.put(Locale.ENGLISH.toLanguageTag(), "English");
        TranslatableLabel s = new TranslatableLabel();
        s.put(Locale.GERMAN.toString(), "Deutsch!");
        s.put(Locale.ENGLISH.toLanguageTag(), "English!");
        TranslatableLabel d = new TranslatableLabel();
        s.put(Locale.GERMAN.toString(), "D1");
        s.put(Locale.ENGLISH.toLanguageTag(), "E1");
        
        naming = EclassSemanticIdResolver.createNaming(p , null, null);
        assertNaming(p, naming, n -> n.getName());
        naming = EclassSemanticIdResolver.createNaming(null , s, null);
        assertNaming(s, naming, n -> n.getStructuredName());
        naming = EclassSemanticIdResolver.createNaming(p, s, d);
        assertNaming(p, naming, n -> n.getName());
        assertNaming(s, naming, n -> n.getStructuredName());
        assertNaming(d, naming, n -> n.getDescription());
    }

    /**
     * Asserts a naming structure for {@code label}.
     * 
     * @param label the label to assert for
     * @param naming the naming structure
     * @param namingGetter a getter which values to assert on {@code naming} with respect to the values in {@code label}
     */
    private void assertNaming(TranslatableLabel label, Map<String, DefaultNaming> naming, 
        Function<DefaultNaming, String> namingGetter) {
        for (Map.Entry<String, String> e: label.entrySet()) {
            Locale loc = new Locale(e.getKey());
            DefaultNaming n = naming.get(loc.getLanguage());
            Assert.assertNotNull(n);
            Assert.assertEquals(e.getValue(), namingGetter.apply(n));
        }
    }
    
}
