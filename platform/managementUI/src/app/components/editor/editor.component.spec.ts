import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { EditorComponent } from './editor.component';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatTooltipModule } from '@angular/material/tooltip';
import { FormsModule } from "@angular/forms";
import { EnvConfigService } from '../../services/env-config.service';

describe('EditorComponent', () => {

  let component: EditorComponent;
  let fixture: ComponentFixture<EditorComponent>;

  beforeEach(async () => {
    await EnvConfigService.init();
    await TestBed.configureTestingModule({
        declarations: [EditorComponent],
        schemas: [CUSTOM_ELEMENTS_SCHEMA],
        imports: [FormsModule, MatTooltipModule],
        providers: [
            { provide: MatDialogRef, useValue: {} },
            { provide: MAT_DIALOG_DATA, useValue: [] },
            provideHttpClient(withInterceptorsFromDi()),
        ],
        teardown: {destroyAfterEach: false} // NG0205: Injector has already been destroyed
    }).compileComponents();

    fixture = TestBed.createComponent(EditorComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should handle Constants', async() => {
    let expectedTypes = [
      {name: "String", input:[
        {name: "value", kind: InputKind.text}
      ]}, 
      {name: "Boolean", input:[
        {name: "value", kind: InputKind.bool}
      ]}, 
      {name: "Real", input:[
        {name: "value", kind: InputKind.text}
      ]}, 
      {name: "Integer", input:[
        {name: "value", kind: InputKind.text}
      ]}
    ];
    await test(fixture, component, "Constants", expectedTypes, false);
  });

  it('should handle Types', async() => {
    let expectedTypes = [
      {name: "UAMethodType"}, 
      {name: "UARootMethodType"}, 
      {name: "UARootVariableType"}, 
      {name: "UAObjectTypeType"}, 
      {name: "UAVariableTypeType"}, 
      {name: "UADataType"}, 
      {name: "ArrayType", input:[
        {name: "name", kind: InputKind.text}, {name: "type", kind: InputKind.ref}
      ]}, 
      {name: "RecordType", input:[
        {name: "name", kind: InputKind.text}, {name: "fields", kind: InputKind.ref}
      ]}, 
      {name: "ByteArrayTypeType"}, 
      {name: "IntegerArrayTypeType"}, 
      {name: "DoubleArrayTypeType"}, 
      {name: "StringArrayTypeType"}];
    await test(fixture, component, "Types", expectedTypes, false);
  });

  it('should handle Dependencies', async() => {
    let expectedTypes = [
      {name: "SystemDependency"}, 
      {name: "LinuxSystemDependency", input:[
        {name: "name", kind: InputKind.text}, {name: "key", kind: InputKind.text}
      ]}, 
      {name: "LinuxCommandBasedSystemDependency"}, 
      {name: "PythonDependency", input:[
        {name: "name", kind: InputKind.text}, {name: "version", kind: InputKind.text}
      ]}, 
      {name: "MavenDependency"}
    ];
    await test(fixture, component, "Dependencies", expectedTypes, false);
  });

  it('should handle Nameplates', async() => {
    let expectedTypes = [
      {name: "NameplateInfo", input: [
        {name: "productInfo", kind: InputKind.text}, {name: "manufacturerProductDesignation", kind: InputKind.text},
        {name: "productImage", kind: InputKind.text}, {name: "manufacturer", kind: InputKind.ref}
      ]}
    ];
    await test(fixture, component, "Nameplates", expectedTypes, false);
  });

  it('should handle Services', async() => {
    let serviceInput = [{name:"class", kind:InputKind.text}, {name:"artifact", kind:InputKind.text},
      {name:"description", kind:InputKind.text}, {name:"deployable", kind:InputKind.bool},
      {name:"ver", kind:InputKind.text}, {name:"id", kind:InputKind.text}, 
      {name:"name", kind:InputKind.text}, {name:"kind", kind:InputKind.enum},
      {name:"asynchronous", kind:InputKind.bool}, {name:"nameplateInfo", kind:InputKind.sub}]; 
    let expectedTypes = [
      {name: "TraceToAasService"}, 
      {name: "TraceToAasJavaService"}, 
      {name: "Connector"}, // TODO abstract?
      {name: "ChannelConnector"}, 
      {name: "OpcUaV1Connector"}, 
      {name: "AasConnector"}, 
      {name: "MqttV3Connector", input: serviceInput.concat([
        {name:"port", kind:InputKind.text}, {name:"deviceServiceKey", kind:InputKind.text},
        {name:"inSerializerClass", kind:InputKind.text}, {name:"outSerializerClass", kind:InputKind.text},
        {name:"inChannel", kind:InputKind.text}, {name:"outChannel", kind:InputKind.text},
        {name:"machineFormatter", kind:InputKind.sub}, {name:"machineParser", kind:InputKind.sub},
        {name:"encoding", kind:InputKind.enum}, {name:"host", kind:InputKind.text},
        {name:"inAdapterClass", kind:InputKind.text}, {name:"outAdapterClass", kind:InputKind.text},
        {name:"inInterface", kind:InputKind.sub}, {name:"outInterface", kind:InputKind.ref},
        {name:"samplingPeriod", kind:InputKind.text}, {name:"security", kind:InputKind.sub},
        {name:"mock", kind:InputKind.bool}, {name:"cacheMode", kind:InputKind.enum},
        {name:"parameter", kind:InputKind.text} // TODO check
      ])}, 
      {name: "MqttV5Connector"}, 
      {name: "MqttConnector"}, 
      {name: "JavaService", input: serviceInput}, 
      {name: "PythonService"}, 
      {name: "FlowerFederatedAiService"}, 
      {name: "KodexService"}, 
      {name: "RtsaService"}, 
      {name: "MipMqttV3Connector"}, 
      {name: "NovoAIMqttV3Connector"}];
      await test(fixture, component, "Services", expectedTypes, false);
  });

  it('should handle Server', async() => {
    let expectedTypes = [{
      name:"JavaServer", input: [
        {name:"class", kind:InputKind.text}, {name:"cmdArg", kind:InputKind.text}, 
        {name:"memLimit", kind:InputKind.text}, {name:"id", kind:InputKind.text}, 
        {name:"description", kind:InputKind.text}, {name:"host", kind:InputKind.text}, 
        {name:"port", kind:InputKind.text}, {name:"executable", kind:InputKind.text}, 
        {name:"nameplateInfo", kind:InputKind.sub}, {name:"artifact", kind:InputKind.text}, 
        {name:"running", kind:InputKind.bool}, {name:"parameter", kind:InputKind.text}, 
        {name:"transportChannel", kind:InputKind.text}, {name:"ver", kind:InputKind.text}]}, 
      {name:"PythonServer"}, 
      {name:"FlowerFederatedAiServer"}];
    await test(fixture, component, "Servers", expectedTypes, false);
  });

  it('should handle Meshes', async() => {
    let expectedTypes = [
      {name: "ServiceMesh", input:[
        {name:"description", kind:InputKind.text}
      ]}
    ];
    await test(fixture, component, "Meshes", expectedTypes, false);
  });

  it('should handle Applications', async() => {
    let expectedTypes = [
      {name: "Application", input:[
        {name:"id", kind: InputKind.text}, {name:"name", kind:InputKind.text}, 
        {name:"description", kind:InputKind.text}, {name:"artifact", kind:InputKind.text}, 
        {name:"nameplateInfo", kind:InputKind.sub}, {name:"debug", kind: InputKind.bool},
        {name:"cmdArg", kind:InputKind.text}, {name:"ver", kind:InputKind.text}, 
        {name:"snapshot", kind:InputKind.bool}, {name:"createContainer", kind:InputKind.bool}
      ]}
    ];
    await test(fixture, component, "Applications", expectedTypes, false);
  });

});

interface TypeProps {
  name:string
  input?: InputProps[] 
}

interface InputProps {
  name: string
  kind: InputKind
}

enum InputKind {
  text, // text input
  bool, // boolean selector
  enum, // boolean selector
  sub,  // type sub-editor 
  ref   // reference selector
}

async function test(fixture: ComponentFixture<EditorComponent>, component: EditorComponent, category: string, 
  typeProps: TypeProps[], debug:boolean) {

  component.category = category;
  await component.ngOnInit();
  await fixture.detectChanges();

  let compiled = fixture.nativeElement as HTMLElement;

  /*if (typeProps.length > 1) { // only selection if multiple
    let expectedItems = new Set<string>(typeProps.map(p => p.name));
    let typeSelect = compiled.querySelector('mat-select[id="typeSelect"]') as HTMLElement;
    expect(typeSelect).toBeTruthy();
    let typeOptions = typeSelect.querySelectorAll('mat-option') as NodeListOf<Element>;
    expect(typeOptions).toBeTruthy();
    typeOptions.forEach((o) => {
      if (debug) {
        console.log("T-TYPE " + o.innerHTML.trim());    
      }
      expectedItems.delete(o.innerHTML.trim());
    });
    expect(expectedItems.size).withContext("Expected types shall all be present: " 
      + Array.from(expectedItems).join(', ')).toBe(0);
  } else if (typeProps.length == 1) {
    expect(component.meta?.value?.length).toBe(1);
    let ra = component.meta?.value;
    if (ra) {
      if (debug) {
        console.log("T-TYPE " + typeProps[0].name);    
      }
      expect(ra[0].idShort).toEqual(typeProps[0].name);
    }
  }*/
  for (let typeProp of typeProps.filter(p => p.input)) {
    component.selectedType = component.meta?.value?.find(type => type.idShort === typeProp.name);
    component.generateInputs();

    await fixture.detectChanges();
    await fixture.whenRenderingDone();
  
    let inputs = compiled.querySelector('div[class="inputGroup"]') as HTMLElement;
    expect(inputs).toBeTruthy();
    var uiGroups = inputs.children;
    for (var g = 1; g < uiGroups.length; g++) {
      var uiGroup = uiGroups[g] as HTMLElement;
      var inputName = uiGroup.querySelector('[id="inputName"]') as HTMLElement;
      if (inputName) {
        expect(inputName).withContext("uiGroup " + g).toBeTruthy();
        var varName = inputName.innerText.trim();
        var expectedEditor = typeProp.input?.find(p => p.name === varName);
        var textEditor = uiGroup.querySelector('mat-label') as HTMLElement;
        var subEditor = uiGroup.querySelector('app-subeditor-button') as HTMLElement;
        var boolEditor = uiGroup.querySelector('app-boolean-dropdown') as HTMLElement;
        var enumEditor = uiGroup.querySelector('app-enum-dropdown') as HTMLElement;
        var refEditor = uiGroup.querySelector('app-input-ref-select') as HTMLElement;
        if (expectedEditor) {
          if (textEditor) {
            var input = uiGroup.querySelector('input') as HTMLElement;
            expect(input).withContext(varName).toBeTruthy();
            expect(expectedEditor?.kind).withContext(varName + " shall be text").toBe(InputKind.text);
          } else if (boolEditor) {
            expect(expectedEditor?.kind).withContext(varName + " shall be bool").toBe(InputKind.bool);
          } else if (enumEditor) {
            expect(expectedEditor?.kind).withContext(varName + " shall be enum").toBe(InputKind.enum);
          } else if (subEditor) {
            expect(expectedEditor?.kind).withContext(varName + " shall be sub").toBe(InputKind.sub);
          } else if (refEditor) {
            expect(expectedEditor?.kind).withContext(varName + " shall be ref").toBe(InputKind.ref);
          }
        } else {
          if (debug) {
            console.log("UNKNOWN " + inputName.innerText.trim() + " text " + (!textEditor)+ " bool " + (!boolEditor)+ " sub " 
              + (!subEditor)+ " enum " + (!enumEditor)+ " ref " + (!refEditor));
          }
        }
      }
      // TODO buttons 
    }
  }
}
