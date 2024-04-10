package de.iip_ecosphere.platform.configuration.aas;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Kinds of enumerations in parsing.
 * 
 * @author Holger Eichelberger, SSE
 */
enum ParsingEnumKind {
    
    ENUM("enumeration: "),
    ENUM_ENTRIES("enumeration entries: "),
    VALUE_LIST2("Value List:", "ValueList:"), // IDTA-02010-1-0, before Value_LIST! also IDTA-02021-1-0 
    VALUE_LIST("Value List", "Value List\\s+\\Q(\\E[^\\Q)\\E]+\\Q)\\E:"),
    IRDIS("\\Q[\\EIRDIs for values\\Q]\\E:"); // IDTA-02004-1-2.

    private Pattern[] marker;

    /**
     * Creates an enumeration marker.
     * 
     * @param marker the marker
     */
    private ParsingEnumKind(String... marker) {
        this.marker = new Pattern[marker.length];
        for (int i = 0; i < marker.length; i++) {
            this.marker[i] = Pattern.compile("^(?<before>.*)(?<pattern>" + marker[i] + ")(?<after>.*)$", 
                Pattern.MULTILINE | Pattern.DOTALL);
        }
    }
    
    /**
     * Returns a match of this enum kind within {@code text}.
     * 
     * @param text the text to match
     * @param atBeginning whether the match shall be at the beginning or within {@code text}
     * @return the match (0: text before, 1: text after) or <b>null</b> for no match
     */
    public String[] getMatch(String text, boolean atBeginning) {
        String[] result = null;
        for (Pattern m: marker) {
            Matcher matcher = m.matcher(text);
            if (matcher.matches()) {
                int patternStart = matcher.start("pattern");
                if ((atBeginning && patternStart >= 0) || (!atBeginning && patternStart > 0)) {
                    result = new String[2];
                    result[0] = matcher.group("before").trim();
                    result[1] = matcher.group("after");
                    break;
                }
            }
        }
        return result;
    }
    
}