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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.configuration.ConfigurationSetup;
import de.iip_ecosphere.platform.configuration.ivml.IvmlGraphMapper.IvmlGraph;
import de.iip_ecosphere.platform.support.aas.InvocablesCreator;
import de.iip_ecosphere.platform.support.aas.Property.PropertyBuilder;
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection.SubmodelElementCollectionBuilder;
import de.iip_ecosphere.platform.support.aas.Type;
import de.iip_ecosphere.platform.support.iip_aas.AasUtils;
import de.iip_ecosphere.platform.support.iip_aas.json.JsonResultWrapper;
import net.ssehub.easy.instantiation.core.model.vilTypes.configuration.ChangeHistory;
import net.ssehub.easy.instantiation.core.model.vilTypes.configuration.Configuration;
import net.ssehub.easy.reasoning.core.frontend.ReasonerFrontend;
import net.ssehub.easy.reasoning.core.reasoner.Message;
import net.ssehub.easy.reasoning.core.reasoner.ReasoningResult;
import net.ssehub.easy.varModel.confModel.AssignmentState;
import net.ssehub.easy.varModel.confModel.ConfigurationException;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.model.AbstractVariable;
import net.ssehub.easy.varModel.model.IvmlDatatypeVisitor;
import net.ssehub.easy.varModel.model.ModelQueryException;
import net.ssehub.easy.varModel.model.Project;
import net.ssehub.easy.varModel.model.datatypes.IDatatype;
import net.ssehub.easy.varModel.model.datatypes.TypeQueries;
import net.ssehub.easy.varModel.model.values.ValueDoesNotMatchTypeException;
import net.ssehub.easy.varModel.model.values.ValueFactory;

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
                setGraph(AasUtils.readString(a, 0), AasUtils.readString(a, 1), AasUtils.readString(a, 2))
            ));
        sBuilder.defineOperation(OP_CREATE_VARIABLE, 
            new JsonResultWrapper(a -> createVariable(AasUtils.readString(a, 0), AasUtils.readString(a, 1))));
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
                var.setValue(ValueFactory.createValue(decl.getType(), ent.getKey()), 
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
            for (Project p: projects) {
                @SuppressWarnings("unused")
                File f = getIvmlFile(p);
                //ConfigurationSaver
                // write 
            }
            // TODO write model!            
        }
        return "";
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
     * @param qualifiedVarName the IVML variable holding the graph
     * @param format the format of the graph to return 
     * @return the graph in the specified {@code format}
     * @throws ExecutionException if reading the graph structure fails
     */
    private String getGraph(String qualifiedVarName, String format) throws ExecutionException {
        GraphFormat gFormat = getGraphFormat(format);
        IDecisionVariable var = getVariable(qualifiedVarName);
        IvmlGraph graph = graphMapper.getGraphFor(var);
        return gFormat.toString(graph);
    }

    /**
     * Changes a graph structure in IVML.
     * 
     * @param qualifiedVarName the IVML variable holding the graph
     * @param format the format of the graph
     * @param value the value
     * @return <b>null</b> always
     * @throws ExecutionException if setting the graph structure fails
     */
    private synchronized Object setGraph(String qualifiedVarName, String format, String value) 
        throws ExecutionException {
        GraphFormat gFormat = getGraphFormat(format);
        IDecisionVariable var = getVariable(qualifiedVarName);
        IvmlGraph graph = gFormat.fromString(value, graphMapper.getGraphFactory(), this);
        graphMapper.synchronize(var, graph);
        return null;
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
     * @param qualifiedVarName the qualified IVML variable name
     * @param type the (qualified) IVML type
     * @return <b>null</b> always
     * @throws ExecutionException if creating the variable fails
     */
    private Object createVariable(String qualifiedVarName, String type) throws ExecutionException {
        return null; // TODO TBD
    }

    /**
     * Deletes an IVML variable. In case of a graph, this may subsequently delete further variables.
     * 
     * @param qualifiedVarName the qualified IVML variable name to delete
     * @return <b>null</b> always
     * @throws ExecutionException if creating the variable fails
     */
    private Object deleteVariable(String qualifiedVarName) throws ExecutionException {
        return null; // TODO TBD
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
            .addInputVariable("qualifiedVarName", Type.STRING)
            .addInputVariable("format", Type.STRING)
            .setInvocable(iCreator.createInvocable(OP_GET_GRAPH))
            .build(Type.STRING);
        smBuilder.createOperationBuilder(OP_SET_GRAPH)
            .addInputVariable("qualifiedName", Type.STRING)
            .addInputVariable("format", Type.STRING)
            .addInputVariable("val", Type.STRING)
            .setInvocable(iCreator.createInvocable(OP_SET_GRAPH))
            .build(Type.NONE);
        smBuilder.createOperationBuilder(OP_CREATE_VARIABLE)
            .addInputVariable("qualifiedVarName", Type.STRING)
            .addInputVariable("type", Type.STRING)
            .setInvocable(iCreator.createInvocable(OP_CREATE_VARIABLE))
            .build(Type.NONE);
        smBuilder.createOperationBuilder(OP_DELETE_VARIABLE)
            .addInputVariable("qualifiedVarName", Type.STRING)
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
            String varName = var.getDeclaration().getName();
            IDatatype varType = var.getDeclaration().getType();
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
                varBuilder.createPropertyBuilder(AasUtils.fixId(metaShortId.apply("size")))
                    .setValue(Type.INTEGER, var.getNestedElementsCount())
                    .build();
                varBuilder.build();
            } else {
                Object aasValue = getValue(var);
                varType.getType().accept(TYPE_VISITOR);
                Type aasType = TYPE_VISITOR.getAasType();
                // TODO setSemanticId via annotation?
                String propName = id == null ? varName : id;
                PropertyBuilder pb = builder.createPropertyBuilder(AasUtils.fixId(propName));
                if (var.getState() == AssignmentState.FROZEN) {
                    pb.setValue(aasType, aasValue);
                } else {
                    pb.setType(aasType).bind(() -> getValue(var), PropertyBuilder.READ_ONLY);
                }
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
