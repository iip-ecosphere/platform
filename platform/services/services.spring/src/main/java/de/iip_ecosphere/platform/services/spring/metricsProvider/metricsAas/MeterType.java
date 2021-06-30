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

package de.iip_ecosphere.platform.services.spring.metricsProvider.metricsAas;

/**
 * Enum used to specify the type of Meter.<br>
 * This enum is used to aid the
 * {@link MetricsAasConstructor#addCustomMetric(MetricsAasConstructionBundle, String, MeterType)}
 * method to create the correct type of custom metric that the user wants to
 * create.<br>
 * There are three types of Metric:
 * <ul>
 * <li>Counters</li>
 * <li>Gauges</li>
 * <li>Timers</li>
 * </ul>
 * 
 * @author Miguel Gomez
 */
public enum MeterType {

    COUNTER, GAUGE, TIMER;

}
