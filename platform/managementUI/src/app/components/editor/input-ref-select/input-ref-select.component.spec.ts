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
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { FormsModule } from '@angular/forms';
import { MatSelectModule } from '@angular/material/select';

describe('InputRefSelectComponent', () => {
  let component: InputRefSelectComponent;
  let fixture: ComponentFixture<InputRefSelectComponent>;
  let apiService: ApiService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ 
        HttpClientModule, 
        MatDialogModule, 
        MatIconModule, 
        MatCardModule, 
        MatTooltipModule, 
        MatToolbarModule,
        MatFormFieldModule,
        MatSelectModule,
        MatInputModule, // required via MatFormFieldModule
        FormsModule, // required via MatFormFieldModule
        BrowserAnimationsModule ],
      declarations: [ 
        InputRefSelectComponent, 
        SubeditorButtonComponent ],
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
    await fixture.whenStable();
    await fixture.whenRenderingDone();

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
    let btn = matCard?.querySelector('button[id="ref-select.container.bnt-shiftLeft"]') as HTMLElement;
    expect(btn).toBeTruthy();
    btn.click();
    expect(matCard?.querySelector('button[id="ref-select.container.bnt-edit"]')).toBeTruthy();
    expect(matCard?.querySelector('button[id="ref-select.container.bnt-delete"]')).toBeTruthy();
    btn = matCard?.querySelector('button[id="ref-select.container.bnt-shiftRight"]') as HTMLElement;
    expect(btn).toBeTruthy();
    btn.click();
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
    await fixture.whenStable();
    await fixture.whenRenderingDone();
    let compiled = fixture.debugElement.nativeElement as HTMLElement;
    let inputSection = compiled.querySelector('span[id="inputName"]');
    expect(inputSection).toBeTruthy();
    let addBtn = inputSection?.querySelector('button[id="ref-select.inputName.btn-add"]') as HTMLElement;
    expect(addBtn).toBeTruthy();
    expect(inputSection?.querySelector('span[id="ref-select.inputName.subEd"]')).toBeFalsy();
    expect(compiled.querySelector('div[id="ref-select.container"]')).toBeFalsy();
    let container = compiled.querySelector('div[id="ref-select.single"]');
    expect(container).toBeTruthy();
    let matCard = container?.querySelector('mat-card');
    expect(matCard).toBeFalsy();
    addBtn.click();

    // this setup allows for an entry editor that uses the InputRefSelectComponent recursively -> selector
    fixture.detectChanges();
    await fixture.whenStable();
    await fixture.whenRenderingDone();
    let modalHeader = document.body.querySelector('.hmiHeader') as HTMLElement; // this header is only there with selector
    expect(modalHeader).toBeTruthy();
    let modalInput = document.body.querySelector('mat-card[id="ref-select.input"]') as HTMLElement; // this mat-card is only there with selector
    expect(modalInput).toBeTruthy();
    let input = modalInput.querySelector('mat-select[id="ref-select.input.select"]') as HTMLSelectElement;
    expect(input).toBeTruthy();
    input.value = "myServer"; // value does not seem to be taken over, just an empty string, see below
    input.dispatchEvent(new Event('change')); // notify change

    fixture.detectChanges();
    await fixture.whenStable();

    expect(modalHeader.querySelector('button[id="ref-select.btn-cancel"]')).toBeTruthy;
    let saveBtn = modalHeader.querySelector('button[id="ref-select.btn-save"') as HTMLInputElement;
    expect(saveBtn).toBeTruthy();
    saveBtn.click(); 
    fixture.detectChanges();
    await fixture.whenStable();
    //expect(component.input.value.length).toBeGreaterThan(0); // value is not taken over in test -> Material TestHarness
  });

  it('should handle setOf(String)', async() => {
    component.activeTextinput = true;
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
    await fixture.whenStable();
    await fixture.whenRenderingDone();

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
    addBtn.click();

    // this setup allows for an entry editor that uses the InputRefSelectComponent recursively -> selector
    fixture.detectChanges();
    await fixture.whenStable();
    await fixture.whenRenderingDone();
    let modalHeader = document.body.querySelector('.hmiHeader') as HTMLElement; // this header is only there with selector
    expect(modalHeader).toBeTruthy();
    let modalInput = document.body.querySelector('mat-card[id="ref-select.input"]') as HTMLElement; // this mat-card is only there with selector
    expect(modalInput).toBeTruthy();
    let input = modalInput.querySelector('input[id="ref-select.input.text"]') as HTMLInputElement;
    expect(input).toBeTruthy();
    input.value = "xyz";
    input.dispatchEvent(new Event('input')); // notify change

    fixture.detectChanges();
    await fixture.whenStable();

    expect(modalHeader.querySelector('button[id="ref-select.btn-cancel"]')).toBeTruthy;
    let saveBtn = modalHeader.querySelector('button[id="ref-select.btn-save"') as HTMLInputElement;
    expect(saveBtn).toBeTruthy();
    saveBtn.click(); 
    fixture.detectChanges();
    await fixture.whenStable();
    expect(component.input.value.length).toBe(1);
    expect(component.input.value[0]).toBe("xyz");
  });

});
