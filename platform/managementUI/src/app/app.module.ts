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
import { ResourcesComponent } from './components/resources/resources.component';
import { ContainersComponent } from './components/containers/containers.component';
import { ServicesComponent } from './components/services/services.component';
import { ConnectorTypesComponent } from './components/connector-types/connector-types.component';
import { TidyPipe } from './pipes/tidy.pipe';
import { EnvConfigService } from './services/env-config.service';

@NgModule({
  declarations: [
    AppComponent,
    ResourcesComponent,
    ContainersComponent,
    ServicesComponent,
    ConnectorTypesComponent,
    TidyPipe
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    BrowserAnimationsModule,
    MatToolbarModule,
    MatCardModule,
    MatTabsModule,
    MatButtonModule,
  ],
  providers: [{
    provide:HTTP_INTERCEPTORS,
    useClass: Interceptor,
    multi: true,
    },
    {
    provide: APP_INITIALIZER,
    useFactory: (EnvConfigService: EnvConfigService) => () => EnvConfigService.load(),
    deps: [EnvConfigService],
    multi: true,

  }],
  bootstrap: [AppComponent]
})
export class AppModule { }
