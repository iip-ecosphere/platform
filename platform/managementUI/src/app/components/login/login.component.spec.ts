import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatTooltipModule } from '@angular/material/tooltip';
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { LoginComponent } from './login.component';
import { Configuration, EnvConfigService } from '../../services/env-config.service';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { MatInputModule } from '@angular/material/input';
import { ApiService } from 'src/app/services/api.service';
import { retry } from 'src/app/services/utils.service';
import { RouterModule } from '@angular/router';

describe('LoginComponent', () => {

    let component: LoginComponent;
    let fixture: ComponentFixture<LoginComponent>;
    let httpController : HttpTestingController;
    let http: HttpClient;
    let api: ApiService;
  
    beforeEach(async () => {
      await EnvConfigService.init();
      await TestBed.configureTestingModule({
          declarations: [LoginComponent],
          schemas: [CUSTOM_ELEMENTS_SCHEMA],
          imports: [FormsModule, 
            MatTooltipModule, 
            MatInputModule,
            MatCardModule,
            ReactiveFormsModule,
            BrowserAnimationsModule, 
            MatFormFieldModule, 
            RouterModule.forRoot(
                [{path: '', component: LoginComponent}, {path: 'resources', component: LoginComponent}]
              )],
          providers: [
              { provide: MatDialogRef, useValue: {} },
              { provide: MAT_DIALOG_DATA, useValue: [] },
              provideHttpClient(withInterceptorsFromDi()),
              provideHttpClientTesting()
          ],
          teardown: {destroyAfterEach: false} // NG0205: Injector has already been destroyed
      }).compileComponents();
  
      fixture = TestBed.createComponent(LoginComponent);
      httpController = TestBed.inject(HttpTestingController);
      http = TestBed.inject(HttpClient);
      api = TestBed.inject(ApiService);
      component = fixture.componentInstance;
    });
  
    it('should create', () => {
      expect(component).toBeTruthy();
    });

    it('should not authenticate', async() => {
        let cfg = EnvConfigService.getConfig();
        if (cfg) {
            cfg.httpTimeout = 2000;
        }

        await fixture.detectChanges();
        await fixture.whenRenderingDone();

        expect(component.loginError).toBeFalsy();

        let compiled = fixture.nativeElement as HTMLElement;
        let userNameInput = compiled.querySelector('input[id="userName"]') as HTMLElement;
        expect(userNameInput).toBeTruthy();
        let passwordInput = compiled.querySelector('input[id="password"]') as HTMLElement;
        expect(passwordInput).toBeTruthy();
        let submitBtn = compiled.querySelector('button[id="btnLogin"]') as HTMLElement;
        expect(submitBtn).toBeTruthy();
        //submitBtn.click();
        //await fixture.detectChanges();
        //await fixture.whenRenderingDone();

        component.loginError = false;
        testLogin(httpController, api, cfg, null, component.login()); // submitBtn.click()
        await retry({ 
            fn: () => component.loginError,
            maxAttempts: 2 * 60,
            delay: 1000
        }).catch(e=>{});

        expect(component.loginError).toBeTruthy();

        userNameInput.nodeValue = "user";
        passwordInput.nodeValue = "password";
        //submitBtn.click();
        //await fixture.detectChanges();
        //await fixture.whenRenderingDone();

        testLogin(httpController, api, cfg, "x", component.login()); // submitBtn.click()
        await retry({
            fn: () => !component.loginError,
            maxAttempts: 2 * 60,
            delay: 1000
        }).catch(e=>{});
        expect(component.loginError).toBeFalsy();
    })

});


/**
 * Tests the login.
 * 
 * @param httpController the HTTP testing controller 
 * @param api the AAS API service
 * @param cfg the UI configuration
 * @param responseValue the response value to be used for mocking the request, may be null for erroneous (401, unauthorized)
 * @param loginCall the actual call to await after 
 */
async function testLogin(httpController : HttpTestingController, api: ApiService, cfg: Configuration|undefined, responseValue: any, loginCall: Promise<void>) {
    await new Promise(r => setTimeout(r, 500)); // allow the cfg be handled/request to be issued first; timeout shall be less than timeout above
    let req = httpController.expectOne(api.getAccessibleUrl(cfg));
    if (responseValue) {
        req.flush(responseValue);
    } else {
        req.error(new ProgressEvent ('error'), {status: 401}); // unauthorized
    }
    //let response = 
    await loginCall;
    expect(req.request.method).toEqual('GET');
    //expect(response).toEqual(expectedValue);
    httpController.verify();
}