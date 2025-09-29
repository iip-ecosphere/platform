# oktoflow platform: metrics  plugin through micrometer

Metrics implementing plugin for micrometer. Can be loaded as plugin or used as JSL component (direct dependency, e.g. for testing).

Besides directly using this plugin, there is also in some cases the way to use an already existing implementation, e.g., of Spring. Then, the JSL integration in a plattform implementation component (not the platform code) via POM as shown below can be the more appropriate way:0

```
    <dependency>
      <artifactId>support.metrics-micrometer</artifactId>
      <groupId>de.iip-ecosphere.platform</groupId>
      <version>${project.version}</version>
      <exclusions>
        <exclusion>
          <groupId>io.micrometer</groupId>
          <artifactId>micrometer-core</artifactId>
        </exclusion>
      </exclusions>
    </dependency> 
```