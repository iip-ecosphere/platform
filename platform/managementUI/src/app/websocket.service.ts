import { Injectable } from '@angular/core';
import { Subject, firstValueFrom} from 'rxjs';
import { statusCollection } from 'src/interfaces';

@Injectable({
  providedIn: 'root'
})
export class WebsocketService {
  http: any;

  constructor() { }

  public socket: WebSocket | undefined;// = new WebSocket("ws://localhost:10000/status");
  data: any;
  //messageSubject: Subject<string> = new Subject<string>();
  messageSubject: Subject<any> = new Subject<any>();


  public connect(url:string): void {
    console.debug('[websocketService | connect] url: ' +  url + "#")
    this.socket = new WebSocket(url);
    this.socket.onopen = () => {
      console.info("Websocket connected with url: " + url);
    }
    this.socket.onmessage = (event) => {
      console.debug('Received message:', event.data);
      this.messageSubject.next(event.data);
    };
  }

  getMsg() {
    return this.messageSubject;
  }

  getJsonMsg() {
    let result = JSON.stringify(this.messageSubject)
    return result
  }

  public close() {
    console.info('[WebSocket] closing socket');
    this.socket?.close()
  }
}
