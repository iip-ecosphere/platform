import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ListComponent } from './list.component';
import { HttpClientModule } from '@angular/common/http';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialog, MatDialogRef } from '@angular/material/dialog';
import { MatTabsModule } from '@angular/material/tabs';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { EnvConfigService } from '../../services/env-config.service';
import { RouterTestingModule } from "@angular/router/testing";
import { Router } from "@angular/router";
import { Location } from "@angular/common";
import { routes } from "../../app-routing.module";
import { of } from 'rxjs';

describe('ListComponent', () => {

  let component: ListComponent;
  let fixture: ComponentFixture<ListComponent>;
  let location: Location;
  let router: Router;
  let dialogSpy: jasmine.Spy;
  let dialogRefSpyObj = jasmine.createSpyObj({ afterClosed : of({}), close: null, 
    componentInstance: {selectedType:null, variableName:null, uiGroups:null, category:null}});

  beforeEach(async () => {
    await EnvConfigService.init();
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
    await component.ngOnInit();
    dialogSpy = spyOn(TestBed.inject(MatDialog), 'open').and.returnValue(dialogRefSpyObj);
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

    await test(fixture, component, router, dialogSpy, dialogRefSpyObj, 0, expectedDataIdShort);
  });

  it('should provided Constants tab/data', async() => {
    const expectedDataIdShort = ["UNUSED_String", "UNUSED_Boolean", "UNUSED_Integer", "UNUSED_Real"] as string[];

    await test(fixture, component, router, dialogSpy, dialogRefSpyObj, 1, expectedDataIdShort);
  });

  it('should provided Types tab/data', async() => {
    const expectedDataIdShort = ["rec1"] as string[];

    await test(fixture, component, router, dialogSpy, dialogRefSpyObj, 2, expectedDataIdShort);
  });

  it('should provided Dependencies tab/data', async() => {
    const expectedDataIdShort = ["PYTHON3", "CONDA", "websocketsNoVersion", "flowrDependency", 
      "lxLibc6Compat"] as string[]; // just some

    await test(fixture, component, router, dialogSpy, dialogRefSpyObj, 3, expectedDataIdShort);
  });

  it('should provided Nameplates tab/data', async() => {
    const expectedDataIdShort = ["manufacturer_kiprotect", "manufacturer_rapidminer", "manufacturer_sse",
       "manufacturer_l3s", "manufacturer_mipTech", "manufacturer_NovoAITech"] as string[];

    await test(fixture, component, router, dialogSpy, dialogRefSpyObj, 4, expectedDataIdShort);
  });

  it('should provided Services tab/data', async() => {
    const expectedDataIdShort = ["mySourceService", "myReceiverService"] as string[];

    await test(fixture, component, router, dialogSpy, dialogRefSpyObj, 5, expectedDataIdShort);
  });

  it('should provided Servers tab/data', async() => {
    const expectedDataIdShort = ["myServer"] as string[];

    await test(fixture, component, router, dialogSpy, dialogRefSpyObj, 6, expectedDataIdShort);
  });

  it('should provided Meshes tab/data', async() => {
    const expectedDataIdShort = ["myMesh"] as string[];

    await test(fixture, component, router, dialogSpy, dialogRefSpyObj, 7, expectedDataIdShort);
  });

  it('should provided Applications tab/data', async() => {
    const expectedDataIdShort = ["myApp"] as string[];

    await test(fixture, component, router, dialogSpy, dialogRefSpyObj, 8, expectedDataIdShort,);
  });

  it('shall survive requesting non-existing information', async() => {
    // inspired by initially empty server structure
  
    await component.loadData(null, "ABC"); // log output is ok
    await fixture.detectChanges();
    await fixture.whenRenderingDone();
    expect(fixture).toBeTruthy(); // to not really test, it just shall run through
  });  

});

async function test(fixture: ComponentFixture<ListComponent>, component: ListComponent, router: Router, 
  dialogSpy: jasmine.Spy, dialogRefSpyObj: any, tabIndex: number, expectedDataIdShort: string[]) {
  
  await fixture.detectChanges();
  let compiled = fixture.nativeElement as HTMLElement;
  let tabName = component.tabsParam[tabIndex].tabName;
  let expIdShort = new Set<string>(expectedDataIdShort);
  let tabContext = "in table " + tabName;
  let navigateSpy = spyOn(router, 'navigateByUrl');
  
  let menuClick = compiled.querySelector(`span[id="menuClick.${tabName}"]`) as HTMLElement;
  expect(menuClick).withContext(tabContext).toBeTruthy();
  menuClick.click(); // TODO mock?
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
    let itemIdShort = item.innerText.trim();
    expIdShort.delete(itemIdShort);
    
    item = tabDataRow.querySelector('div[id="data.item.value"]') as HTMLElement;
    expect(item).withContext(context).toBeTruthy();
    expect(item.innerText).withContext(context).toMatch(/\S+/);

    if (tabName == "Applications") { // TODO not there
        expect(compiled.querySelector('button[id="data.btnGenTemplate"]')).withContext(`genTemplate button of table ${tabName}`).toBeTruthy();
        // click: not implemented/tested
        expect(compiled.querySelector('button[id="data.btnGenApp"]')).withContext(`genApp button of table ${tabName}`).toBeTruthy();
        // click: not implemented/tested
    }
    item = tabDataRow.querySelector('button[id="data.btnEdit"]') as HTMLElement;
    expect(item).withContext(`edit button of table ${tabName}`).toBeTruthy();
    item.click();
    if (tabName == "Meshes") {
      expect(navigateSpy).toHaveBeenCalledWith('flowchart/' + itemIdShort);
    } else {
      expect(dialogSpy).toHaveBeenCalled();
      expect(dialogRefSpyObj.componentInstance).toBeTruthy();
      expect(dialogRefSpyObj.componentInstance.category).toBeTruthy(); // dialog open code did something
    }
    // click: may be router, may be dialog
    item = tabDataRow.querySelector('button[id="data.btnDelete"]') as HTMLElement;
    expect(item).withContext(`delete button of table ${tabName}`).toBeTruthy();
    // click: not implemented so far
    i++;
  }
  expect(expIdShort.size).withContext("Expected items").toBe(0);
  if (tabName != "Setup" && i > 0) { // TODO button new shall also be there if empty       
    let btnNew = compiled.querySelector(`button[id="btnNew"]`) as HTMLElement;
    expect(btnNew).withContext(`new button of table ${tabName}`).toBeTruthy();
    btnNew.click();
    if (tabName == "Meshes") {
      expect(navigateSpy).toHaveBeenCalledWith('flowchart');
    } else {
      expect(dialogSpy).toHaveBeenCalled();
      expect(dialogRefSpyObj.componentInstance.category).toBeTruthy(); // dialog close code did something
    }
  }
} 
