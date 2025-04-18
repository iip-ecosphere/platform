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

package de.iip_ecosphere.platform.monitoring.prometheus;

import de.iip_ecosphere.platform.monitoring.prometheus.PrometheusMonitoringSetup.PrometheusSetup;
import de.iip_ecosphere.platform.support.LifecycleDescriptor;
import de.iip_ecosphere.platform.support.LifecycleProfile;

/**
 * A lifecycle profile to start only prometheus standalone.
 * 
 * @author Holger Eichelberger, SSE
 */
public class PrometheusLifecycleProfile implements LifecycleProfile {

    @Override
    public boolean test(Class<? extends LifecycleDescriptor> desc) {
        return desc == PrometheusLifecycleDescriptor.class;
    }

    @Override
    public String getName() {
        return "prometheus";
    }

    @Override
    public void initialize(String[] args) {
        PrometheusSetup setup = PrometheusMonitoringSetup.getInstance().getPrometheus();
        // we shall start this, reset
        setup.getServer().setRunning(false); 
        // but not those
        setup.getExporter().setRunning(true); 
        setup.getAlertMgr().setRunning(true); 
    }

}
