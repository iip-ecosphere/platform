package test.de.iip_ecosphere.platform.ecsRuntime.kubernetes;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Test;

import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.TimeUtils;

public class MasterAasJavaK8SProxy {

    private static int vabPort = 7711;
    private static int aasPort = 8811;
    private static String serverIP = "192.168.81.212";
    private static String serverPort = "6443";
    private static boolean tlsCheck = false;

    /**
     * Returns the IP Address of the server.
     * 
     * @return the IP Address of the server
     */
    public static String getServerIP() {
        return serverIP;
    }

    /**
     * Set the IP Address of the server.
     * 
     * @param serverIP the IP Address of the server
     */
    public static void setServerIP(String serverIP) {
        MasterAasJavaK8SProxy.serverIP = serverIP;
    }

    /**
     * Returns the port of the server (either the Aas port or K8S apiserver port).
     * 
     * @return the port of the server (either the Aas port or K8S apiserver port)
     */
    public static String getServerPort() {
        return serverPort;
    }

    /**
     * Set the port of the server (either the Aas port or K8S apiserver port).
     *
     * @param serverPort the port of the server (either the Aas port or K8S apiserver port)
     */
    public static void setServerPort(String serverPort) {
        MasterAasJavaK8SProxy.serverPort = serverPort;
    }

    /**
     * Returns the vab port.
     * 
     * @return the vab port
     */
    public int getVabPort() {
        return vabPort;
    }

    /**
     * Set the vab port.
     *
     * @param vabPort the vab port
     */
    public void setVabPort(int vabPort) {
        this.vabPort = vabPort;
    }

    /**
     * Returns the aas port.
     * 
     * @return the aas port
     */
    public int getAasPort() {
        return aasPort;
    }

    /**
     * Set the aas port.
     *
     * @param aasPort the aas port
     */
    public void setAasPort(int aasPort) {
        this.aasPort = aasPort;
    }
    
    /**
     * The main method to run the server proxy.
     * 
     * @param args the main method arguments
     * 
     */
    public static void main(String[] args) {
                
        MasterK8SAas aas = new MasterK8SAas(serverIP, serverPort, vabPort, aasPort, tlsCheck);
        
        try {
            if (tlsCheck) {
                ArrayList<Server> servers = aas.startLocalTLSAas();
            } else {
                ArrayList<Server> servers = aas.startLocalAas();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }

    /**
     * The main method to run the test server proxy.
     * 
     */
    @Test(timeout = 120 * 1000)
    public void mainTest() {
        tlsCheck = Boolean.valueOf(System.getProperty("tlsCheck"));

        MasterK8SAas aas = new MasterK8SAas(serverIP, serverPort, vabPort, aasPort, tlsCheck);
        
        try {
            if (tlsCheck) {
                ArrayList<Server> servers = aas.startLocalTLSAas();
            } else {
                ArrayList<Server> servers = aas.startLocalAas();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        while (true) {
            TimeUtils.sleep(1);
        }
    }
//    /**
//     * Start multi-threads method to receive and process requests.
//     * 
//     * @param aasK8SJavaProxy  the proxy used to receive the new requests
//     * @param localPort        is the port on the localhost to receive the new
//     *                         requests
//     * 
//     * @throws IOException
//     * @throws InvalidKeySpecException
//     * @throws CertificateException
//     * @throws KeyStoreException
//     * @throws NoSuchAlgorithmException
//     * @throws KeyManagementException
//     * @throws UnrecoverableKeyException
//     * 
//     */
//    public static void startMultiThreaded(final K8SJavaProxy aasK8SJavaProxy, int localPort)
//            throws UnrecoverableKeyException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException,
//            CertificateException, InvalidKeySpecException, IOException {
//
//        ServerSocket serverSocket = aasK8SJavaProxy.getServerSocket(localPort, null, null, null);
//
//        System.out.println("Started multi-threaded server at localhost port " + localPort);
//
//        final Charset encoding = StandardCharsets.UTF_8;
//
//        while (true) {
//            final Socket socket = serverSocket.accept();
//            System.out.println("Accept socket");
//
//            Thread requestThread = new Thread() {
//                public void run() {
//                    InputStream reader = null;
//                    BufferedWriter writer = null;
//
//                    try {
//                        reader = socket.getInputStream();
//
//                      writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), encoding.name()));
//
//                        byte[] requestByte = aasK8SJavaProxy.extractK8SRequestByte(reader);
//
//                        if (requestByte.length == 0) {
//                            return;
//                        }
//
//                        K8SRequest request = aasK8SJavaProxy.createK8SRequest(requestByte);
//
//                        String responseString = aasK8SJavaProxy.sendK8SRequest(request);
//
//                        writer.write(responseString);
//
//                        writer.flush();
//                        System.out.println("socket thread ends normal");
//                    } catch (IOException e) {
//                        System.err.println("Exception while creating response");
//                        e.printStackTrace();
//                        System.out.println("socket thread ends Throwable");
//                    } finally {
//                        try {
//                            writer.close();
//                            reader.close();
//                            socket.close();
//                        } catch (IOException e) {
//                            System.err.println("Could not close the streams");
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            };
//            requestThread.start();
//        }
//    }
}