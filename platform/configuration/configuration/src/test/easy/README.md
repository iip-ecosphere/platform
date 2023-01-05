##Test configuration models

These models are partially in (extended) managed platform structure, partial in one simple file. Models in managed platform structure are intended for reuse in the testing/evaluation environment.

- `common` contains re-usable technical definitions. Mounted by testing-code. Not part of the managed platform structure, only for easing the tests here. Models managed by platform will not have this folder as the settings will be in `TechnicalSetup.ivml`.
- `routingTest`, `simpleMesh` and `simpleMesh3` are test models in managed configuration model format (extended for re-use in distributed test/development environment as `AllTypes` and `AllServices` contain wildcard import to further file and only `simpleMesh` contains app/mesh files with names as written by the platform (for separation among models in test/development environment).
- `single` contains single configuration models, i.e., not in managed configuration model format