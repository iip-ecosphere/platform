import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Resource, editorInput } from 'src/interfaces';
import { MatSelectModule } from '@angular/material/select';
import { FormsModule } from '@angular/forms';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { LangStringInputComponent } from './lang-string-input.component';
import { DataUtils } from 'src/app/services/utils.service';
import { MatTooltipModule } from '@angular/material/tooltip';

describe('LangStringInputComponent', () => {
  let component: LangStringInputComponent;
  let fixture: ComponentFixture<LangStringInputComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ MatSelectModule, 
        FormsModule, 
        BrowserAnimationsModule, 
        MatTooltipModule ],
      declarations: [ LangStringInputComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(LangStringInputComponent);
    component = fixture.componentInstance;
    // detect changes after setting value
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should handle LangString', async() => {
    component.input = {
      name: "productDesignation", 
      type: "LangString", 
      value: null, 
      description: [{
        language: "en",
        text: "Designation of the product."
      }],
      refTo: false,
	    multipleInputs: false,
      metaTypeKind: 2
      // meta left out
    } as editorInput;
    component.meta = {
      modelType: { name: "SubmodelElementCollection" }, 
      dataSpecification: [],
      embeddedDataSpecifications: [],
      kind: "Instance",
      value: [ {
        idShort: "LangString",
        kind: "Instance",
        value: []
      } ]
    } as Resource;
    fixture.detectChanges();
    await fixture.whenStable();

    const productDesignation = "My Product";
    let compiled = fixture.debugElement.nativeElement as HTMLElement;
    let textInput = compiled.querySelector('input') as HTMLInputElement;
    expect(textInput).toBeTruthy();
    let selector = compiled.querySelector('mat-select') as HTMLSelectElement;
    expect(selector).toBeTruthy();
    textInput.value = productDesignation;
    textInput.dispatchEvent(new Event('input')); // notify change

    fixture.detectChanges();
    await fixture.whenStable();

    expect(component.langSelected.length).toBeGreaterThan(0);
    expect(component.textInput).toBe(productDesignation); // see above
    expect(component.input.value).toBe(DataUtils.composeLangString(productDesignation, component.langSelected));

    const changedProductDesignation = "My Other Product";
    textInput.value = changedProductDesignation;
    textInput.dispatchEvent(new Event('input')); // notify change
    // cannog change selection of lang -> MatTestHarness

    fixture.detectChanges();
    await fixture.whenStable();

    expect(component.langSelected.length).toBeGreaterThan(0);
    expect(component.textInput).toBe(changedProductDesignation); // see above
    expect(component.input.value).toBe(DataUtils.composeLangString(changedProductDesignation, component.langSelected));
  });

});
