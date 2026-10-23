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

package de.iip_ecosphere.platform.configuration.easyProducer.ivml;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import de.iip_ecosphere.platform.configuration.cfg.ConfigurationChangeType;
import de.iip_ecosphere.platform.configuration.cfg.ConfigurationFactory;
import de.iip_ecosphere.platform.configuration.cfg.PlatformInstantiation;
import de.iip_ecosphere.platform.configuration.easyProducer.ConfigurationLifecycleDescriptor.ExecutionMode;
import de.iip_ecosphere.platform.configuration.easyProducer.ConfigurationLifecycleDescriptor.SenderCloseable;
import de.iip_ecosphere.platform.configuration.easyProducer.ConfigurationLifecycleDescriptor;
import de.iip_ecosphere.platform.configuration.easyProducer.ConfigurationManager;
import de.iip_ecosphere.platform.configuration.easyProducer.ConfigurationSetup;
import de.iip_ecosphere.platform.configuration.easyProducer.EasySetup;
import de.iip_ecosphere.platform.configuration.easyProducer.ModelInfo;
import de.iip_ecosphere.platform.configuration.easyProducer.PlatformInstantiator;
import de.iip_ecosphere.platform.configuration.easyProducer.ivml.IvmlGraphMapper.IvmlGraph;
import de.iip_ecosphere.platform.configuration.easyProducer.ivml.IvmlGraphMapper.IvmlGraphEdge;
import de.iip_ecosphere.platform.configuration.easyProducer.ivml.IvmlGraphMapper.IvmlGraphNode;
import de.iip_ecosphere.platform.support.aas.InvocablesCreator;
import de.iip_ecosphere.platform.support.aas.LangString;
import de.iip_ecosphere.platform.support.aas.Property.PropertyBuilder;
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder;
import de.iip_ecosphere.platform.support.aas.Registry;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElementContainerBuilder;
import de.iip_ecosphere.platform.support.aas.Type;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.support.FileUtils;
import de.iip_ecosphere.platform.support.OsUtils;
import de.iip_ecosphere.platform.support.TaskRegistry;
import de.iip_ecosphere.platform.support.TaskRegistry.TaskData;
import de.iip_ecosphere.platform.support.aas.AasUtils;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor;
import de.iip_ecosphere.platform.support.aas.ElementsAccess;
import de.iip_ecosphere.platform.support.json.JsonResultWrapper;
import de.iip_ecosphere.platform.support.json.JsonUtils;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;
import de.iip_ecosphere.platform.transport.status.TaskUtils;
import net.ssehub.easy.basics.modelManagement.ModelManagementException;
import net.ssehub.easy.instantiation.core.model.vilTypes.PseudoString;
import net.ssehub.easy.instantiation.core.model.vilTypes.configuration.Configuration;
import net.ssehub.easy.producer.core.mgmt.EasyExecutor;
import net.ssehub.easy.reasoning.core.reasoner.ReasoningResult;
import net.ssehub.easy.varModel.confModel.AssignmentState;
import net.ssehub.easy.varModel.confModel.ConfigurationException;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.cst.CSTSemanticException;
import net.ssehub.easy.varModel.cst.ConstantValue;
import net.ssehub.easy.varModel.management.VarModel;
import net.ssehub.easy.varModel.model.AbstractVariable;
import net.ssehub.easy.varModel.model.Attribute;
import net.ssehub.easy.varModel.model.ContainableModelElement;
import net.ssehub.easy.varModel.model.DecisionVariableDeclaration;
import net.ssehub.easy.varModel.model.FreezeBlock;
import net.ssehub.easy.varModel.model.IFreezable;
import net.ssehub.easy.varModel.model.IvmlDatatypeVisitor;
import net.ssehub.easy.varModel.model.ModelQuery;
import net.ssehub.easy.varModel.model.ModelQuery.FirstDeclTypeSelector;
import net.ssehub.easy.varModel.model.ModelQueryException;
import net.ssehub.easy.varModel.model.Project;
import net.ssehub.easy.varModel.model.datatypes.Compound;
import net.ssehub.easy.varModel.model.datatypes.DerivedDatatype;
import net.ssehub.easy.varModel.model.datatypes.IDatatype;
import net.ssehub.easy.varModel.model.datatypes.TypeQueries;
import net.ssehub.easy.varModel.model.values.ContainerValue;
import net.ssehub.easy.varModel.model.values.ReferenceValue;
import net.ssehub.easy.varModel.model.values.Value;
import net.ssehub.easy.varModel.model.values.ValueDoesNotMatchTypeException;

import static de.iip_ecosphere.platform.configuration.easyProducer.ConfigurationManager.*;
import static de.iip_ecosphere.platform.configuration.easyProducer.ivml.AasMapperUtils.*;

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
    public static final String OP_GET_VARIABLE_NAME = "getVariableName";
    public static final String OP_CREATE_VARIABLE = "createVariable";
    public static final String OP_CREATE_CONSTANT = "createConstantVariable";
    public static final String OP_DELETE_VARIABLE = "deleteVariable";
    public static final String OP_RENAME_VARIABLE = "renameVariable";
    public static final String OP_UPDATE_ARTIFACTS = "updateArtifactsAsync";
    public static final String OP_GEN_INTERFACES = "genInterfacesAsync";
    public static final String OP_GEN_INTERFACES_AND_CODE_TEMPLATES = "genInterfacesAndCodeTemplatesAsync";
    public static final String OP_GEN_BOOTSTRAPPED_APPS = "genBootstrappedAppsAsync";
    public static final String OP_GEN_CODE_TEMPLATES = "genCodeTemplatesAsync";
    public static final String OP_GEN_APPS_DIRECT = "genAppsDirectAsync";
    public static final String OP_GEN_APPS_NO_DEPS = "genAppsNoDepsAsync";
    public static final String OP_GEN_APPS = "genAppsAsync";
    public static final String OP_GET_TEMPLATES = "getTemplates";
    public static final String OP_INSTANTIATE_TEMPLATE = "instantiateTemplate";
    public static final String OP_GET_OPEN_TEMPLATE_VARIABLES = "getOpenTemplateVariables";
    public static final String OP_GET_UNUSED_PROJECTNAMES = "getUnusedProjectNames";
    public static final String OP_ADD_IMPORTS = "addImports";
    public static final String OP_REMOVE_IMPORTS = "removeImports";
    
    public static final Predicate<AbstractVariable> FILTER_NO_CONSTRAINT_VARIABLES = 
        v -> !TypeQueries.isConstraint(v.getType());
    // exclude unrefined fields from MetaConcepts, do we need that for refTo(Any)
    public static final Predicate<AbstractVariable> FILTER_NO_ANY = 
        v -> !"Any".equals(IvmlDatatypeVisitor.getUnqualifiedType(v.getType()));
    public static final String META_TYPE_NAME = "meta";
    public static final Function<String, String> SHORTID_PREFIX_META = n -> "meta" + PseudoString.firstToUpperCase(n);
    public static final String PROGRESS_COMPONENT_ID = AasMapperUtils.PROGRESS_COMPONENT_ID;
    public static final String PRJ_NAME_PLATFCONFIG = "PlatformConfiguration";
    public static final String PRJ_NAME_ALLCONSTANTS = "AllConstants";
    public static final String PRJ_NAME_ALLSERVICES = "AllServices";
    public static final String PRJ_NAME_ALLTYPES = "AllTypes";
    public static final String PRJ_NAME_TECHSETUP = "TechnicalSetup";
    private static final Map<String, String> PROJECT_MAPPING;
    private static final Map<String, String> PARENT_MAPPING;
    private static final Set<String> REQUIRED_TYPES;
    private static final TypeVisitor TYPE_VISITOR = new TypeVisitor();
    private static final String[] TOP_FOLDERS = {META_TYPE_NAME, "Dependency", "Manufacturer", "ServiceBase", 
        "Server", "ServiceMesh", "Application"}; // top level SM folders, mostly meta-model type names -> mgtUI

    private Supplier<Configuration> cfgSupplier;
    private Function<String, String> metaShortId = SHORTID_PREFIX_META;
    private Predicate<AbstractVariable> variableFilter = FILTER_NO_CONSTRAINT_VARIABLES.and(FILTER_NO_ANY);
    private Map<String, SubmodelElementContainerBuilder> types = new HashMap<>();
    
    static {
        Map<String, String> parentMap = new HashMap<>(); // tech settings -> parent
        parentMap.put("Aas", PRJ_NAME_TECHSETUP);
        parentMap.put("Transport", PRJ_NAME_TECHSETUP);
        parentMap.put("Services", PRJ_NAME_TECHSETUP);
        parentMap.put("Resources", PRJ_NAME_TECHSETUP);
        parentMap.put("UI", PRJ_NAME_TECHSETUP);
        PARENT_MAPPING = Collections.unmodifiableMap(parentMap);

        Map<String, String> projectMap = new HashMap<>(); // new variable parent type -> project
        projectMap.put("ServiceBase", PRJ_NAME_ALLSERVICES);
        projectMap.put("Server", PRJ_NAME_ALLSERVICES);
        projectMap.put("Manufacturer", PRJ_NAME_ALLSERVICES);
        projectMap.put("Dependency", PRJ_NAME_ALLSERVICES);
        projectMap.put("NameplateInfo", PRJ_NAME_ALLSERVICES);
        projectMap.put("Manufacturer", PRJ_NAME_ALLSERVICES);
        projectMap.put("StateMachine", PRJ_NAME_ALLSERVICES);
        projectMap.put("ServiceRelated", PRJ_NAME_ALLSERVICES);
        projectMap.put("EcsDevice", PRJ_NAME_ALLTYPES); // preliminary
        projectMap.put("Plugin", PRJ_NAME_ALLTYPES); // preliminary
        projectMap.put("DataType", PRJ_NAME_ALLTYPES);
        projectMap.put("DisplayRow", PRJ_NAME_ALLTYPES);
        projectMap.put("DisplayPanel", PRJ_NAME_ALLTYPES);
        projectMap.put("AliasType", PRJ_NAME_ALLTYPES);
        projectMap.put("TypeRelated", PRJ_NAME_ALLTYPES);
        PROJECT_MAPPING = Collections.unmodifiableMap(projectMap);
        
        Set<String> requiredTypes = new HashSet<>(); // not clean, would require reasoning or constraint analysis
        requiredTypes.add("NonEmptyNameString"); // or resolve until e.g. non empty string is found
        requiredTypes.add("Id");
        requiredTypes.add("PositiveInteger");
        requiredTypes.add("OktoVersion");
        requiredTypes.add("NonEmptyPort");
        requiredTypes.add("NonEmptyEphemeralPort");
        REQUIRED_TYPES = Collections.unmodifiableSet(requiredTypes);
    }
    
    /**
     * Creates a mapper with default settings, e.g., short ids for meta IVML information are
     * prefixed by "meta" ({@link #SHORTID_PREFIX_META}) and the variable filter excludes all IVML constraint 
     * variables ({@link #FILTER_NO_CONSTRAINT_VARIABLES}).
     * 
     * @param cfgSupplier a supplier providing the actual configuration instance
     * @param graphMapper maps a graph from IVML to an internal structure
     * @param changeListener optional configuration change listener, may be <b>null</b>
     * @throws IllegalArgumentException if {@code cfgSupplier} is <b>null</b>
     */
    public AasIvmlMapper(Supplier<Configuration> cfgSupplier, IvmlGraphMapper graphMapper, 
        ConfigurationChangeListener changeListener) {
        super(graphMapper, changeListener);
        if (null == cfgSupplier) {
            throw new IllegalArgumentException("cfgSupplier must not be null");
        }
        this.cfgSupplier = cfgSupplier;
    }
    
    /**
     * Returns the name of the parent project of {@code var} while considering {@link #PARENT_MAPPING}.
     * If there is no mapping, the name of the parent project is returned.
     * 
     * @param var the variable to map
     * @return the (mapped) name of the (declaring) parent project
     */
    private static String mapParent(IDecisionVariable var) {
        String result = var.getDeclaration().getParent().getName();
        String mapping = PARENT_MAPPING.get(result);
        if (mapping != null) {
            result = mapping;
        }
        return result;
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
    public void setVariableFilter(Predicate<AbstractVariable> variableFilter) {
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
        Configuration cfg = cfgSupplier.get();
        if (null != cfg) { // as long as we are in transition from platform without contained model to this
            for (String name : TOP_FOLDERS) {
                types.put(name, createTypeCollectionBuilder(smBuilder, name));
            }
            IDatatype primitiveType = null; 
            try {
                primitiveType = ModelQuery.findType(cfg.getConfiguration().getProject(), "PrimitiveType", null);
            } catch (ModelQueryException e) {
            }
            TypeMapper mapper = new TypeMapper(cfg, variableFilter, types.get(META_TYPE_NAME), metaShortId, 
                REQUIRED_TYPES);
            mapper.mapTypes();
            Iterator<IDecisionVariable> iter = cfg.getConfiguration().iterator();
            while (iter.hasNext()) {
                IDecisionVariable var = iter.next();
                if (variableFilter.test(var.getDeclaration())) {
                    IDatatype type = var.getDeclaration().getType();
                    if (primitiveType != null && primitiveType.isAssignableFrom(type)) {
                        type = primitiveType; // group them together to simplify the AAS structure
                    } 
                    String typeName = toAasTypeId(type);
                    SubmodelElementContainerBuilder builder = types.get(typeName);
                    if (null == builder) {
                        builder = createTypeCollectionBuilder(smBuilder, typeName);
                        types.put(typeName, builder);
                    }
                    mapVariable(var, builder, null);
                }
            }
            for (SubmodelElementContainerBuilder builder : types.values()) {
                builder.justBuild();
            }
            addOperations(smBuilder, iCreator);
        } else {
            LoggerFactory.getLogger(AasIvmlMapper.class).warn("No IVML configuration found. "
                + "Cannot create IVML-AAS model elements/operations.");
        }
    }

    /**
     * Turns an IVML variable name to a mapped AAS type id.
     * 
     * @param var the variable to return the id for
     * @return the mapped type id
     */
    private String toAasTypeId(IDecisionVariable var) {
        return toAasTypeId(var.getDeclaration().getType());
    }

    /**
     * Turns an IVML type name to a mapped AAS type id.
     * 
     * @param type the type
     * @return the mapped type id
     */
    private String toAasTypeId(IDatatype type) {
        return AasUtils.fixId(IvmlDatatypeVisitor.getUnqualifiedType(mapType(type)));
    }
    
    /**
     * Maps the type of an IVML variable into the configuration submodel, usually searching for the top-most
     * parent of compound types.
     * 
     * @param type the type to map
     * @return the mapped type, may be {@code type}
     */
    private IDatatype mapType(IDatatype type) {
        IDatatype result = DerivedDatatype.resolveToBasis(type);
        if (type instanceof Compound) {
            result = mapType((Compound) type);
        }
        return result;
    }
    
    /**
     * Maps the type of an IVML variable into the configuration submodel, usually searching for the top-most
     * parent of compound types.
     * 
     * @param type the type to map
     * @return the mapped type, may be {@code type}
     */
    private Compound mapType(Compound type) {
        Compound result = type;
        if (type.getRefinesCount() > 0) {
            for (int r = 0; r < type.getRefinesCount(); r++) {
                Compound ref = type.getRefines(r);
                if (!ref.getProject().getName().equals("MetaConcepts") 
                    && !ref.getName().equals("VersionedElement")) { // scope out this
                    result = mapType(ref);
                    break; // just take the first one for now
                }
            }
        }
        return result;
    }
    
    /**
     * Binds the AAS operations.
     * 
     * @param sBuilder the server builder
     */
    public void bindOperations(ProtocolServerBuilder sBuilder) {
        bind(sBuilder);
    }
    
    /**
     * Binds the AAS operations (ensure static lambda functions).
     * 
     * @param sBuilder the server builder
     */
    private static void bind(ProtocolServerBuilder sBuilder) {
        sBuilder.defineOperation(OP_CHANGE_VALUES, 
            new JsonResultWrapper(a -> {
                getAasIvmlMapper().changeValues(AasUtils.readMap(a, 0, null));
                return null;
            }, getAasOperationCompletedListener())
        );
        sBuilder.defineOperation(OP_GET_GRAPH, 
            new JsonResultWrapper(a -> getAasIvmlMapper().getGraph(AasUtils.readString(a, 0), 
                AasUtils.readString(a, 1)), 
            getAasOperationCompletedListener()));
        sBuilder.defineOperation(OP_SET_GRAPH, 
            new JsonResultWrapper(a ->  
                getAasIvmlMapper().setGraph(AasUtils.readString(a, 0), AasUtils.readString(a, 1), 
                    AasUtils.readString(a, 2), AasUtils.readString(a, 3), AasUtils.readString(a, 4)), 
                getAasOperationCompletedListener()
            ));
        sBuilder.defineOperation(OP_DELETE_GRAPH, 
            new JsonResultWrapper(a ->  
                getAasIvmlMapper().deleteGraph(AasUtils.readString(a, 0), AasUtils.readString(a, 1)), 
                getAasOperationCompletedListener()
            ));
        sBuilder.defineOperation(OP_GET_VARIABLE_NAME, 
            new JsonResultWrapper(a -> {
                return getAasIvmlMapper().getVariableName(AasUtils.readString(a, 0), AasUtils.readString(a, 1), 
                    AasUtils.readString(a, 2));
            })
        );
        sBuilder.defineOperation(OP_CREATE_CONSTANT, 
            new JsonResultWrapper(a -> {
                getAasIvmlMapper().createVariable(AasUtils.readString(a, 0), AasUtils.readString(a, 1), true,
                    AasUtils.readString(a, 2));
                return null;
            }, getAasOperationCompletedListener())
        );
        sBuilder.defineOperation(OP_CREATE_VARIABLE, 
            new JsonResultWrapper(a -> {
                getAasIvmlMapper().createVariable(AasUtils.readString(a, 0), AasUtils.readString(a, 1), false,
                    AasUtils.readString(a, 2));
                return null;
            }, getAasOperationCompletedListener())
        );
        sBuilder.defineOperation(OP_RENAME_VARIABLE, 
            new JsonResultWrapper(a -> {
                getAasIvmlMapper().renameVariable(AasUtils.readString(a), AasUtils.readString(a, 1));
                return null;
            }, getAasOperationCompletedListener())
        );
        sBuilder.defineOperation(OP_DELETE_VARIABLE, 
            new JsonResultWrapper(a -> {
                getAasIvmlMapper().deleteVariable(AasUtils.readString(a));
                return null;
            }, getAasOperationCompletedListener())
        );
        bindInstantiationOperations(sBuilder);
        bindTemplateOperations(sBuilder);
    }
    

    /**
     * Binds the AAS instantiation operations (ensure static lambda functions).
     * 
     * @param sBuilder the server builder
     */
    private static void bindInstantiationOperations(ProtocolServerBuilder sBuilder) {
        bindInstantiationOperation(sBuilder, OP_GEN_APPS, InstantiationMode.APPS, true);
        bindInstantiationOperation(sBuilder, OP_GEN_APPS_NO_DEPS, InstantiationMode.APPS_NO_DEPS, false);
        bindInstantiationOperation(sBuilder, OP_GEN_INTERFACES, InstantiationMode.INTERFACES, false);
        bindInstantiationOperation(sBuilder, OP_GEN_INTERFACES_AND_CODE_TEMPLATES, 
            InstantiationMode.INTERFACES_AND_CODE_TEMPLATES, false);
        bindInstantiationOperation(sBuilder, OP_GEN_BOOTSTRAPPED_APPS, InstantiationMode.BOOTSTRAPPED_APPS, false);
        bindInstantiationOperation(sBuilder, OP_GEN_CODE_TEMPLATES, InstantiationMode.CODE_TEMPLATES, false);
        bindInstantiationOperation(sBuilder, OP_GEN_APPS_DIRECT, InstantiationMode.APPS_DIRECT, false);
        sBuilder.defineOperation(OP_UPDATE_ARTIFACTS, 
            new JsonResultWrapper(a -> {
                return TaskUtils.executeAsTask(PROGRESS_COMPONENT_ID, 
                    // second param optional, may be empty -> null
                    p -> getAasIvmlMapper().updateArtifacts(AasUtils.readString(p))
                    , a);
            })
        );
    }

    /**
     * Binds the AAS template operations (ensure static lambda functions).
     * 
     * @param sBuilder the server builder
     */
    private static void bindTemplateOperations(ProtocolServerBuilder sBuilder) {
        sBuilder.defineOperation(OP_GET_TEMPLATES, 
            new JsonResultWrapper(a -> {
                getAasIvmlMapper().getTemplates();
                return null;
            }, getAasOperationCompletedListener())
        );
        sBuilder.defineOperation(OP_INSTANTIATE_TEMPLATE, 
            new JsonResultWrapper(a -> {
                getAasIvmlMapper().instantiateTemplate(AasUtils.readString(a), AasUtils.readString(a, 1), 
                    AasUtils.readMap(a, 1, null));
                return null;
            }, getAasOperationCompletedListener())
        );
        sBuilder.defineOperation(OP_GET_OPEN_TEMPLATE_VARIABLES, 
            new JsonResultWrapper(a -> {
                getAasIvmlMapper().getOpenTemplateVariables(AasUtils.readString(a));
                return null;
            }, getAasOperationCompletedListener())
        );
        sBuilder.defineOperation(OP_GET_UNUSED_PROJECTNAMES, 
            new JsonResultWrapper(a -> {
                getAasIvmlMapper().getUnusedProjects();
                return null;
            }, getAasOperationCompletedListener())
        );
        sBuilder.defineOperation(OP_ADD_IMPORTS, 
            new JsonResultWrapper(a -> {
                getAasIvmlMapper().addImports(AasUtils.readString(a));
                return null;
            }, getAasOperationCompletedListener())
        );
        sBuilder.defineOperation(OP_REMOVE_IMPORTS, 
            new JsonResultWrapper(a -> {
                getAasIvmlMapper().removeImports(AasUtils.readString(a));
                return null;
            }, getAasOperationCompletedListener())
        );
    }    

    /**
     * Updates artifacts of an application.
     * 
     * @param appName the application name (denoting the output folder)
     * @return <b>null</b> always
     * @throws ExecutionException if the update operation fails
     */
    public Object updateArtifacts(String appName) throws ExecutionException {
        TaskData lastTaskData = TaskUtils.getLastTaskData(); // may be wrong thread
        if (null == lastTaskData) { // fallback
            lastTaskData = TaskRegistry.getTaskData();
        }
        TaskData beforeTaskData = ConfigurationManager.setTaskData(lastTaskData);
        Object result = null;
        try {
            String appFolder = PseudoString.firstToUpperCase(PseudoString.toIdentifier(appName));
            File target = new File(ConfigurationSetup.getSetup().getEasyProducer().getGenTarget(), appFolder);
            if (!target.isDirectory()) {
                throw new ExecutionException("Cannot update artifacts. Target folder for '" + appName 
                    + "' does not exist. Instantiate the app before.", null);
            }
            String mvn = System.getProperty("easy.maven.home", null);
            if (null == mvn) {
                mvn = "mvn";
            } else {
                mvn = new File(mvn, "bin/mvn").getAbsolutePath(); 
            }
            Process proc = new ProcessBuilder(mvn, "-P", "App, Art", "deploy")
                .inheritIO()
                .directory(target)
                .start();
            proc.waitFor();
        } catch (IOException | InterruptedException e) {
            throw new ExecutionException(e);
        } finally {
            ConfigurationManager.setTaskData(beforeTaskData);
        }
        return result;
    }
    
    /**
     * Turns a file to a string.
     * 
     * @param file the file to be processed
     * @return the file as string
     */
    private static String toString(File file) {
        return file.toString(); // make absolute??
    }
    
    /**
     * Returns the (expected) resources directory in {@code base}. For building apps, the directory may not be known 
     * before running the build process.
     * 
     * @param base the base directory
     * @return the existing resources directory or <b>null</b>
     */
    private String getResourcesDir(File base) {
        File resourcesDir = new File(base, "resources.ipr");
        if (!resourcesDir.exists()) {
            resourcesDir = new File(base, "resources");
            if (!resourcesDir.exists()) {
                resourcesDir = null;
            }
        }
        String result = null == resourcesDir ? null : resourcesDir.getAbsolutePath();
        if (null != result) {
            LoggerFactory.getLogger(this).info("Using resources directory " + result);
        }
        return result;
    }
    
    /**
     * Returns the class loader of this class.
     * 
     * @return the class loader
     */
    private ClassLoader getClassLoader() {
        return getClass().getClassLoader();
    }
    
    /**
     * Instantiates according to the given {@code configurer}.
     * 
     * @param mode the instantiation mode
     * @param appId the app to build, ignored if empty or <b>null</b>
     * @param codeFile the code file containing the implementation
     * @return summary of instantiation results depending on {@code mode}, e.g., list of generated and downloadable 
     *     template URIs for {@link InstantiationMode#APPS_NO_DEPS} else <b>null</b>
     * @throws ExecutionException when the instantiation fails
     */
    public Object instantiate(InstantiationMode mode, String appId, String codeFile) throws ExecutionException {
        Object result = null;
        LoggerFactory.getLogger(getClass()).info("Instantiating app id '{}' mode {} codeFile '{}' asProcess {}", 
            appId, mode, codeFile, ConfigurationSetup.getSetup().getInstantiateAsProcess());
        TaskData lastTaskData = TaskUtils.getLastTaskData(); // may be wrong thread
        if (null == lastTaskData) { // fallback
            lastTaskData = TaskRegistry.getTaskData();
        }
        String logPath = "instantiation/" + appId;
        lastTaskData.setPath(logPath);
        TaskData beforeTaskData = ConfigurationManager.setTaskData(lastTaskData);
        if (InstantiationMode.APPS == mode && codeFile != null && codeFile.endsWith(".zip")) {
            File f = new File(ConfigurationSetup.getSetup().getUploadFolder(), codeFile);
            if (f.exists()) {
                f = fixZipConvention(f);
                LoggerFactory.getLogger(AasIvmlMapper.class).info("Integrating {} in {}", codeFile, f);
                OsUtils.setProperty("iip.easy.impl", f.getAbsolutePath());
            }
        }
        OsUtils.setProperty("KEY_PROPERTY_TRACING", "TOP");
        PlatformInstantiator.setTraceFilter(); //ConfigurationManager.cleanGenTarget();
        long start = System.currentTimeMillis();
        OsUtils.setProperty(PlatformInstantiator.KEY_PROPERTY_APPS, appId); // ignored if appId is null
        try {
            ConfigurationLifecycleDescriptor.cleanOutputFolder();
            OsUtils.setProperty(PlatformInstantiator.KEY_PROPERTY_UNPACKRESOURCES, "true");
            if (mode.isUpdateSnapshots()) {
                OsUtils.setProperty(PlatformInstantiator.KEY_PROPERTY_UPDATE_SNAPSHOTS, "true");
            }
            EasySetup easy = ConfigurationSetup.getSetup().getEasyProducer();                
            if (ConfigurationSetup.getSetup().getInstantiateAsProcess()) {
                String[] args = {easy.getIvmlModelName(), toString(easy.getBase()), toString(easy.getGenTarget()), 
                    mode.getStartRuleName(), toString(easy.getIvmlMetaModelFolder())};            
                PlatformInstantiation inst = ConfigurationFactory.createInstantiator(null, null);
                inst.setLogPath(logPath);                
                inst.takeOverProperties();
                inst.executeAsProcess(getClassLoader(), getResourcesDir(easy.getGenTarget()), "TOP", null, args);
            } else {
                OsUtils.setProperty("KEY_PROPERTY_TRACING", "TOP");
                OsUtils.setProperty("configuration.resourcesDirectory", getResourcesDir(easy.getGenTarget()));
                final ExecutionMode execMode = ExecutionMode.FULL;
                SenderCloseable sender = ConfigurationLifecycleDescriptor.setTransportLogConsumer(
                    logPath, execMode, lastTaskData);
                EasyExecutor executor = ConfigurationLifecycleDescriptor.createExecutor(execMode, sender);
                executor.setProgressObserver(ConfigurationManager.getProgressObserver());
                ConfigurationManager.loadIvmlModel(executor);
                ReasoningResult rRes = ConfigurationManager.validateAndPropagate(executor, NO_TEMPLATE_FILTER);
                if (null == rRes) {
                    throw new ExecutionException("No valid IVML model loaded/found.", null);
                }
                IvmlUtils.analyzeReasoningResult(rRes, false, true);            
                ConfigurationManager.setupContainerProperties(executor);
                ConfigurationManager.instantiate(executor, mode.getStartRuleName()); // throws exception if it fails
                executor.discardVILLocations();
                executor.clearVILModels();
                ConfigurationLifecycleDescriptor.setLogConsumer(null);
                ConfigurationLifecycleDescriptor.close(sender);
            }
            result = collectResult(mode, result, start);
            ConfigurationManager.setTaskData(beforeTaskData);
        } catch (ModelManagementException e) {
            throw new ExecutionException(e.getMessage(), e);
        }
        return result;
    }
    
    /**
     * Collects the instantiation result.
     * 
     * @param mode the instantiation mode
     * @param result the instantiation result known so far
     * @param start the start timestamp
     * @return the result, may be {@code result}
     */
    private Object collectResult(InstantiationMode mode, Object result, long start) {
        switch (mode) {
        case APPS_NO_DEPS:
        case BOOTSTRAPPED_APPS:
            result = collectTemplates(start);
            break;
        case APPS:
            break;
        case INTERFACES:
        case INTERFACES_AND_CODE_TEMPLATES:
        case CODE_TEMPLATES:
            result = collectTemplates(start);
            break;
        default:
            break;
        }
        return result;
    }

    /**
     * Collects the generated templates, copies them to {@link ConfigurationSetup#getArtifactsFolder()} and returns 
     * the file names prefixed by {@link ConfigurationSetup#getArtifactsUriPrefix()} as JSON.
     * 
     * @param startTime the time the generation started (as ms timestamp)
     * @return the generated template archives as JSON list
     */
    private Object collectTemplates(long startTime) {
        List<String> tmp = new ArrayList<>();
        ConfigurationSetup setup = ConfigurationSetup.getSetup();
        EasySetup easySetup = setup.getEasyProducer();
        File gen = easySetup.getGenTarget();
        FileUtils.listFiles(gen, 
            f -> acceptTemplateFile(f, startTime), 
            f -> {
                if (f.isFile()) { // don't package directories as we accept them
                    File target = new File(setup.getArtifactsFolder(), f.getName());
                    try {
                        Files.copy(f.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        String prefix = setup.getArtifactsUriPrefix();
                        if (null == prefix) { // not correctly configured
                            prefix = "";
                        } else if (!prefix.endsWith("/")) {
                            prefix += "/";
                        }
                        tmp.add(prefix + f.getName());
                    } catch (IOException e) {
                        LoggerFactory.getLogger(AasIvmlMapper.class).error("Cannot copy generated "
                            + "template {} to {}: {}", f, target, e.getMessage());
                    }
                }
            });
        return JsonUtils.toJson(tmp);
    }
        
    @Override
    protected Project getVariableTarget(Project root, Project varParent, IDatatype type, String name, 
        List<String> meshes) throws ExecutionException {
        Project result = null;
        if (null != type) {
            for (Map.Entry<String, String> ent : PROJECT_MAPPING.entrySet()) {
                try {
                    IDatatype serviceType = ModelQuery.findType(root, ent.getKey(), null);
                    if (null != serviceType && serviceType.isAssignableFrom(type)) {
                        result = ModelQuery.findProject(root, ent.getValue());
                        if (result != null) {
                            break;
                        }
                    } 
                } catch (ModelQueryException e) {
                    LoggerFactory.getLogger(AasIvmlMapper.class).warn(
                        "Cannot find type {} when checking for target IVML project {}: {}", ent.getKey(), 
                        ent.getValue(), e.getMessage());
                }
            }
            if (null == result && IvmlUtils.isOfCompoundType(type, "ServiceMesh")) {
                String prjName = getMeshProjectName(name);
                result = findOrCreateProject(root, prjName, true);
                prepareMeshProject(result, root);
            }
            if (null == result && IvmlUtils.isOfCompoundType(type, "Application")) {
                String prjName = getApplicationProjectName(name);
                result = findOrCreateProject(root, prjName, true);
                prepareApplicationProject(result, root, meshes);
            }
            if (null == result) { // immediate fallback
                if (null != varParent) {
                    if (isAllowedForModification(varParent)) {
                        result = varParent;
                    } else {
                        result = ModelQuery.findProject(root, PRJ_NAME_TECHSETUP);
                    }
                } else {
                    result = ModelQuery.findProject(root, PRJ_NAME_ALLCONSTANTS);
                }
            }
        }
        if (null == result) { // extreme fallback
            result = root;
        }
        return result;
    }
    
    @Override
    protected boolean isAllowedForModification(Project prj) {
        String name = prj.getName();
        return PRJ_NAME_ALLTYPES.equals(name) 
            || PRJ_NAME_ALLCONSTANTS.equals(name) 
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
     * Notifies changing {@code var} by {@code changeType} in the case of {@code operation} on {@code element}.
     * 
     * @param var the changed/created variable, may be <b>null</b> then logged as issue
     * @param changeType the change type, may be <b>null</b> then logged as issue
     * @param operation the operation causing the change to be included in logs if something goes wrong
     * @param element the element involved the change to be included in logs if something goes wrong
     */
    private void notifyChange(DecisionVariableDeclaration var, ConfigurationChangeType changeType, String operation, 
        String element) {
        try {
            if (var != null && changeType != null) {
                net.ssehub.easy.varModel.confModel.Configuration cfg = getIvmlConfiguration();
                notifyChange(IvmlUtils.obtainDecision(cfg, var), changeType);
            } else {
                LoggerFactory.getLogger(this).warn("Trying to notify change for {} {}: variable or change type "
                    + "is null", operation, element);
            }
        } catch (ConfigurationException e) {
            LoggerFactory.getLogger(this).warn("Trying to notify change for {} {}: {}", operation, element, 
                e.getMessage());
        }
    }

    /**
     * Deletes a graph structure in IVML. [public for testing]
     * 
     * @param appName the configured name of the application, may be empty or <b>null</b> to delete an individual mesh 
     *     that is not yet linked into an app (will fail if still linked and deletion happens without {@code appName}
     * @param meshName the configured name of the service mesh to delete a specific mesh in {@code appName}, 
     *     may be <b>null</b> or empty to delete the entire app
     * @return <b>null</b> always
     * @throws ExecutionException if setting the graph structure fails
     */
    public synchronized Object deleteGraph(String appName, String meshName) throws ExecutionException {
        net.ssehub.easy.varModel.confModel.Configuration cfg = getIvmlConfiguration();
        Project root = cfg.getProject();
        Project appProject = ModelQuery.findProject(root, getApplicationProjectName(appName));
        LoggerFactory.getLogger(getClass()).info("Deleting graph in IVML, app '{}' mesh '{}', found {}", 
            appName, meshName, appProject != null);
        try {
            Map<Project, CopiedFile> copies = new HashMap<>();
            Project meshProject = null;
            File appProjectFile = null;
            ModelResults results = new ModelResults();
            results.recordChanges(appProject, null);
            if (isNonEmptyString(meshName)) {
                meshProject = ModelQuery.findProject(root, getMeshProjectName(meshName));
                results.recordChanges(meshProject, null);
                IDatatype meshType = ModelQuery.findType(root, "ServiceMesh", null);
                DecisionVariableDeclaration meshVarDecl = ModelQuery.findDeclaration(
                    meshProject, new FirstDeclTypeSelector(meshType));
                if (null != meshVarDecl && appProject != null) {
                    IDatatype appType = ModelQuery.findType(root, "Application", null);
                    DecisionVariableDeclaration appVarDecl = ModelQuery.findDeclaration(
                        appProject, new FirstDeclTypeSelector(appType));
                    if (null != appVarDecl) {
                        IDecisionVariable var = cfg.getDecision(appVarDecl);
                        IDecisionVariable svc = var.getNestedElement("services");
                        deleteReferenceFromContainerValue(svc, meshVarDecl, var);
                        
                        appProjectFile = getIvmlFile(appProject);
                        copies.put(appProject, copyToTmp(appProjectFile));
                        saveTo(appProject, appProjectFile);
                        results.recordChange(appVarDecl, ConfigurationChangeType.MODIFIED);
                    }
                }
                File f = getIvmlFile(meshProject);
                copies.put(meshProject, copyToTmp(f));
                f.delete();
                results.recordChanges(meshProject, ConfigurationChangeType.DELETED);
            }
            if (null != appProject && (null == meshName || meshName.length() == 0)) {
                if (null == appProjectFile) {
                    appProjectFile = getIvmlFile(appProject);
                    copies.put(appProject, copyToTmp(appProjectFile));
                }
                appProjectFile.delete();
                results.recordChanges(appProject, ConfigurationChangeType.DELETED);
            } else {
                appProject = null;
            }
            reloadAndValidate(copies);
            LoggerFactory.getLogger(getClass()).info("Deleted graph in IVML, app '{}' mesh '{}'", 
                appName, meshName);
            results.notifyChanges("Delete graph", appName + (isNonEmptyString(meshName) ? ", " + meshName : ""));
            notifyChange(meshProject, ConfigurationChangeType.DELETED);
            notifyChange(appProject, ConfigurationChangeType.DELETED);
        } catch (ModelQueryException e) {
            throw new ExecutionException(e);
        }
        return null;
    }

    /**
     * Deletes a reference from a container value and sets the new value.
     * 
     * @param var the variable to take the value from, may be <b>null</b>
     * @param search the variable declaration to search the reference for
     * @param parVar the parent variable, for changing the containing var in IVML/project for saving
     * @return the new container value, may be <b>null</b> for not found
     * @throws ExecutionException if re-assigning the value fails
     */
    private ContainerValue deleteReferenceFromContainerValue(IDecisionVariable var, AbstractVariable search, 
        IDecisionVariable parVar) throws ExecutionException {
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
                    var.setValue(val, AssignmentState.FROZEN); // in config
                    parVar.getDeclaration().setValue(new ConstantValue(parVar.getValue())); // and in model for saving
                } catch (ConfigurationException | CSTSemanticException | ValueDoesNotMatchTypeException e) {
                    throw new ExecutionException(e);
                }
            }
        }
        return val;
    }

    /**
     * Changes an application/graph structure in IVML. Application/mesh files are dynamically linked and require
     * a different approach as, e.g., constants. [public for testing]
     * 
     * @param appName the configured name of the application
     * @param appValueEx the application value as IVML expression, may be empty or <b>null</b> to indicate that just 
     *    a mesh shall be modified
     * @param meshName the configured name of the service mesh (may be <b>null</b> or empty for none; used as import 
     *    resolution if given, existing and {@code format} or {@code value} are not given).
     * @param format the format of the graph (may be <b>null</b> or empty for none).
     * @param value the value (may be <b>null</b> or empty for none).
     * @return <b>null</b> always
     * @throws ExecutionException if setting the graph structure fails
     */
    public synchronized Object setGraph(String appName, String appValueEx, String meshName, String format, 
        String value) throws ExecutionException {
        boolean doApp = isNonEmptyString(appName) && isNonEmptyString(appValueEx);
        boolean doMesh = isNonEmptyString(meshName) && isNonEmptyString(format) && isNonEmptyString(value);
        if (doApp || doMesh) {
            LoggerFactory.getLogger(getClass()).info("Setting graph in IVML app {} = {}, mesh '{}', format {}", 
                appName, appValueEx, meshName, format); // no graph, may become too long
            GraphFormat gFormat = getGraphFormat(format);
            
            try {
                ModelResults results = new ModelResults();
                if (doMesh) {
                    IvmlGraph graph = gFormat.fromString(value, getMapper().getGraphFactory(), this);
                    createMeshProject(appName, meshName, graph, results);
                } 
                if (doApp) {
                    createAppProject(appName, appValueEx, results);
                }
    
                File meshFile = null;
                File appFile = null;
                Map<Project, CopiedFile> copies = new HashMap<>();
                if (doMesh) {
                    meshFile = getIvmlFile(results.meshProject);
                    copies.put(results.meshProject, copyToTmp(meshFile));
                }
                if (doApp) {
                    appFile = getIvmlFile(results.appProject);
                    copies.put(results.appProject, copyToTmp(appFile));
                }
                if (meshFile != null) {
                    saveTo(results.meshProject, meshFile);
                }
                if (appFile != null) {
                    saveTo(results.appProject, appFile);
                }
                reloadAndValidate(copies);
                LoggerFactory.getLogger(getClass()).info("Graph set in IVML app {} = {}, mesh {}, format {}", 
                    appName, appValueEx, meshName, format); // no graph, may become too long
                results.refreshChanges();
                if (doMesh) {
                    results.notifyChanges("mesh", meshName);
                }
                if (doApp) {
                    results.notifyChanges("app", appName);
                }
                
            } catch (ModelQueryException | ModelManagementException e) {
                LoggerFactory.getLogger(getClass()).info("Setting graph in IVML app {} = {}, mesh '{}', format {}: {}", 
                    appName, appValueEx, meshName, format, e.getMessage()); // no graph, may become too long
                e.printStackTrace();
                throw new ExecutionException(e);
            } catch (ExecutionException e) {
                LoggerFactory.getLogger(getClass()).info("Setting graph in IVML app {} = {}, mesh '{}', format {}: {}", 
                    appName, appValueEx, meshName, format, e.getMessage()); // no graph, may become too long
                e.printStackTrace();
                throw new ExecutionException(e);
            }
        } else {
            LoggerFactory.getLogger(getClass()).info("No model change as both, graph and mesh do not have a name"); 
        }
        return null;
    }

    /**
     * Stores intermediary model creation results.
     * 
     * @author Holger Eichelberger, SSE
     */
    protected class ModelResults {
        
        private Project meshProject;
        private Map<String, DecisionVariableDeclaration> names = new HashMap<>();
        private Map<DecisionVariableDeclaration, ConfigurationChangeType> changes = new HashMap<>();
        private DecisionVariableDeclaration meshVar;
        private Project appProject;

        /**
         * Adds {@code var} to {@link #meshProject} and records the change as of {@code type}.
         * 
         * @param var the variable 
         * @param type the change type
         */
        private void addToMeshProject(DecisionVariableDeclaration var, ConfigurationChangeType type) {
            meshProject.addBeforeFreeze(var);
            recordChange(var, type);
        }

        /**
         * Adds {@code var} to {@link #appProject} and records the change as of {@code type}.
         * 
         * @param var the variable 
         * @param type the change type
         */
        private void addToAppProject(DecisionVariableDeclaration var, ConfigurationChangeType type) {
            appProject.addBeforeFreeze(var);
            recordChange(var, type);
        }

        /**
         * Records the change state of all top-level decision variables in {@code prj}.
         * 
         * @param prj the project, ignored if <b>null</b>
         * @param type the change type, may be <b>null</b> for variable is known but change type is yet unclear 
         *   (usually as initialization)
         */
        private void recordChanges(Project prj, ConfigurationChangeType type) {
            if (null != prj) {
                for (int e = 0; e < prj.getElementCount(); e++) {
                    ContainableModelElement elt = prj.getElement(e);
                    if (elt instanceof DecisionVariableDeclaration) {
                        recordChange((DecisionVariableDeclaration) elt, type);
                    }
                }
            }
        }

        /**
         * Records a change on {@code var}. Tracks changes by variable names so if {@code var} replaces an older 
         * variable, the change is still recorded while the older variable reference is removed from the recording
         * and replaced by the newer one. {@link ConfigurationChangeType#DELETED} takes precedence.
         * 
         * @param var the variable
         * @param type the primary change type, may be changed to {@code ConfigurationChangeType#MODIFIED} if the 
         *   variable is already known even if its type is <b>null</b>.
         */
        private void recordChange(DecisionVariableDeclaration var, ConfigurationChangeType type) {
            recordChange(var, type, ConfigurationChangeType.MODIFIED);
        }
        
        /**
         * Records a change on {@code var}. Tracks changes by variable names so if {@code var} replaces an older 
         * variable, the change is still recorded while the older variable reference is removed from the recording
         * and replaced by the newer one. {@link ConfigurationChangeType#DELETED} takes precedence.
         * 
         * @param var the variable
         * @param type the primary change type, may be changed to {@code existsType} if the {@code mode} is not 
         *     {@link ConfigurationChangeType#DELETED} and variable was already recorded even if the state is 
         *     <b>null</b>
         * @param existsType the type to use when the variable is already recorded/known
         */
        private void recordChange(DecisionVariableDeclaration var, ConfigurationChangeType type, 
            ConfigurationChangeType existsType) {
            ConfigurationChangeType chType = type;
            // handle/replace variable re-creation, AAS only sees names
            DecisionVariableDeclaration oldVar = names.get(var.getName()); 
            if (oldVar != null && type != ConfigurationChangeType.DELETED) {
                chType = existsType;
                changes.remove(oldVar);
            }
            names.put(var.getName(), var);
            changes.put(var, chType);
        }
        
        /**
         * Commits changes, i.e., inspects all recorded variables and replaces change types of <b>null</b> (initial) 
         * by {@code type}.
         * 
         * @param type the new change type for initially recorded variables without state
         */
        private void commitChanges(ConfigurationChangeType type) {
            for (Map.Entry<DecisionVariableDeclaration, ConfigurationChangeType> ent : changes.entrySet()) {
                if (ent.getValue() == null) {
                    ent.setValue(type);
                }
            }
        }
        
        /**
         * Notifies all changes with their recorded type.
         * 
         * @param operation the operation causing the change to be included in logs if something goes wrong
         * @param element the element involved the change to be included in logs if something goes wrong
         * @see AasIvmlMapper#notifyChange(DecisionVariableDeclaration, ConfigurationChangeType, String, String)
         */
        private void notifyChanges(String operation, String element) {
            for (Map.Entry<DecisionVariableDeclaration, ConfigurationChangeType> ent : changes.entrySet()) {
                if (ent.getValue() != null) {
                    notifyChange(ent.getKey(), ent.getValue(), operation, element);
                }
            }
            changes.clear();
            names.clear();
        }
        
        /**
         * Refreshes all decision variable declaration references in the change recording. This may be needed if
         * a model reload has been performed and changes shall be notified with the new/reasoned values instead
         * of the original values.
         */
        private void refreshChanges() {
            net.ssehub.easy.varModel.confModel.Configuration cfg = getIvmlConfiguration();
            Project prj = cfg.getProject();
            List<DecisionVariableDeclaration> decls = new ArrayList<>(changes.keySet());
            for (DecisionVariableDeclaration decl : decls) {
                String name = decl.getName();
                try {
                    ContainableModelElement elt = ModelQuery.findElementByName(prj, name, null);
                    if (elt instanceof DecisionVariableDeclaration) {
                        DecisionVariableDeclaration eDecl = (DecisionVariableDeclaration) elt;
                        changes.put(eDecl, changes.remove(decl));
                        names.put(name, eDecl);
                    }
                } catch (ModelQueryException e) {
                    LoggerFactory.getLogger(AasIvmlMapper.class).warn(
                        "Cannot refresh change status for variable {}: {}", name, e.getMessage());
                }
            }
        }
        
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
     * Prepares an application project, e.g., adds default imports.
     * 
     * @param app the application project
     * @param root the root project
     * @param meshes the meshes to be added as imports, may be <b>null</b> for none
     * @throws ExecutionException if adding imports fails
     */
    private void prepareApplicationProject(Project app, Project root, List<String> meshes) 
        throws ExecutionException {
        try {
            addImport(app, "Applications", root, null);
            addImport(app, "AllServices", root, null);
            // > may go down to easy
            Project wildcardPrj = new Project("");
            if (null != meshes) {
                for (String modelName : meshes) { // "resolve" wildcards
                    Project tmp = ModelQuery.findProject(root, modelName);
                    if (null != tmp) {
                        addImport(wildcardPrj, modelName, root, tmp);
                    }
                }
            }
            // < may go down to Easy
            addImport(app, "ServiceMeshPart*", root, wildcardPrj);
        } catch (ModelManagementException e) {
            throw new ExecutionException(e);
        }
        annotate(app);
    }

    /**
     * Returns the IVML project name for a service mesh.
     * 
     * @param meshName the configured name of the service mesh
     * @return the project name
     */
    private String getMeshProjectName(String meshName) {
        return "ServiceMeshPart" + toIdentifierFirstUpper(meshName);
    }

    /**
     * Prepares a mesh project, e.g., adds default imports.
     * 
     * @param mesh the mesh project
     * @param root the root project
     * @throws ExecutionException if adding imports fails
     */
    private void prepareMeshProject(Project mesh, Project root) throws ExecutionException {
        try {
            addImport(mesh, "Applications", root, null);
            addImport(mesh, "AllServices", root, null);
        } catch (ModelManagementException e) {
            throw new ExecutionException(e);
        }
        annotate(mesh);
    }
    

    /**
     * Annotates {@code prj} with the usual binding time.
     * 
     * @param prj the project
     * @throws ExecutionException if the annotation cannot be created
     */
    private void annotate(Project prj) throws ExecutionException {
        final String attrBindingTime = "bindingTime";
        if (!IvmlUtils.hasAnnotation(prj, attrBindingTime)) {
            try {
                final net.ssehub.easy.varModel.model.datatypes.Enum bindingTime 
                    = ModelQuery.findEnum(prj, "BindingTime");
                Attribute attr = new Attribute(attrBindingTime, bindingTime, prj, prj.getVariable());
                attr.setValue(createExpression(bindingTime, "BindingTime.compile", prj));
                prj.addBeforeFreeze(attr);
            } catch (ValueDoesNotMatchTypeException | CSTSemanticException | ModelQueryException e) {
                throw new ExecutionException(e);
            }
        }
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
        results.recordChanges(results.appProject, null);
        prepareApplicationProject(results.appProject, root, 
            VarModel.INSTANCE.getMatchingModelNames("ServiceMeshPart*"));
        List<Object> meshes = new ArrayList<Object>();
        boolean replaced = false;
        if (results.appProject != null) {
            for (int e = 0; e < results.appProject.getElementCount(); e++) {
                ContainableModelElement elt = results.appProject.getElement(e);
                if (elt instanceof DecisionVariableDeclaration) {
                    DecisionVariableDeclaration varDecl = (DecisionVariableDeclaration) elt;
                    IDecisionVariable var = cfg.getDecision(varDecl);
                    Value val = var.getNestedElement("services").getValue();
                    if (val instanceof ContainerValue) {
                        ContainerValue cValue = (ContainerValue) val;
                        for (int v = 0; v < cValue.getElementSize(); v++) {
                            Value mVal = cValue.getElement(v);
                            if (IvmlUtils.isRefWithName(appName, cfg, mVal)) {
                                replaced = true;
                                meshes.add(results.meshVar);
                            } else {
                                meshes.add(mVal); // TODO value conversion?
                            }
                        }
                        results.recordChange(varDecl, ConfigurationChangeType.MODIFIED);
                    }
                }
            }
        }
        if (!replaced) {
            meshes.add(results.meshVar);
        }
            
        DecisionVariableDeclaration appVar = new DecisionVariableDeclaration(
            toIdentifier(appName), applicationType, results.appProject);
        results.addToAppProject(appVar, ConfigurationChangeType.CREATED);
        if (appValueEx.length() > 0) {
            setValue(appVar, appValueEx);
        }
        results.commitChanges(ConfigurationChangeType.DELETED);
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
    protected ReasoningResult validateAndPropagate(Predicate<Project> projectFilter) {
        return ConfigurationManager.validateAndPropagate(projectFilter);
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
     * Validates the name of a node and if there is none, sets a pseudo name based on {@code count}.
     * 
     * @param node the node to validate
     * @param count unique node counter per mesh
     * @return the name of {@code node}
     */
    private String validateName(IvmlGraphNode node, int count) {
        if (node.getName().length() == 0) { // just a fallback
            node.setName(String.valueOf(count));
        }
        return node.getName();
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
        String meshProjectName = getMeshProjectName(meshName);
        results.meshProject = findOrCreateProject(root, meshProjectName, false); // just overwrite
        results.recordChanges(results.meshProject, null);
        prepareMeshProject(results.meshProject, root);
        final IDatatype sourceType = ModelQuery.findType(root, "MeshSource", null);
        final IDatatype processorType = ModelQuery.findType(root, "MeshProcessor", null);
        final IDatatype sinkType = ModelQuery.findType(root, "MeshSink", null);
        final IDatatype connectorType = ModelQuery.findType(root, "MeshConnector", null);
        final IDatatype serviceType = ModelQuery.findType(root, "ServiceBase", null);
        final IDatatype meshType = ModelQuery.findType(root, "ServiceMesh", null);
        ServiceMap services = IvmlUtils.collectServices(cfg, serviceType);
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
                type = processorType; // TODO Probe service
            }
            DecisionVariableDeclaration nodeVar = new DecisionVariableDeclaration(toId("node_" 
                + validateName(n, nodeMap.size())), type, results.meshProject);
            String nodeValEx = "{pos_x=" + n.getXPos() + ",pos_y=" + n.getYPos() 
                + ",impl=" + IvmlUtils.getVarNameSafe(IvmlUtils.findServiceVar(services, n.getImpl()), "null")
                + ",next = {";
            valueMap.put(nodeVar, nodeValEx);
            results.addToMeshProject(nodeVar, ConfigurationChangeType.CREATED);
            nodeMap.put(n, nodeVar);
            if (type == sourceType && !sources.contains(nodeVar)) {
                sources.add(nodeVar);
            }
        }
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
                results.addToMeshProject(edgeVar, ConfigurationChangeType.CREATED);
                DecisionVariableDeclaration end = nodeMap.get(e.getEnd());
                String valueEx = "{name=\"" + edgeName + "\", next=refBy(" + toId(end.getName()) + ")}";
                setValue(edgeVar, valueEx);
                String startNodeValueEx = valueMap.get(nodeMap.get(n));
                if (!startNodeValueEx.endsWith("{")) {
                    startNodeValueEx += ",";
                }
                startNodeValueEx += "refBy(" + edgeVarName + ")";
                valueMap.put(nodeMap.get(n), startNodeValueEx);
            }
        }
        for (IvmlGraphNode n: graph.nodes()) {
            DecisionVariableDeclaration nodeVar = nodeMap.get(n);
            setValue(nodeVar, valueMap.get(nodeVar) + "}}"); 
        }
        results.meshVar = new DecisionVariableDeclaration(toIdentifier(meshName), meshType, results.meshProject);
        results.addToMeshProject(results.meshVar, ConfigurationChangeType.CREATED);
        String meshValEx = "{description=\"" + meshName + "\", sources={";
        for (int s = 0; s < sources.size(); s++) {
            if (s > 0) {
                meshValEx += ", ";
            }
            meshValEx += "refBy(" + sources.get(s).getName() + ")";
        }
        meshValEx += "}}";
        setValue(results.meshVar, meshValEx);
        results.commitChanges(ConfigurationChangeType.DELETED);
    }
    
    /**
     * Adds the default AAS operations for obtaining information on the configuration model / changing the model.
     * 
     * @param smBuilder the submodel builder representing the target
     * @param iCreator the invocables creator for operations
     */
    private void addOperations(SubmodelBuilder smBuilder, InvocablesCreator iCreator) {
        AuthenticationDescriptor aDesc = AasPartRegistry.getSubmodelAuthentication();
        createOperationBuilder(smBuilder, OP_CHANGE_VALUES, iCreator)
            .addInputVariable("valueExprs", Type.STRING)
            .build(aDesc);
        createOperationBuilder(smBuilder, OP_GET_GRAPH, iCreator)
            .addInputVariable("varName", Type.STRING)
            .addInputVariable("format", Type.STRING)
            .build(Type.STRING, aDesc);
        createOperationBuilder(smBuilder, OP_SET_GRAPH, iCreator)
            .addInputVariable("appName", Type.STRING)
            .addInputVariable("appValExpr", Type.STRING)
            .addInputVariable("serviceMeshName", Type.STRING)
            .addInputVariable("format", Type.STRING)
            .addInputVariable("val", Type.STRING)
            .build(aDesc);
        createOperationBuilder(smBuilder, OP_DELETE_GRAPH, iCreator)
            .addInputVariable("appName", Type.STRING)
            .addInputVariable("serviceMeshName", Type.STRING)
            .build(aDesc);
        createOperationBuilder(smBuilder, OP_GET_VARIABLE_NAME, iCreator)
            .addInputVariable("type", Type.STRING)
            .addInputVariable("elementName", Type.STRING)
            .addInputVariable("elementVersion", Type.STRING)
            .build(Type.STRING, aDesc);
        createOperationBuilder(smBuilder, OP_CREATE_CONSTANT, iCreator)
            .addInputVariable("varName", Type.STRING)
            .addInputVariable("type", Type.STRING)
            .addInputVariable("valExpr", Type.STRING)
            .build(aDesc);
        createOperationBuilder(smBuilder, OP_CREATE_VARIABLE, iCreator)
            .addInputVariable("varName", Type.STRING)
            .addInputVariable("type", Type.STRING)
            .addInputVariable("valExpr", Type.STRING)
            .build(aDesc);
        createOperationBuilder(smBuilder, OP_RENAME_VARIABLE, iCreator)
            .addInputVariable("varName", Type.STRING)
            .addInputVariable("newVarName", Type.STRING)
            .build(aDesc);
        createOperationBuilder(smBuilder, OP_DELETE_VARIABLE, iCreator)
            .addInputVariable("varName", Type.STRING)
            .build(aDesc);
        addInstantiateOperation(smBuilder, OP_GEN_APPS, iCreator, aDesc, true);
        addInstantiateOperation(smBuilder, OP_GEN_APPS_NO_DEPS, iCreator, aDesc, false);
        addInstantiateOperation(smBuilder, OP_GEN_INTERFACES, iCreator, aDesc, false);
        addInstantiateOperation(smBuilder, OP_GEN_INTERFACES_AND_CODE_TEMPLATES, iCreator, aDesc, false);
        addInstantiateOperation(smBuilder, OP_GEN_BOOTSTRAPPED_APPS, iCreator, aDesc, false);
        addInstantiateOperation(smBuilder, OP_GEN_CODE_TEMPLATES, iCreator, aDesc, false);
        addInstantiateOperation(smBuilder, OP_GEN_APPS_DIRECT, iCreator, aDesc, false);
        createOperationBuilder(smBuilder, OP_UPDATE_ARTIFACTS, iCreator)
            .addInputVariable("appName", Type.STRING)
            .build(Type.STRING, aDesc);
        createOperationBuilder(smBuilder, OP_GET_TEMPLATES, iCreator)
            .build(Type.STRING, aDesc);
        createOperationBuilder(smBuilder, OP_INSTANTIATE_TEMPLATE, iCreator)
            .addInputVariable("varName", Type.STRING)
            .addInputVariable("appName", Type.STRING)
            .build(Type.STRING, aDesc);
        createOperationBuilder(smBuilder, OP_GET_OPEN_TEMPLATE_VARIABLES, iCreator)
            .addInputVariable("varName", Type.STRING)
            .build(Type.STRING, aDesc);
        createOperationBuilder(smBuilder, OP_GET_UNUSED_PROJECTNAMES, iCreator)
            .addInputVariable("varName", Type.STRING)
            .build(Type.STRING, aDesc);
        createOperationBuilder(smBuilder, OP_ADD_IMPORTS, iCreator)
            .addInputVariable("imports", Type.STRING)
            .build(Type.STRING, aDesc);
        createOperationBuilder(smBuilder, OP_REMOVE_IMPORTS, iCreator)
            .addInputVariable("imports", Type.STRING)
            .build(Type.STRING, aDesc);
    }
    
    /**
     * Maps a single variable {@code var} into {@code builder}.
     * 
     * @param var the variable to map as source
     * @param builder the builder as target (representing the parent of {@code var})
     * @param id the id to use as variable name instead of the variable name itself, may be <b>null</b> for 
     *     the variable name
     */
    void mapVariable(IDecisionVariable var, SubmodelElementContainerBuilder builder, String id) {
        if (variableFilter.test(var.getDeclaration())) {
            AbstractVariable decl = var.getDeclaration();
            String varName = decl.getName();
            IDatatype varType = null == var.getValue() ? decl.getType() : var.getValue().getType();
            //IDatatype varType = null == var.getValue() || var.getValue() == NullValue.INSTANCE 
            //    ? decl.getType() : var.getValue().getType(); // including null values causes reasoning issues
            IDatatype rVarType = DerivedDatatype.resolveToBasis(varType);
            String lang = IvmlUtils.getLang();
            String semanticId = null;
            String displayName = null;
            for (int a = 0; a < var.getAttributesCount(); a++) {
                IDecisionVariable attribute = var.getAttribute(a);
                String attributeName = attribute.getDeclaration().getName();
                if ("semanticId".equals(attributeName)) {
                    semanticId = IvmlUtils.getStringValueEmptyNull(attribute);
                } else if ("displayName".equals(attributeName)) {
                    displayName = IvmlUtils.getStringValueEmptyNull(attribute);
                }
            }
            SubmodelElementContainerBuilder varBuilder;
            if (TypeQueries.isCompound(rVarType)) {
                varBuilder = builder.createSubmodelElementListBuilder(AasUtils.fixId(varName));
                for (int member = 0; member < var.getNestedElementsCount(); member++) {
                    IDecisionVariable elt = var.getNestedElement(member);
                    mapVariable(elt, varBuilder, null);
                }
            } else if (TypeQueries.isContainer(rVarType)) {
                boolean isSequence = TypeQueries.isSequence(rVarType);
                if (isSequence) {
                    varBuilder = builder.createSubmodelElementListBuilder(AasUtils.fixId(varName));
                } else {
                    varBuilder = builder.createSubmodelElementCollectionBuilder(AasUtils.fixId(varName));
                }
                for (int member = 0; member < var.getNestedElementsCount(); member++) {
                    mapVariable(var.getNestedElement(member), varBuilder, "var_" + member);
                }
                PropertyBuilder pb = varBuilder.createPropertyBuilder(AasUtils.fixId(metaShortId.apply("size")))
                    .setDescription(new LangString(lang, ModelInfo.getCommentSafe(decl)))
                    .setValue(Type.INTEGER, var.getNestedElementsCount());
                setSemanticId(pb, semanticId);
                pb.build();
            } else {
                String propName = id == null ? varName : id;
                varBuilder = builder.createSubmodelElementCollectionBuilder(AasUtils.fixId(propName));
                Object aasValue = IvmlUtils.getValue(var);
                varType.accept(TYPE_VISITOR); // resolved anyway
                Type aasType = TYPE_VISITOR.getAasType();
                PropertyBuilder pb = varBuilder.createPropertyBuilder(AasUtils.fixId("varValue")); // reserved prop name
                pb.setValue(aasType, aasValue);
                setSemanticId(pb, semanticId);
                pb.build();
            }
            addMetaProperties(var, varType, varBuilder, displayName);
            varBuilder.justBuild();
        }
    }

    /**
     * Adds the meta properties of {@code var} of type {@code varType} to {@code varBuilder}.
     * 
     * @param var the variable to take the meta-properties from
     * @param varType the type of {@code var} (as we already have determined it)
     * @param varBuilder the AAS builder representing {@code var} to add the AAS properties to
     * @param displayName the displac name of {@code var}, may be <b>null</b> for none
     */
    private void addMetaProperties(IDecisionVariable var, IDatatype varType, 
        SubmodelElementContainerBuilder varBuilder, String displayName) {
        varBuilder.createPropertyBuilder(AasUtils.fixId(metaShortId.apply("variable")))
            .setValue(Type.STRING, var.getDeclaration().getName())
            .build();
        varBuilder.createPropertyBuilder(AasUtils.fixId(metaShortId.apply("state")))
            .setValue(Type.STRING, var.getState().toString())
            .build();
        varBuilder.createPropertyBuilder(AasUtils.fixId(metaShortId.apply("constant")))
            .setValue(Type.BOOLEAN, var.getDeclaration().isConstant())
            .build();
        varBuilder.createPropertyBuilder(AasUtils.fixId(metaShortId.apply("template")))
            .setValue(Type.BOOLEAN, IvmlUtils.isInTemplate(var))
            .build();
        IDatatype type = var.getValue() != null ? var.getValue().getType() : varType;
        String declaredTypeName = IvmlDatatypeVisitor.getUnqualifiedType(var.getDeclaration().getType());
        String varName = var.getDeclaration().getName();
        String varTypeName = IvmlDatatypeVisitor.getUnqualifiedType(type);
        varBuilder.createPropertyBuilder(AasUtils.fixId(metaShortId.apply("type")))
            .setValue(Type.STRING, varTypeName)
            .build();
        TypeMapper.addMetaDefault(var, varBuilder, metaShortId);
        TypeMapper.addMetaRequired(varBuilder, varTypeName, metaShortId, REQUIRED_TYPES);
        TypeMapper.addTypeKind(varBuilder, DerivedDatatype.resolveToBasis(type), metaShortId);
        if (var.getDeclaration().getParent() instanceof Project) { // top-level only for now
            varBuilder.createPropertyBuilder(AasUtils.fixId(metaShortId.apply("project")))
                .setValue(Type.STRING, mapParent(var))
                .build();
        }
        TypeMapper.addDisplayName(displayName, varBuilder, declaredTypeName, varName, metaShortId);
        try {
            IDatatype serviceType = ModelQuery.findType(var.getConfiguration().getProject(), "ServiceBase", null);
            if (null != serviceType && serviceType.isAssignableFrom(varType)) {
                String serviceId = IvmlUtils.getStringValue(var.getNestedElement("id"), "");
                Registry reg = AasPartRegistry.getIipAasRegistry();
                AasPartRegistry.addServiceAasEndpointProperty(reg, varBuilder, metaShortId.apply("Aas"), serviceId);
            }
        } catch (ModelQueryException e) {
            LoggerFactory.getLogger(AasIvmlMapper.class).warn(
                "Cannot find type ServiceBase. No service will have a AAS URL. {}", e.getMessage());
        }
    }

    /**
     * Deletes the mapping of the specified variable in the AAS.
     * 
     * @param sm the submodel to delete from
     * @param var the variable
     */
    void deleteAasVariableMapping(Submodel sm, IDecisionVariable var) {
        String typeName = toAasTypeId(var);
        String varName = AasUtils.fixId(var.getDeclaration().getName());
        LoggerFactory.getLogger(this).info("Processing change for {} -> {}", typeName, varName); // method called anyway
        ElementsAccess c = sm.getSubmodelElementCollection(AasUtils.fixId(typeName));
        if (null == c) {
            c = sm.getSubmodelElementList(AasUtils.fixId(typeName));
        }
        if (null != c) {
            c.deleteElement(varName);
        }
    }

    /**
     * Maps {@code var} into the submodel represented by {@code smB}.
     * 
     * @param smB the submodel builder
     * @param var the variable
     */
    void mapVariableToAas(SubmodelBuilder smB, IDecisionVariable var) {
        String typeName = toAasTypeId(var);
        SubmodelElementContainerBuilder builder = createTypeCollectionBuilder(smB, typeName);
        mapVariable(var, builder, null);
        builder.justBuild();
    }

    @Override
    protected Predicate<String> getFilterUnusedProjectPredicate() {
        return name -> {
            boolean exclude = false;
            exclude |= name.equals(PRJ_NAME_PLATFCONFIG); // top-level
            exclude |= name.contains("Part"); // dynamic/wildcard imports
            return exclude;
        };
    }

    @Override
    protected Project getProjectForImports(net.ssehub.easy.varModel.confModel.Configuration cfg) {
        Project result = cfg.getProject();
        Project tmp = ModelQuery.findProject(result, PRJ_NAME_ALLCONSTANTS);
        if (tmp != null) {
            result = tmp;
        } else { // fallback
            tmp = ModelQuery.findProject(result, PRJ_NAME_ALLSERVICES);
            if (tmp != null) {
                result = tmp;
            }
        }
        return result;
    }

}