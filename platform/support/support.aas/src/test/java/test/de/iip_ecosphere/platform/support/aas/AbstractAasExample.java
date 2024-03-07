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

package test.de.iip_ecosphere.platform.support.aas;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.IdentifierType;
import de.iip_ecosphere.platform.support.aas.LangString;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.PersistenceRecipe.FileResource;
import de.iip_ecosphere.platform.support.aas.Reference;
import de.iip_ecosphere.platform.support.aas.ReferenceElement;
import de.iip_ecosphere.platform.support.resources.ResourceLoader;

import static test.de.iip_ecosphere.platform.support.aas.AasSpecVisitor.createDateFormat;

/**
 * Base class for AAS examples/tests.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractAasExample {

    private static final Pattern[] INT_PATTERN = {Pattern.compile("^\\D*(?<value>\\d+)(\\.\\d+)?.*$")};
    private static final Pattern[] DBL_PATTERN = {Pattern.compile("^\\D*(?<value>\\d+(\\.\\d+)?).*$")};
    private static final DateFormat[] TIME_FORMATTER = {createDateFormat("dd.mm.yyyy"), 
        createDateFormat("yyyy/mm/dd")};
    private static final Date DATE_OF_TEST;
    
    private List<Aas> aasList = new ArrayList<Aas>();
    private Map<String, Aas> parts = new TreeMap<>();
    private List<FileResource> resources = new ArrayList<FileResource>();
    private boolean createOperations = true;
    private boolean createMultiLanguageProperties = true;
    private File tempFolder = new File(FileUtils.getTempDirectory(), getFolderName());
    
    static {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2024);
        cal.set(Calendar.MONTH, 0);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 1);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        DATE_OF_TEST = cal.getTime();
    }
    
    /**
     * Returns the temporary folder.
     * 
     * @return the temporary folder
     */
    protected File getTempFolder() {
        return tempFolder;
    }
    
    /**
     * Indicates the name of the resources/temporary folder for this example.
     * 
     * @return the folder name
     */
    protected abstract String getFolderName();

    /**
     * Returns the resource folder (prefix).
     * 
     * @return the resource folder
     */
    protected String getResourceFolder() {
        return getFolderName() + "/";
    }
    
    /**
     * Returns the target files for persisting the AAS.
     * 
     * @return the target files
     */
    public abstract File[] getTargetFiles();

    /**
     * Enables/disables creating operations.
     * 
     * @param createOperations shall we create operations
     */
    public void setCreateOperations(boolean createOperations) {
        this.createOperations = createOperations;
    }

    /**
     * Enables/disables creating multi-language properties.
     * 
     * @param createMultiLanguageProperties shall we create multi-language properties
     */
    public void setCreateMultiLanguageProperties(boolean createMultiLanguageProperties) {
        this.createMultiLanguageProperties = createMultiLanguageProperties;
    }

    /**
     * Returns whether creating operations is enabled/disabled.
     * 
     * @return shall we create operations
     */
    protected boolean isCreateOperations() {
        return this.createOperations;
    }

    /**
     * Returns whether creating multi-language properties is enabled/disabled.
     * 
     * @return shall we create multi-language properties
     */
    protected boolean isCreateMultiLanguageProperties() {
        return this.createMultiLanguageProperties;
    }

    /**
     * Registers a created AAS (for persisting it).
     * 
     * @param aasBuilder the AAS builder
     * @return the AAS
     * @see #registerAas(Aas)
     */
    protected Aas registerAas(AasBuilder aasBuilder) {
        return registerAas(aasBuilder.build());
    }

    /**
     * Registers a created AAS (for persisting it).
     * 
     * @param aas the AAS
     * @return {@code aas}
     */
    protected Aas registerAas(Aas aas) {
        aasList.add(aas);
        parts.put("node_" + aas.getIdShort(), aas);
        return aas;
    }
    
    /**
     * Registers a resource (once).
     * 
     * @param resource the resource to be registered
     * @return {@code resource}
     */
    protected FileResource registerResource(FileResource resource) {
        if (!resources.stream().anyMatch(r -> resource.getPath().equals(r.getPath()))) {
            resources.add(resource);
        }
        return resource;
    }
    
    /**
     * Iterates over all parts.
     * 
     * @param consumer the iterator consumer
     */
    protected void forEachPart(BiConsumer<? super String, ? super Aas> consumer) {
        parts.forEach(consumer);
    }

    /**
     * Returns a resource as a file. As we store resources on the class path and test execution happens in the
     * specific AAS implementations, we need store a copy in the temporary folder.
     * 
     * @param name the name of the resource
     * @return the file or <b>null</b> if the ressource cannot be found/stored temporarily
     * @see #getResourceFolder()
     */
    protected File getFileResource(String name) {
        File result = null;
        InputStream in = ResourceLoader.getResourceAsStream(getResourceFolder() + name);
        if (null != in) {
            File parent = getTempFolder();
            parent.mkdirs();
            File tmp = new File(parent, name);
            if (tmp.exists()) { // we assume it's the right one then
                result = tmp;
            } else {
                try {
                    FileUtils.copyInputStreamToFile(in, tmp);
                    result = tmp;
                    result.deleteOnExit();
                } catch (IOException e) {
                    System.err.println("Cannot write resource to temporary folder. Ignoring resource " + name);
                }
            }
        } else {
            System.err.println("Cannot find resource on classpath. Ignoring resource " + name);
        }
        return result;
    }
    
    /**
     * Tests creating and storing the AAS.
     * 
     * @throws IOException if persisting does not work.
     */
    @Test
    public void testCreateAndStore() throws IOException {
        testCreateAndStore(true);
    }
    
    /**
     * Tests creating and storing the AAS.
     * 
     * @param compare compare against the stored AAS spec by {@link AasSpecVisitor}
     * @throws IOException if persisting does not work.
     * @see #createAas()
     * @see #getThumbnail()
     * @see #assertAllAas()
     */
    public void testCreateAndStore(boolean compare) throws IOException {
        FileUtils.deleteQuietly(getTempFolder());
        createAas();
        File thumbnail = getThumbnail();
        for (File aasx: getTargetFiles()) {
            aasx.getParentFile().mkdirs();
            AasFactory.getInstance().createPersistenceRecipe().writeTo(aasList, thumbnail, resources, aasx);
        }
        FileUtils.deleteQuietly(getTempFolder());
        if (compare) {
            assertAllAas();
        }
    }

    /**
     * Returns the thumbnail for persisting AAS.
     * 
     * @return the thumbnail
     */
    protected abstract File getThumbnail();
    
    /**
     * Creates the example/test AAS. Call {@link #registerAas(Aas)} on each created AAS,
     * {@link #registerResource(FileResource)} on each created resource.
     */
    protected abstract void createAas();
    
    /**
     * Asserts the structure of all created AAS.
     * 
     * @see #getResourceFolder()
     */
    protected void assertAllAas() {
        for (Aas aas : aasList) {
            AasSpecVisitor.assertEquals(aas, getResourceFolder());
        }
    }
    
    /**
     * Creates a URI treating the given text {@code file} as File and handles exceptions.
     * 
     * @param file the file location
     * @return the URI instance
     */
    public static URI createFileURI(String file) {
        return createURI("file:/" + file.replace("\\", "/").replace(" ", "%20")); // newFile fails on Linux
    }
    
    /**
     * Creates a URI and handles exceptions.
     * 
     * @param uri the URI text
     * @return the URI instance
     */
    public static URI createURI(String uri) {
        try {
            return new URI(uri);
        } catch (URISyntaxException e) {
            System.err.println("Cannot create URI:" + e.getMessage());
            return null;
        }
    }
    
    /**
     * Asserts properties on enum values.
     * 
     * @param <T> the enum type
     * @param values the values
     * @param asserter the asserter function
     */
    public static <T extends Enum<T>> void assertEnum(T[] values, Predicate<T> asserter) {
        for (T value: values) {
            Assert.assertTrue(asserter.test(value));
        }
    }

    /**
     * Turns a string tolerantly to a test enum value. May prevent usual issues from spec parsing/analysis.
     * Considers values of a {@code getValue} method if defined. Uses the first value of the enum as default value.
     * 
     * @param <T> the enum type
     * @param cls the enum class type
     * @param value the value
     * @return the test enum value
     */
    public static <T extends Enum<T>> T toTestEnum(Class<T> cls, String value) {
        return toTestEnum(cls, value, null, true);        
    }

    /**
     * Turns a string tolerantly to a test enum value. May prevent usual issues from spec parsing/analysis.
     * Considers values of a {@code getValue} method if defined.
     * 
     * @param <T> the enum type
     * @param cls the enum class type
     * @param value the value
     * @param dflt the default value to use if there is no matching enum value <b>null</b> is given
     * @return the test enum value
     */
    public static <T extends Enum<T>> T toTestEnum(Class<T> cls, String value, T dflt) {
        return toTestEnum(cls, value, dflt, false);
    }
    
    /**
     * Turns a string tolerantly to a test enum value. May prevent usual issues from spec parsing/analysis.
     * Considers values of a {@code getValue} method if defined.
     * 
     * @param <T> the enum type
     * @param cls the enum class type
     * @param value the value
     * @param dflt the default value to use if there is no matching enum value <b>null</b> is given
     * @param firstAsDefault take the first value of the enum as default
     * @return the test enum value
     */
    @SuppressWarnings("unchecked")
    public static <T extends Enum<T>> T toTestEnum(Class<T> cls, String value, T dflt, boolean firstAsDefault) {
        T result = dflt;
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
        boolean matched = false;
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
                matched = true;
                result = v;
                break;
            }
        }
        if (firstAsDefault && !matched && !values.isEmpty()) {
            result = values.get(0);
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
            matches = value.contains(ev);
            if (!matches && (ev.startsWith(IdentifierType.IRI_PREFIX) || ev.startsWith(IdentifierType.IRDI_PREFIX))) {
                matches = ev.contains(value);
            }
        }
        return matches;
    }
    
    // methods to tolerantly translate value extracted from an AAS spec into a test value of a certain type.
    // Naming convention: toTest<AasType>(String value, <AasType> dflt) except for methods that are explicity
    // called by non-generic generation parts. 
    
    /**
     * Turns a string tolerantly to a test value. May prevent usual issues from spec parsing/analysis.
     * 
     * @param value the value
     * @param dflt the default value to use if an empty String or <b>null</b> is given
     * @return the test value
     */
    public static String toTestString(String value, String dflt) {
        String result;
        if (value == null || value.length() == 0) {
            result = dflt;
        } else {
            result = value; // tolerance needed here?
        }
        return result;
    }

    /**
     * Turns a string tolerantly to multi-language a test lang-string value. May prevent usual issues from spec 
     * parsing/analysis.
     * 
     * @param value the value
     * @param dflt the default value as text@lang
     * @return the test value
     */
    public static LangString[] toTestLangString(String value, String dflt) {
        if (null == dflt) {
            dflt = "";
        }
        Map<String, String> values = parseMLString(value == null || value.length() == 0 ? dflt : value);
        LangString[] result = new LangString[values.size()];
        int count = 0;
        for (Map.Entry<String, String> v : values.entrySet()) {
            result[count++] = new LangString(v.getKey(), v.getValue());
        }
        return result;
    }

    /**
     * Turns a string tolerantly to multi-language a test value. May prevent usual issues from spec parsing/analysis.
     * 
     * @param value the value
     * @param dfltLang the default language to use if none is found, also selects the value to return in case of a 
     *     true multi-language value
     * @param dflt the default value to use if an empty String or <b>null</b> is given
     * @return the test value
     */
    public static String toTestMLString(String value, String dfltLang, String dflt) {
        String result;
        if (value == null || value.length() == 0) {
            result = dflt;
        } else {
            Map<String, String> values = parseMLString(value);
            result = values.get(dfltLang);
            if (null == result) {
                result = dflt;
            } else {
                result += "@" + dfltLang;
            }
        }
        int pos = result.lastIndexOf("@");
        if (pos < 0) {
            while (dfltLang.startsWith("@")) {
                dfltLang = dfltLang.substring(1);
            }
            result += "@" + dfltLang; 
        }
        return result;
    }
    
    /**
     * Tolerantly parses a multi-language string into language-annotated texts.
     *  
     * @param value the value
     * @return the specified language string or <b>null</b>
     */
    private static Map<String, String> parseMLString(String value) {
        value = value.trim();
        Map<String, String> values = new HashMap<>();
        boolean textBefore = value.indexOf("@") > 0;
        int lastPos = 0;
        int pos;
        do {
            pos = value.indexOf("@", lastPos);
            if (pos >= 0) {
                int endPos = value.indexOf(" ", pos);
                if (endPos < 0) {
                    endPos = value.length();
                }
                if (endPos - pos <= 4) {
                    String lang = value.substring(pos + 1, endPos);
                    if (lang.endsWith(":")) {
                        lang = lang.substring(0, lang.length() - 1);
                    }
                    String text = null;
                    if (textBefore) {
                        text = value.substring(lastPos, pos);
                        lastPos = endPos + 1;
                    } else {
                        pos = value.indexOf("@", pos + 1);
                        if (pos < 0) {
                            text = value.substring(endPos + 1);
                        } else {
                            text = value.substring(endPos + 1, pos);
                        }
                        lastPos = pos - 1;
                    }
                    if (lang != null && text != null) {
                        values.put(lang.trim(), text.trim());
                    }
                } else {
                    lastPos = pos + 1;
                }
            }
        } while (pos > 0);
        return values;
    }

    /**
     * Generic function to turn a test value into a typed value.
     * 
     * @param <T> the value type
     * @param value the value
     * @param dflt the default value to use if an empty String or <b>null</b> is given
     * @param converter the value converter from a preprocessed string to a typed value
     * @param patterns additional fallback patterns to extract a string value, must indicate the value by 
     *     <code>(?&lt;value&gt;...)</code>
     * @return the extracted and converted value
     */
    public static <T> T toTestValue(String value, T dflt, Function<String, T> converter, Pattern... patterns) {
        T result;
        if (value == null || value.length() == 0) {
            result = dflt;
        } else {
            try {
                result = converter.apply(value); // find for
            } catch (NumberFormatException e) {
                result = dflt;
                for (Pattern p: patterns) {
                    Matcher m = p.matcher(value);
                    if (m.matches()) {
                        try {
                            result = converter.apply(m.group("value"));
                        } catch (NumberFormatException e1) {
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * Turns a string tolerantly to an integer test value. May prevent usual issues from spec parsing/analysis.
     * 
     * @param value the value
     * @param dflt the default value to use if no integer was found
     * @return the test value
     */
    public static int toTestInt(String value, int dflt) {
        return toTestValue(value, dflt, v -> Integer.parseInt(v), INT_PATTERN);
    }

    /**
     * Turns a string tolerantly to a double test value. May prevent usual issues from spec parsing/analysis.
     * 
     * @param value the value
     * @param dflt the default value to use if no double was found
     * @return the test value
     */
    public static double toTestDouble(String value, double dflt) {
        return toTestValue(value, dflt, v -> Double.parseDouble(v), DBL_PATTERN);
    }

    /**
     * Turns a string tolerantly to a boolean test value. May prevent usual issues from spec parsing/analysis.
     * 
     * @param value the value
     * @param dflt the default value to use if no boolean was found
     * @return the test value
     */
    public static boolean toTestBoolean(String value, boolean dflt) {
        boolean result;
        if (value == null || value.length() == 0) {
            result = dflt;
        } else {
            value = value.replace("\r\n", " ")
                .replace("\n", " ")
                .replace("\r", " ")
                .toLowerCase()
                .trim();
            if (value.equals("true")) {
                result = true;
            } else if (value.equals("false")) {
                result = false;
            } else if (value.contains("true ") || value.contains(" true ") || value.contains(" true")) {
                result = true;
            } else if (value.contains("false ") || value.contains(" false ") || value.contains(" false")) {
                result = false;
            } else {
                result = dflt;
            }
        }
        return result;
    }
    
    /**
     * Turns a string tolerantly to resource mime type. May prevent usual issues from spec parsing/analysis.
     * 
     * @param value the value
     * @param dflt the default value to use if no mime type was found
     * @return the mime type or {@code dflt} if none found in {@code value}
     */
    public static String toTestResourceMimeType(String value, String dflt) {
        String result;
        if (value == null || value.length() == 0) {
            result = dflt;
        } else {
            final String mimeMarker = "MimeType =";
            final String valueMarker = "Value =";
            int mimePos = value.indexOf(mimeMarker);
            int valuePos = value.indexOf(valueMarker);
            if (mimePos >= 0 && valuePos >= 0) {
                if (mimePos > valuePos) {
                    result = value.substring(mimePos + mimeMarker.length());
                } else {
                    result = value.substring(mimeMarker.length(), valuePos);
                }
                result = result.trim();
            } else {
                result = dflt;
            }
        }
        return result;
    }

    /**
     * Turns a string tolerantly to resource file/value. May prevent usual issues from spec parsing/analysis.
     * 
     * @param value the value
     * @param dflt the default value to use if no file was found
     * @return the file/value or {@code dflt} if none found in {@code value}
     */
    public static String toTestResourceFile(String value, String dflt) {
        String result;
        if (value == null || value.length() == 0) {
            result = dflt;
        } else {
            final String mimeMarker = "MimeType =";
            final String valueMarker = "Value =";
            int mimePos = value.indexOf(mimeMarker);
            int valuePos = value.indexOf(valueMarker);
            if (mimePos >= 0 && valuePos >= 0) {
                if (mimePos > valuePos) {
                    result = value.substring(valueMarker.length(), mimePos);
                } else {
                    result = value.substring(valuePos + valueMarker.length());
                }
                result = result.trim();
            } else {
                result = dflt;
            }
        }
        return result;
    }
    
    /**
     * Turns value tolerantly into a reference if possible.
     * 
     * @param value the value
     * @param dflt the default value if {@code value} does not point to an AAS element
     * @return {@code dflt} for now
     */
    public static final Reference toTestReferenceElement(String value, Reference dflt) {
        // may search in AasList for something that looks like value; shall consider value = null || value.length() ==0
        return dflt;
    }
    
    /**
     * Returns a constant date for testing.
     * 
     * @return a date
     */
    public static Date getDateOfTest() {
        return DATE_OF_TEST;
    }

    /**
     * Turns a string tolerantly to a date instance. May prevent usual issues from spec parsing/analysis.
     * 
     * @param value the value
     * @param dflt the default value to use if no date was found
     * @return the date instance or {@code dflt} if none found in {@code value}
     */
    public static Date toTestDate(String value, Date dflt) {
        Date result = null;
        if (value != null && value.length() > 0) {
            for (DateFormat f: TIME_FORMATTER) {
                try {
                    result = f.parse(value);
                    break;
                } catch (ParseException e) {
                }
            }
        }
        if (null == result) {
            result = dflt;
        }
        return result;
    }
    
    /**
     * Asserts language strings independent of their sequence.
     * 
     * @param expected the expected language strings
     * @param actual the actual language strings
     */
    public static void assertLangStringsEquals(LangString[] expected, LangString[] actual) {
        Assert.assertTrue("One is missing. Expected: " + Arrays.toString(expected) + " Actual: " 
            + Arrays.toString(actual), (expected != null && actual != null) || (expected == null && actual == null));
        if (expected != null && actual != null) {
            Assert.assertEquals("LangStrings differ in size: Expected: " + expected.length + " Actual: " 
                + actual.length, expected.length, actual.length);
            Map<String, LangString> act = new HashMap<>();
            for (LangString a : actual) {
                act.put(a.getLanguage(), a);
            }
            for (LangString e : expected) {
                LangString a = act.remove(e.getLanguage());
                Assert.assertNotNull("Expacted LangString for language " + e.getLanguage() + " missing.", a);
                Assert.assertEquals("LangStrings differ. Expected:" + e + " Actual: " + a, e, a);
            }
        }
    }
    
    /**
     * Asserts the (value of) the {@code actual} reference element compared to the given {@code expected} reference.
     * 
     * @param expected the expected reference
     * @param actual the actual reference element
     */
    public static void assertReferenceEquals(Reference expected, ReferenceElement actual) {
        // TODO check with multiSemId!
        /*if (null != expected) {
            Assert.assertNotNull(actual);
            Assert.assertEquals(expected, actual.getValue());
        } else {
            Assert.assertNull(actual);
        }*/
    }
    
    // generic methods in test applied on a given value, set, iterator to obtain the "first" for assertions
    
    /**
     * Returns the "first" element, i.e., {@code obj}. Basic fallback of the other "first" methods. Selection
     * is made statically during compilation!
     * 
     * @param <T> the element type
     * @param obj the object
     * @return {@code obj}
     */
    public static <T> T first(T obj) {
        return obj;
    }

    /**
     * Returns the "first" element, i.e., value of {@code elt}. 
     * 
     * @param elt the reference element
     * @return the reference, possibly <b>null</b>
     */
    public static Reference first(ReferenceElement elt) {
        Reference result = null;
        if (null != elt) {
            result = elt.getValue();
        }
        return result;
    }

    /**
     * Returns the first element of {@code iter}.
     * 
     * @param <T> the element type
     * @param iter the iterator
     * @return the first element or <b>null</b> if there is none
     */
    public static <T> T first(Iterable<T> iter) {
        return first(iter.iterator());
    }

    /**
     * Returns the first element of {@code iter}.
     * 
     * @param <T> the element type
     * @param iter the iterator
     * @return the first element or <b>null</b> if there is none
     */
    public static <T> T first(Iterator<T> iter) {
        T result = null;
        if (iter.hasNext()) {
            result = iter.next();
        }
        return result;
    }

}
