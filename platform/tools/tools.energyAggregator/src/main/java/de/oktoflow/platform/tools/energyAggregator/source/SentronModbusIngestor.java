package de.oktoflow.platform.tools.energyAggregator.source;

import java.io.IOException;
import java.net.InetAddress;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.oktoflow.platform.tools.energyAggregator.model.MachineEnergyMeasurement;
import de.oktoflow.platform.tools.energyAggregator.model.MeasurementQuality;
import de.oktoflow.platform.tools.energyAggregator.model.MeasurementType;
import net.wimpi.modbus.ModbusException;
import net.wimpi.modbus.io.ModbusTCPTransaction;
import net.wimpi.modbus.msg.ReadMultipleRegistersRequest;
import net.wimpi.modbus.msg.ReadMultipleRegistersResponse;
import net.wimpi.modbus.net.TCPMasterConnection;

/**
 * ModbusTCP source for the Siemens SENTRON PAC4200. Polls holding registers (function 03) periodically, decodes
 * the total active power (FLOAT32, registers 65-66, W) and the imported active energy of tariff 1 (FLOAT64,
 * registers 801-804, Wh) and emits one measurement per value. Register map and big-endian byte order as in the
 * IFW Telegraf configuration. If the device cannot be read, both values are emitted as
 * {@link MeasurementQuality#UNAVAILABLE}.
 * 
 * @author Kodjo Gilles Adjroloh
 */
public class SentronModbusIngestor {

    /**
     * Register of the total active power (FLOAT32, 2 registers, W).
     */
    private static final int REG_TOTAL_ACT_POWER = 65;

    /**
     * Register of the imported active energy, tariff 1 (FLOAT64, 4 registers, Wh).
     */
    private static final int REG_TOTAL_ACT_ENERGY = 801;

    /**
     * Number of registers of a FLOAT32 value.
     */
    private static final int FLOAT32_REGISTERS = 2;

    /**
     * Number of registers of a FLOAT64 value.
     */
    private static final int FLOAT64_REGISTERS = 4;

    /**
     * Mask for a 16-bit register value.
     */
    private static final int REGISTER_MASK = 0xFFFF;

    /**
     * Number of bits of a register.
     */
    private static final int REGISTER_BITS = 16;

    /**
     * The logger of this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(SentronModbusIngestor.class);

    /**
     * The source (meter) id.
     */
    private final String sourceId;

    /**
     * The device (machine) id.
     */
    private final String deviceId;

    /**
     * The Modbus host.
     */
    private final String host;

    /**
     * The Modbus port.
     */
    private final int port;

    /**
     * The Modbus unit id.
     */
    private final int unitId;

    /**
     * The polling interval in ms.
     */
    private final int intervalMs;

    /**
     * Receives the measurements.
     */
    private final Consumer<MachineEnergyMeasurement> consumer;

    /**
     * Receives errors, may be <b>null</b>.
     */
    private final Consumer<Throwable> errorConsumer;

    /**
     * The polling scheduler, <b>null</b> if not started.
     */
    private ScheduledExecutorService scheduler;

    /**
     * The polling task, <b>null</b> if not started.
     */
    private ScheduledFuture<?> task;

    /**
     * Whether polling is active.
     */
    private volatile boolean running;

    /**
     * Whether the last connection attempt succeeded.
     */
    private volatile boolean connected;

    // checkstyle: stop parameter number check

    /**
     * Creates a SENTRON source.
     * 
     * @param sourceId the source (meter) id, e.g., "SentronPAC"
     * @param deviceId the device (machine) id, e.g., "MarkingLaser"
     * @param host the Modbus host
     * @param port the Modbus port
     * @param unitId the Modbus unit id
     * @param intervalMs the polling interval in ms
     * @param consumer receives the measurements
     * @param errorConsumer receives errors, may be <b>null</b>
     */
    public SentronModbusIngestor(String sourceId, String deviceId, String host, int port, int unitId,
        int intervalMs, Consumer<MachineEnergyMeasurement> consumer, Consumer<Throwable> errorConsumer) {
        this.sourceId = sourceId;
        this.deviceId = deviceId;
        this.host = host;
        this.port = port;
        this.unitId = unitId;
        this.intervalMs = intervalMs;
        this.consumer = consumer;
        this.errorConsumer = errorConsumer;
    }

    // checkstyle: resume parameter number check

    /**
     * Checks that the device is reachable by opening and closing a connection. Polling opens an own connection
     * per cycle.
     * 
     * @throws IOException if the device is not reachable
     */
    public void connect() throws IOException {
        TCPMasterConnection probe = openConnection();
        probe.close();
        connected = true;
        LOG.info("[{}] ModbusTCP reachable at {}:{}", deviceId, host, port);
    }

    /**
     * Starts periodic polling.
     */
    public void start() {
        running = true;
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "sentron-modbus-" + deviceId);
            thread.setDaemon(true);
            return thread;
        });
        task = scheduler.scheduleAtFixedRate(this::scheduledPoll, 0, intervalMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Stops periodic polling.
     */
    public void stop() {
        running = false;
        if (null != task) {
            task.cancel(false);
        }
        if (null != scheduler) {
            scheduler.shutdown();
        }
    }

    /**
     * Returns whether the last connection attempt succeeded.
     * 
     * @return {@code true} if connected, {@code false} else
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * Polls if active. Shields the scheduler, as an unexpected runtime exception would silently cancel all
     * further executions of the periodic task.
     */
    private void scheduledPoll() {
        if (running) {
            try {
                poll();
                // checkstyle: stop exception type check
            } catch (RuntimeException e) {
                // checkstyle: resume exception type check
                LOG.error("[{}] Unexpected error while polling: {}", deviceId, e.getMessage(), e);
            }
        }
    }

    /**
     * Performs a single poll: reads power and energy and emits them, or emits both as unavailable if the
     * device cannot be read.
     */
    public void poll() {
        TCPMasterConnection con = null;
        try {
            con = openConnection();
            connected = true;
            Instant now = Instant.now();
            int[] power = readHolding(con, REG_TOTAL_ACT_POWER, FLOAT32_REGISTERS);
            consumer.accept(new MachineEnergyMeasurement(sourceId, deviceId, MeasurementType.CURRENT_POWER, 
                decodeFloat32(power[0], power[1]), "W", now, MeasurementQuality.VALID));
            int[] energy = readHolding(con, REG_TOTAL_ACT_ENERGY, FLOAT64_REGISTERS);
            consumer.accept(new MachineEnergyMeasurement(sourceId, deviceId, MeasurementType.CUMULATIVE_ENERGY, 
                decodeFloat64(energy[0], energy[1], energy[2], energy[3]), "Wh", now, MeasurementQuality.VALID));
        } catch (IOException | ModbusException e) {
            connected = false;
            LOG.warn("[{}] Modbus poll failed: {}", deviceId, e.getMessage());
            emitUnavailable();
            notifyError(e);
        } finally {
            if (null != con) {
                con.close();
            }
        }
    }

    /**
     * Opens a connection to the device.
     * 
     * @return the connection
     * @throws IOException if the connection cannot be established
     */
    private TCPMasterConnection openConnection() throws IOException {
        TCPMasterConnection con = new TCPMasterConnection(InetAddress.getByName(host));
        con.setPort(port);
        try {
            con.connect();
            // checkstyle: stop exception type check
        } catch (Exception e) { // jamod declares connect() with "throws Exception"
            // checkstyle: resume exception type check
            throw new IOException("Cannot connect to " + host + ":" + port, e);
        }
        return con;
    }

    /**
     * Reads holding registers (function 03).
     * 
     * @param con the connection
     * @param ref the first register
     * @param count the number of registers
     * @return the register values
     * @throws ModbusException if reading fails
     */
    private int[] readHolding(TCPMasterConnection con, int ref, int count) throws ModbusException {
        ModbusTCPTransaction trans = new ModbusTCPTransaction(con);
        ReadMultipleRegistersRequest req = new ReadMultipleRegistersRequest(ref, count);
        req.setUnitID(unitId);
        trans.setRequest(req);
        trans.execute();
        ReadMultipleRegistersResponse res = (ReadMultipleRegistersResponse) trans.getResponse();
        int[] values = new int[count];
        for (int i = 0; i < count; i++) {
            values[i] = res.getRegisterValue(i);
        }
        return values;
    }

    /**
     * Emits both measurement types as unavailable.
     */
    private void emitUnavailable() {
        Instant now = Instant.now();
        consumer.accept(new MachineEnergyMeasurement(sourceId, deviceId, MeasurementType.CURRENT_POWER, 0.0, "W", 
            now, MeasurementQuality.UNAVAILABLE));
        consumer.accept(new MachineEnergyMeasurement(sourceId, deviceId, MeasurementType.CUMULATIVE_ENERGY, 0.0, 
            "Wh", now, MeasurementQuality.UNAVAILABLE));
    }

    /**
     * Passes an error to the error consumer, if present.
     * 
     * @param error the error
     */
    private void notifyError(Throwable error) {
        if (null != errorConsumer) {
            errorConsumer.accept(error);
        }
    }

    /**
     * Decodes a big-endian FLOAT32 from two 16-bit registers.
     * 
     * @param r0 the high register
     * @param r1 the low register
     * @return the decoded value
     */
    public static float decodeFloat32(int r0, int r1) {
        return Float.intBitsToFloat(((r0 & REGISTER_MASK) << REGISTER_BITS) | (r1 & REGISTER_MASK));
    }

    /**
     * Decodes a big-endian FLOAT64 from four 16-bit registers.
     * 
     * @param r0 the highest register
     * @param r1 the second register
     * @param r2 the third register
     * @param r3 the lowest register
     * @return the decoded value
     */
    public static double decodeFloat64(int r0, int r1, int r2, int r3) {
        long bits = 0;
        for (int register : new int[] {r0, r1, r2, r3}) {
            bits = (bits << REGISTER_BITS) | (register & REGISTER_MASK);
        }
        return Double.longBitsToDouble(bits);
    }

}
