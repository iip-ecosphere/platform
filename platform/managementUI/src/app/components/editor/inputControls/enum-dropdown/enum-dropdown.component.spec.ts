import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EnumDropdownComponent } from './enum-dropdown.component';
import { Resource, editorInput } from 'src/interfaces';
import { MatSelectModule } from '@angular/material/select';
import { FormsModule } from '@angular/forms';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

describe('EnumDropdownComponent', () => {
  let component: EnumDropdownComponent;
  let fixture: ComponentFixture<EnumDropdownComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ MatSelectModule, FormsModule, BrowserAnimationsModule ],
      declarations: [ EnumDropdownComponent ],
      teardown: {destroyAfterEach: false} // NG0205: Injector has already been destroyed
    })
    .compileComponents();

    fixture = TestBed.createComponent(EnumDropdownComponent);
    component = fixture.componentInstance;
    // detect changes after setting value
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should handle Enum', async() => {
    component.input = {
      name: "kind", 
      type: "ServiceKind", 
      value: "SOURCE_SERVICE", 
      description: [{
        language: "en",
        text: "Kind of service (SOURCE_SERVICE, TRANSFORMATION_SERVICE, SINK_SERVICE, PROBE_SERVICE)."
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
        idShort: "ServiceKind",
        kind: "Instance",
        value: [
          {
            modelType: { name: "SubmodelElementCollection" },
            kind: "Instance",
            value: [
              {
                modelType: { name: "Property" },
                kind: "Instance",
                value: "SOURCE_SERVICE",
                valueType: "string",
                idShort: "varValue"
              }
            ]
          }, {
            modelType: { name: "SubmodelElementCollection" },
            kind: "Instance",
            value: [
              {
                modelType: { name: "Property" },
                kind: "Instance",
                value: "TRANSFORMATION_SERVICE",
                valueType: "string",
                idShort: "varValue"
              }
            ]
          }, {
            modelType: { name: "SubmodelElementCollection" },
            kind: "Instance",
            value: [
              {
                modelType: { name: "Property" },
                kind: "Instance",
                value: "SINK_SERVICE",
                valueType: "string",
                idShort: "varValue"
              }
            ]
          }, {
            modelType: { name: "SubmodelElementCollection" },
            kind: "Instance",
            value: [
              {
                modelType: { name: "Property" },
                kind: "Instance",
                value: "PROBE_SERVICE",
                valueType: "string",
                idShort: "varValue"
              }
            ]
          }
        ]
      }
      ]
    } as Resource;
    fixture.detectChanges();

    let compiled = fixture.debugElement.nativeElement as HTMLElement;
    let selector = compiled.querySelector('mat-select') as HTMLSelectElement;
    expect(selector).toBeTruthy();
    // unclear how to get value, options from compiled; material 14 does not yet have the testing harness
    expect(component.enum.length).toBe(4); // see above
    //expect(component.selected).toBeFalse();
  });

});
