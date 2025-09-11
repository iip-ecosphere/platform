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

package de.iip_ecosphere.platform.configuration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import de.iip_ecosphere.platform.configuration.PlatformInstantiator.InstantiationConfigurer;
import de.iip_ecosphere.platform.configuration.PlatformInstantiator.NonCleaningInstantiationConfigurer;
import de.iip_ecosphere.platform.configuration.ivml.IvmlGraphMapper.IvmlGraph;
import de.iip_ecosphere.platform.configuration.ivml.IvmlGraphMapper.IvmlGraphNode;
import de.iip_ecosphere.platform.configuration.ivml.IvmlUtils;
import de.iip_ecosphere.platform.configuration.serviceMesh.ServiceMeshGraphMapper;
import de.iip_ecosphere.platform.support.StringUtils;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.LangString;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection.SubmodelElementCollectionBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElementContainerBuilder;
import de.iip_ecosphere.platform.support.aas.Type;
import de.iip_ecosphere.platform.support.iip_aas.IipVersion;
import de.iip_ecosphere.platform.support.logging.Logger;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;
import de.iip_ecosphere.platform.support.plugins.FolderClasspathPluginSetupDescriptor;
import de.iip_ecosphere.platform.support.plugins.Plugin;
import de.iip_ecosphere.platform.support.plugins.PluginManager;
import de.iip_ecosphere.platform.support.resources.ResourceLoader;
import de.iip_ecosphere.platform.support.yaml.Yaml;
import net.ssehub.easy.varModel.confModel.Configuration;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.model.ModelQuery;
import net.ssehub.easy.varModel.model.ModelQueryException;
import net.ssehub.easy.varModel.model.Project;
import net.ssehub.easy.varModel.model.datatypes.IDatatype;
import net.ssehub.easy.varModel.model.values.EnumValue;
import net.ssehub.easy.varModel.model.values.Value;

/**
 * IVML-to-AAS/submodel mapper for dashboard creation with ReGaP/Bitmotec. Not realized as VTL as intended to provide
 * application dashboard submodels also at runtime. May be integrated with the configuration maven build processes or
 * as VTL Java extension.
 * 
 * @author Holger Eichelberger, SSE
 */
public class IvmlDashboardMapper {

    private AasFactory factory;
    private Map<String, Object> unitMapping;
    private String targetMapping = "grafana";
    
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
    public IvmlDashboardMapper(AasFactory factory) {
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
            // TODO filter out templates
            if (applicationType.isAssignableFrom(type)) {
                clear();
                Aas appAas = null == aas ? factory.createAasBuilder("TestApplication", null).build() : aas;
                SubmodelBuilder smB = appAas.createSubmodelBuilder("dashboardSpec", null);
                appName = IvmlUtils.getStringValue(var, "name", "");
                appId = IvmlUtils.getStringValue(var, "id", "");
                appVersion = IvmlUtils.getStringValue(var, "ver", "");
                if (StringUtils.isNotBlank(appVersion)) {
                    if (IvmlUtils.getBooleanValue(var.getNestedElement("snapshot"), true)) {
                        appVersion += "-SNAPSHOT";
                    }
                }
                createHeader(smB);
                SubmodelElementCollectionBuilder dashboardB = createDashboardSpec(smB);
                SubmodelElementCollectionBuilder panelsB = dashboardB.createSubmodelElementCollectionBuilder("panels");
                
                IDecisionVariable meshes = var.getNestedElement("services");
                if (null != meshes) {
                    ServiceMeshGraphMapper mapper = new ServiceMeshGraphMapper();
                    for (int n = 0; n < meshes.getNestedElementsCount(); n++) {
                        IDecisionVariable mesh = Configuration.dereference(meshes.getNestedElement(n));
                        IvmlGraph graph = mapper.getGraphFor(mesh);
                        for (IvmlGraphNode node : graph.nodes()) {
                            processNode(node, panelsB);
                        }
                    }
                }

                // TODO rows format??

                panelsB.build();
                dashboardB.build();
                Submodel submodel = smB.build();

                if (null != consumer) {
                    consumer.consume(appAas, submodel, appId);
                }
            }
        }
    }
    
    /**
     * Creates submodel header information.
     * 
     * @param smB the submodel builder
     */
    private void createHeader(SubmodelBuilder smB) {
        createProperty(smB, "oktoVersion", Type.STRING, IipVersion.getInstance().getVersionInfo(), "oktoflow version");
        createProperty(smB, "name", Type.STRING, appName, "application name");
        createProperty(smB, "id", Type.STRING, appId, "application id");
        createProperty(smB, "version", Type.STRING, appVersion, "application version");
    }
    
    /**
     * Creates the dashboard specification collection.
     * 
     * @param smB the parent submodel builder
     */
    private SubmodelElementCollectionBuilder createDashboardSpec(SubmodelBuilder smB) {
        SubmodelElementCollectionBuilder dashboardB = smB.createSubmodelElementCollectionBuilder("Dashboard");
        createProperty(dashboardB, "title", Type.STRING, appName, "application name"); // TODO preliminary the app name
        createProperty(dashboardB, "uid", Type.STRING, appId, "application UID");
        SubmodelElementCollectionBuilder tagsB = dashboardB.createSubmodelElementCollectionBuilder("tags");
        tagsB.build();
        // TODO time_from, time_to, timezone
        return dashboardB;
    }

    /**
     * Processes an IVML graph node, filters influx connectors and transfers the information into individual panels.
     * 
     * @param node the node to process
     * @param panelsB the panels (parent) builder
     */
    private void processNode(IvmlGraphNode node, SubmodelElementCollectionBuilder panelsB) {
        IDecisionVariable var = node.getVariable();
        IDecisionVariable impl = Configuration.dereference(var.getNestedElement("impl"));
        if (null != impl) {
            if (IvmlUtils.isOfCompoundType(impl, "InfluxConnector")) {
                ConnectorInfo connInfo = resolveConnector(impl);
                IDecisionVariable inputVar = impl.getNestedElement("input");
                if (null != inputVar && inputVar.getNestedElementsCount() > 0) { // connectors have only one
                    IDecisionVariable ioTypeVar = inputVar.getNestedElement(0);
                    IDecisionVariable typeVar = Configuration.dereference(ioTypeVar.getNestedElement("type"));
                    processType(resolveType(typeVar), connInfo, panelsB);
                }
            }
        }
    }

    /**
     * Represents relevant information form an (INFLUX) connector.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class ConnectorInfo {
        @SuppressWarnings("unused")
        private String id;
        @SuppressWarnings("unused")
        private String name;
        private String bucket;
        private String measurement;
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
        private Legend legend; // type-level or field-level?
        private PanelPosition position; // type-level or field-level?
    }
    
    private static class Field {
        private String name;
        private String field;
        private String description;
        private String displayName;
        private String unit;
        private String panelType; // values??
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
            result.bucket = IvmlUtils.getStringValue(var, "bucket", "");
            result.measurement = IvmlUtils.getStringValue(var, "measurement", "");
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
     * @return the resolved information, may be <b>null</b>
     */
    private RecordType resolveType(IDecisionVariable var) {
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
                        fld.name = IvmlUtils.getStringValue(fieldVar, "name", "");
                        fld.field = IvmlUtils.getStringValue(fieldVar, "mappedName", fld.name);
                        fld.description = IvmlUtils.getStringValue(fieldVar, "description", "");
                        fld.displayName = IvmlUtils.getStringValue(fieldVar, "displayName", "");
                        fld.unit = resolveSemanticIdToUnit(fieldVar);
                        fld.panelType = resolvePanelType(fieldVar);
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
     * Resolves the panel type.
     * 
     * @param var the variable to take the panel type/display enum from
     * @return the panel type
     */
    private String resolvePanelType(IDecisionVariable var) {
        String displayValue = null;
        IDecisionVariable displayVar = var.getNestedElement("display");
        if (null != displayVar) {
            Value val = displayVar.getValue();
            if (val instanceof EnumValue) {
                displayValue = ((EnumValue) val).getValue().getName();
            }
        }
        // TODO mapping to grafana??
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
            switch (IvmlUtils.getStringValue(type, "name", "")) {
            case "String":
                result = "string";
                break;
            case "Boolean": 
                result = "bool"; // bool_yes_no, bool_on_off
                break;
            default:
                result = "none";
                break;
            }
        }
        return result;
    }

    /**
     * Turns a resolved record type into AAS.
     * 
     * @param type the record type, may be <b>null</b> or not qualified for dashbording
     * @param influx the influx connector information
     * @param panelsB the parent panels builder
     */
    private void processType(RecordType type, ConnectorInfo influx, SubmodelElementCollectionBuilder panelsB) {
        if (null != type && influx != null) {
            for (Field f : type.fields) {
                if (!StringUtils.isBlank(f.unit) && !StringUtils.isBlank(f.panelType)) {
                    SubmodelElementCollectionBuilder panelB = panelsB.createSubmodelElementCollectionBuilder(f.name);
                    createProperty(panelB, "title", Type.STRING, f.name, "Panel title");
                    createProperty(panelB, "unit", Type.STRING, f.unit, "Panel unit");
                    // TODO datasource_uid ?? 
                    createProperty(panelB, "bucket", Type.STRING, influx.bucket, "InfluxDB bucket"); 
                    createProperty(panelB, "measurement", Type.STRING, influx.measurement, "InfluxDB measurement");
                    // TODO fields comma separated??
                    createProperty(panelB, "fields", Type.STRING, f.field, "InfluxDB fields in measurement");
                    createProperty(panelB, "panel_type", Type.STRING, f.panelType, "Panel type");
                    createProperty(panelB, "description", Type.STRING, f.description, "Panel description");
                    createProperty(panelB, "displayName", Type.STRING, f.displayName, "Panel display name");
                    // TODO axis_max_soft
                    // TODO axis_min_soft
                    // TODO axis_label
                    processLegend(type.legend, panelB);
                    processPanelPosition(type.position, panelB);
                    panelB.build();
                }
            }
        }
    }

    /**
     * Turns a panel legend into AAS.
     * 
     * @param position the position
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
     */
    public static void main(String[] args) throws ExecutionException {
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
     * @return the exit code
     * @throws ExecutionException in case that the VIL instantiation fails, shall not occur here as handled by 
     *     default {@link InstantiationConfigurer}
     */
    private static void mainImpl(String[] args) throws ExecutionException {
        String supportFolder = "W:\\offlineFiles\\git\\IIP-ecosphere\\platform\\platform\\support";
        // explicitly load plugins
        PluginManager.registerPlugin(new FolderClasspathPluginSetupDescriptor(
            new File(supportFolder + "/support.aas.basyx")));
        PluginManager.registerPlugin(new FolderClasspathPluginSetupDescriptor(
            new File(supportFolder + "/support.aas.basyx2")));
        PluginManager.registerPlugin(new FolderClasspathPluginSetupDescriptor(
            new File(supportFolder + "/support.yaml-snakeyaml")));
        final String pluginId = "aas.basyx-2.0"; // AasFactory.DEFAULT_PLUGIN_ID
        AasFactory factory = getAasFactory(pluginId); 
        
        ConfigurationSetup setup = ConfigurationSetup.getSetup();
        EasySetup easySetup = setup.getEasyProducer();
        easySetup.reset();
        InstantiationConfigurer configurer = new NonCleaningInstantiationConfigurer(args[0], 
            new File(args[1]), new File("gen")); // gen is actually not used
        if (args.length >= 3) {
            configurer.setIvmlMetaModelFolder(new File(args[2]));
        }
        configurer.configure(setup);
        ConfigurationLifecycleDescriptor lcd = configurer.obtainLifecycleDescriptor();
        lcd.startup(new String[0]); // shall register executor
        Configuration cfg = ConfigurationManager.getIvmlConfiguration();
        try {
            new IvmlDashboardMapper(factory).process(cfg, null, (aas, sm, id) -> {
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
