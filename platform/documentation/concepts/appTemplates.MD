# oktoflow app configuration: App Templates

While oktoflow examples typically realize full applications (as all-in-one-examples), it might be difficult to start off app modeling with a fully equipped application. App templates try to address this issue by allowing "applications with intended gaps". This templating concept is new and part of the ongoing work in the [ReGaP innovation community](https://regap.de).

* An app template is an oktoflow application for which not all required fields may be filled. Thus, an app template cannot be instantiated as an usual application configuration. In contrast, the management UI must identify the gaps and explicitly ask the user how to fill these gaps prior to turning the template into an app on behalf of the user. The resulting app must then comply with all configuration rules of oktoflow's application metamodel.
* Following the managed configuration structure, apps reside as single IVML projects (name prefix `TemplatePart`) in the optional `templates` folder. Templates may contain constants, data types (including dashboard information), services, connectors, service meshes and application instances.
* In particular, data types may overlap between different templates; if two data types have the same name, only one is taken over into the platform configuration.
* All services used in a template app shall be platform services, i.e., shipped with the platform, preferrably generic platform services that can be operated with arbitrary data (possibly detailed by additional configuration information).
* Simple gaps are fields that are not filled out or configured in a conflicting way. Alternative services can be represented by service/connector families. 
* If specialized data types are needed, applications created from a template can defined data type substitutions which are taken into account during app instantiation. To be effective, service implementations must then use the generic ways to create instances of data types or to transfer data among data type instances.

During template instantiation, the definitions from a single template file are extracted and transferred into the usual structure of a managed configuration. Currently, application and service mesh elements are renamed (to remain unique), while services, data types and constants are taken over for reuse. This is an initial approach that may prove practical or may require changes in the future. The template itself is not modified. 

As initial validation/example, we equipped the `SimpleMesh` testing app in `configuration.easy` with a simple template. Further concepts or support operations may be needed; their design and realization depend on the evaluation of the app template concept.

### Type substitutions

Type substitutions allows customizing an application derived from a template. When specified in an `Application`, types defined in the application can be substituted during instantiation by refined types. However, this requires that services are implemented in a more generic manner, i.e., output type instances are created through factory methods (then only the instantiated services know the type substituations) and, as input types may also be substituted, taking over input values from input types shall be done through generic copy operations provided by the service base implementations of the platform.

| Field |  Description | Default |
| --- |  --- | --- |
| fromType | Reference to the `DataType` that shall be substituted | |
| toType | Reference to the `DataType` that shall substitute `fromType` | |

### App identity stores

Moreover, app templates may already prescribe identity, authentication and security information, preferrably as gaps, so that the concrete information, e.g., identity tokens must be entered by the user upon app template instantiation. Although by the introduction of identity stores we aim at avoiding plain passwords and identity tokens in configuration models, we tolerate them in this case. If specified, the application instantiation will create an identity store from the following configuration structure of type `AppIdentityStore`:

| Field |  Description | Default |
| --- |  --- | --- |
|includeInApplication | Whether the identity store shall be included in integrated application (required for app templates) or just for code templates. | |
|identities | sequence of `AppIdentityStoreEntry` | |

An `AppIdentityStoreEntry` is an abstract type with the following fields:

| Field |  Description | Default |
| --- |  --- | --- |
|name | Required non-empty name/identification of the entry. Services or connectors may refer to this name in their authentication/security configuration  | |

A `UserPwAppIdentityStoreEntry` is a refined `AppIdentityStoreEntry` with the following fields:

| Field |  Description | Default |
| --- |  --- | --- |
|userName| Required non-empty name of the user account  | |
|password| Required non-empty password or identity token | |
