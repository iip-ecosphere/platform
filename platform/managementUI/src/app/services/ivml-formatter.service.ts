//import { type } from 'os';
import { InputVariable, primitiveDataTypes, IVML_TYPE_PREFIX_enumeration, JsonPlatformOperationResult, IvmlRecordValue, IVML_TYPE_String, IVML_TYPE_Boolean, IvmlValue, UserFeedback, uiGroup, configMetaContainer, MT_metaTypeKind, MTK_enum, configMetaEntry, editorInput, Resource, metaTypes, DR_displayName, MTK_derived, MT_metaRefines, MT_metaDefault, MTK_compound, MT_metaAbstract, MTK_primitive, MTK_langString } from 'src/interfaces';
import { Injectable } from '@angular/core';
import { AAS_OP_PREFIX_SME, AAS_TYPE_STRING, ApiService, GRAPHFORMAT_DRAWFLOW, IDSHORT_SUBMODEL_CONFIGURATION } from './api.service';
import { DataUtils, EditorPartition, UtilsService } from './utils.service';

@Injectable({
  providedIn: 'root'
})
export class IvmlFormatterService extends UtilsService {

  constructor(public api:ApiService) { 
    super();
  }

  nonVisibleValues = ["optional"]

  /**
   * Creates a configuration variable.
   * 
   * @param variableName the IVML variable name 
   * @param data the data representing the IVML value of var variable from editor values 
   * @param type the type of the variable as IVML type
   * @returns user feedback
   */
  public async createVariable(variableName: string, data: IvmlRecordValue, type: string) {
    let ivmlFormat = this.getIvml(variableName, data, type);
    let params: InputVariable[] = [];
    params.push(ApiService.createAasOperationParameter("varName", AAS_TYPE_STRING, ivmlFormat[0]));
    params.push(ApiService.createAasOperationParameter("type", AAS_TYPE_STRING, ivmlFormat[1]));
    params.push(ApiService.createAasOperationParameter("valExpr", AAS_TYPE_STRING, ivmlFormat[2]));
    return await this.callConfigOperation("createVariable", params, "Values have been stored!");
  }

  /**
   * Changes a configuration variable.
   * 
   * @param variableName the IVML variable name 
   * @param data the data representing the IVML value of var variable from editor values 
   * @param type the type of the variable as IVML type
   * @returns user feedback
   */
  public async setVariable(variableName: string, data: IvmlRecordValue, type: string) {
    let ivmlFormat = this.getIvml(variableName, data, type);
    let valueExprs = new Map();
    valueExprs.set(variableName, ivmlFormat);
    let params: InputVariable[] = [];
    params.push(ApiService.createAasOperationParameter("valueExprs", AAS_TYPE_STRING, JSON.stringify(valueExprs)));
    return await this.callConfigOperation("changeValues", params, "Values have been stored!");
  }

  /**
   * Deletes a configuration variable.
   * 
   * @param variableName the IVML variable name 
   * @returns user feedback
   */
  public async deleteVariable(variableName: string) {
    let params: InputVariable[] = [];
    params.push(ApiService.createAasOperationParameter("varName", AAS_TYPE_STRING, variableName));
    return await this.callConfigOperation("deleteVariable", params, "Configuration entry has been deleted!");
  }

  /**
   * Calls a configuration operation.
   * 
   * @param opName the operation name
   * @param params the parameter
   * @param successText the text to be emitted as user feedback if successful, the (exception) error else 
   * @returns user feedback
   */
  private async callConfigOperation(opName: string, params: InputVariable[], successText: string) {
    const response = await this.api.executeAasJsonOperation(IDSHORT_SUBMODEL_CONFIGURATION, 
      AAS_OP_PREFIX_SME + opName, params);
    let exception = this.api.getPlatformResponse(response);
    return this.getFeedback(exception, successText);
  }

  /**
   * Turns a platform operation result into a user feedback, i.e., returns either dflt or the exception in opResult.
   * 
   * @param opResult the operation result from ApiService#getPlatformResponse
   * @param success the text for success 
   * @returns feedback as success text/flag or the text returned as exception
   */
  private getFeedback(opResult: JsonPlatformOperationResult | null, success: string): UserFeedback {
    let result: UserFeedback = {feedback:success, successful:true};
    if (opResult && opResult.exception) {
      result.feedback = "Exception: " + opResult.exception;
      result.successful = false;
    }
    return result;
  }

  /**
   * Turns an IVML value recursively into IVML syntax.
   * 
   * @param value the value 
   * @returns the syntax
   */
  private toIvml(value: IvmlValue) {
    let result = "";
    if (value._type == IVML_TYPE_String) {
      result = `"${value.value}"`;
    } else if (primitiveDataTypes.includes(value._type)) {
      result = String(value.value);
    } else if (DataUtils.isIvmlCollection(value._type)) {
      result = "{";
      let first = true;
      for (let elemt of value.value) {
        if (!first) {
          result += ",";
        }
        if (elemt.hasOwnProperty('_type')) { // IVML "value" or primitive
          result += this.toIvml(elemt);
        } else {
          result += elemt;
        }
        first = false;
      }
      result += "}";
    } else if (DataUtils.isIvmlRefTo(value._type)) {
      result = `refBy(${value.value})`;
    } else if (this.isString(value.value) && value.value.startsWith(IVML_TYPE_PREFIX_enumeration)) { // ivml enums in internal notation
        result = value.value.replace(IVML_TYPE_PREFIX_enumeration, "");
    } else { // compound
      result = value._type + "{";
      let first = true;
      for (let elemt in value.value) {
        if (!first) {
          result += ",";
        }
        result += elemt + "=" + this.toIvml(value.value[elemt]);
        first = false;
      }
      result += "}";
    }
    return result;
  }

  /**
   * Returns whether the input data structure suggest that we only have a single top-level value, i.e., a single field called
   * "value" with none-compound (primitive, collection, reference or enum) value.
   * 
   * @param data the data
   * @param type the top-level type
   * @returns true for top-level value, false else
   */
  private isTopLevelValue(data: IvmlRecordValue, type: string) {
    let result = false;
    if (Object.keys(data).length == 1 && data["value"]) {
      if (primitiveDataTypes.includes(type) || DataUtils.isIvmlCollection(type) || DataUtils.isIvmlRefTo(type)) {
        result = true;
      } else if (this.isString(data["value"].value) && data["value"].value.startsWith(IVML_TYPE_PREFIX_enumeration)) {
        result = true;
      }
    } 
    return result;
  }

  /**
   * -> top-level value always with single "value" attribute
   * @param variableName 
   * @param data 
   * @param type 
   * @returns 
   */
  public getIvml(variableName: string, data: IvmlRecordValue, type: string) {
    // replacing whitespaces with underline
    variableName = this.replaceWhitespaces(variableName)

    // removing empty entries
    for(const key in data) {
      if (data[key].value === "") {
        delete data[key]
      }
      if (this.nonVisibleValues.includes(key)) {
        delete data[key]
      }
      if (Array.isArray(data[key]) && Object.keys(data[key]).length === 0) {
        delete data[key]
      }
      if (data[key] === null) {
        delete data[key]
      }
      if (this.isObject(data[key]) && Object.keys(data[key]).length === 0) {
        delete data[key]
      }
    }

    let ivml = ""

    if (this.isTopLevelValue(data, type)) {
      ivml = this.toIvml(data["value"]);
    } else {
      ivml += "{\n"
      let first = true;
      for (const key in data) {
        if (!first) {
          ivml += ", ";
        }
        ivml += key + " = " + this.toIvml(data[key]);
        first = false;
      }
/*
      // non-primitive types ---------------------------------------------------------
      ivml += "{\n"
      let i = 0

      for (const key in data) {
        if (this.isNumber(data[key].value)) {  // TODO maybe input check will be there some day
          ivml += key + " = " + Number(data[key])

        } else if (this.isString(data[key].value)) {
          // refTo, string inside non-primitive type, ivml enum
          ivml += key + " = " + this.convertToIvml(data[key].value)[0]

        } else if (Array.isArray(data[key].value)) {
          // setOf, sequenceOf ---------------------------------------------
          ivml += key + " = {"
          if (data[key].value.length > 1) {
            let j = 0

            if (this.isObject(data[key].value[0])) {
              // sequence with more than one entry
              ivml += this.handleIvmlSeq(data[key])

            } else {
              for (let elemt of data[key].value) {
                let return_val = this.convertToIvml(elemt)
                ivml += return_val[0]

                // no comma after the last value
                if (j < (Object.keys(data[key]).length - 1)) {
                  ivml += ","
                }
                j += 1
              }
            }
          } else {
            if (this.isObject(data[key].value[0])) {
              // ivml sequence
              ivml += this.handleIvmlSeq(data[key])

            } else {
              // refTo, string or boolean
              let elemt = data[key].value[0]
              let return_val = this.convertToIvml(elemt)
              if (return_val[1] === LIST) {
                ivml += "{" + return_val[0] + "}"
              } else {
                ivml += return_val[0]
              }
            }
          }
          ivml += "}"

        } else if (this.isObject(data[key])) {
          // compound ---------------------------------------------
          ivml += key + " = {" + this.getCompundStructure(data[key]) + "}"

        } else {
          // integer, real
          ivml += key + " = " + data[key]
        }
        // no comma after the last value
        if (i < (Object.keys(data).length - 1)) {
          ivml += ",\n"
        }
        i += 1
      }*/
      ivml += "\n}"
    }

    let result =  [variableName, type, ivml]
    return result
  }

  /*private convertToIvml(elemt:any) {
    let result = ["", null]                   // ivml, type (refTo, string, number, list)
    if (this.isString(elemt)) {
      if (elemt.startsWith(REF_TO)) {
        // refTo
        elemt = elemt.replace("refTo", "refBy")
        result = [elemt, REF_TO]

      } else if (elemt.startsWith(IVML_TYPE_PREFIX_enumeration)) {
        // ivml enums
        elemt = elemt.replace(IVML_TYPE_PREFIX_enumeration, "")
        result = [elemt, null]
      } else {
        // string ''
        result = ["\"" + elemt + "\"", IVML_TYPE_String]
      }
    } else if (Array.isArray(elemt)) {
      result = this.convertToIvml(elemt[0]) // TODO do also for array with multipy elements
    } else if (this.isBoolean(elemt)) {
      result = [String(elemt), IVML_TYPE_Boolean] // TODO do I need this info about boolean?
    } else {
      result = elemt
    }
    return result
  }

  private handleIvmlSeq(seq: any) {
    let result = ""
    let i = 0
    for (let elemt of seq) {
      let j = 0
      result += "{" + this.getCompundStructure(elemt)

      // no comma after the last value
      if (i < (Object.entries(seq).length - 1)) {
        result += "}, "
      }
      i += 1
    }
    return result += "}"
  }

  getCompundStructure(object: any) {
    let result = ""
    let i = 0
    for (const [key, value] of Object.entries(object)) {
      let ivml_value = this.convertToIvml(value)[0]
      result += key + "=" + ivml_value
      // no comma after the last value
      if (i < (Object.entries(object).length - 1)) {
        result += ", "
      }
      i += 1
    }
    return result
  }*/

  replaceWhitespaces(value: string) {
    let temp = value.split(' ')
    return temp.join('_')
  }
  
  // ------------- setGraph --------------------------------------------------

  /**
   * Creates an application without graph.
   * 
   * @param appName the (configured) appliation name
   * @param data the application data from the editor variables
   * @returns the user feedback
   */
  public async createApp(appName:string, data:IvmlRecordValue) {
    let ivmlFormat = this.getIvml(appName, data, "")[2]
    return await this.setMesh(appName, ivmlFormat, "", "");
  }

  /**
   * Creates/changes a graph/mesh in a given application.
   * 
   * @param appName the (configured) application name (app part ignored if empty)
   * @param appValExpr the IVML value expression for the application (app part ignored if empty)
   * @param serviceMeshName the (configured) service mesh name (mesh part ignored if empty), linked into the app if appName is given
   * @param val the graph in drawflow format
   * @returns the user feedback
   */
  public async setMesh(appName: string, appValExpr: string, serviceMeshName:string, val:string) {
    let params: InputVariable[] = [];
    params.push(ApiService.createAasOperationParameter("appName", AAS_TYPE_STRING, appName));
    params.push(ApiService.createAasOperationParameter("appValExpr", AAS_TYPE_STRING, appValExpr));
    params.push(ApiService.createAasOperationParameter("serviceMeshName", AAS_TYPE_STRING, serviceMeshName));
    params.push(ApiService.createAasOperationParameter("format", AAS_TYPE_STRING, GRAPHFORMAT_DRAWFLOW));
    params.push(ApiService.createAasOperationParameter("val", AAS_TYPE_STRING, val));
    return this.callConfigOperation("setGraph", params, `Service mesh '${serviceMeshName}' was stored.`)
    /*const response = await this.api.executeAasJsonOperation(IDSHORT_SUBMODEL_CONFIGURATION, 
      AAS_OP_PREFIX_SME + "setGraph", param);
  
    let exception = this.api.getPlatformResponse(response)
    let result = this.getFeedback(exception, `Service mesh ${serviceMeshName} was stored.`)
    return result*/
  }

  /**
   * Deletes a graph/mesh. Deletes app if no mesh is given.
   * 
   * @param appName the (configured) application name (app part ignored if empty) 
   * @param meshName the (configured) mesh name (mesh part ignored if empty), link from app is removed if appName is given
   * @returns the user feedback
   */
  public async deleteMesh(appName: string, meshName: string) {
    let params: InputVariable[] = [];
    params.push(ApiService.createAasOperationParameter("appName", AAS_TYPE_STRING, appName));
    params.push(ApiService.createAasOperationParameter("serviceMeshName", AAS_TYPE_STRING, meshName));
    let text;
    if (meshName.length > 0) {
      text = `Mesh '${meshName}' has been deleted!`;
    } else {
      text = `App '${appName}' has been deleted!`;
    }
    return await this.callConfigOperation("deleteGraph", params, text);
  }

  /**
   * Calculates UI groups based on inferred (selected type) information.
   * 
   * @param type the editor input type
   * @param meta the filtered type definitions for selectedType
   * @returns the UI groups
   */
  public calculateUiGroupsInf(type: editorInput | null, meta: Resource | undefined) {
    let uiGroups : uiGroup[] = [];
    if (type && type.type && meta) {
      let t = DataUtils.stripGenericType(type.type);
      let sel = meta.value?.find(item => item.idShort === t) as configMetaContainer;
      uiGroups = this.calculateUiGroups(sel, type, meta, meta);
    }
    return uiGroups;
  }

  /**
   * Calculates UI groups. Refactored from EditorComponent where both, (filtered) meta and (full) metaBackup are available.
   * 
   * @param selectedType the selected type
   * @param type the editor input type
   * @param meta the filtered type definitions for selectedType
   * @param metaBackup all type definitions, may be the same as meta
   * @returns the UI groups
   */
  public calculateUiGroups(selectedType: configMetaContainer | undefined, type: editorInput | null, meta: Resource | undefined, metaBackup: Resource | undefined) {
    let uiGroups : uiGroup[] = [];
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
        let val = [type?.value] || []; 
        let editorInput:editorInput =
          {name: "value", type: selectedType.idShort, value:val,
          description: [{language: '', text: ''}],
          refTo: false, multipleInputs: false, meta:meta_entry};
        editorInput.metaTypeKind = selMetaTypeKind;

        let uiGroup = 1
        uiGroups.push({
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
            let uiGroup: number = DataUtils.getPropertyValue(input.value, 'uiGroup'); // translated 100 -> 1
            if (uiGroup < 0) {
              isOptional = true;
              uiGroup = uiGroup * -1;
            }
            if (uiGroup > 0) { // 0 == invisible
              let uiGroupCompare =  uiGroups.find(item => item.uiGroup === uiGroup);

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
              let val = DataUtils.getProperty(type?.value, input.idShort); // TODO may need object access for nested objects
              if (val) {
                editorInput.displayName = val[DR_displayName];
              }
              editorInput.type = DataUtils.getPropertyValue(input.value, 'type');

              editorInput.meta = input;
              let typeGenerics = DataUtils.stripGenericType(editorInput.type);
              let foundType = meta?.value?.find(type => type.idShort === typeGenerics);
              if (foundType) {
                editorInput.metaTypeKind = DataUtils.getPropertyValue(foundType.value, MT_metaTypeKind);
              } else if(metaBackup && metaBackup.value) {
                let iterType = editorInput.type;
                do {
                  let temp = metaBackup.value.find(item => item.idShort === DataUtils.stripGenericType(iterType));
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

              if (editorInput.type.indexOf('refTo') >= 0) {
                editorInput.refTo = true;
              }
              if (editorInput.type.indexOf('setOf') >= 0
                || editorInput.type.indexOf('sequenceOf') >= 0) {
                editorInput.multipleInputs = true;
              }
              editorInput.defaultValue = DataUtils.getPropertyValue(input.value, MT_metaDefault);
              let ivmlValue = type?.value || editorInput.defaultValue || ""; 
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
              } else if (editorInput.type === IVML_TYPE_Boolean) {
                initial = String(ivmlValue).toLowerCase() === 'true';
              } else if (editorInput.metaTypeKind === MTK_compound && !editorInput.multipleInputs) {
                initial = ivmlValue; // input comes as object
              } else {
                if (typeGenerics == "AasLocalizedString") {
                  editorInput.metaTypeKind = MTK_langString;
                  initial = ivmlValue; // handled by LangStringInputComponent
                  /*initial = DataUtils.getLangStringText(ivmlValue);
                  editorInput.valueLang = DataUtils.getLangStringLang(ivmlValue);
                  editorInput.valueTransform = input => DataUtils.composeLangString(input.value, DataUtils.getUserLanguage());*/
                } else {
                  initial = ivmlValue; // input is just the value
                }
              }
              editorInput.value = initial;
              if (!uiGroupCompare){
                if (isOptional) {
                  if (editorInput.multipleInputs) {
                    uiGroups.push({
                      uiGroup: uiGroup,
                      inputs: [],
                      optionalInputs: [],
                      fullLineInputs: [],
                      fullLineOptionalInputs: [editorInput]
                    });
                  } else {
                    uiGroups.push({
                      uiGroup: uiGroup,
                      inputs: [],
                      optionalInputs: [editorInput],
                      fullLineInputs: [],
                      fullLineOptionalInputs: []
                    });
                  }
                } else {
                  if (editorInput.multipleInputs) {
                    uiGroups.push({
                      uiGroup: uiGroup,
                      inputs: [],
                      optionalInputs: [],
                      fullLineInputs: [editorInput],
                      fullLineOptionalInputs: []
                    });
                  } else {
                    uiGroups.push({
                      uiGroup: uiGroup,
                      inputs: [editorInput],
                      optionalInputs: [],
                      fullLineInputs: [],
                      fullLineOptionalInputs: []
                    });
                  }
                }
              } else {
                if (isOptional) {
                  if(editorInput.multipleInputs) {
                    uiGroupCompare?.fullLineOptionalInputs.push(editorInput);
                  } else {
                    uiGroupCompare?.optionalInputs.push(editorInput);
                  }
                } else {
                  if (editorInput.multipleInputs) {
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
    return uiGroups;
  }

  /**
   * Partitions the given uiGroups into a pseudo layout to calculate the expected size of dialogs from.
   * 
   * @param uiGroups the UI groups to partition
   * @returns the partitioned UI groups, currently as maximal two column layout per uiGroup
   */
  public partitionUiGroups(uiGroups: uiGroup[]) {
    let result : EditorPartition[] = [];
    let actual : EditorPartition | null = null;
    let group = 1;
    let cols = 1;
    for (let u of uiGroups) {
      // well, it was defined that way :/
      for (let ei of u.inputs.concat(u.optionalInputs, u.fullLineInputs, u.fullLineOptionalInputs)) {
        if (ei) {
          let c = 1;
          if (DataUtils.isIvmlCollection(ei.type)) {
            c = 2;
          }
          if (!actual || u.uiGroup != group || c != cols) {
            actual = { count : 1, columns : c};
            result.push(actual);
            group = u.uiGroup;
            cols = c;
          } else {
            actual.count++;
          }
        }
      }
    }
    return result;
  }

  /**
   * Derives for the given IVML type and the value structure an available/usable IVML variable name.
   * 
   * @param type the IVML type name
   * @param data the data to be used as IVML value
   * @returns a promise on the variable name
   */
  public async generateVariableName(type: string, data: IvmlRecordValue | null) {
    let result;
    let elementName = "";
    let elementVersion = "";
    if (data) {
      if (data["name"]) {
        elementName = data["name"].value;
      }
      if (data["version"]) {
        elementVersion = data["version"].value;
      }
    }

    let params: InputVariable[] = [];
    params.push(ApiService.createAasOperationParameter("type", AAS_TYPE_STRING, type));
    params.push(ApiService.createAasOperationParameter("elementName", AAS_TYPE_STRING, elementName));
    params.push(ApiService.createAasOperationParameter("elementVersion", AAS_TYPE_STRING, elementVersion));
    const response = await this.api.executeAasJsonOperation(IDSHORT_SUBMODEL_CONFIGURATION, 
      AAS_OP_PREFIX_SME + "getVariableName", params);
    let opResult = this.api.getPlatformResponse(response);
    if (opResult?.result) {
      result = opResult?.result;
    } else { // a bit fallback
      result = type;    
      if (data) {
        let name = data["name"];
        if (name) {
          type += "_" + name.value;
        }
      }
      if (type.length > 0) {
        type = type[0].toLowerCase() + type.substring(1);       
      }
      result = type;
    }
    return type; // preliminary
  }

  /**
   * Documentation in src/assets/doc/filterMeta.jpg
   */
  public filterMeta(metaBackup: Resource, category: string = 'all') {
    let meta = DataUtils.deepCopy(metaBackup) // recovering meta
    let filter = reqTypes.find(type => type.cat === category)
    let newMetaValues = []
    if (meta && meta.value) {
      for (const item of meta.value) {
        let idShort = ""
        if (item.idShort) {
          idShort = item.idShort
        }

        if (!this.isAbstract(item)) {
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
                if (this.isSubtype(meta, metaRefVal, category)) {
                  newMetaValues.push(item)
                }
              }
            } else {
              // toplevel type
              if (filter?.metaRef.includes(idShort)) {
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
    meta!.value = newMetaValues;
    return meta;
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
  private isSubtype(meta: Resource, metaRefines_value: any, category: string):boolean{
    let parent_item = this.getParentItem(meta, metaRefines_value)
    if (parent_item) {
      let filter = reqTypes.find(type => type.cat === category)
      if (filter?.metaRef.includes(this.getMetaRef(parent_item))) {
        return true
      } else {
        let metaRef_val_parent = this.getMetaRef(parent_item)
        if(metaRef_val_parent) {
          return this.isSubtype(meta, metaRef_val_parent, category) //recursion
        } else {
          return false
        }
      }
    } else {
      return false
    }
  }

  private getParentItem(meta: Resource, metaRefines: string) {
    let result = meta?.value?.find(item => item.idShort === metaRefines)
    if(result) {
      return result
    } else {
      return null
    }
  }

}

/* metaRef list:
  empty -> returns ivml types
  includes names of toplevel types -> returns the toplevel type (if not abstract) and all subtypes
  */
const reqTypes = [
    {cat: "Constants", metaRef: []},
    {cat: "Types", metaRef: ["RecordType", "ArrayType"]},
    {cat: "Dependencies", metaRef: ["Dependency"]},
    {cat: "Nameplates", metaRef: ["NameplateInfo"]},
    {cat: "Services", metaRef: ["Service"]},
    {cat: "Servers", metaRef: ["Server"]},
    {cat: "Meshes", metaRef: ["ServiceMesh"]},
    {cat: "Applications", metaRef: ["Application"]}
  ];

  // ivml data types
//const REF_TO = "refTo";
//const LIST = "list";
