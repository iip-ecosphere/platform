package de.iip_ecosphere.platform.configuration.easyProducer.ivml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import de.iip_ecosphere.platform.configuration.cfg.ConfigurationChangeType;
import de.iip_ecosphere.platform.configuration.easyProducer.ivml.IvmlGraphMapper.IvmlGraph;
import de.iip_ecosphere.platform.configuration.easyProducer.ivml.IvmlGraphMapper.IvmlGraphNode;
import de.iip_ecosphere.platform.configuration.easyProducer.serviceMesh.ServiceMeshGraphMapper;
import de.iip_ecosphere.platform.support.FileUtils;
import de.iip_ecosphere.platform.support.json.JsonUtils;
import de.iip_ecosphere.platform.support.logging.Logger;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;
import de.uni_hildesheim.sse.ConstraintSyntaxException;
import de.uni_hildesheim.sse.ModelUtility;
import net.ssehub.easy.basics.modelManagement.ModelManagementException;
import net.ssehub.easy.instantiation.core.model.vilTypes.PseudoString;
import net.ssehub.easy.instantiation.core.model.vilTypes.configuration.ChangeHistory;
import net.ssehub.easy.instantiation.core.model.vilTypes.configuration.Configuration;
import net.ssehub.easy.producer.core.mgmt.EasyExecutor;
import net.ssehub.easy.reasoning.core.frontend.ReasonerFrontend;
import net.ssehub.easy.reasoning.core.reasoner.Message;
import net.ssehub.easy.reasoning.core.reasoner.ReasoningResult;
import net.ssehub.easy.reasoning.sseReasoner.model.SubstitutionVisitor;
import net.ssehub.easy.varModel.confModel.AssignmentState;
import net.ssehub.easy.varModel.confModel.ConfigurationException;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.cst.CSTSemanticException;
import net.ssehub.easy.varModel.cst.ConstantValue;
import net.ssehub.easy.varModel.cst.ConstraintSyntaxTree;
import net.ssehub.easy.varModel.cst.OCLFeatureCall;
import net.ssehub.easy.varModel.cst.Variable;
import net.ssehub.easy.varModel.cstEvaluation.EvaluationVisitor;
import net.ssehub.easy.varModel.model.AbstractVariable;
import net.ssehub.easy.varModel.model.ConstantDecisionVariableDeclaration;
import net.ssehub.easy.varModel.model.Constraint;
import net.ssehub.easy.varModel.model.ContainableModelElement;
import net.ssehub.easy.varModel.model.DecisionVariableDeclaration;
import net.ssehub.easy.varModel.model.IvmlDatatypeVisitor;
import net.ssehub.easy.varModel.model.IvmlModelQuery;
import net.ssehub.easy.varModel.model.ModelQuery;
import net.ssehub.easy.varModel.model.ModelQueryException;
import net.ssehub.easy.varModel.model.Project;
import net.ssehub.easy.varModel.model.ProjectImport;
import net.ssehub.easy.varModel.model.datatypes.Compound;
import net.ssehub.easy.varModel.model.datatypes.ConstraintType;
import net.ssehub.easy.varModel.model.datatypes.IDatatype;
import net.ssehub.easy.varModel.model.datatypes.OclKeyWords;
import net.ssehub.easy.varModel.model.datatypes.Reference;
import net.ssehub.easy.varModel.model.datatypes.Sequence;
import net.ssehub.easy.varModel.model.values.BooleanValue;
import net.ssehub.easy.varModel.model.values.CompoundValue;
import net.ssehub.easy.varModel.model.values.ConstraintValue;
import net.ssehub.easy.varModel.model.values.ContainerValue;
import net.ssehub.easy.varModel.model.values.EnumValue;
import net.ssehub.easy.varModel.model.values.IValueVisitor;
import net.ssehub.easy.varModel.model.values.IntValue;
import net.ssehub.easy.varModel.model.values.MetaTypeValue;
import net.ssehub.easy.varModel.model.values.NullValue;
import net.ssehub.easy.varModel.model.values.RealValue;
import net.ssehub.easy.varModel.model.values.ReferenceValue;
import net.ssehub.easy.varModel.model.values.StringValue;
import net.ssehub.easy.varModel.model.values.Value;
import net.ssehub.easy.varModel.model.values.ValueDoesNotMatchTypeException;
import net.ssehub.easy.varModel.model.values.ValueFactory;
import net.ssehub.easy.varModel.model.values.VersionValue;
import net.ssehub.easy.varModel.persistency.IVMLWriter;
import net.ssehub.easy.varModel.persistency.IVMLWriter.EmitFilter;

/**
 * Maps an IVML configuration generically into an AAS.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractIvmlModifier implements DecisionVariableProvider {

    public static final Predicate<Project> ANY_PROJECT_FILTER = p -> true;
    public static final Predicate<Project> NO_TEMPLATE_FILTER = p -> !IvmlUtils.isTemplate(p);

    private static final EmitFilter EMIT_FILTER = (var, val) -> {
        boolean emit = true;
        emit &= !ConstraintType.isConstraint(var.getType()); // we usually do not configure constraints in the UI
        emit &= val != NullValue.INSTANCE; // we usually do not set null values in the UI
        if (var.getDefaultValue() instanceof ConstantValue) { // poor man's default value checked, no access to state
            emit &= !Value.equals(val, ((ConstantValue) var.getDefaultValue()).getConstantValue());
        }
        return emit;
    };

    private IvmlGraphMapper graphMapper;
    private Map<String, GraphFormat> graphFormats = new HashMap<>();
    private ConfigurationChangeListener changeListener;
    private Set<String> reservedVariableNames = new HashSet<String>();

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
        setChangeListener(changeListener);
    }

    /**
     * Sets the change listener. [testing]
     * 
     * @param changeListener the new change listener
     */
    protected void setChangeListener(ConfigurationChangeListener changeListener) {
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
        getLogger().info("Writing IVML project {} to file {}", prj.getName(), file);
        file.getParentFile().mkdirs();
        try (FileWriter fWriter = new FileWriter(file)) {
            write(prj, fWriter);
            fWriter.close();
        } catch (IOException e) {
            throw new ExecutionException(e);
        }
    }

    /**
     * Writes {@code prj} to {@code out}.
     * 
     * @param prj the project to write
     * @param out the writer to write the project to
     */
    protected static void write(Project prj, Writer out) {
        IVMLWriter writer = new IVMLWriter(out);
        writer.setFormatInitializer(true);
        writer.setEmitProjectFreezeDot(true);
        writer.setEmitFilter(EMIT_FILTER);
        prj.accept(writer);
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
     * Returns whether the given project is allowed for modification (other than root).
     * 
     * @param prj the project
     * @return {@code true} allowed, {@code false} else
     * @see #getVariableTarget(Project, IDatatype, String, List)
     */
    protected boolean isAllowedForModification(Project prj) {
        return false; 
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
        getLogger().info("Deleting IVML variable {}", varName);
        net.ssehub.easy.varModel.confModel.Configuration cfg = getIvmlConfiguration();
        Project root = cfg.getProject();
        try {
            AbstractVariable var = ModelQuery.findVariable(root, varName, null);
            if (null != var) {
                Project prj = var.getProject();
                String subpath = getIvmlSubpath(prj);
                if (subpath != null || prj == root || isAllowedForModification(prj)) {
                    removeConstraintsForVariable(prj, var);
                    prj.removeElement(var);
                    IDecisionVariable dVar = cfg.getDecision(var);
                    cfg.removeDecision(dVar);
                    ReasoningResult res = validateAndPropagate(NO_TEMPLATE_FILTER);
                    throwIfFails(res, true);
                    saveTo(prj, getIvmlFile(prj));
                    notifyChange(dVar, ConfigurationChangeType.DELETED);
                    getLogger().info("Deleted IVML variable {}", varName);
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
     * Returns the IVML variable names representing application templates.
     * 
     * @return the IVML variable names as JSON array
     * @throws ExecutionException if the execution of the operation fails
     */
    public String getTemplates() throws ExecutionException {
        net.ssehub.easy.varModel.confModel.Configuration cfg = getIvmlConfiguration();
        Project root = cfg.getProject();
        List<String> result = IvmlUtils.findTemplates(root)
            .stream()
            .map(var -> var.getName())
            .collect(Collectors.toList());
        return JsonUtils.toJson(result);
    }
    
    /**
     * Represents runtime (collected) information steering the template instantiation.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class InstantiationContext {
        
        private Map<String, String> adjustments;
        private Set<Project> modified = new HashSet<>();
        private Map<AbstractVariable, AbstractVariable> substitutions = new HashMap<>();
        private List<IDecisionVariable> meshes = new ArrayList<>();
        private String currentTemplateVariable;
        private String appPrefix = "";
        private Project targetProject;
        private boolean enableNameAdjustment = false;
        
        /**
         * Creates an instantiation context with given adjustments.
         * 
         * @param adjustments the adjustments
         */
        private InstantiationContext(Map<String, String> adjustments) {
            this.adjustments = adjustments;
        }
        
        /**
         * Returns the names of the meshes created in this context.
         * 
         * @return the names of the meshes
         */
        private List<String> getMeshNames() {
            return meshes
                .stream()
                .map(m -> m.getDeclaration().getParent().getName())
                .collect(Collectors.toList());
        }
        
        /**
         * Returns the name of the current template variable being processed.
         * 
         * @return the name of the variable or empty if unset
         */
        private String getCurrentTemplateVariableName() {
            return null == currentTemplateVariable ? "" : currentTemplateVariable;
        }
        
        /**
         * Prefix an IVML variable name with the {@link #appPrefix}.
         * 
         * @param name the name to prefix
         * @return the prefixed name if {@link #appPrefix} is set
         */
        private String prefixVarName(String name) {
            return appPrefix == null || appPrefix.length() == 0 ? name : appPrefix + toIdentifierFirstUpper(name);
        }
        
    }
    
    /**
     * Gets a value from {@code hash}, if not adds a value for {@code key} as created by {@code creator}.
     * 
     * @param <K> the key type
     * @param <V> the value type
     * @param hash the has to query/modify
     * @param key the key value
     * @param creator the creator for new values
     * @return the retrieved or created value
     */
    private static <K, V> V getOrCreate(HashMap<K, V> hash, K key, Supplier<V> creator) {
        V result = hash.get(key);
        if (null == result) {
            result = creator.get();
            hash.put(key, result);
        }
        return result;
    }
    
    /**
     * Instantiates the given meshes.
     * 
     * @param meshes the meshes
     * @param mode the instantiation mode
     * @param context the instantiation context
     * @throws ExecutionException if setting IVML values fails
     * @throws ConfigurationException if setting IVML values fails
     * @throws ModelQueryException if obtaining IVML types fails
     * @throws ModelManagementException if model management operations fail
     */
    private void instantiateMeshes(Set<IDecisionVariable> meshes, Mode mode, InstantiationContext context) 
        throws ExecutionException, ConfigurationException, ModelQueryException, ModelManagementException {
        for (IDecisionVariable e : meshes) {
            AbstractVariable decl = e.getDeclaration();
            context.currentTemplateVariable = decl.getName(); 
            instantiateTemplateVariable(e, decl.getType().getName(), null, mode, context);
//TODO substitutions                        
        }        
    }

    /**
     * Instantiates the template by the IVML variable {@code varName} to an application with given {@code appName}.
     * 
     * @param varName the name of the IVML variable representing the template
     * @param appName the (display) name of the application, may be used to derive id/variable name
     * @param adjustments variable settings, as variable=value or variable.field=value, may be <b>null</b>; shall be 
     *     filled with variable values from {@link #getOpenTemplateVariables(String)}, otherwise the instantiation 
     *     will fail
     * @return the IVML variable name of the instantiated application
     * @throws ExecutionException if the execution of the operation fails
     */
    public String instantiateTemplate(String varName, String appName, Map<String, String> adjustments) 
        throws ExecutionException {
        String result = "";
        getLogger().info("Instantiating template {} to {} with value adjustments {}", varName, appName, adjustments);
        if (!isValidIdentifier(appName)) {
            throw new ExecutionException("'" + appName + "' is not a valid identifier", null);
        }
        net.ssehub.easy.varModel.confModel.Configuration cfg = getIvmlConfiguration();
        Project root = cfg.getProject();
        Optional<AbstractVariable> templateVarOpt = IvmlUtils.findTemplates(root)
            .stream().filter(var -> var.getName().equals(varName)).findFirst();
        if (templateVarOpt.isPresent()) {
            InstantiationContext context = new InstantiationContext(adjustments);
            try {
                AbstractVariable templateVar = templateVarOpt.get();
                IDecisionVariable template = cfg.getDecision(templateVar);
                IDecisionVariable templateMeshes = template.getNestedElement("services");
                ServiceMeshGraphMapper mapper = new ServiceMeshGraphMapper();
                Set<IDecisionVariable> consts = collectConstants(cfg, templateVar.getProject());
                Set<IDecisionVariable> types = new HashSet<>();
                Set<IDecisionVariable> services = new HashSet<>();
                HashMap<IDecisionVariable, Set<IDecisionVariable>> meshes = new HashMap<>();
                for (int n = 0; n < templateMeshes.getNestedElementsCount(); n++) {
                    IDecisionVariable mesh = IvmlUtils.dereference(templateMeshes.getNestedElement(n));
                    Set<IDecisionVariable> meshElts = getOrCreate(meshes, mesh, () -> new HashSet<IDecisionVariable>());
                    IvmlGraph graph = mapper.getGraphFor(mesh);
                    for (IvmlGraphNode node : graph.nodes()) {
                        IDecisionVariable var = node.getVariable();
                        meshElts.add(var);
                        IDecisionVariable impl = IvmlUtils.dereference(var.getNestedElement("impl"));
                        if (null != impl) {
                            services.add(impl);
                            collectTypes(impl.getNestedElement("input"), types);
                            collectTypes(impl.getNestedElement("output"), types);
                            collectTypes(impl.getNestedElement("inInterface"), types);
                            collectTypes(impl.getNestedElement("outInterface"), types);
                        }
                        node.outEdges().forEach(e -> meshElts.add(IvmlUtils.dereference(e.getVariable())));
                    }
                }
                instantiateVariables(consts, "AllConstants", context);
                instantiateVariables(types, "AllTypes", context);
                context.appPrefix = appName; // leave constants and types as they are for now, may be moved up
                context.enableNameAdjustment = true;
                instantiateVariables(services, "AllServices", context);
                IDecisionVariable tmp;
                for (IDecisionVariable m: meshes.keySet()) {
                    context.currentTemplateVariable = m.getDeclaration().getName(); 
                    instantiateTemplateVariable(m, "ServiceMesh", null, Mode.CREATE_SETTARGET, context);
                    instantiateMeshes(meshes.get(m), Mode.CREATE, context); // register substitutions
                    instantiateMeshes(meshes.get(m), Mode.SET_VALUE, context); // set values, crossref substitutions
                    context.currentTemplateVariable = m.getDeclaration().getName();
                    context.meshes.add(instantiateTemplateVariable(m, "ServiceMesh", null, Mode.SET_VALUE, context));
                    context.targetProject = null;
                }
                context.appPrefix = null; // no prefixing for application
                context.currentTemplateVariable = varName; // adjustments with original variable
                tmp = instantiateTemplateVariable(cfg.getDecision(templateVar), "Application", appName, 
                    Mode.BOTH, context);
                if (null != tmp) {
                    result = tmp.getDeclaration().getName();
                    IDecisionVariable derivedFrom = tmp.getNestedElement("derivedFrom");
                    derivedFrom.setValue(ValueFactory.createValue(
                        derivedFrom.getDeclaration().getType(), templateVar.getName()), AssignmentState.ASSIGNED);
                }
                ReasoningResult res = validateAndPropagate(NO_TEMPLATE_FILTER);
                throwIfFails(res, true);
                for (Project p: context.modified) {
                    saveTo(p, getIvmlFile(p));
                }
                getLogger().info("Instantiated template {} to {} with value adjustments {}", varName, appName, 
                    adjustments);
            } catch (ModelManagementException | ModelQueryException | ConfigurationException 
                | ValueDoesNotMatchTypeException e) {
                throw new ExecutionException(e);
            }
        } else {
            throw new ExecutionException("Cannot find template " + varName, null);
        }
        return result;
    }
    
    /* for debugging
    for (Project p: context.modified) {
        System.out.println("-> " + getIvmlFile(p));        
        write(p, new PrintWriter(System.out));
    }     
    */

    /**
     * Collects types that are used in a service/connector {@code var}.
     * 
     * @param var the variable to inspect
     * @param result the types to be modified as a side effect
     */
    private void collectTypes(IDecisionVariable var, Set<IDecisionVariable> result) {
        if (null != var) {
            for (int n = 0; n < var.getNestedElementsCount(); n++) {
                IDecisionVariable ioType = var.getNestedElement(n);
                IDecisionVariable typeRef = ioType.getNestedElement("type");
                if (null != typeRef) {
                    result.add(IvmlUtils.dereference(typeRef));
                }
            }
        }
    }
    
    /**
     * Collects constants in a given project.
     * 
     * @param cfg the configuration to resolve decision variables
     * @param prj the project to scan
     * @return the identified constants, may be empty
     */
    private Set<IDecisionVariable> collectConstants(net.ssehub.easy.varModel.confModel.Configuration cfg, Project prj) {
        Set<IDecisionVariable> result = new HashSet<>();
        IvmlUtils.iterElements(prj, AbstractVariable.class, v -> {
            if (v.isConstant()) {
                IDecisionVariable decVar = cfg.getDecision(v);
                if (decVar != null) {
                    result.add(decVar);
                }
            }
        });
        return result;
    }
    
    /**
     * Maps model elements/abstract variables by their name taken from a given project.
     * 
     * @param cfg the configuration to resolve decision variables
     * @param prj the project to scan
     * @return the identified name-element mappings
     */
    private Map<String, IDecisionVariable> mapElementsByName(net.ssehub.easy.varModel.confModel.Configuration cfg, 
        Project prj) {
        Map<String, IDecisionVariable> result = new HashMap<>();
        IvmlUtils.iterElements(prj, AbstractVariable.class, v -> {
            IDecisionVariable decVar = cfg.getDecision(v);
            String name = getName(decVar, null); // decVar may be null
            if (null != name) {
                result.put(name, decVar);
            }
        });
        return result;
    }

    /**
     * Returns the oktoflow element name from a given decision variable.
     * 
     * @param var the decision variable
     * @param dflt the value to take if no name is found
     * @return the value or {@code dflt}
     */
    private static String getName(IDecisionVariable var, String dflt) {
        String result = dflt;
        if (null != var) {
            IDecisionVariable nameVar = var.getNestedElement("name");
            if (null != nameVar) {
                Value val = nameVar.getValue();
                if (val instanceof StringValue) {
                    result = ((StringValue) val).getValue();
                }
            }
        }
        return result;
    }

    /**
     * Integrates given variables {@code vars} into {@code targetPrj}.
     * 
     * @param vars the variables
     * @param targetPrj the target project to integrate the variables into
     * @param context the instantiation context, to be modified as a side effect
     * @throws ConfigurationException if setting IVML values fails
     * @throws ExecutionException if setting IVML values fails
     */
    private void instantiateVariables(Set<IDecisionVariable> vars, String targetPrj, InstantiationContext context) 
        throws ConfigurationException, ExecutionException {
        net.ssehub.easy.varModel.confModel.Configuration cfg = getIvmlConfiguration();
        Project root = cfg.getProject();
        Project target = IvmlModelQuery.findProject(root, targetPrj);
        Map<String, IDecisionVariable> known = mapElementsByName(cfg, target);
        for (IDecisionVariable t : vars) {
            String tName = getName(t, null);
            if (null != tName && !known.containsKey(tName)) {
                AbstractVariable tDecl = t.getDeclaration();
                DecisionVariableDeclaration var = new DecisionVariableDeclaration(
                    context.prefixVarName(tDecl.getName()), tDecl.getType(), target);
                context.currentTemplateVariable = t.getDeclaration().getName();
                addNameAdjustment(t, context.currentTemplateVariable, context);
                setValue(var, t.getValue(), context);
                target.addBeforeFreeze(var);
                IDecisionVariable dVar = cfg.createDecision(var);
                context.substitutions.put(t.getDeclaration(), var);
                context.modified.add(target);
                notifyChange(dVar, ConfigurationChangeType.CREATED);
            }
        }
    }
    
    /**
     * Sets the configured value for {@code var} as {@code val} considering substitutions and adjustments from 
     * {@code context}.
     * 
     * @param var the variable to configure
     * @param val the (basic) value to set
     * @param context the context driving the adjustment of individual values
     * @throws ExecutionException if setting the value fails
     */
    private void setValue(DecisionVariableDeclaration var, Value val, InstantiationContext context) 
        throws ExecutionException {
        try {
            ValueAdjustmentVisitor adj = new ValueAdjustmentVisitor(context);
            val.clone().accept(adj);
            ConstraintSyntaxTree cst = new ConstantValue(adj.value);
            cst.inferDatatype();
            var.setValue(cst);
        } catch (ValueDoesNotMatchTypeException | CSTSemanticException e) {
            throw new ExecutionException(e.getMessage(), null);
        }
    }
    
    /**
     * Applies substitutions and value adjustments to values.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class ValueAdjustmentVisitor implements IValueVisitor {
        
        private InstantiationContext context;
        private Value value;
        private String nested;
        
        /**
         * Creates a value adjustment visitor.
         * 
         * @param context the instantiation context
         */
        private ValueAdjustmentVisitor(InstantiationContext context) {
            this.context = context;
            nested = context.getCurrentTemplateVariableName();
        }

        @Override
        public void visitConstraintValue(ConstraintValue value) {
            try {
                SubstitutionVisitor vis = new SubstitutionVisitor();
                for (Map.Entry<AbstractVariable, AbstractVariable> ent : context.substitutions.entrySet()) {
                    vis.addVariableMapping(ent.getKey(), ent.getValue(), 0);
                }
                value.getValue().accept(vis);
                ConstraintSyntaxTree vr = vis.getResult();
                this.value = null == vr || vr == value.getValue() ? value 
                    : ValueFactory.createValue(value.getType(), vr);
            } catch (ValueDoesNotMatchTypeException e) {
                getLogger().error("Value does not match: {}", e.getMessage());
            }
        }

        @Override
        public void visitCompoundValue(CompoundValue value) {
            String origNested = nested;
            for (String slot : value.getSlotNames()) {
                nested = origNested + "." + slot;
                try {
                    Value slotVal = value.getNestedValue(slot);
                    if (null != slotVal) { // unconfigured
                        slotVal.accept(this);
                        if (context.adjustments != null) {
                            String adj = context.adjustments.get(nested);
                            if (null != adj) {
                                IDatatype slotType = ((Compound) value.getType()).getElement(slot).getType();
                                this.value = ValueFactory.createValue(slotType, adj);
                            }
                        }
                        value.configureValue(slot, this.value);
                    }
                } catch (ValueDoesNotMatchTypeException e) {
                    getLogger().error("Value does not match: {}", e.getMessage());
                }
            }
            nested = origNested;
            this.value = value;
        }

        @Override
        public void visitContainerValue(ContainerValue value) {
            for (int e = 0; e < value.getElementSize(); e++) {
                value.getElement(e).accept(this);
                try {
                    value.setValue(e, this.value);
                } catch (ValueDoesNotMatchTypeException ex) {
                    getLogger().error("Value does not match: {}", ex.getMessage());
                }                
            }
            this.value = value;
        }

        @Override
        public void visitReferenceValue(ReferenceValue referenceValue) {
            AbstractVariable var = context.substitutions.get(referenceValue.getValue());
            if (null != var) {
                try {
                    referenceValue.setValue(var);
                } catch (ValueDoesNotMatchTypeException e) {
                    getLogger().error("Value does not match: {}", e.getMessage());
                }                
            }
            this.value = referenceValue;
        }

        @Override
        public void visitEnumValue(EnumValue value) {
            this.value = value;
        }

        @Override
        public void visitStringValue(StringValue value) {
            this.value = value;
        }

        @Override
        public void visitIntValue(IntValue value) {
            this.value = value;
        }

        @Override
        public void visitRealValue(RealValue value) {
            this.value = value;
        }

        @Override
        public void visitBooleanValue(BooleanValue value) {
            this.value = value;
        }

        @Override
        public void visitMetaTypeValue(MetaTypeValue value) {
            this.value = value;
        }

        @Override
        public void visitNullValue(NullValue value) {
            this.value = value;
        }

        @Override
        public void visitVersionValue(VersionValue value) {
            this.value = value;
        }
        
    }

    /**
     * Instantiation modes to enable reuse of the respective methods by separating creation and value setting
     * so that reference substitutions can be established during creating and used/applied during setting the value.
     * 
     * @author Holger Eichelberger, SSE
     */
    private enum Mode {

        CREATE(true, false),
        CREATE_SETTARGET(true, false),
        SET_VALUE(false, true),
        BOTH(true, true);
        
        private boolean create;
        private boolean setValue;
        
        /**
         * Creates a constant.
         * 
         * @param create create the variable
         * @param setValue set the (adjusted) value
         */
        private Mode(boolean create, boolean setValue) {
            this.create = create;
            this.setValue = setValue;
        }
        
    }
    
    /**
     * Instantiates a template application/mesh variable.
     * 
     * @param decVar the variable representing the template
     * @param targetType target type of the variable
     * @param varName the name of the variable to create/instantiate (uses 
     *     {@link InstantiationContext#currentTemplateVariable} if <b>null</b>)
     * @param context the instantiation context, may be modified as a side effect
     * @return the instantiated IVML variable
     * @throws ModelManagementException if model management operations fail
     * @throws ModelQueryException if obtaining IVML types fails
     * @throws ConfigurationException if setting IVML values fails
     * @throws ExecutionException if adapting the target project fails
     */
    private IDecisionVariable instantiateTemplateVariable(IDecisionVariable decVar, String targetType, String varName, 
        Mode mode, InstantiationContext context) throws ModelManagementException, ModelQueryException, 
        ConfigurationException, ExecutionException {
        
        varName = null == varName ? context.currentTemplateVariable : varName;
        IDecisionVariable result = null;
        String varNameId = context.prefixVarName(toIdentifier(varName));
        net.ssehub.easy.varModel.confModel.Configuration cfg = getIvmlConfiguration();
        Project root = cfg.getProject();
        IDatatype appType = findType(root, targetType);
        Project appPrj = adaptTarget(root, null == context.targetProject 
            ? getVariableTarget(root, appType, varName, context.getMeshNames()) : context.targetProject);
        if (null != appType) {
            DecisionVariableDeclaration var;
            if (mode.create) {
                var = new DecisionVariableDeclaration(varNameId, appType, appPrj);
                if (Mode.CREATE_SETTARGET == mode) {
                    context.targetProject = appPrj;
                }
                appPrj.addBeforeFreeze(var);
                context.substitutions.put(decVar.getDeclaration(), var);
                addNameAdjustment(decVar, varName, context);
            } else {
                var = (DecisionVariableDeclaration) appPrj.getElement(varNameId);
            }
            if (mode.setValue) {
                setValue(var, decVar.getValue(), context);
                result = cfg.createDecision(var);
                context.modified.add(appPrj);
                notifyChange(result, ConfigurationChangeType.CREATED);
            }
        }
        return result;
    }

    /**
     * Adds an adjustment for the name of {@code decVar} if enabled by {@code #enableNameAdjustment}.
     * 
     * @param decVar the decision variable to get the value for the adjustment from. If set up, 
     *     {@link InstantiationContext#prefixVarName(String)} will be applied to the value.
     * @param varName the original name of the variable
     * @param context the context to modify
     */
    private void addNameAdjustment(IDecisionVariable decVar, String varName, InstantiationContext context) {
        if (context.enableNameAdjustment) {
            final String nameSlot = varName + ".name";
            if (!context.adjustments.containsKey(nameSlot)) {
                String value = IvmlUtils.getVarNameSafe(decVar.getDeclaration(), null);
                if (null != value) {
                    context.adjustments.put(nameSlot, context.prefixVarName(value));
                }
            }
        }
    }

    /**
     * Returns the names of the IVML variables that still need values in the template represented by the IVML variable 
     * {@code varName}.
     * 
     * @param varName the name of the IVML variable representing the template
     * @return the open variables as JSON array
     * @throws ExecutionException if the execution of the operation fails
     */
    public String getOpenTemplateVariables(String varName) throws ExecutionException {
        ReasoningResult res = validateAndPropagate(ANY_PROJECT_FILTER); // must include templates
        return JsonUtils.toJson(IvmlUtils.analyzeForTemplate(res, varName));
    }

    /**
     * Throws an {@link ExecutionException} if the reasoning result {@code res} indicates a problem.
     * 
     * @param res the reasoning result
     * @param reloadIfFail reload the model if there is a failure
     * @throws ExecutionException the exception if reasoning failed
     */
    protected void throwIfFails(ReasoningResult res, boolean reloadIfFail) throws ExecutionException {
        boolean hasConflict = IvmlUtils.analyzeReasoningResult(res, false, false);
        if (hasConflict) {
            if (reloadIfFail) {
                reloadConfiguration();
            }
            String msg = "";
            if (null != res) {
                for (int m = 0; m < res.getMessageCount(); m++) {
                    if (msg.length() > 0) {
                        msg += "\n";
                    }
                    Message rmsg = res.getMessage(m);
                    msg += rmsg.getDescription();
                    msg += rmsg.getConflictComments();
                    msg += rmsg.getConflictSuggestions();
                    // remove?
                    for (int v = 0; v < res.getAffectedVariablesCount(); v++) {
                        if (v > 0) {
                            msg += ", ";
                        }
                        msg += res.getAffectedVariable(v).getQualifiedName();
                    }
                }
                EasyExecutor.printReasoningMessages(res); // preliminary
            } else {
                msg = "Internal reasoning issue. Please check logs.";
            }
            getLogger().error("Reasoning failed: {}", msg);
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
     * Returns the target for a variable to be created.
     * 
     * @param root the root project (may be used as default)
     * @param type the actual type of the variable to be created, may be <b>null</b> then anyway no variable will 
     *     be created
     * @param name optional name if the name may influence the target, may be <b>null</b>
     * @param meshes mesh project names in case of an application project, else ignored; may be <b>null</b>
     * @return the target project
     * @see #isAllowedForModification(Project prj)
     * @throws ExecutionException if model management operations fail
     */
    protected Project getVariableTarget(Project root, IDatatype type, String name, List<String> meshes) 
        throws ExecutionException {
        return root;
    }
    
    /**
     * Allows to adapt a target IVML project, e.g., in testing context.
     * 
     * @param root the root project (for type resolution)
     * @param project the project to be adapted
     * @return the adapted project
     * @throws ExecutionException if adapting fails
     */
    protected Project adaptTarget(Project root, Project project) throws ExecutionException {
        return project;
    }
    
    /**
     * Limits valid identifiers.
     * 
     * @param name the name
     * @return {@code true} for valid identifier, {@code false} else
     */
    static boolean isValidIdentifier(String name) {
        if (name.isEmpty()) {
            return false;
        }
        if (!Character.isJavaIdentifierStart(name.charAt(0))) {
            return false;
        }
        for (int i = 1; i < name.length(); i++) {
            if (!Character.isJavaIdentifierPart(name.charAt(i))) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Turns a type name and an element name into a valid IVML variable name. Potentially adds a number.
     * 
     * @param type the IVML type name to use, may but shall not contain whitespaces an non-identifier characters
     * @param elementName the element name to use, may contain whitespaces an non-identifier characters
     * @param elementVersion the element version to use, may contain whitespaces an non-identifier characters
     * @return the usable variable name
     */
    public String getVariableName(String type, String elementName, String elementVersion) {
        final String separator = "_";
        String varName = type + separator + elementName;
        if (elementVersion.length() > 0) {
            varName += separator + elementVersion;
        }
        StringBuilder builder = new StringBuilder(varName);
        for (int i = builder.length() - 1; i >= 0; i--) {
            if (!Character.isJavaIdentifierPart(builder.charAt(i))) {
                builder.deleteCharAt(i);
            }
        }
        varName = builder.toString();
        if (separator.equals(varName)) { // unlikely but possible
            varName = "unknown";
        }
        if (!Character.isJavaIdentifierStart(varName.charAt(0))) { // unlikely but possible
            varName = "a" + varName;
        }
        if (Character.isUpperCase(varName.charAt(0))) { // convention
            varName = Character.toLowerCase(varName.charAt(0)) + varName.substring(1);
        }
        net.ssehub.easy.varModel.confModel.Configuration cfg = getIvmlConfiguration();
        try {
            String baseName = varName + separator;
            int count = 1;
            while (cfg.getDecision(varName, false) != null || reservedVariableNames.contains(varName)) {
                varName = baseName + String.format("%02d", count++);
            }
        } catch (ModelQueryException e) {
            // ignore
        }
        reservedVariableNames.add(varName); // TODO lock only for a certain time?
        return varName;
    }

    /**
     * Renames an IVML variable. [public for testing]
     * 
     * @param varName the IVML variable name
     * @param newVarName the new IVML variable name
     * @throws ExecutionException if creating the variable fails
     */
    public void renameVariable(String varName, String newVarName) throws ExecutionException {
        if (varName.length() > 0 && newVarName.length() > 0 && !newVarName.equals(varName)) {
            getLogger().info("Renaming IVML variable {} to {}", varName, newVarName);
            net.ssehub.easy.varModel.confModel.Configuration cfg = getIvmlConfiguration();
            Project root = cfg.getProject();
            try {
                AbstractVariable var = IvmlModelQuery.findVariable(root, varName, null);
                cfg.renameVariable(var, toIdentifier(newVarName));
                ReasoningResult res = validateAndPropagate(NO_TEMPLATE_FILTER);
                throwIfFails(res, true);
                Set<Project> projects = IvmlUtils.findProjectsUsingVariable(root, var);
                for (Project p : projects) {
                    saveTo(p, getIvmlFile(p));
                }
                IDecisionVariable dVar = IvmlUtils.obtainDecision(cfg, var);
                notifyChange(dVar, ConfigurationChangeType.MODIFIED);
                getLogger().info("Renamed IVML variable {} to {}", varName, newVarName);
            } catch (ModelQueryException | ConfigurationException e) {
                throw new ExecutionException(e);
            }
        } else {
            getLogger().info("Skipped renaming IVML variable {} to {} as not given/same.", varName, newVarName);
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
    public void createVariable(String varName, String type, boolean asConst, String valueEx) throws ExecutionException {
        if (!isValidIdentifier(varName)) {
            throw new ExecutionException("'" + varName + "' is not a valid identifier", null);
        }
        getLogger().info("Creating IVML variable {} {} = {};", type, varName, valueEx);
        net.ssehub.easy.varModel.confModel.Configuration cfg = getIvmlConfiguration();
        Project root = cfg.getProject();
        try {
            IDatatype t = findType(root, type);
            Project target = adaptTarget(root, getVariableTarget(root, t, varName, null));
            if (null != t) {
                DecisionVariableDeclaration var;
                if (asConst) {
                    var = new ConstantDecisionVariableDeclaration(toIdentifier(varName), t, target);
                } else {
                    var = new DecisionVariableDeclaration(toIdentifier(varName), t, target);
                }
                setValue(var, valueEx);
                target.add(var);
                //alternative: in own constraint, remove setValue above; may be needed if t is container
                //createAssignment(var, valueEx, target); 
                IDecisionVariable dVar = IvmlUtils.obtainDecision(cfg, var);
                ReasoningResult res = validateAndPropagate(NO_TEMPLATE_FILTER);
                throwIfFails(res, true);
                saveTo(target, getIvmlFile(target));
                notifyChange(dVar, ConfigurationChangeType.CREATED);
                getLogger().info("Created IVML variable {} in {}", varName, target.getName());
            } else {
                throw new ExecutionException("No such type " + type, null);
            }
            reservedVariableNames.remove(varName);
        } catch (ModelQueryException | ConfigurationException e) {
            throw new ExecutionException(e);
        }
    }

    /**
     * Defines an internal type creation function.
     * 
     * @author Holger Eichelberger, SSE
     */
    private interface TypeCreationFunction {

        /**
         * Creates the type.
         * 
         * @param name the type name
         * @param type the generic type
         * @param scope the containing scope
         * @return the type
         */
        public IDatatype createType(String name, IDatatype type, Project scope);
    }

    /**
     * Generic IVML type indicators.
     * 
     * @author Holger Eichelberger, SSE
     */
    private enum GenericTypeIndicator {
        
        REF_TO("refTo(", (n, t, s) -> new Reference(n, t, s)),
        SET_OF("setOf(", (n, t, s) -> new net.ssehub.easy.varModel.model.datatypes.Set(n, t, s)),
        SEQUENCE_OF("sequenceOf(", (n, t, s) -> new Sequence(n, t, s));
        
        private String prefix;
        private TypeCreationFunction creator;

        /**
         * Creates a constant.
         * 
         * @param prefix the prefix
         * @param creator the associated type creator
         */
        private GenericTypeIndicator(String prefix, TypeCreationFunction creator) {
            this.prefix = prefix;
            this.creator = creator;
        }
        
        /**
         * Returns the prefix.
         * 
         * @return the prefix
         */
        public String getPrefix() {
            return prefix;
        }
        
        /**
         * Returns the type creator.
         * 
         * @return the creator
         */
        public TypeCreationFunction getCreator() {
            return creator;
        }
        
    }
    
    /**
     * Finds an IVML type by also resolving {@link GenericTypeIndicator}.
     * 
     * @param scope the scope
     * @param type the type as string
     * @return the found type
     * @throws ModelQueryException if finding the type fails
     */
    private IDatatype findType(Project scope, String type) throws ModelQueryException {
        IDatatype result = null;
        for (GenericTypeIndicator indicator: GenericTypeIndicator.values()) {
            String prefix = indicator.getPrefix();
            if (type.startsWith(prefix) && type.endsWith(")")) {
                String t = type.substring(prefix.length(), type.length() - 1);
                result = indicator.getCreator().createType(type, findType(scope, t), scope);
                break;
            }
        }
        if (null == result) {
            result = ModelQuery.findType(scope, type, null);
        }
        return result;
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
                if (varDecl.getProject() == target) {
                    setValue(varDecl, ent.getValue()); 
                } else {
                    removeConstraintsForVariable(target, varDecl);
                    createAssignment(varDecl, ent.getValue(), target); 
                    projects.add(target);
                }
                notifyChange(var, ConfigurationChangeType.MODIFIED);
            } catch (ExecutionException e) {
                history.rollback();
                throw e;
            }
        }
        ReasoningResult result = ReasonerFrontend.getInstance().propagate(cfg.getConfiguration(), null, null);
        boolean hasConflict = IvmlUtils.analyzeReasoningResult(result, false, false);
        if (hasConflict) {
            history.rollback();
            throwIfFails(result, false);
            /*String text = "";
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
            throw new ExecutionException(text, null);*/
        } else {
            getLogger().info("Committing IVML changes:");
            history.commit();
            Map<Project, CopiedFile> copies = new HashMap<>();
            for (Project p: projects) {
                File f = getIvmlFile(p);
                copies.put(p, copyToTmp(f));
                saveTo(p, f);
                getLogger().info(" - Writing IVML file {}", f);
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
            ConstraintSyntaxTree cst = createExpression(null, expression, var.getConfiguration().getProject());
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
     * @param type the target type, may be <b>null</b> for none
     * @param expression the expression
     * @param scope the resolution scope, may be <b>null</b> for the root project
     * @return the syntax tree
     * @throws ExecutionException if the expression cannot be created, e.g., due to syntactic or semantic errors
     */
    protected ConstraintSyntaxTree createExpression(IDatatype type, String expression, Project scope) 
        throws ExecutionException {
        try {
            if (null == scope) {
                scope = getIvmlConfiguration().getProject();
            }
            return ModelUtility.INSTANCE.createExpression(type, expression, scope);
        } catch (CSTSemanticException e) {
            throw new ExecutionException("IVML expression semantic error: " + e.getMessage(), null);
        } catch (ConstraintSyntaxException e) {
            throw new ExecutionException("IVML expression syntax error: " + e.getMessage(), null);
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
            Constraint c = new Constraint(createExpression(null, varDecl.getName() + "=" + valueEx, prj), prj);
            prj.addBeforeFreeze(c); // the usual position
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
            IDatatype type = var.getType();
            ConstraintSyntaxTree cst = createExpression(type, expression, var.getProject());
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
        ReasoningResult res = validateAndPropagate(NO_TEMPLATE_FILTER);
        String msg = "";
        boolean hasConflict = IvmlUtils.analyzeReasoningResult(res, false, false);
        if (hasConflict) {
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
    protected ReasoningResult validateAndPropagate() {
        return validateAndPropagate(null);
    }

    /**
     * Validates the model and propagates values within the model.
     * 
     * @param projectFilter optional filter on projects to reason on, may be <b>null</b>
     * @return the reasoning result
     */
    protected abstract ReasoningResult validateAndPropagate(Predicate<Project> projectFilter);

    /**
     * Reloads the configuration model.
     */
    protected abstract void reloadConfiguration();

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
     * @param prj the project, ignored if <b>null</b>
     * @param type the change type
     */
    protected void notifyChange(Project prj, ConfigurationChangeType type) {
        if (null != prj) {
            net.ssehub.easy.varModel.confModel.Configuration cfg = getIvmlConfiguration();
            for (int e = 0; e < prj.getElementCount(); e++) {
                ContainableModelElement elt = prj.getElement(e);
                if (elt instanceof AbstractVariable) {
                    notifyChange(cfg.getDecision((AbstractVariable) elt), type);
                }
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
    
    /**
     * Returns the logger.
     * 
     * @return the logger
     */
    private static Logger getLogger() {
        return LoggerFactory.getLogger(AbstractIvmlModifier.class);
    }

}
