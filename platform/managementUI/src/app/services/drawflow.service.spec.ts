import { TestBed } from '@angular/core/testing';

import { DrawflowService } from './drawflow.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('DrawflowService', () => {
  let service: DrawflowService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ HttpClientTestingModule ]
    });
    service = TestBed.inject(DrawflowService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
