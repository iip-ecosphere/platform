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
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.lang3.StringUtils;

import de.iip_ecosphere.platform.support.aas.BlobDataElement;
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
import de.iip_ecosphere.platform.support.aas.Range;
import de.iip_ecosphere.platform.support.aas.ReferenceElement;
import de.iip_ecosphere.platform.support.aas.RelationshipElement;
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
     * @param exception the exception text, may contain "{}" as argument placeholder
     * @param args arguments to replace "{}" in the given sequence
     * @throws IllegalArgumentException if not {@code valid}
     */
    public static void assertThat(boolean valid, String exception, Object... args) {
        if (!valid) {
            String tmp = exception;
            for (Object a : args) {
                tmp = StringUtils.replaceOnce(tmp, "{}", null == a ? "null" : a.toString());
            }
            throw new IllegalArgumentException(tmp);
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
     * Helper to parse a date.
     * 
     * @param date may be <b>null</b> for now or in format YYYY-MM-DDThh:mm:ss.SSS+hh:mm
     * @return the date
     */
    public static Date parseDate(String date) {
        return parseCalendar(date).toGregorianCalendar().getTime();
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
     * Filters and collects {@code elements}.
     * 
     * @param <E> the element type
     * @param elements the elements
     * @param eltCls the type to filter for
     * @param semanticId the semanticId to filter further
     * @return the filtered elements as iterable
     */
    public static <E extends SubmodelElement> Iterable<E> collect(Iterable<SubmodelElement> elements, Class<E> eltCls, 
        String semanticId) {
        return collect(elements, eltCls, semanticId, e -> e);
    }

    /**
     * Filters, translates and collects {@code elements}.
     * 
     * @param <E> the element type
     * @param <T> the target value type
     * @param elements the elements
     * @param eltCls the type to filter for
     * @param semanticId the semanticId to filter further
     * @param translate translates elements to the target value (type) 
     * @return the filtered elements as iterable
     */
    public static <T, E extends SubmodelElement> Iterable<T> collect(Iterable<SubmodelElement> elements, 
        Class<E> eltCls, String semanticId, Function <E, T> translate) {
        return stream(elements, eltCls, e -> semanticId.equals(e.getSemanticId()))
            .map(e -> translate.apply(e))
            .collect(Collectors.toList());
    }

    /**
     * Casts a property value to {@code cls} if possible.
     * 
     * @param <T> the value type
     * @param prop the property
     * @param cls the value type class
     * @return the value or <b>null</b>
     */
    public static <T> T cast(Property prop, Class<T> cls) {
        T result = null;
        try {
            Object v = prop.getValue();
            if (cls.isInstance(v)) {
                result = cls.cast(v);
            }
        } catch (ExecutionException e) {
        }
        return result;
    }
    
    /**
     * Returns a string value from the specified property in {@code parent}.
     * 
     * @param parent the parent access to elements
     * @param idShort the idShort of the property
     * @return the string value, may be <b>null</b> if there is no property
     * @throws ExecutionException if accessing the property value fails
     */
    public static String getStringValue(ElementsAccess parent, String idShort) throws ExecutionException {
        return getStringValue(parent.getProperty(idShort));
    }

    /**
     * Returns a string value from the specified property.
     * 
     * @param prop the property
     * @return the string value, may be <b>null</b> if there is no property
     * @throws ExecutionException if accessing the property value fails
     */
    public static String getStringValue(Property prop) throws ExecutionException {
        try {
            String result = null;
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
    public static int getIntValue(ElementsAccess parent, String idShort) throws ExecutionException {
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
     * Returns a double value from the specified property in {@code parent}.
     * 
     * @param parent the parent access to elements
     * @param idShort the idShort of the property
     * @return the double value
     * @throws ExecutionException if accessing the property value fails
     */
    public static double getDoubleValue(ElementsAccess parent, String idShort) throws ExecutionException {
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
     * Returns a float value from the specified property in {@code parent}.
     * 
     * @param parent the parent access to elements
     * @param idShort the idShort of the property
     * @return the float value
     * @throws ExecutionException if accessing the property value fails
     */
    public static float getFloatValue(ElementsAccess parent, String idShort) throws ExecutionException {
        try {
            float result = 0;
            Property prop = parent.getProperty(idShort);
            if (null != prop) {
                result = (Float) prop.getValue();
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
    public static long getLongValue(ElementsAccess parent, String idShort) throws ExecutionException {
        try {
            long result = 0;
            Property prop = parent.getProperty(idShort);
            if (null != prop) {
                result = (Long) prop.getValue();
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
    public static BigInteger getBigIntegerValue(ElementsAccess parent, String idShort) throws ExecutionException {
        try {
            BigInteger result = BigInteger.ZERO;
            Property prop = parent.getProperty(idShort);
            if (null != prop) {
                result = (BigInteger) prop.getValue();
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
                Object value = prop.getValue();
                if (value instanceof Date) {
                    result = (Date) value;
                } else if (value instanceof XMLGregorianCalendar) { 
                    result = ((XMLGregorianCalendar) value).toGregorianCalendar().getTime();
                } else if (value instanceof Calendar) {
                    result = ((Calendar) value).getTime();
                }
            }
            return result;
        } catch (ClassCastException e) {
            throw new ExecutionException(e.getMessage(), null);
        }
    }
    
    /**
     * Returns a URI as string value from the specified property in {@code parent}.
     * 
     * @param parent the parent access to elements
     * @param idShort the idShort of the property
     * @return the string value, may be <b>null</b> if there is no property
     * @throws ExecutionException if accessing the property value fails
     */
    public static String getAnyUriValue(ElementsAccess parent, String idShort) throws ExecutionException {
        return getStringValue(parent, idShort);
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
     * Returns a {@link Range} from the specified property in {@code parent}.
     * 
     * @param parent the parent access to elements
     * @param idShort the idShort of the property
     * @return the {@link Range} value, may be <b>null</b> if there is no property
     * @throws ExecutionException if accessing/converting the property value fails
     */
    public static Range getRangeValue(ElementsAccess parent, String idShort) 
        throws ExecutionException {
        try {
            return (Range) parent.getDataElement(idShort);
        } catch (ClassCastException e) {
            throw new ExecutionException(e.getMessage(), null);
        }
    }

    /**
     * Returns a {@link BlobDataElement} from the specified property in {@code parent}.
     * 
     * @param parent the parent access to elements
     * @param idShort the idShort of the property
     * @return the {@link BlobDataElement} value, may be <b>null</b> if there is no property
     * @throws ExecutionException if accessing/converting the property value fails
     */
    public static BlobDataElement getBlobDataElementValue(ElementsAccess parent, String idShort) 
        throws ExecutionException {
        try {
            return (BlobDataElement) parent.getDataElement(idShort);
        } catch (ClassCastException e) {
            throw new ExecutionException(e.getMessage(), null);
        }
    }

    /**
     * Returns a {@link ReferenceElement} from the specified property in {@code parent}.
     * 
     * @param parent the parent access to elements
     * @param idShort the idShort of the property
     * @return the {@link ReferenceElement} value, may be <b>null</b> if there is no property
     * @throws ExecutionException if accessing/converting the property value fails
     */
    public static ReferenceElement getReferenceElementValue(ElementsAccess parent, String idShort) 
        throws ExecutionException {
        return parent.getReferenceElement(idShort);
    }

    /**
     * Returns a {@link RelationshipElement} from the specified property in {@code parent}.
     * 
     * @param parent the parent access to elements
     * @param idShort the idShort of the property
     * @return the {@link RelationshipElement} value, may be <b>null</b> if there is no property
     * @throws ExecutionException if accessing/converting the property value fails
     */
    public static RelationshipElement getRelationshipElementValue(ElementsAccess parent, String idShort) 
        throws ExecutionException {
        return parent.getRelationshipElement(idShort);
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
            result = getLangStringValue((MultiLanguageProperty) elt);
        }
        return result;
    }
    
    /**
     * Returns LangString values from the given multi-language property.
     * 
     * @param property the property
     * @return the LangString values, may be <b>null</b> if there is no property
     */
    public static LangString[] getLangStringValue(MultiLanguageProperty property) {
        Collection<LangString> ls = property.getDescription().values();
        return ls.toArray(new LangString[ls.size()]);
    }

    /**
     * Turns a string tolerantly to a test enum value. May prevent usual issues from spec parsing/analysis.
     * Considers values of a {@code getValue} method if defined. Considers registered extending enums
     * in {@link EnumRegistry} with compatible type.
     * 
     * @param <T> the unbound interface-of-enum type
     * @param <E> the enum type
     * @param parent the parent access to elements
     * @param idShort the idShort of the property
     * @param ifCls the interface-of-enum type
     * @param eCls the enum class type
     * @return the test enum value, may be <b>null</b> for not found/compatible
     * @throws ExecutionException if accessing the property value fails
     */
    public static <T, E extends Enum<E>> T getEnumValue(ElementsAccess parent, String idShort, Class<T> ifCls, 
        Class<E> eCls) throws ExecutionException {
        return getEnumValue(getStringValue(parent, idShort), ifCls, eCls);
    }

    /**
     * Turns a string tolerantly to a test enum value. May prevent usual issues from spec parsing/analysis.
     * Considers values of a {@code getValue} method if defined. Considers registered extending enums
     * in {@link EnumRegistry} with compatible type.
     * 
     * @param <T> the unbound interface-of-enum type
     * @param <E> the enum type
     * @param property the property to take the actual value from
     * @param ifCls the interface-of-enum type
     * @param eCls the enum class type
     * @return the test enum value, may be <b>null</b> for not found/compatible
     */
    public static <T, E extends Enum<E>> T getEnumValue(Property property, Class<T> ifCls, Class<E> eCls) {
        T result;
        try {
            result = getEnumValue(getStringValue(property), ifCls, eCls);
        } catch (ExecutionException e) {
            result = null;
        }
        return result;
    }

    /**
     * Turns a string tolerantly to a test enum value. May prevent usual issues from spec parsing/analysis.
     * Considers values of a {@code getValue} method if defined. Considers registered extending enums
     * in {@link EnumRegistry} with compatible type.
     * 
     * @param <T> the unbound interface-of-enum type
     * @param <E> the enum type
     * @param value the value to be matched against the enum literals
     * @param ifCls the interface-of-enum type
     * @param eCls the enum class type
     * @return the test enum value, may be <b>null</b> for not found/compatible
     */
    @SuppressWarnings("unchecked")
    public static <T, E extends Enum<E>> T getEnumValue(String value, Class<T> ifCls, Class<E> eCls) {
        T result = null;
        value = value.trim().toLowerCase();
        List<E> values = new ArrayList<>();
        for (Class<?> cls: EnumRegistry.getEnums(ifCls, eCls)) {
            for (Field f : cls.getDeclaredFields()) {
                int mod = f.getModifiers();
                if (cls.isAssignableFrom(f.getType()) && Modifier.isStatic(mod) && Modifier.isFinal(mod) 
                    && Modifier.isPublic(mod)) {
                    try {
                        values.add((E) f.get(null));
                    } catch (IllegalAccessException e) {
                    }
                }
            }
            Method[] methods = new Method[] {
                getDeclaredMethodSafe(cls, "getValue"),
                getDeclaredMethodSafe(cls, "getSemanticId")};
            for (E v: values) {
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
                    if (ifCls.isInstance(v)) {
                        result = ifCls.cast(v);
                    }
                    break;
                }
            }
            if (null != result) {
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
    
    /**
     * An iterator which runs until the {@code elementProvider} becomes <b>null</b>.
     * 
     * @param <T> the element type
     * @author Holger Eichelberger, SSE
     */
    public static class GetIterator<T> implements Iterator<T> {

        private Function<Integer, T> elementProvider;
        private int pos;
        private T act;

        /**
         * Creates the iterator starting at index position 0.
         * 
         * @param elementProvider the element provider
         */
        public GetIterator(Function<Integer, T> elementProvider) {
            this(elementProvider, 0);
        }

        /**
         * Creates the iterator.
         * 
         * @param elementProvider the element provider
         * @param pos the start index position
         */
        public GetIterator(Function<Integer, T> elementProvider, int pos) {
            this.pos = pos;
            this.elementProvider = elementProvider;
        }
        
        @Override
        public boolean hasNext() {
            act = elementProvider.apply(pos);
            return act != null;
        }

        @Override
        public T next() {
            pos++;
            return act;
        }
        
    }

}
