import { TestBed } from '@angular/core/testing';

import { TechnicalDataRetrieverService } from './technical-data-retriever.service';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { MatIconModule } from '@angular/material/icon';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';

describe('TechnicalDataRetrieverService', () => {
  let service: TechnicalDataRetrieverService;

  beforeEach(() => {
    TestBed.configureTestingModule({
    imports: [MatIconModule],
    providers: [provideHttpClient(withInterceptorsFromDi()), provideHttpClientTesting()]
});  
    service = TestBed.inject(TechnicalDataRetrieverService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
