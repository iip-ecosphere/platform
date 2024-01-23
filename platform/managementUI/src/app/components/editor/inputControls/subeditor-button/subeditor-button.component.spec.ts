import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SubeditorButtonComponent } from './subeditor-button.component';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MTK_compound, Resource } from 'src/interfaces';
import { ApiService } from 'src/app/services/api.service';
import { HttpClientModule } from '@angular/common/http';
import { EditorComponent } from '../../editor.component';
import { MatIconModule } from '@angular/material/icon';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { BooleanDropdownComponent } from '../boolean-dropdown/boolean-dropdown.component';
import { InputRefSelectComponent } from '../../input-ref-select/input-ref-select.component';
import { MatOptionModule } from '@angular/material/core';
import { MatSelectModule } from '@angular/material/select';
import { MatCardModule } from '@angular/material/card';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatFormFieldModule } from '@angular/material/form-field';
import { FormsModule } from '@angular/forms';

describe('SubeditorButtonComponent', () => {
  let component: SubeditorButtonComponent;
  let fixture: ComponentFixture<SubeditorButtonComponent>;
  let apiService: ApiService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ HttpClientModule, MatDialogModule, MatTooltipModule, MatIconModule, 
        BrowserAnimationsModule, MatOptionModule, MatSelectModule, MatCardModule, MatToolbarModule, MatFormFieldModule, FormsModule ],
      declarations: [ SubeditorButtonComponent, EditorComponent, BooleanDropdownComponent, InputRefSelectComponent ],
      providers: [
          {provide: MatDialogRef, useValue: {}},
          {provide: MAT_DIALOG_DATA, useValue: []},
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SubeditorButtonComponent);
    component = fixture.componentInstance;
    apiService = TestBed.inject(ApiService);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should create', async() => {
    component.input = {
      name: "input", 
      type: "sequenceOf(IOType)", 
      value:[{
        type: "feedback",
        forward: false,
        idShort: "feedback",
        _type: "IOType"
      }], description: [{
          language: "en", 
          text: "Data types forming the input to the service."
      }],
      refTo: false,
      multipleInputs: true,
      metaTypeKind : MTK_compound
      // meta left out
    };
    component.meta = await apiService.getMeta();
    /*{
      kind: "Instance",
      value: [ {
        kind: "Instance",
        value: [ 
          createField("name", "The actual implementing data type.", "refTo(DataType)", 1, null),
          createField("forward", "Type will be transported forward to next service. Opposite, backward broadcasting of data.", "Boolean", 1, "true"),
        ],
        idShort: "IOType"
      }]
    };*/
    component.buttonText = "edit";
    component.matIcon = "add";
    component.showValue = "false";

    fixture.detectChanges();
    await fixture.whenRenderingDone();

    let compiled = fixture.debugElement.nativeElement as HTMLElement;
    let button = compiled.querySelector('button') as HTMLElement;
    expect(button).toBeTruthy();
    expect(button.querySelector('mat-icon')).toBeTruthy(); // unsure
    expect(compiled.querySelector('span[id="editorButton.value"]')).toBeFalsy();
    button.click();

    component.buttonText = "add";
    component.matIcon = "";
    component.showValue = "true";

    fixture.detectChanges();
    await fixture.whenRenderingDone();

    button = compiled.querySelector('button') as HTMLElement;
    expect(button).toBeTruthy();
    expect(button.innerText).toBe("add");
    expect(compiled.querySelector('span[id="editorButton.value"]')).toBeTruthy();
    button.click();
  });

});

function createField(name: string, description: string, type: string, uiGroup: number, dflt: string | null) {
  let result = {
    kind: "Instance",
    value: [ {
      kind: "Instance",
      value: [
        {
          kind: "Instance",
          value: "type",
          valueType: "string",
          idShort: name,
          description: [{language: "de", text: description}]
        }, {
          kind: "Instance",
          value: type,
          valueType: "string",
          idShort: "type",
          description: [],
        }, {
          kind: "Instance",
          value: uiGroup,
          valueType: "int",
          idShort: "uiGroup",
          description: [],
        }
      ]
    } ]
  };
  if (dflt) {
    result.value[0].value.push({
      kind: "Instance",
      value: dflt,
      valueType: "int",
      idShort: "metaDefault",
      description: [],
    });
  }
  return result;
}