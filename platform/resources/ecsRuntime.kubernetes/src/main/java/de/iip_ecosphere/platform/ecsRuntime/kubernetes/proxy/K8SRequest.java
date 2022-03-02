package de.iip_ecosphere.platform.ecsRuntime.kubernetes.proxy;

import java.util.Base64;
import java.util.Map;

/**
 * Details of the K8S (Kubernetes) request.
 * 
 * @author Ahmad Alamoush, SSE
 */
public class K8SRequest {

    private byte[] requestByte;
    private String method;
    private String path;
    private String protocol;

    private byte[] payload;
    private Map<String, String[]> headers;

    /**
     * Creates a K8S Request instance.
     * 
     * 
     */
    public K8SRequest() {
    }

    /**
     * Returns the request as bytes.
     * 
     * @return the request as bytes
     */
    public byte[] getRequestByte() {
        return requestByte;
    }
    
    /**
     * Set the request as bytes.
     *
     * @param requestByte the request as bytes
     */
    public void setRequestByte(byte[] requestByte) {
        this.requestByte = requestByte;
    }
    
    /**
     * Returns the method of the request.
     * 
     * @return the method of the request
     */
    public String getMethod() {
        return method;
    }
    
    /**
     * Set the method of the request.
     *
     * @param method the method of the request
     */
    public void setMethod(String method) {
        this.method = method;
    }

    /**
     * Returns the path of the request.
     * 
     * @return the path of the request
     */
    public String getPath() {
        return path;
    }

    /**
     * Set the path of the request.
     *
     * @param path the path of the request
     */
    public void setPath(String path) {
        this.path = path;
    }
    
    /**
     * Returns the protocol of the request.
     * 
     * @return the protocol of the request
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * Set the protocol of the request.
     *
     * @param protocol the protocol of the request
     */
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    /**
     * Returns the payload of the request.
     * 
     * @return the payload of the request
     */
    public byte[] getPayload() {
        return payload;
    }

    /**
     * Set the payload of the request.
     *
     * @param payload the payload of the request
     */
    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    /**
     * Returns the headers of the request.
     * 
     * @return the headers of the request
     */
    public Map<String, String[]> getHeaders() {
        return headers;
    }

    /**
     * Set the headers of the request.
     *
     * @param headers the headers of the request
     */
    public void setHeaders(Map<String, String[]> headers) {
        this.headers = headers;
    }
    
//    /**
//     * convert the request to String.
//     *
//     * @return the request as String
//     */
//    public String convertToString() {
//        
//        String requestString = getMethod() + " " + getPath() + " " + getProtocol() + "\r\n";
//        
//        for (String[] header : getHeaders().values()) {
//            requestString = requestString + header[0] + ": " + header[1] + "\r\n";
//        }
//        
//        if (!getMethod().equals("GET")) {
//            StringBuilder payloadText = new StringBuilder();
//            for (byte b : payload) {
//                payloadText.append(b).append(" ");
//            }
//            requestString = requestString + "\r\n" + payloadText.toString();
//        }
//        
//        return requestString;
//    }
    
//    /**
//     * use the string request to fill method, path, protocol, headers, and payload.
//     *
//     * @param requestString the request as String
//     */
//    public void convertStringToRequest(String requestString) {
//        
//        int requestLength = 0;
//        Map<String, String[]> requestHeaders = new HashMap<String, String[]>();
//        
//        String[] requestLines = requestString.split("\r\n");
//        String[] firstLine = requestLines[0].split(" ");
//        
//        setMethod(firstLine[0]);
//        setPath(firstLine[1]);
//        setProtocol(firstLine[2]);
//        
//        for (int i = 1; i < requestLines.length - 2; i++) {
//            if (requestLines[i].toUpperCase().contains("CONTENT-LENGTH")) {
//                requestLength = Integer.parseInt(requestLines[i].substring(16));
//            }
//            String key = requestLines[i].substring(0, requestLines[i].indexOf(":"));
//            String[] header = {key, requestLines[i].substring(requestLines[i].indexOf(":") + 2)};
//
//            requestHeaders.put(key.toUpperCase(), header);
//        }
//        
//        setHeaders(requestHeaders);
//        
//        if (!getMethod().equals("GET")) {
//            byte[] requestPayload = new byte[requestLength];
//            int count = 0;
//            for (String byteString : requestLines[requestLines.length - 1].toString().split(" ")) {
//                Integer byteInt = Integer.parseInt(byteString);
//                requestPayload[count] = byteInt.byteValue();
//                count++;
//            }
//            
//            setPayload(requestPayload);
//        }
//        
//    }

    /**
     * convert the request array of byte to base64 String.
     *
     * @return the request as base64 String
     */
    public String convertToBase64String() {
        
        String requestBase64String = Base64.getEncoder().encodeToString(requestByte);
        
        return requestBase64String;
    }
    
    /**
     * convert the request from base64 String to array of bytes.
     *
     * @param requestBase64String the request as base64 String
     *
     * @return requestString the request as array of bytes
     */
    public byte[] convertBase64StringToByte(String requestBase64String) {
        
        byte[] newRequestByte = Base64.getDecoder().decode(requestBase64String);        
        
        return newRequestByte;
        
    }
    
    /**
     * convert the request to base64 String with ID.
     *
     * @return the request as base64 String with ID
     */
    public String convertToBase64StringWithID() {
        
        String requestID = "requestID:" + getPathNoParameter() + System.nanoTime() + "\r\n";
        requestID = Base64.getEncoder().encodeToString(requestID.getBytes());
        
        String requestBase64String = Base64.getEncoder().encodeToString(requestByte);
        
        return requestID + "*" + requestBase64String;
    }    
    
    /**
     * convert the String from base64 String to request as String.
     *
     * @param requestBase64String the request as base64 String
     *
     * @return textString the request as string
     */
    public String convertBase64StringToString(String requestBase64String) {
        
        String textString = new String(Base64.getDecoder().decode(requestBase64String));        
        
        return textString;
        
    }
    
    /**
     * convert the array of bytes to Base64 String.
     *
     * @param byteArray the request as array of bytes
     *
     * @return byteArrayBase64String the request as base64 String
     */
    public String convertByteArrayToBase64String(byte[] byteArray) {
        String byteArrayBase64String = Base64.getEncoder().encodeToString(byteArray);
        
        return byteArrayBase64String;
    }
    
    /**
     * get the path without parameters.
     *
     * @return requestString the request as array of bytes
     */
    public String getPathNoParameter() {
        
        if (path.indexOf("?") == -1) {
            return path.replace("/", ""); 
        } else {
            return (path.contains("&watch=true") ? "Watch" : "")
                    + path.substring(0, path.indexOf("?")).replace("/", "");
        }
        
    }
}
