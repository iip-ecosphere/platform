package test.de.iip_ecosphere.platform.ecsRuntime.kubernetes;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;

import org.junit.Test;

import de.iip_ecosphere.platform.ecsRuntime.kubernetes.proxy.HttpK8SJavaProxy;
import de.iip_ecosphere.platform.ecsRuntime.kubernetes.proxy.K8SJavaProxy;
import de.iip_ecosphere.platform.ecsRuntime.kubernetes.proxy.K8SRequest;
import de.iip_ecosphere.platform.ecsRuntime.kubernetes.proxy.ProxyType;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.TimeUtils;

public class ServerHttpJavaK8SProxy {

    private static int localPort = 4411;
    private static String serverIP = "192.168.81.208";
    private static String serverPort = "6443";
    private static boolean tlsCheck = false;
    private static ArrayList<ServerSocket> serverSocketList = new ArrayList<ServerSocket>();
    
    /**
     * Returns the port on localhost to receive new requests.
     * 
     * @return the port on localhost to receive new requests
     */
    public static int getLocalPort() {
        return localPort;
    }

    /**
     * Set the port on localhost to receive new requests.
     *
     * @param localPort the port on localhost to receive new requests
     */
    public static void setLocalPort(int localPort) {
        ServerHttpJavaK8SProxy.localPort = localPort;
    }

    /**
     * Returns the IP Address of the server (either the MasterProxy or K8S
     * apiserver).
     * 
     * @return the IP Address of the server (either the MasterProxy or K8S
     *         apiserver)
     */
    public static String getServerIP() {
        return serverIP;
    }

    /**
     * Set the IP Address of the server (either the MasterProxy or K8S apiserver).
     *
     * @param serverIP the IP Address of the server (either the MasterProxy or K8S
     *                 apiserver)
     */
    public static void setServerIP(String serverIP) {
        ServerHttpJavaK8SProxy.serverIP = serverIP;
    }

    /**
     * Returns the port of the server (either the MasterProxy or K8S apiserver).
     * 
     * @return the port of the server (either the MasterProxy or K8S apiserver)
     */
    public static String getServerPort() {
        return serverPort;
    }

    /**
     * Set the port of the server (either the MasterProxy or K8S apiserver).
     *
     * @param serverPort the port of the server (either the MasterProxy or K8S
     *                   apiserver).
     */
    public static void setServerPort(String serverPort) {
        ServerHttpJavaK8SProxy.serverPort = serverPort;
    }

    /**
     * The main method to run the server proxy.
     * 
     * @param args the main method arguments
     * 
     */
    public static void main(String[] args) {
        
        Thread requestThread = new Thread() { 
            public void run() {
                tlsCheck = Boolean.valueOf(System.getProperty("tlsCheck"));

                try {
                    K8SJavaProxy httpJavaK8SProxy = new HttpK8SJavaProxy(ProxyType.MasterProxy, serverIP, serverPort,
                            tlsCheck);

                    startMultiThreaded(httpJavaK8SProxy, localPort);
                } catch (UnrecoverableKeyException | KeyManagementException | NoSuchAlgorithmException
                        | KeyStoreException | CertificateException | InvalidKeySpecException | IOException e) {
                    System.err.println("Exception in the starting the multi-threads method");
                    e.printStackTrace();
                } 
            }
        };
        requestThread.start();
        
        System.out.println("Waiting");
        while (true) {
            if (new File("/tmp/EndServerRun.k8s").exists()) {
                try {
                    serverSocketList.get(0).close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
            TimeUtils.sleep(1);
        }
    }

//    /**
//     * The main method to run the test server proxy.
//     * 
//     */
//    @Test(timeout = 3 * 60 * 60 * 1000)
//    public void mainTest() {
//        
//        Thread requestThread = new Thread() {
//            public void run() {
//                tlsCheck = Boolean.valueOf(System.getProperty("tlsCheck"));
//
//                try {
//                    K8SJavaProxy httpJavaK8SProxy = new HttpK8SJavaProxy(ProxyType.MasterProxy, serverIP, serverPort,
//                            tlsCheck);
//
//                    startMultiThreaded(httpJavaK8SProxy, localPort);
//                } catch (UnrecoverableKeyException | KeyManagementException | NoSuchAlgorithmException
//                        | KeyStoreException | CertificateException | InvalidKeySpecException | IOException e) {
//                    System.err.println("Exception in the starting the multi-threads method");
//                    e.printStackTrace();
//                } 
//            }
//        };
//        requestThread.start();
//        
//        System.out.println("Waiting");
//        while (true) {
//            if (new File("/tmp/EndServerRun.k8s").exists()) {
//                break;
//            }
//            TimeUtils.sleep(1);
//        }
//    }
    
    /**
     * Start multi-threads method to receive and process requests.
     * 
     * @param httpJavaK8SProxy the proxy used to receive the new requests
     * @param localPort        is the port on the localhost to receive the new
     *                         requests
     * 
     * @throws IOException
     * @throws InvalidKeySpecException
     * @throws CertificateException
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     * @throws UnrecoverableKeyException
     * 
     */
    public static void startMultiThreaded(final K8SJavaProxy httpJavaK8SProxy, int localPort)
            throws UnrecoverableKeyException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException,
            CertificateException, InvalidKeySpecException, IOException {

        if (new File("/tmp/EndServerRun.k8s").exists()) {
            return;
        }
        ServerSocket serverSocket = httpJavaK8SProxy.getServerSocket(localPort, null, null, null, tlsCheck);
        serverSocketList.add(serverSocket);
        
        System.out.println("Started multi-threaded server at localhost port " + localPort);

        final Charset encoding = StandardCharsets.UTF_8;
        
        File file = new File("ServerReady.k8s"); 
        file.createNewFile();
        
        while (true) {
            
            final Socket socket = serverSocket.accept();
//            System.out.println("Accept socket");

            Thread requestThread = new Thread() {
                public void run() {
                    InputStream reader = null;
//                  BufferedWriter writer = null;
                    BufferedOutputStream writer = null;
                    byte[] requestByte = null;
                  
                    try {
                        while (true) {
                            reader = socket.getInputStream();
                            writer = new BufferedOutputStream(socket.getOutputStream());
                            requestByte = httpJavaK8SProxy.extractK8SRequestByte(reader);
                            
                            if (requestByte != null) {
                                K8SRequest request = httpJavaK8SProxy.createK8SRequest(requestByte);
                                byte[] responseString = httpJavaK8SProxy.sendK8SRequest(writer, request);
                                writer.write(responseString);
                                writer.flush();
                            } else {
                                break;
                            }
                            
                        }
                        
                    } catch (SocketException e) {
                        if (e.getMessage().contentEquals("Socket input is already shutdown")) {
                            System.out.println(e.getMessage());
                        } else {
                            System.err.println("SocketException while creating response");
//                            System.out.println(new String(requestByte)); 
                            e.printStackTrace();
                        }
                    } catch (IOException | KeyManagementException | NoSuchAlgorithmException | KeyStoreException
                            | CertificateException e) {
                        System.err.println("Exception while creating response");
                        e.printStackTrace();
                        System.out.println("socket thread ends Throwable");
                    } finally {
                        try {
                            writer.close();
                            reader.close();
                        } catch (IOException e) {
                            System.err.println("Could not close the streams");
                            e.printStackTrace();
                        }
                    }
                }
            };
            requestThread.start();
        }
    }
}