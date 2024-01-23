import { TestBed } from '@angular/core/testing';

import { PlanDeployerService } from './plan-deployer.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { MatIconModule } from '@angular/material/icon';

describe('PlanDeployerService', () => {
  let service: PlanDeployerService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ HttpClientTestingModule, MatIconModule ] // jasmine complains about
    });  
    service = TestBed.inject(PlanDeployerService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
