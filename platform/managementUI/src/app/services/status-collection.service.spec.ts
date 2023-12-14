import { TestBed } from '@angular/core/testing';

import { StatusCollectionService } from './status-collection.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ST_ADDED, ST_CHANGED, ST_ERROR, ST_PROCESS, ST_RESULT } from 'src/interfaces';

describe('StatusCollectionService', () => {
  let service: StatusCollectionService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ HttpClientTestingModule ]
    });  
    service = TestBed.inject(StatusCollectionService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should add and dismiss', () => {
    expect(service.StatusCollection.length).toBe(0);
    service.addReceivedMessage("msg", "1234");
    expect(service.StatusCollection.length).toBe(1);
    service.dismissStatus("1234");
    expect(service.StatusCollection.length).toBe(0);
  });

  it('should handle status messages', () => {
    let success = null;
    service.setFinishedNotifier((s) => {
      success = s;
    });
    expect(service.StatusCollection.length).toBe(0);
    service.receiveStatus({action: ST_ADDED, id:"Cfg", aliasIds:[], componentType:"Cfg", description:"", 
      subDescription:"", deviceId:"", taskId:"1235", progress:-1});
    expect(service.StatusCollection.length).toBe(1);
    
    service.receiveStatus({action: ST_CHANGED, id:"Cfg", aliasIds:[], componentType:"Cfg", description:"", 
      subDescription:"", deviceId:"", taskId:"1236", progress:-1});
    expect(service.StatusCollection.length).toBe(2);
    service.receiveStatus({action: ST_PROCESS, id:"Cfg", aliasIds:[], componentType:"Cfg", description:"", 
      subDescription:"", deviceId:"", taskId:"1236", progress:0});
    expect(service.StatusCollection.length).toBe(2);
    service.receiveStatus({action: ST_PROCESS, id:"Cfg", aliasIds:[], componentType:"Cfg", description:"", 
      subDescription:"", deviceId:"", taskId:"1236", progress:50});
    expect(service.StatusCollection.length).toBe(2);
    service.receiveStatus({action: ST_PROCESS, id:"Cfg", aliasIds:[], componentType:"Cfg", description:"", 
      subDescription:"", deviceId:"", taskId:"1236", progress:100});
    expect(service.StatusCollection.length).toBe(2);
    service.receiveStatus({action: ST_RESULT, id:"Cfg", aliasIds:[], componentType:"Cfg", description:"", 
      subDescription:"", deviceId:"", taskId:"1236", result:"ok", progress:100});
    expect(service.StatusCollection.length).toBe(2);
    expect(success).toBeTrue();
    success = null;
    
    service.receiveStatus({action: ST_ERROR, id:"Cfg", aliasIds:[], componentType:"Cfg", description:"", 
      subDescription:"", deviceId:"", taskId:"1235", result:"failed", progress:-1});
    expect(service.StatusCollection.length).toBe(2);
    expect(success).toBeFalse();
    service.dismissStatus("1235");
    expect(service.StatusCollection.length).toBe(1);
    
    service.triggerDataReloadingAction();
  });

});
