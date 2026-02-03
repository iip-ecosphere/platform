/**
 * ******************************************************************************
 * Copyright (c) {2025} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.configuration.easyProducer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import de.iip_ecosphere.platform.configuration.easyProducer.ConfigurationLifecycleDescriptor.ExecutionMode;
import de.iip_ecosphere.platform.configuration.easyProducer.PlatformInstantiator.InstantiationConfigurer;
import de.iip_ecosphere.platform.configuration.easyProducer.PlatformInstantiator.NonCleaningInstantiationConfigurer;
import de.iip_ecosphere.platform.configuration.easyProducer.ivml.IvmlGraphMapper.IvmlGraph;
import de.iip_ecosphere.platform.configuration.easyProducer.ivml.IvmlGraphMapper.IvmlGraphNode;
import de.iip_ecosphere.platform.configuration.easyProducer.ivml.IvmlUtils;
import de.iip_ecosphere.platform.configuration.easyProducer.serviceMesh.ServiceMeshGraphMapper;
import de.iip_ecosphere.platform.support.IOUtils;
import de.iip_ecosphere.platform.support.StringUtils;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.LangString;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection.SubmodelElementCollectionBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElementContainerBuilder;
import de.iip_ecosphere.platform.support.aas.Type;
import de.iip_ecosphere.platform.support.identities.IdentityStore;
import de.iip_ecosphere.platform.support.identities.IdentityToken;
import de.iip_ecosphere.platform.support.iip_aas.IipVersion;
import de.iip_ecosphere.platform.support.logging.Logger;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;
import de.iip_ecosphere.platform.support.plugins.FolderClasspathPluginSetupDescriptor;
import de.iip_ecosphere.platform.support.plugins.Plugin;
import de.iip_ecosphere.platform.support.plugins.PluginManager;
import de.iip_ecosphere.platform.support.resources.MavenResourceResolver;
import de.iip_ecosphere.platform.support.resources.OktoflowResourceResolver;
import de.iip_ecosphere.platform.support.resources.ResourceLoader;
import de.iip_ecosphere.platform.support.yaml.Yaml;
import net.ssehub.easy.varModel.confModel.Configuration;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.model.ModelQuery;
import net.ssehub.easy.varModel.model.ModelQueryException;
import net.ssehub.easy.varModel.model.Project;
import net.ssehub.easy.varModel.model.datatypes.IDatatype;

/**
 * IVML-to-AAS/submodel mapper for dashboard creation with ReGaP/Bitmotec. Not realized as VTL as intended to provide
 * application dashboard submodels also at runtime. May be integrated with the configuration maven build processes or
 * as VTL Java extension.
 * 
 * @author Holger Eichelberger, SSE
 */
public class IvmlDashboardMapper {

    private File projectFolder;
    private AasFactory factory;
    private Map<String, Object> unitMapping;
    private String targetMapping = "grafana";
    private List<DisplayRow> displayRows = new ArrayList<>();
    private DisplayRow fallbackDisplayRow;
    private Map<String, DisplayPanel> displayPanels = new HashMap<>();
    
    private transient String appName;
    private transient String appId;
    private transient String appVersion;
    private transient int panelCount;
    private transient IDatatype aliasType;
    
    /**
     * Creates a mapper instance.
     * 
     * @param factory the AAS factory to use
     */
    @SuppressWarnings("unchecked")
    public IvmlDashboardMapper(AasFactory factory, File projectFolder) {
        this.projectFolder = projectFolder;
        this.factory = factory;
        try {
            unitMapping = Yaml.getInstance().loadMapping(ResourceLoader.getResourceAsStream(
                "semanticIdDashboard.yml"));   
            Object tmp = unitMapping.get(targetMapping);
            if (tmp instanceof Map) { // focus on target mapping
                unitMapping = (Map<String, Object>) tmp;
            }
        } catch (IOException e) {
            unitMapping = new HashMap<>();
            getLogger().error("Cannot load unit mapping: {}", e.getMessage());
        }        
    }
    
    /**
     * Clears this instance for reuse.
     */
    private void clear() {
        appName = null;
        appId = null;
        appVersion = null;
        panelCount = 1;
    }

    /**
     * Consumes a mapping result.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface ResultConsumer {

        /**
         * Consumes a mapping result.
         * 
         * @param aas the created/temporary/provided AAS
         * @param submodel the created submodel
         * @param appId the oktoflow application id processed
         */
        public void consume(Aas aas, Submodel submodel, String appId);
        
    }

    /**
     * Processes a given configuration.
     * 
     * @param cfg the configuration to process
     * @param aas the AAS to hook the dashboard submodel into, may be <b>null</b> if an AAS shall be created, e.g., 
     *   for JSON export
     * @param consumer consumer for the result, may be <b>null</b>
     * @throws ModelQueryException if accessing the configuration fails
     * @throws ExecutionException if creating AAS parts fails
     */
    public void process(Configuration cfg, Aas aas, ResultConsumer consumer) throws ModelQueryException, 
        ExecutionException {
        Project prj = cfg.getConfiguration().getProject();
        final IDatatype applicationType = ModelQuery.findType(prj, "Application", null);
        aliasType = ModelQuery.findType(prj, "AliasType", null);
        Iterator<IDecisionVariable> iter = cfg.getConfiguration().iterator();
        while (iter.hasNext()) {
            IDecisionVariable var = iter.next();
            IDatatype type = var.getDeclaration().getType();
            if (applicationType.isAssignableFrom(type) && !IvmlUtils.isTemplate(var.getDeclaration())) {
                clear();
                Aas appAas = null == aas ? factory.createAasBuilder("TestApplication", null).build() : aas;
                SubmodelBuilder smB = appAas.createSubmodelBuilder("dashboardSpec", null);
                appName = IvmlUtils.getStringValue(var, "name", "");
                appId = IvmlUtils.getStringValue(var, "id", "");
                appVersion = IvmlUtils.getStringValue(var, "ver", "");
                if (isNotBlank(appVersion)) {
                    if (IvmlUtils.getBooleanValue(var.getNestedElement("snapshot"), true)) {
                        appVersion += "-SNAPSHOT";
                    }
                }
                collectDisplayRows(var.getNestedElement("displayRows"));
                createHeader(smB);
                createDisplayRows(smB);
                SubmodelElementCollectionBuilder dashboardB = createDashboardSpec(smB);
                SubmodelElementCollectionBuilder panelsB = dashboardB.createSubmodelElementCollectionBuilder("panels");
                SubmodelElementCollectionBuilder dbB = dashboardB.createSubmodelElementCollectionBuilder("db");
                Map<String, String> dbMapping = new HashMap<>();

                // collect fields, types -> panelsB
                IDecisionVariable meshes = var.getNestedElement("services");
                if (null != meshes) {
                    ServiceMeshGraphMapper mapper = new ServiceMeshGraphMapper();
                    for (int n = 0; n < meshes.getNestedElementsCount(); n++) {
                        IDecisionVariable mesh = Configuration.dereference(meshes.getNestedElement(n));
                        IvmlGraph graph = mapper.getGraphFor(mesh);
                        for (IvmlGraphNode node : graph.nodes()) {
                            processNode(node, dbB, dbMapping);
                        }
                    }
                }
                createDisplayPanels(panelsB, determineDisplayPanelsSequence(cfg, factory));

                panelsB.build();
                dbB.build();
                dashboardB.build();
                Submodel submodel = smB.build();

                if (null != consumer) {
                    consumer.consume(appAas, submodel, appId);
                }
            }
        }
    }
    
    /**
     * Determines the display panels sequence, mainly from IVML, adding further collected panels 
     * from {@link #displayPanels}.
     * 
     * @param cfg the IVML configuration
     * @param factory the AAS factory
     * @return the display panels sequence
     * @throws ModelQueryException if querying IVML fails
     */
    private List<DisplayPanel> determineDisplayPanelsSequence(Configuration cfg, AasFactory factory) 
        throws ModelQueryException {
        Iterator<IDecisionVariable> iter = cfg.getConfiguration().iterator();
        Project prj = cfg.getConfiguration().getProject();
        final IDatatype panelType = ModelQuery.findType(prj, "DisplayPanel", null);
        Set<DisplayPanel> knownPanels = new HashSet<>();
        List<DisplayPanel> panelSequence = new ArrayList<DisplayPanel>();
        while (iter.hasNext()) {
            IDecisionVariable var = iter.next();
            IDatatype type = var.getDeclaration().getType();
            if (panelType.isAssignableFrom(type) && !IvmlUtils.isTemplate(var.getDeclaration())) {
                String ivmlId = getDisplayPanelId(var);
                DisplayPanel dp = displayPanels.get(ivmlId);
                if (null == dp) {
                    dp = createDisplayPanel(var, factory, ivmlId);
                }
                panelSequence.add(dp);
                knownPanels.add(dp);
            }
        }
        for (DisplayPanel p : displayPanels.values()) {
            if (!knownPanels.contains(p)) {
                panelSequence.add(p);
            }
        }
        return panelSequence;
    }
    
    /**
     * Returns the internal IVML-based ID of a display panel.
     * 
     * @param var the IVML variable representing the display panel
     * @return the display panel id
     */
    private String getDisplayPanelId(IDecisionVariable var) {
        return var.getDeclaration().getName();
    }
        
    /**
     * Creates a display panel.
     * 
     * @param var the IVML variable representing the display panel
     * @param factory the AAS factory
     * @param ivmlId the IVML id to use for the panel (@see #getDisplayPanelId(IDecisionVariable)}
     * @return the display panel
     */
    private DisplayPanel createDisplayPanel(IDecisionVariable var, AasFactory factory, String ivmlId) {
        DisplayPanel dp = new DisplayPanel(factory, ivmlId);
        dp.id = factory.fixId("panel_" + displayPanels.size());
        dp.name = IvmlUtils.getStringValue(var, "name", "");
        dp.displayName = IvmlUtils.getStringValue(var, "displayName", "");
        dp.displayRow = getDisplayRow(var);
        dp.logo = IvmlUtils.getStringValue(var, "logo", null);
        String fit = IvmlUtils.getEnumValueName(var.getNestedElement("fit"));
        if (null != fit) {
            try {
                dp.fit = FitType.valueOf(fit.toUpperCase());
            } catch (IllegalArgumentException e) {
                LoggerFactory.getLogger(IvmlDashboardMapper.class).warn("Cannot map fit value {}: {}", fit, 
                    e.getMessage());
            }
        }
        return dp;
    }
    
    /**
     * Represents a display row.
     * 
     * @author Holger Eichelberger, SSE
     */
    private class DisplayRow {
        private String id;
        private String name;
        private String displayName;

        /**
         * Creates a display row.
         * 
         * @param factory the AAS factory
         */
        DisplayRow(AasFactory factory) {
            id = factory.fixId("row_" + displayRows.size());
            displayRows.add(this);
        }

    }
    
    /**
     * Represents a panel fit type.
     * 
     * @author Holger Eichelberger, SSE
     */
    private enum FitType {
        NONE(""),
        CONTAIN("contain");
        
        private String value;
        
        /**
         * Creates a fit type.
         * 
         * @param value the corresponding dashboard value
         */
        private FitType(String value) {
            this.value = value;
        }
        
        /**
         * Returns the dashboard value.
         * 
         * @return the dashboard value
         */
        public String getValue() {
            return value;
        }
    }
    
    /**
     * Represents a display panel.
     * 
     * @author Holger Eichelberger, SSE
     */
    private class DisplayPanel {
        @SuppressWarnings("unused")
        private String id;
        private String name;
        private String displayName;
        private DisplayRow displayRow;
        private List<Field> fields = new ArrayList<>();
        private String type;
        private String unit;
        private ConnectorInfo influx;
        private String influxDb;
        private Legend legend; 
        private PanelPosition position;
        private String logo; // URL or file name to be resolved
        private FitType fit = FitType.NONE; 
        
        /**
         * Creates a display panel.
         * 
         * @param factory the AAS factory
         * @param ivmlId the IVML/pseudo id to uniquely identify  the panel
         */
        DisplayPanel(AasFactory factory, String ivmlId) {
            id = factory.fixId("panel_" + displayPanels.size());
            displayPanels.put(ivmlId, this);
        }
        
    }

    /**
     * Collects the display rows.
     * 
     * @param var the (application) variable to get the display rows from
     */
    private void collectDisplayRows(IDecisionVariable var) {
        if (null != var) {
            for (int e = 0; e < var.getNestedElementsCount(); e++) {
                IDecisionVariable element = IvmlUtils.dereference(var.getNestedElement(e));
                String name = IvmlUtils.getStringValue(element, "name", "");
                if (name.length() > 0) {
                    DisplayRow row = new DisplayRow(factory);
                    row.name = name;
                    row.displayName = IvmlUtils.getStringValue(element, "displayName", null);
                }
            }
        }
    }
    
    /**
     * Returns whether {@code text} is not blank. Repeated from {@link StringUtils} as long as plugin loading for this
     * app is not clear.
     * 
     * @param text the text to check
     * @return {@code true} for blank, {@code false} else
     */
    private static boolean isNotBlank(CharSequence text) {
        return !isBlank(text);
    }

    /**
     * Returns whether {@code text} is blank. Repeated from {@link StringUtils} as long as plugin loading for this
     * app is not clear.
     * 
     * @param text the text to check
     * @return {@code true} for blank, {@code false} else
     */
    private static boolean isBlank(CharSequence text) {
        return null == text || text.length() == 0;
    }

    /**
     * Creates submodel header information.
     * 
     * @param smB the parent submodel builder
     */
    private void createHeader(SubmodelBuilder smB) {
        createProperty(smB, "oktoVersion", Type.STRING, IipVersion.getInstance().getVersionInfo(), "oktoflow version");
        createProperty(smB, "name", Type.STRING, appName, "application name");
        createProperty(smB, "id", Type.STRING, appId, "application id");
        createProperty(smB, "version", Type.STRING, appVersion, "application version");
        createProperty(smB, "aasMetamodelVersion", Type.STRING, factory.getMetaModelVersion(), "AAS metamodel version");
    }

    /**
     * Creates the display rows submodel.
     * 
     * @param smB the parent submodel builder
     */
    private void createDisplayRows(SubmodelBuilder smB) {
        SubmodelElementCollectionBuilder rowsB = smB.createSubmodelElementCollectionBuilder("Rows");
        for (DisplayRow row : displayRows) {
            SubmodelElementCollectionBuilder rowB = rowsB.createSubmodelElementCollectionBuilder(row.id);
            createProperty(rowB, "id", Type.STRING, row.id, "Unique id of display row");
            createProperty(rowB, "name", Type.STRING, row.name, "Name of display row");
            createProperty(rowB, "displayName", Type.STRING, row.displayName, "Display name of display row");
            rowB.build();
        }
        rowsB.build();
    }
    
    /**
     * Creates the dashboard specification collection.
     * 
     * @param smB the parent submodel builder
     */
    private SubmodelElementCollectionBuilder createDashboardSpec(SubmodelBuilder smB) {
        SubmodelElementCollectionBuilder dashboardB = smB.createSubmodelElementCollectionBuilder("Dashboard");
        createProperty(dashboardB, "title", Type.STRING, appName, "title of the dashboard"); // TODO preliminary name
        createProperty(dashboardB, "uid", Type.STRING, appId, "app/dashboard UID");
        SubmodelElementCollectionBuilder tagsB = dashboardB.createSubmodelElementCollectionBuilder("tags");
        tagsB.build();
        // TODO time_from, time_to, timezone
        return dashboardB;
    }

    /**
     * Processes an IVML graph node, filters influx connectors and transfers the information into individual panels.
     * 
     * @param node the node to process
     * @param dbsB the database builder
     * @param dbMapping mapping of the name of the found connection info to the influxDb
     */
    private void processNode(IvmlGraphNode node, SubmodelElementCollectionBuilder dbsB, Map<String, String> dbMapping) {
        IDecisionVariable var = node.getVariable();
        IDecisionVariable impl = Configuration.dereference(var.getNestedElement("impl"));
        if (null != impl) {
            if (IvmlUtils.isOfCompoundType(impl, "InfluxConnector")) {
                ConnectorInfo connInfo = resolveConnector(impl);
                String influxDb = dbMapping.get(connInfo.name);
                if (null == influxDb) {
                    influxDb = processDb(impl, connInfo, dbsB);
                    dbMapping.put(connInfo.name, influxDb);
                }
                IDecisionVariable inputVar = impl.getNestedElement("input");
                if (null != inputVar && inputVar.getNestedElementsCount() > 0) { // connectors have only one
                    IDecisionVariable ioTypeVar = inputVar.getNestedElement(0);
                    IDecisionVariable typeVar = Configuration.dereference(ioTypeVar.getNestedElement("type"));
                    resolveType(typeVar, connInfo, influxDb);
                }
            }
        }
    }
    
    /**
     * Processes a database entry.
     * 
     * @param impl the service/connector implementation
     * @param connInfo the connector information
     * @param dbsB the databases collection builder
     * @return the database uid
     */
    private String processDb(IDecisionVariable impl, ConnectorInfo connInfo, SubmodelElementCollectionBuilder dbsB) {
        String uid = factory.fixId(connInfo.name.replace(" ", "_"));
        SubmodelElementCollectionBuilder dbB = dbsB.createSubmodelElementCollectionBuilder(uid);
        createProperty(dbB, "uid", Type.STRING, uid, "InfluxDB uid"); 
        if (isNotBlank(connInfo.host)) {
            String url;
            if (connInfo.security != null && connInfo.security.ssl) {
                url = "https";
            } else {
                url = "http";
            }
            url += "://" + connInfo.host;
            if (connInfo.port > 0) {
                url += ":" + connInfo.port;
            }
            String path = IvmlUtils.getStringValue(impl, "urlPath", null);
            if (isNotBlank(path)) {
                url += "/" + path;
            }
            createProperty(dbB, "url", Type.STRING, url, "InfluxDB URL");
        }
        createProperty(dbB, "organization", Type.STRING, connInfo.organization, "InfluxDB organization"); 
        ResourceLoader.registerResourceResolver(new MavenResourceResolver(projectFolder));
        if (connInfo.security != null && isNotBlank(connInfo.security.authenticationKey)) {
            String authKey = connInfo.security.authenticationKey;
            if (connInfo.security.enableTokenExport) {
                IdentityStore store = IdentityStore.getInstance();
                String token = "";
                IdentityToken tok = store.getToken(authKey);
                if (null != tok) {
                    switch (tok.getType()) {
                    case ISSUED:
                    case USERNAME:
                        token = tok.getTokenDataAsString();
                        break;
                    default:
                        getLogger().warn("Cannot process token of type {} for authentication key {}", 
                            tok.getType(), authKey);
                        break;
                    }
                } else {
                    getLogger().warn("No authentication token for authentication key {}", authKey);
                }
                createProperty(dbB, "token", Type.STRING, token, "InfluxDB token");
            } else {
                getLogger().warn("No permission to export authentication token for authentication key {}", authKey);
            }
        }
        dbB.build();
        return uid;
    }

    /**
     * Represents relevant information form an (INFLUX) connector.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class ConnectorInfo {
        @SuppressWarnings("unused")
        private String id;
        private String name;
        private String organization;
        private String bucket;
        private String measurement;
        private String host;
        private int port;
        private SecuritySettings security;
    }
    
    /**
     * Represents optional security/authentication settings.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class SecuritySettings {
        private boolean ssl;
        private String authenticationKey;
        private boolean enableTokenExport;
        @SuppressWarnings("unused")
        private String keystoreKey;
        @SuppressWarnings("unused")
        private String keyAlias;
        @SuppressWarnings("unused")
        private String idStoreAuthenticationPrefix;
    }
    
    /**
     * Represents a resolved and mapped oktoflow record type.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class RecordType {
        @SuppressWarnings("unused")
        private String name;
        private List<Field> fields = new ArrayList<>();
    }
    
    /**
     * Represents a field in a {@link RecordType}.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class Field {
        private String name;
        @SuppressWarnings("unused")
        private String field;
        @SuppressWarnings("unused")
        private String description;
        private String displayName;
        private String unit;
        private String panelType;
        private List<DisplayPanel> displayPanels = new ArrayList<>();
        @SuppressWarnings("unused")
        private RecordType recordType;
        
        /**
         * Returns whether the field is enabled for display.
         * 
         * @return {@code true} if enabled, {@code false} else
         */
        private boolean isEnabled() {
            return !isBlank(unit) && !isBlank(panelType);
        }
    }
    
    /**
     * Represents a dashboard panel legend.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class Legend {
        private boolean asTable;
        private String placement; // left, bottom
        private String calculations; // min, max, mean
    }
    
    /**
     * Represents a dashboard panel position.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class PanelPosition {
        private int x;
        private int y;
        private int width;
        private int height;
    }
    
    /**
     * Resolves connector information.
     * 
     * @param var the IVML variable representing the connector, may be <b>null</b>
     * @return the resolved connector information, may be <b>null</b>
     */
    private ConnectorInfo resolveConnector(IDecisionVariable var) {
        ConnectorInfo result = null;
        if (null != var) {
            result = new ConnectorInfo();
            final String fallbackPanelName = "panel " + panelCount++;
            result.id = IvmlUtils.getStringValue(var, "id", fallbackPanelName);
            result.name = IvmlUtils.getStringValue(var, "name", fallbackPanelName);
            result.organization = IvmlUtils.getStringValue(var, "organization", "");
            result.bucket = IvmlUtils.getStringValue(var, "bucket", "");
            result.measurement = IvmlUtils.getStringValue(var, "measurement", "");
            result.host = IvmlUtils.getStringValue(var, "host", null);
            result.port = IvmlUtils.getIntValue(var, "port", -1);
            
            IDecisionVariable security = var.getNestedElement("security");
            if (null != security) {
                SecuritySettings sec = new SecuritySettings();
                sec.ssl = IvmlUtils.getBooleanValue(var, "ssl", false);
                sec.authenticationKey = IvmlUtils.getStringValue(security, "authenticationKey", null);
                sec.enableTokenExport = IvmlUtils.getBooleanValue(security, "enableTokenExport", false);
                sec.keystoreKey = IvmlUtils.getStringValue(security, "keystoreKey", null);
                sec.keyAlias = IvmlUtils.getStringValue(security, "keyAlias", null);
                sec.idStoreAuthenticationPrefix = IvmlUtils.getStringValue(security, 
                    "idStoreAuthenticationPrefix", null);
                result.security = sec;
            }
        }
        return result;
    }
    
    /**
     * Returns a the semantic id from the given {@code var}.
     * 
     * @param var the IVML variable
     * @param semanticId the actual semantic id (do not overwrite if not <b>null</b>)
     * @return the retrieved semantic id or {@code semanticid}
     */
    private String getSemanticId(IDecisionVariable var, String semanticId) {
        if (semanticId == null) { // topmost value
            semanticId = IvmlUtils.getStringValue(var, "semanticId", semanticId);
        }
        return semanticId;
    }
        
    /**
     * Resolves an IVML type to dashboard relevant information.
     * 
     * @param var the variable representing the type, may be <b>null</b>
     * @param influx the influx connector information
     * @param influxDb the identifier of the db
     * @return the resolved information, may be <b>null</b>
     */
    private RecordType resolveType(IDecisionVariable var, ConnectorInfo influx, String influxDb) {
        RecordType result = null;
        if (null != var) {
            if (var != null) {
                result = new RecordType();
                result.name = IvmlUtils.getStringValue(var, "name", "");
                IDecisionVariable recordIter = var;
                while (recordIter != null && !recordIter.hasNullValue()) {
                    IDecisionVariable fields = recordIter.getNestedElement("fields");
                    for (int f = 0; f < fields.getNestedElementsCount(); f++) {
                        IDecisionVariable fieldVar = fields.getNestedElement(f);
                        Field fld = new Field();
                        fld.recordType = result;
                        fld.name = IvmlUtils.getStringValue(fieldVar, "name", "");
                        fld.field = IvmlUtils.getStringValue(fieldVar, "mappedName", fld.name);
                        fld.description = IvmlUtils.getStringValue(fieldVar, "description", "");
                        fld.displayName = IvmlUtils.getStringValue(fieldVar, "displayName", "");
                        fld.unit = resolveSemanticIdToUnit(fieldVar);
                        fld.panelType = resolvePanelType(fieldVar);
                        IDecisionVariable displayPanel = fieldVar.getNestedElement("displayPanel");
                        if (null != displayPanel && displayPanel.getNestedElementsCount() > 0) {
                            for (int p = 0; p < displayPanel.getNestedElementsCount(); p++) {
                                IDecisionVariable dpVar = IvmlUtils.dereference(displayPanel.getNestedElement(p));
                                String ivmlId = getDisplayPanelId(dpVar);
                                DisplayPanel dp = displayPanels.get(ivmlId);
                                if (null == dp) {
                                    dp = createDisplayPanel(dpVar, factory, ivmlId);
                                    dp.unit = fld.unit;
                                    dp.type = fld.panelType;
                                    dp.influx = influx;
                                    dp.influxDb = influxDb;
                                    dp.fields.add(fld);
                                } else if (dp.unit.equals(fld.unit) && dp.type.equals(fld.panelType)) {
                                    fld.displayPanels.add(dp);
                                    dp.fields.add(fld);
                                } else {
                                    System.out.println("Panel/field mismatch regarding unit and panelType in " 
                                        + dp.name + "/" + fld.name);
                                }
                            }
                        } else if (fld.isEnabled()) { // just fallback so that there is a panel, is not assigned to rows
                            DisplayPanel dp = new DisplayPanel(factory, "*fallback*" + fld.name);
                            dp.id = factory.fixId("panel_" + displayPanels.size());
                            dp.name = fld.name;
                            dp.displayName = fld.displayName;
                            dp.fields.add(fld);
                            if (null == fallbackDisplayRow) {
                                fallbackDisplayRow = new DisplayRow(factory);
                            }
                            dp.displayRow = fallbackDisplayRow;
                            dp.unit = fld.unit;
                            dp.type = fld.panelType;
                            dp.influx = influx;
                            dp.influxDb = influxDb;
                        }
                        result.fields.add(fld);
                    }
                    recordIter = Configuration.dereference(recordIter.getNestedElement("refining"));
                }
                //result.legend
                //result.position
            }
        }        
        return result;
    }
    
    /**
     * Determines the display row of {@code var}.
     * 
     * @param var the variable to query
     * @return the display row, may be <b>null</b> for none
     */
    private DisplayRow getDisplayRow(IDecisionVariable var) {
        DisplayRow result = null;
        IDecisionVariable displayRow = IvmlUtils.dereference(var.getNestedElement("displayRow"));
        String displayRowName = IvmlUtils.getStringValue(displayRow, "name", null);
        if (displayRowName != null) {
            Optional<DisplayRow> dr = displayRows
                .stream()
                .filter(r -> r.name.equals(displayRowName))
                .findFirst();
            if (dr.isPresent()) {
                result = dr.get(); 
            }
        }
        return result;
    }

    /**
     * Resolves the panel type.
     * 
     * @param var the variable to take the panel type/display enum from
     * @return the panel type
     */
    private String resolvePanelType(IDecisionVariable var) {
        IDecisionVariable displayVar = var.getNestedElement("display");
        String displayValue = IvmlUtils.getEnumValueName(displayVar);
        if (displayValue != null) {
            Object tmp = unitMapping.get("Display_" + displayValue);
            if (null != tmp) {
                displayValue = tmp.toString();
            }
        }
        return displayValue;
    }

    /**
     * Resolves the (inherited/refined) semantic id to a display unit.
     * 
     * @param semId the semantic id to map
     * @return the mapped semantic id
     */
    private String mapSemanticIdToUnit(String semId) {
        // https://github.com/grafana/grafana/blob/main/packages/grafana-data/src/valueFormats/categories.ts
        String result = semId;
        if (null != result) {
            Object tmp = unitMapping.get(result);
            result = null == tmp ? null : tmp.toString();
        }
        return result;
    }

    /**
     * Resolves the (inherited/refined) semantic id to a display unit.
     * 
     * @param var the variable to take the type from
     * @return the mapped semantic id
     */
    private String resolveSemanticIdToUnit(IDecisionVariable var) {
        String semanticId = getSemanticId(var, null);
        IDecisionVariable type = Configuration.dereference(var.getNestedElement("type"));
        while (aliasType.isAssignableFrom(type.getDeclaration().getType())) {
            semanticId = getSemanticId(type, semanticId);
            type = Configuration.dereference(type.getNestedElement("represents"));
        }
        String result = semanticId;
        if (null != result) {
            result = mapSemanticIdToUnit(result);
        }
        if (null == result) {
            result = mapSemanticIdToUnit(IvmlUtils.getStringValue(type, "name", ""));
        }
        return result;
    }

    /**
     * Creates the contents of the display panels. 
     * 
     * @param panelsB the parent builder for the panels
     * @param panels the panels sequence, mostly determined by IVML
     */
    private void createDisplayPanels(SubmodelElementCollectionBuilder panelsB, List<DisplayPanel> panels) {
        for (DisplayPanel p : panels) {
            SubmodelElementCollectionBuilder panelB = panelsB.createSubmodelElementCollectionBuilder(
                factory.fixId(p.name));
            processLogo(p, panelB);
            createProperty(panelB, "title", Type.STRING, p.name, "Panel title");
            createPropertyEmpty(panelB, "unit", Type.STRING, p.unit, "Panel unit");
            createPropertyEmpty(panelB, "datasource_uid", Type.STRING, p.influxDb,  
                "Unique identifier for InfluxDB in db section");
            createPropertyEmpty(panelB, "bucket", Type.STRING, p.influx == null ? "" : p.influx.bucket,  
                "InfluxDB bucket"); 
            createPropertyEmpty(panelB, "measurement", Type.STRING, p.influx == null ? "" : p.influx.measurement, 
                "InfluxDB measurement");
            String fields = p.fields.stream().map(f -> f.name).collect(Collectors.joining(","));
            createProperty(panelB, "fields", Type.STRING, fields, "InfluxDB fields in measurement");
            createProperty(panelB, "panel_type", Type.STRING, p.type, "Panel type");
            createProperty(panelB, "description", Type.STRING, "", "Panel description");
            createProperty(panelB, "displayName", Type.STRING, p.displayName, "Panel display name");
            if (p.displayRow != null) {
                createProperty(panelB, "row", Type.STRING, p.displayRow.id, 
                    "Display row id pointing to rows section");
            }
            
            // TODO axis_max_soft
            // TODO axis_min_soft
            // TODO axis_label
            processLegend(p.legend, panelB);
            processPanelPosition(p.position, panelB);
            panelB.build();
        }
    }

    /**
     * Processes a specified logo.
     * 
     * @param panel the panel to take the logo from. If resolved, the panel type may be changed as a side effect.
     * @param panelsB the parent builder for the panels
     */
    private void processLogo(DisplayPanel panel, SubmodelElementCollectionBuilder panelsB) {
        SubmodelElementCollectionBuilder logoB = null;
        if (panel.logo != null && !panel.logo.isBlank()) {
            String logoSpec = panel.logo;
            if (!(logoSpec.startsWith("http://") || logoSpec.startsWith("https://"))) {
                InputStream logoFile = ResourceLoader.getResourceAsStream(logoSpec, 
                    new OktoflowResourceResolver(new File(""), "software"));
                if (null != logoFile) {
                    try {
                        byte[] imageBytes = IOUtils.toByteArray(logoFile);
                        logoFile.close();
                        String imageBase64 = Base64.getEncoder().encodeToString(imageBytes);
                        String imageSpec = "png"; // assume default
                        int pos = logoSpec.lastIndexOf(".");
                        if (pos > 0 && pos + 1 < logoSpec.length()) {
                            imageSpec = logoSpec.substring(pos + 1).toLowerCase();
                        }
                        panel.logo = "data:image/" + imageSpec + ";base64," + imageBase64;
                    } catch (IOException e) {
                        LoggerFactory.getLogger(IvmlDashboardMapper.class).warn("While reading logo {}: {}", 
                            logoSpec, e.getMessage());
                        panel.logo = null;
                    }
                } else {
                    LoggerFactory.getLogger(IvmlDashboardMapper.class).warn("While reading logo {}: no such "
                        + "resource", logoSpec);
                    panel.logo = null;
                }
            }
            if (null != panel.logo) {
                logoB = panelsB.createSubmodelElementCollectionBuilder(factory.fixId("custom_options"));
                createProperty(logoB, "imageUrl", Type.STRING, panel.logo, 
                    "The image URL/data of the image to display");
                panel.type = "image";
            }
        }
        if (panel.fit != FitType.NONE) {
            if (null == logoB) {
                logoB = panelsB.createSubmodelElementCollectionBuilder(factory.fixId("custom_options"));
            }
            createProperty(logoB, "fit", Type.STRING, panel.fit.getValue(), "The panel fitting");
        }
        if (null != logoB) {
            logoB.build();
        }
    }

    /**
     * Turns a panel legend into AAS.
     * 
     * @param legend the legend
     * @param panelB the parent builder
     */
    private void processLegend(Legend legend, SubmodelElementCollectionBuilder panelB) {
        if (null != legend) {
            createProperty(panelB, "as_table", Type.BOOLEAN, legend.asTable, "Legend: as_table");
            createProperty(panelB, "placement", Type.STRING, legend.placement, "Legend: placement");
            createProperty(panelB, "calculations", Type.STRING, legend.calculations, "Legend: calculations");
        }
    }

    /**
     * Turns a panel position into AAS.
     * 
     * @param position the position
     * @param panelB the parent builder
     */
    private void processPanelPosition(PanelPosition position, SubmodelElementCollectionBuilder panelB) {
        if (null != position) {
            createProperty(panelB, "x", Type.INTEGER, position.x, "Panel position: x");
            createProperty(panelB, "y", Type.INTEGER, position.y, "Panel position: y");
            createProperty(panelB, "width", Type.INTEGER, position.width, "Panel position: width");
            createProperty(panelB, "height", Type.INTEGER, position.height, "Panel position: height");
        }
    }

    /**
     * Creates an AAS property with empty default if value is not given.
     * 
     * @param parent the parent builder
     * @param idShort the idShort
     * @param type the property type
     * @param value the property value, may be <b>null</b>, then {@code dflt} is used
     * @param description the description to be added
     */
    private void createPropertyEmpty(SubmodelElementContainerBuilder parent, String idShort, Type type, Object value, 
        String description) {
        createProperty(parent, idShort, type, value == null ? "" : value, description);
    }
    
    /**
     * Creates an AAS property.
     * 
     * @param parent the parent builder
     * @param idShort the idShort
     * @param type the property type
     * @param value the property value, may be <b>null</b>, call is ignored then
     * @param description the description to be added
     */
    private void createProperty(SubmodelElementContainerBuilder parent, String idShort, Type type, Object value, 
        String description) {
        if (value != null) {
            parent.createPropertyBuilder(idShort) 
                .setValue(type, value)
                .setDescription(new LangString("EN", description))
                .build();
        }
    }
    
    /**
     * Performs the dashboard AAS-JSON instantiation.
     * 
     * @param args command line arguments
     * 
     * @throws ExecutionException in case that the VIL instantiation fails, shall not occur here as handled by 
     * default {@link InstantiationConfigurer}
     * @throws IOException if files cannot be located/written
     */
    public static void main(String[] args) throws ExecutionException, IOException {
        System.out.println("oktoflow dashboard instantiator");
        if (args.length < 2) {
            System.out.println("Following arguments are required:");
            System.out.println(" - name of the model/configuration");
            System.out.println(" - folder the model is located in, src/main/easy is used for the metamodel");
            System.out.println(" - optional IVML meta model folder");
        } else {
            mainImpl(args);
        }
    }

    /**
     * Main functionality without returning exit code/output of help for re-use. Could be with explicit parameters...
     * 
     * @param args command line arguments
     * @throws ExecutionException in case that the VIL instantiation fails, shall not occur here as handled by 
     *     default {@link InstantiationConfigurer}
     * @throws IOException if files cannot be located/written
     */
    private static void mainImpl(String[] args) throws ExecutionException, IOException {
        // TODO determine plugin loading, currently setup for local loading
        File support = new File("../../support").getCanonicalFile();
        String supportFolder = support.toString();
        // explicitly load plugins for now
        File logPlugin = new File(supportFolder + "/support.log-slf4j-simple");
        PluginManager.registerPlugin(new FolderClasspathPluginSetupDescriptor(
            logPlugin));
        logPlugin = new File(logPlugin, "\\target\\jars\\classpath");
        PluginManager.registerPlugin(new FolderClasspathPluginSetupDescriptor(
            new File(supportFolder + "/support.aas.basyx"), false, logPlugin));
        PluginManager.registerPlugin(new FolderClasspathPluginSetupDescriptor(
            new File(supportFolder + "/support.aas.basyx2"), false, logPlugin));
        PluginManager.registerPlugin(new FolderClasspathPluginSetupDescriptor(
            new File(supportFolder + "/support.yaml-snakeyaml")));
        final String pluginId = "aas.basyx-2.0"; // AasFactory.DEFAULT_PLUGIN_ID
        AasFactory factory = getAasFactory(pluginId); 
        
        ConfigurationSetup setup = ConfigurationSetup.getSetup();
        EasySetup easySetup = setup.getEasyProducer();
        easySetup.reset();
        File projectFolder = new File(args[1]);
        InstantiationConfigurer configurer = new NonCleaningInstantiationConfigurer(args[0], 
            projectFolder, new File("gen")); // gen is actually not used
        if (args.length >= 3) {
            configurer.setIvmlMetaModelFolder(new File(args[2]));
        }
        configurer.configure(setup);
        ConfigurationLifecycleDescriptor lcd = configurer.obtainLifecycleDescriptor();
        lcd.startup(ExecutionMode.TOOLING, new String[0]); // shall register executor
        Configuration cfg = ConfigurationManager.getIvmlConfiguration();
        ConfigurationManager.validateAndPropagate();
        try {
            new IvmlDashboardMapper(factory, projectFolder).process(cfg, null, (aas, sm, id) -> {
                String fileName = id.replace(' ', '_');
                File file = new File("target/" + fileName + ".json");
                try {
                    factory.createPersistenceRecipe().writeTo(List.of(aas), file);
                    getLogger().info("File {} written.", file);
                } catch (IOException e) {
                    getLogger().error("While writing {}: {}", file, e.getMessage());
                }
            });
        } catch (ModelQueryException e) {
            throw new ExecutionException(e);
        }

        lcd.shutdown();
        setup.getEasyProducer().reset();
    }
    
    /**
     * Returns the logger instance.
     * 
     * @return the logger instance
     */
    private static Logger getLogger() {
        return LoggerFactory.getLogger(IvmlDashboardMapper.class);
    }
    
    /**
     * Returns the AAS factory to use, based on {@code aasFactoryPluginId}.
     *
     * @param aasFactoryPluginId the plugin id of the AAS factory to use
     * @return the factory to use
     */
    private static AasFactory getAasFactory(String aasFactoryPluginId) {
        AasFactory factory;
        Plugin<AasFactory> plugin = PluginManager.getPlugin(aasFactoryPluginId, AasFactory.class);
        if (null != plugin) {
            factory = plugin.getInstance();
        } else { // fallback
            factory = AasFactory.getInstance();
        }
        return factory;
    }    

}
