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

package de.iip_ecosphere.platform.configuration.opcua.parser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.configuration.opcua.data.BaseType;
import de.iip_ecosphere.platform.configuration.opcua.data.RootMethodType;
import de.iip_ecosphere.platform.configuration.opcua.data.RootObjectType;
import de.iip_ecosphere.platform.configuration.opcua.data.RootVariableType;

/**
 * Generates the IVML model.
 * 
 * @author Jan-Hendrik Cepok, SSE
 */
public class Generator {

    /**
     * Generates the IVML model in the given {@code fileName}.
     * 
     * @param fileName  the file name for the IVML model
     * @param ivmlFile  the output file
     * @param hierarchy the parsed element hierarchy
     */
    public static void generateIVMLModel(String fileName, File ivmlFile, ArrayList<BaseType> hierarchy) {
        String ivmlHeader = "project Opc" + fileName + " {\n\n" + "\timport IIPEcosphere;\n" + "\timport DataTypes;\n"
                + "\timport OpcUaDataTypes;\n\n"
                + "\tannotate BindingTime bindingTime = BindingTime::compile to .;\n\n";

        String ivmlEnding = "\tfreeze {\n" + "\t\t.; // every variable declared in this project\n"
                + "\t} but (f|f.bindingTime >= BindingTime.compile);\n\n" + "}";

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(ivmlFile));
            writer.write(ivmlHeader);
            for (BaseType b : hierarchy) {
                writer.write(b.toString());
            }
            writer.write(ivmlEnding);
            writer.close();
        } catch (IOException ioe) {
            LoggerFactory.getLogger(DomParser.class).error(ioe.getMessage());
        }
    }
    
    // checkstyle: stop method length check

    /**
     * Generates the respective connector settings for the given {@code fileName}.
     * 
     * @param fileName  the file name for the IVML model
     * @param hierarchy the parsed element hierarchy
     * @param folder target folder
     */
    public static void generateVDWConnectorSettings(String fileName, ArrayList<BaseType> hierarchy, String folder) {
        File vdwSpecificConfig = new File(folder, "VDW_" + fileName + ".ivml");
        File vdwConfig = new File(folder, "VDW.ivml");
        String configHeader = "project VDW {\n\n" + "    import IIPEcosphere;\n" + "    import OpcUaDataTypes;\n"
                + "    import Opc" + fileName + ";\n\n"
                + "    annotate BindingTime bindingTime = BindingTime::compile to .;\n\n"
                + "    // ------------ component setup ------------------\n\n" + "    serializer = Serializer::Json;\n"
                + "    // serviceManager, containerManager are already defined    \n\n" + "    aasServer = {\n"
                + "        schema = AasSchema::HTTP,\n" + "        port = 9001,\n" + "        host = \"127.0.0.1\"\n"
                + "    };\n" + "    aasRegistryServer = {\n" + "        schema = AasSchema::HTTP,\n"
                + "        port = 9002,\n" + "        host = \"127.0.0.1\",\n" + "        path = \"registry\"\n"
                + "    };\n" + "    aasImplServer = {\n" + "        port = 9003\n" + "    };\n"
                + "    aasProtocol = AasProtocolVabTcp{};\n\n"
                + "    // ------------------ transport --------------------------\n\n"
                + "    transportProtocol = TransportProtocolMQTTv5 {\n" + "        port = 8883\n" + "    };\n\n"
                + "    serviceProtocol = ServiceProtocolMQTTv5 {};\n\n"
                + "    // ------------------ resources/devicemgt ----------------\n\n"
                + "    deviceRegistry = BasicDeviceRegistry {\n" + "    };\n\n"
                + "    deviceMgtStorage = S3MockDeviceMgtStorage {\n" + "        host = \"localhost\",\n"
                + "        port = 8884,\n" + "        region = \"local\"\n" + "    };\n\n"
                + "    // just for testing, this instantiates app rather than platform\n"
                + "    deviceMgtStorageServer = S3MockDeviceMgtStorageServer{};\n\n"
                + "    // ------------ data types ------------------\n\n" + "    RecordType opcIn = {\n"
                + "        name = \"OpcIn\",\n" + "        fields = {\n" + "        }\n" + "    };    \n"
                + "    RecordType opcOut = {\n" + "        path = \"PLACEHOLDER\",\n" + "        name = \"OpcOut\",\n\n"
                + "        fields = {\n";

        String configEnding = "        \n}\n" + "    };\n\n"
                + "    // ------------ individual, reusable services ------------------\n\n"
                + "    OpcUaV1Connector myOpcUaConn = {\n" + "        id = \"myOpcConn\",\n"
                + "        name = \"myOpcConn example\",\n" + "        description = \"\",\n"
                + "        ver = \"0.1.0\",\n" + "        host = \"opcua.umati.app\",\n"
                + "        port = 4840, // default localhost\n" + "        \n"
                + "        input = {{type=refBy(opcIn)}},\n" + "        output = {{type=refBy(opcOut)}},\n"
                + "        inInterface = refBy(opcIn), \n" + "        outInterface = refBy(opcOut)\n"
                + "        /*operations = {\n" + "          FieldAssignmentOperation{field=myConnPltfIn.fields[1], \n"
                + "            operation=AddDataTranslationOperation{\n" + "                arguments={\n"
                + "                    DataFieldAccess{field=myConnMachineOut.fields[0]},\n"
                + "                    IntegerConstantDataOperation{value=100}}\n" + "            }\n" + "          }\n"
                + "        }*/\n" + "    };\n\n"
                + "    // not really needed except for populating the interfaces package\n"
                + "    Service myReceiverService = JavaService {\n" + "        id = \"OpcReceiver\",\n"
                + "        name = \"OPC Receiver\",\n" + "        description = \"\",\n" + "        ver = \"0.1.0\",\n"
                + "        deployable = true,\n" + "        class = \"ReceiverImpl\",\n" + "        artifact = \"\",\n"
                + "        kind = ServiceKind::SINK_SERVICE,\n" + "        input = {{type=refBy(opcOut)}}\n"
                + "    };\n\n" + "    // --------------------- monitoring ---------------------------\n\n"
                + "    // current default: no monitoring configured\n\n"
                + "    // ------------------------- UI -------------------------------\n\n"
                + "    // current default: no UI configured\n\n"
                + "    // ------------ application and service nets ------------------\n\n"
                + "    Application myApp = {\n" + "        id = \"VdwOpcApp\",\n"
                + "        name = \"Simple VDW OPC Demo App\",\n" + "        ver = \"0.1.0\",\n"
                + "        description = \"\",\n" + "        services = {refBy(myMesh)}        \n" + "    };\n\n"
                + "    ServiceMesh myMesh = {\n" + "        description = \"VDW Service Net\",\n"
                + "        sources = {refBy(mqttOpcSource)}\n" + "    };\n\n" + "    MeshSource mqttOpcSource = {\n"
                + "       impl = refBy(myOpcUaConn),\n" + "       next = {refBy(myConnMySourceMyReceiver)}\n"
                + "    };\n\n"
                + "    // needed to form a graph\n\n"
                + "    MeshConnector myConnMySourceMyReceiver = {\n" + "        name = \"Source->Receiver\",\n"
                + "        next = refBy(myReceiver)\n" + "    };\n\n" + "    MeshSink myReceiver = {\n"
                + "        impl = refBy(myReceiverService)\n" + "    };\n\n"
                + "    // ---------- generation setup ------------\n\n" + "    sharedInterfaces = true;\n"
                + "    sharedArtifact = \"de.iip-ecosphere.platform:apps.VdwOpcAppInterfaces:\" + iipVer;\n\n"
                + "    // ------------ freezing ------------------\n\n" + "    freeze {\n" + "        aas;\n"
                + "        aasServer;\n" + "        aasRegistryServer;\n" + "        aasImplServer;\n"
                + "        aasPersistency;\n" + "        aasProtocol;\n" + "        serializer;\n"
                + "        transportProtocol;\n" + "        serviceManager;\n" + "        serviceProtocol;\n"
                + "        containerManager;\n" + "        deviceMgtStorageServer;\n" + "        deviceMgtStorage;\n"
                + "        deviceRegistry;\n" + "        javaModuleOpts;\n" + "        javaOpts;\n"
                + "        pidDir;\n" + "        sharedInterfaces;\n" + "        sharedArtifact;\n"
                + "        platformMonitoring;\n" + "        managementUi;\n" + "        artifactsFolder;\n"
                + "        artifactsUriPrefix;\n" + "        Opc" + fileName + "; // now freeze also cachingTime\n"
                + "        .; // every variable declared in this project\n"
                + "    } but (f|f.bindingTime >= BindingTime.runtimeMon);\n\n}";

        String connectorFields = "";
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(vdwConfig));
            StringBuilder builder = new StringBuilder();
            writer.write(configHeader);
            for (BaseType b : hierarchy) {
                if (b instanceof RootObjectType || b instanceof RootVariableType || b instanceof RootMethodType) {
                    connectorFields = "            Field {\n" + "                name = \"" + b.getVarName() + "\",\n"
                            + "                type = refBy(" + b.getVarName() + "),\n"
                            + "                cachingTime = CACHE_ALWAYS\n" + "            },";
                    builder.append(connectorFields);
                }
            }
            connectorFields = builder.toString();
            if (connectorFields.length() > 0) {
                connectorFields = connectorFields.substring(0, connectorFields.length() - 1);
            }
            writer.write(connectorFields);
            writer.write(configEnding);
            writer.close();
            writer = new BufferedWriter(new FileWriter(vdwSpecificConfig));
            writer.write(configHeader);
            writer.write(connectorFields);
            writer.write(configEnding);
            writer.close();
        } catch (IOException ioe) {
            LoggerFactory.getLogger(DomParser.class).error(ioe.getMessage());
        }
    }

    // checkstyle: resume method length check

}
