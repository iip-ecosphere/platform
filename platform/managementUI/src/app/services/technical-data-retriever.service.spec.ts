import { TestBed } from '@angular/core/testing';

import { TechnicalDataRetrieverService } from './technical-data-retriever.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { MatIconModule } from '@angular/material/icon';

describe('TechnicalDataRetrieverService', () => {
  let service: TechnicalDataRetrieverService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ HttpClientTestingModule, MatIconModule ]
    });  
    service = TestBed.inject(TechnicalDataRetrieverService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
