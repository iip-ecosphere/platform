//import { type } from 'os';
import { primitiveDataTypes } from './env-config.service';
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class IvmlFormatterService {

  constructor() { }

  // ivml data types
  STRING = "string"
  REF_TO = "refTo"
  LIST = "list"
  BOOL = "boolean"

  nonVisibleValues = ["optional"]

  public getIvml(variableName: string, data: any, type: string) {
    // replacing whitespaces with underline
    variableName = this.replaceWhitespaces(variableName)
    console.log("[ivmlFormatter | getIvml] ivml type: " + type)

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
    console.log("[ivmlFormatter | getIvml] clean data --------- ")
    console.log(data)
    console.log("--------------------")

    let ivml = type + " " + variableName + " = "

    if (primitiveDataTypes.includes(type)) {
      if (type === "String") {
        ivml += "\"" + data["value"] + "\";"
      } else {
        ivml += data["value"] + ";"
      }
    } else {
      // non-primitive types ---------------------------------------------------------
      ivml += "{\n"
      let i = 0

      for (const key in data) {
        if (typeof data[key] == "string") {  // refTo, string inside non-primitive type
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
      ivml += "\n};"
    }
    console.log("RESULT: \n" + ivml)
    return ivml
  }

  private convertToIvml(elemt:any) {
    let result = ["", null]                   // ivml, type (refTo, string, number, list)
    if (typeof elemt === this.STRING) {
      if (elemt.startsWith(this.REF_TO)) {
        // refTo
        elemt = elemt.replace("refTo", "refBy")
        result = [elemt, this.REF_TO]

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

    //TODO number
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

  // TODO
  public getCreateVarInputVar(data: any, name: string) {

    let input1 = 4

  }

}
