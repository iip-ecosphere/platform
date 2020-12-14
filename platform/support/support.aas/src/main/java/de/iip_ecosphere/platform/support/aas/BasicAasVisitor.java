/**
 * ******************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.aas;

/**
 * Provides a basic implementation of {@link AasVisitor} to ease implementation of simple/focused visitors and to
 * support interface changes. Visitor implementations are encouraged to inherit from this class rather than directly
 * from the {@link AasVisitor} except that compile errors are preferred in case of interface changes.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BasicAasVisitor implements AasVisitor {

    @Override
    public void visitAas(Aas aas) {
    }

    @Override
    public void endAas(Aas aas) {
    }

    @Override
    public void visitSubmodel(Submodel submodel) {
    }

    @Override
    public void endSubmodel(Submodel submodel) {
    }

    @Override
    public void visitProperty(Property property) {
    }

    @Override
    public void visitOperation(Operation operation) {
    }

    @Override
    public void visitReferenceElement(ReferenceElement referenceElement) {
    }

    @Override
    public void visitSubmodelElementCollection(SubmodelElementCollection collection) {
    }

    @Override
    public void endSubmodelElementCollection(SubmodelElementCollection collection) {
    }

}
