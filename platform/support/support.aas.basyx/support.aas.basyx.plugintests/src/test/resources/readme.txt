Alias must be "tomcat"

keytool -genkey -alias tomcat -keystore keystore.jks -validity 3650 -storepass a1234567 -keypass a1234567 -keyalg rsa -keysize 4096 -storetype jks -ext "SAN:c=DNS:localhost,IP:127.0.0.1"
