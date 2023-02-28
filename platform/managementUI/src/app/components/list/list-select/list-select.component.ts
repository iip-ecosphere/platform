import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-list-select',
  templateUrl: './list-select.component.html',
  styleUrls: ['./list-select.component.scss']
})
export class ListSelectComponent implements OnInit {

  constructor(private router: Router) { }

  ngOnInit(): void {
  }

  public selectList(list: string) {
    this.router.navigateByUrl('list/' + list)

  }

}
