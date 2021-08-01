package de.iip_ecosphere.platform.platform.cli;

/**
 * A command provider wrapping command line arguments.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ArgsCommandProvider implements CommandProvider {
    
    private String[] args;
    private int pos = 0;
    
    /**
     * Creates a command provider based on command line arguments.
     * 
     * @param args the command line arguments
     */
    public ArgsCommandProvider(String[] args) {
        this.args = args;
    }

    @Override
    public String nextCommand() {
        String result;
        skipOptions();
        if (pos < args.length) {
            result = args[pos++];
        } else {
            result = null;
        }
        return result;
    }
    
    /**
     * Advances {@link #pos} until a command, skips options.
     */
    private void skipOptions() {
        // no options so far
    }

    @Override
    public boolean isInteractive() {
        return false;
    }
    
}