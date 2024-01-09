/**
 * ******************************************************************************
 * Copyright (c) {2024} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.support.aas;

import java.io.File;

import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.AssetKind;
import de.iip_ecosphere.platform.support.aas.LangString;
import de.iip_ecosphere.platform.support.aas.types.common.Utils;
import de.iip_ecosphere.platform.support.aas.types.timeSeriesData.TimeSeriesBuilder;
import de.iip_ecosphere.platform.support.aas.types.timeSeriesData.TimeSeriesBuilder.SegmentState;
import de.iip_ecosphere.platform.support.aas.types.timeSeriesData.TimeSeriesBuilder.SegmentsBuilder;
import de.iip_ecosphere.platform.support.aas.types.timeSeriesData.TimeSeriesBuilder.TimeUnit;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;

/**
 * Example for timeseries data.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TimeSeriesDataTest extends AbstractAasExample {

    @Override
    protected String getFolderName() {
        return "timeSeries";
    }
    
    @Override
    public File[] getTargetFiles() {
        return new File[] {new File("./output/timeseriesData.aasx")};
    }

    @Override
    protected void createAas() {
        AasBuilder aasBuilder = AasFactory.getInstance().createAasBuilder("TimeseriesExample", 
            "urn:::AAS:::TimeseriesExample#");
        aasBuilder.createAssetBuilder("tsd", "urn:::Asset:::tsd#", AssetKind.INSTANCE).build();
        TimeSeriesBuilder tsdBuilder = new TimeSeriesBuilder(aasBuilder, "urn:::SM:::TimeSeries#", 
            isCreateMultiLanguageProperties());
        tsdBuilder.createMetadataBuilder()
            .setName(new LangString("en", "Example data"))
            .setDescription(new LangString("en", "Example timeseries data"))
            .createRecordBuilder()
                .addTime(TimeUnit.UTC_TIME.getSemanticId(), Utils.parseCalendar("2024-01-01T12:00:00.000+00:00"))
                .build();
        SegmentsBuilder sBuilder = tsdBuilder.createSegmentsBuilder();
        sBuilder.createLinkedSegmentBuilder()
            .setName(new LangString("en", "External data"))
            .setDescription(new LangString("en", "External data"))
            .setState(SegmentState.IN_PROGRESS)
            .setLastUpdate(Utils.parseCalendar("2024-01-01T12:00:00.000+00:00"))
            .setEndpoint("ws:127.0.0.1/data")
            .setQuery("all=true")
            .build();
        sBuilder.build();
        tsdBuilder.build();
        registerAas(aasBuilder);
    }

    @Override
    protected File getThumbnail() {
        return null;
    }
    
}
