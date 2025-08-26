package de.iip_ecosphere.platform.support.json;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * An iterable JSON structure. Abstraction based on Jsoniter.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface JsonIterator {

    /**
     * JSON value types.
     * 
     * @author Holger Eichelberger, SSE
     */
    public enum ValueType {
        INVALID,
        STRING,
        NUMBER,
        NULL,
        BOOLEAN,
        ARRAY,
        OBJECT
    }
    
    /**
     * Element/entry iterator.
     * 
     * @author Jsoniter
     */
    public interface EntryIterator {
        
        /**
         * Is there a next entry?
         * 
         * @return {@code true} for next entry, {@code false} for none
         */
        public boolean next();

        /**
         * Returns the next entry key.
         * 
         * @return the key
         */
        public String key();

        /**
         * Returns the next entry value.
         * 
         * @return the key
         */
        public JsonIterator value();
        
    }

    /**
     * Returns the value type.
     * 
     * @return the value type
     */
    public ValueType valueType();

    /**
     * Does the given {@code key} exist in the underlying JSON structure.
     * 
     * @param key the key
     * @return {@code true} if the key exist, {@code false} else
     */
    public boolean containsKey(String key);
    
    /**
     * Returns the value of the given {@code key}. Use either {@link #entries() iterator-based access} or name-based 
     * access.
     * 
     * @param key the key
     * @return the value, may be <b>null</b> for none
     */
    public JsonIterator get(String key);    

    /**
     * Slices {@code data} so that the nested part of {@code iter} remains.
     * 
     * @param data the data
     * @param iter the iterator to slice for
     * @return the sliced part of {@code data}
     * @throws IOException if slicing fails
     */
    public byte[] slice(byte[] data, JsonIterator iter) throws IOException;
    
    /**
     * Returns the size of the underlying structure.
     * 
     * @return the size
     */
    public int size();
    
    /**
     * Returns the i-th element in the underlying array structure.
     * 
     * @param index the 0-based index
     * @return the element
     * @throws IndexOutOfBoundsException if not in rage, see {@link #size()}
     */
    public JsonIterator get(int index);

    /**
     * Returns all entries. Use either iterator-based access or {@link #get(String) name-based access}.
     * 
     * @return all entries
     */
    public EntryIterator entries();

    /**
     * Returns any key, in particular if {@link size} is {@code 1}, the first/only key.
     * Works only on objects.
     * 
     * @return any key, may be <b>null</b>
     */
    public String getAnyKey();
    
    /**
     * Turns this iterator to a string value.
     * 
     * @return the string value
     * @throws IOException if the actual value is not a string
     */
    public String toStringValue() throws IOException;

    /**
     * Turns this iterator to a double value.
     * 
     * @return the double value
     * @throws IOException if the actual value is not a double
     */
    public double toDoubleValue() throws IOException;

    /**
     * Turns this iterator to a float value.
     * 
     * @return the float value
     * @throws IOException if the actual value is not a float
     */
    public float toFloatValue() throws IOException;

    /**
     * Turns this iterator to a long value.
     * 
     * @return the long value
     * @throws IOException if the actual value is not a long
     */
    public long toLongValue() throws IOException;

    /**
     * Turns this iterator to a boolean value.
     * 
     * @return the boolean value
     * @throws IOException if the actual value is not a boolean
     */
    public boolean toBooleanValue() throws IOException;

    /**
     * Turns this iterator to an int value.
     * 
     * @return the int value
     * @throws IOException if the actual value is not an int
     */
    public int toIntValue() throws IOException;
    
    /**
     * Turns this iterator to a big integer value.
     * 
     * @return the big integer value
     * @throws IOException if the actual value is not a big integer
     */
    public BigInteger toBigIntegerValue() throws IOException;

    /**
     * Turns this iterator to a big decimal value.
     * 
     * @return the big decimal value
     * @throws IOException if the actual value is not a big decimal
     */
    public BigDecimal toBigDecimalValue() throws IOException;
    
}
