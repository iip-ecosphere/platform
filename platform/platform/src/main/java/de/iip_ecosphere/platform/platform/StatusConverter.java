package de.iip_ecosphere.platform.platform;

import java.util.function.Function;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.services.environment.services.TransportToAasConverter;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection.SubmodelElementCollectionBuilder;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.transport.status.StatusMessage;

/**
 * A status message to AAS-list converter.
 * 
 * @author Holger Eichelberger, SSE
 */
class StatusConverter extends TransportToAasConverter<StatusMessage> {

    /**
     * Creates an instance.
     */
    public StatusConverter() {
        super(PlatformAas.NAME_SUBMODEL_STATUS, StatusMessage.STATUS_STREAM, StatusMessage.class);
    }

    @Override
    public String getAasId() {
        return AasPartRegistry.NAME_AAS;
    }

    @Override
    public String getAasUrn() {
        return AasPartRegistry.URN_AAS;
    }

    @Override
    protected void populateSubmodelElementCollection(SubmodelElementCollectionBuilder smcBuilder,
        StatusMessage data) {
        createPayloadEntries(smcBuilder, data);
    }

    @Override
    protected Function<StatusMessage, String> getSubmodelElementIdFunction() {
        return s -> "time-" + String.valueOf(System.currentTimeMillis());
    }

    @Override
    public CleanupPredicate getCleanupPredicate() {
        return (coll, borderTimestamp) -> {
            boolean delete = false;
            String idShort = coll.getIdShort();
            try {
                long entryTimestamp = Long.parseLong(idShort);
                delete = entryTimestamp < borderTimestamp;
            } catch (NumberFormatException e) {
                LoggerFactory.getLogger(PlatformAas.class).warn("Cannot delete AAS status entry {}", idShort);
            }
            return delete;
        };
    }
    
}