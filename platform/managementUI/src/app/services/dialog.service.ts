import { Injectable, Component } from '@angular/core';
import { Route, Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class DialogService {

  constructor(
  ) { }

  dialogName: string = "app-logs-dialog";

  openDialogInNewWindow(): void {
    const dialogWindow = window.open('/logs', 'Dialog','width=400,height=300');
  }

  /*
  postMessageToWindow(data: any): void {
    window.postMessage(data, "");
  }
*/
}
