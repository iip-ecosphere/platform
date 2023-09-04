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

    // TODO - it is only for testing
    //data = this.t

    // removing empty entries
    for(const key in data) {
      console.log("removing key: " + key)
      console.log(data[key])
      console.log(typeof data[key])
      if (data[key] === "") {
        delete data[key]
      }
      if (this.nonVisibleValues.includes(key)) {
        delete data[key]
      }
      if (Array.isArray(data[key]) && Object.keys(data[key]).length === 0) {
        delete data[key]
      }
      /*
      if (typeof data[key] && Object.keys(data[key]).length === 0) {
        delete data[key]
      }*/
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
        console.log("not primitive data")
        console.log(key + " " + data[key])
        if (typeof data[key] == "string") {  // string inside non-primitive type
          console.log("string")
          //ivml += key + " = \"" + data[key] + "\""
          ivml += key + " = " + this.convertToIvml(data[key])

        } else if (typeof data[key] == "object") {
          // refTo, setOf, sequenceOf ------------------------------------------------
          console.log("object")

          /*
          ivml += key + " = "
          if (data[key].length > 1) {
            ivml += "{"
            let j = 0

            if (typeof data[key][0] === "object") {
              // sequence with more than one entry
              ivml += this.handleIvmlSeq(data[key]) + "}"

            } else {
              // refTo or setOf

              for (let elemt of data[key]) {
                let return_val = this.convertToIvml(elemt)
                ivml += return_val[0]

                // no comma after the last value
                if (j < (Object.keys(data[key]).length - 1)) {
                  ivml += ","
                }
                j += 1
              }
              ivml += "}"
            }

          } else {
            // setOf or sequenceOf with only one element
            if (typeof data[key][0] === "object") {
              // ivml sequence
              ivml += "{" + this.handleIvmlSeq(data[key]) + "}"

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
          */
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
      // probably refTo
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
      result += "{"
      for (const [key, value] of Object.entries(elemt)) {

        let ivml_value = this.convertToIvml(value)[0]
        result += key + "=" + ivml_value

        // no comma after the last value
        if (j < (Object.entries(elemt).length - 1)) {
          result += ","
        }
        j += 1
      }
      // no comma after the last value
      if (i < (Object.entries(seq).length - 1)) {
        result += "}, "
      }
      i += 1
    }
    return result += "}"
  }
  /* only for testing
  t_str = 'test value'
  t_ref = ['refTo(dkakak)']
  t_setOf = ['ddd', 'fff']
  t_setOf1 = ['ddd']
  t_setOfRef = ['refTo(d)', 'refTo(a)']
  t_seq = [{type: ['refTo(a)'], forward: false},
    {type: ['refTo(b)'], forward: true}]
  t = {test_value: this.t_str, ex_ref: this.t_ref,
      ex_setOf_1: this.t_setOf1, ex_setOf: this.t_setOf, ex_setOfRef: this.t_setOfRef}
    */

  replaceWhitespaces(value: string) {
    let temp = value.split(' ')
    return temp.join('_')
  }
  /*
  createList(data:any) {
    let result = "{"
    let i = 0
    for (let elemt of data) {
      result += elemt
      if (i < data.length - 1) {
        result += ","
      }
      i += 1
    }
    return result + "}"
  }*/

  // TODO
  public getCreateVarInputVar(data: any, name: string) {

    let input1 = 4

  }

}
