package de.iip_ecosphere.platform.libs.ads;

import java.io.IOException;
import java.math.BigInteger;

import com.sun.jna.Memory;

/**
 * Reads data from memory for transfer from ADS.
 * 
 * @param <T> the type of the data
 * @author Holger Eichelberger, SSE
 * @author Alexander Weber, SSE
 */
public interface ReadVisitor<T> {

    /**
     * Supplies a read visitor.
     * 
     * @param <T> the type of the data
     * @author Holger Eichelberger, SSE
     * @author Alexander Weber, SSE 
     */
    public interface ReadVisitorSupplier<T> {
     
        /**
         * Creates a read visitor.
         * 
         * @param mem the memory to operate on
         * @return the write visitor
         */
        public ReadVisitor<T> create(Memory mem);
        
    }
    
    /**
     * Reads an object.
     * 
     * @param value the object to be read (must be initialized to have the required memory sizes available)
     * @throws IOException if the value cannot be written
     */
    public void read(T value) throws IOException;
    
    /**
     * Reads a signed double value.
     * 
     * @return the value
     */
    public double readLReal();
    
    /**
     * Reads a signed float value.
     * 
     * @return the value
     */
    public float readReal();
    
    /**
     * Reads a signed long value.
     * 
     * @return the value
     */
    public long readLInt();
    
    /**
     * Reads a unsigned LInt value. 
     * 
     * @return the value
     */
    public BigInteger readULInt();
    
    /**
     * Reads a signed int value.
     * 
     * @return the value
     */
    public int readDInt();
    
    /**
     * Reads a unsigned int or DWORD value.
     * 
     * @return the value
     */
    public long readUDInt();
    
    /**
     * Reads a signed short value.
     * 
     * @return the value
     */
    public short readInt();
    
    /**
     * Reads a unsigned short or WORD value.
     * 
     * @return the value
     */
    public int readUInt();
    
    /**
     * Reads a signed byte value.
     * 
     * @return the value
     */
    public byte readSInt();
    
    /**
     * Reads a unsigned byte or BYTE value.
     * 
     * @return the value
     */
    public short readUSInt();

    /**
     * Reads a string value.
     * 
     * @return the value
     */
    public String readString();
    
    /**
     * Reads a signed double array value.
     * 
     * @param value the array to be modified
     * @param size of the array to write (may be PLC limited)
     */
    public void readLRealArray(double[] value, int size);
    
    /**
     * Reads a signed float array value.
     * 
     * @param value the array to be modified
     * @param size of the array to write (may be PLC limited)
     */
    public void readRealArray(float[] value, int size);
    
    /**
     * Reads a signed long array value.
     * 
     * @param value the array to be modified
     * @param size of the array to write (may be PLC limited)
     */
    public void readLIntArray(long[] value, int size);
    
    /**
     * Reads a signed int array value.
     * 
     * @param value the array to be modified
     * @param size of the array to write (may be PLC limited)
     */
    public void readDIntArray(int[] value, int size);
    
    /**
     * Reads a signed int array value.
     * 
     * @param value the array to be modified
     * @param size of the array to write (may be PLC limited)
     */
    public void readUDIntArray(long[] value, int size);
    
    /**
     * Reads a signed short array value.
     * 
     * @param value the array to be modified
     * @param size of the array to write (may be PLC limited)
     */
    public void readUIntArray(int[] value, int size);
    
    /**
     * Reads a signed short array value.
     * 
     * @param value the array to be modified
     * @param size of the array to write (may be PLC limited)
     */
    public void readIntArray(short[] value, int size);
    
    /**
     * Reads a signed short array value.
     * 
     * @param value the array to be modified
     * @param size of the array to write (may be PLC limited)
     */
    public void readUSIntArray(short[] value, int size);
    
    /**
     * Reads a signed byte array value.
     * 
     * @param value the array to be modified
     * @param size of the array to write (may be PLC limited)
     */
    public void readSIntArray(byte[] value, int size);

    /**
     * Reads a signed byte array value.
     * 
     * @param value the array to be modified
     * @param size of the array to write (may be PLC limited)
     */
    public void readULIntArray(BigInteger[] value, int size);    

}