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

package de.iip_ecosphere.platform.support.aas;

import de.iip_ecosphere.platform.support.Builder;

/**
 * Marks the implementing class as a parent for {@link DeferredBuilder deferred builders}. On a parent, building the 
 * deferred builders can be requested explicitly .
 * 
 * @author Holger Eichelberger, SSE
 */
public interface DeferredParent {

    /**
     * Forces building the {@link DeferredBuilder deferred builders} registered with this sub-model as top-most parent. 
     * During the basic construction of an AAS this is not needed, because at latest the builder of the sub-model is 
     * supposed to trigger building the deferred builders in {@link Builder#build()}. However, in dynamic situations 
     * the parent may already be build, i.e., there is no sub-model builder and you must explicitly force building the 
     * deferred builders. This method will perform this task and transitively also build nested deferred builders.
     */
    public void buildDeferred();

}
