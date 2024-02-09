# Connectors Component for serial protocols in the Transport Layer of the oktoflow platform

Serial interface for bi-directional access to external data sources and devices. 

Testing under Windows requires a tool vor bridging virtual com ports, e.g., HDD Virtual Serial Port Tools. Testing under Linux requires an installed ``socat``.

Accepts the following specific settings:
 * BAUDRATE: Integer (default 9600)
 * DATABITS: Integer (default 8)
 * STOPBITS: Integer (default 1)
 * PARITY: "NO", "EVEN", "ODD", "MARK", "SPACE" (default "NO")

**Issues/Worth considering**
