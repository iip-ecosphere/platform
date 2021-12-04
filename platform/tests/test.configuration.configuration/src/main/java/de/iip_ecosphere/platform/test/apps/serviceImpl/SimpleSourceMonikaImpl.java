package de.iip_ecosphere.platform.test.apps.serviceImpl;

import java.io.InputStream;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import de.iip_ecosphere.platform.services.environment.DataIngestor;
import de.iip_ecosphere.platform.services.environment.DefaultServiceImpl;
import de.iip_ecosphere.platform.services.environment.ServiceKind;
import iip.datatypes.Rec1;
import iip.interfaces.SimpleDataSourceService;

public class SimpleSourceMonikaImpl extends DefaultServiceImpl implements SimpleDataSourceService {
	// Ein verzweifeltes Versuch ein TestApp zu bauen. 
	
	private Timer timer = new Timer();
    private Random random = new Random();
    
    /**
     * Fallback constructor.
     */
    public SimpleSourceMonikaImpl() {
        super(ServiceKind.SOURCE_SERVICE);
    }
    
    /**
     * Creates a service instance from a service id and a YAML artifact.
     * 
     * @param serviceId the service id
     * @param ymlFile the YML file containing the YAML artifact with the service descriptor
     */
    public SimpleSourceMonikaImpl(String serviceId, InputStream ymlFile) {
        super(serviceId, ymlFile);
    }

    // no override here as createRec1 and attach... are alternatives
    
    /**
    * Creates data to be ingested.
    *
    * @return the created data, <b>null</b> for no data
    */
    public Rec1 createRec1() {
        Rec1 rec = new Rec1();
        rec.setIntField(random.nextInt());
        rec.setStringField("SYNC");
        return rec;
    }

    /**
     * Called by the platform to attach an asynchronous data ingestor for type "Rec1".
     *
     * @param ingestor the "Rec1" ingestor instance
     */
    public void attachcreateRec1_SimpleSourceIngestor(final DataIngestor<Rec1> ingestor) {
        if (null != ingestor) {
            timer.schedule(new TimerTask() {
                
                @Override
                public void run() {
                    Rec1 rec = new Rec1();
                    rec.setIntField(random.nextInt());
                    rec.setStringField("ASYNC");
                    ingestor.ingest(rec);
                }
            }, 0, 1000);
        }
    }
}
