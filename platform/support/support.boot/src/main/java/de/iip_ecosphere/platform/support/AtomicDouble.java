package de.iip_ecosphere.platform.support;

import static java.lang.Double.doubleToRawLongBits;
import static java.lang.Double.longBitsToDouble;

import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;

/**
 * A {@code double} value that may be updated atomically. See the
 * {@link java.util.concurrent.atomic} package specification for description of
 * the properties of atomic variables. An {@code AtomicDouble} is used in
 * applications such as atomic accumulation, and cannot be used as a replacement
 * for a {@link Double}. However, this class does extend {@code
 * Number} to allow uniform access by tools and utilities that deal with
 * numerically-based classes. Taken over from Google Guava to reduce
 * dependencies.
 *
 * @author Doug Lea
 * @author Martin Buchholz
 * @since 11.0
 */
public class AtomicDouble extends Number implements java.io.Serializable {

    private static final long serialVersionUID = 0L;
    private static final AtomicLongFieldUpdater<AtomicDouble> UPDATER 
        = AtomicLongFieldUpdater.newUpdater(AtomicDouble.class, "value");

    private transient volatile long value;

    /**
     * Creates a new {@code AtomicDouble} with the given initial value.
     *
     * @param initialValue the initial value
     */
    public AtomicDouble(double initialValue) {
        value = doubleToRawLongBits(initialValue);
    }

    /** Creates a new {@code AtomicDouble} with initial value {@code 0.0}. */
    public AtomicDouble() {
        // assert doubleToRawLongBits(0.0) == 0L;
    }

    /**
     * Gets the current value.
     *
     * @return the current value
     */
    public final double get() {
        return longBitsToDouble(value);
    }

    /**
     * Sets to the given value.
     *
     * @param newValue the new value
     */
    public final void set(double newValue) {
        long next = doubleToRawLongBits(newValue);
        value = next;
    }

    /**
     * Eventually sets to the given value.
     *
     * @param newValue the new value
     */
    public final void lazySet(double newValue) {
        long next = doubleToRawLongBits(newValue);
        UPDATER.lazySet(this, next);
    }

    /**
     * Atomically sets to the given value and returns the old value.
     *
     * @param newValue the new value
     * @return the previous value
     */
    public final double getAndSet(double newValue) {
        long next = doubleToRawLongBits(newValue);
        return longBitsToDouble(UPDATER.getAndSet(this, next));
    }

    /**
     * Atomically sets the value to the given updated value if the current value is
     * <a href="#bitEquals">bitwise equal</a> to the expected value.
     *
     * @param expect the expected value
     * @param update the new value
     * @return {@code true} if successful. False return indicates that the actual
     *         value was not bitwise equal to the expected value.
     */
    public final boolean compareAndSet(double expect, double update) {
        return UPDATER.compareAndSet(this, doubleToRawLongBits(expect), doubleToRawLongBits(update));
    }

    /**
     * Atomically sets the value to the given updated value if the current value is
     * <a href="#bitEquals">bitwise equal</a> to the expected value.
     *
     * <p>
     * May <a href=
     * "http://download.oracle.com/javase/7/docs/api/java/util/concurrent/atomic/package-summary.html#Spurious">
     * fail spuriously</a> and does not provide ordering guarantees, so is only
     * rarely an appropriate alternative to {@code compareAndSet}.
     *
     * @param expect the expected value
     * @param update the new value
     * @return {@code true} if successful
     */
    public final boolean weakCompareAndSet(double expect, double update) {
        return UPDATER.weakCompareAndSet(this, doubleToRawLongBits(expect), doubleToRawLongBits(update));
    }

    /**
     * Atomically adds the given value to the current value.
     *
     * @param delta the value to add
     * @return the previous value
     */
    public final double getAndAdd(double delta) {
        return getAndAccumulate(delta, Double::sum);
    }

    /**
     * Atomically adds the given value to the current value.
     *
     * @param delta the value to add
     * @return the updated value
     */
    public final double addAndGet(double delta) {
        return accumulateAndGet(delta, Double::sum);
    }

    /**
     * Ensures that an object reference passed as a parameter to the calling method
     * is not null.
     *
     * @param reference an object reference
     * @return the non-null reference that was validated
     * @throws NullPointerException if {@code reference} is null
     */
    public static <T> T checkNotNull(T reference) {
        if (reference == null) {
            throw new NullPointerException();
        }
        return reference;
    }

    /**
     * Atomically updates the current value with the results of applying the given
     * function to the current and given values.
     *
     * @param x                   the update value
     * @param accumulatorFunction the accumulator function
     * @return the previous value
     * @since 31.1
     */
    public final double getAndAccumulate(double x, DoubleBinaryOperator accumulatorFunction) {
        checkNotNull(accumulatorFunction);
        return getAndUpdate(oldValue -> accumulatorFunction.applyAsDouble(oldValue, x));
    }

    /**
     * Atomically updates the current value with the results of applying the given
     * function to the current and given values.
     *
     * @param x                   the update value
     * @param accumulatorFunction the accumulator function
     * @return the updated value
     * @since 31.1
     */
    public final double accumulateAndGet(double x, DoubleBinaryOperator accumulatorFunction) {
        checkNotNull(accumulatorFunction);
        return updateAndGet(oldValue -> accumulatorFunction.applyAsDouble(oldValue, x));
    }

    /**
     * Atomically updates the current value with the results of applying the given
     * function.
     *
     * @param updateFunction the update function
     * @return the previous value
     * @since 31.1
     */
    public final double getAndUpdate(DoubleUnaryOperator updateFunction) {
        while (true) {
            long current = value;
            double currentVal = longBitsToDouble(current);
            double nextVal = updateFunction.applyAsDouble(currentVal);
            long next = doubleToRawLongBits(nextVal);
            if (UPDATER.compareAndSet(this, current, next)) {
                return currentVal;
            }
        }
    }

    /**
     * Atomically updates the current value with the results of applying the given
     * function.
     *
     * @param updateFunction the update function
     * @return the updated value
     * @since 31.1
     */
    public final double updateAndGet(DoubleUnaryOperator updateFunction) {
        while (true) {
            long current = value;
            double currentVal = longBitsToDouble(current);
            double nextVal = updateFunction.applyAsDouble(currentVal);
            long next = doubleToRawLongBits(nextVal);
            if (UPDATER.compareAndSet(this, current, next)) {
                return nextVal;
            }
        }
    }

    /**
     * Returns the String representation of the current value.
     *
     * @return the String representation of the current value
     */
    @Override
    public String toString() {
        return Double.toString(get());
    }

    /**
     * Returns the value of this {@code AtomicDouble} as an {@code int} after a
     * narrowing primitive conversion.
     */
    @Override
    public int intValue() {
        return (int) get();
    }

    /**
     * Returns the value of this {@code AtomicDouble} as a {@code long} after a
     * narrowing primitive conversion.
     */
    @Override
    public long longValue() {
        return (long) get();
    }

    /**
     * Returns the value of this {@code AtomicDouble} as a {@code float} after a
     * narrowing primitive conversion.
     */
    @Override
    public float floatValue() {
        return (float) get();
    }

    /** Returns the value of this {@code AtomicDouble} as a {@code double}. */
    @Override
    public double doubleValue() {
        return get();
    }

    /**
     * Saves the state to a stream (that is, serializes it).
     *
     * @param stream the stream to write to
     * @serialData The current value is emitted (a {@code double}).
     */
    private void writeObject(java.io.ObjectOutputStream stream) throws java.io.IOException {
        stream.defaultWriteObject();
        stream.writeDouble(get());
    }

    /** 
     * Reconstitutes the instance from a stream (that is, deserializes it). 
     * 
     * @param stream the stream to read from
     */
    private void readObject(java.io.ObjectInputStream stream) throws java.io.IOException, ClassNotFoundException {
        stream.defaultReadObject();
        set(stream.readDouble());
    }

}
