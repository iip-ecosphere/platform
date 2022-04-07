import { HttpHandler, HttpInterceptor, HttpRequest } from "@angular/common/http";

export class Interceptor implements HttpInterceptor{

  intercept(request: HttpRequest<any>, next: HttpHandler) {
    const newReq = request.clone({
      headers: request.headers.set('Access-Control-Allow-Origin', '*')
      .set('Content-Type', 'application/json')
    })


    return next.handle(request);
  }

}
