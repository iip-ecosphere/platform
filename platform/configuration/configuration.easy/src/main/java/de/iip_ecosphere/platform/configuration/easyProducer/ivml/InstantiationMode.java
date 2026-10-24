package de.iip_ecosphere.platform.configuration.easyProducer.ivml;

/**
 * Instantiation modes.
 * 
 * @author Holger Eichelberger, SSE
 */
public enum InstantiationMode {
    
    APPS_NO_DEPS("generateAppsNoDeps", false),
    APPS("generateApps", true),
    INTERFACES("generateInterfaces", false),
    INTERFACES_AND_CODE_TEMPLATES("generateInterfacesAndCodeTemplates", false),
    BOOTSTRAPPED_APPS("generateBootstrappedApps", false),
    CODE_TEMPLATES("generateCodeTemplates", false),
    APPS_DIRECT("generateAppsDirect", true),
    APPS_TEMPLATES_DIRECT("generateAppsTemplatesDirect", true);
    
    private String startRuleName;
    private boolean updateSnapshots;

    /**
     * Creates an instantiation mode constant.
     * 
     * @param startRuleName the start rule name
     */
    private InstantiationMode(String startRuleName, boolean updateSnapshots) {
        this.startRuleName = startRuleName;
        this.updateSnapshots = updateSnapshots;
    }
    
    /**
     * Returns the start rule name.
     * 
     * @return the start rule name
     */
    public String getStartRuleName() {
        return startRuleName;
    }

    /**
     * Returns whether snapshots shall be updated.
     * 
     * @return whether snapshots shall be updated
     */
    public boolean isUpdateSnapshots() {
        return updateSnapshots;
    }

}