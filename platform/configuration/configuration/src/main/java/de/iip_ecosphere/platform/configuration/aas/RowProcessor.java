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

package de.iip_ecosphere.platform.configuration.aas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.iip_ecosphere.platform.configuration.aas.AasType.EntityType;
import de.iip_ecosphere.platform.configuration.aas.ParsingUtils.AasEnumResultHandler;
import de.iip_ecosphere.platform.support.Version;
import de.iip_ecosphere.platform.support.aas.IdentifierType;
import de.iip_ecosphere.platform.support.aas.SemanticIdRecognizer;
import de.iip_ecosphere.platform.support.logging.Logger;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;

import static de.iip_ecosphere.platform.configuration.aas.ParsingUtils.*;

/**
 * Processes individual rows in an IDTA specification table.
 * 
 * @author Holger Eichelberger, SSE
 */
class RowProcessor {

    private static final boolean CFG_REMOVE_NOTES = true;
    //private static final boolean CFG_CLEAN_SEMANTICID = true;
    
    private static Pattern titlePattern
        = Pattern.compile("(IDTA\\W+\\d+-\\d+-\\d+)\\W+(.*)");
    private static Pattern langStringEnd
        = Pattern.compile(".*@[a-z]+$");
    private static Pattern seeSection
        = Pattern.compile("See [Ss]ection \\d+(\\.\\d+)*(.*)");
    private static Pattern extensionDesc
        = Pattern.compile(".* SMC (.*) in .* with (.*) elements.*");

    private List<AasType> aasTypes = new ArrayList<>();
    private List<AasEnum> aasEnums = new ArrayList<>();
    private AasEnumResultHandler enumsHandler = new AasEnumResultHandler(aasEnums, e -> enumAdded(e));
    private List<AasType> current = new ArrayList<>();
    private String[] rawData = new String[4];
    private String[] lastRawData = new String[4];
    private int maxRawIndex = -1;
    private int column = 0;
    private int lastHeader1Row = -1;
    private int lastHeader2Row = -1;
    private int row = 0;
    private Version version;
    private String versionIdentifier;
    private String versionedName;
    @SuppressWarnings("unused")
    private String projectName;
    private String specNumber = "0000";
    private AasField lastField;
    private List<AasEnum> lastEnum = new ArrayList<>();
    private String lastSemanticIdRaw;
    private Map<String, String> deferredTypes = new HashMap<>();
    private List<AasField> genericFields = new ArrayList<>();
    private int genericTypeCount = 0;
    private boolean currentTypeIsAspect;
    private boolean currentMultiSemIdProcessed;
    private int lastEnum1Row = -1;
    private int lastEnum2Row = -1;

    /**
     * Notifies about starting a data row. Further data passed in through {@link #addDataToRow(String)} is 
     * collected for the actual row until {@link #endRow()} is called.
     */
    void startRow() {
        column = 0;
        for (int i = 0; i < rawData.length; i++) {
            rawData[i] = null;
        }            
    }

    /**
     * Adds (cell) data for the actual row.
     * 
     * @param data the data, may be <b>null</b>
     */
    void addDataToRow(String data) {
        final String idtaMarker = "IDTA";
        if (null == versionedName && null != data && data.startsWith(idtaMarker)) {
            String dta = removeLinebreaks(data);
            versionedName = dta;
            Matcher matcher = titlePattern.matcher(dta);
            if (matcher.matches()) {
                versionIdentifier = matcher.group(1);
                String tmp = matcher.group(2).trim();
                final String marker = "Submodel for ";
                if (tmp.startsWith(marker)) { // topic: IDTA-02003-1-2 name
                    tmp = tmp.substring(marker.length()).trim();
                }
                tmp = stripTitleDate(tmp);
                String vTmp = versionIdentifier.substring(idtaMarker.length()).trim().replace("-", ".");
                int firstDotPos = vTmp.indexOf(".");
                if (firstDotPos > 3 && firstDotPos + 1 < vTmp.length()) { // starts with 5 digits
                    specNumber = vTmp.substring(0, firstDotPos);
                    vTmp = vTmp.substring(firstDotPos + 1);
                } 
                try {
                    version = new Version(vTmp);
                    projectName = toIdentifier(idtaMarker + " " + tmp);
                } catch (IllegalArgumentException e) {
                    projectName = toIdentifier(versionIdentifier + " " + tmp);
                    getLogger().warn("Cannot turn potential version string into version: {}", vTmp);
                }
            }
        }
        if (data != null && column < rawData.length) {
            rawData[column] = data;
            maxRawIndex = column;
        }
        column++;
    }
    
    /**
     * Is called as an {@link AasEnumResultHandler} if a new enumeration instance was added.
     * 
     * @param en the enumeration
     */
    private void enumAdded(AasEnum en) {
        if (ParsingEnumKind.VALUE_LIST == en.getParsingEnumKind()) { // IDTA-02023-0-9
            lastEnum.add(en);
        }
    }
    
    /**
     * If there is a date given at the end of the title, remove that.
     * 
     * @param value the title string value
     * @return {@code value} or a shortened version
     */
    private static String stripTitleDate(String value) { // IDTA 02007-1-0
        String result = value;
        int pos = value.lastIndexOf(' ');
        if (pos > 0) {
            try {
                int year = Integer.parseInt(value.substring(pos).trim());
                if (year > 2000) { // we just assume that month is the next
                    int monthPos = value.lastIndexOf(' ', pos - 1);
                    if (monthPos > 0) {
                        pos = monthPos;
                    }
                } 
                result = value.substring(0, pos).trim();
            } catch (NumberFormatException e) {
                // ok, no year
            }
        }
        return result;
    }
    
    /**
     * Calls {@code setter} on the current {@link #current AasType}. Logs
     * a warning if there is no current instance.
     * 
     * @param setter the setter
     * @param field the field name
     */
    private void setOnCurrent(Consumer<AasType> setter, String field) {
        if (current != null) {
            for (AasType t: current) {
                setter.accept(t);
            }
        } else {
            getLogger().warn("No current AasData for: {}", field);
        }
    }
    
    /**
     * Returns whether collect data for a row contains data
     * until the given index.
     * 
     * @param untilIndex the index to check for until
     * @return {@code true} for relevant data, {@code false} else
     */
    private boolean hasRawData(int untilIndex) {
        boolean hasRawData = maxRawIndex >= 0;
        for (int i = 0; i < Math.min(untilIndex, rawData.length); i++) {
            hasRawData &= rawData[i] != null;
        }
        return hasRawData;
    }

    /**
     * Notifies that collecting data for a row has ended and the collected information
     * shall be classified and processed.
     * 
     * @see #processTwoColumns()
     * @see #processFourColumns()
     */
    void endRow() {
        if (maxRawIndex == 0 && hasRawData(0)) {
            // need to handle data on next table without header but without reading irrelevant data from next non-field
            // table
            if (rawData[0].startsWith("<<")) { // IDTA02026 UML from figure by SmallPDF
                endSection();
            }
            if (rawData[0].startsWith("Table") || rawData[0].startsWith("Figure") 
                || rawData[0].startsWith("2.8 Display names") || rawData[0].startsWith("2.4 Example")) { // IDTA2008-1-1
                endSection();
                if (rawData[0].contains(" ValueList ")) { // IDTA02026, "-" is problematic
                    lastEnum1Row = 0; // indicate start
                }
            }
        } else if (maxRawIndex == 1) {
            if (!lastEnum.isEmpty() && hasRawData(1)) { // IDTA02023-0-9
                if (lastEnum1Row < 0) { // we are not in IDTA 02026 format
                    String idShort = rawData[0];
                    String identifier = getEnumLiteralIdentifier(idShort);
                    addLastEnumLiteral(new AasEnumLiteral(idShort, rawData[1], null, identifier));
                }
            } else if (hasRawData(1)) {
                lastEnum.clear(); // enum reading stops here, next 2 columns
                processTwoColumns();
            } else if (hasRawData(0)) {
                postProcessFourColumns(); // IDTA 02007-1-0
            }
        } else if (maxRawIndex == 2 || maxRawIndex == 3) {
            if (maxRawIndex == 2 && hasRawData(3)) { // IDTA 02026
                if (lastEnum1Row > 0 && lastEnum2Row > 0 && current.size() > 0) {
                    for (AasType t : current) {
                        enumsHandler.add(new AasEnum(t, ParsingEnumKind.VALUE_LIST, id -> id + "ValueList"));
                    }
                    current.clear();
                }
                if (lastEnum1Row == 0 && rawData[0].equals("-") && rawData[1].equals("-") 
                    && rawData[2].startsWith("semanticId =")) {
                    lastEnum1Row = row;
                }
                if (rawData[0].equals("Preferred Name") && rawData[1].startsWith("Description") 
                    && rawData[2].startsWith("Dictionary")) {
                    lastEnum2Row = row;
                }
            }
            
            if (!lastEnum.isEmpty() && maxRawIndex == 2 && rawData[0] != null && hasRawData(3)) { // DRAFT PCF 2023
                String valueId = rawData[0];
                String idShort = rawData[1].replace("–", "-");
                String semanticId = rawData[2];
                String description = null;
                if (lastEnum2Row > 0) { // IDTA 02026
                    // we may have a non-separated classification system, only for ValueList/enum tables
                    if (null != semanticId) {  
                        semanticId = SemanticIdRecognizer.trySafeSemanticIdWithDictionaryFrom(
                            semanticId, null, true, false);
                    }
                    // different structure
                    idShort = rawData[0];
                    description = rawData[1];
                    // and new symbols
                    if (semanticId.contains("n/a")) {
                        semanticId = null;
                    }
                }
                String identifier = getEnumLiteralIdentifier(idShort);
                AasEnumLiteral lit = new AasEnumLiteral(idShort, semanticId, description, identifier);
                lit.setValue(valueId);
                addLastEnumLiteral(lit);
            } else if (maxRawIndex == 2 && null == rawData[0] && lastField != null) {
                postProcessFourColumns(); // IDTA 02007-1-0
            } else if (hasRawData(2) || hasRawData(3)) {
                lastEnum.clear(); // enum reading stops here, next 4 columns
                processFourColumns();
            }
        }
        maxRawIndex = -1;
        row++;
        System.arraycopy(rawData, 0, lastRawData, 0, rawData.length); // IDTA 2007-1-0
    }
    
    /**
     * Adds enum literals.
     * 
     * @param literal the literal to add
     */
    private void addLastEnumLiteral(AasEnumLiteral literal) {
        for (AasEnum e : lastEnum) {
            e.addLiteral(literal); // clone?
        }
    }

    /**
     * Returns the identifier to be used for an enum literal.
     * 
     * @param idShort the idShort
     * @return the identifier or <b>null</b>
     */
    private static String getEnumLiteralIdentifier(String idShort) {
        String identifier = null;
        int pos = idShort.indexOf("-");
        if (pos > 0) {
            int splitPos = idShort.indexOf(" - ");
            if (splitPos > 0) {
                identifier = idShort.substring(0, splitPos).trim();
            } else {
                identifier = idShort;
            }
            identifier = identifier.replace("-", "_");
        }
        return identifier;
    }
    
    /**
     * Notifies that the end of a specification section has been reached and
     * that temporary information for the last section shall be cleared for the next section.
     */
    void endSection() {
        lastHeader1Row = -1;
        lastHeader2Row = -1;
        lastField = null;
        lastEnum.clear();
        lastSemanticIdRaw = null;
        currentTypeIsAspect = false;
        currentMultiSemIdProcessed = false;
        lastEnum1Row = -1;
        lastEnum2Row = -1;
    }
    
    // checkstyle: stop method length check

    /**
     * Processes two column tables usually indicating a submodel or a submodel
     * element collection.
     */
    private void processTwoColumns() {
        String header = rawData[0];
        String headerNoWhitespace = removeWhitespace(header);
        String value = rawData[1];
        if (header.endsWith(":")) { // colon not on all headers, e.g., isCaseOf IDTA 02002-1-0
            header = header.substring(0, header.length() - 1);
        }
        if (header.equals("idShort")) {
            String idShort = null;
            boolean stateIdShort = false;
            boolean isMultiValued = false;
            String[] tmp = toLines(value);
            if (tmp.length == 1) {
                idShort = tmp[0];
            } else if (tmp.length == 2) {
                idShort = tmp[0];
                stateIdShort = hasFixedIdShort(tmp[1]);
            } else {
                getLogger().warn("idShort field has more than two lines: {}", value);
            }
            if (null != idShort) {
                if (idShort.endsWith("{00}")) {
                    idShort = idShort.substring(0, idShort.length() - 4);
                    isMultiValued = true;
                } else if (idShort.startsWith("{") && idShort.endsWith("#00}")) { // IDTA-02027-1-0
                    idShort = idShort.substring(1, idShort.length() - 4).trim();
                    isMultiValued = true;
                }
                getLogger().info("Processing type {}", idShort);
                List<AasType> types = new ArrayList<>();
                String[] ids = null;
                currentTypeIsAspect = false;
                if (idShort.startsWith("{") && idShort.contains(" = ")) { // IDTA-02017-1-0
                    int eqPos = idShort.indexOf(" = ");
                    ids = pruneIds(idShort.substring(eqPos + 3).split(" | "));
                    currentTypeIsAspect = containsPlainType(aasTypes, ids) || containsPlainType(current, ids);
                } else {
                    ids = toIdsBySeparator(idShort);
                }
                currentMultiSemIdProcessed = ids.length > 1; // multiple type ids will be processed, others not
                for (String id: ids) {
                    AasType t = new AasType(id, stateIdShort, isMultiValued);
                    t.setAspect(currentTypeIsAspect);
                    types.add(t);
                    if (ParsingUtils.isGenericIdShort(id)) {
                        t.setGeneric(true);
                        String dispName = t.getIdShort();
                        genericTypeCount++;
                        if (dispName.startsWith("{") && dispName.endsWith("}")) {
                            dispName = dispName.substring(1, dispName.length() - 1) + "_" + genericTypeCount;
                        } else {
                            dispName += "_" + genericTypeCount;
                        }
                        t.setDisplayName(dispName);
                        t.setIdShort("Generic_" + ParsingUtils.toIdentifier(id) + "_" + genericTypeCount);
                        if (genericFields.size() > 0) { // heuristic, specified in sequence after field
                            AasField field = genericFields.remove(0);
                            field.setValueType(t.getIdShort());
                            getLogger().warn("Replaced generic field/type name with '{}'. Please review.", 
                                t.getIdShort());
                        }
                    }
                }
                storeAsCurrent(types);
            }
        } else if (header.equals("Class")) {
            final AasSmeType smeType = AasSmeType.toType(value);
            setOnCurrent(a -> a.setSmeType(smeType), "Class"); // TODO check class, enum?
        } else if (header.equals("semanticId")) {
            if (current.size() > 1) { // IDTA 02023-0-9
                String[] ids = value.split(" or ");
                int idPos = 0;
                for (int curPos = 0; curPos < current.size(); curPos++) {
                    AasType cur = current.get(curPos); 
                    setSemanticId(s -> cur.setSemanticId(s), ids[idPos], false);
                    if (idPos < ids.length - 1) {
                        idPos++;
                    }
                }
            } else {
                setSemanticId(s -> setOnCurrent(a -> a.setSemanticId(s), "semanticId"), value, true);
            }
            lastSemanticIdRaw = value;
            if (currentTypeIsAspect) {
                Map<String, String> semIds = parseMappedSemanticIds(value);
                if (null != semIds) {
                    setOnCurrent(a -> a.setMappedSemanticIds(semIds), "mappedSemanticIds");
                }
            }
        } else if (header.equals("isCaseOf")) {
            setSemanticId(s -> setOnCurrent(a -> a.setIsCaseOf(s), "isCaseOf"), value, false);
        } else if (header.equals("AllowDuplicates") || headerNoWhitespace.equals("AllowDuplicates")) {
            setOnCurrent(a -> a.setAllowDuplicates(Boolean.valueOf(value)), "AllowDuplicates");
        } else if (header.equals("Parent")) { // TODO consider parent for correct nesting?
            String parent = value;
            if (null != parent) {
                if (parent.startsWith("Submodel") || parent.startsWith("SMC")) {
                    int pos = parent.indexOf("\"");
                    int lastPos = -1;
                    if (pos > 0) {
                        lastPos = parent.lastIndexOf("\"");
                    } else {
                        pos = parent.indexOf("â€œ");
                        lastPos = parent.lastIndexOf("â€?");
                    }
                    if (pos > 0 && pos < lastPos) {
                        parent = parent.substring(pos + 1, lastPos);
                    }
                } else if (value.startsWith("Asset Admin") || value.equals("AAS")) { // typo in IDTA 02002-1-0
                    parent = AasType.PARENT_AAS;
                }
            }
            String parentValue = parent;
            getLogger().info("Parent: {}", parentValue);
            setOnCurrent(a -> a.setParent(parentValue), "Parent");
        } else if (header.equals("Explanation")) {
            setOnCurrent(a -> a.setEntityType(EntityType.fromText(value)), "EntityType");
            final String desc = filterLanguage(removeLinebreaks(value));
            setOnCurrent(a -> a.setDescription(desc), "Description");
            String tmp = desc;
            if (null != lastSemanticIdRaw) { // usually before in parsing sequence... IDTA-02017-1-0
                tmp += " " + lastSemanticIdRaw;
            }
            final String multiSemanticIdValue = tmp;
            setOnCurrent(a -> a.setMultiSemanticIds(!currentMultiSemIdProcessed 
                && countSemanticIdMarker(multiSemanticIdValue) > 1), "MultiSemanticIds");
        } else if (header.equals("Kind")) {
            getLogger().info("Kind: {}", value); // ignore for now, taken from title, not present in all
        } else if (header.equals("Version")) {
            getLogger().info("Version: {}", value); // ignore for now, taken from title, not present in all
        } else if (header.equals("Revision")) {
            getLogger().info("Revision: {}", value); // ignore for now, taken from title, not present in all
        } else {
            getLogger().warn("Unknown 2 column header: {}", header);
        }
    }
    
    /**
     * Splits {@code idShort} into multiple ids if certain (varying) conditions/formats apply.
     * 
     * @param idShort the idShort(s) to split
     * @return the splitted idShort(s)
     */
    private static String[] toIdsBySeparator(String idShort) {
        String[] ids = null;
        int pos = idShort.indexOf("(");
        if (pos > 0) { // IDTA 02026-1-0
            int pos2 = idShort.indexOf(")");
            if (pos2 > pos) {
                String tmpIdShort = idShort.substring(0, pos) + "/ " + idShort.substring(pos + 1, pos2);
                tmpIdShort = tmpIdShort.replaceAll("\\s*/\\s*", "/");
                ids = tmpIdShort.split("/");
            }
        }
        if (null == ids) {
            ids = idShort.split(" or "); // IDTA 02023-0-9
        }
        return ids;
    }
    
    // checkstyle: resume method length check

    /**
     * Parses a mapping of semantic IDs in case of an aspect type (IDTA-02017-1-0).
     * 
     * @param value the raw semanticID value of the type
     * @return the mapped semantic ids or <b>null</b>
     */
    private Map<String, String> parseMappedSemanticIds(String value) { // IDTA-02017-1-0
        Map<String, String> result = null;
        int endPos;
        do {
            endPos = value.indexOf(")");
            if (endPos > 0) {
                String sub = value.substring(0, endPos + 1);
                value = value.substring(endPos + 1);
                int startPos = sub.lastIndexOf("(");
                String condition = sub.substring(startPos);
                sub = sub.substring(0, startPos);
                AtomicReference<String> semanticId = new AtomicReference<>(null);
                setSemanticId(s -> semanticId.set(s), sub, false);
                final String onlyForMarker = "(only for ";
                if (condition.startsWith(onlyForMarker)) {
                    condition = condition.substring(onlyForMarker.length(), condition.length() - 1).trim();
                } else {
                    getLogger().warn("Unconsidered condition for aspect type semantic Id: {}. Ignoring.", condition);
                    condition = null;
                }
                if (null != semanticId.get() && null != condition) {
                    if (null == result) {
                        result = new HashMap<>();
                    }
                    result.put(condition, semanticId.get());
                }
            }
        } while (endPos > 0);
        return result;
    }
    
    /**
     * Prunes the given ids for alternative markers.
     * 
     * @param ids the ids
     * @return the pruned ids
     */
    private String[] pruneIds(String[] ids) {
        List<String> tmp = new ArrayList<>();
        for (int i = 0; i < ids.length; i++) {
            if (!ids[i].trim().equals("|")) {
                tmp.add(ids[i]);
            }
        }
        return tmp.toArray(new String[tmp.size()]);
    }
    
    /**
     * Does {@code types} contains a plain non-aspect type with idShort from {@code ids}.
     * 
     * @param types the types to search
     * @param ids the IDs to look for
     * @return {@code true} if found, {@code false} else
     */
    private boolean containsPlainType(List<AasType> types, String[] ids) {
        return types.stream().anyMatch(t -> contains(ids, t.getIdShort()) && !t.isAspect());
    }
    
    /**
     * Preprocesses a semantic id specification.
     * 
     * @param value the value to preprocess
     * @return the preprocessed value
     */
    private static String preprocessSemanticIdSpec(String value) {
        if (value.startsWith("[[")) { // Typo in IDTA 02007-1-0 ReleaseNotes
            value = value.substring(1);
        }
        if (value.toUpperCase().startsWith("[IRDI PATH")) { // IDTA 02004-1-2, IDTA 02010-1-0
            int pos = value.indexOf("/");
            if (pos > 0) {
                value = "[IRDI]" + value.substring(pos + 1);
            }
        }
        return value;
    }
    
    /**
     * Returns whether {@code value} seems to be a specification of a semantic id.
     * 
     * @param value the value
     * @param followedBySpace whether a following whitespace (occurs in some cases) shall specifically be checked and 
     *   be there
     * @return {@code true} if {@code value} seems to be a semantic id, {@code false} else 
     */
    private static boolean isSemanticIdSpec(String value, boolean followedBySpace) { // IDTA 02007-1-0
        value = preprocessSemanticIdSpec(value);
        boolean isSemanticId = value.startsWith("[IRI]") || value.startsWith("[IRDI]");
        if (isSemanticId && followedBySpace) { // IDTA 02007-1-0
            int pos = value.indexOf("]");
            isSemanticId = (pos + 1 < value.length() && Character.isWhitespace(value.charAt(pos + 1)));
        }
        return isSemanticId;
    }

    /**
     * Extracts, converts and finally sets a semantic id passed in from {@code value} through {@code setter}.
     * 
     * @param setter the semanticId setter
     * @param value the actual value representing the semantic id (usually prefixed by [IRI] or [IRDI])
     * @param considerFallback in case that there is no semantic id type given, try a full resolution 
     *     via {@link SemanticIdRecognizer}; may not be needed if a conceptdescription is supposed to override
     *     the semantic id
     */
    private static void setSemanticId(Consumer<String> setter, String value, boolean considerFallback) {
        value = preprocessSemanticIdSpec(value);
        int startPos = value.indexOf("["); 
        int endPos = value.indexOf("]"); 
        if (startPos < endPos) {
            String type = value.substring(startPos + 1, endPos);
            String tmp = value.substring(endPos + 1).trim();
            if ("IRDI".equals(type) && tmp.startsWith("173")) { // unknown: IDTA02004-1-2 0 missing
                tmp = "0" + tmp;
            }
            String semanticId = SemanticIdRecognizer.getSemanticIdFrom(tmp, false);
            if (null != semanticId) { // IDTA02004-1-2
                if ("IRI".equals(type) && semanticId.length() > 0) {
                    setter.accept(IdentifierType.compose(IdentifierType.IRI_PREFIX, semanticId));
                } else if ("IRDI".equals(type) && semanticId.length() > 0) {
                    setter.accept(IdentifierType.compose(IdentifierType.IRDI_PREFIX, semanticId));
                } else {
                    getLogger().warn("semanticId field has unexpected structure: {}", value);
                }
            }
        } else {
            String semanticId = null;
            if (considerFallback) {
                semanticId = SemanticIdRecognizer.getSemanticIdFrom(value, true);
                if (null != semanticId) { // inference fallback for missing [] in IDTA-02005-1-0
                    setter.accept(semanticId);
                } 
            }
            if (null == semanticId) {
                getLogger().warn("semanticId field has unexpected structure: {}", value);
            }
        }
    }

    /**
     * Processes four data cells. This may detect the first/second head line 
     * of the specification field tables, pass on the following lines to {@link #processFourColumnsAsField()}
     * or ignore the lines in case of other four column tables.
     * 
     * @see #processFourColumnsAsField()
     */
    private void processFourColumns() {
        boolean process = true;
        if ("[SME type]".equals(rawData[0])) {
            boolean ok = "semanticId = [idType]value".equals(rawData[1]);
            ok &= "[valueType]".equals(rawData[2]); // fails in excel reading
            ok &= "card.".equals(rawData[3]); // fails in excel reading
            if (ok) {
                process = false;
                lastHeader1Row = row;
            } else {
                getLogger().warn("Unknown line 1 header format: {}", Arrays.toString(rawData));
            }
        } else if (rawData[0].equals("idShort")) {
            boolean ok = "Description@en".equals(rawData[1]); // TODO extract language?
            // ignore case: IDTA-02007-1-0 header
            ok &= "example".equalsIgnoreCase(rawData[2]) || "example".equalsIgnoreCase(rawData[3]); 
            if (ok) {
                lastHeader2Row = row;
                process = false;
            } else {
                getLogger().warn("Unknown line 2 header format: {}", Arrays.toString(rawData));
            }
        }
        if (process) {
            if (lastHeader1Row >= 0 && lastHeader2Row >= 0) {
                if (!"class name of contained elements".equalsIgnoreCase(rawData[0])) { // IDTA 02045-1-0
                    processFourColumnsAsField();
                }
            } else {
                if (lastHeader1Row >= 0 || lastHeader2Row >= 0) { // title page, nothing found
                    getLogger().warn("Missing headers: {}, {}", lastHeader1Row, lastHeader2Row);
                }
            }
        }
    }

    /**
     * In case that field rows are split into two, post-process the last field by adding
     * the field's description and example values. 
     * 
     * @see #setExampleValues(String, List, AasField)
     */
    private void postProcessFourColumns() { // IDTA 2007-1-0
        if (lastField != null) { 
            // we've probably overlooked the value type (without brackets in own cell in line before)
            if (lastRawData[2] != null && (lastField.getValueType() == null || "".equals(lastField.getValueType()))) {
                String type = fixTypeName(lastRawData[2]);
                if (null != AasField.mapPropertyType(type, null)) { // is it plausible?
                    lastField.setValueType(type);
                }
            }
            // and probably the semantic id in own cell, but potentially with line breaks
            setSemanticId(s -> lastField.setSemanticId(s), removeLinebreaks(lastRawData[1]), false);
            // in this line, there might be a field description and/or example values
            if (rawData[1] != null && rawData[2] != null) { 
                setFieldDescription(rawData[1], lastField);     
                setExampleValues(rawData[2], null, lastField);
            } else if (rawData[1] != null) {
                /*String desc = lastField.getDescription();
                if (desc != null) {
                    desc = desc + " " + rawData[1];
                } else {
                    desc = rawData[1];
                }*/
                setFieldDescription(rawData[1], lastField); 
            } else {
                getLogger().warn("Post processing 4 columns, unconsidered format: {}", Arrays.toString(rawData));
            }
        }
    }

    /**
     * Turns four data cells into an AAS field.
     * 
     * @see #processSmeTypeIdShort(String, AasField)
     * @see #processSemanticIdDescription(String, AasField)
     */
    private void processFourColumnsAsField() {
        AasField field = new AasField();
        String[] furtherIdShort = processSmeTypeIdShort(rawData[0], field);
        if (isGenericIdShort(field.getIdShort())) {
            field.setGeneric(true); // IDTA-02003-1-2, IDTA-02012-1-0 a bit heuristic and rest left to to the user
            genericFields.add(field);
        }
        processValueTypeExample(rawData[2], field);
        processSemanticIdDescription(rawData[1], field);
        processFieldCardinality(rawData[3], field);
        setOnCurrent(a -> a.addField(field), "fields");
        lastField = field;
        
        if (furtherIdShort != null) { // IDTA 02021-1-0
            for (String id: furtherIdShort) {
                deferredTypes.put(id, field.getIdShort()); // id -> originalType, resolve later
                AasField furtherField = new AasField(field);
                furtherField.setIdShort(id);
                if (AasSmeType.SUBMODEL_ELEMENT_COLLECTION == furtherField.getSmeType() 
                    || AasSmeType.SUBMODEL_ELEMENT_LIST == furtherField.getSmeType()) {
                    furtherField.setValueType(id);
                }
                setOnCurrent(a -> a.addField(furtherField), "fields");
            }
        }
    }
    
    /**
     * Processes the field part in the first cell, containing the SME type and the idShort.
     * 
     * @param data the cell data
     * @param field the field to be modified as a side effect.
     * @return additional fields in the same row, usually <b>null</b>
     */
    private String[] processSmeTypeIdShort(String data, AasField field) {
        String[] result = null;
        String[] ids = data.replace("\nor ", " or ").split(" or "); // IDTA 02021-1-0
        if (ids.length > 1) { // IDTA 02021-1-0
            result = Arrays.copyOfRange(ids, 1, ids.length);
            for (int r = 0; r < result.length; r++) {
                result[r] = removeWhitespace(result[r]).trim();
            }
            data = ids[0]; // go on with first, clone later
        }
        String[] tmp = toLines(data);
        String rawType = null;
        String rawId = null;
        if (tmp.length == 2 && tmp[1].endsWith("}")) { // line break fix for 02006-2-0
            tmp = new String[] {tmp[0] + tmp[1]};
        }
        if (tmp.length == 1) {
            int pos = tmp[0].indexOf("]");
            if (pos > 0) {
                rawType = tmp[0].substring(0, pos + 1);
                rawId = tmp[0].substring(pos + 1);
            } else { // missing type, IDTA-2014-1-0
                rawType = "property";
                rawId = tmp[0];
            }
        } else if (tmp.length == 2) {
            rawType = tmp[0];
            rawId = tmp[1];
        } else if (tmp.length > 2) { // IDTA-02008-1-1
            rawType = tmp[0];
            int nextNonEmptyPos = 2;
            while (tmp[nextNonEmptyPos].trim().length() == 0) { // IDTA-02001-1-0
                nextNonEmptyPos++;
            }
            if (!tmp[nextNonEmptyPos].trim().startsWith("Example")) {
                rawId = String.join(" ", Arrays.copyOfRange(tmp, 1, tmp.length));
            } else {
                rawId = tmp[1];
            }
        }
        if (rawType != null && rawId != null) {
            AasSmeType smeType = AasSmeType.toType(removeBrackets(rawType));
            field.setSmeType(smeType);
            String idShort = removeWhitespace(rawId);
            boolean isMultiValued = false;
            if (idShort.endsWith("{00}")) {
                idShort = idShort.substring(0, idShort.length() - 4);
                isMultiValued = true;
            }
            getLogger().info("Processing field {}/{}", idShort, smeType);
            field.setIdShort(idShort, isMultiValued);
            if (AasSmeType.SUBMODEL_ELEMENT_COLLECTION == field.getSmeType()
                || AasSmeType.SUBMODEL_ELEMENT_LIST == field.getSmeType()
                || AasSmeType.ENTITY == field.getSmeType()) {
                field.setValueType(field.getIdShort());
            }
        } else {
            getLogger().warn("Unknown SMEtype/idShort format: {}", data);
        }
        return result;
    }

    // TODO examples with ""
    
    /**
     * Processes the second field cell containing the semantic id and the description, potentially with enums.
     *  
     * @param data the cell data
     * @param field the field to be modified as a side effect
     */
    private void processSemanticIdDescription(String data, AasField field) {
        if (data != null) {
            String[] tmp = toLines(data);
            if (tmp.length == 1) { // topic: 02003-1-2, no clear separator
                int pos = tmp[0].indexOf(" ");
                if (isSemanticIdSpec(tmp[0], true)) { // IDTA 02007-1-0
                    pos = tmp[0].indexOf(" ", pos + 1);
                }
                if (pos > 0) {
                    setSemanticId(s -> field.setSemanticId(s), tmp[0].substring(0, pos), false);
                    setFieldDescription(tmp[0].substring(pos + 1), field); // consider enums?    
                } else { // no field description here, IDTA 02007-1-0
                    setSemanticId(s -> field.setSemanticId(s), tmp[0], false);
                }
            } else if (tmp.length >= 2) { // topic: 02002-1-0
                setSemanticId(s -> field.setSemanticId(s), tmp[0], false);
                String description = String.join(" ", Arrays.copyOfRange(tmp, 1, tmp.length));
                
                String semId = field.getSemanticId();
                if (null != semId && semId.length() > 0) { // description may still be after the semanticId in tmp[0]
                    int pos = semId.indexOf(":"); // remove prefix
                    if (pos > 0) {
                        semId = semId.substring(pos + 1);
                    }
                    pos = semId.lastIndexOf(" ");
                    String pattern = pos < 0 ? semId : semId.substring(pos + 1);
                    pos = tmp[0].indexOf(pattern);
                    if (pos > 0) {
                        description = tmp[0].substring(pos + pattern.length()).trim() + " " + description;
                    }
                }
                String rest = removeNote(description);
                description = inferEnum(rest, description, field, enumsHandler, false);
                setFieldDescription(description, field);
            } else {
                getLogger().warn("Unknown semanticId/description format: {}", data);
            }
        }
    }
        
    /**
     * Removes a note description text if {@link #CFG_REMOVE_NOTES}.
     *  
     * @param data the data to remove the text from
     * @return {@code data} or modified {@code data}
     */
    private static String removeNote(String data) {
        if (CFG_REMOVE_NOTES) {
            int pos = data.indexOf("Note: "); // there are sometimes multiple notes, IDTA 02002-1-0
            if (pos > 0) {
                data = data.substring(0, pos); // we ignore the notes here
            }
        }
        return data;
    }
    
    /**
     * Sets the field description, considers in-place "isCaseOf" information and notes.
     *  
     * @param description the raw description 
     * @param field the target field to set the description on
     * 
     * @see #removeNote(String)
     */
    private void setFieldDescription(String description, AasField field) {
        final String marker = "isCaseOf:";
        if (description.startsWith(marker)) {
            int pos = description.indexOf("]", marker.length());
            if (pos > 0) {
                pos = consumeWhitespaces(description, pos + 1);
                pos = consumeNonWhitespaces(description, pos);
                if (pos < description.length()) {
                    String isCaseOf = description.substring(0, pos);
                    isCaseOf = isCaseOf.substring(marker.length());
                    setSemanticId(s -> field.setIsCaseOf(s), isCaseOf, false);
                    pos = consumeWhitespaces(description, pos);
                    description = description.substring(pos);
                }
            }
        }
        field.setMultiSemanticIds(countSemanticIdMarker(description) > 1);
        // topic IDTA 02002-1-0 small/big starting chars, multi-lang descriptions in IDTA 02008-1-1
        field.setDescription(filterLanguage(removeLinebreaks(removeNote(description).trim()))); 
    }
    
    /**
     * Returns whether a value/example entry can be ignored.
     * 
     * @param data the data
     * @return {@code true} for ignore, {@code false} else
     */
    private boolean isValueExampleIgnore(String data) {
        return data.equals("n/a") 
            || data.equals("[-]") 
            || data.equalsIgnoreCase("see below"); // IDTA 02004-1-2
            //|| (data.toLowerCase().startsWith("see section") && toLines(data).length == 1); // IDTA 02017-1-0
    }
    
    /**
     * Processes the third field cell containing the value type and an optional example.
     * 
     * @param data the cell data
     * @param field the field to be modified as a side effect
     * @see #processValueTypeExampleOneLine(String, AasField)
     */
    private void processValueTypeExample(String data, AasField field) {
        // topic: n/a IDTA-02002-0-12 others empty IDTA-02011-1-0
        if (data != null && data.length() > 0 && !isValueExampleIgnore(data)) { 
            String[] tmp = toLines(data);
            if (tmp.length == 1) {
                processValueTypeExampleOneLine(tmp[0], field);
            } else if (tmp.length >= 2) {
                String type = tmp[0];
                String exampleValue = tmp[1];
                int restLinePos = 2;
                while (restLinePos < tmp.length 
                    && (exampleValue.endsWith("or:") || tmp[restLinePos].startsWith("or:"))) { // IDTA 02003-1-2
                    exampleValue += tmp[restLinePos++];
                }
                int pos = type.indexOf("]");
                if (pos + 2 < type.length()) {
                    String pre = type.substring(pos + 1);
                    String sep = " ";
                    if (pre.trim().equals("Z")) { // Z in time spec?
                        sep = "";
                    }
                    exampleValue = pre + sep + exampleValue;
                    type = type.substring(0, pos + 1);
                }
                while (restLinePos < tmp.length // IDTA-02003-1-2 
                    && (exampleValue.trim().endsWith("=") || tmp[restLinePos].trim().startsWith("="))) {
                    exampleValue += " " + tmp[restLinePos];
                    restLinePos++;
                }
                List<String> moreExampleValues = null;
                if (field.getSmeType() == AasSmeType.MULTI_LANGUAGE_PROPERTY) { // IDTA-02003-1-2
                    moreExampleValues = new ArrayList<>();
                    while (restLinePos < tmp.length && langStringEnd.matcher(tmp[restLinePos]).matches()) { 
                        moreExampleValues.add(tmp[restLinePos]);
                        restLinePos++;
                    }
                } else { // IDTA 02017-1-0
                    moreExampleValues = new ArrayList<>();
                    for (int i = restLinePos; i < tmp.length; i++) {
                        String ex = tmp[i];
                        if (ex.startsWith("|_")) {
                            ex = ex.substring(2);
                        }
                        moreExampleValues.add(ex);
                    }
                }
                if (!"[-]".equals(type)) { // IDTA 02011-1-0
                    String tpy = fixTypeName(type);
                    if (tpy.trim().length() > 0) {
                        field.setValueType(fixTypeName(type));
                    }
                    setExampleValues(exampleValue.trim(), moreExampleValues, field);
                }
                if (tmp.length > 0 && restLinePos < tmp.length) {
                    field.setExampleExplanation(String.join("\n", Arrays.copyOfRange(tmp, restLinePos, tmp.length)));
                }
            } else {
                getLogger().warn("Unknown example format: {}", data);
            }
        }
    }

    /**
     * Processes a value type/example entry on a single line.
     * 
     * @param line the line
     * @param field the target field
     */
    private void processValueTypeExampleOneLine(String line, AasField field) {
        String type = line;
        String exampleValue = null;
        boolean done = false;
        if (!type.startsWith("[")) { // IDTA 02023-0-9, conflicts with others just having the example
            int pos = type.indexOf(" ");
            String tmpExampleValue;
            String tmpType;
            if (pos > 0) {
                tmpExampleValue = removeQuotes(type.substring(pos + 1).trim());
                tmpType = type.substring(0, pos + 1).trim();
            } else {
                tmpType = type;
                tmpExampleValue = null;
            }
            if (AasField.mapPropertyType(tmpType, null) != null) { // is it a known type?
                exampleValue = tmpExampleValue;
                if (null != exampleValue && exampleValue.startsWith("[") 
                    && exampleValue.endsWith("]")) { // IDTA 02007-1-0 typo
                    exampleValue = null;
                }
                type = tmpType;
                done = true;
            }
        }
        if (!done) { // starts with [ or was not a type before
            int pos = type.indexOf("]");
            if (pos + 2 < type.length()) {
                exampleValue = type.substring(pos + 1).trim();
                type = type.substring(0, pos + 1);
            }
        }
        String vType = fixTypeName(type);
        if (null == field.getValueType() && null != vType && vType.length() > 0) { // do no override accidentally
            field.setValueType(vType);
        }
        setExampleValues(exampleValue, null, field);        
    }
    
    /**
     * Sets the example value(s).
     * 
     * @param data the data representing the example(s),may be <b>null</b>
     * @param more optional more example values, may be <b>null</b> for none
     * @param field the field to set the values on
     */
    private void setExampleValues(String data, List<String> more, AasField field) {
        data = toNullIfEmpty(data);
        if (data != null) {
            List<String> tokens = new ArrayList<>();
            int lastPos = 0;
            int pos;
            do { // IDTA 02003-1-2
                pos = data.indexOf("or:", lastPos);
                if (pos > 0) {
                    tokens.add(data.substring(lastPos, pos).trim());
                    lastPos = pos + 3; // token length
                }
            } while (pos > 0);
            tokens.add(data.substring(lastPos, data.length()).trim());
            
            boolean containsAt = tokens.stream().anyMatch(t -> t.contains("@"));
            if (AasSmeType.MULTI_LANGUAGE_PROPERTY == field.getSmeType() && containsAt) {
                splitMultiLanguageExample(tokens);
            } else {
                retokenize("[" + field.getValueType() + "]", tokens, null);
                retokenize(", ", tokens, s -> s.startsWith("\"") || s.startsWith("“"));
            }
            String[] tmp;
            if (tokens.size() == 1) { 
                tmp = toLines(data);
            } else {
                tmp = tokens.toArray(new String[tokens.size()]);
            }
            if (more != null) {
                List<String> moreTmp = new ArrayList<>();
                for (String t: tmp) {
                    moreTmp.add(t);
                }
                moreTmp.addAll(more);
                tmp = moreTmp.toArray(new String[moreTmp.size()]);
            }
            for (int i = 0; i < tmp.length; i++) {
                tmp[i] = removeQuotes(tmp[i]);
            }
            field.setExampleValues(pruneExamples(tmp));
        }
    }
    
    /**
     * Prunes examples for "see section" entries.
     * 
     * @param data the example data
     * @return the pruned data, may be <b>null</b>
     */
    private static String[] pruneExamples(String[] data) {
        String[] result = data;
        if (data != null) {
            List<String> tmp = new ArrayList<>();
            for (String d : data) {
                d = d.trim();
                Matcher matcher = seeSection.matcher(d);
                if (matcher.matches()) {
                    d = matcher.group(2).trim();
                }
                if (d != null && d.length() > 0) {
                    tmp.add(d);
                }
            }
            if (tmp.size() == 0) {
                result = null;
            } else {
                result = tmp.toArray(new String[tmp.size()]);
            }
        }
        return result;
    }
    
    /**
     * Retokenizes {@code tokens} for separator {@code sep}, if giving checking {@code condition} on 
     * follow-up strings after a new token. Currently applied only if {@code tokens} length is 1.
     * 
     * @param sep the separator to look for
     * @param tokens the tokens (to be modified as a side effect)
     * @param condition optional next-token condition, may be <b>null</b>
     */
    private void retokenize(String sep, List<String> tokens, Predicate<String> condition) {
        if (tokens.size() == 1) {
            String token = tokens.get(0);
            if (token.startsWith("e.g. ")) { // IDTA 02015-1-0
                token = token.substring(5);
            }
            int pos = token.indexOf(sep); // IDTA 02004-1-2-, separated by [type]
            if (pos > 0) {
                tokens.remove(0);
                int lastPos = 0;
                do {
                    if (null == condition || condition.test(token.substring(pos + sep.length()))) {
                        tokens.add(token.substring(lastPos, pos).trim());
                        lastPos = pos + sep.length();
                    }
                    pos = token.indexOf(sep, pos + sep.length());
                } while (pos > 0);
                tokens.add(token.substring(lastPos).trim());
            }
        }
    }

    /**
     * Tries to split the {@code tokens} into multi-language example strings. Shall be applied only if the field
     * is a multi-language property field.
     * 
     * @param tokens the tokens
     */
    private void splitMultiLanguageExample(List<String> tokens) {
        List<String> tmpToken = new ArrayList<>();
        for (int i = 0; i < tokens.size(); i++) {
            String t = tokens.get(i);
            String[] tmp = t.split(" ");
            if (tmp.length > 1) {
                int lastJ = 0;
                for (int j = 0; j < tmp.length; j++) {
                    int langPos = tmp[j].lastIndexOf("@");
                    if (langPos > 0 && langPos >= tmp[j].length() - 3) { // typical lang code length
                        tmpToken.add(String.join(" ", Arrays.copyOfRange(tmp, lastJ, j + 1)));
                        lastJ = j + 1;
                    }
                }
                if (lastJ < tmp.length) {
                    tmpToken.add(String.join(" ", Arrays.copyOfRange(tmp, lastJ, tmp.length)));
                }
            } else {
                tmpToken.add(t);
            }
        }
        tokens.clear();
        tokens.addAll(tmpToken);        
    }
    
    /**
     * Processes the fourth field cell, potentially containing the field cardinality.
     *  
     * @param data the cell data
     * @param field the field to be modified as side effect
     */
    private void processFieldCardinality(String data, AasField field) {
        data = removeBrackets(data); // needed in IDTA 02002-1-0 but not in IDTA 02003-1-2
        if (data != null) {
            data = data.replace("" + (char) 8230, ".."); // 02022
            if ("*".equals(data)) { // 02016
                data = "0..*";
            }
            int pos = data.indexOf("..");
            if (pos < 0) {
                field.setCardinality(toCardinality(data));
            } else {
                field.setCardinality(toCardinality(data.substring(0, pos)), 
                    toCardinality(data.substring(pos + 1 + 1)));
            }
        }
    }
    
    /**
     * Interprets {@code data} as a single cardinality value. 
     * 
     * @param data the data to be interpreted
     * @return the cardinality value, -1 for "*", or the minimum integer value for none
     */
    private int toCardinality(String data) {
        int result = Integer.MIN_VALUE;
        data = data.trim();
        if (!data.equals("n/a")) {
            if (data.startsWith("1 or ")) { // IDTA 02017-1-0
                data = data.substring(5);
            }
            if (data.equals("*") || data.equals("n")) { // IDTA 2008-1-1
                result = -1;
            } else {
                try {
                    result = Integer.parseInt(data);
                } catch (NumberFormatException e) {
                    getLogger().warn("Reading cardinality '{}': {}", data, e.getMessage());
                }
            }
        }
        return result;
    }

    /**
     * Stores {@code newCurrent} as the current type being processed. If there was a current type before, add that
     * to the result, i.e., the list of detected types.
     * 
     * @param newCurrent the new current type(s), may be <b>null</b> for none
     */
    private void storeAsCurrent(List<AasType> newCurrent) {
        current.removeIf(t -> null != t.getSmeType() && !t.getSmeType().isType()); // IDTA 02026-1-0
        aasTypes.addAll(current);
        current.clear();
        if (null != newCurrent) {
            current.addAll(newCurrent);
        }
    }

    /**
     * Collects and maps the plain non-aspect types into {@code aasTypes}.
     * 
     * @param aasTypes the types to map
     * @return the idShort-type-mapping
     */
    private Map<String, AasType> mapPlainTypes(List<AasType> aasTypes) {
        Map<String, AasType> typeMap = new HashMap<>();
        for (AasType t: aasTypes) {
            if (t.getIdShort() != null && !t.isAspect()) { // IDTA 02017-1-0
                typeMap.put(t.getIdShort(), t);
            }
        }
        return typeMap;
    }
    
    /**
     * Notified when reading of the specification is completed.
     * 
     * @see #storeAsCurrent(List)
     */
    void readingCompleted() {
        storeAsCurrent(null);

        Set<AasType> delete = new HashSet<>();
        Map<String, AasType> typeMap = mapPlainTypes(aasTypes);
        for (AasType t: aasTypes) {
            AasType transferTo = null;
            String aspect = null;
            if (t.isAspect()) { // IDTA 02017-1-0
                // transfer type properties
                transferTo = typeMap.get(t.getIdShort());
                if (null != transferTo) {
                    getLogger().info("Applying extension for {}.", t.getIdShort());
                    if (transferTo.getSemanticId() == null) {
                        String semId = t.getMappedSemanticId(t.getIdShort());
                        if (null == semId) {
                            semId = t.getSemanticId(); // fallback
                        }
                        transferTo.setSemanticId(semId);
                    }
                    if (t.getDescription() != null) {
                        transferTo.setDescription(t.getDescription());
                    }
                } else {
                    getLogger().info("Cannot apply extension for {}. Not found. Skipping.", t.getIdShort());
                }
                delete.add(t);
            } else if (t.getSmeType() == null && t.getIdShort().equals("-")) { // IDTA 02017-1-0
                String desc = t.getDescription();
                Matcher matcher = extensionDesc.matcher(desc);
                if (matcher.matches()) {
                    String targetIdShort = matcher.group(1);
                    transferTo = typeMap.get(targetIdShort);
                    if (null != transferTo) {
                        aspect = removeSuffix(matcher.group(2), "-specific");
                        getLogger().info("Applying extension {} to {}.", aspect, targetIdShort);
                    } else {
                        getLogger().error("Cannot apply extension {} for {} as no such type found. Skipping.", 
                            aspect, targetIdShort);
                    }
                } else {
                    getLogger().error("Cannot identify extension conditions for {}. Skipping.", t.getIdShort());
                }
                delete.add(t);
            }
            if (transferTo != null) {
                for (AasField f : t.fields()) {
                    AasField fn = new AasField(f);
                    fn.setAspect(aspect);
                    transferTo.addField(fn);
                }
                for (AasOperation o : t.operations()) {
                    AasOperation on = new AasOperation(o);
                    on.setAspect(aspect);
                    transferTo.addOperation(on);
                }
            }
        }
        aasTypes.removeAll(delete);
        typeMap.clear();
        
        for (AasType t : aasTypes) {
            typeMap.put(t.getIdShort(), t);
        }
        for (Map.Entry<String, String> ent : deferredTypes.entrySet()) { // IDTA 02021-1-0
            if (!typeMap.containsKey(ent.getKey())) {
                AasType prototype = typeMap.get(ent.getValue());
                int pos = aasTypes.indexOf(prototype);
                if (null != prototype) {
                    AasType t = new AasType(prototype);
                    t.setIdShort(ent.getKey());
                    aasTypes.add(pos + 1, t);
                } else {
                    getLogger().error("Unresolved type '{}' points to type '{}' which does not exist", 
                        ent.getKey(), ent.getValue());
                }
            }
        }
    }
    
    /**
     * Returns the summary result.
     * 
     * @return the summary
     */
    AasSpecSummary getSummary() {
        AasSpecSummary result = new AasSpecSummary(aasTypes, aasEnums);
        // use simplified project name -> null
        result.setIdentifier(null, versionIdentifier, version, versionedName, specNumber);
        return result;
    }

    /**
     * Returns the logger of this class.
     * 
     * @return the logger
     */
    private static Logger getLogger() {
        return LoggerFactory.getLogger(ParsingUtils.class);
    }

}