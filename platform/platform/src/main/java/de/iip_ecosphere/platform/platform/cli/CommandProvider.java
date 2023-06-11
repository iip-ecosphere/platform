package de.iip_ecosphere.platform.platform.cli;

import java.io.IOException;

/**
 * Provides access to incremental command input.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface CommandProvider {
    
    /**
     * Returns the next command.
     * 
     * @return the next command, <b>null</b> for none/end of input
     */
    public String nextCommand();
    
    /**
     * Waits for any key.
     */
    public default void waitForAnyKey() {
        anyKey();
    }
    
    /**
     * Waits for any key.
     */
    public static void anyKey() {
        try {
            System.in.read();
        } catch (IOException e) {
        }
    }
    
    /**
     * Returns whether this provider is interactive.
     * 
     * @return <code>true</code> for interactive, <code>false</code> for static/one shot
     */
    public boolean isInteractive();
    
}