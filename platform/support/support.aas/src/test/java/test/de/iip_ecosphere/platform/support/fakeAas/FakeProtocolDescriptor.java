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

package test.de.iip_ecosphere.platform.support.fakeAas;

import de.iip_ecosphere.platform.support.aas.AasFactory.ProtocolCreator;

import de.iip_ecosphere.platform.support.aas.InvocablesCreator;
import de.iip_ecosphere.platform.support.aas.ProtocolDescriptor;
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder;
import de.iip_ecosphere.platform.support.jsl.ExcludeFirst;
import de.iip_ecosphere.platform.support.net.KeyStoreDescriptor;

/**
 * A fake protocol descriptor for testing.
 * 
 * @author Holger Eichelberger, SSE
 */
@ExcludeFirst // don't occur in real factories
public class FakeProtocolDescriptor implements ProtocolDescriptor {

    // accessible for testing
    static final String NAME = "MyProto";
    
    // accessible for testing
    static final ProtocolCreator CREATOR = new ProtocolCreator() {

        @Override
        public InvocablesCreator createInvocablesCreator(String host, int port, KeyStoreDescriptor kstore) {
            return new FakeInvocablesCreator(); 
        }

        @Override
        public ProtocolServerBuilder createProtocolServerBuilder(int port, KeyStoreDescriptor kstore) {
            return new FakeProtocolServerBuilder();
        }
        
    };
    
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public ProtocolCreator createInstance() {
        return CREATOR;
    }

}
