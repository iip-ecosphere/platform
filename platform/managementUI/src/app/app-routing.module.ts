import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ContainersComponent }
  from './components/containers/containers.component';
import { DeploymentPlansComponent }
  from './components/deployment-plans/deployment-plans.component';
import { EditorComponent } from './components/editor/editor.component';
import { FlowchartComponent }
  from './components/flowchart/flowchart.component';
import { InstancesComponent } from './components/instances/instances.component';
import { ListComponent }
  from './components/list/list.component';
import { ResourceDetailsComponent }
  from './components/resource-details/resource-details.component';
import { ResourcesComponent }
  from './components/resources/resources.component';
import { ServicesComponent }
  from './components/services/services.component';

const routes: Routes = [
  { path: '', redirectTo: 'resources', pathMatch: `full` },
  { path: 'resources/:id', component: ResourceDetailsComponent },
  { path: 'resources', component: ResourcesComponent },
  { path: 'containers', component: ContainersComponent },
  { path: 'services/:id', component: ServicesComponent },
  { path: 'services', component: ServicesComponent },
  { path: 'deploymentPlans', component: DeploymentPlansComponent },
  { path: 'flowchart/:mesh', component: FlowchartComponent },
  { path: 'flowchart', component: FlowchartComponent },
  { path: 'list/editor/:ls', component: EditorComponent},
  { path: 'list', component: ListComponent},
  { path: 'instances', component: InstancesComponent }
  //{ path: 'list/:ls', component: ListComponent }

];

@NgModule({
  imports: [RouterModule.forRoot(routes, {useHash: true})],
  exports: [RouterModule]

})
export class AppRoutingModule { }
