import { TestBed } from '@angular/core/testing';

import { ConfigExpertModeService } from './config-expert-mode.service';

describe('ConfigExportModeService', () => {
  let service: ConfigExpertModeService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ConfigExpertModeService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
