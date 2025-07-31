# Connectors Component for files extension in the Transport Layer of the oktoflow platform

File-based connector, e.g., for JSON or CSV files. stored in an own line. It can be loaded as plugin or used as JSL component (direct dependency, e.g. for testing). The connector can

* read single files, all files from a directory or, based on a Java regular expression, selected files from a given directory (specific connector setting ``READ_FILES``). Multiple files are sorted by their path/name before opening/ingesting data. Files that cannot be opened as files are considered as (classpath) resources, i.e., may be loaded from the packaged binary of an app.
* write to dedicated files or created files within a specified directory (specific connector setting ``WRITE_FILES``)
* simulate a fixed time difference between subsequently received data points (specific connector setting ``DATA_TIMEDIFF``)
* adjust the time difference between subsequently received data points based on the ``DataTimeDifferenceProvider``, superseding specific connector setting ``DATA_TIMEDIFF``

File formats can be specified as usual through data serializers or by wrapping the ``InputParser``/``OutputFormatter`` into serializers as done in generated code.