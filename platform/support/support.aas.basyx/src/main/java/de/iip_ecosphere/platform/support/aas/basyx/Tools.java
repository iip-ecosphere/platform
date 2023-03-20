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
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.basyx.aas.metamodel.map.descriptor.CustomId;
import org.eclipse.basyx.aas.metamodel.map.descriptor.ModelUrn;
import org.eclipse.basyx.submodel.metamodel.api.identifier.IIdentifier;
import org.eclipse.basyx.submodel.metamodel.api.reference.IKey;
import org.eclipse.basyx.submodel.metamodel.api.reference.IReference;
import org.eclipse.basyx.submodel.metamodel.api.reference.enums.KeyElements;
import org.eclipse.basyx.submodel.metamodel.api.reference.enums.KeyType;
import org.eclipse.basyx.submodel.metamodel.api.submodelelement.ISubmodelElement;
import org.eclipse.basyx.submodel.metamodel.map.identifier.Identifier;
import org.eclipse.basyx.submodel.metamodel.map.qualifier.LangString;
import org.eclipse.basyx.submodel.metamodel.map.reference.Key;
import org.eclipse.basyx.submodel.metamodel.map.reference.Reference;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.property.valuetype.ValueType;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import de.iip_ecosphere.platform.support.FileUtils;
import de.iip_ecosphere.platform.support.aas.AssetKind;
import de.iip_ecosphere.platform.support.aas.IdentifierType;
import de.iip_ecosphere.platform.support.aas.Type;

/**
 * Some utilities, such as for parameter checking. Public for testing.
 * 
 * @author Holger Eichelberger, SSE
 */
public class Tools {

    private static final Map<Type, ValueType> TYPES2BASYX = new HashMap<>();
    private static final Map<ValueType, Type> BASYX2TYPES = new HashMap<>();

    private static final Map<AssetKind, org.eclipse.basyx.aas.metamodel.api.parts.asset.AssetKind> ASSETKINDS2BASYX 
        = new HashMap<>();
    private static final Map<org.eclipse.basyx.aas.metamodel.api.parts.asset.AssetKind, AssetKind> BASYX2ASSETKINDS 
        = new HashMap<>();

    static {
        mapType(Type.BOOLEAN, ValueType.Boolean);
        mapType(Type.DOUBLE, ValueType.Double);
        mapType(Type.FLOAT, ValueType.Float);
        mapType(Type.INTEGER, ValueType.Int32);
        mapType(Type.AAS_INTEGER, ValueType.Integer);
        mapType(Type.STRING, ValueType.String);

        mapType(Type.NON_POSITIVE_INTEGER, ValueType.NonPositiveInteger);
        mapType(Type.NON_NEGATIVE_INTEGER, ValueType.NonNegativeInteger);
        mapType(Type.POSITIVE_INTEGER, ValueType.PositiveInteger);
        mapType(Type.NEGATIVE_INTEGER, ValueType.NegativeInteger);
        
        mapType(Type.INT8, ValueType.Int8);
        mapType(Type.INT16, ValueType.Int16);
        mapType(Type.INT32, ValueType.Int32);
        mapType(Type.INT64, ValueType.Int64);
        
        mapType(Type.UINT8, ValueType.UInt8);
        mapType(Type.UINT16, ValueType.UInt16);
        mapType(Type.UINT32, ValueType.UInt32);
        mapType(Type.UINT64, ValueType.UInt64);
        
        mapType(Type.LANG_STRING, ValueType.LangString);
        mapType(Type.ANY_URI, ValueType.AnyURI);
        mapType(Type.BASE64_BINARY, ValueType.Base64Binary);
        mapType(Type.HEX_BINARY, ValueType.HexBinary);
        mapType(Type.NOTATION, ValueType.NOTATION);
        mapType(Type.ENTITY, ValueType.ENTITY);
        mapType(Type.ID, ValueType.ID);
        mapType(Type.IDREF, ValueType.IDREF);
        
        mapType(Type.DURATION, ValueType.Duration);
        mapType(Type.DAY_TIME_DURATION, ValueType.DayTimeDuration); 
        mapType(Type.YEAR_MONTH_DURATION, ValueType.YearMonthDuration);
        mapType(Type.DATE_TIME, ValueType.DateTime);
        mapType(Type.DATE_TIME_STAMP, ValueType.DateTimeStamp);
        mapType(Type.G_DAY, ValueType.GDay);
        mapType(Type.G_MONTH, ValueType.GMonth); 
        mapType(Type.G_MONTH_DAY, ValueType.GMonthDay); 
        mapType(Type.G_YEAR, ValueType.GYear);
        mapType(Type.G_YEAR_MONTH, ValueType.GYearMonth);
        mapType(Type.Q_NAME, ValueType.QName);
        mapType(Type.NONE, ValueType.None);
        
        mapType(Type.ANY_TYPE, ValueType.AnyType); 
        mapType(Type.ANY_SIMPLE_TYPE, ValueType.AnySimpleType);
    }
    
    static {
        mapKind(AssetKind.TYPE, org.eclipse.basyx.aas.metamodel.api.parts.asset.AssetKind.TYPE);
        mapKind(AssetKind.INSTANCE, org.eclipse.basyx.aas.metamodel.api.parts.asset.AssetKind.INSTANCE);
    }
    
    /**
     * Maps an implementation-independent type into a BaSyx property-value type.
     * 
     * @param type the implementation-independent type
     * @param basyxType the corresponding BaSyx property-value type
     */
    private static void mapType(Type type, ValueType basyxType) {
        TYPES2BASYX.put(type, basyxType);
        BASYX2TYPES.put(basyxType, type);
    }

    /**
     * Maps an implementation-independent asset kind into a BaSyx asset kind.
     * 
     * @param kind the implementation-independent asset kind
     * @param basyxKind the corresponding BaSyx asset kind
     */
    private static void mapKind(AssetKind kind, org.eclipse.basyx.aas.metamodel.api.parts.asset.AssetKind basyxKind) {
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
        if (idShort.equals("value") || idShort.equals("invocationList")) { 
            throw new IllegalArgumentException("idShort shall not be \"" + idShort + "\"");
        }
        return idShort;
    }

    /**
     * Translates a implementation-independent type to an implementation-specific type.
     * 
     * @param type the implementation-independent type
     * @return the implementation-specific type
     */
    public static ValueType translate(Type type) {
        return TYPES2BASYX.get(type);
    }

    /**
     * Translates a implementation-specific type to an implementation-independent type.
     * 
     * @param type the implementation-specific type
     * @return the implementation-independent type
     */
    public static Type translate(ValueType type) {
        return BASYX2TYPES.get(type);
    }

    /**
     * Translates a implementation-independent asset kind to an implementation-specific asset kind.
     * 
     * @param kind the implementation-independent asset kind
     * @return the implementation-specific asset kind
     */
    public static org.eclipse.basyx.aas.metamodel.api.parts.asset.AssetKind translate(AssetKind kind) {
        return ASSETKINDS2BASYX.get(kind);
    }

    /**
     * Translates a implementation-specific asset kind to an implementation-independent asset kind.
     * 
     * @param kind the implementation-specific asset kind
     * @return the implementation-independent asset kind
     */
    public static AssetKind translate(org.eclipse.basyx.aas.metamodel.api.parts.asset.AssetKind kind) {
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
     * Translates an identifier. (supports URN and custom ids)
     * 
     * @param id the id showing some form of type, e.g., prefix "urn:", may be empty or <b>null</b> leading to 
     *   a custom identifier based on {@code dfltCustom}
     * @param dfltCustom the default value if id cannot be used
     * @return the identifier
     */
    public static IIdentifier translateIdentifier(String id, String dfltCustom) {
        IIdentifier result;
        if (null == id || id.length() == 0) {
            result = new CustomId(dfltCustom);
        } else if (id.startsWith(IdentifierType.URN_PREFIX)) {
            result = new ModelUrn(id);
        } else if (id.startsWith(IdentifierType.URN_TEXT_PREFIX)) {
            result = new ModelUrn(id.substring(IdentifierType.URN_TEXT_PREFIX.length()));
        } else if (id.startsWith(IdentifierType.IRDI_PREFIX)) {
            result = new Identifier(org.eclipse.basyx.submodel.metamodel.api.identifier.IdentifierType.IRDI, 
                id.substring(IdentifierType.IRDI_PREFIX.length()));
        } else if (id.startsWith(IdentifierType.IRI_PREFIX)) {
            result = new Identifier(org.eclipse.basyx.submodel.metamodel.api.identifier.IdentifierType.IRI, 
                id.substring(IdentifierType.IRI_PREFIX.length()));
        } else {
            result = new CustomId(id);
        }
        return result;
    }
    
    /**
     * Translates an identifier back into its string notation.
     * 
     * @param identifier the identifier (may be <b>null</b>, result is also <b>null</b> then)
     * @return the string notation
     */
    public static String translateIdentifier(IIdentifier identifier) {
        String result = null;
        if (identifier instanceof ModelUrn) {
            result = ((ModelUrn) identifier).getURN();
            if (!result.startsWith(IdentifierType.URN_PREFIX)) {
                result = IdentifierType.URN_PREFIX + result;
            }
        } else if (null != identifier) {
            switch (identifier.getIdType()) {
            case IRDI:
                result = IdentifierType.IRDI_PREFIX + identifier.getId();
                break;
            case IRI:
                result = IdentifierType.IRI_PREFIX + identifier.getId();
                break;
            default:
                result = identifier.getId();
                break;
            }
        }
        return result;
    }

    /**
     * Translates a reference. (supports IRDI, IRI)
     * 
     * @param id the for declaring the reference showing some form of type, e.g., prefix "irdi:", may be empty or 
     *   <b>null</b> leading to <b>null</b>, see {@link IdentifierType}
     * @return the reference or <b>null</b>
     */
    public static IReference translateReference(String id) {
        IReference result = null;
        if (id != null) {
            if (id.startsWith(IdentifierType.IRDI_PREFIX)) {
                //result = new Reference(new Key(KeyElements.PROPERTY, false, id.substring(5), KeyType.IRDI));
                result = new Reference(Collections.singletonList(new Key(KeyElements.CONCEPTDESCRIPTION, false, 
                    id.substring(IdentifierType.IRDI_PREFIX.length()), KeyType.IRDI)));
            } else if (id.startsWith(IdentifierType.IRI_PREFIX)) {
                result = new Reference(Collections.singletonList(new Key(KeyElements.CONCEPTDESCRIPTION, false, 
                    id.substring(IdentifierType.IRI_PREFIX.length()), KeyType.IRI)));
            }
        }
        return result;
    }
    
    /**
     * Translates a reference back to its string format. (supports IRDI, IRI)
     * 
     * @param ref the reference to translate back
     * @param stripPrefix whether the prefix shall be included
     * @return the translated reference or <b>null</b> if {@code ref} was <b>null</b> or it cannot be translated
     */
    public static String translateReference(IReference ref, boolean stripPrefix) {
        String result = null;
        if (ref != null && ref.getKeys().size() > 0) {
            IKey key = ref.getKeys().get(0);
            switch (key.getIdType()) {
            case IRDI:
                result = compose(IdentifierType.IRDI_PREFIX, key.getValue(), stripPrefix);
                break;
            case IRI:
                result = compose(IdentifierType.IRI_PREFIX, key.getValue(), stripPrefix);
                break;
            default:
                // we stay with null for now
                break;
            }
        }
        return result;
    }

    /**
     * Composes a prefix and a value if desired.
     * 
     * @param prefix the prefix
     * @param value the value
     * @param stripPrefix whether the prefix shall be included
     * @return {@code value} or {@code prefix} + {@code value}
     */
    private static String compose(String prefix, String value, boolean stripPrefix) {
        String result;
        if (stripPrefix) {
            result = value;
        } else {
            result = prefix + value;
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
     * Tries to dispose a Tomcat working directory.
     * 
     * @param baseDir the basic directory where the working directory is located in, may be <b>null</b> for default,
     *   i.e., program home directory
     * @param port the port number of the disposed Tomcat instance
     */
    static void disposeTomcatWorkingDir(File baseDir, int port) {
        if (null == baseDir) {
            baseDir = new File(".");
        }
        File workDir = new File(baseDir, "tomcat." + port);
        if (workDir.exists()) {
            if (!FileUtils.deleteQuietly(workDir)) { // may fail if process is not terminated, see Tomcats workaround
                try {
                    org.apache.commons.io.FileUtils.forceDeleteOnExit(workDir);
                } catch (IOException e) {
                }
            }
        }
        FileUtils.listFiles(FileUtils.getTempDirectory(), 
            f -> f.getName().startsWith("tomcat." + port), 
            f -> FileUtils.deleteQuietly(f)); // try to clean up left-over temp folders
        FileUtils.listFiles(FileUtils.getTempDirectory(), 
            f -> f.getName().startsWith("tomcat-docbase." + port), 
            f -> FileUtils.deleteQuietly(f)); // try to clean up left-over temp folders
    }
    
    /**
     * Translates an AAS to a BaSyx lang string.
     * 
     * @param ls the AAS lang string
     * @return the BaSyx lang string
     */
    public static LangString translate(de.iip_ecosphere.platform.support.aas.LangString ls) {
        return new LangString(ls.getLanguage(), ls.getDescription());
    }

    /**
     * Translates BaSyx lang string to an AAS lang string.
     * 
     * @param ls the BaSyx lang string
     * @return the AAS lang string
     */
    public static de.iip_ecosphere.platform.support.aas.LangString translate(LangString ls) {
        return new de.iip_ecosphere.platform.support.aas.LangString(ls.getLanguage(), ls.getDescription());
    }

    /**
     * Translates a value for a given target type.
     * 
     * @param type the BaSyx type
     * @param value the value to translate
     * @return the translated value
     */
    public static Object translateValueToBaSyx(ValueType type, Object value) {
        if (type == ValueType.LangString) {
            if (value instanceof String) {
                value = de.iip_ecosphere.platform.support.aas.LangString.create((String) value);
            } 
            if (value instanceof de.iip_ecosphere.platform.support.aas.LangString) {
                value = Tools.translate((de.iip_ecosphere.platform.support.aas.LangString) value);
            }
        }
        return value;
    }
    
    /**
     * Translates a BaSyx value back.
     * 
     * @param val the value to be translated
     * @param type the expected target type to support the translation, may be <b>null</b>
     * @return the translated value
     */
    @SuppressWarnings("unchecked")
    public static Object translateValueFromBaSyx(Object val, ValueType type) {
        if (ValueType.LangString == type) {
            if (LangString.isLangString(val)) {
                val = Tools.translate(LangString.createAsFacade((Map<String, Object>) val));
            } else if (val instanceof String) {
                Gson gson = new Gson();
                try {
                    Map<String, Object> map = gson.fromJson(val.toString(), Map.class);
                    val = Tools.translate(LangString.createAsFacade(map));
                } catch (JsonSyntaxException e) {
                    // do nothing
                }
            }
        }
        return val;
    }
    
    /**
     * Returns the value type of a submodel element.
     * 
     * @param elt the element
     * @return the value type or <b>null</b> if not available
     */
    public static ValueType getType(ISubmodelElement elt) {
        ValueType result = null;
        if (elt instanceof org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.property.Property) {
            result = ((org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.property.Property) elt)
                .getValueType();
        }
        return result;
    }

}
