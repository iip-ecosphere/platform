//import { type } from 'os';
import { InputVariable, primitiveDataTypes, IVML_TYPE_PREFIX_enumeration, JsonPlatformOperationResult, IvmlRecordValue, IVML_TYPE_String, IVML_TYPE_Boolean, IvmlValue, UserFeedback } from 'src/interfaces';
import { Injectable } from '@angular/core';
import { AAS_OP_PREFIX_SME, AAS_TYPE_STRING, ApiService, GRAPHFORMAT_DRAWFLOW, IDSHORT_SUBMODEL_CONFIGURATION } from './api.service';
import { DataUtils, UtilsService } from './utils.service';

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
    /*const response = await this.api.executeAasJsonOperation(IDSHORT_SUBMODEL_CONFIGURATION, 
      AAS_OP_PREFIX_SME + "createVariable", inputVar);

    let exception = this.api.getPlatformResponse(response);
    return this.getFeedback(exception, "Values have been successfully stored!");*/
  }

  /*
    private getCreateVarInputVar(data: any) {
    let inputVariables: InputVariable[] = [];
    inputVariables.push(ApiService.createAasOperationParameter("varName", AAS_TYPE_STRING, data[0]));
    inputVariables.push(ApiService.createAasOperationParameter("type", AAS_TYPE_STRING, data[1]));
    inputVariables.push(ApiService.createAasOperationParameter("valExpr", AAS_TYPE_STRING, data[2]));
    return inputVariables
  }*/

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

}

  // ivml data types
//const REF_TO = "refTo";
//const LIST = "list";
