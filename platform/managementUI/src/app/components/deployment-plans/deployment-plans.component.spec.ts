import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DeploymentPlansComponent } from './deployment-plans.component';
import { HttpClientModule } from '@angular/common/http';
import { EnvConfigService } from '../../services/env-config.service';

describe('DeploymentPlansComponent', () => {

  let component: DeploymentPlansComponent;
  let fixture: ComponentFixture<DeploymentPlansComponent>;

  beforeEach(async () => {
    await EnvConfigService.initAsync();
    await TestBed.configureTestingModule({
      imports: [ HttpClientModule ],
      declarations: [ DeploymentPlansComponent ]
    })
    .compileComponents();
    fixture = TestBed.createComponent(DeploymentPlansComponent);
    component = fixture.componentInstance;
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
  
    let deployButton = undefined;
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
      // TODO try deploy? what about stopping app?
    }
  });

});
