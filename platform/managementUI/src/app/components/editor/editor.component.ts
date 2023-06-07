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

  uiGroups: uiGroup[] = [];

  metaTypes = ['metaState', 'metaProject',
    'metaSize', 'metaType', 'metaRefines', 'metaAbstract'];

  datatypes = [
    {cat: "Setup", value: ["PrimitiveType",
      "TransportProtocol", "DeviceRegistry"]},
    {cat: "Constants", value: ["PrimitiveType",
      "NumericPrimitiveType"]},
    {cat: "Types", value: ["RecordType", "ArrayType"]},
    {cat: "Dependencies", value: ["Dependency"]},
    {cat: "Nameplates", value: ["NameplateInfo"]},
    {cat: "Services", value: ["Service"]},
    {cat: "Servers", value: ["Server"]},
    {cat: "Meshes", value: ["ServiceMesh"]},
    {cat: "Applications", value: ["Application"]}
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
              this.uiGroups.push({
                uiGroup: uiGroup,
                inputs: [],
                optionalInputs: [editorInput]
              });
            } else {
              this.uiGroups.push({
                uiGroup: uiGroup,
                inputs: [editorInput],
                optionalInputs: []
              });
            }
          } else {
            if(isOptional) {
              uiGroupCompare?.optionalInputs.push(editorInput);
            } else {
              uiGroupCompare?.inputs.push(editorInput);
            }

          }
        }
        }
        console.log("ui groups")
        console.log(this.uiGroups);
    }
  }

  public displayName(property: Resource) {
    let displayName = '';
    if(property.value) {
      displayName = property.value.find(
        item => item.idShort === 'name')?.value;
    }
    return displayName;

  }

  public filterMeta() {
    this.meta = JSON.parse(JSON.stringify(this.metaBackup))
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
    let requiredTypes = this.datatypes.find(type => type.cat === this.category)?.value
    if (requiredTypes?.includes(item.idShort)) {
      return true
    } else {
      let metaRefinesValue = item.value.find(
        (val: { idShort: string; }) => val.idShort === "metaRefines").value
      if (requiredTypes?.includes(metaRefinesValue)) {
        return true
      }
    }
    return false
  }

  public create() {

  }

  public close() {
    this.dialog.close();
  };

}
