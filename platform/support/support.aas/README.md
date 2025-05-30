# Basic support and AAS abstraction Component in the Support Layer of the oktoflow platform

## Asset Administration Shell (AAS)

Asset Administration Shell (AAS) abstraction to ease use of AAS in oktoflow and to make the integration of AAS implementations more flexible/stable. For now, we not aim to be complete here rather than useful for the the project at the actual point in time. Also provides explicit support for distributed/deferred building of AAS parts.

For now, this component also contains the most basic utility classes of the oktoflow platform. Might be that we factor them out in a future version, but so far it is just a single class
and its test.

We apply the following principles (with links also in the package description of ``de.iip_ecosphere.platform.support.aas``:
 - Representation instances like ``Aas`` or ``SubModel`` are wrappers for the implementing instances (of an external library).
 - Representation instances are created through builders (following the builder pattern). Mandatory information shall be stated as far as possible during the creation of the builder. Optional information can be given through further builder methods. The builder may warn/enforce consistency depending on the underlying implementation. Finally, a ``build`` call creates the representation instance and - if applicable - registers it with the parent builder/representation instance.
 - The ``AasFactory`` provides top-level access, in particular to the ``AasBuilder``. Actual implementations such as for BaSyx are realized in own projects and hook themselves into via ``AasFactoryDescriptor`` and the Java Service Loader. So far, the factory just takes the "first" implementation. No resolution is implemented if multiple implementations are there (we will think about that when the case occurs).
 - Implementation-specific parts like VAB-based remote method accesses are (so far) not represented by the abstraction rather than provided by the specific implementation. However, the output of these supporting/creation methods shall plugin into the builder mechanism explained above.
 - Identifiers like URNs are stated as strings and, if adequate, shall be parsed by the respective implementation. Expected 
 - Optional TLS encryption based on keystores can be set up through respective methods of the setup descriptor/component setup. With a BaSyx implementation backend, the AAS registry will not be encrypted, while the AAS will be encrypted.
 - Agreed and standardized AAS structures can be integrated into the AAS frontend. One example is the product nameplate (see [ZVEI specification](https://www.zvei.org/fileadmin/user_upload/Presse_und_Medien/Publikationen/2020/Dezember/Submodel_Templates_of_the_Asset_Administration_Shell/201117_I40_ZVEI_SG2_Submodel_Spec_ZVEI_Technical_Data_Version_1_1.pdf)).
 - Optional RBAC authentication, specific integration so far fo AAS, submodels, properties and operations
 
## Identity Support

This component also introduces basic identity support, i.e., identity tokens and security certificates. The realization is extensible, i.e., we provide a simple basic mechanism for local YAML files, but more complex mechanisms may be added as JSL extensions.

The structure of an identity YAML file (must be resolvable as `identityStore-ipr.yml` (quietly), `identityStore.yml` or as fallback `identityStore-test.yml` if the original one shall not be committed on the classpath or for development also in `src/main/resource` or `src/test/resources`) is

    name: <String>
    identities:
        <String>: 
            type: USERNAME
            userName: <String>
            tokenData: <String>
            tokenEncryptionAlgorithm: <String>
        <String>:
            type: ANONYMOUS
        <String>:
            type: X509
            tokenPolicyId: <String>
            signatureAlgorithm: <String>
            signature: <String>
            tokenData: <String>
        <String>:
            type: ISSUED
            tokenPolicyId: <String>
            signatureAlgorithm: <String>
            signature: <String>
            tokenData: <String>
            tokenEncryptionAlgorithm: <String>
        <String>:
            type: USERNAME
            tokenData: <String>
            tokenEncryptionAlgorithm: <String>
            file: <String>

The identity mechanism currently supports 4 different token types, namely USERNAME (and password), ISSUED, X509 and ANONYMOUS. The required entries are illustrated above, while the first entries below `identities` denote the respective identity keys and must be unique within this file. The `tokenEncryptionAlgorithm` for discouraged USERNAME tokens is usually `UTF-8`. The identity keys can be used in respective configuration elements of the configuration model while in background the identity mechanism is queried for the token. One specific case is the last entry, a USERNAME token with attached `file` (may be a file or an URI) representing a keystore to be opened with `tokenData` as password. The `name` of an identity store is optional, but recommended to better identify, whether the intended store has been loaded.
 
**Missing**
- specific AAS concepts
- AAS Events (currently occurring in BaSyx)

**Open questions**
- How to resolve references to their target?
