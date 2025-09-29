# oktoflow platform: Logging plugin for slf4j simple

Logging plugin for slf4j simple. Configuration files may be loaded via the classloader hierarchy as usual. Can be loaded as plugin or used as JSL component (direct dependency, e.g. for testing). 

For testing, we typically rely on the fallback logger to avoid dependency conflicts that usually disappear through plugins, i.e., isolated class loading. Besides directly using this plugin, there is also in some cases the way to use an already existing implementation, e.g., of Spring. Then, the JSL integration in a plattform implementation component (not the platform code) via POM as shown below can be the more appropriate way:0


```
    <dependency>
      <artifactId>support.log-slf4j-simple</artifactId>
      <groupId>de.iip-ecosphere.platform</groupId>
      <scope>test</scope>
      <version>${project.version}</version>
      <exclusions>
        <exclusion>
          <groupId>org.slf4j</groupId>
          <artifactId>slf4j-api</artifactId>
        </exclusion>
        <exclusion>
          <groupId>org.slf4j</groupId>
          <artifactId>slf4j-simple</artifactId>
        </exclusion>
      </exclusions>
    </dependency> 
```