/**
 * ******************************************************************************
 * Copyright (c) {2025} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.aas.basyx2;

import java.io.IOException;
import java.math.BigInteger;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.net.ssl.SSLContext;

import jakarta.xml.bind.DatatypeConverter;

import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.util.AasUtils;
import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.Key;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultLangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.aas.AssetKind;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor;
import de.iip_ecosphere.platform.support.aas.Entity.EntityType;
import de.iip_ecosphere.platform.support.net.KeyStoreDescriptor;
import de.iip_ecosphere.platform.support.net.SslUtils;
import de.iip_ecosphere.platform.support.aas.IdentifierType;
import de.iip_ecosphere.platform.support.aas.LangString;
import de.iip_ecosphere.platform.support.aas.SemanticIdRecognizer;
import de.iip_ecosphere.platform.support.aas.SetupSpec.ComponentSetup;
import de.iip_ecosphere.platform.support.aas.Type;

/**
 * Some utilities, such as for parameter checking. Public for testing.
 * 
 * @author Holger Eichelberger, SSE
 */
public class Tools {

    // TODO cleanup structures
    private static final Map<Type, DataTypeDefXsd> TYPES2BASYX = new HashMap<>();
    private static final Map<DataTypeDefXsd, Type> BASYX2TYPES = new HashMap<>();
    private static final Map<DataTypeDefXsd, Function<Object, String>> OBJECT2BASYX = new HashMap<>();
    private static final Map<DataTypeDefXsd, Function<String, Object>> BASYX2OBJECT = new HashMap<>();

    private static final Set<DataTypeDefXsd> BASYX2TYPES_ALIAS = new HashSet<>();
    private static final Map<Class<?>, KeyTypes> KEYTYPES = new HashMap<>();

    private static final Map<AssetKind, org.eclipse.digitaltwin.aas4j.v3.model.AssetKind> ASSETKINDS2BASYX 
        = new HashMap<>();
    private static final Map<org.eclipse.digitaltwin.aas4j.v3.model.AssetKind, AssetKind> BASYX2ASSETKINDS 
        = new HashMap<>();
    
    private static final Function<Object, String> DFLT_OBJECT2BASXY = o -> null != o ? o.toString() : null;
    private static final Function<String, Object> DFLT_BASYX2OBJECT = o -> o;
    private static final Function<Object, String> DFLT_INT2BASXY = o -> DatatypeConverter.printInt((Integer) o);
    private static final Function<String, Object> DFLT_BASYX2INT = s -> DatatypeConverter.parseInt(s);
    private static final Function<Object, String> DFLT_INTEGER2BASXY 
        = o -> DatatypeConverter.printInteger((BigInteger) o);
    private static final Function<String, Object> DFLT_BASYX2INTEGER = s -> DatatypeConverter.parseInteger(s);
    private static final Function<Object, String> DFLT_STRING2BASXY = o -> DatatypeConverter.printString(o.toString());
    private static final Function<String, Object> DFLT_BASYX2STRING = s -> DatatypeConverter.parseString(s);
    
    static {
        mapType(Type.BOOLEAN, DataTypeDefXsd.BOOLEAN, 
            o -> DatatypeConverter.printBoolean((Boolean) o), s -> DatatypeConverter.parseBoolean(s));
        mapType(Type.DOUBLE, DataTypeDefXsd.DOUBLE, 
            o -> DatatypeConverter.printDouble((Double) o), s -> DatatypeConverter.parseDouble(s));
        mapType(Type.FLOAT, DataTypeDefXsd.FLOAT,
            o -> DatatypeConverter.printFloat((Float) o), s -> DatatypeConverter.parseFloat(s));
        mapType(Type.INTEGER, DataTypeDefXsd.INT, DFLT_INT2BASXY, DFLT_BASYX2INT);
        mapType(Type.AAS_INTEGER, DataTypeDefXsd.INT, DFLT_INTEGER2BASXY, DFLT_BASYX2INTEGER);
        mapType(Type.STRING, DataTypeDefXsd.STRING, DFLT_STRING2BASXY, DFLT_BASYX2STRING);

        mapType(Type.NON_POSITIVE_INTEGER, DataTypeDefXsd.NON_POSITIVE_INTEGER, DFLT_INTEGER2BASXY, DFLT_BASYX2INTEGER);
        mapType(Type.NON_NEGATIVE_INTEGER, DataTypeDefXsd.NON_NEGATIVE_INTEGER, DFLT_INTEGER2BASXY, DFLT_BASYX2INTEGER);
        mapType(Type.POSITIVE_INTEGER, DataTypeDefXsd.POSITIVE_INTEGER, DFLT_INTEGER2BASXY, DFLT_BASYX2INTEGER);
        mapType(Type.NEGATIVE_INTEGER, DataTypeDefXsd.NEGATIVE_INTEGER, DFLT_INTEGER2BASXY, DFLT_BASYX2INTEGER);
        
        mapType(Type.INT8, DataTypeDefXsd.INT, DFLT_INT2BASXY, DFLT_BASYX2INT);
        mapType(Type.INT16, DataTypeDefXsd.INT, DFLT_INT2BASXY, DFLT_BASYX2INT);
        mapType(Type.INT32, DataTypeDefXsd.INT, DFLT_INT2BASXY, DFLT_BASYX2INT);
        mapType(Type.INT64, DataTypeDefXsd.INT, DFLT_INT2BASXY, DFLT_BASYX2INT);
        
        mapType(Type.UINT8, DataTypeDefXsd.UNSIGNED_INT, DFLT_INT2BASXY, DFLT_BASYX2INT);
        mapType(Type.UINT16, DataTypeDefXsd.UNSIGNED_INT, DFLT_INT2BASXY, DFLT_BASYX2INT);
        mapType(Type.UINT32, DataTypeDefXsd.UNSIGNED_INT, DFLT_INT2BASXY, DFLT_BASYX2INT);
        mapType(Type.UINT64, DataTypeDefXsd.UNSIGNED_INT, DFLT_INT2BASXY, DFLT_BASYX2INT);
        
        mapType(Type.LANG_STRING, DataTypeDefXsd.STRING, o -> langStringToString(o), DFLT_BASYX2STRING);
        mapType(Type.ANY_URI, DataTypeDefXsd.ANY_URI, DFLT_STRING2BASXY, DFLT_BASYX2STRING);
        mapType(Type.BASE64_BINARY, DataTypeDefXsd.BASE64BINARY,
            o -> DatatypeConverter.printBase64Binary((byte[]) o), s -> DatatypeConverter.parseBase64Binary(s));
        mapType(Type.HEX_BINARY, DataTypeDefXsd.HEX_BINARY,
            o -> DatatypeConverter.printHexBinary((byte[]) o), s -> DatatypeConverter.parseHexBinary(s));
        
        mapType(Type.DURATION, DataTypeDefXsd.DURATION, DFLT_OBJECT2BASXY, DFLT_BASYX2OBJECT);
        mapType(Type.DATE_TIME, DataTypeDefXsd.DATE_TIME,
            o -> DatatypeConverter.printDateTime(convertToCalendar(o)), s -> DatatypeConverter.parseDateTime(s));
        mapType(Type.DATE_TIME_STAMP, DataTypeDefXsd.TIME, 
            o -> DatatypeConverter.printTime(convertToCalendar(o)), s -> DatatypeConverter.parseTime(s)); // preliminary
        mapType(Type.G_DAY, DataTypeDefXsd.GDAY, DFLT_OBJECT2BASXY, DFLT_BASYX2OBJECT);
        mapType(Type.G_MONTH, DataTypeDefXsd.GMONTH, DFLT_OBJECT2BASXY, DFLT_BASYX2OBJECT); 
        mapType(Type.G_MONTH_DAY, DataTypeDefXsd.GMONTH_DAY, DFLT_OBJECT2BASXY, DFLT_BASYX2OBJECT); 
        mapType(Type.G_YEAR, DataTypeDefXsd.GYEAR, DFLT_OBJECT2BASXY, DFLT_BASYX2OBJECT);
        mapType(Type.G_YEAR_MONTH, DataTypeDefXsd.GYEAR_MONTH, DFLT_OBJECT2BASXY, DFLT_BASYX2OBJECT);
        mapType(Type.NONE, null, DFLT_OBJECT2BASXY, DFLT_BASYX2OBJECT); // TODO preliminary
    }
    
    static {
        mapKind(AssetKind.TYPE, org.eclipse.digitaltwin.aas4j.v3.model.AssetKind.TYPE);
        mapKind(AssetKind.INSTANCE, org.eclipse.digitaltwin.aas4j.v3.model.AssetKind.INSTANCE);
    }
    
    static {
        mapKeyType(org.eclipse.digitaltwin.aas4j.v3.model.AnnotatedRelationshipElement.class, 
            KeyTypes.ANNOTATED_RELATIONSHIP_ELEMENT);
        mapKeyType(org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell.class, 
            KeyTypes.ASSET_ADMINISTRATION_SHELL);
        mapKeyType(org.eclipse.digitaltwin.aas4j.v3.model.BasicEventElement.class, KeyTypes.BASIC_EVENT_ELEMENT);
        mapKeyType(org.eclipse.digitaltwin.aas4j.v3.model.Blob.class, KeyTypes.BLOB);
        mapKeyType(org.eclipse.digitaltwin.aas4j.v3.model.Capability.class, KeyTypes.CAPABILITY);
        mapKeyType(org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription.class, KeyTypes.CONCEPT_DESCRIPTION);
        mapKeyType(org.eclipse.digitaltwin.aas4j.v3.model.DataElement.class, KeyTypes.DATA_ELEMENT);
        mapKeyType(org.eclipse.digitaltwin.aas4j.v3.model.Entity.class, KeyTypes.ENTITY);
        mapKeyType(org.eclipse.digitaltwin.aas4j.v3.model.EventElement.class, KeyTypes.EVENT_ELEMENT);
        mapKeyType(org.eclipse.digitaltwin.aas4j.v3.model.File.class, KeyTypes.FILE);
        //mapKeyType(, KeyTypes.FRAGMENT_REFERENCE);
        //mapKeyType(, KeyTypes.GLOBAL_REFERENCE);
        mapKeyType(org.eclipse.digitaltwin.aas4j.v3.model.Identifiable.class, KeyTypes.IDENTIFIABLE);
        mapKeyType(org.eclipse.digitaltwin.aas4j.v3.model.MultiLanguageProperty.class, 
            KeyTypes.MULTI_LANGUAGE_PROPERTY);
        mapKeyType(org.eclipse.digitaltwin.aas4j.v3.model.Operation.class, KeyTypes.OPERATION);
        mapKeyType(org.eclipse.digitaltwin.aas4j.v3.model.Property.class, KeyTypes.PROPERTY);
        mapKeyType(org.eclipse.digitaltwin.aas4j.v3.model.Range.class, KeyTypes.RANGE);
        mapKeyType(org.eclipse.digitaltwin.aas4j.v3.model.Referable.class, KeyTypes.REFERABLE);
        mapKeyType(org.eclipse.digitaltwin.aas4j.v3.model.ReferenceElement.class, KeyTypes.REFERENCE_ELEMENT);
        mapKeyType(org.eclipse.digitaltwin.aas4j.v3.model.RelationshipElement.class, KeyTypes.RELATIONSHIP_ELEMENT);
        mapKeyType(org.eclipse.digitaltwin.aas4j.v3.model.Submodel.class, KeyTypes.SUBMODEL);
        mapKeyType(org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement.class, KeyTypes.SUBMODEL_ELEMENT);
        mapKeyType(org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection.class, 
                    KeyTypes.SUBMODEL_ELEMENT_COLLECTION);
        mapKeyType(org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList.class, KeyTypes.SUBMODEL_ELEMENT_LIST);
    }


    /**
     * Converts an object to a calendar.
     * 
     * @param obj the object
     * @return the calendar or <b>null</b>
     */
    @SuppressWarnings("deprecation")
    private static Calendar convertToCalendar(Object obj) {
        Calendar result = null;
        if (obj instanceof Date) {
            result = Calendar.getInstance();
            result.setTime((Date) obj);
        } else if (obj instanceof Calendar) {
            result = (Calendar) obj;
        } else if (obj != null) {
            result = Calendar.getInstance();
            result.setTime(new Date(Date.parse(obj.toString())));
        }
        return result;
    }
    
    /**
     * Maps a key type.
     * 
     * @param cls the class to be used as key
     * @param type the key type
     */
    static void mapKeyType(Class<?> cls, KeyTypes type) {
        KEYTYPES.put(cls, type);
    }

    /**
     * Maps a BaSyx property-value type into an implementation-independent type.
     * 
     * @param type the implementation-independent type
     * @param basyxType the corresponding BaSyx property-value type
     */
    public static void mapBaSyxType(Type type, DataTypeDefXsd basyxType) {
        BASYX2TYPES.put(basyxType, type);
        BASYX2TYPES_ALIAS.add(basyxType);
    }
    
    /**
     * Maps an implementation-independent type into a BaSyx property-value type.
     * 
     * @param type the implementation-independent type
     * @param basyxType the corresponding BaSyx property-value type
     */
    public static void mapType(Type type, DataTypeDefXsd basyxType, Function<Object, String> toBaSyx, 
        Function<String, Object> fromBaSyx)  {
        TYPES2BASYX.put(type, basyxType);
        BASYX2TYPES.put(basyxType, type);
        OBJECT2BASYX.put(basyxType, toBaSyx);
        BASYX2OBJECT.put(basyxType, fromBaSyx);
    }

    /**
     * Maps an implementation-independent asset kind into a BaSyx asset kind.
     * 
     * @param kind the implementation-independent asset kind
     * @param basyxKind the corresponding BaSyx asset kind
     */
    public static void mapKind(AssetKind kind, org.eclipse.digitaltwin.aas4j.v3.model.AssetKind basyxKind) {
        ASSETKINDS2BASYX.put(kind, basyxKind);
        BASYX2ASSETKINDS.put(basyxKind, kind);
    }

    /**
     * Checks a given URN for not being empty.
     * 
     * @param urn the URN
     * @return {@code urn}
     * @throws IllegalArgumentException if the urn is empty or <b>null</b>
     */
    public static String checkUrn(String urn) {
        if (null == urn || 0 == urn.length()) {
            throw new IllegalArgumentException("urn must be given");
        }
        return urn;
    }

    /**
     * Checks a given short id for not being empty.
     * 
     * @param idShort the short id
     * @return {@code idShort}
     * @throws IllegalArgumentException if the id is empty or <b>null</b>
     */
    public static String checkId(String idShort) {
        if (null == idShort || 0 == idShort.length()) {
            throw new IllegalArgumentException("idShort must be given");
        }
        
        //https://wiki.eclipse.org/BaSyx_/_Documentation_/_AssetAdministrationShell
        //Property idShort shall only feature letters, digits, underscore ("_"); starting mandatory with a letter. 
        //Property idShort shall be matched case-insensitive. 
        if (!idShort.matches("[a-zA-Z][a-zA-Z0-9_]+")) {
            throw new IllegalArgumentException("idShort '" + idShort + "' shall only feature letters, digits, "
                + "underscore (\"_\"); starting mandatory with a letter.");
        }
        /*if (idShort.equals("value") || idShort.equals("invocationList")) { 
            throw new IllegalArgumentException("idShort shall not be \"" + idShort + "\"");
        }*/
        return idShort;
    }

    /**
     * Translates a implementation-specific entity type to an implementation-independent type.
     * 
     * @param type the implementation-independent type
     * @return the implementation-specific type
     */
    public static EntityType translate(
            org.eclipse.digitaltwin.aas4j.v3.model.EntityType type) {
        EntityType result;
        switch (type) {
        case CO_MANAGED_ENTITY:
            result = EntityType.COMANAGEDENTITY;
            break;
        case SELF_MANAGED_ENTITY:
        default:
            result = EntityType.SELFMANAGEDENTITY;
            break;
        }
        return result;
    }

    /**
     * Translates a implementation-independent entity type to an implementation-specific type.
     * 
     * @param type the implementation-independent type
     * @return the implementation-specific type
     */
    public static org.eclipse.digitaltwin.aas4j.v3.model.EntityType translate(
        EntityType type) {
        org.eclipse.digitaltwin.aas4j.v3.model.EntityType result;
        switch (type) {
        case COMANAGEDENTITY:
            result = org.eclipse.digitaltwin.aas4j.v3.model.EntityType.CO_MANAGED_ENTITY;
            break;
        case SELFMANAGEDENTITY:
        default:
            result = org.eclipse.digitaltwin.aas4j.v3.model.EntityType.SELF_MANAGED_ENTITY;
            break;
        }
        return result;
    }

    /**
     * Translates a implementation-independent type to an implementation-specific type.
     * 
     * @param type the implementation-independent type
     * @return the implementation-specific type
     */
    public static DataTypeDefXsd translate(Type type) {
        return TYPES2BASYX.get(type);
    }

    /**
     * Translates a implementation-specific type to an implementation-independent type.
     * 
     * @param type the implementation-specific type
     * @return the implementation-independent type
     */
    public static Type translate(DataTypeDefXsd type) {
        return BASYX2TYPES.get(type);
    }
    
    /**
     * Returns whether {@code type} is mapped through {@link #mapBaSyxType(Type, ValueType)} as alias/one-way 
     * fashion to an implementation type.
     * 
     * @param type the type to check
     * @return {@code true} for one-way, {@code false} else
     */
    public static boolean isAlias(DataTypeDefXsd type) {
        return BASYX2TYPES_ALIAS.contains(type);
    }

    /**
     * Translates a implementation-independent asset kind to an implementation-specific asset kind.
     * 
     * @param kind the implementation-independent asset kind
     * @return the implementation-specific asset kind
     */
    public static org.eclipse.digitaltwin.aas4j.v3.model.AssetKind translate(AssetKind kind) {
        return ASSETKINDS2BASYX.get(kind);
    }

    /**
     * Translates a implementation-specific asset kind to an implementation-independent asset kind.
     * 
     * @param kind the implementation-specific asset kind
     * @return the implementation-independent asset kind
     */
    public static AssetKind translate(org.eclipse.digitaltwin.aas4j.v3.model.AssetKind kind) {
        return BASYX2ASSETKINDS.get(kind);
    }

    /**
     * Turns an id into a URL path.
     * 
     * @param id the id
     * @return the URL path
     */
    static String idToUrlPath(String id) {
        return id; // to allow for translations, whitespaces, whatever
    }
    
    /**
     * Adds {@code element} to {@code list} and returns {@code list}.
     * 
     * @param <T> the element type
     * @param list the list to add to (may be <b>null</b>)
     * @param element the element to add
     * @return a list with {@code element} added
     */
    static <T> List<T> addElement(List<T> list, T element) {
        if (null == list) {
            list = new ArrayList<T>();
        }
        list.add(element);
        return list;
    }
    
    /**
     * Removes {@code element} from {@code list} and returns {@code list}.
     * 
     * @param <T> the element type
     * @param list the list to remove the element from (may be <b>null</b>)
     * @param element the element to remove
     * @return {@code list}
     */
    static <T> List<T> removeElement(List<T> list, T element) {
        if (null != list) {
            list.remove(element);
        }
        return list;
    }

    /**
     * Removes elements complying with {@code pred} from {@code list} and returns {@code list}.
     * 
     * @param <T> the element type
     * @param list the list to remove the element from (may be <b>null</b>)
     * @param pred a predicate to select the elements to be removed
     * @return {@code list}
     */
    static <T> List<T> removeElements(List<T> list, Predicate<T> pred) {
        if (null != list) {
            list.removeIf(pred);
        }
        return list;
    }

    /**
     * Returns an element based on its {@code idShort}.
     * 
     * @param <T> the element type
     * @param list the list to look into
     * @param idShort the id short to return the element for
     * @return the element or <b>null</b> for not found
     */
    static <T extends org.eclipse.digitaltwin.aas4j.v3.model.Referable> T getElement(List<T> list, String idShort) {
        for (int i = 0; i < list.size(); i++) {
            T elt = list.get(i);
            if (elt.getIdShort().equals(idShort)) {
                return elt;
            }
        }
        return null;
    }
    
    /**
     * Returns the elements of the container {@code cont}.
     * 
     * @param cont the container
     * @return the elements or <b>null</b> for not available
     */
    static List<org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement> getElements(Object cont) {
        List<org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement> result;
        if (cont instanceof org.eclipse.digitaltwin.aas4j.v3.model.Submodel) {
            result = ((org.eclipse.digitaltwin.aas4j.v3.model.Submodel) cont).getSubmodelElements();
        } else if (cont instanceof org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection) {
            result = ((org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection) cont).getValue();
        } else if (cont instanceof org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList) {
            result = ((org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList) cont).getValue();
        } else {
            result = null;
        }
        return result;
    }

    /**
     * Translates an identifier. (supports URN and custom ids)
     * 
     * @param id the id showing some form of type, e.g., prefix "urn:", may be empty or <b>null</b> leading to 
     *   a custom identifier based on {@code dfltCustom}
     * @param dfltCustom the default value if id cannot be used
     * @return the identifier
     */
    public static String translateIdentifierToBaSyx(String id, String dfltCustom) {
        String result = id;
        if (null == id || id.length() == 0) { 
            result = dfltCustom;
        }
        return result;
    }
    
    /**
     * Translates an identifier back into its string notation.
     * 
     * @param identifier the identifier (may be <b>null</b>, result is also <b>null</b> then)
     * @return the string notation
     */
    public static String translateIdentifierFromBaSyx(String identifier) {
        String result = identifier;
        return result;
    }

    /**
     * Translates a reference, supports IRDI, IRI, see {@link SemanticIdRecognizer}.
     * 
     * @param id the for declaring the reference showing some form of type, e.g., prefix "irdi:", may be empty or 
     *   <b>null</b> leading to <b>null</b>, see {@link IdentifierType}
     * @return the reference or <b>null</b>
     */
    public static org.eclipse.digitaltwin.aas4j.v3.model.Reference translateReference(String id) {
        org.eclipse.digitaltwin.aas4j.v3.model.Reference result = null;
        result = toReferenceIf(result, IdentifierType.IRDI_PREFIX, id);
        result = toReferenceIf(result, IdentifierType.IRI_PREFIX, id);
        //result = toReferenceIf(result, null, id); // potential fallback, see tests
        return result;
    }

    /**
     * Turns a semantic {@code id} conditionally into a reference.
     * 
     * @param ref optional reference, already detected, <b>null</b> for none
     * @param prefix optional id prefix to match, <b>null</b> matches everything
     * @param id the semantic id, potentially prefixed, ignored if <b>null</b> or empty
     * @return {@code ref} if not <b>null</b>, the created reference if the prefix matches, <b>null</b> else
     */
    private static org.eclipse.digitaltwin.aas4j.v3.model.Reference toReferenceIf(
        org.eclipse.digitaltwin.aas4j.v3.model.Reference ref, String prefix, String id) {
        org.eclipse.digitaltwin.aas4j.v3.model.Reference result = ref;
        if (null == result && null != id && id.length() > 0) {
            if (prefix == null || id.startsWith(prefix)) {
                if (prefix != null) {
                    id = id.substring(prefix.length());
                }
                org.eclipse.digitaltwin.aas4j.v3.model.Key key = new DefaultKey.Builder()
                    .type(KeyTypes.CONCEPT_DESCRIPTION)
                    .value(id)
                    .build();
                result = new DefaultReference.Builder()
                    .type(ReferenceTypes.EXTERNAL_REFERENCE)
                    .keys(key)
                    .build();
            }
        }
        return result;
    }

    /**
     * Returns a model reference to {@code target}.
     * 
     * @param target the element to return the references to
     * @return the reference
     */
    public static org.eclipse.digitaltwin.aas4j.v3.model.Reference createModelReference(
            org.eclipse.digitaltwin.aas4j.v3.model.Identifiable target) {
        return AasUtils.toReference(target);
    }
    
    /**
     * Returns a model reference to {@code target}.
     * 
     * @param target the element to return the references to
     * @return the reference
     */
    public static org.eclipse.digitaltwin.aas4j.v3.model.Reference createModelReference(
        org.eclipse.digitaltwin.aas4j.v3.model.Referable target) {
        KeyTypes keyType = KEYTYPES.get(target.getClass());
        org.eclipse.digitaltwin.aas4j.v3.model.Key key = new DefaultKey.Builder().type(keyType).value(
            target.getIdShort()).build();
        return new DefaultReference.Builder().type(ReferenceTypes.MODEL_REFERENCE).keys(key).build();
    }

    /**
     * Sets the {@code semanticId} on {@code target}.
     * 
     * @param caller the caller to return
     * @param semanticId the semantic id
     * @param target the target element to set the id on
     * @return caller
     */
    public static <T> T setSemanticId(T caller, String semanticId, 
        org.eclipse.digitaltwin.aas4j.v3.model.HasSemantics target) {
        org.eclipse.digitaltwin.aas4j.v3.model.Reference ref = Tools.translateReference(semanticId);
        if (ref != null && target != null) {
            target.setSemanticId(ref);
        }
        return caller;
    }
    
    /**
     * Translates a reference back to its string format. Supports IRDI, IRI, see {@link SemanticIdRecognizer}.
     * 
     * @param ref the reference to translate back
     * @param stripPrefix whether the prefix shall be included
     * @return the translated reference or <b>null</b> if {@code ref} was <b>null</b> or it cannot be translated
     */
    public static String translateReference(org.eclipse.digitaltwin.aas4j.v3.model.Reference ref, boolean stripPrefix) {
        String result = null;
        if (ref != null) { // TODO preliminary
            for (Key key : ref.getKeys()) {
                if (KeyTypes.CONCEPT_DESCRIPTION == key.getType()) {
                    result = key.getValue();
                }
            }
            if (null != result && !stripPrefix) {
                String prefix = SemanticIdRecognizer.getIdentifierPrefix(result);
                if (null != prefix) {
                    result = prefix + result;
                }
            }
        }
        return result;
    }

    /**
     * Tests the values in {@code pptions} against the constants in {@code cls} and returns 
     * a matching constant or {@code dflt}.
     * 
     * @param <E> the enum type
     * @param options the options to check
     * @param dflt the default value
     * @param cls the enum class providing the constants
     * @return the matching option or {@code dflt}
     */
    public static <E extends Enum<E>> E getOption(String[] options, E dflt, Class<E> cls) {
        E result = dflt;
        for (String o : options) {
            try {
                result = Enum.valueOf(cls, o);
            } catch (IllegalArgumentException e) {
                // ignore, not that options
            }
        }
        return result;
    }
    
    
    /**
     * Transforms the given descriptions.
     * 
     * @param description the description(s) as language string(s)
     * @return the translated BaSyx instance
     */
    public static List<LangStringTextType> translate(de.iip_ecosphere.platform.support.aas.LangString... description) {
        List<LangStringTextType> result = new ArrayList<>();
        for (de.iip_ecosphere.platform.support.aas.LangString d: description) {
            result.add(Tools.translate(d));
        }
        return result;
    }
    
    /**
     * Turns a {@link LangString} given as object to a String.
     * 
     * @param obj the object
     * @return the string value
     */
    private static String langStringToString(Object obj) {
        if (obj instanceof LangString) {
            LangString ls = (LangString) obj;
            return ls.getDescription() + "@" + ls.getLanguage();
        } else {
            return DFLT_STRING2BASXY.apply(obj);
        }
    }

    /**
     * Translates an AAS to a BaSyx lang string.
     * 
     * @param ls the AAS lang string
     * @return the BaSyx lang string
     */
    public static LangStringTextType translate(de.iip_ecosphere.platform.support.aas.LangString ls) {
        DefaultLangStringTextType s = new DefaultLangStringTextType();
        s.setLanguage(ls.getLanguage());
        s.setText(ls.getDescription());
        return s;
    }    
    
    /**
     * Translates BaSyx LangStrings back.
     * 
     * @param ls the LangStrings
     * @return the translation into a map
     */
    public static Map<String, de.iip_ecosphere.platform.support.aas.LangString> translate(List<LangStringTextType> ls) {
        Map<String, de.iip_ecosphere.platform.support.aas.LangString> result = null;
        if (null != ls && !ls.isEmpty()) {
            result = new HashMap<>();
            for (LangStringTextType lang : ls) {
                result.put(lang.getLanguage(), new de.iip_ecosphere.platform.support.aas.LangString(
                    lang.getLanguage(), lang.getText()));
            }
        }
        return result;
    }
    
    // checkstyle: stop exception type check

    /**
     * Translates a value for a given target type.
     * 
     * @param type the BaSyx type
     * @param value the value to translate
     * @return the translated value
     */
    public static String translateValueToBaSyx(DataTypeDefXsd type, Object value) {
        String result = null;
        if (value != null) {
            Function<Object, String> func = OBJECT2BASYX.get(type);
            if (null == func) {
                LoggerFactory.getLogger(Tools.class).warn("No conversion defined for '{}', converting "
                    + "to String as fallback.", type);
            } else {
                try {
                    result = func.apply(value);
                } catch (Throwable t) {
                    LoggerFactory.getLogger(Tools.class).warn("Conversion of '{}' failed for '{}', converting "
                        + "to String as fallback. Reason: {}", value, type, t.getMessage());
                }
            }
            if (null == result) {
                result = value.toString();
            }
        }
        return result;
    }

    /**
     * Translates a BaSyx (property) value back.
     * 
     * @param val the value to be translated
     * @param type the expected target type to support the translation, may be <b>null</b>
     * @return the translated value
     */
    public static Object translateValueFromBaSyx(String val, DataTypeDefXsd type) {
        Object result = null;
        if (null != val) {
            Function<String, Object> func = BASYX2OBJECT.get(type);
            if (null == func) {
                LoggerFactory.getLogger(Tools.class).warn("No conversion defined for '{}', passing "
                    + "String through as fallback.", type);
            } else {
                try {
                    result = func.apply(val);
                } catch (Throwable t) {
                    LoggerFactory.getLogger(Tools.class).warn("Conversion of '{}' failed for '{}', passing "
                        + "String through as fallback. Reason: {}", val, type, t.getMessage());
                }
            }
            if (null == result) {
                result = val;
            }
        }
        return result;
    }
    
    // checkstyle: resume exception type check
    
    /**
     * Translates a BaSyx value back.
     * 
     * @param val the value to be translated
     * @param type the expected target type to support the translation, may be <b>null</b>
     * @return the translated value
     */
    public static Object translateValueFromBaSyx(SubmodelElement val, DataTypeDefXsd type) {
        Object result;
        if (val instanceof Property) {
            result = translateValueFromBaSyx(((Property) val).getValue(), type);
        } else {
            result = null == val ? null : val.toString(); // TODO preliminary
        }
        return result;
    }

    /**
     * Consumes an HTTPClient builder and applies it to client.
     * @param <C> the client type
     * @author Holger Eichelberger, SSE
     */
    interface HttpClientBuilderConsumer<C> {

        /**
         * Applies {@code builder} to {@code client}.
         * 
         * @param builder the builder
         * @param client the client
         */
        public void accept(HttpClient.Builder builder, C client, Consumer<HttpRequest.Builder> interceptor);
        
    }

    /**
     * Consumes an URI and applies it to client.
     * @param <C> the client type
     * @author Holger Eichelberger, SSE
     */
    interface UriConsumer<C> {
        
        /**
         * Applies {@code uri} to {@code client}.
         * 
         * @param uri the uri
         * @param client the client
         */
        public void accept(String uri, C client);
        
    }

    /**
     * Consumes a client and creates for it an API instance.
     * @param <A> the API type
     * @param <C> the client type
     * @author Holger Eichelberger, SSE
     */
    interface ApiProvider<A, C> {

        /**
         * Creates the API instance.
         * 
         * @param uri the client URI
         * @param client the client
         */
        public A create(String uri, C client);
        
    }

    // checkstyle: stop parameter number check
    
    /**
     * Creates an API instance.
     * 
     * @param <A> the API type
     * @param <C> the client type
     * @param setup the component setup carrying endpoint, keystore, authentication
     * @param uri specific URI, may be <b>null</b> for {@code endpoint}
     * @param builderConsumer applies the configured HTTPClient builder to the client
     * @param uriConsumer applies the uri (either {@code uri} or {@code endpoint} to the client
     * @param apiProvider creates the API instance
     * @return the API instance
     */
    static <A, C> A createApi(ComponentSetup setup, String uri, C client, HttpClientBuilderConsumer<C> builderConsumer, 
        UriConsumer<C> uriConsumer, ApiProvider<A, C> apiProvider, Class<A> cls) {
        de.iip_ecosphere.platform.support.Endpoint endpoint = setup.getEndpoint();
        KeyStoreDescriptor keystore = setup.getKeyStore();
        KeyStoreDescriptor ksd = null;
        if (null != keystore && keystore.appliesToClient()) {
            if (null == uri || uri.startsWith(endpoint.toServerUri())) {
                ksd = keystore;
            }
        }
        Consumer<HttpRequest.Builder> interceptor = null;
        if (setup.getAuthentication() != null) {
            interceptor = b -> { 
                AuthenticationDescriptor.authenticate((n, v) -> b.header(n, v), setup.getAuthentication());
            };
        }
        try {
            builderConsumer.accept(createHttpClient(ksd), client, interceptor);
        } catch (IOException e) {
            LoggerFactory.getLogger(AasRegistryUtils.class).error(
                "While creating {}, creating http client failed: {}", cls.getName(), e.getMessage());
        }
        String u = null == uri ? endpoint.toServerUri() : uri;
        uriConsumer.accept(u, client);
        
        // TokenManager may go via interceptor
        return apiProvider.create(u, client);
    }

    // checkstyle: resume parameter number check
    
    /**
     * Creates a HTTP client builder from a keystore descriptor.
     * 
     * @param desc the descriptor
     * @return the client builder
     * @throws IOException if creating the SSL/TSL context from {@code desc} fails
     */
    public static HttpClient.Builder createHttpClient(KeyStoreDescriptor desc) throws IOException {
        SSLContext context = null;
        Boolean oldHNV = null;
        if (null != desc) {
            context = SslUtils.createTlsContext(desc.getPath(), desc.getPassword(), desc.getAlias());
            oldHNV = setJdkHostnameVerification(desc);
        }
        HttpClient.Builder result = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1);
        if (null != context) {
            result.sslContext(context);
        } 
        /* if (AUTHENTICATED) {
            // authenticator sets header empty, requires challenge-response-auth 
            result.authenticator(new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                }
            });
        }*/
        if (null != oldHNV) {
            setJdkHostnameVerification(oldHNV);
        }
        return result;
    }

    /**
     * Sets JDK HTTP/SSL hostname verification.
     * 
     * @param desc the keystore descriptor indicating whether verification is enabled or disabled
     * @return the value of the flag before, by default {@code false}
     */
    static boolean setJdkHostnameVerification(KeyStoreDescriptor desc) {
        return setJdkHostnameVerification(!desc.applyHostnameVerification());
    }

    /**
     * Sets JDK HTTP/SSL hostname verification.
     * 
     * @param disable {@code true} the verification, {@code false} enables it
     * @return the value of the flag before, by default {@code false}
     */
    static boolean setJdkHostnameVerification(boolean disable) {
        final String prop = "jdk.internal.httpclient.disableHostnameVerification";
        boolean old = Boolean.valueOf(System.getProperty(prop, "false"));
        System.setProperty(prop, String.valueOf(disable));
        return old;
    }

}
