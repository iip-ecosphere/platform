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
import java.util.List;
import java.util.regex.Pattern;

import de.iip_ecosphere.platform.support.aas.IdentifierType;

/**
 * SemanticId parser/recognizer with default instances.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class SemanticIdRecognizer {

    public static final SemanticIdRecognizer ECLASS_IRDI_RECOGNIZER = new EclassIrdiRecognizer(); 
    public static final SemanticIdRecognizer IEC_CCD_IRDI_RECOGNIZER = new IecCcdIrdiRecognizer();
    public static final SemanticIdRecognizer IDTA_IRI_RECOGNIZER = new IdtaIriRecognizer();
    public static final SemanticIdRecognizer URL_IRI_RECOGNIZER = new UrlIriRecognizer();

    private static final List<SemanticIdRecognizer> INSTANCES = new ArrayList<>();

    static {
        register(ECLASS_IRDI_RECOGNIZER);
        register(IEC_CCD_IRDI_RECOGNIZER);
        register(IDTA_IRI_RECOGNIZER);
        register(URL_IRI_RECOGNIZER);
    }
    
    /**
     * Returns whether this recognizer shall be used as fallback.
     * 
     * @return {@code true} for fallback, {@code false} for primary
     */
    public abstract boolean isFallback();
    
    /**
     * Returns whether this recognizer will handle the given {@code value}.
     * 
     * @param value the value
     * @return the recognizer
     */
    public abstract boolean handles(String value);
    
    /**
     * Returns the identifier prefix from {@link IdentifierType}.
     * 
     * @return the identifier prefix
     */
    public abstract String getIdentifierPrefix();
    
    /**
     * Parses a semantic id from {@code value}.
     * 
     * @param value the value
     * @return the contained semantic id (usually at the beginning) or <b>null</b> for none
     */
    public abstract String parseSemanticId(String value);
    
    /**
     * Returns the last value from a path of semantic ids.
     * 
     * @param value the value
     * @return the last, {@code value} if there is none
     */
    public String lastOfPath(String value) {
        return value;
    }
    
    /**
     * Returns whether {@code value} is a semantic id.
     * 
     * @param value the value
     * @return the semantic id
     */
    public abstract boolean isASemanticId(String value);
    

    /**
     * Registers a recognizer instance.
     * 
     * @param recognizer the recognizer, ignored if <b>null</b>
     * @see #isFallback()
     */
    public static void register(SemanticIdRecognizer recognizer) {
        if (null != recognizer) {
            if (recognizer.isFallback()) {
                INSTANCES.add(recognizer);
            } else {
                INSTANCES.add(0, recognizer);
            }
        }
    }
    
    /**
     * Returns the identifier prefix of the first handling/registered/responding recognizer.
     * 
     * @param value the value
     * @return the prefix, <b>null</b> for none
     * @see #handles(String)
     * @see #getIdentifierPrefix()
     */
    public static String getIdentifierPrefix(String value) {
        String result = null;
        if (null != value) {
            for (SemanticIdRecognizer r : INSTANCES) {
                if (r.handles(value)) {
                    result = r.getIdentifierPrefix();
                    if (null != result) {
                        break;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Returns the identifier prefix of the first handling/registered/responding recognizer.
     * 
     * @param value the value
     * @param addPrefix whether the platform prefix from {@link #getIdentifierPrefix()} shall be prefixed
     * @return the prefix, <b>null</b> for none
     * @see #getSemanticId(String)
     * @see #handles(String)
     */
    public static String getSemanticIdFrom(String value, boolean addPrefix) {
        return getSemanticIdFrom(value, addPrefix, false);
    }
    
    /**
     * Returns the identifier prefix of the first handling/registered/responding recognizer.
     * 
     * @param value the value
     * @param addPrefix whether the platform prefix from {@link #getIdentifierPrefix()} shall be prefixed
     * @param fromPath if {@code value} is a path, consider the relevant semantic id from the path
     * @return the prefix, <b>null</b> for none
     * @see #getSemanticId(String)
     * @see #handles(String)
     */
    public static String getSemanticIdFrom(String value, boolean addPrefix, boolean fromPath) {
        String result = null;
        if (null != value) {
            for (SemanticIdRecognizer r : INSTANCES) {
                if (r.handles(value)) {
                    if (fromPath) {
                        result = r.parseSemanticId(r.lastOfPath(value));
                    } else {
                        result = r.parseSemanticId(value);
                    }
                    if (null != result) {
                        if (addPrefix) {
                            result = IdentifierType.compose(r.getIdentifierPrefix(), result);
                        }
                        break;
                    }
                }
            }
        }
        return result;
    }
    
    /**
     * Returns whether {@code value} is a semantic id based on the first handling/registered/responding recognizer.
     * 
     * @param value the value
     * @return the prefix, <b>null</b> for none
     * @see #getSemanticId(String)
     * @see #handles(String)
     */
    public static boolean isSemanticId(String value) {
        boolean result = false;
        for (SemanticIdRecognizer r : INSTANCES) {
            if (r.handles(value)) {
                result = r.isASemanticId(value);
                if (result) {
                    break;
                }
            }
        }
        return result;
    }
    
    /**
     * Whether two strings can be combined to a semanticId.
     * 
     * @author Holger Eichelberger, SSE
     */
    protected interface SplitAndCombinePredicate {

        /**
         * Whether two strings can be combined to a semanticId.
         * 
         * @param before the string before
         * @param after the string after
         * @return {@code true} for combinable, {@code false} else
         */
        public boolean isCombinable(String before, String after);
        
    }

    /**
     * Splits {@code value} along whitespaces and tries to find a maximum semanticId match to return.
     * 
     * @param value the value to be considered as semanticId
     * @param predicate a predicate for combining, may be <b>null</b> for none
     * @return <b>null</b> if {@code value} is no semantic id, a maximum semanticId starting at the beginning of 
     *   value else
     */
    protected String splitAndCombine(String value, SplitAndCombinePredicate predicate) {
        value = ParsingUtils.replaceWhitespace(value, " ");
        String[] tmp = value.split(" ");
        String result = tmp[0]; // assumption, may be wrong, see below
        for (int i = 1; i < tmp.length; i++) {
            if (null == predicate || predicate.isCombinable(result, tmp[i])) {
                String tmpId = result + tmp[i];
                if (isASemanticId(tmpId)) {
                    result = tmpId;
                } else {
                    break;
                }
            }
        }
        if (!isASemanticId(result)) {
            result = null;
        }
        return result;
    }
    
    /**
     * Recognizer for ECLASS IRDIs.
     * 
     * @author Holger Eichelberger, SSE
     */
    private abstract static class PatternIrdiRecognizer extends SemanticIdRecognizer {

        /**
         * Returns the regular parsing pattern.
         * 
         * @return the pattern
         */
        protected abstract Pattern getPattern();

        @Override
        public boolean isFallback() {
            return false;
        }

        @Override
        public String getIdentifierPrefix() {
            return IdentifierType.IRDI_PREFIX;
        }

        @Override
        public String parseSemanticId(String value) {
            return splitAndCombine(value, null);
        }

        @Override
        public boolean isASemanticId(String value) {
            return getPattern().matcher(value).matches();
        }
        
    };
    
    /**
     * Recognizer for ECLASS IRDIs.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class EclassIrdiRecognizer extends PatternIrdiRecognizer {

        private static final Pattern PATTERN = Pattern.compile("^\\d+-\\d#\\d+-[A-Z]+\\d+#\\d+$");
        
        @Override
        protected Pattern getPattern() {
            return PATTERN;
        }

        @Override
        public boolean handles(String value) {
            return value.startsWith("0173-");
        }

        @Override
        public String lastOfPath(String value) {
            String result = value;
            int pos = value.lastIndexOf("/");
            if (pos > 0) {
                result = value.substring(pos + 1);
            }
            return result;
        }

    };
    

    /**
     * Recognizer for IEC CCD IRDIs.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class IecCcdIrdiRecognizer extends PatternIrdiRecognizer {

        private static final Pattern PATTERN = Pattern.compile("^\\d+/\\d+///\\d+#[A-Z0-9]+\\d+.*$");

        @Override
        protected Pattern getPattern() {
            return PATTERN;
        }

        @Override
        public boolean handles(String value) {
            return value.startsWith("0112/");
        }

    };
    
    /**
     * Basic/Fallback recognizer for URL-based IRIs.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class UrlIriRecognizer extends SemanticIdRecognizer {

        private static final Pattern HANDLES_PATTERN = Pattern.compile(
            "^(https?|ftp|file)://.*");
        private static final Pattern PATTERN = Pattern.compile(
            "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
        
        @Override
        public boolean isFallback() {
            return true;
        }

        @Override
        public boolean handles(String value) {
            return HANDLES_PATTERN.matcher(value).matches();
        }

        @Override
        public String getIdentifierPrefix() {
            return IdentifierType.IRI_PREFIX;
        }

        @Override
        public String parseSemanticId(String value) {
            return splitAndCombine(value, (b, a) -> isCombinable(b, a));
        }
        
        /**
         * Whether two strings can be combined to a semanticId.
         * 
         * @param before the string before
         * @param after the string after
         * @return {@code true} for combinable, {@code false} else
         */
        private boolean isCombinable(String before, String after) {
            boolean result;
            if (before.endsWith("-")) { // split here into two pieces
                result = true;
            } else if (Character.isDigit(before.charAt(before.length() - 1))) {
                result = after.startsWith("/"); // assumption... version/revision is in before
            } else {
                result = before.endsWith("/") || after.startsWith("/") 
                    || after.contains("/") // split somewhere
                    || after.length() < 10; // typically not a description :o
            }
            return result;
        }

        @Override
        public boolean isASemanticId(String value) {
            return PATTERN.matcher(value).matches();
        }

    };

    /**
     * Recognizer for IDTA IRIs.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class IdtaIriRecognizer extends UrlIriRecognizer {

        @Override
        public boolean isFallback() {
            return false;
        }

        @Override
        public boolean handles(String value) {
            return value.startsWith("https://admin-shell.io/"); //  idta, zvei
        }

    };

}
