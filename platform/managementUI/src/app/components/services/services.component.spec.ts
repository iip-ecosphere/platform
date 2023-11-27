import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { ServicesComponent } from './services.component';
import { HttpClientModule } from '@angular/common/http';
import { EnvConfigService } from '../../services/env-config.service';
import { MatTabsModule } from '@angular/material/tabs';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { retry } from '../../services/utils.service';

describe('ServicesComponent', () => {

  let component: ServicesComponent;
  let fixture: ComponentFixture<ServicesComponent>;

  beforeEach(async () => {
    await EnvConfigService.initAsync();
    await TestBed.configureTestingModule({
      imports: [ HttpClientModule, MatTabsModule, BrowserAnimationsModule ],
      declarations: [ ServicesComponent ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    })
    .compileComponents();
    fixture = TestBed.createComponent(ServicesComponent);
    component = fixture.componentInstance;
    await component.ngOnInit();
    await fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should show services of running app', async() => {
    await fixture.detectChanges();
    await fixture.whenRenderingDone();
    let compiled = fixture.nativeElement as HTMLElement;

    let windowOpenSpy = spyOn(window, 'open');
    let navTabsBtn = new Map<string, HTMLElement>();
    let expectedTabs = new Set<string>(component.tabsParam.map(p => p.tabName));
    let navTabs = compiled.querySelectorAll('td[id="tab.text"]');
    expect(navTabs).toBeTruthy();
    navTabs.forEach((n) => {
      expectedTabs.delete(n.innerHTML.trim());
      navTabsBtn.set(n.innerHTML.trim(), n.closest('span') as HTMLElement);
    });
    expect(expectedTabs.size).toBe(0);

    for (let tp of component.tabsParam) {
      let btn = navTabsBtn.get(tp.tabName);
      let oldData = component.dataToDisplay;
      btn?.click();
      await retry({ // techData resolved asynchronously
        fn: function () {
          return oldData != component.dataToDisplay;
        },
        maxAttempts: 3,
        delay: 500,
      }).catch(e => {});
      await fixture.detectChanges();
      await fixture.whenRenderingDone();
    
      if (tp.tabName === "running services") {
        let dataTable = compiled.querySelector('table[id="data"]');
        if (dataTable) { // don't expect, it may not be there if no apps are running
          let expectedServices = new Set<string>(["SimpleSource_SimpleMeshApp", "SimpleReceiver_SimpleMeshApp"]);
          let dataRows = dataTable?.querySelectorAll('tr[id="data.row"]');
          expect(dataRows).toBeTruthy();
          dataRows?.forEach((r) => {
            let elt = r.querySelector('span[id="data.idShort"]') as HTMLElement;
            expect(elt).toBeTruthy();
            let tmp = elt.innerText.trim().split("_");
            if (tmp.length == 3) { // service_app_appId
              expectedServices.delete(tmp[0] + "_" + tmp[1]);
            }

            elt = r.querySelector('button[id="data.btnStdout"]') as HTMLElement;
            expect(elt).toBeTruthy();
            elt.click();
            expect(windowOpenSpy).toHaveBeenCalled();

            elt = r.querySelector('button[id="data.btnStderr"]') as HTMLElement;
            expect(elt).toBeTruthy();
            elt.click();
            expect(windowOpenSpy).toHaveBeenCalled();
          });
          expect(expectedServices.size).toBe(0);
        }
      } // TODO others?
    }
  });

});
