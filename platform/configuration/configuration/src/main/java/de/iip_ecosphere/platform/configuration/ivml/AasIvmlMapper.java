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
import java.io.FileInputStream;
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
import java.util.Optional;
import java.util.Set;
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
import de.iip_ecosphere.platform.configuration.ivml.IvmlGraphMapper.IvmlGraph;
import de.iip_ecosphere.platform.configuration.ivml.IvmlGraphMapper.IvmlGraphEdge;
import de.iip_ecosphere.platform.configuration.ivml.IvmlGraphMapper.IvmlGraphNode;
import de.iip_ecosphere.platform.support.aas.IdentifierType;
import de.iip_ecosphere.platform.support.aas.InvocablesCreator;
import de.iip_ecosphere.platform.support.aas.LangString;
import de.iip_ecosphere.platform.support.aas.Property.PropertyBuilder;
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder;
import de.iip_ecosphere.platform.support.aas.Registry;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.aas.SubmodelElementContainerBuilder;
import de.iip_ecosphere.platform.support.aas.Type;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.support.FileUtils;
import de.iip_ecosphere.platform.support.TaskRegistry;
import de.iip_ecosphere.platform.support.ZipUtils;
import de.iip_ecosphere.platform.support.TaskRegistry.TaskData;
import de.iip_ecosphere.platform.support.aas.AasUtils;
import de.iip_ecosphere.platform.support.json.JsonResultWrapper;
import de.iip_ecosphere.platform.support.json.JsonUtils;
import de.iip_ecosphere.platform.transport.status.TaskUtils;
import net.ssehub.easy.basics.modelManagement.ModelManagementException;
import net.ssehub.easy.instantiation.core.model.vilTypes.PseudoString;
import net.ssehub.easy.instantiation.core.model.vilTypes.configuration.Configuration;
import net.ssehub.easy.producer.core.mgmt.EasyExecutor;
import net.ssehub.easy.reasoning.core.reasoner.ReasoningResult;
import net.ssehub.easy.varModel.confModel.AssignmentState;
import net.ssehub.easy.varModel.confModel.ConfigurationException;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.management.VarModel;
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
import net.ssehub.easy.varModel.model.datatypes.Compound;
import net.ssehub.easy.varModel.model.datatypes.DerivedDatatype;
import net.ssehub.easy.varModel.model.datatypes.IDatatype;
import net.ssehub.easy.varModel.model.datatypes.TypeQueries;
import net.ssehub.easy.varModel.model.values.ContainerValue;
import net.ssehub.easy.varModel.model.values.ReferenceValue;
import net.ssehub.easy.varModel.model.values.Value;

import static de.iip_ecosphere.platform.configuration.ConfigurationManager.*;

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
    public static final String OP_DELETE_VARIABLE = "deleteVariable";
    public static final String OP_GEN_INTERFACES = "genInterfacesAsync";
    public static final String OP_GEN_APPS_NO_DEPS = "genAppsNoDepsAsync";
    public static final String OP_GEN_APPS = "genAppsAsync";
    
    public static final Predicate<AbstractVariable> FILTER_NO_CONSTRAINT_VARIABLES = 
        v -> !TypeQueries.isConstraint(v.getType());
    // exclude unrefined fields from MetaConcepts, do we need that for refTo(Any)
    public static final Predicate<AbstractVariable> FILTER_NO_ANY = 
        v -> !"Any".equals(IvmlDatatypeVisitor.getUnqualifiedType(v.getType()));
    public static final String META_TYPE_NAME = "meta";
    public static final Function<String, String> SHORTID_PREFIX_META = n -> "meta" + PseudoString.firstToUpperCase(n);
    public static final String PROGRESS_COMPONENT_ID = "Configuration";
    protected static final String PRJ_NAME_ALLCONSTANTS = "AllConstants";
    protected static final String PRJ_NAME_ALLSERVICES = "AllServices";
    protected static final String PRJ_NAME_ALLTYPES = "AllTypes";
    protected static final String PRJ_NAME_TECHSETUP = "TechnicalSetup";
    private static final Map<String, String> PROJECT_MAPPING;
    private static final Map<String, String> PARENT_MAPPING;
    private static final TypeVisitor TYPE_VISITOR = new TypeVisitor();
    private static final String[] TOP_FOLDERS = {META_TYPE_NAME, "Dependency", "Manufacturer", "ServiceBase", 
        "Server", "ServiceMesh", "Application"}; // top level SM folders, mostly meta-model type names -> mgtUI

    private Supplier<Configuration> cfgSupplier;
    private Function<String, String> metaShortId = SHORTID_PREFIX_META;
    private Predicate<AbstractVariable> variableFilter = FILTER_NO_CONSTRAINT_VARIABLES.and(FILTER_NO_ANY);
    private Map<String, SubmodelElementContainerBuilder> types = new HashMap<>();
    
    static {
        // tech settings -> parent
        Map<String, String> parentMap = new HashMap<>();
        parentMap.put("Aas", PRJ_NAME_TECHSETUP);
        parentMap.put("Transport", PRJ_NAME_TECHSETUP);
        parentMap.put("Services", PRJ_NAME_TECHSETUP);
        parentMap.put("Resources", PRJ_NAME_TECHSETUP);
        parentMap.put("UI", PRJ_NAME_TECHSETUP);
        PARENT_MAPPING = Collections.unmodifiableMap(parentMap);

        // new variable parent type -> project
        Map<String, String> projectMap = new HashMap<>();
        projectMap.put("ServiceBase", PRJ_NAME_ALLSERVICES);
        projectMap.put("Server", PRJ_NAME_ALLSERVICES);
        projectMap.put("Manufacturer", PRJ_NAME_ALLSERVICES);
        projectMap.put("Dependency", PRJ_NAME_ALLSERVICES);
        projectMap.put("DataType", PRJ_NAME_ALLTYPES);
        PROJECT_MAPPING = Collections.unmodifiableMap(projectMap);
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
            TypeMapper mapper = new TypeMapper(cfg, variableFilter, types.get(META_TYPE_NAME), metaShortId);
            mapper.mapTypes();
            Iterator<IDecisionVariable> iter = cfg.getConfiguration().iterator();
            while (iter.hasNext()) {
                IDecisionVariable var = iter.next();
                if (variableFilter.test(var.getDeclaration())) {
                    IDatatype type = var.getDeclaration().getType();
                    if (primitiveType != null && primitiveType.isAssignableFrom(type)) {
                        type = primitiveType; // group them together to simplify the AAS structure
                    } 
                    String typeName = IvmlDatatypeVisitor.getUnqualifiedType(mapType(type));
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
     * Creates a AAS collection representing an IVML type.
     * 
     * @param smBuilder the submodel builder
     * @param typeName the type name (turned into an AAS shortID)
     * @return the collection builder
     */
    private static SubmodelElementContainerBuilder createTypeCollectionBuilder(SubmodelBuilder smBuilder, 
        String typeName) {
        return smBuilder.createSubmodelElementCollectionBuilder(AasUtils.fixId(typeName), true, false);
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
        sBuilder.defineOperation(OP_CREATE_VARIABLE, 
            new JsonResultWrapper(a -> {
                getAasIvmlMapper().createVariable(AasUtils.readString(a, 0), AasUtils.readString(a, 1), 
                    AasUtils.readString(a, 2));
                return null;
            }, getAasOperationCompletedListener())
        );
        sBuilder.defineOperation(OP_DELETE_VARIABLE, 
            new JsonResultWrapper(a -> {
                getAasIvmlMapper().deleteVariable(AasUtils.readString(a));
                return null;
            }, getAasOperationCompletedListener())
        );
        // generate Interfaces, generate Templates
        sBuilder.defineOperation(OP_GEN_APPS, 
            new JsonResultWrapper(a -> {
                return TaskUtils.executeAsTask(PROGRESS_COMPONENT_ID, 
                    p -> getAasIvmlMapper().instantiate(InstantiationMode.APPS, AasUtils.readString(p), 
                        AasUtils.readString(p, 1))
                    , a);
            })
        );
        sBuilder.defineOperation(OP_GEN_APPS_NO_DEPS, 
            new JsonResultWrapper(a -> {
                return TaskUtils.executeAsTask(PROGRESS_COMPONENT_ID, 
                    p -> getAasIvmlMapper().instantiate(InstantiationMode.APPS_NO_DEPS, AasUtils.readString(p), null)
                    , a);
            })
        );
        sBuilder.defineOperation(OP_GEN_INTERFACES, 
            new JsonResultWrapper(a -> {
                return TaskUtils.executeAsTask(PROGRESS_COMPONENT_ID, 
                    p -> getAasIvmlMapper().instantiate(InstantiationMode.INTERFACES, null, null)
                    , a);
            })
        );
    }
    
    /**
     * Instantiates according to the given {@code configurer}.
     * 
     * @param mode the instantiation mode
     * @param appId the app to build
     * @param codeFile the code file containing the implementation
     * @return summary of instantiation results depending on {@code mode}, e.g., list of generated and downloadable 
     *     template URIs for {@link InstantiationMode#APPS_NO_DEPS} else <b>null</b>
     * @throws ExecutionException when the instantiation fails
     */
    private Object instantiate(InstantiationMode mode, String appId, String codeFile) throws ExecutionException {
        TaskData lastTaskData = TaskUtils.getLastTaskData(); // may be wrong thread
        if (null == lastTaskData) { // fallback
            lastTaskData = TaskRegistry.getTaskData();
        }
        TaskData beforeTaskData = ConfigurationManager.setTaskData(lastTaskData);
        if (InstantiationMode.APPS == mode && codeFile != null && codeFile.endsWith(".zip")) {
            File f = new File(ConfigurationSetup.getSetup().getUploadFolder(), codeFile);
            if (f.exists()) {
                f = fixZipConvention(f);
                LoggerFactory.getLogger(AasIvmlMapper.class).info("Integrating {} in {}", codeFile, f);
                System.setProperty("iip.easy.impl", f.getAbsolutePath());
            }
        }
        System.setProperty("KEY_PROPERTY_TRACING", "TOP");
        PlatformInstantiator.setTraceFilter();
        ConfigurationManager.cleanGenTarget();
        long start = System.currentTimeMillis();
        if (null != appId) {
            System.setProperty(PlatformInstantiator.KEY_PROPERTY_APPS, appId);
        }
        ReasoningResult rRes = ConfigurationManager.validateAndPropagate();
        if (null == rRes) {
            throw new ExecutionException("No valid IVML model loaded/found.", null);
        }
        EasyExecutor.printReasoningMessages(rRes);
        ConfigurationManager.setupContainerProperties();
        ConfigurationManager.instantiate(mode.getStartRuleName()); // throws exception if it fails
        if (null != appId) {
            System.setProperty(PlatformInstantiator.KEY_PROPERTY_APPS, "");
        }
        Object result = null;
        switch (mode) {
        case APPS_NO_DEPS:
            result = collectTemplates(start);
            break;
        case APPS:
        case INTERFACES:
            break;
        default:
            break;
        }
        ConfigurationManager.setTaskData(beforeTaskData);
        return result;
    }
    
    /**
     * Checking and fixing ZIP file conventions for instantiation. The instantiation requires that
     * either the project is directly located in the root folder of the ZIP or in a single contained
     * folder having the same name as the ZIP file. If possible, through renaming/copying, we fix the 
     * second situation here.
     * 
     * @param file the original ZIP file
     * @return the fixed ZIP file, may be {@code file}
     */
    private static File fixZipConvention(File file) {
        File result = file;
        Set<String> folders = new HashSet<String>();
        Set<String> files = new HashSet<String>();
        try {
            ZipUtils.listFiles(new FileInputStream(file), f -> true, f -> {
                String path = f.toString();
                int pos = path.indexOf("/");
                if (pos > 0) {
                    folders.add(path.substring(0, pos));
                } else {
                    files.add(path);
                }
            });
        } catch (IOException e) {
            LoggerFactory.getLogger(AasIvmlMapper.class).warn("Cannot scan ZIP: {}", e.getMessage());
        }
        if (folders.size() == 1 && files.size() == 0) {
            String name = file.getName();
            if (name.endsWith(".zip")) {
                name = name.substring(0, name.length() - 4);
            }
            Optional<String> folder = folders.stream().findAny();
            if (folder.isPresent() && !name.equals(folder.get())) {
                result = new File(FileUtils.getTempDirectory(), folder.get() + ".zip");
                try {
                    Files.copy(file.toPath(), result.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    LoggerFactory.getLogger(AasIvmlMapper.class).info("Zip convention: Using {} instead of {}", 
                        result, file);
                } catch (IOException e) {
                    LoggerFactory.getLogger(AasIvmlMapper.class).warn("Cannot copy ZIP: {}", e.getMessage());
                    result = file; // stay with the original
                }
            }
        }
        return result;
    }

    /**
     * Collects the generated templates, copies them to {@link ConfigurationSetup#getArtifactsFolder()} and returns 
     * the file names prefixed by {@link ConfigurationSetup#getArtifactsUriPrefix()} as JSON.
     * 
     * @param startTime the time the generation started (as ms timestamp)
     * @return the generated template archives
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
    
    /**
     * Returns whether {@code file} is acceptable for template copying/publishing.
     * 
     * @param file the file to check
     * @param startTime the start timestamp of the generation
     * @return {@code true} for acceptable, {@code false} else
     */
    private static boolean acceptTemplateFile(File file, long startTime) {
        boolean accept = file.isDirectory();
        if (!accept && file.getName().endsWith(".zip")) {
            accept = file.getName().startsWith("impl.") || file.getName().startsWith("ApplicationInterfaces.");
            //accept &= file.lastModified() > startTime;
        }
        return accept;
    }
    
    /**
     * Instantiation modes.
     * 
     * @author Holger Eichelberger, SSE
     */
    private enum InstantiationMode {
        
        APPS_NO_DEPS("generateAppsNoDeps"),
        APPS("generateApps"),
        INTERFACES("generateInterfaces");
        
        private String startRuleName;

        /**
         * Creates an instantiation mode constant.
         * 
         * @param startRuleName the start rule name
         */
        private InstantiationMode(String startRuleName) {
            this.startRuleName = startRuleName;
        }
        
        /**
         * Returns the start rule name.
         * 
         * @return the start rule name
         */
        public String getStartRuleName() {
            return startRuleName;
        }
        
    }

    @Override
    protected Project getVariableTarget(Project root, IDatatype type) {
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
            if (null == result) { // immediate fallback
                result = ModelQuery.findProject(root, PRJ_NAME_ALLCONSTANTS);
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
            if (isNonEmptyString(meshName)) {
                Project meshProject = ModelQuery.findProject(root, getMeshProjectName(appName, meshName));
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
                        deleteReferenceFromContainerValue(svc, meshVarDecl); 
                    }
                }
                File f = getIvmlFile(meshProject);
                copies.put(meshProject, copyToTmp(f));
                f.delete();
                notifyChange(meshProject, ConfigurationChangeType.DELETED);
            }
            if (null != appProject && null == meshName || meshName.length() == 0) {
                File f = getIvmlFile(appProject);
                copies.put(appProject, copyToTmp(f));
                f.delete();
                notifyChange(appProject, ConfigurationChangeType.DELETED);
            }
            reloadAndValidate(copies);
            LoggerFactory.getLogger(getClass()).info("Deleted graph in IVML, app '{}' mesh '{}'", 
                appName, meshName);
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
     * Returns whether {@code string} is a non-empty string.
     * 
     * @param string the string to test
     * @return {@code true} for a non-empty string, {@code false} for an empty string or <b>null</b>
     */
    private static boolean isNonEmptyString(String string) {
        return string != null && string.length() > 0;
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
        return "ServiceMeshPart" //+ toIdentifierFirstUpper(appName) // mesh can be part of many applications
            + toIdentifierFirstUpper(meshName);
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
        addImport(results.appProject, "AllServices", root, null);
        // > may go down to easy
        Project wildcardPrj = new Project("");
        for (String modelName : VarModel.INSTANCE.getMatchingModelNames("ServiceMeshPart*")) {
            Project tmp = ModelQuery.findProject(root, modelName);
            if (null != tmp) {
                addImport(wildcardPrj, modelName, root, tmp);
            }
        }
        addImport(results.appProject, "ServiceMeshPart*", root, wildcardPrj);

        // < may go down to Easy
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
        if (appValueEx.length() > 0) {
            setValue(appVar, appValueEx);
        }
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
     * Turns {@code string} into an identifier, i.e., each non-Java identifier characters into a "_".
     * 
     * @param string the string to be checked
     * @return the validated string, potentially modified to be an id
     */
    private static String toId(String string) {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if (!Character.isJavaIdentifierPart(c)) {
                c = '_';
            }
            result.append(c);
        }
        return result.toString();
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
        addImport(results.meshProject, "AllServices", root, null);
        final IDatatype sourceType = ModelQuery.findType(root, "MeshSource", null);
        final IDatatype processorType = ModelQuery.findType(root, "MeshProcessor", null);
        final IDatatype sinkType = ModelQuery.findType(root, "MeshSink", null);
        final IDatatype connectorType = ModelQuery.findType(root, "MeshConnector", null);
        final IDatatype serviceType = ModelQuery.findType(root, "ServiceBase", null);
        final IDatatype meshType = ModelQuery.findType(root, "ServiceMesh", null);
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
                type = processorType; // TODO Probe service
            }
            DecisionVariableDeclaration nodeVar = new DecisionVariableDeclaration(toId("node_" 
                + validateName(n, nodeMap.size())), type, results.meshProject);
            String nodeValEx = "{pos_x=" + n.getXPos() + ",pos_y=" + n.getYPos() 
                + ",impl=" + IvmlUtils.getVarNameSafe(findServiceVar(services, n.getImpl()), "null")
                + ",next = {";
            valueMap.put(nodeVar, nodeValEx);
            results.meshProject.add(nodeVar);
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
                results.meshProject.add(edgeVar);
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
            .build();
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
            .build();
        smBuilder.createOperationBuilder(OP_DELETE_GRAPH)
            .addInputVariable("appName", Type.STRING)
            .addInputVariable("serviceMeshName", Type.STRING)
            .setInvocable(iCreator.createInvocable(OP_DELETE_GRAPH))
            .build();
        smBuilder.createOperationBuilder(OP_GET_VARIABLE_NAME)
            .addInputVariable("type", Type.STRING)
            .addInputVariable("elementName", Type.STRING)
            .addInputVariable("elementVersion", Type.STRING)
            .setInvocable(iCreator.createInvocable(OP_GET_VARIABLE_NAME))
            .build(Type.STRING);
        smBuilder.createOperationBuilder(OP_CREATE_VARIABLE)
            .addInputVariable("varName", Type.STRING)
            .addInputVariable("type", Type.STRING)
            .addInputVariable("valExpr", Type.STRING)
            .setInvocable(iCreator.createInvocable(OP_CREATE_VARIABLE))
            .build();
        smBuilder.createOperationBuilder(OP_DELETE_VARIABLE)
            .addInputVariable("varName", Type.STRING)
            .setInvocable(iCreator.createInvocable(OP_DELETE_VARIABLE))
            .build();
        smBuilder.createOperationBuilder(OP_GEN_APPS)
            .setInvocable(iCreator.createInvocable(OP_GEN_APPS))
            .addInputVariable("appId", Type.STRING)
            .addInputVariable("codeFile", Type.STRING)
            .build(Type.STRING);
        smBuilder.createOperationBuilder(OP_GEN_APPS_NO_DEPS)
            .setInvocable(iCreator.createInvocable(OP_GEN_APPS_NO_DEPS))
            .addInputVariable("appId", Type.STRING)
            .build(Type.STRING);
        smBuilder.createOperationBuilder(OP_GEN_INTERFACES)
            .setInvocable(iCreator.createInvocable(OP_GEN_INTERFACES))
            .build(Type.STRING);
    }
    
    /**
     * Returns the language to be used for {@link LangString}.
     * 
     * @return the language
     */
    static String getLang() {
        return ModelInfo.getLocale().getLanguage();
    }
    
    /**
     * Returns the string value of {@code var}. If the value is empty, return <b>null</b>.
     * 
     * @param var the variable to read the string value from
     * @return the string value or <b>null</b>
     */
    static String getStringValueEmptyNull(IDecisionVariable var) {
        String result = null;
        Object val = getValue(var);
        if (null != val) {
            result = val.toString();
            if (result.length() == 0) {
                result = null;
            }
        }
        return result;
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
            IDatatype rVarType = DerivedDatatype.resolveToBasis(varType);
            String lang = getLang();
            String semanticId = null;
            String displayName = null;
            for (int a = 0; a < var.getAttributesCount(); a++) {
                IDecisionVariable attribute = var.getAttribute(a);
                String attributeName = attribute.getDeclaration().getName();
                if ("semanticId".equals(attributeName)) {
                    semanticId = getStringValueEmptyNull(attribute);
                } else if ("displayName".equals(attributeName)) {
                    displayName = getStringValueEmptyNull(attribute);
                }
            }
            SubmodelElementContainerBuilder varBuilder;
            if (TypeQueries.isCompound(rVarType)) {
                varBuilder = builder.createSubmodelElementCollectionBuilder(AasUtils.fixId(varName));
                for (int member = 0; member < var.getNestedElementsCount(); member++) {
                    IDecisionVariable elt = var.getNestedElement(member);
                    mapVariable(elt, varBuilder, null);
                }
            } else if (TypeQueries.isContainer(rVarType)) {
                boolean isSequence = TypeQueries.isSequence(rVarType);
                boolean isOrdered = isSequence; // just to clarify
                boolean allowsDuplicates = isSequence; // just to clarify
                varBuilder = builder.createSubmodelElementCollectionBuilder(
                    AasUtils.fixId(varName), isOrdered, allowsDuplicates);
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
                Object aasValue = getValue(var);
                varType.getType().accept(TYPE_VISITOR); // resolved anyway
                Type aasType = TYPE_VISITOR.getAasType();
                // value is reserved by BaSyx/AAS
                PropertyBuilder pb = varBuilder.createPropertyBuilder(AasUtils.fixId("varValue")); 
                pb.setValue(aasType, aasValue);
                /*if (var.getState() == AssignmentState.FROZEN) {
                    pb.setValue(aasType, aasValue);
                } else {
                    // not serializable, e.g., needs to query from qualified name
                    pb.setType(aasType).bind(
                        ((Supplier<Object> & Serializable) () -> getValue(var)), 
                        PropertyBuilder.READ_ONLY);
                }*/
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
        IDatatype type = var.getValue() != null ? var.getValue().getType() : varType;
        String declaredTypeName = IvmlDatatypeVisitor.getUnqualifiedType(var.getDeclaration().getType());
        String varName = var.getDeclaration().getName();
        varBuilder.createPropertyBuilder(AasUtils.fixId(metaShortId.apply("type")))
            .setValue(Type.STRING, IvmlDatatypeVisitor.getUnqualifiedType(type))
            .build();
        if (null == displayName && "OktoVersion".equals(declaredTypeName) && "ver".equals(varName)) {
            // mitigate field misnomer for now
            displayName = "version";
        }
        TypeMapper.addMetaDefault(var, varBuilder, metaShortId);
        TypeMapper.addTypeKind(varBuilder, DerivedDatatype.resolveToBasis(type), metaShortId);
        if (var.getDeclaration().getParent() instanceof Project) { // top-level only for now
            varBuilder.createPropertyBuilder(AasUtils.fixId(metaShortId.apply("project")))
                .setValue(Type.STRING, mapParent(var))
                .build();
        }
        if (null != displayName) {
            varBuilder.createPropertyBuilder(AasUtils.fixId(metaShortId.apply("displayName")))
                .setValue(Type.STRING, displayName)
                .build();
        }
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
     * Sets a semantic id if available. 
     * 
     * @param pBuilder the property builder
     * @param semanticId the semantic id, may be <b>null</b>
     */
    private static void setSemanticId(PropertyBuilder pBuilder, String semanticId) {
        if (null != semanticId) {
            if (!semanticId.startsWith(IdentifierType.IRDI_PREFIX)) {
                semanticId = IdentifierType.IRDI_PREFIX + semanticId;
            }
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
        if (null != c) {
            c.deleteElement(AasUtils.fixId(varName));
        }
    }

    /**
     * Maps {@code var} into the submodel represented by {@code smB}.
     * 
     * @param smB the submodel builder
     * @param var the variable
     */
    private void mapVariableToAas(SubmodelBuilder smB, IDecisionVariable var) {
        SubmodelElementContainerBuilder builder = createTypeCollectionBuilder(smB, getType(var));
        mapVariable(var, builder, null);
        builder.justBuild();
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
