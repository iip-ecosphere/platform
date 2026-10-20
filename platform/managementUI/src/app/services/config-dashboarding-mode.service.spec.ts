import { TestBed } from '@angular/core/testing';

import { ConfigDashboardingModeService } from './config-dashboarding-mode.service';

describe('ConfigDashboardingModeService', () => {
  let service: ConfigDashboardingModeService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ConfigDashboardingModeService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
