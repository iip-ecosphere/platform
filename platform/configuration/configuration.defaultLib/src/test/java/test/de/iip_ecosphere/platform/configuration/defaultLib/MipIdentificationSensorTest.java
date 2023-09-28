/**
 * ******************************************************************************
 * Copyright (c) {2023} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.configuration.defaultLib;

import java.util.Date;
import java.util.Timer;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.configuration.defaultLib.MipIdentificationSensor;
import de.iip_ecosphere.platform.configuration.defaultLib.MipIdentificationSensor.MipTimerTask;
import de.iip_ecosphere.platform.services.environment.DataIngestor;
import de.iip_ecosphere.platform.services.environment.DataIngestors;
import de.iip_ecosphere.platform.support.TimeUtils;
import iip.datatypes.MipMqttInput;

/**
 * Tests {@link MipIdentificationSensor}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class MipIdentificationSensorTest {
    
    /**
     * Tests {@link MipIdentificationSensor}.
     */
    @Test
    public void testMip() {
        String date = MipIdentificationSensor.formatDate(new Date());
        Assert.assertNotNull(date);
        Assert.assertTrue(date.length() > 0);
        Assert.assertNotNull(MipIdentificationSensor.createStartStopCommand(true, "ID"));
        
        AtomicInteger count = new AtomicInteger();
        DataIngestor<MipMqttInput> ingestor = d -> {
            count.incrementAndGet();
        };
        DataIngestors<MipMqttInput> ingestors = new DataIngestors<>();
        ingestors.attachIngestor(ingestor);
        MipTimerTask task = new MipTimerTask(false, "ID", ingestors);
        Timer timer = new Timer();
        timer.schedule(task, 500L);
        TimeUtils.sleep(1000);
        Assert.assertTrue(count.get() > 0);
        timer.cancel();
    }
    
}
