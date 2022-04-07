import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ConnectorTypesComponent } from './components/connector-types/connector-types.component';
import { ContainersComponent } from './components/containers/containers.component';
import { ResourcesComponent } from './components/resources/resources.component';
import { ServicesComponent } from './components/services/services.component';

const routes: Routes = [
  { path: '', redirectTo: 'resources', pathMatch: `full` },
  { path: 'resources', component: ResourcesComponent },
  { path: 'containers', component: ContainersComponent },
  { path: 'services', component: ServicesComponent },
  { path: 'connectorTypes', component: ConnectorTypesComponent }

];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]

})
export class AppRoutingModule { }
