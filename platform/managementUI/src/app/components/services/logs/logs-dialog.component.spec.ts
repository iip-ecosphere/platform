import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { LogsDialogComponent } from './logs-dialog.component';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { EnvConfigService } from '../../../services/env-config.service';

describe('LogsDialogComponent', () => {

  let component: LogsDialogComponent;
  let fixture: ComponentFixture<LogsDialogComponent>;

  beforeEach(async () => {
    await EnvConfigService.init();
    await TestBed.configureTestingModule({
    declarations: [LogsDialogComponent],
    schemas: [CUSTOM_ELEMENTS_SCHEMA],
    imports: [],
    providers: [provideHttpClient(withInterceptorsFromDi())]
})
    .compileComponents();
    fixture = TestBed.createComponent(LogsDialogComponent);
    component = fixture.componentInstance;
    //>SimpleSource@SimpleMeshApp@1|SimpleSource_SimpleMeshApp_1|stdout
    component.getUrl = () => "http://localhost/test.html&id=SimpleSource@SimpleMeshApp@1&idShort=SimpleSource_SimpleMeshApp_1&type=stdout";
    await component.ngOnInit();
    await fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should show log', async() => {
    await fixture.detectChanges();
    await fixture.whenRenderingDone();
    let compiled = fixture.nativeElement as HTMLElement;

    let log = compiled.querySelector('pre[id="log"]') as HTMLElement;
    expect(log).toBeTruthy();
    let btnStart = compiled.querySelector('button[id="btn.start"]') as HTMLElement;
    expect(btnStart).toBeTruthy();
    let btnStop = compiled.querySelector('button[id="btn.stop"]') as HTMLElement;
    expect(btnStop).toBeTruthy();
    let btnReset = compiled.querySelector('button[id="btn.reset"]') as HTMLElement;
    expect(btnReset).toBeTruthy();

    expect(log.innerText).toBeTruthy();
    expect(log.innerText.length).toBeGreaterThan(0);
    btnStop.click();
  });


});
