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
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
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

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.configuration.ConfigurationManager;
import de.iip_ecosphere.platform.configuration.ConfigurationSetup;
import de.iip_ecosphere.platform.configuration.ModelInfo;
import de.iip_ecosphere.platform.configuration.ivml.IvmlGraphMapper.IvmlGraph;
import de.iip_ecosphere.platform.configuration.ivml.IvmlGraphMapper.IvmlGraphEdge;
import de.iip_ecosphere.platform.configuration.ivml.IvmlGraphMapper.IvmlGraphNode;
import de.iip_ecosphere.platform.support.FileUtils;
import de.iip_ecosphere.platform.support.aas.InvocablesCreator;
import de.iip_ecosphere.platform.support.aas.LangString;
import de.iip_ecosphere.platform.support.aas.Property.PropertyBuilder;
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection.SubmodelElementCollectionBuilder;
import de.iip_ecosphere.platform.support.aas.Type;
import de.iip_ecosphere.platform.support.iip_aas.AasUtils;
import de.iip_ecosphere.platform.support.iip_aas.NameplateSetup;
import de.iip_ecosphere.platform.support.iip_aas.json.JsonResultWrapper;
import de.iip_ecosphere.platform.support.iip_aas.json.JsonUtils;
import net.ssehub.easy.basics.modelManagement.ModelManagementException;
import net.ssehub.easy.instantiation.core.model.vilTypes.PseudoString;
import net.ssehub.easy.instantiation.core.model.vilTypes.configuration.ChangeHistory;
import net.ssehub.easy.instantiation.core.model.vilTypes.configuration.Configuration;
import net.ssehub.easy.reasoning.core.frontend.ReasonerFrontend;
import net.ssehub.easy.reasoning.core.reasoner.Message;
import net.ssehub.easy.reasoning.core.reasoner.ReasoningResult;
import net.ssehub.easy.varModel.confModel.AssignmentState;
import net.ssehub.easy.varModel.confModel.ConfigurationException;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.cst.CSTSemanticException;
import net.ssehub.easy.varModel.cst.ConstantValue;
import net.ssehub.easy.varModel.cst.ConstraintSyntaxTree;
import net.ssehub.easy.varModel.cst.OCLFeatureCall;
import net.ssehub.easy.varModel.cst.Variable;
import net.ssehub.easy.varModel.model.AbstractVariable;
import net.ssehub.easy.varModel.model.Constraint;
import net.ssehub.easy.varModel.model.ContainableModelElement;
import net.ssehub.easy.varModel.model.DecisionVariableDeclaration;
import net.ssehub.easy.varModel.model.IvmlDatatypeVisitor;
import net.ssehub.easy.varModel.model.ModelQuery;
import net.ssehub.easy.varModel.model.ModelQueryException;
import net.ssehub.easy.varModel.model.Project;
import net.ssehub.easy.varModel.model.ProjectImport;
import net.ssehub.easy.varModel.model.datatypes.Compound;
import net.ssehub.easy.varModel.model.datatypes.IDatatype;
import net.ssehub.easy.varModel.model.datatypes.TypeQueries;
import net.ssehub.easy.varModel.model.filter.ConstraintSeparator;
import net.ssehub.easy.varModel.model.values.ContainerValue;
import net.ssehub.easy.varModel.model.values.ReferenceValue;
import net.ssehub.easy.varModel.model.values.Value;
import net.ssehub.easy.varModel.model.values.ValueDoesNotMatchTypeException;
import net.ssehub.easy.varModel.model.values.ValueFactory;
import net.ssehub.easy.varModel.persistency.IVMLWriter;

/**
 * Maps an IVML configuration generically into an AAS.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AasIvmlMapper implements DecisionVariableProvider {
    
    public static final String OP_CHANGE_VALUES = "changeValues";
    public static final String OP_GET_GRAPH = "getGraph";
    public static final String OP_SET_GRAPH = "setGraph";
    public static final String OP_CREATE_VARIABLE = "createVariable";
    public static final String OP_DELETE_VARIABLE = "deleteVariable";
    
    public static final Predicate<IDecisionVariable> FILTER_NO_CONSTRAINT_VARIABLES = 
        v -> !TypeQueries.isConstraint(v.getDeclaration().getType());
    public static final Function<String, String> SHORTID_PREFIX_META = n -> "meta" + capitalizeFirst(n);
    private static final TypeVisitor TYPE_VISITOR = new TypeVisitor();
    
    private Supplier<Configuration> cfgSupplier;
    private IvmlGraphMapper graphMapper;
    private Map<String, GraphFormat> graphFormats = new HashMap<>();
    private Function<String, String> metaShortId = SHORTID_PREFIX_META;
    private Predicate<IDecisionVariable> variableFilter = FILTER_NO_CONSTRAINT_VARIABLES;
    
    /**
     * Creates a mapper with default settings, e.g., short ids for meta IVML information are
     * prefixed by "meta" ({@link #SHORTID_PREFIX_META}) and the variable filter excludes all IVML constraint 
     * variables ({@link #FILTER_NO_CONSTRAINT_VARIABLES}).
     * 
     * @param cfgSupplier a supplier providing the actual configuration instance
     * @param graphMapper maps a graph from IVML to an internal structure
     * @throws IllegalArgumentException if {@code cfgSupplier} is <b>null</b>
     */
    public AasIvmlMapper(Supplier<Configuration> cfgSupplier, IvmlGraphMapper graphMapper) {
        if (null == cfgSupplier) {
            throw new IllegalArgumentException("cfgSupplier must not be null");
        }
        if (null == graphMapper) {
            throw new IllegalArgumentException("graphMapper must not be null");
        }
        this.cfgSupplier = cfgSupplier;
        this.graphMapper = graphMapper;
    }

    /**
     * Adds a graph format.
     * 
     * @param format the format
     */
    public void addGraphFormat(GraphFormat format) {
        if (null != format) {
            graphFormats.put(format.getName(), format);
        }
    }
    
    /**
     * Returns the factory to use to crate graphs.
     * 
     * @return the factory
     */
    public GraphFactory getGraphFactory() {
        return graphMapper.getGraphFactory();
    }

    /**
     * Helper function to capitalize the first character in the given {@code str}.
     * 
     * @param str the string to be capitalized (may be <b>null</b>)
     * @return the capitalized string
     */
    public static String capitalizeFirst(String str) {
        String result;
        if (str == null || str.length() <= 1) {
            result = str;
        } else {
            result = str.substring(0, 1).toUpperCase() + str.substring(1);
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
            Iterator<IDecisionVariable> iter = cfg.getConfiguration().iterator();
            while (iter.hasNext()) {
                IDecisionVariable var = iter.next();
                if (variableFilter.test(var)) {
                    IDatatype type = var.getDeclaration().getType();
                    String typeName = type.getName();
                    SubmodelElementCollectionBuilder builder = types.get(typeName);
                    if (null == builder) {
                        builder = smBuilder.createSubmodelElementCollectionBuilder(
                            AasUtils.fixId(typeName), true, false);
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
     * Binds the AAS operations.
     * 
     * @param sBuilder the server builder
     */
    public void bindOperations(ProtocolServerBuilder sBuilder) {
        sBuilder.defineOperation(OP_CHANGE_VALUES, 
            new JsonResultWrapper(a -> changeValues(AasUtils.readMap(a, 0, null))));
        sBuilder.defineOperation(OP_GET_GRAPH, 
            new JsonResultWrapper(a -> getGraph(AasUtils.readString(a, 0), AasUtils.readString(a, 1))));
        sBuilder.defineOperation(OP_SET_GRAPH, 
            new JsonResultWrapper(a ->  
                setGraph(JsonUtils.fromJson(AasUtils.readString(a, 0), AppInfo.class), 
                    AasUtils.readString(a, 1), AasUtils.readString(a, 2), AasUtils.readString(a, 3))
            ));
        sBuilder.defineOperation(OP_CREATE_VARIABLE, 
            new JsonResultWrapper(a -> createVariable(AasUtils.readString(a, 0), AasUtils.readString(a, 1), 
                AasUtils.readMap(a, 0, null))));
        sBuilder.defineOperation(OP_DELETE_VARIABLE, 
            new JsonResultWrapper(a -> deleteVariable(AasUtils.readString(a))));
    }

    /**
     * Changes a given set of values and performs reasoning before committing the values into the actual configuration.
     * 
     * @param values the values, given as qualified IVML variables names mapped to serialized values
     * @return <b>null</b>
     * @throws ExecutionException if changing values fails
     */
    private synchronized Object changeValues(Map<String, String> values) throws ExecutionException {
        Configuration cfg = cfgSupplier.get();
        Set<Project> projects = new HashSet<>();
        Map<String, IDecisionVariable> vars = new HashMap<>();
        for (String varName: values.keySet()) {
            vars.put(varName, getVariable(cfg, varName));
        }
        ChangeHistory history = cfg.getChangeHistory();
        history.start();
        for (Map.Entry<String, String> ent: values.entrySet()) {
            IDecisionVariable var = vars.get(ent.getKey());
            // ent.getKey may have to be parsed before
            AbstractVariable decl = var.getDeclaration();
            try {
                var.setValue(ValueFactory.createValue(decl.getType(), ent.getValue()), 
                    AssignmentState.USER_ASSIGNED);
                projects.add(decl.getProject());
            } catch (ValueDoesNotMatchTypeException | ConfigurationException e) {
                history.rollback();
                throw new ExecutionException(e.getMessage(), null);
            }
        }
        ReasoningResult result = ReasonerFrontend.getInstance().propagate(cfg.getConfiguration(), null, null);
        if (result.hasConflict()) {
            history.rollback();
            String text = "";
            for (int m = 0; m < result.getMessageCount(); m++) {
                if (m > 0) {
                    text += "\n";
                }
                Message msg = result.getMessage(m);
                text += msg.getStatus();
                text += ": ";
                text += msg.getDetailedDescription();
            }
            throw new ExecutionException(text, null);
        } else {
            history.commit();
            Map<Project, CopiedFile> copies = new HashMap<>();
            for (Project p: projects) {
                File f = getIvmlFile(p);
                copies.put(p, copyToTmp(f));
                saveTo(p, f);
            }
            reloadAndValidate(copies);
        }
        return null;
    }
    
    /**
     * Returns the filename/path for {@code p}.
     * 
     * @param project the project
     * @return the filename/path
     */
    private File getIvmlFile(Project project) {
        String projectName = project.getName();
        String subpath;
        if (projectName.startsWith("ServiceMeshPart")) {
            subpath = "meshes";
        } else if (projectName.startsWith("ApplicationPart")) {
            subpath = "apps";
        } else {
            subpath = null;
        }
        return createIvmlConfigPath(subpath, project);
    }
    
    /**
     * Creates an IVML configuration (not meta-model) model path with {@code subpath} and for project {@code p}.
     *  
     * @param subpath the subpath, may be <b>null</b> for none
     * @param project the project to create the path for
     * @return the file name/path
     */
    private File createIvmlConfigPath(String subpath, Project project) {
        File result = ConfigurationSetup.getSetup().getEasyProducer().getIvmlConfigFolder();
        if (subpath != null) {
            result = new File(result, subpath);
        }
        return new File(result, project.getName() + ".ivml");
    }
    
    /**
     * Returns a graph structure in IVML.
     * 
     * @param varName the IVML variable holding the graph
     * @param format the format of the graph to return 
     * @return the graph in the specified {@code format}
     * @throws ExecutionException if reading the graph structure fails
     */
    private String getGraph(String varName, String format) throws ExecutionException {
        GraphFormat gFormat = getGraphFormat(format);
        IDecisionVariable var = getVariable(varName);
        IvmlGraph graph = graphMapper.getGraphFor(var);
        return gFormat.toString(graph);
    }

    /**
     * Changes a graph structure in IVML.
     * 
     * @param appInfo the configured information of the application
     * @param meshName the configured name of the service mesh
     * @param format the format of the graph
     * @param value the value
     * @return <b>null</b> always
     * @throws ExecutionException if setting the graph structure fails
     */
    private synchronized Object setGraph(AppInfo appInfo, String meshName, String format, String value) 
        throws ExecutionException {
        GraphFormat gFormat = getGraphFormat(format);
        IvmlGraph graph = gFormat.fromString(value, graphMapper.getGraphFactory(), this);
        
        try {
            ModelResults results = new ModelResults();
            createMeshProject(appInfo, meshName, graph, results);
            createAppProject(appInfo, results);

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
     * Stores original and copied file.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class CopiedFile {
        
        private File original;
        private File copy;

        /**
         * Creates an instance.
         * 
         * @param original the original
         * @param copy the copy, may be <b>null</b> if {@code original} was yet created
         */
        private CopiedFile(File original, File copy) {
            this.original = original;
            this.copy = copy;
        }
        
        /**
         * Restores the original file or, if no copy exists/{@link #original} was yet created, 
         * deletes {@link #original}.
         * 
         * @throws IOException if copying/overwriting fails
         */
        private void restore() throws IOException {
            if (null == copy) {
                original.delete();
            } else {
                Files.copy(copy.toPath(), original.toPath(), StandardCopyOption.REPLACE_EXISTING);                
            }
        }
    }
    
    /**
     * Copies {@code file} to temp if {@code file} exists.
     * 
     * @param file the file to copy
     * @return the copied file, else <b>null</b>
     * @throws ExecutionException if copying failed
     */
    private static CopiedFile copyToTmp(File file) throws ExecutionException {
        CopiedFile result = null;
        if (file.exists()) {
            File cp = new File(FileUtils.getTempDirectory(), file.getName());
            try {
                Files.copy(file.toPath(), cp.toPath(), StandardCopyOption.REPLACE_EXISTING);                
                result = new CopiedFile(file, cp);
            } catch (IOException e) {
                throw new ExecutionException(e);
            }
        } else {
            result = new CopiedFile(file, null);
        }
        return result;
    }

    /**
     * Saving model project {@code prj} to {@code file}.
     * 
     * @param prj the project
     * @param file the file to write to
     * @throws ExecutionException if writing fails
     */
    private static void saveTo(Project prj, File file) throws ExecutionException {
        try (FileWriter fWriter = new FileWriter(file)) {
            IVMLWriter writer = new IVMLWriter(fWriter);
            prj.accept(writer);
            fWriter.close();
        } catch (IOException e) {
            throw new ExecutionException(e);
        }
    }

    /**
     * Reloads the and validates the model, in case of problems, restore changed files from {@code copies}.
     * 
     * @param copies copied files to be restored
     * @throws ExecutionException if reasoning/restoring fails
     */
    private static void reloadAndValidate(Map<Project, CopiedFile> copies) throws ExecutionException {
        ConfigurationManager.reload();
        ReasoningResult res = ConfigurationManager.validateAndPropagate();
        String msg = "";
        if (res.hasConflict()) {
            for (CopiedFile c : copies.values()) {
                try {
                    c.restore();
                } catch (IOException e) {
                    if (msg.length() > 0) {
                        msg += "\n";
                    }
                    msg += e.getMessage();
                }
            }
            if (msg.length() > 0) {
                throw new ExecutionException("Cannot restore model: " + msg, null);
            }
            ConfigurationManager.reload(); // use original?
            throwIfFails(res, false);
        }
    }
    
    /**
     * Stores intermediary model creation results.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class ModelResults {
        
        private Project meshProject;
        private DecisionVariableDeclaration meshVar;
        private Project appProject;
        
    }

    /**
     * Represents application information.
     * 
     * @author Holger Eichelberger, SSE
     */
    static class AppInfo {
        
        private String id;
        private String name;
        private String description;
        private String version;
        private boolean snapshot;
        private NameplateSetup nameplate = new NameplateSetup();
        
        /**
         * Returns the id.
         * 
         * @return the id
         */
        public String getId() {
            return id;
        }
        
        /**
         * Changes the id.
         * 
         * @param id the id to set
         */
        public void setId(String id) {
            this.id = id;
        }
        
        /**
         * Returns the name.
         * 
         * @return the name
         */
        public String getName() {
            return name;
        }
        
        /**
         * Changes the name.
         * 
         * @param name the name to set
         */
        public void setName(String name) {
            this.name = name;
        }
        
        /**
         * String returns the description.
         * 
         * @return the description
         */
        public String getDescription() {
            return description;
        }
        
        /**
         * Changes the description.
         * 
         * @param description the description to set
         */
        public void setDescription(String description) {
            this.description = description;
        }
        
        /**
         * Returns the version.
         * 
         * @return the version
         */
        public String getVersion() {
            return version;
        }
        
        /**
         * Changes the version.
         * 
         * @param version the version to set
         */
        public void setVersion(String version) {
            this.version = version;
        }
        
        /**
         * Returns whether it shall be a snapshot.
         * 
         * @return shall it be a snapshot
         */
        public boolean getSnapshot() {
            return snapshot;
        }
        
        /**
         * Changes the snapshot property.
         * 
         * @param snapshot the snapshot to set
         */
        public void setSnapshot(boolean snapshot) {
            this.snapshot = snapshot;
        }
        
        /**
         * Returns the nameplate information.
         * 
         * @return the nameplate
         */
        public NameplateSetup getNameplate() {
            return nameplate;
        }
        
        /**
         * Sets the nameplate information.
         * 
         * @param nameplate the nameplate to set
         */
        public void setNameplate(NameplateSetup nameplate) {
            this.nameplate = nameplate;
        } 
        
    }

    /**
     * Writes an application project with the given new mesh variable.
     * 
     * @param appInfo the application info
     * @param results collected result information
     */
    private static void createAppProject(AppInfo appInfo, ModelResults results) 
        throws ModelQueryException, ModelManagementException {
        net.ssehub.easy.varModel.confModel.Configuration cfg = ConfigurationManager.getIvmlConfiguration();
        Project root = cfg.getProject();
        IDatatype applicationType = ModelQuery.findType(root, "Application", null);
        
        String appProjectName = "ApplicationPart" + toIdentifierFirstUpper(appInfo.getName());
        results.appProject = ModelQuery.findProject(root, appProjectName);
        results.meshProject.addImport(new ProjectImport("Applications"));
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
                            if (isRefWithName(appInfo.getName(), cfg, mVal)) {
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
            
        results.appProject = new Project(appProjectName);
        DecisionVariableDeclaration appVar = new DecisionVariableDeclaration(
            toIdentifier(appInfo.getName()), applicationType, results.appProject);
        List<Object> values = new ArrayList<>();
        addValue(values, "id", appInfo.getId());
        addValue(values, "name", appInfo.getName());
        addValue(values, "description", appInfo.getDescription()); 
        addValue(values, "ver", appInfo.getVersion());
        addValue(values, "snapshot", appInfo.getSnapshot());
        addListValue(values, "services", meshes);
        // TODO nameplate info
        setValue(appVar, values);
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
            String var2Name = getValue(var2.getNestedElement("name")).toString();
            if (var2Name.equals(name)) {
                result = true;
            }
        }
        return result;
    }

    /**
     * Helper to turn the first char of {@code str} into upper case and {@code str} into an identifier..
     * 
     * @param str the string
     * @return the identifier
     */
    private static String toIdentifierFirstUpper(String str) {
        return PseudoString.firstToUpperCase(toIdentifier(str));
    }

    /**
     * Helper to turn {@code str} into a Java identifier.
     * 
     * @param str the text
     * @return the identifier
     */
    private static String toIdentifier(String str) {
        return PseudoString.toIdentifier(str);
    }

    /**
     * Creates a mesh project for {@code graph}.
     * 
     * @param appInfo the application information
     * @param meshName the mesh name
     * @param graph
     * @param results
     * @throws ModelQueryException
     * @throws ModelManagementException
     */
    private static void createMeshProject(AppInfo appInfo, String meshName, IvmlGraph graph, ModelResults results) 
         throws ModelQueryException, ModelManagementException {
        net.ssehub.easy.varModel.confModel.Configuration cfg = ConfigurationManager.getIvmlConfiguration();
        Project root = cfg.getProject();
        results.meshProject = new Project("ServiceMeshPart" + toIdentifierFirstUpper(appInfo.getName()) 
            + toIdentifierFirstUpper(meshName));
        results.meshProject.addImport(new ProjectImport("Applications"));
        IDatatype sourceType = ModelQuery.findType(root, "MeshSource", null);
        IDatatype transformerType = ModelQuery.findType(root, "MeshTransformer", null);
        IDatatype sinkType = ModelQuery.findType(root, "MeshSink", null);
        IDatatype connectorType = ModelQuery.findType(root, "MeshConnector", null);
        IDatatype serviceType = ModelQuery.findType(root, "ServiceBase", null);
        IDatatype applicationType = ModelQuery.findType(root, "Application", null);
        Map<String, IDecisionVariable> services = collectServices(cfg, serviceType);
        Map<IvmlGraphNode, DecisionVariableDeclaration> nodeMap = new HashMap<>();
        Map<DecisionVariableDeclaration, List<Object>> valueMap = new HashMap<>();
        List<Object> sources = new ArrayList<>();
        for (IvmlGraphNode n : graph.nodes()) {
            IDatatype type;
            if (n.getInEdgesCount() == 0) {
                type = sourceType;
            } else if (n.getOutEdgesCount() == 0) {
                type = sinkType;
            } else {
                type = transformerType; // TODO Probe service
            }
            DecisionVariableDeclaration nodeVar = new DecisionVariableDeclaration(n.getName(), type, 
                results.meshProject);
            List<Object> values = new ArrayList<Object>();
            addValue(values, "x_pos", n.getXPos());
            addValue(values, "y_pos", n.getYPos());
            addValue(values, "impl", findServiceVar(services, n.getName()));
            valueMap.put(nodeVar, values);
            results.meshProject.add(nodeVar);
            nodeMap.put(n, nodeVar);
            if (type == sourceType) {
                sources.add(nodeVar);
            }
        }
        for (IvmlGraphNode n: graph.nodes()) {
            for (IvmlGraphEdge e: n.outEdges()) {
                String edgeName = e.getName();
                if (null == edgeName || edgeName.length() == 0) {
                    edgeName = n.getName() + " -> " + e.getEnd().getName();
                }
                DecisionVariableDeclaration edgeVar = new DecisionVariableDeclaration(
                    edgeName, connectorType, results.meshProject);
                List<Object> values = new ArrayList<>();
                addValue(values, "name", edgeName);
                addValue(values, "next", nodeMap.get(e.getEnd()));
                setValue(edgeVar, values);
                List<Object> startNodeValues = valueMap.get(nodeMap.get(n));
                addListValue(startNodeValues, "next", nodeMap.get(n));
            }
        }
        for (IvmlGraphNode n: graph.nodes()) {
            DecisionVariableDeclaration nodeVar = nodeMap.get(n);
            setValue(nodeVar, valueMap.get(nodeVar)); 
        }
        results.meshVar = new DecisionVariableDeclaration(toIdentifier(meshName), applicationType, results.meshProject);
        List<Object> values = new ArrayList<>();
        addValue(values, "description", meshName);
        addListValue(values, "sources", sources);
        setValue(results.meshVar, values);
    }

    /**
     * Collects all declared services.
     * 
     * @param cfg the configuration to take the services from
     * @param serviceType the IVML data type used to select services
     * @return a mapping between service names and configured IVML variables
     */
    private static Map<String, IDecisionVariable> collectServices(net.ssehub.easy.varModel.confModel.Configuration cfg, 
        IDatatype serviceType) {
        Map<String, IDecisionVariable> result = new HashMap<>();
        Iterator<IDecisionVariable> iter = cfg.iterator();
        while (iter.hasNext()) {
            IDecisionVariable cVar = iter.next();
            if (serviceType.isAssignableFrom(cVar.getDeclaration().getType())) {
                String name = cVar.getDeclaration().getName(); // just fallback
                IDecisionVariable nameVar = cVar.getNestedElement("name");
                if (null != nameVar) {
                    Object nameVal = getValue(nameVar);
                    if (null != nameVal) {
                        name = nameVal.toString(); // is AAS value, i.e., String
                    }
                }
                result.put(name, cVar);
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
    private static AbstractVariable findServiceVar(Map<String, IDecisionVariable> services, String name) {
        AbstractVariable result = null;
        IDecisionVariable cVar = services.get(name);
        if (null != cVar) {
            result = cVar.getDeclaration();
        }
        return result;
    }

    /**
     * Adds a list value to a (temporary) IVML default variables list.
     * 
     * @param values the values to be modified as side effect
     * @param name the name of the variable
     * @param value the actual value
     */
    @SuppressWarnings("unchecked")
    private static void addListValue(List<Object> values, String name, Object value) {
        List<Object> list = null;
        int pos = -1;
        for (int i = 0; i < values.size(); i = i + 2) {
            if (name.equals(values.get(i))) {
                pos = i + 1;
                if (values.get(pos) instanceof List) {
                    list = (List<Object>) values.get(pos);
                }
            }
        }
        if (list == null) {
            list = new ArrayList<>();
            addValue(values, name, list);
        }
        list.add(value);
    }

    /**
     * Adds a value to a (temporary) IVML default variables list.
     * 
     * @param values the values to be modified as side effect
     * @param name the name of the variable
     * @param value the actual value
     */
    private static void addValue(List<Object> values, String name, Object value) {
        values.add(name);
        values.add(value);
    }

    /**
     * Sets a value represented as list on the given decision variable.
     * 
     * @param var the variable
     * @param values the values
     */
    private static void setValue(DecisionVariableDeclaration var, List<Object> values) {
        try {
            // inner lists to array for value factory?
            Object[] val = values.toArray();
            var.setValue(new ConstantValue(ValueFactory.createValue(var.getType(), val))); 
        } catch (ValueDoesNotMatchTypeException | CSTSemanticException e) {
            LoggerFactory.getLogger(AasIvmlMapper.class).error("Turning graph to IVML: {}", e.getMessage());
        }
    }

    @Override
    public IDecisionVariable getVariable(String qualifiedVarName) throws ExecutionException {
        return getVariable(cfgSupplier.get(), qualifiedVarName);
    }

    /**
     * Returns an IVML variable.
     * 
     * @param cfg the configuration to take the variable from
     * @param qualifiedVarName the (qualified) variable name
     * @return the variable
     * @throws ExecutionException if querying the variable fails
     */
    private IDecisionVariable getVariable(Configuration cfg, String qualifiedVarName) throws ExecutionException {
        try {
            return cfg.getConfiguration().getDecision(qualifiedVarName, false);
        } catch (ModelQueryException e) {
            throw new ExecutionException(e.getMessage(), null);
        }
    }

    /**
     * Returns a graph format instance.
     * 
     * @param format the unique name of the graph format
     * @return the graph format instance
     * @throws ExecutionException if the format instance cannot be found
     */
    private GraphFormat getGraphFormat(String format) throws ExecutionException {
        if (null == format) {
            throw new ExecutionException("format must not be null", null);
        }
        GraphFormat result = graphFormats.get(format);
        if (null == result) {
            throw new ExecutionException("format '" + format + "' is unknown", null);
        }
        return result;
    }

    /**
     * Creates an IVML variable.
     * 
     * @param varName the IVML variable name
     * @param type the (qualified) IVML type
     * @param value the value
     * @return <b>null</b> always
     * @throws ExecutionException if creating the variable fails
     */
    private Object createVariable(String varName, String type, Map<String, String> value) throws ExecutionException {
        net.ssehub.easy.varModel.confModel.Configuration cfg = ConfigurationManager.getIvmlConfiguration();
        Project root = cfg.getProject();
        try {
            IDatatype t = ModelQuery.findType(root, type, null);
            if (null != t) {
                DecisionVariableDeclaration var = new DecisionVariableDeclaration(toIdentifier(varName), t, root);
                if (t instanceof Compound) {
                    Compound tc = (Compound) t;
                    List<Object> values = new ArrayList<>();
                    for (Map.Entry<String, String> ent : value.entrySet()) {
                        try {
                            DecisionVariableDeclaration slot = tc.getElement(ent.getKey());
                            if (null == slot) {
                                throw new ExecutionException("No such slot: " + ent.getKey(), null);
                            }
                            addValue(values, ent.getKey(), ValueFactory.createValue(slot.getType(), ent.getValue()));
                        } catch (ValueDoesNotMatchTypeException e) {
                            throw new ExecutionException(e.getMessage(), null);
                        }
                    }
                    setValue(var, values);
                } else {
                    throw new ExecutionException("No such variable: " + varName, null);
                }
            } else {
                throw new ExecutionException("No such type " + t, null);
            }
            ReasoningResult res = ConfigurationManager.validateAndPropagate();
            throwIfFails(res, true);
            saveTo(root, getIvmlFile(root));
        } catch (ModelQueryException e) {
            throw new ExecutionException(e);
        }
        return null;
    }

    /**
     * Deletes an IVML variable. In case of a graph, this may subsequently delete further variables.
     * 
     * @param varName the qualified IVML variable name to delete
     * @return <b>null</b> always
     * @throws ExecutionException if creating the variable fails
     */
    private Object deleteVariable(String varName) throws ExecutionException {
        net.ssehub.easy.varModel.confModel.Configuration cfg = ConfigurationManager.getIvmlConfiguration();
        Project root = cfg.getProject();
        try {
            AbstractVariable var = ModelQuery.findVariable(root, varName, null);
            if (null != var) {
                Project prj = var.getProject();
                ConstraintSeparator sep = new ConstraintSeparator(prj);
                for (Constraint c : sep.getAssingmentConstraints()) {
                    ConstraintSyntaxTree op = ((OCLFeatureCall) c.getConsSyntax()).getOperand();
                    if (op instanceof Variable) {
                        if (((Variable) op).getVariable() == var) {
                            prj.removeElement(c);
                        }
                    }
                }
                prj.removeElement(var);
                ReasoningResult res = ConfigurationManager.validateAndPropagate();
                throwIfFails(res, true);
                saveTo(prj, getIvmlFile(prj));
            } else {
                throw new ExecutionException("Cannot find variable " + varName, null);
            }
        } catch (ModelQueryException e) {
            throw new ExecutionException(e);
        }
        return null;
    }

    /**
     * Throws an {@link ExecutionException} if the reasoning result {@code res} indicates a problem.
     * 
     * @param res the reasoning result
     * @param reloadIfFail reload the model if there is a failure
     * @throws ExecutionException the exception if reasoning failed
     */
    private static void throwIfFails(ReasoningResult res, boolean reloadIfFail) throws ExecutionException {
        if (res.hasConflict()) {
            if (reloadIfFail) {
                ConfigurationManager.reload();
            }
            String msg = "";
            for (int m = 0; m < res.getMessageCount(); m++) {
                if (msg.length() > 0) {
                    msg += "\n";
                }
                msg += res.getMessage(m).getDetailedDescription();
            }
            throw new ExecutionException(msg, null);
        }
    }
    
    /**
     * Adds the default AAS operations for obtaining information on the configuration model / changing the model.
     * 
     * @param smBuilder the submodel builder representing the target
     * @param iCreator the invocables creator for operations
     */
    private void addOperations(SubmodelBuilder smBuilder, InvocablesCreator iCreator) {
        smBuilder.createOperationBuilder(OP_CHANGE_VALUES)
            .addInputVariable("values", Type.STRING)
            .setInvocable(iCreator.createInvocable(OP_CHANGE_VALUES))
            .build(Type.NONE);
        smBuilder.createOperationBuilder(OP_GET_GRAPH)
            .addInputVariable("varName", Type.STRING)
            .addInputVariable("format", Type.STRING)
            .setInvocable(iCreator.createInvocable(OP_GET_GRAPH))
            .build(Type.STRING);
        smBuilder.createOperationBuilder(OP_SET_GRAPH)
            .addInputVariable("application", Type.STRING)
            .addInputVariable("serviceMesh", Type.STRING)
            .addInputVariable("format", Type.STRING)
            .addInputVariable("val", Type.STRING)
            .setInvocable(iCreator.createInvocable(OP_SET_GRAPH))
            .build(Type.NONE);
        smBuilder.createOperationBuilder(OP_CREATE_VARIABLE)
            .addInputVariable("varName", Type.STRING)
            .addInputVariable("type", Type.STRING)
            .addInputVariable("val", Type.STRING)
            .setInvocable(iCreator.createInvocable(OP_CREATE_VARIABLE))
            .build(Type.NONE);
        smBuilder.createOperationBuilder(OP_DELETE_VARIABLE)
            .addInputVariable("varName", Type.STRING)
            .setInvocable(iCreator.createInvocable(OP_DELETE_VARIABLE))
            .build(Type.NONE);
    }
    
    /**
     * Maps a single variable {@code var} into {@code builder}.
     * 
     * @param var the variable to map as source
     * @param builder the builder as target
     * @param id the id to use as variable name instead of the variable name itself, may be <b>null</b> for 
     *     the variable name
     */
    private void mapVariable(IDecisionVariable var, SubmodelElementCollectionBuilder builder, String id) {
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
                    pb.setType(aasType).bind(() -> getValue(var), PropertyBuilder.READ_ONLY);
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
        if (null != var.getValue()) {
            ValueVisitor valueVisitor = new ValueVisitor();
            var.getValue().accept(valueVisitor);
            aasValue = valueVisitor.getAasValue();
        }
        return aasValue;
    }

}
