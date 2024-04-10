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
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;

import de.iip_ecosphere.platform.configuration.FallbackLogger;
import de.iip_ecosphere.platform.support.aas.IdentifierType;

/**
 * Utilities for text parsing.
 * 
 * @author Holger Eichelberger, SSE
 */
class ParsingUtils {

    public static final String IRI_MARKER_TEXT = "IRI";
    public static final String IRDI_MARKER_TEXT = "IRDI";
    public static final String[] SEMANTICID_MARKER = {toSemanticIdMarker(IRI_MARKER_TEXT), 
        toSemanticIdMarker(IRDI_MARKER_TEXT)};
    public static final String[] SEMANTICID_MARKER_WITH_PATH = {SEMANTICID_MARKER[0], SEMANTICID_MARKER[1], 
        "[IRDI PATH]", "[IRDI Path]"};

    private static FallbackLogger.LoggingLevel loggingLevel = FallbackLogger.LoggingLevel.INFO;
    private static Logger logger;
    private static final Pattern BRACKETS_WIITH_FOOTNOTE = Pattern.compile("^\\[([^\\]]+)\\]\\W*\\d*$");
    private static final Pattern ENUM_LITERAL_PATTERN = Pattern.compile("(and\\W+)?\\d+\\.\\W*\"([^\"]+)\"(\\.)?");

    /**
     * Returns the actual logging level for new loggers.
     * 
     * @return the logging level
     */
    static FallbackLogger.LoggingLevel getLoggingLevel() {
        return loggingLevel;
    }
    
    /**
     * Changes the actual logging level for new loggers.
     * 
     * @param level the new level, ignored if <b>null</b>
     */
    static void setLoggingLevel(FallbackLogger.LoggingLevel level) {
        if (null != level) {
            loggingLevel = level;
        }
    }
    
    /**
     * Turns {@code name} into a valid Java/IVML identifier.
     * 
     * @param name the name to be processed
     * @return the identifier based on {@code name}
     */
    static String toIdentifier(String name) {
        StringBuilder builder = new StringBuilder(name.trim());
        for (int i = 0; i < builder.length(); i++) {
            char c = builder.charAt(i);
            if (!Character.isJavaIdentifierPart(c)) {
                builder.setCharAt(i, '_');
            }
        }
        return builder.toString();
    }
    
    /**
     * Removes linebreaks from data replacing them by a single whitespace.
     * 
     * @param data the data
     * @return the modified version of {@code data}
     */
    static String removeLinebreaks(String data) {
        return data.replace("\r\n", " ").replace("\r", " ").replace("\n", " ");
    }

    /**
     * Replaces whitespaces in data.
     * 
     * @param data the data (may be <b>null</b>)
     * @param replacement the replacement for whitespaces
     * @return the modified version of {@code data}
     */
    static String replaceWhitespace(String data, String replacement) {
        String result = data;
        if (data != null) {
            result = data.replace("\n", replacement)
                .replace("\r", replacement)
                .replace(" ", replacement);
        }
        return result;
    }
    
    /**
     * Removes whitespaces from data.
     * 
     * @param data the data
     * @return the modified version of {@code data}
     */
    static String removeWhitespace(String data) {
        return replaceWhitespace(data, "");
    }
    
    /**
     * Consumes whitespaces starting at {@code pos}.
     * 
     * @param data the text to iterate over
     * @param pos the position to start at
     * @return the next position in {@code data} that is not a whitespace, may be after string length
     */
    static int consumeWhitespaces(String data, int pos) {
        while (pos < data.length() && Character.isWhitespace(data.charAt(pos))) {
            pos++;
        }
        return pos;
    }

    /**
     * Consumes not-whitespaces starting at {@code pos}.
     * 
     * @param data the text to iterate over
     * @param pos the position to start at
     * @return the next position in {@code data} that is a whitespace, may be after string length
     */
    static int consumeNonWhitespaces(String data, int pos) {
        while (pos < data.length() && !Character.isWhitespace(data.charAt(pos))) {
            pos++;
        }
        return pos;
    }
    
    /**
     * Turns {@code data} to <b>null</b> if {@code data} is empty.
     *  
     * @param data the string
     * @return data or <b>null</b>
     */
    static String toNullIfEmpty(String data) {
        String result = data;
        if (null != data && data.trim().length() == 0) {
            result = null;
        }
        return result;
    }

    /**
     * Splits a string into an array of individual lines.
     * 
     * @param data the string to be split (may be <b>null</b>)
     * @return the lines or <b>null</b> if {@code data} was <b>null</b>
     */
    static String[] toLines(String data) {
        return null == data ? null : data.split("\\R");            
    }
    
    /**
     * Removes surrounding brackets from data.
     * 
     * @param data the data to be processed
     * @return {@code data} or {@code data} with surrounding brackets, then whitespaces removed
     * @see #removeWhitespace(String) 
     */
    static String removeBrackets(String data) {
        String result = data;
        if (data != null) {
            Matcher matcher = BRACKETS_WIITH_FOOTNOTE.matcher(data);
            if (matcher.matches()) {
                result = ParsingUtils.removeWhitespace(matcher.group(1));
            }
        }
        return result;
    }
    
    /**
     * Checks and fixes a type name. In some specs, there is additional information or typos such as
     * cardinalities. Is some specs, the type name is in brackets. This information is removed.
     * 
     * @param name the type name
     * @return {@code name} or a shortened version
     * @see #removeBrackets(String)
     */
    static String fixTypeName(String name) {
        String result = removeBrackets(name); // IDTA 2002-1-0, IDTA 2003-1-2 not in IDTA 02007-0-1
        int pos = name.indexOf("["); // IDTA 02007-0-1
        if (pos > 0) {
            result = name.substring(0, pos).trim();
        }
        return result;
    }

    /**
     * Returns the last "Note :" comment in {@code text}.
     * 
     * @param text the text to take the comment from
     * @return the comment including "Note: ", <b>null</b> if there is none
     */
    static String getLastNoteComment(String text) {
        String result = null;
        if (text != null) {
            int pos = text.lastIndexOf("Note: ");
            result = text.substring(pos);
        }
        return result;
    }

    /**
     * Analyzes an AAS type idShort comment for a note.
     * 
     * @param noteComment the note comment
     * @return {@code true} for fixed idShort, {@code false} for free idShort
     */
    static boolean hasFixedIdShort(String noteComment) {
        boolean fixedIdShort = false;
        if (null != noteComment) {
            String tmp = noteComment.toLowerCase();
            if (tmp.equals("note: the above idshort shall always be as stated.")) {
                fixedIdShort = true;
            } else if (tmp.equals("note: the idshort can be chosen freely.") 
                || tmp.startsWith("note: a different idshort might be used")) { // IDTA-02008-1-1
                fixedIdShort = false;
            } else {
                getLogger().warn("unexpected AasType idShort comment: {}", noteComment);
            }
        }
        return fixedIdShort;
    }
    
    /**
     * Encapsulation of enumeration storage structure to enable notifications for multi-row enumeration
     * specifications as in IDTA-02023-0-9.
     * 
     * @author Holger Eichelberger, SSE
     */
    static class AasEnumResultHandler {
        
        private List<AasEnum> enums;
        private Consumer<AasEnum> notifier;

        /**
         * Creates a result handler instance for the given {@code enums} without notifier.
         * 
         * @param enums the collection of known enumerations
         */
        AasEnumResultHandler(List<AasEnum> enums) {
            this(enums, null);
        }

        /**
         * Creates a result handler instance for the given {@code enums} witht notifier.
         * 
         * @param enums the collection of known enumerations
         * @param notifier the notifier, may be <b>null</b> for none
         */
        AasEnumResultHandler(List<AasEnum> enums, Consumer<AasEnum> notifier) {
            this.enums = enums;
            this.notifier = notifier;
        }
        
        /**
         * Adds {@code en} as new enumeration. Notifies {@link #notifier} if specified.
         *  
         * @param en the new enumeration
         */
        public void add(AasEnum en) {
            enums.add(en);
            if (null != notifier) {
                notifier.accept(en);
            }
        }
        
        /**
         * Returns whether there is already an enum with the given {@code idShort}/name.
         * 
         * @param idShort the idShort/name to look for
         * @return {@code true} if there is an enum, {@code false} else
         */
        public boolean hasEnum(String idShort) {
            return enums.stream().anyMatch(e -> e.getIdShort().equals(idShort));
        }
        
    }

    /**
     * Tries to infer an enumeration description from the given {@code descriptionRest} based on the defined 
     * {@link ParsingEnumKind enum kinds}.
     * 
     * @param data the actual part containing the enumeration text
     * @param description the actual description text
     * @param field the target field (to be modified as a side effect)
     * @param aasEnums the known enums, to be modified as a side effect
     * @param atBeginning whether the enum marker may be at the very beginning of {@code data} or not
     * @return {@code description} or an updated/modified text
     */
    static String inferEnum(String data, String description, AasField field, AasEnumResultHandler aasEnums, 
        boolean atBeginning) {
        String result = description;
        boolean isOpen =
            description.matches(".*declared as.*open.*for further addition.*") // IDTA 02002-1-0 
            || description.matches(".*usage of values that are not given.*"); // IDTA 02023-0-9
        for (ParsingEnumKind kind: ParsingEnumKind.values()) {
            String[] match = kind.getMatch(data, atBeginning);
            if (null != match) {
                result = match[0];
                data = match[1];
                field.setValueType(inferEnum(kind, data, field.getIdShort(), 
                    field.getSemanticId(), result, aasEnums, isOpen));
            }
            /*for (String marker: kind.getMarker()) {
                int pos = data.indexOf(marker);
                if ((atBeginning && pos >= 0) || (!atBeginning && pos > 0)) {
                    result = data.substring(0, pos).trim();
                    data = data.substring(pos + marker.length());
                    field.setValueType(inferEnum(kind, data, field.getIdShort(), 
                        field.getSemanticId(), result, aasEnums));
                    break;
                }
            }*/
        }
        return result;
    }
    
    // checkstyle: stop parameter number check
    
    /**
     * Tries to infer an enumeration description from the given {@code descriptionRest}.
     * 
     * @param kind the enumeration kind
     * @param descriptionRest the description rest that potentially contains a flattened, comma-separated description
     *    of enum literals
     * @param idShort the idShort of the containing field
     * @param semanticId optional semantic id of the enum
     * @param description the description of the enum
     * @param aasEnums the known enums, to be modified as a side effect
     * @param isOpen whether the enum is considered to be "open" for extensions
     * @return the type name of the enum (for now, just {@code idShort})
     */
    static String inferEnum(ParsingEnumKind kind, String descriptionRest, String idShort, String semanticId, 
        String description, AasEnumResultHandler aasEnums, boolean isOpen) {
        String enumName = idShort;
        if (!aasEnums.hasEnum(enumName)) {
            AasEnum en = new AasEnum(enumName);
            en.setSemanticId(semanticId);
            en.setIsOpen(isOpen);
            en.setDescription(filterLanguage(description));
            en.setParsingEnumKind(kind); // temporary info
            Enumeration<Object> tokenizer = tokenizeEnumSpec(descriptionRest, kind);
            while (tokenizer.hasMoreElements()) { // topic: no defined/clear structure
                String token = tokenizer.nextElement().toString();
                switch (kind) {
                case ENUM:
                    inferEnumLiteralFromToken(token, en);
                    break;
                case ENUM_ENTRIES:
                    inferEnumEntriesLiteralFromToken(token, en);
                    break;
                case VALUE_LIST2:
                    inferValueList2EntriesLiteralFromToken(token, en);
                    break;
                case IRDIS: 
                    inferEnumIrdiLiteralsFromToken(token, en); // IDTA-02004-1-2.
                    break;
                default:
                    break;
                }
            }
            aasEnums.add(en);
        }
        return enumName;
    }

    // checkstyle: resume parameter number check

    /**
     * Tokenizes an enum specification. Prevents token splitting within parentheses.
     * 
     * @param text the text to tokenize
     * @param enumKind the kind of enum to tokenize
     * @return the token enumeration (compliant to {@link StringTokenizer}
     */
    private static Enumeration<Object> tokenizeEnumSpec(String text, ParsingEnumKind enumKind) {
        // new StringTokenizer
        Vector<Object> tokens = new Vector<>(); 
        String delim = ",";
        if (ParsingEnumKind.VALUE_LIST2 == enumKind) {
            tokenizeEnumSpecValueList2(text, tokens);
        } else {
            int pos = 0;
            int inValueSpec = 0;
            int lastPos = 0;
            while (pos < text.length()) {
                char c = text.charAt(pos);
                if ('(' == c) {
                    inValueSpec++;
                } else if (')' == c) {
                    inValueSpec--;
                } else if (0 == inValueSpec) {
                    if (text.indexOf(delim, pos) == pos) {
                        String tok = text.substring(lastPos, pos).trim();
                        if (tok.length() > 0) {
                            tokens.add(tok);
                        }
                        pos = pos + delim.length();
                        lastPos = pos;
                    }
                }
                pos++;
            }
            String tok = text.substring(lastPos, pos).trim();
            if (tok.length() > 0) {
                tokens.add(tok);
            }
        }
        return tokens.elements();
    }

    /**
     * Tokenizes an enum specification according to {@link ParsingEnumKind#VALUE_LIST2}.
     * 
     * @param text the text to tokenize
     * @param tokens the tokens (compliant to {@link StringTokenizer}
     */
    private static void tokenizeEnumSpecValueList2(String text, Vector<Object> tokens) {
        int pos = text.indexOf((char) 61623); // bullet char
        if (pos > 0) {
            StringTokenizer tk = new StringTokenizer(text, "" + (char) 61623);
            while (tk.hasMoreTokens()) {
                String t = tk.nextToken().trim();
                if (t.length() > 0) {
                    tokens.add(t + "|");
                }
            }
        } else {
            pos = 0;
            do {
                pos = text.indexOf("[", 0);
                if (pos > 0) {
                    int pos2 = text.indexOf("]", pos);
                    if (pos2 > 0) {
                        String idShort = text.substring(0, pos).trim();
                        String semId = SemanticIdRecognizer.getSemanticIdFrom(text.substring(pos2 + 1).trim(), true);
                        if (null != semId) {
                            pos = text.indexOf(" ", pos + semId.length() + 1);
                            tokens.add(semId + "|" + idShort);
                            text = (pos > 0) ? text.substring(pos) : text; 
                        } else {
                            pos = -1; // stop
                        }
                    } else {
                        pos = -1; // stop
                    }
                }
            } while (pos > 0);
        }
    }

    /**
     * Infers enumeration literals from a token according to {@link ParsingEnumKind#ENUM}.
     * 
     * @param token the token
     * @param en the enumeration to add the literals to
     */
    private static void inferEnumLiteralFromToken(String token, AasEnum en) {
        int brStartPos = token.indexOf("(");
        int brEndPos = token.indexOf(")");
        if (brStartPos > 0 && brStartPos < brEndPos) {
            String beforePar = token.substring(0, brStartPos);
            String beforeParPrefix = getSemanticIdPrefix(beforePar);
            String inPar = token.substring(brStartPos + 1, brEndPos);
            String[] inParParts = inPar.split(",");
            String[] inParPrefix = new String[inParParts.length];
            for (int i = 0; i < inParParts.length; i++) {
                inParParts[i] = inParParts[i].trim();
                inParPrefix[i] = getSemanticIdPrefix(inParParts[i]);
            }
            //String afterPart = token.substring(brEndPos);
            if (null != beforeParPrefix) { // TODO IDTA ??
                beforePar = removeWhitespace(beforePar);
                String desc = token.substring(brStartPos + 1, brEndPos);
                String name = toLiteralName(desc);
                String identifier = null;
                int idPos = name.indexOf(" - ");
                if (idPos > 0) {
                    identifier = toIdentifier(name.substring(0, idPos));
                }
                AasEnumLiteral lit = new AasEnumLiteral(name, 
                    IdentifierType.compose(beforeParPrefix, beforePar), desc, identifier);
                //lit.setValue(value);
                en.addLiteral(lit);
            } else {
                if (inParParts.length == 2 && inParPrefix[1] != null) { // IDTA 02013-1-0
                    AasEnumLiteral lit = new AasEnumLiteral(beforePar, 
                        IdentifierType.compose(inParPrefix[1].trim(), inParParts[1]), "", null);
                    lit.setValue(inParParts[0]);
                    en.addLiteral(lit);
                } else if (inParParts.length == 1 && inParPrefix[0] != null) { // IDTA 02008-1-1
                    AasEnumLiteral lit = new AasEnumLiteral(beforePar, 
                        IdentifierType.compose(inParPrefix[0].trim(), inParParts[0]), "", null);
                    en.addLiteral(lit);
                } else {
                    getLogger().warn("Unknown enum literal structure: {}", token);
                }
            }
        } else {
            getLogger().warn("Unknown enum literal structure: {}", token);
        }
    }

    /**
     * Infers value list (2) entries. IDTA-02010-1-0
     * 
     * @param token the token
     * @param en the enumeration to be modified as a side effect
     */
    private static void inferValueList2EntriesLiteralFromToken(String token, AasEnum en) {
        int pos = token.indexOf("|"); // pseudo separator added by tokenization
        if (pos > 0) {
            String semId = token.substring(0, pos);
            String name = token.substring(pos + 1);
            String id = null;
            int spacePos = name.indexOf("  ");
            if (spacePos > 0) {
                id = name.substring(0, spacePos).trim();
                name = name.substring(spacePos + 2).trim();
            }
            en.addLiteral(new AasEnumLiteral(name, semId, null, id));
        }
    }


    /**
     * Infers an enumeration literal based on IRDI values. IDTA-02004-1-2.
     * 
     * @param token the token containing the literal(s)
     * @param en the enum to complement with literals
     */
    private static void inferEnumIrdiLiteralsFromToken(String token, AasEnum en) {
        token = token.replace("–", "-"); // simplify, unify
        int pos;
        do {
            pos = token.indexOf("-");
            if (pos > 0) {
                String idShort = token.substring(0, pos).trim();
                token = token.substring(pos + 1).trim();
                String semId = SemanticIdRecognizer.getSemanticIdFrom(token, false);
                if (null != semId) {
                    token = token.substring(semId.length());
                    en.addLiteral(new AasEnumLiteral(idShort, semId, null, null));
                }
            }
        } while (pos > 0);
    }
    
    /**
     * Infers the semanticId prefix of {@code text}.
     * 
     * @param text the text to use as basis
     * @return the semanticId prefix as in {@link IdentifierType}, may be <b>null</b> for none
     */
    private static String getSemanticIdPrefix(String text) {
        //String result = null;
        //text = removeWhitespace(text);
        return SemanticIdRecognizer.getIdentifierPrefix(removeWhitespace(text));
        /*if (irdiPattern.matcher(text).matches() || irdiPattern2.matcher(text).matches()) {
            result = IdentifierType.IRDI_PREFIX;
        } else if (iriPattern.matcher(text).matches()) {
            result = IdentifierType.IRI_PREFIX;
        }
        return result;*/
    }

    /**
     * Infers enumeration literals from a token according to {@link ParsingEnumKind#ENUM_ENTRIES}.
     * 
     * @param token the token
     * @param en the enumeration to add the literals to
     */
    private static void inferEnumEntriesLiteralFromToken(String token, AasEnum en) {
        String lastToken = null;
        token = token.replace("“", "\"").replace("”", "\"").trim();
        int pos = token.indexOf("\" and");
        if (pos > 0) {
            lastToken = token.substring(pos + 1);
            token = token.substring(0, pos + 1).trim();
            parseEnumEntriesLiteralFromToken(token, en);
            pos = lastToken.indexOf("\".");
            if (pos > 0) {
                lastToken = lastToken.substring(0, pos + 2).trim();
            }
            parseEnumEntriesLiteralFromToken(lastToken, en);
        } else {
            parseEnumEntriesLiteralFromToken(token, en);
        }
    }
    
    /**
     * Removes beautified paired quotes.
     * 
     * @param value the value to be processed
     * @return the processed value
     */
    static String removeQuotes(String value) {
        int pos1 = value.indexOf("“", 1);
        int pos2 = value.lastIndexOf("”", value.length() - 2);
        if (value.startsWith("“") && value.endsWith("”") && pos1 < 0 && pos2 < 0) {
            value = value.substring(1, value.length() - 1);
        }
        if (value.length() > 0 && value.charAt(0) == (char) 61623) { // the "bullet" IDTA 02017-1-0
            value = value.substring(1).trim();
        }
        return value;
    }

    /**
     * Parses enum literals via {@link #ENUM_LITERAL_PATTERN} a given token.
     * 
     * @param token the token
     * @param en the enumeration to add the literal to
     */
    private static void parseEnumEntriesLiteralFromToken(String token, AasEnum en) {
        Matcher matcher = ENUM_LITERAL_PATTERN.matcher(token);
        if (matcher.matches()) {
            AasEnumLiteral lit = new AasEnumLiteral(matcher.group(2), "", "", null);
            en.addLiteral(lit);
        }
    }
    
    /**
     * Tries to infer an enum field name from {@code description}.
     * 
     * @param description the description
     * @return the field name or {@code description}
     */
    private static String toLiteralName(String description) {
        String result = description;
        int count = 3;
        int nameCutPos = description.indexOf(" ");
        while (nameCutPos > 0 && count >= 0) {
            int nextCutPos = description.indexOf(" ", nameCutPos + 1);
            if (nextCutPos > 0) {
                nameCutPos = nextCutPos; 
                count--;
            } else {
                nameCutPos = -1; // take the full string
                break;
            }
        }
        if (nameCutPos > 0) {
            result = description.substring(0, nameCutPos);
        }
        return result.trim();
    }
    
    /**
     * Filter English or any other language from {@code description} if given in multi languages, prefer
     * "definition @" if given.
     * 
     * @param description the description
     * @return the last language-specific description
     */
    static String filterLanguage(String description) {
        String result = description;
        // IDTA-02008-1-1, both forms
        if (description.contains("definition @") || description.contains("Definition @")) { 
            String tmp = description
                .replace("preferredName @", "") // hide undesirable
                .replace("definition @", "@")
                .replace("Definition @", "@");  // mask desirable
            result = filterLanguageImpl(tmp);
        }
        if (result.equals(description)) {
            result = filterLanguageImpl(description);
        }
        return result;
    }

    /**
     * Filter English or any other language from {@code description} if given in multi languages. 
     * 
     * @param description the description
     * @return the last language-specific description
     */
    private static String filterLanguageImpl(String description) {
        String result = description;
        String[] tmp = description.split("@");
        if (tmp.length > 1) {
            String en = null;
            String any = null;
            for (int i = 0; i < tmp.length; i++) {
                if (tmp[i].length() > 0) {
                    int pos = tmp[i].indexOf(" ");
                    if (pos >= 0 && pos <= 3) {
                        String lang = tmp[i].substring(0, pos);
                        if (lang.endsWith(":")) {
                            lang = lang.substring(0, lang.length() - 1);
                        }
                        String desc = tmp[i].substring(pos + 1).trim();
                        if ("en".equals(lang)) {
                            en = desc;
                        } else {
                            any = desc;
                        }
                    }
                }
            }
            if (en != null) {
                result = en;
            } else if (any != null) {
                result = any;
            }
        }
        return result;
    }

    /**
     * Whether a field shall be ignored.
     * 
     * @param idShort the idShort to test (may be <b>null</b>)
     * @return {@code true} for ignore, {@code false} else
     */
    static boolean isGenericIdShort(String idShort) {
        boolean known = false;
        if (idShort != null) {
            known = "{arbitrary}".equals(idShort) // IDTA-02003-1-2, we leave that to the user 
                || "{Variable}".equals(idShort)
                || idShort.startsWith("{Local"); // IDTA-02012-1-0 
            known |= (idShort.startsWith("{") && idShort.endsWith("}"));  // IDTA-02017-1-0
            known |= removeWhitespace(idShort).equalsIgnoreCase("<noidshort>"); // IDTA-02017-1-0
        }
        return known;
    }
    
    /**
     * Turns {@code value} into a semanticID marker.
     * 
     * @param value the value
     * @return the marker
     */
    private static String toSemanticIdMarker(String value) {
        return "[" + value + "]";
    }

    /**
     * Returns the sequence of semantic ID markers in {@code value}.
     * 
     * @param value the value
     * @return the number of semantic IDs
     */
    static String[] getSemanticIdMarkers(String value) {
        List<String> result = new ArrayList<>();
        for (String m : SEMANTICID_MARKER_WITH_PATH) {
            int pos = -(m.length() + 1);
            do {
                pos = value.indexOf(m, pos + m.length() + 1);
                if (pos >= 0) {
                    result.add(m);
                }
            } while (pos >= 0);
        }
        return result.toArray(new String[result.size()]);
    }
    
    /**
     * Returns the number of semantic ID markers in {@code value}.
     * 
     * @param value the value
     * @return the number of semantic IDs
     */
    static int countSemanticIdMarker(String value) {
        return getSemanticIdMarkers(value).length;
    }
    
    /**
     * Returns whether {@code string} starts with one of the markers in {@link #SEMANTICID_MARKER}.
     * 
     * @param value the value to check
     * @return {@code true} if a semanticId marker was found, {@code false} else
     */
    static boolean hasSemanticIdMarker(String value) {
        boolean result = false;
        for (String m : SEMANTICID_MARKER) {
            if (value.startsWith(m)) {
                result = true;
                break;
            }
        }
        return result;
    }
    
    /**
     * If {@code text} has {@code suffix}, return {@code text} without {@code suffix}.
     * 
     * @param text the text
     * @param suffix the suffix
     * @return {@code text} or {@code text} without {@code suffix}
     */
    static String removeSuffix(String text, String suffix) {
        String result = text;
        if (text.endsWith(suffix)) {
            result = text.substring(0, text.length() - suffix.length());
        }
        return result;
    }

    /**
     * If {@code text} has {@code prefix}, return {@code text} without {@code prefix}.
     * 
     * @param text the text
     * @param prefix the prefix
     * @return {@code text} or {@code text} without {@code prefix}
     */
    static String removePrefix(String text, String prefix) {
        String result = text;
        if (text.startsWith(prefix)) {
            result = text.substring(prefix.length());
        }
        return result;
    }

    /**
     * Returns whether {@code data} contains {@code item} through equals.
     * 
     * @param data the data to search
     * @param item the item to look for
     * @return {@code true} if contained, {@code false} else
     */
    static boolean contains(String[] data, String item) {
        boolean found = false;
        for (int d = 0; !found && d < data.length; d++) {
            found = data[d].equals(item);
        }
        return found;
    }

    
    /**
     * Returns whether {@val} is an value to be emitted.
     * 
     * @param val the value
     * @return {@code true} for emitting, {@code false} for skipping
     */
    static boolean isValue(String val) {
        return null != val && val.length() > 0;
    }

    /**
     * Returns whether {@val} is an value to be emitted.
     * 
     * @param val the value
     * @return {@code true} for emitting, {@code false} for skipping
     */
    static boolean isValue(Object[] val) {
        return null != val && val.length > 0;
    }
    
    /**
     * Strips an IVML refBy declaration and returns the contained type.
     * 
     * @param type the type
     * @return {@code type} or {@code type} without refBy
     */
    static String stripRefBy(String type) {
        String result = type;
        if (type.startsWith("refBy(") && type.endsWith(")")) {
            result = type.substring(6, type.length() - 1);
        }
        return result;
    }
    
    /**
     * Returns the logger of this class.
     * 
     * @return the logger
     */
    private static Logger getLogger() {
        logger = FallbackLogger.getLogger(logger, RowProcessor.class, getLoggingLevel());
        return logger;
    }
    
}
