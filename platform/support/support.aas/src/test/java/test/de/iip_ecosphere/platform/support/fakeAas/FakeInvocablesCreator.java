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

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import de.iip_ecosphere.platform.support.aas.InvocablesCreator;

/**
 * A fake invocables creator that does nothing (in case that the fake AAS is active in basic component tests).
 * 
 * @author Holger Eichelberger, SSE
 */
public class FakeInvocablesCreator implements InvocablesCreator {

    @Override
    public Supplier<Object> createGetter(String name) {
        return WRITE_ONLY;
    }

    @Override
    public Consumer<Object> createSetter(String name) {
        return READ_ONLY;
    }

    @Override
    public Function<Object[], Object> createInvocable(String name) {
        return p -> null;
    }

}
