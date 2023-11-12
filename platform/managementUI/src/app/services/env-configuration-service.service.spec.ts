import { TestBed } from '@angular/core/testing';

import { EnvConfigService, Configuration } from './env-config.service';

describe('EnvConfigurationServiceService', () => {
  let service: EnvConfigService;

  beforeAll(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(EnvConfigService);
    return EnvConfigService.initAsync();
  });

  beforeEach(() => {
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should have setup/configuration', () => {
    expect(service.getEnv()).toBeTruthy();

    let cfg : Configuration | undefined = EnvConfigService.getConfig();
    expect(cfg).toBeTruthy();
    expect(cfg?.ip && cfg?.ip?.length > 0).toBeTrue();
    expect(cfg?.urn && cfg?.urn?.length > 0).toBeTrue();
    // cfg.stage currently unclear
  });

});
