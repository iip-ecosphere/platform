import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ConnectorTypesComponent }
  from './components/connector-types/connector-types.component';
import { ContainersComponent }
  from './components/containers/containers.component';
import { DeploymentPlansComponent }
  from './components/deployment-plans/deployment-plans.component';
import { FlowchartComponent }
  from './components/flowchart/flowchart.component';
import { ListSelectComponent }
  from './components/list/list-select/list-select.component';
import { ListComponent }
  from './components/list/list.component';
import { ResourceDetailsComponent }
  from './components/resource-details/resource-details.component';
import { ResourcesComponent }
  from './components/resources/resources.component';
import { ServicesComponent }
  from './components/services/services.component';
//import { ListAppComponent } from './components/lists/list-app/list-app.component';
//import { ListsComponent } from './components/lists/lists.component';

const routes: Routes = [
  { path: '', redirectTo: 'resources', pathMatch: `full` },
  { path: 'resources/:id', component: ResourceDetailsComponent },
  { path: 'resources', component: ResourcesComponent },
  { path: 'containers', component: ContainersComponent },
  { path: 'services/:id', component: ServicesComponent },
  { path: 'services', component: ServicesComponent },
  { path: 'connectorTypes', component: ConnectorTypesComponent },
  { path: 'deploymentPlans', component: DeploymentPlansComponent },
  { path: 'flowchart', component: FlowchartComponent },
  //{ path: 'list-app', component: ListAppComponent}, // TODO remove
  //{ path: 'lists', component: ListsComponent }, // TODO remove
  { path: 'list', component: ListSelectComponent},
  { path: 'list/:ls', component: ListComponent }

];

@NgModule({
  imports: [RouterModule.forRoot(routes, {useHash: true})],
  exports: [RouterModule]

})
export class AppRoutingModule { }
