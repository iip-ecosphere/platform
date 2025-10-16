# oktoflow platform: App example with separated model/implementation projects.

consisting of
* [model project](example.templates.model/README.MD) akin to a managed model maintained by the platform.
* [implementation project](example.templates.impl/README.MD) service implementation project based on the template generated from the model.

Tests disabled as scikit-learn changed massively so that used pickled models from Python 3.5/3.9 cannot be loaded anymore.

