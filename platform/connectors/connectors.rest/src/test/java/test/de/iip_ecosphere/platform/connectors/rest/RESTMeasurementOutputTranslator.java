package test.de.iip_ecosphere.platform.connectors.rest;

import java.io.IOException;

import de.iip_ecosphere.platform.connectors.model.AbstractModelAccess;
import de.iip_ecosphere.platform.connectors.model.ModelAccess;
import de.iip_ecosphere.platform.connectors.model.ModelInputConverter;
import de.iip_ecosphere.platform.connectors.types.AbstractConnectorOutputTypeTranslator;

public class RESTMeasurementOutputTranslator<S>
        extends AbstractConnectorOutputTypeTranslator<S, RESTMeasurement> {

    private boolean withNotifications;
    private Class<? extends S> sourceType;

    /**
     * Constructor.
     * 
     * @param withNotifications
     * @param sourceType
     */
    public RESTMeasurementOutputTranslator(boolean withNotifications, Class<? extends S> sourceType) {
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
        return sourceType;
    }

    @Override
    public Class<? extends RESTMeasurement> getTargetType() {
        return RESTMeasurement.class;
    }

    @Override
    public RESTMeasurement to(S source) throws IOException {
        AbstractModelAccess access = (AbstractModelAccess) getModelAccess();
        final ModelInputConverter inConverter = access.getInputConverter();

        RESTMeasurement result = new RESTMeasurement();
        Object stringVal = access.get("String");
        result.setStringValue(inConverter.toString(stringVal));
        result.setShortValue(Short.valueOf(access.get("Short").toString()));
        result.setIntValue(Integer.valueOf(access.get("Integer").toString()));
        result.setLongValue(Long.valueOf(access.get("Long").toString()));
        result.setFloatValue(Float.valueOf(access.get("Float").toString()));
        result.setDoubleValue(Double.valueOf(access.get("Double").toString()));

        return result;
    }

}
