# oktoflow platform: Component template

Maven template for implementing components of the oktoflow platform. Use the parent POM `platformDependencies` only for dependency-free platform core components, while `platformDependenciesBOM` or `platformDependenciesSpring` is adequate for platform implementation components/plugins, see [PlatformHandbook](../../documentation/PlatformHandbook.pdf).

Please consider 

- The name of the new project shall indicate the component name, which shall also be reflected in the java packages for production and testing code as well as in the Maven POM file. 
- The package name shall start with `de.oktoflow.platform`. In justified special cases, the legacy name `de.iip_ecosphere.platform` may be used. The Maven groupId shall be taken from the parent POM and, thus, stay with IIP-ecosphere.
- The name of the test package shall start with ``test``, unless too many methods have to be declared public to be accessible to tests, the package may directly start with `de`` to allow for package access. 
- Packages shall be documented (`package-info.java`).
- The main test suite is `AllTests.java` in the test package (only tests declared there will be executed). Further test suites, i.e., tests to be executed in individual JVM processes, can be stated in numbered variants of the main test suite, e.g., `AllTests1.java`. Also they will be found and executed during the build process.
- The component is by default developed for JDK 17 as specified as system library and compiler settings in the Maven POM. Where applicable rely on the platform’s JDK version, i.e., the smallest possible JDK version for compatibility.
- The component template ships with a checkstyle setup taking the style information from the `platformDependencies` project. For parent POMs, see above.Please consider, that various support plugins with helpful functionalities do exist and shall be used over re-including similar dependencies.
- There is a default `.gitignore` file that excludes the `target` folder. Please ensure that only needed files, i.e., not generated/obtained files during the build process, are committed. 
- The `build-jk.xml` file is needed for the continuous integration (Jenkins, therefore `-jk``). This file refers to further ANT imports containing the settings for Jenkins as well as macros for Maven execution and deployment. As these files are in other repositories, it may be that your IDE issues errors about missing files. You can ignore these errors. In the ANT file, change the name of the project as well as the pattern for the files to be taken from the Maven target folder for Maven deployment. In specific cases, further adjustments are needed here. 
- pom.xml is the Maven build specification. Usually, it declares the platform dependencies as parent, defines only its artifactId (taking over the IIP-Ecosphere group), its deployment form, name, description and dependencies. The build plugins are inherited from the parent. In some cases, e.g., for obtaining/unpacking specific artifacts like the configuration model further steps can be added, which usually extend the existing build setup.
- The `README.md` is the readable technical documentation of this component. It shall briefly explain the aim of the component, its setup (Yaml structure), its specific requirements/limitations but also actual issues and problems. Please keep this file up to date. Upon first commit, this file shall be linked into the parent GitHub folder `README.md` (see the platform layering for selecting a proper folder) as well as in the overview `README.md` file of the platform by HTML links.
- You may remove the dummy classes in `src/main/java` and `src/test/java`, except for `AllTests.java`.

Parent POM:
* Platform core components shall use `platformDependencies`
* Platform implementation components/plugins shall use `platformDependenciesBOM` (bill-of-material)
* Spring-based implementation components may use `platformDependenciesSpring`


