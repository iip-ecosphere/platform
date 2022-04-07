import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'tidy'
})
export class TidyPipe implements PipeTransform {

  transform(value: any): string {
    if (typeof(value) === 'string') {
      let index = value.indexOf('value');
      if(index != -1 && value.indexOf('{') != -1) {
        value = value.substring(index + 7);
        index = value.indexOf('}');
        value = value.substring(0, index);
        value = parseFloat(value);
      }
    }
    return value;
  }

}
