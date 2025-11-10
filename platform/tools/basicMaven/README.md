# oktoflow platform: Component template

Maven template for implementing components of the oktoflow platform.

Adjust
* `pom.xml` to component name
* `.project` to component name`
* `build-jk.xml` to component name (required for Continuous Integation)
* package names in `src/main/java` and `src/test/java`

Remove
* Dummy classes in `src/main/java` and `src/test/java` (`AllTests.java` required by CI to call tests)

Parent POM:
* Platform core components shall use `platformDependencies`
* Platform implementation components/plugins shall use `platformDependenciesBOM` (bill-of-material)
* Spring-based implementation components may use `platformDependenciesSpring`


