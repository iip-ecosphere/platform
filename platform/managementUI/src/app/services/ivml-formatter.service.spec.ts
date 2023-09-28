import { TestBed } from '@angular/core/testing';

import { IvmlFormatterService } from './ivml-formatter.service';

describe('IvmlFormatterService', () => {
  let service: IvmlFormatterService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(IvmlFormatterService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
