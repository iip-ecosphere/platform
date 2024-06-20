package de.iip_ecosphere.platform.examples.modbusTcp;

public class ModbusCommandE {

    private Short day = null;
    private Short mounth = null;
    private Short year = null;
    private Float u12 = null;
    private Float u23 = null;
    private Float u31 = null;
//    private float u1;
//    private float u2;
//    private float u3;
//    private float frequency;
//    private float i1;
//    private float i2;
//    private float i3;
//    private float totalActivePower;
    
    /**
     * Constructor.
     */
    public ModbusCommandE() {

    }
    
    /**
     * Setter for day.
     * 
     * @param day the day to set
     */
    public void setDay(Short day) {
        this.day = day;
    }

    /**
     * Setter for month.
     * 
     * @param month the month to set
     */
    public void setMonth(Short month) {
        this.mounth = month;
    }

    /**
     * Setter for year.
     * 
     * @param year the year to set
     */
    public void setYear(Short year) {
        this.year = year;
    }

    /**
     * Setter for u12.
     * 
     * @param u12 the u12 to set
     */
    public void setU12(Float u12) {
        this.u12 = u12;
    }

    /**
     * Setter for u23.
     * 
     * @param u23 the u23 to set
     */
    public void setU23(Float u23) {
        this.u23 = u23;
    }

    /**
     * Setter for u31.
     * 7
     * @param u31 the u31 to set
     */
    public void setU31(Float u31) {
        this.u31 = u31;
    }

    /**
     * Setter for u1.
     * 
     * @param u1 the u1 to set
     */
//    public void setU1(float u1) {
//        this.u1 = u1;
//    }

    /**
     * Setter for u2.
     * 
     * @param u2 the u2 to set
     */
//    public void setU2(float u2) {
//        this.u2 = u2;
//    }

    /**
     * Setter for u3.
     * 
     * @param u3 the u3 to set
     */
//    public void setU3(float u3) {
//        this.u3 = u3;
//    }

    /**
     * Setter for frequency.
     * 
     * @param freq the frequency to set
     */
//    public void setFrequency(float freq) {
//        frequency = freq;
//    }

    /**
     * Setter for i1.
     * 
     * @param i1 the i1 to set
     */
//    public void setI1(float i1) {
//        this.i1 = i1;
//    }

    /**
     * Setter for i2.
     * 
     * @param i2 the i2 to set.
     */
//    public void setI2(float i2) {
//        this.i2 = i2;
//    }

    /**
     * Setter for i3.
     * 
     * @param i3 the i3 to set
     */
//    public void setI3(float i3) {
//        this.i3 = i3;
//    }

    /**
     * Setter for totalActivePower.
     * 
     * @param tap the totalActivePower to set
     */
//    public void setTotalActivePower(float tap) {
//        totalActivePower = tap;
//    }

    /**
     * Getter for day.
     * 
     * @return the value of day
     */
    public Short getDay() {
        return day;
    }

    /**
     * Getter for month.
     * 
     * @return the value of month
     */
    public Short getMonth() {
        return mounth;
    }

    /**
     * Getter for year.
     * 
     * @return the value of year
     */
    public Short getYear() {
        return year;
    }

    /**
     * Getter for u12.
     * 
     * @return the value of u12
     */
    public Float getU12() {
        return u12;
    }

    /**
     * Getter for u23.
     * 
     * @return the value of u23
     */
    public Float getU23() {
        return u23;
    }

    /**
     * Getter for u31.
     * 
     * @return the value of u31
     */
    public Float getU31() {
        return u31;
    }

    /**
     * Getter for u1.
     * 
     * @return the value of u1
      */
//    public float getU1() {
//        return u1;
//    }

    /**
     * Getter for u2.
     * 
     * @return the value of u2
     */
//    public float getU2() {
//        return u2;
//    }

    /**
     * Getter for u3.
     * 
     * @return the value of u3
     */
//    public float getU3() {
//        return u3;
//    }

    /**
     * Getter for frequency.
     * 
     * @return the value of frequency
     */
//    public float getFrequency() {
//        return frequency;
//    }

    /**
     * Getter for i1.
     * 
     * @return the value of i1
     */
//    public float getI1() {
//        return i1;
//    }

    /**
     * Getter for i2.
     * 
     * @return the value of i2
     */
//    public float getI2() {
//        return i2;
//    }

    /**
     * Getter for i3.
     * 
     * @return the value for i3.
     */
//    public float getI3() {
//        return i3;
//    }

    /**
     * Getter for taotalActivePower.
     * 
     * @return the value of totalActivePower
     */
//    public float getTotalActivePower() {
//        return totalActivePower;
//    }
    
    @Override
    public String toString() {
        return org.apache.commons.lang3.builder.ReflectionToStringBuilder.toString(this, 
            de.iip_ecosphere.platform.services.environment.IipStringStyle.SHORT_STRING_STYLE);
    }
}
