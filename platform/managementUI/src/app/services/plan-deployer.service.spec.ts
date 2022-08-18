import { TestBed } from '@angular/core/testing';

import { PlanDeployerService } from './plan-deployer.service';

describe('PlanDeployerService', () => {
  let service: PlanDeployerService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(PlanDeployerService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
