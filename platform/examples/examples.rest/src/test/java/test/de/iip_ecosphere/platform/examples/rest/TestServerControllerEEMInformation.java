package test.de.iip_ecosphere.platform.examples.rest;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.iip_ecosphere.platform.examples.rest.TestServerResponseInformationRestType;
import de.iip_ecosphere.platform.examples.rest.TestServerResponseInformationInfoItem;
import de.iip_ecosphere.platform.examples.rest.TestServerResponseInformationRootItem;


@RestController
@RequestMapping("TestServerEEM/api/information")
public class TestServerControllerEEMInformation {

    private TestServerResponseInformationRestType information;
    
    /**
     * Constructor.
     */
    public TestServerControllerEEMInformation() {
        information = new TestServerResponseInformationRestType();
        information.setContext("/api/v1/information");
        information.setTimestamp(getCurrentTimestamp());
        
        TestServerResponseInformationRootItem root1 = new TestServerResponseInformationRootItem();
        root1.setHref("/api/v1/information");
        root1.setDescription("Device information");
        
        TestServerResponseInformationRootItem root2 = new TestServerResponseInformationRootItem();
        root2.setHref("/api/v1/measurements");
        root2.setDescription("Instantaneous values");
        
        TestServerResponseInformationRootItem root3 = new TestServerResponseInformationRootItem();
        root3.setHref("/api/v1/meters");
        root3.setDescription("Meter readings");
        
        TestServerResponseInformationRootItem root4 = new TestServerResponseInformationRootItem();
        root4.setHref("/api/v1/measurement-system-control");
        root4.setDescription("Measurement control");
        
        TestServerResponseInformationRootItem root5 = new TestServerResponseInformationRootItem();
        root5.setHref("/api/v1/history");
        root5.setDescription("Historical data access");
        
        TestServerResponseInformationRootItem[] rootItems = new TestServerResponseInformationRootItem[5];
        rootItems[0] = root1;
        rootItems[1] = root2;
        rootItems[2] = root3;
        rootItems[3] = root4;
        rootItems[4] = root5;
        
        
        
        TestServerResponseInformationInfoItem info1 = new TestServerResponseInformationInfoItem();
        info1.setHref("/api/v1/information/name");
        info1.setId("name");
        info1.setName("Device");
        info1.setValue("EEM-MA370");
        info1.setDescription("Device identifier");
        
        TestServerResponseInformationInfoItem info2 = new TestServerResponseInformationInfoItem();
        info2.setHref("/api/v1/information/hw");
        info2.setId("hw");
        info2.setName("Hardware version");
        info2.setValue("2.0");
        info2.setDescription("Hardware revision number");
        
        TestServerResponseInformationInfoItem info3 = new TestServerResponseInformationInfoItem();
        info3.setHref("/api/v1/information/fw");
        info3.setId("fw");
        info3.setName("Firmware version");
        info3.setValue("1.5.1");
        info3.setDescription("Firmware revision number");
        
        TestServerResponseInformationInfoItem info4 = new TestServerResponseInformationInfoItem();
        info4.setHref("/api/v1/information/serial-number");
        info4.setId("serial-number");
        info4.setName("Serial number");
        info4.setValue("1361394185");
        info4.setDescription("Serial number");
        
        TestServerResponseInformationInfoItem info5 = new TestServerResponseInformationInfoItem();
        info5.setHref("/api/v1/information/label");
        info5.setId("label");
        info5.setName("Device label");
        info5.setValue("Individual device label");
        info5.setDescription("Device label");
        
        TestServerResponseInformationInfoItem[] infoItems = new TestServerResponseInformationInfoItem[5];
        infoItems[0] = info1;
        infoItems[1] = info2;
        infoItems[2] = info3;
        infoItems[3] = info4;
        infoItems[4] = info5;
        
        information.setRootItems(rootItems);
        information.setInfoItems(infoItems);
    }
    
    /**
     * Getter for information.
     * 
     * @return information
     */
    @GetMapping()
    public TestServerResponseInformationRestType getInformation() {
        
        return information;
    }
    
    /**
     * Creates and returns a currentTimestamp.
     * 
     * @return currentTimestamp in format yyyy-MM-dd'T'HH:mm:ss'Z'
     */
    private String getCurrentTimestamp() {
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        String formattedTimestamp = format.format(date);
        return formattedTimestamp;
    }
}
