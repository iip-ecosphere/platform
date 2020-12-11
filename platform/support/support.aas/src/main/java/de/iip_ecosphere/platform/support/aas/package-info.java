/**
 * The IIP-Ecosphere AAS abstraction. Does not aim to be complete rather than useful for the actual point in time. 
 * 
 * We apply the following principles:<ul>
 *   <li>Representation instances like {@link de.iip_ecosphere.platform.support.aas.Aas} or 
 *       {@link de.iip_ecosphere.platform.support.aas.Submodel} are instances for wrappers shielding the implementing 
 *       instances (of an external library).</li>
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
 *   <li>We focus on interfaces here as a basic implementation may create too many limitations on wrapping the 
 *       underlying implementation. If it appears feasible, we can pull up parts of the fake implementation from the 
 *       tests as basic implementation.</li>
 *   <li>If you are unsure how things go together or how the abstraction shall be used, please look into the 
 *       {@code PrintVisitorTest} in the test part for a simple AAS/use of the visitor. If you are more interested 
 *       in implementing wrappers, please look into the fake implementation for testing. It does not really do much 
 *       but shows the structure and where it is recommended to use the more specific wrapper types.</li>    
 * </ul>
 */
package de.iip_ecosphere.platform.support.aas;