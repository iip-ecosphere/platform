package test.de.iip_ecosphere.platform.services.environment.metricsProvider.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;

import javax.json.Json;
import javax.json.JsonObject;

import de.iip_ecosphere.platform.support.TimeUtils;

/**
 * Class to avoid repetition of code in testing environment.<br>
 * This class extracts a JsonObject from a resource and has some timed methods.
 * 
 * @author Miguel Gomez
 */
public class TestUtils {

    public static final String DATA = "DATA";

    /**
     * Extracts a JsonObject from a resource.<br>
     * The resource will be nested in a directory nested under the directory
     * {@code jsonsamples} in the resource directory.
     * 
     * @param folder   name of the directory where the resource is found
     * @param filename name of the file containing the resource
     * @return JSON representation of the resource
     * @throws IOException if the file cannot be found or an error occurs when
     *                     opening the stream
     */
    public static JsonObject readJsonFromResources(String folder, String filename) throws IOException {
        String rssName = "jsonsamples/" + folder + "/" + filename;
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(
                new FileReader(Thread.currentThread().getContextClassLoader().getResource(rssName).getFile()));

        while (br.ready()) {
            sb.append(br.readLine());
        }
        br.close();
        return Json.createReader(new StringReader(sb.toString())).readObject();
    }

    /**
     * Runs/waits for one second.
     */
    public static void oneSecondRunnable() {
        TimeUtils.sleep(1000);
    }

    /**
     * Runs/waits for two seconds.
     */
    public static void twoSecondRunnable() {
        TimeUtils.sleep(2000);
    }

    /**
     * Runs/waits for three seconds.
     */
    public static void threeSecondRunnable() {
        TimeUtils.sleep(3000);
    }

    /**
     * Runs/waits for one second and returns {@link #DATA}.
     * 
     * @return {@link #DATA}
     */
    public static String oneSecondSupplier() {
        TimeUtils.sleep(1000);
        return DATA;
    }

    /**
     * Runs/waits for two seconds and returns {@link #DATA}.
     * 
     * @return {@link #DATA}
     */
    public static String twoSecondSupplier() {
        TimeUtils.sleep(2000);
        return DATA;
    }

    /**
     * Runs/waits for three seconds and returns {@link #DATA}.
     * 
     * @return {@link #DATA}
     */
    public static String threeSecondSupplier() {
        TimeUtils.sleep(3000);
        return DATA;
    }
    
    // taking over parts of jUnit 4.13 due to problems with Jenkins
    
    /**
     * Asserts that {@code runnable} throws an exception of type {@code expectedThrowable} when
     * executed. If it does, the exception object is returned. If it does not throw an exception, an
     * {@link AssertionError} is thrown. If it throws the wrong type of exception, an {@code
     * AssertionError} is thrown describing the mismatch; the exception that was actually thrown can
     * be obtained by calling {@link AssertionError#getCause}.
     *
     * @param <T> throwable type
     * @param expectedThrowable the expected type of the exception
     * @param runnable       a function that is expected to throw an exception when executed
     * @return the exception thrown by {@code runnable}
     */
    public static <T extends Throwable> T assertThrows(Class<T> expectedThrowable,
            ThrowingRunnable runnable) {
        return assertThrows(null, expectedThrowable, runnable);
    }
    
    // checkstyle: stop exception type check

    /**
     * Asserts that {@code runnable} throws an exception of type {@code expectedThrowable} when
     * executed. If it does, the exception object is returned. If it does not throw an exception, an
     * {@link AssertionError} is thrown. If it throws the wrong type of exception, an {@code
     * AssertionError} is thrown describing the mismatch; the exception that was actually thrown can
     * be obtained by calling {@link AssertionError#getCause}.
     *
     * @param <T> throwable type
     * @param message the identifying message for the {@link AssertionError} (<code>null</code>
     * okay)
     * @param expectedThrowable the expected type of the exception
     * @param runnable a function that is expected to throw an exception when executed
     * @return the exception thrown by {@code runnable}
     */
    public static <T extends Throwable> T assertThrows(String message, Class<T> expectedThrowable,
            ThrowingRunnable runnable) {
        try {
            runnable.run();
        } catch (Throwable actualThrown) {
            if (expectedThrowable.isInstance(actualThrown)) {
                @SuppressWarnings("unchecked") T retVal = (T) actualThrown;
                return retVal;
            } else {
                String expected = formatClass(expectedThrowable);
                Class<? extends Throwable> actualThrowable = actualThrown.getClass();
                String actual = formatClass(actualThrowable);
                if (expected.equals(actual)) {
                    // There must be multiple class loaders. Add the identity hash code so the message
                    // doesn't say "expected: java.lang.String<my.package.MyException> ..."
                    expected += "@" + Integer.toHexString(System.identityHashCode(expectedThrowable));
                    actual += "@" + Integer.toHexString(System.identityHashCode(actualThrowable));
                }
                String mismatchMessage = buildPrefix(message)
                        + format("unexpected exception type thrown;", expected, actual);

                // The AssertionError(String, Throwable) ctor is only available on JDK7.
                AssertionError assertionError = new AssertionError(mismatchMessage);
                assertionError.initCause(actualThrown);
                throw assertionError;
            }
        }
        String notThrownMessage = buildPrefix(message) + String
                .format("expected %s to be thrown, but nothing was thrown",
                        formatClass(expectedThrowable));
        throw new AssertionError(notThrownMessage);
    }

    // checkstyle: resume exception type check
    
    /**
     * Builds a message prefix.
     * 
     * @param message the message
     * @return the message with prefix
     */
    private static String buildPrefix(String message) {
        return message != null && message.length() != 0 ? message + ": " : "";
    }

    /**
     * Formats the given class name.
     * 
     * @param value the class
     * @return the formatted class name
     */
    private static String formatClass(Class<?> value) {
        String className = value.getCanonicalName();
        return className == null ? value.getName() : className;
    }
    
    /**
     * Formats a message with given expected and actual value.
     * 
     * @param message the message
     * @param expected the expected object
     * @param actual the actual object
     * @return the message
     */
    static String format(String message, Object expected, Object actual) {
        String formatted = "";
        if (message != null && !"".equals(message)) {
            formatted = message + " ";
        }
        String expectedString = String.valueOf(expected);
        String actualString = String.valueOf(actual);
        if (equalsRegardingNull(expectedString, actualString)) {
            return formatted + "expected: "
                    + formatClassAndValue(expected, expectedString)
                    + " but was: " + formatClassAndValue(actual, actualString);
        } else {
            return formatted + "expected:<" + expectedString + "> but was:<"
                    + actualString + ">";
        }
    }

    /**
     * Formats a class name including value.
     * 
     * @param value the class/object value
     * @param valueString the value
     * @return the formatted string
     */
    private static String formatClassAndValue(Object value, String valueString) {
        String className = value == null ? "null" : value.getClass().getName();
        return className + "<" + valueString + ">";
    }

    /**
     * Returns whether expected and actual are considered equal regarding <b>null</b>.
     * 
     * @param expected the expected value
     * @param actual the actual value
     * @return {@code true} for equal, {@code false} else
     */
    private static boolean equalsRegardingNull(Object expected, Object actual) {
        if (expected == null) {
            return actual == null;
        }

        return isEquals(expected, actual);
    }

    /**
     * Returns whether expected and actual are considered equal.
     * 
     * @param expected the expected value
     * @param actual the actual value
     * @return {@code true} for equal, {@code false} else
     */
    private static boolean isEquals(Object expected, Object actual) {
        return expected.equals(actual);
    }

}
