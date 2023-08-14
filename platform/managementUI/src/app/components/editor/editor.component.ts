import { Component, Input, OnInit } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { ApiService } from 'src/app/services/api.service';
import { Resource, uiGroup, editorInput, configMetaContainer } from 'src/interfaces';

@Component({
  selector: 'app-editor',
  templateUrl: './editor.component.html',
  styleUrls: ['./editor.component.scss']
})
export class EditorComponent implements OnInit {

  //type to generate subeditor for, null if this editor instance is not a subeditor
  @Input() type: editorInput | null = null;

  category: string = 'all';
  meta: Resource | undefined;
  /* backup needed for data recovery before re-filtering data
   for the next tab */
  metaBackup: Resource | undefined;
  selectedType: Resource | undefined;

  enums: any[] = [];

  uiGroups: uiGroup[] = [];

  showInputs = true;

  metaTypes = ['metaState', 'metaProject',
    'metaSize', 'metaType', 'metaRefines', 'metaAbstract', 'metaTypeKind'];

  //metatypekind: PRIMITIVE=1, ENUM=2, CONTAINER=3, CONSTRAINT=4, DERIVED=9, COMPOUND=10

  /* metaTypeKind */
  primitive = 1

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

  numOfItemsInMeta: number = 0;

  constructor(private api: ApiService,
    public dialog: MatDialogRef<EditorComponent>,
    public subDialog: MatDialog) { }

  ngOnInit(): void {
    if(!this.type) {
      this.getMeta()
    } else if(this.metaBackup && this.metaBackup.value && this.type.type){
      let type = this.cleanTypeName(this.type.type);
      this.selectedType = this.metaBackup.value.find(item => item.idShort === type);
      this.generateInputs()
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

public getEnumValue(value: editorInput) {
  if(this.metaBackup && this.metaBackup.value) {
    let enumMeta = this.metaBackup.value.find(a => a.idShort === value.type)
    console.log(enumMeta);
    let a
    if(value && Array.isArray(value.value)) {
      let varValue = value.value.find((a: { idShort: string; }) => a.idShort === "varValue");
      a = varValue.value
    }
    console.log(a);
    if(typeof(a) == "string") {
      return a
    } else {
      return null
    }
  } else {
    return null
  }
}

public getEnums(value: editorInput) {
  if(this.metaBackup && this.metaBackup.value) {
    let enumMeta = this.metaBackup.value.find(a => a.idShort === value.type)

  }

}

  public generateInputs() {
    console.log("[editor | generateInputs] type:")
    console.log(this.type)
    console.log("[editor | generateInputs] selected type:")
    console.log(this.selectedType)
    this.uiGroups = [];
    const selectedType = this.selectedType as configMetaContainer;
    if(selectedType && selectedType.value) {

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
          console.log(editorInput);
          let initial;
          if(editorInput.multipleInputs) {
            initial = []
          } else if(editorInput.type === 'Boolean'){
            initial = false;
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
    console.log("[editor | generateInputs] END uiGroup: ")
    console.log(this.uiGroups)
  }

  public toggleOptional(uiGroup: uiGroup) {
    uiGroup.toggleOptional = !uiGroup.toggleOptional;
  }

  public create() {
    const creationData = this.prepareCreation();
    console.log(creationData);
    //actual creation
  }

  public close() {
    this.dialog.close();
  };

  public openSubeditor(type: editorInput) {
    //this.router.navigateByUrl("list/editor/all");
    let dialogRef = this.subDialog.open(EditorComponent, {
      height: '80%',
      width:  '80%',
    })
    dialogRef.componentInstance.type = type;
    dialogRef.componentInstance.metaBackup = this.metaBackup;
  }

  public prepareCreation() {
    let complexType: Record<string, any> = {};
    this.showInputs = false;
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
      this.type.value.push(complexType);
    }
    this.dialog.close();
  }
}
