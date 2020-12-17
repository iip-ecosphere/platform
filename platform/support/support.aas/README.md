# AAS abstraction Component in the Support Layer of the IIP-Ecosphere platform

Asset Administration Shell (AAS) abstraction to ease use of AAS in IIP-Ecosphere and to make the integration
of AAS implementations more flexible/stable. For now, we not aim to be complete here rather than useful for the the 
project at the actual point in time. 

For now, this component also contains the most basic utility classes of the 
IIP-Ecosphere platform. Might be that we factor them out in a future version, but so far it is just a single class
and its test.

We apply the following principles (with links also in the package description of ``de.iip_ecosphere.platform.support.aas``:
 - Representation instances like ``Aas`` or ``SubModel`` are wrappers for the implementing instances (of an external library).
 - Representation instances are created through builders (following the builder pattern). Mandatory information shall be stated as far as possible during the creation of the builder. Optional information can be given through further builder methods. The builder may warn/enforce consistency depending on the underlying implementation. Finally, a ``build`` call creates the representation instance and - if applicable - registers it with the parent builder/representation instance.
 - The ``AasFactory`` provides top-level access, in particular to the ``AasBuilder``. Actual implementations such as for BaSyx are realized in own projects and hook themselves into via ``AasFactoryDescriptor`` and the Java Service Loader. So far, the factory just takes the "first" implementation. No resolution is implemented if multiple implementations are there (we will think about that when the case occurs).
 - Implementation-specific parts like VAB-based remote method accesses are (so far) not represented by the abstraction rather than provided by the specific implementation. However, the output of these supporting/creation methods shall plugin into the builder mechanism explained above.
 
** Issues **
 
For now, there seem to be problems adding sub-models to an existing AAS at runtime. The created sub-model exists, but 
it is not taken over into the deployed version while this works well for sub-model elements. If the name of the 
sub-model is known at startup time, it is one workaround to create an empty sub-model upfront and to just insert the 
elements later.

Moreover, setting values of dynamically created properties seems to cause problems, as the server responds that the
respective element does not exist (while it can read it).

We will investigate whether these issues arise from the abstraction or from the underlying implementation. May be, an
update to a more recent state of BaSyx could resolve these problems.

**Missing**
- Various AAS concepts (AAS, sub-model, sub-model elements, properties and operations do exist; the others will be added incrementally on demand)
- Further deployment abstractions (remote, sub-model)
- Authentication/Security
- AAS Events (currently occurring in BaSyx)
