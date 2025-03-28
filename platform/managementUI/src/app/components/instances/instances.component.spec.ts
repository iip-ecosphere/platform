import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InstancesComponent } from './instances.component';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { EnvConfigService } from '../../services/env-config.service';
import { retry } from '../../services/utils.service';
import { MatIconModule } from '@angular/material/icon';

describe('InstancesComponent', () => {

  let component: InstancesComponent;
  let fixture: ComponentFixture<InstancesComponent>;

  beforeEach(async () => {
    await EnvConfigService.init();
    await TestBed.configureTestingModule({
    declarations: [InstancesComponent],
    imports: [MatIconModule],
    providers: [provideHttpClient(withInterceptorsFromDi())]
})
    .compileComponents();

    fixture = TestBed.createComponent(InstancesComponent);
    component = fixture.componentInstance;
    await component.ngOnInit();
    await retry({ // techData resolved asynchronously
      fn: function () {
        return component?.filteredData?.length > 0;
      },
      maxAttempts: 3,
      delay: 500,
    }).catch(e => {});
    await fixture.detectChanges();        
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should show app instances', async() => {
    await fixture.detectChanges();
    await fixture.whenRenderingDone();
    let compiled = fixture.nativeElement as HTMLElement;
    let rows = compiled.querySelectorAll("table tr") as NodeListOf<Element>;
    expect(rows).toBeTruthy();
    let undeployButton = undefined as HTMLElement | undefined;
    // if not app is running, there may not be any instance; there may also be two
    rows.forEach((r) => {
      let td = r.querySelector('td[id="data.nr"]') as HTMLElement;
      expect(td).toBeTruthy();
      expect(td.innerText).toMatch(/\d+/);

      td = r.querySelector('span[id="data.idShort"]') as HTMLElement;
      expect(td).toBeTruthy();
      let tmp = td.innerText.trim(); 
      expect(tmp).toMatch(/\S+/);
      let parts = tmp.split("_");
      let idShort;
      if (parts.length == 1) {
        idShort = tmp;
      } else {
        idShort = parts[0];
      }

      // item.value may be there or not

      let btn  = r.querySelector('button[id="data.btnUndeploy"]') as HTMLElement;
      expect(btn).toBeTruthy();
      if (idShort === 'SimpleMeshApp') {
        undeployButton = btn;
      }
    });

    if (undeployButton) {
      let finished = false;
      component.setFinishedNotifier((success) => {
        console.log("undeployment finished, success: " + success); 
        finished = true; 
      });
      undeployButton.click();

      console.log("Test: Waiting for undeployment completion...");
      await retry({ // wait until undeployment done
        fn: () => finished,
        maxAttempts: 1 * 60,
        delay: 1000
      }).catch(e=>{});
    }
  }, 3 * 60 * 1000);

});