//import { type } from 'os';
import { InputVariable, platformResponse } from 'src/interfaces';
import { primitiveDataTypes, ivmlEnumeration } from './env-config.service';
import { Injectable } from '@angular/core';
import { ApiService } from './api.service';

@Injectable({
  providedIn: 'root'
})
export class IvmlFormatterService {

  constructor(public api:ApiService) { }

  // ivml data types
  STRING = "string"
  REF_TO = "refTo"
  LIST = "list"
  BOOL = "boolean"

  nonVisibleValues = ["optional"]

  success_feedback = "Object successfully created!"
  state_feedback = "The operation \'setGraph\' has been invoked."

  public async createVariable(variableName: string, data: any, type: string) {
    let ivmlFormat = this.getIvml(variableName, data, type)
    let inputVar = this.getCreateVarInputVar(ivmlFormat)
    console.log("[ivml-formatter | create] input var")
    console.log(inputVar)

    let resourceId = ""
    let aasElementURL = "/aas/submodels/Configuration/submodel/submodelElements/"
    let basyxFun = "/createVariable"

    const response = await this.api.executeFunction(
      resourceId,
      aasElementURL,
      basyxFun,
      inputVar) as unknown as platformResponse

    let exception = this.getPlatformResponse(response)
    return this.getFeedback(exception)
  }

  public getPlatformResponse(response:platformResponse) {
    let output = null
    if(response && response.outputArguments) {
      output = response.outputArguments[0]?.value?.value;
    }
    return output
  }

  private getFeedback(exception: any) {
    let result = this.success_feedback
    console.log(exception)
    if (exception != "{}") {
      exception = exception.substring(1, exception.length - 1)
      result = "Exception: " + exception.replace("\"exception\":", "")
    }
    return result
  }

  private getGraphFeedback(exception: any) {
    let result = this.state_feedback
    console.log(exception)
    if (exception != "{}") {
      exception = exception.substring(1, exception.length - 1)
      result = "Exception: " + exception.replace("\"exception\":", "")
    }
    return result
  }

  public getIvml(variableName: string, data: any, type: string) {
    // replacing whitespaces with underline
    variableName = this.replaceWhitespaces(variableName)

    // removing empty entries
    for(const key in data) {
      if (data[key] === "") {
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
      if (typeof data[key] === 'object' && Object.keys(data[key]).length === 0) {
        delete data[key]
      }
    }

    let ivml = ""

    if (primitiveDataTypes.includes(type)) {
      if (type === "String") {
        ivml += "\"" + data["value"] + "\""
      } else {
        ivml += data["value"]
      }
    } else {
      // non-primitive types ---------------------------------------------------------
      ivml += "{\n"
      let i = 0

      for (const key in data) {
        if (key == "port") {  // TODO maybe input check will be there some day
          ivml += key + " = " + Number(data[key])

        } else if (typeof data[key] == "string") {
          // refTo, string inside non-primitive type, ivml enum
          ivml += key + " = " + this.convertToIvml(data[key])[0]

        } else if (Array.isArray(data[key])) {
          // setOf, sequenceOf ---------------------------------------------
          ivml += key + " = {"
          if (data[key].length > 1) {
            let j = 0

            if (typeof data[key][0] === "object") {
              // sequence with more than one entry
              ivml += this.handleIvmlSeq(data[key])

            } else {
              for (let elemt of data[key]) {
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
            if (typeof data[key][0] === "object") {
              // ivml sequence
              ivml += this.handleIvmlSeq(data[key])

            } else {
              // refTo, string or boolean
              let elemt = data[key][0]
              let return_val = this.convertToIvml(elemt)
              if (return_val[1] === this.LIST) {
                ivml += "{" + return_val[0] + "}"
              } else {
                ivml += return_val[0]
              }
            }
          }
          ivml += "}"

        } else if (typeof data[key] == "object") {
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
      }
      ivml += "\n}"
    }
    console.log("[ivml-formatter | getIvml]: object as ivml:\n" + ivml)

    let result =  [variableName, type, ivml]
    return result
  }

  private convertToIvml(elemt:any) {
    let result = ["", null]                   // ivml, type (refTo, string, number, list)
    if (typeof elemt === this.STRING) {
      if (elemt.startsWith(this.REF_TO)) {
        // refTo
        elemt = elemt.replace("refTo", "refBy")
        result = [elemt, this.REF_TO]

      } else if (elemt.startsWith(ivmlEnumeration)) {
        // ivml enums
        elemt = elemt.replace(ivmlEnumeration, "")
        result = [elemt, null]
      } else {
        // string ''
        result = ["\"" + elemt + "\"", this.STRING]
      }
    } else if (Array.isArray(elemt)) {
      result = this.convertToIvml(elemt[0]) // TODO do also for array with multipy elements
    } else if (typeof elemt === 'boolean') {
      result = [String(elemt), this.BOOL] // TODO do I need this info about boolean?
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
  }

  replaceWhitespaces(value: string) {
    let temp = value.split(' ')
    return temp.join('_')
  }

  public getCreateVarInputVar(data: any) {
    let inputVariables: InputVariable[] = [];
    let input0:InputVariable = {
      value: {
        modelType: {
          name: "Property"
        },
        valueType: "string",
        idShort: "varName",
        kind: "Template",
        value: data[0]
      }
    }
    let input1:InputVariable = {
      value: {
        modelType: {
          name: "Property"
        },
        valueType: "string",
        idShort: "type",
        kind: "Template",
        value: data[1]
      }
    }
    let input2:InputVariable = {
      value: {
        modelType: {
          name: "Property"
        },
        valueType: "string",
        idShort: "valExpr",
        kind: "Template",
        value: data[2]
      }
    }
    inputVariables.push(input0)
    inputVariables.push(input1)
    inputVariables.push(input2)

    return inputVariables
  }
  // ------------- setGraph --------------------------------------------------
  public createApp(appName:string, data:any) {
    let ivmlFormat = this.getIvml(appName, data, "")[2]
    let feedback = this.setGraph(appName, ivmlFormat, "", "")
    return feedback
  }

  public async setGraph(appName: string, appValExpr: string,
    serviceMeshName:string, val:string) {
    let inputVar = this.getSetGraphInputVar(appName, appValExpr,
      serviceMeshName, val)
    //console.log("[ivml-formatter | setGraph] input variables")
    //console.log(inputVar)

    let resourceId = ""
    let aasElementURL = "/aas/submodels/Configuration/submodel/submodelElements/"
    let basyxFun = "/setGraph"

    const response = await this.api.executeFunction(
      resourceId,
      aasElementURL,
      basyxFun,
      inputVar) as unknown as platformResponse

    console.log("Platform response: ")
    console.log(response)
    let exception = this.getPlatformResponse(response)
    let result = this.getGraphFeedback(exception)
    return result
  }

  public getSetGraphInputVar(appName:string, appValExpr:string,
    serviceMeshName:string, val:string ) {
    let inputVariables: InputVariable[] = [];
    let input0 = this.getInputVar("appName", appName)
    let input1 = this.getInputVar("appValExpr", appValExpr)
    let input2 = this.getInputVar("serviceMeshName", serviceMeshName)
    let input3 = this.getInputVar("format", "drawflow")
    let input4 = this.getInputVar("val", val)

    inputVariables.push(input0, input1, input2, input3, input4)

    return inputVariables
  }

  getInputVar(idShort:string, value:any) {
    let result:InputVariable = {
      value: {
        modelType: {
          name: "Property"
        },
        valueType: "string",
        idShort: idShort,
        kind: "Template",
        value: value
      }
    }
    return result
  }
}
