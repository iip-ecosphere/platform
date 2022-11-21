# IIP-Ecosphere platform: additional tools

This part contains development tools for
* [Installing](Install/README.md) the IIP-Ecosphere platform through Maven.
* [MvnCentral](Install/README.md) for deploying  platform components to Maven Central upon a release.
* Template projects for setting up an application: [impl.model](impl.model/README.md) for setting up a separated configuration model and [impl.impl](impl.impl/README.md) for implementing the services. With the advent of generated implementation templates that will be located in the `gen` folder of a model-project, we recommend using the more specific templates rather than `impl.impl`.
* Template project for implementing new platform components: [basicMaven](basicMaven/README.md).
* Maven build plugins for Python: [tools.maven.python](tools.maven.python/README.md).
* Extended Maven build plugins for dependencies (delete for unpack): [tools.maven.dependencies](tools.maven.dependencies/README.md).