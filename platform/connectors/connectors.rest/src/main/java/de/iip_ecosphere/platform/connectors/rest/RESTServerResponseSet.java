package de.iip_ecosphere.platform.connectors.rest;

import java.util.ArrayList;

public class RESTServerResponseSet extends RESTServerResponse {

    private ArrayList<RESTServerResponseValue> values;

    /**
     * Getter for values.
     * 
     * @return ArrayList<RESTServerResponseValue> values
     */
    public ArrayList<RESTServerResponseValue> getValues() {
        return values;
    }

    /**
     * Setter for values.
     * 
     * @param values to set
     */
    public void setValues(ArrayList<RESTServerResponseValue> values) {
        this.values = values;
    }
}
