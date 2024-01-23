import { Injectable } from '@angular/core';
import { editorInput, uiGroup } from 'src/interfaces';

// for naming conventions, keep utility methods here
/**
 * Utils basic class providing some repeated function, inteneded to inherited.
 */
export class Utils {

  constructor() { }

  /**
   * Returns whether value is an array, mapped into a member method for components/templates. 
   *  
   * @param value the value to check
   * @returns true if array, false else
   */
  public isArray(value: any) {
    return Array.isArray(value);
  }

  /**
   * Returns whether value is a non-empty string.
   * 
   * @param value the value to check 
   * @returns true for a non-empty string, false else
   */
  public isNonEmptyString(value: any) {
    let result = false
    if (this.isString(value) && value.length > 0) {
      result = true
    }
    return result
  }

  /**
   * Detect whether value is an "object" (null, prototype instance, array but not number, string or undefined).
   * 
   * @param value the value to test 
   * @returns true for object, false else
   */
  public isObject(value: any) {
    return (typeof value === 'object');
  }

  /**
   * Detect whether value is an "string".
   * 
   * @param value the value to test 
   * @returns true for string, false else
   */
  public isString(value: any) {
    return (typeof value === 'string');
  }

   /**
   * Detect whether value is a number.
   * 
   * @param value the value to test 
   * @returns true for number, false else
   */
  public isNumber(value: any) {
    return typeof value === 'number' && !Number.isNaN(value);
  }

  /**
   * Detect whether value is a boolean.
   * 
   * @param value the value to test 
   * @returns true for boolean, false else
   */
  public isBoolean(value: any) {
    return typeof value === 'boolean';
  }

  /**
   * Returns the value of input considering the value transformation function of input.
   * 
   * @param input the input 
   * @returns the value of input
   */
  public getValue(input: editorInput | undefined) {
    let result = undefined;
    if (input) {
      result = input.value;
      if (input.valueTransform) {
        result = input.valueTransform(input);
      }
    }
    return result;
  }

  /**
   * Returns the display name of input, if defined the display name else the name.
   * 
   * @param input the input 
   * @returns the (display) name
   */
  public getDisplayName(input: editorInput) {
    let result = input.name;
    if (input.displayName) {
      result = input.displayName;
    }
    return result;
  }

  /**
   * Returns the display name of an element/value.
   * 
   * @param element the element/value
   * @param refTo is element supposed to be an IVML reference, i.e., refTo(x)
   * @returns the display name
   */
  public getElementDisplayName(element: any, refTo: boolean | undefined) {
    if (typeof(element) === 'string') {
      // plain string value
      if (refTo) {
        return DataUtils.stripGenericType(element);
      } else {
        return element
      }
    } else if(element.name){
      // object with name
      return element.name;
    } else {
      // neseted AAS config value
      let idShort = '';
      if (this.isArray(element.value)) {
        let temp = element.value?.find((item: { idShort: string; }) => item.idShort == 'varValue');
        if (temp && temp.idShort) {
          idShort = temp.value;
        } else {
          idShort = element.idShort;
        }
      } else if (element.idShort) {
        // AAS top-level value
        idShort = element.idShort;
      } else if (element.value && element._type) {
        // editor IVML value
        if (DataUtils.isIvmlRefTo(element._type)) {
          idShort = this.getElementDisplayName(element.value, true);
        } else {
          idShort = element.value;
        }
      } else if (element.hasOwnProperty("productImage") || element.hasOwnProperty("manufacturerProductDesignation") || element.manufacturer) {
        // nameplate
        if (element.manufacturer) {
          idShort = this.getElementDisplayName(element.manufacturer, true); // shall find manufacturer and return name
        } // else empty
      } else if (this.isArray(element)) {
        // array of editor IVML values
        for (let e of element) {
          if (idShort.length > 0) {
            idShort += ", ";
          }
          idShort += this.getElementDisplayName(e, true);
        }
      } else {
        console.log("Unconsidered alternative in getElementDisplayName " + JSON.stringify(element));
      }
      return idShort;
    }
  }

  /**
   * Returns a material dialog configuration. May derive a feasible size from a potential layout of the given groups, in particular
   * for smaller windows where larger default values may lead to superflous dialog window sizes.
   * 
   * @param width the default width as HTML specification (e.g., '90%')
   * @param height the default height as HTML specification (e.g., '90%')
   * @param input the actual input sub-editor partitions for the dialog to open, may be null
   * @returns the configuration object
   */
  public configureDialog(width: string, height: string, input: EditorPartition[] | null) {
    if (input) {
      let cols = 0;
      let rows = 0;      
      for (let part of input) {
        let r = 1;
        let c = 1;
        if (part.count > 1) { // more columns not yet considered!
          if (part.columns == 2) {
            c = 2;
            r = part.count;
          } else { // part.columns == 1
            c = 2; // simple fixed two column layout for now
            r = Math.ceil(part.count / c);
          }
        }
        rows += r;
        cols = Math.max(c, cols);
      }
      let w = 0;
      let h = 0;
      w = Math.max(WIDTH_DIALOG_MIN, cols * WIDTH_CARD + (cols - 1) * WIDTH_CARD_GRID + 3);
      h = Math.max(HEIGHT_DIALOG_MIN, HEIGHT_HEADER + rows * HEIGHT_CARD + (rows - 1) * HEIGHT_CARD_GRID + 2);
      width = `${w}em`
      height = `${h}em`
    }
    return {
      width: width,
      height: height,
      panelClass: 'custom-dialog-container'
    };
  }

}

/**
 * Keeping the naming convention, providing the utils as service.
 */
@Injectable({
  providedIn: 'root'
})
export class UtilsService extends Utils {

  constructor() { 
    super();
  }

}

/**
 * Data utility functions that must not be visible in the scope of a component/template.
 */
export class DataUtils {

  /**
   * Returns an AAS-style property, i.e., searches for an item in data with idShort property and value id.
   * 
   * @param data the data to search
   * @param id the expected value of the idShort property 
   * @returns the item (as property) or undefined
   */
  public static getProperty(data: any[], id: string) {
    if (data && Array.isArray(data)) {
      return data.find((item: { idShort: string; }) => item.idShort === id);
    } else {
      return undefined; // null?
    }
  }

  /**
   * Returns the value of an AAS-style property , i.e., searches for an item in data with idShort property and value 
   * id and returns the item's  value property.
   * 
   * @param data the data to search
   * @param id the expected value of the idShort property 
   * @returns the value or undefined
   */
  public static getPropertyValue(data: any[], id: string) {
    return DataUtils.getProperty(data, id)?.value;
  }

  /**
   * Returns whether the given value starts with string.
   * 
   * @param value the value to check 
   * @param prefix the prefix to check for
   * @returns true for a starting with prefix, false else
   */
  public static startsWith(value: any, prefix: string) {
    return String(value).trim().startsWith(prefix);
  }

  /**
   * Returns whether the given value denotes an IVML set type.
   * 
   * @param value the value to check 
   * @returns true for a set, false else
   */
  public static isIvmlSet(value: any) {
    return DataUtils.startsWith(value,'setOf(');
  }
  
  /**
   * Returns whether the given value denotes an IVML sequence type.
   * 
   * @param value the value to check 
   * @returns true for a sequence, false else
   */
  public static isIvmlSequence(value: any) {
    return DataUtils.startsWith(value,'sequenceOf(');
  }
  
  /**
   * Returns whether the given value denotes an IVML set/sequence type.
   * 
   * @param value the value to check 
   * @returns true for a collection, false else
   */
  public static isIvmlCollection(value: any) {
    return DataUtils.isIvmlSet(value) || DataUtils.isIvmlSequence(value);
  }

    /**
   * Returns whether the given value denotes an IVML reference (type).
   * 
   * @param value the value to check 
   * @returns true for a reference, false else
   */
  public static isIvmlRefTo(value: any) {
    return DataUtils.startsWith(value,'refTo(');
  }

  /**
   * Strips the generic type name, returning the generics.
   * 
   * @param type the type 
   * @returns the generics, or type if there are no generics
   */
  public static stripGenericType(type: string) {
    const startIndex = type.indexOf('(');
    const endIndex = type.lastIndexOf(')');
    if (endIndex > 0) {
      return type.substring(startIndex + 1, endIndex);
    } else {
      return type;
    }
  }

  /**
   * Turns value into a boolean value.
   * 
   * @param value the value
   * @returns the value converted into boolean
   */
  public static toBoolean(value: any): boolean {
    return String(value).toLowerCase() === 'true';
  }

  /**
   * Deliberately considers text as string or as IvmlValue.
   * 
   * @param text the text
   * @returns text or its value
   */
  private static textOrValue(text: any) : string {
    let result;
    if (typeof text === 'string') {
      result = String(text);
    } else {
      if (text.hasOwnProperty("value")) {
        result = text["value"];
      } else {
        result = JSON.stringify(text);
      }
    }
    return result;
  }

  /**
   * Returns the description text of an AAS-inspired language string.
   * 
   * @param text the AAS-inspired language string 
   * @returns the description text, i.e., the language indicator stripped off
   */
  public static getLangStringText(text: string) {
    let result: string = text;
    if (text) {
      text = DataUtils.textOrValue(text);
      const endIndex = text.lastIndexOf('@');
      if (endIndex > 0) {
        result = text.substring(0, endIndex);
      } else {
        result = text;
      }
    }
    return result;
  }

  /**
   * Returns the language indicator of an AAS-inspired language string.
   * 
   * @param text the AAS-inspired language string 
   * @returns the language indicator, i.e., the description text stripped off; may be 
   * undefined if there is no language indicator 
   */
  public static getLangStringLang(text: string): string | undefined {
    let result: string | undefined = text;
    if (text) {
      text = DataUtils.textOrValue(text);
      const endIndex = text.lastIndexOf('@');
      if (endIndex > 0) {
        result = text.substring(endIndex + 1);
      } else {
        result = undefined;
      }
    }
    return result;
  }

  /**
   * Composes an AAS-inspired langString from the given parameters.
   * 
   * @param text the text/description
   * @param lang the optional language
   * @returns the composed language string
   */
  public static composeLangString(text: string, lang?: string): string {
    let result: string = text;
    if (lang) {
      result += "@" + lang;
    }
    return result;
  }

  /**
   * Returns the user language, so far based on the user language of the browser.
   * 
   * @returns the user language, "en" as default
   */
  public static getUserLanguage(): string {
    let userLang = navigator.language;
    if (!userLang) {
      userLang = "en"
    } else {
      userLang = userLang.split('-')[0];
    }
    return userLang;
  }
  
  /**
   * Creates a deep copy of value.
   * 
   * @param value the value to copy 
   * @returns the deep copy
   */
  public static deepCopy<T>(value: T): T {
    return JSON.parse(JSON.stringify(value)); // there might be better ways
  }

  /**
   * Turns an array buffer to a base64 encoded string.
   * 
   * @param buffer the buffer
   * @returns the base64 encoded string
   */
  public static arrayBufferToBase64( buffer: ArrayBuffer ) {
    // https://www.isummation.com/blog/convert-arraybuffer-to-base64-string-and-vice-versa/
    var binary = '';
    var bytes = new Uint8Array( buffer );
    var len = bytes.byteLength;
    for (var i = 0; i < len; i++) {
      binary += String.fromCharCode( bytes[ i ] );
    }
    return window.btoa( binary );
  }

  /**
   * Turns abase64 encoded string to an array buffer.
   * 
   * @param base64 the base64 encoded string
   * @returns the array buffer
   */
  public static base64ToArrayBuffer(base64: string) {
    // https://www.isummation.com/blog/convert-arraybuffer-to-base64-string-and-vice-versa/
    var binary_string =  window.atob(base64);
    var len = binary_string.length;
    var bytes = new Uint8Array( len );
    for (var i = 0; i < len; i++)        {
        bytes[i] = binary_string.charCodeAt(i);
    }
    return bytes.buffer;
  }

  /**
   * Turns an array buffer to a string.
   * 
   * @param buf the array buffer
   * @returns the string
   */
  public static arrayBufferToString(buf: ArrayBuffer) {
    return new TextDecoder().decode(buf);
  }

  /**
   * Turns a string to an array buffer.
   * 
   * @param str the string
   * @returns the array buffer
   */
  public static stringToArrayBuffer(str: string): ArrayBuffer {
    return new TextEncoder().encode(str);
  }

}

/**
 * Properties for {@link retry}. Function to ceck, initial attempt count, attempt delay in ms, maximum attempts.
 */
export interface RetryProps {
  attempts?: number
  delay: number
  fn: () => boolean
  maxAttempts: number
}

/**
 * Retries a given testing function for a given maximum number of retires.
 * 
 * @param param0 retry properties
 * @returns promise to execute retry as await
 */
export function retry({ fn, maxAttempts = 1, delay = 1000, attempts = 1 }: RetryProps) {
  return new Promise((resolve, reject) => {
    if (fn()) resolve(true)
    else {
      if (attempts < maxAttempts) {
        setTimeout(
          () =>
            retry({ fn, maxAttempts, delay, attempts: attempts + 1 })
              .then(() => resolve(true))
              .catch((err) => reject(err)),
          delay
        )
      } else reject('Could not finally resolve promise - retry failed.')
    }
  })
}

/**
 * Abstracted form of irregular grid/cards representing sub-editors.
 */
export interface EditorPartition {
  count: number;
  columns: number;
  // rows
}

export const WIDTH_CARD = 20 + 0.9; // em
export const WIDTH_CARD_GRID = 3;   // em
export const HEIGHT_CARD = 8; // em
export const HEIGHT_HEADER = 5; // em
export const HEIGHT_CARD_GRID = 3;   // em
export const WIDTH_DIALOG_MIN = 25; // em
export const HEIGHT_DIALOG_MIN = 6; // em 