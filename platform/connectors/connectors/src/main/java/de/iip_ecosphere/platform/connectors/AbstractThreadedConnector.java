package de.iip_ecosphere.platform.connectors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Supplier;

import de.iip_ecosphere.platform.connectors.model.ModelAccess;
import de.iip_ecosphere.platform.connectors.types.ProtocolAdapter;

/**
 * Provides a reusable base for connectors that require a {@link ModelAccess} instance per calling
 * thread. Extends {@link AbstractConnector} implementation using the
 * {@link ProtocolAdapter}. Call {@link #setModelAccessSupplier(Supplier)} (and implicitly 
 * {@link #configureModelAccess(ModelAccess)}) before {@link #connect(ConnectorParameter)}. 
 * Calls {@link ModelAccess#dispose()}.
 * 
 * @param <O>  the output type from the underlying machine/platform
 * @param <I>  the input type to the underlying machine/platform
 * @param <CO> the output type of the connector
 * @param <CI> the input type of the connector
 * @param <M> the model access type of the connector
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractThreadedConnector<O, I, CO, CI, M extends ModelAccess> 
    extends AbstractConnector<O, I, CO, CI> {

    private Timer timer;
    private Supplier<M> modelAccessSupplier;
    private Map<Thread, M> accesses = new HashMap<>();
    private TimerTask cleanupTask;
    private int cleanupPeriod = 5000;
    
    /**
     * Creates an instance and installs the protocol adapter(s) with a default
     * selector for the first adapter. For integration compatibility, connector
     * constructors are supposed to accept a var-arg parameter for adapters.
     * 
     * @param adapter the protocol adapter(s)
     * @throws IllegalArgumentException if {@code adapter} is <b>null</b> or empty
     *                                  or adapters are <b>null</b>
     * @see #setModelAccessSupplier(Supplier)
     */
    @SafeVarargs
    protected AbstractThreadedConnector(ProtocolAdapter<O, I, CO, CI>... adapter) {
        super(adapter);
    }

    /**
     * Creates an instance and installs the protocol adapter(s). For integration
     * compatibility, connector constructors are supposed to accept a var-arg
     * parameter for adapters.
     * 
     * @param selector the adapter selector (<b>null</b> leads to a default selector
     *                 for the first adapter)
     * @param adapter  the protocol adapter(s)
     * @throws IllegalArgumentException if {@code adapter} is <b>null</b> or empty
     *                                  or adapters are <b>null</b>
     * @see #setModelAccessSupplier(Supplier)
     */
    @SafeVarargs
    protected AbstractThreadedConnector(AdapterSelector<O, I, CO, CI> selector, 
        ProtocolAdapter<O, I, CO, CI>... adapter) {
        super(selector, adapter);
    }

    /**
     * Sets the model access supplier. Calls {@link #configureModelAccess(ModelAccess)}
     * 
     * @param modelAccessSupplier supplier for model access instances, ignored if <b>null</b>
     */
    protected void setModelAccessSupplier(Supplier<M> modelAccessSupplier) {
        if (null != modelAccessSupplier) {
            this.modelAccessSupplier = modelAccessSupplier;
            configureModelAccess(modelAccessSupplier.get());
        }
    }
    
    /**
     * Returns the model access supplier.
     * 
     * @return the supplier for model access instances (may be <b>null</b> if {@link #setModelAccessSupplier(Supplier)}
     *     was not called with an appropriate instance before
     */
    protected Supplier<M> getModelAccessSupplier() {
        return modelAccessSupplier;
    }
    
    /**
     * Returns/allocates a model access object for {@code thread}.
     * 
     * @param thread the thread
     * @return the model access object
     */
    private M obtainModelAccess(Thread thread) {
        M acc = accesses.get(thread);
        if (null == acc) {
            acc = modelAccessSupplier.get();
            accesses.put(thread, acc);
        }
        return acc;
    }

    @Override
    protected ProtocolAdapter<O, I, CO, CI> configureAdapter(ProtocolAdapter<O, I, CO, CI> adapter) {
        if (modelAccessSupplier != null) {
            adapter.setModelAccess(obtainModelAccess(Thread.currentThread()));
        }
        return adapter;
    }

    @Override
    public void connect(ConnectorParameter params) throws IOException {
        cleanupTask = new TimerTask() {
            
            @Override
            public void run() {
                // alternative: ThreadLocal, but per connector - be careful with static
                List<Thread> threads = new ArrayList<>(accesses.keySet());
                for (Thread t : threads) {
                    if (!t.isAlive()) {
                        accesses.remove(t).dispose();
                    }
                }
            }
        };
        if (null == timer) {
            timer = new Timer();
        }
        timer.schedule(cleanupTask, 0, cleanupPeriod);
        super.connect(params);
    }

    /**
     * Changes the cleanup period/timeout.
     * 
     * @param cleanupPeriod the period in ms, ignored if not positive
     */
    protected void setCleanupPeriod(int cleanupPeriod) {
        if (cleanupPeriod > 0) {
            this.cleanupPeriod = cleanupPeriod;
        }
    }
    
    /**
     * Returns the cleanup period.
     * 
     * @return the cleanup period in ms
     */
    public int getCleanupPeriod() {
        return cleanupPeriod;
    }

    @Override
    protected void uninstallPollTask() {
        if (null != cleanupTask) {
            cleanupTask.cancel();
        }
        if (null != timer) {
            timer.cancel();
        }
        super.uninstallPollTask();
    }

    /**
     * Returns the model-access instance for the current thread.
     * 
     * @return the model-access instance, may be <b>null</b>
     */
    protected M getModelAccess() {
        return getModelAccess(Thread.currentThread());
    }

    /**
     * Returns the model-access instance for the given thread.
     * 
     * @param thread the thread to return the model access for
     * @return the model-access instance, may be <b>null</b>
     */
    protected M getModelAccess(Thread thread) {
        return obtainModelAccess(thread);
    }
    
}
