import { TestBed } from '@angular/core/testing';

import { PlanDeployerService } from './plan-deployer.service';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { MatIconModule } from '@angular/material/icon';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';

describe('PlanDeployerService', () => {
  let service: PlanDeployerService;

  beforeEach(() => {
    TestBed.configureTestingModule({
    imports: [MatIconModule],
    providers: [provideHttpClient(withInterceptorsFromDi()), provideHttpClientTesting()]
});  
    service = TestBed.inject(PlanDeployerService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
