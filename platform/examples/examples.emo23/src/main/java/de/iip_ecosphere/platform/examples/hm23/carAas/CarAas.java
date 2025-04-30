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

package de.iip_ecosphere.platform.examples.hm23.carAas;

import java.io.IOException;
import java.util.GregorianCalendar;

import de.iip_ecosphere.platform.examples.hm23.carAas.CarsYaml.Car;
import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.AasServer;
import de.iip_ecosphere.platform.support.aas.DeploymentRecipe;
import de.iip_ecosphere.platform.support.aas.LangString;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.aas.DeploymentRecipe.ImmediateDeploymentRecipe;
import de.iip_ecosphere.platform.support.aas.Type;
import de.iip_ecosphere.platform.support.aas.types.technicaldata.TechnicalDataBuilder;
import de.iip_ecosphere.platform.support.aas.types.technicaldata.TechnicalDataBuilder.GeneralInformationBuilder;
import de.iip_ecosphere.platform.support.aas.AasUtils;
import de.iip_ecosphere.platform.support.aas.BasicSetupSpec;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;

/**
 * Implements a local car AAS fallback server. This is intentionally not a main program as the 
 * BaSyx dependency can only be used here in test scope.
 * 
 * @author Holger Eichelberger, SSE
 */
public class CarAas implements Runnable {

    private static AasFactory factory = AasFactory.getInstance();
    private String hostname;
    private int port;
    private AasServer server;
    private boolean running;
    private boolean withHook;
    
    /**
     * Creates the AAS.
     * 
     * @param hostname the hostname/IP of the server, may be required to make it visible to outside
     * @param port the port to create the AAS server/registry on
     * @param withHook adds a shutdown hook
     */
    CarAas(String hostname, int port, boolean withHook) {
        this.hostname = hostname;
        this.port = port;
        this.withHook = withHook;
    }

    /**
     * Creates the AAS of a single car based on the generic frame for technical submodels.
     * 
     * @param car the car
     * @return the created AAS
     */
    private static final Aas createCarAas(Car car) {
        String id = car.getId();
        if (!id.startsWith("car")) {
            id = "car" + id;
        }
        final String urn = "urn:::AAS:::" + id + "#";
        AasBuilder aasB = factory.createAasBuilder(id, urn);
        
        // standard stuff
        
        TechnicalDataBuilder tdBuilder = new TechnicalDataBuilder(aasB, null);
        GeneralInformationBuilder giBuilder = tdBuilder.createGeneralInformationBuilder()
            .setManufacturerName("Mittelstandszentrum Digital Hannover")
            .setManufacturerArticleNumber("car" + id)
            .setManufacturerOrderCode("car" + id)
            .setManufacturerProductDesignation(LangString.create("HM'22 Demonstration car"));
        AasUtils.resolveImage(car.getProductImage(), AasUtils.CLASSPATH_RESOURCE_RESOLVER, false, 
            (n, r, m) -> giBuilder.setProductImage(r, m));
        AasUtils.resolveImage(car.getManufacturerLogo(), AasUtils.CLASSPATH_RESOURCE_RESOLVER, true, 
            (n, r, m) -> giBuilder.setManufacturerLogo(r, m));
        giBuilder.build();
        
        final GregorianCalendar now = new GregorianCalendar();
        tdBuilder.createFurtherInformationBuilder()
            .setValidDate(now.getTime())
            .build();
        tdBuilder.createTechnicalPropertiesBuilder().build();
        tdBuilder.createProductClassificationsBuilder().build();

        // car stuff
        SubmodelBuilder smb = aasB.createSubmodelBuilder("ProductData", null);
        smb.createPropertyBuilder("ProductId").setValue(Type.STRING, car.getId()).build();
        smb.createPropertyBuilder("Length").setValue(Type.STRING, car.getLength()).build();
        smb.createPropertyBuilder("Thickness").setValue(Type.STRING, car.getThickness()).build();
        smb.createPropertyBuilder("Weight").setValue(Type.STRING, car.getWeight()).build();
        smb.createPropertyBuilder("HardwareRevision").setValue(Type.STRING, car.getHardwareRevision()).build();
        smb.createPropertyBuilder("Windows").setValue(Type.INTEGER, car.getWindows()).build();
        smb.createPropertyBuilder("TiresColor").setValue(Type.STRING, car.getTiresColor()).build();
        smb.createPropertyBuilder("Pattern").setValue(Type.BOOLEAN, car.isPattern()).build();
        smb.createPropertyBuilder("EngravingText").setValue(Type.STRING, car.getEngravingText()).build();
        smb.createPropertyBuilder("Diagnosis").setValue(Type.STRING, car.getDiagnosis()).build();
        smb.build();
        
        tdBuilder.build();
        
        return aasB.build();
    }

    /**
     * Creates the AAS.
     * 
     * @param hostname the hostname of the server
     * @param port the port to create the AAS server/registry on
     */
    public static void buildAas(String hostname, int port) {
        new Thread(new CarAas(hostname, port, true)).start(); 
    }

    @Override
    public void run() {
        try {
            System.out.println("Reading cars...");
            CarsYaml yml = CarsYaml.readFromYaml();
    
            final String registryPath = "registry";
            ServerAddress serverAdr = new ServerAddress(Schema.HTTP, hostname, port);
            Endpoint regEp = new Endpoint(serverAdr, registryPath);
            BasicSetupSpec spec = new BasicSetupSpec(regEp, serverAdr);
            ImmediateDeploymentRecipe rcp = factory.createDeploymentRecipe(spec)
                .setAccessControlAllowOrigin(DeploymentRecipe.ANY_CORS_ORIGIN)
                .forRegistry();
    
            for (Car c: yml.getCars()) {
                System.out.println("Creating AAS for car with id: " + c.getId());
                rcp.deploy(createCarAas(c));
            }
            
            server = rcp.createServer();
            if (withHook) {
                Runtime.getRuntime().addShutdownHook(new Thread(() -> { stop(true); }));
            }
            System.out.println("Starting AAS server: " + serverAdr.toUri());
            System.out.println("Starting Registry server: " + factory.getFullRegistryUri(regEp));
            if (withHook) {
                System.out.println("Running until CTRL-C");
            }
            server.start();
            running = true;
        } catch (IOException e) {
            System.out.println("Cannot start AAS server: " + e.getMessage());
        }        
    }
    
    /**
     * Stops the server.
     * 
     * @param dispose shall also allocated resources of this server be disposed
     */
    public void stop(boolean dispose) {
        if (running) {
            running = false;
            server.stop(dispose);
        }
    }

}
