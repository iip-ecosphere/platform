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
import de.iip_ecosphere.platform.configuration.EasySetup;
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
import de.iip_ecosphere.platform.support.iip_aas.json.JsonResultWrapper;
import de.uni_hildesheim.sse.ConstraintSyntaxException;
import de.uni_hildesheim.sse.ModelUtility;
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
import net.ssehub.easy.varModel.cst.ConstraintSyntaxTree;
import net.ssehub.easy.varModel.cst.OCLFeatureCall;
import net.ssehub.easy.varModel.cst.Variable;
import net.ssehub.easy.varModel.cstEvaluation.EvaluationVisitor;
import net.ssehub.easy.varModel.model.AbstractVariable;
import net.ssehub.easy.varModel.model.Constraint;
import net.ssehub.easy.varModel.model.ContainableModelElement;
import net.ssehub.easy.varModel.model.DecisionVariableDeclaration;
import net.ssehub.easy.varModel.model.IvmlDatatypeVisitor;
import net.ssehub.easy.varModel.model.ModelQuery;
import net.ssehub.easy.varModel.model.ModelQueryException;
import net.ssehub.easy.varModel.model.Project;
import net.ssehub.easy.varModel.model.ProjectImport;
import net.ssehub.easy.varModel.model.datatypes.IDatatype;
import net.ssehub.easy.varModel.model.datatypes.OclKeyWords;
import net.ssehub.easy.varModel.model.datatypes.TypeQueries;
import net.ssehub.easy.varModel.model.values.ContainerValue;
import net.ssehub.easy.varModel.model.values.ReferenceValue;
import net.ssehub.easy.varModel.model.values.Value;
import net.ssehub.easy.varModel.model.values.ValueDoesNotMatchTypeException;
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
                setGraph(AasUtils.readString(a, 0), AasUtils.readString(a, 1), AasUtils.readString(a, 2), 
                    AasUtils.readString(a, 3), AasUtils.readString(a, 4))
            ));
        sBuilder.defineOperation(OP_CREATE_VARIABLE, 
            new JsonResultWrapper(a -> createVariable(AasUtils.readString(a, 0), AasUtils.readString(a, 1), 
                AasUtils.readString(a, 2))));
        sBuilder.defineOperation(OP_DELETE_VARIABLE, 
            new JsonResultWrapper(a -> deleteVariable(AasUtils.readString(a))));
    }
    
    /**
     * Changes the value of the decision variable {@code var} by parsing {@code expression} and evaluating 
     * it through {@code eval}.
     * 
     * @param var the variable to change, may be a top-level variable and {@code expression} may be a compound 
     *   value expression
     * @param expression the IVML expression
     * @param eval the expression evaluator to reuse, may be <b>null</b> to create a temporary one within
     * @throws ExecutionException if parsing, evaluating or assigning fails
     */
    @SuppressWarnings("unused")
    private void setValue(IDecisionVariable var, String expression, EvaluationVisitor eval) throws ExecutionException {
        try {
            ConstraintSyntaxTree cst = createExpression(expression, var.getConfiguration().getProject());
            if (null == eval) {
                eval = new EvaluationVisitor();
            }
            eval.init(var.getConfiguration(), AssignmentState.USER_ASSIGNED, false, null);
            eval.visit(cst);
            Value val = eval.getResult();
            eval.clear();
            var.setValue(val, AssignmentState.USER_ASSIGNED);
        } catch (ConfigurationException e) {
            throw new ExecutionException(e.getMessage(), null);
        }
    }

    /**
     * Creates an IVML expression syntax tree for {@code expression}.
     * 
     * @param expression the expression
     * @param scope the resolution scope, may be <b>null</b> for the root project
     * @return the syntax tree
     * @throws ExecutionException if the expression cannot be created, e.g., due to syntactic or semantic errors
     */
    private ConstraintSyntaxTree createExpression(String expression, Project scope) throws ExecutionException {
        try {
            if (null == scope) {
                Configuration cfg = cfgSupplier.get();
                scope = cfg.getConfiguration().getProject();
            }
            return ModelUtility.INSTANCE.createExpression(expression, scope);
        } catch (ConstraintSyntaxException | CSTSemanticException e) {
            throw new ExecutionException(e.getMessage(), null);
        }
    }

    /**
     * Creates an assignment of {@code valueEx} to {@code varDecl} and adds it to {@code prj}.
     * 
     * @param varDecl the variable declaration
     * @param valueEx the IVML value expression
     * @param prj the project to add the constraint to
     * @return the created constraint
     * @throws ExecutionException if creating the constraint fails
     */
    private Constraint createAssignment(AbstractVariable varDecl, String valueEx, Project prj) 
        throws ExecutionException {
        try {
            Constraint c = new Constraint(createExpression(varDecl.getName() + "=" + valueEx, prj), prj);
            prj.add(c);
            return c;
        } catch (CSTSemanticException e) {
            throw new ExecutionException(e.getMessage(), null);
        }
    }

    /**
     * Changes the value of the variable declaration {@code var} by parsing {@code expression}.
     * 
     * @param var the variable to change, may be a top-level variable and {@code expression} may be a compound 
     *   value expression
     * @param expression the IVML expression
     * @throws ExecutionException if parsing, evaluating or assigning fails
     */
    private void setValue(AbstractVariable var, String expression) throws ExecutionException {
        try {
            if (TypeQueries.isCompound(var.getType()) && expression.trim().startsWith("{")) {
                expression = IvmlDatatypeVisitor.getUnqualifiedType(var.getType()) + expression;
            } // container type may require special treatment
            ConstraintSyntaxTree cst = createExpression(expression, var.getProject());
            cst.inferDatatype();
            var.setValue(cst);
        } catch (ValueDoesNotMatchTypeException | CSTSemanticException e) {
            throw new ExecutionException(e.getMessage(), null);
        }
    }

    /**
     * Changes a given set of values and performs reasoning before committing the values into the 
     * actual configuration. For compounds/containers it is advisable to assign complete values to avoid
     * illegal re-assignments. [public for testing]
     * 
     * @param values the values, given as qualified IVML variables names mapped to serialized values
     * @return <b>null</b>
     * @throws ExecutionException if changing values fails
     */
    public synchronized Object changeValues(Map<String, String> values) throws ExecutionException {
        Configuration cfg = cfgSupplier.get();
        Project root = cfg.getConfiguration().getProject();
        Set<Project> projects = new HashSet<>();
        Map<String, IDecisionVariable> vars = new HashMap<>();
        for (String varName: values.keySet()) {
            vars.put(varName, getVariable(cfg, varName));
        }
        ChangeHistory history = cfg.getChangeHistory();
        history.start();
        for (Map.Entry<String, String> ent: values.entrySet()) {
            IDecisionVariable var = vars.get(ent.getKey());
            try {
                AbstractVariable varDecl = var.getDeclaration();
                Project target = varDecl.getProject();
                String subpath = getIvmlSubpath(target);
                if (null == subpath) { // if it is one of the "writable" wildcard imports
                    target = root;
                }
                removeConstraintsForVariable(target, varDecl);
                createAssignment(varDecl, ent.getValue(), target); 
                projects.add(target);
            } catch (ExecutionException e) {
                history.rollback();
                throw e;
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
                if (msg.getConflictsCount() > 0) {
                    for (int i = 0; i < msg.getConflictsCount(); i++) {
                        text += msg.getConflictLabels().get(i) + ". ";
                    }
                }
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
     * Returns the IVML subpath for the given project.
     * 
     * @param project the project
     * @return the subpath, may be <b>null</b> for a top-level project
     */
    private String getIvmlSubpath(Project project) {
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
     * Returns the filename/path for {@code p}.
     * 
     * @param project the project
     * @return the filename/path
     */
    private File getIvmlFile(Project project) {
        return createIvmlConfigPath(getIvmlSubpath(project), project);
    }
    
    /**
     * Creates an IVML configuration (not meta-model) model path with {@code subpath} and for project {@code p}.
     *  
     * @param subpath the subpath, may be <b>null</b> for none
     * @param project the project to create the path for
     * @return the file name/path
     */
    private File createIvmlConfigPath(String subpath, Project project) {
        EasySetup ep = ConfigurationSetup.getSetup().getEasyProducer();
        File result = ep.getIvmlConfigFolder();
        if (null == result || result.toString().equals(".")) {
            result = ep.getBase();
        }
        if (subpath != null) {
            result = new File(result, subpath);
        }
        return new File(result, project.getName() + ".ivml");
    }
    
    /**
     * Returns a graph structure in IVML. [public for testing]
     * 
     * @param varName the IVML variable holding the graph
     * @param format the format of the graph to return 
     * @return the graph in the specified {@code format}
     * @throws ExecutionException if reading the graph structure fails
     */
    public String getGraph(String varName, String format) throws ExecutionException {
        GraphFormat gFormat = getGraphFormat(format);
        IDecisionVariable var = getVariable(varName);
        IvmlGraph graph = graphMapper.getGraphFor(var);
        return gFormat.toString(graph);
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
        IvmlGraph graph = gFormat.fromString(value, graphMapper.getGraphFactory(), this);
        
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
        
        /**
         * Cleans up unneeded copies.
         */
        private void clean() {
            if (null != copy) {
                copy.delete();
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
        file.getParentFile().mkdirs();
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
            ConfigurationManager.reload();
            throwIfFails(res, false);
        } else {
            for (CopiedFile c : copies.values()) {
                c.clean();
            }
        }
        // TODO update AAS
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
     * Adds an import statement, if needed, temporarily resolved to be able to create expressions and constraints.
     * 
     * @param target the target project where to add the import to
     * @param imp the imported name, may be a wildcard
     * @param root the root project where to resolve projects from
     * @param res already resolved project, takes precedence over resolving {@code imp} from {@code root}; use to 
     *     temporarily resolve wildcard imports with one concrete project
     * @throws ModelManagementException if resolving/setting the resolved project fails
     */
    private static void addImport(Project target, String imp, Project root, Project res) 
        throws ModelManagementException {
        ProjectImport i = new ProjectImport(imp);
        if (null == res) {
            i.setResolved(ModelQuery.findProject(root, imp));
        } else {
            i.setResolved(res);
        }
        target.addImport(i);
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
        net.ssehub.easy.varModel.confModel.Configuration cfg = ConfigurationManager.getIvmlConfiguration();
        Project root = cfg.getProject();
        IDatatype applicationType = ModelQuery.findType(root, "Application", null);
        
        String appProjectName = "ApplicationPart" + toIdentifierFirstUpper(appName);
        results.appProject = ModelQuery.findProject(root, appProjectName);
        if (null == results.appProject) {
            results.appProject = new Project(appProjectName);
        }
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
     * @param appName the application name
     * @param meshName the mesh name
     * @param graph
     * @param results
     * @throws ModelQueryException if IVML types/variables cannot be found
     * @throws ModelManagementException if IVML types/variables cannot be found
     * @throws ExecutionException if setting IVML values fails
     */
    private void createMeshProject(String appName, String meshName, IvmlGraph graph, ModelResults results) 
         throws ModelQueryException, ModelManagementException, ExecutionException {
        net.ssehub.easy.varModel.confModel.Configuration cfg = ConfigurationManager.getIvmlConfiguration();
        Project root = cfg.getProject();
        results.meshProject = new Project("ServiceMeshPart" + toIdentifierFirstUpper(appName) 
            + toIdentifierFirstUpper(meshName));
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
            DecisionVariableDeclaration nodeVar = new DecisionVariableDeclaration(n.getName(), type, 
                results.meshProject);
            String nodeValEx = "{pos_x=" + n.getXPos() 
                + ",pos_y=" + n.getYPos() 
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
                String edgeVarName = toIdentifier(edgeName);
                if (null == edgeName || edgeName.length() == 0) {
                    edgeName = n.getName() + " -> " + e.getEnd().getName();
                    edgeVarName = "edge_" + edgeCounter++;
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
     * Creates an IVML variable. [public for testing]
     * 
     * @param varName the IVML variable name
     * @param type the (qualified) IVML type
     * @param valueEx the value as IVML expression 
     * @return <b>null</b> always
     * @throws ExecutionException if creating the variable fails
     */
    public Object createVariable(String varName, String type, String valueEx) throws ExecutionException {
        net.ssehub.easy.varModel.confModel.Configuration cfg = ConfigurationManager.getIvmlConfiguration();
        Project root = cfg.getProject();
        try {
            IDatatype t = ModelQuery.findType(root, type, null);
            if (null != t) {
                DecisionVariableDeclaration var = new DecisionVariableDeclaration(toIdentifier(varName), t, root);
                setValue(var, valueEx);
                root.add(var);
                //alternative: in own constraint, remove setValue above; may be needed if t is container
                //createAssignment(var, valueEx, root); 
                cfg.createDecision(var);
            } else {
                throw new ExecutionException("No such type " + t, null);
            }
            ReasoningResult res = ConfigurationManager.validateAndPropagate();
            throwIfFails(res, true);
            saveTo(root, getIvmlFile(root));
        } catch (ModelQueryException | ConfigurationException e) {
            throw new ExecutionException(e);
        }
        return null;
    }

    /**
     * Deletes an IVML variable. In case of a graph, this may subsequently delete further 
     * variables. IVML reference to a variable shall be cleaned up before. Left-over references shall
     * lead to a syntax error and to no modification of the model. [public for testing]
     * 
     * @param varName the qualified IVML variable name to delete
     * @return <b>null</b> always
     * @throws ExecutionException if creating the variable fails
     */
    public Object deleteVariable(String varName) throws ExecutionException {
        net.ssehub.easy.varModel.confModel.Configuration cfg = ConfigurationManager.getIvmlConfiguration();
        Project root = cfg.getProject();
        try {
            AbstractVariable var = ModelQuery.findVariable(root, varName, null);
            if (null != var) {
                Project prj = var.getProject();
                String subpath = getIvmlSubpath(prj);
                if (subpath != null || prj == root) {
                    removeConstraintsForVariable(prj, var);
                    prj.removeElement(var);
                    cfg.removeDecision(cfg.getDecision(var));
                    ReasoningResult res = ConfigurationManager.validateAndPropagate();
                    throwIfFails(res, true);
                    saveTo(prj, getIvmlFile(prj));
                } else {
                    throw new ExecutionException("Project " + prj.getName() + " is not allowed for modification", null);
                }
            } else {
                throw new ExecutionException("Cannot find variable " + varName, null);
            }
        } catch (ModelQueryException e) {
            throw new ExecutionException(e);
        }
        return null;
    }
    
    /**
     * Removes assignment constraints for a given {@code var}.
     *  
     * @param prj the project to start searching for constraints within
     * @param var the variable to remove constraints for
     */
    private void removeConstraintsForVariable(Project prj, AbstractVariable var) {
        // EASy ConstraintSeparator does not detect all forms of assignment constraints, e.g., compound init as arg
        for (int e = 0; e < prj.getElementCount(); e++) {
            ContainableModelElement elt = prj.getElement(e);
            if (elt instanceof Constraint) {
                Constraint c = (Constraint) elt;
                ConstraintSyntaxTree cst = c.getConsSyntax();
                if (cst instanceof OCLFeatureCall) {
                    OCLFeatureCall call = (OCLFeatureCall) cst;
                    if (OclKeyWords.ASSIGNMENT.equals(call.getOperation())) {
                        if (call.getOperand() instanceof Variable 
                            && (((Variable) call.getOperand()).getVariable() == var)) {
                            c.getProject().removeElement(c);
                        }
                    }
                }
            }
        }
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
