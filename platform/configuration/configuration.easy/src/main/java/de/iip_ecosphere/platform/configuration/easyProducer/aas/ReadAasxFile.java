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

package de.iip_ecosphere.platform.configuration.easyProducer.aas;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import static de.iip_ecosphere.platform.configuration.easyProducer.aas.ParsingUtils.*;

import de.iip_ecosphere.platform.configuration.easyProducer.aas.AasType.EntityType;
import de.iip_ecosphere.platform.support.Version;
import de.iip_ecosphere.platform.support.aas.IdentifierType;
import de.iip_ecosphere.platform.support.aas.SemanticIdRecognizer;
import de.iip_ecosphere.platform.support.logging.Logger;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;

/**
 * Translates AASX IDTA spec files to IVML. Does not rely on AAS abstraction/Basyx as these AASX files cannot be read.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ReadAasxFile {
    
    /**
     * Returns the spec number of an AASX file from its file name.
     * 
     * @param file the file
     * @return the spec number, may be 0000 for unknown
     */
    public static String getSpecNumber(File file) {
        String fileName = file.getName();
        if (fileName.endsWith(".aasx")) {
            fileName = fileName.substring(0, fileName.length() - 5);
        }
        return getSpecNumber(file.getParentFile().getName() + "/" + fileName);
    }

    /**
     * Returns the spec number of an AASX file from its file name.
     * 
     * @param fileName the file name
     * @return the spec number, may be 0000 for unknown
     */
    public static String getSpecNumber(String fileName) {
        int pos = fileName.indexOf("/");
        String specNumber = "0000";
        if (pos > 0) {
            specNumber = fileName.substring(0, pos);
        } 
        return specNumber;
    }

    /**
     * Reads the given AASX file and translates it to {@code target} IVML.
     * 
     * @param source the source file
     * @param target the target file
     * @param specNumber the specification number
     * @throws IOException if reading/writing fails
     */
    public static void readFile(String source, String target, String specNumber) throws IOException {
        readFile(source, target, specNumber, null);
    }

    /**
     * Reads the given AASX file and returns a spec summary.
     * 
     * @param source the source file
     * @param specNumber the specification number
     * @return the spec summary or <b>null</b>
     * @throws IOException if reading/writing fails
     */
    public static AasSpecSummary read(String source, String specNumber) throws IOException {
        AasSpecSummary result = null;
        ZipInputStream zin = new ZipInputStream(new FileInputStream(source));
        ZipEntry ze = null;
        Document document = null;
        while ((ze = zin.getNextEntry()) != null) {
            String name = ze.getName();
            if (name.startsWith("aasx/") && name.endsWith(".xml")) {
                getLogger().info("Reading " + name);                
                try {
                    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
                    document = docBuilder.parse(zin);        
                } catch (SAXException | ParserConfigurationException e) {
                    throw new IOException(e);
                }
                break; // closed by parse
            }
        }
        if (null != document) {
            XmlReader reader = new XmlReader();
            result = reader.readFromXml(document, specNumber);
        }
        return result;
    }
    
    /**
     * Reads the given AASX file and translates it to {@code target} IVML.
     * 
     * @param source the source file
     * @param target the target file
     * @param specNumber the specification number
     * @param idShortPrefix optional prefix for idShorts, may be <b>null</b> (use "name" instead)
     * @throws IOException if reading/writing fails
     */
    public static void readFile(String source, String target, String specNumber, String idShortPrefix) 
        throws IOException {
        AasSpecSummary result = read(source, specNumber);
        if (null != result) {
            System.out.println(source);
            result.printStatistics(System.out);
            IvmlWriter writer = new IvmlWriter(target)
                .setNamePrefix(idShortPrefix);
            writer.toIvml(result);
            writer.toIvmlText(result);
        }
    }

    /**
     * Iterates over childs.
     * 
     * @param node the node to iterate over
     * @param function the function to apply to each child
     */
    private static void iterateChilds(Node node, Consumer<Node> function) {
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node currentNode = nodeList.item(i);
            if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                function.accept(currentNode);
            }
        }
    }
    
    /**
     * Returns the number of iterable childs.
     * 
     * @param node the node
     * @param pred a predicate to focus the counting, may be <b>null</b> for none
     * @return the number of childs
     * @see #iterateChilds(Node, Consumer)
     */
    private static int getIterableChildsCount(Node node, Predicate<Node> pred) {
        AtomicInteger result = new AtomicInteger(0);
        iterateChilds(node, n -> {
            if (null == pred || pred.test(n)) { 
                result.incrementAndGet();
            }
        });
        return result.get();
    }

    /**
     * Represents an intermediary type result with additional values to be considered by the caller.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class AasTypeResult {
       
        private AasType type;
        private int[] cardinality; 
        private String exampleValue;
        private String version;
        
        /**
         * Creates an instance.
         * 
         * @param type the type
         * @param cardinality the cardinality (may be <b>null</b> for none)
         * @param exampleValue the example value (may be <b>null</b> for none)
         * @param version the version information (may be <b>null</b> for none)
         */
        private AasTypeResult(AasType type, int[] cardinality, String exampleValue, String version) {
            this.type = type;
            this.cardinality = cardinality;
            this.exampleValue = exampleValue;
            this.version = version;
        }
        
    }

    /**
     * Reads AAS xml nodes.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class XmlReader {
        
        private List<AasType> types = new ArrayList<>();
        private Map<String, List<AasType>> semIdTypes = new HashMap<>();
        private Map<String, List<AasField>> semIdFields = new HashMap<>();
        private List<AasEnum> enums = new ArrayList<>();
        private AasEnumResultHandler enumsHandler = new AasEnumResultHandler(enums);
        private Map<String, List<AasEnum>> semIdEnums = new HashMap<>();
        private List<ConceptDescription> concepts = new ArrayList<>();

        /**
         * Reads from the given XML document.
         * 
         * @param document the document
         * @param specNumber the specification number
         * @return the spec summary for {@link IvmlWriter}
         */
        AasSpecSummary readFromXml(Document document, String specNumber) {
            AtomicReference<String> version = new AtomicReference<>(null);
            iterateChilds(document.getDocumentElement(), n -> {
                String name = getName(n);
                switch (name) {
                case "submodels":
                    String ver = readSubmodels(n);
                    if (version.get() == null) {
                        version.set(ver);
                    }
                    break;
                case "conceptDescriptions":
                    readConceptDescriptions(n);
                    break;
                default:
                    break;
                }
            });
            for (AasEnum en: enums) {
                registerSemanticId(semIdEnums, en);
            }
            for (ConceptDescription cd: concepts) {
                if (cd.identification != null) {
                    List<AasType> types = semIdTypes.get(cd.identification);
                    if (null != types) {
                        for (AasType t : types) {
                            getLogger().info("Applying concept description to type " + t.getIdShort());
                            applyConceptDescription(t, cd);
                        }
                    } 
                    List<AasField> fields = semIdFields.get(cd.identification);
                    if (null != fields) {
                        for (AasField f : fields) {
                            getLogger().info("Applying concept description to field " + f.getIdShort());    
                            applyConceptDescription(f, cd);
                        }
                    } 
                    List<AasEnum> enums = semIdEnums.get(cd.identification);
                    if (null != enums) {
                        for (AasEnum e: enums) {
                            getLogger().info("Applying concept description to enum " + e.getIdShort());    
                            applyConceptDescription(e, cd);
                        }
                    }
                }
            }
            AasSpecSummary result = new AasSpecSummary(types, enums);
            Version ver = null;
            if (null != version.get()) {
                ver = new Version(version.get());
            }
            result.setIdentifier(null, null, ver, null, specNumber);                
            return result;
        }
        
        /**
         * Returns the name of {@code node}, stripping potential prefixes like "aas:".
         * 
         * @param node the node
         * @return the node name name, eventually without prefix
         */
        private static String getName(Node node) {
            String name = node.getNodeName();
            int pos = name.indexOf(":");
            if (pos > 0) {
                name = name.substring(pos + 1);
            }
            return name;
        }

        /**
         * Registers a semantic id.
         * 
         * @param <E> the element type
         * @param map the map storing the id-element relations
         * @param element the element to register
         */
        private <E extends AbstractAasElement> void registerSemanticId(Map<String, List<E>> map, E element) {
            String sId = element.getSemanticId();
            if (sId != null && sId.length() > 0) {
                List<E> elements = map.get(sId);
                if (null == elements) {
                    elements = new ArrayList<E>();
                    map.put(sId, elements);
                }
                elements.add(element);
            }
        }
        
        /**
         * Applies a concept description to {@code elt}.
         * 
         * @param elt the element to apply to
         * @param cd the concept description
         */
        private void applyConceptDescription(AbstractAasElement elt, ConceptDescription cd) {
            elt.setIsCaseOf(cd.isCaseOf);
            String desc = elt.getDescription();
            String cdDesc = cd.getDescription();
            if (cdDesc != null) {
                if (desc == null || desc.length() == 0) {
                    elt.setDescription(removeLinebreaks(cdDesc));
                } else if (!desc.equals(cdDesc) && !desc.contains(cdDesc) 
                    && (desc.startsWith("Note: ") || desc.startsWith("Recommendation: "))) {
                    // works in 02002-1-0 or 02003-1-2, but not in 02007-1-0
                    elt.setDescription(removeLinebreaks(cdDesc + " " + desc));
                }
            }
        }

        /**
         * Reads a set of submodels.
         * 
         * @param node the XML node representing the submodels
         * @return the version
         */
        private String readSubmodels(Node node) {
            AtomicReference<String> version = new AtomicReference<>(null);
            iterateChilds(node, submodel -> {
                AasTypeResult result = readAsAasType(submodel, AasSmeType.SUBMODEL);
                if (version.get() == null) {
                    version.set(result.version);
                }
            });
            return version.get();
        }
        
        // checkstyle: stop method length check
        
        /**
         * Reads the given {@code node} as {@link AasType} with given {@code smeType}.
         * 
         * @param node the XML node to be read
         * @param smeType the SME type
         * @return the reading result
         */
        private AasTypeResult readAsAasType(Node node, AasSmeType smeType) {
            AtomicReference<String> idShort = new AtomicReference<>("");
            AtomicReference<String> semanticId = new AtomicReference<>("");
            AtomicReference<String> identification = new AtomicReference<>(null);
            AtomicReference<String> entityType = new AtomicReference<>(null);
            AtomicReference<String> description = new AtomicReference<>("");
            AtomicReference<String> formInfoValue = new AtomicReference<>(""); // alt description
            AtomicReference<String> displayName = new AtomicReference<>(""); // alt description
            AtomicReference<String> exampleValue = new AtomicReference<>("");
            AtomicReference<String> version = new AtomicReference<>(null);
            AtomicReference<int[]> cardinality = new AtomicReference<>(null);
            AtomicBoolean fixedIdShort = new AtomicBoolean(false);
            AtomicBoolean ordered = new AtomicBoolean(false);
            AtomicBoolean allowDuplicates = new AtomicBoolean(false);
            AtomicBoolean isMultiValued = new AtomicBoolean(false);
            iterateChilds(node, field -> {
                String name = getName(field);
                switch (name) {
                case "idShort":
                    String tmp = field.getTextContent().trim();
                    if (tmp.endsWith("{00}")) {
                        tmp = tmp.substring(0, tmp.length() - 4);
                        isMultiValued.set(true);
                    }
                    getLogger().info("Reading type {}", tmp);
                    idShort.set(tmp);
                    break;
                case "semanticId":
                    semanticId.set(getSemanticId(field));
                    break;
                case "description":
                    description.set(getDescription(field, null, false, false));
                    fixedIdShort.set(hasFixedIdShort(description.get()));
                    break;
                case "ordered":
                    ordered.set(getBoolean(field));
                    break;
                case "allowDuplicates":
                    allowDuplicates.set(getBoolean(field));
                    break;
                case "qualifiers": // IDTA 02023-0-9
                case "qualifier":
                    readQualifier(field, c -> cardinality.set(c), ex -> exampleValue.set(ex), (t, v) -> {
                        if ("FormInfo".equals(t)) {
                            formInfoValue.set(v);
                        }
                    });
                    break;
                case "administration":
                    version.set(readAdministration(field));
                    break;
                case "id": // IDTA-02023-0-9, IDTA 02021-1-0
                case "identification": // aas linking, fallback semanticId
                    identification.set(readSemanticId(field));
                    break;
                case "entityType": 
                    entityType.set(node.getTextContent().trim());
                    break;
                case "supplementalSemanticIds": // additional semanticIDs, ignored for now
                case "category": // ignored
                case "value": // see below
                case "typeValueListElement": // see below
                case "submodelElements": // see below
                case "statements": // see below
                case "kind": // -> template
                case "embeddedDataSpecifications": // IDTA-02023-0-9 
                case "displayName": // IDTA-02023-0-9
                    String dpn = readDisplayName(field);
                    if (dpn.length() > 0) {
                        displayName.set(dpn);
                    }
                    break;
                default:
                    getLogger().warn("Not reading submodel element/entity property: " + name);
                    break;
                } 
            });
            AasType type = new AasType(idShort.get(), false, isMultiValued.get()); // TODO fixedId??
            type.setAllowDuplicates(allowDuplicates.get());
            type.setOrdered(ordered.get());
            type.setSemanticId(semanticId.get() != null && semanticId.get().length() > 0 
                ? semanticId.get() : identification.get()); // IDTA02007-1-0
            String desc = description.get();
            if (desc.length() == 0) {
                desc = formInfoValue.get();
                if (desc.length() == 0) {
                    desc = displayName.get();
                }
            }
            type.setDescription(removeLinebreaks(desc));
            type.setSmeType(smeType);
            type.setEntityType(EntityType.valueOfSafe(entityType.get()));
            addType(type);
            iterateChilds(node, field -> {
                String name = getName(field);
                switch (name) {
                case "typeValueListElement":
                case "submodelElements":
                case "value":
                case "statements":
                    readSubmodelElement(field, type);
                    break;
                default:
                    // warning handled above
                    break;
                }
            });
            return new AasTypeResult(type, cardinality.get(), exampleValue.get(), version.get());
        }

        // checkstyle: resume method length check
        
        /**
         * Reads the display name.
         * 
         * @param node the node containing the display name contents
         * @return the display name, may be empty for none
         */
        private String readDisplayName(Node node) {
            String result = null;
            Map<String, String> desc = new HashMap<>();
            iterateChilds(node, field -> {
                String name = getName(field);
                switch (name) {
                case "langStringNameType":
                    readLangStringTextType(field, desc);
                    break;
                default:
                    break;
                }
            });
            if (desc.size() > 0) {
                result = desc.get("en");
                if (null == result) {
                    result = desc.values().iterator().next();
                }
            }
            return result == null ? "" : result;
        }
        
        /**
         * Adds a type to {@link #types} and registers {@code type} with {@link #semIdTypes}.
         * 
         * @param type the type to add
         */
        private void addType(AasType type) {
            if (!types.stream().anyMatch(t -> t.getIdShort().equals(type.getIdShort()))) {
                types.add(type);
                registerSemanticId(semIdTypes, type);
            }
        }

        /**
         * Reads the administration entry to the version information if specified.
         * 
         * @param node the node
         * @return the version, or <b>null</b>
         */
        private String readAdministration(Node node) {
            AtomicReference<String> version = new AtomicReference<>(null);
            AtomicReference<String> revision = new AtomicReference<>(null);
            iterateChilds(node, e -> {
                String name = getName(e);
                switch (name) {
                case "version":
                    version.set(e.getTextContent().trim());
                    break;
                case "revision":
                    revision.set(e.getTextContent().trim());
                    break;
                case "templateId": // IDTA02023-0-9
                    break;
                default:
                    getLogger().warn("Unknown administration element: " + name);
                    break;
                }
            });
            String result = null;
            if (version.get() != null) {
                result = version.get();
            }
            if (result != null && revision.get() != null) {
                result += "." + revision.get();
            }
            return result;
        }
        
        /**
         * Consumes a qualifier.
         * 
         * @author Holger Eichelberger, SSE
         */
        private interface QualifierConsumer {

            /**
             * Consumes a qualifier.
             * 
             * @param type the type of the qualifier
             * @param value the value
             */
            public void consume(String type, String value);
            
        }
        
        /**
         * Reads the qualifiers, may be cardinalities or example values.
         * 
         * @param node the node representing the qualifiers
         * @param cardinalitySetter setter for the cardinalities
         * @param exampleValueSetter setter for example values
         * @param fallbackConsumer in case that qualifier type is not known, may be <b>null</b>
         */
        private void readQualifier(Node node, Consumer<int[]> cardinalitySetter, Consumer<String> exampleValueSetter, 
            QualifierConsumer fallbackConsumer) {
            AtomicReference<String> type = new AtomicReference<>("");
            AtomicReference<String> value = new AtomicReference<>("");
            iterateChilds(node, sme -> {
                String name = getName(sme);
                if ("qualifier".equals(name)) {
                    iterateChilds(sme, q -> {
                        String qName = getName(q);
                        switch (qName) {
                        case "type":
                            type.set(q.getTextContent().trim());
                            break;
                        case "value":
                            value.set(q.getTextContent().trim());
                            break;
                        case "valueId": // ignore for now
                        case "valueType": // ignore for now
                        case "semanticId": // ignore for now
                        case "kind": // ignore for now
                            break;
                        default:
                            getLogger().warn("Unconsidered cardinality entry: " + qName);
                            break;
                        }
                    });
                    if ("Cardinality".equals(type.get()) || "Multiplicity".equals(type.get())) {
                        switch (value.get()) {
                        case "OneToMany":
                            cardinalitySetter.accept(new int[] {1, -1});
                            break;
                        case "ZeroToMany":
                            cardinalitySetter.accept(new int[] {0, -1});
                            break;
                        case "ZeroToOne":
                            cardinalitySetter.accept(new int[] {0, 1});
                            break;
                        case "One":
                            cardinalitySetter.accept(new int[] {1, 1});
                            break;
                        default:
                            getLogger().warn("Unconsidered cardinality: " + value.get());
                            break;
                        }
                    } else if ("ExampleValue".equals(type.get())) {
                        exampleValueSetter.accept(value.get());
                    } else {
                        if (null != fallbackConsumer) {
                            fallbackConsumer.consume(type.get(), value.get());
                        }
                    }
                } else {
                    getLogger().warn("Unconsidered qualifier entry: " + name);
                }
            });
        }
        
        /**
         * Reads a set of submodel elements.
         * 
         * @param node the XML node representing the submodels
         * @param type the parent type where to add the fields to
         */
        /*private void readSubmodelElements(Node node, AasType type) {
            iterateChilds(node, sme -> {
                String name = getName(sme);
                switch (name) {
                case "submodelElement":
                    readSubmodelElement(sme, type);
                    break;
                case "submodelElementCollection":
                    readFieldAsNewType(sme, type, AasSmeType.SUBMODEL_ELEMENT_COLLECTION);
                    break;
                case "property":
                    addField(type, readAasProperty(sme, null, AasSmeType.PROPERTY, null));
                    break;
                default:
                    getLogger().warn("Unknown submodel element: " + name);
                    break;
                }
            });
        }*/
        
        /**
         * Reads a submodel elements.
         * 
         * @param node the XML node representing the submodel
         * @param type the parent type where to add the fields to
         */
        private void readSubmodelElement(Node node, AasType type) {
            iterateChilds(node, field -> {
                String name = getName(field);
                switch (name) {
                case "submodelElement":
                    readSubmodelElement(field, type);
                    break;                
                case "submodelElementCollection":
                    readFieldAsNewType(field, type, AasSmeType.SUBMODEL_ELEMENT_COLLECTION);
                    break;
                case "submodelElementList":
                    readFieldAsNewType(field, type, AasSmeType.SUBMODEL_ELEMENT_LIST);
                    break;
                case "entity":
                    readFieldAsNewType(field, type, AasSmeType.ENTITY);
                    break;
                case "property":
                    addField(type, readAasProperty(field, null, AasSmeType.PROPERTY, null));
                    break;
                case "file":
                    addField(type, readAasProperty(field, "AasFileResourceType", AasSmeType.FILE, (fi, no, na) -> {
                        boolean done = false;
                        if (na.equals("mimeType")) {
                            fi.setExampleExplanation(no.getTextContent().trim());
                            done = true;
                        }
                        return done;
                    }));
                    break;
                case "multiLanguageProperty":
                    addField(type, readAasProperty(field, "AasMultiLangStringType", 
                        AasSmeType.MULTI_LANGUAGE_PROPERTY, null));
                    break;
                case "blob":
                    addField(type, readAasProperty(field, "AasBlobType", 
                        AasSmeType.BLOB, null));
                    break;
                case "operation":
                    AasField fld = readAasProperty(field, null, AasSmeType.OPERATION, null);
                    // intentionally incomplete for now
                    addField(type, new AasOperation(fld));
                    break;
                case "range":
                    addField(type, readAasProperty(field, "AasRangeType", 
                        AasSmeType.RANGE, null));
                    break;
                case "relationshipElement":
                    addField(type, readAasProperty(field, "AasRelationType", AasSmeType.RELATION, 
                        (fi, no, na) -> {
                            boolean done = false;
                            if (na.equals("first") || na.equals("second")) {
                                done = true; // used in templates at all?
                            }
                            return done;
                        }));
                    break;
                case "referenceElement":
                    addField(type, readAasProperty(field, "AasReferenceType", 
                        AasSmeType.REFERENCE, null));
                    break;
                default:
                    getLogger().warn("Unconsidered submodel element type: " + name);
                    break;
                }
            });
        }

        /**
         * Reads a field as new type and registers the type.
         * 
         * @param field the XML node representing the field
         * @param parentType the parent type where to add the field
         * @param smeType the SME type of the field/type
         */
        private void readFieldAsNewType(Node field, AasType parentType, AasSmeType smeType) {
            AasField fld = new AasField();
            AasTypeResult eltTypeResult = readAsAasType(field, smeType);
            AasType eltType = eltTypeResult.type;
            fld.setSmeType(AasSmeType.PROPERTY);
            fld.setIdShort(eltType.getIdShort(), eltType.isMultiValued()); // not explicit, taken over
            fld.setValueType(eltType.getIdShort()); 
            fld.setSemanticId(eltType.getSemanticId());
            fld.setDescription(removeLinebreaks(eltType.getDescription()));
            if (null != eltTypeResult.cardinality) {
                fld.setCardinality(eltTypeResult.cardinality[0], eltTypeResult.cardinality[1]);
            }
            if (null != eltTypeResult.exampleValue && eltTypeResult.exampleValue.length() > 0) {
                setExampleValues(eltTypeResult.exampleValue, fld);
            }
            addField(parentType, fld);
        }
        
        /**
         * Adds {@code field} to {@code type} registering {@code field} with {@link #semIdFields}.
         * 
         * @param type the type
         * @param field the field
         */
        private void addField(AasType type, AasField field) {
            if (null != field) { // may be filtered out
                type.addField(field);
                registerSemanticId(semIdFields, field);
            }
        }
        
        /**
         * Sets example values splitting the actual value in {@code data} heuristically into multiple ones if needed.
         * 
         * @param data the data
         * @param field the target field
         */
        private void setExampleValues(String data, AasField field) {
            if (null != data && data.trim().length() > 0) {
                List<String> tokens = new ArrayList<>();
                int lastPos = 0;
                int pos;
                do { // IDTA 02003-1-2
                    pos = data.indexOf("|", lastPos);
                    if (pos > 0) {
                        tokens.add(data.substring(lastPos, pos).trim());
                        lastPos = pos + 1; // token length
                    }
                } while (pos > 0);
                tokens.add(data.substring(lastPos, data.length()).trim());
                field.setExampleValues(tokens.toArray(new String[tokens.size()]));
            }
        }

        /**
         * Represents an AAS concept description.
         * 
         * @author Holger Eichelberger, SSE
         */
        private class ConceptDescription {
            
            @SuppressWarnings("unused")
            private String idShort;
            private String identification;
            private String description;
            @SuppressWarnings("unused")
            private String dataSpecId;
            private String isCaseOf;
            private Map<String, String> iecDefinition = new HashMap<>();

            /**
             * Returns a description string.
             * 
             * @return the description string, or <b>null</b>
             */
            public String getDescription() {
                String result = iecDefinition.get("en");
                if (null == result && iecDefinition.size() > 0) {
                    result = iecDefinition.values().iterator().next();
                }
                if (null == result) {
                    result = description;
                }
                return result;
            }
            
        }
        
        /**
         * Reads concept descriptions from {@code node}.
         * 
         * @param node the XML node representing the concept descriptions
         */
        private void readConceptDescriptions(Node node) {
            iterateChilds(node, cd -> {
                String name = getName(cd);
                if ("conceptDescription".equals(name)) {
                    readConceptDescription(cd);
                }
            });
        }

        /**
         * Reads a concept description from {@code node}.
         * 
         * @param node the XML node representing the concept description
         */
        private void readConceptDescription(Node node) {
            ConceptDescription result = new ConceptDescription();
            iterateChilds(node, field -> {
                String name = getName(field);
                switch (name) {
                case "idShort":
                    result.idShort = field.getTextContent().trim();
                    break;
                case "id": // IDTA02023-0-9, IDTA 02021-1-0 
                case "identification":
                    result.identification = readSemanticId(field);
                    break;
                case "description": // IDTA02007-1-0
                    result.description = getDescription(field, null, false, false);
                    break;
                case "embeddedDataSpecification":
                    readEmbeddedDataSpecification(field, result);
                    break;
                case "embeddedDataSpecifications": // IDTA02023-0-9, IDTA-02021-1-0
                    readEmbeddedDataSpecifications(field, result);
                    break;
                case "isCaseOf":
                    result.isCaseOf = getSemanticId(field);
                    break;
                case "displayName": // IDTA02023-0-9
                case "category": // ignored for now
                case "administration": // ignored for now
                    break;
                default:
                    getLogger().warn("Unconsidered concept description field: " + name);
                    break;
                }
            });
            concepts.add(result);
        }

        /**
         * Reads an embedded data specification from {@code node} into {@code cd}.
         * 
         * @param node the XML node representing the concept description
         * @param cd the concept description
         */
        private void readEmbeddedDataSpecifications(Node node, ConceptDescription cd) {
            iterateChilds(node, spec -> {
                String name = getName(spec);
                switch (name) {
                case "embeddedDataSpecification":
                    readEmbeddedDataSpecification(spec, cd);
                    break;
                default:
                    getLogger().warn("Unconsidered embedded data specifications entry: " + name);
                    break;
                }
            });
        }

        /**
         * Reads an embedded data specification from {@code node} into {@code cd}.
         * 
         * @param node the XML node representing the concept description
         * @param cd the concept description
         */
        private void readEmbeddedDataSpecification(Node node, ConceptDescription cd) {
            iterateChilds(node, spec -> {
                String name = getName(spec);
                switch (name) {
                case "dataSpecificationContent":
                    readDataSpecificationContent(spec, cd);
                    break;
                case "dataSpecification":
                    cd.dataSpecId = getSemanticId(spec);
                    break;
                default:
                    getLogger().warn("Unconsidered embedded data specification field: " + name);
                    break;
                }
            });
        }

        /**
         * Reads an embedded data specification from {@code node} into {@code cd}.
         * 
         * @param node the XML node representing the concept description
         * @param cd the concept description
         */
        private void readDataSpecificationContent(Node node, ConceptDescription cd) {
            iterateChilds(node, c -> {
                String name = getName(c);
                switch (name) {
                case "dataSpecificationIec61360": // IDTA-02021-1-0
                case "dataSpecificationIEC61360":
                    readDataSpecificationIEC61360(c, cd);
                    break;
                default:
                    getLogger().warn("Unconsidered data specification content field: " + name);
                    break;
                }
            });
        }

        /**
         * Reads a data specification according to IEC61360 from {@code node} into {@code cd}.
         * 
         * @param node the XML node representing the concept description
         * @param cd the concept description
         */
        private void readDataSpecificationIEC61360(Node node, ConceptDescription cd) {
            iterateChilds(node, c -> {
                String name = getName(c);
                switch (name) {
                case "definition":
                    readIECdefinition(c, cd);
                    break;
                case "preferredName": // ignore for now
                case "symbol": // ignore for now
                case "shortName": // ignore for now
                case "unit": // ignore for now
                case "unitId": // ignore for now
                case "value": // ignore for now
                case "valueFormat": // ignore for now
                case "valueList": // ignore for now
                case "dataType": // ignore for now
                    break;
                default:
                    getLogger().warn("Unconsidered IEC61360 data specification content field: " + name);
                    break;
                }
            });
        }
        
        /**
         * Reads an IEC61360 definition from {@code node} into {@code cd}.
         * 
         * @param node the XML node representing the concept description
         * @param cd the concept description
         */
        private void readIECdefinition(Node node, ConceptDescription cd) {
            iterateChilds(node, d -> {
                String dName = getName(d); // may have multi-lang strings
                if ("langString".equals(dName) || "langStringDefinitionTypeIec61360".equals(dName)) {
                    String lang;
                    String description;
                    if (getIterableChildsCount(d, null) > 0) {
                        AtomicReference<String> cLang = new AtomicReference<>();
                        AtomicReference<String> cDesc = new AtomicReference<>();
                        iterateChilds(d, c -> {
                            String cName = getName(c); 
                            switch (cName) {
                            case "language":
                                cLang.set(c.getTextContent().trim());
                                break;
                            case "text":
                                cDesc.set(c.getTextContent().trim());
                                break;
                            default:
                                break;
                            }
                        });
                        lang = cLang.get();
                        description = cDesc.get();
                    } else {
                        lang = getAttributeValue(d, "lang", "").toLowerCase();
                        description = d.getTextContent().trim();
                    }
                    if (null != lang && null != description) {
                        lang = lang.toLowerCase();
                        description = ParsingUtils.removeLinebreaks(description)
                            .replaceAll(" +", " ")
                            .replace(". .", ".");
                        cd.iecDefinition.put(lang, description);
                    }
                } else {
                    getLogger().warn("Unconsidered IEC description entry: " + dName);
                }
            });
        }

        /**
         * Handler for unknown/specific fields.
         * 
         * @author Holger Eichelberger, SSE
         */
        private interface FieldHandler {
           
            /**
             * Handles additional unhandled entries of {@code node}.
             * 
             * @param field the target field
             * @param node the node
             * @param nodeName name of the unhandled node
             * @return {@code true} for handled, {@code false} for unhandled
             */
            public boolean handle(AasField field, Node node, String nodeName);
        }
        
        /**
         * Reads an AAS property.
         * 
         * @param node the node representing the property
         * @param type the type to use (if not specified in {@code node})
         * @param smeType the SME type
         * @param handler optional handler for unhandled entries
         * @return the created field
         */
        private AasField readAasProperty(Node node, String type, AasSmeType smeType, FieldHandler handler) {
            AtomicReference<Node> descriptionField = new AtomicReference<>(null);
            AasField result = new AasField();
            result.setValueType(type);
            result.setSmeType(smeType);
            iterateChilds(node, field -> {
                String name = getName(field);
                switch (name) {
                case "idShort":
                    String tmp = field.getTextContent().trim();
                    boolean isMultiValued = false;
                    if (tmp.endsWith("{00}")) {
                        tmp = tmp.substring(0, tmp.length() - 4);
                        isMultiValued = true;
                    }
                    getLogger().info("Reading property {}", tmp);
                    result.setIdShort(tmp, isMultiValued);
                    break;
                case "semanticId":
                    result.setSemanticId(getSemanticId(field));
                    break;
                case "description":
                    descriptionField.set(field);
                    break;
                case "qualifier":
                case "qualifiers": // IDTA 02023-9-0
                    // just one?, no example value explanation
                    readQualifier(field, c -> result.setCardinality(c[0], c[1]), e -> setExampleValues(e, result), 
                        null); 
                    break;
                case "valueType":
                    if (type == null) {
                        result.setValueType(field.getTextContent().trim());
                    }
                    break;
                case "value": // seems to be used for examples in IDTA02002-1-0
                    if (getIterableChildsCount(field, null) > 0) {
                        setExampleValues(getDescription(field, result, false, true), result); // at least one option 
                    } else if (field.getTextContent().trim().length() > 0) {
                        setExampleValues(field.getTextContent().trim(), result); // may need inner trimming
                    }
                    break;
                case "inputVariable": // operation, ignored for now
                case "outputVariable": // operation, ignored for now
                case "supplementalSemanticIds": // ignored for now, additional semanticIds, IDTA-2017-1-0
                case "embeddedDataSpecification": 
                case "embeddedDataSpecifications": // IDTA02023-0-9
                case "valueId": // ignored for now
                case "kind": // ignored for now
                case "category": // ignored for now
                case "displayName": // IDTA-02023-0-9
                    break;
                default:
                    boolean done = false;
                    if (null != handler) {
                        done = handler.handle(result, field, name);
                    }
                    if (!done) {
                        getLogger().warn("Unconsidered property entry: " + name);
                    }
                    break;
                }
            });
            // defer so that "valueType" cannot override an enum type inferred within getDescription 
            if (descriptionField.get() != null) {
                result.setDescription(getDescription(descriptionField.get(), result, true, false));                
            }
            result.setGeneric(isGenericIdShort(result.getIdShort()));
            return result;
        }
        
        /**
         * Reads the given node as a Boolean.
         * 
         * @param node the node
         * @return the boolean value
         */
        private boolean getBoolean(Node node) {
            return Boolean.valueOf(node.getTextContent().trim());
        }
        
        /**
         * Reads the given node as a description value.
         * 
         * @param node the node
         * @param field the target field if enums shall be inferred from the description; may then override 
         *     {@link AasField#getValueType() the value type}
         * @param inferEnum infer enum specs if {@code field} is not <b>null</b>
         * @param addMissingLang add missing language {@code field} is not <b>null</b> and multi-language property
         * @return the pre-formatted description value
         */
        private String getDescription(Node node, AasField field, boolean inferEnum, boolean addMissingLang) {
            String result = "";
            Map<String, String> desc = new HashMap<>();
            iterateChilds(node, s -> {
                String name = getName(s); // may have multi-lang strings
                switch (name) {
                case "langString":
                    String lang = getAttributeValue(s, "lang", "");
                    String description = ParsingUtils.removeLinebreaks(s.getTextContent().trim())
                        .replaceAll(" +", " ")
                        .replace(". .", ".");
                    desc.put(lang, description);
                    break;
                case "langStringTextType": // IDTA 02023-0-9
                    readLangStringTextType(s, desc);
                    break;
                case "keys": // ignored for now
                    //getSemanticId(node)??
                    break;
                default:
                    getLogger().warn("Unconsidered description entry: " + name);
                    break;
                }
            });
            String selLang = null;
            if (desc.size() > 0) {
                selLang = "en";
                result = desc.get("en");
                if (null == result) {
                    Entry<String, String> ent = desc.entrySet().iterator().next();
                    selLang = ent.getKey();
                    result = ent.getValue();
                    //result = desc.values().iterator().next();
                }
            }
            if (field != null) {
                if (inferEnum) {
                    result = inferEnum(result, result, field, enumsHandler, true);
                }
                if (addMissingLang && AasSmeType.MULTI_LANGUAGE_PROPERTY == field.getSmeType()) {
                    if (result != null && result.trim().length() > 0 && selLang.length() > 0 
                        && !result.trim().toUpperCase().endsWith("@" + selLang.toUpperCase())) {
                        result = result.trim() + "@" + selLang;
                    }
                }
            }
            return removeLinebreaks(result);
        }
        
        /**
         * Reads a LangStringTextType.
         * 
         * @param node the node to read from
         * @param desc the mapping to write information into
         */
        private void readLangStringTextType(Node node, Map<String, String> desc) { // IDTA 02023-0-9
            AtomicReference<String> lang = new AtomicReference<>(null);
            AtomicReference<String> text = new AtomicReference<>(null);
            iterateChilds(node, l -> {
                String name = getName(l); // may have multi-lang strings
                switch (name) {
                case "language":
                    lang.set(l.getTextContent().trim().toLowerCase());
                    break;
                case "text":
                    text.set(l.getTextContent().trim());
                    break;
                default:
                    getLogger().warn("Unconsidered LangStringTextType entry: " + name);
                    break;
                }
            });
            if (lang.get() != null && text.get() != null) {
                desc.put(lang.get(), text.get());
            }
        }
        
        /**
         * Returns a semantic id in style of the AAS abstraction of the platform.
         * 
         * @param node the node from which to read the semantic id
         * @return the semantic id, may be empty
         */
        private String getSemanticId(Node node) {
            AtomicReference<String> result = new AtomicReference<>("");
            iterateChilds(node, child -> {
                String name = getName(child);
                if ("keys".equals(name)) {
                    iterateChilds(child, cd -> {
                        if (getIterableChildsCount(cd, null) > 0) {
                            result.set(readKeyAsSemanticId(cd));
                        } else {
                            String type = getAttributeValue(cd, "type", "");
                            // TODO local, resolve against concept description?
                            if (isSemanticIdValidKey(type)) { 
                                result.set(readSemanticId(cd));
                            }
                        }
                    });
                }
            });
            return result.get();
        }
        
        /**
         * Returns whether {@code type} is a valid key type for a semantic id.
         * 
         * @param type the type
         * @return {@code true} for valid, {@code false} else
         */
        private boolean isSemanticIdValidKey(String type) {
            boolean result;
            if ("Submodel".equals(type) || "ConceptDescription".equals(type) 
                || "GlobalReference".equals(type) || "ConceptDictionary".equals(type)) {
                result = true;
            } else {
                result = false;
                getLogger().warn("Unconsidered semanticId key type '{}'", type);
            }
            return result;
        }
        
        /**
         * Reads a key node with subelements as semantic id.
         * 
         * @param node the node
         * @return the semantic id, may be empty
         */
        private String readKeyAsSemanticId(Node node) {
            AtomicReference<String> type = new AtomicReference<>();
            AtomicReference<String> value = new AtomicReference<>();
            iterateChilds(node, child -> {
                String name = getName(child);
                switch (name) {
                case "type":
                    type.set(child.getTextContent().trim());
                    break;
                case "value":
                    value.set(child.getTextContent().trim());
                    break;
                default:
                    getLogger().warn("Unconsidered key element '{}'", name);
                    break;
                }
            });
            String result = "";
            if (type.get() != null && value.get() != null) {
                if (isSemanticIdValidKey(type.get())) {
                    result = getSemanticId(null, value.get());
                }
            }
            return result;
        }
        
        /**
         * Reads a semantic id in {@code node}.
         * 
         * @param node the node
         * @return the semantic id in platform abstraction format
         */
        private String readSemanticId(Node node) {
            String idType = getAttributeValue(node, "idType", "");
            String tmp = node.getTextContent().trim();
            return getSemanticId(idType, tmp);
/*            if ("IRI".equals(idType)) {
                tmp = IdentifierType.compose(IdentifierType.IRI_PREFIX, tmp);
            } else if ("IRDI".equals(idType)) {
                tmp = IdentifierType.compose(IdentifierType.IRDI_PREFIX, tmp);
            } else {
                getLogger().warn("Unconsidered semanticId idType " + idType + " at " + tmp);
            }
            return tmp;*/
        }
        
        /**
         * Returns a semantic id in platform notation.
         * 
         * @param idType the id type
         * @param id the id
         * @return the composed id, may be {@code id}
         */
        private String getSemanticId(String idType, String id) {
            String result = id;
            if (null == idType || idType.length() == 0) { // IDTA 02023-0-9, IDTA 02021-1-0
                result = SemanticIdRecognizer.getSemanticIdFrom(id, true);
                if (null == result) { // fallback IDTA 02010-1-0
                    result = SemanticIdRecognizer.getSemanticIdFrom(id, true, true);
                }
            } else {
                if ("IRI".equals(idType)) {
                    result = IdentifierType.compose(IdentifierType.IRI_PREFIX, id);
                } else if ("IRDI".equals(idType)) {
                    result = IdentifierType.compose(IdentifierType.IRDI_PREFIX, id);
                } else {
                    getLogger().warn("Unconsidered semanticId idType " + idType + " at " + id);
                }
            }
            return result;
        }
        
        /**
         * Returns the string value of an XML attribute.
         * 
         * @param node the node holding the attribute
         * @param attribute the attribute name
         * @param dflt the default value to return if the attribute was not found
         * @return {@code dflt} or the value of {@code attribute} in {@code node}
         */
        private String getAttributeValue(Node node, String attribute, String dflt) {
            String result = dflt;
            Node attr = node.getAttributes().getNamedItem(attribute);
            if (null != attr) {
                result = attr.getTextContent().trim();
            }
            return result;
        }
    
    }
    
    /**
     * Returns the logger instance for this class.
     * 
     * @return the logger instance
     */
    private static Logger getLogger() {
        return LoggerFactory.getLogger(ReadAasxFile.class);
    }
    
    /**
     * Executes the AASX file reader.
     *  
     * @param args the first contains the AASX file name, the second the output, the third the specification number, 
     * without some test files are executed, the fourth the optional idShort prefix
     * @throws IOException if reading fails
     */
    public static void main(String... args) throws IOException {
        if (args.length >= 3) {
            readFile(args[0], args[1], args[2], args.length >= 4 ? args[3] : null);
        } else {
            String baseDir = "src/test/resources/idta/";
            //readFile(baseDir + "2001/IDTA 02001-1-0_Subomdel_MTPv1.0-rc2-with-documentation.aasx", null, "02001");
            //readFile(baseDir + "2002/IDTA 02002-1-0_Template_ContactInformation.aasx", null, "02002");
            //readFile(baseDir + "2003/IDTA 02003-1-2_SubmodelTemplate_TechnicalData_v1.2_withQualifier.aasx", 
            //      null, "02003");
            //readFile(baseDir + "2004/IDTA 02004-1-2_Template_Handover Documentation.aasx", null, "02004");
            //readFile(baseDir + "2005/IDTA 02005-1-0_Template_ProvisionOfSimulationModels.aasx", null, "02005");
            //readFile(baseDir + "2006/IDTA 02006-2-0_Template_Digital Nameplate.aasx", null, "02006");
            //readFile(baseDir + "2007/IDTA 02007-1-0_Template_Software Nameplate.aasx", null, "02007");
            //readFile(baseDir + "2008/IDTA 02008-1-1_Template_withOperations_TimeSeriesData.aasx", null, "02008");
            //readFile(baseDir + "2010/IDTA 02010-1-0_Template_ServiceRequestNotification.aasx", null, "02010");
            //readFile(baseDir + "2011/IDTA 02011-1-0_Template_HierarchicalStructuresEnablingBoM.aasx", null, "02011");
            //readFile(baseDir + "2012/IDTA 02012-1-0_Template_DEXPI.aasx", null, "02012");
            //readFile(baseDir + "2013/IDTA 02013-1-0_Template_Reliability.aasx", null, "02013", "Ry");
            //readFile(baseDir + "2014/IDTA 02014-1-0_Template_FunctionalSafety.aasx", null, "02014", "Fs);
            //readFile(baseDir + "2015/IDTA 02015-1-0 _Template_ControlComponentType.aasx", null, "02015", "Ct");
            //readFile(baseDir + "2016/IDTA 02016-1-0 _Template_ControlComponentInstance.aasx", null, "02016", "Ci");
            //readFile(baseDir + "2017/IDTA 02017-1-0_Template_Asset Interfaces Description.aasx", null, "02017");
            //readFile(baseDir + "2021/IDTA 02021-1-0_Template_Sizing of Power Drive Trains.aasx", null, "02021");
            readFile(baseDir + "2023/IDTA 2023-0-9 _Template_CarbonFootprint.aasx", null, "02023");
        }
    }

}
