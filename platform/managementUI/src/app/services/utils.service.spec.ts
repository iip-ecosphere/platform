import { TestBed } from '@angular/core/testing';

import { UtilsService, retry } from './utils.service';

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

  it('should implement getProperty', () => {
    expect(service.getProperty(data, "xx")).toBeUndefined();
    expect(service.getPropertyValue(data, "xx")).toBeUndefined();

    let d: any[] = [];
    expect(service.getProperty(d, "xx")).toBeUndefined();
    expect(service.getPropertyValue(d, "xx")).toBeUndefined();
    d = [{idShort: "xx"}, {idShort: "yy"}];
    expect(service.getProperty(d, "xx")).toBeDefined();
    expect(service.getPropertyValue(d, "xx")).toBeUndefined();
    d = [{idShort: "xx", value:"abc"}, {idShort: "yy", value:"def"}];
    expect(service.getProperty(d, "xx")).toBeDefined();
    expect(service.getPropertyValue(d, "xx")).toBe("abc");
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
