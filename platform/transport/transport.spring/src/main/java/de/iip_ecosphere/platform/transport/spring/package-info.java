/**
 * Reusable Spring support classes. {@link de.iip_ecosphere.platform.transport.spring.StartupApplicationListener} 
 * configures {@link de.iip_ecosphere.platform.transport.serialization.SerializerRegistry} and 
 * {@link de.iip_ecosphere.platform.transport.TransportFactory} from configuration settings in 
 * {@link de.iip_ecosphere.platform.transport.spring.SerializerConfiguration} or 
 * {@link de.iip_ecosphere.platform.transport.spring.TransportFactoryConfiguration}, respectively. 
 * {@link de.iip_ecosphere.platform.transport.spring.BeanHelper} offers methods to add a default 
 * beans to the reachable parent context (shall be used in a binder configurations).
 * 
 * Application may require link to one class in here via 
 * {@link org.springframework.boot.autoconfigure.SpringBootApplication#scanBasePackageClasses()}.
 * In some contexts, we experienced that {@code scanBasePackageClasses} on this package disturbs the spring 
 * initialization, in particular spring cloud multi-binder settings. Then the underlying functionality can be utilized 
 * directly through {@link RegistrationHelper} or this class can be subclassed in the application and marked as 
 * component.
 */
package de.iip_ecosphere.platform.transport.spring;