import { Component, OnInit } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';
import { ActivatedRoute } from '@angular/router';
import { ApiService } from 'src/app/services/api.service';
import { Resource, ResourceAttribute } from 'src/interfaces';

@Component({
  selector: 'app-editor',
  templateUrl: './editor.component.html',
  styleUrls: ['./editor.component.scss']
})
export class EditorComponent implements OnInit {

  category = 'all';
  meta: Resource | undefined;
  selectedType: Resource | undefined;
  inputs: ResourceAttribute[] = [];
  inputReferenceTo: ResourceAttribute[] = [];
  inputSequenceOf: ResourceAttribute[] = [];

  metaTypes = ['metaState', 'metaProject', 'metaSize', 'metaType', 'metaRefines', 'metaAbstract'];

  constructor(private route: ActivatedRoute, private api: ApiService, public dialog: MatDialogRef<EditorComponent>) { }

  ngOnInit(): void {
    this.getMeta();
    const category = this.route.snapshot.paramMap.get('ls');
    if(category && category != '') {
      this.category = category;
    }
  }

  private async getMeta() {
      this.meta = await this.api.getMeta();
  }

  public checkRef() {
    this.inputs = [];
    this.inputReferenceTo = [];
    this.inputSequenceOf = [];

    const selectedType = this.selectedType;
    if(selectedType && selectedType.value) {
      for(const input of selectedType.value) {
        if(input.idShort && this.metaTypes.indexOf(input.idShort) === -1) {
          let value = input.value.toString().toLocaleLowerCase();
          if(value.indexOf('refto') >= 0 || value.indexOf('sequenceof') >= 0) {
            this.inputReferenceTo.push(input);
          } else {
            this.inputs.push(input);
          }
          if(!(input.description && input.description[0] && input.description[0].text)) {
            input.description = [];
            input.description.push({text: '', language: ''});
          }
        }
        }
    }
  }

  public create() {

  }

  public close() {
    this.dialog.close();
  };

}
