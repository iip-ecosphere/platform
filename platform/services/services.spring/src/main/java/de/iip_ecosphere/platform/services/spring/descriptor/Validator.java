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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.iip_ecosphere.platform.services.Version;

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
        
        if (assertList(artifact.getServices(), true, "services", msgContext, (s, m) -> validate(s, m))) {
            int sCount = 0;
            for (Service s : artifact.getServices()) {
                if (null != s.getEnsembleWith()) {
                    for (String e : s.getEnsembleWith()) {
                        if (null != e && e.length() > 0 && !serviceIds.contains(e)) {
                            messages.add("Ensemble entry '" + e + "' in service #" + sCount + " is not declared as "
                                + "service .");
                        }
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
     */
    public void validate(Service service) {
        validate(service, "");
    }

    /**
     * Validates the given service (and contained descriptor elements).
     * 
     * @param service the service to validate
     * @param msgContext nested context information for location of unnamed elements in validation messages
     */
    private void validate(Service service, String msgContext) {
        if (assertStringNotEmpty(service.getId(), "id", msgContext)) {
            serviceIds.add(service.getId());
        }
        assertStringNotEmpty(service.getName(), "name", msgContext);
        assertStringNotEmpty(service.getVersion(), "version", msgContext);
        assertCondition(Version.isVersion(service.getVersion()), 
            "Field 'version' must be formatted as a version string", msgContext);
        assertFieldNotNull(service.getDescription(), "description", msgContext); // optional
        assertFieldNotNull(service.getKind(), "kind", msgContext);
        assertStringList(service.getCmdArg(), "cmdArg", "arg", msgContext);
        assertStringList(service.getEnsembleWith(), "ensembleWith", "id", msgContext);
        assertList(service.getDependencies(), true, "dependencies", msgContext, (d, m) -> validate(d, m));
        assertList(service.getRelations(), true, "relations", msgContext, (r, m) -> validate(r, m));
        if (null != service.getProcess()) {
            validate(service.getProcess(), appendToContext(msgContext, "process"));
        }
    }

    /**
     * Validates the given dependency (and contained descriptor elements).
     * 
     * @param dependency the dependency to validate
     */
    public void validate(ServiceDependency dependency) {
        validate(dependency, "");
    }

    /**
     * Validates the given dependency (and contained descriptor elements).
     * 
     * @param dependency the dependency to validate
     * @param msgContext nested context information for location of unnamed elements in validation messages
     */
    private void validate(ServiceDependency dependency, String msgContext) {
        assertStringNotEmpty(dependency.getId(), "id", msgContext);
    }

    /**
     * Validates the given relation (and contained descriptor elements).
     * 
     * @param relation the relation to validate
     */
    public void validate(Relation relation) {
        validate(relation, "");
    }

    /**
     * Validates the given relation (and contained descriptor elements).
     * 
     * @param relation the relation to validate
     * @param msgContext nested context information for location of unnamed elements in validation messages
     */
    private void validate(Relation relation, String msgContext) {
        assertFieldNotNull(relation.getChannel(), "channel", msgContext);
        if (assertFieldNotNull(relation.getEndpoint(), "endpoint", msgContext)) {
            validate(relation.getEndpoint(), appendToContext(msgContext, "endpoint"));
        }
    }

    /**
     * Validates the given process (and contained descriptor elements).
     * 
     * @param process the process to validate
     */
    public void validate(Process process) {
        validate(process, "");
    }

    /**
     * Validates the given process (and contained descriptor elements).
     * 
     * @param process the process to validate
     * @param msgContext nested context information for location of unnamed elements in validation messages
     */
    private void validate(Process process, String msgContext) {
        assertStringNotEmpty(process.getPath(), "path", msgContext);
        assertStringList(process.getCmdArg(), "cmdArg", "arg", msgContext);
        if (assertFieldNotNull(process.getAasEndpoint(), "aasEndpoint", msgContext)) {
            validate(process.getAasEndpoint(), appendToContext(msgContext, "aasEndpoint"));
        }
        if (assertFieldNotNull(process.getStreamEndpoint(), "streamEndpoint", msgContext)) {
            validate(process.getStreamEndpoint(), appendToContext(msgContext, "streamEndpoint"));
        }
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
     * @param condition
     * @param msg the message if {@code condition} does not hold. A missing trailing "." is added. 
     * @param msgContext the context of the message/validate element for better location by the caller, ignored if 
     *   empty or <b>null</b>
     * @return {@code true} if successful, {@code false} if failed
     */
    private boolean assertCondition(boolean condition, String msg, String msgContext) {
        if (!condition) {
            if (null != msgContext && msgContext.length() > 0) {
                msg += " (in " + msgContext + ")"; 
            }
            if (!msg.endsWith(".")) {
                msg += ".";
            }
            messages.add(msg);
        }
        return condition;
    }

}
