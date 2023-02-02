import { APP_INITIALIZER, NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule, HTTP_INTERCEPTORS } from "@angular/common/http";

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

import { ResourcesComponent } from './components/resources/resources.component';
import { ContainersComponent } from './components/containers/containers.component';
import { ServicesComponent } from './components/services/services.component';
import { ConnectorTypesComponent } from './components/connector-types/connector-types.component';
import { TidyPipe } from './pipes/tidy.pipe';
import { EnvConfigService } from './services/env-config.service';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { DeploymentPlansComponent } from './components/deployment-plans/deployment-plans.component';
import { ResourceDetailsComponent } from './components/resource-details/resource-details.component';
import { OperationQueryComponent } from './components/resource-details/operation-query/operation-query.component';
import { OnlyIdPipe } from './pipes/only-id.pipe';
import { StatusBoxComponent } from './components/deployment-plans/status-box/status-box.component';
import { FlowchartComponent } from './components/flowchart/flowchart.component';


@NgModule({
  declarations: [
    AppComponent,
    ResourcesComponent,
    ContainersComponent,
    ServicesComponent,
    ConnectorTypesComponent,
    TidyPipe,
    DeploymentPlansComponent,
    ResourceDetailsComponent,
    OperationQueryComponent,
    OnlyIdPipe,
    StatusBoxComponent,
    FlowchartComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
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
  ],
  providers: [{
    provide:HTTP_INTERCEPTORS,
    useClass: Interceptor,
    multi: true,
    },
    {
    provide: APP_INITIALIZER,
    useFactory: (EnvConfigService: EnvConfigService) => () => EnvConfigService.init(),
    deps: [EnvConfigService],
    multi: true,

  }],
  bootstrap: [AppComponent]
})
export class AppModule { }
