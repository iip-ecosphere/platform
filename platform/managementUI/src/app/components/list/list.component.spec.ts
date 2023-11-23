import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ListComponent } from './list.component';
import { HttpClientModule } from '@angular/common/http';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatTabsModule } from '@angular/material/tabs';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { EnvConfigService } from '../../services/env-config.service';
import { RouterTestingModule } from "@angular/router/testing";
import { Router } from "@angular/router";
import { Location } from "@angular/common";
import { routes } from "../../app-routing.module";

describe('ListComponent', () => {

  let component: ListComponent;
  let fixture: ComponentFixture<ListComponent>;
  let location: Location;
  let router: Router;

  beforeEach(async () => {
    await EnvConfigService.initAsync();
    await TestBed.configureTestingModule({
      imports: [ HttpClientModule, 
        MatTabsModule, 
        BrowserAnimationsModule, 
        MatDialogModule, 
        RouterTestingModule.withRoutes(routes) ],
      declarations: [ ListComponent ],
      providers: [
          {provide: MatDialogRef, useValue: {}},
          {provide: MAT_DIALOG_DATA, useValue: []},
      ]
    })
    .compileComponents().then(() => {
      fixture = TestBed.createComponent(ListComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
      router = TestBed.inject(Router);
      location = TestBed.inject(Location);
    });
    component.ngOnInit();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have tabs', async() => {
    await fixture.detectChanges();
    let compiled = fixture.nativeElement as HTMLElement;

    for (let tab of component.tabsParam) {
      let container = compiled.querySelector(`table[id="menu.${tab.tabName}"]`) as HTMLElement;
      expect(container).toBeTruthy();
      expect(container?.querySelector('tr td img')).toBeTruthy(); // AAS icon
      let elt = container?.querySelector('tr td:nth-child(2)') as HTMLElement;
      expect(elt).toBeTruthy();
      expect(elt.innerText).toContain(tab.tabName);

      let menuClick = compiled.querySelector(`span[id="menuClick.${tab.tabName}"]`) as HTMLElement;
      expect(menuClick).toBeTruthy();
    }
  });

  it('should provided Setup tab/data', async() => {
    const expectedDataIdShort = ["aas", "aasAccessControlAllowOrigin", "deviceMgtStorage", "serviceManager", 
      "aasSemanticIdResolver", "aasPersistency", "serializer", "aasProtocol", "aasServer", "aasRegistryServer", 
      "aasImplServer"];

    await test(fixture, component, 0, expectedDataIdShort);
  });

  it('should provided Constants tab/data', async() => {
    const expectedDataIdShort = [] as string[]; // TODO add a constant to model

    await test(fixture, component, 1, expectedDataIdShort);
  });

  it('should provided Types tab/data', async() => {
    const expectedDataIdShort = [] as string[]; // TODO no types found

    await test(fixture, component, 2, expectedDataIdShort);
  });

  it('should provided Dependencies tab/data', async() => {
    const expectedDataIdShort = ["PYTHON3", "CONDA", "websocketsNoVersion", "flowrDependency", 
      "lxLibc6Compat"] as string[]; // just some

    await test(fixture, component, 3, expectedDataIdShort);
  });

  it('should provided Nameplates tab/data', async() => {
    const expectedDataIdShort = ["manufacturer_kiprotect", "manufacturer_rapidminer", "manufacturer_sse",
       "manufacturer_l3s", "manufacturer_mipTech", "manufacturer_NovoAITech"] as string[];

    await test(fixture, component, 4, expectedDataIdShort);
  });

  it('should provided Services tab/data', async() => {
    const expectedDataIdShort = ["mySourceService", "myReceiverService"] as string[];

    await test(fixture, component, 5, expectedDataIdShort);
  });

  it('should provided Servers tab/data', async() => {
    const expectedDataIdShort = [] as string[];

    await test(fixture, component, 6, expectedDataIdShort); // TODO no servers
  });

  it('should provided Meshes tab/data', async() => {
    const expectedDataIdShort = ["myMesh"] as string[];

    await test(fixture, component, 7, expectedDataIdShort);
  });

  it('should provided Applications tab/data', async() => {
    const expectedDataIdShort = ["myApp"] as string[];

    await test(fixture, component, 8, expectedDataIdShort);
  });

});

async function test(fixture: ComponentFixture<ListComponent>, component: ListComponent, tabIndex: number, 
  expectedDataIdShort: string[]) {
  
  await fixture.detectChanges();
  let compiled = fixture.nativeElement as HTMLElement;
  let tabName = component.tabsParam[tabIndex].tabName;
  let expIdShort = new Set<string>(expectedDataIdShort);
  let tabContext = "in table " + tabName;

  let menuClick = compiled.querySelector(`span[id="menuClick.${tabName}"]`) as HTMLElement;
  expect(menuClick).withContext(tabContext).toBeTruthy();
  menuClick.click(); // sufficient?
  await component.loadData(component.tabsParam[tabIndex].metaProject, component.tabsParam[tabIndex].submodelElement);

  await fixture.detectChanges();
  await fixture.whenRenderingDone();
    compiled = fixture.nativeElement as HTMLElement;
    let tabData = compiled.querySelector('table[id="data"]') as HTMLElement;
    expect(tabData).withContext(tabContext).toBeTruthy();
    let i = 0;
    
    for (let d of component.filteredData) {
      let tabDataRow = tabData.querySelector(`tr:nth-child(${i + 1})`) as HTMLElement;
      let context = `in table row ${i + 1} of ${tabName}`;

      expect(tabDataRow).withContext(context).toBeTruthy();
      let td = tabDataRow.querySelector('td[id="data.index"]') as HTMLElement;
      expect(td).withContext(context).toBeTruthy();
      expect(td.innerText).withContext(context).toContain(`${i + 1}`);
      // data.item.logo ?
      let item = tabDataRow.querySelector('span[id="data.item.idShort"]') as HTMLElement;
      expect(item).withContext(context).toBeTruthy();
      expect(item.innerText).toMatch(/\S+/);
      expIdShort.has(item.innerText.trim());        

      item = tabDataRow.querySelector('div[id="data.item.value"]') as HTMLElement;
      expect(item).withContext(context).toBeTruthy();
      expect(item.innerText).withContext(context).toMatch(/\S+/);

      /*if (tabName == "Applications") { // TODO not there
          expect(tabDataRow.querySelector('button[id="data.btnGenTemplate"]')).withContext(`genTemplate button of table ${tabName}`).toBeTruthy();
          // click: not implemented/tested
          expect(tabDataRow.querySelector('button[id="data.btnGenApp"]')).withContext(`genApp button of table ${tabName}`).toBeTruthy();
          // click: not implemented/tested
      }*/
      expect(tabDataRow.querySelector('button[id="data.btnEdit"]')).withContext(`edit button of table ${tabName}`).toBeTruthy();
      // click: may be router, may be dialog
      expect(tabDataRow.querySelector('button[id="data.btnDelete"]')).withContext(`delete button of table ${tabName}`).toBeTruthy();
      // click: not implemented
      i++;
    }
    if (tabName != "Setup" && i > 0) { // TODO button new shall also be there if empty       
      let btnNew = compiled.querySelector(`button[id="btnNew"]`) as HTMLElement;
      expect(btnNew).withContext(`new button of table ${tabName}`).toBeTruthy();
    }
} 
