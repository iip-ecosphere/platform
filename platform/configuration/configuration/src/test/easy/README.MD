Test configuration models

- `common` contains re-usable technical definitions. Mounted by testing-code.
- `routingTest`, `simpleMesh` and `simpleMesh3` are test models in managed configuration model format (extended for re-use in distributed test/development environment as `AllTypes` and `AllServices` contain wildcard import to further file and only `simpleMesh` contains app/mesh files with names as written by the platform (for separation among models in test/development environment).
- `single` contains single configuration models, i.e., not in managed configuration model format