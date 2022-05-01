/**
 * Default Device AAS providers. There are single providers such as {@link de.iip_ecosphere.platform.ecsRuntime
 * .deviceAas.YamlDeviceAasProvider} or {@link de.iip_ecosphere.platform.ecsRuntime.deviceAas.AasxDeviceAasProvider} 
 * as well as multi-providers that return a result of a specified selection of providers, e.g., 
 * {@link de.iip_ecosphere.platform.ecsRuntime.deviceAas.FirstMatchingDeviceAasProvider}.
 * When using a multi-provider, list the multi-provider first in the JLS file, then the single providers to consider.
 */
package de.iip_ecosphere.platform.ecsRuntime.deviceAas;