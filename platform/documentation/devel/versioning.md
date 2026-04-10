# Artifact Coordinates and Versioning

Versioning of platform artifacts is an important aspect to identify parts and pieces that can be operated together. In this document, we briefly explain the versioning that is applied by oktoflow and that also defines how oktoflow apps and services are specified.

## Artifact Coordinates

An **artifact** is essentially a file with contents that is valuable for the platform or the apps. An artifact may contain source code, binary code, documentation, tests, but also model files or reusable resources like images or AI models.

Artifacts are identified by **artifact coordinates**. As we use the Maven build system for building platform artifacts, artifact coordinates are defined by Maven. Although Maven focuses on Java, we also use it to transport and deploy Python source code and AI models. A Maven artifact coordinate consists of
- a **group identifier**, a namespace typically indicating the origin of the artifact, the system it belongs to etc. For oktoflow, the typical group identifier is `de.iip-ecosphere.platform` which still indicates its origin, the IIP-Ecosphere project. Over time, we plan to migrate the group identifiers of the platform to `de.oktoflow.platform`.
- an **artifiact identifier** uniquely naming the artifact within the namespace formed by the the group identifier.
- a **version**, e.g., `0.8.0` or `0.7.1-SNAPSHOT`. A snapshot version indicates an artifact in development, i.e., it can be overridden at any time, e.g., by our continuous integration. If a version is not suffixed by `-SNAPSHOT` it is a release version that is not supposed to change anymore.
To obtain a full coordinate, the parts are joined by colons in the sequence given above.

>An example for an artifact coordinate is `de.iip-ecosphere.platform:support:0.7.1-SNAPSHOT`

The Maven artifact coordinate system also allows for specifying (file) types or classifiers (e.g., `binary`, `plugin`, `easy`) indicating sub-artifacts. This information is implicitly used by oktoflow and usually not relevant to the user.

## Origin of Artifacts

Artifacts are created when a build process is executed. This may be locally on your computer, e.g., for debugging, or, usually during a platform build (then created by our continuous integration server). During a platform build, artifacts, in particular SNAPSHOTS, are deployed to the SSE Maven repository so that they become available. As the SSE Maven repository is not an offical repository of the Maven ecosystem, we need to explicitly defined it in build process specifications.

When the platform state becomes stable, we perform from time to time a release. This requires changing the version of the artifacts from snapshot to release, re-building the artifacts so that they become available on the SSE Maven repository and, in an extra step, deploy relevant artifacts to the Maven Central server (then they become globally available even without naming the SSE Maven repository). As a final step, we tag the source code in github with a version tag can create a release there and re-build the platform with a new, subsequent snapshot version.

The overall artifact build process also explains why locally build snapshot artifacts may suddenly be replaced by a different snapshot: When you have built a snapshot artifact locally and any other build process that requires that artifact is executed with update enabled (the default), it may override it by a more recent version built by our continuous integration. If you plan to work for a longer period with local snapshots, either disable updates ((`-o` flag) or changing the global Maven settings) or keep an eye on the updated artifact and re-build your local artifacts when needed.

## Artifact Coordinates in Apps

For our convenience, the example apps of oktflow use a sub-namespace of the platform namespace (usually `de.iip-ecosphere.platform.apps`) as group identifier as well as the same version as the platform, either through the Maven variable `${project.version}` or, in IVML, through the variable `iipVer`.

Maven coordinates for apps and services are specified in IVML and taken up for code generation and creation of application code templates. While the initial build of an application excludes all dependencies, i.e. ignores their Maven coordinates, app integration builds need the service implementations and thus must consider the Maven coordinates.

By default, the artifact identifier of an app is composed from the app name (spaces and difficult characters removed) using the actual platform version. The artifact identifier of an application code template is composed from the artifact identifier of the application postfixed by `Services` also using the actual platform version.

For your apps, it may initially be convenient to use the same convention, i.e., the IVML specification of your services shall point to the artifact coordinate of the application code artifact. However, there is no obligation to do so. While platform-supplied services will ship with the platform group identifier/namespace, in particular your service implementations or app may be branded by your preferred group identifier. This then requires that you specify the Maven coordinate of your services (each service may be in an individual artifact) accordingly, e.g., to adjust the Maven coordinate of the build specification that you took over from the app code template.

## Platform Releases

From time to time we release a new version of the platform. How does this impact your applications?

Initially there is no impact as we keep the older versions online. However, you will not receive any updates of platform coordinates, but this may be fine. If you want to change the version of your platform, there are two steps to be done
- For the platform, obtain an installation package for the new version and re-install the platform.
- For apps, adjust the version number of the platform in the build specifications of your services and your app. Usually, you will find the platform version in the `<parent>` element of the build specification.
