## oktoflow application configuration

This documentation summarizes the most important configuration concepts for oktoflow applications:
- [Datatypes](types.MD)
- [Services (including authentication and security, AAS nameplates, service parameters, dependencies, serviers)](services.MD)
- [Connectors](connectors.MD)
- [Apps and Service Meshes](apps.MD)
- [App Templates](appTemplates.MD)
- [Platform](platform.MD)

Further, we provide support for app/service implementation and platform installation.
- [App and Service implementation](implementation.MD)
- [Platform service implementation](platformServices.MD)
- [Platform installation considerations](install.MD)

Application configurations are specified in terms of the [Integrated Variability Modeling Language (IVML)](http://projects.sse.uni-hildesheim.de/easy/docs-git/docPreview/IVML%20Language%20Spec.pdf). At it's core, IVML consists of typed variables, type definitions, constraints and references among variables. For specifying oktoflow apps, we only need a small structural part of IVML. The IVML models then drive the code generation/instantiation.

Briefly summarized, an IVML file used for app configuration looks as follows:

```
project <Name> {

   // imports

   annotate BindingTime bindingTime = BindingTime::compile to .;

   // type and variable declararations
    
   freeze {
      .;
   }

}
```

The top-level structure (`project`) contains all related elements, starting with the import of other IVML files, the declaration of annotations (must be repeated in each file, here when must an IVML variable be bound at latest), type and variable declarations (for data types, services, connectors, applications and meshes) and finally a `freeze` block. As IVML configurations can be layered it is relevant for code generation to know, which information is stable, i.e., `frozen`. Usually, application configuration files end with an all-freeze, expressed by the freeze block and a "." for the whole file (like in operating systems the actual directory).

Managed platform configurations (and most of the oktoflow examples) follow a certain file structure, all files located in `src/main/easy`:
- `AllConstants.ivml` (optional) global contants used by upstream IVML projects
- `AllTypes.ivml` all datatype specifications (see [Datatypes](types.MD))
- `AllServices.ivml` all service specifications using datatypes (see [Services](services.MD))
- `meshes/<MeshName>.ivml` mesh definition linking services to data flows (see [Apps](apps.MD))
- `apps/<AppName>.ivml` app definition linking all meshes of an app together (see [Apps](apps.MD))
- `templates` optional folder containing application template files (see [Apps](apps.MD))
- `TechnicalSetup.ivml` technical settings on where to find AAS servers, whether monitoring shall be started, etc.
- `PlatformConfiguration.ivml` the top-most project gluing all files together via imports

If created via the oktoflow management UI, you will get in touch with the concepts rather than IVML itself. If you wish to work based on examples, as some of our videos show, a bit more configuration/IVML knowledge is helpful. 
