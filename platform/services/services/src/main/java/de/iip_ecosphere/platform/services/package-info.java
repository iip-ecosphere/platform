/**
 * Interfaces of the service and the service management. Access to the service manager instance is provided
 * via {@link de.iip_ecosphere.platform.services.ServiceFactory} and the actual service manager is loaded
 * via Java Service Loading and {@link de.iip_ecosphere.platform.services.ServiceFactoryDescriptor}. Intentionally, 
 * interfaces are not templates while abstract classes define templates over the actually used types to avoid later 
 * explicit casting.
 */
package de.iip_ecosphere.platform.services;
