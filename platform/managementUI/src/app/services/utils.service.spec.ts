import { TestBed } from '@angular/core/testing';

import { UtilsService, retry } from './utils.service';

describe('UtilsService', () => {
  let service: UtilsService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(UtilsService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should fail 3 times', async() => {
    let count = 0;
    let caught = false;
    let done = false;

    await retry({
      fn: function () {
        count++;
        return false;
      },
      maxAttempts: 3,
      delay: 500,
    }).then(() => done = true)
      .catch(() => caught = true);

    expect(done).toBeFalse();
    expect(caught).toBeTrue();
    expect(count).toEqual(3);
  });

  it('should succeed', async() => {
    let count = 0;
    let caught = false;
    let done = false;

    await retry({
      fn: function () {
        count++;
        return true;
      },
      maxAttempts: 4,
      delay: 500,
    }).then(() => done = true)
      .catch(() => caught = true);

    expect(done).toBeTrue();
    expect(caught).toBeFalse();
    expect(count).toEqual(1);
  });

  it('should succeed second time', async() => {
    let count = 0;
    let caught = false;
    let done = false;

    await retry({
      fn: function () {
        count++;
        return count > 1;
      },
      maxAttempts: 3,
      delay: 500,
    }).then(() => done = true)
      .catch(() => caught = true);

    expect(done).toBeTrue();
    expect(caught).toBeFalse();
    expect(count).toEqual(2);
  });

});
