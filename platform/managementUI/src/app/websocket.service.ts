import { switchAll, tap } from 'rxjs/operators';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Subject, catchError} from 'rxjs';
import { WebSocketSubject, webSocket } from 'rxjs/webSocket';

@Injectable({
  providedIn: 'root'
})
export class WebsocketService {

  constructor() { }

  public socket: WebSocket = new WebSocket("ws://localhost:10000/status");
  data: any; // todo loe
  messageSubject: Subject<string> = new Subject<string>();

  public connect(url:string): void {
    this.socket = new WebSocket(url);
    this.socket.onopen = () => {
      console.log("Websocket connected with url: "
        + url);
    }

    this.socket.onmessage = (event) => {
      console.log('Received message:', event.data);
      this.messageSubject.next(event.data)
    };
  }

  getMsg() {
    return this.messageSubject;
  }

  public close() {
    console.log('[WebSocket] closing socket');
    this.socket.close()
  }

  // todo loe?
  public test2() {
    const subject = new Subject();
    let temp;
    let temp2 = subject.subscribe()
    console.log("the value of temp: " + temp)
    subject.subscribe({next: (v) => console.log('observerA: ' + v)})
    subject.subscribe({next: (v) => console.log('observerB: ' + v)})
    subject.subscribe({next: (v) => temp=v})

    subject.next(2)
    subject.next(34)
    console.log("the value of temp: " + temp)
    console.log("the value of temp2: ")
    console.log(temp2)
  }

}
