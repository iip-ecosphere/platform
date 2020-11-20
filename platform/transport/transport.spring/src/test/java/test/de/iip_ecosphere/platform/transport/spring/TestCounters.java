package test.de.iip_ecosphere.platform.transport.spring;

import de.iip_ecosphere.platform.transport.serialization.Serializer;

/**
 * A test helper class to count statistics for a serializer. The {@link StringSerializer} used in testing
 * will record its activities here. Please consider {@link #reset() resetting} the counters before/after a test.
 * 
 * @author Holger Eichelberger, SSE
 */
class TestCounters {

    private static int toCount = 0;
    private static int fromCount = 0;
    private static int copyCount = 0;
    
    /**
     * Resets all counters.
     */
    static void reset() {
        toCount = 0;
        fromCount = 0;
        copyCount = 0;
    }

    /**
     * Increases the to-counter (related to the {@link Serializer#to(Object) to-method} of the serializers.
     */
    static void increaseTo() {
        toCount++;
    }

    /**
     * Increases the from-counter (related to the {@link Serializer#from(Object) to-method} of the serializers.
     */
    static void increaseFrom() {
        fromCount++;
    }
    
    /**
     * Increases the copy-counter (related to the {@link Serializer#copy(Object) copy-method} of the serializers.
     */
    static void increaseCopy() {
        copyCount++;
    }

    /**
     * Returns the to-counter value.
     * 
     * @return the number of calls to {@link Serializer#to(Object)}
     */
    static int getToCount() {
        return toCount;
    }

    /**
     * Returns the from-counter value.
     * 
     * @return the number of calls to {@link Serializer#from(Object)}
     */
    static int getFromCount() {
        return fromCount;
    }

    /**
     * Returns the copy-counter value.
     * 
     * @return the number of calls to {@link Serializer#copy(Object)}
     */
    static int getCopyCount() {
        return copyCount;
    }

}
