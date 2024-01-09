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

package de.iip_ecosphere.platform.support.aas.types.common;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import de.iip_ecosphere.platform.support.aas.LangString;
import de.iip_ecosphere.platform.support.aas.SubmodelElementContainerBuilder;
import de.iip_ecosphere.platform.support.aas.Type;
import de.iip_ecosphere.platform.support.aas.MultiLanguageProperty.MultiLanguagePropertyBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection.SubmodelElementCollectionBuilder;
import de.iip_ecosphere.platform.support.aas.Property;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.SubmodelElement;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;

/**
 * Utility methods.
 * 
 * @author Holger Eichelberger, SSE
 */
public class Utils {

    /**
     * Returns a 2 digits counting id short as usual in IDTA templates.
     * 
     * @param prefix the prefix name
     * @param nr the counting number
     * @return the composed idShort
     */
    public static String getCountingIdShort(String prefix, int nr) {
        return prefix + String.format("%02d", nr);
    }

    /**
     * Assert that {@code valid} else emits an {@link IllegalArgumentException} with text 
     * {@code exception}.
     * 
     * @param valid the validity criteria
     * @param exception the exception text
     * @throws IllegalArgumentException if not {@code valid}
     */
    public static void assertThat(boolean valid, String exception) {
        if (!valid) {
            throw new IllegalArgumentException(exception);
        }
    }

    /**
     * Creates a multi-language property.
     * 
     * @param <B> the (parent) builder type
     * @param builder the parent builder
     * @param enable whether the creation of multi-language properties is enabled (incompatibility with AASX Explorer)
     * @param idShort the idShort
     * @param semanticId the semanticId of the property
     * @param texts the values of the property
     * @return {@code builde}
     */
    public static <B extends SubmodelElementContainerBuilder> B createMultiLanguageProperty(B builder, boolean enable, 
        String idShort, String semanticId, LangString... texts) {
        if (enable) {
            MultiLanguagePropertyBuilder mlpb = builder
                .createMultiLanguagePropertyBuilder(idShort)
                .setSemanticId(semanticId);
            for (LangString t: texts) {
                mlpb.addText(t);
            }
            mlpb.build();
        }
        return builder;
    }

    /**
     * Helper to create a Gregorian calendar.
     * 
     * @param date may be <b>null</b> for now or in format YYYY-MM-DDThh:mm:ss.SSS+hh:mm
     * @return the Gregorian calendar, <b>null</b> if the creation fails
     */
    public static XMLGregorianCalendar parseCalendar(String date) {
        XMLGregorianCalendar result = null;
        if (null == date) {
            try {
                GregorianCalendar now = new GregorianCalendar();
                result = DatatypeFactory.newInstance().newXMLGregorianCalendar(now);
            } catch (DatatypeConfigurationException e) {
            }
        } else {
            try {
                result = DatatypeFactory.newInstance().newXMLGregorianCalendar(date);
            } catch (DatatypeConfigurationException e) {
            }
        }
        return result;
    }

    /**
     * Wraps a submodel element collection.
     * 
     * @param <T> the wrapped type
     * @param submodel the submodel to take the collection from
     * @param idShort the idShort of the collection in {@code submodel}
     * @param constructor the constructor function wrapping the submodel into {@code T}
     * @return an instance of {@code T} or <b>null</b> if there is no such submodel
     */
    public static <T extends SubmodelElementCollection> T wrapSubmodelElementCollection(Submodel submodel, 
        String idShort, Function<SubmodelElementCollection, T> constructor) {
        SubmodelElementCollection sme = submodel.getSubmodelElementCollection(idShort);
        return null == sme ? null : constructor.apply(sme);
    }

    /**
     * Wraps a submodel element collection.
     * 
     * @param <T> the wrapped type
     * @param collection the collection to take the collection from
     * @param idShort the idShort of the collection in {@code submodel}
     * @param constructor the constructor function wrapping the submodel into {@code T}
     * @return an instance of {@code T} or <b>null</b> if there is no such submodel
     */
    public static <T extends SubmodelElementCollection> T wrapSubmodelElementCollection(
        SubmodelElementCollection collection, String idShort, Function<SubmodelElementCollection, T> constructor) {
        SubmodelElementCollection sme = collection.getSubmodelElementCollection(idShort);
        return null == sme ? null : constructor.apply(sme);
    }

    /**
     * Returns a type-filtered stream.
     * 
     * @param <T> the actual type of submodel element
     * @param iterable the iterable containing the elements
     * @param type the type to filter for
     * @param filter filters the elements
     * @return the stream of filtered elements
     */
    public static <T extends SubmodelElement> Stream<T> stream(Iterable<SubmodelElement> iterable, Class<T> type, 
        Predicate<T> filter) {
        Predicate<SubmodelElement> typeFilter = null == type ? e -> true : e -> type.isInstance(e);
        List<T> tmp = new ArrayList<>(); // initial
        for (SubmodelElement e : iterable) {
            if (typeFilter.test(e)) {
                T t = type.cast(e);
                if (filter.test(t)) {
                    tmp.add(t);
                }
            }
        }
        return tmp.stream();
    }
    
    /**
     * Returns a string value from the specified property in {@code collection}.
     * 
     * @param collection the collection
     * @param idShort the idShort of the property
     * @return the string value, may be <b>null</b> if there is no property
     * @throws ExecutionException if accessing the property value fails
     */
    public static String getStringValue(SubmodelElementCollection collection, String idShort) 
        throws ExecutionException {
        try {
            String result = null;
            Property prop = collection.getProperty(idShort);
            if (null != prop) {
                result = (String) prop.getValue();
            }
            return result;
        } catch (ClassCastException e) {
            throw new ExecutionException(e.getMessage(), null);
        }
    }
    
    /**
     * Creates a property.
     * 
     * @param <B> the (parent) builder type
     * @param builder the (parent) builder instance
     * @param idShort the idShort of the property
     * @param semanticId the semantic id of the property
     * @param type the type of the property value
     * @param value the value
     * @return {@code builder}
     */
    public static <B extends SubmodelElementCollectionBuilder> B createProperty(B builder, String idShort, 
        String semanticId, Type type, Object value) {
        builder.createPropertyBuilder(idShort)
            .setSemanticId(semanticId)
            .setValue(type, value)
            .build();
        return builder;
    }

}
