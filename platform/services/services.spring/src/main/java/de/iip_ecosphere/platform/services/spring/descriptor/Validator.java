/**
 * ******************************************************************************
 * Copyright (c) {2021} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.services.spring.descriptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.iip_ecosphere.platform.support.iip_aas.Version;

/**
 * A simple validator for deployment descriptors.
 * 
 * @author Holger Eichelberger, SSE
 */
public class Validator {

    private List<String> messages = new ArrayList<>();
    private Set<String> serviceIds = new HashSet<>();
    
    /**
     * Returns whether there are validation (error) messages.
     * 
     * @return {@code true} for messages, {@code false} for no messages/everything is ok
     */
    public boolean hasMessages() {
        return messages.size() > 0;
    }
    
    /**
     * Returns all messages as string.
     * 
     * @return all messages
     */
    public String getMessages() {
        StringBuilder tmp = new StringBuilder();
        for (int m = 0; m < messages.size(); m++) {
            if (m > 0) {
                tmp.append("\n");
            }
            tmp.append(messages.get(m));
        }
        return tmp.toString();
    }
    
    /**
     * Returns the validation messages.
     * 
     * @return the individual messages
     */
    public Iterable<String> messages() {
        return messages;
    }
    
    /**
     * Clears this instance for re-use.
     */
    public void clear() {
        messages.clear();
        serviceIds.clear();
    }
    
    /**
     * Validates the given artifact (and contained descriptor elements).
     * 
     * @param artifact the artifact to validate
     */
    public void validate(Artifact artifact) {
        String msgContext = "";
        assertStringNotEmpty(artifact.getId(), "id", msgContext);
        assertStringNotEmpty(artifact.getName(), "name", msgContext);
        assertVersion(artifact.getVersion(), "version", msgContext);

        Map<String, Type> typeMap = new HashMap<>();
        for (Type t : artifact.getTypes()) {
            if (assertJavaIdentifier(t.getName(), "name", msgContext)) {
                if (typeMap.containsKey(t.getName())) {
                    messages.add("Declared type `" + t.getName() + "` is not unique.");
                }
                try {
                    Class.forName(t.getName());
                    messages.add("Declared type `" + t.getName() + "` shall not be known as Java class here.");
                } catch (ClassNotFoundException e) {
                    // this is ok
                }
                typeMap.put(t.getName(), t);
            }
        }
        Set<String> fieldNames = new HashSet<String>();
        for (Type t : artifact.getTypes()) {
            for (Field f: t.getFields()) {
                assertStringNotEmpty(f.getName(), "name", msgContext);
                if (fieldNames.contains(t.getName())) {
                    messages.add("Declared field `" + f.getName() + "`in declared type `" + t.getName() 
                        + "` is not unique.");
                }
                assertType(f.getType(), typeMap, "type", f.getName() + " in " + t.getName());
                fieldNames.add(f.getName());
            }
            fieldNames.clear();
        }

        if (assertList(artifact.getServices(), true, "services", msgContext, (s, m) -> validate(s, typeMap, m))) {
            int sCount = 0;
            for (Service s : artifact.getServices()) {
                if (null != s.getEnsembleWith()) {
                    String ensemble = s.getEnsembleWith(); 
                    if (null != ensemble && ensemble.length() > 0 && !serviceIds.contains(ensemble)) {
                        messages.add("Ensemble entry '" + ensemble + "' in service #" + sCount + " is not declared as "
                            + "service .");
                    }
                }
                sCount++;
            }
        }
    }

    /**
     * Validates the given service (and contained descriptor elements).
     * 
     * @param service the service to validate
     * @param types the declared types
     */
    public void validate(Service service, List<? extends Type> types) {
        validate(service, toMap(types), "");
    }

    /**
     * Validates the given service (and contained descriptor elements).
     * 
     * @param service the service to validate
     * @param types the declared types
     * @param msgContext nested context information for location of unnamed elements in validation messages
     */
    private void validate(Service service, Map<String, Type> types, String msgContext) {
        if (assertStringNotEmpty(service.getId(), "id", msgContext)) {
            serviceIds.add(service.getId());
        }
        assertStringNotEmpty(service.getName(), "name", msgContext);
        assertVersion(service.getVersion(), "version", msgContext);
        assertFieldNotNull(service.getDescription(), "description", msgContext); // optional
        assertFieldNotNull(service.getKind(), "kind", msgContext);
        assertStringList(service.getCmdArg(), "cmdArg", "arg", msgContext);
        assertList(service.getParameters(), true, "parameters", msgContext, (p, m) -> validate(p, types, m));
        assertList(service.getRelations(), true, "relations", msgContext, (r, m) -> validate(r, types, m));
        if (null != service.getProcess()) {
            validate(service.getProcess(), appendToContext(msgContext, "process"));
        }
    }

    /**
     * Turns the given {@code types} into a map, mapping the type name to its type descriptor.
     * 
     * @param types the types to map
     * @return the mapped types
     */
    public static Map<String, Type> toMap(List<? extends Type> types) {
        Map<String, Type> typeMap = new HashMap<>();
        for (Type t : types) {
            typeMap.put(t.getName(), t);
        }
        return typeMap;
    }

    /**
     * Validates the given relation (and contained descriptor elements).
     * 
     * @param relation the relation to validate
     * @param types the declared types
     */
    public void validate(Relation relation, Map<String, Type> types) {
        validate(relation, types, "");
    }

    /**
     * Validates the given typed data (and contained descriptor elements).
     * 
     * @param typed the typed data to validate
     * @param msgContext nested context information for location of unnamed elements in validation messages
     * @param types the declared types
     */
    private void validate(TypedData typed, Map<String, Type> types, String msgContext) {
        assertFieldNotNull(typed.getName(), "channel", msgContext);
        assertFieldNotNull(typed.getDescription(), "description", msgContext); // optional
        assertType(typed.getType(), types, "type", msgContext);
    }
    
    /**
     * Validates the given relation (and contained descriptor elements).
     * 
     * @param relation the relation to validate
     * @param msgContext nested context information for location of unnamed elements in validation messages
     * @param types the declared types
     */
    private void validate(Relation relation, Map<String, Type> types, String msgContext) {
        boolean chOk = assertFieldNotNull(relation.getChannel(), "channel", msgContext);
        if (null != relation.getEndpoint()) { // endpoints optional depending on the relation
            validate(relation.getEndpoint(), appendToContext(msgContext, "endpoint"));
        }
        assertFieldNotNull(relation.getDescription(), "description", msgContext); // optional
        if (chOk && relation.getChannel().length() > 0) {
            assertFieldNotNull(relation.getDirection(), "direction", msgContext);
            for (String t : relation.getTypes()) {
                assertType(t, types, "type", msgContext);
            }
        }
    }

    /**
     * Validates the given process (and contained descriptor elements).
     * 
     * @param process the process to validate
     */
    public void validate(ProcessSpec process) {
        validate(process, "");
    }

    /**
     * Validates the given process (and contained descriptor elements).
     * 
     * @param process the process to validate
     * @param msgContext nested context information for location of unnamed elements in validation messages
     */
    private void validate(ProcessSpec process, String msgContext) {
        assertStringList(process.getArtifacts(), "artifacts", "artifact", msgContext);
        assertCondition(process.getArtifacts() != null && process.getArtifacts().size() > 0, 
            "At least one artifact must be given", msgContext);
        assertStringList(process.getCmdArg(), "cmdArg", "arg", msgContext);
        if (!process.isStarted()) {
            assertStringNotEmpty(process.getExecutable(), "executable", msgContext);
            if (assertFieldNotNull(process.getAasEndpoint(), "aasEndpoint", msgContext)) {
                validate(process.getAasEndpoint(), appendToContext(msgContext, "aasEndpoint"));
            }
            if (assertFieldNotNull(process.getStreamEndpoint(), "streamEndpoint", msgContext)) {
                validate(process.getStreamEndpoint(), appendToContext(msgContext, "streamEndpoint"));
            }
            if (assertFieldNotNull(process.getServiceStreamEndpoint(), "serviceStreamEndpoint", msgContext)) {
                validate(process.getServiceStreamEndpoint(), appendToContext(msgContext, "serviceStreamEndpoint"));
            }
        }
        // started and waitTime are valid anyway
    }

    /**
     * Appends {@code text} to {@code context} considering that {@code context} may be empty or <b>null</b>.
     * 
     * @param context the context to append, may be empty or <b>null</b>
     * @param text the text to append
     * @return the appended context information
     */
    private String appendToContext(String context, String text) {
        String result = context;
        if (result == null) {
            result = "";
        } else if (result.length() > 0) {
            result += "/";
        }
        return result + text;
    }

    /**
     * Validates the given endpoint (and contained descriptor elements).
     * 
     * @param endpoint  the endpoint  to validate
     */
    public void validate(Endpoint endpoint) {
        validate(endpoint, "");
    }
    
    /**
     * Validates the given endpoint (and contained descriptor elements).
     * 
     * @param endpoint  the endpoint  to validate
     * @param msgContext nested context information for location of unnamed elements in validation messages
     */
    private void validate(Endpoint endpoint, String msgContext) {
        assertStringNotEmpty(endpoint.getPortArg(), "portArg", msgContext); // port shall be given
        assertFieldNotNull(endpoint.getHostArg(), "hostArg", msgContext); // host is optional
    }

    /**
     * Asserts that {@code type} is a known type, either in {@link TypeResolver#isPrimitive(String) primitives}, 
     * {@code types} or as a Java type.
     * 
     * @param type the type name to check
     * @param types the declared types
     * @param field the field the string is taken from for composing an error message
     * @param msgContext the context of the message/validate element for better location by the caller, ignored if 
     *   empty or <b>null</b>
     * @return {@code true} if successful, {@code false} if failed
     */
    private boolean assertType(String type, Map<String, Type> types, String field, String msgContext) {
        boolean ok = assertJavaIdentifier(type, field, msgContext);
        if (ok && !types.containsKey(type) && !TypeResolver.isPrimitive(type)) {
            try {
                Class.forName(type);
            } catch (ClassNotFoundException e) {
                messages.add(appendContext("Type `" + type + "` is not known", msgContext));
                ok = false;
            }
        }
        return ok; 
    }

    /**
     * Asserts that {@code name} in a field is not <b>null</b>, not empty and a Java identifier.
     * 
     * @param name the name to check
     * @param field the field the string is taken from for composing an error message
     * @param msgContext the context of the message/validate element for better location by the caller, ignored if 
     *   empty or <b>null</b>
     * @return {@code true} if successful, {@code false} if failed
     */
    private boolean assertJavaIdentifier(String name, String field, String msgContext) {
        boolean ok = assertStringNotEmpty(name, "Field '" + field + "' must not be null", msgContext); 
        if (ok) {
            ok &= Character.isJavaIdentifierStart(name.charAt(0));
            for (int c = 1; ok && c < name.length(); c++) {
                ok &= Character.isJavaIdentifierPart(name.charAt(c));
            }
        }
        return ok;
    }
    
    /**
     * Asserts that {@code object} in a field is not <b>null</b>.
     * 
     * @param object the object to check
     * @param field the field the string is taken from for composing an error message
     * @param msgContext the context of the message/validate element for better location by the caller, ignored if 
     *   empty or <b>null</b>
     * @return {@code true} if successful, {@code false} if failed
     */
    private boolean assertFieldNotNull(Object object, String field, String msgContext) {
        return assertCondition(object != null, "Field '" + field + "' must not be null", msgContext); 
    }
    
    /**
     * Asserts that {@code object} in a field is a version.
     * 
     * @param object the object to check
     * @param field the field the string is taken from for composing an error message
     * @param msgContext the context of the message/validate element for better location by the caller, ignored if 
     *   empty or <b>null</b>
     * @return {@code true} if successful, {@code false} if failed
     */
    private boolean assertVersion(Object object, String field, String msgContext) {
        boolean ok = assertCondition(object instanceof Version, "Field '" + field + "' must be a version", msgContext);
        if (ok) {
            ok = assertCondition(Version.isVersion(object.toString()), 
                "Field 'version' must be formatted as a version string", msgContext);
        }
        return ok;
    }

    /**
     * A validation function, to point generically to the private validation functions defined by this class.
     * 
     * @param <O> the object type to validate
     * @author Holger Eichelberger, SSE
     */
    private interface ValidatorFunction<O> {

        /**
         * Validate the given object.
         * 
         * @param object the object to validate
         * @param msgContext the context of the message/validate element for better location by the caller, ignored if 
         *   empty or <b>null</b>
         */
        public void validate(O object, String msgContext);
        
    }
    
    /**
     * Asserts a list of objects that can be validated.
     * 
     * @param <O> the type of objects
     * @param list the list of objects to be asserted
     * @param fieldNotNull if {@code true}, apply {@link #assertFieldNotNull(Object, String, String)} to {@code list} 
     *    and return its result, else return always {@code true}
     * @param field the field the string is taken from for composing an error message
     * @param msgContext the context of the message/validate element for better location by the caller, ignored if 
     *   empty or <b>null</b>
     * @param func if {@code list} as object/field is considered to be ok, apply {@code func} to each element
     *   with respective iterative context set
     * @return {@code true} if successful, {@code false} if failed
     */
    private <O> boolean assertList(List<O> list, boolean fieldNotNull, String field, String msgContext, 
        ValidatorFunction<O> func) {
        boolean ok = fieldNotNull ? assertFieldNotNull(list, field, msgContext) : true;
        if (ok) {
            for (int e = 0; e < list.size(); e++) {
                func.validate(list.get(e), appendToContext(msgContext, field + " #" + e));
            }
        }
        return ok;   
    }

    /**
     * Asserts that the given string list is either empty or strings are not empty.
     * 
     * @param list the command line argument list
     * @param field the field the string is taken from for composing an error message
     * @param entry the symbolic name of an entry for composing an error message
     * @param msgContext the context of the message/validate element for better location by the caller, ignored if 
     *   empty or <b>null</b>
     * @return {@code true} if successful, {@code false} if failed
     */
    private boolean assertStringList(List<String> list, String field, String entry, String msgContext) {
        boolean ok = assertFieldNotNull(list, field, msgContext);
        if (ok) {
            for (int c = 0; c < list.size(); c++) {
                ok &= assertStringNotEmpty(list.get(c), field, appendToContext(msgContext, entry + " #" + c));
            }
        }
        return ok;   
    }

    /**
     * Asserts that {@code string} is not empty, in particular not <b>null</b>.
     * 
     * @param string the string to check
     * @param field the field the string is taken from for composing an error message
     * @param msgContext the context of the message/validate element for better location by the caller, ignored if 
     *   empty or <b>null</b>
     * @return {@code true} if successful, {@code false} if failed
     */
    private boolean assertStringNotEmpty(String string, String field, String msgContext) {
        return assertCondition(string != null && string.length() > 0, 
            "String in field '" + field + "' must not be empty", msgContext);
    }

    /**
     * Asserts a condition.
     * 
     * @param condition the evaluated condition
     * @param msg the message if {@code condition} does not hold. A missing trailing "." is added. 
     * @param msgContext the context of the message/validate element for better location by the caller, ignored if 
     *   empty or <b>null</b>
     * @return {@code true} if successful, {@code false} if failed
     */
    private boolean assertCondition(boolean condition, String msg, String msgContext) {
        if (!condition) {
            messages.add(appendContext(msg, msgContext));
        }
        return condition;
    }

    /**
     * Appends the {@code msgContext} to {@code msg} for output.
     * 
     * @param msg the message
     * @param msgContext the message context
     * @return msg with appende context
     */
    private String appendContext(String msg, String msgContext) {
        if (null != msgContext && msgContext.length() > 0) {
            msg += " (in " + msgContext + ")"; 
        }
        if (!msg.endsWith(".")) {
            msg += ".";
        }
        return msg;
    }

}
