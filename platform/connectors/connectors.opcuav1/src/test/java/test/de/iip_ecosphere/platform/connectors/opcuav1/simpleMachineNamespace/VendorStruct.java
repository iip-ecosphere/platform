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

package test.de.iip_ecosphere.platform.connectors.opcuav1.simpleMachineNamespace;

import org.eclipse.milo.opcua.stack.core.serialization.SerializationContext;
import org.eclipse.milo.opcua.stack.core.serialization.UaDecoder;
import org.eclipse.milo.opcua.stack.core.serialization.UaEncoder;
import org.eclipse.milo.opcua.stack.core.serialization.UaStructure;
import org.eclipse.milo.opcua.stack.core.serialization.codecs.GenericDataTypeCodec;
import org.eclipse.milo.opcua.stack.core.types.builtin.ExpandedNodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UShort;
import org.eclipse.milo.opcua.stack.core.types.enumerated.StructureType;
import org.eclipse.milo.opcua.stack.core.types.structured.StructureDefinition;
import org.eclipse.milo.opcua.stack.core.types.structured.StructureDescription;
import org.eclipse.milo.opcua.stack.core.types.structured.StructureField;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import test.de.iip_ecosphere.platform.connectors.opcuav1.DataTypeDictionaryManager;

import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;

import org.eclipse.milo.opcua.sdk.core.ValueRanks;
import org.eclipse.milo.opcua.sdk.server.OpcUaServer;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.UaSerializationException;

/**
 * Implements a user-defined type.
 * 
 * @author Holger Eichelberger, SSE
 */
public class VendorStruct implements UaStructure {

    public static final String TYPE_NAME = "VendorStructType";
    public static final String TYPE_QNAME = "DataType." + TYPE_NAME;
    
    public static final ExpandedNodeId TYPE_ID = ExpandedNodeId.parse(String.format(
        "nsu=%s;s=%s",
        Namespace.NAMESPACE_URI,
        TYPE_QNAME
    ));

    public static final ExpandedNodeId BINARY_ENCODING_ID = ExpandedNodeId.parse(String.format(
        "nsu=%s;s=%s",
        Namespace.NAMESPACE_URI,
        TYPE_QNAME + ".BinaryEncoding"
    ));

    private final String vendor;
    private final UInteger year;
    private final boolean opcUa;

    /**
     * Creates a struct instance.
     */
    public VendorStruct() {
        this(null, 0, false);
    }

    /**
     * Creates a struct instance with values.
     * 
     * @param vendor the vendor name
     * @param year the production year
     * @param opcUa is OPC UA compliant
     */
    public VendorStruct(String vendor, int year, boolean opcUa) {
        this(vendor, uint(year), opcUa);
    }
    
    /**
     * Creates a struct instance with values.
     * 
     * @param vendor the vendor name
     * @param year the production year
     * @param opcUa is OPC UA compliant
     */
    private VendorStruct(String vendor, UInteger year, boolean opcUa) {
        this.vendor = vendor;
        this.year = year;
        this.opcUa = opcUa;
    }

    /**
     * Returns the vendor name.
     * 
     * @return the vendor name
     */
    public String getVendor() {
        return vendor;
    }

    /**
     * Returns the production year.
     * 
     * @return the production year
     */
    public int getYear() {
        return year.intValue();
    }

    /**
     * Returns whether the machine is OPC UA compliant.
     * 
     * @return whether the machine is OPC UA compliant
     */
    public boolean isOpcUa() {
        return opcUa;
    }

    @Override
    public ExpandedNodeId getTypeId() {
        return TYPE_ID;
    }

    @Override
    public ExpandedNodeId getBinaryEncodingId() {
        return BINARY_ENCODING_ID;
    }

    @Override
    public ExpandedNodeId getXmlEncodingId() {
        // XML encoding not supported
        return ExpandedNodeId.NULL_VALUE;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null || getClass() != obj.getClass()) {
            return false;
        } else {
            VendorStruct that = (VendorStruct) obj;
            return opcUa == that.opcUa 
                && Objects.equal(vendor, that.vendor) 
                && Objects.equal(year, that.year);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(vendor, year, opcUa);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("vendor", vendor)
            .add("year", year)
            .add("opcUa", opcUa)
            .toString();
    }

    public static class Codec extends GenericDataTypeCodec<VendorStruct> {
        @Override
        public Class<VendorStruct> getType() {
            return VendorStruct.class;
        }

        @Override
        public VendorStruct decode(SerializationContext context,
            UaDecoder decoder) throws UaSerializationException {

            String vendor = decoder.readString("vendor");
            UInteger year = decoder.readUInt32("year");
            boolean opcUa = decoder.readBoolean("opcUa");

            return new VendorStruct(vendor, year, opcUa);
        }

        @Override
        public void encode(SerializationContext context,
            UaEncoder encoder, VendorStruct value) throws UaSerializationException {

            encoder.writeString("vendor", value.vendor);
            encoder.writeUInt32("year", value.year);
            encoder.writeBoolean("opcUa", value.opcUa);
        }
    }

    // checkstyle: stop exception type check

    /**
     * Registers the type in the given OPC UA server.
     * 
     * @param server the server instance
     * @param namespaceIndex the namespace index
     * @param dictionaryManager the dictionary manager
     * @throws Exception in case of a problem (called methods also just throw an Exception)
     */
    public static void registerType(OpcUaServer server, UShort namespaceIndex, 
        DataTypeDictionaryManager dictionaryManager) throws Exception {
        // Get the NodeId for the DataType and encoding Nodes.

        NodeId dataTypeId = VendorStruct.TYPE_ID.toNodeIdOrThrow(server.getNamespaceTable());

        NodeId binaryEncodingId = VendorStruct.BINARY_ENCODING_ID.toNodeIdOrThrow(server.getNamespaceTable());

        // At a minimum, custom types must have their codec registered.
        // If clients don't need to dynamically discover types and will
        // register the codecs on their own then this is all that is
        // necessary.
        // The dictionary manager will add a corresponding DataType Node to
        // the AddressSpace.

        dictionaryManager.registerStructureCodec(
            new VendorStruct.Codec().asBinaryCodec(),
            TYPE_NAME,
            dataTypeId,
            binaryEncodingId
        );

        // If the custom type also needs to be discoverable by clients then it
        // needs an entry in a DataTypeDictionary that can be read by those
        // clients. We describe the type using StructureDefinition or
        // EnumDefinition and register it with the dictionary manager.
        // The dictionary manager will add all the necessary nodes to the
        // AddressSpace and generate the required dictionary bsd.xml file.

        StructureField[] fields = new StructureField[]{
            new StructureField(
                "vendor",
                LocalizedText.NULL_VALUE,
                Identifiers.String,
                ValueRanks.Scalar,
                null,
                server.getConfig().getLimits().getMaxStringLength(),
                false
            ),
            new StructureField(
                "year",
                LocalizedText.NULL_VALUE,
                Identifiers.UInt32,
                ValueRanks.Scalar,
                null,
                uint(0),
                false
            ),
            new StructureField(
                "opcUa",
                LocalizedText.NULL_VALUE,
                Identifiers.Boolean,
                ValueRanks.Scalar,
                null,
                uint(0),
                false
            )
        };

        StructureDefinition definition = new StructureDefinition(
            binaryEncodingId,
            Identifiers.Structure,
            StructureType.Structure,
            fields
        );

        StructureDescription description = new StructureDescription(
            dataTypeId,
            new QualifiedName(namespaceIndex, TYPE_NAME),
            definition
        );

        dictionaryManager.registerStructureDescription(description, binaryEncodingId);
    }

    // checkstyle: resume exception type check

}
