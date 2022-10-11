package de.iip_ecosphere.platform.configuration.ivml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import de.iip_ecosphere.platform.configuration.ivml.IvmlGraphMapper.IvmlGraph;
import de.iip_ecosphere.platform.support.FileUtils;
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
import net.ssehub.easy.varModel.model.values.Value;
import net.ssehub.easy.varModel.model.values.ValueDoesNotMatchTypeException;
import net.ssehub.easy.varModel.persistency.IVMLWriter;

/**
 * Maps an IVML configuration generically into an AAS without referencing to IIP-Ecosphere.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractIvmlModifier implements DecisionVariableProvider {

    private IvmlGraphMapper graphMapper;
    private Map<String, GraphFormat> graphFormats = new HashMap<>();
    private ConfigurationChangeListener changeListener;

    /**
     * Creates a mapper with default settings.
     * 
     * @param graphMapper maps a graph from IVML to an internal structure
     * @param changeListener optional configuration change listener, may be <b>null</b>
     * @throws IllegalArgumentException if {@code cfgSupplier} is <b>null</b>
     */
    public AbstractIvmlModifier(IvmlGraphMapper graphMapper, ConfigurationChangeListener changeListener) {
        if (null == graphMapper) {
            throw new IllegalArgumentException("graphMapper must not be null");
        }
        this.graphMapper = graphMapper;
        this.changeListener = changeListener;
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
     * Saving model project {@code prj} to {@code file}.
     * 
     * @param prj the project
     * @param file the file to write to
     * @throws ExecutionException if writing fails
     */
    protected static void saveTo(Project prj, File file) throws ExecutionException {
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
     * Returns the IVML subpath for the given project.
     * 
     * @param project the project
     * @return the subpath, may be <b>null</b> for a top-level or a non-writable project, may be empty for the 
     *    top-level folder or a sub-folder
     */
    protected abstract String getIvmlSubpath(Project project);
    
    /**
     * Creates an IVML configuration (not meta-model) model path with {@code subpath} and for project {@code p}.
     *  
     * @param subpath the subpath, may be <b>null</b> for none
     * @param project the project to create the path for
     * @return the file name/path
     * @see #getIvmlSubpath(Project)
     */
    protected abstract File createIvmlConfigPath(String subpath, Project project);
    
    /**
     * Returns the filename/path for {@code project}.
     * 
     * @param project the project
     * @return the filename/path
     * @see #getIvmlSubpath(Project)
     * @see #createIvmlConfigPath(String, Project)
     */
    protected File getIvmlFile(Project project) {
        return createIvmlConfigPath(getIvmlSubpath(project), project);
    }
    
    /**
     * Deletes an IVML variable. In case of a graph, this may subsequently delete further 
     * variables. IVML reference to a variable shall be cleaned up before. Left-over references shall
     * lead to a syntax error and to no modification of the model. [public for testing]
     * 
     * @param varName the qualified IVML variable name to delete
     * @throws ExecutionException if creating the variable fails
     */
    public void deleteVariable(String varName) throws ExecutionException {
        net.ssehub.easy.varModel.confModel.Configuration cfg = getIvmlConfiguration();
        Project root = cfg.getProject();
        try {
            AbstractVariable var = ModelQuery.findVariable(root, varName, null);
            if (null != var) {
                Project prj = var.getProject();
                String subpath = getIvmlSubpath(prj);
                if (subpath != null || prj == root) {
                    removeConstraintsForVariable(prj, var);
                    prj.removeElement(var);
                    IDecisionVariable dVar = cfg.getDecision(var);
                    cfg.removeDecision(dVar);
                    notifyChange(dVar, ConfigurationChangeType.DELETED);
                    ReasoningResult res = validateAndPropagate();
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
    }

    /**
     * Throws an {@link ExecutionException} if the reasoning result {@code res} indicates a problem.
     * 
     * @param res the reasoning result
     * @param reloadIfFail reload the model if there is a failure
     * @throws ExecutionException the exception if reasoning failed
     */
    protected void throwIfFails(ReasoningResult res, boolean reloadIfFail) throws ExecutionException {
        if (res.hasConflict()) {
            if (reloadIfFail) {
                reloadConfiguration();
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
     * Removes assignment constraints for a given {@code var}.
     *  
     * @param prj the project to start searching for constraints within
     * @param var the variable to remove constraints for
     */
    protected void removeConstraintsForVariable(Project prj, AbstractVariable var) {
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
     * Creates an IVML variable. [public for testing]
     * 
     * @param varName the IVML variable name
     * @param type the (qualified) IVML type
     * @param valueEx the value as IVML expression 
     * @throws ExecutionException if creating the variable fails
     */
    public void createVariable(String varName, String type, String valueEx) throws ExecutionException {
        net.ssehub.easy.varModel.confModel.Configuration cfg = getIvmlConfiguration();
        Project root = cfg.getProject();
        try {
            IDatatype t = ModelQuery.findType(root, type, null);
            if (null != t) {
                DecisionVariableDeclaration var = new DecisionVariableDeclaration(toIdentifier(varName), t, root);
                setValue(var, valueEx);
                root.add(var);
                //alternative: in own constraint, remove setValue above; may be needed if t is container
                //createAssignment(var, valueEx, root); 
                IDecisionVariable dVar = cfg.createDecision(var);
                notifyChange(dVar, ConfigurationChangeType.CREATED);
            } else {
                throw new ExecutionException("No such type " + t, null);
            }
            ReasoningResult res = validateAndPropagate();
            throwIfFails(res, true);
            saveTo(root, getIvmlFile(root));
        } catch (ModelQueryException | ConfigurationException e) {
            throw new ExecutionException(e);
        }
    }

    /**
     * Changes a given set of values and performs reasoning before committing the values into the 
     * actual configuration. For compounds/containers it is advisable to assign complete values to avoid
     * illegal re-assignments. [public for testing]
     * 
     * @param values the values, given as qualified IVML variables names mapped to serialized values
     * @throws ExecutionException if changing values fails
     */
    public synchronized void changeValues(Map<String, String> values) throws ExecutionException {
        Configuration cfg = getVilConfiguration();
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
                notifyChange(var, ConfigurationChangeType.MODIFIED);
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
    }

    /**
     * Changes the value of the decision variable {@code var} by parsing {@code expression} and evaluating 
     * it through {@code eval}.
     * 
     * @param var the variable to change, may be a top-level variable and {@code expression} may be a compound 
     *   value expression
     * @param expression the IVML expression
     * @param eval the expression evaluator to reuse, may be <b>null</b> to create a temporary one within
     * @param state the assignment state to apply
     * @throws ExecutionException if parsing, evaluating or assigning fails
     */
    protected void setValue(IDecisionVariable var, String expression, EvaluationVisitor eval, AssignmentState state) 
        throws ExecutionException {
        try {
            ConstraintSyntaxTree cst = createExpression(expression, var.getConfiguration().getProject());
            if (null == eval) {
                eval = new EvaluationVisitor();
            }
            eval.init(var.getConfiguration(), state, false, null);
            eval.visit(cst);
            Value val = eval.getResult();
            eval.clear();
            var.setValue(val, state);
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
    protected ConstraintSyntaxTree createExpression(String expression, Project scope) throws ExecutionException {
        try {
            if (null == scope) {
                scope = getIvmlConfiguration().getProject();
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
    protected Constraint createAssignment(AbstractVariable varDecl, String valueEx, Project prj) 
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
    protected void setValue(AbstractVariable var, String expression) throws ExecutionException {
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
     * Stores original and copied file.
     * 
     * @author Holger Eichelberger, SSE
     */
    protected static class CopiedFile {
        
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
    protected static CopiedFile copyToTmp(File file) throws ExecutionException {
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
     * Reloads the and validates the model, in case of problems, restore changed files from {@code copies}.
     * 
     * @param copies copied files to be restored
     * @throws ExecutionException if reasoning/restoring fails
     */
    protected void reloadAndValidate(Map<Project, CopiedFile> copies) throws ExecutionException {
        reloadConfiguration();
        ReasoningResult res = validateAndPropagate();
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
            reloadConfiguration();
            throwIfFails(res, false);
        } else {
            for (CopiedFile c : copies.values()) {
                c.clean();
            }
        }
        // TODO update AAS
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
     * Returns a graph format instance.
     * 
     * @param format the unique name of the graph format
     * @return the graph format instance
     * @throws ExecutionException if the format instance cannot be found
     */
    protected GraphFormat getGraphFormat(String format) throws ExecutionException {
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
     * Returns the graph mapper.
     * 
     * @return the graph mapper
     */
    protected IvmlGraphMapper getMapper() {
        return graphMapper;
    }

    @Override
    public IDecisionVariable getVariable(String qualifiedVarName) throws ExecutionException {
        return getVariable(getVilConfiguration(), qualifiedVarName);
    }

    /**
     * Returns an IVML variable.
     * 
     * @param cfg the configuration to take the variable from
     * @param qualifiedVarName the (qualified) variable name
     * @return the variable
     * @throws ExecutionException if querying the variable fails
     */
    protected IDecisionVariable getVariable(Configuration cfg, String qualifiedVarName) throws ExecutionException {
        try {
            return cfg.getConfiguration().getDecision(qualifiedVarName, false);
        } catch (ModelQueryException e) {
            throw new ExecutionException(e.getMessage(), null);
        }
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
    protected static void addImport(Project target, String imp, Project root, Project res) 
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
     * Helper to turn the first char of {@code str} into upper case and {@code str} into an identifier..
     * 
     * @param str the string
     * @return the identifier
     */
    protected static String toIdentifierFirstUpper(String str) {
        return PseudoString.firstToUpperCase(toIdentifier(str));
    }

    /**
     * Helper to turn {@code str} into a Java identifier.
     * 
     * @param str the text
     * @return the identifier
     */
    protected static String toIdentifier(String str) {
        return PseudoString.toIdentifier(str);
    }

    /**
     * Returns the actual VIL configuration. Shall be consistent with {@link #getIvmlConfiguration()}.
     * 
     * @return the configuration
     */
    protected abstract Configuration getVilConfiguration();

    /**
     * Returns the actual IVML configuration. Shall be consistent with {@link #getVilConfiguration()}.
     * 
     * @return the configuration
     */
    protected abstract net.ssehub.easy.varModel.confModel.Configuration getIvmlConfiguration();
    
    /**
     * Validates the model and propagates values within the model.
     * 
     * @return the reasoning result
     */
    protected abstract ReasoningResult validateAndPropagate();

    /**
     * Reloads the configuration model.
     */
    protected abstract void reloadConfiguration();

    /**
     * Configuration change types.
     * 
     * @author Holger Eichelberger, SSE
     */
    public enum ConfigurationChangeType {
        CREATED,
        MODIFIED,
        DELETED
    }

    /**
     * Some part listening on configuration changes.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface ConfigurationChangeListener {
        
        /**
         * Called when a decision variable changed.
         * 
         * @param var the variable (may not be part of any configuration anymore)
         * @param type the change type
         */
        public void configurationChanged(IDecisionVariable var, ConfigurationChangeType type);
        
    }
    
    /**
     * Notifies a potential change listener about a configuration change.
     * 
     * @param var the variable (may not be part of any configuration anymore)
     * @param type the change type
     */
    protected void notifyChange(IDecisionVariable var, ConfigurationChangeType type) {
        if (null != changeListener && null != var) {
            changeListener.configurationChanged(var, type);
        }
    }
    
    /**
     * Notifies a potential change listener about changing a whole project the same way.
     * 
     * @param prj the project
     * @param type the change type
     */
    protected void notifyChange(Project prj, ConfigurationChangeType type) {
        net.ssehub.easy.varModel.confModel.Configuration cfg = getIvmlConfiguration();
        for (int e = 0; e < prj.getElementCount(); e++) {
            ContainableModelElement elt = prj.getElement(e);
            if (elt instanceof AbstractVariable) {
                notifyChange(cfg.getDecision((AbstractVariable) elt), type);
            }
        }
    }

    /**
     * Returns the type of {@code var} as string.
     * 
     * @param var the variable
     * @return the type
     */
    public static String getType(IDecisionVariable var) {
        IDatatype type = var.getDeclaration().getType();
        return IvmlDatatypeVisitor.getUnqualifiedType(type);
    }

}
