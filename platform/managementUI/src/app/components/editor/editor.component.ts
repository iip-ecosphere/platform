import { firstValueFrom } from 'rxjs';
import { Component, OnInit } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';
import { ActivatedRoute } from '@angular/router';
import { ApiService } from 'src/app/services/api.service';
import { Resource, uiGroup, editorInput, ResourceAttribute } from 'src/interfaces';

@Component({
  selector: 'app-editor',
  templateUrl: './editor.component.html',
  styleUrls: ['./editor.component.scss']
})
export class EditorComponent implements OnInit {

  category: string = 'all';
  meta: Resource | undefined;
  // backup needed for data recovery before re-filtering data
  // for the next tab
  metaBackup: Resource | undefined;
  selectedType: Resource | undefined;
  bool = true;

  uiGroups: uiGroup[] = [];

  metaTypes = ['metaState', 'metaProject',
    'metaSize', 'metaType', 'metaRefines', 'metaAbstract'];

  //metaTypeKind
  primitive = 1

  datatypes = [
    {cat: "Constants", value: []},
    {cat: "Types", value: ["RecordType", "ArrayType"]},
    {cat: "Dependencies", value: ["Dependency"]},
    {cat: "Nameplates", value: ["NameplateInfo"]},
    {cat: "Services", value: ["Service"]},
    {cat: "Servers", value: ["Server"]},
    {cat: "Meshes", value: ["ServiceMesh"]},
    {cat: "Applications", value: ["Application"]}
  ];

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

  constructor(private route: ActivatedRoute,
    private api: ApiService,
    public dialog: MatDialogRef<EditorComponent>) { }

  ngOnInit(): void {
    this.getMeta()
    /*
    const category = this.route.snapshot.paramMap.get('ls');
    if(category && category != '') {
      this.category = category;
    }
    */
  }

  private async getMeta() {
    this.meta = await this.api.getMeta();
    this.metaBackup = JSON.parse(JSON.stringify(this.meta)) // deep copy
    this.filterMeta();

  }

  public filterMeta() {
    console.log("## (filterMeta) \nmeta:")
    console.log(this.meta)
    this.meta = JSON.parse(JSON.stringify(this.metaBackup)) // recovering meta from deep copy
    let filter = this.reqTypes.find(type => type.cat === this.category)
    let newMetaValues = []
    if (this.meta && this.meta.value) {
      for (const item of this.meta.value) {
        //console.log("item: " + item.idShort + " has metaRef")
        //console.log(this.hasMetaRef(item))
        if (this.hasMetaRef(item)) {
          let metaRefVal = item.value.find((val: { idShort: string; }) => val.idShort === "metaRefines").value

          let abstract = this.isAbstract(item)
          if(metaRefVal != "") {
            let x = 0
          } else {
            console.log(item.idShort + " is top level, \n\tMetaAstract? " + abstract)
          }

        } else {
          // ivml types
          if (this.isTypeMetaKindEqual(item, this.primitive) && filter?.metaRef.length == 0) {
            newMetaValues.push(item)
          }
        }
      }
    }
    this.meta!.value = newMetaValues
  }

  /** Returns false if there is no value "metaAbstract" */
  private isAbstract(item:any) {
    let abstract = item.value.find((val: { idShort: string; }) => val.idShort === "metaAbstract")?.value
    if (abstract) {
      return true
    } else {
      return false
    }
  }

  private hasMetaRef(item: any) {
    let value = item.value.find((val: { idShort: string; }) => val.idShort === "metaRefines")
    if (value) {
      return true
    } else {
      return false
    }
  }

  private isTypeMetaKindEqual(item:any, num:number) {
    let value = item.value.find((val: { idShort: string; }) => val.idShort === "metaTypeKind").value
    if (value == num) {
      return true
    } else {
      return false
    }
  }

  public filterMeta2() {
    this.meta = JSON.parse(JSON.stringify(this.metaBackup)) // recovering meta from deep copy
    let newMetaValues = []
    if (this.meta && this.meta.value) {
      for (const item of this.meta.value) {
        if (this.isType(item)) {
          newMetaValues.push(item)
        }
      }
    }
    this.meta!.value = newMetaValues

    /*
    let filter = this.filters.find(item => item.cat === this.category)?.value
    if (this.meta && filter != "") {
      let temp = this.meta.value
      if (temp) {
        let tempValues = temp.find(
          item => item.idShort === filter) as ResourceAttribute
        this.meta!.value = [tempValues] // TODO what if there are more than one values
      }
    }
    */
  }

  public isType(item:any) {
    //console.log("# (isType) \n item:")
    //console.log(item)
    let requiredTypes = this.datatypes.find(type => type.cat === this.category)?.value
    if (requiredTypes?.includes(item.idShort)) {
      return true
    } else {

      let metaRefinesValue = item.value.find(
        (val: { idShort: string; }) => val.idShort === "metaRefines" ? item.value : "nic")


      if (metaRefinesValue == "nic") {
        console.log(item.idShort + ", metaRef: " + metaRefinesValue)
      }

      if (metaRefinesValue == "") {
        console.log(item.idShort + " toplevel type")
      }

      if (requiredTypes?.includes(metaRefinesValue)) {
        return true
      }
    }
    return false
  }

  public displayName(property: Resource) {
    let displayName = '';
    if(property.value) {
      displayName = property.value.find(
        item => item.idShort === 'name')?.value;
    }
    return displayName;
  }

  public generateInputs() {
    this.uiGroups = [];
    console.log(this.selectedType);
    const selectedType = this.selectedType;
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
          console.log(uiGroup);
          console.log(uiGroupCompare);


          let editorInput: editorInput =
            {name: '', type: '', value:[], description:
              [{language: '', text: ''}],
              refTo: false, multipleInputs: false};
          let name = input.value.find(
            (item: { idShort: string; }) => item.idShort === 'name')
          editorInput.name = name.value;
          if(name.description
            && name.description[0]
            && name.description[0].text
            && name.description[0].language) {
            editorInput.description = name.description;
          }
          editorInput.type = input.value.find(
            (item: { idShort: string; }) => item.idShort === 'type')?.value
          if(editorInput.type.indexOf('refTo') >= 0) {
            editorInput.refTo = true;
          }
          if(editorInput.type.indexOf('setOf') >= 0
            || editorInput.type.indexOf('sequenceOf') >= 0) {
            editorInput.multipleInputs = true;
          }
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
        console.log("ui groups")
        console.log(this.uiGroups);
    }
  }

  public create() {

  }

  public close() {
    this.dialog.close();
  };

}
