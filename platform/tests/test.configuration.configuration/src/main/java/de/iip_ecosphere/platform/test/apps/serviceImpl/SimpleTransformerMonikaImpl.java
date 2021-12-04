package de.iip_ecosphere.platform.test.apps.serviceImpl;

import java.io.InputStream;

import de.iip_ecosphere.platform.services.environment.DataIngestor;
import de.iip_ecosphere.platform.services.environment.DefaultServiceImpl;
import de.iip_ecosphere.platform.services.environment.ServiceKind;
import iip.datatypes.Rec13;
import iip.interfaces.SimpleDataTransformer3Service;

public class SimpleTransformerMonikaImpl extends DefaultServiceImpl implements SimpleDataTransformer3Service{
	
	 private DataIngestor<Rec13> ingestor;
	    
	    /**
	     * Fallback constructor.
	     */
	    public SimpleTransformerMonikaImpl() {
	        super(ServiceKind.TRANSFORMATION_SERVICE);
	    }
	    
	    /**
	     * Creates a service instance from a service id and a YAML artifact.
	     * 
	     * @param serviceId the service id
	     * @param ymlFile the YML file containing the YAML artifact with the service descriptor
	     */
	    public SimpleTransformerMonikaImpl(String serviceId, InputStream ymlFile) {
	        super(serviceId, ymlFile);
	    }
	    
	    /**
	    * Called when data arrived that shall be processed (synchronously).
	    *
	    * @param data the arrived data
	    * @return the transformation result, <b>null</b> for no data
	    */
	    public Rec13 transformRec13Rec13(Rec13 data) {
	        Rec13 result = new Rec13();
	        result.setIntField(data.getIntField());
	        result.setStringField(data.getStringField() + " SyncT");
	        return result;
	    }

	    // no override here as methods are alternatives for sync/async interface

	    /**
	    * Called when data arrived that shall be processed (asynchronously).
	    *
	    * @param data the arrived data 
	    */
	    public void processRec13(Rec13 data) {
	        if (null != ingestor) {
	            Rec13 result = new Rec13();
	            result.setIntField(data.getIntField());
	            result.setStringField(data.getStringField() + " ASyncT");
	            ingestor.ingest(result);
	        }
	    }

	    /**
	     * Called by the platform to attach an asynchronous data ingestor for type "Rec13".
	     *
	     * @param ingestor the "Rec13" ingestor instance
	     */
	    public void attachprocessRec13_SimpleTransformerIngestor(DataIngestor<Rec13> ingestor) {
	        this.ingestor = ingestor; 
	    }
	
}
