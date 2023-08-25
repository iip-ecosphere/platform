import { primitiveDataTypes } from './env-config.service';
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class IvmlFormatterService {

  constructor() { }

  public getIvml(variableName: string, data: any, type: string) {
    // replacing whitespaces with underline
    variableName = this.replaceWhitespaces(variableName)
    // removing empty entries
    for(const key in data) {
      if (data[key] === "") { //|| Object.keys(data[key]).length === 0) {
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
      ivml += "{\n"
      let i = 0


      for(const key in data) {
        //console.log("key: " + key)
        // quotes or no qoutes
        if (typeof data[key] == "string") {
          ivml += key + " = \"" + data[key] + "\""
        } else if (typeof data[key] == "object") {
          //console.log("\t value is a object")
          //console.log(data[key])
          //this.getObjectType(data[key])
          ivml += key + " = " + this.createList(data[key])
        } else {
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

  replaceWhitespaces(value: string) {
    let temp = value.split(' ')
    return temp.join('_')
  }

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
  }
  /*
  getObjectType(object: any) {
    let type = "string"
    let first_elemt = object[0]
    console.log("1st elemt: ")
    console.log(first_elemt)
    let isString = typeof first_elemt == "string"
    console.log("Is a string? " + isString)
    if (isString) {
      // string or refTo
      console.log("Is refTo type? " + first_elemt.startsWith("refTo"))
      if (first_elemt.startsWith("refTo")) {
        type = "refTo"
      }
    } else {
      // setOf or sequenceOf
      console.log("1st element is a object? " +  typeof first_elemt == "object")
      console.log("Length of the object " + first_elemt.length)
    }
    console.log("Type is " + type)
    return type
  }
  */

  // TODO
  public getCreateVarInputVar(data: any, name: string) {

    let input1 = 4

  }

}
