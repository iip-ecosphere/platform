import { TestBed } from '@angular/core/testing';

import { IvmlFormatterService } from './ivml-formatter.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('IvmlFormatterService', () => {
  let service: IvmlFormatterService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ HttpClientTestingModule ]
    });
    service = TestBed.inject(IvmlFormatterService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
