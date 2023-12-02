import { Injectable } from '@angular/core';

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
   * Returns an AAS-style property, i.e., searches for an item in data with idShort property and value id.
   * 
   * @param data the data to search
   * @param id the expected value of the idShort property 
   * @returns the item (as property) or undefined
   */
  public getProperty(data: any[], id: string) {
    if (data) {
      return data.find((item: { idShort: string; }) => item.idShort === id);
    } else {
      return undefined;
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
  public getPropertyValue(data: any[], id: string) {
    return this.getProperty(data, id)?.value;
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
