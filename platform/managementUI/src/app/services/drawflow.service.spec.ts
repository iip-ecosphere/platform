import { TestBed } from '@angular/core/testing';

import { DrawflowService } from './drawflow.service';

describe('DrawflowService', () => {
  let service: DrawflowService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(DrawflowService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
