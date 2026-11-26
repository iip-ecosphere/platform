# oktoflow app configuration: Deployment Plans

Applications running on the oktoflow typically run distributed and, thus, need a deployment plan. Applications that shall be started/stopped from the management user interface require a deployment plan, even if the deployment is "local" on the same device.

Deployment plans can be specified in three forms that complement each other:
* In the application model, i.e., in IVML. Such deployment plans can also be created and maintained via the management user interface. Deployment plans in the application model are bound to that application and can be shared along with the application, which is particularly important for application templates so that an instantiated application in the best case directly becomes executable, at least on a local device. However, adjustments regarding the specific distribution to multiple devices may be needed as the specific setup in an application context is not known upfront. The application code generation turns modeled deployment plans into YAML files on the file system (see below).
* Upload a YAML file through the UI, which saves it into the configured `artifacts` folder of oktoflow. The platform takes these files up when they occur in the `artifacts` folder and makes them available via the user interface. In contrast to modeled deployment plans, these files currently can not be modified through the user interface and they also do not become automatically available with an application (template).
* As YAML file that is copied manually into the configured `artifacts` folder of oktoflow. Same as upload a YAML file, these files currently can not be modified through the user interface and they also do not become automatically available with an application (template).

We encourage using modeled deployment plans although, in particular for testing, experiments and temporary purposes, authored YAML files may be more easily usable and more agile.

## Modeled Deployment Plan

Fields of the `DeploymentPlan` type:

| Field |  Description | Default |
| ---   |  --- | --- |
| name  | Required non-empty (descriptive) name of the plan. | |
| application | Reference to the application to be deployed with this plan ||
| id | Required non-empty unique deplyoment plan identifier ||
| ver | Version of the plan | `"0.0.0"` |
| description | Textual description of the purpose of the plan ||
| assignments | Sequence of assignments, see `ServiceAssignment` below | `{}` |
| ensembles | Sequence of ensembles, i.e., services to be executed in the same process, see `EnsembleSpec` below |  |
| parallelize | Shall the plan execution run in parallel or sequential. | `false` | 
| onUndeployRemoveArtifact | Remove the application artifact from the service manager when stopping/undeploying the app. | `false` | 
| allowMultiExecution | Is the plan a singleton, i.e., does it start a single application or is a repeated multi-execution possible, then starting individual instances of the app. | `true` | 
| disabled | Is the plan disabled, i.e., cannot be started, e.g., through the management UI. | `false` | 
| cmdArg | Comma-separated application arguments as String |  | 
| server | Sequence of server assignments, see `ServerAssignment` below |  | 
| parameter | Sequence of service assignment parameters, see `ParameterServiceAssignment` below |  | 
| ensembleStrategy | Strategy for automatically determining ensembles | `MANUAL`, `ALL_ON_FIRST` | |

The constraints of the deployment plan validate whether only services of the referenced application are stated and the service mesh is fully covered, that ensemble services do not form loops, whether parameter services and parameter service names are unique and whether plan ids are unique.

### Service Assignments

A service assignment indicates services to be treated by a deployment plan.

Fields of the abstract 'ServiceAssignment' type:

| Field |  Description | Default |
| ---   |  --- | --- |
|services | Sequence of references to `MeshElement` | |

One concrete type refining 'ServiceAssignment' is 'NamedServiceAssignment' addressing the target resource by its name/id. Additional fields are:

| Field |  Description | Default |
| ---   |  --- | --- |
|resource | Required non-empty name/id of the target resource | |

### Ensemble Specifications

Ensembles indicate that stated services shall be executed within the same JVM process rather than in individual JVM processes per service. 

Fields of the 'EnsembleSpec' type:

| Field |  Description | Default |
| ---   |  --- | --- |
| leader | Reference to the `MeshElement` that shall be the ensemble leader, the service that is primarily started in the process. | |
| member | Reference to the `MeshElement` that shall be started in the same process as the ensemble leader. ||

Specifying ensembles can be tedious and in most cases it is fully sufficient if the first specified service in `services` becomes the respective ensemble leader. This can be enabled by the `ensembleStrategy` `ALL_ON_FIRST`. By default, the `ensembleStrategy` is `MANUAL` and if no `ensembles` are given, services are started in individual processes. Please note, in particular with `ALL_ON_FIRST` that depending on your application and input data, it might be required to start sinks, transformers and sources in sequence, i.e., put the sources last in your service lists. 

### Server Assignments

A server assignment indicates the servers to be started/stopped within the lifecycle of the deployment plan.

Fields of the abstract 'ServerAssignment' type:

| Field |  Description | Default |
| ---   |  --- | --- |
| server | Reference to the `Server` that is addressed. | |

A concrete refining type is the 'NamedServerAssignment' type. Additional fields are:

| Field |  Description | Default |
| ---   |  --- | --- |
| hostId | Required non-empty name/id of the host/resource where the server shall be running. | |

### Parameter Service Assignments

Structured and more abstracted way of assigning service parameters on application startup. Can also be realized via `cmdArg` in the assignment plan.

Fields of the 'ParameterServiceAssignment' type:

| Field |  Description | Default |
| ---   |  --- | --- |
| service | Reference to the `Server` that is addressed. | |
| parameter | Set of `ParameterAssignment` types. | |

Fields of the abstract 'ParameterAssignment' type:

| Field |  Description | Default |
| ---   |  --- | --- |
| parameter | Required non-empty name of the parameter | |

Additional fields of the 'StringParameterAssignment' type refining `ParameterAssignment`:

| Field |  Description | Default |
| ---   |  --- | --- |
| value | String value to assign to the parameter | |

Additional fields of the 'IntegerParameterAssignment' type refining `ParameterAssignment`:

| Field |  Description | Default |
| ---   |  --- | --- |
| value | Integer value to assign to the parameter | |

## YAML Format of Deployment Plans

As said above, deployment plans can be stated directly in ![YAML](../../platform). The YAML format follows the model structure described above (or vice versa), but, in addition, specifies the `artifact` to be executed. 
* In case of a modeled deployment plan, this information is derived by oktoflow and placed automatically into the deployment plan (akin to the application artifact, for which the generated build process is enabled to copy the executable artifact into the `artifacts` folder). 
* In case of a manually written deployment plan, the `artifact` information (as well as the artifact) must be provided by the author of the deployment plan. The following rules apply:
  - If the artifact is given as an absolute path name, exactly that path is used when executing the application through the deployment plan. However, absolute paths are discouraged as they are usually not portable across platform installations.
  - If the artifact is specified as a relative path, first (for legacy reasons) the path is turned into an absolute path based on the platform installation location. If such a file exists, it is used for execution. If not, the path of the configured `artifacts` directory is prepended. If that path is absolute, it is used as it is; if it is not absolute, the path is turned into an absolute path based on the platform installation folder. If the latter does not exist, the platform goes on with the relative path as specified although the file potentially does not exist and deployment may fail.

