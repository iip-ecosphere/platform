import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InputRefSelectComponent } from './input-ref-select.component';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { HttpClientModule } from '@angular/common/http';
import { ApiService } from 'src/app/services/api.service';
import { SubeditorButtonComponent } from '../inputControls/subeditor-button/subeditor-button.component';
import { MatCardModule } from '@angular/material/card';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MTK_compound, MTK_primitive } from 'src/interfaces';

describe('InputRefSelectComponent', () => {
  let component: InputRefSelectComponent;
  let fixture: ComponentFixture<InputRefSelectComponent>;
  let apiService: ApiService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ HttpClientModule, MatDialogModule, MatIconModule, MatCardModule, MatTooltipModule ],
      declarations: [ InputRefSelectComponent, SubeditorButtonComponent ],
      providers: [
        {provide: MatDialogRef, useValue: {}},
        {provide: MAT_DIALOG_DATA, useValue: []},
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(InputRefSelectComponent);
    component = fixture.componentInstance;
    apiService = TestBed.inject(ApiService);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should handle sequenceOf(IOType)', async() => {
    component.activeTextinput = false;
    component.meta = await apiService.getMeta(); // we could construct it, easier as e2e test
    component.rows = -1;
    component.input = {
      name: "input",
      type: "sequenceOf(IOType)",
      value: [{
        type: "feedback",
        forward: false,
        idShort: "feedback",
        _type: "IOType"
      }],
      description: [{
        language: "en",
        text: "Data types forming the input to the service."
      }],
      // meta left out
      refTo: false,
      multipleInputs: true,
      metaTypeKind: MTK_compound
    };
    fixture.detectChanges();
    fixture.whenRenderingDone();

    let compiled = fixture.debugElement.nativeElement as HTMLElement;
    let inputSection = compiled.querySelector('span[id="inputName"]');
    expect(inputSection).toBeTruthy();
    expect(inputSection?.querySelector('span[id="ref-select.inputName.subEd"]')).toBeTruthy();
    expect(inputSection?.querySelector('span[id="ref-select.inputName.btn-add"]')).toBeFalsy();
    expect(compiled.querySelector('div[id="ref-select.single"]')).toBeFalsy();
    let container = compiled.querySelector('div[id="ref-select.container"]');
    expect(container).toBeTruthy();
    let matCard = container?.querySelector('mat-card');
    expect(matCard).toBeTruthy();
    expect(matCard?.querySelector('button[id="ref-select.container.bnt-shiftLeft"]')).toBeTruthy();
    expect(matCard?.querySelector('button[id="ref-select.container.bnt-edit"]')).toBeTruthy();
    expect(matCard?.querySelector('button[id="ref-select.container.bnt-delete"]')).toBeTruthy();
    expect(matCard?.querySelector('button[id="ref-select.container.bnt-shiftRight"]')).toBeTruthy();
  });

  it('should handle refTo(Server)', async() => {
    component.activeTextinput = false;
    component.meta = await apiService.getMeta(); // we could construct it, easier as e2e test
    component.rows = -1;
    component.input = {
      name: "server",
      type: "refTo(Server)",
      value: null,
      description: [{
        language: "en",
        text: "Optional link to a required server."
      }],
      // meta left out
      refTo: true,
      multipleInputs: false,
      metaTypeKind: MTK_compound
    };
    fixture.detectChanges();
    fixture.whenRenderingDone();

    let compiled = fixture.debugElement.nativeElement as HTMLElement;
    let inputSection = compiled.querySelector('span[id="inputName"]');
    expect(inputSection).toBeTruthy();
    expect(inputSection?.querySelector('button[id="ref-select.inputName.btn-add"]')).toBeTruthy();
    expect(inputSection?.querySelector('span[id="ref-select.inputName.subEd"]')).toBeFalsy();
    expect(compiled.querySelector('div[id="ref-select.container"]')).toBeFalsy();
    let container = compiled.querySelector('div[id="ref-select.single"]');
    expect(container).toBeTruthy();
    let matCard = container?.querySelector('mat-card');
    expect(matCard).toBeFalsy();
  });

  it('should handle setOf(String)', async() => {
    component.activeTextinput = false;
    component.meta = await apiService.getMeta(); // we could construct it, easier as e2e test
    component.rows = -1;
    component.input = {
      name: "artifacts",
      type: "setOf(String)",
      value: [],
      description: [{
        language: "en",
        text: "Names of (shared) application artifacts to be unpacked with a service."
      }],
      // meta left out
      refTo: false,
      multipleInputs: true,
      metaTypeKind: MTK_primitive
    };
    fixture.detectChanges();
    fixture.whenRenderingDone();

    let compiled = fixture.debugElement.nativeElement as HTMLElement;
    let inputSection = compiled.querySelector('span[id="inputName"]');
    expect(inputSection).toBeTruthy();
    let addBtn = inputSection?.querySelector('button[id="ref-select.inputName.btn-add"]') as HTMLElement;
    expect(addBtn).toBeTruthy();
    expect(inputSection?.querySelector('span[id="ref-select.inputName.subEd"]')).toBeFalsy();
    expect(compiled.querySelector('div[id="ref-select.single"]')).toBeFalsy();
    let container = compiled.querySelector('div[id="ref-select.container"]');
    expect(container).toBeTruthy();
    expect(container?.querySelector('mat-card')).toBeFalsy(); // array is empty
    if (addBtn) {
      addBtn.click();
    }
  });

});
