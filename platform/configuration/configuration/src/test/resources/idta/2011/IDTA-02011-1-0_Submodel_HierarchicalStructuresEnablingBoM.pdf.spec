project IDTA_02011_HierarchicalStructures {

  version v1.0;

  import AASDataTypes;

  annotate BindingTime bindingTime = BindingTime::compile to .;

  AasEnumType ArcheType = {
    name = "ArcheType",
    description = "ArcheType of the Submodel, there are three allowed.",
    versionIdentifier = "IDTA 02011-1-0",
    semanticId = "iri:https://admin-shell.io/idta/HierarchicalStructures/ArcheType/1/0",
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
    description = "Definition of the Submodel HierarchicalStructures identified by its semanticId. The Submodel idShort can be picked freely.",
    versionIdentifier = "IDTA 02011-1-0",
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
    description = "Base entry point for the Entity tree in this Submodel, this must be a Self-managed Entity reflecting the Assets administrated in the Asset Administration Shell this Submodel is part of. The idShort of the EntryNode can be picked freely and may reflect a name of the asset.",
    versionIdentifier = "IDTA 02011-1-0",
    fields = {
      AasField {
        name = "Node",
        semanticId = "iri:https://admin-shell.io/idta/HierarchicalStructures/Node/1/0",
        type = refBy(Node),
        minimumInstances = 1,
        description = "The Entity Node can be a co-managed or self-managed entity representing an asset in the hierarchical structure."
      },
      AasField {
        name = "SameAs",
        semanticId = "iri:https://admin-shell.io/idta/HierarchicalStructures/SameAs/1/0",
        type = refBy(AasRelationType),
        minimumInstances = 0,
        description = "Reference between two Entities in the same Submodel or across Submodels. First and Second attributes must contain either an EntryNode or a Node."
      },
      AasField {
        name = "IsPartOf",
        semanticId = "iri:https://admin-shell.io/idta/HierarchicalStructures/IsPartOf/1/0",
        type = refBy(AasRelationType),
        minimumInstances = 0,
        description = "First and Second attributes must contain either a EntryNode or a Node. The relationships shall only reference EntryNodes or Nodes in the same Submodel."
      },
      AasField {
        name = "HasPart",
        semanticId = "iri:https://admin-shell.io/idta/HierarchicalStructures/HasPart/1/0",
        type = refBy(AasRelationType),
        minimumInstances = 0,
        description = "Note: The idShort can be chosen freely."
      }
    }
  };

  AasEntityType Node = {
    name = "Node",
    semanticId = "iri:https://admin-shell.io/idta/HierarchicalStructures/Node/1/0",
    description = "Can be a Co-managed or Self-managed entity. A Node reflects an element in the hierarchical model is set into relation with one or more defined relations. The name of a node can be picked freely but it must be unique in its hierarchical (sub-)level.",
    versionIdentifier = "IDTA 02011-1-0",
    fields = {
      AasField {
        name = "Node",
        semanticId = "iri:https://admin-shell.io/idta/HierarchicalStructures/Node/1/0",
        type = refBy(Node),
        minimumInstances = 0,
        description = "The Entity Node can be a co-managed or self-managed entity representing an asset in the hierarchical structure."
      },
      AasField {
        name = "SameAs",
        semanticId = "iri:https://admin-shell.io/idta/HierarchicalStructures/SameAs/1/0",
        type = refBy(AasRelationType),
        minimumInstances = 0,
        description = "Reference between two Entities in the same Submodel or across Submodels. First attribute must contain either an EntryNode or a Node. The Second attribute may contain an Entity element in a different Submodel, including Submodels of a different specification."
      },
      AasField {
        name = "IsPartOf",
        semanticId = "iri:https://admin-shell.io/idta/HierarchicalStructures/IsPartOf/1/0",
        type = refBy(AasRelationType),
        minimumInstances = 0,
        description = "Modeling of logical connections between components and sub-components. Either this or 'HasPart' must be used, not both. First and Second attributes must contain either a EntryNode or a Node. The relationships shall only reference EntryNodes or Nodes in the same Submodel."
      },
      AasField {
        name = "HasPart",
        semanticId = "iri:https://admin-shell.io/idta/HierarchicalStructures/HasPart/1/0",
        type = refBy(AasRelationType),
        minimumInstances = 0,
        description = "Modeling of logical connections between components and sub-components. Either this or 'IsPartOf' must be used, not both. First and Second attributes must contain either a EntryNode or a Node. The relationships shall only reference EntryNodes or Nodes in the same Submodel."
      },
      AasField {
        name = "BulkCount",
        semanticId = "iri:https://admin-shell.io/idta/HierarchicalStructures/BulkCount/1/0",
        type = refBy(UnsignedInteger64Type),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "To be used if bulk components are referenced, e.g., a 10x M4x30 screw. Additional constraint: With bulk count only a reference to an asset with kind type is allowed, e.g., the M4x30 type asset."
      }
    }
  };

  freeze {
    .;
  } but (f|f.bindingTime >= BindingTime.runtimeMon);
}
