# oktoflow app configuration: Deployment Plans

Applications running on the oktoflow typically run distributed and, thus, need a deployment plan. Deployment plans can be stated directly in ![Yaml](../../platform) or modeled in IVML and turned into a deployment plan through the platform/application instantiation. We focus here on the IVML structure.

## Deployment Plan

Fields of the 'DeploymentPlan' type:

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

The constraints of the deployment plan validate whether only services of the referenced application are stated and the service mesh is fully covered, that ensemble services do not form loops, whether parameter services and parameter service names are unique and whether plan ids are unique.

## Service Assignments

A service assignment indicates services to be treated by a deployment plan.

Fields of the abstract 'ServiceAssignment' type:

| Field |  Description | Default |
| ---   |  --- | --- |
|services | Sequence of references to `MeshElement` | |

One concrete type refining 'ServiceAssignment' is 'NamedServiceAssignment' addressing the target resource by its name/id. Additional fields are:

| Field |  Description | Default |
| ---   |  --- | --- |
|resource | Required non-empty name/id of the target resource | |

## Ensemble Specifications

Ensembles indicate that stated services shall be executed within the same JVM process rather than in individual JVM processes per service. 

Fields of the 'EnsembleSpec' type:

| Field |  Description | Default |
| ---   |  --- | --- |
| leader | Reference to the `MeshElement` that shall be the ensemble leader, the service that is primarily started in the process. | |
| member | Reference to the `MeshElement` that shall be started in the same process as the ensemble leader. ||

## Server Assignments

A server assignment indicates the servers to be started/stopped within the lifecycle of the deployment plan.

Fields of the abstract 'ServerAssignment' type:

| Field |  Description | Default |
| ---   |  --- | --- |
| server | Reference to the `Server` that is addressed. | |

A concrete refining type is the 'NamedServerAssignment' type. Additional fields are:

| Field |  Description | Default |
| ---   |  --- | --- |
| hostId | Required non-empty name/id of the host/resource where the server shall be running. | |

## Parameter Service Assignments

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