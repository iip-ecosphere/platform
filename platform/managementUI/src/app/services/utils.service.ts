import { Injectable } from '@angular/core';
import { editorInput } from 'src/interfaces';

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
    if (typeof value == "string" && value.length > 0) {
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
   * Returns the description text of an AAS-inspired language string.
   * 
   * @param text the AAS-inspired language string 
   * @returns the description text, i.e., the language indicator stripped off
   */
  public static getLangStringText(text: string) {
    let result: string = text;
    if (text) {
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
