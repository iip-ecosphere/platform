import { TestBed, waitForAsync, ComponentFixture } from '@angular/core/testing';

import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { RouterTestingModule } from '@angular/router/testing';
import { AppComponent } from './app.component';
import { HttpClientModule } from '@angular/common/http';
import { EnvConfigService } from './services/env-config.service';

describe('AppComponent', () => {

  let fixture: ComponentFixture<AppComponent>;
  let component: AppComponent;

  beforeEach(async () => {
    await EnvConfigService.init();
    await TestBed
      .configureTestingModule({
        imports: [
          RouterTestingModule, 
          EnvConfigService.imp(HttpClientModule, HttpClientModule)
        ],
        declarations: [
          AppComponent
        ],
        schemas: [CUSTOM_ELEMENTS_SCHEMA]
      })
      .compileComponents()
      .then(() => {
        fixture = TestBed.createComponent(AppComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
      });
    await component.ngOnInit();
  });

  it('should create the app', () => {
    expect(component).toBeTruthy();
  });

  it(`should have as title 'IIPES_Web'`, () => {
    expect(component.title).toEqual('IIPES_Web');
  });

  it('should render title', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('div[class="main-title"]')?.textContent).toContain('oktoflow Management UI');
  });

  it('shall have version information', () => {
      fixture.detectChanges();
      const compiled = fixture.nativeElement as HTMLElement;
      expect(compiled.querySelector('p[id="version"]')?.textContent).toMatch(/^Version: \d+\.\d+\.\d+ (\(S\))?$/);
  });

  it('shall have build identification', () => {
      fixture.detectChanges();
      const compiled = fixture.nativeElement as HTMLElement;
      expect(compiled.querySelector('p[id="buildId"]')?.textContent).toMatch(/^Build-Id: \d+$/);
  });

});
