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
    expect(service.isArray(null)).toBeFalse();
    expect(service.isArray(1)).toBeFalse();
    expect(service.isArray([])).toBeTrue();
    expect(service.isArray(["a"])).toBeTrue();
  });

  it('should implement isObject', () => {
    expect(service.isObject(undefined)).toBeFalse();
    expect(service.isObject(null)).toBeTrue();
    expect(service.isObject(1)).toBeFalse();
    expect(service.isObject({a:"b"})).toBeTrue();
    expect(service.isObject([])).toBeTrue();
    expect(service.isObject("abba")).toBeFalse();
  });

  it('should implement isNumber', () => {
    expect(service.isNumber(undefined)).toBeFalse();
    expect(service.isNumber(null)).toBeFalse();
    expect(service.isNumber(1)).toBeTrue();
    expect(service.isNumber({a:"b"})).toBeFalse();
    expect(service.isNumber([])).toBeFalse();
    expect(service.isNumber("abba")).toBeFalse();
    expect(service.isNumber(true)).toBeFalse();
  });

  it('should implement isBoolean', () => {
    expect(service.isBoolean(undefined)).toBeFalse();
    expect(service.isBoolean(null)).toBeFalse();
    expect(service.isBoolean(1)).toBeFalse();
    expect(service.isBoolean({a:"b"})).toBeFalse();
    expect(service.isBoolean([])).toBeFalse();
    expect(service.isBoolean("abba")).toBeFalse();
    expect(service.isBoolean(true)).toBeTrue();
  });

  it('should implement isString', () => {
    expect(service.isString(undefined)).toBeFalse();
    expect(service.isString(null)).toBeFalse();
    expect(service.isString(1)).toBeFalse();
    expect(service.isString({a:"b"})).toBeFalse();
    expect(service.isString([])).toBeFalse();
    expect(service.isString("abba")).toBeTrue();
    expect(service.isString(true)).toBeFalse();
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

  it('should implement getElementDisplayName', () => {
    // plain string value
    expect(service.getElementDisplayName("", false)).toBe("");
    expect(service.getElementDisplayName("name", false)).toBe("name");
    expect(service.getElementDisplayName("refTo(xyz)", false)).toBe("refTo(xyz)");
    expect(service.getElementDisplayName("refTo(xyz)", true)).toBe("xyz");
    // object with name
    expect(service.getElementDisplayName({name:"abc"}, false)).toBe("abc");
    // AAS config value
    expect(service.getElementDisplayName({value:[{idShort:"name"}, {idShort:"varValue", value: "abc"}]}, false)).toBe("abc");
    expect(service.getElementDisplayName({idShort:"outer", value:[{idShort:"name"}]}, false)).toBe("outer");
    expect(service.getElementDisplayName({idShort:"name"}, false)).toBe("name");
    // editor IVML value
    expect(service.getElementDisplayName({value:"name", _type:"type"}, false)).toBe("name");
    expect(service.getElementDisplayName({value:"refTo(abc)", _type:"refTo(ABC)"}, false)).toBe("abc");
    // nameplate
    expect(service.getElementDisplayName({manufacturer:"refTo(SSE)"}, false)).toBe("SSE"); // preliminary
    expect(service.getElementDisplayName({productImage:"xyz"}, false)).toBe(""); // preliminary
    expect(service.getElementDisplayName({department:"dep"}, false)).toBe("dep"); // preliminary
    // array of editor IVML values
    // [{"type":"feedback","forward":false,"idShort":"feedback","_type":"IOType"}]
    expect(service.getElementDisplayName([
      {type: "feedback", forward: false, idShort: "feedback", _type: "IOType"}
      ], false)).toBe("feedback");
    expect(service.getElementDisplayName([
      {type: "fb", forward: false, idShort: "feedback", _type: "IOType"}, 
      {type: "te", forward: false, idShort: "test", _type: "IOType"}
      ], false)).toBe("feedback, test");
  });

  it('should implement configureDialog', () => {
    // defaults if no groups given
    let res = service.configureDialog("90%", "70%", null);
    expect(res).toBeTruthy();
    expect(res.width).toBe("90%");
    expect(res.height).toBe("70%");
    expect(res.panelClass).toBeTruthy();
    expect(res.panelClass.length).toBeGreaterThan(0);

    // TODO size based on groups
    res = service.configureDialog("90%", "70%", [{count : 1, columns : 1}]);
    expect(res).toBeTruthy();
    res = service.configureDialog("90%", "70%", [{count : 2, columns : 1}]);
    expect(res).toBeTruthy();
    res = service.configureDialog("90%", "70%", [{count : 3, columns : 1}]);
    expect(res).toBeTruthy();
  });

  // -------------------------- Data Utils -----------------------------------

  it('should implement getProperty', () => {
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

  it('should implement isEmpty', () => {
    expect(DataUtils.isEmpty({})).toBeTrue();
    expect(DataUtils.isEmpty({value: 1})).toBeFalse();
    expect(DataUtils.isEmpty(null)).toBeFalse();
    expect(DataUtils.isEmpty(undefined)).toBeFalse();
  });

  it('should realize string to base64 and back encoding/decoding', () =>{
    const original = "Hello, 😊 Angular!";
    const encoded = DataUtils.stringToBase64(original);
    const decoded = DataUtils.base64ToString(encoded);
    expect(decoded).toEqual(original);
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
