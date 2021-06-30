package test.de.iip_ecosphere.platform.services.environment.metricsProvider.utils;

/**
 * This interface facilitates the use of
 * {@link org.junit.Assert#assertThrows(Class, ThrowingRunnable)} from Java 8. It allows method
 * references to void methods (that declare checked exceptions) to be passed directly into
 * {@code assertThrows}
 * without wrapping. It is not meant to be implemented directly.
 *
 * @since 4.13
 */
public interface ThrowingRunnable {

    // checkstyle: stop exception type check
    
    /**
     * Run something potentially throwing a throwable.
     * 
     * @throws Throwable if something fails
     */
    void run() throws Throwable;
    
    // checkstyle: resume exception type check
    
}
