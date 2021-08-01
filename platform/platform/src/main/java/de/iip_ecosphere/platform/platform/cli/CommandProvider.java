package de.iip_ecosphere.platform.platform.cli;

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
     * Returns whether this provider is interactive.
     * 
     * @return <code>true</code> for interactive, <code>false</code> for static/one shot
     */
    public boolean isInteractive();
}