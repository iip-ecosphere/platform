import { TestBed, waitForAsync } from '@angular/core/testing';

import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { RouterTestingModule } from '@angular/router/testing';
import { AppComponent } from './app.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { EnvConfigService } from './services/env-config.service';

describe('AppComponent', () => {

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        RouterTestingModule, 
        EnvConfigService.imp(HttpClientTestingModule, HttpClientTestingModule)
      ],
      declarations: [
        AppComponent
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    }).compileComponents();
    return EnvConfigService.initAsync();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it(`should have as title 'IIPES_Web'`, () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    expect(app.title).toEqual('IIPES_Web');
  });

  it('should render title', () => {
    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('div[class="main-title"]')?.textContent).toContain('IIP Ecosphere Management UI');
  });

  it('shall have version information', () => {
    if (EnvConfigService.inPlatformTest()) { // not outside, evaluated before, may not be loaded
      waitForAsync(() => {
        const fixture = TestBed.createComponent(AppComponent);
        fixture.detectChanges();
        const compiled = fixture.nativeElement as HTMLElement;
        expect(compiled.querySelector('p[id="version"]')?.textContent).toMatch(/^\d+\.\d+\.\d+(-SNAPSHOT)?$/);
      });
    }
  });

  it('shall have build identification', () => {
    if (EnvConfigService.inPlatformTest()) { // not outside, evaluated before, may not be loaded
      waitForAsync(() => {
        const fixture = TestBed.createComponent(AppComponent);
        fixture.detectChanges();
        const compiled = fixture.nativeElement as HTMLElement;
        expect(compiled.querySelector('p[id="buildId"]')?.textContent).toMatch(/^\d+$/);
      });
    }
  });

});
