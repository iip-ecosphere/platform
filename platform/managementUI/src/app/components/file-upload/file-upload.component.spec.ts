import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FileUploadComponent, chunkInput } from './file-upload.component';
import { EnvConfigService } from 'src/app/services/env-config.service';
import { retry } from 'src/app/services/utils.service';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { FormsModule } from '@angular/forms';
import { NO_ERRORS_SCHEMA } from '@angular/core';

describe('FileUploadComponent', () => {

  let component: FileUploadComponent;
  let fixture: ComponentFixture<FileUploadComponent>;

  beforeEach(async () => {
    await EnvConfigService.init();
    await TestBed.configureTestingModule({
      imports: [ MatIconModule, MatTooltipModule, FormsModule ],
      declarations: [ FileUploadComponent ],
      schemas: [NO_ERRORS_SCHEMA] // complains about matTooltip although imported
    })
    .compileComponents().then(() => {
      fixture = TestBed.createComponent(FileUploadComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should click', () => {
    let compiled = fixture.nativeElement as HTMLElement;
    spyOn(component.result, 'emit');
    let button = compiled.querySelector('button') as HTMLElement;
    button.click(); // does not lead to connection to input/file, difficult to test
    let file = createFile();
    component.onFileSelected({target:{files:[file]}});
    expect(component.result.emit).toHaveBeenCalledWith(file);
  });

  it('should chunkInput', async() => {
    const file = createFile();
    let seqNr = -20;
    let done = false;
    chunkInput(file, 100, (c, s) => seqNr = Math.max(seqNr, Math.abs(s)), succ => done = succ);
    await retry({ // wait until loaded
      fn: () => done,
      maxAttempts: 4,
      delay: 300
    }).catch(e=>{});    
    expect(seqNr).toBe(0); // one chunk

    seqNr = -20;
    done = false;
    chunkInput(file, 10, (c, s) => seqNr = Math.max(seqNr, Math.abs(s)), succ => done = succ);
    await retry({ // wait until loaded
      fn: () => done,
      maxAttempts: 4,
      delay: 300
    }).catch(e=>{});    
    expect(seqNr).toBe(2); // one chunk
  });

});

/**
 * Creates a file instance with dummy information.
 * 
 * @returns the file instance
 */
function createFile(): File {
  const dataBase64 = "VEhJUyBJUyBUSEUgQU5TV0VSCg==";
  const arrayBuffer = Uint8Array.from(window.atob(dataBase64), c => c.charCodeAt(0));
  return new File([arrayBuffer], "dummy.pdf", {type: 'application/pdf'});
}
