import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ConnectorTypesComponent } from './components/connector-types/connector-types.component';
import { ContainersComponent } from './components/containers/containers.component';
import { DeploymentPlansComponent } from './components/deployment-plans/deployment-plans.component';
import { ResourcesComponent } from './components/resources/resources.component';
import { ServicesComponent } from './components/services/services.component';

const routes: Routes = [
  { path: '', redirectTo: 'resources', pathMatch: `full` },
  { path: 'resources', component: ResourcesComponent },
  { path: 'containers', component: ContainersComponent },
  { path: 'services/:id', component: ServicesComponent },
  { path: 'services', component: ServicesComponent },
  { path: 'connectorTypes', component: ConnectorTypesComponent },
  { path: 'deploymentPlans', component: DeploymentPlansComponent }

];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]

})
export class AppRoutingModule { }
