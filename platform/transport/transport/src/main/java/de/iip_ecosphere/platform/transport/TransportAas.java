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

package de.iip_ecosphere.platform.transport;

import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.aas.Type;
import de.iip_ecosphere.platform.support.iip_aas.AasContributor;
import de.iip_ecosphere.platform.transport.serialization.SerializerRegistry;

/**
 * Implements the transport AAS contributor. Do not rename, this class is referenced in {@code META-INF/services}.
 * 
 * The created AAS sub-model has the following (very preliminary) structure:
 * <ul>
 *   <li>Property: protocol (String)</li>
 *   <li>Property: wireFormat (String)</li>
 * </ul>
 * 
 * @author Holger Eichelberger, SSE
 */
public class TransportAas implements AasContributor {

    public static final String NAME_SUBMODEL = "IIP-Ecosphere.transport";
    public static final String NAME_VAR_CONNECTOR = "protocol";
    public static final String NAME_VAR_SERIALIZER = "wireFormat";
    // TODO endpoints?
    
    @Override
    public Aas contributeTo(AasBuilder aasBuilder) {
        SubmodelBuilder smB = aasBuilder.createSubmodelBuilder(NAME_SUBMODEL);
        smB.createPropertyBuilder(NAME_VAR_CONNECTOR)
            .setValue(Type.STRING, TransportFactory.getConnectorName())
            .build();
        smB.createPropertyBuilder(NAME_VAR_SERIALIZER)
            .setValue(Type.STRING, SerializerRegistry.getName())
            .build();
        smB.build();
        return null;
    }

}
