package de.oktoflow.platform.tools.energyAggregator.server;

/**
 * Thrown if a model update refers to a device/target combination that does not exist in the address space.
 * 
 * @author Kodjo Gilles Adjroloh
 */
public class UnknownTargetException extends Exception {

    /**
     * Serialization version.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The unknown node key.
     */
    private final String target;

    /**
     * Creates an exception.
     * 
     * @param target the unknown node key
     */
    public UnknownTargetException(String target) {
        super("Unknown target: " + target);
        this.target = target;
    }

    /**
     * Returns the unknown node key.
     * 
     * @return the node key
     */
    public String getTarget() {
        return target;
    }

}
