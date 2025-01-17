# oktoflow platform: Configuration Default Library

Basic code for devices or services integrated into the configuration meta-model. This project contains a simple/minimal configuration file to create the code for the data types defined in the platform configuration meta-model. The classes implemented in this project are based on the generated type classes.

The project setup differs a bit from the examples, indicating the future intended setup, namely the configuration meta-model in `target/easy`, the actual configuration in `src/main/easy`. After checkout, the project may show errors, which disappear when the maven build process has been executed. Use `mvn install -Dunpack.force=true` to update the meta model.

Contents:
* Support code for [MIP technology](https://mip-technology.de/) identification reader

The deployable artifacts intentionally do not contain the generated classes as they will be created as part of the interfaces of the application projects.