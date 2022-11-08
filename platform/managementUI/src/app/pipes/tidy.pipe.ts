import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'tidy'
})
export class TidyPipe implements PipeTransform {

  transform(value: any): string {
    if (typeof(value) === 'string') {
      // let index = value.indexOf('value');
      // if(index != -1 && value.indexOf('{') != -1) {
      //   value = value.substring(index + 7);
      //   index = value.indexOf('}');
      //   value = value.substring(0, index);
      //   value = parseFloat(value);
      // }

      // let index2 = value.indexOf('-274');
      // if(index2 != -1) {
      //   console.log('hit!');
      //   value = value.replace('-274', '?');
      // }
    } else if(typeof(value) ==='number'){
      if(value == -274) {
        value = '?';
      }
    }
    console.log(value);
    return value;
  }

}
