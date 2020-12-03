package test.de.iip_ecosphere.platform.connectors.basyx;

import org.eclipse.basyx.models.controlcomponent.ControlComponentChangeListener;
import org.eclipse.basyx.models.controlcomponent.ExecutionMode;
import org.eclipse.basyx.models.controlcomponent.ExecutionState;
import org.eclipse.basyx.models.controlcomponent.OccupationState;
import org.eclipse.basyx.models.controlcomponent.SimpleControlComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.transport.Utils;

public class TestControlComponent extends SimpleControlComponent implements ControlComponentChangeListener {

    // Operation modes
    public static final String OPMODE_BASIC = "BASIC";
    public static final String OPMODE_STARTING = "STARTING";
    public static final String OPMODE_CONFIGURING = "CONFIGURING";
    public static final String OPMODE_STOPPING = "STOPPING";
    
    private static final long serialVersionUID = -5524351270180073574L;
    private static final Logger LOGGER = LoggerFactory.getLogger(TestControlComponent.class);
    private TestMachine machine;

    /**
     * Creates a control component for {@code machine}.
     *
     * @param machine the machine instance
     */
    public TestControlComponent(TestMachine machine) {
        this.machine = machine;
        addControlComponentChangeListener(this);
    }

    @Override
    public void onChangedExecutionState(ExecutionState newExecutionState) {
        LOGGER.info("CC:   new execution state: " + newExecutionState); 
        
        if (newExecutionState == ExecutionState.EXECUTE) {
            String opMode = this.getOperationMode();
            switch(opMode) {
            case OPMODE_STARTING:
                startMachine();
                break;
            case OPMODE_CONFIGURING:
                configureMachine(); // TODO params
                break;
            case OPMODE_STOPPING:
                stopMachine();
                break;
            default:
                setExecutionState(ExecutionState.COMPLETE.getValue());
                break;
            }       
        }        
    }

    /**
     * Starts the machine.
     */
    protected void startMachine() {
        new Thread(() -> {
            machine.start();
            Utils.sleep(100);
            setExecutionState(ExecutionState.COMPLETE.getValue());
        }).start();
    }
    
    /**
     * Configures the machine.
     */
    protected void configureMachine() {
        new Thread(() -> {
            machine.setLotSize(5); // TODO params
            Utils.sleep(100);
            setExecutionState(ExecutionState.COMPLETE.getValue());
        }).start();
    }
    
    /**
     * Stops the machine.
     */
    protected void stopMachine() {
        new Thread(() -> {
            machine.stop();
            Utils.sleep(100);
            setExecutionState(ExecutionState.COMPLETE.getValue());
        }).start();
    }

    @Override
    public void onVariableChange(String varName, Object newValue) {
        // not needed
    }

    @Override
    public void onNewOccupier(String occupierId) {
        // not needed
    }

    @Override
    public void onNewOccupationState(OccupationState state) {
        // not needed
    }

    @Override
    public void onChangedExecutionMode(ExecutionMode newExecutionMode) {
        // not needed
    }

    @Override
    public void onChangedOperationMode(String newOperationMode) {
        // not needed
    }

    @Override
    public void onChangedWorkState(String newWorkState) {
        // not needed
    }

    @Override
    public void onChangedErrorState(String newWorkState) {
        // not needed
    }

}
