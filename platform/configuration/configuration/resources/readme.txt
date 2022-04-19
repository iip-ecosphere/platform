global resources for the generation of a platform
- location defined via property iip.resources
- intentionally not in src/main/resources or src/test/resources as used by generation
- intended for resources that shall not be committed with individual services, e.g., due to size or IPR
- this folder shall not contain any IPR-protected/commercial resources, rather than harmless surrgogates (functional or not)
- if existent, the generation shall take into account the mirror directory resources.ipr that is excluded in .gitignore

devices: resources for devices such as nameplate information (preliminary, -> resources in JAR)
platform: resources for platform components (preliminary, -> resources in JAR)
software: resources for software services (preliminary, -> resources in JAR)