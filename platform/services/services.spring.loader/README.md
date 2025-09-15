# IIP-Ecosphere platform: App loader for Spring Cloud Stream and ZIP packaging

Extended implementation of spring app loader for isolated class loading.

Testing is a bit complicated as first the required "app" files must be constructed. Thus, tests may fail in Eclipse if not built before with Maven. For updating the "app" files, run "mvn install".

- oktoflow ZIP packaging: all jars in ZIP file, in root folder `classpath` file (java @ rules) listing all relevant jars in sequence
- Spring jar packaging: FAT jar, containing `BOOT-INF/classpath.idx`, app classes in `BOOT-INF/classes`, library JARs uncompressed in `BOOT-INF/lib`
- oktoflow ZIP split packaging: all jars in ZIP file, in root folder `classpath` file (java @ rules) listing all main class loader jars in sequence as well as `classpath-app` with isolated JARs. Must be passed into loader as absolute path via `-Dokto.loader.app=<fullpath>`
- Spring jar packaging: FAT jar, containing `BOOT-INF/classpath.idx` for main class loader (related app classes in `BOOT-INF/classes` and library JARs uncompressed in `BOOT-INF/lib`) as well as `BOOT-INF/classpath-app.idx` for isolated class loader (related app classes in `BOOT-INF/classes-app` and library JARs uncompressed in `BOOT-INF/lib-app`) as well as