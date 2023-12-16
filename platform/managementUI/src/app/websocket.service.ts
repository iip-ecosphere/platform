import { Injectable } from '@angular/core';
import { Subject, firstValueFrom} from 'rxjs';
import { statusCollection } from 'src/interfaces';
import { ApiService } from './services/api.service';

/**
 * Subscribes to a given websocket. User calles getMsgSubject and subscribes to it.
 */
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
  public emitInfo = true;

  /**
   * Creates a new instance of this service, for local use.
   * 
   * @returns the new instance
   */
  public createInstance(): WebsocketService {
    return new WebsocketService();
  }

  public connect(url:string): void {
    console.debug('[websocketService | connect] url: ' +  url + "#")
    this.socket = new WebSocket(url);
    this.socket.onopen = () => {
      if (this.emitInfo) {
        console.info("Websocket connected with url: " + url);
      }
    }
    this.socket.onmessage = (event) => {
      console.debug('Received message:', event.data);
      this.messageSubject.next(event.data);
    };
  }

  /**
   * Obtains the status URI from the platform and connects to it.
   * 
   * @param api the ApiService to obtain the URI from
   */
  public async connectToStatusUri(api: ApiService) {
    let statusUri = await api.getStatusUri();
    if (statusUri.length > 0) {
      this.connect(statusUri)
    }
  }

  getMsgSubject() {
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
