package de.iip_ecosphere.platform.platform;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.services.environment.services.TransportToAasConverter;
import de.iip_ecosphere.platform.support.aas.Type;
import de.iip_ecosphere.platform.support.aas.SubmodelElement;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection.SubmodelElementCollectionBuilder;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.support.iip_aas.AasUtils;
import de.iip_ecosphere.platform.transport.status.ActionType;
import de.iip_ecosphere.platform.transport.status.ActionTypes;
import de.iip_ecosphere.platform.transport.status.ComponentType;
import de.iip_ecosphere.platform.transport.status.ComponentTypes;
import de.iip_ecosphere.platform.transport.status.StatusMessage;

/**
 * A status message to AAS-list converter.
 * 
 * @author Holger Eichelberger, SSE
 */
public class StatusConverter extends TransportToAasConverter<StatusMessage> {

    public static final StatusConverter INSTANCE = new StatusConverter();
    private static final String IDSHORT_PREFIX = "time_";
    private AtomicInteger counter = new AtomicInteger(0);
    
    /**
     * Creates an instance.
     */
    public StatusConverter() {
        super(PlatformAas.NAME_SUBMODEL_STATUS, StatusMessage.STATUS_STREAM, StatusMessage.class);
        addConverter(ActionType.class, new TypeConverter(Type.STRING, STRING_CONVERTER));
        addConverter(ActionTypes.class, new TypeConverter(Type.STRING, ENUM_NAME_CONVERTER));
        addConverter(ComponentType.class, new TypeConverter(Type.STRING, STRING_CONVERTER));
        addConverter(ComponentTypes.class, new TypeConverter(Type.STRING, ENUM_NAME_CONVERTER));
    }
    
    /*@Override
    protected boolean isAasEnabled() { // TODO preliminary, AAS performance problems
        return false;
    }*/

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
        return s -> {
            int c = Math.abs(counter.incrementAndGet()); // something temporarily unique, may also be random
            return IDSHORT_PREFIX + c + "_" + String.valueOf(System.currentTimeMillis());
        };
    }

    @Override
    public CleanupPredicate getCleanupPredicate() {
        return (coll, borderTimestamp) -> {
            long entryTimestamp = getTimestamp(coll, true);
            return entryTimestamp > 0 && entryTimestamp < borderTimestamp;
        };
    }

    @Override
    protected void doWatch(SubmodelElementCollection coll, long lastRun) {
        long timestamp = StatusConverter.getTimestamp(coll, false);
        if (timestamp > 0 && timestamp > lastRun) {
            String action = AasUtils.getPropertyValueAsStringSafe(coll, "action", "");
            String id = AasUtils.getPropertyValueAsStringSafe(coll, "id", "");
            String devId = AasUtils.getPropertyValueAsStringSafe(coll, "deviceId", "");
            if (devId.length() > 0) {
                System.out.print("On " + devId + " ");
            }
            System.out.println(id + " " + action);
        }
    }
    
    /**
     * Returns the timestamp of the given element.
     * 
     * @param elt the element to take the timestamp from.
     * @param logError log a timestamp conversion error
     * @return negative if there is no timestamp, the timestamp else
     */
    public static long getTimestamp(SubmodelElement elt, boolean logError) {
        long result = -1;
        String idShort = elt.getIdShort();
        String id = idShort;
        if (id.startsWith(IDSHORT_PREFIX)) {
            id = id.substring(id.lastIndexOf('_') + 1);
        }
        try {
            result = Long.parseLong(id);
        } catch (NumberFormatException e) {
            if (logError) {
                LoggerFactory.getLogger(PlatformAas.class).warn(
                    "Cannot check AAS status entry {} ({}): {}", idShort, id, e.getMessage());
            }
        }
        return result;
    }
    
}