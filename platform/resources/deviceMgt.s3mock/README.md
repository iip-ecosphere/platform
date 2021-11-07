# IIP-Ecosphere platform: central device management (S3Mock S3 storage extension)

This component provides a S3Mock (MIT license) S3 storage implementation for the device management. S3mock is not a full S3 server, e.g., it accepts any authentication. Although this is not feasible for a production setup, this component is helpful for test setups, e.g., if no external S3 server shall be installed. A ``storageServer`` setup is considered and brings up either an in-memory or a file-based S3mock instance.

