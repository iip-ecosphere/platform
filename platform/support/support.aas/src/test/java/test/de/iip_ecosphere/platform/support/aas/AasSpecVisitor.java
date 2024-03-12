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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.IOUtils;

import de.iip_ecosphere.platform.support.CollectionUtils;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.AasVisitor;
import de.iip_ecosphere.platform.support.aas.Asset;
import de.iip_ecosphere.platform.support.aas.BlobDataElement;
import de.iip_ecosphere.platform.support.aas.DataElement;
import de.iip_ecosphere.platform.support.aas.Entity;
import de.iip_ecosphere.platform.support.aas.FileDataElement;
import de.iip_ecosphere.platform.support.aas.HasSemantics;
import de.iip_ecosphere.platform.support.aas.MultiLanguageProperty;
import de.iip_ecosphere.platform.support.aas.Operation;
import de.iip_ecosphere.platform.support.aas.Property;
import de.iip_ecosphere.platform.support.aas.Range;
import de.iip_ecosphere.platform.support.aas.ReferenceElement;
import de.iip_ecosphere.platform.support.aas.RelationshipElement;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.SubmodelElement;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.resources.ResourceLoader;

import org.junit.Assert;

/**
 * A re-usable visitor for comparing AAS structures in tests. Sorts submodels and submodel elements by their idShort.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AasSpecVisitor implements AasVisitor {

    public static final DateFormat DATE_FORMATTER = createDateFormat("yyyy/mm/dd HH:mm:ss");
    
    private String indentation = "";
    private PrintStream out;
    
    /**
     * Creates a new instance.
     * 
     * @param out the target stream
     */
    public AasSpecVisitor(PrintStream out) {
        this.out = out;
    }
    
    /**
     * Creates a date format.
     * 
     * @param format format in the form of simple date format
     * @return the date format, in GMT
     */
    public static DateFormat createDateFormat(String format) {
        SimpleDateFormat result = new SimpleDateFormat(format);
        result.setTimeZone(TimeZone.getTimeZone("GMT"));
        return result;
    }
    
    /**
     * Increases the indentation.
     */
    private void increaseIndentation() {
        indentation = indentation + " ";
    }

    /**
     * Decreases the indentation.
     */
    private void decreaseIndentation() {
        if (indentation.length() > 0) {
            indentation = indentation.substring(0, indentation.length() - 1);
        }
    }
    
    /**
     * Returns a sorted collection of submodels determined by this visitor.
     * 
     * @param <T> the type of submodels
     * @param elements the elements
     * @return {@code elements}
     */
    public <T extends Submodel> Collection<T> sortSubmodels(Collection<T> elements) {
        List<T> tmp = null == elements ? new ArrayList<T>() : CollectionUtils.toList(elements.iterator());
        tmp.sort((e1, e2) -> e1.getIdShort().compareTo(e2.getIdShort()));
        return tmp;
    }

    /**
     * Returns a sorted collection of submodel elements determined by this visitor.
     * 
     * @param <T> the type of submodel element
     * @param elements the elements
     * @return {@code elements}
     */
    public <T extends SubmodelElement> Collection<T> sortSubmodelElements(Collection<T> elements) {
        List<T> tmp = null == elements ? new ArrayList<T>() : CollectionUtils.toList(elements.iterator());
        tmp.sort((e1, e2) -> e1.getIdShort().compareTo(e2.getIdShort()));
        return tmp;
    }
    
    
    /**
     * Logs the {@code text}.
     * 
     * @param text the text
     */
    private void log(String text) {
        out.println(indentation + text);
    }
    
    @Override
    public void visitAas(Aas aas) {
        log("AAS " + aas.getIdShort());
        increaseIndentation();
    }
    
    @Override
    public void endAas(Aas aas) {
        decreaseIndentation();
    }

    @Override
    public void visitAsset(Asset asset) {
        log("ASSET " + asset.getIdShort() + " " + asset.getAssetKind());
    }

    @Override
    public void visitSubmodel(Submodel submodel) {
        log("SUBMODEL " + submodel.getIdShort() + getSemanticId(submodel));
        increaseIndentation();
    }

    @Override
    public void endSubmodel(Submodel submodel) {
        decreaseIndentation();
    }
    
    @Override
    public void visitProperty(Property property) {
        String value;
        try {
            Object obj = property.getValue();
            if (obj instanceof Date) {
                value = DATE_FORMATTER.format((Date) obj);
            } else {
                value = String.valueOf(obj);
            }
        } catch (ExecutionException e) {
            value = "?";
        }
        log("PROPERTY " + property.getIdShort() + " = " + value + getSemanticId(property));
    }

    /**
     * Returns the semantic id of {@code elt}.
     * 
     * @param elt the element
     * @return the semantic id or an empty string for none
     */
    private String getSemanticId(HasSemantics elt) {
        String semId = elt.getSemanticId();
        if (null == semId) {
            semId = "";
        } else {
            semId = " (semanticId: " + semId + ")";
        }
        return semId;
    }

    @Override
    public void visitOperation(Operation operation) {
        log("OPERATION " + operation.getIdShort() + " #args " + operation.getArgsCount() + getSemanticId(operation));
    }

    @Override
    public void visitReferenceElement(ReferenceElement referenceElement) {
        log("REFERENCE " + referenceElement.getIdShort() + " -> " + (referenceElement.getValue() != null)
            + getSemanticId(referenceElement));
    }

    @Override
    public void visitSubmodelElementCollection(SubmodelElementCollection collection) {
        log(getHeader("SMC", collection));
        increaseIndentation();
    }

    @Override
    public void endSubmodelElementCollection(SubmodelElementCollection collection) {
        decreaseIndentation();
    }
 
    /**
     * Returns the header for {@code elt}.
     * 
     * @param type the type id to emit
     * @param elt the element
     * @return the header
     */
    private String getHeader(String type, SubmodelElement elt) {
        return type + " " + elt.getIdShort() + getSemanticId(elt);
    }

    @Override
    public void visitDataElement(DataElement dataElement) {
        log(getHeader("DATAELT", dataElement));
    }
    
    @Override
    public void visitFileDataElement(FileDataElement dataElement) {
        String contents = dataElement.getContents();
        int len = contents == null ? 0 : contents.length();
        log(getHeader("FILE", dataElement) + " length " + len);
    }

    @Override
    public void visitRange(Range range) {
        log(getHeader("RANGE", range) + " min " + range.getMin() + " max " + range.getMax());
    }

    @Override
    public void visitBlobDataElement(BlobDataElement dataElement) {
        byte[] val = dataElement.getValueAsByteArray();
        int len = val == null ? 0 : val.length;
        log(getHeader("BLOB", dataElement) + " length " + len);
    }
    
    @Override
    public void visitMultiLanguageProperty(MultiLanguageProperty property) {
        log(getHeader("MLP", property));
    }

    @Override
    public void visitRelationshipElement(RelationshipElement relationship) {
        log(getHeader("RELATIONSHIP", relationship));
    }

    @Override
    public void visitEntity(Entity entity) {
        log(getHeader("ENTITY", entity));
        increaseIndentation();
    }

    @Override
    public void endVisitEntity(Entity entity) {
        decreaseIndentation();
    }
    
    /**
     * Asserts equality of {@code aas} with it's spec. We use the id short + ".spec" as file name where
     * the spec is (considering resourcePrefix as prefix).
     * 
     * @param aas the AAS
     * @param fileName the test specification file within {@code resourcePrefix}
     * @param resourcePrefix optional resource prefix, may be empty or <b>null</b> for none
     */
    public static void assertEquals(Aas aas, String resourcePrefix, String fileName) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        AasSpecVisitor v = new AasSpecVisitor(new PrintStream(bos));
        aas.accept(v);
        Charset cs = Charset.defaultCharset();
        String testSpec = bos.toString(cs).trim();
        if (null == resourcePrefix) {
            resourcePrefix = "";
        }
        if (null == fileName) {
            fileName = aas.getIdShort().toLowerCase() + ".spec";
        }
        String resourceName = resourcePrefix + fileName;
        System.out.println(">-- test-out --");
        System.out.println(testSpec);
        System.out.println("<-- test-out --");
        try {
            InputStream in = ResourceLoader.getResourceAsStream(resourceName);
            String spec = IOUtils.toString(in, cs).trim();
            Assert.assertEquals(spec, testSpec);
        } catch (NullPointerException | IOException e) {
            Assert.fail(null == e.getMessage() ? "Cannot read resource " + resourceName : e.getMessage());
        }
    }

}
