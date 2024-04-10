project IDTA_02011_HierarchicalStructures {

  import AASDataTypes;

  annotate BindingTime bindingTime = BindingTime::compile to .;

  AasEnumType ArcheType = {
    name = "ArcheType",
    description = "ArcheType of the Submodel, there are three allowed.",
    literals = {
      AasEnumLiteral {
        name = "Full"
      },
      AasEnumLiteral {
        name = "OneDown"
      },
      AasEnumLiteral {
        name = "OneUp"
      }
    }
  };

  AasSubmodelType HierarchicalStructures = {
    name = "HierarchicalStructures",
    semanticId = "iri:https://admin-shell.io/idta/HierarchicalStructures/1/0/Submodel",
    description = "The Submodel HierarchicalStructures identified by its semanticId. The Submodel idShort can be picked freely.",
    fields = {
      AasField {
        name = "EntryNode",
        semanticId = "iri:https://admin-shell.io/idta/HierarchicalStructures/EntryNode/1/0",
        type = refBy(EntryNode),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Base entry point for the Entity tree in this Submodel, this must be a Self-managed Entity reflecting the Assets administrated in the AAS this Submodel is part of."
      },
      AasField {
        name = "ArcheType",
        semanticId = "iri:https://admin-shell.io/idta/HierarchicalStructures/ArcheType/1/0",
        type = refBy(ArcheType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "ArcheType of the Submodel, there are three allowed."
      }
    }
  };

  AasEntityType EntryNode = {
    name = "EntryNode",
    semanticId = "iri:https://admin-shell.io/idta/HierarchicalStructures/EntryNode/1/0",
    description = "Base entry point for the Entity tree in this Submodel, this must be a Self-managed Entity reflecting the Assets administrated in the AAS this Submodel is part of.",
    fields = {
      AasField {
        name = "Node",
        semanticId = "iri:https://admin-shell.io/idta/HierarchicalStructures/Node/1/0",
        type = refBy(Node),
        minimumInstances = 1,
        description = "Base entry point for the Entity tree in this Submodel, this must be a Self-managed Entity reflecting the Assets administrated in the Asset Administration Shell this Submodel is part of. The idShort of the EntryNode can be picked freely and may reflect a name of the asset."
      },
      AasField {
        name = "SameAs",
        semanticId = "iri:https://admin-shell.io/idta/HierarchicalStructures/SameAs/1/0",
        type = refBy(AasRelationType),
        minimumInstances = 0,
        description = "Reference between two Entities in the same Submodel or across Submodels."
      },
      AasField {
        name = "IsPartOf",
        semanticId = "iri:https://admin-shell.io/idta/HierarchicalStructures/IsPartOf/1/0",
        type = refBy(AasRelationType),
        minimumInstances = 0,
        description = "Modeling of logical connections between asset and sub-asset. Either this or 'HasPart' must be used, not both."
      },
      AasField {
        name = "HasPart",
        semanticId = "iri:https://admin-shell.io/idta/HierarchicalStructures/HasPart/1/0",
        type = refBy(AasRelationType),
        minimumInstances = 0,
        description = "Modeling of logical connections between components and sub-components. Either this or 'IsPartOf' must be used, not both."
      }
    }
  };

  AasEntityType Node = {
    name = "Node",
    semanticId = "iri:https://admin-shell.io/idta/HierarchicalStructures/Node/1/0",
    description = "Base entry point for the Entity tree in this Submodel, this must be a Self-managed Entity reflecting the Assets administrated in the Asset Administration Shell this Submodel is part of. The idShort of the EntryNode can be picked freely and may reflect a name of the asset.",
    fields = {
      AasField {
        name = "Node",
        semanticId = "iri:https://admin-shell.io/idta/HierarchicalStructures/Node/1/0",
        type = refBy(Node),
        minimumInstances = 0,
        description = "Can be a Co-managed or Self-managed entity. A Node reflects an element in the hierarchical model is set into relation with one or more defined relations. The name of a node can be picked freely but it must be unique in its hierarchical (sub-)level."
      },
      AasField {
        name = "SameAs",
        semanticId = "iri:https://admin-shell.io/idta/HierarchicalStructures/SameAs/1/0",
        type = refBy(AasRelationType),
        minimumInstances = 0,
        description = "Reference between two Entities in the same Submodel or across Submodels."
      },
      AasField {
        name = "IsPartOf",
        semanticId = "iri:https://admin-shell.io/idta/HierarchicalStructures/IsPartOf/1/0",
        type = refBy(AasRelationType),
        minimumInstances = 0,
        description = "Modeling of logical connections between components and sub-components. Either this or 'HasPart' must be used, not both."
      },
      AasField {
        name = "HasPart",
        semanticId = "iri:https://admin-shell.io/idta/HierarchicalStructures/HasPart/1/0",
        type = refBy(AasRelationType),
        minimumInstances = 0,
        description = "Modeling of logical connections between components and sub-components. Either this or 'IsPartOf' must be used, not both."
      },
      AasField {
        name = "BulkCount",
        semanticId = "iri:https://admin-shell.io/idta/HierarchicalStructures/BulkCount/1/0",
        type = refBy(UnsignedInteger64Type),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "To be used if bulk components are referenced, e.g., a 10x M4x30 screw."
      }
    }
  };

  freeze {
    .;
  } but (f|f.bindingTime >= BindingTime.runtimeMon);
}
