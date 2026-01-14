/**
 * ******************************************************************************
 * Copyright (c) {2026} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.connectors;

import java.io.PrintStream;
import java.util.List;
import java.util.function.Function;

import de.iip_ecosphere.platform.support.Builder;

/**
 * Descriptive information about a connector field.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ConnectorField {
    
    private String name;
    private String path;
    private String nativeType;
    private String nativeId;
    // initial, more may follow
    
    /**
     * Builds a connector field.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class ConnectorFieldBuilder implements Builder<ConnectorField> {

        private String name;
        private String path;
        private String nativeType;
        private String nativeId;

        /**
         * Creates a field builder.
         * 
         * @param name the name of the field, by default also used as path
         */
        public ConnectorFieldBuilder(String name) {
            this.name = name;
            this.path = name;
        }
        
        /**
         * Sets the (hierarchical) path to the field. By default, the path is just the same as the name.
         * 
         * @param path the path
         * @return <b>this</b> for chaining
         */
        public ConnectorFieldBuilder setPath(String path) {
            this.path = path;
            return this;
        }

        /**
         * Sets the native type as used in/by the protocol.
         * 
         * @param nativeType the native type
         * @return <b>this</b> for chaining
         */
        public ConnectorFieldBuilder setNativeType(String nativeType) {
            this.nativeType = nativeType;
            return this;
        }

        /**
         * Sets the native type as used in/by the protocol if {@code nativeType} is not <b>null</b>.
         * 
         * @param nativeType the native type
         * @return <b>this</b> for chaining
         */
        public ConnectorFieldBuilder setNativeTypeIfNotNull(String nativeType) {
            if (null != nativeType) {
                setNativeType(nativeType);
            }
            return this;
        }

        /**
         * Sets the native id as used in/by the protocol.
         * 
         * @param nativeId the native id
         * @return <b>this</b> for chaining
         */
        public ConnectorFieldBuilder setNativeId(String nativeId) {
            this.nativeId = nativeId;
            return this;
        }

        @Override
        public ConnectorField build() {
            ConnectorField result = new ConnectorField();
            result.name = name;
            result.path = path;
            result.nativeType = nativeType;
            result.nativeId = nativeId;
            return result;
        }
        
    }

    /**
     * Creates a connector field instance.
     */
    private ConnectorField() {
    }

    /**
     * Returns a {@link ConnectorFieldBuilder}.
     * 
     * @param name the name of the field
     * @return the builder
     */
    public static ConnectorFieldBuilder builder(String name) {
        return new ConnectorFieldBuilder(name);
    }

    /**
     * Returns the name of the field.
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the access path to the field.
     * 
     * @return the path, by default the same as {@link #getName()}
     */
    public String getPath() {
        return path;
    }

    /**
     * Returns the native type as used in/by the protocol.
     * 
     * @return the nativeType, may be <b>null</b> or empty if unknown/unused
     */
    public String getNativeType() {
        return nativeType;
    }

    /**
     * Returns the native id as used in/by the protocol.
     * 
     * @return the nativeId, may be <b>null</b> or empty if unknown/unused
     */
    public String getNativeId() {
        return nativeId;
    }

    /**
     * Helper function to print connector fields with a default formatter to {@code out}.
     * 
     * @param fields the fields to print
     * @param out the output stream to print to
     * @see #printFields(List, Function, PrintStream)
     */
    public static void printFields(List<ConnectorField> fields, PrintStream out) {
        printFields(fields, f -> "- " + f.getPath() + ", " + f.getNativeType() + ", " + f.getNativeId(), out);
    }

    /**
     * Helper function to print connector fields with a default formatter to {@code out}.
     * 
     * @param fields the fields to print
     * @param formatter the field output formatter
     * @param out the output stream to print to
     */
    public static void printFields(List<ConnectorField> fields, Function<ConnectorField, String> formatter, 
        PrintStream out) {
        if (null != fields) {
            for (ConnectorField f : fields) {
                out.println(formatter.apply(f));
            }
        }
    }

}
