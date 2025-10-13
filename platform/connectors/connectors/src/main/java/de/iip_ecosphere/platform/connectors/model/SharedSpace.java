/**
 * ******************************************************************************
 * Copyright (c) {2025} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.connectors.model;

/**
 * Shared memory space for MIMO connectors. The first connector may create the space (e.g., including a certain 
 * connection), the subsequent connectors may take it up. This is just the top-level type, connectors may/require their 
 * own sub-type of shared space.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface SharedSpace {

}
