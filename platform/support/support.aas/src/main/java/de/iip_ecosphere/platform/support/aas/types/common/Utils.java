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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import de.iip_ecosphere.platform.support.aas.DataElement;
import de.iip_ecosphere.platform.support.aas.ElementsAccess;
import de.iip_ecosphere.platform.support.aas.FileDataElement;
import de.iip_ecosphere.platform.support.aas.LangString;
import de.iip_ecosphere.platform.support.aas.MultiLanguageProperty;
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
     * Returns a string value from the specified property in {@code parent}.
     * 
     * @param parent the parent access to elements
     * @param idShort the idShort of the property
     * @return the string value, may be <b>null</b> if there is no property
     * @throws ExecutionException if accessing the property value fails
     */
    public static String getStringValue(ElementsAccess parent, String idShort) 
        throws ExecutionException {
        try {
            String result = null;
            Property prop = parent.getProperty(idShort);
            if (null != prop) {
                result = (String) prop.getValue();
            }
            return result;
        } catch (ClassCastException e) {
            throw new ExecutionException(e.getMessage(), null);
        }
    }
    
    /**
     * Returns an int value from the specified property in {@code parent}.
     * 
     * @param parent the parent access to elements
     * @param idShort the idShort of the property
     * @return the int value
     * @throws ExecutionException if accessing the property value fails
     */
    public static int getIntValue(ElementsAccess parent, String idShort) 
        throws ExecutionException {
        try {
            int result = 0;
            Property prop = parent.getProperty(idShort);
            if (null != prop) {
                result = (Integer) prop.getValue();
            } else {
                throw new ExecutionException("Property " + idShort + " does not exist", null);
            }
            return result;
        } catch (ClassCastException e) {
            throw new ExecutionException(e.getMessage(), null);
        }
    }

    /**
     * Returns an double value from the specified property in {@code parent}.
     * 
     * @param parent the parent access to elements
     * @param idShort the idShort of the property
     * @return the double value
     * @throws ExecutionException if accessing the property value fails
     */
    public static double getDoubleValue(ElementsAccess parent, String idShort) 
        throws ExecutionException {
        try {
            double result = 0;
            Property prop = parent.getProperty(idShort);
            if (null != prop) {
                result = (Double) prop.getValue();
            } else {
                throw new ExecutionException("Property " + idShort + " does not exist", null);
            }
            return result;
        } catch (ClassCastException e) {
            throw new ExecutionException(e.getMessage(), null);
        }
    }

    /**
     * Returns an Boolean value from the specified property in {@code parent}.
     * 
     * @param parent the parent access to elements
     * @param idShort the idShort of the property
     * @return the Boolean value
     * @throws ExecutionException if accessing the property value fails
     */
    public static boolean getBooleanValue(ElementsAccess parent, String idShort) 
        throws ExecutionException {
        try {
            boolean result = false;
            Property prop = parent.getProperty(idShort);
            if (null != prop) {
                result = (Boolean) prop.getValue();
            } else {
                throw new ExecutionException("Property " + idShort + " does not exist", null);
            }
            return result;
        } catch (ClassCastException e) {
            throw new ExecutionException(e.getMessage(), null);
        }
    }

    /**
     * Returns a Date value from the specified property in {@code parent}.
     * 
     * @param parent the parent access to elements
     * @param idShort the idShort of the property
     * @return the Date value, may be <b>null</b> if there is no property
     * @throws ExecutionException if accessing the property value fails
     */
    public static Date getDateValue(ElementsAccess parent, String idShort) 
        throws ExecutionException {
        try {
            Date result = null;
            Property prop = parent.getProperty(idShort);
            if (null != prop) {
                result = (Date) prop.getValue();
            }
            return result;
        } catch (ClassCastException e) {
            throw new ExecutionException(e.getMessage(), null);
        }
    }

    /**
     * Returns a {@link FileDataElement} from the specified property in {@code parent}.
     * 
     * @param parent the parent access to elements
     * @param idShort the idShort of the property
     * @return the {@link FileDataElement} value, may be <b>null</b> if there is no property
     * @throws ExecutionException if accessing/converting the property value fails
     */
    public static FileDataElement getFileDataElementValue(ElementsAccess parent, String idShort) 
        throws ExecutionException {
        try {
            return (FileDataElement) parent.getDataElement(idShort);
        } catch (ClassCastException e) {
            throw new ExecutionException(e.getMessage(), null);
        }
    }
    
    /**
     * Returns LangString values from the specified (multi-language) property in {@code parent}.
     * 
     * @param parent the parent access to elements
     * @param idShort the idShort of the property
     * @return the LangString values, may be <b>null</b> if there is no property
     * @throws ExecutionException if accessing the property value fails
     */
    public static LangString[] getLangStringValue(ElementsAccess parent, String idShort) 
        throws ExecutionException {
        LangString[] result = null;
        DataElement elt = parent.getDataElement(idShort);
        if (elt instanceof MultiLanguageProperty) {
            MultiLanguageProperty mlElt = (MultiLanguageProperty) elt;
            Collection<LangString> ls = mlElt.getDescription().values();
            result = ls.toArray(new LangString[ls.size()]);
        }
        return result;
    }
    
    /**
     * Turns a string tolerantly to a test enum value. May prevent usual issues from spec parsing/analysis.
     * Considers values of a {@code getValue} method if defined.
     * 
     * @param <T> the enum type
     * @param parent the parent access to elements
     * @param idShort the idShort of the property
     * @param cls the enum class type
     * @return the test enum value
     */
    @SuppressWarnings("unchecked")
    public static <T extends Enum<T>> T getEnumValue(ElementsAccess parent, String idShort, Class<T> cls) 
        throws ExecutionException {
        T result = null;
        String value = getStringValue(parent, idShort);
        value = value.trim().toLowerCase();
        List<T> values = new ArrayList<>();
        for (Field f : cls.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (cls.isAssignableFrom(f.getType()) &&  Modifier.isStatic(mod) && Modifier.isFinal(mod) 
                && Modifier.isPublic(mod)) {
                try {
                    values.add((T) f.get(null));
                } catch (IllegalAccessException e) {
                }
            }
        }
        Method[] methods = new Method[] {
            getDeclaredMethodSafe(cls, "getValue"),
            getDeclaredMethodSafe(cls, "getSemanticId")};
        for (T v: values) {
            boolean matches = matchesEnum(value, v.name().toLowerCase());
            for (Method m : methods) {
                if (!matches && null != m) {
                    try {
                        matches = matchesEnum(value, m.invoke(v));
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    }
                }
                if (matches) {
                    break;
                }
            }
            if (matches) {
                result = v;
                break;
            }
        }
        return result;
    }
    
    /**
     * Returns a declared method without throwing an exception.
     * 
     * @param cls the class to look within
     * @param name the name of the method
     * @return the method, <b>null</b> for none
     */
    private static Method getDeclaredMethodSafe(Class<?> cls, String name) {
        Method result = null;
        try {
            result = cls.getDeclaredMethod(name);
        } catch (NoSuchMethodException e) {
        }
        return result;
    }
    
    /**
     * Returns whether {@code enumValue} matches {@code value}.
     * 
     * @param value the provided value to return an enum
     * @param enumValue the value from the enum to match against {@code value}
     * @return {@code true} if the value matches, {@code false} else
     */
    private static boolean matchesEnum(String value, Object enumValue) {
        boolean matches = false;
        if (enumValue != null) {
            String ev = enumValue.toString().toLowerCase();
            matches = value.equals(ev);
        }
        return matches;
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
