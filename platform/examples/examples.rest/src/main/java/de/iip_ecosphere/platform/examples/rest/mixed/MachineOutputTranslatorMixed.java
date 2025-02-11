package de.iip_ecosphere.platform.examples.rest.mixed;

import java.io.IOException;

import de.iip_ecosphere.platform.connectors.model.AbstractModelAccess;
import de.iip_ecosphere.platform.connectors.model.ModelAccess;

import de.iip_ecosphere.platform.connectors.rest.RESTEndpointMap;
import de.iip_ecosphere.platform.connectors.rest.RESTItem;
import de.iip_ecosphere.platform.connectors.types.AbstractConnectorOutputTypeTranslator;
import de.iip_ecosphere.platform.examples.rest.TestServerResponseMeasurementSingle;
import de.iip_ecosphere.platform.examples.rest.TestServerResponseTariffNumber;
import de.iip_ecosphere.platform.examples.rest.TestServerResponseInformation;
import de.iip_ecosphere.platform.examples.rest.TestServerResponseMeasurementSet;

public class MachineOutputTranslatorMixed<S> extends AbstractConnectorOutputTypeTranslator<S, MachineOutputMixed> {

    private boolean withNotifications;
    private Class<? extends S> sourceType;

    /**
     * Constructor.
     * 
     * @param withNotifications
     * @param sourceType
     */
    public MachineOutputTranslatorMixed(boolean withNotifications, Class<? extends S> sourceType) {
        this.withNotifications = withNotifications;
        this.sourceType = sourceType;
    }

    @Override
    public void initializeModelAccess() throws IOException {
        ModelAccess access = getModelAccess();
        access.useNotifications(withNotifications);

    }

    @Override
    public Class<? extends S> getSourceType() {
        // TODO Auto-generated method stub
        return sourceType;
    }

    @Override
    public Class<? extends MachineOutputMixed> getTargetType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MachineOutputMixed to(S source) throws IOException {

        if (source != null) {

            RESTItem item = (RESTItem) source;
            RESTEndpointMap map = item.getEndpointMap();

            AbstractModelAccess access = (AbstractModelAccess) getModelAccess();

            MachineOutputMixed result = new MachineOutputMixed();
            result.setTn1((TestServerResponseTariffNumber) access.get("tn1"));
            result.setTn2((TestServerResponseTariffNumber) access.get("tn2"));
            result.setF((TestServerResponseMeasurementSingle) access.get("f"));
            result.setU1((TestServerResponseMeasurementSingle) access.get("u1"));
            result.setU2((TestServerResponseMeasurementSingle) access.get("u2"));
            result.setU3((TestServerResponseMeasurementSingle) access.get("u3"));
            result.setI1(((TestServerResponseMeasurementSet) 
                    access.get("all")).getItems()[map.get("all").getItemIndex("i1")]);
            result.setI2(((TestServerResponseMeasurementSet) 
                    access.get("all")).getItems()[map.get("all").getItemIndex("i2")]);
            result.setI3(((TestServerResponseMeasurementSet) 
                    access.get("all")).getItems()[map.get("all").getItemIndex("i3")]);
            result.setRoot1(((TestServerResponseInformation) 
                    access.get("information")).getRootItems()[map.get("information").getItemIndex("root1")]);
            result.setRoot2(((TestServerResponseInformation) 
                    access.get("information")).getRootItems()[map.get("information").getItemIndex("root2")]);
            result.setInfo1(((TestServerResponseInformation) 
                    access.get("information")).getInfoItems()[map.get("information").getItemIndex("info1")]);
            result.setInfo2(((TestServerResponseInformation) 
                    access.get("information")).getInfoItems()[map.get("information").getItemIndex("info2")]);

            return result;

        }

        return null;
    }

}
