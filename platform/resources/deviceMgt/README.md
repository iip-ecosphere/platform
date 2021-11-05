# IIP-Ecosphere platform: central device management

Component to perform/integrate the central AAS-based device management.

## Setup

To run any code, it is important that to setup the environment first. For example a dependency of deviceMgt is at least one minio (port 9001) instance and a running thingsboard (port 8080) instance.
One must provide the credentials in the storage configuration part inside the iipecosphere.yml

A configuration can look like the following example:

```yaml
runtimeStorage:
  endpoint: "http://localhost:9001"
  accessKey: "minio_access_key"
  secretAccessKey: "minio_secret_access_key"
  bucket: "storage"
  prefix: "runtimes/"
  packageDescriptor: "runtime.yml"
  packageFilename: "runtime.zip"

configStorage:
  endpoint: "http://localhost:9001"
  accessKey: "minio_access_key"
  secretAccessKey: "minio_secret_access_key"
  bucket: "storage"
  prefix: "configs/"
  packageDescriptor: "config.yml"
  packageFilename: "config.zip"
```

In this case the deviceMgt is configured to use a local installed minio as its S3 storage storage provider for both (runtime and config) storages. It is also possible to have separate S3 buckets which will hold the data. 

Please note that neither thingsboard not minio are part of this basic device management component, i.e., respective extensions must be linked through JSL.
