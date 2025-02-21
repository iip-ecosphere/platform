package de.iip_ecosphere.platform.examples.rest.mixed;

import de.iip_ecosphere.platform.examples.rest.TestServerResponseMeasurementSingleRestType;
import de.iip_ecosphere.platform.examples.rest.TestServerResponseTariffNumberRestType;
import de.iip_ecosphere.platform.examples.rest.TestServerResponseInformationRestType;
import de.iip_ecosphere.platform.examples.rest.TestServerResponseMeasurementSetRestType;

public class MachineOutputMixed {

    private TestServerResponseTariffNumberRestType tn1;
    private TestServerResponseTariffNumberRestType tn2;
    
    private TestServerResponseMeasurementSingleRestType f;
    private TestServerResponseMeasurementSingleRestType u1;
    private TestServerResponseMeasurementSingleRestType u2;
    private TestServerResponseMeasurementSingleRestType u3;
    
    private TestServerResponseMeasurementSetRestType all;
    
    private TestServerResponseInformationRestType information;

    
    /**
     * Getter for tn1.
     * 
     * @return tn1
     */
    public TestServerResponseTariffNumberRestType getTn1() {
        return tn1;
    }
    
    /**
     * Setter for tn1.
     * 
     * @param tn1 to set
     */
    public void setTn1(TestServerResponseTariffNumberRestType tn1) {
        this.tn1 = tn1;
    }
    
    /**
     * Getter for tn2.
     * 
     * @return tn2
     */
    public TestServerResponseTariffNumberRestType getTn2() {
        return tn2;
    }
    
    /**
     * Setter for tn2.
     * 
     * @param tn2 to set
     */
    public void setTn2(TestServerResponseTariffNumberRestType tn2) {
        this.tn2 = tn2;
    }
    
    /**
     * Getter for f.
     * 
     * @return f
     */
    public TestServerResponseMeasurementSingleRestType getF() {
        return f;
    }
    
    /**
     * Setter for f.
     * 
     * @param f to set
     */
    public void setF(TestServerResponseMeasurementSingleRestType frq) {
        this.f = frq;
    }
    
    /**
     * Getter for u1.
     * 
     * @return u1
     */
    public TestServerResponseMeasurementSingleRestType getU1() {
        return u1;
    }
    
    /**
     * Setter for u1.
     * 
     * @param u1 to set
     */
    public void setU1(TestServerResponseMeasurementSingleRestType u1) {
        this.u1 = u1;
    }
    
    /**
     * Getter for u2.
     * 
     * @return u2
     */
    public TestServerResponseMeasurementSingleRestType getU2() {
        return u2;
    }
    
    /**
     * Setter for u2.
     * 
     * @param u2 to set
     */
    public void setU2(TestServerResponseMeasurementSingleRestType u2) {
        this.u2 = u2;
    }
    
    /**
     * Getter for u3.
     * 
     * @return u3
     */
    public TestServerResponseMeasurementSingleRestType getU3() {
        return u3;
    }
    
    /**
     * Setter for u3.
     * 
     * @param u3 to set
     */
    public void setU3(TestServerResponseMeasurementSingleRestType u3) {
        this.u3 = u3;
    }

    /**
     * Getter for all.
     * 
     * @return all
     */
    public TestServerResponseMeasurementSetRestType getAll() {
        return all;
    }

    /**
     * Setter for all.
     * 
     * @param all to set
     */
    public void setAll(TestServerResponseMeasurementSetRestType all) {
        this.all = all;
    }

    /**
     * Getter for information.
     * 
     * @return information
     */
    public TestServerResponseInformationRestType getInformation() {
        return information;
    }

    /**
     * Setter for information.
     * 
     * @param information to set
     */
    public void setInformation(TestServerResponseInformationRestType information) {
        this.information = information;
    }
}
