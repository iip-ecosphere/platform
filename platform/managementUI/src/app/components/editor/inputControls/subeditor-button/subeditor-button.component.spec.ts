import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SubeditorButtonComponent } from './subeditor-button.component';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MTK_compound, Resource } from 'src/interfaces';
import { ApiService } from 'src/app/services/api.service';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
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
        declarations: [SubeditorButtonComponent, EditorComponent, BooleanDropdownComponent, InputRefSelectComponent],
        imports: [MatDialogModule, MatTooltipModule, MatIconModule,
            BrowserAnimationsModule, MatOptionModule, MatSelectModule, MatCardModule, MatToolbarModule, MatFormFieldModule, FormsModule],
        providers: [
            { provide: MatDialogRef, useValue: {} },
            { provide: MAT_DIALOG_DATA, useValue: [] },
            provideHttpClient(withInterceptorsFromDi()),
        ],
        teardown: {destroyAfterEach: false} // NG0205: Injector has already been destroyed
    }).compileComponents();

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
    component.meta = await apiService.getMeta(); // we could construct it, easier as e2e test
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

