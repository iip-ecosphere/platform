package de.iip_ecosphere.platform.configuration.opcua.parser;

import java.io.File;
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

import de.iip_ecosphere.platform.configuration.opcua.data.*;

/**
 * Denotes the OPC UA element types.
 * 
 * @author Jan-Hendrik Cepok, SSE
 */
enum ElementType {

    OBJECTTYPE(false), VARIABLETYPE(false), ROOTOBJECT(true), ROOTVARIABLE(false), ROOTMETHOD(true), SUBOBJECT(true),
    SUBMETHOD(true), FIELDOBJECT(true), FIELDVARIABLE(false), FIELDMETHOD(true), ENUM(false), DATATYPE(false);

    private boolean isCore;

    /**
     * Creates a new enum value.
     * 
     * @param isCore whether this value is part of "core"
     */
    private ElementType(boolean isCore) {
        this.isCore = isCore;
    }

    /**
     * Returns whether this value is part of "core".
     * 
     * @return {@code true} for core, {@code false} else
     */
    public boolean isCore() {
        return isCore;
    }

}

/**
 * XML parser for OPC UA companion spec files.
 * 
 * @author Jan-Hendrik Cepok, SSE
 */
public class DomParser {

    private static boolean verboseDefault = true;
    private static String usingIvmlFolder = "src/test/easy";
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

    private Document[] documents;
    private NodeList objectTypeList;
    private NodeList objectList;
    private NodeList variableList;
    private NodeList methodList;
    private NodeList dataTypeList;
    private NodeList variableTypeList;
    private NodeList aliasList;
    private ArrayList<BaseType> hierarchy;
    private boolean verbose = verboseDefault;
    private String baseNameSpace;
    private ArrayList<NodeList> externAliasLists;

    // checkstyle: stop parameter number check

    /**
     * Creates a DOM parser/translator.
     * 
     * @param objectTypeList   the already parsed object type list
     * @param objectList       the already parsed object list
     * @param variableList     the already parsed variable list
     * @param methodList       the already parsed method list
     * @param dataTypeList     the already parsed data type list
     * @param variableTypeList the already parsed variable type list
     * @param aliasList        the already parsed alias list
     * @param hierarchy        the base type hierarchy
     */
    private DomParser(NodeList objectTypeList, NodeList objectList, NodeList variableList, NodeList methodList,
            NodeList dataTypeList, NodeList variableTypeList, NodeList aliasList, ArrayList<BaseType> hierarchy) {
        this.objectTypeList = objectTypeList;
        this.objectList = objectList;
        this.variableList = variableList;
        this.methodList = methodList;
        this.dataTypeList = dataTypeList;
        this.variableTypeList = variableTypeList;
        this.aliasList = aliasList;
        this.hierarchy = hierarchy;
    }

    /**
     * Defines the OPC UA core alias list.
     * 
     * @param externAliasLists the list of OPC UA core aliases.
     */
    public void setExternAliasLists(ArrayList<NodeList> externAliasLists) {
        this.externAliasLists = externAliasLists;
    }

    /**
     * Defines the OPC UA document collection.
     * 
     * @param documents the collection of documents.
     */
    public void setDocuments(Document[] documents) {
        this.documents = documents;
    }

    /**
     * Defines the name space of the basic OPC UA Spec.
     * 
     * @param baseNameSpace the base name space
     */
    public void setBaseNameSpace(String baseNameSpace) {
        this.baseNameSpace = baseNameSpace;
    }

    /**
     * Changes the default verbose mode used when instantiating a parser.
     * 
     * @param verbose verbose or non verbose mode
     */
    public static void setDefaultVerbose(boolean verbose) {
        verboseDefault = verbose;
    }

    // checkstyle: resume parameter number check

    /**
     * Searches for a field type variable name.
     * 
     * @param uaElement the UA element delivering the node Id to search for
     * @param hierarchy the type hierarchy to search within
     * @return the variable name of the found field type
     */
    private static String searchVarName(BaseType uaElement, ArrayList<BaseType> hierarchy) {
        String varName = "";
        for (BaseType o : hierarchy) {
            if (o instanceof ObjectType) {
                ArrayList<FieldType> fields = ((ObjectType) o).getFields();
                if (!fields.isEmpty()) {
                    for (FieldType f : fields) {
                        if (!(f instanceof FieldVariableType)) {
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
    private String changeVariableDataTypes(String dataType) {

        if (dataType.contains("i=") && !dataType.contains("ns=")) {
            for (int i = 0; i < aliasList.getLength(); i++) {
                Element alias = getNextNodeElement(aliasList, i);
                NodeList childNodeList = alias.getChildNodes();
                for (int j = 0; j < childNodeList.getLength(); j++) {
                    Element childNode = getNextNodeElement(childNodeList, j);
                    if (childNode != null) {
                        String nodeId = childNode.getTextContent();
                        if (nodeId.equals(dataType)) {
                            dataType = childNode.getAttribute("Alias");
                            break;
                        }
                    }
                }
            }
        }

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
        case "UInteger":
            dataType = "opcUnsignedIntegerType";
            break;
        case "":
            dataType = "opcUnknownDataType";
            break;
        default:
            if (dataType.contains("ns=" + baseNameSpace)) {
                dataType = checkForInternDataType(dataType);
                dataType = "opc" + dataType + "Type";
            } else if (!dataType.contains("opc") && !modelTypes) {
                dataType = checkForExternDataType(dataType);
                dataType = "opc" + dataType + "Type";
            }
            break;
        }
        return dataType;
    }

    // checkstyle: resume method length check

    /**
     * Adapts the OPC UA data types to IIP-Ecosphere meta model type names.
     * 
     * @param uaObject the UA object to adapt the types for
     * @param uaMethod the UA method to adapt the types for
     */
    private void adaptDatatypesToModel(ObjectType uaObject, MethodType uaMethod) {
        ArrayList<FieldType> fields;
        if (uaMethod == null) {
            fields = uaObject.getFields();
            for (FieldType f : fields) {
                if (f instanceof FieldVariableType) {
                    nop();
                } else if (f instanceof FieldObjectType) {
                    f.setDataType(BaseType.validateVarName(uaObject.getVarName() + f.getDisplayname()));
                    fields.set(fields.indexOf(f), f);
                } else if (f instanceof FieldMethodType) {
                    f.setDataType(BaseType.validateVarName(uaObject.getVarName() + f.getDisplayname()));
                    fields.set(fields.indexOf(f), f);
                }
            }
        } else {
            fields = uaMethod.getFields();
            for (FieldType f : fields) {
                if (f instanceof FieldVariableType) {
                    nop();
                } else if (f instanceof FieldObjectType) {
                    f.setDataType(BaseType.validateVarName(uaMethod.getVarName() + f.getDisplayname()));
                    fields.set(fields.indexOf(f), f);
                } else if (f instanceof FieldMethodType) {
                    f.setDataType(BaseType.validateVarName(uaMethod.getVarName() + f.getDisplayname()));
                    fields.set(fields.indexOf(f), f);
                }
            }
        }

    }

    /**
     * Does nothing, just allows for code convention compliance while bugfixing.
     */
    private static void nop() {
    }

    /**
     * Checks the relations and returns a node with NodeId {@code currentNodeId}.
     * 
     * @param currentNodeId the node id to search for
     * @param nodes         the nodes to search
     * @return the found element
     */
    private static Element checkRelation(String currentNodeId, NodeList nodes) {

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
    private static Element getNextNodeElement(NodeList nodes, int iterator) {
        Node n = nodes.item(iterator);
        Element node = null;
        if (n.getNodeType() == Node.ELEMENT_NODE) {
            node = (Element) n;
        }
        return node;
    }

    /**
     * Retrieves the displayName of given parentNodeId.
     * 
     * @param parentNodeId the parent node id
     * @param list         the node list to check
     * @return the root parent
     */
    private String retrieveParent(String parentNodeId, NodeList list) {
        String rootParent = "";
        Element element = checkRelation(parentNodeId, list);
        if (element != null) {
            NodeList childNodeList = element.getChildNodes();

            for (int j = 0; j < childNodeList.getLength(); j++) {
                Element childNode = getNextNodeElement(childNodeList, j);
                if (childNode != null) {
                    if (childNode.getTagName().equals("DisplayName")) {
                        rootParent = BaseType.validateVarName("opc" + childNode.getTextContent());
                        break;
                    }
                }
            }
        }
        return rootParent;
    }

    /**
     * Checks for intern data type.
     * 
     * @param dataTypeNodeId the referenced node id of a data type
     * @return the identified data type
     */
    private String checkForInternDataType(String dataTypeNodeId) {
        String identifiedDataType = "";
        for (int i = 0; i < dataTypeList.getLength(); i++) {
            Element dataType = getNextNodeElement(dataTypeList, i);
            if (dataType != null) {
                if (dataType.getAttribute("NodeId").equals(dataTypeNodeId)) {
                    NodeList childNodeList = dataType.getChildNodes();
                    for (int j = 0; j < childNodeList.getLength(); j++) {
                        Element childNode = getNextNodeElement(childNodeList, j);
                        if (childNode != null && !childNode.getTagName().equals("References")) {
                            if (childNode.getTagName().equals("DisplayName")) {
                                identifiedDataType = childNode.getTextContent().replaceAll("[“”\"_\\\\]", "");
                                break;
                            }
                        }
                    }
                }
            }
        }

        return identifiedDataType;
    }

    /**
     * Checks for an extern data type.
     * 
     * @param dataType the data type to look for
     * @return the data type
     */
    private String checkForExternDataType(String dataType) {
        if ((dataType.contains("ns=") | dataType.contains("i="))) {
            dataType = retrieveAttributesForExternDataType(dataType);
        }
        boolean foundAlias = false;
        for (int i = 0; i < aliasList.getLength(); i++) {
            Element alias = getNextNodeElement(aliasList, i);
            NodeList childNodeList = alias.getChildNodes();
            for (int j = 0; j < childNodeList.getLength(); j++) {
                Element childNode = getNextNodeElement(childNodeList, j);
                if (childNode != null) {
                    if (childNode.getAttribute("Alias").equals(dataType)) {
                        foundAlias = true;
                        String nodeId = childNode.getTextContent();
                        if (nodeId.contains("i=")) {
                            if (!nodeId.contains("ns=" + baseNameSpace)) {
                                dataType = retrieveAttributesForExternDataType(nodeId);
                            }
                        }
                        break;
                    }
                }
            }
        }
        if (!foundAlias) {
            for (NodeList l : externAliasLists) {
                for (int i = 0; i < l.getLength(); i++) {
                    Element alias = getNextNodeElement(l, i);
                    NodeList childNodeList = alias.getChildNodes();
                    for (int j = 0; j < childNodeList.getLength(); j++) {
                        Element childNode = getNextNodeElement(childNodeList, j);
                        if (childNode != null) {
                            if (childNode.getAttribute("Alias").equals(dataType)) {
                                foundAlias = true;
                                String nodeId = childNode.getTextContent();
                                if (nodeId.contains("i=") && !nodeId.contains("ns=" + baseNameSpace)) {
                                    dataType = retrieveAttributesForExternDataType(nodeId);
                                }
                                break;
                            }
                        }
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
            String externNodeId = nodeId;
            if (externNodeId.contains("ns=")) {
                externNodeId = externNodeId.substring(0, externNodeId.indexOf("=") + 1) + 1
                        + externNodeId.substring(externNodeId.indexOf(";"), externNodeId.length());
            }
            Element element = checkRelation(externNodeId, typeList);
            if (element != null) {

                NodeList childNodeList = element.getChildNodes();

                for (int j = 0; j < childNodeList.getLength(); j++) {
                    Element childNode = getNextNodeElement(childNodeList, j);
                    if (childNode != null && !childNode.getTagName().equals("References")) {
                        if (childNode.getTagName().equals("DisplayName")) {
                            dataType = childNode.getTextContent().replaceAll("[“”\"_\\\\]", "");
                            break;
                        }
                    }
                }
                retrieveAttributes(element, null, ElementType.DATATYPE, nodeId);
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
    private String identifySpecificReference(String reference, Node node, ElementType type) {
        NodeList references = node.getChildNodes();
        for (int k = 0; k < references.getLength(); k++) {
            Element refNode = getNextNodeElement(references, k);
            if (refNode != null && refNode.getAttribute("ReferenceType").equals(reference)) {
                String refId = refNode.getTextContent();
                if (refId.contains("ns=" + baseNameSpace) || !refId.contains("ns=")) {
                    TypeListAndType r = getTypeListAndTypeRootNs(refId, reference, type);
                    type = r.type;
                    Element refElement = checkRelation(refId, r.typeList);
                    if (refElement != null) {
                        DescriptionOrDocumentation d = getDescriptionOrDocumentation(reference, refElement);
                        reference = d.reference;
                        if (!type.isCore()) {
                            createElement(type, refElement, refId, d.displayName, d.description, d.documentation, null,
                                    null, null, null, null, false);
                        }
                    }
                } else {
                    // other models
                    String newRefId = refId.substring(0, refId.indexOf("=") + 1) + 1
                            + refId.substring(refId.indexOf(";"), refId.length());
                    for (int i = 1; i < documents.length; i++) {
                        NodeList typeList = null;
                        if (type == ElementType.ROOTOBJECT || type == ElementType.SUBOBJECT) {
                            typeList = documents[i].getElementsByTagName("UAObjectType");
                            type = ElementType.OBJECTTYPE;
                        } else if (type == ElementType.FIELDVARIABLE) {
                            typeList = documents[i].getElementsByTagName("UAVariableType");
                            type = ElementType.VARIABLETYPE;
                        }
                        Element refElement = checkRelation(newRefId, typeList);
                        if (refElement != null) {
                            DescriptionOrDocumentation d = getDescriptionOrDocumentation(reference, refElement);
                            // create Attribute
                            reference = d.reference;
                            createElement(type, refElement, refId, d.displayName, d.description, d.documentation, null,
                                    null, null, null, null, false);
                            break;
                        } else if (type == ElementType.OBJECTTYPE) {
                            type = ElementType.ROOTOBJECT;
                        } else if (type == ElementType.VARIABLETYPE) {
                            type = ElementType.FIELDVARIABLE;
                        }
                    }

                }
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
        if (refId.contains("ns=" + baseNameSpace)) {
            // comp spec
            if (type == ElementType.ROOTOBJECT || type == ElementType.SUBOBJECT) {
                result.typeList = objectTypeList;
            } else if (type == ElementType.FIELDVARIABLE || type == ElementType.ROOTVARIABLE) {
                result.typeList = variableTypeList;
                result.type = ElementType.VARIABLETYPE;
            }
        } else if (!refId.contains("ns=")) {
            // core
            if (type.isCore()) {
                if (reference.equals("HasModellingRule")) {
                    result.typeList = documents[0].getElementsByTagName("UAObject");
                } else {
                    result.typeList = documents[0].getElementsByTagName("UAObjectType");
                    result.type = ElementType.OBJECTTYPE;
                }
            } else if (type == ElementType.FIELDVARIABLE || type == ElementType.ROOTVARIABLE) {
                if (reference.equals("HasModellingRule")) {
                    result.typeList = documents[0].getElementsByTagName("UAObject");
                    result.type = ElementType.ROOTOBJECT;
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
            if (childNode != null && !childNode.getTagName().equals("References")) {
                if (childNode.getTagName().equals("DisplayName")) {
                    result.reference = childNode.getTextContent().replaceAll("[“”\"\\\\]", "");
                    result.displayName = result.reference;
                } else if (childNode.getTagName().equals("Description")) {
                    result.description = childNode.getTextContent().replaceAll("[“”\"\\\\]", "");
                } else if (childNode.getTagName().equals("Documentation")) {
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
    private ArrayList<FieldType> identifyFields(Node childNode) {
        ArrayList<FieldType> fields = new ArrayList<FieldType>();
        NodeList references = childNode.getChildNodes();

        for (int k = 0; k < references.getLength(); k++) {
            Element refNode = getNextNodeElement(references, k);
            if (refNode != null
                    && IDENTIFY_FIELDS_PERMITTED_REFERENCE_TYPE.contains(refNode.getAttribute("ReferenceType"))) {
                String refId = refNode.getTextContent();
                Element refElement = checkRelation(refId, variableList);
                if (refElement != null) {
                    retrieveAttributesForRefElement(fields, refId, refElement, ElementType.FIELDVARIABLE);
                } else {
                    refElement = checkRelation(refId, objectList);
                    if (refElement != null && !(refNode.getAttribute("IsForward").equals("false"))) {
                        retrieveAttributesForRefElement(fields, refId, refElement, ElementType.FIELDOBJECT);
                    } else {
                        refElement = checkRelation(refId, methodList);
                        if (refElement != null) {
                            retrieveAttributesForRefElement(fields, refId, refElement, ElementType.FIELDMETHOD);
                        }
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
            retrieveAttributes(refElement, fields, elementType, null);
        } else {
            boolean retrieve = true;
            for (FieldType f : fields) {
                if (f.getNodeId().equals(refId)) {
                    retrieve = false;
                }
            }
            if (retrieve) {
                retrieveAttributes(refElement, fields, elementType, null);
            }
        }
    }

    /**
     * Retrieves the root element.
     * 
     * @param object the element to start with
     * @param type   the type
     */
    private void retrieveRootElement(Element object, ElementType type) {
        retrieveAttributes(object, null, type, null);
    }

    /**
     * Retrieves the related sub elements.
     * 
     * @param subElements the sub elements to analyze
     */
    private void retrieveRelatedSubElements(ArrayList<FieldType> subElements) {

        ArrayList<FieldType> fields = new ArrayList<FieldType>();

        for (FieldType field : subElements) {
            if (!(field instanceof FieldVariableType) && !(field instanceof FieldMethodType)) {
                Element object = checkRelation(field.getNodeId(), objectList);
                retrieveAttributes(object, fields, ElementType.SUBOBJECT, null);
            } else if (!(field instanceof FieldVariableType) && !(field instanceof FieldObjectType)) {
                Element method = checkRelation(field.getNodeId(), methodList);
                retrieveAttributes(method, fields, ElementType.SUBMETHOD, null);
            }
        }
    }

    // checkstyle: stop method length check

    /**
     * Retrieves the attributes and creates respective elements.
     * 
     * @param element      the element to analyze the child nodes for
     * @param subFields    the sub fields for the creation of output for
     *                     {@code element}
     * @param type         the element type
     * @param externNodeId the element type
     */
    private void retrieveAttributes(Element element, ArrayList<FieldType> subFields, ElementType type,
            String externNodeId) {

        String id = "";
        String description = "";
        String displayName = "";
        String documentation = "";
        String typeDef = "";
        String modellingRule = "";
        boolean optional = false;
        ArrayList<FieldType> objectFields = new ArrayList<FieldType>();
        ArrayList<EnumLiteral> literals = new ArrayList<EnumLiteral>();
        ArrayList<DataLiteral> dataLiterals = new ArrayList<DataLiteral>();

        if (externNodeId == null) {
            id = element.getAttribute("NodeId");
        } else {
            id = externNodeId;
        }

        NodeList childNodeList = element.getChildNodes();

        for (int j = 0; j < childNodeList.getLength(); j++) {
            Element childNode = getNextNodeElement(childNodeList, j);
            if (childNode != null && !childNode.getTagName().equals("References")) {
                if (childNode.getTagName().equals("Description")) {
                    description = (childNode.getTextContent() + "@" + childNode.getAttribute("Locale"))
                            .replaceAll("[“”\"\\\\]", "");
                } else if (childNode.getTagName().equals("DisplayName")) {
                    displayName = childNode.getTextContent().replaceAll("[“”\"_\\\\]", "");
                } else if (childNode.getTagName().equals("Documentation")) {
                    documentation = childNode.getTextContent().replaceAll("[“”\"\\\\]", "");
                } else if (childNode.getTagName().equals("Definition")) {
                    NodeList fields = childNode.getChildNodes();

                    for (int k = 0; k < fields.getLength(); k++) {
                        Element fieldNode = getNextNodeElement(fields, k);
                        if (fieldNode != null) {
                            String fieldName = "_" + fieldNode.getAttribute("Name").replaceAll("[,“”\"\\\\]", "_");
                            if (fieldName.equals("") || fieldName.equals("_")) {
                                fieldName = "placeholder_"
                                        + childNode.getAttribute("Name").replaceAll("[/,“”\"\\\\]", "_");
                            } else {
                                fieldName = fieldName.replace("µ", "mu");
                                fieldName = fieldName.replace("/", "_per_");
                                fieldName = fieldName.replace("²", "_toPowerOf2");
                                fieldName = fieldName.replace("³", "_toPowerOf3");
                                fieldName = fieldName.replace("°", "degree_");
                            }
                            String fieldDescription = getFieldDescription(fieldNode);
                            String fieldValue = fieldNode.getAttribute("Value");
                            String fieldDataType = "";
                            if (fieldValue.equals("")) {
                                fieldDataType = fieldNode.getAttribute("DataType");
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
            } else if (childNode != null && childNode.getTagName().equals("References")) {
                if (type.equals(ElementType.ROOTOBJECT) || type.equals(ElementType.SUBOBJECT)
                        || type.equals(ElementType.ROOTMETHOD) || type.equals(ElementType.SUBMETHOD)) {
                    objectFields = identifyFields(childNode);
                }
                if (type != ElementType.ENUM) {
                    if (type != ElementType.FIELDOBJECT && type != ElementType.FIELDMETHOD
                            && type != ElementType.SUBMETHOD && type != ElementType.ROOTMETHOD) {
                        typeDef = identifySpecificReference("HasTypeDefinition", childNode, type);
                    }
                    if (type != ElementType.OBJECTTYPE && type != ElementType.VARIABLETYPE
                            && type != ElementType.DATATYPE) {
                        modellingRule = identifySpecificReference("HasModellingRule", childNode, type);
                        if (modellingRule.toUpperCase().contains("OPTIONAL")) {
                            optional = true;
                        }
                    }
                }
            }
        }
        createElement(type, element, id, displayName, description, documentation, subFields, objectFields, literals,
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
                if (fieldChildNode.getTagName().equals("Description")) {
                    if (fieldChildNode.getAttribute("Locale").equals("")) {
                        fieldDescription = fieldChildNode.getTextContent().replaceAll("[“”\"\\\\]", "");
                    } else {
                        fieldDescription = (fieldChildNode.getTextContent() + "@"
                                + fieldChildNode.getAttribute("Locale")).replaceAll("[“”\"\\\\]", "");
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
     * @param id            the id
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
    private void createElement(ElementType type, Element element, String id, String displayName, String description,
            String documentation, ArrayList<FieldType> subFields, ArrayList<FieldType> objectFields,
            ArrayList<EnumLiteral> literals, ArrayList<DataLiteral> dataLiterals, String typeDef, boolean optional) {

        String dataType;

        switch (type) {
        case ROOTOBJECT:
            ObjectType uaRootObject = new RootObjectType(id,
                    element.getAttribute("BrowseName").replaceAll("[“”\"\\\\]", ""), displayName, description, optional,
                    BaseType.validateVarName("opc" + typeDef),
                    retrieveParent(element.getAttribute("ParentNodeId"), objectTypeList), objectFields);
            uaRootObject.setVarName(retrieveParent(element.getAttribute("ParentNodeId"), objectTypeList) + displayName);
            if (!objectFields.isEmpty()) {
                adaptDatatypesToModel(uaRootObject, null);
            }
            addElement(uaRootObject, type);
            if (!uaRootObject.getFields().isEmpty()) {
                retrieveRelatedSubElements(uaRootObject.getFields());
            }
            break;
        case ROOTVARIABLE:
            dataType = element.getAttribute("DataType");
            if (dataType.equals("EnumValueType")) {
                Element relatedDataTypeElement = checkRelation(element.getAttribute("ParentNodeId"), dataTypeList);
                NodeList dataChildNodeList = relatedDataTypeElement.getChildNodes();
                for (int j = 0; j < dataChildNodeList.getLength(); j++) {
                    Element childNode = getNextNodeElement(dataChildNodeList, j);
                    if (childNode != null) {
                        if (childNode.getTagName().equals("DisplayName")) {
                            dataType = childNode.getTextContent();
                        }
                    }
                }
            } else {
                dataType = changeVariableDataTypes(dataType);
            }
            String dimension = element.getAttribute("ArrayDimensions");
            if (dimension.contains(",")) {
                dimension = dimension.substring(0, dimension.indexOf(","));
            }
            RootVariableType uaRootVariable = new RootVariableType(id,
                    element.getAttribute("BrowseName").replaceAll("[“”\"\\\\]", ""), displayName, description, dataType,
                    BaseType.validateVarName("opc" + typeDef + "Type"), optional, element.getAttribute("AccessLevel"),
                    element.getAttribute("ValueRank"), dimension,
                    retrieveParent(element.getAttribute("ParentNodeId"), objectTypeList));
            uaRootVariable
                    .setVarName(retrieveParent(element.getAttribute("ParentNodeId"), objectTypeList) + displayName);
            addElement(uaRootVariable, type);
            break;
        case ROOTMETHOD:
            RootMethodType uaRootMethod = new RootMethodType(id,
                    element.getAttribute("BrowseName").replaceAll("[“”\"\\\\]", ""), displayName, description, optional,
                    null, retrieveParent(element.getAttribute("ParentNodeId"), objectTypeList), objectFields);
            uaRootMethod.setVarName(retrieveParent(element.getAttribute("ParentNodeId"), objectTypeList) + displayName);
            if (!objectFields.isEmpty()) {
                adaptDatatypesToModel(uaRootMethod, null);
            }
            addElement(uaRootMethod, type);
            if (!uaRootMethod.getFields().isEmpty()) {
                retrieveRelatedSubElements(uaRootMethod.getFields());
            }
            break;
        case SUBOBJECT:
            ObjectType uaSubObject = new ObjectType(id, element.getAttribute("BrowseName").replaceAll("[“”\"\\\\]", ""),
                    displayName, description, optional, BaseType.validateVarName(typeDef), objectFields);
            uaSubObject.setVarName(searchVarName(uaSubObject, hierarchy));
            if (!objectFields.isEmpty()) {
                adaptDatatypesToModel(uaSubObject, null);
            }
            println(uaSubObject.toString());
            hierarchy.add(uaSubObject);
            if (!uaSubObject.getFields().isEmpty()) {
                retrieveRelatedSubElements(uaSubObject.getFields());
            }
            break;
        case SUBMETHOD:
            MethodType uaMethod = new MethodType(id, element.getAttribute("BrowseName").replaceAll("[“”\"\\\\]", ""),
                    displayName, description, optional, objectFields);
            uaMethod.setVarName(searchVarName(uaMethod, hierarchy));
            if (!objectFields.isEmpty()) {
                adaptDatatypesToModel(null, uaMethod);
            }
            println(uaMethod.toString());
            hierarchy.add(uaMethod);
            if (!uaMethod.getFields().isEmpty()) {
                retrieveRelatedSubElements(uaMethod.getFields());
            }
            break;
        case FIELDOBJECT:
            FieldObjectType uaFieldObject = new FieldObjectType(id,
                    element.getAttribute("BrowseName").replaceAll("[“”\"\\\\]", ""), displayName, description, "",
                    optional);
            uaFieldObject.setVarName("opc" + displayName);
            if (!checkRedundancy(uaFieldObject.getVarName(), subFields)) {
                subFields.add(uaFieldObject);
            }
            break;
        case FIELDVARIABLE:
            dataType = element.getAttribute("DataType");
            if (dataType.equals("EnumValueType")) {
                Element relatedDataTypeElement = checkRelation(element.getAttribute("ParentNodeId"), dataTypeList);
                NodeList dataChildNodeList = relatedDataTypeElement.getChildNodes();
                for (int j = 0; j < dataChildNodeList.getLength(); j++) {
                    Element childNode = getNextNodeElement(dataChildNodeList, j);
                    if (childNode != null) {
                        if (childNode.getTagName().equals("DisplayName")) {
                            dataType = childNode.getTextContent();
                        }
                    }
                }
            } else {
                dataType = changeVariableDataTypes(dataType);
            }
            String fieldDimension = element.getAttribute("ArrayDimensions");
            if (fieldDimension.contains(",")) {
                fieldDimension = fieldDimension.substring(0, fieldDimension.indexOf(","));
            }
            FieldVariableType uaFieldVariable = new FieldVariableType(id,
                    element.getAttribute("BrowseName").replaceAll("[“”\"\\\\]", ""), displayName, description, dataType,
                    BaseType.validateVarName("opc" + typeDef + "Type"), optional, element.getAttribute("AccessLevel"),
                    element.getAttribute("ValueRank"), fieldDimension);
            uaFieldVariable.setVarName(retrieveParent(element.getAttribute("ParentNodeId"), objectList) + displayName);
            if (!checkRedundancy(uaFieldVariable.getVarName(), subFields)) {
                subFields.add(uaFieldVariable);
            }
            break;
        case FIELDMETHOD:
            FieldMethodType uaFieldMethod = new FieldMethodType(id,
                    element.getAttribute("BrowseName").replaceAll("[“”\"\\\\]", ""), displayName, description, "",
                    optional);
            uaFieldMethod.setVarName("opc" + displayName);
            if (!checkRedundancy(uaFieldMethod.getVarName(), subFields)) {
                subFields.add(uaFieldMethod);
            }
            break;
        case ENUM:
            EnumType enumeration = new EnumType(id, element.getAttribute("BrowseName").replaceAll("[“”\"\\\\]", ""),
                    displayName, description, documentation, literals);
            enumeration.setVarName("opc" + displayName + "Type");
            addElement(enumeration, type);
            break;
        case DATATYPE:
            DataType uaDataType = new DataType(id, element.getAttribute("BrowseName").replaceAll("[“”\"\\\\]", ""),
                    displayName, description, documentation, dataLiterals);
            uaDataType.setVarName("opc" + displayName + "Type");
            addElement(uaDataType, type);
            break;
        case OBJECTTYPE:
            ObjectTypeType uaObjectType = new ObjectTypeType(id,
                    element.getAttribute("BrowseName").replaceAll("[“”\"\\\\]", ""), displayName, description,
                    documentation);
            uaObjectType.setVarName("opc" + displayName);
            addElement(uaObjectType, type);
            break;
        case VARIABLETYPE:
            VariableTypeType uaVariableType = new VariableTypeType(id,
                    element.getAttribute("BrowseName").replaceAll("[“”\"\\\\]", ""), displayName, description,
                    documentation, changeVariableDataTypes(element.getAttribute("DataType")));
            uaVariableType.setVarName("opc" + displayName + "Type");
            addElement(uaVariableType, type);
            break;
        default:
            break;
        }
    }

    /**
     * Adds an element to the hierarchy.
     * 
     * @param element the element
     * @param type    the element type
     */
    private void addElement(BaseType element, ElementType type) {
        if (!checkRedundancy(element.getVarName(), null)) {
            println(element.toString());
            hierarchy.add(element);
        }
    }

    /**
     * Checks for redundant/duplicate variable names in {@link #hierarchy}.
     * 
     * @param varName the variable name to check for
     * @param list    the list of fields to check
     * @return {@code true} if there are duplicates, {@code false} else
     */
    private boolean checkRedundancy(String varName, ArrayList<FieldType> list) {
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
    private void retrieveElementTypes() {

        for (int i = 0; i < objectTypeList.getLength(); i++) {
            Element objectType = getNextNodeElement(objectTypeList, i);
            if (objectType != null) {
                retrieveAttributes(objectType, null, ElementType.OBJECTTYPE, null);
            }
        }
        for (int i = 0; i < dataTypeList.getLength(); i++) {
            Element dataType = getNextNodeElement(dataTypeList, i);
            if (dataType != null) {
                retrieveAttributes(dataType, null, ElementType.DATATYPE, null);
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
     * @param parser        the parser instance
     * @param modelName     the model name to check for
     * @param path          the path to check for
     * @param fileName      the file name to check for
     * @param nameSpaceUris the OPC namespace URIs
     * @return the found/required model files
     */
    private static File[] checkRequiredModels(DomParser parser, String modelName, String path, String fileName,
            NodeList nameSpaceUris) {
        Scanner scanner = new Scanner(System.in);
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
                        if (childNode.getTagName().equals("Uri")) {
                            uris.add(childNode.getTextContent());
                        }
                    }
                }
            }
        }
        int nameSpace = 0;
        int ns = 0;
        for (Iterator<String> iterator = uris.iterator(); iterator.hasNext();) {
            String value = iterator.next();
            if (value.equals(modelName)) {
                nameSpace = ns;
                parser.setBaseNameSpace(Integer.toString(nameSpace));
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
        File f = new File(path, "/RequiredModels");
        do {
            if (!f.exists()) {
                File requiredModels = new File(path, "/RequiredModels");
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
                File requiredModels = new File(path, "/RequiredModels");
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
                        s = StringUtils.removeEnd(s.replace("http://opcfoundation.org/UA/", ""), "/").replace("/", ".")
                                .toUpperCase();

                        for (int i = 0; i < files.length; i++) {
                            String model = null;
                            if (toOsPath(files[i]).equals(toOsPath(path + "/RequiredModels/Opc.Ua.NodeSet2.xml"))) {
                                model = "UA";
                            } else {
                                model = toOsPath(files[i]).toUpperCase()
                                        .replace(toOsPath(path.toUpperCase() + "/REQUIREDMODELS/OPC.UA."), "")
                                        .replace(".NODESET2.XML", "");
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
    private void parseFile() {
        // rootObjects
        for (int i = 0; i < objectList.getLength(); i++) {
            Element object = getNextNodeElement(objectList, i);
            if (object != null) {
                String parentNodeId = object.getAttribute("ParentNodeId");
                Element rootObject = checkRelation(parentNodeId, objectTypeList);
                if (rootObject != null) {
                    retrieveRootElement(object, ElementType.ROOTOBJECT);
                }
            }
        }
        // rootVars
        for (int i = 0; i < variableList.getLength(); i++) {
            Element variable = getNextNodeElement(variableList, i);
            if (variable != null) {
                String parentNodeId = variable.getAttribute("ParentNodeId");
                Element rootVariable = checkRelation(parentNodeId, objectTypeList);
                if (rootVariable != null) {
                    retrieveRootElement(variable, ElementType.ROOTVARIABLE);
                }
            }
        }
        // rootMethods
        for (int i = 0; i < methodList.getLength(); i++) {
            Element method = getNextNodeElement(methodList, i);
            if (method != null) {
                String parentNodeId = method.getAttribute("ParentNodeId");
                Element rootMethod = checkRelation(parentNodeId, objectTypeList);
                if (rootMethod != null) {
                    retrieveRootElement(method, ElementType.ROOTMETHOD);
                }
            }
        }
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
    private static DomParser createParser(String path, File compSpec, boolean verbose) {
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
            NodeList methodList = doc.getElementsByTagName("UAMethod");
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
                            break;
                        }
                    }
                }
            }
            ArrayList<BaseType> hierarchy = new ArrayList<BaseType>();

            parser = new DomParser(objectTypeList, objectList, variableList, methodList, dataTypeList, variableTypeList,
                    aliasList, hierarchy);
            File[] reqModels = checkRequiredModels(parser, modelName, path,
                    toOsPath(compSpec).replace(toOsPath(path + "/Opc.Ua."), "").replace(".NodeSet.xml", ""),
                    nameSpaceUris);
            Document[] documents = new Document[reqModels.length];
            ArrayList<NodeList> aliasLists = new ArrayList<NodeList>();
            for (int i = 0; i < reqModels.length; i++) {
                System.out.println(reqModels[i]);
                documents[i] = builder.parse(reqModels[i]);
                NodeList externAliasList = documents[i].getElementsByTagName("Aliases");
                aliasLists.add(externAliasList);
            }
            parser.setExternAliasLists(aliasLists);
            parser.setDocuments(documents);
            parser.verbose = verbose;
            parser.parseFile();
            Collector.collectInformation(compSpec.getName(), objectTypeList, objectList, variableList, methodList,
                    dataTypeList, variableTypeList, hierarchy, reqModels.length);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            LoggerFactory.getLogger(DomParser.class).error(e.getMessage());
        }
        return parser;
    }

    /**
     * Sets the folder where to generate example using IVML models.
     * 
     * @param folder the folder name (by default "src/test/easy")
     */
    public static void setUsingIvmlFolder(String folder) {
        usingIvmlFolder = folder;
    }

    /**
     * Creates the IVML model in the given {@code fileName}.
     * 
     * @param fileName the file name for the IVML model
     * @param ivmlFile the output file
     */
    private void createIvmlModel(String fileName, File ivmlFile) {
        Generator.generateIVMLModel(fileName, ivmlFile, hierarchy);
        Generator.generateVDWConnectorSettings(fileName, hierarchy, usingIvmlFolder);
        println("FINISHED");
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
     * Processes an OPC XML file. [public for testing]
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

    // checkstyle: stop method length check

    /**
     * Executes the parser, per default in verbose mode.
     * 
     * @param args command line arguments (ignored)
     */
    public static void main(String[] args) {
        File file;
        ArrayList<File> files = new ArrayList<File>();
        if (args.length == 1) {
            file = new File(args[0]);
        } else {
            file = new File("src/main/resources/NodeSets/Opc.Ua.Woodworking.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.MachineTool.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.Weihenstephan.NodeSet2.xml");
            files.add(file);
            /*file = new File("src/main/resources/NodeSets/Opc.Ua.Adi.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.AutoID.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.CNC.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.BACnet.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.CAS.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.CommercialKitchenEquipment.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.CSPPlusForMachine.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.DEXPI.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.Sercos.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.TMC.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.Scheduler.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.Scales.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.Safety.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.RSL.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.Robotics.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.Pumps.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.POWERLINK.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.PnRio.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.PnEm.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.Pn.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.PLCopen.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.OPENSCS.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.Onboarding.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.AMLLibraries.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.AMB.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.Fdi5.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.Fdi7.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.FDT.NodeSet.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.fx.ac.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.fx.cm.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.fx.data.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.Glass.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.I4AAS.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.IEC61850-6.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.IEC61850-7-3.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.IEC61850-7-4.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.Ijt.Tightening.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.IOLink.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.IOLinkIODD.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.IRDI.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/opc.ua.isa95-jobcontrol.nodeset2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.ISA95.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.MachineVision.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.MDIS.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.MTConnect.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.Mining.DevelopmentSupport.Dozer.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.Mining.DevelopmentSupport.General.NodeSet2.xml");
            files.add(file);
            file = new File(
                    "src/main/resources/NodeSets/Opc.Ua.Mining.DevelopmentSupport.RoofSupportSystem.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.Mining.Extraction.General.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.Mining.Extraction.ShearerLoader.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.Mining.General.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.Mining.Loading.General.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.Mining.Loading.HydraulicExcavator.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.Mining.MineralProcessing.General.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.Mining.MineralProcessing.RockCrusher.NodeSet2.xml");
            files.add(file);
            file = new File(
                    "src/main/resources/NodeSets/Opc.Ua.Mining.MonitoringSupervisionServices.General.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.Mining.PELOServices.FaceAlignmentSystem.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.Mining.PELOServices.General.NodeSet2.xml");
            files.add(file);
            file = new File(
                    "src/main/resources/NodeSets/Opc.Ua.Mining.TransportDumping.ArmouredFaceConveyor.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.Mining.TransportDumping.General.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.Mining.TransportDumping.RearDumpTruck.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.PlasticsRubber.Extrusion_v2.Calender.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.PlasticsRubber.Extrusion_v2.Calibrator.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.PlasticsRubber.Extrusion_v2.Corrugator.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.PlasticsRubber.Extrusion_v2.Cutter.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.PlasticsRubber.Extrusion_v2.Die.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.PlasticsRubber.Extrusion_v2.Extruder.NodeSet2.xml");
            files.add(file);
            file = new File(
                    "src/main/resources/NodeSets/Opc.Ua.PlasticsRubber.Extrusion_v2.ExtrusionLine.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.PlasticsRubber.Extrusion_v2.Filter.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.PlasticsRubber.Extrusion_v2.GeneralTypes.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.PlasticsRubber.Extrusion_v2.HaulOff.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.PlasticsRubber.Extrusion_v2.MeltPump.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.PlasticsRubber.Extrusion_v2.Pelletizer.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.PlasticsRubber.GeneralTypes.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.PlasticsRubber.HotRunner.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.PlasticsRubber.IMM2MES.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.PlasticsRubber.LDS.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.PlasticsRubber.TCD.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.PackML.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.DI.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.IA.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.Gds.NodeSet2.xml");
            files.add(file);
            file = new File("src/main/resources/NodeSets/Opc.Ua.Machinery.NodeSet2.xml");
            files.add(file);*/
        }
        for (File f : files) {
            file = f;
            String fileName = file.getName();
            fileName = StringUtils.removeStart(fileName, "Opc.Ua");
            fileName = StringUtils.removeEnd(fileName, ".xml");
            fileName = StringUtils.removeEnd(fileName, ".NodeSet2");
            fileName = fileName.replace(".", "");
            fileName = fileName.replace("-", "_");
            File ivmlFile = new File("gen/Opc" + fileName + ".ivml");
            process(file, fileName, ivmlFile, verboseDefault);
        }
        Collector.informationToExcel();
    }
    // checkstyle: resume method length check

}