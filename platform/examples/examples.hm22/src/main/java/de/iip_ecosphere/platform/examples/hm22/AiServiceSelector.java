/**
 * ******************************************************************************
 * Copyright (c) {2022} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.examples.hm22;

import de.iip_ecosphere.platform.services.environment.switching.ServiceSelector;
import iip.datatypes.Command;
import iip.impl.KIFamilyExampleImpl;

/**
 * A selector plugin that threats the {@link Command#getStringParam()} as service id to switch to.
 * Linked into the AI service family via the model.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AiServiceSelector implements ServiceSelector<Command> {
    
    private String actualServiceId;
    
    @Override
    public String select(Command input) {
        String result;
        if (Commands.SWITCH_AI.equalsName(input.getCommand())) {
            result = input.getStringParam();
            if (null == result) { // alternative, do it in ActionDecider
                if (KIFamilyExampleImpl.MEMBER_PYTHONAI.equals(actualServiceId)) {
                    result = KIFamilyExampleImpl.MEMBER_MYRTSA;
                } else if (KIFamilyExampleImpl.MEMBER_MYRTSA.equals(actualServiceId)) {
                    result = KIFamilyExampleImpl.MEMBER_PYTHONAI;
                }
            }
        } else {
            result = null; // keep algorithm
        }
        return result;
    }
    
    @Override
    public void actionCompleted(String id) {
        actualServiceId = id;
        ActionDecider.notifyAiSwitchCompleted(id);
    }

    @Override
    public void initial(String id) {
        actualServiceId = id;
    }

}
