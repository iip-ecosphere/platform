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

package de.iip_ecosphere.platform.support.aas.types.timeSeriesData;

import static de.iip_ecosphere.platform.support.aas.IdentifierType.*;
import static de.iip_ecosphere.platform.support.aas.types.common.Utils.*;

import javax.xml.datatype.XMLGregorianCalendar;

import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.Type;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.BlobDataElement.BlobDataElementBuilder;
import de.iip_ecosphere.platform.support.aas.FileDataElement.FileDataElementBuilder;
import de.iip_ecosphere.platform.support.aas.LangString;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection.SubmodelElementCollectionBuilder;
import de.iip_ecosphere.platform.support.aas.types.common.DelegatingSubmodelBuilder;
import de.iip_ecosphere.platform.support.aas.types.common.DelegatingSubmodelElementCollectionBuilder;
import de.iip_ecosphere.platform.support.aas.types.common.Utils;

/**
 * A timeseries data builder as support for <a href="https://industrialdigitaltwin.org/en/wp-content/uploads/
 * sites/2/2023/03/IDTA-02008-1-1_Submodel_TimeSeriesData.pdf">IDTA 02008-1-1 Time Series Data</a>.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TimeSeriesBuilder extends DelegatingSubmodelBuilder {
    
    private int metadataCount = 0;
    private int segmentsCount = 0;
    private boolean createMultiLanguageProperties;

    /**
     * Creates a timeseries builder.
     * 
     * @param aasBuilder the parent AAS
     * @param identifier the submodel identifier
     * @param createMultiLanguageProperties whether multi-language properties shall be created, taints compliance 
     *     if {@code false}
     */
    public TimeSeriesBuilder(AasBuilder aasBuilder, String identifier, boolean createMultiLanguageProperties) {
        super(aasBuilder.createSubmodelBuilder("TimeSeries", identifier));
        this.createMultiLanguageProperties = createMultiLanguageProperties;
        setSemanticId(iri("https://admin-shell.io/idta/TimeSeries/1/1"));
    }
    
    /**
     * Creates a metadata builder.
     * 
     * @return the builder
     */
    public MetadataBuilder createMetadataBuilder() {
        metadataCount++;
        return new MetadataBuilder(getDelegate());
    }

    /**
     * Creates a segments builder.
     * 
     * @return the builder
     */
    public SegmentsBuilder createSegmentsBuilder() {
        segmentsCount++;
        return new SegmentsBuilder(getDelegate());
    }

    /**
     * A builder for timeseries metadata.
     * 
     * @author Holger Eichelberger, SSE
     */
    public class MetadataBuilder extends DelegatingSubmodelElementCollectionBuilder {

        private boolean hasName = false;
        
        /**
         * Creates a metadata builder.
         * 
         * @param smBuilder the parent submodel builder
         */
        private MetadataBuilder(SubmodelBuilder smBuilder) {
            super(smBuilder.createSubmodelElementCollectionBuilder("Metadata", false, false));
            setSemanticId(iri("https://admin-shell.io/idta/TimeSeries/Metadata/1/1"));
        }
        
        /**
         * Sets the name.
         * 
         * @param name a meaningful name(s) for labeling
         * @return <b>this</b>
         */
        public MetadataBuilder setName(LangString... name) {
            hasName = true;
            createMultiLanguageProperty(getParentBuilder(), createMultiLanguageProperties, "Name", 
                iri("https://admin-shell.io/idta/TimeSeries/Metadata/Name/1/1"), name);
            return this;
        }
        
        /**
         * Sets the description.
         * 
         * @param description short description(s) of the time series
         * @return <b>this</b>
         */
        public MetadataBuilder setDescription(LangString... description) {
            createMultiLanguageProperty(getParentBuilder(), createMultiLanguageProperties, "Description", 
                iri("https://admin-shell.io/idta/TimeSeries/Metadata/Description/1/1"), description);
            return this;
        }
        
        /**
         * Creates a record builder.
         * 
         * @return the record builder
         */
        public RecordBuilder createRecordBuilder() {
            return new RecordBuilder(getDelegate());
        }
        
        @Override
        public SubmodelElementCollection build() {
            assertThat(hasName, "Must have a name");
            return super.build();
        }
        
    }

    /**
     * Some none-exclusive time-units mentioned in the specification.
     * 
     * @author Holger Eichelberger, SSE
     */
    public enum TimeUnit {
        
        UTC_TIME(iri("https://admin-shell.io/idta/TimeSeries/UtcTime/1/1")),
        TAI_TIME(iri("https://admin-shell.io/idta/TimeSeries/TaiTime/1/1")),
        RELATIVE_POINT_IN_TIME(iri("https://admin-shell.io/idta/TimeSeries/RelativePointInTime/1/1")),
        RELATIVE_TIME_DURATION(iri("https://admin-\r\nshell.io/idta/TimeSeries/RelativeTimeDuration/1/1"));
        
        private String semanticId;
        
        /**
         * Creates a time unit constant.
         * 
         * @param semanticId the semanticId
         */
        private TimeUnit(String semanticId) {
            this.semanticId = semanticId;
        }
        
        /**
         * Returns the semanticId.
         * 
         * @return the semanticId, usually an IRI or IRDI
         */
        public String getSemanticId() {
            return semanticId;
        }
    }
    
    /**
     * Builder for the record structure.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class RecordBuilder extends DelegatingSubmodelElementCollectionBuilder {
        
        private int timeCount;
        
        /**
         * Creates a metadata builder.
         * 
         * @param smBuilder the parent submodel builder
         */
        private RecordBuilder(SubmodelElementCollectionBuilder smBuilder) {
            super(smBuilder.createSubmodelElementCollectionBuilder("Record", false, false));
            setSemanticId(iri("https://admin-shell.io/idta/TimeSeries/Record/1/1"));
        }
        
        /**
         * Adds a timestamp.
         * 
         * @param semanticId the semantic id, e.g., from {@link TimeUnit}
         * @param value the value
         * @return <b>this</b>
         * @see #addTime(String, Type, Object)
         */
        public RecordBuilder addTime(String semanticId, String value) {
            return addTime(semanticId, Type.STRING, value);
        }

        /**
         * Adds a timestamp.
         * 
         * @param semanticId the semantic id, e.g., from {@link TimeUnit}
         * @param value the value
         * @return <b>this</b>
         * @see #addTime(String, Type, Object)
         */
        public RecordBuilder addTime(String semanticId, int value) {
            return addTime(semanticId, Type.INTEGER, value);
        }

        /**
         * Adds a timestamp.
         * 
         * @param semanticId the semantic id, e.g., from {@link TimeUnit}
         * @param value the value
         * @return <b>this</b>
         * @see #addTime(String, Type, Object)
         */
        public RecordBuilder addTime(String semanticId, long value) {
            return addTime(semanticId, Type.INT64, value);
        }

        /**
         * Adds a timestamp.
         * 
         * @param semanticId the semantic id
         * @param value the value
         * @return <b>this</b>
         * @see #addTime(String, Type, Object)
         */
        public RecordBuilder addTime(String semanticId, XMLGregorianCalendar value) {
            return addTime(semanticId, Type.DATE_TIME, value);
        }

        /**
         * Adds a timestamp.
         * 
         * @param semanticId the semantic id
         * @param type the value type
         * @param value the value
         * @return <b>this</b>
         * @see #addTime(String, Type, Object)
         */
        private RecordBuilder addTime(String semanticId, Type type, Object value) {
            createPropertyBuilder(Utils.getCountingIdShort("Time", ++timeCount))
                .setSemanticId(semanticId)
                .setValue(type, value)
                .build();
            return this;
        }

        // not further specified
        
    }

    /**
     * A builder for the segments structure.
     * 
     * @author Holger Eichelberger, SSE
     */
    public class SegmentsBuilder extends DelegatingSubmodelElementCollectionBuilder {

        private int externalSegmentsCount;
        private int internalSegmentsCount;
        private int linkedSegmentsCount;
        
        /**
         * Creates a segments builder.
         * 
         * @param smBuilder the parent submodel builder
         */
        private SegmentsBuilder(SubmodelBuilder smBuilder) {
            super(smBuilder.createSubmodelElementCollectionBuilder("Segments", false, true));
            setSemanticId(iri("https://admin-shell.io/idta/TimeSeries/Segments/1/1"));
        }
        
        /**
         * Creates an external segment builder.
         * 
         * @return the builder
         */
        public ExternalSegmentBuilder createExternalSegmentBuilder() {
            return new ExternalSegmentBuilder(getDelegate(), ++externalSegmentsCount);
        }

        /**
         * Creates a linked segment builder.
         * 
         * @return the builder
         */
        public LinkedSegmentBuilder createLinkedSegmentBuilder() {
            return new LinkedSegmentBuilder(getDelegate(), ++linkedSegmentsCount);
        }

        /**
         * Creates an internal segment builder.
         * 
         * @return the builder
         */
        public InternalSegmentBuilder createInternalSegmentBuilder() {
            return new InternalSegmentBuilder(getDelegate(), ++internalSegmentsCount);
        }

    }

    /**
     * A builder for external segments.
     * 
     * @author Holger Eichelberger, SSE
     */
    public class ExternalSegmentBuilder extends AbstractSegmentBuilder<ExternalSegmentBuilder> {

        /**
         * Creates a segment builder.
         * 
         * @param smBuilder the parent submodel builder
         * @param segmentNr the segment number
         */
        private ExternalSegmentBuilder(SubmodelElementCollectionBuilder smBuilder, int segmentNr) {
            super(smBuilder, segmentNr, "ExternalSegment", 
                iri("https://admin-shell.io/idta/TimeSeries/Segments/ExternalSegment/1/1"));
        }

        /**
         * Creates a nested file builder.
         * 
         * @param contents the contents of the file
         * @param mimeType the mime type of the contents
         * @return the builder
         */
        public FileDataElementBuilder createFile(String contents, String mimeType) {
            FileDataElementBuilder result = createFileDataElementBuilder("File", contents, mimeType);
            result.setSemanticId(iri("https://admin-shell.io/idta/TimeSeries/File/1/1"));
            return result;
        }
        
        /**
         * Creates a nested blob builder.
         * 
         * @param contents the contents of the blob, may be <b>null</b> for none
         * @param mimeType the mime type of the contents
         * @return the builder
         */
        public BlobDataElementBuilder createBlob(String contents, String mimeType) {
            BlobDataElementBuilder result = createBlobDataElementBuilder("Blob", contents, mimeType);
            result.setSemanticId(iri("https://admin-shell.io/idta/TimeSeries/Blob/1/1"));
            return result;
        }

    }

    /**
     * A builder for linked segments.
     * 
     * @author Holger Eichelberger, SSE
     */
    public class LinkedSegmentBuilder extends AbstractSegmentBuilder<LinkedSegmentBuilder> {

        /**
         * Creates a segment builder.
         * 
         * @param smBuilder the parent submodel builder
         * @param segmentNr the segment number
         */
        private LinkedSegmentBuilder(SubmodelElementCollectionBuilder smBuilder, int segmentNr) {
            super(smBuilder, segmentNr, "LinkedSegment", 
                iri("https://admin-shell.io/idta/TimeSeries/Segments/LinkedSegment/1/1"));
        }
        
        /**
         * Sets the endpoint.
         * 
         * @param endpoint specifies a location of a resource on an API server through which time series can be 
         *     requested
         * @return <b>this</b>
         */
        public LinkedSegmentBuilder setEndpoint(String endpoint) { // we go for String for now
            createPropertyBuilder("Endpoint")
                .setSemanticId(iri("https://admin-shell.io/idta/TimeSeries/Endpoint/1/1"))
                .setValue(Type.STRING, endpoint)
                .build();
            return this;
        }
        
        /**
         * Sets the query.
         * 
         * @param query generic query component to read time series data from an API
         * @return <b>this</b>
         */
        public LinkedSegmentBuilder setQuery(String query) { // we go for String for now
            createPropertyBuilder("Query")
                .setSemanticId(iri("https://admin-shell.io/idta/TimeSeries/Query/1/1"))
                .setValue(Type.STRING, query)
                .build();
            return this;
        }

    }
    
    /**
     * Defines the segment state.
     * 
     * @author Holger Eichelberger, SSE
     */
    public enum SegmentState {
        
        IN_PROGRESS("in progress", iri("https://admin-shell.io/idta/TimeSeries/Segment/State/InProgress/1/1),")),
        COMPLETED("completed", iri("https://admin-shell.io/idta/TimeSeries/Segment/State/Completed/1/1),"));
        
        private String value;
        private String valueId;
        
        /**
         * Creates a constant.
         * 
         * @param value the value
         * @param valueId the semantic id of the value
         */
        private SegmentState(String value, String valueId) {
            this.value = value;
            this.valueId = valueId;
        }

        /**
         * Returns the value.
         * 
         * @return the value
         */
        public String getValue() {
            return value;
        }

        /**
         * Returns the semantic id of the value.
         * 
         * @return the semantic id
         */
        public String getValueId() {
            return valueId;
        }

    }

    /**
     * A builder for internal segments.
     * 
     * @param <T> the actual implementing type
     * 
     * @author Holger Eichelberger, SSE
     */
    public abstract class AbstractSegmentBuilder<T extends AbstractSegmentBuilder<T>> 
        extends DelegatingSubmodelElementCollectionBuilder {

        private T self;
        
        /**
         * Creates a segment builder.
         * 
         * @param smBuilder the parent submodel builder
         * @param segmentNr the segment number
         * @param prefix the idShort prefix
         * @param semanticId the semantic ID
         */
        @SuppressWarnings("unchecked")
        private AbstractSegmentBuilder(SubmodelElementCollectionBuilder smBuilder, int segmentNr, String prefix, 
            String semanticId) {
            super(smBuilder.createSubmodelElementCollectionBuilder(
                getCountingIdShort(prefix, segmentNr), false, true));
            setSemanticId(semanticId);
            self = (T) this;
        }
        
        /**
         * Sets the name.
         * 
         * @param name a meaningful name(s) for labeling
         * @return <b>this</b>
         */
        public T setName(LangString... name) {
            createMultiLanguageProperty(getParentBuilder(), createMultiLanguageProperties, "Name", 
                iri("https://admin-shell.io/idta/TimeSeries/Segment/Name/1/1"), name);
            return self;
        }

        /**
         * Sets the description.
         * 
         * @param description short description of the time series segment
         * @return <b>this</b>
         */
        public T setDescription(LangString... description) {
            createMultiLanguageProperty(getParentBuilder(), createMultiLanguageProperties, "Description", 
                iri("https://admin-shell.io/idta/TimeSeries/Segment/Description/1/1"), description);
            return self;
        }
        
        /**
         * Sets the record count.
         * 
         * @param count indicates how many records are present in a segment
         * @return <b>this</b>
         */
        public T setRecordCount(long count) {
            createPropertyBuilder("RecordCount")
                .setSemanticId(iri("https://admin-shell.io/idta/TimeSeries/Segment/RecordCount/1/1"))
                .setValue(Type.INT64, count)
                .build();
            return self;
        }

        /**
         * Sets the start time.
         * 
         * @param startTime Contains the first recorded timestamp of the time series segment or its start time if 
         * it is a qualitative time series. Time format and scale corresponds to that of the time series.
         * @return <b>this</b>
         */
        public T setStartTime(XMLGregorianCalendar startTime) {
            createPropertyBuilder("StartTime")
                .setSemanticId(iri("https://admin-shell.io/idta/TimeSeries/Segment/StartTime/1/1"))
                .setValue(Type.DATE_TIME_STAMP, startTime)
                .build();
            return self;
        }

        /**
         * Sets the end time.
         * 
         * @param endTime Contains the last recorded timestamp of the time series segment or its start time if 
         * it is a qualitative time series. Time format and scale corresponds to that of the time series.
         * @return <b>this</b>
         */
        public T setEndTime(XMLGregorianCalendar endTime) {
            createPropertyBuilder("EndTime")
                .setSemanticId(iri("https://admin-shell.io/idta/TimeSeries/Segment/EndTime/1/1"))
                .setValue(Type.DATE_TIME_STAMP, endTime)
                .build();
            return self;
        }

        /**
         * Sets the duration.
         * 
         * @param duration Period covered by the segment, represented according to ISO 8601 by the format 
         * P[n]Y[n]M[n]DT[n]H[n]M[n]S
         * @return <b>this</b>
         */
        public T setDuration(String duration) {
            // may check format of duration
            createPropertyBuilder("Duration")
                .setSemanticId(iri("https://admin-shell.io/idta/TimeSeries/Segment/Duration/1/1"))
                .setValue(Type.STRING, duration)
                .build();
            return self;
        }

        /**
         * Sets the duration.
         * 
         * @param duration the duration
         * @param semanticId the semanticID of the value type
         * @return <b>this</b>
         */
        public T setDuration(long duration, String semanticId) {
            createPropertyBuilder("Duration")
                .setSemanticId(semanticId)
                .setValue(Type.INT64, duration)
                .build();
            return self;
        }

        /**
         * Sets the sampling interval.
         * 
         * @param interval the interval, the time period between two time series records (Length of cycle)
         * @param semanticId the semanticID of the value type, may be <b>null</b> for the default from the specification
         *   indicating seconds
         * @return <b>this</b>
         */
        public T setSamplingInterval(long interval, String semanticId) {
            createPropertyBuilder("SamplingInterval")
                .setSemanticId(semanticId == null 
                    ? iri("https://admin-shell.io/idta/TimeSeries/Segment/SamplingInterval/1/1") : semanticId)
                .setValue(Type.INT64, interval)
                .build();
            return self;
        }

        /**
         * Sets the sampling rate.
         * 
         * @param rate the rate, defines the number of samples per second for a regular time series in Hz.
         * @param semanticId the semanticID of the value type, may be <b>null</b> for the default from the specification
         *   indicating Hz
         * @return <b>this</b>
         */
        public T setSamplingRate(long rate, String semanticId) {
            createPropertyBuilder("SamplingRate")
                .setSemanticId(semanticId == null 
                    ? iri("https://admin-shell.io/idta/TimeSeries/Segment/SamplingRate/1/1") : semanticId)
                .setValue(Type.INT64, rate)
                .build();
            return self;
        }

        /**
         * Sets the state.
         * 
         * @param state the state of the time series related to its progress
         * @return <b>this</b>
         */
        public T setState(SegmentState state) {
            createPropertyBuilder("State")
                .setSemanticId(iri("https://admin-shell.io/idta/TimeSeries/Segment/State/1/1"))
                .setValue(Type.STRING, state.getValue())
                .build();
            return self;
        }

        /**
         * Sets the last update.
         * 
         * @param timestamp the time of the last chance
         * @return <b>this</b>
         */
        public T setLastUpdate(XMLGregorianCalendar timestamp) {
            createPropertyBuilder("LastUpdate")
                .setSemanticId(iri("https://admin-shell.io/idta/TimeSeries/Segment/LastUpdate/1/1"))
                .setValue(Type.DATE_TIME_STAMP, timestamp)
                .build();
            return self;
        }

    }

    /**
     * A builder for internal segments.
     * 
     * @author Holger Eichelberger, SSE
     */
    public class InternalSegmentBuilder extends AbstractSegmentBuilder<InternalSegmentBuilder> {

        /**
         * Creates a segment builder.
         * 
         * @param smBuilder the parent submodel builder
         * @param segmentNr the segment number
         */
        private InternalSegmentBuilder(SubmodelElementCollectionBuilder smBuilder, int segmentNr) {
            super(smBuilder, segmentNr, "InternalSegment", 
                iri("https://admin-shell.io/idta/TimeSeries/Segments/InternalSegment/1/1"));
        }

        /**
         * Creates a builder for the records SMC.
         * 
         * @return the builder
         */
        public SubmodelElementCollectionBuilder createRecordsBuilder() {
            return createSubmodelElementCollectionBuilder("Records", true, false)
                .setSemanticId(iri("https://admin-shell.io/idta/TimeSeries/Records/1/1"));
        }

    }

    @Override
    public Submodel build() {
        assertThat(metadataCount == 1, "Must have exactly one metadata SMC");
        assertThat(segmentsCount == 1, "Must have exactly one segments SMC");
        return super.build();
    }

}
