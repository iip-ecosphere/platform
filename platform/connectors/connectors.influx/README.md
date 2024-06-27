# Connectors Component INFLUX extension in the Transport Layer of the oktoflow platform

INFLUX database connector for bi-directional access to external timeseries data. We run the tests without AAS factory installed in order to simplify testing against a mocked INFLUX DB client. If required, additionally also an AAS server according to the ``AasPartRegistry`` must be initiated. Manually tested against Influx2 version 2.7.6.

The DB URL is composed from the respective information of the connector parameters, i.e., schema, host, port and endpoint path.

The state of this connector is initial but promising. It still needs testing against a real INFLUX DB.
Besides connection and authentication data, a connector parameters object passed in on connection must further specify the following specific parameters:
- ORG: the organization 
- BUCKET: the source/target bucket in the database
- MEASUREMENT: the logical name of the data/objects to be written
- TAGS: the field names to be used as tags, may be empty, a single name or a list of comma-separated name
- BATCH: the size of a batch for batched writing of multiple cached data points, by default 1

For now, INFLUX specific StringTriggerQueries or more abstract SimpleTimeSeriesQueries are processed. Query results must be monotonic ascending in the time field of the data points. Results are streamed either based on time differences of the data points in the database or - with higher priority - a fixed delay given by the query.