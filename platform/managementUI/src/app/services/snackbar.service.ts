import { Injectable } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';

@Injectable({
  providedIn: 'root'
})
export class SnackbarService {

  constructor(private bar: MatSnackBar) { }

  public openSnackbar(message: string, type?: string) {

    this.bar.open(message);

  }
}
