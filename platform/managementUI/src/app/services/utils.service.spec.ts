import { TestBed } from '@angular/core/testing';

import { UtilsService, DataUtils, retry } from './utils.service';
import { editorInput } from 'src/interfaces';

describe('UtilsService', () => {

  let service: UtilsService;
  let data: any[];

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(UtilsService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should implement isArray', () => {
    expect(service.isArray(null)).toBeFalsy();
    expect(service.isArray(1)).toBeFalsy();
    expect(service.isArray([])).toBeTruthy();
    expect(service.isArray(["a"])).toBeTruthy();
  });

  it('should implement isObject', () => {
    expect(service.isObject(undefined)).toBeFalsy();
    expect(service.isObject(null)).toBeTruthy();
    expect(service.isObject(1)).toBeFalsy();
    expect(service.isObject({a:"b"})).toBeTruthy();
    expect(service.isObject([])).toBeTruthy();
    expect(service.isObject("abba")).toBeFalsy();
  });

  it('should implement isNonEmptyString', () => {
    expect(service.isNonEmptyString(undefined)).toBeFalsy();
    expect(service.isNonEmptyString(null)).toBeFalsy();
    expect(service.isNonEmptyString(1)).toBeFalsy();
    expect(service.isNonEmptyString("")).toBeFalsy();
    expect(service.isNonEmptyString("abba")).toBeTruthy();
  });

  it('should implement getValue', () => {
    let input: editorInput = {value: 10, name:"var", type:"Integer", description: [{language:"de", text: "abc"}]};
    expect(service.getValue(input)).toBe(10);
    input.valueTransform = i => i.value + 10;
    expect(service.getValue(input)).toBe(20);
  });

  it('should implement getDisplayName', () => {
    let input: editorInput = {value: 10, name:"var", type:"Integer", description: [{language:"de", text: "abc"}]};
    expect(service.getDisplayName(input)).toBe("var");
    input.displayName = "dName";
    expect(service.getDisplayName(input)).toBe("dName");
  });

  // -------------------------- Data Utils -----------------------------------

  it('should implement getProperty', () => {
    expect(DataUtils.getProperty(data, "xx")).toBeUndefined();
    expect(DataUtils.getPropertyValue(data, "xx")).toBeUndefined();

    let d: any[] = [];
    expect(DataUtils.getProperty(d, "xx")).toBeUndefined();
    expect(DataUtils.getPropertyValue(d, "xx")).toBeUndefined();
    d = [{idShort: "xx"}, {idShort: "yy"}];
    expect(DataUtils.getProperty(d, "xx")).toBeDefined();
    expect(DataUtils.getPropertyValue(d, "xx")).toBeUndefined();
    d = [{idShort: "xx", value:"abc"}, {idShort: "yy", value:"def"}];
    expect(DataUtils.getProperty(d, "xx")).toBeDefined();
    expect(DataUtils.getPropertyValue(d, "xx")).toBe("abc");
  });

  it('should implement IVML type helpers', () => {
    expect(DataUtils.isIvmlSet(undefined)).toBeFalsy();
    expect(DataUtils.isIvmlSequence(undefined)).toBeFalsy();
    expect(DataUtils.isIvmlCollection(undefined)).toBeFalsy();
    expect(DataUtils.isIvmlRefTo(undefined)).toBeFalse();

    let type = "setOf(Integer)";
    expect(DataUtils.isIvmlSet(type)).toBeTrue();
    expect(DataUtils.isIvmlSequence(type)).toBeFalse();
    expect(DataUtils.isIvmlCollection(type)).toBeTrue();
    expect(DataUtils.isIvmlRefTo(type)).toBeFalse();

    type = "sequenceOf(Integer)";
    expect(DataUtils.isIvmlSet(type)).toBeFalse();
    expect(DataUtils.isIvmlSequence(type)).toBeTrue();
    expect(DataUtils.isIvmlCollection(type)).toBeTrue();
    expect(DataUtils.isIvmlRefTo(type)).toBeFalse();

    type = "refTo(RecordType)";
    expect(DataUtils.isIvmlSet(type)).toBeFalse();
    expect(DataUtils.isIvmlSequence(type)).toBeFalse();
    expect(DataUtils.isIvmlCollection(type)).toBeFalse();
    expect(DataUtils.isIvmlRefTo(type)).toBeTrue();
  });

  it('should implement IVML strip generic type', () => {
    let type = "setOf(Integer)";
    expect(DataUtils.stripGenericType("setOf(Integer)")).toBe("Integer");
    expect(DataUtils.stripGenericType("Integer")).toBe("Integer");
    expect(DataUtils.stripGenericType("setOf(setOf(Integer))")).toBe("setOf(Integer)");
  });

  it('should implement deepCopy', () => {
    let val = {id:"25"};
    let copy = DataUtils.deepCopy(val);
    expect(copy).toBeTruthy();
    expect(copy.id).toBe(val.id);
  });

  it('should implement toBoolean', () => {
    expect(DataUtils.toBoolean(null)).toBeFalse();
    expect(DataUtils.toBoolean(undefined)).toBeFalse();
    expect(DataUtils.toBoolean("text")).toBeFalse();
    expect(DataUtils.toBoolean("true")).toBeTrue();
    expect(DataUtils.toBoolean("True")).toBeTrue();
    expect(DataUtils.toBoolean(true)).toBeTrue();
    expect(DataUtils.toBoolean("false")).toBeFalse();
  });

  it('should implement IVML langString operations', () => {
    expect(DataUtils.getLangStringText("text")).toBe("text");
    expect(DataUtils.getLangStringText("text@de")).toBe("text");

    expect(DataUtils.getLangStringLang("text@de")).toBe("de");
    expect(DataUtils.getLangStringLang("text")).toBeUndefined();

    expect(DataUtils.composeLangString("text", undefined)).toBe("text");
    expect(DataUtils.composeLangString("text", "en")).toBe("text@en");
  });

  it('should implement userLanguage operation', () => {
    expect(DataUtils.getUserLanguage()).toBeDefined();
    expect(DataUtils.getUserLanguage().length).toBeGreaterThan(0);
  });

  it('should implement string to ArrayBuffer to base64 and back encoding/decoding', () => {
    let text = "My input text";
    let buf = DataUtils.stringToArrayBuffer(text);
    expect(buf).toBeTruthy();
    let base64 = DataUtils.arrayBufferToBase64(buf);
    expect(base64).toBeTruthy();
    let buf2 = DataUtils.base64ToArrayBuffer(base64);
    expect(buf2).toBeTruthy();
    let text2 = DataUtils.arrayBufferToString(buf2);
    expect(text).toBe(text2);
  });


  // -------------------------- retry -----------------------------------

  it('should fail 3 times', async() => {
    let count = 0;
    let caught = false;
    let done = false;

    await retry({
      fn: function () {
        count++;
        return false;
      },
      maxAttempts: 3,
      delay: 500,
    }).then(() => done = true)
      .catch(() => caught = true);

    expect(done).toBeFalse();
    expect(caught).toBeTrue();
    expect(count).toEqual(3);
  });

  it('should succeed', async() => {
    let count = 0;
    let caught = false;
    let done = false;

    await retry({
      fn: function () {
        count++;
        return true;
      },
      maxAttempts: 4,
      delay: 500,
    }).then(() => done = true)
      .catch(() => caught = true);

    expect(done).toBeTrue();
    expect(caught).toBeFalse();
    expect(count).toEqual(1);
  });

  it('should succeed second time', async() => {
    let count = 0;
    let caught = false;
    let done = false;

    await retry({
      fn: function () {
        count++;
        return count > 1;
      },
      maxAttempts: 3,
      delay: 500,
    }).then(() => done = true)
      .catch(() => caught = true);

    expect(done).toBeTrue();
    expect(caught).toBeFalse();
    expect(count).toEqual(2);
  });

});
