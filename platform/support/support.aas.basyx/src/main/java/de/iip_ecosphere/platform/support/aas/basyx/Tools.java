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
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.eclipse.basyx.aas.metamodel.map.descriptor.CustomId;
import org.eclipse.basyx.aas.metamodel.map.descriptor.ModelUrn;
import org.eclipse.basyx.submodel.metamodel.api.identifier.IIdentifier;
import org.eclipse.basyx.submodel.metamodel.api.reference.IReference;
import org.eclipse.basyx.submodel.metamodel.api.reference.enums.KeyElements;
import org.eclipse.basyx.submodel.metamodel.api.reference.enums.KeyType;
import org.eclipse.basyx.submodel.metamodel.map.reference.Key;
import org.eclipse.basyx.submodel.metamodel.map.reference.Reference;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.property.valuetype.ValueType;
import org.slf4j.LoggerFactory;

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
        mapType(Type.INTEGER, ValueType.Integer);
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
        } else {
            result = new CustomId(id);
        } // IRI, others?
        return result;
    }

    /**
     * Translates a reference. (supports IRDI)
     * 
     * @param id the for declaring the reference showing some form of type, e.g., prefix "irdi:", may be empty or 
     * <b>null</b> leading to <b>null</b>
     * @return the reference or <b>null</b>
     */
    public static IReference translateReference(String id) {
        IReference result = null;
        if (id != null) {
            if (id.startsWith("irdi:")) {
                result = new Reference(new Key(KeyElements.PROPERTY, false, id.substring(5), KeyType.IRDI));
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
                    FileUtils.forceDeleteOnExit(workDir);
                } catch (IOException e) {
                }
            }
        } else {
            LoggerFactory.getLogger(Tools.class).warn("Tomcat working directory '" + workDir.getAbsolutePath() 
                + "' not found for disposal.");
        }
    }

}
