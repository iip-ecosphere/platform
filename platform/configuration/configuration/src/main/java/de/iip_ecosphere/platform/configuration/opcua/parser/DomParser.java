package de.iip_ecosphere.platform.configuration.opcua.parser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

//import de.iip_ecosphere.platform.configuration.opcua.data.*;
import de.iip_ecosphere.platform.configuration.opcua.data.*;

/**
 * Denotes the OPC UA element types.
 * 
 * @author Jan-Hendrick Cepok, SSE
 */
enum ElementType {
    OBJECTTYPE, OBJECT, SUBOBJECT, VARIABLE, FIELD, ENUM, DATATYPE, VARIABLETYPE
}

/**
 * XML parser for OPC UA companion spec files.
 * 
 * @author Jan-Hendrick Cepok, SSE
 */
public class DomParser {

    private static boolean verboseDefault = true;
    private static String mainOutFolder = "src/test/easy";
    private static final Set<String> IDENTIFY_FIELDS_PERMITTED_REFERENCE_TYPE;

    static {
        HashSet<String> tmp = new HashSet<>();
        tmp.add("HasComponent");
        tmp.add("HasOrderedComponent");
        tmp.add("HasProperty");
        tmp.add("FromState");
        tmp.add("ToState");
        IDENTIFY_FIELDS_PERMITTED_REFERENCE_TYPE = Collections.unmodifiableSet(tmp);
    }

    // private NodeList requiredModels;
    private Document[] documents;
    private NodeList objectTypeList;
    private NodeList objectList;
    private NodeList variableList;
    private NodeList dataTypeList;
    private NodeList variableTypeList;
    private NodeList aliasList;
    private ArrayList<BaseType> hierarchy;
    private boolean verbose = verboseDefault;

    // checkstyle: stop parameter number check

    /**
     * Creates a DOM parser/translator.
     * 
     * @param documents        returns the documents to process
     * @param objectTypeList   the already parsed object type list
     * @param objectList       the already parsed object list
     * @param variableList     the already parsed variable list
     * @param dataTypeList     the already parsed data type list
     * @param variableTypeList the already parsed variable type list
     * @param aliasList        the already parsed alias list
     * @param hierarchy        the base type hierarchy
     */
    private DomParser(Document[] documents, NodeList objectTypeList, NodeList objectList, NodeList variableList,
            NodeList dataTypeList, NodeList variableTypeList, NodeList aliasList, ArrayList<BaseType> hierarchy) {
        // this.requiredModels = requiredModels;
        this.documents = documents;
        this.objectTypeList = objectTypeList;
        this.objectList = objectList;
        this.variableList = variableList;
        this.dataTypeList = dataTypeList;
        this.variableTypeList = variableTypeList;
        this.aliasList = aliasList;
        this.hierarchy = hierarchy;
    }

    /**
     * Changes the default verbose mode used when instantiating a parser.
     * 
     * @param verbose verbose or non verbose mode
     */
    public static void setDefaultVerbose(boolean verbose) {
        verboseDefault = verbose;
    }
    
    /**
     * Sets the name of the output folder to be used in {@link #main(String[])}.
     * 
     * @param folder the folder name
     */
    public static void setMainOutFolder(String folder) {
        mainOutFolder = folder;
    }

    // checkstyle: resume parameter number check

//  public NodeList getRequiredModels() {
//      return requiredModels;
//  }

    /**
     * Searches for a field type variable name.
     * 
     * @param uaElement the UA element delivering the node Id to search for
     * @param hierarchy the type hierarchy to search within
     * @return the variable name of the found field type
     */
    public static String searchVarName(ObjectType uaElement, ArrayList<BaseType> hierarchy) {
        String varName = "";
        for (BaseType o : hierarchy) {
            if (o instanceof ObjectType) {
                ArrayList<FieldType> fields = ((ObjectType) o).getFields();
                if (!fields.isEmpty()) {
                    for (FieldType f : fields) {
                        if (!(f instanceof VariableType)) {
                            if (uaElement.getNodeId().equals(f.getNodeId())) {
                                varName = f.getDataType();
                                break;
                            }
                        }
                    }
                }
            }
        }
        return varName;
    }

    // checkstyle: stop method length check

    /**
     * Turns OPC UA type names to IIP-Ecosphere meta model type names.
     * 
     * @param dataType the data type
     * @return the translated data type
     */
    // TODO REMOVE nur default case ist nötig
    public String changeVariableDataTypes(String dataType) {
        boolean modelTypes = false;
        switch (dataType) {
        case "SByte":
            dataType = "SByteType";
            modelTypes = true;
            break;
        case "Boolean":
            dataType = "BooleanType";
            modelTypes = true;
            break;
        case "Byte":
            dataType = "ByteType";
            modelTypes = true;
            break;
        case "ByteString":
            dataType = "ByteStringType";
            modelTypes = true;
            break;
        case "Integer":
            dataType = "IntegerType";
            modelTypes = true;
            break;
        case "Int16":
            dataType = "Integer16Type";
            modelTypes = true;
            break;
        case "UInt16":
            dataType = "UnsignedInteger16Type";
            modelTypes = true;
            break;
        case "Int32":
            dataType = "Integer32Type";
            modelTypes = true;
            break;
        case "UInt32":
            dataType = "UnsignedInteger32Type";
            modelTypes = true;
            break;
        case "Int64":
            dataType = "Integer64Type";
            modelTypes = true;
            break;
        case "UInt64":
            dataType = "UnsignedInteger64Type";
            modelTypes = true;
            break;
        case "Float":
            dataType = "FloatType";
            modelTypes = true;
            break;
        case "Double":
            dataType = "DoubleType";
            modelTypes = true;
            break;
        case "String":
            dataType = "StringType";
            modelTypes = true;
            break;
        case "DateTime":
            dataType = "DateTimeType";
            modelTypes = true;
            break;
        case "Guid":
            dataType = "opcGuidType";
            break;
        case "IdType":
            dataType = "opcIdType";
            break;
        case "LocalizedText":
            dataType = "opcLocalizedTextType";
            break;
        case "UInteger":
            dataType = "opcUnsignedIntegerType";
            break;
        case "Number":
            dataType = "opcNumberType";
            break;
        case "NumericRange":
            dataType = "opcNumericRangeType";
            break;
        case "Range":
            dataType = "opcRangeType";
            break;
        case "EUInformation":
            dataType = "opcEUInformationType";
            break;
        case "UtcTime":
            dataType = "opcUtcTimeType";
            break;
        case "Argument":
            dataType = "opcArgumentType";
            break;
        case "Structure":
            dataType = "opcStructureType";
            break;
        case "DecimalString":
            dataType = "opcDecimalStringType";
            break;
        case "DateString":
            dataType = "opcDateStringType";
            break;
        // TODO zun opcDurationType ändern sobald verfügbar
        case "Duration":
            dataType = "opcDurationStringType";
            break;
        case "DurationString":
            dataType = "opcDurationStringType";
            break;
        case "NormalizedString":
            dataType = "opcNormalizedStringType";
            break;
        case "TimeString":
            dataType = "opcTimeStringType";
            break;
        case "":
            dataType = "opcUnknownDataType";
            break;
        default:
//          if(check) {
//              if(!dataType.contains("i=")) {
//                  dataType = "opcExternalType";
//              }
//          } else {
            if (!dataType.contains("opc") && !modelTypes) {
                dataType = checkForExternDataType(dataType);
                dataType = "opc" + dataType + "Type";
            }
            // }
            // dataType = "opcExternType";
            // dataType = "opc" + dataType + "Type";
            break;
        }
        return dataType;
    }

    // checkstyle: resume method length check

    /**
     * Adapts the OPC UA data types to IIP-Ecosphere meta model type names.
     * 
     * @param uaElement the UA element to adapt the types for
     */
    public void adaptDatatypesToModel(ObjectType uaElement) {
        ArrayList<FieldType> fields = uaElement.getFields();
        for (FieldType f : fields) {
            if (f instanceof VariableType) {
                nop();
//                f.setDataType(changeVariableDataTypes(f.getDataType()));
//                fields.set(fields.indexOf(f), f);
            } else if (f instanceof FieldType) {
                f.setDataType(BaseType.validateVarName(uaElement.getVarName() + f.getDisplayname()));
                fields.set(fields.indexOf(f), f);
            }
        }
    }
    
    /**
     * Does nothing, just allows for code convention compliance while bugfixing.
     */
    private static void nop() {
    }

//  public static String checkString(String input) {
//    return relatedElement;
//  }

    /**
     * Checks the relations and returns a node with NodeId {@code currentNodeId}.
     * 
     * @param currentNodeId the node id to search for
     * @param nodes         the nodes to search
     * @return the found element
     */
    public static Element checkRelation(String currentNodeId, NodeList nodes) {

        Element relatedElement = null;

        for (int i = 0; i < nodes.getLength(); i++) {
            Element node = getNextNodeElement(nodes, i);
            String nodeId = node.getAttribute("NodeId");
            if (currentNodeId.equals(nodeId)) {
                relatedElement = node;
                i = nodes.getLength();
            }
        }
        return relatedElement;
    }

    /**
     * Returns the next node element.
     * 
     * @param nodes    the nodes to search for
     * @param iterator the 0-based index into nodes
     * @return the next node element
     */
    public static Element getNextNodeElement(NodeList nodes, int iterator) {

        Node n = nodes.item(iterator);
        Element node = null;

        if (n.getNodeType() == Node.ELEMENT_NODE) {
            node = (Element) n;
        }
        return node;
    }

    /**
     * Retrieves the root parent.
     * 
     * @param parentNodeId the parent node id
     * @return the root parent
     */
    public String retrieveRootParent(String parentNodeId) {
        String rootParent = "";
        Element element = checkRelation(parentNodeId, objectTypeList);
        NodeList childNodeList = element.getChildNodes();

        for (int j = 0; j < childNodeList.getLength(); j++) {
            Element childNode = getNextNodeElement(childNodeList, j);
            if (childNode != null) {
                if (childNode.getTagName() == "DisplayName") {
                    rootParent = BaseType.validateVarName("opc" + childNode.getTextContent());
                    break;
                }
            }
        }
        return rootParent;
    }

    /**
     * Checks for an extern data type.
     * 
     * @param dataType the data type to look for
     * @return the data type
     */
    public String checkForExternDataType(String dataType) {
        if (dataType.contains("ns=") || dataType.contains("i=")) {
            dataType = retrieveAttributesForExternDataType(dataType);
        }
        for (int i = 0; i < aliasList.getLength(); i++) {
            Element alias = getNextNodeElement(aliasList, i);
            NodeList childNodeList = alias.getChildNodes();
            for (int j = 0; j < childNodeList.getLength(); j++) {
                Element childNode = getNextNodeElement(childNodeList, j);
                if (childNode != null) {
                    if (childNode.getAttribute("Alias").equals(dataType)) {
                        String nodeId = childNode.getTextContent();
                        if (nodeId.contains("i=")) {
                            if (!nodeId.contains("ns=1")) {
                                dataType = retrieveAttributesForExternDataType(nodeId);
                            }
                        }
                        break;
                    }
                }
            }
        }
        return dataType;
    }

    /**
     * Retrieves attributes from {@link #documents} for extern data types.
     * 
     * @param nodeId the node id of the node to retrieve the attributes for
     * @return the data type
     */
    private String retrieveAttributesForExternDataType(String nodeId) {
        String dataType = "";
        for (int k = 0; k < documents.length; k++) {
            NodeList typeList = documents[k].getElementsByTagName("UADataType");

            // TODO NOCHMAL MIT WEIHENSTEPHAN PRÜFEN WEGEN NS=4
            if (nodeId.contains("ns=")) {
                nodeId = nodeId.substring(0, nodeId.indexOf("=") + 1) + 1
                        + nodeId.substring(nodeId.indexOf(";"), nodeId.length());
            }
            Element element = checkRelation(nodeId, typeList);
            if (element != null) {

                NodeList childNodeList = element.getChildNodes();

                for (int j = 0; j < childNodeList.getLength(); j++) {
                    Element childNode = getNextNodeElement(childNodeList, j);
                    if (childNode != null && childNode.getTagName() != "References") {
                        if (childNode.getTagName() == "DisplayName") {
                            dataType = childNode.getTextContent().replaceAll("[“”\"_]", "");
                            break;
                        }
                    }
                }
                retrieveAttributes(element, null, ElementType.DATATYPE);
                break;
            }
        }
        return dataType;
    }

    /**
     * Identifies a specific reference.
     * 
     * @param reference the reference type to look for
     * @param node      the node to analyze the children
     * @param type      the type to look for
     * @return the reference value
     */
    public String identifySpecificReference(String reference, Node node, ElementType type) {
        NodeList references = node.getChildNodes();
        for (int k = 0; k < references.getLength(); k++) {
            Element refNode = getNextNodeElement(references, k);
            if (refNode != null && refNode.getAttribute("ReferenceType").equals(reference)) {

                // get extern List
                String refId = refNode.getTextContent();

                if (refId.contains("ns=1;") || !refId.contains("ns=")) {
                    TypeListAndType r = getTypeListAndTypeRootNs(refId, reference, type);
                    type = r.type;
                    Element refElement = checkRelation(refId, r.typeList);
                    if (refElement != null) {
                        // create Attribute, LÖSUNG FÜR REFERENCE INIT ÜBERLEGEN
                        DescriptionOrDocumentation d = getDescriptionOrDocumentation(reference, refElement);
                        reference = d.reference;
                        if (type != ElementType.OBJECT && type != ElementType.SUBOBJECT) {
                            createElement(type, refElement, d.displayName, d.description, d.documentation, null, null,
                                    null, null, null, false);
                        }
                    }
                } else {
                    // other models
                    String newRefId = refId.substring(0, refId.indexOf("=") + 1) + 1
                            + refId.substring(refId.indexOf(";"), refId.length());
                    refId = newRefId;
                    for (int i = 1; i < documents.length; i++) {
                        NodeList typeList = null;
                        if (type == ElementType.OBJECT || type == ElementType.SUBOBJECT) {
                            typeList = documents[i].getElementsByTagName("UAObjectType");
                            type = ElementType.OBJECTTYPE;
                        } else if (type == ElementType.VARIABLE) {
                            typeList = documents[i].getElementsByTagName("UAVariableType");
                            type = ElementType.VARIABLETYPE;
                        }
                        Element refElement = checkRelation(refId, typeList);
                        if (refElement != null) {
                            DescriptionOrDocumentation d = getDescriptionOrDocumentation(reference, refElement);
                            // create Attribute
                            reference = d.reference;
                            createElement(type, refElement, d.displayName, d.description, d.documentation, null, null,
                                    null, null, null, false);
                            break;
                        } else if (type == ElementType.OBJECTTYPE) {
                            type = ElementType.OBJECT;
                        } else if (type == ElementType.VARIABLETYPE) {
                            type = ElementType.VARIABLE;
                        }
                    }

                }
                // TODO METHODENAUFRUF ERSTELLEN
                break;
            }
        }
        return reference;
    }

    /**
     * Result for
     * {@link DomParser#getTypeListAndTypeRootNs(String, String, ElementType)}.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class TypeListAndType {
        private NodeList typeList;
        private ElementType type;
    }

    /**
     * Extracts the type list and type for a given root namespace {@code refId}.
     * 
     * @param refId     the ref id
     * @param reference the reference type
     * @param type      the actual element type
     * @return the extracted result
     */
    private TypeListAndType getTypeListAndTypeRootNs(String refId, String reference, ElementType type) {
        TypeListAndType result = new TypeListAndType();
        result.type = type;
        if (refId.contains("ns=1;")) {
            // comp spec
            if (type == ElementType.OBJECT || type == ElementType.SUBOBJECT) {
                result.typeList = objectTypeList;
            } else if (type == ElementType.VARIABLE) {
                result.typeList = variableTypeList;
                result.type = ElementType.VARIABLETYPE;
            }
        } else if (!refId.contains("ns=")) {
            // core
            if (type == ElementType.OBJECT || type == ElementType.SUBOBJECT) {
                if (reference.equals("HasModellingRule")) {
                    result.typeList = documents[0].getElementsByTagName("UAObject");
                } else {
                    result.typeList = documents[0].getElementsByTagName("UAObjectType");
                    result.type = ElementType.OBJECTTYPE;
                }
            } else if (type == ElementType.VARIABLE) {
                if (reference.equals("HasModellingRule")) {
                    result.typeList = documents[0].getElementsByTagName("UAObject");
                    result.type = ElementType.OBJECT;
                } else {
                    result.typeList = documents[0].getElementsByTagName("UAVariableType");
                    result.type = ElementType.VARIABLETYPE;
                }
            }
        }
        return result;
    }

    /**
     * Results instance for
     * {@link DomParser#getDescriptionOrDocumentation(String, Element)}.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class DescriptionOrDocumentation {

        private String reference;
        private String displayName = "";
        private String description = "";
        private String documentation = "";

    }

    /**
     * Extracts information about description or documentation from
     * {@code refElement}.
     * 
     * @param reference  the actual reference being processed
     * @param refElement the reference element to take the data from
     * @return result instance carrying the extracted information
     */
    private DescriptionOrDocumentation getDescriptionOrDocumentation(String reference, Element refElement) {
        DescriptionOrDocumentation result = new DescriptionOrDocumentation();
        result.reference = reference;
        NodeList childNodeList = refElement.getChildNodes();
        for (int j = 0; j < childNodeList.getLength(); j++) {
            Element childNode = getNextNodeElement(childNodeList, j);
            if (childNode != null && childNode.getTagName() != "References") {
                if (childNode.getTagName() == "DisplayName") {
                    result.reference = childNode.getTextContent().replaceAll("[“”\"]", "");
                    result.displayName = result.reference;
                } else if (childNode.getTagName() == "Description") {
                    result.description = childNode.getTextContent();
                } else if (childNode.getTagName() == "Documentation") {
                    result.documentation = childNode.getTextContent();
                }
            }
        }
        return result;
    }

    /**
     * Identifies the fields of {@code childNode}.
     * 
     * @param childNode the child node to analyze
     * @return the identified fields
     */
    public ArrayList<FieldType> identifyFields(Node childNode) {
        ArrayList<FieldType> fields = new ArrayList<FieldType>();
        NodeList references = childNode.getChildNodes();

        for (int k = 0; k < references.getLength(); k++) {
            Element refNode = getNextNodeElement(references, k);
            // TODO REFERENCES HasOrderedComponent, ToState, FromState ergÃ¤nzen --> weitere
            // prÃ¼fen
            if (refNode != null
                    && IDENTIFY_FIELDS_PERMITTED_REFERENCE_TYPE.contains(refNode.getAttribute("ReferenceType"))) {
                String refId = refNode.getTextContent();
                Element refElement = checkRelation(refId, variableList);
                if (refElement != null) {
                    retrieveAttributesForRefElement(fields, refId, refElement, ElementType.VARIABLE);
                } else {
                    refElement = checkRelation(refId, objectList);
                    if (refElement != null && !(refNode.getAttribute("IsForward").equals("false"))) {
                        retrieveAttributesForRefElement(fields, refId, refElement, ElementType.FIELD);
                    }
                }
            }
        }
        return fields;
    }

    /**
     * Retrieves attribute for a reference element.
     * 
     * @param fields      the fields to retrieve the attributes for
     * @param refId       the ref id
     * @param refElement  the ref element
     * @param elementType the elementType
     */
    private void retrieveAttributesForRefElement(ArrayList<FieldType> fields, String refId, Element refElement,
            ElementType elementType) {
        if (fields.size() == 0) {
            retrieveAttributes(refElement, fields, elementType);
        } else {
            boolean retrieve = true;
            for (FieldType f : fields) {
                if (f.getNodeId().equals(refId)) {
                    retrieve = false;
                }
            }
            if (retrieve) {
                retrieveAttributes(refElement, fields, elementType);
            }
        }
    }

    /**
     * Retrieves the root element.
     * 
     * @param object the element to start with
     */
    public void retrieveRootElement(Element object) {
        retrieveAttributes(object, null, ElementType.OBJECT);
    }

    /**
     * Retrieves the related sub elements.
     * 
     * @param subElements the sub elements to analyze
     */
    public void retrieveRelatedSubElements(ArrayList<FieldType> subElements) {

        ArrayList<FieldType> fields = new ArrayList<FieldType>();

        for (FieldType field : subElements) {
            if (!(field instanceof VariableType)) {
                Element object = checkRelation(field.getNodeId(), objectList);
                retrieveAttributes(object, fields, ElementType.SUBOBJECT);
            }
        }
    }

    // checkstyle: stop method length check

    /**
     * Retrieves the attributes and creates respective elements.
     * 
     * @param element   the element to analyze the child nodes for
     * @param subFields the sub fields for the creation of output for
     *                  {@code element}
     * @param type      the element type
     */
    public void retrieveAttributes(Element element, ArrayList<FieldType> subFields, ElementType type) {

        String description = "";
        String displayName = "";
        String documentation = "";
        String typeDef = "";
        String modellingRule = "";
        boolean optional = false;
        ArrayList<FieldType> objectFields = new ArrayList<FieldType>();
        ArrayList<EnumLiteral> literals = new ArrayList<EnumLiteral>();
        ArrayList<DataLiteral> dataLiterals = new ArrayList<DataLiteral>();

        NodeList childNodeList = element.getChildNodes();

        for (int j = 0; j < childNodeList.getLength(); j++) {
            Element childNode = getNextNodeElement(childNodeList, j);
            if (childNode != null && childNode.getTagName() != "References") {
                if (childNode.getTagName() == "Description") {
                    description = (childNode.getTextContent() + "@" + childNode.getAttribute("Locale"))
                            .replaceAll("[“”\"]", "");
                } else if (childNode.getTagName() == "DisplayName") {
                    displayName = childNode.getTextContent().replaceAll("[“”\"_]", "");
                } else if (childNode.getTagName() == "Documentation") {
                    documentation = childNode.getTextContent().replaceAll("[“”\"]", "");
                } else if (childNode.getTagName() == "Definition") {
                    NodeList fields = childNode.getChildNodes();

                    for (int k = 0; k < fields.getLength(); k++) {
                        Element fieldNode = getNextNodeElement(fields, k);
                        if (fieldNode != null) {
                            String fieldName = fieldNode.getAttribute("Name").replaceAll("[/,“”\"]", "_");
                            String fieldDescription = getFieldDescription(fieldNode);
                            String fieldValue = fieldNode.getAttribute("Value");
                            String fieldDataType = "";
                            if (fieldValue.equals("")) {
                                // type = ElementType.DATATYPE;
                                fieldDataType = fieldNode.getAttribute("DataType");
//                              String externDataType = changeVariableDataTypes(fieldDataType);
//                              if(externDataType.equals("opcExternalType") || externDataType.contains("i=")) {
//                                  fieldDataType = checkForExternDataType(fieldDataType);
//                              }
                                DataLiteral literal = new DataLiteral(fieldName, changeVariableDataTypes(fieldDataType),
                                        fieldDescription);
                                dataLiterals.add(literal);
                            } else {
                                type = ElementType.ENUM;
                                EnumLiteral literal = new EnumLiteral(fieldName, fieldValue, fieldDescription);
                                literals.add(literal);
                            }
                        }

                    }
                }
            } else if (childNode != null && childNode.getTagName() == "References") {
                if (type != ElementType.VARIABLE && type != ElementType.FIELD && type != ElementType.ENUM) {
                    objectFields = identifyFields(childNode);
                }
                if (type != ElementType.ENUM && type != ElementType.FIELD) {
                    typeDef = BaseType.validateVarName(identifySpecificReference("HasTypeDefinition", childNode, type));
                    modellingRule = identifySpecificReference("HasModellingRule", childNode, type);
                    if (modellingRule.toUpperCase().contains("OPTIONAL")) {
                        optional = true;
                    }
                }
            }
        }
        createElement(type, element, displayName, description, documentation, subFields, objectFields, literals,
                dataLiterals, typeDef, optional);
    }

    // checkstyle: stop parameter number check

    /**
     * Returns the field description of {@code fieldNode}.
     * 
     * @param fieldNode the field node
     * @return the field description
     */
    private static String getFieldDescription(Element fieldNode) {
        String fieldDescription = "";
        NodeList fieldChilds = fieldNode.getChildNodes();

        for (int l = 0; l < fieldChilds.getLength(); l++) {
            Element fieldChildNode = getNextNodeElement(fieldChilds, l);
            if (fieldChildNode != null) {
                if (fieldChildNode.getTagName() == "Description") {
                    if (fieldChildNode.getAttribute("Locale").equals("")) {
                        fieldDescription = fieldChildNode.getTextContent();
                    } else {
                        fieldDescription = fieldChildNode.getTextContent() + "@"
                                + fieldChildNode.getAttribute("Locale");
                    }
                }
            }
        }
        return fieldDescription;
    }

    /**
     * Creates an element.
     * 
     * @param type          the element type.
     * @param element       the actual element
     * @param displayName   the display name
     * @param description   the description
     * @param documentation the documentation
     * @param subFields     the sub fields
     * @param objectFields  the object fields
     * @param literals      the literals
     * @param dataLiterals  the data literals
     * @param typeDef       the type def
     * @param optional      whether the type is optional
     */
    public void createElement(ElementType type, Element element, String displayName, String description,
            String documentation, ArrayList<FieldType> subFields, ArrayList<FieldType> objectFields,
            ArrayList<EnumLiteral> literals, ArrayList<DataLiteral> dataLiterals, String typeDef, boolean optional) {

        String dataType;
        ObjectType uaElement;

        switch (type) {
        case ENUM:
            EnumType enumeration = new EnumType(element.getAttribute("NodeId"),
                    element.getAttribute("BrowseName").replaceAll("[“”\"]", ""), displayName, description,
                    documentation, literals);
            enumeration.setVarName("opc" + displayName + "Type");
            if (!checkRedundancy(enumeration.getVarName(), null)) {
                println(enumeration.toString());
                hierarchy.add(enumeration);
                println("");
            }
            break;
        case DATATYPE:
            DataType uaDataType = new DataType(element.getAttribute("NodeId"),
                    element.getAttribute("BrowseName").replaceAll("[“”\"]", ""), displayName, description,
                    documentation, dataLiterals);
            uaDataType.setVarName("opc" + displayName + "Type");
            if (!checkRedundancy(uaDataType.getVarName(), null)) {
                println(uaDataType.toString());
                hierarchy.add(uaDataType);
            }
            break;
        case SUBOBJECT:
            uaElement = new ObjectType(element.getAttribute("NodeId"),
                    element.getAttribute("BrowseName").replaceAll("[“”\"]", ""), displayName, description, typeDef,
                    objectFields);
            uaElement.setVarName(searchVarName(uaElement, hierarchy));
            if (!objectFields.isEmpty()) {
                adaptDatatypesToModel(uaElement);
            }
            println(uaElement.toString());
            hierarchy.add(uaElement);
            println("");
            if (!uaElement.getFields().isEmpty()) {
                retrieveRelatedSubElements(uaElement.getFields());
            }
            break;
        case OBJECT:
            uaElement = new RootObject(element.getAttribute("NodeId"),
                    element.getAttribute("BrowseName").replaceAll("[“”\"]", ""), displayName, description, typeDef,
                    retrieveRootParent(element.getAttribute("ParentNodeId")), objectFields);
            uaElement.setVarName(retrieveRootParent(element.getAttribute("ParentNodeId")) + displayName);
            if (!objectFields.isEmpty()) {
                adaptDatatypesToModel(uaElement);
            }
            if (!checkRedundancy(uaElement.getVarName(), null)) {
                println(uaElement.toString());
                hierarchy.add(uaElement);
            }
            if (!uaElement.getFields().isEmpty()) {
                retrieveRelatedSubElements(uaElement.getFields());
            }
            break;
        case OBJECTTYPE:
            ObjectTypeType uaObjectType = new ObjectTypeType(element.getAttribute("NodeId"),
                    element.getAttribute("BrowseName").replaceAll("[“”\"]", ""), displayName, description,
                    documentation);
            uaObjectType.setVarName("opc" + displayName);
            if (!checkRedundancy(uaObjectType.getVarName(), null)) {
                println(uaObjectType.toString());
                hierarchy.add(uaObjectType);
            }
            break;
        case VARIABLE:
            if (displayName.equals("Sublots")) {
                println(element.getAttribute("DataType"));
            }

            dataType = element.getAttribute("DataType");
            if (dataType.equals("EnumValueType")) {
                Element relatedDataTypeElement = checkRelation(element.getAttribute("ParentNodeId"), dataTypeList);
                NodeList dataChildNodeList = relatedDataTypeElement.getChildNodes();
                for (int j = 0; j < dataChildNodeList.getLength(); j++) {
                    Element childNode = getNextNodeElement(dataChildNodeList, j);
                    if (childNode != null) {
                        if (childNode.getTagName() == "DisplayName") {
                            dataType = childNode.getTextContent();
                        }
                    }
                }
            } else {
                dataType = changeVariableDataTypes(dataType);
            }
            VariableType uaVariable = new VariableType(element.getAttribute("NodeId"),
                    element.getAttribute("BrowseName").replaceAll("[“”\"]", ""), displayName, description, dataType,
                    typeDef, optional, element.getAttribute("AccessLevel"), element.getAttribute("ValueRank"),
                    element.getAttribute("ArrayDimensions"));
            uaVariable.setVarName("opc" + displayName);
            if (!checkRedundancy(uaVariable.getVarName(), subFields)) {
                subFields.add(uaVariable);
            }
            break;
        case FIELD:
            FieldType uaField = new FieldType(element.getAttribute("NodeId"),
                    element.getAttribute("BrowseName").replaceAll("[“”\"]", ""), displayName, description, "");
            uaField.setVarName("opc" + displayName);
            if (!checkRedundancy(uaField.getVarName(), subFields)) {
                subFields.add(uaField);
            }
            break;
        case VARIABLETYPE:
            if (displayName.equals("AudioVariableType")) {
                println("AudioVariableType");
            }
            // dataType = checkForExternDataType(element.getAttribute("DataType"));
            VariableTypeType uaVariableType = new VariableTypeType(element.getAttribute("NodeId"),
                    element.getAttribute("BrowseName").replaceAll("[“”\"]", ""), displayName, description,
                    documentation, changeVariableDataTypes(element.getAttribute("DataType")));
            uaVariableType.setVarName("opc" + displayName);
            if (!checkRedundancy(uaVariableType.getVarName(), null)) {
                println(uaVariableType.toString());
                hierarchy.add(uaVariableType);
            }
            break;
        default:
            break;
        }
    }

    // checkstyle: resume parameter number check
    // checkstyle: resume method length check

    /**
     * Checks for redundant/duplicate variable names in {@link #hierarchy}.
     * 
     * @param varName the variable name to check for
     * @param list the list of fields to check
     * @return {@code true} if there are duplicates, {@code false} else
     */
    public boolean checkRedundancy(String varName, ArrayList<FieldType> list) {
        boolean duplicateVar = false;
        if (list != null) {
            for (FieldType f : list) {
                if (f.getVarName().equals(varName)) {
                    duplicateVar = true;
                    break;
                }
            }
        } else {
            for (BaseType o : hierarchy) {
                if (o.getVarName().equals(varName)) {
                    duplicateVar = true;
                    break;
                }
            }
        }
        return duplicateVar;
    }

    /**
     * Retrieves the element types and nested attributes via
     * {@link #retrieveAttributes(Element, ArrayList, ElementType)}.
     */
    public void retrieveElementTypes() {

        for (int i = 0; i < objectTypeList.getLength(); i++) {
            Element objectType = getNextNodeElement(objectTypeList, i);
            if (objectType != null) {
                retrieveAttributes(objectType, null, ElementType.OBJECTTYPE);
            }
        }
        for (int i = 0; i < dataTypeList.getLength(); i++) {
            Element dataType = getNextNodeElement(dataTypeList, i);
            if (dataType != null) {
                retrieveAttributes(dataType, null, ElementType.DATATYPE);
                // retrieveAttributes(dataType, null, ElementType.ENUM);
            }
        }
    }

    // checkstyle: stop method length check

    /**
     * Turns a file into an operating-system dependent string path.
     * 
     * @param file the file
     * @return the path
     */
    private static String toOsPath(File file) {
        return file.toString();
    }

    /**
     * Turns a string (assumed to be a path) into an operating-system dependent
     * string path.
     * 
     * @param path the path to be translated
     * @return the path
     */
    private static String toOsPath(String path) {
        return toOsPath(new File(path)); // normalize to OS paths
    }

    /**
     * Checks for required models.
     * 
     * @param modelName     the model name to check for
     * @param path          the path to check for
     * @param fileName      the file name to check for
     * @param nameSpaceUris the OPC namespace URIs
     * @return the found/required model files
     */
    public static File[] checkRequiredModels(String modelName, String path, String fileName, NodeList nameSpaceUris) {
        Scanner scanner = new Scanner(System.in);
        // suche die Modelle
        ArrayList<String> uris = new ArrayList<String>();
        // CORE SPEC
        uris.add(0, "UA");
        for (int i = 0; i < nameSpaceUris.getLength(); i++) {
            Element element = getNextNodeElement(nameSpaceUris, i);
            if (element != null) {
                NodeList childNodeList = element.getChildNodes();

                for (int j = 0; j < childNodeList.getLength(); j++) {
                    Element childNode = getNextNodeElement(childNodeList, j);
                    if (childNode != null) {
                        if (childNode.getTagName() == "Uri") {
                            uris.add(childNode.getTextContent());
                        }
                    }
                }
            }
        }
//      //entferne das MutterNodeSet

        // TODO nutzte <Model ModelUri="http://opcfoundation.org/UA/AMLLibs/" > um das
        // Mutternodeset zu identifizieren + ggf. mutternamespace anpassen
        // WENN MUTTERNODESET NICHT GEFUNDEN WURDE DANN BITTE MANUELL NAMEN ANPASSEN UND
        // CONTINUE
        int nameSpace = 0;
        int ns = 0;
        for (Iterator<String> iterator = uris.iterator(); iterator.hasNext();) {
            String value = iterator.next();
            if (value.equals(modelName)) {
                nameSpace = ns;
                iterator.remove();

                break;
            } else {
                ns++;
            }
        }

        boolean correct = false;
        File[] models = null;
        File[] files = null;
        ArrayList<File> foundFiles = new ArrayList<File>();
        File f = new File(path, "RequiredModels");
        do {
            if (!f.exists()) {
                File requiredModels = new File(path, "RequiredModels");
                System.out.println("Directory " + requiredModels.toString() + " is not existing.");
                boolean created = requiredModels.mkdir();
                if (created) {
                    System.out.println("Creating directory.\n");
                } else {
                    System.out.println("Directory " + requiredModels.toString() + " can't be created.\n");
                }
                System.out.println("Please add the following models to " + requiredModels.toString() + ":");
                for (String s : uris) {
                    System.out.println(s);
                }
            } else {
                File requiredModels = new File(path, "RequiredModels");
                files = f.listFiles();
                if (files.length == 0) {
                    System.out.println("The folder RequiredModels is still empty.");
                    System.out.println("Please add the following models to " + requiredModels.toString() + ":");
                    for (String s : uris) {
                        System.out.println(s);
                    }
                } else {
                    String missingModels = "";
                    boolean modelFound = false;
                    for (String s : uris) {
                        s = s.replace("http://opcfoundation.org/UA/", "").replace("/", "").toUpperCase();
                        for (int i = 0; i < files.length; i++) {
                            String model = null;
                            if (toOsPath(files[i]).equals(toOsPath(path + "/RequiredModels/Opc.Ua.NodeSet2.xml"))) {
                                model = "UA";
                            } else {
                                model = toOsPath(files[i]).replace(toOsPath(path + "/RequiredModels/Opc.Ua."), "")
                                        .replace(".NodeSet2.xml", "").toUpperCase();
                            }
                            if (model.equals(s)) {
                                if (model.equals("UA")) {
                                    File rModel = new File(files[i].toString());
                                    foundFiles.add(rModel);
                                } else {
                                    File rModel = new File(files[i].toString());
                                    foundFiles.add(rModel);
                                }
                                modelFound = true;
                                break;
                            }
                        }
                        if (!modelFound) {
                            missingModels += s + "\n";
                        }
                        modelFound = false;
                    }
                    if (missingModels.isEmpty()) {
                        System.out.println("All required models are available in " + path);
                        models = new File[foundFiles.size()];
                        models = foundFiles.toArray(models);
                        correct = true;
                    } else {
                        System.out.println("The following models are still missing:\n" + missingModels);
                    }
                }
                // Überprüfung, ob files fehlen und wenn, ja welche
            }
            if (!correct) {
                boolean confirmed = false;
                do {
                    System.out.println("\nPress y to continue if the respective files were added.");
                    String input = scanner.nextLine();
                    if (input.equals("y")) {
                        confirmed = true;
                    }
                } while (!confirmed);
            }
        } while (!correct);
        scanner.close();
        return models;
    }

    // checkstyle: resume method length check

    /**
     * Parses a file by retrieving all root elements of the objects in
     * {@link #objectList} and retriving all data types in {@link #dataTypeList}.
     */
    public void parseFile() {
        for (int i = 0; i < objectList.getLength(); i++) {
            Element object = getNextNodeElement(objectList, i);
            if (object != null) {
                String parentNodeId = object.getAttribute("ParentNodeId");
                Element rootObject = checkRelation(parentNodeId, objectTypeList);
                if (rootObject != null) {
                    retrieveRootElement(object);
                }
            }
        }
        // TODO DASSELBE FÜR VARIABLEN MACHEN DIE EINEN OBJECTTYPE ALS PARENT HABEN

        if (dataTypeList.getLength() > 0 || objectTypeList.getLength() > 0) {
            retrieveElementTypes();
        }
    }

    /**
     * Creates the parser.
     * 
     * @param path     the path to the OPC UA nodeset models
     * @param compSpec the companion spec to be parsed
     * @param verbose  verbose output
     * @return the DOM parser after parsing
     */
    public static DomParser createParser(String path, File compSpec, boolean verbose) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DomParser parser = null;
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(compSpec);
            System.out.println("\n" + compSpec.getName());
            NodeList nameSpaceUris = doc.getElementsByTagName("NamespaceUris");
            NodeList objectTypeList = doc.getElementsByTagName("UAObjectType");
            NodeList objectList = doc.getElementsByTagName("UAObject");
            NodeList variableList = doc.getElementsByTagName("UAVariable");
            NodeList dataTypeList = doc.getElementsByTagName("UADataType");
            NodeList variableTypeList = doc.getElementsByTagName("UAVariableType");
            NodeList aliasList = doc.getElementsByTagName("Aliases");
            NodeList models = doc.getElementsByTagName("Models");
            String modelName = "";

            for (int i = 0; i < models.getLength(); i++) {
                Element element = getNextNodeElement(models, i);
                if (element != null) {
                    NodeList childNodeList = element.getChildNodes();

                    for (int j = 0; j < childNodeList.getLength(); j++) {
                        Element childNode = getNextNodeElement(childNodeList, j);
                        if (childNode != null) {
                            modelName = childNode.getAttribute("ModelUri");
                        }
                    }
                }
            }
            ArrayList<BaseType> hierarchy = new ArrayList<BaseType>();
            // TODO bis zum 2. Punkt entfernen ohne statischen Namen, da es auch ".NodeSet"
            // gibt (ohne "2")
//            File[] models = checkRequiredModels(path, toOsPath(compSpec).replace(toOsPath(path + "/Opc.Ua."), "")
//                    .replace(".NodeSet2.xml", ""), nameSpaceUris);
            File[] reqModels = checkRequiredModels(modelName, path,
                    toOsPath(compSpec).replace(toOsPath(path + "/Opc.Ua."), "").replace(".NodeSet.xml", ""),
                    nameSpaceUris);
            Document[] documents = new Document[reqModels.length];
            for (int i = 0; i < reqModels.length; i++) {
                System.out.println(reqModels[i]);
                documents[i] = builder.parse(reqModels[i]);
            }

            parser = new DomParser(documents, objectTypeList, objectList, variableList, dataTypeList, variableTypeList,
                    aliasList, hierarchy);
            parser.verbose = verbose;
            parser.parseFile();
        } catch (ParserConfigurationException | SAXException | IOException e) {
            LoggerFactory.getLogger(DomParser.class).error(e.getMessage());
        }
        return parser;
    }

    /**
     * Creates the IVML model in the given {@code fileName}.
     * 
     * @param fileName the file name for the IVML model
     * @param ivmlFile the output file
     */
    public void createIvmlModel(String fileName, File ivmlFile) {
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

    /**
     * Prints out information in verbose mode.
     * 
     * @param text the text to print
     */
    private void println(String text) {
        if (verbose) {
            System.out.println(text);
        }
    }

    /**
     * Processes an OPC XML file.
     * 
     * @param xmlIn   the input file
     * @param outName the output file/model name
     * @param ivmlOut the full output file name
     * @param verbose verbose output
     */
    public static void process(File xmlIn, String outName, File ivmlOut, boolean verbose) {
        System.out.println("Processing " + xmlIn + " to " + outName + "(" + ivmlOut + ")");
        String path = xmlIn.getParent();
        DomParser parser = createParser(path, xmlIn, verbose);
        parser.createIvmlModel(outName, ivmlOut);
    }

    /**
     * Executes the parser, per default in verbose mode.
     * 
     * @param args command line arguments (ignored)
     */
    public static void main(String[] args) {
        File file;
        if (args.length == 1) {
            file = new File(args[0]);
        } else {
            file = new File("src/main/resources/NodeSets/Opc.Ua.Woodworking.NodeSet2.xml");
            // file = new
            // File("src/main/resources/NodeSets/Opc.Ua.MachineTool.NodeSet2.xml");
            // file = new
            // File("src/main/resources/NodeSets/Opc.Ua.Weihenstephan.NodeSet2.xml");
            // file = new File("src/main/resources/NodeSets/Opc.Ua.Adi.NodeSet2.xml");
            // file = new File("src/main/resources/NodeSets/Opc.Ua.AutoID.NodeSet2.xml");
            // file = new File("src/main/resources/NodeSets/Opc.Ua.CNC.NodeSet2.xml");
            // file = new File("src/main/resources/NodeSets/Opc.Ua.BACnet.NodeSet2.xml");
            // TODO
            // file = new File("src/main/resources/NodeSets/Opc.Ua.CAS.NodeSet2.xml");
            // file = new
            // File("src/main/resources/NodeSets/Opc.Ua.CommercialKitchenEquipment.NodeSet2.xml");
            // file = new
            // File("src/main/resources/NodeSets/Opc.Ua.CSPPlusForMachine.NodeSet2.xml");
            // file = new File("src/main/resources/NodeSets/Opc.Ua.DEXPI.NodeSet2.xml");
            // file = new File("src/main/resources/NodeSets/Opc.Ua.Sercos.NodeSet2.xml");
            // file = new File("src/main/resources/NodeSets/Opc.Ua.TMC.NodeSet2.xml");
            // file = new File("src/main/resources/NodeSets/Opc.Ua.Scheduler.NodeSet2.xml");
            // file = new File("src/main/resources/NodeSets/Opc.Ua.Scales.NodeSet2.xml");
//          file = new File("src/main/resources/NodeSets/Opc.Ua.Safety.NodeSet2.xml");
//          file = new File("src/main/resources/NodeSets/Opc.Ua.RSL.NodeSet2.xml");
//          file = new File("src/main/resources/NodeSets/Opc.Ua.Robotics.NodeSet2.xml");
//          file = new File("src/main/resources/NodeSets/Opc.Ua.Pumps.NodeSet2.xml");
//          file = new File("src/main/resources/NodeSets/Opc.Ua.POWERLINK.NodeSet2.xml");
//          file = new File("src/main/resources/NodeSets/Opc.Ua.PnRio.NodeSet2.xml");
//          file = new File("src/main/resources/NodeSets/Opc.Ua.PnEm.NodeSet2.xml");
//          file = new File("src/main/resources/NodeSets/Opc.Ua.Pn.NodeSet2.xml");
//          file = new File("src/main/resources/NodeSets/Opc.Ua.PLCopen.NodeSet2.xml");
//          file = new File("src/main/resources/NodeSets/Opc.Ua.OPENSCS.NodeSet2.xml");
//          file = new File("src/main/resources/NodeSets/Opc.Ua.Onboarding.NodeSet2.xml");

//          file = new File("src/main/resources/NodeSets/Opc.Ua.PADIM.NodeSet2.xml");
            // file = new
            // File("src/main/resources/NodeSets/Opc.Ua.AMLLibraries.NodeSet2.xml");
        }
        String fileName = file.getName();
        fileName = StringUtils.removeStart(fileName, "Opc.Ua");
        fileName = StringUtils.removeEnd(fileName, ".xml");
        // TODO bis zum 2. Punkt entfernen ohne statischen Namen, da es auch ".NodeSet"
        // gibt (ohne "2")
        fileName = StringUtils.removeEnd(fileName, ".NodeSet2");
        // fileName = StringUtils.removeEnd(fileName, ".NodeSet");
        fileName = fileName.replace(".", "");
        // TODO wieder in "gen/opc" ändern
        // File ivmlFile = new File("gen/Opc" + fileName + ".ivml");
        File ivmlFile = new File(mainOutFolder, "Opc" + fileName + ".ivml");
        process(file, fileName, ivmlFile, verboseDefault);
    }

}
