package de.iip_ecosphere.platform.examples.rest.mixed;

import de.iip_ecosphere.platform.examples.rest.TestServerResponseMeasurementSingle;
import de.iip_ecosphere.platform.examples.rest.TestServerResponseTariffNumber;
import de.iip_ecosphere.platform.examples.rest.TestServerResponseInformation;
import de.iip_ecosphere.platform.examples.rest.TestServerResponseMeasurementSet;

public class MachineOutputMixed {

    private TestServerResponseTariffNumber tn1;
    private TestServerResponseTariffNumber tn2;
    
    private TestServerResponseMeasurementSingle f;
    private TestServerResponseMeasurementSingle u1;
    private TestServerResponseMeasurementSingle u2;
    private TestServerResponseMeasurementSingle u3;
    
    private TestServerResponseMeasurementSet all;
    
    private TestServerResponseInformation information;

    
    /**
     * Getter for tn1.
     * 
     * @return tn1
     */
    public TestServerResponseTariffNumber getTn1() {
        return tn1;
    }
    
    /**
     * Setter for tn1.
     * 
     * @param tn1 to set
     */
    public void setTn1(TestServerResponseTariffNumber tn1) {
        this.tn1 = tn1;
    }
    
    /**
     * Getter for tn2.
     * 
     * @return tn2
     */
    public TestServerResponseTariffNumber getTn2() {
        return tn2;
    }
    
    /**
     * Setter for tn2.
     * 
     * @param tn2 to set
     */
    public void setTn2(TestServerResponseTariffNumber tn2) {
        this.tn2 = tn2;
    }
    
    /**
     * Getter for f.
     * 
     * @return f
     */
    public TestServerResponseMeasurementSingle getF() {
        return f;
    }
    
    /**
     * Setter for f.
     * 
     * @param f to set
     */
    public void setF(TestServerResponseMeasurementSingle frq) {
        this.f = frq;
    }
    
    /**
     * Getter for u1.
     * 
     * @return u1
     */
    public TestServerResponseMeasurementSingle getU1() {
        return u1;
    }
    
    /**
     * Setter for u1.
     * 
     * @param u1 to set
     */
    public void setU1(TestServerResponseMeasurementSingle u1) {
        this.u1 = u1;
    }
    
    /**
     * Getter for u2.
     * 
     * @return u2
     */
    public TestServerResponseMeasurementSingle getU2() {
        return u2;
    }
    
    /**
     * Setter for u2.
     * 
     * @param u2 to set
     */
    public void setU2(TestServerResponseMeasurementSingle u2) {
        this.u2 = u2;
    }
    
    /**
     * Getter for u3.
     * 
     * @return u3
     */
    public TestServerResponseMeasurementSingle getU3() {
        return u3;
    }
    
    /**
     * Setter for u3.
     * 
     * @param u3 to set
     */
    public void setU3(TestServerResponseMeasurementSingle u3) {
        this.u3 = u3;
    }

    /**
     * Getter for all.
     * 
     * @return all
     */
    public TestServerResponseMeasurementSet getAll() {
        return all;
    }

    /**
     * Setter for all.
     * 
     * @param all to set
     */
    public void setAll(TestServerResponseMeasurementSet all) {
        this.all = all;
    }

    /**
     * Getter for information.
     * 
     * @return information
     */
    public TestServerResponseInformation getInformation() {
        return information;
    }

    /**
     * Setter for information.
     * 
     * @param information to set
     */
    public void setInformation(TestServerResponseInformation information) {
        this.information = information;
    }
}
