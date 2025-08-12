import { inject } from '@angular/core';
import { HttpHandler, HttpHeaders, HttpInterceptor, HttpRequest } from "@angular/common/http";
import { UserService } from "./services/user.service";

export class Interceptor implements HttpInterceptor {

  intercept(request: HttpRequest<any>, next: HttpHandler) {
    const user = inject(UserService);
    const newReq = request.clone({
      headers: this.setupHeaders(request.headers) 
    })
    return next.handle(newReq);
  }

  private setupHeaders(headers: HttpHeaders) : HttpHeaders {
    const user = inject(UserService);
    headers = headers
      //.set('Access-Control-Allow-Origin', '*') // fails since mid 2025 on browsers, requires specific server policy
      .set('Content-Type', 'application/json');
    return user.injectTo(headers);
  }

}
