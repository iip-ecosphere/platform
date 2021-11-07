# IIP-Ecosphere platform: central device management

Component to perform/integrate the central AAS-based device management.

## Setup

To run any code, it is important that to setup the environment first. For example a dependency of deviceMgt is at least one minio (port 9001) instance and a running thingsboard (port 8080) instance.
One must provide the credentials in the storage configuration part inside the iipecosphere.yml

A configuration can look like the following example:

```yaml
runtimeStorage:
  endpoint: <String>
  region: <String>
  accessKey: <String>
  secretAccessKey: <String>
  bucket: <String>
  prefix: <String>
  packageDescriptor: <String>
  packageFilename: <String>

configStorage:
  endpoint: <String>
  region: <String>
  accessKey: <String>
  secretAccessKey: <String>
  bucket: <String>
  prefix: <String>
  packageDescriptor: <String>
  packageFilename: <String>
  
storageServer:
  port: <Integer>
  path: <String>
  accessKey: <String>
  secretAccessKey: <String>
```
There are two storage setup parts, one for the storage of the runtimes, one for the device setup and one optional server setup part. 

* Both storage parts be on the same ``endpoint`` (URL string) in the same ``region`` (depends on cloud provider, may be empty for local installations, defaults to empty), an access key, a secret access key, the target ``bucket`` within the storage, and a prefix path. Further, default descriptor/file names may be given that are appended to a device specific path infix in order to create a uniform storage layout.
* The server part is optional and only considered if the respective storage extension ships with a server. Alternatively, a cloud server or a locally installed S3 server component may be used. If given, the ``storageServer`` setup consists of a ``port``, an optional ``path`` (may be empty for in-memory or temporary storage), an access key and a secret access key as used for the storages. 

A configuration can look like the following example:

```yaml
runtimeStorage:
  endpoint: <String>"http://localhost:9001"
  region: 
  accessKey: "minio_access_key"
  secretAccessKey: "minio_secret_access_key"
  bucket: "storage"
  prefix: "runtimes/"
  packageDescriptor: "runtime.yml"
  packageFilename: "runtime.zip"

configStorage:
  endpoint: "http://localhost:9001"
  region: 
  accessKey: "minio_access_key"
  secretAccessKey: "minio_secret_access_key"
  bucket: "storage"
  prefix: "configs/"
  packageDescriptor: "config.yml"
  packageFilename: "config.zip"
  
```

In this case the deviceMgt is configured to use a local installed minio as its S3 storage storage provider for both (runtime and config) storages. It is also possible to have separate S3 buckets which will hold the data. No storage server configuration is given, no server will be started.

Please note that neither thingsboard not minio are part of this basic device management component, i.e., respective extensions must be linked through JSL.
