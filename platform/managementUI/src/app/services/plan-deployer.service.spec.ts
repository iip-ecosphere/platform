import { TestBed } from '@angular/core/testing';

import { PlanDeployerService } from './plan-deployer.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('PlanDeployerService', () => {
  let service: PlanDeployerService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ HttpClientTestingModule ]
    });  
    service = TestBed.inject(PlanDeployerService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
