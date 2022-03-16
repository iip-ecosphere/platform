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

package test.de.iip_ecosphere.platform.examples.vdw;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.configuration.ConfigurationSetup;
import de.iip_ecosphere.platform.configuration.EasyLogLevel;
import de.iip_ecosphere.platform.configuration.EasySetup;
import de.iip_ecosphere.platform.configuration.PlatformInstantiator;
import de.iip_ecosphere.platform.configuration.PlatformInstantiator.InstantiationConfigurer;
import de.iip_ecosphere.platform.connectors.opcuav1.OpcUaConnector;
import de.iip_ecosphere.platform.services.environment.metricsProvider.MetricsProvider;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase.NotificationMode;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import iip.datatypes.OpcBwIdentification;
import iip.datatypes.OpcBwStateMachine;
import iip.datatypes.OpcBwStateMachineFlags;
import iip.datatypes.OpcBwStateMachineOverview;
import iip.datatypes.OpcIn;
import iip.datatypes.OpcLocalizedText;
import iip.datatypes.OpcOut;
import iip.nodes.MyOpcConnExample;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import net.ssehub.easy.reasoning.core.reasoner.ReasoningResult;
import net.ssehub.easy.varModel.confModel.Configuration;
import test.de.iip_ecosphere.platform.configuration.IvmlTests;

/**
 * OPC UA test with generated platform/connector.
 * 
 * @author Holger Eichelberger, SSE
 */
public class OpcUaModelTest {

    private static MetricsProvider metrics = new MetricsProvider(new SimpleMeterRegistry());

    /**
     * Reusable test configuration/setup.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class TestConfigurer extends InstantiationConfigurer {

        /**
         * Creates a configurer instance.
         * 
         * @param ivmlModelName the name of the IVML model representing the topmost platform configuration
         * @param modelFolder the folder where the model is located (ignored if <b>null</b>)
         * @param outputFolder the output folder for code generation
         */
        public TestConfigurer(String ivmlModelName, File modelFolder, File outputFolder) {
            super(ivmlModelName, modelFolder, outputFolder);
        }

        
        @Override
        protected void validateConfiguration(Configuration conf) throws ExecutionException {
            Assert.assertNotNull(conf);
        }
        
        @Override
        protected void validateReasoningResult(ReasoningResult res) throws ExecutionException {
            Assert.assertFalse(res.hasConflict());
        }
        
        @Override
        protected void handleExecutionException(ExecutionException ex) throws ExecutionException {
            throw ex;
        }

        @Override
        protected void configure(ConfigurationSetup setup) {
            super.configure(setup);
            EasySetup easySetup = setup.getEasySetup();
            easySetup.setLogLevel(EasyLogLevel.VERBOSE); // override for debugging
            File modelFolder;
            String folder = System.getProperty("easyModelFolder", null);
            if (null == folder) {
                modelFolder = new File("src/main/easy").getAbsoluteFile();
            } else {
                modelFolder = new File(folder);
            }
            try {            
                modelFolder = modelFolder.getCanonicalFile();
            } catch (IOException e) {
                Assert.fail("Cannot create canonical file name for " + modelFolder);
            }
            easySetup.setIvmlMetaModelFolder(modelFolder);
        }

    }
    
    /**
     * Tests the VDW platform/connector configuration.
     * 
     * @throws ExecutionException shall not occur
     * @throws IOException shall not occur
     */
    @Test
    public void testVdw() throws ExecutionException, IOException {
        File gen = new File("gen/vdw");
        PlatformInstantiator.instantiate(
            IvmlTests.genApps(new TestConfigurer("VDW", new File("src/test/easy"), gen)));
    }
    
    /**
     * Runs the generated connector. Currently not integrated with test as the VDW OPC server must be online 
     * (external, not guaranteed).
     * 
     * @param args the command line arguments, ignored
     * @throws IOException in case that the VDW server cannot be accessed
     */
    public static void main(String[] args) throws IOException {
        ActiveAasBase.setNotificationMode(NotificationMode.NONE); // disable AAS connector registration
        AtomicInteger count = new AtomicInteger(0);
        ReceptionCallback<OpcOut> cb = new ReceptionCallback<OpcOut>() {

            @Override
            public void received(OpcOut data) {
                System.out.println("RCV " 
                    + OpcUaModelTest.toString(data.getState().getMachine()) + "\n" 
                    + OpcUaModelTest.toString(data.getIdentification()));
                count.incrementAndGet();
            }
            
            @Override
            public Class<OpcOut> getType() {
                return OpcOut.class;
            }
            
            
        };
        OpcUaConnector<OpcOut, OpcIn> conn = createPlatformConnector(cb);
        final int maxRequests = 1;
        for (int i = 0; i < maxRequests; i++) {
            System.out.println("REQUEST " + i);
            conn.request(true);
        }
        System.out.println("Sleeping to flush...");
        TimeUtils.sleep(1000);
        System.out.println("Disconnecting...");
        conn.disconnect();
        System.out.println("Received: " + count);
        System.exit(0);
    }
    
    /**
     * Turns a machine state into a string for display.
     * 
     * @param machine the machine instance
     * @return the string representation
     */
    private static String toString(OpcBwStateMachine machine) {
        return toString(machine.getFlags()) + "\n" + toString(machine.getOverview());
    }
    
    /**
     * Turns a machine flags instance into a string for display.
     * 
     * @param flags the machine flags instance
     * @return the string representation
     */
    private static String toString(OpcBwStateMachineFlags flags) {
        return "flags:\n" 
            + " - alarm: " + flags.getAlarm() + "\n" 
            + " - cal: " + flags.getCalibrated() + "\n" 
            + " - emergency:" + flags.getEmergency() + "\n" 
            + " - error:" + flags.getError() + "\n" 
            + " - init:" + flags.getMachineInitialized() + "\n" 
            + " - on:" + flags.getMachineOn() + "\n" 
            + " - power:" + flags.getPowerPresent() + "\n" 
            + " - recInRun:" + flags.getRecipeInRun() + "\n" 
            + " - warning:" + flags.getWarning();
    }
    
    /**
     * Turns an identification instance into a string for display.
     * 
     * @param id the identification instance
     * @return the string representation
     */
    private static String toString(OpcBwIdentification id) {
        return "id:\n"
            + " - manufacturer: " + toString(id.getManufacturer()) + "\n"
            + " - model: " + toString(id.getModel()) + "\n"
            + " - deviceCls: " + id.getDeviceClass() + "\n" 
            + " - instanceUri: " + id.getProductInstanceUri() + "\n" 
            + " - serNr: " + id.getSerialNumber() + "\n"
            + " - yearOfConstr: " + id.getYearOfConstruction();
    }

    /**
     * Turns a localized text instance into a string for display.
     * 
     * @param text the localized text instance
     * @return the string representation
     */
    private static String toString(OpcLocalizedText text) {
        return "(loc " + text.getLocale() + " text " + text.getText() + ")";
    }

    /**
     * Turns a machine overview instance into a string for display.
     * 
     * @param overview the machine overview instance
     * @return the string representation
     */
    private static String toString(OpcBwStateMachineOverview overview) {
        return "overview:\n"
            + " - mode: " + overview.getCurrentMode() + "\n"
            + " - state: " + overview.getCurrentState();
    }

    /**
     * Creates the platform connector to be tested.
     *  
     * @param callback the callback
     * @return the connector instance
     * @throws IOException if creating the connector fails
     */
    private static OpcUaConnector<OpcOut, OpcIn> createPlatformConnector(
        ReceptionCallback<OpcOut> callback) throws IOException {
        OpcUaConnector<OpcOut, OpcIn> conn = new OpcUaConnector<>(
            MyOpcConnExample.createConnectorAdapter(metrics, new File("opcTest.txt")));
        conn.connect(MyOpcConnExample.createConnectorParameter());
        conn.setReceptionCallback(callback);
        return conn;
    }

}
