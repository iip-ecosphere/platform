import { TestBed } from '@angular/core/testing';

import { IvmlFormatterService } from './ivml-formatter.service';
import { HttpClientModule } from '@angular/common/http';
import { IVML_TYPE_Boolean, IVML_TYPE_Integer, IVML_TYPE_PREFIX_enumeration, IVML_TYPE_Real, IVML_TYPE_String } from 'src/interfaces';

describe('IvmlFormatterService', () => {

  const TIMEOUT_LIFECYCLE_MS = 20000;
  let service: IvmlFormatterService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ HttpClientModule ]
    });
    service = TestBed.inject(IvmlFormatterService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should translate IVML variables with basic types', () => {
    exp(service.getIvml("var", {value:{value:25, _type:IVML_TYPE_Integer}}, IVML_TYPE_Integer), 
      "var", IVML_TYPE_Integer, "25");
    exp(service.getIvml("var", {value:{value:"abba", _type:IVML_TYPE_String}}, IVML_TYPE_String), 
      "var", IVML_TYPE_String, '"abba"');
    exp(service.getIvml("var", {value:{value:true, _type:IVML_TYPE_Boolean}}, IVML_TYPE_Boolean), 
      "var", IVML_TYPE_Boolean, "true");
    exp(service.getIvml("var", {value:{value:1.23, _type:IVML_TYPE_Real}}, IVML_TYPE_Real), 
      "var", IVML_TYPE_Real, "1.23");
  });

  it('should translate IVML variables with enum types', () => {
    exp(service.getIvml("var", {value:{value:IVML_TYPE_PREFIX_enumeration+"MyEnum::VAL", _type:"MyEnum"}}, "MyEnum"), 
      "var", "MyEnum", 'MyEnum::VAL');
  })

  it('should translate IVML variables with reference type', () => {
    exp(service.getIvml("var", {value:{value:"varA", _type:"refTo(MyComp)"}}, "refTo(MyComp)"), 
      "var", "refTo(MyComp)", 'refBy(varA)');
  })

  it('should translate IVML variables with collection types', () => {
    let array = [{value: "a", _type:IVML_TYPE_String}, {value: "b", _type:IVML_TYPE_String}, {value: "c", _type:IVML_TYPE_String}];
    exp(service.getIvml("var", {value:{value:array, _type:"sequenceOf(String)"}}, "sequenceOf(String)"), 
      "var", "sequenceOf(String)", '{"a","b","c"}');
    exp(service.getIvml("var", {value:{value:array, _type:"setOf(String"}}, "setOf(String)"), 
      "var", "setOf(String)", '{"a","b","c"}');
  })

  it('should translate IVML variables with compound type', () => {
    exp(service.getIvml("var", {a:{value: 25, _type:IVML_TYPE_Integer}, b:{value: "abba", _type:IVML_TYPE_String}}, "RecordType"), 
      "var", "RecordType", '{a=25,b="abba"}');
    let array = [{value: "a", _type:IVML_TYPE_String}, {value: "b", _type:IVML_TYPE_String}, {value: "c", _type:IVML_TYPE_String}];
    exp(service.getIvml("var", {a:{value: array, _type:"sequenceOf(String)"}, b:{value: "abba", _type:IVML_TYPE_String}}, "RecordType"), 
      "var", "RecordType", '{a={"a", "b", "c"},b="abba"}');
    let comp = {a:{value: "10", _type:IVML_TYPE_Integer}, b:{value: "b", _type:IVML_TYPE_String}, c:{value: true, _type:IVML_TYPE_Boolean}};
    exp(service.getIvml("var", {a:{value: comp, _type:"MyComp"}, b:{value: "abba", _type:IVML_TYPE_String}}, "RecordType"), 
    "var", "RecordType", '{a=MyComp{a=10, b="b", c=true},b="abba"}');
  });

  it('should create/edit/delete IVML variables with basic type', async() => {
    await testVarLifecycle(service, "testIntVar", 25, 26, IVML_TYPE_String);
    await testVarLifecycle(service, "testBoolVar", true, false, IVML_TYPE_Boolean);
    await testVarLifecycle(service, "testRealVar", 0.01, 10.1, IVML_TYPE_Real);
    await testVarLifecycle(service, "testStringVar", "ab", "cd", IVML_TYPE_String);
  }, 4 * TIMEOUT_LIFECYCLE_MS);

  it('should create/edit/delete IVML variables with collection type', async() => {
    await testVarLifecycle(service, "testSequenceInt", [25], [25, 26], "sequenceOf(Integer)");
    await testVarLifecycle(service, "testSetInt", [25], [-25, 26], "setOf(Integer)");
  }, 2 * TIMEOUT_LIFECYCLE_MS);

  it('should create/edit/delete IVML variables with reference type', async() => {
    await testVarLifecycle(service, "testRefVar", "UNUSED_Integer", "deviceHeartbeatTimeout", "refTo(Integer)");
  }, 1 * TIMEOUT_LIFECYCLE_MS);

  it('should create/edit/delete IVML variables with compound type', async() => {
    await testVarLifecycle(service, "testMvnRepo", 
      {id:{value:"myRepo", _type:IVML_TYPE_String}, url:{value:"127.0.0.1:8ß8ß", _type:IVML_TYPE_String}}, 
      {id:{value:"myRepo", _type:IVML_TYPE_String}, name:{value:"MyRepo", _type:IVML_TYPE_String}, url:{value:"127.0.0.1:8ß81", _type:IVML_TYPE_String}}, 
      "MavenRepository");
  }, 1 * TIMEOUT_LIFECYCLE_MS);

});

function exp(data: string[], varName: string, type: string, ivml: string):void {
  expect(data[0]).toBe(varName);
  expect(data[1]).toBe(type);
  expect(String(data[2]).replace(/[\s\r\n]/g, "")).toBe(ivml.replace(/[\s\r\n]/g, ""));
}

async function testVarLifecycle(service: IvmlFormatterService, varName: string, val1: any, val2: any, varType: string) {
  let uf = await service.createVariable(varName, {value:{value:val1, _type:varType}}, varType);
  expect(uf.successful).toBeTrue();
  uf = await service.setVariable(varName, {value:{value:val2, _type:varType}}, varType);
  expect(uf.successful).toBeTrue();
  uf = await service.deleteVariable(varName);
  expect(uf.successful).toBeTrue();
}