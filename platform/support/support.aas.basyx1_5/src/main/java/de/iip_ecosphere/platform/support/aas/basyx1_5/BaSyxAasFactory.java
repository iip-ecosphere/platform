/**
 * ******************************************************************************
 * Copyright (c) {2024} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.aas.basyx1_5;

import java.io.IOException;
import java.util.Optional;

import org.eclipse.basyx.components.aas.configuration.AASServerBackend;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.property.valuetype.ValueType;
import org.eclipse.basyx.vab.coder.json.connector.JSONConnector;
import org.eclipse.basyx.vab.modelprovider.api.IModelProvider;
import org.eclipse.basyx.vab.protocol.api.IConnectorFactory;
import org.eclipse.basyx.vab.protocol.http.connector.HTTPConnector;
import org.eclipse.basyx.vab.protocol.http.connector.HTTPConnectorFactory;
import org.eclipse.basyx.vab.protocol.http.connector.IAuthorizationSupplier;
import org.eclipse.basyx.vab.protocol.https.HTTPSConnectorProvider;

import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor;
import de.iip_ecosphere.platform.support.aas.Registry;
import de.iip_ecosphere.platform.support.aas.SetupSpec;
import de.iip_ecosphere.platform.support.aas.Type;
import de.iip_ecosphere.platform.support.aas.SetupSpec.AasComponent;
import de.iip_ecosphere.platform.support.aas.SetupSpec.ComponentSetup;
import de.iip_ecosphere.platform.support.aas.basyx.BaSyxAbstractAasServer;
import de.iip_ecosphere.platform.support.aas.basyx.BaSyxRegistry;
import de.iip_ecosphere.platform.support.aas.basyx.DeploymentSpec;
import de.iip_ecosphere.platform.support.aas.basyx.Tools;
import de.iip_ecosphere.platform.support.aas.basyx.VersionAdjustment;
import de.iip_ecosphere.platform.support.aas.basyx.VersionAdjustment.RegistryDeploymentServerCreator;
import de.iip_ecosphere.platform.support.aas.basyx.VersionAdjustment.ServerCreator;
import de.iip_ecosphere.platform.support.aas.basyx1_5.basyx.BaSyxHTTPServer;

/**
 * AAS factory for BaSyx. Do not rename, this class is referenced in {@code META-INF/services}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxAasFactory extends de.iip_ecosphere.platform.support.aas.basyx.BaSyxAasFactory {

    private static final String PLUGIN_ID = "aas.basyx-1.5";
    
    /**
     * Factory descriptor for Java Service Loader.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class Descriptor extends AbstractDescriptor {

        @Override
        public AasFactory createInstance() {
            return new BaSyxAasFactory();
        }

        @Override
        public String getId() {
            return PLUGIN_ID;
        }
        
    }
    
    /**
     * Creates an instance.
     */
    public BaSyxAasFactory() {
        super();
        Tools.mapBaSyxType(Type.DOUBLE, ValueType.Decimal); // preliminary
        Tools.mapBaSyxType(Type.DATE_TIME, ValueType.Date); // preliminary
        // set up default BaSyx Server creator
        VersionAdjustment.setupBaSyxServerCreator(new ServerCreator() {

            @Override
            public Server createServer(DeploymentSpec dSpec, SetupSpec sSpec, AasComponent component) {
                BaSyxHTTPServer server = new BaSyxHTTPServer(dSpec.getContext(), sSpec, component);
                Server result = new Server() {

                    @Override
                    public Server start() {
                        server.start();
                        return this;
                    }

                    @Override
                    public void stop(boolean dispose) {
                        server.shutdown();
                    }

                };
                return result;
            }
            
        });

        // checkstyle: stop parameter number check

        VersionAdjustment.setupRegistryDeploymentServerCreator(new RegistryDeploymentServerCreator() {
            
            @Override
            public BaSyxAbstractAasServer createRegistryDeploymentServer(DeploymentSpec deploymentSpec, SetupSpec spec,
                AasComponent component, String regUrl, AASServerBackend backend, String... options) {
                return new BaSyxRegistryDeploymentAasServer(deploymentSpec, spec, component, regUrl, backend, options);
            }
        });
        
        // checkstyle: resume parameter number check
    }

    @Override
    public String getName() {
        return "AAS/BaSyx v1.5.1 (2024/05/02)";
    }
    
    @Override
    public boolean supportsPropertyFunctions() {
        return true;
    }
    
    @Override
    public Registry obtainRegistry(SetupSpec spec, Schema aasSchema) throws IOException {
        IConnectorFactory cFactory;
        ComponentSetup cSetup = spec.getSetup(AasComponent.AAS_REGISTRY);
        AuthenticationDescriptor aDesc = cSetup.getAuthentication();
        IAuthorizationSupplier authSupplier = new IAuthorizationSupplier() {
            
            @Override
            public Optional<String> getAuthorization() {
                String header = null;
                if (AuthenticationDescriptor.isEnabledOnClient(aDesc)) {
                    header = AuthenticationDescriptor.authenticate(aDesc, false);
                }
                return header == null ? Optional.empty() : Optional.of(header);
            }
        };
        if (Schema.HTTPS == aasSchema) {
            cFactory = new HTTPSConnectorProvider(authSupplier);
        } else {
            cFactory = new HTTPConnectorFactory() {
                @Override
                protected IModelProvider createProvider(String addr) {
                    return new JSONConnector(new HTTPConnector(addr, authSupplier));
                }
            };
        }
        return new BaSyxRegistry(cSetup.getEndpoint(), cFactory);
    }

}
