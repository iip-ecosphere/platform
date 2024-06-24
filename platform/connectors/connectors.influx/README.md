# Connectors Component INFLUX extension in the Transport Layer of the oktoflow platform

INFLUX database connector for bi-directional access to external timeseries data. We run the tests without AAS factory installed in order to simplify testing against a mocked INFLUX DB client. If required, additionally also an AAS server according to the ``AasPartRegistry`` must be initiated.

The state of this connector is initial but promising. It still needs testing against a real INFLUX DB.
Besides connection and authentication data, a connector parameters object passed in on connection must further specify the following specific parameters:
- ORG: the organization 
- BUCKET: the source/target bucket in the database
- MEASUREMENT: the logical name of the data/objects to be written
- TAGS the field names to be used as tags, may be empty, a single name or a list of comma-separated name

For now, only simple, INFLUX specific StringTriggerQueries are processed. Results are streamed either based on time differences of the data points in the database or - with higher priority - a fixed delay given by the query.