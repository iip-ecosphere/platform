/**
 * Tests for the spring-based implementation of the service manager. This folder contains some basic tests that can be 
 * executed independently of the plugin character of this project. However, real execution tests can only be conducted
 * in an environment with a clean root class loader (only platform layers, dependencies only via plugins). This is not
 * possible here and happens in the subproject services.spring.plugintests (based on the simple test application in 
 * test.simpleStream.spring). The actual tests are implemented here, provided through the SpringTestProviderDescriptor
 * to services.spring.plugintests and executed there.
 */
package test.de.iip_ecosphere.platform.services.spring;
