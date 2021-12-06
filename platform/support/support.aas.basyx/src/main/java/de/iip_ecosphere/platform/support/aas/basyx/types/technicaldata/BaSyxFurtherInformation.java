/**
 * ******************************************************************************
 * Copyright (c) {2021} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.aas.basyx.types.technicaldata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.xml.datatype.XMLGregorianCalendar;

import org.eclipse.basyx.submodel.metamodel.api.submodelelement.dataelement.IMultiLanguageProperty;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.MultiLanguageProperty;
import org.eclipse.basyx.submodel.types.technicaldata.submodelelementcollections.furtherinformation.FurtherInformation;

import de.iip_ecosphere.platform.support.aas.LangString;
import de.iip_ecosphere.platform.support.aas.basyx.BaSyxMultiLanguageDataElement;
import de.iip_ecosphere.platform.support.aas.basyx.BaSyxProperty;
import de.iip_ecosphere.platform.support.aas.basyx.BaSyxSubmodelElementCollection;
import de.iip_ecosphere.platform.support.aas.basyx.BaSyxSubmodelElementContainerBuilder;

/**
 * Wrapper for the BaSyx further information class.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxFurtherInformation extends BaSyxSubmodelElementCollection implements 
    de.iip_ecosphere.platform.support.aas.types.technicaldata.FurtherInformation {

    public static final String ID_SHORT = FurtherInformation.IDSHORT;
    
    /**
     * The sub-model element collection builder.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class BaSyxFurtherInformationBuilder extends BaSyxSubmodelElementCollectionBuilder 
        implements FurtherInformationBuilder {

        private Map<String, Collection<LangString>> statements;
        
        /**
         * Creates a sub-model element collection builder. The parent builder must be set by the calling
         * constructor.
         * 
         * @param parentBuilder the parent builder
         * @param validDate denotes a date on which the data specified in the submodel was valid from for the 
         *     associated asset
         * @throws IllegalArgumentException may be thrown if {@code idShort} is not given
         */
        BaSyxFurtherInformationBuilder(BaSyxSubmodelElementContainerBuilder<?> parentBuilder, 
            XMLGregorianCalendar validDate) {
            super(parentBuilder, ID_SHORT, 
                () -> new BaSyxFurtherInformation(), 
                () -> new FurtherInformation(validDate)); 
        }
        
        /**
         * Creates an instance from an existing BaSyx instance.
         * 
         * @param parentBuilder the parent builder
         * @param instance the BaSyx instance
         */
        BaSyxFurtherInformationBuilder(BaSyxSubmodelElementContainerBuilder<?> parentBuilder,
            BaSyxSubmodelElementCollection instance) {
            super(parentBuilder, instance);
        }
        
        @Override
        public BaSyxSubmodelElementCollection build() {
            FurtherInformation fi = (FurtherInformation) getCollection();
            register(new BaSyxProperty(fi.getValidDate()));
            if (null != statements) {
                setTextStatements(getCollectionInstance(), fi, statements);
            }
            return super.build();
        }

        @Override
        public FurtherInformationBuilder addStatement(String name, LangString statement) {
            if (null == statements) {
                statements = new HashMap<>();
            }
            Collection<LangString> stmt = statements.get(name); // prefixing happens later
            if (null == stmt) {
                stmt = new HashSet<>();
                statements.put(name, stmt);
            }
            stmt.add(statement);
            return this;
        }

    }
    
    /**
     * Creates an instance. Prevents external creation.
     */
    private BaSyxFurtherInformation() {
        super();
    }
    
    /**
     * Creates an instance and sets the BaSyx instance directly.
     * 
     * @param collection the collection instance
     */
    BaSyxFurtherInformation(org.eclipse.basyx.submodel.types.technicaldata.submodelelementcollections
        .furtherinformation.FurtherInformation collection) {
        super(collection);
    }
    
    @Override
    public FurtherInformation getSubmodelElement() {
        return (FurtherInformation) super.getSubmodelElement();
    }
    
    /**
     * Returns the valid date.
     * 
     * @return denotes a date on which the data specified in the submodel was valid from for the 
     *     associated asset
     */
    @Override
    public XMLGregorianCalendar getValidDate() {
        return (XMLGregorianCalendar) getSubmodelElement().getValidDate().getValue();
    }
    
    @Override
    public void setValidDate(XMLGregorianCalendar validDate) {
        getSubmodelElement().setValidDate(validDate);
    }

    @Override
    public Map<String, Collection<LangString>> getStatements() {
        Map<String, Collection<LangString>> result = null;
        List<IMultiLanguageProperty> tmp = getSubmodelElement().getStatements();
        if (null != tmp) {
            result = new HashMap<String, Collection<LangString>>();
            for (IMultiLanguageProperty t : tmp) {
                List<LangString> tResult = new ArrayList<>();
                for (org.eclipse.basyx.submodel.metamodel.map.qualifier.LangString l : t.getValue()) {
                    tResult.add(new LangString(l.getLanguage(), l.getDescription()));
                }
                result.put(t.getIdShort(), tResult);
            }
        }
        return result;
    }
    
    @Override
    public void setStatements(Map<String, Collection<LangString>> statements) {
        // BaSyx also just does an add
        setTextStatements(this, getSubmodelElement(), statements);
    }

    /**
     * Turns the nested structure into text statements and sets them on the given AAS structures.
     * 
     * @param coll the collection to register the new multi-language data elements with
     * @param fi the BaSyx instance to set the new multi-language data elements on
     * @param statements the statements, first level maps short_ids to (language, text) 
     */
    private static void setTextStatements(BaSyxSubmodelElementCollection coll, FurtherInformation fi, 
        Map<String, Collection<LangString>> statements) {
        List<MultiLanguageProperty> stmts = new ArrayList<>();
        for (Map.Entry<String, Collection<LangString>> l : statements.entrySet()) {
            BaSyxMultiLanguageDataElement stmt = new BaSyxMultiLanguageDataElement(
                FurtherInformation.TEXTSTATEMENTPREFIX + l.getKey(), l.getValue());
            coll.register(stmt);
            stmts.add(stmt.getDataElement());
        }
        fi.setTextStatements(stmts);
    }

}
