/**
 * Some mocking classes for flow testing. Cannot be in test folder as must be available to service integration.
 * Mocking classes shall have the same name (just different package), same constructors (delegating to super) and 
 * just overwrite some funtionalities to be as close as possible to the production code. As the configuration will
 * just replace the package names, all Java service classes shall be re-defined with inheritance in here. 
 */
package de.iip_ecosphere.platform.examples.hm23.mock;