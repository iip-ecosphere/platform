# AAS abstraction Component in the Support Layer of the IIP-Ecosphere platform

Asset Administration Shell (AAS) abstraction to ease use of AAS in IIP-Ecosphere and to make the integration
of AAS implementations more flexible/stable. For now, we not aim to be complete here rather than useful for the the 
project at the actual point in time. Also provides explicit support for distributed/deferred building of AAS parts.

For now, this component also contains the most basic utility classes of the 
IIP-Ecosphere platform. Might be that we factor them out in a future version, but so far it is just a single class
and its test.

We apply the following principles (with links also in the package description of ``de.iip_ecosphere.platform.support.aas``:
 - Representation instances like ``Aas`` or ``SubModel`` are wrappers for the implementing instances (of an external library).
 - Representation instances are created through builders (following the builder pattern). Mandatory information shall be stated as far as possible during the creation of the builder. Optional information can be given through further builder methods. The builder may warn/enforce consistency depending on the underlying implementation. Finally, a ``build`` call creates the representation instance and - if applicable - registers it with the parent builder/representation instance.
 - The ``AasFactory`` provides top-level access, in particular to the ``AasBuilder``. Actual implementations such as for BaSyx are realized in own projects and hook themselves into via ``AasFactoryDescriptor`` and the Java Service Loader. So far, the factory just takes the "first" implementation. No resolution is implemented if multiple implementations are there (we will think about that when the case occurs).
 - Implementation-specific parts like VAB-based remote method accesses are (so far) not represented by the abstraction rather than provided by the specific implementation. However, the output of these supporting/creation methods shall plugin into the builder mechanism explained above.
 
**Missing**
- Various AAS concepts (AAS, sub-model, sub-model elements, properties [with semantic ids] and operations do exist; others will be added incrementally on demand)
- Further deployment abstractions (remote)
- Authentication/Security
- AAS Events (currently occurring in BaSyx)

**Open questions**
- How to resolve references to their target?
- How to correctly store an AAS so that the AASX also can read them back?
- How to define semantic references, e.g., to eClass.
