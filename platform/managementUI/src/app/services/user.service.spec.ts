import { TestBed } from '@angular/core/testing';
import { UserService } from './user.service';
import { HttpHeaders } from '@angular/common/http';

describe('UserService', () => {

  let service: UserService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(UserService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should run through uninitialized operations', () => {
    service.clear();
    let headers = new HttpHeaders({});
    service.injectTo(headers);
    expect(headers.keys()).toHaveSize(0);    
    service.clear(); // just to be safe
  })

  it('should run through initialized user/password operations', () => {
    service.setup("user", "password");
    let headers = new HttpHeaders({});
    headers = service.injectTo(headers);
    expect(headers.get("Authorization")).toBeTruthy();
    service.clear();
  })

});