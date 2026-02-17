import { MatRadioModule } from '@angular/material/radio';
import { NgModule, inject, provideAppInitializer } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HTTP_INTERCEPTORS, provideHttpClient, withInterceptorsFromDi } from "@angular/common/http";

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { Interceptor } from './interceptor';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

import {MatToolbarModule} from '@angular/material/toolbar';
import {MatCardModule} from '@angular/material/card';
import {MatTabsModule} from '@angular/material/tabs';
import {MatButtonModule} from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatIconModule} from '@angular/material/icon';
import {MatSelectModule} from '@angular/material/select';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatSidenavModule} from '@angular/material/sidenav';
import {MatListModule} from '@angular/material/list';
import {MatDividerModule} from '@angular/material/divider';
import {MatTooltipModule} from '@angular/material/tooltip';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatCheckboxModule } from '@angular/material/checkbox';

import { ResourcesComponent } from './components/resources/resources.component';
import { ContainersComponent } from './components/containers/containers.component';
import { ServicesComponent } from './components/services/services.component';
import { TidyPipe } from './pipes/tidy.pipe';
import { EnvConfigService } from './services/env-config.service';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { DeploymentPlansComponent } from './components/deployment-plans/deployment-plans.component';
import { ResourceDetailsComponent } from './components/resource-details/resource-details.component';
import { OnlyIdPipe } from './pipes/only-id.pipe';
import { StatusBoxComponent } from './components/deployment-plans/status-box/status-box.component';
import { FlowchartComponent } from './components/flowchart/flowchart.component';
import { ListComponent } from './components/list/list.component';
import { InstancesComponent } from './components/instances/instances.component';
import { EditorComponent } from './components/editor/editor.component';
import { InputRefSelectComponent } from './components/editor/input-ref-select/input-ref-select.component';
import { StatusDetailsComponent }
  from './components/deployment-plans/status-box/status-details/status-details.component';
import {LogsDialogComponent}
  from './components/services/logs/logs-dialog.component';
import { EnumDropdownComponent } from './components/editor/inputControls/enum-dropdown/enum-dropdown.component';
import { BooleanDropdownComponent } from './components/editor/inputControls/boolean-dropdown/boolean-dropdown.component';
import { SubeditorButtonComponent } from './components/editor/inputControls/subeditor-button/subeditor-button.component';
import { MeshFeedbackComponent } from './components/flowchart/feedback/mesh-feedback/mesh-feedback.component';
import { NgVar } from './directives/ng-var.directive';
import { FileUploadComponent } from './components/file-upload/file-upload.component';
import { LangStringInputComponent } from './components/editor/inputControls/lang-string-input/lang-string-input.component';
import { LoginComponent } from './components/login/login.component';

@NgModule({ declarations: [
        AppComponent,
        ResourcesComponent,
        ContainersComponent,
        ServicesComponent,
        TidyPipe,
        DeploymentPlansComponent,
        ResourceDetailsComponent,
        OnlyIdPipe,
        StatusBoxComponent,
        FlowchartComponent,
        ListComponent,
        InstancesComponent,
        EditorComponent,
        InputRefSelectComponent,
        LangStringInputComponent,
        StatusDetailsComponent,
        LogsDialogComponent,
        EnumDropdownComponent,
        BooleanDropdownComponent,
        SubeditorButtonComponent,
        MeshFeedbackComponent,
        NgVar,
        FileUploadComponent,
        LoginComponent
    ],
    bootstrap: [AppComponent], imports: [BrowserModule,
        AppRoutingModule,
        BrowserAnimationsModule,
        FormsModule,
        ReactiveFormsModule,
        MatToolbarModule,
        MatCardModule,
        MatTabsModule,
        MatButtonModule,
        MatInputModule,
        MatFormFieldModule,
        MatIconModule,
        MatSelectModule,
        MatSnackBarModule,
        MatProgressSpinnerModule,
        MatSidenavModule,
        MatListModule,
        MatDividerModule,
        MatTooltipModule,
        MatDialogModule,
        MatCheckboxModule,
        MatRadioModule], providers: [{
            provide: HTTP_INTERCEPTORS,
            useClass: Interceptor,
            multi: true,
        },
        provideAppInitializer(() => {
        const initializerFn = ((envConfigService: EnvConfigService) => () => EnvConfigService.init())(inject(EnvConfigService));
        return initializerFn();
      }), provideHttpClient(withInterceptorsFromDi())] })
export class AppModule { }
