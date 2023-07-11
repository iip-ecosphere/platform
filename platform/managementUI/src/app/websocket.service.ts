import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class WebsocketService {

  constructor() { }

  private socket: WebSocket = new WebSocket("ws://localhost:10000/status");

  public connect(url:string): void {
    this.socket = new WebSocket("ws://localhost:10000/SimpleReceiver/Simple-Mesh-Testing-App/1/stderr");

    this.socket.onopen = () => {
      console.log("Websocket connected with url: "
        + url);
    }

    this.socket.onmessage = (event) => {
      console.log('Received message:', event.data);
    };

    this.socket.onclose = () => {
      console.log('WebSocket disconnected');
    };
  }
}
