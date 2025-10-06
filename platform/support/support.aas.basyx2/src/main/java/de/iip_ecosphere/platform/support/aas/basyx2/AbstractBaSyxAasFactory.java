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

package de.iip_ecosphere.platform.support.aas.basyx2;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.SetupSpec.AasComponent;
import de.iip_ecosphere.platform.support.FileFormat;
import de.iip_ecosphere.platform.support.NetUtils;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.DeploymentRecipe;
import de.iip_ecosphere.platform.support.aas.InvocablesCreator;
import de.iip_ecosphere.platform.support.aas.OperationsProvider;
import de.iip_ecosphere.platform.support.aas.PersistenceRecipe;
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder;
import de.iip_ecosphere.platform.support.aas.Registry;
import de.iip_ecosphere.platform.support.aas.ServerRecipe;
import de.iip_ecosphere.platform.support.aas.SetupSpec;
import de.iip_ecosphere.platform.support.aas.SimpleLocalProtocolCreator;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;

/**
 * AAS factory for BaSyx. Do not rename, this class is referenced in {@code META-INF/services}.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractBaSyxAasFactory extends AasFactory {

    // public for testing, do not reference from outside
    public static final String PROTOCOL_AAS_REST = "AAS-REST";
    
    private final Map<FileFormat, PersistenceRecipe> recipes = new HashMap<>();
    
    /**
     * The VAB-TCP Protocol creator.
     * 
     * @author Holger Eichelberger, SSE
     */
    protected static class AasRestProtocolCreator implements ProtocolCreator {

        @Override
        public InvocablesCreator createInvocablesCreator(SetupSpec spec) {
            return new AasRestInvocablesCreator(spec); 
        }

        @Override
        public ProtocolServerBuilder createProtocolServerBuilder(SetupSpec spec) {
            return new AasOperationsProvider.AasRestOperationsBuilder(spec);
        }
        
    }
    
    /**
     * Creates an instance.
     */
    public AbstractBaSyxAasFactory() {
        registerProtocolCreator(LOCAL_PROTOCOL, new SimpleLocalProtocolCreator() {
            
            @Override
            protected OperationsProvider createOperationsProvider() {
                return new AasOperationsProvider();
            }
            
        });
        
        AasRestProtocolCreator rest = new AasRestProtocolCreator();
        registerProtocolCreator(DEFAULT_PROTOCOL, rest);
        registerProtocolCreator(PROTOCOL_AAS_REST, rest);
        
        registerPersistenceRecipe(new XmlPersistenceRecipe());
        registerPersistenceRecipe(new JsonPersistenceRecipe());

        registerAvailabilityFunction(s -> NetUtils.connectionOk(s.getEndpoint().toServerUri() + "/actuator/health"), 
            AasComponent.values());
    }
    
    /**
     * Registers a recipe.
     * 
     * @param recipe the recipe to be registered
     */
    protected void registerPersistenceRecipe(PersistenceRecipe recipe) {
        if (null != recipe) {
            for (FileFormat f : recipe.getSupportedFormats()) {
                recipes.put(f, recipe);
            }
        }
    }
    
    @Override
    public AasBuilder createAasBuilder(String idShort, String identifier) {
        return new BaSyxAas.BaSyxAasBuilder(idShort, identifier);
    }

    @Override
    public SubmodelBuilder createSubmodelBuilder(String idShort, String identifier) {
        return new BaSyxSubmodel.BaSyxSubmodelBuilder(null, idShort, identifier, null);
    }

    @Override
    protected ServerRecipe createDefaultServerRecipe() {
        return new BaSyxServerRecipe();
    }
    
    @Override
    public Registry obtainRegistry(SetupSpec spec) throws IOException {
        return new BaSyxRegistry(spec);
    }
    
    @Override
    public Registry obtainRegistry(SetupSpec spec, Schema aasSchema) throws IOException {
        return obtainRegistry(spec);
    }

    @Override
    public DeploymentRecipe createDeploymentRecipe(SetupSpec spec) {
        return new BaSyxDeploymentRecipe(spec);
    }

    @Override
    public PersistenceRecipe createPersistenceRecipe() {
        return new BaSyxPersistenceRecipe();
    }

    /**
     * A generic, delegating persistence recipe based on those registered in 
     * {@link AbstractBaSyxAasFactory#registerPersistenceRecipe(PersistenceRecipe)}.
     * 
     * @author Holger Eichelberger, SSE
     */
    private class BaSyxPersistenceRecipe implements PersistenceRecipe {
        
        /**
         * Finds a matching {@link PersistenceRecipe} against the known recipes and their file formats.
         * 
         * @param file the file to look for
         * @return the persistence recipe
         * @throws IllegalArgumentException if there is no matching file format/recipe
         */
        private PersistenceRecipe findMatching(File file) {
            PersistenceRecipe result = null;
            for (FileFormat ff : recipes.keySet()) {
                if (ff.matches(file)) {
                    result = recipes.get(ff);
                }
            }
            if (null == result) {
                throw new IllegalArgumentException("Unrecognized file format for " + file);
            }
            return result;
        }
        
        @Override
        public void writeTo(List<Aas> aas, File thumbnail, List<FileResource> resources, File file) throws IOException {
            findMatching(file).writeTo(aas, thumbnail, resources, file);
        }

        @Override
        public List<Aas> readFrom(File file) throws IOException {
            return findMatching(file).readFrom(file);
        }

        @Override
        public Collection<FileFormat> getSupportedFormats() {
            return Collections.unmodifiableCollection(recipes.keySet()); // this shall be unmodifiable
        }

    }
    
    @Override
    protected boolean needsIdFix(String id) {
        return false; // unclear whether this still applies, before it was on "value", "invocationList" -> Tools.checkId
    }

    @Override
    public boolean createPropertiesEarly() {
        return true;
    }

}
