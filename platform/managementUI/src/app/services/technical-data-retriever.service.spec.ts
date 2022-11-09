import { TestBed } from '@angular/core/testing';

import { TechnicalDataRetrieverService } from './technical-data-retriever.service';

describe('TechnicalDataRetrieverService', () => {
  let service: TechnicalDataRetrieverService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(TechnicalDataRetrieverService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
