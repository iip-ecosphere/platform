/**
 * ******************************************************************************
 * Copyright (c) {2022} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.configuration.ivml;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.configuration.ConfigurationManager;
import de.iip_ecosphere.platform.configuration.ConfigurationSetup;
import de.iip_ecosphere.platform.configuration.EasySetup;
import de.iip_ecosphere.platform.configuration.ModelInfo;
import de.iip_ecosphere.platform.configuration.PlatformInstantiator;
import de.iip_ecosphere.platform.configuration.PlatformInstantiator.InstantiationConfigurer;
import de.iip_ecosphere.platform.configuration.ivml.IvmlGraphMapper.IvmlGraph;
import de.iip_ecosphere.platform.configuration.ivml.IvmlGraphMapper.IvmlGraphEdge;
import de.iip_ecosphere.platform.configuration.ivml.IvmlGraphMapper.IvmlGraphNode;
import de.iip_ecosphere.platform.support.aas.InvocablesCreator;
import de.iip_ecosphere.platform.support.aas.LangString;
import de.iip_ecosphere.platform.support.aas.Property.PropertyBuilder;
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection.SubmodelElementCollectionBuilder;
import de.iip_ecosphere.platform.support.aas.Type;
import de.iip_ecosphere.platform.support.iip_aas.AasUtils;
import de.iip_ecosphere.platform.support.iip_aas.json.JsonResultWrapper;
import de.iip_ecosphere.platform.support.iip_aas.json.JsonResultWrapper.OperationCompletedListener;
import de.iip_ecosphere.platform.transport.status.TaskUtils;
import net.ssehub.easy.basics.modelManagement.ModelManagementException;
import net.ssehub.easy.instantiation.core.model.vilTypes.PseudoString;
import net.ssehub.easy.instantiation.core.model.vilTypes.configuration.Configuration;
import net.ssehub.easy.reasoning.core.reasoner.ReasoningResult;
import net.ssehub.easy.varModel.confModel.AssignmentState;
import net.ssehub.easy.varModel.confModel.ConfigurationException;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.model.AbstractVariable;
import net.ssehub.easy.varModel.model.ContainableModelElement;
import net.ssehub.easy.varModel.model.DecisionVariableDeclaration;
import net.ssehub.easy.varModel.model.FreezeBlock;
import net.ssehub.easy.varModel.model.IFreezable;
import net.ssehub.easy.varModel.model.IvmlDatatypeVisitor;
import net.ssehub.easy.varModel.model.ModelQuery;
import net.ssehub.easy.varModel.model.ModelQuery.FirstDeclTypeSelector;
import net.ssehub.easy.varModel.model.ModelQueryException;
import net.ssehub.easy.varModel.model.Project;
import net.ssehub.easy.varModel.model.datatypes.IDatatype;
import net.ssehub.easy.varModel.model.datatypes.TypeQueries;
import net.ssehub.easy.varModel.model.values.ContainerValue;
import net.ssehub.easy.varModel.model.values.ReferenceValue;
import net.ssehub.easy.varModel.model.values.Value;

/**
 * Maps an IVML configuration generically into an AAS with references to IIP-Ecosphere.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AasIvmlMapper extends AbstractIvmlModifier {
    
    public static final String OP_CHANGE_VALUES = "changeValues";
    public static final String OP_GET_GRAPH = "getGraph";
    public static final String OP_SET_GRAPH = "setGraph";
    public static final String OP_DELETE_GRAPH = "deleteGraph";
    public static final String OP_CREATE_VARIABLE = "createVariable";
    public static final String OP_DELETE_VARIABLE = "deleteVariable";
    public static final String OP_GEN_APPS_NO_DEPS = "genAppsNoDepsAsync";
    public static final String OP_GEN_APPS = "genAppsAsync";
    
    public static final Predicate<IDecisionVariable> FILTER_NO_CONSTRAINT_VARIABLES = 
        v -> !TypeQueries.isConstraint(v.getDeclaration().getType());
    public static final Function<String, String> SHORTID_PREFIX_META = n -> "meta" + PseudoString.firstToUpperCase(n);
    protected static final String PRJ_NAME_ALLSERVICES = "AllServices";
    protected static final String PRJ_NAME_ALLTYPES = "AllTypes";
    protected static final String PRJ_NAME_TECHSETUP = "TechnicalSetup";
    private static final TypeVisitor TYPE_VISITOR = new TypeVisitor();
    private static final String PROGRESS_COMPONENT_ID = "configuration.configuration";

    private Supplier<Configuration> cfgSupplier;
    private Function<String, String> metaShortId = SHORTID_PREFIX_META;
    private Predicate<IDecisionVariable> variableFilter = FILTER_NO_CONSTRAINT_VARIABLES;
    private OperationCompletedListener aasOpListener;
    
    /**
     * Creates a mapper with default settings, e.g., short ids for meta IVML information are
     * prefixed by "meta" ({@link #SHORTID_PREFIX_META}) and the variable filter excludes all IVML constraint 
     * variables ({@link #FILTER_NO_CONSTRAINT_VARIABLES}).
     * 
     * @param cfgSupplier a supplier providing the actual configuration instance
     * @param graphMapper maps a graph from IVML to an internal structure
     * @param changeListener optional configuration change listener, may be <b>null</b>
     * @param opListener optional operation completed listener, may be <b>null</b>
     * @throws IllegalArgumentException if {@code cfgSupplier} is <b>null</b>
     */
    public AasIvmlMapper(Supplier<Configuration> cfgSupplier, IvmlGraphMapper graphMapper, 
        ConfigurationChangeListener changeListener, OperationCompletedListener opListener) {
        super(graphMapper, changeListener);
        if (null == cfgSupplier) {
            throw new IllegalArgumentException("cfgSupplier must not be null");
        }
        this.cfgSupplier = cfgSupplier;
        this.aasOpListener = opListener;
    }
    
    /**
     * Defines a function that turns an IVML name for a meta value, e.g., type or status, into an AAS short id 
     * candidate. The result of a function call will be validated by {@link AasUtils#fixId(String)}. The default
     * value is to prefix a given name with "meta".
     * 
     * @param metaShortId the functor, ignored if <b>null</b>
     */
    public void setShortIdToMeta(Function<String, String> metaShortId) {
        if (null != metaShortId) {
            this.metaShortId = metaShortId;
        }
    }
    
    /**
     * Defines a predicate acting as IVML variable filter, i.e., variables to be included into the configuration AAS.
     * 
     * @param variableFilter the predicate, ignored if <b>null</b>
     */
    public void setVariableFilter(Predicate<IDecisionVariable> variableFilter) {
        if (null != variableFilter) {
            this.variableFilter = variableFilter;
        }
    }
    
    /**
     * Maps {@code cfg} into the submodel represented by {@code smBuilder}.
     * 
     * @param smBuilder the submodel builder representing the target
     * @param iCreator the invocables creator for operations
     */
    public void mapByType(SubmodelBuilder smBuilder, InvocablesCreator iCreator) {
        Map<String, SubmodelElementCollectionBuilder> types = new HashMap<>();
        Configuration cfg = cfgSupplier.get();
        if (null != cfg) { // as long as we are in transition from platform without contained model to this
            IDatatype primitiveType = null; 
            try {
                primitiveType = ModelQuery.findType(cfg.getConfiguration().getProject(), "PrimitiveType", null);
            } catch (ModelQueryException e) {
            }
            Iterator<IDecisionVariable> iter = cfg.getConfiguration().iterator();
            while (iter.hasNext()) {
                IDecisionVariable var = iter.next();
                if (variableFilter.test(var)) {
                    IDatatype type = var.getDeclaration().getType();
                    if (primitiveType != null && primitiveType.isAssignableFrom(type)) {
                        type = primitiveType; // group them together to simplify the AAS structure
                    } 
                    String typeName = IvmlDatatypeVisitor.getUnqualifiedType(type);                    
                    SubmodelElementCollectionBuilder builder = types.get(typeName);
                    if (null == builder) {
                        builder = createTypeCollectionBuilder(smBuilder, typeName);
                        types.put(typeName, builder);
                    }
                    mapVariable(var, builder, null);
                }
            }
            for (SubmodelElementCollectionBuilder builder : types.values()) {
                builder.build();
            }
            addOperations(smBuilder, iCreator);
        } else {
            LoggerFactory.getLogger(AasIvmlMapper.class).warn("No IVML configuration found. "
                + "Cannot create IVML-AAS model elements/operations.");
        }
    }
    
    /**
     * Creates a AAS collection representing an IVML type.
     * 
     * @param smBuilder the submodel builder
     * @param typeName the type name (turned into an AAS shortID)
     * @return the collection builder
     */
    private static SubmodelElementCollectionBuilder createTypeCollectionBuilder(SubmodelBuilder smBuilder, 
        String typeName) {
        return smBuilder.createSubmodelElementCollectionBuilder(AasUtils.fixId(typeName), true, false);
    }
    
    /**
     * Binds the AAS operations.
     * 
     * @param sBuilder the server builder
     */
    public void bindOperations(ProtocolServerBuilder sBuilder) {
        sBuilder.defineOperation(OP_CHANGE_VALUES, 
            new JsonResultWrapper(a -> {
                changeValues(AasUtils.readMap(a, 0, null));
                return null;
            }, aasOpListener)
        );
        sBuilder.defineOperation(OP_GET_GRAPH, 
            new JsonResultWrapper(a -> getGraph(AasUtils.readString(a, 0), AasUtils.readString(a, 1)), 
                aasOpListener));
        sBuilder.defineOperation(OP_SET_GRAPH, 
            new JsonResultWrapper(a ->  
                setGraph(AasUtils.readString(a, 0), AasUtils.readString(a, 1), AasUtils.readString(a, 2), 
                    AasUtils.readString(a, 3), AasUtils.readString(a, 4)), 
                aasOpListener
            ));
        sBuilder.defineOperation(OP_DELETE_GRAPH, 
            new JsonResultWrapper(a ->  
                deleteGraph(AasUtils.readString(a, 0), AasUtils.readString(a, 1)), 
                aasOpListener
            ));
        sBuilder.defineOperation(OP_CREATE_VARIABLE, 
            new JsonResultWrapper(a -> {
                createVariable(AasUtils.readString(a, 0), AasUtils.readString(a, 1), AasUtils.readString(a, 2));
                return null;
            }, aasOpListener)
        );
        sBuilder.defineOperation(OP_DELETE_VARIABLE, 
            new JsonResultWrapper(a -> {
                deleteVariable(AasUtils.readString(a));
                return null;
            }, aasOpListener)
        );
        sBuilder.defineOperation(OP_GEN_APPS, 
            new JsonResultWrapper(a -> {
                return TaskUtils.executeAsTask(PROGRESS_COMPONENT_ID, 
                    p -> instantiate(createInstantiationConfigurer(false)));
            })
        );
        sBuilder.defineOperation(OP_GEN_APPS_NO_DEPS, 
            new JsonResultWrapper(a -> {
                return TaskUtils.executeAsTask(PROGRESS_COMPONENT_ID, 
                    p -> instantiate(createInstantiationConfigurer(true)));
            })
        );
    }
    
    /**
     * Instantiates according to the given {@code configurer}.
     * 
     * @param configurer the configurer
     * @return <b>null</b>
     * @throws ExecutionException when the instantiation fails
     */
    private Object instantiate(InstantiationConfigurer configurer) throws ExecutionException {
        PlatformInstantiator.instantiate(configurer);
        return null;
    }
    
    /**
     * Creates an instantiation configurer from {@link ConfigurationSetup} to create application code.
     * 
     * @param noDeps do a no-deps generation or do a full generation
     * @return the configurer
     */
    private InstantiationConfigurer createInstantiationConfigurer(boolean noDeps) {
        EasySetup ep = ConfigurationSetup.getSetup().getEasyProducer();
        InstantiationConfigurer result = new InstantiationConfigurer(
            ep.getIvmlModelName(), getIvmlConfigFolder(ep), ep.getGenTarget());
        if (noDeps) {
            result.setStartRuleName("generateAppsNoDeps");
        } else {
            result.setStartRuleName("generateApps");
        }
        return result;
    }

    @Override
    protected Project getVariableTarget(Project root, IDatatype type) {
        Project result = null;
        if (null != type) {
            try {
                IDatatype serviceType = ModelQuery.findType(root, "ServiceBase", null);
                if (null != serviceType && serviceType.isAssignableFrom(type)) {
                    result = ModelQuery.findProject(root, PRJ_NAME_ALLSERVICES);
                } else {
                    IDatatype dataType = ModelQuery.findType(root, "DataType", null);
                    if (null != dataType && dataType.isAssignableFrom(type)) {
                        result = ModelQuery.findProject(root, PRJ_NAME_ALLTYPES);
                    }
                }
            } catch (ModelQueryException e) {
                LoggerFactory.getLogger(AasIvmlMapper.class).warn(
                    "Cannot find type. Target of new variable will be the root project. {}", e.getMessage());
            }
        }
        if (null == result) {
            result = root;
        }
        return result;
    }
    
    @Override
    protected boolean isAllowedForModification(Project prj) {
        String name = prj.getName();
        return PRJ_NAME_ALLTYPES.equals(name) 
            || PRJ_NAME_ALLSERVICES.equals(name) 
            || PRJ_NAME_TECHSETUP.equals(name);
    }

    
    @Override
    protected String getIvmlSubpath(Project project) {
        String projectName = project.getName();
        String subpath;
        if (projectName.startsWith("ServiceMeshPart")) {
            subpath = "meshes";
        } else if (projectName.startsWith("ApplicationPart")) {
            subpath = "apps";
        } else {
            subpath = null;
        }
        return subpath;
    }

    /**
     * Returns the actual IVML config folder.
     * 
     * @param ep the EASy setup instance
     * @return the config folder
     */
    private File getIvmlConfigFolder(EasySetup ep) {
        File result = ep.getIvmlConfigFolder();
        if (null == result || result.toString().equals(".")) {
            result = ep.getBase();
        }
        return result;
    }
    
    @Override
    protected File createIvmlConfigPath(String subpath, Project project) {
        EasySetup ep = ConfigurationSetup.getSetup().getEasyProducer();
        File result = getIvmlConfigFolder(ep);
        if (subpath != null) {
            result = new File(result, subpath);
        }
        return new File(result, project.getName() + ".ivml");
    }

    /**
     * Deletes a graph structure in IVML. [public for testing]
     * 
     * @param appName the configured name of the application
     * @param meshName the configured name of the service mesh to delete a specific mesh in {@code appName}, 
     *     may be <b>null</b> or empty to delete the entire app
     * @return <b>null</b> always
     * @throws ExecutionException if setting the graph structure fails
     */
    public synchronized Object deleteGraph(String appName, String meshName) throws ExecutionException {
        net.ssehub.easy.varModel.confModel.Configuration cfg = getIvmlConfiguration();
        Project root = cfg.getProject();
        Project appProject = ModelQuery.findProject(root, getApplicationProjectName(appName));
        if (null != appProject) {
            try {
                Map<Project, CopiedFile> copies = new HashMap<>();
                if (meshName != null) {
                    Project meshProject = ModelQuery.findProject(root, getMeshProjectName(appName, meshName));
                    IDatatype meshType = ModelQuery.findType(root, "ServiceMesh", null);
                    DecisionVariableDeclaration meshVarDecl = ModelQuery.findDeclaration(
                        meshProject, new FirstDeclTypeSelector(meshType));
                    if (null != meshVarDecl) {
                        IDatatype appType = ModelQuery.findType(root, "Application", null);
                        DecisionVariableDeclaration appVarDecl = ModelQuery.findDeclaration(
                            appProject, new FirstDeclTypeSelector(appType));
                        if (null != appVarDecl) {
                            IDecisionVariable var = cfg.getDecision(appVarDecl);
                            IDecisionVariable svc = var.getNestedElement("services");
                            deleteReferenceFromContainerValue(svc, meshVarDecl); 
                        }
                    }
                    File f = getIvmlFile(meshProject);
                    copies.put(meshProject, copyToTmp(f));
                    f.delete();
                    notifyChange(meshProject, ConfigurationChangeType.DELETED);
                }
                if (null == meshName) {
                    File f = getIvmlFile(appProject);
                    copies.put(appProject, copyToTmp(f));
                    f.delete();
                    notifyChange(appProject, ConfigurationChangeType.DELETED);
                }
                reloadAndValidate(copies);
            } catch (ModelQueryException e) {
                throw new ExecutionException(e);
            }
        }
        return null;
    }

    /**
     * Deletes a reference from a container value and sets the new value.
     * 
     * @param var the variable to take the value from, may be <b>null</b>
     * @param search the variable declaration to search the reference for
     * @return the new container value, may be <b>null</b> for not found
     * @throws ExecutionException if re-assigning the value fails
     */
    private ContainerValue deleteReferenceFromContainerValue(IDecisionVariable var, AbstractVariable search) 
        throws ExecutionException {
        ContainerValue val = null;
        if (null != var && var.getValue() instanceof ContainerValue) {
            val = (ContainerValue) var.getValue();
            int eltSize = val.getElementSize();
            for (int e = eltSize - 1; e >= 0; e--) {
                if (val.getElement(e) instanceof ReferenceValue) {
                    if (((ReferenceValue) val.getElement(e)).getValue() == search) {
                        val.removeElement(e);
                    }
                }
            }
            if (eltSize != val.getElementSize()) {
                var.unfreeze(AssignmentState.ASSIGNED);
                try {
                    var.setValue(val, AssignmentState.FROZEN);
                } catch (ConfigurationException e) {
                    throw new ExecutionException(e);
                }
            }
        }
        return val;
    }

    /**
     * Changes a graph structure in IVML. [public for testing]
     * 
     * @param appName the configured name of the application
     * @param appValueEx the application value as IVML expression
     * @param meshName the configured name of the service mesh
     * @param format the format of the graph
     * @param value the value
     * @return <b>null</b> always
     * @throws ExecutionException if setting the graph structure fails
     */
    public synchronized Object setGraph(String appName, String appValueEx, String meshName, String format, 
        String value) throws ExecutionException {
        GraphFormat gFormat = getGraphFormat(format);
        IvmlGraph graph = gFormat.fromString(value, getMapper().getGraphFactory(), this);
        
        try {
            ModelResults results = new ModelResults();
            createMeshProject(appName, meshName, graph, results);
            createAppProject(appName, appValueEx, results);

            Map<Project, CopiedFile> copies = new HashMap<>();
            File meshFile = getIvmlFile(results.meshProject);
            copies.put(results.meshProject, copyToTmp(meshFile));
            File appFile = getIvmlFile(results.appProject);
            copies.put(results.appProject, copyToTmp(appFile));
            saveTo(results.meshProject, meshFile);
            saveTo(results.appProject, appFile);
            reloadAndValidate(copies);
        } catch (ModelQueryException | ModelManagementException e) {
            throw new ExecutionException(e);
        }
        return null;
    }

    /**
     * Stores intermediary model creation results.
     * 
     * @author Holger Eichelberger, SSE
     */
    protected static class ModelResults {
        
        private Project meshProject;
        private DecisionVariableDeclaration meshVar;
        private Project appProject;
        
    }
    
    /**
     * Returns the IVML project name for an application.
     * 
     * @param appName the application name
     * @return the project name
     */
    private String getApplicationProjectName(String appName) {
        return "ApplicationPart" + toIdentifierFirstUpper(appName);
    }
    
    /**
     * Returns the IVML project name for a service mesh.
     * 
     * @param appName the application name
     * @param meshName the configured name of the service mesh
     * @return the project name
     */
    private String getMeshProjectName(String appName, String meshName) {
        return "ServiceMeshPart" + toIdentifierFirstUpper(appName) + toIdentifierFirstUpper(meshName);
    }
    
    /**
     * Writes an application project with the given new mesh variable.
     * 
     * @param appName the application name
     * @param appValueEx the IVML value expression for the application
     * @param results collected result information
     * @throws ModelQueryException if IVML types/variables cannot be found
     * @throws ModelManagementException if IVML types/variables cannot be found
     * @throws ExecutionException if the application value cannot be set
     */
    private void createAppProject(String appName, String appValueEx, ModelResults results) 
        throws ModelQueryException, ModelManagementException, ExecutionException {
        net.ssehub.easy.varModel.confModel.Configuration cfg = getIvmlConfiguration();
        Project root = cfg.getProject();
        IDatatype applicationType = ModelQuery.findType(root, "Application", null);
        
        String appProjectName = getApplicationProjectName(appName);
        results.appProject = findOrCreateProject(root, appProjectName, true); 
        addImport(results.appProject, "Applications", root, null);
        addImport(results.appProject, "ServiceMeshPart*", root, results.meshProject);
        List<Object> meshes = new ArrayList<Object>();
        boolean replaced = false;
        if (results.appProject != null) {
            for (int e = 0; e < results.appProject.getElementCount(); e++) {
                ContainableModelElement elt = results.appProject.getElement(e);
                if (elt instanceof DecisionVariableDeclaration) {
                    IDecisionVariable var = cfg.getDecision((DecisionVariableDeclaration) elt);
                    Value val = var.getNestedElement("services").getValue();
                    if (val instanceof ContainerValue) {
                        ContainerValue cValue = (ContainerValue) val;
                        for (int v = 0; v < cValue.getElementSize(); v++) {
                            Value mVal = cValue.getElement(v);
                            if (isRefWithName(appName, cfg, mVal)) {
                                replaced = true;
                                meshes.add(results.meshVar);
                            } else {
                                meshes.add(mVal); // TODO value conversion?
                            }
                        }
                    }
                }
            }
        }
        if (!replaced) {
            meshes.add(results.meshVar);
        }
            
        DecisionVariableDeclaration appVar = new DecisionVariableDeclaration(
            toIdentifier(appName), applicationType, results.appProject);
        results.appProject.add(appVar);
        setValue(appVar, appValueEx);
        notifyChange(results.appProject, ConfigurationChangeType.CREATED); // may be modified, shall work anyway
    }

    /**
     * Returns if {@code mVal} is a reference to a variable with a field name with value {@code name}.
     * 
     * @param name the name to look for
     * @param cfg the configuration to resolve the reference
     * @param mVal the value possibly containing a reference value
     * @return {@code true} if it is a reference with the desired property, {@code false} else
     */
    private static boolean isRefWithName(String name, net.ssehub.easy.varModel.confModel.Configuration cfg, 
        Value mVal) {
        boolean result = false;
        if (mVal instanceof ReferenceValue) {
            ReferenceValue rVal = (ReferenceValue) mVal;
            IDecisionVariable var2 = cfg.getDecision(rVal.getValue());
            Object var2n = getValue(var2.getNestedElement("name"));
            String var2Name = null == var2n ? "" : var2n.toString();
            if (var2Name.equals(name)) {
                result = true;
            }
        }
        return result;
    }
    
    @Override
    protected Configuration getVilConfiguration() {
        return cfgSupplier.get();
        //return ConfigurationManager.getVilConfiguration();
    }
    
    @Override
    protected net.ssehub.easy.varModel.confModel.Configuration getIvmlConfiguration() {
        return ConfigurationManager.getIvmlConfiguration();
    }
    
    @Override
    protected ReasoningResult validateAndPropagate() {
        return ConfigurationManager.validateAndPropagate();
    }

    @Override
    protected void reloadConfiguration() {
        ConfigurationManager.reload();
    }

    /**
     * Finds an existing IVML project in {@code scope} with given name or creates a new one with 
     * default freeze block.
     * 
     * @param scope the scope to look for
     * @param projectName the project name
     * @param find try to find the project or directly create a new project 
     * @return the project, created or found
     */
    private Project findOrCreateProject(Project scope, String projectName, boolean find) {
        Project result = find ? ModelQuery.findProject(scope, projectName) : null;
        if (null == result) {
            result = new Project(projectName);
            IFreezable[] freezables = new IFreezable[] {result};
            // TODO add condition
            FreezeBlock freeze = new FreezeBlock(freezables, null, null, result);
            result.add(freeze);
        }
        return result;
    }

    /**
     * Creates a mesh project for {@code graph}.
     * 
     * @param appName the application name
     * @param meshName the mesh name
     * @param graph the graph to create the mesh project for
     * @param results the results to be modified as a side effect
     * @throws ModelQueryException if IVML types/variables cannot be found
     * @throws ModelManagementException if IVML types/variables cannot be found
     * @throws ExecutionException if setting IVML values fails
     */
    private void createMeshProject(String appName, String meshName, IvmlGraph graph, ModelResults results) 
        throws ModelQueryException, ModelManagementException, ExecutionException {
        net.ssehub.easy.varModel.confModel.Configuration cfg = getIvmlConfiguration();
        Project root = cfg.getProject();
        String meshProjectName = getMeshProjectName(appName, meshName);
        results.meshProject = findOrCreateProject(root, meshProjectName, false); // just overwrite
        addImport(results.meshProject, "Applications", root, null);
        IDatatype sourceType = ModelQuery.findType(root, "MeshSource", null);
        IDatatype transformerType = ModelQuery.findType(root, "MeshTransformer", null);
        IDatatype sinkType = ModelQuery.findType(root, "MeshSink", null);
        IDatatype connectorType = ModelQuery.findType(root, "MeshConnector", null);
        IDatatype serviceType = ModelQuery.findType(root, "ServiceBase", null);
        IDatatype meshType = ModelQuery.findType(root, "ServiceMesh", null);
        ServiceMap services = collectServices(cfg, serviceType);
        Map<IvmlGraphNode, DecisionVariableDeclaration> nodeMap = new HashMap<>();
        Map<DecisionVariableDeclaration, String> valueMap = new HashMap<>();
        List<DecisionVariableDeclaration> sources = new ArrayList<>();
        for (IvmlGraphNode n : graph.nodes()) {
            IDatatype type;
            if (n.getInEdgesCount() == 0) {
                type = sourceType;
            } else if (n.getOutEdgesCount() == 0) {
                type = sinkType;
            } else {
                type = transformerType; // TODO Probe service
            }
            DecisionVariableDeclaration nodeVar = new DecisionVariableDeclaration("node_" + n.getName(), type, 
                results.meshProject);
            String nodeValEx = "{pos_x=" + n.getXPos() + ",pos_y=" + n.getYPos() 
                + ",impl=" + IvmlUtils.getVarNameSafe(findServiceVar(services, n.getImpl()), "null")
                + ",next = {";
            valueMap.put(nodeVar, nodeValEx);
            results.meshProject.add(nodeVar);
            nodeMap.put(n, nodeVar);
            if (type == sourceType) {
                sources.add(nodeVar);
            }
        }
        boolean first = true;
        int edgeCounter = 0;
        for (IvmlGraphNode n: graph.nodes()) {
            for (IvmlGraphEdge e: n.outEdges()) {
                String edgeName = e.getName();
                String edgeVarName = "conn_" + toIdentifier(edgeName);
                if (null == edgeName || edgeName.length() == 0) {
                    edgeName = n.getName() + " -> " + e.getEnd().getName();
                    edgeVarName = "conn_" + edgeCounter++;
                }
                DecisionVariableDeclaration edgeVar = new DecisionVariableDeclaration(
                    edgeVarName, connectorType, results.meshProject);
                results.meshProject.add(edgeVar);
                DecisionVariableDeclaration end = nodeMap.get(e.getEnd());
                String valueEx = "{name=\"" + edgeName + "\", next=refBy(" + end.getName() + ")}";
                setValue(edgeVar, valueEx);
                String startNodeValueEx = valueMap.get(nodeMap.get(n));
                if (!first) {
                    startNodeValueEx += ",";
                }
                startNodeValueEx += "refBy(" + edgeVarName + ")";
                valueMap.put(nodeMap.get(n), startNodeValueEx);
                first = false;
            }
        }
        for (IvmlGraphNode n: graph.nodes()) {
            DecisionVariableDeclaration nodeVar = nodeMap.get(n);
            setValue(nodeVar, valueMap.get(nodeVar) + "}}"); 
        }
        results.meshVar = new DecisionVariableDeclaration(toIdentifier(meshName), meshType, results.meshProject);
        results.meshProject.add(results.meshVar);
        String meshValEx = "{description=\"" + meshName + "\", sources={";
        for (int s = 0; s < sources.size(); s++) {
            if (s > 0) {
                meshValEx += ", ";
            }
            meshValEx += "refBy(" + sources.get(s).getName() + ")";
        }
        meshValEx += "}}";
        setValue(results.meshVar, meshValEx);
        notifyChange(results.meshProject, ConfigurationChangeType.CREATED);
    }
    
    /**
     * A "map" holding names/id to service mappings.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class ServiceMap {
        
        private Map<String, IDecisionVariable> nameToService = new HashMap<>();
        private Map<String, IDecisionVariable> idToService = new HashMap<>();
        
        /**
         * Returns a service by id or by configured name.
         * 
         * @param svc the service id/name
         * @return the resolved service, may be <b>null</b>
         */
        public IDecisionVariable getService(String svc) {
            IDecisionVariable result = nameToService.get(svc);
            if (null == result) {
                result = idToService.get(svc);
            }
            return result;
        }
        
        /**
         * Adds a service.
         * 
         * @param var the configured variable representing the service
         */
        private void add(IDecisionVariable var) {
            String name = var.getDeclaration().getName(); // just fallback
            name = IvmlUtils.getStringValue(var.getNestedElement("name"), name);
            if (null != name) {
                nameToService.put(name, var);
            }
            String id = IvmlUtils.getStringValue(var.getNestedElement("id"), name);
            if (null != id) {
                idToService.put(id, var);
            }
        }
        
    }
    
    /**
     * Collects all declared services.
     * 
     * @param cfg the configuration to take the services from
     * @param serviceType the IVML data type used to select services
     * @return a mapping between service names and configured IVML variables
     */
    private static ServiceMap collectServices(net.ssehub.easy.varModel.confModel.Configuration cfg, 
        IDatatype serviceType) {
        ServiceMap result = new ServiceMap();
        Iterator<IDecisionVariable> iter = cfg.iterator();
        while (iter.hasNext()) {
            IDecisionVariable cVar = iter.next();
            if (serviceType.isAssignableFrom(cVar.getDeclaration().getType())) {
                result.add(cVar);
            }
        }
        return result;
    }

    /**
     * Finds a service as IVML variable. 
     * 
     * @param services the mapped services
     * @param name the service name to search for
     * @return the service IVML variable, or <b>null</b> for not found
     */
    private static AbstractVariable findServiceVar(ServiceMap services, String name) {
        AbstractVariable result = null;
        IDecisionVariable cVar = services.getService(name);
        if (null != cVar) {
            result = cVar.getDeclaration();
        }
        return result;
    }
    
    /**
     * Adds the default AAS operations for obtaining information on the configuration model / changing the model.
     * 
     * @param smBuilder the submodel builder representing the target
     * @param iCreator the invocables creator for operations
     */
    private void addOperations(SubmodelBuilder smBuilder, InvocablesCreator iCreator) {
        smBuilder.createOperationBuilder(OP_CHANGE_VALUES)
            .addInputVariable("valueExprs", Type.STRING)
            .setInvocable(iCreator.createInvocable(OP_CHANGE_VALUES))
            .build(Type.NONE);
        smBuilder.createOperationBuilder(OP_GET_GRAPH)
            .addInputVariable("varName", Type.STRING)
            .addInputVariable("format", Type.STRING)
            .setInvocable(iCreator.createInvocable(OP_GET_GRAPH))
            .build(Type.STRING);
        smBuilder.createOperationBuilder(OP_SET_GRAPH)
            .addInputVariable("appName", Type.STRING)
            .addInputVariable("appValExpr", Type.STRING)
            .addInputVariable("serviceMeshName", Type.STRING)
            .addInputVariable("format", Type.STRING)
            .addInputVariable("val", Type.STRING)
            .setInvocable(iCreator.createInvocable(OP_SET_GRAPH))
            .build(Type.NONE);
        smBuilder.createOperationBuilder(OP_DELETE_GRAPH)
            .addInputVariable("appName", Type.STRING)
            .addInputVariable("serviceMeshName", Type.STRING)
            .setInvocable(iCreator.createInvocable(OP_DELETE_GRAPH))
            .build(Type.NONE);
        smBuilder.createOperationBuilder(OP_CREATE_VARIABLE)
            .addInputVariable("varName", Type.STRING)
            .addInputVariable("type", Type.STRING)
            .addInputVariable("valExpr", Type.STRING)
            .setInvocable(iCreator.createInvocable(OP_CREATE_VARIABLE))
            .build(Type.NONE);
        smBuilder.createOperationBuilder(OP_DELETE_VARIABLE)
            .addInputVariable("varName", Type.STRING)
            .setInvocable(iCreator.createInvocable(OP_DELETE_VARIABLE))
            .build(Type.NONE);
        smBuilder.createOperationBuilder(OP_GEN_APPS)
            .setInvocable(iCreator.createInvocable(OP_GEN_APPS))
            .build(Type.STRING);
        smBuilder.createOperationBuilder(OP_GEN_APPS_NO_DEPS)
            .setInvocable(iCreator.createInvocable(OP_GEN_APPS_NO_DEPS))
            .build(Type.STRING);
    }
    
    /**
     * Maps a single variable {@code var} into {@code builder}.
     * 
     * @param var the variable to map as source
     * @param builder the builder as target
     * @param id the id to use as variable name instead of the variable name itself, may be <b>null</b> for 
     *     the variable name
     */
    @SuppressWarnings("unchecked")
    void mapVariable(IDecisionVariable var, SubmodelElementCollectionBuilder builder, String id) {
        if (variableFilter.test(var)) {
            AbstractVariable decl = var.getDeclaration();
            String varName = decl.getName();
            IDatatype varType = decl.getType();
            String lang = ModelInfo.getLocale().getLanguage();
            String semanticId = null;
            for (int a = 0; a < var.getAttributesCount(); a++) {
                IDecisionVariable attribute = var.getAttribute(a);
                if ("semanticId".equals(attribute.getDeclaration().getName())) {
                    Object val = getValue(var);
                    if (null != val) {
                        semanticId = val.toString();
                    }
                }
            }            
            if (TypeQueries.isCompound(varType)) {
                SubmodelElementCollectionBuilder varBuilder = builder.createSubmodelElementCollectionBuilder(
                    AasUtils.fixId(varName), false, false);
                for (int member = 0; member < var.getNestedElementsCount(); member++) {
                    mapVariable(var.getNestedElement(member), varBuilder, null);
                }
                varBuilder.build();
            } else if (TypeQueries.isContainer(varType)) {
                boolean isSequence = TypeQueries.isSequence(varType);
                boolean isOrdered = isSequence; // just to clarify
                boolean allowsDuplicates = isSequence; // just to clarify
                SubmodelElementCollectionBuilder varBuilder = builder.createSubmodelElementCollectionBuilder(
                    AasUtils.fixId(varName), isOrdered, allowsDuplicates);
                for (int member = 0; member < var.getNestedElementsCount(); member++) {
                    mapVariable(var.getNestedElement(member), varBuilder, "var_" + member);
                }
                PropertyBuilder pb = varBuilder.createPropertyBuilder(AasUtils.fixId(metaShortId.apply("size")))
                    .setDescription(new LangString(ModelInfo.getCommentSafe(decl), lang))
                    .setValue(Type.INTEGER, var.getNestedElementsCount());
                setSemanticId(pb, semanticId);
                pb.build();
                varBuilder.build();
            } else {
                Object aasValue = getValue(var);
                varType.getType().accept(TYPE_VISITOR);
                Type aasType = TYPE_VISITOR.getAasType();
                String propName = id == null ? varName : id;
                PropertyBuilder pb = builder.createPropertyBuilder(AasUtils.fixId(propName));
                if (var.getState() == AssignmentState.FROZEN) {
                    pb.setValue(aasType, aasValue);
                } else {
                    pb.setType(aasType).bind(
                        ((Supplier<Object> & Serializable) () -> getValue(var)), 
                        PropertyBuilder.READ_ONLY);
                }
                setSemanticId(pb, semanticId);
                pb.build();
            }
            builder.createPropertyBuilder(AasUtils.fixId(metaShortId.apply("state")))
                .setValue(Type.STRING, var.getState().toString())
                .build();
            builder.createPropertyBuilder(AasUtils.fixId(metaShortId.apply("type")))
                .setValue(Type.STRING, IvmlDatatypeVisitor.getUnqualifiedType(varType))
                .build();
        }
    }
    
    /**
     * Sets a semantic id if available. 
     * 
     * @param pBuilder the property builder
     * @param semanticId the semantic id, may be <b>null</b>
     */
    private static void setSemanticId(PropertyBuilder pBuilder, String semanticId) {
        if (null != semanticId) {
            pBuilder.setSemanticId(semanticId);
        }
    }

    /**
     * Returns the value of a variable.
     * 
     * @param var the variable
     * @return the value
     */
    private static Object getValue(IDecisionVariable var) {
        Object aasValue = null;
        if (null != var && null != var.getValue()) {
            ValueVisitor valueVisitor = new ValueVisitor();
            var.getValue().accept(valueVisitor);
            aasValue = valueVisitor.getAasValue();
        }
        return aasValue;
    }

    /**
     * Deletes the mapping of the specified variable in the AAS.
     * 
     * @param sm the submodel to delete from
     * @param typeName the type name
     * @param varName the variable name
     */
    private static void deleteAasVariableMapping(Submodel sm, String typeName, String varName) {
        SubmodelElementCollection c = sm.getSubmodelElementCollection(AasUtils.fixId(typeName));
        c.deleteElement(AasUtils.fixId(varName));
    }

    /**
     * Maps {@code var} into the submodel represented by {@code smB}.
     * 
     * @param smB the submodel builder
     * @param var the variable
     */
    private void mapVariableToAas(SubmodelBuilder smB, IDecisionVariable var) {
        SubmodelElementCollectionBuilder builder = createTypeCollectionBuilder(smB, getType(var));
        mapVariable(var, builder, null);
        builder.build();
    }
    
    /**
     * Records an AAS change.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class AasChange {
        
        private IDecisionVariable var;
        private ConfigurationChangeType type;

        /**
         * Creates an instance.
         * 
         * @param var the decision variable
         * @param type the change type
         */
        public AasChange(IDecisionVariable var, ConfigurationChangeType type) {
            this.var = var;
            this.type = type;
        }
        
        /**
         * Applies the change.
         * 
         * @param mapper the mapper instance
         * @param sm the submodel containing the configuration
         * @param smB the submodel builder of {@code sm} for modifications
         */
        public void apply(AasIvmlMapper mapper, Submodel sm, SubmodelBuilder smB) {
            AbstractVariable decl = var.getDeclaration();
            String varName = decl.getName();
            String typeName = getType(var);

            deleteAasVariableMapping(sm, typeName, varName); // throw away, do nothing if not exists
            switch (type) {
            case CREATED:
                mapper.mapVariableToAas(smB, var);
                break;
            case DELETED:
                break;
            case MODIFIED:
                mapper.mapVariableToAas(smB, var);
                break;
            default:
                break;
            }
        }
        
    }

}
