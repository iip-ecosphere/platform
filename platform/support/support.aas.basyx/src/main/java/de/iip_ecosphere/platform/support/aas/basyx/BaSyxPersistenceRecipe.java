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

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.basyx.aas.factory.xml.MetamodelToXMLConverter;
import org.eclipse.basyx.aas.factory.xml.XMLToMetamodelConverter;
import org.eclipse.basyx.aas.metamodel.api.IAssetAdministrationShell;
import org.eclipse.basyx.aas.metamodel.api.parts.asset.IAsset;
import org.eclipse.basyx.aas.metamodel.map.AssetAdministrationShell;
import org.eclipse.basyx.submodel.metamodel.api.ISubModel;
import org.eclipse.basyx.submodel.metamodel.api.parts.IConceptDescription;
import org.eclipse.basyx.submodel.metamodel.api.reference.IReference;
import org.eclipse.basyx.submodel.metamodel.map.SubModel;
import org.eclipse.basyx.support.bundle.AASBundle;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.PersistenceRecipe;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.basyx.impl.AASXPackageManager;

/**
 * A persistence recipe for BaSyx AAS. This implementation is internally based on short ids. 
 * 
 * Limitations:
 * <ul>
 *   <li>This class does not consider concept descriptions or assets when reading directly from XML.</li>
 *   <li>Might be this is not enough for reading models uniquely back directly from XML.</li>
 *   <li>Reading of AASX supports only 1 AAS per AASX (BaSyx). Therefore, two classes from BaSyx taken over
 *       into IIP-Ecosphere code as they are not sufficiently usable/extensible.</li>
 *   <li>Translating AAS shortIds to file names is limited to whitespace replacement, i.e., no Umlauts etc. for 
 *       now.</li>
 *   <li>Written management XML elements in AAS may contain empty xmlns attributes.</li>
 * </ul>
 * 
 * @author Holger Eichelberger, SSE
 */
class BaSyxPersistenceRecipe implements PersistenceRecipe {

    // well, we could do better with plugins etc... sufficient for now and here
    
    private static final Map<String, String> CONTENT_TYPES = new TreeMap<>();
    
    static {
        CONTENT_TYPES.put("rels", "application/vnd.openxmlformats-package.relationships+xml");
        CONTENT_TYPES.put("xml", "text/xml");
        CONTENT_TYPES.put("jpeg", "image/jpeg");
        CONTENT_TYPES.put("png", "image/png");
        CONTENT_TYPES.put("pdf", "application/pdf");
        CONTENT_TYPES.put("jpg", "image/jpeg");
        CONTENT_TYPES.put("zip", "text/plain");
    }
    
    /**
     * Represents an AASX relationship.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class Relationship {
        private String type;
        private String target;
        private String id;

        /**
         * Creates a relationship.
         * 
         * @param type the relationship type
         * @param target the relationship target
         */
        private Relationship(String type, String target) {
            this.type = type;
            this.target = target;
            this.id = "I-" + (type + target).hashCode();
        }
    }
    
    @Override
    public void writeTo(List<Aas> aas, File file) throws IOException {
        if (file.getName().toLowerCase().endsWith(".aasx")) {
            writeToAasx(aas, file);
        } else {
            writeToXml(aas, file);
        }
    }

    /**
     * Writes the given {@code aas} to {@code file} in XML format.
     * 
     * @param aas the AAS
     * @param file the target file
     * @throws IOException if I/O problems occur while writing
     */
    private void writeToXml(List<Aas> aas, File file) throws IOException {
        List<IAssetAdministrationShell> basyxAas = new ArrayList<IAssetAdministrationShell>();
        List<ISubModel> basyxSubmodels = new ArrayList<ISubModel>();
        Collection<IAsset> assetList = new ArrayList<IAsset>();
        Collection<IConceptDescription> conceptDescriptionList = new ArrayList<IConceptDescription>();
        for (Aas a : aas) {
            basyxAas.add(((BaSyxAas) a).getAas());
            for (Submodel s : a.submodels()) {
                basyxSubmodels.add(((BaSyxSubmodel) s).getSubmodel());
            }
        }
        
        try (FileWriter result = new FileWriter(file)) {
            MetamodelToXMLConverter.convertToXML(basyxAas, assetList, conceptDescriptionList, basyxSubmodels, 
                new StreamResult(result));
        } catch (IOException e) {
            throw e;
        } catch (ParserConfigurationException | TransformerException e) {
            throw new IOException(e);
        }
    }

    /**
     * Writes the given {@code aas} to {@code file} in Aasx format.
     * 
     * @param aas the AAS
     * @param file the target file
     * @throws IOException if I/O problems occur while writing
     */
    private void writeToAasx(List<Aas> aas, File file) throws IOException {
        if (aas.size() > 1) {
            LoggerFactory.getLogger(getClass()).warn("Writing multiple AAS to a single file may not be read back as "
                + "BaSyx currently just supports one AAS to be read from an AASX package.");
        }
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        
        try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(file))) {
            out.putNextEntry(new ZipEntry("[Content Types].xml"));
            xmlToStream(out, transformerFactory, createContentTypesXML(factory));

            out.putNextEntry(new ZipEntry("_rels/.rels"));
            List<Relationship> rels = new ArrayList<>();
            rels.add(new Relationship("http://www.admin-shell.io/aasx/relationships/aasx-origin", "/aasx/aasx-origin"));
            xmlToStream(out, transformerFactory, createRelsXML(factory, rels));
            rels.clear();
            
            out.putNextEntry(new ZipEntry("aasx/aasx-origin"));
            textToStream(out, "Intentionally empty.");
            for (Aas a : aas) {
                aasToStream(out, factory, transformerFactory, a, rels);
            }
            out.putNextEntry(new ZipEntry("aasx/_rels/aasx-origin.rels"));
            xmlToStream(out, transformerFactory, createRelsXML(factory, rels));
        } catch (IOException e) {
            throw e;
        } catch (TransformerException | ParserConfigurationException e) {
            throw new IOException(e);
        }
    }
    
    /**
     * Turns an AAS name into a file-compatible name.
     * 
     * @param idShort the short name
     * @return the transformed name
     */
    private String toFileName(String idShort) {
        // TODO umlauts etc
        return idShort.replace(' ', '_');
    }
    
    /**
     * Turns an AAS to stream.
     * 
     * @param out the output ZIP stream
     * @param factory the XML document factory
     * @param transformerFactory the XML transformer factory
     * @param aas the AAS to be turned into the stream
     * @param rels the AAS-level relationships to be modified with an added relationship for {@code aas} 
     * @throws IOException in case that stream writing fails
     * @throws ParserConfigurationException in case that the XML parser configuration is erroneous
     * @throws TransformerException in case that writing the XML through transformation fails
     */
    private void aasToStream(ZipOutputStream out, DocumentBuilderFactory factory, TransformerFactory transformerFactory,
        Aas aas, List<Relationship> rels) throws IOException, 
        ParserConfigurationException, TransformerException {
        String aasName = toFileName(aas.getIdShort());
        out.putNextEntry(new ZipEntry("aasx/" + aasName + "/" + aasName + ".aas.xml"));
        rels.add(new Relationship("http://www.admin-shell.io/aasx/relationships/aas-spec", "/aasx/" + aasName 
            + "/" + aasName + ".aas.xml"));

        List<IAssetAdministrationShell> basyxAas = new ArrayList<IAssetAdministrationShell>();
        List<ISubModel> basyxSubmodels = new ArrayList<ISubModel>();
        Collection<IAsset> assetList = new ArrayList<IAsset>();
        Collection<IConceptDescription> conceptDescriptionList = new ArrayList<IConceptDescription>();
        basyxAas.add(((BaSyxAas) aas).getAas());
        for (Submodel s : aas.submodels()) {
            basyxSubmodels.add(((BaSyxSubmodel) s).getSubmodel());
        }
        
        MetamodelToXMLConverter.convertToXML(basyxAas, assetList, conceptDescriptionList, basyxSubmodels, 
            new StreamResult(out));

        List<Relationship> aRels = new ArrayList<Relationship>();
        //http://www.admin-shell.io/aasx/relationships/aas-suppl -> /aasx/assetIdentification/logo.png
        //http://www.admin-shell.io/aasx/relationships/aas-suppl -> /aasx/Nameplate/xxx.png
        //http://www.admin-shell.io/aasx/relationships/aas-suppl -> /aasx/Document/xxx.pdf
        //http://www.admin-shell.io/aasx/relationships/aas-suppl -> /aasx/Software/xxx.zip"
        out.putNextEntry(new ZipEntry("aasx/" + aasName + "/_rels/" + aasName + ".aas.xml.rels"));
        xmlToStream(out, transformerFactory, createRelsXML(factory, aRels));
    }
    
    /**
     * Creates the content types XML from {@link #CONTENT_TYPES}.
     * 
     * @param factory the XML document factory
     * @return the XML document containing the content types
     * @throws ParserConfigurationException in case that the XML parser configuration is erroneous
     */
    private Document createContentTypesXML(DocumentBuilderFactory factory) throws ParserConfigurationException {
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();
        Element root = doc.createElementNS("http://schemas.openxmlformats.org/package/2006/content-types", "Types");
        for (Map.Entry<String, String> ent : CONTENT_TYPES.entrySet()) {
            Element dflt = doc.createElement("Default");
            dflt.setAttribute("Extension", ent.getKey());
            dflt.setAttribute("ContentType", ent.getValue());
            root.appendChild(dflt);
        }
        Element over = doc.createElement("Override");
        over.setAttribute("PartName", "/aasx/aasx-origin");
        over.setAttribute("ContentType", "text/plain");
        root.appendChild(over);
        doc.appendChild(root);
        return doc;
    }

    /**
     * Creates the relationships XML from the relationships in {@code rels}.
     * 
     * @param factory the XML document factory
     * @param rels the relationships to emit
     * @return the XML document containing the relationships
     * @throws ParserConfigurationException in case that the XML parser configuration is erroneous
     */
    private Document createRelsXML(DocumentBuilderFactory factory, List<Relationship> rels) 
        throws ParserConfigurationException {
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();
        Element root = doc.createElementNS("http://schemas.openxmlformats.org/package/2006/relationships", 
            "Relationships");
        for (Relationship r : rels) {
            Element rel = doc.createElement("Relationship");
            rel.setAttribute("Type", r.type);
            rel.setAttribute("Target", r.target);
            rel.setAttribute("Id", "I" + r.id);
            root.appendChild(rel);
        }
        doc.appendChild(root);
        
        return doc;
    }

    /**
     * Emits an XML document into the output streams.
     * 
     * @param out the output stream
     * @param transformerFactory the XML transformer factory to use
     * @param doc the XML document to emit
     * @throws TransformerException in case that writing the XML through transformation fails
     */
    private void xmlToStream(ZipOutputStream out, TransformerFactory transformerFactory, Document doc) 
        throws TransformerException {
        Transformer transf = transformerFactory.newTransformer();
        
        transf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transf.setOutputProperty(OutputKeys.INDENT, "yes");
        transf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        
        DOMSource source = new DOMSource(doc);
        transf.transform(source, new StreamResult(out));
    }

    /**
     * Emits text file contents into the output stream.
     * 
     * @param out the output stream
     * @param text the text to emit
     */
    private void textToStream(ZipOutputStream out, String text) {
        PrintStream ps = new PrintStream(out);
        ps.println(text);
    }
    
    @Override
    public List<Aas> readFrom(File file) throws IOException {
        List<Aas> result = new ArrayList<Aas>();
        if (file.getName().toLowerCase().endsWith(".aasx")) {
            readFromAasx(file, result);
        } else {
            readFromXml(file, result);
        }
        return result;
    }

    /**
     * Reads AAS from an AAS ZIP package in {@code file}.
     * 
     * @param file the file to read
     * @param result the resulting {@link Aas} instances (to be modified as a side effect)
     * @throws IOException in case of I/O reading problems
     */
    private void readFromAasx(File file, List<Aas> result) throws IOException {
        try {
            AASXPackageManager apm = new AASXPackageManager(file.getAbsolutePath());
            Set<AASBundle> bundles = apm.retrieveAASBundles();
            List<IAssetAdministrationShell> aas = new ArrayList<>();
            List<ISubModel> submodels = new ArrayList<>();
            for (AASBundle b : bundles) {
                aas.add(b.getAAS());
                submodels.addAll(b.getSubmodels());
                transform(aas, submodels, result);
                aas.clear();
                submodels.clear();
            }
        } catch (SAXException | ParserConfigurationException e) {
            throw new IOException(e);
        }
    }

    /**
     * Reads AAS from XML in {@code file}.
     * 
     * @param file the file to read
     * @param result the resulting {@link Aas} instances (to be modified as a side effect)
     * @throws IOException in case of I/O reading problems
     */
    private void readFromXml(File file, List<Aas> result) throws IOException {
        try {
            String content = new String(Files.readAllBytes(file.toPath()));
            XMLToMetamodelConverter conv = new XMLToMetamodelConverter(content);
            transform(conv.parseAAS(), conv.parseSubmodels(), result);
        } catch (SAXException | ParserConfigurationException e) {
            throw new IOException(e);
        }
    }
    
    /**
     * Transforms a list of related {@code aas} and {@code submodels} to a list of {@link Aas} instances of the
     * abstraction.
     * 
     * @param aas the AAS to transform
     * @param submodels the sub-models for {@code aas} to transform
     * @param result the resulting {@link Aas} instances (to be modified as a side effect)
     * @throws IOException in case that something goes wrong
     */
    private void transform(List<IAssetAdministrationShell> aas, List<ISubModel> submodels, List<Aas> result) 
        throws IOException {
        Map<String, SubModel> subMapping = new HashMap<>();
        for (ISubModel sm : submodels) {
            if (sm instanceof SubModel) {
                subMapping.put(sm.getIdShort(), (SubModel) sm);
            }
        }
        for (IAssetAdministrationShell a : aas) {
            if (a instanceof AssetAdministrationShell) {
                BaSyxAas bAas = new BaSyxAas((AssetAdministrationShell) a);
                for (IReference r : a.getSubmodelReferences()) {
                    if (!r.getKeys().isEmpty()) {
                        String name = r.getKeys().get(0).getValue(); // really the first??
                        SubModel submodel = subMapping.get(name);
                        if (null != submodel) {
                            bAas.register(new BaSyxSubmodel(bAas, submodel));
                        }
                    }
                }
                result.add(bAas);
            }
        }
    }

}
