# IIP-Ecosphere platform: SemanticId resolver for ECLASS

Initial semantic id resolver for the Eclass webservice. As the Eclass webservice is not free, a certificate and an 
account are required. However, we cannot share our login information, i.e., we cannot fully test this resolver.

The resolver relies on the IdentityStore of the IIP-Ecosphere platform and obtains the user authentication token and 
the keystore/certificate from there. By default, the user authentication token has the identity key `eclassUser`, 
the keystore the identity key `eclassCert`.

When you received a certificate, you can use it as follows:

* Translate it into a Java keystore (please remember the keystore password)
  
    openssl x509 -outform der -in eclass.full.pem -out eclass.der
    keytool -import -alias eclass -keystore eclass.p12 -file eclass.der
    
* Place the `eclass.p12` file into a folder reachable for class loading and resource resolution, for testing, e.g., `resources.ipr`. 
* Define an identity store to take up your account and your certificate (with the keystore password above):

    identities:
        "eclassUser":
            type: USERNAME
            userName: <USER>
            tokenData: <PASSWORD>
            tokenEncryptionAlgorithm: UTF-8
        "eclassCert": 
            type: USERNAME
            userName: <IRRELEVANT>
            tokenData: <KEYSTORE PASSWORD>
            tokenEncryptionAlgorithm: UTF-8
            file: resources.ipr/eclass.p12

 The identity store keys can be changed via the environment settings `iip.eclass.authenticationKey` and `iip.eclass.keystoreKey`. The default language for querying the Eclass API can be set in terms of a locale given 
 in the environment settings (`iip.eclass.locale`, default is your system locale).
 
The Eclass semantic resolver is an optional extension of the IIP-Ecosphere platform. It is in initial/preliminary state, 
because the initial tests ended with a timeout.

