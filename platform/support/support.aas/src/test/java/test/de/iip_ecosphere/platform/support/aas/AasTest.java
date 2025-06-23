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

package test.de.iip_ecosphere.platform.support.aas;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import org.junit.Test;

import de.iip_ecosphere.platform.support.CollectionUtils;
import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.AasPrintVisitor;
import de.iip_ecosphere.platform.support.aas.AasUtils;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor.DefaultRole;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor.IdentityTokenWithRole;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor.RbacAction;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor.Role;
import de.iip_ecosphere.platform.support.aas.BasicSetupSpec;
import de.iip_ecosphere.platform.support.aas.DelegatingAuthenticationDescriptor;
import de.iip_ecosphere.platform.support.aas.IdentityStoreAuthenticationDescriptor;
import de.iip_ecosphere.platform.support.aas.InvocablesCreator;
import de.iip_ecosphere.platform.support.aas.LangString;
import de.iip_ecosphere.platform.support.aas.Operation;
import de.iip_ecosphere.platform.support.aas.Property;
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder;
import de.iip_ecosphere.platform.support.aas.RbacRoles;
import de.iip_ecosphere.platform.support.aas.Reference;
import de.iip_ecosphere.platform.support.aas.ReferenceElement;
import de.iip_ecosphere.platform.support.aas.Registry;
import de.iip_ecosphere.platform.support.aas.SetupSpec;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElement;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.aas.SubmodelElementContainerBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection.SubmodelElementCollectionBuilder;
import de.iip_ecosphere.platform.support.identities.IdentityToken;
import de.iip_ecosphere.platform.support.net.KeyStoreDescriptor;
import de.iip_ecosphere.platform.support.aas.Type;

import org.junit.Assert;

/**
 * Tests the AAS abstraction with server and client on the "same machine".
 * 
 * @author Monika Staciwa, SSE
 * @author Holger Eichelberger, SSE
 */
public class AasTest {
    
    public static final String QNAME_VAR_LOTSIZE;
    public static final String QNAME_VAR_VENDOR;
    public static final String QNAME_VAR_POWCONSUMPTION;
    public static final String QNAME_OP_STARTMACHINE;
    public static final String QNAME_OP_RECONFIGURE;
    public static final String QNAME_OP_STOPMACHINE;

    public static final String NAME_AAS = "aasTest";
    public static final String NAME_SUBMODEL = "machine";
    private static final String NAME_SUBMODELC_OUTER = "outer";
    private static final String NAME_VAR_SUBMODELC_OUTER_VAR = "outerVar";
    private static final String NAME_VAR_SUBMODELC_OUTER_REF = "outerRef";
    private static final String NAME_SUBMODELC_INNER = "inner";
    private static final String NAME_VAR_SUBMODELC_INNER_VAR = "innerVar";
    private static final String NAME_VAR_SUBMODELC_INNER_INT = "innerInt";
    private static final String NAME_VAR_SUBMODELC_INNER_REF = "innerRef";
    private static final String NAME_VAR_LOTSIZE = "lotSize";
    private static final String NAME_VAR_VENDOR = "vendor";
    private static final String NAME_VAR_POWCONSUMPTION = "powerConsumption";
    private static final String NAME_VAR_DESCRIPTION1 = "description1";
    private static final String NAME_VAR_DESCRIPTION2 = "description2";
    private static final String NAME_OP_STARTMACHINE = "startMachine";
    private static final String NAME_OP_RECONFIGURE = "setLotSize";
    private static final String NAME_OP_STOPMACHINE = "stopMachine";

    private static final String NAME_RBAC_SMOPEN = "openSm";
    private static final String NAME_RBAC_PROPCLOSED = "closedProp";
    private static final String NAME_RBAC_OPCLOSED = "closedOp";

    private static final String NAME_RBAC_SMCLOSED = "closedSm";
    private static final String NAME_RBAC_PROPOPEN = "openProp";
    private static final String NAME_RBAC_OPOPEN = "openOp";
    
    private static final ServerAddress VAB_SERVER = new ServerAddress(Schema.HTTP); // localhost, ephemeral
    private static final String URN_AAS = "urn:::AAS:::testMachines#";
    
    private static final LangString DESCRIPTION = new LangString("en", "A description");
    
    static {
        QNAME_VAR_LOTSIZE = NAME_SUBMODEL + "/" + NAME_VAR_LOTSIZE;
        QNAME_VAR_VENDOR = NAME_SUBMODEL + "/" + NAME_VAR_VENDOR;
        QNAME_VAR_POWCONSUMPTION = NAME_SUBMODEL + "/" + NAME_VAR_POWCONSUMPTION;
        QNAME_OP_STARTMACHINE = NAME_SUBMODEL + "/" + NAME_OP_STARTMACHINE;
        QNAME_OP_RECONFIGURE = NAME_SUBMODEL + "/" + NAME_OP_RECONFIGURE;
        QNAME_OP_STOPMACHINE = NAME_SUBMODEL + "/" + NAME_OP_STOPMACHINE;
    }
    
    /**
     * Creates the operations server for the given machine instance and for the operations in 
     * {@link #createAasOperationsElements(SubmodelElementContainerBuilder, SetupSpec, TestMachine, 
     * AuthenticationDescriptor)}.
     * 
     * @param spec the setup specification
     * @param machine the machine
     * @return the protocol server
     */
    public static Server createOperationsServer(SetupSpec spec, TestMachine machine) {
        AasFactory instance = AasFactory.getInstance();
        AasFactory.setPluginId("unknown"); // forth and back that it has been called once
        AasFactory.setPluginId(AasFactory.DEFAULT_PLUGIN_ID);
        AasFactory.setInstance(instance);
        AasFactory factory = AasFactory.getInstance();
        ProtocolServerBuilder builder = factory.createProtocolServerBuilder(spec);
        builder.defineProperty(NAME_VAR_LOTSIZE, () -> {
            return machine.getLotSize(); 
        }, (param) -> {
            machine.setLotSize((int) param); 
        });
        builder.defineProperty(NAME_VAR_VENDOR, () -> {
            return machine.getVendor(); 
        }, (param) -> { // whether meaningful or not
            machine.setVendor((String) param); 
        });
        builder.defineProperty(NAME_VAR_POWCONSUMPTION, () -> {
            return machine.getPowerConsumption(); 
        }, null);
        builder.defineOperation(NAME_OP_STARTMACHINE, (params) -> {
            machine.start();
            return null;
        });
        builder.defineOperation(NAME_OP_RECONFIGURE, (params) 
            -> machine.reconfigure(AasUtils.readInt(params, 0, -1)));
        builder.defineOperation(NAME_OP_STOPMACHINE, (params) -> {
            machine.stop();
            return null;
        });
        builder.createPayloadCodec(); // there are specific tests for that, we ignore the result here..
        
        builder.defineOperation(NAME_RBAC_OPCLOSED, (params) -> null);
        builder.defineOperation(NAME_RBAC_OPOPEN, (params) -> null);
        return builder.build();
    }

    /**
     * Creates the corresponding AAS elements for {@link #createOperationsServer(SetupSpec, TestMachine)}.
     * 
     * @param subModelBuilder the sub model container builder to add the elements to
     * @param spec the setup specification
     * @param machine the test machine instance
     * @param authDesc the authentication descriptor, may be <b>null</b> for none
     */
    public void createAasOperationsElements(SubmodelElementContainerBuilder subModelBuilder, 
        SetupSpec spec, TestMachine machine, AuthenticationDescriptor authDesc) {
        AasFactory factory = AasFactory.getInstance();
        InvocablesCreator invC = factory.createInvocablesCreator(spec);
        Utils.setValue(
            subModelBuilder.createPropertyBuilder(NAME_VAR_LOTSIZE).setType(Type.INTEGER), 
            machine.getLotSize(), invC.createGetter(NAME_VAR_LOTSIZE), invC.createSetter(NAME_VAR_LOTSIZE))
            .build();
        subModelBuilder.createPropertyBuilder(NAME_VAR_VENDOR)
            .setType(Type.STRING)
            .bindLazy(invC.createGetter(NAME_VAR_VENDOR), invC.createSetter(NAME_VAR_VENDOR))
            .build();
        Utils.setValue(
            subModelBuilder.createPropertyBuilder(NAME_VAR_POWCONSUMPTION).setType(Type.DOUBLE)
            .setSemanticId("irdi:0173-1#02-AAV232#002"), // id taken from BaSyX -> temperature ???
            machine.getPowerConsumption(), invC.createGetter(NAME_VAR_POWCONSUMPTION), InvocablesCreator.READ_ONLY)
            .build();
        subModelBuilder.createOperationBuilder(NAME_OP_STARTMACHINE)
            .setInvocable(invC.createInvocable(NAME_OP_STARTMACHINE))
            .build(authDesc);
        subModelBuilder.createOperationBuilder(NAME_OP_RECONFIGURE)
            .addInputVariable(NAME_VAR_LOTSIZE, Type.INTEGER)
            .setInvocableLazy(invC.createInvocable(NAME_OP_RECONFIGURE))
            .build(Type.BOOLEAN, authDesc);
        subModelBuilder.createOperationBuilder(NAME_OP_STOPMACHINE)
            .setInvocable(invC.createInvocable(NAME_OP_STOPMACHINE))
            .build(authDesc);
    }
    
    /**
     * Tests creating/reading an AAS over all protocols of a factory, without authentication.
     * 
     * @throws SocketException shall not occur if the test works
     * @throws UnknownHostException shall not occur if the test works
     * @throws ExecutionException shall not occur if the test works
     * @throws IOException shall not occur if the test works
     */
    @Test
    public void testVabQuery() throws SocketException, UnknownHostException, ExecutionException, IOException {
        testVabQuery(null);
    }

    /**
     * Tests creating/reading an AAS over all protocols of a factory, without authentication.
     * 
     * @throws SocketException shall not occur if the test works
     * @throws UnknownHostException shall not occur if the test works
     * @throws ExecutionException shall not occur if the test works
     * @throws IOException shall not occur if the test works
     */
    @Test
    public void testVabQueryAuth() throws SocketException, UnknownHostException, ExecutionException, IOException {
        if (AasFactory.getInstance().supportsAuthentication()) {
            testVabQuery(new IdentityStoreAuthenticationDescriptor());
        }
    }

    /**
     * Tests creating/reading an AAS over all protocols of a factory.
     * 
     * @throws SocketException shall not occur if the test works
     * @throws UnknownHostException shall not occur if the test works
     * @throws ExecutionException shall not occur if the test works
     * @throws IOException shall not occur if the test works
     */
    private void testVabQuery(AuthenticationDescriptor aDesc) throws SocketException, UnknownHostException, 
        ExecutionException, IOException {
        String[] desiredProtocols = getServerProtocols();
        String[] providedProtocols = AasFactory.getInstance().getProtocols();
        desiredProtocols = Stream.of(desiredProtocols) // only intersection counts, test only what is provided
            .filter(Arrays.asList(providedProtocols)::contains)
            .toArray(String[]::new);

        for (String sProto : desiredProtocols) {
            for (String proto : providedProtocols) {
                if (!AasFactory.LOCAL_PROTOCOL.equals(proto) && !excludeProtocol(proto)) { // VAB only
                    System.out.println("Testing Asset protocol: " 
                        + (proto.length() > 0 ? "'" + proto + "'" : "<default>") + " " 
                        + (sProto.length() > 0 ? "on server protocol '" + sProto + "'" : "<default>"));
                    testVabQuery(proto, sProto, aDesc);
                }
            }
        }
    }
    
    /**
     * Returns the server protocols to use during test.
     * 
     * @return the server protocols, empty for HTTP/unencrypted
     */
    public String[] getServerProtocols() {
        return new String[] {""};
    }

    /**
     * To be overridden: descriptor for keystore per protocol.
     * 
     * @param protocol the protocol
     * @return the keystore, may be <b>null</b> for none
     */
    protected KeyStoreDescriptor getKeyStoreDescriptor(String protocol) {
        return null;
    }
    
    /**
     * To be overridden: Exclude the given protocol from testing. 
     * 
     * @param protocol the protocol
     * @return {@code true} for exclusion, {@code false} for inclusion
     */
    protected boolean excludeProtocol(String protocol) {
        return false;
    }
    
    /**
     * To be overridden: schema per protocol.
     * 
     * @param serverProtocol the protocol
     * @return the schema
     */
    protected Schema getAasServerAddressSchema(String serverProtocol) {
        return Schema.HTTP;
    }

    /**
     * Tests creating/reading an AAS.
     *
     * @param protocol the VAB protocol as used in {@link AasFactory}
     * @param serverProtocol use server protocols to use, empty string for HTTP, rest see {@link AasFactory}
     * @throws SocketException shall not occur if the test works
     * @throws UnknownHostException shall not occur if the test works
     * @throws ExecutionException shall not occur if the test works
     * @throws IOException shall not occur if the test works
     */
    protected void testVabQuery(String protocol, String serverProtocol, AuthenticationDescriptor authDesc) 
        throws SocketException, UnknownHostException, ExecutionException, IOException {

        TestMachine machine = new TestMachine();

        AasFactory factory = AasFactory.getInstance();
        ServerAddress aasServerAddress = new ServerAddress(
            getAasServerAddressSchema(serverProtocol)); // localhost, ephemeral
        Server httpServer;
        Endpoint registryEndpoint;
        KeyStoreDescriptor ksd = getKeyStoreDescriptor(serverProtocol);
        registryEndpoint = createDependentEndpoint(aasServerAddress, "registry");
        BasicSetupSpec spec = new BasicSetupSpec(registryEndpoint, aasServerAddress, ksd, authDesc);
        spec.setAssetServerAddress(VAB_SERVER, protocol);
        spec.setAssetServerKeystore(getKeyStoreDescriptor(protocol));
        spec.setAssetServerAuthentication(authDesc);
        Aas aas = createAas(machine, spec, authDesc);
        Server ccServer = createOperationsServer(spec, machine);
        ccServer.start(); // required here by basyx-0.1.0-SNAPSHOT
        ProtocolServerBuilder builder = AasFactory.getInstance().createProtocolServerBuilder(spec);
        Assert.assertTrue(builder.isAvailable(VAB_SERVER.getHost(), 5000));

        httpServer = factory.createDeploymentRecipe(spec)
            .forRegistry()
            .deploy(aas)
            .createServer()
            .start();

        queryAas(spec, machine, authDesc);
        Server.stop(httpServer, true);
        Server.stop(ccServer, true);
    }

    /**
     * Creates a dependent endpoint. May have to be overridden for more recent implementations.
     * 
     * @param address the address
     * @param endpoint the endpoint
     * @return the created enpoint
     */
    protected Endpoint createDependentEndpoint(ServerAddress address, String endpoint) {
        return new Endpoint(address, endpoint);
    }
    
    /**
     * This method creates a test Asset Administration Shell.
     * 
     * @param machine the test machine instance
     * @param spec the setup specification
     * @param authDesc optional authentication descriptor (may be <b>null</b>)
     * @return the created AAS
     * @throws SocketException if the port to be used for the AAS is occupied
     * @throws UnknownHostException shall not occur
     */
    private Aas createAas(TestMachine machine, SetupSpec spec, AuthenticationDescriptor authDesc) 
        throws SocketException, UnknownHostException {
        AasFactory factory = AasFactory.getInstance();
        if (null != authDesc) {
            System.out.println("USER: " + authDesc.getClientToken());
            if (null != authDesc.getServerUsers()) {
                for (IdentityTokenWithRole u : authDesc.getServerUsers()) {
                    System.out.println("SERVER USER: " + u);
                }
            }
        }
        AasBuilder aasBuilder = factory.createAasBuilder(NAME_AAS, URN_AAS)
            .rbacAll(authDesc);
        SubmodelBuilder subModelBuilder = aasBuilder.createSubmodelBuilder(NAME_SUBMODEL, null)
            .rbacAll(authDesc);
        Assert.assertTrue(subModelBuilder.isNew());
        createAasOperationsElements(subModelBuilder, spec, machine, authDesc);
        Reference subModelBuilderRef = subModelBuilder.createReference();
        Assert.assertNotNull(aasBuilder.createSubmodelBuilder(NAME_SUBMODEL, null)); // for modification
        subModelBuilder.createPropertyBuilder(NAME_VAR_DESCRIPTION1)
            .setValue(Type.LANG_STRING, LangString.create("test@de"))
            .build();
        subModelBuilder.createPropertyBuilder(NAME_VAR_DESCRIPTION2)
            .setValue(Type.LANG_STRING, "test2@en")
            .build();
        
        SubmodelElementCollectionBuilder smcBuilderOuter = subModelBuilder.createSubmodelElementCollectionBuilder(
            NAME_SUBMODELC_OUTER);
        SubmodelElementCollectionBuilder smcBuilderInner = smcBuilderOuter.createSubmodelElementCollectionBuilder(
            NAME_SUBMODELC_INNER);
        smcBuilderInner.createPropertyBuilder(NAME_VAR_SUBMODELC_INNER_VAR).setType(Type.AAS_INTEGER)
            .setDescription(DESCRIPTION).build();
        smcBuilderInner.createPropertyBuilder(NAME_VAR_SUBMODELC_INNER_INT).setValue(Type.INTEGER, 1).build();
        ReferenceElement re = smcBuilderInner.createReferenceElementBuilder(NAME_VAR_SUBMODELC_INNER_REF)
            .setValue(subModelBuilderRef).build();
        Assert.assertNotNull(re.getValue());
        Assert.assertTrue(re.getValue().hasReference());
        
        Reference smcBuilder1Ref = smcBuilderInner.createReference();
        smcBuilderInner.build();
        smcBuilderOuter.createPropertyBuilder(NAME_VAR_SUBMODELC_OUTER_VAR).setType(Type.STRING).build();
        smcBuilderOuter.createReferenceElementBuilder(NAME_VAR_SUBMODELC_OUTER_REF).setValue(smcBuilder1Ref).build();
        SubmodelElementCollection smcOuter = smcBuilderOuter.build();
        assertSize(3, smcOuter.elements());
        Assert.assertEquals(3, smcOuter.getElementsCount());
        Assert.assertNotNull(smcOuter.getDataElement(NAME_VAR_SUBMODELC_OUTER_VAR));
        Assert.assertNotNull(smcOuter.getElement(NAME_SUBMODELC_INNER));
        Assert.assertNotNull(subModelBuilder.createSubmodelElementCollectionBuilder(
            NAME_SUBMODELC_OUTER)); // for modification
        
        Submodel submodel = subModelBuilder.build();
        Assert.assertNotNull(submodel.getIdentification());
        assertSize(3, submodel.operations());
        assertSize(0, submodel.dataElements());
        assertSize(5, submodel.properties());
        assertSize(9, submodel.submodelElements());
        Assert.assertNotNull(submodel.getOperation(NAME_OP_RECONFIGURE));
        Assert.assertEquals(9, submodel.getSubmodelElementsCount());
        Assert.assertNull(submodel.getReferenceElement("myRef"));
        Aas aas = aasBuilder.build();
        Assert.assertNotNull(aas.getIdentification());
        
        // adding on local models
        Submodel subAdd = aas.createSubmodelBuilder("sub_add", null)
            .rbacAll(authDesc)
            .build();
        Assert.assertNotNull(aas.getSubmodel("sub_add"));
        subAdd.createSubmodelElementListBuilder("sub_coll").build();
        Assert.assertNotNull(aas.getSubmodel("sub_add").getSubmodelElementList("sub_coll"));
        submodel.createSubmodelElementCollectionBuilder("sub_coll2").build();
        Assert.assertNotNull(submodel.getSubmodelElementCollection("sub_coll2"));

        aas.accept(new AasPrintVisitor());

        testCreateIterate(aas, authDesc);
        testCreateRbac(aas, spec, authDesc);

        return aas;
    }
    
    /**
     * Unused role, just for testing {@link RbacRoles}.
     * 
     * @author Holger Eichelberger, SSE
     */
    public enum MyRole implements Role {
        MY_ROLE;

        @Override
        public boolean anonymous() {
            return false;
        }
    }
    
    /**
     * Asserts roles.
     * 
     * @param addMyRole whether {@link MyRole} shall be added/registered or removed/unregistered before asserting
     */
    private static void assertRoles(boolean addMyRole) {
        if (addMyRole) {
            RbacRoles.registerRole(MyRole.class);
        } else {
            RbacRoles.unregisterRole(MyRole.class);
        }
        for (Role r : DefaultRole.values()) {
            Assert.assertTrue(RbacRoles.contains(DefaultRole.values(), r));
        }
        Assert.assertEquals(addMyRole, RbacRoles.contains(Role.all(), MyRole.MY_ROLE));
        Assert.assertFalse(RbacRoles.contains(Role.allAnonymous(), MyRole.MY_ROLE));
        Assert.assertEquals(addMyRole, RbacRoles.contains(Role.allAuthenticated(), MyRole.MY_ROLE));
    }

    /**
     * Creates two submodels, one open to anonymous and one closed to anonymous, both with open and closed elements.
     * 
     * @param aas the AAS to create the submodels for
     * @param spec the setup spec
     * @param auth the authentication descriptor, may be <b>null</b> for none
     */
    private static void testCreateRbac(Aas aas, SetupSpec spec, AuthenticationDescriptor auth) {
        assertRoles(true);
        
        InvocablesCreator invC = AasFactory.getInstance().createInvocablesCreator(spec);
        SubmodelBuilder sm = aas.createSubmodelBuilder(NAME_RBAC_SMOPEN, null)
            .rbacAll(auth);
        sm.createPropertyBuilder(NAME_RBAC_PROPOPEN)
            .setValue(Type.AAS_INTEGER, 10)
            .build(auth);
        sm.createPropertyBuilder(NAME_RBAC_PROPCLOSED)
            .setValue(Type.AAS_INTEGER, 11)
            .rbacAllAuthenticated(auth)
            .build();
        sm.createOperationBuilder(NAME_RBAC_OPOPEN)
            .setInvocable(invC.createInvocable(NAME_RBAC_OPOPEN))
            .build(auth);
        sm.createOperationBuilder(NAME_RBAC_OPCLOSED)
            .rbacAllAuthenticated(auth)
            .setInvocable(invC.createInvocable(NAME_RBAC_OPCLOSED))
            .build();
        sm.build();

        sm = aas.createSubmodelBuilder(NAME_RBAC_SMCLOSED, null)
            .rbacAllAuthenticated(auth);
        sm.createPropertyBuilder(NAME_RBAC_PROPOPEN)
            .setValue(Type.AAS_INTEGER, 20)
            .rbacAll(auth)
            .build();
        sm.createPropertyBuilder(NAME_RBAC_PROPCLOSED)
            .setValue(Type.AAS_INTEGER, 21)
            .build(auth);
        sm.createOperationBuilder(NAME_RBAC_OPOPEN)
            .rbacAll(auth)
            .setInvocable(invC.createInvocable(NAME_RBAC_OPOPEN))
            .build();
        sm.createOperationBuilder(NAME_RBAC_OPCLOSED)
            .setInvocable(invC.createInvocable(NAME_RBAC_OPCLOSED))
            .build(auth);
        sm.build();

        assertRoles(false);
    }
    
    /**
     * Tests the create and iterate functions.
     * 
     * @param aas the aas to test the functions with
     * @param authDesc the authentication descriptor (may be <b>null</b>)
     */
    private static void testCreateIterate(Aas aas, AuthenticationDescriptor authDesc) {
        // build some submodel with sub-structure
        final int numElts = 10;
        Submodel subAdd = aas.createSubmodelBuilder("sub_ai", null)
            .rbac(authDesc, Role.all(), RbacAction.all())
            .build();
        SubmodelElementCollectionBuilder outerB = subAdd.createSubmodelElementCollectionBuilder("outer");
        SubmodelElementCollectionBuilder collsB = outerB.createSubmodelElementCollectionBuilder("colls");
        for (int i = 1; i <= numElts; i++) {
            SubmodelElementCollectionBuilder b = collsB.createSubmodelElementCollectionBuilder("id_" + i);
            b.createPropertyBuilder("prop")
                .setValue(Type.STRING, "a")
                .build();
            b.build();
        }
        collsB.createPropertyBuilder("prop") // for type filtering
            .setValue(Type.STRING, "a")
            .build();
        collsB.build();
        outerB.build();
        
        // false, submodel does not exist
        Assert.assertFalse(subAdd.iterate(c -> false, SubmodelElement.class, "xyz"));
        // modify by iterate
        Assert.assertTrue(subAdd.iterate(c -> {
            SubmodelElement elt = c.getElement("prop");
            if (elt instanceof Property) {
                try {
                    ((Property) elt).setValue("b");
                } catch (ExecutionException e) {
                    Assert.fail("Unexpected exception: " + e.getMessage());
                }
            }
            return true;
        }, SubmodelElementCollection.class, "outer", "colls"));
        
        // assert iterate changes
        int count = 0;
        for (SubmodelElement e : subAdd.getSubmodelElementCollection("outer")
            .getSubmodelElementCollection("colls").elements()) {
            if (e instanceof SubmodelElementCollection) {
                try {
                    Property prop = ((SubmodelElementCollection) e).getProperty("prop");
                    Assert.assertNotNull(prop);
                    Object val = prop.getValue();
                    Assert.assertNotNull(val);
                    Assert.assertEquals("b", val);
                    count++;
                } catch (ExecutionException ex) {
                    Assert.fail("Unexpected exception: " + ex.getMessage());
                }
            }
        }
        Assert.assertEquals(numElts, count);
        
        // false, submodel does not exist
        Assert.assertFalse(subAdd.create(c -> { }, true, "zyx"));
        Assert.assertTrue(subAdd.create(c-> {
            c.createSubmodelElementCollectionBuilder("id_100").build();
        }, false, "outer", "colls"));
        
        SubmodelElementCollection oc = subAdd.getSubmodelElementCollection("outer")
            .getSubmodelElementCollection("colls");
        oc.update(); // propagation disabled above
        
        Assert.assertNotNull(oc.getSubmodelElementCollection("id_100"));
    }
    
    /**
     * Asserts lang string equality.
     * 
     * @param val the value of a property
     * @param str the (composed) lang string to test for
     */
    private static void assertLangString(Object val, String str) {
        Assert.assertNotNull(val);
        LangString ls1;
        if (val instanceof LangString) {
            ls1 = (LangString) val; // BaSyX v1
        } else {
            ls1 = LangString.create(val.toString());
        }
        LangString ls2 = LangString.create(str);
        Assert.assertEquals(ls2, ls1);
    }
   
    // checkstyle: stop method length check
    
    /**
     * Queries the created AAS.
     * 
     * @param spec the setup to get the machine AAS from
     * @param machine the test machine as reference
     * @param authDesc the authentication descriptor
     * @throws ExecutionException if operation invocations fail
     * @throws IOException if retrieving the AAS/submodel/elements fails
     */
    private static void queryAas(BasicSetupSpec spec, TestMachine machine, AuthenticationDescriptor authDesc) 
        throws ExecutionException, IOException {
        AasFactory factory = AasFactory.getInstance();
        Registry reg = factory.obtainRegistry(spec);
        Aas aas = reg.retrieveAas(URN_AAS);
        Assert.assertNotNull(aas);
        Assert.assertEquals(NAME_AAS, aas.getIdShort());
        Assert.assertEquals(5, aas.getSubmodelCount());
        Iterator<? extends Submodel> iter = aas.submodels().iterator();
        Submodel subm = null;
        while (iter.hasNext() && (subm == null || !subm.getIdShort().equals(NAME_SUBMODEL))) {
            subm = iter.next();
        } 
        Assert.assertNotNull(subm);
        Assert.assertEquals(5, subm.getPropertiesCount());
        Property lotSize = subm.getProperty(NAME_VAR_LOTSIZE);
        Assert.assertNotNull(lotSize);
        Assert.assertEquals(machine.getLotSize(), lotSize.getValue());
        Property powConsumption = subm.getProperty(NAME_VAR_POWCONSUMPTION);
        Assert.assertNotNull(powConsumption);
        Assert.assertEquals(machine.getPowerConsumption(), powConsumption.getValue());

        assertLangString(subm.getProperty(NAME_VAR_DESCRIPTION1).getValue(), "test@de");
        assertLangString(subm.getProperty(NAME_VAR_DESCRIPTION2).getValue(), "test2@en");

        Assert.assertEquals(3, subm.getOperationsCount());
        Operation op = subm.getOperation(NAME_OP_STARTMACHINE);
        Assert.assertNotNull(op);
        op.invoke();
        if (factory.supportsPropertyFunctions()) {
            Assert.assertEquals(machine.getLotSize(), lotSize.getValue());
            Assert.assertEquals(machine.getPowerConsumption(), powConsumption.getValue());
        }
        op = subm.getOperation(NAME_OP_RECONFIGURE);
        Assert.assertNotNull(op);
        op.invoke(5);
        if (factory.supportsPropertyFunctions()) {
            Assert.assertEquals(machine.getLotSize(), lotSize.getValue());
            Assert.assertEquals(machine.getPowerConsumption(), powConsumption.getValue());
        }
        op = subm.getOperation(NAME_OP_STOPMACHINE);
        Assert.assertNotNull(op);
        op.invoke();
        if (factory.supportsPropertyFunctions()) {
            Assert.assertEquals(machine.getLotSize(), lotSize.getValue());
            Assert.assertEquals(machine.getPowerConsumption(), powConsumption.getValue());
        }
        
        SubmodelElement se = subm.getSubmodelElement(NAME_SUBMODELC_OUTER);
        Assert.assertNotNull(se);
        Assert.assertTrue(se instanceof SubmodelElementCollection);
        SubmodelElementCollection secOuter = subm.getSubmodelElementCollection(NAME_SUBMODELC_OUTER);
        Assert.assertNotNull(secOuter);
        Assert.assertTrue(se == secOuter);
        Assert.assertNotNull(secOuter.getProperty(NAME_VAR_SUBMODELC_OUTER_VAR));
        Assert.assertNotNull(secOuter.getReferenceElement(NAME_VAR_SUBMODELC_OUTER_REF));
        
        SubmodelElementCollection secInner = secOuter.getSubmodelElementCollection(NAME_SUBMODELC_INNER);
        Assert.assertNotNull(secInner);
        Assert.assertNotNull(secInner.getProperty(NAME_VAR_SUBMODELC_INNER_VAR));
        assertDescription(secInner.getProperty(NAME_VAR_SUBMODELC_INNER_VAR), DESCRIPTION);
        Assert.assertNotNull(secInner.getProperty(NAME_VAR_SUBMODELC_INNER_INT));
        Assert.assertEquals(1, secInner.getProperty(NAME_VAR_SUBMODELC_INNER_INT).getValue());
        Assert.assertNotNull(secInner.getReferenceElement(NAME_VAR_SUBMODELC_INNER_REF));

        // the lately added sub-models/elements
        Assert.assertNotNull(aas.getSubmodel("sub_add"));
        Assert.assertNotNull(aas.getSubmodel("sub_add").getSubmodelElementList("sub_coll"));
        Assert.assertNotNull(subm.getSubmodelElementCollection("sub_coll2"));

        // adding on connected models
        Submodel subAdd = aas.createSubmodelBuilder("conn_add", null).build();
        Assert.assertNotNull(aas.getSubmodel("conn_add"));
        subAdd.createSubmodelElementListBuilder("conn_coll").build();
        Assert.assertNotNull(aas.getSubmodel("conn_add").getSubmodelElementList("conn_coll"));
        subm.createSubmodelElementCollectionBuilder("conn_coll2").build();
        Assert.assertNotNull(subm.getSubmodelElementCollection("conn_coll2"));
        SubmodelElementCollectionBuilder cc3 = subm.createSubmodelElementCollectionBuilder("conn_coll3");
        cc3.createSubmodelElementCollectionBuilder("cc3_1").build();
        cc3.build();
        Assert.assertNotNull(subm.getSubmodelElementCollection("conn_coll3"));
        Assert.assertNotNull(subm.getSubmodelElementCollection("conn_coll3").getSubmodelElementCollection("cc3_1"));
        
        subm.getSubmodelElementCollection("conn_coll3").deleteElement("cc3_1");
        Assert.assertNull(subm.getSubmodelElementCollection("conn_coll3").getSubmodelElementCollection("cc3_1"));
        aas.accept(new AasPrintVisitor()); // assert the accepts
        Aas aas2 = reg.retrieveAas(reg.getEndpoint(aas));
        Assert.assertNotNull(aas2);
        Assert.assertEquals(aas2.getIdShort(), aas.getIdShort());
        Assert.assertNull(reg.retrieveAas("http://me.here.de/aas")); // does not exist, shall lead to null
        
        // do authenticated
        queryRbac(aas, true);

        if (authDesc != null) {
            // do unauthenticated
            AuthenticationDescriptor anonDesc = new DelegatingAuthenticationDescriptor(authDesc) {
                
                public IdentityToken getClientToken() {
                    return IdentityToken.IdentityTokenBuilder.newBuilder().build(); // ANONYMOUS
                }
    
            };
            BasicSetupSpec anonSetup = new BasicSetupSpec(spec).setAuthentication(anonDesc);
            Registry anonReg = AasFactory.getInstance().obtainRegistry(anonSetup);
            Aas anonAas = anonReg.retrieveAas(URN_AAS);
            Assert.assertNotNull(anonAas);
            queryRbac(anonAas, false);
        }
    }

    // checkstyle: resume method length check

    /**
     * Queries the RBAC part.
     * 
     * @param aas the AAS, obtained via authenticated or non-authenticated access
     * @throws ExecutionException if operation invocations fail
     * @throws IOException if retrieving the AAS/submodel/elements fails
     */
    private static void queryRbac(Aas aas, boolean authenticated) throws ExecutionException, IOException {
        Submodel sm = aas.getSubmodel(NAME_RBAC_SMOPEN);
        Assert.assertNotNull(sm);

        Property prop = sm.getProperty(NAME_RBAC_PROPOPEN);
        Assert.assertNotNull(prop);
        Assert.assertEquals(10, prop.getValue());
        prop = sm.getProperty(NAME_RBAC_PROPCLOSED); // BaSyx 2 does not check
        Assert.assertNotNull(prop);
        Assert.assertEquals(11, prop.getValue()); // no online access, BaSyx 2 does not check

        assertOpExecution(sm, NAME_RBAC_OPOPEN, authenticated, true);
        assertOpExecution(sm, NAME_RBAC_OPCLOSED, authenticated, false); // not without authentication

        sm = aas.getSubmodel(NAME_RBAC_SMCLOSED); // TODO not without authen

        prop = sm.getProperty(NAME_RBAC_PROPOPEN);
        Assert.assertNotNull(prop);
        Assert.assertEquals(20, prop.getValue());
        
        prop = sm.getProperty(NAME_RBAC_PROPCLOSED); // BaSyx 2 does not check
        Assert.assertNotNull(prop);
        Assert.assertEquals(21, prop.getValue()); // no online access, BaSyx 2 does not check

        assertOpExecution(sm, NAME_RBAC_OPOPEN, authenticated, true);
        assertOpExecution(sm, NAME_RBAC_OPCLOSED, authenticated, false); // not without authentication
    }
    
    /**
     * Assert authenticated/permitted operation executions.
     * 
     * @param sm the containing submodel
     * @param opName the operation name/idShort
     * @param authenticated are we in an authenticated or an anonymous/unauthenticated setting
     * @param expectedSuccess do we expect success
     * @see AasFactory#supportsOperationExecutionAuthorization()
     */
    private static void assertOpExecution(Submodel sm, String opName, boolean authenticated, boolean expectedSuccess) {
        Operation op = sm.getOperation(opName);
        Assert.assertNotNull(op); // BaSyx 2 does not check
        try {
            op.invoke();
            if (!authenticated && !expectedSuccess 
                && AasFactory.getInstance().supportsOperationExecutionAuthorization()) {
                Assert.fail(opName + "shall not be executable due to missing permissions.");
            }
        } catch (ExecutionException e) {
            if (authenticated) {
                Assert.fail(opName + " shall not be failing due to granted permissions.");
            }
            if (expectedSuccess) {
                Assert.fail(opName + " shall not fail.");
            }
        }
    }
    
    /**
     * Asserts the description of {@code prop}.
     * 
     * @param prop the property to assert
     * @param expected the expected lang strings
     */
    private static void assertDescription(Property prop, LangString... expected) {
        Map<String, LangString> value = prop.getDescription();
        if (expected.length == 0) {
            Assert.assertTrue(value == null || value.size() == 0);
        } else {
            for (LangString l : expected) {
                Assert.assertTrue(value.containsKey(l.getLanguage()));
                LangString v = value.get(l.getLanguage());
                Assert.assertEquals(l.getLanguage(), v.getLanguage());
                Assert.assertEquals(l.getDescription(), v.getDescription());
            }
        }
    }

    /**
     * Asserts an iterator contents (just) by counting the number of elements.
     * 
     * @param <T> the element type
     * @param expectedSize the expected size
     * @param iter the iterator to assert
     */
    private static <T> void assertSize(int expectedSize, Iterable<T> iter) {
        Assert.assertEquals(expectedSize, CollectionUtils.toList(iter.iterator()).size());
    }
    
    /**
     * Tests the factory.
     */
    @Test
    public void testFactory() {
        AasFactory factory = AasFactory.getInstance();
        Assert.assertTrue(factory.getName().length() > 0);
        factory.supportsPropertyFunctions(); // may be true, may be false
    }

    /**
     * Tests for illegal short ids. Seems to be valid for all AAS.
     */
    @Test
    public void testIllegalShortId() {
        AasFactory factory = AasFactory.getInstance();
        AasBuilder aasBuilder = factory.createAasBuilder(NAME_AAS, URN_AAS);
        SubmodelBuilder subModelBuilder = aasBuilder.createSubmodelBuilder(NAME_SUBMODEL, null);
        try {
            subModelBuilder.createPropertyBuilder("value").setValue(1).build();
            Assert.fail("No exception due to illegal name");
        } catch (IllegalArgumentException e) {
        }
        try {
            subModelBuilder.createPropertyBuilder("1234").setValue(1).build();
            Assert.fail("No exception due to illegal name");
        } catch (IllegalArgumentException e) {
        }
        try {
            subModelBuilder.createPropertyBuilder("java.lang.String").setValue(1).build();
            Assert.fail("No exception due to illegal name");
        } catch (IllegalArgumentException e) {
        }
    }
    
    /**
     * Tests {@link AasFactory#fixId(String)} for BaSyX.
     */
    @Test
    public void testFixId() {
        AasFactory instance = AasFactory.getInstance();
        Assert.assertNull(instance.fixId(null));
        Assert.assertEquals("", instance.fixId(""));
        Assert.assertEquals("id", instance.fixId("id"));
        Assert.assertEquals("a1id", instance.fixId("1id"));
        Assert.assertEquals("a_id", instance.fixId("a_id"));
        Assert.assertEquals("a_id", instance.fixId("a id"));
        Assert.assertEquals("test_log", instance.fixId("test-log"));

        Assert.assertEquals("de_uni_hildesheim_sse_Test_TEst", instance.fixId("de.uni-hildesheim.sse.Test$TEst"));
        Assert.assertEquals("a1de_uni_hildesheim_sse_Test", instance.fixId("1de.uni-hildesheim.sse.Test"));
        Assert.assertEquals("a1de_uni_hildesheim_sse_Test_TEst", instance.fixId("1de.uni-hildesheim.sse.Test$TEst"));
        Assert.assertEquals("jenkins_2_localhost", instance.fixId("jenkins-2@localhost"));
    }

}
