import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StatusDetailsComponent } from './status-details.component';
import { EnvConfigService } from '../../../../services/env-config.service';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';

describe('StatusDetailsComponent', () => {
  let component: StatusDetailsComponent;
  let fixture: ComponentFixture<StatusDetailsComponent>;

  const dialogMock = {
    close: () => { }
  };

  beforeEach(async () => {
    await EnvConfigService.init();
    await TestBed.configureTestingModule({
      imports: [ MatDialogModule, MatIconModule ],
      declarations: [ StatusDetailsComponent ],
      providers: [
        {provide: MatDialogRef, useValue: dialogMock},
        {provide: MAT_DIALOG_DATA, useValue: []},
      ],
      teardown: {destroyAfterEach: false} // NG0205: Injector has already been destroyed
    }).compileComponents();

    fixture = TestBed.createComponent(StatusDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    component.ngOnInit(); 
  });

  it('should create', async() => {
    expect(component).toBeTruthy();

    await fixture.detectChanges();
    await fixture.whenRenderingDone();
    let compiled = fixture.nativeElement as HTMLElement;
    
    let btn = compiled.querySelector('button[id="statusDialog.btnClose"]') as HTMLElement;
    expect(btn).toBeTruthy();
    btn.click();
  });

});
