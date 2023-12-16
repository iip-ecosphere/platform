import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DeploymentPlansComponent } from './deployment-plans.component';
import { HttpClientModule } from '@angular/common/http';
import { EnvConfigService } from '../../services/env-config.service';
import { retry } from '../../services/utils.service';
import { FileUploadComponent } from '../file-upload/file-upload.component';
import { MatIconModule } from '@angular/material/icon';

describe('DeploymentPlansComponent', () => {

  let component: DeploymentPlansComponent;
  let fixture: ComponentFixture<DeploymentPlansComponent>;

  beforeEach(async () => {
    await EnvConfigService.init();
    await TestBed.configureTestingModule({
      imports: [ HttpClientModule, MatIconModule ],
      declarations: [ DeploymentPlansComponent, FileUploadComponent ]
    })
    .compileComponents();
    fixture = TestBed.createComponent(DeploymentPlansComponent);
    component = fixture.componentInstance;
    component.websocket.emitInfo = false;
    await component.ngOnInit();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have deployment plan', async() => {
    await fixture.detectChanges();
    let compiled = fixture.nativeElement as HTMLElement;
    
    expect(component.deploymentPlans).toBeTruthy();
    let tabData = compiled.querySelector('table[id="data"]') as HTMLElement;
    expect(tabData).toBeTruthy();
  
    let deployButton = null;
    if (component?.deploymentPlans?.value) {
      let i = 0;
      for (let d of component?.deploymentPlans?.value) {
        let tabDataRow = tabData.querySelector(`tr:nth-child(${i + 1})`) as HTMLElement;
        let td = tabDataRow.querySelector('td[id="data.index"]') as HTMLElement;
        expect(td).toBeTruthy();
        expect(td.innerText).toContain(`${i + 1}`);

        td = tabDataRow.querySelector('span[id="data.idShort"]') as HTMLElement;
        expect(td).toBeTruthy();
        let planId = td.innerText.trim();
        expect(planId).toMatch(/\S+/);
        
        td = tabDataRow.querySelector('span[id="data.description"]') as HTMLElement;
        expect(td).toBeTruthy();
        expect(td.innerText).toMatch(/\S+/);

        let btn = tabDataRow.querySelector('button[id="data.btnDeploy"]') as HTMLElement;
        if (planId === 'SimpleMesh') {
          deployButton = btn;
        }
        expect(btn).toBeTruthy();
        expect(btn.innerText).toContain("deploy");
        expect(btn.getAttribute("disabled")).toEqual(null); // else toEqual('')
        i++;
      }
    }
    if (deployButton) {
      let finished = false;
      component.setFinishedNotifier((success) => {
        console.log("deployment finished, success: " + success); 
        finished = true;
      });
      // start deployment
      deployButton.click();
      await fixture.detectChanges();
      await fixture.whenRenderingDone();
      compiled = fixture.nativeElement as HTMLElement;
      console.log("Test: Waiting for deployment completion...");
      await retry({ // wait until deployment done
        fn: () => finished,
        maxAttempts: 2 * 60,
        delay: 1000
      }).catch(e=>{});
    }
  }, 3 * 60 * 1000);

  it('should have upload button', async() => {
    await fixture.detectChanges();
    let compiled = fixture.nativeElement as HTMLElement;
    let button = compiled.querySelector('button[class="upload-btn"]') as HTMLElement;
    expect(button).toBeTruthy();
    // click does not help much here due to input
    const dataBase64 = "VEhJUyBJUyBUSEUgQU5TV0VSCg==";
    const arrayBuffer = Uint8Array.from(window.atob(dataBase64), c => c.charCodeAt(0));
    var f = new File([arrayBuffer], "test.yml", {type: 'text/yaml'});
    component.uploadFile(f);
    console.log("Waiting for upload...");
    await retry({
      fn: () => component.uploadEnabled,
      maxAttempts: 4,
      delay: 300
    }).catch(e=>{});
  });

});
