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
 - Identifiers like URNs are stated as strings and, if adequate, shall be parsed by the respective implementation. Expected 
 - Optional TLS encryption based on keystores can be set up through respective methods of the factory. With a BaSyx implementation backend, the AAS registry will not be encrypted, while the AAS will be encrypted.
 - Agreed and standardized AAS structures can be integrated into the AAS frontend. One example is the product nameplate (see [ZVEI specification](https://www.zvei.org/fileadmin/user_upload/Presse_und_Medien/Publikationen/2020/Dezember/Submodel_Templates_of_the_Asset_Administration_Shell/201117_I40_ZVEI_SG2_Submodel_Spec_ZVEI_Technical_Data_Version_1_1.pdf)).
 
**Missing**
- Various AAS concepts (AAS, sub-model, sub-model elements, properties [with semantic ids] and operations do exist; others will be added incrementally on demand)
- Further deployment abstractions (remote)
- Authentication/RBAC
- AAS Events (currently occurring in BaSyx)

**Open questions**
- How to resolve references to their target?
- How to define semantic references, e.g., to eClass.
