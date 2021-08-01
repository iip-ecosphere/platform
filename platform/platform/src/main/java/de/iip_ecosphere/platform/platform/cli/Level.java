package de.iip_ecosphere.platform.platform.cli;

/**
 * The input/shell level.
 * 
 * @author Holger Eichelberger, SSE
 */
public enum Level {
    
    TOP(""),
    SERVICES("services"),
    CONTAINER("container"),
    RESOURCES("resources");
    
    private String prompt;
    
    /**
     * Creates a level constant with prompt text.
     * 
     * @param prompt the prompt text
     */
    private Level(String prompt) {
        this.prompt = prompt;
    }
    
    /**
     * Returns the prompt text.
     * 
     * @return the prompt text
     */
    public String getPrompt() {
        return prompt;
    }
    
    /**
     * Returns whether this level is top-level.
     * 
     * @return {@code true} for top-level, {@code false} else
     */
    public boolean isTopLevel() {
        return TOP == this;
    }
    
}