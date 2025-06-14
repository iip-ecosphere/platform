import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StatusBoxComponent } from './status-box.component';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { statusMessage } from 'src/interfaces';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { NgVar } from 'src/app/directives/ng-var.directive';
import { FileUploadComponent } from '../../file-upload/file-upload.component';
import { MatIconModule } from '@angular/material/icon';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';

describe('StatusBoxComponent', () => {
  let component: StatusBoxComponent;
  let fixture: ComponentFixture<StatusBoxComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
        declarations: [StatusBoxComponent, NgVar, FileUploadComponent],
        imports: [MatDialogModule,
            BrowserAnimationsModule,
            MatCardModule,
            MatIconModule,
            MatProgressSpinnerModule,
            MatTooltipModule],
        providers: [
            { provide: MatDialogRef, useValue: {} },
            { provide: MAT_DIALOG_DATA, useValue: [] },
            provideHttpClient(withInterceptorsFromDi()),
            provideHttpClientTesting(),
        ],
        teardown: {destroyAfterEach: false} // NG0205: Injector has already been destroyed
    }).compileComponents();
    fixture = TestBed.createComponent(StatusBoxComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    component.ngOnInit(); 
    let msg : statusMessage = {
      action: "action",
      aliasIds: [],
      componentType: "component",
      description: "description",
      deviceId: "deviceID",
      id: "id",
      progress: 0,
      result: "ok",
      subDescription: "sub",
      taskId: "1234"
    };
    component.StatusCollection.push({taskId: "1234", isFinished: false, isSuccesful: false, messages: [msg]});
  });

  it('should create', async() => {
    expect(component).toBeTruthy();

    await fixture.detectChanges();
    await fixture.whenRenderingDone();
    let compiled = fixture.nativeElement as HTMLElement;

    let result = { componentInstance: { process: undefined }} as MatDialogRef<any>;
    const openDialogSpy = spyOn(component.dialog, 'open').and.returnValue(result);
    let btn = compiled.querySelector('button[id="status.btnDetails"]') as HTMLElement;
    expect(btn).toBeTruthy();
    btn.click();
    expect(openDialogSpy).toHaveBeenCalled();

    btn = compiled.querySelector('button[id="status.btnDismiss"]') as HTMLElement;
    expect(btn).toBeTruthy();
    btn.click();
  });

});
