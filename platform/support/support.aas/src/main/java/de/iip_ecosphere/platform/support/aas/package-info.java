/**
 * The IIP-Ecosphere AAS abstraction. Does not aim to be complete rather than useful for the actual point in time. 
 * 
 * We apply the following principles:<ul>
 *   <li>Representation instances like {@link de.iip_ecosphere.platform.support.aas.Aas} or 
 *       {@link de.iip_ecosphere.platform.support.aas.SubModel} are wrappers for the implementing instances (of an 
 *       external library).</li>
 *   <li>Representation instances are created through builders (following the builder pattern). Mandatory information
 *       shall be stated as far as possible during the creation of the builder. Optional information can be given
 *       through further builder methods. The builder may warn/enforce consistency depending on the underlying 
 *       implementation. Finally, a "build" call creates the representation instance and - if applicable - registers
 *       it with the parent builder/representation instance.</li>
 *   <li>The {@link de.iip_ecosphere.platform.support.aas.AasFactory} provides top-level access, in particular to the 
 *       {@link de.iip_ecosphere.platform.support.aas.Aas.AasBuilder}. Actual implementations such as for BaSyx are
 *       realized in own projects and hook themselves into via 
 *       {@link de.iip_ecosphere.platform.support.aas.AasFactoryDescriptor} and the Java Service Loader. So far, the
 *       factory just takes the "first" implementation. No resolution is implemented if multiple implementations
 *       are there (we will think about that when the case occurs).
 *   <li>Implementation-specific parts like VAB-based remote method accesses are (so far) not represented by the
 *       abstraction rather than provided by the specific implementation. However, the output of these 
 *       supporting/creation methods shall plugin into the builder mechanism explained above.</li>
 * </ul>
 */
package de.iip_ecosphere.platform.support.aas;