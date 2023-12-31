import { Component, Input, OnInit } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';
import { ApiService } from 'src/app/services/api.service';
import { IvmlFormatterService } from 'src/app/services/ivml-formatter.service';
import { Resource, uiGroup, editorInput, configMetaContainer, configMetaEntry, ResourceAttribute, metaTypes, 
  MTK_primitive, MTK_derived, MTK_enum, MTK_compound, MT_metaRefines, MT_metaTypeKind, MT_metaAbstract, 
  primitiveDataTypes, MT_metaDefault, IVML_TYPE_PREFIX_enumeration, DR_displayName, IvmlRecordValue, IvmlValue} from 'src/interfaces';
import { Utils, DataUtils } from 'src/app/services/utils.service';

@Component({
  selector: 'app-editor',
  templateUrl: './editor.component.html',
  styleUrls: ['./editor.component.scss']
})
export class EditorComponent extends Utils implements OnInit {

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

  feedback: string = ""

  constructor(private api: ApiService,
    public dialog: MatDialogRef<EditorComponent>,
    public ivmlFormatter: IvmlFormatterService) {
      super();
  }

  async ngOnInit() {
    if(this.refinedTypes) {
      this.meta = {
        idShort: 'meta',
        value: this.refinedTypes
      }
    } else if(!this.type) {
      await this.getMeta()
    } else if (this.type.type){
      if(!this.metaBackup || !this.metaBackup.value) {
        await this.getMeta()
      }
      if(this.metaBackup && this.metaBackup.value) {
        let type = DataUtils.stripGenericType(this.type.type);
        this.selectedType = this.metaBackup.value.find(item => item.idShort === type);
        this.generateInputs()
      }
    }
    if(this.metaBackup && this.metaBackup.value) {
      let searchTerm = 'Field'
      for(const type of this.metaBackup.value) {
        const refined = DataUtils.getProperty(type.value, MT_metaRefines);
        if(refined && refined.value != '') {
          if(searchTerm === refined.value) {
            console.debug("TYPE " + type);
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
            let metaRefVal = DataUtils.getPropertyValue(item.value, MT_metaRefines)
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
            if (this.isTypeMetaKindEqualNum(item, MTK_primitive)
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
    let abstract = DataUtils.getPropertyValue(item.value, MT_metaAbstract);
    if (abstract) {
      return true
    } else {
      return false
    }
  }

  private getMetaRef(item: any) {
    let value = DataUtils.getProperty(item.value, MT_metaRefines);
    if (value) {
      return value.value
    } else {
      return null
    }
  }

  private isTypeMetaKindEqualNum(item:any, num:number) {
    let value = DataUtils.getPropertyValue(item.value, MT_metaTypeKind);
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
    this.ivmlType = selectedType.idShort
    if (selectedType && selectedType.value) {
      // (Constants) hard-coded in case of primitive types
      let selMetaTypeKind = DataUtils.getPropertyValue(selectedType.value, MT_metaTypeKind);
      if (primitiveDataTypes.includes(selectedType.idShort) || selMetaTypeKind == MTK_enum) {
        let meta_entry:configMetaEntry = {
          modelType: {name: ""},
          kind: "",
          value: "",
          idShort: "value"
        }
        let val = [this.type?.value] || []; 
        let editorInput:editorInput =
          {name: "value", type: selectedType.idShort, value:val,
          description: [{language: '', text: ''}],
          refTo: false, multipleInputs: false, meta:meta_entry};
        editorInput.metaTypeKind = selMetaTypeKind;

        let uiGroup = 1
        this.uiGroups.push({
          uiGroup: uiGroup,
          inputs: [editorInput],
          optionalInputs: [],
          fullLineInputs: [],
          fullLineOptionalInputs: []
        });
      } else {
        for (const input of selectedType.value) {
          if (input.idShort && metaTypes.indexOf(input.idShort) === -1) {
            let isOptional = false;
            let uiGroup: number = DataUtils.getPropertyValue(input.value, 'uiGroup');

            if (uiGroup < 0) {
              isOptional = true;
              uiGroup = uiGroup * -1;
            }
            let uiGroupCompare =  this.uiGroups.find(
              item => item.uiGroup === uiGroup);

            let editorInput: editorInput =
              {name: '', type: '', value:[], description:
                [{language: '', text: ''}],
                refTo: false, multipleInputs: false};

            let name = DataUtils.getProperty(input.value, 'name');
            if (name) {
              editorInput.name = name.value;
              if (name.description
                && name.description[0]
                && name.description[0].text
                && name.description[0].language) {
                  editorInput.description = name.description;
              }
            }
            let val = DataUtils.getProperty(this.type?.value, input.idShort); // TODO may need object access for nested objects
            if (val) {
              editorInput.displayName = val[DR_displayName];
            }
            editorInput.type = DataUtils.getPropertyValue(input.value, 'type');

            editorInput.meta = input;
            let typeGenerics = DataUtils.stripGenericType(editorInput.type);
            let type = this.meta?.value?.find(type => type.idShort === typeGenerics);
            if (type) {
              editorInput.metaTypeKind = DataUtils.getPropertyValue(type.value, MT_metaTypeKind);
            } else if(this.metaBackup && this.metaBackup.value) {
              let iterType = editorInput.type;
              do {
                let temp = this.metaBackup.value.find(item => item.idShort === DataUtils.stripGenericType(iterType));
                editorInput.metaTypeKind = DataUtils.getPropertyValue(temp?.value, MT_metaTypeKind);
                editorInput.type = iterType;
                if (editorInput.metaTypeKind == MTK_derived) {
                  iterType = DataUtils.getPropertyValue(temp?.value, MT_metaRefines);
                  if (!iterType) {
                    break;
                  }
                }
              } while (editorInput.metaTypeKind == MTK_derived);
            }
            //the metaTypeKind was so far not included on the values of the types in the configuration/meta collection
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
            editorInput.defaultValue = DataUtils.getPropertyValue(input.value, MT_metaDefault);
            let ivmlValue = this.type?.value || editorInput.defaultValue || ""; 
            if (selMetaTypeKind === MTK_compound && this.isArray(ivmlValue)) {
              ivmlValue = DataUtils.getPropertyValue(ivmlValue, input.idShort);
            }
            let initial;
            if (this.isObject(ivmlValue) && ivmlValue && input.idShort in ivmlValue) { 
              // compound instances may be passed in as object with properties, those being undefined are defaults
              ivmlValue = ivmlValue[input.idShort];
              if (!ivmlValue) {
                ivmlValue = editorInput.defaultValue;
              }
            }
            if (editorInput.multipleInputs) {
              initial = ivmlValue
            } else if (editorInput.metaTypeKind === MTK_enum) {
              initial = ivmlValue
              editorInput.valueTransform = input => IVML_TYPE_PREFIX_enumeration + (input.type || "") + '.' + input.value;
            } else if (editorInput.type === 'Boolean') {
              initial = String(ivmlValue).toLowerCase() === 'true';
            } else if (editorInput.metaTypeKind === MTK_compound && !editorInput.multipleInputs) {
              initial = ivmlValue; // input comes as object
            } else {
              if (typeGenerics == "AasLocalizedString") {
                initial = DataUtils.getLangStringText(ivmlValue);
                editorInput.valueLang = DataUtils.getLangStringLang(ivmlValue);
                editorInput.valueTransform = input => DataUtils.composeLangString(input.value, DataUtils.getUserLanguage());
              } else {
                initial = ivmlValue; // input is just the value
              }
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
  }

  public toggleOptional(uiGroup: uiGroup) {
    uiGroup.toggleOptional = !uiGroup.toggleOptional;
  }

  /**
   * Called when creating a new variable is requested from the editor.
   */
  public async create() {
    let creationData: IvmlRecordValue = {};
    this.showInputs = false;
    this.transferUiGroups(this.uiGroups, creationData);
    if (this.selectedType?.idShort == "Application") {
      let fdb = await this.ivmlFormatter.createApp(this.variableName, creationData);
      this.feedback = fdb.feedback;
    } else {
      let fdb = await this.ivmlFormatter.createVariable(this.variableName, creationData, this.ivmlType);
      this.feedback = fdb.feedback;
    }
  }

  /**
   * Called to close the editor.
   */
  public close() {
    this.dialog.close();
  };

  /**
   * Called from the editor to save the entered values into type.value.
   */
  public async save() {
    let complexType: IvmlRecordValue = {};

    if (this.type) {
      this.transferUiGroups(this.uiGroups, complexType);
      if(this.type.multipleInputs) {
        this.type.value.push(complexType);
      } else {
        this.type.value = complexType;
      }
    }
    this.dialog.close();
  }

  /**
   * Transfers all inputs from uiGroups to result.
   * Calls {@link this.transferInputs}.
   * 
   * @param uiGroups the UI groups 
   * @param result the results object to be modified as a side effect
   */
  private transferUiGroups(uiGroups: uiGroup[], result: IvmlRecordValue) {
    for (let uiGroup of this.uiGroups) {
      this.transferInputs(uiGroup.inputs, result);
      this.transferInputs(uiGroup.optionalInputs, result);
      this.transferInputs(uiGroup.fullLineInputs, result);
      this.transferInputs(uiGroup.fullLineOptionalInputs, result);
    }
  }

  /**
   * Transfers the values in the given inputs into properties of result filtering out unchanged IVML default values.
   * Calls {@link this.getValue}.
   * 
   * @param inputs the editor inputs to process 
   * @param result the results object to be modified as a side effect
   */
  private transferInputs(inputs: editorInput[], result: IvmlRecordValue) {
    for (let input of inputs) {
      let tmp: any = null;
      if (input.meta) {
        tmp = this.getValue(input);
      } else if (primitiveDataTypes.includes(input.type)) { // was only in prepareCreation and only for uiGroup.inputs
        tmp = this.getValue(input);
      }
      if (tmp && tmp != input.defaultValue) { // don't write back IVML default values
        let val : IvmlValue = {value: tmp, _type: input.type};
        result[input.name] = val;
        //result[input.name] = tmp;
      }
    }
  }

}
