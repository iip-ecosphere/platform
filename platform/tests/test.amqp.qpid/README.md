# Test support for Apache QPid

Test server support code for QPid (AMQP).

If you use this support class, please make the config file in src/test available to your code, e.g., through copying/adjusting.

In addition to the plaintext configuration, there is also a `secCfg`, which binds a Java keystore (RSA, JKS format as requested by QPid) with a certificate with alias `qpid` and plaintext password to the AMQP server port to enable TLS.

