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

package de.iip_ecosphere.platform.security.services.kodex;

import de.iip_ecosphere.platform.services.environment.AbstractGenericServicePluginDescriptor;
import de.iip_ecosphere.platform.services.environment.YamlService;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import de.iip_ecosphere.platform.transport.serialization.TypeTranslator;

/**
 * The plugin descriptor for {@link KodexService}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class KodexServicePluginDescriptor 
    extends AbstractGenericServicePluginDescriptor<KodexService<?, ?>> {

    /**
     * Creates the instance for JSL.
     */
    public KodexServicePluginDescriptor() {
        super(PLUGIN_ID_PREFIX + "kodex-single-cmd", KodexService.class);
    }

    @Override
    public <I, O> KodexService<I, O> createService(TypeTranslator<I, String> inTrans,
        TypeTranslator<String, O> outTrans, ReceptionCallback<O> callback, YamlService yaml, Object... args) {
        return new KodexService<>(inTrans, outTrans, callback, yaml, args);
    }

}
