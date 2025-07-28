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

package de.oktoflow.platform.support.ssh.sshd;

import de.iip_ecosphere.platform.support.jsl.ExcludeFirst;
import de.iip_ecosphere.platform.support.plugins.SingletonPluginDescriptor;
import de.iip_ecosphere.platform.support.ssh.Ssh;
import de.iip_ecosphere.platform.support.ssh.SshProviderDescriptor;

/**
 * The Apache Sshd plugin descriptor.
 * 
 * @author Holger Eichelberger, SSE
 */
@ExcludeFirst // reduce priority if there is a plugin
public class SshdSshRestPluginDescriptor extends SingletonPluginDescriptor<Ssh> implements SshProviderDescriptor {
    
    /**
     * Creates the descriptor.
     */
    public SshdSshRestPluginDescriptor() {
        super("ssh-sshd", null, Ssh.class, p -> new SshdSsh());
    }
    
    @Override
    public Ssh create() {
        return new SshdSsh();
    }
    
}
