# Test support for HiveMQ (JDK 11)

Test server support code for HiveMq, which requires JDK 11. Thus, this code and its required dependencies can only be utilized as an optional component.

If you use this support class, please make the folder/config file in src/test available to your code, e.g., through copying/adjusting.

In addition to the plaintext configuration, there is also a `secCfg`, which binds a singleton key/cert in a Java keystore/truststore and plaintext password to the MQTT server port to enable TLS. Please note that HiveMQ requires a certain key length (we use 2048) and some shorter key length cause errors in the SSL handshake.
