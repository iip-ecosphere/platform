package de.iip_ecosphere.platform.examples.energy.aggregator.source;

import de.iip_ecosphere.platform.examples.energy.aggregator.model.MachineEnergyMeasurement;
import de.iip_ecosphere.platform.examples.energy.aggregator.model.MeasurementQuality;
import de.iip_ecosphere.platform.examples.energy.aggregator.model.MeasurementType;

import net.wimpi.modbus.io.ModbusTCPTransaction;
import net.wimpi.modbus.msg.ReadMultipleRegistersRequest;
import net.wimpi.modbus.msg.ReadMultipleRegistersResponse;
import net.wimpi.modbus.net.TCPMasterConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * ModbusTCP ingestor for the Siemens Sentron PAC (PAC4200).
 *
 * <p>Reads holding registers (Modbus function 03) at 200 ms intervals,
 * decodes FLOAT32 total active power (registers 65–66) and FLOAT64 imported
 * active energy tariff 1 (registers 801–804), and emits one
 * {@link MachineEnergyMeasurement} per value.</p>
 *
 * <p>Register map: IFW Telegraf configuration, byte order big-endian
 * (ABCD / ABCDEFGH).</p>
 */
public class SentronModbusIngestor {

    private static final Logger LOG = LoggerFactory.getLogger(SentronModbusIngestor.class);

    /** FLOAT32, 2 registers, W — register 65. */
    private static final int REG_TOTAL_ACT_POWER = 65;
    /** FLOAT64, 4 registers, Wh — register 801. Verify unit with one real reading. */
    private static final int REG_TOTAL_ACT_ENERGY = 801;

    private final String sourceId;   // "ShellyPro3EM" / "SentronPAC" — the meter
    private final String deviceId;   // "LatheNEF400" / "MarkingLaser" — the machine
    private final String host;
    private final int port;
    private final int unitId;
    private final int intervalMs;
    private final Consumer<MachineEnergyMeasurement> consumer;
    private final Consumer<Throwable> errorConsumer;

    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> task;
    private volatile boolean running = false;
    private volatile boolean connected = false;

    public SentronModbusIngestor(String sourceId,
                                 String deviceId,
                                 String host,
                                 int port,
                                 int unitId,
                                 int intervalMs,
                                 Consumer<MachineEnergyMeasurement> consumer,
                                 Consumer<Throwable> errorConsumer) {
        this.sourceId = sourceId;
        this.deviceId = deviceId;
        this.host = host;
        this.port = port;
        this.unitId = unitId;
        this.intervalMs = intervalMs;
        this.consumer = consumer;
        this.errorConsumer = errorConsumer;
    }

    // -----------------------------------------------------------------
    // Lifecycle
    // -----------------------------------------------------------------

    /** Single probe connection; the real polling opens its own connection per cycle. */
    public void connect() throws Exception {
        TCPMasterConnection probe = openConnection();
        try {
            connected = true;
            LOG.info("[{}] ModbusTCP reachable at {}:{}", deviceId, host, port);
        } finally {
            probe.close();
        }
    }

    public void start() {
        running = true;
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "sentron-modbus-" + deviceId);
            t.setDaemon(true);
            return t;
        });
        task = scheduler.scheduleAtFixedRate(this::pollOnce, 0, intervalMs, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        running = false;
        if (task != null) task.cancel(false);
        if (scheduler != null) scheduler.shutdown();
    }

    public void disconnect() {
        stop();
        connected = false;
    }

    public boolean isConnected() {
        return connected;
    }

    // -----------------------------------------------------------------
    // Polling
    // -----------------------------------------------------------------

    private void pollOnce() {
        if (!running) return;
        TCPMasterConnection con = null;
        try {
            con = openConnection();
            connected = true;
            Instant now = Instant.now();

            int[] p = readHolding(con, REG_TOTAL_ACT_POWER, 2);
            float power = decodeFloat32(p[0], p[1]);
            consumer.accept(new MachineEnergyMeasurement(
                sourceId, deviceId,
                MeasurementType.CURRENT_POWER, power, "W",
                now, MeasurementQuality.VALID));

            int[] e = readHolding(con, REG_TOTAL_ACT_ENERGY, 4);
            double energy = decodeFloat64(e[0], e[1], e[2], e[3]);
            consumer.accept(new MachineEnergyMeasurement(
                sourceId, deviceId,
                MeasurementType.CUMULATIVE_ENERGY, energy, "Wh",
                now, MeasurementQuality.VALID));

        } catch (Exception e) {
            connected = false;
            LOG.warn("[{}] Modbus poll failed", deviceId, e);
            emitBadQuality();
            notifyError(e);
        } finally {
            if (con != null) {
                con.close();
            }
        }
    }

    private TCPMasterConnection openConnection() throws Exception {
        TCPMasterConnection con = new TCPMasterConnection(InetAddress.getByName(host));
        con.setPort(port);
        con.connect();
        return con;
    }

    /** Reads {@code count} holding registers (function 03) starting at {@code ref}. */
    private int[] readHolding(TCPMasterConnection con, int ref, int count) throws Exception {
        ModbusTCPTransaction trans = new ModbusTCPTransaction(con);
        ReadMultipleRegistersRequest req = new ReadMultipleRegistersRequest(ref, count);
        req.setUnitID(unitId);
        trans.setRequest(req);
        trans.execute();

        ReadMultipleRegistersResponse res =
            (ReadMultipleRegistersResponse) trans.getResponse();
        int[] values = new int[count];
        for (int i = 0; i < count; i++) {
            values[i] = res.getRegisterValue(i);
        }
        return values;
    }

    /** Emits UNAVAILABLE for both measurement types on a failed poll. */
    private void emitBadQuality() {
        Instant now = Instant.now();
        consumer.accept(new MachineEnergyMeasurement(
            sourceId, deviceId,
            MeasurementType.CURRENT_POWER, 0.0, "W",
            now, MeasurementQuality.UNAVAILABLE));
        consumer.accept(new MachineEnergyMeasurement(
            sourceId, deviceId,
            MeasurementType.CUMULATIVE_ENERGY, 0.0, "Wh",
            now, MeasurementQuality.UNAVAILABLE));
    }
    
    
    /** Test hook — do not use in production. */
    void emitBadQualityForTest() {
        emitBadQuality();
    }

    private void notifyError(Throwable t) {
        if (errorConsumer != null) errorConsumer.accept(t);
    }

    // -----------------------------------------------------------------
    // Decoders — package-private and static so they can be unit-tested
    // without a live device.
    // -----------------------------------------------------------------

    /** Big-endian FLOAT32 from two 16-bit registers. */
    static float decodeFloat32(int r0, int r1) {
        return Float.intBitsToFloat(((r0 & 0xFFFF) << 16) | (r1 & 0xFFFF));
    }

    /** Big-endian FLOAT64 from four 16-bit registers. */
    static double decodeFloat64(int r0, int r1, int r2, int r3) {
        long bits = ((long) (r0 & 0xFFFF) << 48)
                  | ((long) (r1 & 0xFFFF) << 32)
                  | ((long) (r2 & 0xFFFF) << 16)
                  |  (long) (r3 & 0xFFFF);
        return Double.longBitsToDouble(bits);
    }
}
