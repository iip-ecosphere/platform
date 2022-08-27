# IIP-Ecosphere platform: SemanticId resolver for ECLASS

Initial semantic id resolver for the Eclass webservice. As the Eclass webservice is not free, a certificate and an 
account are required. However, we cannot share our login information, i.e., we cannot fully test this resolver.

The resolver relies on the IdentityStore of the IIP-Ecosphere platform and obtains the user authentication token and 
the keystore/certificate from there. By default, the identity key for the keystore is `eclassCert`.

When you received a certificate, you can use it as follows:

* Place the certificate file (`pfx` extension) into a folder reachable for class loading and resource resolution, for testing, e.g., `resources.ipr`. 
* Define an identity store to take up your account and your certificate (with the keystore password above):

    identities:
        "eclassCert": 
            type: USERNAME
            userName: <IRRELEVANT>
            tokenData: <KEYSTORE PASSWORD>
            tokenEncryptionAlgorithm: UTF-8
            file: resources.ipr/cert.full.pfx

`resources.ipr/cert.full.pfx` is just an example, the username is irrelevant, the keystore password depends on the received certificate and may be empty. The keystore key name can be changed via the environment setting `iip.eclass.keystoreKey`. The default language for querying the Eclass API can be set in terms of a locale given in the environment settings (`iip.eclass.locale`, default is your system locale).
 
The Eclass semantic resolver is an optional extension of the IIP-Ecosphere platform.

