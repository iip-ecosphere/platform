identityStore.yml contains identity tokens to run the application. Identity information shall not be in the model. 
The platform offers an interface to utilize more secure and advanced mechanisms than just a YAML file ;) This file is
authoritative, it will be copied by the build process into resources/software.

Generating the keys:
  - Use JDK 8 (for compatibility, keystores of newer JDK versions cannot be read on JDK 8)
  - keytool -genkeypair -alias device -keyalg RSA -keysize 2048 -validity 1480 -storetype PKCS12 -keystore k2.p12 -storepass 123456
  - enter storepass to identityStore.yml
  - export (under Linux) to PEM format: openssl pkcs12 -in k2.p12 -out k2.pem
  - Upload key into PLC next
  - Specify key in IVML configuration to be used for the OPC UA connector, along with keystore key from identityStore.yml
  - Specify PLC user as own token in identityStore.yml, use that store key in IVML configuration to identify identity for OPC UA connector.