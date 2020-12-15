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

package de.iip_ecosphere.platform.support.aas.basyx;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.basyx.aas.manager.ConnectedAssetAdministrationShellManager;
import org.eclipse.basyx.aas.metamodel.map.descriptor.ModelUrn;
import org.eclipse.basyx.aas.registration.proxy.AASRegistryProxy;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.property.valuetypedef.PropertyValueTypeDef;
import org.eclipse.basyx.vab.protocol.http.connector.HTTPConnectorProvider;

import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.AasFactoryDescriptor;
import de.iip_ecosphere.platform.support.aas.DeploymentRecipe;
import de.iip_ecosphere.platform.support.aas.PersistenceRecipe;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.aas.Type;

/**
 * AAS factory for BaSyx. Do not rename, this class is referenced in {@code META-INF/services}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxAasFactory extends AasFactory {

    private static Map<Type, PropertyValueTypeDef> types = new HashMap<>();
    
    static {
        types.put(Type.BOOLEAN, PropertyValueTypeDef.Boolean);
        types.put(Type.DOUBLE, PropertyValueTypeDef.Double);
        types.put(Type.FLOAT, PropertyValueTypeDef.Float);
        types.put(Type.INTEGER, PropertyValueTypeDef.Integer);
        types.put(Type.NULL, PropertyValueTypeDef.Null);
        types.put(Type.STRING, PropertyValueTypeDef.String);
        types.put(Type.VOID, PropertyValueTypeDef.Void);
    }
    
    /**
     * Factory descriptor for Java Service Loader.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class Descriptor implements AasFactoryDescriptor {

        @Override
        public AasFactory createInstance() {
            return new BaSyxAasFactory();
        }
        
    }
    
    @Override
    public AasBuilder createAasBuilder(String idShort, String urn) {
        return new BaSyxAas.BaSyxAasBuilder(idShort, urn);
    }

    @Override
    public SubmodelBuilder createSubmodelBuilder(String idShort) {
        return new BaSyxSubmodel.BaSyxSubmodelBuilder(null, idShort);
    }

    /**
     * Translates a frontend type to an implementation type.
     * 
     * @param type the frontend type
     * @return the implementation type
     */
    static PropertyValueTypeDef translate(Type type) {
        return types.get(type);
    }
    
    // checkstyle: stop exception type check

    @Override
    public Aas retrieveAas(String host, int port, String endpointPath, String urn) throws IOException {
        try {
            String uri = "http://" + host + ":" + port + "/" + endpointPath;
            AASRegistryProxy registry = new AASRegistryProxy(uri);
            HTTPConnectorProvider connectorProvider = new HTTPConnectorProvider();
            ConnectedAssetAdministrationShellManager manager = new ConnectedAssetAdministrationShellManager(
                registry, connectorProvider);
            ModelUrn aasURN = new ModelUrn(urn);
            return new BaSyxConnectedAas(manager.retrieveAAS(aasURN));
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    // checkstyle: resume exception type check

    @Override
    public DeploymentRecipe createDeploymentRecipe(String host, int port) {
        return new BaSyxDeploymentBuilder(host, port);
    }


    @Override
    public DeploymentRecipe createDeploymentRecipe(String contextPath, String host, int port) {
        return new BaSyxDeploymentBuilder(contextPath, host, port);
    }

    @Override
    public String getName() {
        return "AAS/BaSyx";
    }

    @Override
    public PersistenceRecipe createPersistenceRecipe() {
        return new BaSyxPersistenceRecipe();
    }

}
