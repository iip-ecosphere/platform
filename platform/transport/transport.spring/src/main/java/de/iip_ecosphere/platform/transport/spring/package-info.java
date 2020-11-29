/**
 * Reusable Spring support classes. {@link de.iip_ecosphere.platform.transport.spring.StartupApplicationListener} 
 * configures {@link de.iip_ecosphere.platform.transport.serialization.SerializerRegistry} and 
 * {@link de.iip_ecosphere.platform.transport.TransportFactory} from configuration settings in 
 * {@link de.iip_ecosphere.platform.transport.spring.SerializerConfiguration} or 
 * {@link de.iip_ecosphere.platform.transport.spring.TransportFactoryConfiguration}, respectively. 
 * {@link de.iip_ecosphere.platform.transport.spring.BeanHelper} offers methods to add a default 
 * beans to the reachable parent context (shall be used in a binder configurations).
 */
package de.iip_ecosphere.platform.transport.spring;