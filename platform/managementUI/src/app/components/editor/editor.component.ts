import { Component, Input, OnInit } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';
import { ApiService } from 'src/app/services/api.service';
import { primitiveDataTypes } from 'src/app/services/env-config.service';
import { IvmlFormatterService } from 'src/app/services/ivml-formatter.service';
import { Resource, uiGroup, editorInput, configMetaContainer, configMetaEntry, ResourceAttribute, InputVariable } from 'src/interfaces';

@Component({
  selector: 'app-editor',
  templateUrl: './editor.component.html',
  styleUrls: ['./editor.component.scss']
})
export class EditorComponent implements OnInit {

  //type to generate subeditor for, null if this editor instance is not a subeditor
  @Input() type: editorInput | null = null;

  //for generating dropdown options of abstract type
  @Input() refinedTypes: ResourceAttribute[] | null = null;


  category: string = 'all';
  meta: Resource | undefined;
  /* backup needed for data recovery before re-filtering data
   for the next tab */
  metaBackup: Resource | undefined;
  selectedType: Resource | undefined;

  uiGroups: uiGroup[] = [];

  showDropdown = true;
  showInputs = true;

  variableName = '';

  metaTypes = ['metaState', 'metaProject',
    'metaSize', 'metaType', 'metaRefines', 'metaAbstract', 'metaTypeKind'];

  //metatypekind: PRIMITIVE=1, ENUM=2, CONTAINER=3, CONSTRAINT=4, DERIVED=9, COMPOUND=10

  /* metaTypeKind */
  primitive = 1

  ivmlType:string = "";

  /* metaRef list:
  empty -> returns ivml types
  includes names of toplevel types -> returns the toplevel type (if not abstract) and all subtypes
  */
  reqTypes = [
    {cat: "Constants", metaRef: []},
    {cat: "Types", metaRef: ["RecordType", "ArrayType"]},
    {cat: "Dependencies", metaRef: ["Dependency"]},
    {cat: "Nameplates", metaRef: ["NameplateInfo"]},
    {cat: "Services", metaRef: ["Service"]},
    {cat: "Servers", metaRef: ["Server"]},
    {cat: "Meshes", metaRef: ["ServiceMesh"]},
    {cat: "Applications", metaRef: ["Application"]}
  ];

  constructor(private api: ApiService,
    public dialog: MatDialogRef<EditorComponent>,
    public ivmlFormatter: IvmlFormatterService) { }

  ngOnInit(): void {
    console.log("refinedTypes " + this.refinedTypes)
    console.log("type: " + this.type)
    console.log("selectedType: " + this.selectedType)
    if(this.refinedTypes) {
      console.log(this.refinedTypes);
      this.meta = {
        idShort: 'meta',
        value: this.refinedTypes
      }
    } else if(!this.type) {
      console.log("get meta")
      this.getMeta()
    } else if(this.metaBackup && this.metaBackup.value && this.type.type){
      let type = this.cleanTypeName(this.type.type);
      this.selectedType = this.metaBackup.value.find(item => item.idShort === type);
      this.generateInputs()
    }
    console.log(this.metaBackup);
    if(this.metaBackup && this.metaBackup.value) {
      let searchTerm = 'Field'
      for(const type of this.metaBackup.value) {
        const refined = type.value.find((item: { idShort: string; }) => item.idShort === 'metaRefines');
        if(refined && refined.value != '') {
          if(searchTerm === refined.value) {
            console.log(type);
          }
        }
      }
    }
  }

  private async getMeta() {
    this.meta = await this.api.getMeta();
    this.metaBackup = JSON.parse(JSON.stringify(this.meta)); // deep copy
    this.filterMeta();
  }

  /**
   * Documentation in src/assets/doc/filterMeta.jpg
   */
  public filterMeta() {
    this.meta = JSON.parse(JSON.stringify(this.metaBackup)) // recovering meta from deep copy
    let filter = this.reqTypes.find(type => type.cat === this.category)
    let newMetaValues = []
    if (this.meta && this.meta.value) {
      for (const item of this.meta.value) {
        let idShort = ""
        if(item.idShort) {
          idShort = item.idShort
        }

        if(!this.isAbstract(item)) {
          if(filter?.metaRef.includes(idShort)) {
            newMetaValues.push(item)
          }

          if (this.getMetaRef(item)) {
            let metaRefVal = item.value.find(
              (val: { idShort: string; }) => val.idShort === "metaRefines").value
            if(metaRefVal != "") {
              // sub-type
              if(filter?.metaRef.includes(metaRefVal)) {
                // direct inheritance
                newMetaValues.push(item)
              } else {
                // indirect inheritance (recursion)
                if (this.isSubtype(metaRefVal)) {
                  newMetaValues.push(item)
                }
              }
            } else {
              // toplevel type
              if(filter?.metaRef.includes(idShort)) {
                newMetaValues.push(item)
              }
            }
          } else {
            // ivml types
            if (this.isTypeMetaKindEqualNum(item, this.primitive)
                  && filter?.metaRef.length == 0) {
              newMetaValues.push(item)
            }
          }
        }
      }
    }
    this.meta!.value = newMetaValues

    // single item
    if (newMetaValues.length == 1) {
      this.setInputForSingleItem(newMetaValues[0])
      this.showDropdown = false
    }
  }

  public setInputForSingleItem(item: any) {
    this.selectedType = item
    this.generateInputs()
  }

  /** Returns false when metaAbstract is false or
   * there is no attribute "metaAbstract" */
  private isAbstract(item:any) {
    let abstract = item.value.find(
      (val: { idShort: string; }) => val.idShort === "metaAbstract")?.value
    if (abstract) {
      return true
    } else {
      return false
    }
  }

  private getMetaRef(item: any) {
    let value = item.value.find(
      (val: { idShort: string; }) => val.idShort === "metaRefines")
    if (value) {
      return value.value
    } else {
      return null
    }
  }

  private isTypeMetaKindEqualNum(item:any, num:number) {
    let value = item.value.find(
      (val: { idShort: string; }) => val.idShort === "metaTypeKind").value
    if (value == num) {
      return true
    } else {
      return false
    }
  }

  /**
   * Documentation in src/assets/doc/filterMeta.jpg
   * @returns {boolean}
   */
  private isSubtype(metaRefines_value: any):boolean{
    let parent_item = this.getParentItem(metaRefines_value)
    if (parent_item) {
      let filter = this.reqTypes.find(type => type.cat === this.category)
      if (filter?.metaRef.includes(this.getMetaRef(parent_item))) {
        return true
      } else {
        let metaRef_val_parent = this.getMetaRef(parent_item)
        if(metaRef_val_parent) {
          return this.isSubtype(metaRef_val_parent) //recursion
        } else {
          return false
        }
      }
    } else {
      return false
    }
  }

  private getParentItem(metaRefines: string) {
    let result = this.meta?.value?.find(item => item.idShort === metaRefines)
    if(result) {
      return result
    } else {
      return null
    }
  }

// ----------------------------------------------------------------------

  private cleanTypeName(type: string) {
    //console.log("[editor | cleanTypeName] triggered, type:")
    //console.log(type)
    const startIndex = type.lastIndexOf('(') + 1;
    const endIndex = type.indexOf(')');
    if(endIndex > 0){
      return type.substring(startIndex, endIndex);
    } else {
      return type;
    }
  }

  public displayName(property: Resource | string) {
    let displayName = '';
    if(typeof(property) == 'string') {
      displayName = property;
    } else if(property.value) {
      displayName = property.value.find(
        item => item.idShort === 'name')?.value;
    }
    return displayName;
  }

  public generateInputs() {

    this.uiGroups = [];
    const selectedType = this.selectedType as configMetaContainer;
    this.ivmlType = selectedType.idShort // TODO leave ?

    if(selectedType && selectedType.value) {

      // (Constants) hard-coded in case of primitive types
      //if (this.primitiveTypes.includes(selectedType.idShort)) {
      if (primitiveDataTypes.includes(selectedType.idShort)) {
        let meta_entry:configMetaEntry = {
          modelType: {name: ""},
          kind: "",
          value: "",
          idShort: "value"
        }

        let editorInput:editorInput =
          {name: "value", type: selectedType.idShort, value:[],
          description: [{language: '', text: ''}],
          refTo: false, multipleInputs: false, meta:meta_entry}


        let uiGroup = 1 // TODO what value here?
        this.uiGroups.push({
          uiGroup: uiGroup,
          inputs: [editorInput],
          optionalInputs: [],
          fullLineInputs: [],
          fullLineOptionalInputs: []
        });
      }

      for(const input of selectedType.value) {
        if(input.idShort && this.metaTypes.indexOf(input.idShort) === -1) {
          let isOptional = false;
          let uiGroup: number = input.value.find(
            (item: { idShort: string; }) => item.idShort === 'uiGroup')?.value

          if(uiGroup < 0) {
            isOptional = true;
            uiGroup = uiGroup * -1;
          }
          let uiGroupCompare =  this.uiGroups.find(
            item => item.uiGroup === uiGroup);

          let editorInput: editorInput =
            {name: '', type: '', value:[], description:
              [{language: '', text: ''}],
              refTo: false, multipleInputs: false};

          let name = input.value.find(
            (item: { idShort: string; }) => item.idShort === 'name')
            if(name) {
              editorInput.name = name.value;
              if(name.description
                && name.description[0]
                && name.description[0].text
                && name.description[0].language) {
                editorInput.description = name.description;
              }
            }

          editorInput.type = input.value.find(
            (item: { idShort: string; }) => item.idShort === 'type')?.value;

          editorInput.meta = input;
          let cleanType = this.cleanTypeName(editorInput.type);
          let type = this.meta?.value?.find(type => type.idShort == cleanType);
          if(type) {
            editorInput.metaTypeKind = type.value.find(
              (item: { idShort: string; }) => item.idShort === 'metaTypeKind')?.value;
          } else if(this.metaBackup && this.metaBackup.value) {
            let temp = this.metaBackup.value.find(item => item.idShort === this.cleanTypeName(editorInput.type));
            editorInput.metaTypeKind = temp?.value.find((item: { idShort: string; }) => item.idShort === 'metaTypeKind').value
          }

          //the metaTypeKind is not included on the values of the types in the configuration/meta collection
          //therefore this approach doesnt work, but it would be much more performant if it did
          // editorInput.metaTypeKind = input.value.find(
          //   (item: { idShort: string; }) => item.idShort === 'metaTypeKind')?.value;
          //   console.log(editorInput);
          //   console.log(input.value);


          if(editorInput.type.indexOf('refTo') >= 0) {
            editorInput.refTo = true;
          }
          if(editorInput.type.indexOf('setOf') >= 0
            || editorInput.type.indexOf('sequenceOf') >= 0) {
            editorInput.multipleInputs = true;
          }
          //assign initial value of inputFields
          let initial;
          if(editorInput.multipleInputs || editorInput.metaTypeKind === 2) {
            initial = []
          } else if(editorInput.type === 'Boolean'){
            initial = false;
          } else if(editorInput.metaTypeKind === 10 && !editorInput.multipleInputs) {
            initial = {};
          } else {
            initial = '';
          }
          editorInput.value = initial;

          if(!uiGroupCompare ){
            if(isOptional) {
              if(editorInput.multipleInputs) {
                this.uiGroups.push({
                  uiGroup: uiGroup,
                  inputs: [],
                  optionalInputs: [],
                  fullLineInputs: [],
                  fullLineOptionalInputs: [editorInput]
                });
              } else {
                this.uiGroups.push({
                  uiGroup: uiGroup,
                  inputs: [],
                  optionalInputs: [editorInput],
                  fullLineInputs: [],
                  fullLineOptionalInputs: []
                });
              }
            } else {
              if(editorInput.multipleInputs) {
                this.uiGroups.push({
                  uiGroup: uiGroup,
                  inputs: [],
                  optionalInputs: [],
                  fullLineInputs: [editorInput],
                  fullLineOptionalInputs: []
                });
              } else {
                this.uiGroups.push({
                  uiGroup: uiGroup,
                  inputs: [editorInput],
                  optionalInputs: [],
                  fullLineInputs: [],
                  fullLineOptionalInputs: []
                });
              }
            }
          } else {
            if(isOptional) {
              if(editorInput.multipleInputs) {
                uiGroupCompare?.fullLineOptionalInputs.push(editorInput);
              } else {
                uiGroupCompare?.optionalInputs.push(editorInput);
              }

            } else {
              if(editorInput.multipleInputs) {
                uiGroupCompare?.fullLineInputs.push(editorInput);
              } else {
                uiGroupCompare?.inputs.push(editorInput);
              }
            }
          }
        }
      }
    }
  }

  public toggleOptional(uiGroup: uiGroup) {
    uiGroup.toggleOptional = !uiGroup.toggleOptional;
  }

  public create() {
    const creationData = this.prepareCreation();
    //TODO: mach ein ivml draus
    this.ivmlFormatter.createVariable(this.variableName, creationData, this.ivmlType)
    //let ivml = this.ivmlFormatter.getIvml(this.variableName, creationData, this.ivmlType)
    //TODO: platform request
    //let inputVar:InputVariable[] = this.getCreateVarInputVar(creationData, variableName)
  }

  public close() {
    this.dialog.close();
  };

  public prepareCreation() {
    let complexType: Record<string, any> = {};
    this.showInputs = false;
    for(let uiGroup of this.uiGroups) {

      for(let input of uiGroup.inputs) {
        if(input.meta){
          complexType[input.name] = input.value;
        }

        if(primitiveDataTypes.includes(input.type)) {
          complexType[input.name] = input.value
        }

      }
      for(let input of uiGroup.optionalInputs) {
        if(input.meta){
          complexType[input.name] = input.value;
        }
      }
      for(let input of uiGroup.fullLineInputs) {
        if(input.meta){
          complexType[input.name] = input.value;
        }
      }
      for(let input of uiGroup.fullLineOptionalInputs) {
        if(input.meta){
          complexType[input.name] = input.value;
        }
      }
    }
    return complexType;
  }

  public addType() {
    let complexType: Record<string, any> = {};

    if(this.type) {
      for(let uiGroup of this.uiGroups) {
        for(let input of uiGroup.inputs) {
          if(input.meta){
            complexType[input.name] = input.value;
          }
        }
        for(let input of uiGroup.optionalInputs) {
          if(input.meta){
            complexType[input.name] = input.value;
          }
        }
        for(let input of uiGroup.fullLineInputs) {
          if(input.meta){
            complexType[input.name] = input.value;
          }
        }
        for(let input of uiGroup.fullLineOptionalInputs) {
          if(input.meta){
            complexType[input.name] = input.value;
          }
        }
      }
      if(this.type.multipleInputs) {
        this.type.value.push(complexType);
      } else {
        this. type.value = complexType;
      }

    }
    this.dialog.close();
  }
}
