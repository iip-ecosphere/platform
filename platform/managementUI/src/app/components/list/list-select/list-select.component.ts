import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-list-select',
  templateUrl: './list-select.component.html',
  styleUrls: ['./list-select.component.scss']
})
export class ListSelectComponent implements OnInit {

  constructor(private router: Router) { }

  listTitles = {
    "Setup":"EndpointAddress",
    "Constants":"String",
    "Types":"RecordType",
    "Services":"Service",
    "Servers":"Servers",
    "Meshes":"ServiceMesh",
    "Applications":"Application"
  }


  //listTitles = ["Setup", "Constants", "Types", "Services"]

  ngOnInit(): void {
    for (const [key, value] of Object.entries(this.listTitles)){
      console.log(key, value)
    }
  }

  public selectList(list: string) {
    this.router.navigateByUrl('list/' + list)

  }

}
