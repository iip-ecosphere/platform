import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FlowchartComponent } from './flowchart.component';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { RouterTestingModule } from "@angular/router/testing";
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatInputModule } from '@angular/material/input';
import { EnvConfigService } from '../../services/env-config.service';

describe('FlowchartComponent', () => {

  let component: FlowchartComponent;
  let fixture: ComponentFixture<FlowchartComponent>;

  beforeEach(async () => {
    await EnvConfigService.init();
    await TestBed.configureTestingModule({
    declarations: [FlowchartComponent],
    imports: [RouterTestingModule,
        MatDialogModule,
        FormsModule,
        MatIconModule,
        MatCardModule,
        MatInputModule],
    providers: [
        { provide: MatDialogRef, useValue: {} },
        { provide: MAT_DIALOG_DATA, useValue: [] },
        provideHttpClient(withInterceptorsFromDi()),
    ]
})
    .compileComponents();
    fixture = TestBed.createComponent(FlowchartComponent);
    component = fixture.componentInstance;
    await component.ngOnInit();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have app elements', async() => {
    await fixture.detectChanges();
    await fixture.whenRenderingDone();
    let compiled = fixture.nativeElement as HTMLElement;

    let expectedMeshId = new Set<string>();
    expectedMeshId.add('myMesh');
    let expectedServiceNames = new Set<string>();
    expectedServiceNames.add('SimpleSource');
    expectedServiceNames.add('SimpleReceiver');

    let checkSet = new Set<string>(expectedServiceNames);
    let servicePanel = compiled.querySelector('div[class="outer-container"]') as HTMLElement;
    expect(servicePanel).toBeTruthy();
    let services = servicePanel.querySelectorAll('mat-card');
    expect(services).toBeTruthy();
    services.forEach((s) => {
      let service = s.querySelector('td[id="service.id"]') as HTMLElement;
      if (service) {
        let serviceName = service.innerText.trim();
        expect(expectedServiceNames.has(serviceName)).withContext(`Expected service name ${serviceName} not found`).toBeTrue();
        checkSet.delete(serviceName);
      }
    });
    expect(checkSet.size).withContext("Not all expected services found").toBe(0);

    let simpleMeshLink = undefined as HTMLElement | undefined;
    let navPanel = compiled.querySelectorAll('a[id="nav-link"]')
    expect(navPanel).toBeTruthy();
    navPanel.forEach((n) => {
      let link = n as HTMLElement;
      if (link) {
        let meshId = link.innerText.trim();
        expect(expectedMeshId.has(meshId)).toBeTrue();
        if (meshId === "myMesh") {
          simpleMeshLink = link;
        }
      }
    });

    expectedServiceNames.forEach(i => checkSet.add(i));
    if (simpleMeshLink) {
      simpleMeshLink.click();
      await fixture.detectChanges();
      await fixture.whenRenderingDone();
      let drawflowContainer = compiled.querySelector('div[class="drawflow-container"]');
      expect(drawflowContainer).toBeTruthy();
      let nodes = drawflowContainer?.querySelectorAll('div[class="drawflow-node node"]');
      expect(nodes).toBeTruthy();
      if (nodes) {
        nodes.forEach((n) => {
          let node = n as HTMLElement;
          let nodeHeadline = node.querySelector("h3");
          if (nodeHeadline) {
            let serviceName = nodeHeadline.innerText.trim();
            expect(expectedServiceNames.has(serviceName)).toBeTrue();
            checkSet.delete(serviceName);
          }
        });
      }
    }
    expect(checkSet.size).toBe(0);

    // TODO requires started instance from deployment-plans and test frame
  });

});
