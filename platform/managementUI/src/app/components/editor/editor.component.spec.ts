import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { EditorComponent } from './editor.component';
import { HttpClientModule } from '@angular/common/http';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatTooltipModule } from '@angular/material/tooltip';
import { FormsModule } from "@angular/forms";
import { EnvConfigService } from '../../services/env-config.service';
import { Resource } from 'src/interfaces';

describe('EditorComponent', () => {

  let component: EditorComponent;
  let fixture: ComponentFixture<EditorComponent>;
  var originalTimeout : number;

  beforeEach(async () => {
    originalTimeout = jasmine.DEFAULT_TIMEOUT_INTERVAL;
    jasmine.DEFAULT_TIMEOUT_INTERVAL = 10000;
    await EnvConfigService.initAsync();
    await TestBed.configureTestingModule({
      imports: [ HttpClientModule, FormsModule, MatTooltipModule ],
      declarations: [ EditorComponent ],
      providers: [
          {provide: MatDialogRef, useValue: {}},
          {provide: MAT_DIALOG_DATA, useValue: []},
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EditorComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should handle Server', async() => {
    await test(fixture, component, "Servers", ["JavaServer", "PythonServer", "FlowerFederatedAiServer"], 0, [
      {name:"class", kind:"text"}, {name:"cmdArg", kind:"text"}, {name:"memLimit", kind:"text"}, 
      {name:"id", kind:"text"}, {name:"description", kind:"text"}, {name:"host", kind:"text"}, 
      {name:"port", kind:"text"}, {name:"executable", kind:"text"}, {name:"nameplateInfo", kind:"sub"}, 
      {name:"artifact", kind:"text"}, {name:"running", kind:"bool"}, {name:"parameter", kind:"text"}, 
      {name:"transportChannel", kind:"text"}, {name:"ver", kind:"text"}
    ]);
  });

  afterEach(() => {
    if (originalTimeout) {
      jasmine.DEFAULT_TIMEOUT_INTERVAL = originalTimeout;
    }
  });

});

interface InputProps {
  name: string
  kind: string
}

async function test(fixture: ComponentFixture<EditorComponent>, component: EditorComponent, category: string, 
  types: string[], selectedType: number, props: InputProps[]) {
  component.category = category;
  await component.ngOnInit();
  await fixture.detectChanges();

  let compiled = fixture.nativeElement as HTMLElement;

  let expectedItems = new Set<string>(types);
  let typeSelect = compiled.querySelector('mat-select[id="typeSelect"]') as HTMLElement;
  expect(typeSelect).toBeTruthy();
  let typeOptions = typeSelect.querySelectorAll('mat-option') as NodeListOf<Element>;
  expect(typeOptions).toBeTruthy();
  typeOptions.forEach((o) => {
    expectedItems.delete(o.innerHTML.trim());
  });
  expect(expectedItems.size).withContext("Expected types shall all be present").toBe(0);
  component.selectedType = component.meta?.value?.find(type => type.idShort === types[selectedType]);
  component.generateInputs();

  await fixture.detectChanges();
  await fixture.whenRenderingDone();

//console.log(fixture.nativeElement);

  let inputs = compiled.querySelector('div[class="inputGroup"]') as HTMLElement;
//console.log("INPUTS " + inputs);
//console.log("UIGROUPS " + component.uiGroups);
//console.log("CATEGORY " + component.category);
//console.log("TYPE " + component.type);
//console.log("DROP " + component.showDropdown);
//console.log("META " + component.meta);
//console.log("META-V " + component.meta?.value);
  expect(inputs).toBeTruthy();
  var uiGroups = inputs.children;
  for (var g = 0; g < uiGroups.length; g++) {
    var uiGroup = uiGroups[g] as HTMLElement;
    var inputName = uiGroup.querySelector('p[id="inputName"]') as HTMLElement;
    expect(inputName).toBeTruthy();
    var varName = inputName.innerText.trim();

    var expectedEditor = props.find(p => p.name === varName);
    var textEditor = uiGroup.querySelector('mat-label') as HTMLElement;
    var subEditor = uiGroup.querySelector('app-subeditor-button') as HTMLElement;
    var boolEditor = uiGroup.querySelector('app-boolean-dropdown') as HTMLElement;
    var enumEditor = uiGroup.querySelector('app-enum-dropdown') as HTMLElement;
    var refEditor = uiGroup.querySelector('app-input-ref-select') as HTMLElement;
  if (expectedEditor) {
      if (textEditor) {
        var input = uiGroup.querySelector('input') as HTMLElement;
        expect(input).withContext(varName).toBeTruthy();
        expect(expectedEditor?.kind).withContext(varName).toBe("text");
      } else if (boolEditor) {
        expect(expectedEditor?.kind).withContext(varName).toBe("bool");
      } else if (enumEditor) {
        expect(expectedEditor?.kind).withContext(varName).toBe("enum");
      } else if (subEditor) {
        expect(expectedEditor?.kind).withContext(varName).toBe("sub");
      } else if (refEditor) {
        expect(expectedEditor?.kind).withContext(varName).toBe("ref");
      }
    } else {
console.log("UNKNOWN " + inputName.innerText.trim() + " text " + (!textEditor)+ " bool " + (!boolEditor)+ " sub " 
  + (!subEditor)+ " enum " + (!enumEditor)+ " ref " + (!refEditor));
    } 
  }

  expect(component).toBeTruthy();
}
