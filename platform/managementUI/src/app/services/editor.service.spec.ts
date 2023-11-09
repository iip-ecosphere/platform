import { TestBed } from '@angular/core/testing';

import { EditorService } from './editor.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('EditorService', () => {
  let service: EditorService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ HttpClientTestingModule ]
    });  
    service = TestBed.inject(EditorService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
